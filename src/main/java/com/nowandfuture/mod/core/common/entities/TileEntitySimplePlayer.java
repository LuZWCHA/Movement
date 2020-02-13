package com.nowandfuture.mod.core.common.entities;

import com.google.common.collect.Lists;
import com.nowandfuture.ffmpeg.FrameGrabber;
import com.nowandfuture.ffmpeg.IMediaPlayer;
import com.nowandfuture.ffmpeg.player.JavaSoundHandler;
import com.nowandfuture.ffmpeg.player.SimplePlayer;
import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.client.renders.MinecraftJavaSoundHandler;
import com.nowandfuture.mod.core.client.renders.MinecraftOpenALSoundHandler;
import com.nowandfuture.mod.core.client.renders.MinecraftOpenGLDisplayHandler;
import com.nowandfuture.mod.core.client.renders.tiles.VideoRenderer;
import com.nowandfuture.mod.core.common.MediaPlayerServer;
import com.nowandfuture.mod.network.NetworkHandler;
import com.nowandfuture.mod.utils.math.MathHelper;
import joptsimple.internal.Strings;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;

// TODO: 2020/1/31 server sync not finished
//client only now
public class TileEntitySimplePlayer extends TileEntity implements ITickable {

    private static String NBT_URL = "Url";
    private static String NBT_BRIGHTNESS = "Brightness";
    private static String NBT_FACE = "Face";
    private static String NBT_PANEL_SIZE_X = "PanelSizeX";
    private static String NBT_PANEL_SIZE_Y = "PanelSizeY";
    private static String NBT_VOLUME = "Volume";
    private static String NBT_SYNC = "needSync";
    private static String NBT_TIME = "time";
    private static String NBT_STATE = "state";

    private boolean sync;
    private String url;
    private int brightness;
    private EnumFacing facing;
    private short width,height;

    private IMediaPlayer simplePlayer;

    private AxisAlignedBB screenAABB;

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
        width = compound.getShort(NBT_PANEL_SIZE_X);
        height = compound.getShort(NBT_PANEL_SIZE_Y);
        if(width < 1) width = 1;
        if(height < 1) height = 1;
        setFacing(EnumFacing.values()[compound.getInteger(NBT_FACE)]);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString(NBT_URL,url == null ? Strings.EMPTY:url);
        compound.setInteger(NBT_BRIGHTNESS,brightness);
        compound.setInteger(NBT_FACE,facing.ordinal());
        compound.setShort(NBT_PANEL_SIZE_X,width);
        compound.setShort(NBT_PANEL_SIZE_Y,height);
        return compound;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = new NBTTagCompound();
        return writeToNBT(compound);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if(screenAABB == null) {
            updateAABB();
        }
        return screenAABB.offset(getPos());
    }

    public AxisAlignedBB getScreenAABB() {
        if(screenAABB == null) {
            updateAABB();
        }
        return screenAABB;
    }

    @SideOnly(Side.CLIENT)
    public void updateAABB(){
        Vec3d[] panel = new Vec3d[4];
        panel[0] = new Vec3d(0,0,0);
        panel[1] = new Vec3d(0,getHeight(),0);
        panel[2] = new Vec3d(getWidth(),getHeight(),0);
        panel[3] = new Vec3d(getWidth(),0,0);

        transformPanel(panel,getFacing());

        //--------------------update render boundbox--------------------
        double temp[] = {Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE,
                Double.MIN_VALUE,Double.MIN_VALUE,Double.MIN_VALUE,};
        Lists.newArrayList(panel).forEach(new Consumer<Vec3d>() {
            @Override
            public void accept(Vec3d vec3d) {
                temp[0] = Math.min(temp[0],vec3d.x);
                temp[1] = Math.min(temp[1],vec3d.y);
                temp[2] = Math.min(temp[2],vec3d.z);
                temp[3] = Math.max(temp[3],vec3d.x);
                temp[4] = Math.max(temp[4],vec3d.y);
                temp[5] = Math.max(temp[5],vec3d.z);
            }
        });

        this.screenAABB = new AxisAlignedBB(temp[0],temp[1],temp[2],temp[3],temp[4],temp[5]).offset(0,1,0);
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 1<<8;
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
                    ((SimplePlayer)simplePlayer).setHandlers(new MinecraftOpenGLDisplayHandler(),
                            new MinecraftOpenALSoundHandler((SimplePlayer) simplePlayer,getPos().toString(),getPos()));//new MinecraftJavaSoundHandler(simplePlayer,Movement.proxy.getClientPlayer(),getPos())new MinecraftOpenALSoundHandler((SimplePlayer) simplePlayer,getPos().toString(),getPos())
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
            simplePlayer.end();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(world.isRemote){
            TileEntitySpecialRenderer renderer =
                    TileEntityRendererDispatcher.instance.getRenderer(this);
            if(renderer instanceof  VideoRenderer){
                ((VideoRenderer) renderer).clear(this);
            }
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
        if(this.facing != facing) {
            this.facing = facing;

            if(world.isRemote)
                updateAABB();
        }
    }

    @SideOnly(Side.CLIENT)
    public void setVolume(float volume){
        ((SimplePlayer)simplePlayer).setVolume(volume);
    }

    @SideOnly(Side.CLIENT)
    public float getVolume(){
        return ((SimplePlayer)simplePlayer).getVolume();
    }

    @SideOnly(Side.CLIENT)
    private void transformPanel(Vec3d[] panel, EnumFacing facing){
        switch (facing){
            case NORTH:
            case DOWN:
            case UP:
                break;
            case EAST:
                for (int i = 0;i < 4;i++) {
                    panel[i] = MathHelper.rotateAroundVector(panel[i],0,1,0,90 * 0.017453292F);
                }
                break;
            case WEST:
                for (int i = 0;i < 4;i++) {
                    panel[i] = MathHelper.rotateAroundVector(panel[i],0,1,0,-90 * 0.017453292F);
                }
                break;
            case SOUTH:
                for (int i = 0;i < 4;i++) {
                    panel[i] = MathHelper.rotateAroundVector(panel[i],0,1,0,180 * 0.017453292F);
                }
                break;
        }
    }

    public short getWidth() {
        return width;
    }

    public void setWidth(short width) {
        this.width = width;
    }

    public short getHeight() {
        return height;
    }

    public void setHeight(short height) {
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
