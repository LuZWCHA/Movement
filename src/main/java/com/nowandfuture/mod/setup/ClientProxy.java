package com.nowandfuture.mod.setup;


import com.nowandfuture.mod.handler.ClientTickHandler;
import com.nowandfuture.mod.handler.CollisionHandler;
import com.nowandfuture.mod.handler.KeyBindHandler;
import com.nowandfuture.mod.handler.RenderHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends DefaultClientProxy {

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        KeyBindHandler.init();
        FMLCommonHandler.instance().bus().register(new ClientTickHandler());
        FMLCommonHandler.instance().bus().register(new RenderHandler());
        FMLCommonHandler.instance().bus().register(new KeyBindHandler());
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
    }
}
