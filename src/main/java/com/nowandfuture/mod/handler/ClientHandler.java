package com.nowandfuture.mod.handler;

import com.nowandfuture.asm.IRender;
import com.nowandfuture.asm.RenderHook;
import com.nowandfuture.asm.Utils;
import com.nowandfuture.mod.core.client.renders.CopyBlockItemModel;
import com.nowandfuture.mod.core.client.renders.ModuleRenderManager;
import com.nowandfuture.mod.core.client.renders.TransformedBlockRenderMap;
import com.nowandfuture.mod.core.client.renders.tiles.VideoRenderer;
import com.nowandfuture.mod.core.client.renders.videorenderer.VideoRendererUtil;
import com.nowandfuture.mod.core.common.entities.TileEntitySimplePlayer;
import com.nowandfuture.mod.utils.DrawHelper;
import com.nowandfuture.mod.utils.SyncTasks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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
public class ClientHandler {
    private static final Map<BlockPos,Integer> scores = new LinkedHashMap<>();
    private static final List<AxisAlignedBB> aabbList = new LinkedList<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void handleHighLightRender(DrawBlockHighlightEvent event){
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void handleWorldRender(RenderWorldLastEvent renderWorldLastEvent) {
        EntityPlayer player = Minecraft.getMinecraft().player;

        RenderHook.forceClear();
        while (!ModuleRenderManager.INSTANCE.getRenderQueue().isEmpty()){
            IRender iRender = ModuleRenderManager.INSTANCE.getRenderQueue().poll();
            RenderHook.offer(iRender);
        }
        scores.clear();
        VideoRendererUtil.
                getScoreOfScreen(Minecraft.getMinecraft(),scores,
                        renderWorldLastEvent.getPartialTicks());



        if(Minecraft.getMinecraft().gameSettings.showDebugInfo) {
            for (AxisAlignedBB aabb :
                    aabbList) {
                GlStateManager.color(1, 1, 1, .5f);
                DrawHelper.preDraw();
                double x = TileEntityRendererDispatcher.staticPlayerX;
                double y = TileEntityRendererDispatcher.staticPlayerY;
                double z = TileEntityRendererDispatcher.staticPlayerZ;
                DrawHelper.drawBoundingBox(aabb.offset(-x, -y, -z).grow(0.001));
                DrawHelper.postDraw();
            }
        }

//        aabbList.clear();
    }

    @SubscribeEvent
    public void registerModelBake(ModelBakeEvent event){
        ModelResourceLocation model = new ModelResourceLocation(Objects.requireNonNull(RegisterHandler.copyItem.getRegistryName()), "inventory");
        event.getModelRegistry().putObject(model,new CopyBlockItemModel());
    }

    public static Map<BlockPos,Integer> getScores(){
        return scores;
    }

    public static List<AxisAlignedBB> getAabbList() {
        return aabbList;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void handleUnloadWorld(WorldEvent.Unload unload){
        if(unload.getWorld().isRemote) {
            TransformedBlockRenderMap.INSTANCE.clear();
            Utils.mapCache = null;
            ModuleRenderManager.INSTANCE.invalid();

            SyncTasks.INSTANCE.showdownNow();
            SyncTasks.INSTANCE.init();
            unload.getWorld().loadedTileEntityList
                    .forEach(new Consumer<TileEntity>() {
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