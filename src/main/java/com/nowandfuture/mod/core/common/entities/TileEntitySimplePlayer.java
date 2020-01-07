package com.nowandfuture.mod.core.common.entities;

import com.nowandfuture.ffmpeg.FrameGrabber;
import com.nowandfuture.ffmpeg.IMediaPlayer;
import com.nowandfuture.ffmpeg.player.JavaSoundHandler;
import com.nowandfuture.ffmpeg.player.OpenGLDisplayHandler;
import com.nowandfuture.ffmpeg.player.SimplePlayer;
import com.nowandfuture.mod.core.common.MediaPlayerServer;
import com.nowandfuture.mod.network.NetworkHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntitySimplePlayer extends TileEntity implements ITickable {

    private boolean sync;
    private String url;

    //---------------------------------------client--------------------------------------
    private IMediaPlayer simplePlayer;

    public TileEntitySimplePlayer(){

    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    @SideOnly(Side.CLIENT)
    public void prepare(){
        simplePlayer.prepare(new OpenGLDisplayHandler(),new JavaSoundHandler(simplePlayer));
    }

    public boolean touchSource(String url) throws FrameGrabber.Exception {
       return simplePlayer.touchSource(url);
    }

    public void play() throws Exception {
        simplePlayer.play();
    }

    public void pause(){
        simplePlayer.pause();
    }

    public void resume(){
        simplePlayer.resume();
    }

    public void end() throws Exception {
        simplePlayer.end();
    }

    public boolean seekTo(long time){
        return simplePlayer.seekTo(time);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        return compound;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = new NBTTagCompound();

        return writeToNBT(compound);
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(),1,getUpdateTag());
    }

    public void setSimplePlayer(IMediaPlayer simplePlayer) {
        this.simplePlayer = simplePlayer;
    }

    public IMediaPlayer getSimplePlayer() {
        return simplePlayer;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if(!world.isRemote){
            simplePlayer = new SimplePlayer();
            prepare();
        }else{
            simplePlayer = new MediaPlayerServer();
            simplePlayer.prepare(null,null);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        try {
            simplePlayer.end();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update() {
        if(simplePlayer != null){
            if(!world.isRemote){
                if(simplePlayer.getSyncInfo().sysStartTime == -1){
                    simplePlayer.getSyncInfo().sysStartTime = System.currentTimeMillis();
                }
                simplePlayer.getSyncInfo().setLastTime(System.currentTimeMillis());

                long time = System.currentTimeMillis() - simplePlayer.getSyncInfo().sysStartTime;

                // TODO: 2019/12/27 sync with client per-tick
                NetworkHandler.syncToTrackingClients(world,this,getUpdatePacket());

            }else{
                //do nothing
            }
        }
    }
}
