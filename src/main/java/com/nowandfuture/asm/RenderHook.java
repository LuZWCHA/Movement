package com.nowandfuture.asm;

import com.nowandfuture.mod.core.client.renderers.TransformedBlockRenderMap;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class RenderHook {
    private final static Deque<IRender> renderModules = new LinkedList<>();
    //store chunks which are rendered by minecraft itself
    private final static Map<BlockPos,RenderChunk> renderChunks = new HashMap<>();

    public static void prepare(double p) {
        synchronized (renderModules) {
            for (IRender renderer :
                    renderModules) {
                if (renderer.isRenderValid()) {
                    renderer.prepare((float) p);
                }
            }
        }
    }

    public static void renderBlockLayer(int pass, double p, BlockRenderLayer layer) {
        synchronized (renderModules) {
            for (IRender renderer :
                    renderModules) {
                renderer.renderBlockLayer(pass,p,layer);
            }
            TransformedBlockRenderMap.INSTANCE.renderBlockLayer(pass, p, layer);
        }
    }

    public static void addChunks(RenderChunk chunk){
        renderChunks.put(chunk.getPosition(),chunk);
    }

    public static void clearChunks(){
        renderChunks.clear();
    }

    public static Map<BlockPos,RenderChunk> getRenderChunks(){
        return renderChunks;
    }

    public synchronized static void offer(IRender iRender){
        renderModules.offerFirst(iRender);
    }

    public static Deque<IRender> getRenderModules() {
        return renderModules;
    }

    public static void forceClear(){
        renderModules.clear();
    }
    
}
