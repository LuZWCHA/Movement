package com.nowandfuture.mod.handler;

import com.nowandfuture.mod.api.Unstable;
import com.nowandfuture.mod.core.common.entities.TileEntityCoreModule;
import com.nowandfuture.mod.core.selection.OBBox;
import com.nowandfuture.mod.utils.collision.CollisionInfo;
import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

//all obb collision will be disabled
//use the aabb at the aligned-matrix(the matrix transform the aabb to a aabb,too) instead of obb
@Unstable
public class CollisionHandler {
//    public static Queue<TileEntityCoreModule> modules = new ConcurrentLinkedQueue<>();

    @SubscribeEvent
    public void handleCollision(GetCollisionBoxesEvent event){

        World world = event.getWorld();
        Entity entity = event.getEntity();
        AxisAlignedBB expanded = event.getAabb();
        List<AxisAlignedBB> list = event.getCollisionBoxesList();

        for (TileEntity module :
                world.loadedTileEntityList) {
            if(module instanceof TileEntityCoreModule){
                if(((TileEntityCoreModule) module).isEnable() && ((TileEntityCoreModule) module).isEnableCollision()){
                    ((TileEntityCoreModule) module).collectAABBs(list,expanded);
                }
            }
        }


        if(world.isRemote && entity instanceof EntityPlayer){
            ClientHandler.getAabbList().clear();
            ClientHandler.getAabbList().addAll(list);
        }
    }

    //---------------------------- these methods are still testing -------------------------------

    public static void collisionWithModules(@Nonnull Entity entity,Vector3f v,List<CollisionInfo> result){
        List<OBBox> obBoxes = new ArrayList<>();
        collectOBBoxes(obBoxes,entity,v);
        CollisionHandler.collisionWithOBBoxes(entity,v,obBoxes,result);
    }

    public static void collectOBBoxes(List<OBBox> boxes, Entity entity, Vector3f v){
        World world = entity.world;

        for (TileEntity te:
                world.loadedTileEntityList) {
            if (te instanceof TileEntityCoreModule) {
                if (((TileEntityCoreModule) te).isEnable() && ((TileEntityCoreModule) te).isEnableCollision())
                    ((TileEntityCoreModule) te).collectOBBoxs(boxes);
            }
        }

        boxes.removeIf(new Predicate<OBBox>() {
            @Override
            public boolean test(OBBox obBox) {
                return !obBox.intersect(entity.getEntityBoundingBox().expand(v.x,v.y,v.z));
            }
        });
    }

    public static void collisionWithAABBoxes(@Nonnull Entity entity, Vector3f v, List<AxisAlignedBB> boxes,List<CollisionInfo> result){
        AxisAlignedBB movedAABB = entity.getEntityBoundingBox();

        double x = v.x;
        double y = v.y;
        double z = v.z;

        if (y != 0.0D) {
            int k = 0;

            for (int l = boxes.size(); k < l; ++k) {
                y = boxes.get(k).calculateYOffset(movedAABB, y);
            }

            movedAABB = movedAABB.offset(0.0D, y, 0.0D);
        }

        if (x != 0.0D) {
            int j5 = 0;

            for (int l5 = boxes.size(); j5 < l5; ++j5) {
                x = boxes.get(j5).calculateXOffset(movedAABB, x);
            }

            if (x != 0.0D) {
                movedAABB = movedAABB.offset(x, 0.0D, 0.0D);
            }
        }

        if (z != 0.0D) {
            int k5 = 0;

            for (int i6 = boxes.size(); k5 < i6; ++k5) {
                z = boxes.get(k5).calculateZOffset(movedAABB, z);
            }

            if (z != 0.0D) {
                movedAABB = movedAABB.offset(0.0D, 0.0D, z);
            }
        }

        if(v.x != x){
            CollisionInfo collisionInfo = new CollisionInfo(new Vector3f(1,0,0), x/v.x);
            addVector2List(result,collisionInfo);
        }
        if(v.y != y){
            CollisionInfo collisionInfo = new CollisionInfo(new Vector3f(0,1,0), y/v.y);
            addVector2List(result,collisionInfo);
        }
        if(v.z != z){
            CollisionInfo collisionInfo = new CollisionInfo(new Vector3f(0,0,1), z/v.z);
            addVector2List(result,collisionInfo);
        }

    }

    public static void collisionWithOBBoxes(@Nonnull Entity entity, Vector3f v, List<OBBox> boxes,List<CollisionInfo> result){
        for (OBBox obb :
                boxes) {

            Vector3f a = new Vector3f();
            float time = obb.collisionDetermination(entity.getEntityBoundingBox(), v, a);

            if (time >= 0 && a.lengthSquared() > 0) {
                CollisionInfo collisionInfo = new CollisionInfo(a, time);
                if(result.isEmpty())
                    result.add(collisionInfo);
                else
                    for (int i = 0; i < result.size(); i++) {
                        CollisionInfo info = result.get(i);
                        addVector2List(result,info);
                    }
            }
        }
    }

    private static void addVector2List(List<CollisionInfo> result,CollisionInfo collisionInfo){
        if(result.isEmpty())
            result.add(collisionInfo);
        else
            for (int i = 0; i < result.size(); i++) {
                CollisionInfo info = result.get(i);
                if(info.getImpactAxis().equals(collisionInfo.getImpactAxis()) ||
                        info.getImpactAxis().equals(collisionInfo.getImpactAxis().negate())){
                    if(info.getImpactTime() > collisionInfo.getImpactTime()){
                        result.set(i,collisionInfo);
                        break;
                    }
                }else{
                    result.add(collisionInfo);
                    break;
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

    private Vector3f calculateOffsetValue(AxisAlignedBB org,AxisAlignedBB offset){
        Vector3f v = new Vector3f();
        double ex = offset.minX - org.minX;
        double ey = offset.minY - org.minY;
        double ez = offset.minZ - org.minZ;
        v.x = (float) (ex);
        v.y = (float) (ey);
        v.z = (float) (ez);
        return v;
    }
}
