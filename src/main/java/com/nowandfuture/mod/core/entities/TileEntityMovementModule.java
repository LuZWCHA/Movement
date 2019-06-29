package com.nowandfuture.mod.core.entities;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.api.IModule;
import com.nowandfuture.mod.core.movecontrol.ModuleBase;
import com.nowandfuture.mod.core.transformers.AbstractTransformNode;
import com.nowandfuture.mod.core.transformers.animation.KeyFrameLine;
import com.nowandfuture.mod.core.prefab.AbstractPrefab;
import com.sun.istack.internal.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class TileEntityMovementModule extends TileEntity implements IModule,ITickable {
    private ModuleBase moduleBase;

    public TileEntityMovementModule(){
        moduleBase = new ModuleBase();
        moduleBase.setModuleWorld(world);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }

    public void setModuleBase(ModuleBase moduleBase) {
        this.moduleBase = moduleBase;
    }

    public ModuleBase getModuleBase() {
        return moduleBase;
    }

    public void setPrefab(AbstractPrefab recipe) {
        moduleBase.setPrefab(recipe);
    }

    public void setModulePos(BlockPos posIn) {
        moduleBase.setModulePos(posIn);
    }

    public void setAuthor(String author) {
        moduleBase.setAuthor(author);
    }

    public void setName(String name) {
        moduleBase.setName(name);
    }

    public void enable() {
        moduleBase.enable();
    }

    public void disable() {
        moduleBase.disable();
    }

    public boolean isEnable() {
        return moduleBase.isEnable() && !isInvalid();
    }

    public boolean canRender(double renderPosX, double renderPosY, double renderPosZ) {
        return isRenderValid() && getMaxRenderDistanceSquared() >= getDistanceSq(renderPosX,renderPosY,renderPosZ);
    }

    public BlockPos getModulePos() {
        return moduleBase.getModulePos();
    }

    public void setTransformNode(@Nonnull AbstractTransformNode part) {
        moduleBase.setPart(part);
    }

    public void removePartIfExit() {
        moduleBase.removePartIfExit();
    }

    public void constructPrefab() {
        moduleBase.constructPrefab();
    }

    public void render(int pass,float p) {
        moduleBase.render(pass,p);
    }

    public boolean isRenderValid() {
        return moduleBase.isRenderValid();
    }

    public void update() {
        if(world.isRemote){

        }
        moduleBase.update();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if(world.isRemote)
            moduleBase.invalidate();
    }

    @Override
    protected void setWorldCreate(World worldIn) {
        world = worldIn;
        moduleBase.setModuleWorld(worldIn);
    }

    @Override
    public void setWorld(World worldIn) {
        super.setWorld(worldIn);
        moduleBase.setModuleWorld(worldIn);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        moduleBase.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        return moduleBase.writeToNBT(compound);
    }

    public void readModuleFromNBT(NBTTagCompound compound) {
        moduleBase.readModuleFromNBT(compound);
    }

    public NBTTagCompound writeModuleToNBT(NBTTagCompound compound) {
        return moduleBase.writeModuleToNBT(compound);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = new NBTTagCompound();
        return writeToNBT(compound);
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(),1,getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        Movement.logger.info("onDataPacket");
        NBTTagCompound nbtGet = pkt.getNbtCompound();
        this.readFromNBT(nbtGet);
    }

    public KeyFrameLine getLine() {
        return moduleBase.getLine();
    }
}
