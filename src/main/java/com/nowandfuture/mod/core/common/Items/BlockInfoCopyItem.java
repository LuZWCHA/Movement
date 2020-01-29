package com.nowandfuture.mod.core.common.Items;

import com.nowandfuture.mod.core.common.blocks.TransformedBlock;
import com.nowandfuture.mod.core.prefab.AbstractPrefab;
import com.nowandfuture.mod.core.prefab.LocalWorld;
import joptsimple.internal.Strings;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import java.util.List;

import static com.nowandfuture.mod.Movement.MODID;

public class BlockInfoCopyItem extends Item {

    private static String NBT_IS_EMPTY = "Empty";
    public static String NBT_BLOCK_ID = "BlockId";

    public BlockInfoCopyItem() {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        addPropertyOverride(new ResourceLocation(MODID, "empty"), new IItemPropertyGetter() {
            @Override
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
                return isEmpty(stack.getTagCompound()) ? 1:0;//1 - isReady
            }
        });
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        NBTTagCompound nbt = stack.getTagCompound();
        if(nbt != null){
            if(nbt.hasKey(NBT_BLOCK_ID)) {
                int id = nbt.getInteger(NBT_BLOCK_ID);
                IBlockState blockState = Block.getStateById(id);
                tooltip.add(I18n.format(blockState.getBlock().getUnlocalizedName())+
                        " (" +
                        blockState.getBlock().getMetaFromState(blockState) +
                        ")");
            }
        }
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        IBlockState blockState = world.getBlockState(pos);
        if(blockState.getBlock() != Blocks.AIR &&
                blockState.getBlock().delegate.get().getClass() != TransformedBlock.class){
            LocalWorld.LocalBlock localBlock = new LocalWorld.LocalBlock(pos, blockState);

            if(hand == EnumHand.MAIN_HAND) {
                NBTTagCompound compound = player.getHeldItem(hand).getTagCompound();
                if(compound == null)
                    compound = new NBTTagCompound();

                int id = Block.getStateId(localBlock.blockState);

                compound.setInteger(NBT_BLOCK_ID, id);

                player.getHeldItem(hand).setTagCompound(compound);
            }
        }
        return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }

    public boolean isEmpty(NBTTagCompound nbt){
        boolean isEmpty = nbt != null &&
                nbt.hasKey(NBT_IS_EMPTY) &&
                nbt.getBoolean(NBT_IS_EMPTY);
        return isEmpty;
    }
}
