package com.nowandfuture.asm;

import com.nowandfuture.mod.Movement;
import net.minecraftforge.fml.common.FMLLog;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class EntityPlayerMPPacket extends TransPacket {
    public static final String TARGET_CLASS = "net.minecraft.entity.player.EntityPlayerMP";

    @Override
    public String[] getTargetClassName() {
        return new String[]{TARGET_CLASS};
    }

    @Override
    public MethodVisitor MethodAdapt(MethodVisitor mv, String name, String desc) {

        MethodName = name;
        MethodDesc = desc;
        if (check("func_71110_a", "sendAllContents", "(Lnet/minecraft/inventory/Container;Lnet/minecraft/util/NonNullList;)V")) {

            return new MethodVisitor(ASM5, mv) {

                @Override
                public void visitInsn(int opcode) {
                    if(opcode == Opcodes.RETURN) {
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 1);
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitMethodInsn(INVOKESTATIC, "com/nowandfuture/asm/PlayerMPHook", "sendAllContent", "(Lnet/minecraft/entity/player/EntityPlayerMP;Lnet/minecraft/inventory/Container;Lnet/minecraft/util/NonNullList;)V", false);
                        FMLLog.log.info(Movement.MODID + " Inject sendAllContents success.");
                    }
                    super.visitInsn(opcode);
                }

            };
        }

        return mv;
    }
}
