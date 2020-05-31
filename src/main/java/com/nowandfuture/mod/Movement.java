package com.nowandfuture.mod;

import com.nowandfuture.mod.handler.CollisionHandler;
import com.nowandfuture.mod.handler.GuiHandler;
import com.nowandfuture.mod.handler.PlayerActionHandler;
import com.nowandfuture.mod.network.NetworkHandler;
import com.nowandfuture.mod.setup.IProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;

import static com.nowandfuture.mod.Movement.*;

@Mod(modid = MODID,name = NAME,version = VERSION,acceptedMinecraftVersions = "(1.8,1.12.2]")
public class Movement{

    public static final String MODID = "movement";
    public static final String NAME = "Movement Mod";
    public static final String VERSION = "0.2.4-200530";

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
        MinecraftForge.EVENT_BUS.register(new PlayerActionHandler());

        NetworkHandler.INSTANCE.init();
        com.nowandfuture.mod.core.common.gui.mygui.network.NetworkHandler
                .INSTANCE.init();
        MinecraftForge.EVENT_BUS.register(new CollisionHandler());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);
        NetworkRegistry.INSTANCE.registerGuiHandler(this,new GuiHandler());

    }
}
