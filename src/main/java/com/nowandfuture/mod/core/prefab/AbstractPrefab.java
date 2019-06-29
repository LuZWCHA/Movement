package com.nowandfuture.mod.core.prefab;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.utils.ByteZip;
import com.nowandfuture.mod.utils.DrawHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class AbstractPrefab implements ITickable {
    public static final String NBT_DECOMPRESS_SIZE = "DecompressSize";
    public static final String NBT_COMPRESSED_BYTE_ARRAY = "CompressedByteArray";
    public static final String NBT_TITLE_ENTITY = "TitleEntity";

    public static final String NBT_BASE_X = "BaseX";
    public static final String NBT_BASE_Y = "BaseY";
    public static final String NBT_BASE_Z = "BaseZ";

    public static final String NBT_SIZE_X = "SizeX";
    public static final String NBT_SIZE_Y = "SizeY";
    public static final String NBT_SIZE_Z = "SizeZ";

    public static final String NBT_CENTER_X = "CenterX";
    public static final String NBT_CENTER_Y = "CenterY";
    public static final String NBT_CENTER_Z = "CenterZ";

    public static final String NBT_CONSTRUCT_READY = "ConstructReady";

    private LocalWorld localWorld;
    private LocalWorldWrap worldWrap;

    protected BlockPos controlPoint;
    protected Vec3i size;

    private BlockRenderHelper blockRenderHelper;

    private volatile boolean ready = false;

    public AbstractPrefab() {

    }

    public void init(@Nonnull World world, BlockPos baseLocation, Vec3i size) {
        localWorld = new LocalWorld(size, baseLocation, world);
        worldWrap = new LocalWorldWrap(world.getSaveHandler(), world.getWorldInfo(), world.provider, world.profiler, true);
        worldWrap.wrap(localWorld);
        blockRenderHelper = new BlockRenderHelper(localWorld);
        this.size = size;
    }

    public AbstractPrefab(World world, BlockPos baseLocation, Vec3i size) {
        this();
        init(world, baseLocation, size);
        controlPoint = baseLocation;
    }

    public void setControlPoint(BlockPos controlPoint) {
        this.controlPoint = controlPoint;
    }

    public BlockPos getBasePos() {
        return localWorld.getParentWorldPos();
    }

    public Vector3f getTransformedBasePos(){
        return localWorld.getTransformedPos();
    }

    public Vector3f getTransformedPos(Vector3f vector3f){
        return localWorld.getTransformedPos(vector3f);
    }

    public BlockPos getControlPoint() {
        return controlPoint;
    }

    public void setBaseLocation(@Nonnull BlockPos baseLocation) {
        localWorld.setParentWorldPos(baseLocation);
    }

    public Vector3f getTransformedPos(){
        return localWorld.getTransformedPos();
    }

    public Vec3i getSize() {
        return size;
    }

    public void renderFast(BufferBuilder bufferBuilder) {
        blockRenderHelper.doFastRender(bufferBuilder);
    }

    public void renderPre(float partialTicks) {
        final Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        if (entity == null) {
            return;
        }

        double renderPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
        double renderPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
        double renderPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;

        //Movement.logger.warn("base:" + getBasePos().toString() + "trans:" + vector3f.toString());


        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();

        GlStateManager.translate(getBasePos().getX() - renderPosX,
                getBasePos().getY() - renderPosY,
                getBasePos().getZ() - renderPosZ);

        final Vector3f vector3f = getTransformedBasePos();
        final Vector3f vector3f1 = getTransformedPos(new Vector3f(getSize().getX(),getSize().getY(),getSize().getZ()));

        DrawHelper.drawOutlinedBoundingBox(new AxisAlignedBB(
                        vector3f.x,vector3f.y,vector3f.z,
                vector3f1.x,vector3f1.y ,vector3f1.z)
        );
    }

    public void renderPost(float partialTicks) {
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    public void render(float p) {
        blockRenderHelper.doRender();
        GlStateManager.resetColor();
        renderTileEntity(p);
    }

    public void renderTileEntity(final float p) {

        ForgeHooksClient.setRenderPass(0);
        Minecraft.getMinecraft().entityRenderer.enableLightmap();

        TileEntityRendererDispatcher dispatcher = TileEntityRendererDispatcher.instance;

        RenderHelper.enableStandardItemLighting();
        dispatcher.preDrawBatch();

        localWorld.getTileEntitySet()
                .forEach(tileEntry -> {
                    if (localWorld.isBaned(tileEntry.getValue())) return;
                    tileEntry.getValue().setPos(tileEntry.getKey());
                    tileEntry.getValue().setWorld(worldWrap);

                    int i = this.getActrualWorld().getCombinedLight(tileEntry.getValue().getPos().add(localWorld.getParentWorldPos()), 0);
                    int j = i % 65536;
                    int k = i / 65536;
                    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                    TileEntitySpecialRenderer renderer = dispatcher.getRenderer(tileEntry.getValue().getClass());
                    boolean isGlobal = (renderer == null ? false : renderer.isGlobalRenderer(tileEntry.getValue()));

                    if (isGlobal) {
                        Minecraft.getMinecraft().entityRenderer.disableLightmap();
                    }

                    dispatcher.render(tileEntry.getValue(),
                            tileEntry.getKey().getX(),
                            tileEntry.getKey().getY(),
                            tileEntry.getKey().getZ(),
                            p, -1, 1);

                    if (isGlobal) {
                        Minecraft.getMinecraft().entityRenderer.enableLightmap();
                    }
                });

        //enableStandardItemLighting in drawBatch(int pass);
        dispatcher.drawBatch(ForgeHooksClient.getWorldRenderPass());

//        GlStateManager.disableBlend();
//        GlStateManager.enableCull();
        RenderHelper.disableStandardItemLighting();
        ForgeHooksClient.setRenderPass(0);

        Minecraft.getMinecraft().entityRenderer.disableLightmap();

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
                                Movement.logger.warn(tileEntity.getClass().getSimpleName() + "crashed, try to ban the tile entity in render!" + e.getMessage());
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

    public synchronized void constructLocalWoldFromActrualWorld() throws InterruptedException {
        final World actrualWorld = getActrualWorld();
        for (int x = 0; x < size.getX(); ++x)
            for (int y = 0; y < size.getY(); ++y)
                for (int z = 0; z < size.getZ(); ++z) {
                    BlockPos localPos = new BlockPos(x, y, z);
                    BlockPos fixedPos = getBasePos().add(localPos);
                    IBlockState s = actrualWorld.getBlockState(fixedPos);
                    IBlockState copyState = s.getActualState(actrualWorld, fixedPos);

                    localWorld.addBlockState(localPos, copyState);
                    if (!s.getBlock().hasTileEntity(s)) continue;

                    TileEntity tileEntity = actrualWorld.getTileEntity(fixedPos);
                    if (tileEntity != null && !localWorld.isBaned(tileEntity)) {
                        TileEntity copyTileEntity = s.getBlock().createTileEntity(worldWrap, s);

                        if (copyTileEntity != null) {
                            copyTileEntity.deserializeNBT(tileEntity.serializeNBT());
                            copyTileEntity.setPos(localPos);
                            //copyTileEntity.setModuleWorld(getActrualWorld());
                        }

                        localWorld.addTitleEntity(localPos, copyTileEntity);
                    }
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }
                }


    }

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

        ready = nbt.getBoolean(NBT_CONSTRUCT_READY);

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

        ready = nbt.getBoolean(NBT_CONSTRUCT_READY);

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

        return nbt;
    }

    public void decompressLocalBlocks(@Nonnull NBTTagCompound nbt) {

        //blockid  x x x x\x x x x\x x x x\x x x x  ;16 Bytes
        //          meta        block    id
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
//                    final int light = buf.getInt();

                    final IBlockState state = Block.getStateById(blockId);
                    BlockPos blockPos = new BlockPos(x, y, z);
                    localWorld.addBlockState(blockPos, state);

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

        //blockid  x x x x\x x x x\x x x x\x x x x  ;16 Bytes
        //          meta  \    b l o c k   i d
        final ByteZip byteZip = new ByteZip();

        localWorld.stream(new LocalWorld.LocalWorldSearch() {
            @Override
            public void search(BlockPos blockPos, IBlockState blockState, TileEntity tileEntity) {
                int blockId = Block.getStateId(blockState);
                final short usefulId = (short) (blockId & 0xffff);
                byteZip.setShort(usefulId);
//                byteZip.setInt(light);

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


    //clear recipe and delete render;
    public void clear() {
        localWorld.fastClear();
        invalidRenderList();
    }

    public boolean isLocalWorldInit() {
        return localWorld != null;
    }

    public void invalidRenderList() {
        blockRenderHelper.clear();
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

    public Matrix4f getModelMatrix() {
        return localWorld.getModelMatrix();
    }

    public void setModelMatrix(Matrix4f modelMatrix) {
        this.localWorld.setModelMatrix(modelMatrix);
    }

    public void setModelMatrix(FloatBuffer modelMatrix) {
        getModelMatrix().load(modelMatrix);
    }
}
