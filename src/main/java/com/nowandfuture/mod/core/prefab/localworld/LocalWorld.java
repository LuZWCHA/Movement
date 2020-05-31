package com.nowandfuture.mod.core.prefab.localworld;

import com.nowandfuture.mod.core.common.entities.TileEntityTimelineEditor;
import com.nowandfuture.mod.core.prefab.AbstractPrefab;
import com.nowandfuture.mod.core.selection.OBBox;
import com.nowandfuture.mod.utils.math.MathHelper;
import com.nowandfuture.mod.utils.math.Matrix4f;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

import static com.nowandfuture.mod.core.client.renderers.cubes.CubesBuilder.CUBE_SIZE;

public class LocalWorld implements IBlockAccess {
    private final Matrix4f matrix4f;
    {
        matrix4f = new Matrix4f();
        matrix4f.setIdentity();
    }

    private World parentWorld;
    private BlockPos parentWorldPos;
    private AbstractPrefab prefab;

    private boolean useFixedSkyLight = true;

    public Matrix4f getMatrix4f() {
        return matrix4f;
    }

    public BlockPos getParentWorldPos() {
        return parentWorldPos;
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

    public static class LocalBlock {
        public IBlockState blockState;
        public BlockPos pos;
        public LocalBlock(BlockPos pos, IBlockState blockState){this.pos = pos;this.blockState = blockState;}
    }

    private Map<BlockPos,TileEntity> tileEntities;

    private IBlockState[][][] blocks;
    private Vec3i size;
    private List<LocalBlock> renderBlocks;
    private int[][][] lightMap;

    private int blockNum;

    public int getAllBlockNum() {
        return size.getX() * size.getY() * size.getZ();
    }

    public Vec3i getSize() {
        return size;
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

    public LocalWorld stream(LocalWorldSearch localWorldSearch) {
        for(int i=0;i<size.getX();++i)
            for(int j=0;j<size.getY();++j)
                for(int k=0;k<size.getZ();++k) {
                    BlockPos pos = new BlockPos(i,j,k);
                    TileEntity tileEntity = tileEntities.get(pos);

                    localWorldSearch.search(pos,blocks[i][j][k],tileEntity);
                }
        return this;
    }

    public LocalWorld streamCubes(BlockPos cubePos, LocalWorldSearch localWorldSearch){
        final int x = cubePos.getX() * CUBE_SIZE;
        final int y = cubePos.getY() * CUBE_SIZE;
        final int z = cubePos.getZ() * CUBE_SIZE;

        for(int i = x;i< x + CUBE_SIZE;++i)
            for(int j = y;j<y + CUBE_SIZE;++j)
                for(int k = z;k<z + CUBE_SIZE;++k) {
                    BlockPos pos = new BlockPos(i,j,k);
                    TileEntity tileEntity = tileEntities.get(pos);

                    localWorldSearch.search(pos,get(pos).orElse(Blocks.AIR.getDefaultState()),tileEntity);
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

    public IBlockState get(int x,int y,int z){
        return blocks[x][y][z];
    }

    private void allocBlockArray(int x, int y, int z)
    {
        blockNum = 0;
        blocks = new IBlockState[x][y][z];
        lightMap = new int[x][y][z];
        toAir(x, y, z);
    }

    private void toAir(Vec3i size){
        toAir(size.getX(),size.getY(),size.getZ());
    }

    private void toAir(int x, int y, int z){
        for(int i=0;i<x;++i)
            for(int j=0;j<y;++j)
                for(int k=0;k<z;++k) {
                    blocks[i][j][k] = Blocks.AIR.getDefaultState();
                    lightMap[i][j][k] = -1;
                }
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

    public void addBlockState(BlockPos blockPos,IBlockState blockState){

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


    public LocalWorld(Vec3i size, BlockPos parentWorldPos, World world, AbstractPrefab prefab){
        this.size = size;
        this.prefab = prefab;
        this.allocBlockArray(size.getX(),size.getY(),size.getZ());
        this.tileEntities = new HashMap<>();
        this.parentWorld = world;
        this.parentWorldPos = parentWorldPos;
        this.renderBlocks = new LinkedList<>();
    }

    public boolean isBaned(TileEntity entity){
        return entity instanceof TileEntityTimelineEditor;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return tileEntities.get(pos);
    }

    public int getActCombinedLight(BlockPos pos, int lightValue,Matrix4f matrix4f,boolean useFixSkyLight){

        Vec3d transPos = OBBox.transformCoordinate(matrix4f,new Vec3d(pos));
        BlockPos np = new BlockPos(transPos.x + parentWorldPos.getX(),
                transPos.y + parentWorldPos.getY(),
                transPos.z + parentWorldPos.getZ());

        int i =  useFixSkyLight ? 15 :this.getLightFromNeighborsFor(EnumSkyBlock.SKY, pos, matrix4f);
        int j = this.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, pos, matrix4f);
        int k = getParentWorld().getLightFor(EnumSkyBlock.BLOCK,np);

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

    public void setUseFixedSkyLight(boolean useFixedSkyLight) {
        this.useFixedSkyLight = useFixedSkyLight;
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return getActCombinedLight(pos, lightValue, matrix4f,false);
    }

    @SideOnly(Side.CLIENT)
    public int getLightFromNeighborsFor(EnumSkyBlock type, BlockPos pos, Matrix4f matrix4f)
    {
        if (!parentWorld.provider.hasSkyLight() && type == EnumSkyBlock.SKY)
        {
            return 0;
        }
        else
        {
            Vec3d transPos = OBBox.transformCoordinate(matrix4f,new Vec3d(pos));
            BlockPos np = new BlockPos(transPos.x + parentWorldPos.getX(),
                    transPos.y + parentWorldPos.getY(),
                    transPos.z + parentWorldPos.getZ());

            if (!this.isValid(pos) && type == EnumSkyBlock.BLOCK)
            {
                return type.defaultLightValue;
            }
            else if (!parentWorld.isBlockLoaded(np))
            {
                return type.defaultLightValue;
            }
            else if (this.getBlockState(pos).useNeighborBrightness())
            {
                int i1 = this.getLightFor(type, pos.up(),matrix4f);
                int i = this.getLightFor(type, pos.east(),matrix4f);
                int j = this.getLightFor(type, pos.west(),matrix4f);
                int k = this.getLightFor(type, pos.south(),matrix4f);
                int l = this.getLightFor(type, pos.north(),matrix4f);

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
                if (type == EnumSkyBlock.SKY) {
                    Chunk chunk = parentWorld.getChunk(np);
                    return chunk.getLightFor(type, np);
                }

                return getLightFor(type,pos,matrix4f);
            }
        }
    }

    public int getLightFor(EnumSkyBlock type, BlockPos pos, Matrix4f matrix4f)
    {
        if (!this.isValid(pos) && type == EnumSkyBlock.BLOCK)
        {
            return type.defaultLightValue;
        }
        else
        {
            Vec3d transPos = OBBox.transformCoordinate(matrix4f,new Vec3d(pos));
            BlockPos np = new BlockPos(transPos.x + parentWorldPos.getX(),
                    transPos.y + parentWorldPos.getY(),
                    transPos.z + parentWorldPos.getZ());

            if(type == EnumSkyBlock.BLOCK){
                return getLight(pos);
            }else{
                Chunk chunk = parentWorld.getChunk(np);
                return chunk.getLightFor(type, np);
            }
        }
    }

    public int getLight(BlockPos pos){
        if(isValid(pos))
            return lightMap[pos.getX()][pos.getY()][pos.getZ()];
        else
            return 0;
    }

    private int getLight(int x,int y,int z){
        return lightMap[x][y][z];
    }


    @Deprecated
    public void updateLightMap() throws InterruptedException{
        final int x = size.getX(),y = size.getY(),z = size.getZ();

        for(int i=0;i<x;++i)
            for(int j=0;j<y;++j)
                for(int k=0;k<z;++k) {
                    int light = parentWorld.getLightFromNeighborsFor(EnumSkyBlock.BLOCK,parentWorldPos.add(i,j,k));
                    setLightFor(light,i,j,k);
                    if(Thread.currentThread().isInterrupted()){
                        throw new InterruptedException();
                    }
                }
    }

    private void setLightFor(int light, int x, int y, int z){
        if(isValid(x,y,z)) {
            int orgLight = lightMap[x][y][z];
            if(light > orgLight) {
                lightMap[x][y][z] = light;
            }
        }
    }

//    public boolean checkLightFor(EnumSkyBlock lightType, BlockPos pos)
//    {
//        if (!this.isAreaLoaded(pos, 16, false))
//        {
//            return false;
//        }
//        else
//        {
//            int updateRange = this.isAreaLoaded(pos, 18, false) ? 17 : 15;
//            int j2 = 0;
//            int k2 = 0;
//            int light = this.getLightFor(lightType, pos);
//            int orgLight = this.getRawLight(pos, lightType);
//            int x = pos.getX();
//            int y = pos.getY();
//            int z = pos.getZ();
//
//            if (orgLight > light)
//            {
//                this.lightUpdateBlockList[k2++] = 133152;
//            }
//            else if (orgLight < light)
//            {
//                this.lightUpdateBlockList[k2++] = 133152 | light << 18;
//
//                while (j2 < k2)
//                {
//                    int lightPacket = this.lightUpdateBlockList[j2++];
//                    int startX = (lightPacket & 63) - 32 + x;
//                    int startY = (lightPacket >> 6 & 63) - 32 + y;
//                    int startZ = (lightPacket >> 12 & 63) - 32 + z;
//                    int curLight = lightPacket >> 18 & 15;
//                    BlockPos blockpos1 = new BlockPos(startX, startY, startZ);
//                    int startLight = this.getLightFor(lightType, blockpos1);
//
//                    if (startLight == curLight)
//                    {
//                        this.setLightFor(lightType, blockpos1, 0);
//
//                        if (curLight > 0)
//                        {
//                            int aX = net.minecraft.util.math.MathHelper.abs(startX - x);
//                            int aY = net.minecraft.util.math.MathHelper.abs(startY - y);
//                            int aZ = net.minecraft.util.math.MathHelper.abs(startZ - z);
//
//                            if (aX + aY + aZ < updateRange)
//                            {
//                                BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();
//
//                                for (EnumFacing enumfacing : EnumFacing.values())
//                                {
//                                    int sideX = startX + enumfacing.getXOffset();
//                                    int sideY = startY + enumfacing.getYOffset();
//                                    int sideZ = startZ + enumfacing.getZOffset();
//                                    blockpos$pooledmutableblockpos.setPos(sideX, sideY, sideZ);
//                                    IBlockState bs = this.getBlockState(blockpos$pooledmutableblockpos);
//                                    int newLight = Math.max(1, bs.getBlock().getLightOpacity(bs, this, blockpos$pooledmutableblockpos));
//                                    startLight = this.getLightFor(lightType, blockpos$pooledmutableblockpos);
//
//                                    if (startLight == curLight - newLight && k2 < this.lightUpdateBlockList.length)
//                                    {
//                                        this.lightUpdateBlockList[k2++] = sideX - x + 32 | sideY - y + 32 << 6 | sideZ - z + 32 << 12 | curLight - newLight << 18;
//                                    }
//                                }
//
//                                blockpos$pooledmutableblockpos.release();
//                            }
//                        }
//                    }
//                }
//
//                j2 = 0;
//            }
//
//            while (j2 < k2)
//            {
//                int j7 = this.lightUpdateBlockList[j2++];
//                int k7 = (j7 & 63) - 32 + x;
//                int l7 = (j7 >> 6 & 63) - 32 + y;
//                int i8 = (j7 >> 12 & 63) - 32 + z;
//                BlockPos blockpos2 = new BlockPos(k7, l7, i8);
//                int j8 = this.getLightFor(lightType, blockpos2);
//                int k8 = this.getRawLight(blockpos2, lightType);
//
//                if (k8 != j8)
//                {
//                    this.setLightFor(lightType, blockpos2, k8);
//
//                    if (k8 > j8)
//                    {
//                        int l8 = Math.abs(k7 - x);
//                        int i9 = Math.abs(l7 - y);
//                        int j9 = Math.abs(i8 - z);
//                        boolean flag = k2 < this.lightUpdateBlockList.length - 6;
//
//                        if (l8 + i9 + j9 < updateRange && flag)
//                        {
//                            if (this.getLightFor(lightType, blockpos2.west()) < k8)
//                            {
//                                this.lightUpdateBlockList[k2++] = k7 - 1 - x + 32 + (l7 - y + 32 << 6) + (i8 - z + 32 << 12);
//                            }
//
//                            if (this.getLightFor(lightType, blockpos2.east()) < k8)
//                            {
//                                this.lightUpdateBlockList[k2++] = k7 + 1 - x + 32 + (l7 - y + 32 << 6) + (i8 - z + 32 << 12);
//                            }
//
//                            if (this.getLightFor(lightType, blockpos2.down()) < k8)
//                            {
//                                this.lightUpdateBlockList[k2++] = k7 - x + 32 + (l7 - 1 - y + 32 << 6) + (i8 - z + 32 << 12);
//                            }
//
//                            if (this.getLightFor(lightType, blockpos2.up()) < k8)
//                            {
//                                this.lightUpdateBlockList[k2++] = k7 - x + 32 + (l7 + 1 - y + 32 << 6) + (i8 - z + 32 << 12);
//                            }
//
//                            if (this.getLightFor(lightType, blockpos2.north()) < k8)
//                            {
//                                this.lightUpdateBlockList[k2++] = k7 - x + 32 + (l7 - y + 32 << 6) + (i8 - 1 - z + 32 << 12);
//                            }
//
//                            if (this.getLightFor(lightType, blockpos2.south()) < k8)
//                            {
//                                this.lightUpdateBlockList[k2++] = k7 - x + 32 + (l7 - y + 32 << 6) + (i8 + 1 - z + 32 << 12);
//                            }
//                        }
//                    }
//                }
//            }
//
//            return true;
//        }
//    }

    public void setLightFor(int light, BlockPos pos){
        if(isValid(pos)) {
            lightMap[pos.getX()][pos.getY()][pos.getZ()] = light;
        }
    }

    public void setLightFor(int light, double x, double y, double z){
        if(isValid(x,y,z)) {
            lightMap[(int)x][(int)y][(int)z] = light;
        }
    }

    /**
     * Check if the given BlockPos has valid coordinates
     */
    public boolean isValid(BlockPos pos)
    {
        return MathHelper.isInCuboid(pos,Vec3i.NULL_VECTOR,size);
    }

    public boolean isValid(double x,double y,double z){
        return x >= 0 && x < size.getX() &&
                y >= 0 && y < size.getY() &&
                z >= 0 && z < size.getZ() ;
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
}
