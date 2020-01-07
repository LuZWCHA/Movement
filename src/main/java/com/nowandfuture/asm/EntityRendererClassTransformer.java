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
        if (!"net.minecraft.client.renderer.RenderGlobal$ContainerLocalRenderInformation".equals(transformedName))
            return basicClass;

        TransPacket packet = new RenderChunkModifyPacket();
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
