package com.nowandfuture.mod.core.client.renders;

import com.nowandfuture.mod.core.client.renders.tiles.VideoRenderer;
import com.nowandfuture.mod.core.common.entities.TileEntitySimplePlayer;
import com.nowandfuture.mod.utils.math.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nonnull;
import java.util.*;

public class VideoRendererUtil {

    public final static int MAX_SCORE = 128;
    public static float RIO_GAIN = 0.5f;
    private static ScreenDistanceComparator comparator = new ScreenDistanceComparator();

    public static void getScoreOfScreen(Minecraft mc, @Nonnull Map<BlockPos,Integer> scores, float p){

        Entity entity = mc.player;
        final double distance = VideoRenderer.LookDistance;
        final Vec3d start = entity.getPositionEyes(p);
        final Vec3d look = entity.getLook(p);
        final Vec3d end = start.addVector(look.x * distance,
                look.y * distance, look.z * distance);
        List<ScreenFocusInfo> list = new LinkedList<>();
        final float[] sum = {0};
        for (TileEntity tileEntity :
                mc.world.loadedTileEntityList) {
            if(tileEntity instanceof TileEntitySimplePlayer){
                RayTraceResult rayTraceResult =
                        tileEntity.getRenderBoundingBox().calculateIntercept(start,end);
                Vec3i d = ((TileEntitySimplePlayer) tileEntity).getFacing().getDirectionVec();
                double dot = look.normalize().dotProduct(new Vec3d(d));
                double factor = - Math.min(0,dot);

                if(factor <= 0) continue;

                if(rayTraceResult != null){
                    float ls = (float) rayTraceResult.hitVec.subtract(start).lengthSquared();
                    list.add(new ScreenFocusInfo(tileEntity.getPos(),true,ls));
                }else{
                    double distance1 = tileEntity.getRenderBoundingBox().getCenter().squareDistanceTo(start);
                    distance1 = (64/distance1) / factor;
                    list.add(new ScreenFocusInfo(tileEntity.getPos(),false, (float) distance1));
                    sum[0] += distance1;
                }
            }
        }

        //list sorted by distance
        list.sort(comparator);

        //search screens in RIO
        List<ScreenFocusInfo> rioScreens = new ArrayList<>();
        //at lest 2 screens
        if(list.size() > 1){
            ScreenFocusInfo top1 = list.get(0);
            if(top1.project){
                TileEntity t = mc.world.getTileEntity(top1.pos);
                if(t instanceof TileEntitySimplePlayer){
                    double d = top1.distance;
                    Vec3d center = t.getRenderBoundingBox().getCenter();
                    for(int i = 1; i < list.size();i++){
                        Vec3d o = new Vec3d(list.get(i).pos);
                        if(center.squareDistanceTo(o) <= d){
                            rioScreens.add(list.get(i));
                        }
                    }
                }
            }else{
                //todo Predict the nearest screens
            }
        }

        int s = MAX_SCORE;

        for (ScreenFocusInfo info :
                list) {
            if(info.project){
                if(rioScreens.contains(info))
                    scores.put(info.pos, (int) (s * (1 + RIO_GAIN)));
                else
                    scores.put(info.pos,s);
                if(s > 16){
                    s >>= 1;
                }
            }else{
                if(!rioScreens.contains(info)) {
                    scores.put(info.pos, ((int) (info.distance / sum[0] * s)));
                }
                else
                    scores.put(info.pos, s / rioScreens.size());

            }
        }
    }

    public static class ScreenDistanceComparator implements Comparator<ScreenFocusInfo>{

        @Override
        public int compare(ScreenFocusInfo o1, ScreenFocusInfo o2) {
            if(o1.project && !o2.project) return -1;
            if(!o1.project && o2.project) return 1;
            else{
                return o1.distance - o2.distance > 0 ? 1 : -1;
            }
        }
    }

    public static class ScreenFocusInfo{
        boolean project;
        float distance;
        BlockPos pos;

        public ScreenFocusInfo(BlockPos pos){
            this.project = false;
            this.distance = Integer.MAX_VALUE;
            this.pos = pos;
        }

        public ScreenFocusInfo(BlockPos pos,boolean isProject,float distance){
            this.project = isProject;
            this.distance = distance;
            this.pos = pos;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ScreenFocusInfo that = (ScreenFocusInfo) o;
            return pos.equals(that.pos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos);
        }
    }
}
