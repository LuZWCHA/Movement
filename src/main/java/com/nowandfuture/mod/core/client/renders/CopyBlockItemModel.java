package com.nowandfuture.mod.core.client.renders;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.util.Pair;
import com.nowandfuture.mod.core.common.Items.BlockInfoCopyItem;
import com.nowandfuture.mod.handler.RegisterHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.client.model.pipeline.VertexTransformer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class CopyBlockItemModel implements IBakedModel{
    private IBakedModel lowerModel,upperModel;
    private final static ModelResourceLocation lowerLocation = new ModelResourceLocation(RegisterHandler.copyItem.getRegistryName(), "inventory");
    public CopyBlockItemModel() {
        lowerModel = getBakedModel(lowerLocation);
    }

    private static IBakedModel getBakedModel(ModelResourceLocation location){
        IModel model = ModelLoaderRegistry.getModelOrMissing(location);
        return model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
    }
    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        return upperModel.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    //if no way to render chest and the other tileitem,render before
    /**do render here,because stack.getItem().getTileEntityItemStackRenderer().renderByItem(stack) don't render
     * if (model.isBuiltInRenderer())
     {
     GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
     GlStateManager.enableRescaleNormal();
     stack.getItem().getTileEntityItemStackRenderer().renderByItem(stack);
     }
     else
     {
     this.renderModel(model, stack);

     if (stack.hasEffect())
     {
     this.renderEffect(model);
     }
     }*/
    // TODO: 2020/2/22 render here
    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return null;
    }


    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return new ItemOverrideList(new ArrayList<ItemOverride>()){
            @Override
            public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world,
                                               EntityLivingBase entity) {
                if(stack.getItem() != RegisterHandler.copyItem){
                    throw new RuntimeException("Copier Block Model should only be used ");
                }
                MultipartBakedModel.Builder builder = new MultipartBakedModel.Builder();

                NBTTagCompound nbt = stack.getTagCompound();

                builder.putModel(null, lowerModel);

                if(nbt != null && nbt.hasKey(BlockInfoCopyItem.NBT_BLOCK_ID)){
                    IBlockState storedBlk = (Block.getStateById(nbt.getInteger(BlockInfoCopyItem.NBT_BLOCK_ID)));
                    IBakedModel storedBlkModel;

                    if(storedBlk.getBlock().getRenderType(storedBlk) ==
                            EnumBlockRenderType.ENTITYBLOCK_ANIMATED){
                        Item item = storedBlk.getBlock().getItemDropped(storedBlk,new Random(),1);

                        ItemStack itemStack = new ItemStack(item,1);
//                        itemStack.setItemDamage(storedBlk.getBlock().getMetaFromState(storedBlk));
                        storedBlkModel = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(itemStack);
                    }
                    else
                        storedBlkModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(storedBlk);
                    builder.putModel(storedBlk, storedBlkModel);
                    upperModel = storedBlkModel;
                }else {
                    builder.putModel(null, null);
                }
                return builder.makeMultipartModel();
            }
        };
    }

    @SideOnly(Side.CLIENT)
    public static class MultipartBakedModel implements IBakedModel
    {
        private final List<Pair<IBlockState, IBakedModel>> selectors;
        protected final boolean ambientOcclusion;
        protected final boolean gui3D;
        protected final TextureAtlasSprite particleTexture;
        protected final ItemCameraTransforms cameraTransforms;

        public MultipartBakedModel(List<Pair<IBlockState, IBakedModel>> selectorsIn)
        {
            this.selectors = selectorsIn;
            IBakedModel ibakedmodel = selectorsIn.get(0).second();
            this.ambientOcclusion = ibakedmodel.isAmbientOcclusion();
            this.gui3D = false;
            this.particleTexture = ibakedmodel.getParticleTexture();
            this.cameraTransforms = ibakedmodel.getItemCameraTransforms();
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand){
            List<BakedQuad> list = Lists.<BakedQuad>newArrayList();
            List<BakedQuad> list2 = Lists.newArrayList();
            int i = 0;
            for (Pair<IBlockState, IBakedModel> entry : this.selectors)
            {
                if(entry.second() == null) continue;
                List<BakedQuad> list1 = entry.second().getQuads(entry.first(),side,rand++);

                if(i++ == 1) {
                    list1.forEach(new Consumer<BakedQuad>() {
                        @Override
                        public void accept(BakedQuad bakedQuad) {
                            list2.add(transform(bakedQuad));
                        }
                    });
                    list.addAll(list2);
                }else {
                    list.addAll(list1);
                }

            }

            return list;
        }

        private BakedQuad transform(BakedQuad quad) {
            UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(DefaultVertexFormats.BLOCK);
            final IVertexConsumer consumer = new VertexTransformer(builder) {
                @Override
                public void put(int element, float... data) {
                    VertexFormatElement formatElement = DefaultVertexFormats.BLOCK.getElement(element);
                    switch(formatElement.getUsage()) {
                        case COLOR: {
                            /*
                             * 0 is x (positive to east)
                             * 1 is y (positive to up)
                             * 2 is z (positive to south)
                             * 3 is idk
                             */
                            float[] newData = data;

                            newData[3] *= 0.8;

                            super.put(element, newData);
                        }
                        break;
                        case POSITION: {
                            /*
                             * 0 is x (positive to east)
                             * 1 is y (positive to up)
                             * 2 is z (positive to south)
                             * 3 is idk
                             */
                            float[] newData = data;

                            newData[1] += 1f;

                            super.put(element, newData);
                            break;
                        }

                        default: {
                            super.put(element, data);
                            break;
                        }
                    }
                }
            };
            quad.pipe(consumer);
            return builder.build();
        }


        @Override
        public org.apache.commons.lang3.tuple.Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType type)
        {
            if (type == ItemCameraTransforms.TransformType.GUI)
            {
                Matrix4f matrix4f = new Matrix4f();
                matrix4f.setIdentity();
                if(selectors.get(1).second() != null) {
                    matrix4f.setTranslation(new Vector3f(0, -.25f, 0));
                    matrix4f.setScale(0.5f);
                }
                return org.apache.commons.lang3.tuple.Pair.of(this,matrix4f);
            }
            return this.selectors.get(0).second().handlePerspective(type);
        }

        public boolean isAmbientOcclusion()
        {
            return this.ambientOcclusion;
        }

        public boolean isGui3d()
        {
            return this.gui3D;
        }

        public boolean isBuiltInRenderer()
        {
            return selectors.get(1).second()!=null && selectors.get(1).second().isBuiltInRenderer();
        }

        public TextureAtlasSprite getParticleTexture()
        {
            return this.particleTexture;
        }

        public ItemOverrideList getOverrides()
        {
            return selectors.get(1).second().getOverrides();
        }

        @SideOnly(Side.CLIENT)
        public static class Builder
        {
            private List<Pair<IBlockState, IBakedModel>> builderSelectors = Lists.<Pair<IBlockState, IBakedModel>>newArrayList();
            public void putModel(IBlockState state, IBakedModel model)
            {
                this.builderSelectors.add(Pair.of(state, model));
            }

            public IBakedModel makeMultipartModel()
            {
                return new MultipartBakedModel(this.builderSelectors);
            }
        }
    }

}
