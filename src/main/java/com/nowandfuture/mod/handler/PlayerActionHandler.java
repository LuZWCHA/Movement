package com.nowandfuture.mod.handler;

import com.nowandfuture.mod.core.common.entities.IClickableTile;
import com.nowandfuture.mod.core.common.entities.TileEntityRayResult;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.function.Consumer;

public class PlayerActionHandler {

    public PlayerActionHandler(){
    }

    @SubscribeEvent
    public void handleTick(TickEvent.ClientTickEvent clientTickEvent){
        if(clientTickEvent.phase == TickEvent.Phase.START){

        }else {

        }
    }

    @SubscribeEvent
    public void handleRightClick(PlayerInteractEvent.EntityInteractSpecific interactEvent){
    }

    //click block
    @SubscribeEvent
    public void handleClickBlock(PlayerInteractEvent.RightClickBlock rightClickBlock){
        TileEntityRayResult rayResult = checkIsClickedOnTileEntity(rightClickBlock,false);
        if(rayResult != null && rightClickBlock.getHand() == EnumHand.MAIN_HAND){
            rayResult.tileEntity.onRightClick(rayResult.hitVec);
            rightClickBlock.setCanceled(true);
        }
    }

    //click empty
    @SubscribeEvent
    public void handleClickEmpty(PlayerInteractEvent.RightClickEmpty rightClickBlock){
        TileEntityRayResult rayResult = checkIsClickedOnTileEntity(rightClickBlock,true);
        if(rayResult != null && rightClickBlock.getHand() == EnumHand.MAIN_HAND){
            rayResult.tileEntity.onRightClick(rayResult.hitVec);
        }
    }

    //click with item
    @SubscribeEvent
    public void handleClickItem(PlayerInteractEvent.RightClickItem rightClickBlock){

    }

    @SubscribeEvent
    public void handleClickEntity(PlayerInteractEvent.EntityInteract entityInteract){
        TileEntityRayResult rayResult = checkIsClickedOnTileEntity(entityInteract,false);
        if(rayResult != null && entityInteract.getHand() == EnumHand.MAIN_HAND){
            rayResult.tileEntity.onRightClick(rayResult.hitVec);
            entityInteract.setCanceled(true);
        }
    }

    private TileEntityRayResult checkIsClickedOnTileEntity(PlayerInteractEvent event, boolean isEmpty){
        BlockPos pos = event.getPos();

        float p = 1f;
        if(event.getWorld().isRemote) {
            Minecraft mc = Minecraft.getMinecraft();
            p = mc.getRenderPartialTicks();
        }

        final Vec3d start = event.getEntity().getPositionEyes(p);
        final Vec3d look = event.getEntity().getLook(p);

        TileEntityRayResult result = new TileEntityRayResult(new Vec3d(pos), EnumFacing.NORTH);

        event.getWorld().loadedTileEntityList.forEach(new Consumer<TileEntity>() {
            double minDistance = (isEmpty ? Double.MAX_VALUE : start.squareDistanceTo(new Vec3d(pos)));

            @Override
            public void accept(TileEntity tileEntity) {
                if(tileEntity instanceof IClickableTile){
                    Vec3d vec3d = look.scale(((IClickableTile) tileEntity).getReachedDistance());
                    final Vec3d end = start.add(vec3d.x,vec3d.y,vec3d.z);
                    RayTraceResult rayTraceResult =
                            ((IClickableTile) tileEntity).getClickBox().calculateIntercept(start,end);
                    Vec3d d = ((IClickableTile) tileEntity).getClickableFaceNormal();
                    double dot = look.normalize().dotProduct(d);
                    double factor = - Math.min(0,dot);

                    if(factor <= 0) return;

                    if(rayTraceResult != null) {
                        float ls = (float) rayTraceResult.hitVec.squareDistanceTo(start);
                        if(ls < minDistance){
                            minDistance = ls;
                            result.hitInfo = rayTraceResult.hitInfo;
                            result.tileEntity = (IClickableTile) tileEntity;
                            result.hitVec = rayTraceResult.hitVec;
                            result.sideHit = rayTraceResult.sideHit;
                        }
                    }
                }
            }
        });

        return result.tileEntity == null ? null : result;
    }
}
