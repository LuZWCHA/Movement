package com.nowandfuture.asm;

import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.util.LinkedList;
import java.util.function.Consumer;

public class RenderChunkModifyPacket extends TransPacket {
    public static final String TARGET_CLASS = "net.minecraft.client.renderer.RenderGlobal$ContainerLocalRenderInformation";

    private String name;

    @Override
    public String[] getTargetClassName() {
        return new String[]{TARGET_CLASS};
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(name, outerName, innerName, access);
    }

    @Override
    public FieldVisitor FieldAdapt(FieldVisitor fv, int access, String name, String desc, Object value) {
        return fv;

    }

    @Override
    public void visitClass() {
        MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "getRenderChunk", "()Lnet/minecraft/client/renderer/chunk/RenderChunk;", null, null);
        methodVisitor.visitCode();
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(37, label0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, "net/minecraft/client/renderer/RenderGlobal$ContainerLocalRenderInformation", "renderChunk"/*field_178036_a*/, "Lnet/minecraft/client/renderer/chunk/RenderChunk;");
        methodVisitor.visitInsn(ARETURN);
        Label label1 = new Label();
        methodVisitor.visitLabel(label1);
        methodVisitor.visitLocalVariable("this", "Lnet/minecraft/client/renderer/RenderGlobal$ContainerLocalRenderInformation;", null, label0, label1, 0);
        methodVisitor.visitMaxs(1, 1);
        methodVisitor.visitEnd();
        classWriter.visitEnd();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, new String[]{IRenderChunk.class.getName().replace(".","/")});
    }

    @Override
    public MethodVisitor MethodAdapt(MethodVisitor mv, String name, String desc) {
        MethodName = name;
        MethodDesc = desc;

        return mv;
    }


    public static String mapMethodName(String owner, String methodName, String desc) {
        return FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(unmapClassName(owner), methodName, desc);
    }
    public static String unmapClassName(String name) {
        return FMLDeobfuscatingRemapper.INSTANCE.unmap(name.replace('.', '/')).replace('/', '.');
    }
    public static String mapFieldName(String owner, String methodName, String desc) {
        return FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(unmapClassName(owner), methodName, desc);
    }

}
