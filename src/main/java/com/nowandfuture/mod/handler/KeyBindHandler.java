package com.nowandfuture.mod.handler;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyBindHandler {

    public static void init(){

    }

    @SubscribeEvent
    public void keyDown(InputEvent.KeyInputEvent event){

    }
}
