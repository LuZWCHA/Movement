package com.nowandfuture.mod.core.client.renders;

import com.creativemd.creativecore.client.mods.optifine.OptifineHelper;
import com.nowandfuture.asm.IRender;
import com.nowandfuture.mod.core.common.entities.TileEntityTransformedBlock;
import com.nowandfuture.mod.utils.DrawHelper;
import com.nowandfuture.mod.utils.MathHelper;
import com.nowandfuture.mod.utils.math.Quaternion;
import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.optifine.shaders.ShadersRender;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.nowandfuture.mod.utils.MathHelper.quaternionToEulerAngles;

@SideOnly(Side.CLIENT)
public enum  TransformedBlockRenderMap implements IWorldEventListener, IRender {
    INSTANCE;

    private Map<BlockPos, TransformedBlockRender> map;
    private final VertexBufferUploader vertexBufferUploader = new VertexBufferUploader();

    private final Set<TransformedBlockRender>[] transformedBlockRenders;
    private World world = Minecraft.getMinecraft().world;
    private static float DELTA = 1.0f / 2048;
    private boolean hasEdited = false;

    TransformedBlockRenderMap(){
        map = new HashMap<>();
        transformedBlockRenders = new HashSet[BlockRenderLayer.values().length];
        for (BlockRenderLayer blockRenderLayer: BlockRenderLayer.values()
             ) {
            transformedBlockRenders[blockRenderLayer.ordinal()]
                    = new HashSet<>();
        }
        Minecraft.getMinecraft().world.addEventListener(this);
    }

    public VertexBuffer upload(BlockPos pos,BlockRenderLayer layer ,BufferBuilder bufferBuilder){
        if(getBlockRender(pos) != null){
            removeRender(pos);
        }
        TransformedBlockRender blockRender = TransformedBlockRender.newBlockRender(layer);
        vertexBufferUploader.setVertexBuffer(blockRender.vbo);
        vertexBufferUploader.draw(bufferBuilder);
        blockRender.pos = pos;
        map.put(pos,blockRender);
        transformedBlockRenders[layer.ordinal()].add(blockRender);
        return blockRender.vbo;
    }

    public TransformedBlockRender getBlockRender(BlockPos pos){
        return map.get(pos);
    }

    public void removeRender(BlockPos pos){
        TransformedBlockRender blockRender = map.get(pos);
        if(blockRender != null){
            blockRender.vbo.deleteGlBuffers();
            map.remove(pos);
            transformedBlockRenders[blockRender.blockRenderLayer.ordinal()].remove(blockRender);
        }
    }

    public void clear(){
        map.forEach(new BiConsumer<BlockPos, TransformedBlockRender>() {
            @Override
            public void accept(BlockPos pos, TransformedBlockRender transformedBlockRender) {
                transformedBlockRender.vbo.deleteGlBuffers();
            }
        });
        map.clear();
        for (Set list :
                transformedBlockRenders) {
            list.clear();
        }
        hasEdited = false;
        Minecraft.getMinecraft().world.removeEventListener(this);
    }

    @Override
    public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {

    }

    @Override
    public void notifyLightSet(BlockPos pos) {
        map.keySet().removeIf((BlockPos pos1) -> {
            if(pos.distanceSq(pos1) <= 1){
                TransformedBlockRender render = map.get(pos1);
                if(render != null)
                    transformedBlockRenders[render.blockRenderLayer.ordinal()]
                        .remove(render);
                return true;
            }
            return false;
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

    // TODO: 2020/1/29 处理原版所有粒子效果
    @Override
    public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
//        map.keySet().forEach(new Consumer<BlockPos>() {
//            @Override
//            public void accept(BlockPos pos) {
//                TileEntity tileEntity = world.getTileEntity(pos);
//                if(tileEntity instanceof TileEntityTransformedBlock){
//
//                }
//            }
//        });
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


    //--------------------------------------Render Function--------------------------------

    private void renderBlockLayer(BlockRenderLayer blockLayerIn,VertexBuffer vertexBuffer)
    {
        if (OpenGlHelper.useVbo()) {
            GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);
        }

        if(OptifineHelper.isActive() && OptifineHelper.isShaders())
            ShadersRender.preRenderChunkLayer(blockLayerIn);

        this.drawVBO(vertexBuffer);

        if(OptifineHelper.isActive() && OptifineHelper.isShaders())
            ShadersRender.postRenderChunkLayer(blockLayerIn);

        if (OpenGlHelper.useVbo())
        {
            for (VertexFormatElement vertexformatelement : DefaultVertexFormats.BLOCK.getElements())
            {
                VertexFormatElement.EnumUsage usage = vertexformatelement.getUsage();
                int index = vertexformatelement.getIndex();

                switch (usage)
                {
                    case POSITION:
                        GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                        break;
                    case UV:
                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + index);
                        GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                        break;
                    case COLOR:
                        GlStateManager.glDisableClientState(GL11.GL_COLOR_ARRAY);
                        GlStateManager.resetColor();
                }
            }
        }
    }


    private void drawVBO(VertexBuffer vertexBuffer)
    {
        GlStateManager.pushMatrix();

        vertexBuffer.bindBuffer();
        if(OptifineHelper.isActive() && OptifineHelper.isShaders())
            ShadersRender.setupArrayPointersVbo();
        else
            this.setupArrayPointers();
        vertexBuffer.drawArrays(GL11.GL_QUADS);
        GlStateManager.popMatrix();

        OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
        GlStateManager.resetColor();
    }

    private void setupArrayPointers()
    {
        GlStateManager.glVertexPointer(3, 5126, 28, 0);
        GlStateManager.glColorPointer(4, 5121, 28, 12);
        GlStateManager.glTexCoordPointer(2, 5126, 28, 16);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glTexCoordPointer(2, 5122, 28, 24);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    @Override
    public void renderBlockLayer(int pass, double p, BlockRenderLayer blockRenderLayer) {

        final Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        if (entity == null ) {
            return;
        }

        final double renderPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * p;
        final double renderPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * p;
        final double renderPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * p;

        Set<TransformedBlockRender> blockRenders =
                transformedBlockRenders[blockRenderLayer.ordinal()];
        if(blockRenders != null){
            for (TransformedBlockRender tbr :
                    blockRenders) {
                GlStateManager.pushMatrix();
                Minecraft.getMinecraft().entityRenderer.enableLightmap();
                BlockPos pos = tbr.pos;

//                DrawHelper.zFightPre(((pos.getX() & 1) + (pos.getY() & 1) + (pos.getZ() & 1)) - 3,((pos.getX() & 1) + (pos.getY() & 1) + (pos.getZ() & 1)) - 3);

                GlStateManager.translate(
                        pos.getX() - renderPosX,
                        pos.getY() - renderPosY,
                        pos.getZ() - renderPosZ);

                transform(tbr);

                float res = getScaleFactor(pos) * DELTA;
                GlStateManager.scale(1+res,1+res,1+res);

                renderBlockLayer(blockRenderLayer,tbr.vbo);

//                DrawHelper.zFightPost();
                Minecraft.getMinecraft().entityRenderer.disableLightmap();
                GlStateManager.popMatrix();
            }

        }
    }

    private void transform(TransformedBlockRender blockRender){
        TileEntity te;
        if(world != null){
            te = world.getTileEntity(blockRender.pos);
            if(te instanceof TileEntityTransformedBlock) {
                AxisAlignedBB axisAlignedBB = ((TileEntityTransformedBlock) te).getLocalBlock().blockState.getBoundingBox(te.getWorld(), te.getPos());

                Vec3d center = axisAlignedBB.getCenter();

                Vector3f vector3f = ((TileEntityTransformedBlock) te).getRotVec();
                GlStateManager.translate(center.x, center.y, center.z);
                GlStateManager.rotate(vector3f.z, 0, 0, 1);
                GlStateManager.rotate(vector3f.y, 0, 1, 0);
                GlStateManager.rotate(vector3f.x, 1, 0, 0);
                GlStateManager.translate(-center.x, -center.y, -center.z);

            }
        }

    }

    @Override
    public void prepare(float p) {

    }

    @Override
    public boolean isRenderValid() {
        return true;
    }

    public boolean isHasEdited() {
        return hasEdited;
    }

    public void setHasEdited(boolean hasEdited) {
        this.hasEdited = hasEdited;
    }

    private int getScaleFactor(BlockPos pos){
        int res = (pos.getX() & 1) + ((pos.getY() & 1) << 1) + ((pos.getZ() & 1) << 2);
        return res + 1;
    }

    public static class TransformedBlockRender{
        public VertexBuffer vbo;
        public BlockRenderLayer blockRenderLayer;
        public BlockPos pos;

        private TransformedBlockRender(BlockRenderLayer blockRenderLayer){
            this.blockRenderLayer = blockRenderLayer;
            this.vbo = new VertexBuffer(DefaultVertexFormats.BLOCK);
        }

        public static TransformedBlockRender newBlockRender(BlockRenderLayer blockRenderLayer){
            return new TransformedBlockRender(blockRenderLayer);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TransformedBlockRender that = (TransformedBlockRender) o;
            return pos.equals(that.pos);
        }

        @Override
        public int hashCode() {
            return pos.hashCode();
        }
    }
}
