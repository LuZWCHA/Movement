package com.nowandfuture.mod.core.common;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSetMultimap;
import com.nowandfuture.mod.core.common.blocks.TransformedBlock;
import com.nowandfuture.mod.core.prefab.LocalChunkProvider;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.ChunkProviderClient;
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
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.*;
public class TransformedBlockWorld extends World {
    private World realWorld;

    @Override
    public World init() {
        return realWorld.init();
    }

    @Override
    public Biome getBiomeForCoordsBody(BlockPos pos) {
        return realWorld.getBiomeForCoordsBody(pos);
    }

    @Override
    public BiomeProvider getBiomeProvider() {
        return realWorld.getBiomeProvider();
    }

    @Override
    public void initialize(WorldSettings settings) {
        realWorld.initialize(settings);
    }

    @Override
    @Nullable
    public MinecraftServer getMinecraftServer() {
        return realWorld.getMinecraftServer();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setInitialSpawnLocation() {
        realWorld.setInitialSpawnLocation();
    }

    @Override
    public IBlockState getGroundAboveSeaLevel(BlockPos pos) {
        return realWorld.getGroundAboveSeaLevel(pos);
    }

    @Override
    public boolean isValid(BlockPos pos) {
        return realWorld.isValid(pos);
    }

    @Override
    public boolean isOutsideBuildHeight(BlockPos pos) {
        return realWorld.isOutsideBuildHeight(pos);
    }

    @Override
    public boolean isBlockLoaded(BlockPos pos) {
        return realWorld.isBlockLoaded(pos);
    }

    @Override
    public boolean isBlockLoaded(BlockPos pos, boolean allowEmpty) {
        return realWorld.isBlockLoaded(pos, allowEmpty);
    }

    @Override
    public boolean isAreaLoaded(BlockPos center, int radius) {
        return realWorld.isAreaLoaded(center, radius);
    }

    @Override
    public boolean isAreaLoaded(BlockPos center, int radius, boolean allowEmpty) {
        return realWorld.isAreaLoaded(center, radius, allowEmpty);
    }

    @Override
    public boolean isAreaLoaded(BlockPos from, BlockPos to) {
        return realWorld.isAreaLoaded(from, to);
    }

    @Override
    public boolean isAreaLoaded(BlockPos from, BlockPos to, boolean allowEmpty) {
        return realWorld.isAreaLoaded(from, to, allowEmpty);
    }

    @Override
    public boolean isAreaLoaded(StructureBoundingBox box) {
        return realWorld.isAreaLoaded(box);
    }

    @Override
    public boolean isAreaLoaded(StructureBoundingBox box, boolean allowEmpty) {
        return realWorld.isAreaLoaded(box, allowEmpty);
    }

    @Override
    public Chunk getChunk(BlockPos pos) {
        return realWorld.getChunk(pos);
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkZ) {
        return realWorld.getChunk(chunkX, chunkZ);
    }

    @Override
    public boolean isChunkGeneratedAt(int x, int z) {
        return realWorld.isChunkGeneratedAt(x, z);
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        return realWorld.setBlockState(pos, newState, flags);
    }

    @Override
    public void markAndNotifyBlock(BlockPos pos, @Nullable Chunk chunk, IBlockState iblockstate, IBlockState newState, int flags) {
        realWorld.markAndNotifyBlock(pos, chunk, iblockstate, newState, flags);
    }

    @Override
    public boolean setBlockToAir(BlockPos pos) {
        return realWorld.setBlockToAir(pos);
    }

    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock) {
        return realWorld.destroyBlock(pos, dropBlock);
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState state) {
        return realWorld.setBlockState(pos, state);
    }

    @Override
    public void notifyBlockUpdate(BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
        realWorld.notifyBlockUpdate(pos, oldState, newState, flags);
    }

    @Override
    public void notifyNeighborsRespectDebug(BlockPos pos, Block blockType, boolean p_175722_3_) {
        realWorld.notifyNeighborsRespectDebug(pos, blockType, p_175722_3_);
    }

    @Override
    public void markBlocksDirtyVertical(int x, int z, int y1, int y2) {
        realWorld.markBlocksDirtyVertical(x, z, y1, y2);
    }

    @Override
    public void markBlockRangeForRenderUpdate(BlockPos rangeMin, BlockPos rangeMax) {
        realWorld.markBlockRangeForRenderUpdate(rangeMin, rangeMax);
    }

    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
        realWorld.markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2);
    }

    @Override
    public void updateObservingBlocksAt(BlockPos pos, Block blockType) {
        realWorld.updateObservingBlocksAt(pos, blockType);
    }

    @Override
    public void notifyNeighborsOfStateChange(BlockPos pos, Block blockType, boolean updateObservers) {
        realWorld.notifyNeighborsOfStateChange(pos, blockType, updateObservers);
    }

    @Override
    public void notifyNeighborsOfStateExcept(BlockPos pos, Block blockType, EnumFacing skipSide) {
        realWorld.notifyNeighborsOfStateExcept(pos, blockType, skipSide);
    }

    @Override
    public void neighborChanged(BlockPos pos, Block blockIn, BlockPos fromPos) {
        realWorld.neighborChanged(pos, blockIn, fromPos);
    }

    @Override
    public void observedNeighborChanged(BlockPos pos, Block p_190529_2_, BlockPos p_190529_3_) {
        realWorld.observedNeighborChanged(pos, p_190529_2_, p_190529_3_);
    }

    @Override
    public boolean isBlockTickPending(BlockPos pos, Block blockType) {
        return realWorld.isBlockTickPending(pos, blockType);
    }

    @Override
    public boolean canSeeSky(BlockPos pos) {
        return realWorld.canSeeSky(pos);
    }

    @Override
    public boolean canBlockSeeSky(BlockPos pos) {
        return realWorld.canBlockSeeSky(pos);
    }

    @Override
    public int getLight(BlockPos pos) {
        return realWorld.getLight(pos);
    }

    @Override
    public int getLightFromNeighbors(BlockPos pos) {
        return realWorld.getLightFromNeighbors(pos);
    }

    @Override
    public int getLight(BlockPos pos, boolean checkNeighbors) {
        return realWorld.getLight(pos, checkNeighbors);
    }

    @Override
    public BlockPos getHeight(BlockPos pos) {
        return realWorld.getHeight(pos);
    }

    @Override
    public int getHeight(int x, int z) {
        return realWorld.getHeight(x, z);
    }

    @Override
    @Deprecated
    public int getChunksLowestHorizon(int x, int z) {
        return realWorld.getChunksLowestHorizon(x, z);
    }

    @Override
    public void notifyLightSet(BlockPos pos) {
        realWorld.notifyLightSet(pos);
    }

    @Override
    public float getLightBrightness(BlockPos pos) {
        return realWorld.getLightBrightness(pos);
    }

    @Override
    public boolean isDaytime() {
        return realWorld.isDaytime();
    }

    @Override
    @Nullable
    public RayTraceResult rayTraceBlocks(Vec3d start, Vec3d end) {
        return realWorld.rayTraceBlocks(start, end);
    }

    @Override
    @Nullable
    public RayTraceResult rayTraceBlocks(Vec3d start, Vec3d end, boolean stopOnLiquid) {
        return realWorld.rayTraceBlocks(start, end, stopOnLiquid);
    }

    @Override
    @Nullable
    public RayTraceResult rayTraceBlocks(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
        return realWorld.rayTraceBlocks(vec31, vec32, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
    }

    @Override
    public void playSound(@Nullable EntityPlayer player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
        realWorld.playSound(player, pos, soundIn, category, volume, pitch);
    }

    @Override
    public void playSound(@Nullable EntityPlayer player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
        realWorld.playSound(player, x, y, z, soundIn, category, volume, pitch);
    }

    @Override
    public void playSound(double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay) {
        realWorld.playSound(x, y, z, soundIn, category, volume, pitch, distanceDelay);
    }

    @Override
    public void playRecord(BlockPos blockPositionIn, @Nullable SoundEvent soundEventIn) {
        realWorld.playRecord(blockPositionIn, soundEventIn);
    }

    @Override
    public void spawnParticle(EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        realWorld.spawnParticle(particleType, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
    }

    @Override
    public void spawnAlwaysVisibleParticle(int p_190523_1_, double p_190523_2_, double p_190523_4_, double p_190523_6_, double p_190523_8_, double p_190523_10_, double p_190523_12_, int... p_190523_14_) {
        realWorld.spawnAlwaysVisibleParticle(p_190523_1_, p_190523_2_, p_190523_4_, p_190523_6_, p_190523_8_, p_190523_10_, p_190523_12_, p_190523_14_);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void spawnParticle(EnumParticleTypes particleType, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        realWorld.spawnParticle(particleType, ignoreRange, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
    }

    @Override
    public boolean addWeatherEffect(Entity entityIn) {
        return realWorld.addWeatherEffect(entityIn);
    }

    @Override
    public boolean spawnEntity(Entity entityIn) {
        return realWorld.spawnEntity(entityIn);
    }

    @Override
    public void onEntityAdded(Entity entityIn) {
        realWorld.onEntityAdded(entityIn);
    }

    @Override
    public void onEntityRemoved(Entity entityIn) {
        realWorld.onEntityRemoved(entityIn);
    }

    @Override
    public void removeEntity(Entity entityIn) {
        realWorld.removeEntity(entityIn);
    }

    @Override
    public void removeEntityDangerously(Entity entityIn) {
        realWorld.removeEntityDangerously(entityIn);
    }

    @Override
    public void addEventListener(IWorldEventListener listener) {
        realWorld.addEventListener(listener);
    }

    @Override
    public List<AxisAlignedBB> getCollisionBoxes(@Nullable Entity entityIn, AxisAlignedBB aabb) {
        return realWorld.getCollisionBoxes(entityIn, aabb);
    }

    @Override
    public void removeEventListener(IWorldEventListener listener) {
        realWorld.removeEventListener(listener);
    }

    @Override
    public boolean isInsideWorldBorder(Entity p_191503_1_) {
        return realWorld.isInsideWorldBorder(p_191503_1_);
    }

    @Override
    public boolean collidesWithAnyBlock(AxisAlignedBB bbox) {
        return realWorld.collidesWithAnyBlock(bbox);
    }

    @Override
    public int calculateSkylightSubtracted(float partialTicks) {
        return realWorld.calculateSkylightSubtracted(partialTicks);
    }

    @Override
    public float getSunBrightnessFactor(float partialTicks) {
        return realWorld.getSunBrightnessFactor(partialTicks);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getSunBrightness(float partialTicks) {
        return realWorld.getSunBrightness(partialTicks);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getSunBrightnessBody(float partialTicks) {
        return realWorld.getSunBrightnessBody(partialTicks);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getSkyColor(Entity entityIn, float partialTicks) {
        return realWorld.getSkyColor(entityIn, partialTicks);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getSkyColorBody(Entity entityIn, float partialTicks) {
        return realWorld.getSkyColorBody(entityIn, partialTicks);
    }

    @Override
    public float getCelestialAngle(float partialTicks) {
        return realWorld.getCelestialAngle(partialTicks);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getMoonPhase() {
        return realWorld.getMoonPhase();
    }

    @Override
    public float getCurrentMoonPhaseFactor() {
        return realWorld.getCurrentMoonPhaseFactor();
    }

    @Override
    public float getCurrentMoonPhaseFactorBody() {
        return realWorld.getCurrentMoonPhaseFactorBody();
    }

    @Override
    public float getCelestialAngleRadians(float partialTicks) {
        return realWorld.getCelestialAngleRadians(partialTicks);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getCloudColour(float partialTicks) {
        return realWorld.getCloudColour(partialTicks);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getCloudColorBody(float partialTicks) {
        return realWorld.getCloudColorBody(partialTicks);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getFogColor(float partialTicks) {
        return realWorld.getFogColor(partialTicks);
    }

    @Override
    public BlockPos getPrecipitationHeight(BlockPos pos) {
        return realWorld.getPrecipitationHeight(pos);
    }

    @Override
    public BlockPos getTopSolidOrLiquidBlock(BlockPos pos) {
        return realWorld.getTopSolidOrLiquidBlock(pos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getStarBrightness(float partialTicks) {
        return realWorld.getStarBrightness(partialTicks);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getStarBrightnessBody(float partialTicks) {
        return realWorld.getStarBrightnessBody(partialTicks);
    }

    @Override
    public boolean isUpdateScheduled(BlockPos pos, Block blk) {
        return realWorld.isUpdateScheduled(pos, blk);
    }

    @Override
    public void scheduleUpdate(BlockPos pos, Block blockIn, int delay) {
        realWorld.scheduleUpdate(pos, blockIn, delay);
    }

    @Override
    public void updateBlockTick(BlockPos pos, Block blockIn, int delay, int priority) {
        realWorld.updateBlockTick(pos, blockIn, delay, priority);
    }

    @Override
    public void scheduleBlockUpdate(BlockPos pos, Block blockIn, int delay, int priority) {
        realWorld.scheduleBlockUpdate(pos, blockIn, delay, priority);
    }

    @Override
    public void updateEntities() {
        realWorld.updateEntities();
    }

    @Override
    public boolean addTileEntity(TileEntity tile) {
        return realWorld.addTileEntity(tile);
    }

    @Override
    public void addTileEntities(Collection<TileEntity> tileEntityCollection) {
        realWorld.addTileEntities(tileEntityCollection);
    }

    @Override
    public void updateEntity(Entity ent) {
        realWorld.updateEntity(ent);
    }

    @Override
    public void updateEntityWithOptionalForce(Entity entityIn, boolean forceUpdate) {
        realWorld.updateEntityWithOptionalForce(entityIn, forceUpdate);
    }

    @Override
    public boolean checkNoEntityCollision(AxisAlignedBB bb) {
        return realWorld.checkNoEntityCollision(bb);
    }

    @Override
    public boolean checkNoEntityCollision(AxisAlignedBB bb, @Nullable Entity entityIn) {
        return realWorld.checkNoEntityCollision(bb, entityIn);
    }

    @Override
    public boolean checkBlockCollision(AxisAlignedBB bb) {
        return realWorld.checkBlockCollision(bb);
    }

    @Override
    public boolean containsAnyLiquid(AxisAlignedBB bb) {
        return realWorld.containsAnyLiquid(bb);
    }

    @Override
    public boolean isFlammableWithin(AxisAlignedBB bb) {
        return realWorld.isFlammableWithin(bb);
    }

    @Override
    public boolean handleMaterialAcceleration(AxisAlignedBB bb, Material materialIn, Entity entityIn) {
        return realWorld.handleMaterialAcceleration(bb, materialIn, entityIn);
    }

    @Override
    public boolean isMaterialInBB(AxisAlignedBB bb, Material materialIn) {
        return realWorld.isMaterialInBB(bb, materialIn);
    }

    @Override
    public Explosion createExplosion(@Nullable Entity entityIn, double x, double y, double z, float strength, boolean isSmoking) {
        return realWorld.createExplosion(entityIn, x, y, z, strength, isSmoking);
    }

    @Override
    public Explosion newExplosion(@Nullable Entity entityIn, double x, double y, double z, float strength, boolean isFlaming, boolean isSmoking) {
        return realWorld.newExplosion(entityIn, x, y, z, strength, isFlaming, isSmoking);
    }

    @Override
    public float getBlockDensity(Vec3d vec, AxisAlignedBB bb) {
        return realWorld.getBlockDensity(vec, bb);
    }

    @Override
    public boolean extinguishFire(@Nullable EntityPlayer player, BlockPos pos, EnumFacing side) {
        return realWorld.extinguishFire(player, pos, side);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getDebugLoadedEntities() {
        return realWorld.getDebugLoadedEntities();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getProviderName() {
        return realWorld.getProviderName();
    }

    @Override
    public void setTileEntity(BlockPos pos, @Nullable TileEntity tileEntityIn) {
        realWorld.setTileEntity(pos, tileEntityIn);
    }

    @Override
    public void removeTileEntity(BlockPos pos) {
        realWorld.removeTileEntity(pos);
    }

    @Override
    public void markTileEntityForRemoval(TileEntity tileEntityIn) {
        realWorld.markTileEntityForRemoval(tileEntityIn);
    }

    @Override
    public boolean isBlockFullCube(BlockPos pos) {
        return realWorld.isBlockFullCube(pos);
    }

    @Override
    public boolean isBlockNormalCube(BlockPos pos, boolean _default) {
        return realWorld.isBlockNormalCube(pos, _default);
    }

    @Override
    public void calculateInitialSkylight() {
        realWorld.calculateInitialSkylight();
    }

    @Override
    public void setAllowedSpawnTypes(boolean hostile, boolean peaceful) {
        realWorld.setAllowedSpawnTypes(hostile, peaceful);
    }

    @Override
    public void tick() {
        realWorld.tick();
    }

    @Override
    public void calculateInitialWeatherBody() {
        realWorld.calculateInitialWeatherBody();
    }

    @Override
    public void updateWeatherBody() {
        realWorld.updateWeatherBody();
    }

    @Override
    public void immediateBlockTick(BlockPos pos, IBlockState state, Random random) {
        realWorld.immediateBlockTick(pos, state, random);
    }

    @Override
    public boolean canBlockFreezeWater(BlockPos pos) {
        return realWorld.canBlockFreezeWater(pos);
    }

    @Override
    public boolean canBlockFreezeNoWater(BlockPos pos) {
        return realWorld.canBlockFreezeNoWater(pos);
    }

    @Override
    public boolean canBlockFreeze(BlockPos pos, boolean noWaterAdj) {
        return realWorld.canBlockFreeze(pos, noWaterAdj);
    }

    @Override
    public boolean canBlockFreezeBody(BlockPos pos, boolean noWaterAdj) {
        return realWorld.canBlockFreezeBody(pos, noWaterAdj);
    }

    @Override
    public boolean canSnowAt(BlockPos pos, boolean checkLight) {
        return realWorld.canSnowAt(pos, checkLight);
    }

    @Override
    public boolean canSnowAtBody(BlockPos pos, boolean checkLight) {
        return realWorld.canSnowAtBody(pos, checkLight);
    }

    @Override
    public boolean checkLight(BlockPos pos) {
        return realWorld.checkLight(pos);
    }

    @Override
    public boolean checkLightFor(EnumSkyBlock lightType, BlockPos pos) {
        return realWorld.checkLightFor(lightType, pos);
    }

    @Override
    public boolean tickUpdates(boolean runAllPending) {
        return realWorld.tickUpdates(runAllPending);
    }

    @Override
    @Nullable
    public List<NextTickListEntry> getPendingBlockUpdates(Chunk chunkIn, boolean remove) {
        return realWorld.getPendingBlockUpdates(chunkIn, remove);
    }

    @Override
    @Nullable
    public List<NextTickListEntry> getPendingBlockUpdates(StructureBoundingBox structureBB, boolean remove) {
        return realWorld.getPendingBlockUpdates(structureBB, remove);
    }

    @Override
    public List<Entity> getEntitiesWithinAABBExcludingEntity(@Nullable Entity entityIn, AxisAlignedBB bb) {
        return realWorld.getEntitiesWithinAABBExcludingEntity(entityIn, bb);
    }

    @Override
    public List<Entity> getEntitiesInAABBexcluding(@Nullable Entity entityIn, AxisAlignedBB boundingBox, @Nullable Predicate<? super Entity> predicate) {
        return realWorld.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate);
    }

    @Override
    public <T extends Entity> List<T> getEntities(Class<? extends T> entityType, Predicate<? super T> filter) {
        return realWorld.getEntities(entityType, filter);
    }

    @Override
    public <T extends Entity> List<T> getPlayers(Class<? extends T> playerType, Predicate<? super T> filter) {
        return realWorld.getPlayers(playerType, filter);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> classEntity, AxisAlignedBB bb) {
        return realWorld.getEntitiesWithinAABB(classEntity, bb);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> clazz, AxisAlignedBB aabb, @Nullable Predicate<? super T> filter) {
        return realWorld.getEntitiesWithinAABB(clazz, aabb, filter);
    }

    @Override
    @Nullable
    public <T extends Entity> T findNearestEntityWithinAABB(Class<? extends T> entityType, AxisAlignedBB aabb, T closestTo) {
        return realWorld.findNearestEntityWithinAABB(entityType, aabb, closestTo);
    }

    @Override
    @Nullable
    public Entity getEntityByID(int id) {
        return realWorld.getEntityByID(id);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public List<Entity> getLoadedEntityList() {
        return realWorld.getLoadedEntityList();
    }

    @Override
    public int countEntities(Class<?> entityType) {
        return realWorld.countEntities(entityType);
    }

    @Override
    public void loadEntities(Collection<Entity> entityCollection) {
        realWorld.loadEntities(entityCollection);
    }

    @Override
    public int getSeaLevel() {
        return realWorld.getSeaLevel();
    }

    @Override
    public void setSeaLevel(int seaLevelIn) {
        realWorld.setSeaLevel(seaLevelIn);
    }

    @Override
    @Nullable
    public EntityPlayer getClosestPlayerToEntity(Entity entityIn, double distance) {
        return realWorld.getClosestPlayerToEntity(entityIn, distance);
    }

    @Override
    @Nullable
    public EntityPlayer getNearestPlayerNotCreative(Entity entityIn, double distance) {
        return realWorld.getNearestPlayerNotCreative(entityIn, distance);
    }

    @Override
    @Nullable
    public EntityPlayer getClosestPlayer(double posX, double posY, double posZ, double distance, boolean spectator) {
        return realWorld.getClosestPlayer(posX, posY, posZ, distance, spectator);
    }

    @Override
    @Nullable
    public EntityPlayer getClosestPlayer(double x, double y, double z, double p_190525_7_, Predicate<Entity> p_190525_9_) {
        return realWorld.getClosestPlayer(x, y, z, p_190525_7_, p_190525_9_);
    }

    @Override
    public boolean isAnyPlayerWithinRangeAt(double x, double y, double z, double range) {
        return realWorld.isAnyPlayerWithinRangeAt(x, y, z, range);
    }

    @Override
    @Nullable
    public EntityPlayer getNearestAttackablePlayer(Entity entityIn, double maxXZDistance, double maxYDistance) {
        return realWorld.getNearestAttackablePlayer(entityIn, maxXZDistance, maxYDistance);
    }

    @Override
    @Nullable
    public EntityPlayer getNearestAttackablePlayer(BlockPos pos, double maxXZDistance, double maxYDistance) {
        return realWorld.getNearestAttackablePlayer(pos, maxXZDistance, maxYDistance);
    }

    @Override
    @Nullable
    public EntityPlayer getNearestAttackablePlayer(double posX, double posY, double posZ, double maxXZDistance, double maxYDistance, @Nullable Function<EntityPlayer, Double> playerToDouble, @Nullable Predicate<EntityPlayer> p_184150_12_) {
        return realWorld.getNearestAttackablePlayer(posX, posY, posZ, maxXZDistance, maxYDistance, playerToDouble, p_184150_12_);
    }

    @Override
    @Nullable
    public EntityPlayer getPlayerEntityByName(String name) {
        return realWorld.getPlayerEntityByName(name);
    }

    @Override
    @Nullable
    public EntityPlayer getPlayerEntityByUUID(UUID uuid) {
        return realWorld.getPlayerEntityByUUID(uuid);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void sendQuittingDisconnectingPacket() {
        realWorld.sendQuittingDisconnectingPacket();
    }

    @Override
    public void checkSessionLock() throws MinecraftException {
        realWorld.checkSessionLock();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setTotalWorldTime(long worldTime) {
        realWorld.setTotalWorldTime(worldTime);
    }

    @Override
    public long getSeed() {
        return realWorld.getSeed();
    }

    @Override
    public long getTotalWorldTime() {
        return realWorld.getTotalWorldTime();
    }

    @Override
    public long getWorldTime() {
        return realWorld.getWorldTime();
    }

    @Override
    public void setWorldTime(long time) {
        realWorld.setWorldTime(time);
    }

    @Override
    public BlockPos getSpawnPoint() {
        return realWorld.getSpawnPoint();
    }

    @Override
    public void setSpawnPoint(BlockPos pos) {
        realWorld.setSpawnPoint(pos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void joinEntityInSurroundings(Entity entityIn) {
        realWorld.joinEntityInSurroundings(entityIn);
    }

    @Override
    public boolean isBlockModifiable(EntityPlayer player, BlockPos pos) {
        return realWorld.isBlockModifiable(player, pos);
    }

    @Override
    public boolean canMineBlockBody(EntityPlayer player, BlockPos pos) {
        return realWorld.canMineBlockBody(player, pos);
    }

    @Override
    public void setEntityState(Entity entityIn, byte state) {
        realWorld.setEntityState(entityIn, state);
    }

    @Override
    public IChunkProvider getChunkProvider() {
        return realWorld.getChunkProvider();
    }

    @Override
    public ISaveHandler getSaveHandler() {
        return realWorld.getSaveHandler();
    }

    @Override
    public WorldInfo getWorldInfo() {
        return realWorld.getWorldInfo();
    }

    @Override
    public GameRules getGameRules() {
        return realWorld.getGameRules();
    }

    @Override
    public void updateAllPlayersSleepingFlag() {
        realWorld.updateAllPlayersSleepingFlag();
    }

    @Override
    public float getThunderStrength(float delta) {
        return realWorld.getThunderStrength(delta);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setThunderStrength(float strength) {
        realWorld.setThunderStrength(strength);
    }

    @Override
    public float getRainStrength(float delta) {
        return realWorld.getRainStrength(delta);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setRainStrength(float strength) {
        realWorld.setRainStrength(strength);
    }

    @Override
    public boolean isThundering() {
        return realWorld.isThundering();
    }

    @Override
    public boolean isRaining() {
        return realWorld.isRaining();
    }

    @Override
    @Nullable
    public MapStorage getMapStorage() {
        return realWorld.getMapStorage();
    }

    @Override
    public void setData(String dataID, WorldSavedData worldSavedDataIn) {
        realWorld.setData(dataID, worldSavedDataIn);
    }

    @Override
    @Nullable
    public WorldSavedData loadData(Class<? extends WorldSavedData> clazz, String dataID) {
        return realWorld.loadData(clazz, dataID);
    }

    @Override
    public int getUniqueDataId(String key) {
        return realWorld.getUniqueDataId(key);
    }

    @Override
    public void playBroadcastSound(int id, BlockPos pos, int data) {
        realWorld.playBroadcastSound(id, pos, data);
    }

    @Override
    public void playEvent(int type, BlockPos pos, int data) {
        realWorld.playEvent(type, pos, data);
    }

    @Override
    public void playEvent(@Nullable EntityPlayer player, int type, BlockPos pos, int data) {
        realWorld.playEvent(player, type, pos, data);
    }

    @Override
    public int getHeight() {
        return realWorld.getHeight();
    }

    @Override
    public int getActualHeight() {
        return realWorld.getActualHeight();
    }

    @Override
    public Random setRandomSeed(int p_72843_1_, int p_72843_2_, int p_72843_3_) {
        return realWorld.setRandomSeed(p_72843_1_, p_72843_2_, p_72843_3_);
    }

    @Override
    public CrashReportCategory addWorldInfoToCrashReport(CrashReport report) {
        return realWorld.addWorldInfoToCrashReport(report);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getHorizon() {
        return realWorld.getHorizon();
    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
        realWorld.sendBlockBreakProgress(breakerId, pos, progress);
    }

    @Override
    public Calendar getCurrentDate() {
        return realWorld.getCurrentDate();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void makeFireworks(double x, double y, double z, double motionX, double motionY, double motionZ, @Nullable NBTTagCompound compound) {
        realWorld.makeFireworks(x, y, z, motionX, motionY, motionZ, compound);
    }

    @Override
    public Scoreboard getScoreboard() {
        return realWorld.getScoreboard();
    }

    @Override
    public DifficultyInstance getDifficultyForLocation(BlockPos pos) {
        return realWorld.getDifficultyForLocation(pos);
    }

    @Override
    public EnumDifficulty getDifficulty() {
        return realWorld.getDifficulty();
    }

    @Override
    public int getSkylightSubtracted() {
        return realWorld.getSkylightSubtracted();
    }

    @Override
    public void setSkylightSubtracted(int newSkylightSubtracted) {
        realWorld.setSkylightSubtracted(newSkylightSubtracted);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getLastLightningBolt() {
        return realWorld.getLastLightningBolt();
    }

    @Override
    public void setLastLightningBolt(int lastLightningBoltIn) {
        realWorld.setLastLightningBolt(lastLightningBoltIn);
    }

    @Override
    public VillageCollection getVillageCollection() {
        return realWorld.getVillageCollection();
    }

    @Override
    public WorldBorder getWorldBorder() {
        return realWorld.getWorldBorder();
    }

    @Override
    public boolean isSpawnChunk(int x, int z) {
        return realWorld.isSpawnChunk(x, z);
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side) {
        return realWorld.isSideSolid(pos, side);
    }

    @Override
    public ImmutableSetMultimap<ChunkPos, ForgeChunkManager.Ticket> getPersistentChunks() {
        return realWorld.getPersistentChunks();
    }

    @Override
    public Iterator<Chunk> getPersistentChunkIterable(Iterator<Chunk> chunkIterator) {
        return realWorld.getPersistentChunkIterable(chunkIterator);
    }

    @Override
    public int getBlockLightOpacity(BlockPos pos) {
        if(pos.equals(this.pos))
            return blockWrapper.blockState.getLightOpacity(this,pos);
        return realWorld.getBlockLightOpacity(pos);
    }

    @Override
    public int countEntities(EnumCreatureType type, boolean forSpawnCount) {
        return realWorld.countEntities(type, forSpawnCount);
    }

    @Override
    @Deprecated
    public void markTileEntitiesInChunkForRemoval(Chunk chunk) {
        realWorld.markTileEntitiesInChunkForRemoval(chunk);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return realWorld.hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        return realWorld.getCapability(capability, facing);
    }

    @Override
    public MapStorage getPerWorldStorage() {
        return realWorld.getPerWorldStorage();
    }

    @Override
    public void sendPacketToServer(Packet<?> packetIn) {
        realWorld.sendPacketToServer(packetIn);
    }

    @Override
    public LootTableManager getLootTableManager() {
        return realWorld.getLootTableManager();
    }

    @Override
    @Nullable
    public BlockPos findNearestStructure(String p_190528_1_, BlockPos p_190528_2_, boolean p_190528_3_) {
        return realWorld.findNearestStructure(p_190528_1_, p_190528_2_, p_190528_3_);
    }

    private TransformedBlock.BlockWrapper blockWrapper;

    private BlockPos pos;

    public TransformedBlockWorld(World realWorld, TransformedBlock.BlockWrapper blockWrapper, BlockPos pos) {
        super(realWorld.getSaveHandler(), realWorld.getWorldInfo(), realWorld.provider, realWorld.profiler, realWorld.isRemote);
        this.realWorld = realWorld;
        this.blockWrapper = blockWrapper;
        this.pos = pos;
    }

    public void setBlockWrapper(TransformedBlock.BlockWrapper blockWrapper) {
        this.blockWrapper = blockWrapper;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public void setRealWorld(World realWorld) {
        this.realWorld = realWorld;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        if(pos.equals(this.pos))
            return blockWrapper.tileEntity;
        return realWorld.getTileEntity(pos);
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        if(pos.equals(this.pos))
            return blockWrapper.blockState;
        return realWorld.getBlockState(pos);
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return realWorld.isAirBlock(pos);
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return allowEmpty || !this.getChunkProvider().provideChunk(x, z).isEmpty();
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return realWorld.getBiome(pos);
    }

    @Override
    protected IChunkProvider createChunkProvider() {
       return realWorld.getChunkProvider();
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return realWorld.getStrongPower(pos,direction);
    }

    @Override
    public WorldType getWorldType() {
        return realWorld.getWorldType();
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        if(pos.equals(this.pos))
            return blockWrapper.blockState.getBlock().isSideSolid(blockWrapper.blockState,this,pos,side);
        return realWorld.isSideSolid(pos, side, _default);
    }
}
