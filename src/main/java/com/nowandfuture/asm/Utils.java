package com.nowandfuture.asm;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public class Utils {
    private static Field renderChunks;
    public static Map mapCache;

    public static List<Object> getRenderChunks(){
        if(renderChunks == null)
            renderChunks = ObfuscationReflectionHelper.findField(RenderGlobal.class, "field_72755_R" );
        try{
            Minecraft mc = Minecraft.getMinecraft();
            List<Object> renderChunkList = (List<Object>) renderChunks.get(mc.renderGlobal);
            return renderChunkList;
        }catch (IllegalArgumentException | IllegalAccessException e){
            e.printStackTrace();
        }
        return null;
    }

    public static Map<BlockPos,RenderChunk> getRenderChunkMap() throws NoSuchFieldException, IllegalAccessException {
//        long time = System.currentTimeMillis();
        List<Object> chunkInfos = getRenderChunks();
//        System.out.println(System.currentTimeMillis() - time);

        if(chunkInfos != null){
            Map<BlockPos,RenderChunk> map = new HashMap<>();
            for (Object cf :
                    chunkInfos) {
                RenderChunk chunk = ((IRenderChunk)cf).getRenderChunk();
                map.put(chunk.getPosition(), chunk);
            }
            mapCache = map;
            return map;
        }

        return null;
    }

}
