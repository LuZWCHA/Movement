package com.nowandfuture.mod.handler;

import com.nowandfuture.mod.core.selection.OBBox;
import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientTickHandler {

    private RayTraceResult rtr = Minecraft.getMinecraft().objectMouseOver;

    public ClientTickHandler(){
    }

    @SubscribeEvent
    public void handleTick(TickEvent.ClientTickEvent clientTickEvent){
        if(clientTickEvent.phase == TickEvent.Phase.START){

        }else {

        }
    }


    //click block
    @SubscribeEvent
    public void handleClickBlock(PlayerInteractEvent.RightClickBlock rightClickBlock){

    }

    //click empty
    @SubscribeEvent
    public void handleClickEmpty(PlayerInteractEvent.RightClickEmpty rightClickBlock){

    }

    //click with item
    @SubscribeEvent
    public void handleClickItem(PlayerInteractEvent.RightClickItem rightClickBlock){

    }

    //click with item
    @SubscribeEvent
    public void handleClickEntity(PlayerInteractEvent.EntityInteractSpecific rightClickBlock){

    }

    private boolean checkInteractOBB(OBBox obBox, Vector3f start,Vector3f end){

        return false;
    }

}
