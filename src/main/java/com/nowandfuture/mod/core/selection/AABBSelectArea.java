package com.nowandfuture.mod.core.selection;

import com.nowandfuture.mod.utils.DrawHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AABBSelectArea {
    private AxisAlignedBB box;
    private boolean show;
    private Minecraft mc = Minecraft.getMinecraft();

    public AABBSelectArea(){
        show = false;
        box = new AxisAlignedBB(0,0,0,1,1,1);
    }

    public void setBox(AxisAlignedBB box) {
        this.box = box;
    }

    public AxisAlignedBB getBox() {
        return box;
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

    public void expand(DIRECTION dir,int step){
        Vec3d look = mc.player.getLook(0);
        int lookNum = transLookToInt(look.x,look.z);

        switch (dir){
            case UP:
                box = box.expand(0,step,0);
                break;
            case DOWN:
                box = box.expand(0,-step,0);
                break;
            case LEFT:
                switch (lookNum){
                    case 0: box = box.expand(step,0,0);break;
                    case 1: box = box.expand(0,0, -step);break;
                    case 2: box = box.expand(-step,0,0);break;
                    case 3: box = box.expand(0,0,step);break;
                }
                break;
            case RIGHT:
                switch (lookNum){
                    case 0: box = box.expand(-step,0,0);break;
                    case 1: box = box.expand(0,0, step);break;
                    case 2: box = box.expand(step,0,0);break;
                    case 3: box = box.expand(0,0,-step);break;
                }
                break;
            case FORWARD:
                switch (lookNum){
                    case 3: box = box.expand(-step,0,0);break;
                    case 0: box = box.expand(0,0, step);break;
                    case 1: box = box.expand(step,0,0);break;
                    case 2: box = box.expand(0,0,-step);break;
                }
                break;
            case BACKWARD:
                switch (lookNum){
                    case 3: box = box.expand(step,0,0);break;
                    case 0: box = box.expand(0,0, -step);break;
                    case 1: box = box.expand(-step,0,0);break;
                    case 2: box = box.expand(0,0,step);break;
                }
                break;
        }
    }

    public void contract(DIRECTION dir,int step){
        Vec3d look = mc.player.getLook(0);
        int lookNum = transLookToInt(look.x,look.z);

        switch (dir){
            case UP:
                box = box.contract(0,step,0);
                break;
            case DOWN:
                box = box.contract(0,-step,0);
                break;
            case LEFT:
                switch (lookNum){
                    case 0: box = box.contract(step,0,0);break;
                    case 1: box = box.contract(0,0, -step);break;
                    case 2: box = box.contract(-step,0,0);break;
                    case 3: box = box.contract(0,0,step);break;
                }
                break;
            case RIGHT:
                switch (lookNum){
                    case 0: box = box.contract(-step,0,0);break;
                    case 1: box = box.contract(0,0, step);break;
                    case 2: box = box.contract(step,0,0);break;
                    case 3: box = box.contract(0,0,-step);break;
                }
                break;
            case FORWARD:
                switch (lookNum){
                    case 3: box = box.contract(-step,0,0);break;
                    case 0: box = box.contract(0,0, step);break;
                    case 1: box = box.contract(step,0,0);break;
                    case 2: box = box.contract(0,0,-step);break;
                }
                break;
            case BACKWARD:
                switch (lookNum){
                    case 3: box = box.contract(step,0,0);break;
                    case 0: box = box.contract(0,0, -step);break;
                    case 1: box = box.contract(-step,0,0);break;
                    case 2: box = box.contract(0,0,step);break;
                }
                break;
        }
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public enum DIRECTION{
        UP,DOWN,LEFT,RIGHT,FORWARD,BACKWARD;
    }

    public static class Renderer{
        private float r = 1;
        private float g = 1;
        private float b = 1;
        private float alpha = 1;
        private int width = 2;
        private AABBSelectArea selectArea;

        public Renderer(AABBSelectArea aabbSelectArea){
            selectArea = aabbSelectArea;
        }

        public void render(){
            GlStateManager.enablePolygonOffset();
            GlStateManager.doPolygonOffset(-1,-1);
            DrawHelper.drawCube(selectArea.box,r,b,g);
            GlStateManager.disablePolygonOffset();
        }
    }
}
