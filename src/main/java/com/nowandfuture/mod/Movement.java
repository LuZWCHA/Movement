package com.nowandfuture.mod;

import com.nowandfuture.mod.core.common.gui.GuiConstructor;
import com.nowandfuture.mod.core.common.gui.GuiMediaPlayer;
import com.nowandfuture.mod.core.common.gui.GuiModule;
import com.nowandfuture.mod.core.common.gui.GuiTimelineEditor;
import com.nowandfuture.mod.core.common.gui.mygui.GuiManager;
import com.nowandfuture.mod.core.common.gui.mygui.JEIGuiHandler;
import com.nowandfuture.mod.handler.CollisionHandler;
import com.nowandfuture.mod.handler.GuiHandler;
import com.nowandfuture.mod.handler.RenderHandler;
import com.nowandfuture.mod.network.NetworkHandler;
import com.nowandfuture.mod.setup.IProxy;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import static com.nowandfuture.mod.Movement.MODID;
import static com.nowandfuture.mod.Movement.NAME;
import static com.nowandfuture.mod.Movement.VERSION;

@Mod(modid = MODID,name = NAME,version = VERSION,acceptedMinecraftVersions = "(1.8,1.12.2]")
@JEIPlugin
public class Movement implements IModPlugin {

    public static final String MODID = "movement";
    public static final String NAME = "Movement Mod";
    public static final String VERSION = "0.1";

    @Mod.Instance
    public static Movement instance;

    @SidedProxy(serverSide = "com.nowandfuture.mod.setup.ServerProxy",
            clientSide = "com.nowandfuture.mod.setup.ClientProxy")
    public static IProxy proxy;

    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        proxy.preInit(event);
        NetworkHandler.INSTANCE.init();
        MinecraftForge.EVENT_BUS.register(new CollisionHandler());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);
        NetworkRegistry.INSTANCE.registerGuiHandler(this,new GuiHandler());
    }

    @Mod.EventHandler
    public void stop(FMLServerStoppedEvent event){
        CollisionHandler.modules.clear();
    }

    @Override
    public void register(IModRegistry registry) {
        registry.addAdvancedGuiHandlers(GuiTimelineEditor.getJEIGuiHandler());
    }
}
