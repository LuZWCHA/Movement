package com.nowandfuture.mod.core.client.renders.tiles;

import com.nowandfuture.ffmpeg.player.SimplePlayer;
import com.nowandfuture.mod.core.client.renders.FrameTexture;
import com.nowandfuture.mod.core.common.entities.TileEntityConstructor;
import com.nowandfuture.mod.utils.DrawHelper;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SelectAreaRenderer extends TileEntitySpecialRenderer<TileEntityConstructor> {
    SimplePlayer simplePlayer;
    FrameTexture imageTexture;
    long lastTimeStamp = -1;

    public SelectAreaRenderer(){
        super();
        simplePlayer = new SimplePlayer();
    }

    @Override
    public void render(TileEntityConstructor te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 1,y,z);
        DrawHelper.render(te.getAABBSelectArea(),0,0,0);
        GlStateManager.popMatrix();



//        if(te.isLock()){
//            simplePlayer.prepare(new OpenGLDisplayHandler(),new JavaSoundHandler(simplePlayer));
//            try {
//                simplePlayer.play("D:\\迅雷下载\\【minecraft】你能坚持多久不说卧槽？ MiniaTuria材质练习-1214\\test.flv");
//            } catch (FrameGrabber.Exception e) {
//                e.printStackTrace();
//            }
//            te.setLock(false);
//        }

    }

    @Override
    public boolean isGlobalRenderer(TileEntityConstructor te) {
        return true;
    }
}
