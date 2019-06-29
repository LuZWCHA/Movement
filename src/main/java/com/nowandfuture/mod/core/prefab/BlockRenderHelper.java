package com.nowandfuture.mod.core.prefab;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.locks.Lock;

public class BlockRenderHelper {
    private int perListSize = 1 << 12;
    private int listNum;

    private int listStartIndex;
    private int currentIndex;

    private State displayListState = State.IDLE;

    private LocalWorld localWorld;

    private int lastIndexInRenderList;
    private BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
    private BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
    private final Object locker = new Object();

    private boolean isLightChanging = true;

    enum State{
        IDLE,
        WORKING,
        FINISHED
    }


    BlockRenderHelper(LocalWorld localWorld){
        this.localWorld = localWorld;
    }

    public void init(){
        listNum = (int) Math.ceil((double)localWorld.getBlockNum()/(double)perListSize);
        listStartIndex = GLAllocation.generateDisplayLists(listNum);
        currentIndex = 0;
        lastIndexInRenderList = 0;
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

        if(displayListState.equals(State.IDLE)) init();

        if(isLightChanging)
            render2();
        else {
            synchronized (locker) {
                preRender();
                render();
                postRender();
            }
        }
    }

    private void preRender(){
        Minecraft.getMinecraft().entityRenderer.enableLightmap();

        if(displayListState.equals(State.WORKING)){
            for(int i=listStartIndex;i<listStartIndex + currentIndex;++i) GL11.glCallList(i);
        }else if(displayListState.equals(State.FINISHED)){
            for(int i=listStartIndex;i<listStartIndex + listNum;++i) GL11.glCallList(i);
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
            GlStateManager.glNewList(listStartIndex + currentIndex, GL11.GL_COMPILE_AND_EXECUTE);
            render1();
            GlStateManager.glEndList();
            currentIndex ++;
        }
    }

    private void render1(){

        GlStateManager.pushMatrix();

        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        int size = localWorld.getRenderBlocks().size();
        int i;
        for(i=0; i < perListSize; ++i)
        {
            if(i + lastIndexInRenderList >= size) break;
            LocalWorld.LocalBlock wrapBlock = localWorld.getRenderBlocks().get(i + lastIndexInRenderList);
            Block b = wrapBlock.blockState.getBlock();
            if(b==Blocks.AIR)continue;

            dispatcher.renderBlock(wrapBlock.blockState,wrapBlock.pos,localWorld,bufferBuilder);
        }
        lastIndexInRenderList += i;

        Tessellator.getInstance().draw();

        GlStateManager.popMatrix();
    }

    private void render2(){
        int size = localWorld.getRenderBlocks().size();

        GlStateManager.pushMatrix();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        Minecraft.getMinecraft().entityRenderer.enableLightmap();

        for(int i=0; i < size; ++i)
        {
            LocalWorld.LocalBlock wrapBlock = localWorld.getRenderBlocks().get(i);
            Block b = wrapBlock.blockState.getBlock();
            if(b==Blocks.AIR)continue;

            dispatcher.renderBlock(wrapBlock.blockState,wrapBlock.pos,localWorld,bufferBuilder);
        }

        Tessellator.getInstance().draw();
        Minecraft.getMinecraft().entityRenderer.disableLightmap();

        GlStateManager.popMatrix();
    }

    private void postRender(){
        Minecraft.getMinecraft().entityRenderer.disableLightmap();

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

}
