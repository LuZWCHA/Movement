package com.nowandfuture.mod.core.common.entities;

import com.nowandfuture.mod.core.client.renders.TransformedBlockRenderMap;
import com.nowandfuture.mod.core.common.TransformedBlockWorld;
import com.nowandfuture.mod.core.common.blocks.TransformedBlock;
import com.nowandfuture.mod.handler.IKeyListener;
import com.nowandfuture.mod.handler.KeyBindHandler;
import com.nowandfuture.mod.network.NetworkHandler;
import com.nowandfuture.mod.network.message.LMessage;
import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class TileEntityTransformedBlock extends TileEntity implements ITickable,IKeyListener {
    private static String NBT_CONTAIN_BLOCK = "BlockId";
    private static String NBT_ROT_X = "RotX";
    private static String NBT_ROT_Y = "RotY";
    private static String NBT_ROT_Z = "RotZ";

    private TransformedBlock.BlockWrapper localBlock;
    private TransformedBlockWorld transformedBlockWorld;
    private Vector3f rotVec;

    public static float STEP = 10;

    //----------------------------------------client------------------------------------------
    private boolean isEdited;
    private boolean updateVBO;


    public TileEntityTransformedBlock(){
        localBlock = new TransformedBlock.BlockWrapper();
        rotVec = new Vector3f();

        isEdited = false;
        updateVBO = false;
    }

    public Vector3f getRotVec() {
        return rotVec;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return super.shouldRenderInPass(pass);
    }

    @Override
    public int getBlockMetadata() {
        return localBlock.blockState.getBlock().getMetaFromState(localBlock.blockState);
    }

    @Override
    public Block getBlockType() {
        if (this.blockType == null && this.world != null)
        {
            this.blockType = getLocalBlock().blockState.getBlock();
        }

        return this.blockType;
    }


    @Override
    public void validate() {
        if(localBlock.tileEntity != null){
            (localBlock.tileEntity).validate();
        }

        if(world.isRemote)
            KeyBindHandler.register(this);
        super.validate();
    }

    public void setLocalBlock(TransformedBlock.BlockWrapper localBlock) {
        transformedBlockWorld.setBlockWrapper(localBlock);
        this.localBlock = localBlock;
    }

    public TransformedBlock.BlockWrapper getLocalBlock() {
        return localBlock;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        readFromNBT(pkt.getNbtCompound());

        if(world.isRemote) {
            setUpdateVBO(true);
        }
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos,1,getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = super.getUpdateTag();
        return writeToNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
         compound = super.writeToNBT(compound);
         if(localBlock != null && localBlock.blockState != null){
             compound.setInteger(NBT_CONTAIN_BLOCK, Block.getStateId(localBlock.blockState));
             compound.setFloat(NBT_ROT_X, rotVec.x);
             compound.setFloat(NBT_ROT_Y, rotVec.y);
             compound.setFloat(NBT_ROT_Z, rotVec.z);
         }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if(!compound.hasKey(NBT_CONTAIN_BLOCK)) return;

        int id = compound.getInteger(NBT_CONTAIN_BLOCK);

        rotVec.x = compound.getFloat(NBT_ROT_X);
        rotVec.y = compound.getFloat(NBT_ROT_Y);
        rotVec.z = compound.getFloat(NBT_ROT_Z);

        if(id > 0){
            localBlock.blockState = Block.getStateById(id);
            IBlockState blockState = localBlock.blockState;
            TileEntity tileEntity = localBlock.tileEntity;
            if(blockState.getBlock().hasTileEntity(blockState) &&
                    (tileEntity == null || Block.getStateId(blockState) != id)){
                if(tileEntity != null) {
                    tileEntity.invalidate();
                }

                localBlock.tileEntity = blockState.getBlock().createTileEntity(
                        world, blockState);
                if(transformedBlockWorld == null){
                    transformedBlockWorld = new TransformedBlockWorld(world,localBlock,pos);
                }
                localBlock.tileEntity.setWorld(transformedBlockWorld);
                localBlock.tileEntity.setPos(pos);
                localBlock.tileEntity.validate();
                localBlock.tileEntity.onLoad();
            }
        }
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return super.getMaxRenderDistanceSquared();
    }

    @Override
    public void setWorld(World worldIn) {
        transformedBlockWorld = new TransformedBlockWorld(worldIn, localBlock, pos);
        if (localBlock.tileEntity != null) {
            transformedBlockWorld.setRealWorld(worldIn);
            (localBlock.tileEntity).setWorld(transformedBlockWorld);
        }

        super.setWorld(worldIn);
    }

    @Override
    protected void setWorldCreate(World worldIn) {
        transformedBlockWorld = new TransformedBlockWorld(worldIn, localBlock, pos);
        if (localBlock.tileEntity != null) {
            transformedBlockWorld.setRealWorld(worldIn);
            (localBlock.tileEntity).setWorld(transformedBlockWorld);
        }

        super.setWorldCreate(worldIn);
    }

    @Override
    public void update() {
        if(localBlock != null && localBlock.tileEntity instanceof ITickable){
            ((ITickable) localBlock.tileEntity).update();
        }
    }

    @Override
    public void onLoad() {
        if(localBlock != null && localBlock.tileEntity != null){
            (localBlock.tileEntity).onLoad();
        }
        super.onLoad();
    }

    @Override
    public void invalidate() {
        if(localBlock != null && localBlock.tileEntity != null){
            (localBlock.tileEntity).invalidate();
        }

        if(world.isRemote){
            if(isEdited)
                TransformedBlockRenderMap.INSTANCE.setHasEdited(false);
            isEdited = false;
            clearGLBuffer();
            KeyBindHandler.unregister(this);
        }

        super.invalidate();
    }

    @SideOnly(Side.CLIENT)
    public void clearGLBuffer(){
        TransformedBlockRenderMap.INSTANCE.removeRender(getPos());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onKeyDown() {
        if(!world.isRemote) return;
        if(isInvalid()){
            KeyBindHandler.unregister(this);
            return;
        }
        EntityPlayer player = Minecraft.getMinecraft().player;

        if(isEdited() && player != null){

            BlockPos pos = player.getPosition();
            pos.add(0,player.getEyeHeight(),0);

            boolean changed = true;
            Vector3f rot = getRotVec();
            if(KeyBindHandler.keyYRotN.isKeyDown()){
                rot.setY((rot.y + STEP)%360);
            }else if(KeyBindHandler.keyYRotP.isKeyDown()){
                rot.setY((rot.y - STEP)%360);
            }else if(KeyBindHandler.keyXRotN.isKeyDown()){
                rot.setX((rot.x + STEP)%360);
            }else if(KeyBindHandler.keyXRotP.isKeyDown()){
                rot.setX((rot.x - STEP)%360);
            }else if(KeyBindHandler.keyZRotP.isKeyDown()){
                rot.setZ((rot.z + STEP)%360);
            }else if(KeyBindHandler.keyZRotN.isKeyDown()){
                rot.setZ((rot.z - STEP)%360);
            }else if(Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown()){
                rot.set(0,0,0);
            }else{
                changed = false;
            }

            if(changed) {
                LMessage.NBTMessage nbtMessage =
                        new LMessage.NBTMessage(LMessage.NBTMessage.TRANSFORMED_BLOCK_FLAG, getUpdateTag());
                nbtMessage.setPos(getPos());
                NetworkHandler.INSTANCE.sendMessageToServer(nbtMessage);
            }

            KeyBinding.unPressAllKeys();
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean isEdited() {
        return isEdited;
    }

    @SideOnly(Side.CLIENT)
    public void setEdited(boolean edited) {
        isEdited = edited;
    }

    @SideOnly(Side.CLIENT)
    public void toggleEditState(){
        isEdited = !isEdited;
    }

    @SideOnly(Side.CLIENT)
    public boolean isUpdateVBO() {
        return updateVBO;
    }

    @SideOnly(Side.CLIENT)
    public void setUpdateVBO(boolean updateVBO) {
        this.updateVBO = updateVBO;
    }
}
