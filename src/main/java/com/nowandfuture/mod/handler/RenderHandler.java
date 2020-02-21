package com.nowandfuture.mod.handler;

import com.nowandfuture.asm.IRender;
import com.nowandfuture.asm.RenderHook;
import com.nowandfuture.asm.Utils;
import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.client.renders.CopyBlockItemModel;
import com.nowandfuture.mod.core.client.renders.ModuleRenderManager;
import com.nowandfuture.mod.core.client.renders.TransformedBlockRenderMap;
import com.nowandfuture.mod.core.client.renders.tiles.VideoRenderer;
import com.nowandfuture.mod.core.client.renders.videorenderer.VideoRendererUtil;
import com.nowandfuture.mod.core.common.entities.TileEntitySimplePlayer;
import com.nowandfuture.mod.utils.SyncTasks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.*;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
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
        scores.clear();
        VideoRendererUtil.
                getScoreOfScreen(Minecraft.getMinecraft(),scores,
                        renderWorldLastEvent.getPartialTicks());
    }

    @SubscribeEvent
    public void registerModelBake(ModelBakeEvent event){
        ModelResourceLocation model = new ModelResourceLocation(RegisterHandler.copyItem.getRegistryName(), "inventory");
        event.getModelRegistry().putObject(model,new CopyBlockItemModel());
    }

    public static Map<BlockPos,Integer> getScores(){
        return scores;
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
            ModuleRenderManager.INSTANCE.invalid();

            SyncTasks.INSTANCE.showdownNow();
            SyncTasks.INSTANCE.init();
            unload.getWorld().loadedTileEntityList.forEach(new Consumer<TileEntity>() {
                @Override
                public void accept(TileEntity tileEntity) {
                    if(tileEntity instanceof TileEntitySimplePlayer){
                        try {
                            ((TileEntitySimplePlayer) tileEntity).end();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }finally {

                        }
                    }
                }
            });

            VideoRenderer.clear();
        }
    }

}
