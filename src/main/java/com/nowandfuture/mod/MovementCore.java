package com.nowandfuture.mod;

import net.minecraftforge.fml.relauncher.*;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.Name("MovementCore")
public class MovementCore implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        if(FMLLaunchHandler.side().isClient())
            return new String[]{"com.nowandfuture.asm.EntityRendererClassTransformer",
                    "com.nowandfuture.asm.RenderGlobalClassTransformer"};
        return new String[]{};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
