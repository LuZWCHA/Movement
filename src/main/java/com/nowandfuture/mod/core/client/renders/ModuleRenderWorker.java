package com.nowandfuture.mod.core.client.renders;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.nowandfuture.mod.Movement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nullable;
import java.util.List;

public class ModuleRenderWorker implements Runnable,Comparable<ModuleRenderWorker> {

    private CubeCompileTask cubeCompileTask;
    private RegionRenderCacheBuilder regionRenderCacheBuilder;

    public ModuleRenderWorker(CubeCompileTask task){
        this.cubeCompileTask = task;
    }

    @Override
    public void run() {
        try {
            if(cubeCompileTask.getStatus() == CubeCompileTask.Status.PENDING){
                compileBlocks();
                if(cubeCompileTask.getStatus() == CubeCompileTask.Status.COMPILED){
                    List<ListenableFuture<?>> futures = Lists.newArrayList();

                    for (BlockRenderLayer layer :
                            BlockRenderLayer.values()) {
                        if (cubeCompileTask.isLayerStarted(layer)) {
                            futures.add(upload(layer));
                        }
                    }
                    final ListenableFuture<List<Object>> listenableFuture = Futures.allAsList(futures);

                    cubeCompileTask.addFinishRunnable(new Runnable() {
                        @Override
                        public void run() {
                            listenableFuture.cancel(false);
                        }
                    });

                    Futures.addCallback(listenableFuture, new FutureCallback<List<Object>>(){

                        @Override
                        public void onSuccess(@Nullable List<Object> result) {
                            cubeCompileTask.getLock().lock();
                            try {
                                if (cubeCompileTask.getStatus() == CubeCompileTask.Status.COMPILED) {
                                    cubeCompileTask.setStatus(CubeCompileTask.Status.UPLOADED);

                                    cubeCompileTask.setFinished(true);
                                    ModuleRenderWorker.this.regionRenderCacheBuilder = null;
                                    freeCacheBuilder(cubeCompileTask.getCacheBuilder());
                                    cubeCompileTask.setStatus(CubeCompileTask.Status.FINISHED);

                                } else {
                                    if(!cubeCompileTask.isFinished())
                                        Movement.logger.warn("upload failed!");
                                }
                                cubeCompileTask.getRenderCube().setCompiling(false);

                            }finally {
                                cubeCompileTask.getLock().unlock();
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            ModuleRenderWorker.this.regionRenderCacheBuilder = null;
                            freeCacheBuilder(cubeCompileTask.getCacheBuilder());
//                            cubeCompileTask.setStatus(CubeCompileTask.Status.PENDING);
                            cubeCompileTask.getRenderCube().setCompiling(false);
                            Movement.logger.warn("upload failed:" + t.getMessage());
                        }
                    });



                }else{
                    if(cubeCompileTask == null || cubeCompileTask.isFinished()){
                        Movement.logger.info("force finished");
                    }
                    Movement.logger.warn("compileBlocks failed!");
                    cubeCompileTask.getLock().lock();
                    try {
                        cubeCompileTask.getRenderCube().setCompiling(false);
                    }finally {
                        cubeCompileTask.getLock().unlock();
                    }
                }
            }
        } catch (InterruptedException e){
            cubeCompileTask.getRenderCube().setCompiling(false);
            Movement.logger.warn("interruped during compileBlocks" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void compileBlocks() throws InterruptedException{
        cubeCompileTask.getLock().lock();
        try{
            cubeCompileTask.setCacheBuilder(this.allocateRenderBuilder());
            this.regionRenderCacheBuilder = cubeCompileTask.getCacheBuilder();

            if(!cubeCompileTask.isRenderingTranslucent()){
                if(cubeCompileTask.getRenderCube().build()) {
                    cubeCompileTask.setStatus(CubeCompileTask.Status.COMPILED);
                }else{
                    throw new InterruptedException();
                }
            }else{
                cubeCompileTask.getRenderCube().restoreTranslucent();
                cubeCompileTask.setStatus(CubeCompileTask.Status.COMPILED);
            }
        }finally {
            cubeCompileTask.getLock().unlock();
        }
    }

    public ListenableFuture upload(BlockRenderLayer layer){
        return cubeCompileTask.getRenderCube().upload(layer,getDistance());
    }

    public void freeCacheBuilder(RegionRenderCacheBuilder regionRenderCacheBuilder){
        if(regionRenderCacheBuilder != null)
            ModuleRenderManager.INSTANCE.freeRenderBuilder(regionRenderCacheBuilder);

    }

    public RegionRenderCacheBuilder allocateRenderBuilder() throws InterruptedException {
        return regionRenderCacheBuilder == null ? ModuleRenderManager.INSTANCE.allocateRenderBuilder() : regionRenderCacheBuilder;
    }

    public double getDistance(){
        Entity entity = Minecraft.getMinecraft().player;
        if(entity != null) {
            Vector3f vector3f = cubeCompileTask.getRenderCube().getTransformedBasePos();
            return entity.getDistance(vector3f.x,vector3f.y,vector3f.z);
        }
        return 0;
    }

    @Override
    public int compareTo(ModuleRenderWorker o) {
        return (int) (this.getDistance() - o.getDistance());
    }
}
