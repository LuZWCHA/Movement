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
import net.minecraft.client.model.ModelRenderer;
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
        GameRegistry.registerTileEntity(TileEntityConstructor.class,new ResourceLocation(MODID,"constructor_tile"));
        GameRegistry.registerTileEntity(TileEntityTimelineEditor.class,new ResourceLocation(MODID,"editor_tile"));
        GameRegistry.registerTileEntity(TileEntityShowModule.class,new ResourceLocation(MODID,"moduleshow_tile"));

        constructorBlock = new ConstructorBlock();
        anmEditorBlock = new ModuleTimelineEditorBlock();
        moduleBlock = new ModuleCoreBlock();

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

    }
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> itemRegister){

        IForgeRegistry<Item> registry = itemRegister.getRegistry();

        registry.register(
                new ItemBlock(constructorBlock)
                        .setRegistryName(constructorBlock.getRegistryName())
                        .setCreativeTab(creativeTab)
        );

        registry.register(
                new ItemBlock(anmEditorBlock)
                        .setRegistryName(anmEditorBlock.getRegistryName())
                        .setCreativeTab(creativeTab)

        );

        registry.register(
                new ItemBlock(moduleBlock)
                        .setRegistryName(moduleBlock.getRegistryName())
                        .setCreativeTab(creativeTab)

        );

        registry.register(
                prefabItem.setRegistryName(new ResourceLocation(MODID,"item_prefab"))
                        .setUnlocalizedName("prefab")
                        .setCreativeTab(creativeTab)

        );

        registry.register(
                timelineItem.setRegistryName(new ResourceLocation(MODID,"item_timeline"))
                        .setUnlocalizedName("timeline")
                        .setCreativeTab(creativeTab)

        );
    }

    @SubscribeEvent
    public static void registerItems(ModelRegistryEvent event) {
        registerModel(constructorBlock,0);//"inventory"
        registerModel(anmEditorBlock,0);
        registerModel(moduleBlock,0);
//        registerModel(moduleBlock,8);
        registerModel(prefabItem,0,"inventory");
        registerModel(timelineItem,0,"inventory");
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
