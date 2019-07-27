package com.nowandfuture.mod.core.prefab;

import com.creativemd.creativecore.client.mods.optifine.OptifineHelper;
import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.client.renders.CubesRenderer;
import com.nowandfuture.mod.core.selection.OBBounding;
import com.nowandfuture.mod.utils.ByteZip;
import joptsimple.internal.Strings;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.optifine.shaders.ShadersRender;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.*;
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

    public static final String NBT_PREFAB_NAME = "PrefabName";

    private LocalWorld localWorld;
    private LocalWorldWrap worldWrap;

    private String name = Strings.EMPTY;

    protected BlockPos controlPoint;
    protected Vec3i size;

    private OBBounding obbounding;

    //---------------------------------for build-------------------------------------------------------------------------
//    private BlockRenderHelper blockRenderHelper;
    private CubesRenderer cubesRenderer;
    private Minecraft mc =  Minecraft.getMinecraft();

    private volatile boolean ready = false;
    private boolean accurateLight = false;

    public AbstractPrefab() {

    }

    public OBBounding getOBBounding() {
        if(this.obbounding == null){
            this.obbounding = new OBBounding(new AxisAlignedBB(0,0,0,size.getX(),size.getY(),size.getZ()));
        }
        return this.obbounding;
    }

    public OBBounding getTransformedBounding(){
        return getOBBounding().transform(getModelMatrix());
    }

    public void init(@Nonnull World world, BlockPos baseLocation, Vec3i size) {
        this.localWorld = new LocalWorld(size, baseLocation, world);
        this.worldWrap = new LocalWorldWrap(world.getSaveHandler(), world.getWorldInfo(), world.provider, world.profiler, true);
        this.worldWrap.wrap(localWorld);
        this.cubesRenderer = new CubesRenderer(this);
        this.size = size;
    }

    public CubesRenderer getCubesRenderer() {
        return cubesRenderer;
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
        return localWorld.getParentWorldPos();
    }

    public Vector3f getTransformedBasePos(){
        return localWorld.getTransformedPos();
    }

    public int getPrefabMaxNum(){
        return localWorld.getAllBlockNum();
    }

    public Vector3f getTransformedPos(Vector3f vector3f){
        return localWorld.getTransformedPos(vector3f);
    }

    public Vector3f getTransformedWorldPos(Vector3f vector3f){
        return getTransformedPos(vector3f).translate(getBasePos().getX(),getBasePos().getY(),getBasePos().getZ());
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalWorld getLocalWorld() {
        return localWorld;
    }


    //------------------------------------------render function-----------------------------------------------
    public void renderPre(double partialTicks) {
        final Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        if (entity == null ) {
            return;
        }

        final double renderPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
        final double renderPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
        final double renderPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;

        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();

        GlStateManager.translate(getBasePos().getX() - renderPosX,
                getBasePos().getY() - renderPosY,
                getBasePos().getZ() - renderPosZ);
    }

    public void renderPost(double partialTicks) {
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    public void prepare(double p){
        if(cubesRenderer == null) return;
        if(cubesRenderer.isBuilt()) {
            cubesRenderer.prepare(p);
        }else{
            cubesRenderer.build();
        }
    }

    public void buildTranslucent(float p){
        cubesRenderer.buildTranslucent();
    }

    @Deprecated
    public void render(double p) {
        Minecraft.getMinecraft().entityRenderer.enableLightmap();
        renderBlocks(p);
        RenderHelper.enableStandardItemLighting();
        renderTileEntity(p);
        RenderHelper.disableStandardItemLighting();
        Minecraft.getMinecraft().entityRenderer.disableLightmap();
    }

    @Deprecated
    public void renderBlocks(double p){
        if(cubesRenderer == null) return;
        if(cubesRenderer.isBuilt()) {
            mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            GlStateManager.disableAlpha();
            mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, this.mc.gameSettings.mipmapLevels > 0); // FORGE: fix flickering leaves when mods mess up the blurMipmap settings
//            ShadersRender.beginTerrainSolid();
            cubesRenderer.render(BlockRenderLayer.SOLID);
            GlStateManager.enableAlpha();

            mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
            mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
//            ShadersRender.beginTerrainCutoutMipped();
            cubesRenderer.render(BlockRenderLayer.CUTOUT_MIPPED);
//            ShadersRender.beginTerrainCutout();
            cubesRenderer.render(BlockRenderLayer.CUTOUT);
            mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
//            ShadersRender.endTerrain();
//            renderTranslucentBlocks(p);
        }else{
            cubesRenderer.build();
        }
    }

    public void renderBlockRenderLayer(BlockRenderLayer layer){
        if(cubesRenderer == null || !cubesRenderer.isBuilt()) return;
        Minecraft.getMinecraft().entityRenderer.enableLightmap();
        cubesRenderer.render(layer);
        Minecraft.getMinecraft().entityRenderer.disableLightmap();

    }

    public void renderTileEntity(final double p) {

        GlStateManager.resetColor();
        Minecraft.getMinecraft().entityRenderer.enableLightmap();
        ForgeHooksClient.setRenderPass(0);
        final TileEntityRendererDispatcher dispatcher = TileEntityRendererDispatcher.instance;
        RenderHelper.enableStandardItemLighting();

//        dispatcher.preDrawBatch();

        localWorld.getTileEntitySet()
                .forEach(tileEntry -> {
                    TileEntity tileEntity = tileEntry.getValue();

                    if (tileEntity == null || localWorld.isBaned(tileEntity)) return;

                    tileEntity.setPos(tileEntry.getKey());
                    tileEntity.setWorld(worldWrap);

                    BlockPos blockPos = tileEntity.getPos();

                    int i = accurateLight ? this.getLocalWorld().getActCombinedLight(blockPos,0):
                            this.getLocalWorld().getCombinedLight(blockPos, 0);
                    int j = i % 65536;
                    int k = i / 65536;
                    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                    TileEntitySpecialRenderer renderer = dispatcher.getRenderer(tileEntity.getClass());
                    boolean isGlobal = (renderer != null && renderer.isGlobalRenderer(tileEntity));

                    if (isGlobal) {
                        Minecraft.getMinecraft().entityRenderer.disableLightmap();
                    }

                    dispatcher.render(tileEntry.getValue(),
                            tileEntry.getKey().getX(),
                            tileEntry.getKey().getY(),
                            tileEntry.getKey().getZ(),
                            (float) p, -1, 1);

                    if (isGlobal) {
                        Minecraft.getMinecraft().entityRenderer.enableLightmap();
                    }
                });

        //enableStandardItemLighting in drawBatch(int pass);
//        dispatcher.drawBatch(ForgeHooksClient.getWorldRenderPass());
        RenderHelper.disableStandardItemLighting();
        Minecraft.getMinecraft().entityRenderer.disableLightmap();

        ForgeHooksClient.setRenderPass(0);
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
                                Movement.logger.warn(tileEntity.getClass().getSimpleName() + "crashed, try to ban the tile entity in build!\n" + e.getMessage());
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
        final World actrualWorld = getActrualWorld();
        for (int x = 0; x < size.getX(); ++x)
            for (int y = 0; y < size.getY(); ++y)
                for (int z = 0; z < size.getZ(); ++z) {

                    BlockPos localPos = new BlockPos(x, y, z);
                    BlockPos fixedPos = getBasePos().add(localPos);
                    IBlockState s = actrualWorld.getBlockState(fixedPos);
                    IBlockState copyState = s.getActualState(actrualWorld, fixedPos);

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
                    constructBlockIndex ++;

                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }
                }
    }

    @Deprecated
    public void diffuseLight() throws InterruptedException {
        localWorld.updateLightMap();
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

        return nbt;
    }

    public void decompressLocalBlocks(@Nonnull NBTTagCompound nbt) {

        //blockid  x x x x\x x x x\x x x x\x x x x  ;
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
                    final int light = buf.get();

                    final IBlockState state = Block.getStateById(blockId);
                    BlockPos blockPos = new BlockPos(x, y, z);
                    localWorld.addBlockState(blockPos, state);
                    localWorld.setLightForCoord(light,blockPos);

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


    //clear recipe and delete build;
    public void clear() {
        if(localWorld != null)
            localWorld.fastClear();
        invalidRenderList();
    }

    public boolean isLocalWorldInit() {
        return localWorld != null;
    }

    public void invalidRenderList() {
        if(cubesRenderer != null)
            cubesRenderer.invalid();
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

    public int getConstructBlockIndex() {
        return constructBlockIndex;
    }

    public void setConstructBlockIndex(int constructBlockIndex) {
        this.constructBlockIndex = constructBlockIndex;
    }
}
