package com.nowandfuture.mod.handler;

import net.minecraft.client.Minecraft;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientTickHandler {

    private Minecraft mc = Minecraft.getMinecraft();;

    public ClientTickHandler(){
    }

    @SubscribeEvent
    public void handleTick(TickEvent.ClientTickEvent clientTickEvent){
        if(clientTickEvent.phase == TickEvent.Phase.START){

        }else {

        }
    }

    @SubscribeEvent
    public void handleClickBlock(PlayerInteractEvent.RightClickBlock rightClickBlock){

    }

}
