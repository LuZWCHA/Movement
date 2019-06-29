package com.nowandfuture.mod;

import com.nowandfuture.mod.setup.IProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

import static com.nowandfuture.mod.Movement.MODID;
import static com.nowandfuture.mod.Movement.NAME;
import static com.nowandfuture.mod.Movement.VERSION;

@Mod(modid = MODID,name = NAME,version = VERSION,acceptedMinecraftVersions = "(1.8,1.12.2]")
public class Movement {

        public static final String MODID = "movement";
        public static final String NAME = "Movement Mod";
        public static final String VERSION = "0.1";

        @Mod.Instance
        public static Movement instance;

        @SidedProxy(serverSide = "com.nowandfuture.mod.setup.ServerProxy",clientSide = "com.nowandfuture.mod.setup.ClientProxy")
        public static IProxy proxy;

        public static Logger logger;

        @Mod.EventHandler
        public void preInit(FMLPreInitializationEvent event)
        {
            logger = event.getModLog();
            proxy.preInit(event);
        }

        @Mod.EventHandler
        public void init(FMLInitializationEvent event)
        {
            proxy.init(event);
        }

//        @SubscribeEvent
//        public void registerBlocks(RegistryEvent.Register<Block> blockRegister){
//            TestBlock testBlock = new TestBlock();
//            blockRegister.getRegistry().register(testBlock);
//
//
//        }
//
//        public void registerItem(RegistryEvent.Register<Item> itemRegister){
//            //itemRegister.getRegistry().register(new Item());
//        }
}
