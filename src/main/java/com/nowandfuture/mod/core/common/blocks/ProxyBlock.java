package com.nowandfuture.mod.core.common.blocks;

import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class ProxyBlock extends Block {
    protected Block block;

    public static int getIdFromBlock(Block blockIn) {
        return Block.getIdFromBlock(blockIn);
    }

    public static int getStateId(IBlockState state) {
        return Block.getStateId(state);
    }

    public static Block getBlockById(int id) {
        return Block.getBlockById(id);
    }

    public static IBlockState getStateById(int id) {
        return Block.getStateById(id);
    }

    public static Block getBlockFromItem(@Nullable Item itemIn) {
        return Block.getBlockFromItem(itemIn);
    }

    @Nullable
    public static Block getBlockFromName(String name) {
        return Block.getBlockFromName(name);
    }

    @Override
    @Deprecated
    public boolean isTopSolid(IBlockState state) {
        return block.isTopSolid(state);
    }

    @Override
    @Deprecated
    public boolean isFullBlock(IBlockState state) {
        return block.isFullBlock(state);
    }

    @Override
    @Deprecated
    public boolean canEntitySpawn(IBlockState state, Entity entityIn) {
        return block.canEntitySpawn(state, entityIn);
    }

    @Override
    @Deprecated
    public int getLightOpacity(IBlockState state) {
        return block.getLightOpacity(state);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @Deprecated
    public boolean isTranslucent(IBlockState state) {
        return block.isTranslucent(state);
    }

    @Override
    @Deprecated
    public int getLightValue(IBlockState state) {
        return block.getLightValue(state);
    }

    @Override
    @Deprecated
    public boolean getUseNeighborBrightness(IBlockState state) {
        return block.getUseNeighborBrightness(state);
    }

    @Override
    @Deprecated
    public Material getMaterial(IBlockState state) {
        return block.getMaterial(state);
    }

    @Override
    @Deprecated
    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return block.getMapColor(state, worldIn, pos);
    }

    @Override
    @Deprecated
    public IBlockState getStateFromMeta(int meta) {
        return block.getStateFromMeta(meta);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return block.getMetaFromState(state);
    }

    @Override
    @Deprecated
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return block.getActualState(state, worldIn, pos);
    }

    @Override
    @Deprecated
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return block.withRotation(state, rot);
    }

    @Override
    @Deprecated
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return block.withMirror(state, mirrorIn);
    }

    @Override
    public Block setLightOpacity(int opacity) {
        return block.setLightOpacity(opacity);
    }

    @Override
    public Block setLightLevel(float value) {
        return block.setLightLevel(value);
    }

    @Override
    public Block setResistance(float resistance) {
        return block.setResistance(resistance);
    }

    public static boolean isExceptionBlockForAttaching(Block attachBlock) {
        return Block.isExceptionBlockForAttaching(attachBlock);
    }

    public static boolean isExceptBlockForAttachWithPiston(Block attachBlock) {
        return Block.isExceptBlockForAttachWithPiston(attachBlock);
    }

    @Override
    @Deprecated
    public boolean isBlockNormalCube(IBlockState state) {
        return block.isBlockNormalCube(state);
    }

    @Override
    @Deprecated
    public boolean isNormalCube(IBlockState state) {
        return block.isNormalCube(state);
    }

    @Override
    @Deprecated
    public boolean causesSuffocation(IBlockState state) {
        return block.causesSuffocation(state);
    }

    @Override
    @Deprecated
    public boolean isFullCube(IBlockState state) {
        return block.isFullCube(state);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @Deprecated
    public boolean hasCustomBreakingProgress(IBlockState state) {
        return block.hasCustomBreakingProgress(state);
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return block.isPassable(worldIn, pos);
    }

    @Override
    @Deprecated
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return block.getRenderType(state);
    }

    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return block.isReplaceable(worldIn, pos);
    }

    @Override
    @Deprecated
    public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
        return block.getBlockHardness(blockState, worldIn, pos);
    }

    @Override
    public Block setTickRandomly(boolean shouldTick) {
        return block.setTickRandomly(shouldTick);
    }

    @Override
    public boolean getTickRandomly() {
        return block.getTickRandomly();
    }

    @Override
    @Deprecated
    public boolean hasTileEntity() {
        return block.hasTileEntity();
    }

    @Override
    @Deprecated
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return block.getBoundingBox(state, source, pos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @Deprecated
    public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos) {
        return block.getPackedLightmapCoords(state, source, pos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @Deprecated
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return block.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }

    @Override
    @Deprecated
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return block.getBlockFaceShape(worldIn, state, pos, face);
    }

    @Override
    @Deprecated
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        block.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
    }

    @Override
    @Nullable
    @Deprecated
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return block.getCollisionBoundingBox(blockState, worldIn, pos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @Deprecated
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
        return block.getSelectedBoundingBox(state, worldIn, pos);
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState state) {
        return block.isOpaqueCube(state);
    }

    @Override
    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
        return block.canCollideCheck(state, hitIfLiquid);
    }

    @Override
    public boolean isCollidable() {
        return block.isCollidable();
    }

    @Override
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {
        block.randomTick(worldIn, pos, state, random);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        block.updateTick(worldIn, pos, state, rand);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        block.randomDisplayTick(stateIn, worldIn, pos, rand);
    }

    @Override
    public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state) {
        block.onBlockDestroyedByPlayer(worldIn, pos, state);
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        block.neighborChanged(state, worldIn, pos, blockIn, fromPos);
    }

    @Override
    public int tickRate(World worldIn) {
        return block.tickRate(worldIn);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        block.onBlockAdded(worldIn, pos, state);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        block.breakBlock(worldIn, pos, state);
    }

    @Override
    public int quantityDropped(Random random) {
        return block.quantityDropped(random);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return block.getItemDropped(state, rand, fortune);
    }

    @Override
    @Deprecated
    public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World worldIn, BlockPos pos) {
        return block.getPlayerRelativeBlockHardness(state, player, worldIn, pos);
    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
        block.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);
    }

    @Override
    public void dropXpOnBlockBreak(World worldIn, BlockPos pos, int amount) {
        block.dropXpOnBlockBreak(worldIn, pos, amount);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return block.damageDropped(state);
    }

    @Override
    @Deprecated
    public float getExplosionResistance(Entity exploder) {
        return block.getExplosionResistance(exploder);
    }

    @Override
    @Nullable
    @Deprecated
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        return block.collisionRayTrace(blockState, worldIn, pos, start, end);
    }

    @Override
    public void onBlockDestroyedByExplosion(World worldIn, BlockPos pos, Explosion explosionIn) {
        block.onBlockDestroyedByExplosion(worldIn, pos, explosionIn);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return block.getBlockLayer();
    }

    @Override
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
        return block.canPlaceBlockOnSide(worldIn, pos, side);
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return block.canPlaceBlockAt(worldIn, pos);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return block.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        block.onEntityWalk(worldIn, pos, entityIn);
    }

    @Override
    @Deprecated
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return block.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
    }

    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn) {
        block.onBlockClicked(worldIn, pos, playerIn);
    }

    @Override
    public Vec3d modifyAcceleration(World worldIn, BlockPos pos, Entity entityIn, Vec3d motion) {
        return block.modifyAcceleration(worldIn, pos, entityIn, motion);
    }

    @Override
    @Deprecated
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return block.getWeakPower(blockState, blockAccess, pos, side);
    }

    @Override
    @Deprecated
    public boolean canProvidePower(IBlockState state) {
        return block.canProvidePower(state);
    }

    @Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        block.onEntityCollidedWithBlock(worldIn, pos, state, entityIn);
    }

    @Override
    @Deprecated
    public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return block.getStrongPower(blockState, blockAccess, pos, side);
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
        block.harvestBlock(worldIn, player, pos, state, te, stack);
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        return block.quantityDroppedWithBonus(fortune, random);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        block.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }

    @Override
    public boolean canSpawnInBlock() {
        return block.canSpawnInBlock();
    }

    @Override
    public Block setUnlocalizedName(String name) {
        return block.setUnlocalizedName(name);
    }

    @Override
    public String getLocalizedName() {
        return block.getLocalizedName();
    }

    @Override
    public String getUnlocalizedName() {
        return block.getUnlocalizedName();
    }

    @Override
    @Deprecated
    public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param) {
        return block.eventReceived(state, worldIn, pos, id, param);
    }

    @Override
    @Deprecated
    public EnumPushReaction getMobilityFlag(IBlockState state) {
        return block.getMobilityFlag(state);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @Deprecated
    public float getAmbientOcclusionLightValue(IBlockState state) {
        return block.getAmbientOcclusionLightValue(state);
    }

    @Override
    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
        block.onFallenUpon(worldIn, pos, entityIn, fallDistance);
    }

    @Override
    public void onLanded(World worldIn, Entity entityIn) {
        block.onLanded(worldIn, entityIn);
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        block.getSubBlocks(itemIn, items);
    }

    @Override
    public CreativeTabs getCreativeTabToDisplayOn() {
        return super.getCreativeTabToDisplayOn();
    }

    @Override
    public Block setCreativeTab(CreativeTabs tab) {
        return super.setCreativeTab(tab);
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        block.onBlockHarvested(worldIn, pos, state, player);
    }

    @Override
    public void fillWithRain(World worldIn, BlockPos pos) {
        block.fillWithRain(worldIn, pos);
    }

    @Override
    public boolean requiresUpdates() {
        return block.requiresUpdates();
    }

    @Override
    public boolean canDropFromExplosion(Explosion explosionIn) {
        return block.canDropFromExplosion(explosionIn);
    }

    @Override
    public boolean isAssociatedBlock(Block other) {
        return block.isAssociatedBlock(other);
    }

    public static boolean isEqualTo(Block blockIn, Block other) {
        return Block.isEqualTo(blockIn, other);
    }

    @Override
    @Deprecated
    public boolean hasComparatorInputOverride(IBlockState state) {
        return block.hasComparatorInputOverride(state);
    }

    @Override
    @Deprecated
    public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
        return block.getComparatorInputOverride(blockState, worldIn, pos);
    }

    @Override
    public BlockStateContainer getBlockState() {
        return block.getBlockState();
    }

    @Override
    public EnumOffsetType getOffsetType() {
        return block.getOffsetType();
    }

    @Override
    @Deprecated
    public Vec3d getOffset(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return block.getOffset(state, worldIn, pos);
    }

    @Override
    public String toString() {
        return block.toString();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        block.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    public float getSlipperiness(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable Entity entity) {
        return block.getSlipperiness(state, world, pos, entity);
    }

    @Override
    public void setDefaultSlipperiness(float slipperiness) {
        block.setDefaultSlipperiness(slipperiness);
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return block.getLightValue(state, world, pos);
    }

    @Override
    public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
        return block.isLadder(state, world, pos, entity);
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return block.isNormalCube(state, world, pos);
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        return block.doesSideBlockRendering(state, world, pos, face);
    }

    @Override
    @Deprecated
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return block.isSideSolid(base_state, world, pos, side);
    }

    @Override
    public boolean isBurning(IBlockAccess world, BlockPos pos) {
        return block.isBurning(world, pos);
    }

    @Override
    public boolean isAir(IBlockState state, IBlockAccess world, BlockPos pos) {
        return block.isAir(state, world, pos);
    }

    @Override
    public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return block.canHarvestBlock(world, pos, player);
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        return block.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return block.getFlammability(world, pos, face);
    }

    @Override
    public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return block.isFlammable(world, pos, face);
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return block.getFireSpreadSpeed(world, pos, face);
    }

    @Override
    public boolean isFireSource(World world, BlockPos pos, EnumFacing side) {
        return block.isFireSource(world, pos, side);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return block.hasTileEntity(state);
    }

    @Override
    @Nullable
    public TileEntity createTileEntity(World world, IBlockState state) {
        return block.createTileEntity(world, state);
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
        return block.quantityDropped(state, fortune, random);
    }

    @Override
    @Deprecated
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        return block.getDrops(world, pos, state, fortune);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        block.getDrops(drops, world, pos, state, fortune);
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return block.canSilkHarvest(world, pos, state, player);
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return block.canCreatureSpawn(state, world, pos, type);
    }

    @Override
    public boolean isBed(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable Entity player) {
        return block.isBed(state, world, pos, player);
    }

    @Override
    @Nullable
    public BlockPos getBedSpawnPosition(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EntityPlayer player) {
        return block.getBedSpawnPosition(state, world, pos, player);
    }

    @Override
    public void setBedOccupied(IBlockAccess world, BlockPos pos, EntityPlayer player, boolean occupied) {
        block.setBedOccupied(world, pos, player, occupied);
    }

    @Override
    public EnumFacing getBedDirection(IBlockState state, IBlockAccess world, BlockPos pos) {
        return block.getBedDirection(state, world, pos);
    }

    @Override
    public boolean isBedFoot(IBlockAccess world, BlockPos pos) {
        return block.isBedFoot(world, pos);
    }

    @Override
    public void beginLeavesDecay(IBlockState state, World world, BlockPos pos) {
        block.beginLeavesDecay(state, world, pos);
    }

    @Override
    public boolean canSustainLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
        return block.canSustainLeaves(state, world, pos);
    }

    @Override
    public boolean isLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
        return block.isLeaves(state, world, pos);
    }

    @Override
    public boolean canBeReplacedByLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
        return block.canBeReplacedByLeaves(state, world, pos);
    }

    @Override
    public boolean isWood(IBlockAccess world, BlockPos pos) {
        return block.isWood(world, pos);
    }

    @Override
    public boolean isReplaceableOreGen(IBlockState state, IBlockAccess world, BlockPos pos, Predicate<IBlockState> target) {
        return block.isReplaceableOreGen(state, world, pos, target);
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        return block.getExplosionResistance(world, pos, exploder, explosion);
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
        block.onBlockExploded(world, pos, explosion);
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
        return block.canConnectRedstone(state, world, pos, side);
    }

    @Override
    public boolean canPlaceTorchOnTop(IBlockState state, IBlockAccess world, BlockPos pos) {
        return block.canPlaceTorchOnTop(state, world, pos);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return block.getPickBlock(state, target, world, pos, player);
    }

    @Override
    public boolean isFoliage(IBlockAccess world, BlockPos pos) {
        return block.isFoliage(world, pos);
    }

    @Override
    public boolean addLandingEffects(IBlockState state, WorldServer worldObj, BlockPos blockPosition, IBlockState iblockstate, EntityLivingBase entity, int numberOfParticles) {
        return block.addLandingEffects(state, worldObj, blockPosition, iblockstate, entity, numberOfParticles);
    }

    @Override
    public boolean addRunningEffects(IBlockState state, World world, BlockPos pos, Entity entity) {
        return block.addRunningEffects(state, world, pos, entity);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(IBlockState state, World worldObj, RayTraceResult target, ParticleManager manager) {
        return block.addHitEffects(state, worldObj, target, manager);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
        return block.addDestroyEffects(world, pos, manager);
    }

    @Override
    public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable) {
        return block.canSustainPlant(state, world, pos, direction, plantable);
    }

    @Override
    public void onPlantGrow(IBlockState state, World world, BlockPos pos, BlockPos source) {
        block.onPlantGrow(state, world, pos, source);
    }

    @Override
    public boolean isFertile(World world, BlockPos pos) {
        return block.isFertile(world, pos);
    }

    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
        return block.getLightOpacity(state, world, pos);
    }

    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
        return block.canEntityDestroy(state, world, pos, entity);
    }

    @Override
    public boolean isBeaconBase(IBlockAccess worldObj, BlockPos pos, BlockPos beacon) {
        return block.isBeaconBase(worldObj, pos, beacon);
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
        return block.rotateBlock(world, pos, axis);
    }

    @Override
    @Nullable
    public EnumFacing[] getValidRotations(World world, BlockPos pos) {
        return block.getValidRotations(world, pos);
    }

    @Override
    public float getEnchantPowerBonus(World world, BlockPos pos) {
        return block.getEnchantPowerBonus(world, pos);
    }

    @Override
    public boolean recolorBlock(World world, BlockPos pos, EnumFacing side, EnumDyeColor color) {
        return block.recolorBlock(world, pos, side, color);
    }

    @Override
    public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) {
        return block.getExpDrop(state, world, pos, fortune);
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
        block.onNeighborChange(world, pos, neighbor);
    }

    @Override
    public void observedNeighborChange(IBlockState observerState, World world, BlockPos observerPos, Block changedBlock, BlockPos changedBlockPos) {
        block.observedNeighborChange(observerState, world, observerPos, changedBlock, changedBlockPos);
    }

    @Override
    public boolean shouldCheckWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return block.shouldCheckWeakPower(state, world, pos, side);
    }

    @Override
    public boolean getWeakChanges(IBlockAccess world, BlockPos pos) {
        return block.getWeakChanges(world, pos);
    }

    @Override
    public void setHarvestLevel(String toolClass, int level) {
        block.setHarvestLevel(toolClass, level);
    }

    @Override
    public void setHarvestLevel(String toolClass, int level, IBlockState state) {
        block.setHarvestLevel(toolClass, level, state);
    }

    @Override
    @Nullable
    public String getHarvestTool(IBlockState state) {
        return block.getHarvestTool(state);
    }

    @Override
    public int getHarvestLevel(IBlockState state) {
        return block.getHarvestLevel(state);
    }

    @Override
    public boolean isToolEffective(String type, IBlockState state) {
        return block.isToolEffective(type, state);
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return block.getExtendedState(state, world, pos);
    }

    @Override
    @Nullable
    public Boolean isEntityInsideMaterial(IBlockAccess world, BlockPos blockpos, IBlockState iblockstate, Entity entity, double yToTest, Material materialIn, boolean testingHead) {
        return block.isEntityInsideMaterial(world, blockpos, iblockstate, entity, yToTest, materialIn, testingHead);
    }

    @Override
    @Nullable
    public Boolean isAABBInsideMaterial(World world, BlockPos pos, AxisAlignedBB boundingBox, Material materialIn) {
        return block.isAABBInsideMaterial(world, pos, boundingBox, materialIn);
    }

    @Override
    @Nullable
    public Boolean isAABBInsideLiquid(World world, BlockPos pos, AxisAlignedBB boundingBox) {
        return block.isAABBInsideLiquid(world, pos, boundingBox);
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return block.canRenderInLayer(state, layer);
    }

    @Override
    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, @Nullable Entity entity) {
        return block.getSoundType(state, world, pos, entity);
    }

    @Override
    @Nullable
    public float[] getBeaconColorMultiplier(IBlockState state, World world, BlockPos pos, BlockPos beaconPos) {
        return block.getBeaconColorMultiplier(state, world, pos, beaconPos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getFogColor(World world, BlockPos pos, IBlockState state, Entity entity, Vec3d originalColor, float partialTicks) {
        return block.getFogColor(world, pos, state, entity, originalColor, partialTicks);
    }

    @Override
    public IBlockState getStateAtViewpoint(IBlockState state, IBlockAccess world, BlockPos pos, Vec3d viewpoint) {
        return block.getStateAtViewpoint(state, world, pos, viewpoint);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return block.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
    }

    @Override
    public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        return block.canBeConnectedTo(world, pos, facing);
    }

    @Override
    @Nullable
    public PathNodeType getAiPathNodeType(IBlockState state, IBlockAccess world, BlockPos pos) {
        return block.getAiPathNodeType(state, world, pos);
    }

    @Override
    public boolean doesSideBlockChestOpening(IBlockState blockState, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return block.doesSideBlockChestOpening(blockState, world, pos, side);
    }

    @Override
    public boolean isStickyBlock(IBlockState state) {
        return block.isStickyBlock(state);
    }

    public ProxyBlock(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
    }

    public ProxyBlock(Material materialIn) {
        super(materialIn);
    }
}
