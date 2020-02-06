package com.nowandfuture.mod.handler;

import com.nowandfuture.asm.IRender;
import com.nowandfuture.asm.RenderHook;
import com.nowandfuture.asm.Utils;
import com.nowandfuture.mod.core.client.renders.ModuleRenderManager;
import com.nowandfuture.mod.core.client.renders.TransformedBlockRenderMap;
import com.nowandfuture.mod.core.client.renders.tiles.VideoRenderer;
import com.nowandfuture.mod.core.common.entities.TileEntitySimplePlayer;
import com.nowandfuture.mod.utils.MathHelper;
import com.nowandfuture.mod.utils.SyncTasks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.*;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class RenderHandler {
    private static final Deque<IRender> renderModules = new LinkedList<>();
    private static final Map<BlockPos,Integer> scores = new LinkedHashMap<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void handleHighLightRender(DrawBlockHighlightEvent event){
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void handleWorldRender(RenderWorldLastEvent renderWorldLastEvent) {

        RenderHook.forceClear();
        while (!renderModules.isEmpty()){
            IRender iRender = renderModules.poll();
            RenderHook.offer(iRender);
        }

//        long time = System.currentTimeMillis();

        Entity entity = Minecraft.getMinecraft().player;
        final double distance = VideoRenderer.LookDistance;
        final Vec3d start = entity.getPositionEyes(renderWorldLastEvent.getPartialTicks());
        final Vec3d vec3d1 = entity.getLook(renderWorldLastEvent.getPartialTicks());
        final Vec3d end = start.addVector(vec3d1.x * distance,
                vec3d1.y * distance, vec3d1.z * distance);

        scores.clear();
        List<ScreenFocusInfo> list = new LinkedList<>();
        final float[] sum = {0};
        Minecraft.getMinecraft().world.loadedTileEntityList.forEach(new Consumer<TileEntity>() {
            @Override
            public void accept(TileEntity tileEntity) {
                if(tileEntity instanceof TileEntitySimplePlayer){
                    RayTraceResult rayTraceResult =
                            tileEntity.getRenderBoundingBox().calculateIntercept(start,end);
                    if(rayTraceResult != null){
                        float ls = (float) rayTraceResult.hitVec.subtract(start).lengthSquared();
                        list.add(new ScreenFocusInfo(tileEntity.getPos(),true,ls));
                    }else{
                        double distance1 = tileEntity.getRenderBoundingBox().getCenter().distanceTo(start);
                        distance1 = 1 - MathHelper.fastSigmoid(distance1);
                        list.add(new ScreenFocusInfo(tileEntity.getPos(),false, (float) distance1));
                        sum[0] += distance1;
                    }
                }
            }
        });

        list.sort(new Comparator<ScreenFocusInfo>() {
            @Override
            public int compare(ScreenFocusInfo o1, ScreenFocusInfo o2) {
                if(o1.project && !o2.project) return -1;
                if(!o1.project && o2.project) return 1;
                else{
                    return o1.distance - o2.distance > 0 ? 1 : -1;
                }
            }
        });

        list.forEach(new Consumer<ScreenFocusInfo>() {
            int s = 128;

            @Override
            public void accept(ScreenFocusInfo screenFocusInfo) {
                if(screenFocusInfo.project){
                    scores.put(screenFocusInfo.pos,s);
                    if(s > 16){
                        s >>= 1;
                    }
                }else{
                    scores.put(screenFocusInfo.pos, ((int) (screenFocusInfo.distance / sum[0] * s)));
                }
            }
        });

//        System.out.println("delay:" + (System.currentTimeMillis() - time));
    }


    public static Map<BlockPos,Integer> getScores(){
        return scores;
    }


    public static class ScreenFocusInfo{
        boolean project;
        float distance;
        BlockPos pos;

        public ScreenFocusInfo(BlockPos pos){
            this.project = false;
            this.distance = Integer.MAX_VALUE;
            this.pos = pos;
        }

        public ScreenFocusInfo(BlockPos pos,boolean isProject,float distance){
            this.project = isProject;
            this.distance = distance;
            this.pos = pos;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ScreenFocusInfo that = (ScreenFocusInfo) o;
            return pos.equals(that.pos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos);
        }
    }

    public static Deque<IRender> getRenderModules() {
        return renderModules;
    }

    public static void addRenderer(IRender renderer){
        renderModules.addFirst(renderer);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void handleUnloadWorld(WorldEvent.Unload unload){
        if(unload.getWorld().isRemote) {
            TransformedBlockRenderMap.INSTANCE.clear();
            renderModules.clear();
            Utils.mapCache = null;
            ModuleRenderManager.INSTANCE.stopAll();

            SyncTasks.INSTANCE.showdownNow();
            SyncTasks.INSTANCE.init();
            unload.getWorld().loadedTileEntityList.forEach(new Consumer<TileEntity>() {
                @Override
                public void accept(TileEntity tileEntity) {
                    if(tileEntity instanceof TileEntitySimplePlayer){
                        try {
                            ((TileEntitySimplePlayer) tileEntity).getSimplePlayer().end();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

}
