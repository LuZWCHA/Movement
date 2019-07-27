package com.nowandfuture.mod.handler;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.client.renders.tiles.TileEntityModuleRenderer;
import com.nowandfuture.mod.core.client.renders.tiles.TileEntityTimeLineEditorRenderer;
import com.nowandfuture.mod.core.common.Items.PrefabItem;
import com.nowandfuture.mod.core.common.Items.TimelineItem;
import com.nowandfuture.mod.core.common.MovementCreativeTab;
import com.nowandfuture.mod.core.common.block.ConstructorBlock;
import com.nowandfuture.mod.core.common.block.ModuleTimelineEditorBlock;
import com.nowandfuture.mod.core.common.block.ModuleCoreBlock;
import com.nowandfuture.mod.core.common.entities.TileEntityConstructor;
import com.nowandfuture.mod.core.common.entities.TileEntityShowModule;
import com.nowandfuture.mod.core.common.entities.TileEntityTimelineEditor;
import com.nowandfuture.mod.core.client.renders.tiles.SelectAreaRenderer;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
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
    public static Block constructorBlock = new ConstructorBlock();
    public static Block anmEditorBlock = new ModuleTimelineEditorBlock();
    public static Block moduleBlock = new ModuleCoreBlock();
    public static Item prefabItem = new PrefabItem();
    public static Item timelineItem = new TimelineItem();
    public static CreativeTabs creativeTab = new MovementCreativeTab(Movement.NAME);

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> blockRegister){
        blockRegister.getRegistry().register(
                constructorBlock.setRegistryName(MODID,"construct_block")
                        .setCreativeTab(creativeTab)
        );
        GameRegistry.registerTileEntity(TileEntityConstructor.class,new ResourceLocation(MODID,"construct_block"));
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityConstructor.class,new SelectAreaRenderer());

        blockRegister.getRegistry().register(
                anmEditorBlock.setRegistryName(MODID,"editor_block")
                        .setCreativeTab(creativeTab)
        );
        GameRegistry.registerTileEntity(TileEntityTimelineEditor.class,new ResourceLocation(MODID,"editor_block"));
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTimelineEditor.class,new TileEntityTimeLineEditorRenderer());

        blockRegister.getRegistry().register(
                moduleBlock.setRegistryName(MODID,"module_block")
                        .setCreativeTab(creativeTab)
        );
        GameRegistry.registerTileEntity(TileEntityShowModule.class,new ResourceLocation(MODID,"module_block"));
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityShowModule.class,new TileEntityModuleRenderer<TileEntityShowModule>());

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
