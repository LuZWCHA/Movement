package com.nowandfuture.mod.core.prefab;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.*;

public class LocalWorldWrap extends World {

    private LocalWorld wrap;

    @Override
    public World init() {
        return getParentWorld();
    }

    public World getParentWorld(){
        return wrap.getParentWorld();
    }

    @Override
    @Nullable
    public TileEntity getTileEntity(BlockPos pos) {
        return wrap.getTileEntity(pos);
    }

    @Override
    public void setTileEntity(BlockPos pos, @Nullable TileEntity tileEntityIn) {
        wrap.addTitleEntity(pos, tileEntityIn);
    }

    @Override
    public void removeTileEntity(BlockPos pos) {
        wrap.removeTileEntity(pos);
    }

    @Override
    public void markTileEntityForRemoval(TileEntity tileEntityIn) {
        //getParentWorld().markTileEntityForRemoval(tileEntityIn);
    }

    @Override
    public boolean isBlockFullCube(BlockPos pos) {
        return wrap.isBlockFullCube(pos);
    }

    @Override
    public boolean isBlockNormalCube(BlockPos pos, boolean _default) {
        return wrap.isBlockNormalCube(pos, _default);
    }

    @Override
    public void calculateInitialSkylight() {
        //getParentWorld().calculateInitialSkylight();
    }

    @Override
    public void setAllowedSpawnTypes(boolean hostile, boolean peaceful) {
        getParentWorld().setAllowedSpawnTypes(hostile, peaceful);
    }

    @Override
    public void tick() {
        //getParentWorld().tick();
    }

    @Override
    public void calculateInitialWeather() {
        //getParentWorld().calculateInitialWeather();
    }

    @Override
    public void calculateInitialWeatherBody() {
        //getParentWorld().calculateInitialWeatherBody();
    }

    @Override
    public void updateWeather() {
        //getParentWorld().updateWeather();
    }

    @Override
    public void updateWeatherBody() {
        //getParentWorld().updateWeatherBody();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void playMoodSoundAndCheckLight(int p_147467_1_, int p_147467_2_, Chunk chunkIn) {
        //getParentWorld().playMoodSoundAndCheckLight(p_147467_1_, p_147467_2_, chunkIn);
    }

    @Override
    public void updateBlocks() {
    }

    @Override
    public void immediateBlockTick(BlockPos pos, IBlockState state, Random random) {
        //getParentWorld().immediateBlockTick(pos, state, random);
    }

    @Override
    public boolean canBlockFreezeWater(BlockPos pos) {
        return getParentWorld().canBlockFreezeWater(pos.add(wrap.getParentWorldPos()));
    }

    @Override
    public boolean canBlockFreezeNoWater(BlockPos pos) {
        return getParentWorld().canBlockFreezeNoWater(pos.add(wrap.getParentWorldPos()));
    }

    @Override
    public boolean canSnowAt(BlockPos pos, boolean checkLight) {
        return getParentWorld().canSnowAt(pos.add(wrap.getParentWorldPos()), checkLight);
    }

    @Override
    public boolean checkLight(BlockPos pos) {
        return getParentWorld().checkLight(pos.add(wrap.getParentWorldPos()));
    }

    @Override
    public boolean tickUpdates(boolean runAllPending) {
        //return getParentWorld().tickUpdates(runAllPending);
        return true;
    }

    @Override
    @Nullable
    public List<NextTickListEntry> getPendingBlockUpdates(Chunk chunkIn, boolean remove) {
        return null;
    }

    @Override
    @Nullable
    public List<NextTickListEntry> getPendingBlockUpdates(StructureBoundingBox structureBB, boolean remove) {
        return null;
    }

    @Override
    public List<Entity> getEntitiesInAABBexcluding(@Nullable Entity entityIn, AxisAlignedBB boundingBox, @Nullable Predicate<? super Entity> predicate) {
        return super.getEntitiesInAABBexcluding(entityIn,transFormLocalToActrual(boundingBox),predicate);
    }

    @Override
    public <T extends Entity> List<T> getEntities(Class<? extends T> entityType, Predicate<? super T> filter) {
        return Collections.emptyList();
    }

    @Override
    public <T extends Entity> List<T> getPlayers(Class<? extends T> playerType, Predicate<? super T> filter) {
        return getParentWorld().getPlayers(playerType, filter);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> classEntity, AxisAlignedBB bb) {
        return getParentWorld().getEntitiesWithinAABB(classEntity, transFormLocalToActrual(bb));
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> clazz, AxisAlignedBB aabb, @Nullable Predicate<? super T> filter) {
        return getParentWorld().getEntitiesWithinAABB(clazz, transFormLocalToActrual(aabb), filter);
    }

    @Override
    @Nullable
    public <T extends Entity> T findNearestEntityWithinAABB(Class<? extends T> entityType, AxisAlignedBB aabb, T closestTo) {
        return getParentWorld().findNearestEntityWithinAABB(entityType, transFormLocalToActrual(aabb), closestTo);
    }

    @Override
    @Nullable
    public Entity getEntityByID(int id) {
        return getParentWorld().getEntityByID(id);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public List<Entity> getLoadedEntityList() {
        return getParentWorld().getLoadedEntityList();
    }

    @Override
    public void markChunkDirty(BlockPos pos, TileEntity unusedTileEntity) {
        //getParentWorld().markChunkDirty(pos, unusedTileEntity);
    }

    @Override
    public int countEntities(Class<?> entityType) {
        return getParentWorld().countEntities(entityType);
    }

    @Override
    public void loadEntities(Collection<Entity> entityCollection) {
        //getParentWorld().loadEntities(entityCollection);
    }

    @Override
    public void unloadEntities(Collection<Entity> entityCollection) {
//        getParentWorld().unloadEntities(entityCollection);
    }

    @Override
    public boolean mayPlace(Block blockIn, BlockPos pos, boolean skipCollisionCheck, EnumFacing sidePlacedOn, @Nullable Entity placer) {
        return getParentWorld().mayPlace(blockIn, pos, skipCollisionCheck, sidePlacedOn, placer);
    }

    @Override
    public int getSeaLevel() {
        return getParentWorld().getSeaLevel();
    }

    @Override
    public void setSeaLevel(int seaLevelIn) {
        getParentWorld().setSeaLevel(seaLevelIn);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return wrap.getCombinedLight(pos, lightValue);
    }

    @Override
    public float getLightBrightness(BlockPos pos) {
        return getParentWorld().getLightBrightness(pos.add(wrap.getParentWorldPos()));
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return wrap.getBlockState(pos);
    }

    @Override
    public boolean isDaytime() {
        return getParentWorld().isDaytime();
    }

    @Override
    @Nullable
    public RayTraceResult rayTraceBlocks(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
        return null;
    }

    @Override
    public void playSound(@Nullable EntityPlayer player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
        //getParentWorld().playSound(player, pos, soundIn, category, volume, pitch);
    }

    @Override
    public void playSound(@Nullable EntityPlayer player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
        //getParentWorld().playSound(player, x, y, z, soundIn, category, volume, pitch);
    }

    @Override
    public void playSound(double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay) {
        //getParentWorld().playSound(x, y, z, soundIn, category, volume, pitch, distanceDelay);
    }

    @Override
    public void playRecord(BlockPos blockPositionIn, @Nullable SoundEvent soundEventIn) {
        //getParentWorld().playRecord(blockPositionIn, soundEventIn);
    }

    @Override
    public void spawnParticle(EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        //getParentWorld().spawnParticle(particleType, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
    }

    @Override
    public void spawnAlwaysVisibleParticle(int p_190523_1_, double p_190523_2_, double p_190523_4_, double p_190523_6_, double p_190523_8_, double p_190523_10_, double p_190523_12_, int... p_190523_14_) {
        //getParentWorld().spawnAlwaysVisibleParticle(p_190523_1_, p_190523_2_, p_190523_4_, p_190523_6_, p_190523_8_, p_190523_10_, p_190523_12_, p_190523_14_);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void spawnParticle(EnumParticleTypes particleType, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        //getParentWorld().spawnParticle(particleType, ignoreRange, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
    }

    @Override
    public boolean addWeatherEffect(Entity entityIn) {
        return getParentWorld().addWeatherEffect(entityIn);
    }

    @Override
    public boolean spawnEntity(Entity entityIn) {
        return getParentWorld().spawnEntity(entityIn);
    }

    @Override
    public void onEntityAdded(Entity entityIn) {
        getParentWorld().onEntityAdded(entityIn);
    }

    @Override
    public void onEntityRemoved(Entity entityIn) {
        getParentWorld().onEntityRemoved(entityIn);
    }

    @Override
    public void removeEntity(Entity entityIn) {
        getParentWorld().removeEntity(entityIn);
    }

    @Override
    public void removeEntityDangerously(Entity entityIn) {
        getParentWorld().removeEntityDangerously(entityIn);
    }

    @Override
    public List<AxisAlignedBB> getCollisionBoxes(@Nullable Entity entityIn, AxisAlignedBB aabb) {
        return getParentWorld().getCollisionBoxes(entityIn, transFormLocalToActrual(aabb));
    }

    @Override
    public boolean isInsideWorldBorder(Entity p_191503_1_) {
        return getParentWorld().isInsideWorldBorder(p_191503_1_);
    }

    @Override
    public boolean collidesWithAnyBlock(AxisAlignedBB bbox) {
        return false;
    }

    @Override
    public int calculateSkylightSubtracted(float partialTicks) {
        return getParentWorld().calculateSkylightSubtracted(partialTicks);
    }

    @Override
    public float getSunBrightnessFactor(float partialTicks) {
        return getParentWorld().getSunBrightnessFactor(partialTicks);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getSunBrightnessBody(float partialTicks) {
        return getParentWorld().getSunBrightnessBody(partialTicks);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getSkyColorBody(Entity entityIn, float partialTicks) {
        return getParentWorld().getSkyColorBody(entityIn, partialTicks);
    }

    @Override
    public float getCelestialAngle(float partialTicks) {
        return getParentWorld().getCelestialAngle(partialTicks);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getMoonPhase() {
        return getParentWorld().getMoonPhase();
    }

    @Override
    public float getCurrentMoonPhaseFactor() {
        return getParentWorld().getCurrentMoonPhaseFactor();
    }

    @Override
    public float getCurrentMoonPhaseFactorBody() {
        return getParentWorld().getCurrentMoonPhaseFactorBody();
    }

    @Override
    public float getCelestialAngleRadians(float partialTicks) {
        return getParentWorld().getCelestialAngleRadians(partialTicks);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getCloudColorBody(float partialTicks) {
        return getParentWorld().getCloudColorBody(partialTicks);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getFogColor(float partialTicks) {
        return getParentWorld().getFogColor(partialTicks);
    }

    @Override
    public BlockPos getPrecipitationHeight(BlockPos pos) {
        return getParentWorld().getPrecipitationHeight(pos.add(wrap.getParentWorldPos()));
    }

    @Override
    public BlockPos getTopSolidOrLiquidBlock(BlockPos pos) {
        return getParentWorld().getTopSolidOrLiquidBlock(pos.add(wrap.getParentWorldPos()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getStarBrightness(float partialTicks) {
        return getParentWorld().getStarBrightness(partialTicks);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getStarBrightnessBody(float partialTicks) {
        return getParentWorld().getStarBrightnessBody(partialTicks);
    }

    @Override
    public boolean isUpdateScheduled(BlockPos pos, Block blk) {
        return getParentWorld().isUpdateScheduled(pos, blk);
    }

    @Override
    public void scheduleUpdate(BlockPos pos, Block blockIn, int delay) {
        //getParentWorld().scheduleUpdate(pos, blockIn, delay);
    }

    @Override
    public void updateBlockTick(BlockPos pos, Block blockIn, int delay, int priority) {
        //getParentWorld().updateBlockTick(pos, blockIn, delay, priority);
    }

    @Override
    public void scheduleBlockUpdate(BlockPos pos, Block blockIn, int delay, int priority) {
        //getParentWorld().scheduleBlockUpdate(pos, blockIn, delay, priority);
    }

    @Override
    public void updateEntities() {
        //getParentWorld().updateEntities();
    }

    @Override
    public void tickPlayers() {
        //getParentWorld().tickPlayers();
        //Minecraft.getMinecraft().world.getBlockState()
    }

    // TODO: 2019/6/9
    @Override
    public boolean isFlammableWithin(AxisAlignedBB bb) {
        return getParentWorld().isFlammableWithin(bb);
    }

    // TODO: 2019/6/9
    @Override
    public boolean handleMaterialAcceleration(AxisAlignedBB bb, Material materialIn, Entity entityIn) {
        return getParentWorld().handleMaterialAcceleration(bb, materialIn, entityIn);
    }

    // TODO: 2019/6/9
    @Override
    public boolean isMaterialInBB(AxisAlignedBB bb, Material materialIn) {
        return getParentWorld().isMaterialInBB(bb, materialIn);
    }

    // TODO: 2019/6/9
    @Override
    public float getBlockDensity(Vec3d vec, AxisAlignedBB bb) {
        return getParentWorld().getBlockDensity(vec, bb);
    }

    @Override
    public boolean extinguishFire(@Nullable EntityPlayer player, BlockPos pos, EnumFacing side) {
        return getParentWorld().extinguishFire(player, pos, side);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getDebugLoadedEntities() {
        return getParentWorld().getDebugLoadedEntities();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getProviderName() {
        return getParentWorld().getProviderName();
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return wrap.isAirBlock(pos);
    }

    @Override
    public boolean isBlockLoaded(BlockPos pos) {
        return getParentWorld().isBlockLoaded(pos);
    }

    @Override
    public boolean isBlockLoaded(BlockPos pos, boolean allowEmpty) {
        return getParentWorld().isBlockLoaded(pos, allowEmpty);
    }

    @Override
    public boolean isAreaLoaded(BlockPos center, int radius) {
        return getParentWorld().isAreaLoaded(center, radius);
    }

    @Override
    public boolean isAreaLoaded(BlockPos center, int radius, boolean allowEmpty) {
        return getParentWorld().isAreaLoaded(center, radius, allowEmpty);
    }

    @Override
    public boolean isAreaLoaded(BlockPos from, BlockPos to) {
        return getParentWorld().isAreaLoaded(from, to);
    }

    @Override
    public boolean isAreaLoaded(BlockPos from, BlockPos to, boolean allowEmpty) {
        return getParentWorld().isAreaLoaded(from, to, allowEmpty);
    }

    @Override
    public boolean isAreaLoaded(StructureBoundingBox box) {
        return getParentWorld().isAreaLoaded(box);
    }

    @Override
    public boolean isAreaLoaded(StructureBoundingBox box, boolean allowEmpty) {
        return getParentWorld().isAreaLoaded(box, allowEmpty);
    }

    @Override
    public boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return true;
    }

    @Override
    public Chunk getChunkFromBlockCoords(BlockPos pos) {
        return getParentWorld().getChunkFromBlockCoords(pos);
    }

    @Override
    public Chunk getChunkFromChunkCoords(int chunkX, int chunkZ) {
        return getParentWorld().getChunkFromChunkCoords(chunkX, chunkZ);
    }

    @Override
    public boolean isChunkGeneratedAt(int x, int z) {
        return getParentWorld().isChunkGeneratedAt(x, z);
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        return getParentWorld().setBlockState(pos, newState, flags);
    }

    @Override
    public void markAndNotifyBlock(BlockPos pos, @Nullable Chunk chunk, IBlockState iblockstate, IBlockState newState, int flags) {
        getParentWorld().markAndNotifyBlock(pos, chunk, iblockstate, newState, flags);
    }

    @Override
    public boolean setBlockToAir(BlockPos pos) {
        return getParentWorld().setBlockToAir(pos);
    }

    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock) {
        return getParentWorld().destroyBlock(pos, dropBlock);
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState state) {
        return getParentWorld().setBlockState(pos, state);
    }

    @Override
    public void notifyBlockUpdate(BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
        getParentWorld().notifyBlockUpdate(pos, oldState, newState, flags);
    }

    @Override
    public void notifyNeighborsRespectDebug(BlockPos pos, Block blockType, boolean p_175722_3_) {
        getParentWorld().notifyNeighborsRespectDebug(pos, blockType, p_175722_3_);
    }

    @Override
    public void markBlocksDirtyVertical(int x, int z, int y1, int y2) {
        getParentWorld().markBlocksDirtyVertical(x, z, y1, y2);
    }

    @Override
    public void markBlockRangeForRenderUpdate(BlockPos rangeMin, BlockPos rangeMax) {
        getParentWorld().markBlockRangeForRenderUpdate(rangeMin, rangeMax);
    }

    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
        getParentWorld().markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2);
    }

    @Override
    public void updateObservingBlocksAt(BlockPos pos, Block blockType) {
        getParentWorld().updateObservingBlocksAt(pos, blockType);
    }

    @Override
    public void notifyNeighborsOfStateChange(BlockPos pos, Block blockType, boolean updateObservers) {
        getParentWorld().notifyNeighborsOfStateChange(pos, blockType, updateObservers);
    }

    @Override
    public void notifyNeighborsOfStateExcept(BlockPos pos, Block blockType, EnumFacing skipSide) {
        getParentWorld().notifyNeighborsOfStateExcept(pos, blockType, skipSide);
    }

    @Override
    public void neighborChanged(BlockPos pos, Block blockIn, BlockPos fromPos) {
        getParentWorld().neighborChanged(pos, blockIn, fromPos);
    }

    @Override
    public void observedNeighborChanged(BlockPos pos, Block p_190529_2_, BlockPos p_190529_3_) {
        getParentWorld().observedNeighborChanged(pos, p_190529_2_, p_190529_3_);
    }

    @Override
    public boolean isBlockTickPending(BlockPos pos, Block blockType) {
        return getParentWorld().isBlockTickPending(pos, blockType);
    }

    @Override
    public boolean canSeeSky(BlockPos pos) {
        return getParentWorld().canSeeSky(pos);
    }

    @Override
    public boolean canBlockSeeSky(BlockPos pos) {
        return getParentWorld().canBlockSeeSky(pos);
    }

    @Override
    public int getLight(BlockPos pos) {
        return getParentWorld().getLight(pos);
    }

    @Override
    public int getLightFromNeighbors(BlockPos pos) {
        return getParentWorld().getLightFromNeighbors(pos);
    }

    @Override
    public int getLight(BlockPos pos, boolean checkNeighbors) {
        return getParentWorld().getLight(pos, checkNeighbors);
    }

    @Override
    public BlockPos getHeight(BlockPos pos) {
        return getParentWorld().getHeight(pos);
    }

    @Override
    public int getHeight(int x, int z) {
        return getParentWorld().getHeight(x, z);
    }

    @Override
    @Deprecated
    public int getChunksLowestHorizon(int x, int z) {
        return getParentWorld().getChunksLowestHorizon(x, z);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getLightFromNeighborsFor(EnumSkyBlock type, BlockPos pos) {
        return getParentWorld().getLightFromNeighborsFor(type, pos);
    }

    @Override
    public int getLightFor(EnumSkyBlock type, BlockPos pos) {
        return getParentWorld().getLightFor(type, pos);
    }

    @Override
    public void setLightFor(EnumSkyBlock type, BlockPos pos, int lightValue) {
        getParentWorld().setLightFor(type, pos, lightValue);
    }

    @Override
    public void notifyLightSet(BlockPos pos) {
        getParentWorld().notifyLightSet(pos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Biome getBiome(BlockPos pos) {
        return wrap.getBiome(pos);
    }

    @Override
    public Biome getBiomeForCoordsBody(BlockPos pos) {
        return getParentWorld().getBiomeForCoordsBody(pos);
    }

    @Override
    public BiomeProvider getBiomeProvider() {
        return getParentWorld().getBiomeProvider();
    }

    @Override
    public IChunkProvider createChunkProvider() {
        return getParentWorld().getChunkProvider();
    }

    @Override
    public void initialize(WorldSettings settings) {
        getParentWorld().initialize(settings);
    }

    @Override
    @Nullable
    public MinecraftServer getMinecraftServer() {
        return getParentWorld().getMinecraftServer();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setInitialSpawnLocation() {
        getParentWorld().setInitialSpawnLocation();
    }

    @Override
    public IBlockState getGroundAboveSeaLevel(BlockPos pos) {
        return getParentWorld().getGroundAboveSeaLevel(pos);
    }

    @Override
    public boolean isValid(BlockPos pos) {
        return getParentWorld().isValid(pos);
    }

    @Override
    public boolean isOutsideBuildHeight(BlockPos pos) {
        return getParentWorld().isOutsideBuildHeight(pos);
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return wrap.getStrongPower(pos, direction);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public WorldType getWorldType() {
        return wrap.getWorldType();
    }

    @Override
    public int getStrongPower(BlockPos pos) {
        return getParentWorld().getStrongPower(pos);
    }

    @Override
    public boolean isSidePowered(BlockPos pos, EnumFacing side) {
        return getParentWorld().isSidePowered(pos, side);
    }

    @Override
    public int getRedstonePower(BlockPos pos, EnumFacing facing) {
        return getParentWorld().getRedstonePower(pos, facing);
    }

    @Override
    public boolean isBlockPowered(BlockPos pos) {
        return getParentWorld().isBlockPowered(pos);
    }

    @Override
    public int isBlockIndirectlyGettingPowered(BlockPos pos) {
        return getParentWorld().isBlockIndirectlyGettingPowered(pos);
    }

    @Override
    @Nullable
    public EntityPlayer getClosestPlayerToEntity(Entity entityIn, double distance) {
        return getParentWorld().getClosestPlayerToEntity(entityIn, distance);
    }

    @Override
    @Nullable
    public EntityPlayer getNearestPlayerNotCreative(Entity entityIn, double distance) {
        return getParentWorld().getNearestPlayerNotCreative(entityIn, distance);
    }

    @Override
    @Nullable
    public EntityPlayer getClosestPlayer(double posX, double posY, double posZ, double distance, boolean spectator) {
        return getParentWorld().getClosestPlayer(posX, posY, posZ, distance, spectator);
    }

    @Override
    @Nullable
    public EntityPlayer getClosestPlayer(double x, double y, double z, double p_190525_7_, Predicate<Entity> p_190525_9_) {
        return getParentWorld().getClosestPlayer(x, y, z, p_190525_7_, p_190525_9_);
    }

    @Override
    public boolean isAnyPlayerWithinRangeAt(double x, double y, double z, double range) {
        return getParentWorld().isAnyPlayerWithinRangeAt(x, y, z, range);
    }

    @Override
    @Nullable
    public EntityPlayer getNearestAttackablePlayer(Entity entityIn, double maxXZDistance, double maxYDistance) {
        return getParentWorld().getNearestAttackablePlayer(entityIn, maxXZDistance, maxYDistance);
    }

    @Override
    @Nullable
    public EntityPlayer getNearestAttackablePlayer(BlockPos pos, double maxXZDistance, double maxYDistance) {
        return getParentWorld().getNearestAttackablePlayer(pos, maxXZDistance, maxYDistance);
    }

    @Override
    @Nullable
    public EntityPlayer getNearestAttackablePlayer(double posX, double posY, double posZ, double maxXZDistance, double maxYDistance, @Nullable Function<EntityPlayer, Double> playerToDouble, @Nullable Predicate<EntityPlayer> p_184150_12_) {
        return getParentWorld().getNearestAttackablePlayer(posX, posY, posZ, maxXZDistance, maxYDistance, playerToDouble, p_184150_12_);
    }

    @Override
    @Nullable
    public EntityPlayer getPlayerEntityByName(String name) {
        return getParentWorld().getPlayerEntityByName(name);
    }

    @Override
    @Nullable
    public EntityPlayer getPlayerEntityByUUID(UUID uuid) {
        return getParentWorld().getPlayerEntityByUUID(uuid);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void sendQuittingDisconnectingPacket() {
        getParentWorld().sendQuittingDisconnectingPacket();
    }

    @Override
    public void checkSessionLock() throws MinecraftException {
        getParentWorld().checkSessionLock();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setTotalWorldTime(long worldTime) {
        getParentWorld().setTotalWorldTime(worldTime);
    }

    @Override
    public long getSeed() {
        return getParentWorld().getSeed();
    }

    @Override
    public long getTotalWorldTime() {
        return getParentWorld().getTotalWorldTime();
    }

    @Override
    public long getWorldTime() {
        return getParentWorld().getWorldTime();
    }

    @Override
    public void setWorldTime(long time) {
        getParentWorld().setWorldTime(time);
    }

    @Override
    public BlockPos getSpawnPoint() {
        return getParentWorld().getSpawnPoint();
    }

    @Override
    public void setSpawnPoint(BlockPos pos) {
        getParentWorld().setSpawnPoint(pos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void joinEntityInSurroundings(Entity entityIn) {
        getParentWorld().joinEntityInSurroundings(entityIn);
    }

    @Override
    public boolean isBlockModifiable(EntityPlayer player, BlockPos pos) {
        return getParentWorld().isBlockModifiable(player, pos);
    }

    @Override
    public boolean canMineBlockBody(EntityPlayer player, BlockPos pos) {
        return getParentWorld().canMineBlockBody(player, pos);
    }

    @Override
    public void setEntityState(Entity entityIn, byte state) {
        getParentWorld().setEntityState(entityIn, state);
    }

    @Override
    public IChunkProvider getChunkProvider() {
        return getParentWorld().getChunkProvider();
    }

    @Override
    public void addBlockEvent(BlockPos pos, Block blockIn, int eventID, int eventParam) {
        getBlockState(pos).onBlockEventReceived(this, pos, eventID, eventParam);
    }

    @Override
    public ISaveHandler getSaveHandler() {
        return getParentWorld().getSaveHandler();
    }

    @Override
    public WorldInfo getWorldInfo() {
        return getParentWorld().getWorldInfo();
    }

    @Override
    public GameRules getGameRules() {
        return getParentWorld().getGameRules();
    }

    @Override
    public void updateAllPlayersSleepingFlag() {
        getParentWorld().updateAllPlayersSleepingFlag();
    }

    @Override
    public float getThunderStrength(float delta) {
        return getParentWorld().getThunderStrength(delta);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setThunderStrength(float strength) {
        getParentWorld().setThunderStrength(strength);
    }

    @Override
    public float getRainStrength(float delta) {
        return getParentWorld().getRainStrength(delta);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setRainStrength(float strength) {
        getParentWorld().setRainStrength(strength);
    }

    @Override
    public boolean isThundering() {
        return getParentWorld().isThundering();
    }

    @Override
    public boolean isRaining() {
        return getParentWorld().isRaining();
    }

    @Override
    public boolean isRainingAt(BlockPos position) {
        return getParentWorld().isRainingAt(position.add(wrap.getParentWorldPos()));
    }

    @Override
    public boolean isBlockinHighHumidity(BlockPos pos) {
        return getParentWorld().isBlockinHighHumidity(pos.add(wrap.getParentWorldPos()));
    }

    @Override
    @Nullable
    public MapStorage getMapStorage() {
        return getParentWorld().getMapStorage();
    }

    @Override
    public void setData(String dataID, WorldSavedData worldSavedDataIn) {
        getParentWorld().setData(dataID, worldSavedDataIn);
    }

    @Override
    @Nullable
    public WorldSavedData loadData(Class<? extends WorldSavedData> clazz, String dataID) {
        return getParentWorld().loadData(clazz, dataID);
    }

    @Override
    public int getUniqueDataId(String key) {
        return getParentWorld().getUniqueDataId(key);
    }

    @Override
    public void playBroadcastSound(int id, BlockPos pos, int data) {
        //getParentWorld().playBroadcastSound(id, pos, data);
    }

    @Override
    public void playEvent(int type, BlockPos pos, int data) {
        //getParentWorld().playEvent(type, pos, data);
    }

    @Override
    public void playEvent(@Nullable EntityPlayer player, int type, BlockPos pos, int data) {
        //getParentWorld().playEvent(player, type, pos, data);
    }

    @Override
    public int getHeight() {
        return wrap.getHeight();
    }

    @Override
    public int getActualHeight() {
        return getHeight();
    }

    @Override
    public CrashReportCategory addWorldInfoToCrashReport(CrashReport report) {
        return getParentWorld().addWorldInfoToCrashReport(report);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getHorizon() {
        return getParentWorld().getHorizon();
    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
        //getParentWorld().sendBlockBreakProgress(breakerId, pos, progress);
    }

    @Override
    public Calendar getCurrentDate() {
        return getParentWorld().getCurrentDate();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void makeFireworks(double x, double y, double z, double motionX, double motionY, double motionZ, @Nullable NBTTagCompound compound) {
        //getParentWorld().makeFireworks(x, y, z, motionX, motionY, motionZ, compound);
    }

    @Override
    public Scoreboard getScoreboard() {
        return getParentWorld().getScoreboard();
    }

    @Override
    public void updateComparatorOutputLevel(BlockPos pos, Block blockIn) {
//        getParentWorld().updateComparatorOutputLevel(pos, blockIn);
    }

    @Override
    public DifficultyInstance getDifficultyForLocation(BlockPos pos) {
        return getParentWorld().getDifficultyForLocation(pos.add(wrap.getParentWorldPos()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getLastLightningBolt() {
        return getParentWorld().getLastLightningBolt();
    }

    @Override
    public VillageCollection getVillageCollection() {
        return getParentWorld().getVillageCollection();
    }

    // TODO: 2019/6/8
    @Override
    public WorldBorder getWorldBorder() {
        return getParentWorld().getWorldBorder();
    }

    @Override
    public boolean isSpawnChunk(int x, int z) {
        return getParentWorld().isSpawnChunk(wrap.getParentWorldPos().getX() + x, wrap.getParentWorldPos().getZ());
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side) {
        return getParentWorld().isSideSolid(pos, side);
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        return wrap.isSideSolid(pos, side, _default);
    }

//    @Override
//    public ImmutableSetMultimap<ChunkPos, ForgeChunkManager.Ticket> getPersistentChunks() {
//        return getParentWorld().getPersistentChunks();
//    }
//
//    @Override
//    public Iterator<Chunk> getPersistentChunkIterable(Iterator<Chunk> chunkIterator) {
//        return getParentWorld().getPersistentChunkIterable(chunkIterator);
//    }

    @Override
    public int getBlockLightOpacity(BlockPos pos) {
        return getParentWorld().getBlockLightOpacity(pos.add(wrap.getParentWorldPos()));
    }

    @Override
    public int countEntities(EnumCreatureType type, boolean forSpawnCount) {
        return getParentWorld().countEntities(type, forSpawnCount);
    }

    @Override
    @Deprecated
    public void markTileEntitiesInChunkForRemoval(Chunk chunk) {
        //getParentWorld().markTileEntitiesInChunkForRemoval(chunk);
    }

    @Override
    public void initCapabilities() {
        //getParentWorld().initCapabilities();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return getParentWorld().hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        return getParentWorld().getCapability(capability, facing);
    }

    @Override
    public MapStorage getPerWorldStorage() {
        return getParentWorld().getPerWorldStorage();
    }

    @Override
    public void sendPacketToServer(Packet<?> packetIn) {
        getParentWorld().sendPacketToServer(packetIn);
    }

    @Override
    public LootTableManager getLootTableManager() {
        return getParentWorld().getLootTableManager();
    }

    @Override
    @Nullable
    public BlockPos findNearestStructure(String p_190528_1_, BlockPos p_190528_2_, boolean p_190528_3_) {
        return getParentWorld().findNearestStructure(p_190528_1_, p_190528_2_, p_190528_3_);
    }


    protected LocalWorldWrap(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
    }


    public void wrap(LocalWorld access){
        this.wrap = access;
    }

    public AxisAlignedBB transFormLocalToActrual(AxisAlignedBB axisAlignedBB){
        return axisAlignedBB.offset(wrap.getParentWorldPos());
    }
}
