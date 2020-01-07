package com.nowandfuture.asm;

import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.ASM5;

public class PacketClassVisitor extends ClassVisitor {

    TransPacket packet;

    public PacketClassVisitor(ClassVisitor cv, TransPacket packet) {
        super(ASM5,cv);
        this.packet = packet;
        this.packet.visitor = cv;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        packet.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitEnd() {
//        super.visitEnd();
        packet.visitClass();
        packet.visitEnd();
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        FieldVisitor fieldVisitor = super.visitField(access, name, desc, signature, value);
        return packet.FieldAdapt(fieldVisitor,access,name,desc,value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        return packet.MethodAdapt(methodVisitor,name,desc);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(name, outerName, innerName, access);
        packet.visitInnerClass(name, outerName, innerName, access);
    }
}
