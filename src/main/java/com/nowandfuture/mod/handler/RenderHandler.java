package com.nowandfuture.mod.handler;

import com.nowandfuture.asm.IRender;
import com.nowandfuture.asm.RenderHook;
import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.selection.AABBSelectArea;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Deque;
import java.util.LinkedList;

@SideOnly(Side.CLIENT)
public class RenderHandler {
    private static final Deque<IRender> renderModules = new LinkedList<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void handleWorldRender(RenderWorldLastEvent renderWorldLastEvent) {
        Minecraft.getMinecraft().entityRenderer.enableLightmap();

        RenderHook.forceClear();
        while (!renderModules.isEmpty()){
            IRender iRender = renderModules.poll();
            RenderHook.offer(iRender);
        }

        Minecraft.getMinecraft().entityRenderer.disableLightmap();
    }

    public static Deque<IRender> getRenderModules() {
        return renderModules;
    }

    public static void addRenderer(IRender renderer){
        renderModules.addFirst(renderer);
    }

}
