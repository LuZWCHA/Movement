package com.nowandfuture.mod.handler;

import com.nowandfuture.mod.core.block.TestBlock;
import com.nowandfuture.mod.core.movecontrol.ModuleRender;
import com.nowandfuture.mod.core.entities.TileEntityMovementModule;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import static com.nowandfuture.mod.Movement.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public final class RegisterHandler {
    public static Block testBlock = new TestBlock();

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> blockRegister){
        blockRegister.getRegistry().register(
                testBlock.setRegistryName(MODID,"test_block")
        );
        GameRegistry.registerTileEntity(TileEntityMovementModule.class,new ResourceLocation(MODID,"test_block"));
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMovementModule.class,new ModuleRender());
    }
    @SubscribeEvent
    public static void registerItem(RegistryEvent.Register<Item> itemRegister){
        itemRegister.getRegistry().register(
                new ItemBlock(testBlock).setRegistryName(MODID,"test_block")
        );
    }
}
