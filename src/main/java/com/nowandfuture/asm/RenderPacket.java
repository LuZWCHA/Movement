package com.nowandfuture.asm;

import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.MethodVisitor;

//unused
@Deprecated
public class RenderPacket extends TransPacket {
    public static final String TARGET_CLASS = "net.minecraft.client.renderer.EntityRenderer";

    @Override
    public String[] getTargetClassName() {
        return new String[]{TARGET_CLASS};
    }

    @Override
    public MethodVisitor MethodAdapt(MethodVisitor mv, String name, String desc) {

        MethodName = name;
        MethodDesc = desc;
        if (check("func_175068_a", "renderWorldPass", "(IFJ)V")) {
            FMLLog.log.info("Injecting renderWorldPass(IFJ)V");
            return new MethodVisitor(ASM5, mv) {
                public int MethodCount = 0;
                private static final String TARGET_TRANSFORMED_NAME = "restoreLastBlurMipmap";
                private static final String TARGET_ORG_NAME = "func_174935_a";
                private static final String TARGET_DESC = "()V";

                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                    boolean nameFlag = TARGET_TRANSFORMED_NAME.equals(mapMethodName(owner, name, desc))|
                            TARGET_ORG_NAME.equals(mapMethodName(owner, name, desc));
                    if(nameFlag & desc.equals(TARGET_DESC) ) {
                        MethodCount ++;
                        if(MethodCount == 2) {
                            this.mv.visitMethodInsn(opcode, owner, name, desc, itf);
                            this.mv.visitVarInsn(ILOAD, 1);
                            this.mv.visitVarInsn(FLOAD, 2);
                            this.mv.visitMethodInsn(INVOKESTATIC, "com/nowandfuture/asm/RenderHook", "render", "(IF)V", false);
//                            this.mv.visitVarInsn(Opcodes.FLOAD, 2);
//                            this.mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/nowandfuture/asm/RenderHook", "build", "(F)V", false);
                            FMLLog.log.info("success Inject");
                            return;
                        }
                    }
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                }
            };
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
