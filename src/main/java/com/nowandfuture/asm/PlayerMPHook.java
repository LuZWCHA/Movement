package com.nowandfuture.asm;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class PlayerMPHook {

    public static void sendAllContent(EntityPlayerMP playerMP,Container container, NonNullList<ItemStack> list){
//        System.out.println("send!!" + (container instanceof AbstractContainer));
    }
}
