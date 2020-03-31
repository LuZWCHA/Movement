package com.nowandfuture.mod.core.prefab;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.client.renders.ModuleRenderManager;
import com.nowandfuture.mod.utils.ByteZip;
import joptsimple.internal.Strings;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class AbstractPrefab implements ITickable {
    private static final String NBT_DECOMPRESS_SIZE = "DecompressSize";
    private static final String NBT_COMPRESSED_BYTE_ARRAY = "CompressedByteArray";
    private static final String NBT_TITLE_ENTITY = "TitleEntity";

    private static final String NBT_BASE_X = "BaseX";
    private static final String NBT_BASE_Y = "BaseY";
    private static final String NBT_BASE_Z = "BaseZ";

    public static final String NBT_SIZE_X = "SizeX";
    public static final String NBT_SIZE_Y = "SizeY";
    public static final String NBT_SIZE_Z = "SizeZ";

    private static final String NBT_CENTER_X = "CenterX";
    private static final String NBT_CENTER_Y = "CenterY";
    private static final String NBT_CENTER_Z = "CenterZ";

    private static final String NBT_MIN_AABB_MIN_X = "minX";
    private static final String NBT_MIN_AABB_MIN_Y = "minY";
    private static final String NBT_MIN_AABB_MIN_Z = "minZ";
    private static final String NBT_MIN_AABB_MAX_X = "maxX";
    private static final String NBT_MIN_AABB_MAX_Y = "maxY";
    private static final String NBT_MIN_AABB_MAX_Z = "maxZ";

    public static final String NBT_CONSTRUCT_READY = "ConstructReady";

    public static final String NBT_PREFAB_NAME = "PrefabName";

    private LocalWorld localWorld;
    private LocalWorldWrap worldWrap;

    private String name = Strings.EMPTY;

    protected BlockPos controlPoint;
    protected Vec3i size;

    private AxisAlignedBB minAABB = null;

    //---------------------------------for build-------------------------------------------------------------------------

    private volatile boolean ready = false;

    public AbstractPrefab() {

    }

    public void init(@Nonnull World world, BlockPos baseLocation, Vec3i size) {
        this.localWorld = new LocalWorld(size, baseLocation, world, this);
        this.worldWrap = new LocalWorldWrap(world.getSaveHandler(), world.getWorldInfo(), world.provider, world.profiler, world.isRemote);
        this.worldWrap.wrap(localWorld);
        this.size = size;
        if(world.isRemote){
            initRenderer();
        }
    }

    private void initRenderer(){
        ModuleRenderManager.INSTANCE.addCubesRenderer(this);
    }

    public AbstractPrefab(World world, BlockPos baseLocation, Vec3i size) {
        this();
        init(world, baseLocation, size);
        this.controlPoint = new BlockPos(0,0,0);
    }

    public void setControlPoint(BlockPos controlPoint) {
        this.controlPoint = controlPoint;
    }

    public BlockPos getBasePos() {
        if(isLocalWorldInit())
            return localWorld.getParentWorldPos();
        return BlockPos.ORIGIN;
    }

    public int getPrefabMaxNum(){
        return localWorld.getAllBlockNum();
    }

    public BlockPos getControlPoint() {
        return controlPoint;
    }

    public void setBaseLocation(@Nonnull BlockPos baseLocation) {
        if(isLocalWorldInit())
            localWorld.setParentWorldPos(baseLocation);
    }

    public Vec3i getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalWorld getLocalWorld() {
        return localWorld;
    }

    public LocalWorldWrap getWorldWrap() {
        return worldWrap;
    }

    @Override
    public void update() {
        localWorld.getTileEntitySet()
                .forEach(new Consumer<Map.Entry<BlockPos, TileEntity>>() {
                    TileEntity tileEntity;

                    @Override
                    public void accept(Map.Entry<BlockPos, TileEntity> blockPosTileEntityEntry) {
                        tileEntity = blockPosTileEntityEntry.getValue();

                        if (!tileEntity.isInvalid() &&
                                !localWorld.isBaned(tileEntity) &&
                                tileEntity instanceof ITickable) {
                            try {
                                tileEntity.setWorld(worldWrap);
                                ((ITickable) tileEntity).update();
                            } catch (Exception e) {
                                Movement.logger.warn(tileEntity.getClass().getSimpleName() + " updated crashed, " +
                                        "try to ban the tile entity in build!\n" + e.getMessage());
                                tileEntity.setWorld(getActrualWorld());
                                ((ITickable) tileEntity).update();
                            }
                        }
                    }
                });

    }

    public World getActrualWorld() {
        return localWorld.getParentWorld();
    }

    private int constructBlockIndex = 0;

    public synchronized void constructLocalWoldFromActrualWorld() throws InterruptedException {
        constructBlockIndex = 0;
        int maxX = 0,maxY = 0,maxZ = 0,minX = size.getX(),minY = size.getY(),minZ = size.getZ();
        final World actrualWorld = getActrualWorld();
        for (int x = 0; x < size.getX(); ++x) {
            for (int y = 0; y < size.getY(); ++y)
                for (int z = 0; z < size.getZ(); ++z) {

                    BlockPos localPos = new BlockPos(x, y, z);
                    BlockPos fixedPos = getBasePos().add(localPos);
                    IBlockState s = actrualWorld.getBlockState(fixedPos);
                    IBlockState copyState = s.getActualState(actrualWorld, fixedPos);

                    boolean hasCollision = !(copyState.getCollisionBoundingBox(actrualWorld, fixedPos)
                            == Block.NULL_AABB);
                    if (hasCollision) {
                        if (x < minX) minX = x;
                        if (y < minY) minY = y;
                        if (z < minZ) minZ = z;
                        if (x > maxX) maxX = x;
                        if (y > maxY) maxY = y;
                        if (z > maxZ) maxZ = z;
                    }

                    localWorld.addBlockState(localPos, copyState);

                    if (s.getBlock().hasTileEntity(s)) {

                        TileEntity tileEntity = actrualWorld.getTileEntity(fixedPos);
                        if (tileEntity != null && !localWorld.isBaned(tileEntity)) {
                            TileEntity copyTileEntity = s.getBlock().createTileEntity(worldWrap, s);

                            if (copyTileEntity != null) {
                                copyTileEntity.deserializeNBT(tileEntity.serializeNBT());
                                copyTileEntity.setPos(localPos);
                            }

                            localWorld.addTitleEntity(localPos, copyTileEntity);
                        }
                    }
                    constructBlockIndex++;

                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }
                }
        }
        if(maxX < minX || maxY < minY || maxZ < minZ){
            minAABB = null;
        }else{
            minAABB = new AxisAlignedBB(minX,minY,minZ,maxX + 1,maxY + 1,maxZ + 1);
        }
    }

    @Deprecated
    public void diffuseLight() throws InterruptedException {
        localWorld.updateLightMap();
    }

    public void useFixSkyLight(boolean fixedLight){
        localWorld.setUseFixedSkyLight(fixedLight);
    }

    public AxisAlignedBB getMinAABB() {
        return minAABB;
    }

    //first build
    public void readFromNBT(@Nonnull NBTTagCompound nbt, @Nonnull World world) {
        BlockPos base = new BlockPos(
                nbt.getInteger(NBT_BASE_X),
                nbt.getInteger(NBT_BASE_Y),
                nbt.getInteger(NBT_BASE_Z));

        size = new Vec3i(
                nbt.getInteger(NBT_SIZE_X),
                nbt.getInteger(NBT_SIZE_Y),
                nbt.getInteger(NBT_SIZE_Z)
        );

        controlPoint = new BlockPos(
                nbt.getInteger(NBT_CENTER_X),
                nbt.getInteger(NBT_CENTER_Y),
                nbt.getInteger(NBT_CENTER_Z)
        );

        name = nbt.getString(NBT_PREFAB_NAME);

        ready = nbt.getBoolean(NBT_CONSTRUCT_READY);

        if(nbt.hasKey(NBT_MIN_AABB_MIN_X)){
            double minX = nbt.getDouble(NBT_MIN_AABB_MIN_X);
            double minY = nbt.getDouble(NBT_MIN_AABB_MIN_Y);
            double minZ = nbt.getDouble(NBT_MIN_AABB_MIN_Z);
            double maxX = nbt.getDouble(NBT_MIN_AABB_MAX_X);
            double maxY = nbt.getDouble(NBT_MIN_AABB_MAX_Y);
            double maxZ = nbt.getDouble(NBT_MIN_AABB_MAX_Z);
            minAABB = new AxisAlignedBB(minX,minY,minZ,maxX,maxY,maxZ);
        }

        init(world, base, size);

        localWorld.fastClear();

        decompressLocalBlocks(nbt);
    }

    public void readFromNBT(@Nonnull NBTTagCompound nbt) {
        setBaseLocation(new BlockPos(
                nbt.getInteger(NBT_BASE_X),
                nbt.getInteger(NBT_BASE_Y),
                nbt.getInteger(NBT_BASE_Z)
        ));

        size = new Vec3i(
                nbt.getInteger(NBT_SIZE_X),
                nbt.getInteger(NBT_SIZE_Y),
                nbt.getInteger(NBT_SIZE_Z)
        );

        controlPoint = new BlockPos(
                nbt.getInteger(NBT_CENTER_X),
                nbt.getInteger(NBT_CENTER_Y),
                nbt.getInteger(NBT_CENTER_Z)
        );

        name = nbt.getString(NBT_PREFAB_NAME);

        ready = nbt.getBoolean(NBT_CONSTRUCT_READY);

        if(nbt.hasKey(NBT_MIN_AABB_MIN_X)){
            double minX = nbt.getDouble(NBT_MIN_AABB_MIN_X);
            double minY = nbt.getDouble(NBT_MIN_AABB_MIN_Y);
            double minZ = nbt.getDouble(NBT_MIN_AABB_MIN_Z);
            double maxX = nbt.getDouble(NBT_MIN_AABB_MAX_X);
            double maxY = nbt.getDouble(NBT_MIN_AABB_MAX_Y);
            double maxZ = nbt.getDouble(NBT_MIN_AABB_MAX_Z);
            minAABB = new AxisAlignedBB(minX,minY,minZ,maxX,maxY,maxZ);
        }

        decompressLocalBlocks(nbt);
    }

    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
        compressLocalBlocks(nbt);

        nbt.setInteger(NBT_BASE_X, getBasePos().getX());
        nbt.setInteger(NBT_BASE_Y, getBasePos().getY());
        nbt.setInteger(NBT_BASE_Z, getBasePos().getZ());

        nbt.setInteger(NBT_SIZE_X, size.getX());
        nbt.setInteger(NBT_SIZE_Y, size.getY());
        nbt.setInteger(NBT_SIZE_Z, size.getZ());

        nbt.setInteger(NBT_CENTER_X, controlPoint.getX());
        nbt.setInteger(NBT_CENTER_Y, controlPoint.getY());
        nbt.setInteger(NBT_CENTER_Z, controlPoint.getZ());

        nbt.setBoolean(NBT_CONSTRUCT_READY, ready);
        nbt.setString(NBT_PREFAB_NAME,name);

        if(minAABB != null) {
            nbt.setDouble(NBT_MIN_AABB_MIN_X, minAABB.minX);
            nbt.setDouble(NBT_MIN_AABB_MIN_Y, minAABB.minY);
            nbt.setDouble(NBT_MIN_AABB_MIN_Z, minAABB.minZ);
            nbt.setDouble(NBT_MIN_AABB_MAX_X, minAABB.maxX);
            nbt.setDouble(NBT_MIN_AABB_MAX_Y, minAABB.maxY);
            nbt.setDouble(NBT_MIN_AABB_MAX_Z, minAABB.maxZ);
        }

        return nbt;
    }

    public void decompressLocalBlocks(@Nonnull NBTTagCompound nbt) {

        //blockid  x x x x\x x x x\x x x x\x x x x  ;
        //          meta        blocks    id
        final int decompressSize = nbt.getInteger(NBT_DECOMPRESS_SIZE);
        if (decompressSize <= 0) return;

        final byte[] bytes = nbt.getByteArray(NBT_COMPRESSED_BYTE_ARRAY);
        final byte[] decompressedData = new byte[decompressSize];
        ByteZip.decompress(decompressedData, bytes);
        ByteBuffer buf = ByteBuffer.allocate(decompressedData.length);
        buf.put(decompressedData);
        buf.rewind();

        for (int x = 0; x < size.getX(); ++x)
            for (int y = 0; y < size.getY(); ++y)
                for (int z = 0; z < size.getZ(); ++z) {
                    final short blockId = buf.getShort();
                    final int light = buf.get();

                    final IBlockState state = Block.getStateById(blockId);
                    BlockPos blockPos = new BlockPos(x, y, z);
                    localWorld.addBlockState(blockPos, state);
                    localWorld.setLightFor(light,blockPos);

                    if (nbt.hasKey(NBT_TITLE_ENTITY + "." + x + "." + y + "." + z)) {
                        TileEntity tileEntity = state.getBlock().createTileEntity(getActrualWorld(), state);
                        if (tileEntity != null) {
                            NBTTagCompound nbtTagCompound = nbt.getCompoundTag(NBT_TITLE_ENTITY + "." + x + "." + y + "." + z);
                            tileEntity.readFromNBT(nbtTagCompound);
                            tileEntity.setWorld(getActrualWorld());
                            localWorld.addTitleEntity(blockPos, tileEntity);
                        }
                    }
                }

    }

    public void compressLocalBlocks(@Nonnull NBTTagCompound nbt) {

        //blockid  x x x x\x x x x\x x x x\x x x x  ;
        //          meta  \    b l o c k   i d
        final ByteZip byteZip = new ByteZip();

        localWorld.stream(new LocalWorld.LocalWorldSearch() {
            @Override
            public void search(BlockPos blockPos, IBlockState blockState, TileEntity tileEntity) {
                int blockId = Block.getStateId(blockState);
                final short usefulId = (short) (blockId & 0xffff);
                byteZip.setShort(usefulId);
                byteZip.setByte((byte)(localWorld.getLight(blockPos) & 0x000f));

                if (tileEntity != null) {
                    final NBTTagCompound nbtTagCompound = new NBTTagCompound();
                    tileEntity.writeToNBT(nbtTagCompound);
                    nbt.setTag(NBT_TITLE_ENTITY + "." +
                                    blockPos.getX() + "." +
                                    blockPos.getY() + "." +
                                    blockPos.getZ(),
                            nbtTagCompound);
                }
            }
        });

        byteZip.compress();

        nbt.setInteger(NBT_DECOMPRESS_SIZE, byteZip.getOrgSize());
        nbt.setByteArray(NBT_COMPRESSED_BYTE_ARRAY, byteZip.getOutput());
    }

    //invalid recipe and delete build;
    public void invalid() {
        if(localWorld != null)
            localWorld.fastClear();

        if(isLocalWorldInit() && getActrualWorld().isRemote){
            ModuleRenderManager.INSTANCE.removeCubesRenderer(this);
        }
    }

    public boolean isLocalWorldInit() {
        return localWorld != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractPrefab)) return false;
        AbstractPrefab that = (AbstractPrefab) o;
        return Objects.equals(localWorld, that.localWorld);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localWorld);
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public int getConstructBlockIndex() {
        return constructBlockIndex;
    }

    public void setConstructBlockIndex(int constructBlockIndex) {
        this.constructBlockIndex = constructBlockIndex;
    }
}
