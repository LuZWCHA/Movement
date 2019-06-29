package com.nowandfuture.mod.core.movecontrol;

import com.nowandfuture.asm.RenderHook;
import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.entities.TileEntityMovementModule;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.client.model.animation.FastTESR;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModuleRender extends FastTESR<TileEntityMovementModule> {

    @Override
    public void renderTileEntityFast(TileEntityMovementModule te, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {
        RenderHook.offer(te);
    }

    @Override
    public boolean isGlobalRenderer(TileEntityMovementModule te) {
        return true;
    }
}
