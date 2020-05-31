package com.nowandfuture.mod.core.client.renderers;

import com.nowandfuture.mod.core.prefab.localworld.LocalWorld;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class BlockRenderHelper {
    private int perListSize = 1 << 12;
    private int listNum;

    private int listStartIndex;
    private int currentIndex;

    private State displayListState = State.IDLE;

    private LocalWorld localWorld;

    private int lastIndexInRenderList;
    private BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

    private BufferBuilder bufferBuilder = new BufferBuilder(2097152);
    private WorldVertexBufferUploader worldVertexBufferUploader = new WorldVertexBufferUploader();

    private final Object locker = new Object();

    private boolean init = false;

    private boolean isLightChanging = false;

    enum State{
        IDLE,
        WORKING,
        FINISHED
    }


    public BlockRenderHelper(LocalWorld localWorld){
        this.localWorld = localWorld;
    }

    public void init(){
        listNum = (int) Math.ceil((double)localWorld.getRenderBlocks().size()/(double)perListSize);
        listStartIndex = GLAllocation.generateDisplayLists(listNum);
        currentIndex = 0;
        lastIndexInRenderList = 0;
        init = true;
    }

    @Deprecated
    public void doFastRender(BufferBuilder bufferBuilder){
        if(localWorld == null || localWorld.getBlockNum() <= 0)
            return;

        if(displayListState.equals(State.IDLE)) init();

        preRender();
        renderFast(bufferBuilder);
        postRender();
    }

    public void doRender(){
        if(localWorld == null || localWorld.getBlockNum() <= 0)
            return;

        synchronized (locker) {
            if(displayListState.equals(State.IDLE)) init();

            if(isLightChanging)
                renderWithLight();
            else {
                preRender();
                render();
                postRender();
            }
        }
    }

    private void preRender(){
       if(displayListState.equals(State.WORKING)){
            for(int i=listStartIndex;i<listStartIndex + currentIndex;++i) {
                GL11.glCallList(i);
            }
        }else if(displayListState.equals(State.FINISHED)){
            for(int i=listStartIndex;i<listStartIndex + listNum;++i) {
                GL11.glCallList(i);
            }
        }

    }

    @Deprecated
    private void renderFast(BufferBuilder bufferBuilder){
        if(displayListState.equals(State.IDLE) || displayListState.equals(State.WORKING) ){
            GlStateManager.glNewList(listStartIndex + currentIndex, GL11.GL_COMPILE_AND_EXECUTE);
            GlStateManager.pushMatrix();

            GlStateManager.blendFunc(org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA);

            GlStateManager.enableBlend();
            GlStateManager.disableCull();

            int size = localWorld.getRenderBlocks().size();
            int i;
            for(i=0; i < perListSize; ++i)
            {
                if(i + lastIndexInRenderList >= size) break;
                LocalWorld.LocalBlock wrapBlock = localWorld.getRenderBlocks().get(i + lastIndexInRenderList);
                Block b = wrapBlock.blockState.getBlock();
                if(b==Blocks.AIR) continue;
                IBakedModel model = dispatcher.getModelForState(wrapBlock.blockState);

                dispatcher.getBlockModelRenderer()
                        .renderModel(localWorld,
                                model,
                                wrapBlock.blockState,
                                wrapBlock.pos,
                                bufferBuilder,
                                false);
            }
            lastIndexInRenderList += i;

            GlStateManager.disableBlend();
            GlStateManager.enableCull();

            GlStateManager.popMatrix();
            GlStateManager.glEndList();
            currentIndex ++;
        }
    }

    private void render(){
        if(displayListState.equals(State.IDLE) || displayListState.equals(State.WORKING) ){
            render1();

        }
    }

    private void render1(){
        GlStateManager.glNewList(listStartIndex + currentIndex, GL11.GL_COMPILE);
        GlStateManager.pushMatrix();

        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        int size = localWorld.getRenderBlocks().size();
        int i;
        for(i=0; i < perListSize; ++i)
        {
            if(i + lastIndexInRenderList >= size) break;
            LocalWorld.LocalBlock wrapBlock = localWorld.getRenderBlocks().get(i + lastIndexInRenderList);
            Minecraft.getMinecraft().getTextureManager()
                    .bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            dispatcher.renderBlock(wrapBlock.blockState,wrapBlock.pos,localWorld,bufferBuilder);

            TileEntity tileEntity = localWorld.getTileEntity(wrapBlock.pos);
            if(tileEntity != null) {
                BlockPos blockPos = tileEntity.getPos();

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                TileEntitySpecialRenderer renderer = TileEntityRendererDispatcher.instance.getRenderer(tileEntity.getClass());

                renderer.render(tileEntity,
                        blockPos.getX(),
                        blockPos.getY(),
                        blockPos.getZ(),
                        (float) 0, -1, 1);
            }

        }
        lastIndexInRenderList += i;

        bufferBuilder.finishDrawing();
        worldVertexBufferUploader.draw(bufferBuilder);

        GlStateManager.popMatrix();
        GlStateManager.glEndList();
        currentIndex ++;
    }

    private void renderWithLight(){
        render2();
    }

    private void render2(){
        final int size = localWorld.getRenderBlocks().size();

        GlStateManager.pushMatrix();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        for(int i=0; i < size; ++i)
        {
            LocalWorld.LocalBlock wrapBlock = localWorld.getRenderBlocks().get(i);

            if(wrapBlock.blockState.getRenderType() == EnumBlockRenderType.INVISIBLE)
                continue;

            Minecraft.getMinecraft().getTextureManager()
                    .bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            dispatcher.renderBlock(wrapBlock.blockState,wrapBlock.pos,localWorld,bufferBuilder);
            TileEntity tileEntity = localWorld.getTileEntity(wrapBlock.pos);

            if(tileEntity != null) {
                BlockPos blockPos = tileEntity.getPos();

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                TileEntitySpecialRenderer renderer = TileEntityRendererDispatcher.instance.getRenderer(tileEntity.getClass());

                renderer.render(tileEntity,
                        blockPos.getX(),
                        blockPos.getY(),
                        blockPos.getZ(),
                        (float) 0, -1, 1);
            }
        }

        bufferBuilder.finishDrawing();
        worldVertexBufferUploader.draw(bufferBuilder);

        GlStateManager.popMatrix();
    }

    private void postRender(){
//        Minecraft.getMinecraft().entityRenderer.disableLightmap();

        if((displayListState.equals(State.WORKING) || displayListState.equals(State.IDLE)) && currentIndex >= listNum) {
            displayListState = State.FINISHED;
            currentIndex = 0;
        }

        if(displayListState.equals(State.IDLE) && currentIndex > 0){
            displayListState = State.WORKING;
        }
    }

    public void clear(){
        GLAllocation.deleteDisplayLists(listStartIndex,listNum);
        displayListState = State.IDLE;
        lastIndexInRenderList = 0;
        currentIndex = 0;
        listNum = 0;
        init = false;
    }

    public boolean isInit() {
        return init;
    }

    public void setLightChanging(boolean lightChanging) {
        synchronized (locker) {
            if (lightChanging != isLightChanging) {
                isLightChanging = lightChanging;
                clear();
                init();
            }
        }
    }

    @Deprecated
    static class RenderThread extends Thread{

        private BlockRenderHelper helper;

        public void setHelper(BlockRenderHelper helper) {
            this.helper = helper;
        }

        @Override
        public void run() {
            if(helper != null){
                helper.render1();
            }
        }
    }

}