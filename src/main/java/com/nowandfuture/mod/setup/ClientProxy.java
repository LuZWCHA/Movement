package com.nowandfuture.mod.setup;


import com.nowandfuture.mod.core.client.renders.tiles.*;
import com.nowandfuture.mod.core.common.entities.*;
import com.nowandfuture.mod.handler.KeyBindHandler;
import com.nowandfuture.mod.handler.RenderHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends DefaultClientProxy {

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        MinecraftForge.EVENT_BUS.register(new RenderHandler());
        MinecraftForge.EVENT_BUS.register(KeyBindHandler.getInstance());

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityConstructor.class,new SelectAreaRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTimelineEditor.class,new TileEntityTimeLineEditorRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCoreModule.class,new TileEntityCoreModuleRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTransformedBlock.class,new TileEntityTransformedBlockRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySimplePlayer.class,new VideoRenderer());
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
    }
}
