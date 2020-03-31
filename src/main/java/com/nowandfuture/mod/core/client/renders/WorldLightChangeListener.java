package com.nowandfuture.mod.core.client.renders;

import com.nowandfuture.mod.core.prefab.AbstractPrefab;
import com.nowandfuture.mod.core.prefab.LocalWorld;
import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class WorldLightChangeListener implements IWorldEventListener {

    private CubesRenderer cubesRenderer;

    public WorldLightChangeListener(CubesRenderer cubesRenderer) {
        this.cubesRenderer = cubesRenderer;
    }

    @Override
    public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {

    }

    // TODO: 2019/8/7 not finished
    @Override
    public void notifyLightSet(BlockPos pos) {
        AbstractPrefab prefab = cubesRenderer.getPrefab();
        //not transformed pos
        BlockPos localPos = pos.subtract(prefab.getBasePos());
        cubesRenderer.getCubeByCubeInPos(CubesBuilder.getVisitorCubePos(cubesRenderer,localPos))
                .ifPresent(new Consumer<RenderCube>() {
                    @Override
                    public void accept(RenderCube renderCube) {
                        LocalWorld world = renderCube.getWorld();
                        World actWorld = world.getParentWorld();

                        int light = actWorld.getLightFor(EnumSkyBlock.BLOCK,pos);
                        Vector3f actLocalPos = CubesBuilder.getLocalPos(localPos, cubesRenderer);
                        world.setLightFor(light,actLocalPos.getX(),actLocalPos.getY(),actLocalPos.getZ());
                        renderCube.markUpdate(true);
                    }
                });
    }

    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {

    }

    @Override
    public void playSoundToAllNearExcept(@Nullable EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) {

    }

    @Override
    public void playRecord(SoundEvent soundIn, BlockPos pos) {

    }

    @Override
    public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {

    }

    @Override
    public void spawnParticle(int id, boolean ignoreRange, boolean p_190570_3_, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... parameters) {

    }

    @Override
    public void onEntityAdded(Entity entityIn) {

    }

    @Override
    public void onEntityRemoved(Entity entityIn) {

    }

    @Override
    public void broadcastSound(int soundID, BlockPos pos, int data) {

    }

    @Override
    public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {

    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {

    }
}
