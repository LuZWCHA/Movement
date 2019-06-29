package com.nowandfuture.mod.handler;

import com.nowandfuture.mod.core.selection.AABBSelectArea;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderHandler {

    private Minecraft mc = Minecraft.getMinecraft();
    private static AABBSelectArea aabbSelectArea = new AABBSelectArea();
    private AABBSelectArea.Renderer renderer = new AABBSelectArea.Renderer(aabbSelectArea);

    public static AABBSelectArea getAabbSelectArea() {
        return aabbSelectArea;
    }

    public static void toggleShow(){
        aabbSelectArea.setShow(!aabbSelectArea.isShow());
    }

    public static void setShow(boolean show){
        aabbSelectArea.setShow(show);
    }

    public static void setPosAndSize(BlockPos pos1,BlockPos pos2){
        aabbSelectArea.setBox(new AxisAlignedBB(pos1,pos2.add(pos1)));
    }

    //@SubscribeEvent(priority = EventPriority.LOWEST)
    public void handleHighLightRender(RenderGameOverlayEvent highlightEvent){


    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void handleWorldRender(RenderWorldLastEvent renderWorldLastEvent){
        final Entity entity = mc.getRenderViewEntity();

        if (entity == null) {
            return;
        }

        final float partialTicks = renderWorldLastEvent.getPartialTicks();

        // Copied from EntityRenderer. This code can be found by looking at usages of Entity.prevPosX.
        // It also appears in many other places throughout Minecraft's rendering
        final double renderPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
        final double renderPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
        final double renderPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;

//        ModuleManager.INSTANCE.getModules()
//                .forEach(new Consumer<IModule>() {
//                    @Override
//                    public void accept(IModule iModule) {
//                        if(iModule.isEnable() &&
//                                iModule.canRender(renderPosX,renderPosY,renderPosZ)){
//                            RenderHook.offer(iModule);
//                        }
//                    }
//                });

        if(aabbSelectArea.isShow()){
            GlStateManager.pushMatrix();
            GlStateManager.translate(-renderPosX  ,-renderPosY, -renderPosZ);
            renderer.render();
            GlStateManager.popMatrix();
        }
    }
//
//    @Deprecated
//    private void renderOld(ModuleBase movementModuleBase, float partialTicks){
//        final Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
//        if (entity == null) {
//            return;
//        }
//
//        // Copied from EntityRenderer. This code can be found by looking at usages of Entity.prevPosX.
//        // It also appears in many other places throughout Minecraft's rendering
//        double renderPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
//        double renderPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
//        double renderPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;
//        //if(renderTickEvent.phase == TickEvent.Phase.END){
//        ModuleManager.INSTANCE.getModules()
//                .forEach(new Consumer<IModule>() {
//                    @Override
//                    public void accept(IModule movementModule) {
//                        if(movementModule.isEnable() && movementModule.canRender(renderPosX,renderPosY,renderPosZ)) {
//                            //GlStateManager.clear(256);
//                            Minecraft.getMinecraft().entityRenderer.enableLightmap();
//                            Minecraft.getMinecraft().getTextureManager().bindTexture(LOCATION_BLOCKS_TEXTURE);
//                            GlStateManager.pushAttrib();
//                            GlStateManager.pushMatrix();
////                                GlStateManager.blendFunc(org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA);
////                                GlStateManager.enableBlend();
////                                GlStateManager.disableCull();
////                                GlStateManager.enableDepth();
//
//                            RenderHelper.disableStandardItemLighting();
//                            //GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_ZERO);
//                            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//
//                            GlStateManager.enableBlend();
//                            GlStateManager.disableCull();
//
//                            if (net.minecraft.client.Minecraft.isAmbientOcclusionEnabled())
//                            {
//                                GlStateManager.shadeModel(GL11.GL_SMOOTH);
//                            }
//                            else
//                            {
//                                GlStateManager.shadeModel(GL11.GL_FLAT);
//                            }
//
////                                if(pass > 0)
////                                {
////                                    net.minecraft.util.math.Vec3d cameraPos = net.minecraft.client.renderer.ActiveRenderInfo.getCameraPosition();
////                                    batchBuffer.getBuffer().sortVertexData((float)cameraPos.x, (float)cameraPos.y, (float)cameraPos.z);
////                                }
//
//
//                            int i = Minecraft.getMinecraft().world.getCombinedLight(
//                                    movementModule.getModulePos(),0);
//                            int j = i % 65536;
//                            int k = i / 65536;
//                            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
//                            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//                            //RenderHelper.enableStandardItemLighting();
//                            GlStateManager.disableAlpha();
//
//                            //GlStateManager.disableBlend();
//                            //GlStateManager.clear(256);
//                            GlStateManager.translate(movementModule.getModulePos().getX() - renderPosX,
//                                    movementModule.getModulePos().getY() - renderPosY,
//                                    movementModule.getModulePos().getZ() - renderPosZ);
//                            movementModule.render(partialTicks);
//
//                            //GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//                            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//
//                            GlStateManager.enableAlpha();
//                            GlStateManager.disableBlend();
//                            GlStateManager.enableCull();
//                            RenderHelper.enableStandardItemLighting();
//                            GlStateManager.popAttrib();
//                            GlStateManager.popMatrix();
//                            Minecraft.getMinecraft().entityRenderer.disableLightmap();
//
//                        }
//                    }
//                });
//    }

}
