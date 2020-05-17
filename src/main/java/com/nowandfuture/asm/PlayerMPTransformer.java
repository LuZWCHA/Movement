package com.nowandfuture.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.FMLLog;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

@Deprecated
public class PlayerMPTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!"net.minecraft.entity.player.EntityPlayerMP".equals(transformedName))
            return basicClass;

        TransPacket packet = new EntityPlayerMPPacket();
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
