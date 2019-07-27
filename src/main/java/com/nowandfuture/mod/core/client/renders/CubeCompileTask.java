package com.nowandfuture.mod.core.client.renders;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.util.BlockRenderLayer;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class CubeCompileTask {

    private RenderCube renderCube;
    private final ReentrantLock lock = new ReentrantLock();

    private RegionRenderCacheBuilder cacheBuilder;

    private final boolean[] layersUsed = new boolean[BlockRenderLayer.values().length];
    private final boolean[] layersStarted = new boolean[BlockRenderLayer.values().length];

    private Status status = Status.PENDING;
    private volatile boolean finished = false;
    private boolean empty = true;

    private BufferBuilder.State state;
    private List<Runnable> finishRunnableList;
    private boolean renderingTranslucent = false;


    public CubeCompileTask(@Nonnull RenderCube cube){
        this.renderCube = cube;
        finishRunnableList = new LinkedList<>();
    }

    public boolean isEmpty()
    {
        return this.empty;
    }

    public RegionRenderCacheBuilder getCacheBuilder() {
        return cacheBuilder;
    }

    protected void setLayerUsed(BlockRenderLayer layer)
    {
        this.empty = false;
        this.layersUsed[layer.ordinal()] = true;
    }

    public boolean isLayerEmpty(BlockRenderLayer layer)
    {
        return !this.layersUsed[layer.ordinal()];
    }

    public void setLayerStarted(BlockRenderLayer layer)
    {
        this.layersStarted[layer.ordinal()] = true;
    }

    public boolean isLayerStarted(BlockRenderLayer layer)
    {
        return this.layersStarted[layer.ordinal()];
    }

    public void setCacheBuilder(RegionRenderCacheBuilder cacheBuilder) {
        this.cacheBuilder = cacheBuilder;
    }

    public void finish()
    {
        this.lock.lock();

        try
        {

            this.finished = true;
            this.status = Status.FINISHED;

            for (Runnable runnable : this.finishRunnableList)
            {
                runnable.run();
            }
        }
        finally
        {
            this.lock.unlock();
        }
    }

    public void addFinishRunnable(Runnable runnable)
    {
        this.lock.lock();

        try
        {
            this.finishRunnableList.add(runnable);

            if (this.finished)
            {
                runnable.run();
            }
        }
        finally
        {
            this.lock.unlock();
        }
    }


    @Nonnull
    public RenderCube getRenderCube() {
        return renderCube;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status state) {
        this.lock.lock();
        try {
            this.status = state;

        }finally {
            lock.unlock();
        }
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public void setState(BufferBuilder.State vertexState) {
        state = vertexState;
    }

    public BufferBuilder.State getState() {
        return state;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean isRenderingTranslucent() {
        return renderingTranslucent;
    }

    public void setRenderingTranslucent(boolean renderingTranslucent) {
        this.renderingTranslucent = renderingTranslucent;
    }

    public enum Status{
        PENDING,
        COMPILED,
        UPLOADED,
        FINISHED
    }
}
