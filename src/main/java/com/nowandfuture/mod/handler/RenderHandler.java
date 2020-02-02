package com.nowandfuture.mod.handler;

import com.nowandfuture.asm.IRender;
import com.nowandfuture.asm.RenderHook;
import com.nowandfuture.asm.Utils;
import com.nowandfuture.ffmpeg.player.SimplePlayer;
import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.client.renders.CopyBlockItemModel;
import com.nowandfuture.mod.core.client.renders.ModuleRenderManager;
import com.nowandfuture.mod.core.client.renders.TransformedBlockRenderMap;
import com.nowandfuture.mod.core.common.Items.BlockInfoCopyItem;
import com.nowandfuture.mod.core.common.entities.TileEntityModule;
import com.nowandfuture.mod.core.common.entities.TileEntitySimplePlayer;
import com.nowandfuture.mod.core.selection.AABBSelectArea;
import com.nowandfuture.mod.utils.DrawHelper;
import com.nowandfuture.mod.utils.SyncTasks;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItemFrame;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class RenderHandler {
    private static final Deque<IRender> renderModules = new LinkedList<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void handleHighLightRender(DrawBlockHighlightEvent event){
//        event.getTarget().entityHit.world.getTileEntity(event.getTarget().getBlockPos());
//        if(event.getTarget()!=null) {
//            EntityPlayer player = event.getPlayer();
//            double partialTicks = event.getPartialTicks();
//            double d3 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
//            double d4 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
//            double d5 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;
//
//            GlStateManager.enableBlend();
//            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//            GlStateManager.glLineWidth(2.0F);
//            GlStateManager.disableTexture2D();
//            GlStateManager.depthMask(false);
//            DrawHelper.drawOutlinedBoundingBox(new AxisAlignedBB(0,0,0,1,1,1).offset(event.getTarget().getBlockPos()).offset(-d3,-d4,-d5),1,0,0,0.4f);
//            GlStateManager.depthMask(true);
//            GlStateManager.enableTexture2D();
//            GlStateManager.disableBlend();
//        }
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void handleWorldRender(RenderWorldLastEvent renderWorldLastEvent) {

        RenderHook.forceClear();
        while (!renderModules.isEmpty()){
            IRender iRender = renderModules.poll();
            RenderHook.offer(iRender);
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
