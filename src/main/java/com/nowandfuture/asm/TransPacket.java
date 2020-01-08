package com.nowandfuture.asm;

import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.*;

public abstract class TransPacket implements Opcodes {
    protected String TARGET_CLASS = "";
    protected ClassWriter classWriter;
    protected ClassVisitor visitor;

    public abstract String[] getTargetClassName();

    //public
    public FieldVisitor FieldAdapt(FieldVisitor fv, int access, String name , String desc, Object value){return fv;}
    public MethodVisitor MethodAdapt(MethodVisitor mv, String name, String desc){return null;}
    public void visitEnd(){visitor.visitEnd();}
    public void visitClass(){}

    public void addMember(ClassWriter cw){
        classWriter = cw;
    }

    protected String MethodName;
    protected String MethodDesc;
    public String nowTarget;

    protected boolean check(String MethodName, String MethodForgeName, String Desc) {
        boolean flag;
        String target = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(nowTarget, this.MethodName, MethodDesc);
        flag = MethodName.equals(target);
        flag |= MethodForgeName.equals(target);
        flag &= Desc.equals("*") || Desc.equals(MethodDesc);
        return flag;
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        visitor.visitInnerClass(name, outerName, innerName, access);
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        visitor.visit(version, access, name, signature, superName, interfaces);
    }


}
