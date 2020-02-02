package com.nowandfuture.mod.core.common.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class ContainerSimplePlayer extends Container {

    public ContainerSimplePlayer(){

    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }
}
