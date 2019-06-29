package com.nowandfuture.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.FMLLog;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class GlobalRenderClassTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!"net.minecraft.client.renderer.EntityRenderer".equals(transformedName))
            return basicClass;

        TransPacket packet = new RenderPacket();
        packet.nowTarget = name;

        //使用ASM读入basicClass
        ClassReader cr = new ClassReader(basicClass);
        ClassWriter cw = new ClassWriter(cr, 2);
        ClassVisitor cn = new RenderGlobalClassVisitor(cw,packet);
        cr.accept(cn, 0);
        packet.addMember(cw);


//        //遍历methods
//        for (MethodNode mn : cn.methods) {
//            //调用FML接口获得方法名，运行时获得的是srg，测试时获得的是mcp
//            String methodName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(name, mn.name, mn.desc);
//            if(!"func_174977_a".equals(methodName) && !"renderBlockLayer".equals(methodName))
//                continue;
//            FMLLog.log.info("touched method");
//
//            mn.visitCode();
//
//            break;
//
//        }

        //返回修改后的bytes
        //ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        //cn.accept(cw);

        FMLLog.log.info("transformer finished");

        return cw.toByteArray();
    }
}
