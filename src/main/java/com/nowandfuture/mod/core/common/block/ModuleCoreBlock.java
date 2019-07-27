package com.nowandfuture.mod.core.common.block;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.common.entities.TileEntityShowModule;
import com.nowandfuture.mod.core.common.gui.GuiModule;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ModuleCoreBlock extends Block {

    public ModuleCoreBlock() {
        super(Material.SAND);
        setHardness(2);
        setUnlocalizedName("ModuleCoreBlock");
    }

    public ModuleCoreBlock(Material materialIn) {
        super(materialIn);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof TileEntityShowModule){
            InventoryHelper.dropInventoryItems(worldIn, pos, (TileEntityShowModule)tileEntity);
            worldIn.updateComparatorOutputLevel(pos, this);
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(worldIn != null && !worldIn.isRemote){
            playerIn.openGui(Movement.instance,GuiModule.GUI_ID,worldIn,pos.getX(),pos.getY(),pos.getZ());
        }
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityShowModule();
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }
}
