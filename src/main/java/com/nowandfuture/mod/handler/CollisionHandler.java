package com.nowandfuture.mod.handler;

import com.nowandfuture.mod.core.common.entities.TileEntityModule;
import com.nowandfuture.mod.core.selection.AxisAlignedBBWrap;
import com.nowandfuture.mod.core.selection.OBBox;
import com.nowandfuture.mod.utils.math.Matrix4f;
import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;

//not finished,just test
public class CollisionHandler {
    public static Queue<TileEntityModule> modules = new ConcurrentLinkedQueue<>();

    @SubscribeEvent
    public void handleCollision(GetCollisionBoxesEvent event){

        Entity entity = event.getEntity();
        AxisAlignedBB abb = event.getAabb();
        List<AxisAlignedBB> list = event.getCollisionBoxesList();

        modules.removeIf(new Predicate<TileEntityModule>() {
            @Override
            public boolean test(TileEntityModule tileEntityModule) {
                return tileEntityModule.isInvalid();
            }
        });

        for (TileEntityModule module:
                modules) {

            if(!module.isEnable() || !module.isEnableCollision()) continue;

            AxisAlignedBB axisAlignedBB = module.getModuleBase().getMinAABB();
            if(axisAlignedBB != null){

                OBBox obBox = new OBBox(axisAlignedBB);
                Matrix4f matrix4f = module.getModuleBase().getTransRes();
                obBox.mulMatrix(matrix4f);
                module.setRenderBox(new OBBox(obBox));

                obBox.translate(module.getModulePos());


                try {
                    if(obBox.intersect(abb)){

                        float impactTime = 0;
                        Vector3f v;
                        if(entity != null) {

                            AxisAlignedBB orgAABB = entity.getEntityBoundingBox();
                            v = new Vector3f(
                                    ((float) entity.motionX),
                                    ((float) entity.motionY),
                                    ((float) entity.motionZ)
                            );
                            Vector3f axis = new Vector3f();

                            float time = obBox.collisionDetermination(orgAABB, v,axis);
                            if(time >= 0) {
                                module.setImpactAxis(axis);
                                list.add(new AxisAlignedBBWrap(entity,orgAABB,obBox,impactTime,axis,v));
                            }else if(time == Float.MIN_VALUE){
                                module.setImpactAxis(null);
                            }

                        }else{
                            //conclusion with particles or blocks
                        }

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }

    }

//    //not finished
//    @SubscribeEvent
//    public void handleServerTick(TickEvent.ServerTickEvent serverTickEvent){
//        if(serverTickEvent.phase == TickEvent.Phase.START){
//
//        }
//
//        if(serverTickEvent.phase == TickEvent.Phase.END){
//            time = MinecraftServer.getCurrentTimeMillis();
////            for (TileEntityModule module:
////                 modules) {
////                if(module.isEnable()) {
////                    module.getModuleBase().transform(0);
////                }else if(module.isInvalid()){
////                    modules.remove(module);
////                }
////            }
//        }
//    }
}
