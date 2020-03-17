package com.nowandfuture.mixins;

import com.nowandfuture.mod.core.common.gui.mygui.AbstractContainer;
import com.nowandfuture.mod.core.common.gui.mygui.api.IDynamicInventory;
import com.nowandfuture.mod.core.common.gui.mygui.network.InventorySMessage;
import com.nowandfuture.mod.core.common.gui.mygui.network.NetworkHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP {

    @Shadow public int currentWindowId;

    @Inject(
            method = "sendAllContents",
            at = @At("HEAD"),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void inject_sendAllContents(Container arg0, NonNullList<ItemStack> arg1, CallbackInfo ci){
        if(arg0 instanceof AbstractContainer){
            AbstractContainer container = (AbstractContainer) arg0;
            IDynamicInventory dynamicInventory = container.getDynamicInventory();
            if(dynamicInventory != null){
                InventorySMessage message = new InventorySMessage(currentWindowId,dynamicInventory.writeToNBT(new NBTTagCompound()));
                NetworkHandler.INSTANCE.sendMessageToPlayer(message,(EntityPlayerMP) (Object)this);
            }
        }
    }
}
