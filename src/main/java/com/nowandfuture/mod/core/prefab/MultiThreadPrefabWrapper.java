package com.nowandfuture.mod.core.prefab;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;

public class MultiThreadPrefabWrapper extends AbstractPrefab {
    private AbstractPrefab prefab;
    private ConstructLocalWorldThread constructLocalWorldThread;

    public void setConstructListener(ConstructListener constructListener) {
        constructLocalWorldThread.constructListener = constructListener;
    }

    public interface ConstructListener{
        void onError(Exception e);
        void onStart();
        void onCompleted();
    }

    @Override
    public void setControlPoint(BlockPos controlPoint) {
        prefab.setControlPoint(controlPoint);
    }

    @Override
    public BlockPos getBasePos() {
        return prefab.getBasePos();
    }

    @Override
    public void setBaseLocation(@Nonnull BlockPos baseLocation) {
        prefab.setBaseLocation(baseLocation);
    }

    @Override
    public Vector3f getTransformedBasePos() {
        return prefab.getTransformedBasePos();
    }

    public Vector3f getTransformedPos(Vector3f vector3f){
        return prefab.getTransformedPos(vector3f);
    }

    @Override
    public BlockPos getControlPoint() {
        return prefab.getControlPoint();
    }

    @Override
    public Vec3i getSize() {
        return prefab.getSize();
    }

    @Override
    public void renderPre(float p) {
        prefab.renderPre(p);
    }

    public void renderPost(float p){
        prefab.renderPost(p);
    }

    public MultiThreadPrefabWrapper(AbstractPrefab prefab){
        super(prefab.getActrualWorld(), prefab.getBasePos(), prefab.size);
        this.prefab = prefab;
        constructLocalWorldThread = new ConstructLocalWorldThread(prefab);
    }

    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt){
        return prefab.writeToNBT(nbt);
    }

    @Override
    public void decompressLocalBlocks(@Nonnull NBTTagCompound nbt){
        prefab.decompressLocalBlocks(nbt);
    }

    @Override
    public void compressLocalBlocks(@Nonnull NBTTagCompound nbt){
        prefab.compressLocalBlocks(nbt);
    }

    @Override
    public void clear() {
        prefab.clear();
    }

    @Override
    public boolean equals(Object o) {
        return prefab.equals(o);
    }

    @Override
    public int hashCode() {
        return prefab.hashCode();
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbt,@Nonnull World world) {
        prefab.readFromNBT(nbt, world);
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbt) {
        prefab.readFromNBT(nbt);
    }

    @Override
    public void render(float p) {
        prefab.render(p);
    }

    @Override
    public World getActrualWorld() {
        return prefab.getActrualWorld();
    }

    @Override
    public void constructLocalWoldFromActrualWorld() {
        if(constructLocalWorldThread == null){
            constructLocalWorldThread = new ConstructLocalWorldThread(prefab);
        }
        constructLocalWorldThread.start();
    }

    private static class ConstructLocalWorldThread extends  Thread{

        private ConstructListener constructListener;
        private AbstractPrefab prefab;

        ConstructLocalWorldThread(AbstractPrefab prefab){
            this.prefab = prefab;
        }

        @Override
        public void run() {
            try {
                if(constructListener != null)
                    constructListener.onStart();
                prefab.constructLocalWoldFromActrualWorld();
                if(constructListener != null)
                    constructListener.onCompleted();
            }catch (InterruptedException e){
                if(constructListener != null)
                    constructListener.onError(e);
            }
        }

    }

}
