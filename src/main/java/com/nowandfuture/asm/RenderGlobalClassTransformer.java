package com.nowandfuture.asm;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.lang.reflect.Field;
import java.util.List;

@SideOnly(Side.CLIENT)
public class RenderGlobalClassTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
//        if(transformedName.contains("net.minecraft.client.renderer.RenderGlobal"))
//            System.out.println(transformedName);
        if (!"net.minecraft.client.renderer.RenderGlobal".equals(transformedName))
            return basicClass;

        TransPacket packet = new RenderChunkPacket();
        packet.nowTarget = name;

        ClassReader cr = new ClassReader(basicClass);
        ClassWriter cw = new ClassWriter(cr, 2);
        ClassVisitor cn = new PacketClassVisitor(cw,packet);
        packet.addMember(cw);

        cr.accept(cn, 0);

        FMLLog.log.info("transformer finished");

        return cw.toByteArray();
    }



}
