package com.nowandfuture.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM5;

public class RenderGlobalClassVisitor extends ClassVisitor {

    TransPacket packet;

    public RenderGlobalClassVisitor(ClassVisitor cv,TransPacket packet) {
        super(ASM5,cv);
        this.packet = packet;
    }


    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        return packet.MethodAdapt(methodVisitor,name,desc);
    }
}
