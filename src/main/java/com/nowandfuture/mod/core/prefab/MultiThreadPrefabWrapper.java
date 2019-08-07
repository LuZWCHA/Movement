package com.nowandfuture.mod.core.prefab;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

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
        void onCompleted(AbstractPrefab prefab);
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
    public String getName() {
        return prefab.getName();
    }

    @Override
    public int getPrefabMaxNum() {
        return prefab.getPrefabMaxNum();
    }


    @Override
    public int getConstructBlockIndex() {
        return prefab.getConstructBlockIndex();
    }


    @Override
    public BlockPos getControlPoint() {
        return prefab.getControlPoint();
    }

    @Override
    public Vec3i getSize() {
        return prefab.getSize();
    }

    public MultiThreadPrefabWrapper(){
        super();
    }

    public void set(AbstractPrefab prefab){
        this.prefab = prefab;
        constructLocalWorldThread = new ConstructLocalWorldThread(prefab);
    }

    protected MultiThreadPrefabWrapper(AbstractPrefab prefab){
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
    public void invalid() {
        prefab.invalid();
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
    public World getActrualWorld() {
        return prefab.getActrualWorld();
    }

    @Override
    public void setReady(boolean ready) {
        if(prefab != null)
            prefab.setReady(ready);
    }

    @Override
    public void constructLocalWoldFromActrualWorld() {
        if(constructLocalWorldThread == null){
            constructLocalWorldThread = new ConstructLocalWorldThread(prefab);
        }
        constructLocalWorldThread.start();
    }

    public boolean tryStopConstruct(){
        if(isConstructing()){
            if(constructLocalWorldThread.constructListener != null) {
                constructLocalWorldThread.constructListener.onError(new ForceStopException());
                constructLocalWorldThread.constructListener = null;
            }
            constructLocalWorldThread.interrupt();
            constructLocalWorldThread.prefab = null;
            constructLocalWorldThread = null;
            return true;
        }
        return false;
    }

    public boolean isConstructing(){
        return constructLocalWorldThread != null && constructLocalWorldThread.isConstructing();
    }

    @Override
    public boolean isLocalWorldInit() {
        return prefab.isLocalWorldInit();
    }

    @Override
    public boolean isReady() {
        return prefab.isReady();
    }

    public double getProgress(){
        AbstractPrefab prefab = constructLocalWorldThread.prefab;
        return prefab == null ? 0d : ((double) prefab.getConstructBlockIndex())/prefab.getPrefabMaxNum();
    }

    public static class ForceStopException extends Exception{
        public ForceStopException(){

        }
    }

    private static class ConstructLocalWorldThread extends Thread{

        private ConstructListener constructListener;
        private AbstractPrefab prefab;
        private boolean isConstructing;

        ConstructLocalWorldThread(AbstractPrefab prefab){
            this.prefab = prefab;
            this.isConstructing = false;
        }

        @Override
        public void run() {
            try {
                if(constructListener != null) {
                    constructListener.onStart();
                }

                prefab.setReady(false);
                isConstructing = true;
                prefab.constructLocalWoldFromActrualWorld();
                prefab.diffuseLight();
                isConstructing = false;
                prefab.setReady(true);

                if(constructListener != null) {
                    constructListener.onCompleted(prefab);
                }
            }catch (Exception e){
                isConstructing = false;
                if(prefab != null)
                    prefab.setReady(false);

                if(constructListener != null) {
                    constructListener.onError(e);
                }
            }finally {
                isConstructing = false;
            }
        }

        public boolean isConstructing() {
            return isConstructing;
        }
    }

}
