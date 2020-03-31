package com.nowandfuture.mod.core.client.renders;

import com.creativemd.creativecore.client.mods.optifine.OptifineHelper;
import com.google.common.base.Objects;
import com.google.common.util.concurrent.ListenableFuture;
import com.nowandfuture.mod.core.prefab.LocalWorld;
import com.nowandfuture.mod.core.selection.OBBox;
import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.chunk.SetVisibility;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.optifine.shaders.SVertexBuilder;
import org.lwjgl.opengl.GL11;


import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static com.nowandfuture.mod.core.client.renders.CubesBuilder.CUBE_SIZE;

public class RenderCube {

    private final CubesRenderer cubesRenderer;

    private LocalWorld world;
    private SetVisibility setVisibility;
    //pos in parent
    private BlockPos pos;

    private ReentrantLock taskLock = new ReentrantLock();
    private CubeCompileTask cubeCompileTask;

    private VertexBuffer[] vertexBuffers = new VertexBuffer[BlockRenderLayer.values().length];
    private boolean[] vboBind = new boolean[BlockRenderLayer.values().length];

    private final ReentrantLock lockCompileTask = new ReentrantLock();
    private BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
    private OBBox bounding;

    private volatile boolean compiling = false;
    private int renderFrame;

    private AtomicBoolean toUpdate = new AtomicBoolean(true);

    public RenderCube(CubesRenderer cubesRenderer,LocalWorld world,BlockPos pos){
        this.cubesRenderer = cubesRenderer;
        this.world = world;
        this.pos = pos;
        this.cubeCompileTask = new CubeCompileTask(this);

        if (OpenGlHelper.useVbo())
        {
            for (int j = 0; j < BlockRenderLayer.values().length; ++j)
            {
                this.vertexBuffers[j] = new VertexBuffer(DefaultVertexFormats.BLOCK);
            }
        }
    }

    public void rebuildCompileTask(){
        taskLock.lock();
        try {
            this.cubeCompileTask = new CubeCompileTask(this);
        }finally {
            taskLock.unlock();
        }
    }

    public void createNewCompileTask(){
        taskLock.lock();
        try {
            if (!cubeCompileTask.isFinished())
                this.stopCompileTask();
            this.cubeCompileTask = new CubeCompileTask(this);
        }finally {
            taskLock.unlock();
        }
    }

    public void createTranslucentCompileTask(){
        taskLock.lock();
        try {
            if (!cubeCompileTask.isFinished())
                this.stopCompileTask();
            this.cubeCompileTask = new CubeCompileTask(this);
            cubeCompileTask.setRenderingTranslucent(true);
        }finally {
            taskLock.unlock();
        }
    }

    public boolean isToUpdate() {
        return toUpdate.get();
    }

    public boolean markUpdate(boolean toUpdate){
        return this.toUpdate.getAndSet(toUpdate);
    }

    //local world pos
    public OBBox getBounding() {
        if(bounding == null){
            BlockPos worldPos = new BlockPos(pos.getX() * CUBE_SIZE,pos.getY() * CUBE_SIZE,pos.getZ() * CUBE_SIZE);
            bounding = new OBBox(new AxisAlignedBB(worldPos,worldPos.add(CUBE_SIZE,CUBE_SIZE,CUBE_SIZE)));
        }
        return bounding;
    }

    //local world pos
    public OBBox getTransformedOBBounding(){
        return getBounding().transform(cubesRenderer.getModelMatrix());
    }

    public CubeCompileTask getCubeCompileTask() {
        return cubeCompileTask;
    }

    public LocalWorld getWorld() {
        return world;
    }

    public VertexBuffer getVertexBufferByLayer(int layer)
    {
        return vertexBuffers[layer];
    }

    public SetVisibility getSetVisibility() {
        return setVisibility;
    }

    public void setSetVisibility(SetVisibility setVisibility) {
        this.setVisibility = setVisibility;
    }

    //has path form face1 to face2;
    public boolean isVisible(EnumFacing from/*face1*/, EnumFacing to/*face2*/) {
        return this.setVisibility.isVisible(from, to);
    }

    public Vector3f getTransformedBasePos(){
        return CubesBuilder.getTransformPos(new Vector3f(pos.getX() * CUBE_SIZE,pos.getY() * CUBE_SIZE,pos.getZ() * CUBE_SIZE),cubesRenderer);
    }

    public BlockPos getPos() {
        return pos;
    }

    public boolean build(){
        world.getMatrix4f().load(cubesRenderer.getModelMatrix());

        boolean[] success = new boolean[BlockRenderLayer.values().length];
        Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        if(entity == null) return false;

        float x = (float)entity.posX;
        float y = (float)entity.posY + entity.getEyeHeight();
        float z = (float)entity.posZ;

        BlockPos worldPos = new BlockPos(pos.getX() * CUBE_SIZE,pos.getY() * CUBE_SIZE,pos.getZ() * CUBE_SIZE);

        BlockPos.MutableBlockPos.getAllInBoxMutable(worldPos,worldPos.add(CUBE_SIZE ,CUBE_SIZE ,CUBE_SIZE ))
                .forEach(new Consumer<BlockPos.MutableBlockPos>() {
                    @Override
                    public void accept(BlockPos.MutableBlockPos mutableBlockPos) {
                        IBlockState blockState = world.getBlockState(mutableBlockPos);
                        for(BlockRenderLayer blockRenderLayer : BlockRenderLayer.values()) {
                            if(!blockState.getBlock().canRenderInLayer(blockState, blockRenderLayer)) continue;

                            net.minecraftforge.client.ForgeHooksClient.setRenderLayer(blockRenderLayer);

                            if (blockState.getBlock().getDefaultState().getRenderType() != EnumBlockRenderType.INVISIBLE) {
                                BufferBuilder bufferBuilder = cubeCompileTask.getCacheBuilder().getWorldRendererByLayer(blockRenderLayer);

                                if (!cubeCompileTask.isLayerStarted(blockRenderLayer)) {
                                    cubeCompileTask.setLayerStarted(blockRenderLayer);
                                    RenderCube.this.preRenderBlocks(bufferBuilder);
                                }

                                if(OptifineHelper.isActive() && OptifineHelper.isShaders())
                                    SVertexBuilder.pushEntity(blockState,worldPos,world,bufferBuilder);

                                success[blockRenderLayer.ordinal()] |=
                                        dispatcher.renderBlock(blockState,mutableBlockPos,world,bufferBuilder);

                                if(OptifineHelper.isActive() && OptifineHelper.isShaders())
                                    SVertexBuilder.popEntity(bufferBuilder);
                            }

                        }
                        net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
                    }
                });


        for (BlockRenderLayer blockrenderlayer : BlockRenderLayer.values())
        {
            if (success[blockrenderlayer.ordinal()]) {
                cubeCompileTask.setLayerUsed(blockrenderlayer);
            }

            if (cubeCompileTask.isLayerStarted(blockrenderlayer)) {
                if(OptifineHelper.isActive() && OptifineHelper.isShaders()) {
                    BufferBuilder bufferBuilder = cubeCompileTask.getCacheBuilder().getWorldRendererByLayer(blockrenderlayer);
                    SVertexBuilder.calcNormalChunkLayer(bufferBuilder);
                }
                RenderCube.this.postRenderBlocks(blockrenderlayer,x,y,z,cubeCompileTask.getCacheBuilder().getWorldRendererByLayer(blockrenderlayer));
            }
        }
        return true;
    }

    private void preRenderBlocks(BufferBuilder bufferBuilderIn)
    {
        bufferBuilderIn.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
    }

    private void postRenderBlocks(BlockRenderLayer layer, float x, float y, float z, BufferBuilder bufferBuilderIn)
    {
        if (layer == BlockRenderLayer.TRANSLUCENT && !cubeCompileTask.isLayerEmpty(layer))
        {
            bufferBuilderIn.sortVertexData(x, y, z);
            cubeCompileTask.setState(bufferBuilderIn.getVertexState());
        }
        bufferBuilderIn.finishDrawing();
    }

    public void restoreTranslucent(){
        Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        if(entity == null || cubeCompileTask == null) return;

        float x = (float)entity.posX;
        float y = (float)entity.posY + entity.getEyeHeight();
        float z = (float)entity.posZ;
        BufferBuilder bufferBuilder = cubeCompileTask.getCacheBuilder().getWorldRendererByLayer(BlockRenderLayer.TRANSLUCENT);

        if(!cubeCompileTask.isLayerEmpty(BlockRenderLayer.TRANSLUCENT)) {
            preRenderBlocks(bufferBuilder);
            bufferBuilder.setVertexState(cubeCompileTask.getState());
            postRenderBlocks(BlockRenderLayer.TRANSLUCENT, x, y, z, bufferBuilder);
        }
    }

    public ListenableFuture upload(BlockRenderLayer layer, double distance){
        VertexBuffer vertexBuffer = getVertexBufferByLayer(layer.ordinal());
        BufferBuilder bufferBuilder = cubeCompileTask.getCacheBuilder().getWorldRendererByLayer(layer);
        return cubesRenderer.upload(this,layer,vertexBuffer,bufferBuilder,distance);
    }

    public void setVboBind(int index ,boolean b){
        vboBind[index] = b;
    }

    public boolean isVboBind(BlockRenderLayer blockRenderLayer){
        return vboBind[blockRenderLayer.ordinal()];
    }

    public void deleteGlResources()
    {
        this.stopCompileTask();
        this.world = null;

        for (int i = 0; i < BlockRenderLayer.values().length; ++i)
        {
            if (this.vertexBuffers[i] != null)
            {
                this.vertexBuffers[i].deleteGlBuffers();
            }
        }
    }

    protected void finishCompileTask()
    {
        this.lockCompileTask.lock();

        try
        {
            if (this.cubeCompileTask != null && this.cubeCompileTask.getStatus() != CubeCompileTask.Status.FINISHED)
            {
                this.cubeCompileTask.finish();
                this.cubeCompileTask = null;
            }
        }
        finally
        {
            this.lockCompileTask.unlock();
        }
    }

    private void stopCompileTask() {
        finishCompileTask();
        this.cubeCompileTask = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RenderCube that = (RenderCube) o;

        return Objects.equal(this.pos, that.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pos);
    }

    public boolean isCompiling() {
        return compiling;
    }

    public void setCompiling(boolean compiling) {
        this.compiling = compiling;
    }

    public boolean setRenderFrame(int renderFrame) {
        if(renderFrame != this.renderFrame) {
            this.renderFrame = renderFrame;
            return true;
        }else{
            return false;
        }
    }

    public int getRenderFrame() {
        return renderFrame;
    }

    public CubesRenderer getCubesRenderer() {
        return cubesRenderer;
    }
}
