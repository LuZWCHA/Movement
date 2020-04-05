package com.nowandfuture.mixins;

import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FontRenderer.class)
public abstract class MixinFontRenderer {

    @Inject(
            method = "drawString(Ljava/lang/String;FFIZ)I",
            at = @At("HEAD"),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void inject_drawString(String text, float x, float y, int color, boolean dropShadow, CallbackInfoReturnable<Integer> callbackInfo){
        System.out.println(text);
    }
}
