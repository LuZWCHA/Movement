package com.nowandfuture.mod.core.block;

import com.nowandfuture.mod.core.entities.TileEntityMovementModule;
import com.nowandfuture.mod.core.transformers.*;
import com.nowandfuture.mod.core.transformers.animation.KeyFrame;
import com.nowandfuture.mod.core.transformers.animation.TimeLine;
import com.nowandfuture.mod.core.prefab.BasePrefab;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.vecmath.AxisAngle4f;
import java.util.UUID;

public class TestBlock extends Block {

    public static final AxisAlignedBB AABB_BOX = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);

    public TestBlock(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
    }

    public TestBlock() {
        super(Material.SAND);
        setCreativeTab(CreativeTabs.REDSTONE);
        setHardness(2);
        setUnlocalizedName("TestBlock");
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return AABB_BOX;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        constructTest(worldIn,pos,placer.getName());
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }

    public void constructTest(World worldIn, BlockPos pos, String name){
        BasePrefab prefab = new BasePrefab(worldIn,pos.add(1,0,0),new Vec3i(4,4,4));
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof TileEntityMovementModule) {
            ((TileEntityMovementModule) tileEntity).setAuthor(name);
            ((TileEntityMovementModule) tileEntity).setName(UUID.randomUUID().toString());

            ((TileEntityMovementModule) tileEntity).setPrefab(prefab);
            ((TileEntityMovementModule) tileEntity).constructPrefab();

            ScaleTransformNode node1 = new ScaleTransformNode();
            RotationTransformNode node2 = new RotationTransformNode();

            LinearTransformNode node = new LinearTransformNode();
            node.setInterpolation(TimeInterpolation.Type.HIGHER_POWER_DOWN);

            AbstractTransformNode.Builder.newBuilder()
                    .create(node)
                    .parent(node1)
                    .parent(node2)
                    .build();

            ((TileEntityMovementModule) tileEntity).setTransformNode(node);

//            ((TileEntityMovementModule) tileEntity).getLine().addKeyFrames(KeyFrame.KeyFrameType.LINEAR,
//                    new LinearTransformNode.LinearKeyFrame(new BlockPos(0,0,0)),0);
//            ((TileEntityMovementModule) tileEntity).getLine().addKeyFrames(KeyFrame.KeyFrameType.SCALE,
//                    new ScaleTransformNode.ScaleKeyFrame(.5f),0);
            ((TileEntityMovementModule) tileEntity).getLine().addKeyFrames(KeyFrame.KeyFrameType.ROTATION,
                    new RotationTransformNode.RotationKeyFrame(new AxisAngle4f(0,0,1,0)),0);

//            ((TileEntityMovementModule) tileEntity).getLine().addKeyFrames(KeyFrame.KeyFrameType.SCALE,
//                    new ScaleTransformNode.ScaleKeyFrame(2f),50);
//
//            ((TileEntityMovementModule) tileEntity).getLine().addKeyFrames(KeyFrame.KeyFrameType.LINEAR,
//                    new LinearTransformNode.LinearKeyFrame(new BlockPos(2,0,0)),33);
//
//            ((TileEntityMovementModule) tileEntity).getLine().addKeyFrames(KeyFrame.KeyFrameType.LINEAR,
//                    new LinearTransformNode.LinearKeyFrame(new BlockPos(0,0,2)),66);
//
//            ((TileEntityMovementModule) tileEntity).getLine().addKeyFrames(KeyFrame.KeyFrameType.LINEAR,
//                    new LinearTransformNode.LinearKeyFrame(new BlockPos(0,0,0)),100);
//            ((TileEntityMovementModule) tileEntity).getLine().addKeyFrames(KeyFrame.KeyFrameType.SCALE,
//                    new ScaleTransformNode.ScaleKeyFrame(.5f),100);
            ((TileEntityMovementModule) tileEntity).getLine().addKeyFrames(KeyFrame.KeyFrameType.ROTATION,
                    new RotationTransformNode.RotationKeyFrame(new AxisAngle4f(0,1,0, 360)),100);

            ((TileEntityMovementModule) tileEntity).getLine()
                    .setTotalTick(100)
                    .setEnable(true)
                    .setMode(TimeLine.Mode.CYCLE_RESTART)
                    .resetTick();

            ((TileEntityMovementModule) tileEntity).enable();
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityMovementModule();
    }

//    @Nullable
//    @Override
//    public TileEntity createNewTileEntity(World worldIn, int meta) {
//        BasePrefab recipe = new BasePrefab(worldIn,blockPos,new Vec3i(4,4,4));
//        return new ModuleBase(recipe);
//    }
}
