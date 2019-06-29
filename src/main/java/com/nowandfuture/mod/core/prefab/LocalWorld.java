package com.nowandfuture.mod.core.prefab;

import com.nowandfuture.mod.core.entities.TileEntityMovementModule;
import com.nowandfuture.mod.utils.MathHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public class LocalWorld implements IBlockAccess {
    private World parentWorld;
    private BlockPos parentWorldPos;

    private Matrix4f modelMatrix;

    public BlockPos getParentWorldPos() {
        return parentWorldPos;
    }

    public Vector3f getTransformedPos() {
        return getTransformedPos(new Vector3f(0,0,0));
    }

    public Vector3f getTransformedPos(Vector3f pos) {
        Vector4f vector4f = new Vector4f();
        Matrix4f.transform(modelMatrix,
                new Vector4f(pos.x,pos.y,pos.z,1),
                vector4f);
        return (new Vector3f(vector4f));
    }

    public void setParentWorldPos(BlockPos parentWorldPos) {
        this.parentWorldPos = parentWorldPos;
    }

    public void removeTileEntity(BlockPos pos) {
        tileEntities.remove(pos);
    }

    public boolean isBlockFullCube(BlockPos pos) {
        return get(pos).orElse(Blocks.AIR.getDefaultState()).isFullCube();
    }

    public boolean isBlockNormalCube(BlockPos pos, boolean aDefault) {
        return get(pos).orElse(Blocks.AIR.getDefaultState()).isBlockNormalCube();
    }

    // TODO: 2019/6/28 finish the light inside localworld
    public static class LocalBlock {
        public int light;
        public IBlockState blockState;
        public BlockPos pos;
        public LocalBlock(BlockPos pos, IBlockState blockState){this.pos = pos;this.blockState = blockState;}
    }

    private Map<BlockPos,TileEntity> tileEntities;

    //private Map<BlockPos,IBlockState> blocks;
    private IBlockState[][][] blocks;
    private Vec3i size;
    private List<LocalBlock> renderBlocks;

    private int blockNum;
//    private int nowBlockArrayIndex;

    public int getAllBlockNum()
    {
        return size.getX() * size.getY() * size.getZ();
    }

    public int getBlockNum() {
        return blockNum;
    }

    public interface LocalWorldSearch{
        void search(BlockPos blockPos,IBlockState blockState,TileEntity tileEntity)  ;
    }

    public List<LocalBlock> getRenderBlocks() {
        return renderBlocks;
    }

    public LocalWorld stream(LocalWorldSearch localWorldSearch)   {
        for(int i=0;i<size.getX();++i)
            for(int j=0;j<size.getY();++j)
                for(int k=0;k<size.getZ();++k) {
                    BlockPos pos = new BlockPos(i,j,k);
                    TileEntity tileEntity = tileEntities.get(pos);

                    localWorldSearch.search(pos,blocks[i][j][k],tileEntity);
                }
        return this;
    }

    private boolean put(BlockPos blockPos, IBlockState blockState){
        if(MathHelper.isInCuboid(blockPos,Vec3i.NULL_VECTOR,size)){
            return false;
        }
        blocks[blockPos.getX()][blockPos.getY()][blockPos.getZ()] = blockState;
        return true;
    }

    private void putNotCheck(BlockPos blockPos, IBlockState blockState){
        blocks[blockPos.getX()][blockPos.getY()][blockPos.getZ()] = blockState;
    }

    public Stream<TileEntity> streamTiles(){
        return tileEntities.values().stream();
    }

    public Set<Map.Entry<BlockPos, TileEntity>> getTileEntitySet() {
        return tileEntities.entrySet();
    }

    public void addTitleEntity(BlockPos pos, TileEntity tileEntity){
        tileEntities.put(pos, tileEntity);
    }

    public Optional<IBlockState> get(BlockPos pos){
        if(MathHelper.isInCuboid(pos,Vec3i.NULL_VECTOR,size))
            return Optional.of(blocks[pos.getX()][pos.getY()][pos.getZ()]);
        return Optional.empty();
    }

    private void allocBlockArray(int x, int y, int z)
    {
        blockNum = 0;
        blocks = new IBlockState[x][y][z];
        toAir(x, y, z);
    }

    private void toAir(Vec3i size){
        toAir(size.getX(),size.getY(),size.getZ());
    }

    private void toAir(int x, int y, int z){
        for(int i=0;i<x;++i)
            for(int j=0;j<y;++j)
                for(int k=0;k<z;++k)
                    blocks[i][j][k] = Blocks.AIR.getDefaultState();
    }

    public boolean addBlockStateSafely(BlockPos blockPos,IBlockState blockState){
        if(put(blockPos,blockState)) {
            if(blockState.getBlock() != Blocks.AIR) {
                blockNum ++ ;
                renderBlocks.add(new LocalBlock(blockPos, blockState));
            }
            return true;
        }
        return false;
    }

    void addBlockState(BlockPos blockPos,IBlockState blockState){

        putNotCheck(blockPos,blockState);
        if(blockState.getBlock() != Blocks.AIR) {
            blockNum ++ ;
            renderBlocks.add(new LocalBlock(blockPos, blockState));
        }
    }

    public void fastClear(){
        clear(false);
    }

    private void clear(boolean setAir){
        if(!tileEntities.isEmpty())
            tileEntities.clear();
        if(!renderBlocks.isEmpty())
            renderBlocks.clear();

        if(blockNum > 0 && setAir) toAir(size);

        blockNum = 0;
    }

    //==================================================================================================================================


    public LocalWorld(Vec3i size,BlockPos parentWorldPos, World world){
        this.size = size;
        allocBlockArray(size.getX(),size.getY(),size.getZ());
        tileEntities = new HashMap<>();
        parentWorld = world;
        this.parentWorldPos = parentWorldPos;

        renderBlocks = new LinkedList<>();

        modelMatrix = new Matrix4f();
        modelMatrix.setIdentity();
    }

    public boolean isBaned(TileEntity entity){
        return entity instanceof TileEntityMovementModule;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return tileEntities.get(pos);
    }

    // TODO: 2019/6/28 fix position (need transform), and fix lightmap so that lightmap will be constructTest when
    // TODO: 2019/6/28 constructTest the localworld
    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        int i = this.getLightFromNeighborsFor(EnumSkyBlock.SKY, pos);
        int j = this.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, pos);
        int k = getParentWorld().getLightFor(EnumSkyBlock.BLOCK,pos.add(parentWorldPos));

        if (j < lightValue)
        {
            j = lightValue;
        }

        if(k < lightValue){
            k = lightValue;
        }

        if(j < k) {
            j = k;
        }

        return i << 20 | j << 4;
    }

    @SideOnly(Side.CLIENT)
    public int getLightFromNeighborsFor(EnumSkyBlock type, BlockPos pos)
    {
        if (!parentWorld.provider.hasSkyLight() && type == EnumSkyBlock.SKY)
        {
            return 0;
        }
        else
        {

            if (!this.isValid(pos))
            {
                return type.defaultLightValue;
            }
            else if (!parentWorld.isBlockLoaded(pos.add(parentWorldPos)))
            {
                return type.defaultLightValue;
            }
            else if (this.getBlockState(pos).useNeighborBrightness())
            {
                int i1 = this.getLightFor(type, pos.up());
                int i = this.getLightFor(type, pos.east());
                int j = this.getLightFor(type, pos.west());
                int k = this.getLightFor(type, pos.south());
                int l = this.getLightFor(type, pos.north());

                if (i > i1)
                {
                    i1 = i;
                }

                if (j > i1)
                {
                    i1 = j;
                }

                if (k > i1)
                {
                    i1 = k;
                }

                if (l > i1)
                {
                    i1 = l;
                }

                return i1;
            }
            else
            {
                Chunk chunk = parentWorld.getChunkFromBlockCoords(pos.add(parentWorldPos));
                if(type == EnumSkyBlock.SKY)
                    return chunk.getLightFor(type,pos.add(parentWorldPos));

                IBlockState blockState = get(pos).orElse(Blocks.AIR.getDefaultState());
                return blockState.getBlock().getLightValue(blockState,this,pos);
            }
        }
    }

    public int getLightFor(EnumSkyBlock type, BlockPos pos)
    {
        if (!this.isValid(pos))
        {
            return type.defaultLightValue;
        }
        else
        {
            Chunk chunk = parentWorld.getChunkFromBlockCoords(pos.add(parentWorldPos));
            if(type == EnumSkyBlock.SKY)
                return chunk.getLightFor(type,pos.add(parentWorldPos));

            IBlockState blockState = get(pos).orElse(Blocks.AIR.getDefaultState());
            return blockState.getBlock().getLightValue(blockState,this,pos);
        }
    }

    /**
     * Check if the given BlockPos has valid coordinates
     */
    public boolean isValid(BlockPos pos)
    {
        return MathHelper.isInCuboid(pos,Vec3i.NULL_VECTOR,size);
    }


    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return get(pos).orElse(Blocks.AIR.getDefaultState());
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return get(pos).orElse(Blocks.AIR.getDefaultState()).getBlock() == Blocks.AIR;
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return parentWorld.getBiome(pos.add(parentWorldPos));
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return getBlockState(pos).getStrongPower(this, pos, direction);
    }

    @Override
    public WorldType getWorldType() {
        return parentWorld.getWorldType();
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        return getBlockState(pos).isSideSolid(this,pos,side);
    }

    public World getParentWorld() {
        return parentWorld;
    }


    public int getHeight(){
        return size.getY();
    }

    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    public void setModelMatrix(Matrix4f modelMatrix) {
        this.modelMatrix = modelMatrix;
    }
}
