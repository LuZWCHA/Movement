package com.nowandfuture.mod.core.client.renders.tiles;

import com.nowandfuture.mod.core.common.Items.BlockInfoCopyItem;
import com.nowandfuture.mod.handler.RegisterHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;

import java.util.List;
import java.util.Random;

public class CopyItemStackRenderer extends TileEntityItemStackRenderer {

    private IBakedModel lowerModel;

    private static IBakedModel getBakedModel(ModelResourceLocation location){
        IModel model = ModelLoaderRegistry.getModelOrMissing(location);
        return model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
    }

    @Override
    public void renderByItem(ItemStack itemStackIn) {
        if(lowerModel == null) lowerModel = getBakedModel(new ModelResourceLocation(RegisterHandler.copyItem.getRegistryName(), "inventory"));
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        GlStateManager.pushMatrix();
        GlStateManager.translate(.5f,.5f,0);
        renderItem.renderItem(itemStackIn,lowerModel);
        GlStateManager.popMatrix();

        NBTTagCompound nbt = itemStackIn.getTagCompound();

        if(nbt != null && nbt.hasKey(BlockInfoCopyItem.NBT_BLOCK_ID)) {
            IBlockState storedBlk = (Block.getStateById(nbt.getInteger(BlockInfoCopyItem.NBT_BLOCK_ID)));
            Item item = storedBlk.getBlock().getItemDropped(storedBlk,new Random(),1);
            ItemStack itemStack = new ItemStack(item,1);
            item.setDamage(itemStack,storedBlk.getBlock().getMetaFromState(storedBlk));

            super.renderByItem(itemStack);
        }else {
            super.renderByItem(itemStackIn);
        }
    }

    @Override
    public void renderByItem(ItemStack p_192838_1_, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(.5f,.36f,0);
        GlStateManager.scale(.4f,.4f,.4f);
        super.renderByItem(p_192838_1_, partialTicks);
        GlStateManager.popMatrix();
    }

}
