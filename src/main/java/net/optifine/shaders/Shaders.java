package net.optifine.shaders;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import net.minecraft.client.Minecraft;

public class Shaders {

    private Shaders() {
    }


    public static void updateBlockLightLevel() {

    }

    public static boolean isOldHandLight() {
        return false;
    }

    public static boolean isDynamicHandLight() {
        return false;
    }

    public static boolean isOldLighting() {
        return false;
    }
    public static int getBufferIndexFromString(String name) {
        if (!name.equals("colortex0") && !name.equals("gcolor")) {
            if (!name.equals("colortex1") && !name.equals("gdepth")) {
                if (!name.equals("colortex2") && !name.equals("gnormal")) {
                    if (!name.equals("colortex3") && !name.equals("composite")) {
                        if (!name.equals("colortex4") && !name.equals("gaux1")) {
                            if (!name.equals("colortex5") && !name.equals("gaux2")) {
                                if (!name.equals("colortex6") && !name.equals("gaux3")) {
                                    return !name.equals("colortex7") && !name.equals("gaux4") ? -1 : 7;
                                } else {
                                    return 6;
                                }
                            } else {
                                return 5;
                            }
                        } else {
                            return 4;
                        }
                    } else {
                        return 3;
                    }
                } else {
                    return 2;
                }
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }

    public static void beginRender(Minecraft minecraft, float partialTicks, long finishTimeNano) {

    }


    public static void enableTexture2D() {

    }

    public static void disableTexture2D() {

    }

    public static void enableLightmap() {

    }

    public static void disableLightmap() {

    }

    public static void checkShadersModInstalled() {
        try {
            Class var0 = Class.forName("shadersmod.transform.SMCClassTransformer");
        } catch (Throwable var1) {
            return;
        }

        throw new RuntimeException("Shaders Mod detected. Please remove it, OptiFine has built-in support for shaders.");
    }

}
