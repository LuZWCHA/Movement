package com.nowandfuture.mod.handler;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.common.entities.TileEntityModule;
import com.nowandfuture.mod.core.selection.AxisAlignedBBWrap;
import com.nowandfuture.mod.core.selection.OBBox;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Matrix4f;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

//not finished,just test
public class CollisionHandler {
    public static Queue<TileEntityModule> modules = new ConcurrentLinkedQueue<>();
    private long time;

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void handleCollision(GetCollisionBoxesEvent event){

        Entity entity = event.getEntity();
        AxisAlignedBB abb = event.getAabb();
        List<AxisAlignedBB> list = event.getCollisionBoxesList();

        for (TileEntityModule module:
                modules) {
            if(!module.isEnable() || !module.isEnableCollision()) continue;
            AxisAlignedBB axisAlignedBB = module.getModuleBase().getMinAABB();
            if(axisAlignedBB != null){
                OBBox obBox = new OBBox(axisAlignedBB);
                Matrix4f matrix4f = module.getModuleBase().getPrefab().getModelMatrix();
                obBox.mulMatrix(matrix4f);
                obBox.translate(module.getModulePos());

                try {
                    if(obBox.intersect(abb)){
                        list.add(new AxisAlignedBBWrap(obBox));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }

    }

    //not finished
    @SubscribeEvent
    public void handleServerTick(TickEvent.ServerTickEvent serverTickEvent){
        if(serverTickEvent.phase == TickEvent.Phase.START){

        }

        if(serverTickEvent.phase == TickEvent.Phase.END){
            time = MinecraftServer.getCurrentTimeMillis();
//            for (TileEntityModule module:
//                 modules) {
//                if(module.isEnable()) {
//                    module.getModuleBase().transform(0);
//                }else if(module.isInvalid()){
//                    modules.remove(module);
//                }
//            }
        }
    }
}
