package com.nowandfuture.mod.setup;


import com.nowandfuture.mod.core.client.renders.tiles.*;
import com.nowandfuture.mod.core.common.entities.*;
import com.nowandfuture.mod.handler.ClientTickHandler;
import com.nowandfuture.mod.handler.KeyBindHandler;
import com.nowandfuture.mod.handler.RenderHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends DefaultClientProxy {

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        FMLCommonHandler.instance().bus().register(new ClientTickHandler());
        FMLCommonHandler.instance().bus().register(new RenderHandler());
        FMLCommonHandler.instance().bus().register(KeyBindHandler.getInstance());

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityConstructor.class,new SelectAreaRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTimelineEditor.class,new TileEntityTimeLineEditorRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCoreModule.class,new TileEntityModuleShowerRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTransformedBlock.class,new TileEntityTransformedBlockRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySimplePlayer.class,new VideoRenderer());
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
    }
}
