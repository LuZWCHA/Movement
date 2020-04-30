package com.nowandfuture.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(Entity.class)
public class MixinEntity {

    @Shadow public World world;

    @Inject(
            method = "move",
            at = @At("TAIL"),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void inject_move(MoverType type, double x, double y, double z, CallbackInfo callbackInfo){
        Entity it = (Entity)((Object)this);

        if(x != 0 || y != 0 || z != 0){
            List<AxisAlignedBB> collisionList =
                    world.getCollisionBoxes(it,it.getEntityBoundingBox().expand(x,y,z));
            if()
        }
    }
}
