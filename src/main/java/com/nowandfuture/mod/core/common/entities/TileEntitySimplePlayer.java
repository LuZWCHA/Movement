package com.nowandfuture.mod.core.common.entities;

import com.nowandfuture.ffmpeg.FrameGrabber;
import com.nowandfuture.ffmpeg.IMediaPlayer;
import com.nowandfuture.ffmpeg.player.JavaSoundHandler;
import com.nowandfuture.ffmpeg.player.OpenGLDisplayHandler;
import com.nowandfuture.ffmpeg.player.SimplePlayer;
import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.client.renders.MinecraftOpenGLDisplayHandler;
import com.nowandfuture.mod.core.common.MediaPlayerServer;
import com.nowandfuture.mod.network.NetworkHandler;
import joptsimple.internal.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// TODO: 2020/1/31 server sync not finished
//client only now
public class TileEntitySimplePlayer extends TileEntity implements ITickable {

    private static String NBT_URL = "Url";
    private static String NBT_BRIGHTNESS = "Brightness";
    private static String NBT_FACE = "Face";
    private static String NBT_SYNC = "needSync";
    private static String NBT_TIME = "time";
    private static String NBT_STATE = "state";

    private boolean sync;
    private String url;
    private int brightness;
    private EnumFacing facing;
    private float width,height;

    private IMediaPlayer simplePlayer;

    public TileEntitySimplePlayer(){
        width = 4;
        height = 4;
        facing = EnumFacing.NORTH;
        brightness = 15;
        sync = false;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }


    public void prepare(){
        simplePlayer.prepare();
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
        url = compound.getString(NBT_URL);
        brightness = compound.getInteger(NBT_BRIGHTNESS);
        facing = EnumFacing.values()[compound.getInteger(NBT_FACE)];
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString(NBT_URL,url == null ? Strings.EMPTY:url);
        compound.setInteger(NBT_BRIGHTNESS,brightness);
        compound.setInteger(NBT_FACE,facing.ordinal());
        return compound;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = new NBTTagCompound();
        return writeToNBT(compound);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 256;
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
        if(world.isRemote){
            simplePlayer = new SimplePlayer();
            Movement.proxy.addScheduledTaskClient(new Runnable() {
                @Override
                public void run() {
                    ((SimplePlayer)simplePlayer).setHandlers(new MinecraftOpenGLDisplayHandler(),new JavaSoundHandler(simplePlayer));
                    prepare();
                }
            });

        }else{
            simplePlayer = new MediaPlayerServer();
            simplePlayer.prepare();
        }
    }

    @Override
    public void invalidate() {
        try {
            System.out.println("invalidate");
            simplePlayer.end();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.invalidate();
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

                if(time >= simplePlayer.getTotalTime()){
                    try {
                        simplePlayer.end();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    simplePlayer.seekTo(time);
                }

                if(sync)
                // TODO: 2019/12/27 sync with client per-tick
                    NetworkHandler.syncToTrackingClients(world,this,getUpdatePacket());

            }else{
                //do nothing
            }
        }
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
    }

    public EnumFacing getFacing() {
        return facing;
    }

    public void setFacing(EnumFacing facing) {
        if(facing == EnumFacing.DOWN || facing == EnumFacing.UP){
            facing = EnumFacing.NORTH;
        }
        this.facing = facing;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return super.shouldRenderInPass(pass);
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public int getVideoWidth(){
        return simplePlayer.getWidth();
    }

    public int getVideoHeight(){
        return simplePlayer.getHeight();
    }
}
