package com.nowandfuture.mod.handler;

import com.nowandfuture.mod.core.common.entities.TileEntityModule;
import com.nowandfuture.mod.core.selection.AxisAlignedBBWrap;
import com.nowandfuture.mod.core.selection.OBBox;
import com.nowandfuture.mod.utils.math.Matrix4f;
import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

//not finished,just test
public class CollisionHandler {
    public static Queue<TileEntityModule> modules = new ConcurrentLinkedQueue<>();

    @SubscribeEvent
    public void handleCollision(GetCollisionBoxesEvent event){

        Entity entity = event.getEntity();
        AxisAlignedBB expanded = event.getAabb();
        List<AxisAlignedBB> list = event.getCollisionBoxesList();

        if(entity == null) return;

        modules.clear();

        for (TileEntity te:
        entity.getEntityWorld().loadedTileEntityList) {
            if(te instanceof TileEntityModule)
                modules.add((TileEntityModule) te);
        }

        for (TileEntityModule module:
                modules) {

            if(!module.isEnable() || !module.isEnableCollision()) continue;

            AxisAlignedBB moduleAABB = module.getModuleBase().getMinAABB();
            if(moduleAABB != null){

                OBBox obBox = new OBBox(moduleAABB);
                Matrix4f matrix4f = module.getModuleBase().getTransRes();
                obBox.mulMatrix(matrix4f);
                module.setRenderBox(new OBBox(obBox));

                obBox.translate(module.getModulePos());
                try {

                    if(obBox.intersect(expanded)){
                        float impactTime = 0;
                        Vector3f v;
                        if(entity != null) {
                            AxisAlignedBB orgAABB = entity.getEntityBoundingBox();

                            v = calculateExpandValue(orgAABB,expanded);

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

    private Vector3f calculateExpandValue(AxisAlignedBB org,AxisAlignedBB expanded){
        Vector3f v = new Vector3f();
        double ex = expanded.minX - org.minX;
        double ey = expanded.minY - org.minY;
        double ez = expanded.minZ - org.minZ;
        v.x = (float) (ex == 0 ? expanded.maxX - org.maxX : ex);
        v.y = (float) (ey == 0 ? expanded.maxY - org.maxY : ey);
        v.z = (float) (ez == 0 ? expanded.maxZ - org.maxZ : ez);
        return v;
    }
}
