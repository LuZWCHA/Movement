package com.nowandfuture.mod.core.selection;

import com.nowandfuture.mod.utils.DrawHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AABBSelectArea {
    public final String NBT_AABB_MIN_X = "AxisAlignedBBMinX";
    public final String NBT_AABB_MAX_X = "AxisAlignedBBMaxX";
    public final String NBT_AABB_MIN_Y = "AxisAlignedBBMinY";
    public final String NBT_AABB_MAX_Y = "AxisAlignedBBMaxY";
    public final String NBT_AABB_MIN_Z = "AxisAlignedBBMinZ";
    public final String NBT_AABB_MAX_Z = "AxisAlignedBBMaxZ";


    private AxisAlignedBB box;
    private boolean show;
//    private Minecraft mc = Minecraft.getMinecraft();

    public AABBSelectArea(){
        show = true;
        box = new AxisAlignedBB(0,0,0,3,3,3);
    }

    public double getXLength(){
        return box.maxX - box.minX;
    }

    public double getYLength(){
        return box.maxY - box.minY;
    }

    public double getZLength(){
        return box.maxZ - box.minZ;
    }

    public void setBox(AxisAlignedBB box) {
        this.box = box;
    }

    public AxisAlignedBB getBox() {
        return box;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound){
        compound.setDouble(NBT_AABB_MAX_X,box.maxX);
        compound.setDouble(NBT_AABB_MAX_Y,box.maxY);
        compound.setDouble(NBT_AABB_MAX_Z,box.maxZ);
        compound.setDouble(NBT_AABB_MIN_X,box.minX);
        compound.setDouble(NBT_AABB_MIN_Y,box.minY);
        compound.setDouble(NBT_AABB_MIN_Z,box.minZ);

        return compound;
    }

    public void readFromNBT(NBTTagCompound compound){

        double xmax = compound.getDouble(NBT_AABB_MAX_X);
        double ymax = compound.getDouble(NBT_AABB_MAX_Y);
        double zmax = compound.getDouble(NBT_AABB_MAX_Z);
        double xmin = compound.getDouble(NBT_AABB_MIN_X);
        double ymin = compound.getDouble(NBT_AABB_MIN_Y);
        double zmin = compound.getDouble(NBT_AABB_MIN_Z);
        box = new AxisAlignedBB(xmin,ymin,zmin,xmax,ymax,zmax);
    }

    public BlockPos getPos(){
        return new BlockPos(box.minX,box.minY,box.minZ);
    }

    public BlockPos getSize(){
        return new BlockPos(box.maxX - box.minX,box.maxY - box.minY,box.maxZ - box.minZ);
    }

    /*                     [2]
    *                       |
    *        ------[3]------|------[1]------→(x)
    *                       |
    *                      [0]
     *                      |
     *                      |
    *                       V(z)
    *
     */

    private int transLookToInt(double x, double z){
        if(x> -z && x <= z){
            return 0;
        }else if(z > x && z <= -x){
            return 1;
        }else if(x >= z && x < -z){
            return 2;
        }else {
            return 3;
        }
    }

    public void setMaxX(int maxX){
        box = new AxisAlignedBB(box.minX, box.minY, box.minZ, maxX, box.maxY, box.maxZ);
    }

    public void setMaxY(double y2)
    {
        box = new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, y2, box.maxZ);
    }

    public void setMaxZ(int maxZ){
        box = new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, box.maxY,maxZ);
    }

    public void expandX(int x){
        box = box.expand(x,0,0);
    }

    public void expandY(int y){
        box = box.expand(0,y,0);
    }

    public void expandZ(int z){
        box = box.expand(0,0,z);
    }

    public void contractX(int x){
        box = box.contract(x,0,0);
    }

    public void contractY(int y){
        box = box.contract(0,y,0);
    }

    public void contractZ(int z){
        box = box.contract(0,0,z);
    }

    public void contract(int x,int y,int z){
        box = box.contract(x,y,z);
    }

    public void expand(int x,int y,int z){
        box = box.expand(x,y,z);
    }

//    public void expand(DIRECTION dir,int step){
//        Vec3d look = mc.player.getLook(0);
//        int lookNum = transLookToInt(look.x,look.z);
//
//        switch (dir){
//            case UP:
//                box = box.expand(0,step,0);
//                break;
//            case DOWN:
//                box = box.expand(0,-step,0);
//                break;
//            case LEFT:
//                switch (lookNum){
//                    case 0: box = box.expand(step,0,0);break;
//                    case 1: box = box.expand(0,0, -step);break;
//                    case 2: box = box.expand(-step,0,0);break;
//                    case 3: box = box.expand(0,0,step);break;
//                }
//                break;
//            case RIGHT:
//                switch (lookNum){
//                    case 0: box = box.expand(-step,0,0);break;
//                    case 1: box = box.expand(0,0, step);break;
//                    case 2: box = box.expand(step,0,0);break;
//                    case 3: box = box.expand(0,0,-step);break;
//                }
//                break;
//            case FORWARD:
//                switch (lookNum){
//                    case 3: box = box.expand(-step,0,0);break;
//                    case 0: box = box.expand(0,0, step);break;
//                    case 1: box = box.expand(step,0,0);break;
//                    case 2: box = box.expand(0,0,-step);break;
//                }
//                break;
//            case BACKWARD:
//                switch (lookNum){
//                    case 3: box = box.expand(step,0,0);break;
//                    case 0: box = box.expand(0,0, -step);break;
//                    case 1: box = box.expand(-step,0,0);break;
//                    case 2: box = box.expand(0,0,step);break;
//                }
//                break;
//        }
//    }
//
//    public void contract(DIRECTION dir,int step){
//        Vec3d look = mc.player.getLook(0);
//        int lookNum = transLookToInt(look.x,look.z);
//
//        switch (dir){
//            case UP:
//                box = box.contract(0,step,0);
//                break;
//            case DOWN:
//                box = box.contract(0,-step,0);
//                break;
//            case LEFT:
//                switch (lookNum){
//                    case 0: box = box.contract(step,0,0);break;
//                    case 1: box = box.contract(0,0, -step);break;
//                    case 2: box = box.contract(-step,0,0);break;
//                    case 3: box = box.contract(0,0,step);break;
//                }
//                break;
//            case RIGHT:
//                switch (lookNum){
//                    case 0: box = box.contract(-step,0,0);break;
//                    case 1: box = box.contract(0,0, step);break;
//                    case 2: box = box.contract(step,0,0);break;
//                    case 3: box = box.contract(0,0,-step);break;
//                }
//                break;
//            case FORWARD:
//                switch (lookNum){
//                    case 3: box = box.contract(-step,0,0);break;
//                    case 0: box = box.contract(0,0, step);break;
//                    case 1: box = box.contract(step,0,0);break;
//                    case 2: box = box.contract(0,0,-step);break;
//                }
//                break;
//            case BACKWARD:
//                switch (lookNum){
//                    case 3: box = box.contract(step,0,0);break;
//                    case 0: box = box.contract(0,0, -step);break;
//                    case 1: box = box.contract(-step,0,0);break;
//                    case 2: box = box.contract(0,0,step);break;
//                }
//                break;
//        }
//    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

//    public enum DIRECTION{
//        UP,DOWN,LEFT,RIGHT,FORWARD,BACKWARD;
//    }

//    public static class RenderHelper {
//
//        public static void render(AABBSelectArea selectArea,float r,float b,float g){
//            if(selectArea.isShow()) {
//                GlStateManager.enablePolygonOffset();
//                GlStateManager.doPolygonOffset(-1, -1);
//                DrawHelper.drawCube(selectArea.box, r, b, g);
//                GlStateManager.disablePolygonOffset();
//            }
//        }
//    }
}
