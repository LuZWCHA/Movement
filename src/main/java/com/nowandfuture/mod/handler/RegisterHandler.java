package com.nowandfuture.mod.handler;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.common.Items.PrefabItem;
import com.nowandfuture.mod.core.common.Items.TimelineItem;
import com.nowandfuture.mod.core.common.MovementCreativeTab;
import com.nowandfuture.mod.core.common.blocks.ConstructorBlock;
import com.nowandfuture.mod.core.common.blocks.ModuleCoreBlock;
import com.nowandfuture.mod.core.common.blocks.ModuleTimelineEditorBlock;
import com.nowandfuture.mod.core.common.entities.TileEntityConstructor;
import com.nowandfuture.mod.core.common.entities.TileEntityModule;
import com.nowandfuture.mod.core.common.entities.TileEntityShowModule;
import com.nowandfuture.mod.core.common.entities.TileEntityTimelineEditor;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import static com.nowandfuture.mod.Movement.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public final class RegisterHandler {

    public static Block constructorBlock;
    public static Block anmEditorBlock;
    public static Block moduleBlock;
    public static Item prefabItem = new PrefabItem();
    public static Item timelineItem = new TimelineItem();
    public static CreativeTabs creativeTab = new MovementCreativeTab(Movement.NAME);

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> blockRegister){

        GameRegistry.registerTileEntity(TileEntityModule.class,new ResourceLocation(MODID,"module_tile"));
        GameRegistry.registerTileEntity(TileEntityConstructor.class,new ResourceLocation(MODID,"construct_tile"));
        GameRegistry.registerTileEntity(TileEntityTimelineEditor.class,new ResourceLocation(MODID,"editor_tile"));
        GameRegistry.registerTileEntity(TileEntityShowModule.class,new ResourceLocation(MODID,"moduleshow_tile"));

        constructorBlock = new ConstructorBlock();
        anmEditorBlock = new ModuleTimelineEditorBlock();
        moduleBlock = new ModuleCoreBlock();

        blockRegister.getRegistry().register(
                constructorBlock.setRegistryName(MODID,"construct_block")
                        .setCreativeTab(creativeTab)
        );

        blockRegister.getRegistry().register(
                anmEditorBlock.setRegistryName(MODID,"editor_block")
                        .setCreativeTab(creativeTab)
        );

        blockRegister.getRegistry().register(
                moduleBlock.setRegistryName(MODID,"module_block")
                        .setCreativeTab(creativeTab)
        );

    }
    @SubscribeEvent
    public static void registerItem(RegistryEvent.Register<Item> itemRegister){
        itemRegister.getRegistry().register(
                new ItemBlock(constructorBlock).setRegistryName(MODID,"construct_block")
                        .setCreativeTab(creativeTab)
        );

        itemRegister.getRegistry().register(
                new ItemBlock(anmEditorBlock).setRegistryName(MODID,"editor_block")
                        .setCreativeTab(creativeTab)

        );

        itemRegister.getRegistry().register(
                new ItemBlock(moduleBlock).setRegistryName(MODID,"module_block")
                        .setCreativeTab(creativeTab)

        );

        itemRegister.getRegistry().register(
                prefabItem.setRegistryName(MODID,"prefab")
                        .setUnlocalizedName("prefab")
                        .setCreativeTab(creativeTab)

        );

        itemRegister.getRegistry().register(
                timelineItem.setRegistryName(MODID,"timeline")
                        .setUnlocalizedName("timeline")
                        .setCreativeTab(creativeTab)

        );
    }
}
