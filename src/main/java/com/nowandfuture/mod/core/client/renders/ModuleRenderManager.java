package com.nowandfuture.mod.core.client.renders;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.FutureCallback;
import com.nowandfuture.asm.IRender;
import com.nowandfuture.mod.core.prefab.AbstractPrefab;
import com.nowandfuture.mod.core.selection.OBBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public enum ModuleRenderManager {
    INSTANCE;
    private ClippingHelperExt clippingHelper;
    private PriorityBlockingQueue<Runnable> priorityBlockingQueue;
    private final static long ALIVE_TIME = 0;
    private ThreadPoolExecutor executor;
    private ChunkRenderDispatcher chunkRenderDispatcher;
    private BlockingQueue<RegionRenderCacheBuilder> queueFreeRenderBuilders;

    private Map<AbstractPrefab,CubesRenderer> cubesRendererMap;
    private int cpuCores;
    private boolean isInit = false;

    private Queue<IRender> renderQueue;

    ModuleRenderManager(){
        init();
    }

    public boolean isInit() {
        return isInit;
    }

    public void init(){
//        try {
//            chunkRenderDispatcher = ReflectionHelper.getPrivateValue(RenderGlobal.class, Minecraft.getMinecraft().renderGlobal,"renderDispatcher","field_174995_M");
//        }catch (Exception e){
//            e.printStackTrace();
//            chunkRenderDispatcher = null;
//        }

        int i = Math.max(1, (int)((double)Runtime.getRuntime().maxMemory() * 0.15D) / 10485760);
        cpuCores = Math.max(1, MathHelper.clamp(Runtime.getRuntime().availableProcessors(), 1, i / 2));
        clippingHelper = new ClippingHelperExt();
        priorityBlockingQueue = new PriorityBlockingQueue<>();
        initExecutor(cpuCores);
        if(chunkRenderDispatcher == null) {
            queueFreeRenderBuilders = Queues.newArrayBlockingQueue(i);

            int countRenderBuilders = MathHelper.clamp(cpuCores * 10, 1, i);
            for (int l = 0; l < countRenderBuilders; ++l) {
                this.queueFreeRenderBuilders.add(new RegionRenderCacheBuilder());
            }
        }

        cubesRendererMap = new HashMap<>();
        renderQueue = new LinkedList<>();
        isInit = true;
    }

    private void initExecutor(int maxSize){
        executor = new ThreadPoolExecutor(1,maxSize,ALIVE_TIME,TimeUnit.MILLISECONDS,priorityBlockingQueue);
    }

    public void addCubesRenderer(@Nonnull AbstractPrefab abstractPrefab){
        CubesRenderer renderer = new CubesRenderer(abstractPrefab);
        cubesRendererMap.put(abstractPrefab,renderer);
    }

    public void removeCubesRenderer(@Nonnull AbstractPrefab prefab){
        CubesRenderer cubesRenderer = cubesRendererMap.get(prefab);
        if(cubesRenderer != null) {
            cubesRenderer.invalid();
            cubesRendererMap.remove(prefab);
        }
    }

    public CubesRenderer getRenderer(@Nonnull AbstractPrefab prefab){
        return cubesRendererMap.get(prefab);
    }

    public void invalid(){
        if(!executor.isShutdown())
            executor.shutdownNow();
        priorityBlockingQueue.clear();
        renderQueue.clear();

        for (CubesRenderer c :
                cubesRendererMap.values()) {
            c.invalid();
        }
        cubesRendererMap.clear();
        init();
    }

    public ThreadPoolExecutor getExecutor(){
        return executor;
    }

    public RegionRenderCacheBuilder allocateRenderBuilder() throws InterruptedException {
        return chunkRenderDispatcher == null ? this.queueFreeRenderBuilders.take() : chunkRenderDispatcher.allocateRenderBuilder();
    }

    public void freeRenderBuilder(RegionRenderCacheBuilder regionRenderCacheBuilder){
        if (chunkRenderDispatcher == null) {
            this.queueFreeRenderBuilders.add(regionRenderCacheBuilder);
        } else {
            chunkRenderDispatcher.freeRenderBuilder(regionRenderCacheBuilder);
        }
    }

    public void execute(Runnable runnable){

        executor.execute(runnable);
    }

    public boolean remove(Runnable runnable){
        return executor.remove(runnable);
    }

    public ClippingHelperExt getClippingHelper() {
        return clippingHelper;
    }

    public Queue<IRender> getRenderQueue() {
        return renderQueue;
    }

    public static class ClippingHelperExt extends ClippingHelper {

        private final ClippingHelper clippingHelper;

        public ClippingHelperExt(){
            clippingHelper = ClippingHelperImpl.getInstance();
        }

        //copy from ClippingHelperImpl
        public double dot(float[] p_178624_1_, double p_178624_2_, double p_178624_4_, double p_178624_6_)
        {
            return (double)p_178624_1_[0] * p_178624_2_ + (double)p_178624_1_[1] * p_178624_4_ + (double)p_178624_1_[2] * p_178624_6_ + (double)p_178624_1_[3];
        }

        public boolean isOBBInFrustum(OBBox bounding){

            for (int i = 0; i < 6; ++i)
            {
                float[] afloat = clippingHelper.frustum[i];

                if (this.dot(afloat, bounding.getXyz000().x, bounding.getXyz000().y, bounding.getXyz000().z) <= 0.0D && this.dot(afloat, bounding.getXyz001().x, bounding.getXyz001().y, bounding.getXyz001().z) <= 0.0D && this.dot(afloat, bounding.getXyz010().x, bounding.getXyz010().y, bounding.getXyz010().z) <= 0.0D && this.dot(afloat, bounding.getXyz011().x, bounding.getXyz011().y, bounding.getXyz011().z) <= 0.0D && this.dot(afloat, bounding.getXyz100().x, bounding.getXyz100().y, bounding.getXyz100().z) <= 0.0D && this.dot(afloat, bounding.getXyz101().x, bounding.getXyz101().y, bounding.getXyz101().z) <= 0.0D && this.dot(afloat, bounding.getXyz110().x, bounding.getXyz110().y, bounding.getXyz110().z) <= 0.0D && this.dot(afloat, bounding.getXyz111().x, bounding.getXyz111().y, bounding.getXyz111().z) <= 0.0D)
                {
                    return false;
                }
            }

            return true;
        }

        public boolean isOBBInFrustum(Vec3d point0, Vec3d point1, Vec3d point2, Vec3d point3, Vec3d point4, Vec3d point5, Vec3d point6, Vec3d point7){

            for (int i = 0; i < 6; ++i)
            {
                float[] afloat = clippingHelper.frustum[i];

                if (this.dot(afloat, point0.x, point0.y, point0.z) <= 0.0D && this.dot(afloat, point1.x, point1.y, point1.z) <= 0.0D && this.dot(afloat, point2.x, point2.y, point2.z) <= 0.0D && this.dot(afloat, point3.x, point3.y, point3.z) <= 0.0D && this.dot(afloat, point4.x, point4.y, point4.z) <= 0.0D && this.dot(afloat, point5.x, point5.y, point5.z) <= 0.0D && this.dot(afloat, point6.x, point6.y, point6.z) <= 0.0D && this.dot(afloat, point7.x, point7.y, point7.z) <= 0.0D)
                {
                    return false;
                }
            }

            return true;
        }

        @Override
        public boolean isBoxInFrustum(double p_78553_1_, double p_78553_3_, double p_78553_5_, double p_78553_7_, double p_78553_9_, double p_78553_11_) {
            return clippingHelper.isBoxInFrustum(p_78553_1_, p_78553_3_, p_78553_5_, p_78553_7_, p_78553_9_, p_78553_11_);
        }
    }

}
