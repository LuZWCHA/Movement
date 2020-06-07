package com.nowandfuture.mod.core.common.blocks;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.common.entities.TileEntityCoreModule;
import com.nowandfuture.mod.core.common.entities.TileEntityModule;
import com.nowandfuture.mod.core.common.gui.GuiModule;
import com.nowandfuture.mod.core.common.gui.mygui.api.IDynamicInventory;
import com.nowandfuture.mod.network.NetworkHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ModuleCoreBlock extends BlockDirectional {

    public static final PropertyBool POWERED = PropertyBool.create("powered");

    public ModuleCoreBlock() {
        super(Material.ROCK);
        setHardness(2);
        setTranslationKey("ModuleCoreBlock");
        setDefaultState(this.getDefaultState().withProperty(POWERED,false));
    }

    public ModuleCoreBlock(Material materialIn) {
        super(materialIn);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof TileEntityCoreModule){
            InventoryHelper.dropInventoryItems(worldIn, pos, (TileEntityCoreModule)tileEntity);
            List<IDynamicInventory> list = ((TileEntityCoreModule) tileEntity).collectAllDynInventories();
            for (IDynamicInventory di :
                    list) {
                IDynamicInventory.dropInventoryItems(worldIn,pos,di);
            }
            worldIn.updateComparatorOutputLevel(pos, this);
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if(!worldIn.isRemote){
            updateState(worldIn,pos,state);
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(worldIn != null && !worldIn.isRemote){
            playerIn.openGui(Movement.instance,GuiModule.GUI_ID,worldIn,pos.getX(),pos.getY(),pos.getZ());
        }
        return true;
    }

    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta & 7));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        TileEntity tileEntity = source.getTileEntity(pos);
        if(tileEntity instanceof TileEntityCoreModule)
            return ((TileEntityCoreModule) tileEntity).isShowBlock() ? FULL_BLOCK_AABB : NULL_AABB;
        return super.getBoundingBox(state, source, pos);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = 0;
        i = i | state.getValue(FACING).getIndex();

        if (state.getValue(POWERED))
        {
            i |= 8;
        }

        return i;
    }

    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return this.getDefaultState().withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer).getOpposite());
    }

    public IBlockState withRotation(IBlockState state, Rotation rot)
    {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        this.updateState(worldIn, pos, state);
    }

    private void updateState(World world, BlockPos pos, IBlockState state)
    {
        if(!world.isRemote) {
            boolean isPowered = world.isBlockPowered(pos);
            TileEntity tileEntity = world.getTileEntity(pos);

            if(state.getValue(POWERED) != isPowered) {
                state = state.withProperty(POWERED, isPowered);
                world.setBlockState(pos, state, 2);
            }

            if (tileEntity instanceof TileEntityModule) {
                if (isPowered && !((TileEntityModule) tileEntity).getLine().isEnable()) {
                    ((TileEntityModule) tileEntity).getLine().start();
                    NetworkHandler.syncToTrackingClients(world,tileEntity,((TileEntityModule) tileEntity).getTimelineUpdatePacket(((TileEntityModule) tileEntity).getLine().getTick(),((TileEntityModule) tileEntity).getLine().isEnable()));
                } else if(!isPowered && ((TileEntityModule) tileEntity).getLine().isEnable()){
                    ((TileEntityModule) tileEntity).getLine().stop();
                    NetworkHandler.syncToTrackingClients(world,tileEntity,((TileEntityModule) tileEntity).getTimelineUpdatePacket(((TileEntityModule) tileEntity).getLine().getTick(),((TileEntityModule) tileEntity).getLine().isEnable()));
                }
            }

        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, POWERED,FACING);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        TileEntityCoreModule coreModule = new TileEntityCoreModule();
        coreModule.setFacing(state.getValue(FACING));
        return coreModule;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }
}
