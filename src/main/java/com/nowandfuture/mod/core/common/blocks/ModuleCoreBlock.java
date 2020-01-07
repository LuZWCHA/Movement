package com.nowandfuture.mod.core.common.blocks;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.common.entities.TileEntityModule;
import com.nowandfuture.mod.core.common.entities.TileEntityShowModule;
import com.nowandfuture.mod.core.common.gui.GuiModule;
import com.nowandfuture.mod.core.selection.AxisAlignedBBWrap;
import com.nowandfuture.mod.core.selection.OBBox;
import com.nowandfuture.mod.network.NetworkHandler;
import com.nowandfuture.mod.utils.math.Matrix4f;
import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
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
        setUnlocalizedName("ModuleCoreBlock");
        setDefaultState(this.getDefaultState().withProperty(POWERED,false));
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
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {

        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if(!worldIn.isRemote){
            updateState(worldIn,pos,state);
        }
        super.onBlockAdded(worldIn, pos, state);
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
        EnumFacing enumfacing;

        switch (meta & 7)
        {
            case 0:
                enumfacing = EnumFacing.DOWN;
                break;
            case 1:
                enumfacing = EnumFacing.EAST;
                break;
            case 2:
                enumfacing = EnumFacing.WEST;
                break;
            case 3:
                enumfacing = EnumFacing.SOUTH;
                break;
            case 4:
                enumfacing = EnumFacing.NORTH;
                break;
            case 5:
            default:
                enumfacing = EnumFacing.UP;
        }

        return this.getDefaultState().withProperty(POWERED, (meta & 8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = 0;

//        switch (state.getValue(FACING))
//        {
//            case EAST:
//                i = 1;
//                break;
//            case WEST:
//                i = 2;
//                break;
//            case SOUTH:
//                i = 3;
//                break;
//            case NORTH:
//                i = 4;
//                break;
//            case UP:
//            default:
//                i = 5;
//                break;
//            case DOWN:
//                i = 0;
//        }
        return i|(state.getValue(POWERED) ? 8:0);
    }


    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        this.updateState(worldIn, pos, state);
    }

    private void updateState(World world, BlockPos pos, IBlockState state)
    {
        boolean flag = world.isBlockPowered(pos);

        if(!world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(pos);
            state = state.withProperty(POWERED,flag);
//            world.notifyBlockUpdate(pos,state,state,3);
            world.setBlockState(pos, state, 3);
            world.notifyNeighborsOfStateChange(pos,this,false);

            if (tileEntity != null && tileEntity instanceof TileEntityModule) {
                if (flag && !((TileEntityModule) tileEntity).getLine().isEnable()) {
                    ((TileEntityModule) tileEntity).getLine().start();
                    NetworkHandler.syncToTrackingClients(world,tileEntity,((TileEntityModule) tileEntity).getTimelineUpdatePacket(((TileEntityModule) tileEntity).getLine().getTick(),((TileEntityModule) tileEntity).getLine().isEnable()));
                } else if(!flag && ((TileEntityModule) tileEntity).getLine().isEnable()){
                    ((TileEntityModule) tileEntity).getLine().stop();
                    NetworkHandler.syncToTrackingClients(world,tileEntity,((TileEntityModule) tileEntity).getTimelineUpdatePacket(((TileEntityModule) tileEntity).getLine().getTick(),((TileEntityModule) tileEntity).getLine().isEnable()));
                }
            }

        }
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {

//        TileEntity tileEntity = worldIn.getTileEntity(pos);
//        if(tileEntity == null || tileEntity.isInvalid()) {
//
//
//        }else{
//            if(tileEntity instanceof TileEntityModule){
//                TileEntityModule module = ((TileEntityModule) tileEntity);
//                AxisAlignedBB axisAlignedBB = module.getModuleBase().getMinAABB();
//                if(axisAlignedBB != null) {
//
//                    OBBox obBox = new OBBox(axisAlignedBB);
//                    Matrix4f matrix4f = module.getModuleBase().getTransRes();
//                    obBox.mulMatrix(matrix4f);
//                    obBox.translate(module.getModulePos());
//
//                    doCollisionTest(obBox,entityBox, collidingBoxes, entityIn);
//                    return;
//                }
//            }
//        }
        super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);

    }

    public void doCollisionTest(OBBox box,AxisAlignedBB abb, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity){
        if(entity != null){

            float impactTime = 0;
            Vector3f v;

            //noinspection PointlessNullCheck
            Movement.logger.info(entity.getName() + entity.getClass());
            AxisAlignedBB orgAABB = entity.getEntityBoundingBox();
            v = new Vector3f(
                    (float) (abb.minX - orgAABB.minX),
                    (float) (abb.minY - orgAABB.minY),
                    (float) (abb.minZ - orgAABB.minZ)
            );

            if(!box.intersect(orgAABB)) {
                impactTime = box.sweepTest(orgAABB, v);
            }else{
                impactTime = -1;
            }
//                            Movement.logger.info(impactTime);
            collidingBoxes.add(new AxisAlignedBBWrap(box,impactTime,v));

        }

    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, POWERED);
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
