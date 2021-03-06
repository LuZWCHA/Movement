package com.nowandfuture.mod.core.client.renders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.realmsclient.util.Pair;
import com.nowandfuture.mod.Movement;
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
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.model.*;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.client.model.pipeline.VertexTransformer;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CopyBlockItemModel implements IBakedModel{
    private IBakedModel lowerModel;
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
        return Lists.newArrayList();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

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
                    IBakedModel storedBlkModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(storedBlk);
                    builder.putModel(storedBlk, storedBlkModel);
                }else {
                    builder.putModel(null, lowerModel);
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



        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
        {
            List<BakedQuad> list = Lists.<BakedQuad>newArrayList();
            List<BakedQuad> list2 = Lists.newArrayList();
            int i = 0;
            for (Pair<IBlockState, IBakedModel> entry : this.selectors)
            {
                List<BakedQuad> list1 = entry.second().getQuads(entry.first(),side,rand++);


                if(i++ == 1) {
                    list1.forEach(new Consumer<BakedQuad>() {
                        @Override
                        public void accept(BakedQuad bakedQuad) {
                            list2.add(bakedQuad);
                        }
                    });
                    list.addAll(list2);
                }else {
                    list.addAll(list1);
                }

            }

            return list;
        }
//
//        private BakedQuad transform(BakedQuad quad, int slotNo, boolean isOpened) {
//            UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(DefaultVertexFormats.BLOCK);
//            final IVertexConsumer consumer = new VertexTransformer(builder) {
//                @Override
//                public void put(int element, float... data) {
//                    VertexFormatElement formatElement = DefaultVertexFormats.BLOCK.getElement(element);
//                    switch(formatElement.getUsage()) {
//                        case POSITION: {
//                            /*
//                             * 0 is x (positive to east)
//                             * 1 is y (positive to up)
//                             * 2 is z (positive to south)
//                             * 3 is idk
//                             */
//                            float[] newData = data;
//
//                            float moveToRight = (slotNo * 0.3f)/16f;
//                            float moveForward = isOpened ? 0.3f/16f : 0f;
//
//                            newData[0] = newData[0] + moveToRight;
//                            newData[2] = newData[2] + moveForward;
//
//                            System.out.println("newData = " + newData[2]);
//                            parent.put(element, newData);
//                            break;
//                        }
//                        case GENERIC:
//
//
//                        default: {
//                            parent.put(element, data);
//                            break;
//                        }
//                    }
//                }
//            };
//            quad.pipe(consumer);
//            return builder.build();
//        }


        @Override
        public org.apache.commons.lang3.tuple.Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType type)
        {
            if (type == ItemCameraTransforms.TransformType.GUI)
            {
                selectors.get(0).second().handlePerspective(type);
            }
            return this.selectors.get(1).second().handlePerspective(type);
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
            return false;
        }

        public TextureAtlasSprite getParticleTexture()
        {
            return this.particleTexture;
        }

        public ItemOverrideList getOverrides()
        {
            return ItemOverrideList.NONE;
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
