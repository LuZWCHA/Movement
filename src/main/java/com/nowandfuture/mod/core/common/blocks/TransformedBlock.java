package com.nowandfuture.mod.core.common.blocks;

import com.google.common.base.Predicate;
import com.nowandfuture.mod.core.client.renders.TransformedBlockRenderMap;
import com.nowandfuture.mod.core.common.Items.BlockInfoCopyItem;
import com.nowandfuture.mod.core.common.TransformedBlockWorld;
import com.nowandfuture.mod.core.common.entities.TileEntityTransformedBlock;
import com.nowandfuture.mod.handler.RegisterHandler;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class TransformedBlock extends Block {
    private boolean isEdited;

    public TransformedBlock(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
    }

    public TransformedBlock(){
        this(Material.WEB);

        setUnlocalizedName("TransformedBlock");
    }

    public TransformedBlock(Material materialIn) {
        super(materialIn);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.TRANSLUCENT;
    }


    //render all
    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return super.isFullCube(state);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return super.isOpaqueCube(state);
    }

    @Nullable
    @Override
    public Boolean isEntityInsideMaterial(IBlockAccess world, BlockPos blockpos, IBlockState iblockstate, Entity entity, double yToTest, Material materialIn, boolean testingHead) {

        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) world.getTileEntity(blockpos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        return block == null ? super.isEntityInsideMaterial(world, blockpos, iblockstate, entity, yToTest, materialIn, testingHead) :
                block.isEntityInsideMaterial(world, blockpos, transformedBlock.getLocalBlock().blockState, entity, yToTest, materialIn, testingHead);
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        return false;
    }

    @Override
    public boolean isReplaceableOreGen(IBlockState state, IBlockAccess world, BlockPos pos, Predicate<IBlockState> target) {
        return super.isReplaceableOreGen(state, world, pos, target);
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) world.getTileEntity(pos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        return block == null ? super.getLightValue(state, world, pos) :
                block.getLightValue(transformedBlock.getLocalBlock().blockState, world, pos);
    }

    @Override
    public IBlockState getStateAtViewpoint(IBlockState state, IBlockAccess world, BlockPos pos, Vec3d viewpoint) {
        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) world.getTileEntity(pos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        return block == null ? super.getStateAtViewpoint(state, world, pos, viewpoint) :
                block.getStateAtViewpoint(state, world, pos, viewpoint);
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) world.getTileEntity(pos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        return block == null ? super.isNormalCube(state, world, pos) :
                block.isNormalCube(transformedBlock.getLocalBlock().blockState, world, pos);

    }

    @Override
    public boolean isAir(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) world.getTileEntity(pos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        return block == null ? super.isAir(state, world, pos) :
                block.isAir(transformedBlock.getLocalBlock().blockState, world, pos);
    }

    @Override
    public boolean isLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) world.getTileEntity(pos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        return block == null ? super.isLeaves(state, world, pos) :
                block.isLeaves(transformedBlock.getLocalBlock().blockState, world, pos);
    }

    @Override
    public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable) {
        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) world.getTileEntity(pos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        return block == null ? super.canSustainPlant(state, world, pos,direction,plantable) :
                block.canSustainPlant(transformedBlock.getLocalBlock().blockState, world, pos,direction,plantable);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) worldIn.getTileEntity(pos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        return block == null ? super.getCollisionBoundingBox(blockState, worldIn, pos) :
                block.getCollisionBoundingBox(transformedBlock.getLocalBlock().blockState, worldIn, pos);
    }

    @Override
    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, @Nullable Entity entity) {
        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) world.getTileEntity(pos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        return block == null ? super.getSoundType(state, world, pos, entity) :
                block.getSoundType(transformedBlock.getLocalBlock().blockState, world, pos, entity);
    }

    //fixed
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) source.getTileEntity(pos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        return block == null ? super.getBoundingBox(state,source,pos) :
                block.getBoundingBox(transformedBlock.getLocalBlock().blockState, source, pos);
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) world.getTileEntity(pos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        return block == null ? super.getExtendedState(state,world,pos) :
                block.getExtendedState(transformedBlock.getLocalBlock().blockState, world, pos);
    }

    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) worldIn.getTileEntity(pos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        return block == null ? super.getMapColor(state,worldIn,pos) :
                block.getMapColor(transformedBlock.getLocalBlock().blockState, worldIn, pos);
    }

    @Override
    public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos) {
        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) source.getTileEntity(pos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        return block == null ? super.getPackedLightmapCoords(state,source,pos) :
                block.getPackedLightmapCoords(transformedBlock.getLocalBlock().blockState, source, pos);
    }

    @Override
    public Vec3d getOffset(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) worldIn.getTileEntity(pos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        return block == null ? super.getOffset(state,worldIn,pos) :
                block.getOffset(transformedBlock.getLocalBlock().blockState, worldIn, pos);
    }

    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) world.getTileEntity(pos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        return block == null ? super.getLightOpacity(state,world,pos) :
                block.getLightOpacity(transformedBlock.getLocalBlock().blockState, world, pos);
    }

    @Nullable
    @Override
    public PathNodeType getAiPathNodeType(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) world.getTileEntity(pos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        return block == null ? super.getAiPathNodeType(state,world,pos) :
                block.getAiPathNodeType(transformedBlock.getLocalBlock().blockState, world, pos);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) worldIn.getTileEntity(pos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        return block == null ? super.getActualState(state,worldIn,pos) :
                block.getActualState(transformedBlock.getLocalBlock().blockState, worldIn, pos);
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) worldIn.getTileEntity(pos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        return block == null ? super.getSelectedBoundingBox(state,worldIn,pos) :
                block.getSelectedBoundingBox(transformedBlock.getLocalBlock().blockState, worldIn, pos);
    }

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) worldIn.getTileEntity(pos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        return block == null ? super.collisionRayTrace(blockState, worldIn, pos, start, end) :
                block.collisionRayTrace(transformedBlock.getLocalBlock().blockState, worldIn, pos, start, end);
    }

    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn) {
        super.onBlockClicked(worldIn, pos, playerIn);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) worldIn.getTileEntity(pos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        if (block == null) {
            super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
        } else {
            block.addCollisionBoxToList(transformedBlock.getLocalBlock().blockState, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
        }
    }

    @Override
    public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param) {
        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) worldIn.getTileEntity(pos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        return block == null ? super.eventReceived(state,worldIn,pos,id,param) :
                block.eventReceived(transformedBlock.getLocalBlock().blockState, worldIn, pos,id,param);

    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) world.getTileEntity(pos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        return block == null ? super.canConnectRedstone(state, world, pos, side):
                block.canConnectRedstone(transformedBlock.getLocalBlock().blockState, world, pos, side);
    }

    @Override
    public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        return super.canBeConnectedTo(world, pos, facing);
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) worldIn.getTileEntity(pos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        if(block == null)
            super.harvestBlock(worldIn, player, pos, state, te, stack);
        else
            block.harvestBlock(worldIn, player, pos, transformedBlock.getLocalBlock().blockState, te, stack);
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) world.getTileEntity(pos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        if(block == null)
            return super.removedByPlayer(state, world, pos, player, willHarvest);
        else
            return block.removedByPlayer(transformedBlock.getLocalBlock().blockState, world, pos, player, willHarvest);

    }

    @Override
    public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state) {

        super.onBlockDestroyedByPlayer(worldIn, pos, state);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (hand == EnumHand.MAIN_HAND) {
            if (!playerIn.getHeldItem(hand).isEmpty()) {
                ItemStack stack = playerIn.getHeldItemMainhand();
                NBTTagCompound compound = stack.getTagCompound();
                if (compound != null && compound.hasKey(BlockInfoCopyItem.NBT_BLOCK_ID)) {
                    int id = compound.getInteger(BlockInfoCopyItem.NBT_BLOCK_ID);
                    IBlockState blockState = Block.getStateById(id);
                    if (blockState.getBlock() != Blocks.AIR) {
                        if (blockState.getBlock().getClass() != this.getClass()) {

                            BlockWrapper blockWrapper = new BlockWrapper();
                            blockWrapper.blockState = blockState;

                            TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) worldIn.getTileEntity(pos);

                            TransformedBlockWorld world = new TransformedBlockWorld(worldIn, blockWrapper, pos);

                            if (blockWrapper.blockState.getBlock().hasTileEntity(blockWrapper.blockState)) {
                                if (blockWrapper.tileEntity != null) blockWrapper.tileEntity.invalidate();
                                blockWrapper.tileEntity =
                                        blockWrapper.blockState.getBlock().createTileEntity(worldIn, blockWrapper.blockState);
                                blockWrapper.tileEntity.setPos(pos);
                                blockWrapper.tileEntity.setWorld(world);
                                blockWrapper.tileEntity.validate();
                                blockWrapper.tileEntity.onLoad();
                            }

                            transformedBlock.setLocalBlock(blockWrapper);

                            int blockLight = blockState.getLightValue();

                            world.setLightFor(EnumSkyBlock.BLOCK, pos, blockLight);
                            world.checkLight(pos);
                            world.checkLight(pos.offset(EnumFacing.UP));
                            world.checkLight(pos.offset(EnumFacing.DOWN));
                            world.checkLight(pos.offset(EnumFacing.WEST));
                            world.checkLight(pos.offset(EnumFacing.EAST));
                            world.checkLight(pos.offset(EnumFacing.NORTH));
                            world.checkLight(pos.offset(EnumFacing.SOUTH));

                            worldIn.notifyBlockUpdate(pos, state, blockState, worldIn.isRemote ? 3 : 11);
                            transformedBlock.setUpdateVBO(true);
                            return true;
                        }
                    }
                }
            }
        }

        if (worldIn.isRemote) {
            TileEntity transformedBlock = worldIn.getTileEntity(pos);
            if (transformedBlock instanceof TileEntityTransformedBlock) {
                if (!((TileEntityTransformedBlock) transformedBlock).isEdited() && !TransformedBlockRenderMap.INSTANCE.isHasEdited()) {
                    ((TileEntityTransformedBlock) transformedBlock).toggleEditState();
                    TransformedBlockRenderMap.INSTANCE.setHasEdited(true);
                } else if (((TileEntityTransformedBlock) transformedBlock).isEdited() && TransformedBlockRenderMap.INSTANCE.isHasEdited()) {
                    ((TileEntityTransformedBlock) transformedBlock).toggleEditState();
                    TransformedBlockRenderMap.INSTANCE.setHasEdited(false);
                }
            }
        }

        return true;
    }



    @Override
    public boolean getUseNeighborBrightness(IBlockState state) {
        return false;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        TileEntityTransformedBlock transformedBlock = new TileEntityTransformedBlock();
        transformedBlock.setWorld(world);
        transformedBlock.setLocalBlock(new BlockWrapper());
        transformedBlock.getLocalBlock().blockState = Blocks.SAND.getDefaultState();
        return transformedBlock;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    public static class BlockWrapper{
        public int type;
        public IBlockState blockState;
        public TileEntity tileEntity;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) worldIn.getTileEntity(pos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        if(block != null)
            block.updateTick(worldIn, pos, transformedBlock.getLocalBlock().blockState, rand);
        else
            super.updateTick(worldIn, pos, state, rand);
    }

    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) worldIn.getTileEntity(pos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        if(block != null) {
            block.randomDisplayTick(transformedBlock.getLocalBlock().blockState, worldIn, pos, rand);
        } else
            super.randomDisplayTick(stateIn,  worldIn, pos,rand);
    }

    @Override
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {
        TileEntityTransformedBlock transformedBlock = (TileEntityTransformedBlock) worldIn.getTileEntity(pos);
        Block block = null;
        if(transformedBlock != null && transformedBlock.getLocalBlock().blockState != null){
            block = transformedBlock.getLocalBlock().blockState.getBlock();
        }
        if(block != null)
            block.randomTick(worldIn, pos, transformedBlock.getLocalBlock().blockState, random);
        else
            super.randomTick(worldIn, pos, state, random);
    }
}
