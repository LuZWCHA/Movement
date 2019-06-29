package com.nowandfuture.asm;

import net.minecraft.entity.Entity;

import static com.nowandfuture.asm.RenderHook.render;

public class TargetClass {

    private void renderWorldPass(int pass, float partialTicks,Entity entity)
    {
        //render(partialTicks);
        RenderHook.render(pass,partialTicks);
    }

}
