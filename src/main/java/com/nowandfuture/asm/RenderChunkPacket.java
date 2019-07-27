package com.nowandfuture.asm;

import com.nowandfuture.mod.Movement;
import jdk.nashorn.internal.runtime.regexp.joni.constants.OPCode;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class RenderChunkPacket extends TransPacket {
    public static final String TARGET_CLASS = "net.minecraft.client.renderer.RenderGlobal";
    @Override
    public String[] getTargetClassName() {
        return new String[]{TARGET_CLASS};
    }

    //collect render-chunks by minecraft (not use when shader on)
    @Override
    public MethodVisitor MethodAdapt(MethodVisitor mv, String name, String desc) {

        MethodName = name;
        MethodDesc = desc;
        if (check("func_174970_a", "setupTerrain", "*")) {

            return new MethodVisitor(ASM5, mv) {
                int count = 0;
                boolean i = false;

                @Override
                public void visitInsn(int opcode) {
                    if(opcode == Opcodes.RETURN) {
                        mv.visitVarInsn(DLOAD, 2);
                        mv.visitMethodInsn(INVOKESTATIC, "com/nowandfuture/asm/RenderHook", "prepare", "(D)V", false);
                        FMLLog.log.info(Movement.MODID + " Inject setupTerrain success.");
                    }
                    super.visitInsn(opcode);
                }

                    @Override
                public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {

                    if (owner.equals("com/google/common/collect/Lists") && name.equals("newArrayList")) {
                        this.mv.visitMethodInsn(INVOKESTATIC, "com/nowandfuture/asm/RenderHook", "clearChunks", "()V", false);
                        i = true;
                    }

                    if(i && owner.equals("java/util/List") && name.equals("add")) {
                        count ++;
                        if(count == 1) {


                        }else if(count == 2){
                            this.mv.visitInsn(POP);
                            this.mv.visitVarInsn(ALOAD, 26);
                            this.mv.visitMethodInsn(INVOKESTATIC, "com/nowandfuture/asm/RenderHook", "addChunks", "(Lnet/minecraft/client/renderer/chunk/RenderChunk;)V", false);

                            this.mv.visitVarInsn(ALOAD, 25);
                            this.mv.visitMethodInsn(opcode, owner, name, desc, itf);
                            FMLLog.log.info(Movement.MODID + " Inject setupTerrain success,add collection of renderChunks!");
                            return;
                        }
                    }

                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                }
            };
        }

        if(check("func_174977_a", "renderBlockLayer", "*")){

            if(desc.equals("(Lnet/minecraft/util/BlockRenderLayer;DILnet/minecraft/entity/Entity;)I")||
                    desc.equals("(Lamm;DILvg;)I")){
                return new MethodVisitor(ASM5, mv) {
                    @Override
                    public void visitCode() {
                        mv.visitVarInsn(ILOAD, 4);
                        mv.visitVarInsn(DLOAD, 2);
                        mv.visitVarInsn(ALOAD, 1);
                        mv.visitMethodInsn(INVOKESTATIC, "com/nowandfuture/asm/RenderHook", "renderBlockLayer", "(IDLnet/minecraft/util/BlockRenderLayer;)V", false);
                        super.visitCode();
                        FMLLog.log.info(Movement.MODID + " Inject renderBlockLayer success.");
                    }
                };
            }

        }

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
