package com.nowandfuture.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

@SideOnly(Side.CLIENT)
public class EntityRendererClassTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!"net.minecraft.client.renderer.EntityRenderer".equals(transformedName))
            return basicClass;

        TransPacket packet = new RenderPacket();
        packet.nowTarget = name;

        //使用ASM读入basicClass
        ClassReader cr = new ClassReader(basicClass);
        ClassWriter cw = new ClassWriter(cr, 2);
        ClassVisitor cn = new PacketClassVisitor(cw,packet);
        cr.accept(cn, 0);
        packet.addMember(cw);

        FMLLog.log.info("transformer finished");

        return cw.toByteArray();
    }
}
