package com.nowandfuture.mod.handler;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.common.Items.BlockInfoCopyItem;
import com.nowandfuture.mod.core.common.Items.ModuleLinkWatcherItem;
import com.nowandfuture.mod.core.common.Items.PrefabItem;
import com.nowandfuture.mod.core.common.Items.TimelineItem;
import com.nowandfuture.mod.core.common.MovementCreativeTab;
import com.nowandfuture.mod.core.common.blocks.*;
import com.nowandfuture.mod.core.common.entities.*;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Objects;

import static com.nowandfuture.mod.Movement.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public final class RegisterHandler {

    public static Block constructorBlock;
    public static Block anmEditorBlock;
    public static Block moduleBlock;
    public static Block transformedBlock;
    public static Block videoBlock;
    public static Item prefabItem = new PrefabItem();
    public static Item timelineItem = new TimelineItem();
    public static Item copyItem = new BlockInfoCopyItem();
    public static Item linkWatcherItem = new ModuleLinkWatcherItem();
    public static CreativeTabs creativeTab = new MovementCreativeTab(Movement.NAME);

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> blockRegister){

        GameRegistry.registerTileEntity(TileEntityModule.class,new ResourceLocation(MODID,"module_tile"));
        GameRegistry.registerTileEntity(TileEntityConstructor.class,new ResourceLocation(MODID,"constructor_tile"));
        GameRegistry.registerTileEntity(TileEntityTimelineEditor.class,new ResourceLocation(MODID,"editor_tile"));
        GameRegistry.registerTileEntity(TileEntityCoreModule.class,new ResourceLocation(MODID,"moduleshow_tile"));
        GameRegistry.registerTileEntity(TileEntityTransformedBlock.class,new ResourceLocation(MODID,"transformedblock_tile"));
        GameRegistry.registerTileEntity(TileEntitySimplePlayer.class,new ResourceLocation(MODID,"video_tile"));

        constructorBlock = new ConstructorBlock();
        anmEditorBlock = new ModuleTimelineEditorBlock();
        moduleBlock = new ModuleCoreBlock();
        transformedBlock = new TransformedBlock();
        videoBlock = new VideoBlock();

        blockRegister.getRegistry().register(
                constructorBlock.setRegistryName(MODID,"constructor_block")
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

        blockRegister.getRegistry().register(
                transformedBlock.setRegistryName(MODID,"transformed_block")
                        .setCreativeTab(creativeTab)
        );

        blockRegister.getRegistry().register(
                videoBlock.setRegistryName(MODID,"video_block")
                        .setCreativeTab(creativeTab)
        );

    }
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> itemRegister){

        IForgeRegistry<Item> registry = itemRegister.getRegistry();

        registry.register(
                new ItemBlock(constructorBlock)
                        .setRegistryName(Objects.requireNonNull(constructorBlock.getRegistryName()))
                        .setCreativeTab(creativeTab)
        );

        registry.register(
                new ItemBlock(anmEditorBlock)
                        .setRegistryName(Objects.requireNonNull(anmEditorBlock.getRegistryName()))
                        .setCreativeTab(creativeTab)

        );

        registry.register(
                new ItemBlock(moduleBlock)
                        .setRegistryName(Objects.requireNonNull(moduleBlock.getRegistryName()))
                        .setCreativeTab(creativeTab)

        );

        registry.register(
                new ItemBlock(transformedBlock)
                        .setRegistryName(Objects.requireNonNull(transformedBlock.getRegistryName()))
                        .setCreativeTab(creativeTab)

        );

        registry.register(
                new ItemBlock(videoBlock)
                        .setRegistryName(Objects.requireNonNull(videoBlock.getRegistryName()))
                        .setCreativeTab(creativeTab)

        );

        registry.register(
                prefabItem.setRegistryName(new ResourceLocation(MODID,"item_prefab"))
                        .setTranslationKey("prefab")
                        .setCreativeTab(creativeTab)

        );

        registry.register(
                timelineItem.setRegistryName(new ResourceLocation(MODID,"item_timeline"))
                        .setTranslationKey("timeline")
                        .setCreativeTab(creativeTab)

        );

        registry.register(
                copyItem.setRegistryName(new ResourceLocation(MODID,"item_blockcopier"))
                        .setTranslationKey("blockCopier")
                        .setCreativeTab(creativeTab)

        );

        registry.register(
                linkWatcherItem.setRegistryName(new ResourceLocation(MODID,"item_link_watcher"))
                        .setTranslationKey("linkWatcher")
                        .setCreativeTab(creativeTab)

        );
    }

    @SubscribeEvent
    public static void registerItems(ModelRegistryEvent event) {
        registerModel(constructorBlock,0);//"inventory"
        registerModel(anmEditorBlock,0);
        registerModel(moduleBlock,0);
        registerModel(transformedBlock,0);
        registerModel(videoBlock,0);
        registerModel(prefabItem,0,"inventory");
        registerModel(timelineItem,0,"inventory");
        registerModel(copyItem,0,"inventory");
        registerModel(linkWatcherItem,0,"inventory");
    }

    public static void registerModel(Block block,int metadata){
        registerModel(block, metadata,null);
    }

    public static void registerModel(Block block,int metadata,String variantln){
        ModelResourceLocation model = new ModelResourceLocation(block.getRegistryName(), variantln);
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), metadata, model);
    }

    public static void registerModel(Item item,int metadata,String variantln){
        ModelResourceLocation model = new ModelResourceLocation(item.getRegistryName(), variantln);
        ModelLoader.setCustomModelResourceLocation(item, metadata, model);
    }
}
