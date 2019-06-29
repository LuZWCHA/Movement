package com.nowandfuture.asm;

import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public abstract class TransPacket implements Opcodes {
    protected String TARGET_CLASS = "";

    public abstract String[] getTargetClassName();
    //public
    public FieldVisitor FieldAdapt(FieldVisitor fv, int access, String name , String desc, Object value){return fv;}
    public MethodVisitor MethodAdapt(MethodVisitor mv, String name, String desc){return null;}
    public void addMember(ClassWriter cw){}

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

}
