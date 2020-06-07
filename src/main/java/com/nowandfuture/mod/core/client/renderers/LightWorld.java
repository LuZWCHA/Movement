package com.nowandfuture.mod.core.client.renderers;

import com.nowandfuture.mod.core.common.blocks.TransformedBlock;
import com.nowandfuture.mod.core.common.entities.TileEntityTransformedBlock;
import com.nowandfuture.mod.utils.math.MathHelper;
import com.nowandfuture.mod.utils.math.Quaternion;
import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

//Only for transformed block render
// TODO: 2020/1/28 to render block,not finished
@SideOnly(Side.CLIENT)
public class LightWorld implements IBlockAccess {
    IBlockAccess realWorld;
    private TransformedBlock.BlockWrapper blockWrapper;
    private BlockPos pos;

    public LightWorld(IBlockAccess iBlockAccess, TransformedBlock.BlockWrapper blockWrapper,BlockPos pos){
        this.realWorld = iBlockAccess;
        this.blockWrapper = blockWrapper;
        this.pos = pos;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        if(pos.equals(this.pos))
            return blockWrapper.tileEntity;
        return realWorld.getTileEntity(pos);
    }


    /**
     * @param pos
     * @param lightValue
     * @return
     * @see LightWorld#getBlockState(BlockPos)
     */
    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        double dis = pos.getDistance(this.pos.getX(),this.pos.getY(),this.pos.getZ());
        if(dis <= 1.733 && dis > 0){
            TileEntity tileEntity = realWorld.getTileEntity(this.pos);
            if(tileEntity instanceof TileEntityTransformedBlock){
                Vector3f vector3f = ((TileEntityTransformedBlock) tileEntity).getRotVec();
                if(vector3f.lengthSquared() != 0) {
                    Quaternion quaternion = MathHelper.eulerAnglesToQuaternion(vector3f.x, vector3f.y, vector3f.z);
                    BlockPos ap = pos.subtract(this.pos);

                    Vector3f transPos = MathHelper.mulQuaternion(new Vector3f(ap), quaternion);
                    BlockPos newPos = this.pos.add(transPos.x + 0.5 ,transPos.y + 0.5,transPos.z + 0.5);
                    int light = realWorld.getCombinedLight(newPos,lightValue);

                    return light;
                }
            }
        }
        return realWorld.getCombinedLight(pos, lightValue);
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        if(pos.equals(this.pos))
            return blockWrapper.blockState;

        double dis = pos.getDistance(this.pos.getX(),this.pos.getY(),this.pos.getZ());

        //because of the light-render in the backed consumer(processor)
        //the light packets around the block,for example,the 3 * 3 * 3 - 1 (=26)blocks around this
        //block, that will affect the light values(on 6 faces), so to refine the brightness, we have to
        //get a transformed position based on the BlockPos#pos.
        if(dis <= 1.733 && dis > 0){
            TileEntity tileEntity = realWorld.getTileEntity(this.pos);
            if(tileEntity instanceof TileEntityTransformedBlock){
                Vector3f vector3f = ((TileEntityTransformedBlock) tileEntity).getRotVec();
                if(vector3f.lengthSquared() != 0) {
                    Quaternion quaternion = MathHelper.eulerAnglesToQuaternion(vector3f.x, vector3f.y, vector3f.z);
                    BlockPos ap = pos.subtract(this.pos);

                    Vector3f transPos = MathHelper.mulQuaternion(new Vector3f(ap), quaternion);
                    BlockPos newPos = this.pos.add(transPos.x + 0.5 ,transPos.y + 0.5,transPos.z + 0.5);
                    return realWorld.getBlockState(newPos);
                }
            }
        }
        return realWorld.getBlockState(pos);
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return realWorld.isAirBlock(pos);
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return realWorld.getBiome(pos);
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
