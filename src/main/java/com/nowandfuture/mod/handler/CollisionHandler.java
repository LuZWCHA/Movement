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
                                AxisAlignedBBWrap wrap = null;
                                for (AxisAlignedBB aabb :
                                        list) {
                                    if (aabb instanceof AxisAlignedBBWrap)
                                        wrap = (AxisAlignedBBWrap) aabb;
                                }

                                if(wrap == null) {
                                    wrap = new AxisAlignedBBWrap(entity, impactTime, v);
                                    list.add(wrap);
                                }
                                wrap.pushAxis(axis);
                                wrap.setImpactTime(impactTime);

                            }else{
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
}
