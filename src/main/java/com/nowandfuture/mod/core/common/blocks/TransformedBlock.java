package com.nowandfuture.mod.core.common.blocks;

import com.nowandfuture.mod.core.common.Items.BlockInfoCopyItem;
import com.nowandfuture.mod.core.common.entities.TileEntityTransformedBlock;
import com.nowandfuture.mod.core.prefab.LocalWorld;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class TransformedBlock extends ProxyBlock {
    BlockWrapper blockWrapper;

    public TransformedBlock(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
        block = Blocks.AIR;
    }

    public TransformedBlock(Material materialIn) {
        super(materialIn);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(hand == EnumHand.MAIN_HAND){
            if(!playerIn.getHeldItem(hand).isEmpty()){
                ItemStack stack = playerIn.getHeldItemMainhand();
                NBTTagCompound compound = stack.getTagCompound();
                if(compound != null && compound.hasKey(BlockInfoCopyItem.NBT_BLOCK_ID)) {
                    int id = compound.getInteger(BlockInfoCopyItem.NBT_BLOCK_ID);
                    IBlockState blockState = Block.getStateById(id);
                    if (blockState.getBlock() != Blocks.AIR) {
                        if (Block.getStateId(blockState) != id) {

                            blockWrapper.blockState = blockState;
                            block = blockState.getBlock();

                            TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) worldIn.getTileEntity(pos);
                            transformedBlock.setLocalBlock(blockWrapper);
                            if (blockWrapper.blockState.getBlock().hasTileEntity(blockWrapper.blockState))
                                blockWrapper.tileEntity =
                                        blockWrapper.blockState.getBlock().createTileEntity(worldIn, blockWrapper.blockState);
                        }
                    }
                }
            }
        }

        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        // TODO: 2020/1/18 block init localBlock,tileEntity create Renderer and localBlock
        TileEntityTransformedBlock transformedBlock = new TileEntityTransformedBlock();
        if(blockWrapper.blockState.getBlock().hasTileEntity(blockWrapper.blockState))
            blockWrapper.tileEntity =
                blockWrapper.blockState.getBlock().createTileEntity(world,blockWrapper.blockState);

        transformedBlock.setLocalBlock(blockWrapper);
        return transformedBlock;
    }


    public static class BlockWrapper{
        public int type;
        public IBlockState blockState;
        public TileEntity tileEntity;
    }
}
