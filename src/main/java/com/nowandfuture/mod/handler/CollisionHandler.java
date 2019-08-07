package com.nowandfuture.mod.handler;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.client.renders.CubesRenderer;
import com.nowandfuture.mod.core.client.renders.ModuleRenderManager;
import com.nowandfuture.mod.core.common.entities.TileEntityModule;
import com.nowandfuture.mod.core.selection.AxisAlignedBBWrap;
import com.nowandfuture.mod.core.selection.OBBox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;

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
            CubesRenderer renderer = ModuleRenderManager.INSTANCE.getRenderer(module.getPrefab());
            if(axisAlignedBB != null && renderer != null){
                OBBox obBox = new OBBox(axisAlignedBB);
                Matrix4f matrix4f = renderer.getModelMatrix();
                obBox.mulMatrix(matrix4f);
                obBox.translate(module.getModulePos());

                try {
                    if(obBox.intersect(abb)){
                        float impactTime = 0;
                        Vector3f v = null;
                        //noinspection PointlessNullCheck
                        if(entity != null) {
                            AxisAlignedBB orgAABB = entity.getEntityBoundingBox();
                            v = new Vector3f(
                                    (float) (abb.minX - orgAABB.minX),
                                    (float) (abb.minY - orgAABB.minY),
                                    (float) (abb.minZ - orgAABB.minZ)
                            );

                            if(!obBox.intersect(orgAABB)) {
                                impactTime = obBox.sweepTest(orgAABB, v);
                            }else{
                                impactTime = -1;
                            }
//                            Movement.logger.info(impactTime);
                        }
                        list.add(new AxisAlignedBBWrap(obBox,impactTime,v));
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
