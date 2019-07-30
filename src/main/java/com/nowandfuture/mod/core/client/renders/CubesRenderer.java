package com.nowandfuture.mod.core.client.renders;

import com.creativemd.creativecore.client.mods.optifine.OptifineHelper;
import com.google.common.collect.Queues;
import com.google.common.primitives.Doubles;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.nowandfuture.mod.core.prefab.AbstractPrefab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.VertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.optifine.shaders.SVertexBuilder;
import net.optifine.shaders.ShadersRender;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@SideOnly(Side.CLIENT)
public class CubesRenderer {

    private boolean built = false;
    private AbstractPrefab prefab;
    private Map<BlockPos,RenderCube> cubes;
    private Vec3i size;
    private Map<EnumFacing,Set<RenderCube>> eachFaceCubes;

    private final Queue<PendingUpload> queueChunkUploads;
    private final static int MAX_UPLOAD_NUM_PERFRAME = 8;
    private final VertexBufferUploader vertexBufferUploader;

    //--------------------------------------------------------------
    private Set<RenderCube> visibleCubes;
    private List<RenderCube> cubesToRender;

    private int frameCounter = 0;
    private double x,y,z;
    private WorldLightChangeListener lightChangeListener;
    private boolean isShaderOn = OptifineHelper.isActive() && OptifineHelper.isShaders();

    public CubesRenderer(AbstractPrefab prefab){
        this.prefab = prefab;
        cubes = new HashMap<>();
        visibleCubes = new HashSet<>();
        cubesToRender = new LinkedList<>();
        queueChunkUploads = Queues.newPriorityQueue();
        vertexBufferUploader = new VertexBufferUploader();
        //enable it will case some problem
//        lightChangeListener = new WorldLightChangeListener(this);
        eachFaceCubes = new HashMap<>();
        for (EnumFacing facing :
                EnumFacing.values()) {
            eachFaceCubes.put(facing,new HashSet<>());
        }
    }

    public void build(){
        size = CubesBuilder.reMapPrefab(cubes,prefab);
        if(size == Vec3i.NULL_VECTOR) return;

        cubes.forEach(new BiConsumer<BlockPos, RenderCube>() {
            @Override
            public void accept(BlockPos pos, RenderCube cube) {
                CubesBuilder.computeVisibleMapInCube(cube);
            }
        });

        eachFaceCubes = CubesBuilder.createVisibleCubesForEachFace(cubes,size);

        if(lightChangeListener != null)
            Minecraft.getMinecraft().world.addEventListener(lightChangeListener);

        built = true;
    }

    public boolean isBuilt() {
        return built;
    }

    public AbstractPrefab getPrefab() {
        return prefab;
    }

    public Vec3i getSize() {
        return size;
    }

    public Optional<RenderCube> getCubeByCubeInPos(BlockPos pos){
        return Optional.ofNullable(cubes.get(pos));
    }

    public void forceUpdateAll(){
        if(built){
            cubes.forEach(new BiConsumer<BlockPos, RenderCube>() {
                @Override
                public void accept(BlockPos pos, RenderCube cube) {
                    cube.markUpdate(true);
                }
            });
        }
    }

    public void checkShader(){
        boolean curState = OptifineHelper.isActive() && OptifineHelper.isShaders();
        if(isShaderOn != curState) {
            forceUpdateAll();
            isShaderOn = curState;
        }
    }

    public void prepare(double partialTicks){
        if(!built) return;

        Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        visibleCubes = new HashSet<>();

        if(entity != null) {

            x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
            y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
            z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;

            final Vector3f eyesPos = new Vector3f((float) x, (float)(y + entity.getEyeHeight()), (float)z);

            final Set<EnumFacing> set = CubesBuilder.getVisibleFaces(eyesPos, prefab);

            final Function<RenderCube,Boolean> frustumFilter = new Function<RenderCube, Boolean>() {
                @Override
                public Boolean apply(RenderCube cube) {
                    boolean flag = true;
                    if(!OptifineHelper.isActive()) flag = CubesBuilder.checkRenderChunkIsRender(cube,prefab.getBasePos());

                    Vector3f center = prefab.getTransformedWorldPos(cube.getBounding().getCenter());
                    return entity.getDistanceSq(center.getX(),center.getY(),center.getZ()) <=
                            (Minecraft.getMinecraft().gameSettings.renderDistanceChunks * Minecraft.getMinecraft().gameSettings.renderDistanceChunks << 8) &&
                            ModuleRenderManager.INSTANCE.getClippingHelper()
                            .isOBBInFrustum(cube.getTransformedOBBounding()
                                    .translate(prefab.getBasePos().getX() - x,
                                            prefab.getBasePos().getY() - y,
                                            prefab.getBasePos().getZ() - z)) && flag;
                }
            };

            if(set.isEmpty()) {//inside
                BlockPos cubePos = CubesBuilder.getVisitorCubePos(
                        this,eyesPos
                                .translate(-prefab.getBasePos().getX(),
                                        -prefab.getBasePos().getY(),
                                        -prefab.getBasePos().getZ()));

                RenderCube inCube = cubes.get(cubePos);

                if(inCube != null){
                    Vector3f lookVec = CubesBuilder.getViewVector(entity,partialTicks);
                    visibleCubes.addAll(CubesBuilder.getVisibleRenderCubesFromCube(lookVec,cubes,inCube,frustumFilter,frameCounter));
                }
            }else {//outside
                set.forEach(new Consumer<EnumFacing>() {
                    @Override
                    public void accept(EnumFacing facing) {
                        visibleCubes.addAll(CubesBuilder.getVisibleRenderCubesFromFacing(cubes, eachFaceCubes, facing, frustumFilter,frameCounter));
                    }
                });
            }

            frameCounter ++;

            checkShader();

            for (RenderCube cube :
                    visibleCubes) {

                if(cube.isToUpdate() && !cube.isCompiling()){
                    cube.setCompiling(true);
                    cube.markUpdate(false);

                    if(cube.getCubeCompileTask() == null || cube.getCubeCompileTask().isFinished()){
                        cube.createCompileTask();
                    }
                    ModuleRenderManager.INSTANCE.execute(new ModuleRenderWorker(cube.getCubeCompileTask()));
                }
            }

            int i = 0;
            while (i++ <= MAX_UPLOAD_NUM_PERFRAME){
                PendingUpload pendingUpload;
                synchronized (queueChunkUploads) {
                    pendingUpload = queueChunkUploads.poll();
                }
                if(pendingUpload != null){
                    pendingUpload.uploadTask.run();
                }else{
                    break;
                }
            }
        }
    }

    public void buildTranslucent(){
        for (RenderCube cube :
                visibleCubes) {

            if(cube.isToUpdate() && !cube.isCompiling()){
                cube.setCompiling(true);
                cube.markUpdate(false);

//                if(cube.getCubeCompileTask() == null || cube.getCubeCompileTask().isFinished()){
                    cube.createTranslucentCompileTask();
//                }
                ModuleRenderManager.INSTANCE.execute(new ModuleRenderWorker(cube.getCubeCompileTask()));
            }
        }
    }

    public void render(BlockRenderLayer layer){
        if(!built) return;
        for (RenderCube cube :
                visibleCubes) {

            if (!cube.getCubeCompileTask().isLayerEmpty(layer)) {
                cubesToRender.add(cube);
            }
        }

        if(!cubesToRender.isEmpty())
            renderBlockLayer(layer);
    }

    public void invalid(){
        if(lightChangeListener != null)
            Minecraft.getMinecraft().world.removeEventListener(lightChangeListener);

        cubes.forEach(new BiConsumer<BlockPos, RenderCube>() {
            @Override
            public void accept(BlockPos pos, RenderCube cube) {
                cube.markUpdate(false);
                cube.deleteGlResources();
            }
        });

        cubesToRender.clear();
        visibleCubes.clear();
        cubes.clear();
        eachFaceCubes.clear();
        queueChunkUploads.clear();

        built = false;
    }

    public ListenableFuture upload(VertexBuffer vertexBuffer, BufferBuilder bufferBuilder,double distance){
        if(Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            uploadVBO(vertexBuffer, bufferBuilder);
            return Futures.immediateFuture(null);
        }else{
            ListenableFutureTask<Object> listenableFutureTask =
                    ListenableFutureTask.create(
                            new Runnable(){
                                public void run() {
                                    CubesRenderer.this.upload(vertexBuffer, bufferBuilder,distance);
                                }
                            },
                            null);

            synchronized (queueChunkUploads) {
                queueChunkUploads.offer(new PendingUpload(listenableFutureTask, distance));
            }
            return listenableFutureTask;
        }
    }

    private void uploadVBO(VertexBuffer vertexBuffer, BufferBuilder bufferBuilder){
        vertexBufferUploader.setVertexBuffer(vertexBuffer);
        vertexBufferUploader.draw(bufferBuilder);
    }


    //-----------------------------------------Render Compiled Cubes----------------------------------------------------
    //must open VBO in optifine(in Chinese:顶点缓冲)
    private void renderBlockLayer(BlockRenderLayer blockLayerIn)
    {
        if (OpenGlHelper.useVbo()) {
            GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);
        }

        if(OptifineHelper.isActive() && OptifineHelper.isShaders())
            ShadersRender.preRenderChunkLayer(blockLayerIn);

        this.renderCubeLayer(blockLayerIn);

        if(OptifineHelper.isActive() && OptifineHelper.isShaders())
            ShadersRender.postRenderChunkLayer(blockLayerIn);

        if (OpenGlHelper.useVbo())
        {
            for (VertexFormatElement vertexformatelement : DefaultVertexFormats.BLOCK.getElements())
            {
                VertexFormatElement.EnumUsage usage = vertexformatelement.getUsage();
                int index = vertexformatelement.getIndex();

                switch (usage)
                {
                    case POSITION:
                        GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                        break;
                    case UV:
                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + index);
                        GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                        break;
                    case COLOR:
                        GlStateManager.glDisableClientState(GL11.GL_COLOR_ARRAY);
                        GlStateManager.resetColor();
                }
            }
        }
    }

    private void renderCubeLayer(BlockRenderLayer layer)
    {
        for (RenderCube renderCube : this.cubesToRender)
        {
            VertexBuffer vertexbuffer = renderCube.getVertexBufferByLayer(layer.ordinal());
            GlStateManager.pushMatrix();
            vertexbuffer.bindBuffer();
            if(OptifineHelper.isActive() && OptifineHelper.isShaders())
                ShadersRender.setupArrayPointersVbo();
            else
                this.setupArrayPointers();
            vertexbuffer.drawArrays(GL11.GL_QUADS);
            GlStateManager.popMatrix();
        }

        OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
        GlStateManager.resetColor();
        cubesToRender.clear();
    }

    private void setupArrayPointers()
    {
        GlStateManager.glVertexPointer(3, 5126, 28, 0);
        GlStateManager.glColorPointer(4, 5121, 28, 12);
        GlStateManager.glTexCoordPointer(2, 5126, 28, 16);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glTexCoordPointer(2, 5122, 28, 24);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    @SideOnly(Side.CLIENT)
    class PendingUpload implements Comparable<PendingUpload>
    {
        private final ListenableFutureTask<Object> uploadTask;
        private final double distanceSq;

        public PendingUpload(ListenableFutureTask<Object> uploadTaskIn, double distanceSqIn)
        {
            this.uploadTask = uploadTaskIn;
            this.distanceSq = distanceSqIn;
        }

        public int compareTo(PendingUpload other)
        {
            return Doubles.compare(this.distanceSq, other.distanceSq);
        }
    }

    @SideOnly(Side.CLIENT)
    static class RenderCubeInformation {

        final RenderCube renderCube;
        final EnumFacing facing;
        byte setFacing;
        final int routerCount;

        public RenderCubeInformation(RenderCube renderCube, EnumFacing facingIn, int count) {
            this.renderCube = renderCube;
            this.facing = facingIn;
            this.routerCount = count;
            this.setFacing = 0;
        }

        public void setDirection(byte setFace, EnumFacing facing) {
            this.setFacing = (byte)(this.setFacing | setFace | 1 << facing.ordinal());
        }

        public void setDirection(EnumFacing facing)
        {
            this.setFacing = (byte)(this.setFacing | 1 << facing.ordinal());
        }

        public boolean hasDirection(EnumFacing face)
        {
            return (this.setFacing & 1 << face.ordinal()) > 0;
        }
    }

}
