package com.nowandfuture.mod.core.client.renders.tiles;

import com.nowandfuture.asm.Utils;
import com.nowandfuture.mod.core.client.renders.CubesBuilder;
import com.nowandfuture.mod.core.client.renders.LightWorld;
import com.nowandfuture.mod.core.client.renders.TransformedBlockRenderMap;
import com.nowandfuture.mod.core.common.Items.BlockInfoCopyItem;
import com.nowandfuture.mod.core.common.blocks.TransformedBlock;
import com.nowandfuture.mod.core.common.entities.TileEntityTransformedBlock;
import com.nowandfuture.mod.utils.DrawHelper;
import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.model.animation.FastTESR;
import org.lwjgl.opengl.GL11;

public class TileEntityTransformedBlockRenderer extends FastTESR<TileEntityTransformedBlock> {

    private BufferBuilder bufferBuilder = new BufferBuilder(2097152);

    @Override
    public void renderTileEntityFast(TileEntityTransformedBlock te, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {
        TransformedBlockRenderMap.INSTANCE.checkShader();
        if(!getWorld().isBlockLoaded(te.getPos()) || te.isInvalid() || (Utils.mapCache != null &&
                !Utils.mapCache.containsKey(
                        CubesBuilder.transferToRenderChunkPos(te.getPos().getX(),
                                te.getPos().getY(),te.getPos().getZ())))
        ) {

            TransformedBlockRenderMap.INSTANCE.removeRender(te.getPos());
            return;
        }

        TransformedBlock.BlockWrapper wrapper = te.getLocalBlock();
        TileEntity tileEntityWrapper = null;

        GlStateManager.pushMatrix();

        if(wrapper != null) {
            tileEntityWrapper = wrapper.tileEntity;

            if(Minecraft.getMinecraft().player.getHeldItemMainhand().getItem() instanceof BlockInfoCopyItem){
                DrawHelper.preDraw();

                setLightmapDisabled(true);
                GlStateManager.enableDepth();
                GlStateManager.depthMask(false);
                RenderHelper.disableStandardItemLighting();
                DrawHelper.drawOutlinedBoundingBox(getWorld().getBlockState(te.getPos())
                        .getBoundingBox(te.getWorld(),te.getPos()).offset(x,y,z).grow(0.002),1,0,0,1);
                RenderHelper.enableStandardItemLighting();
                setLightmapDisabled(false);
                DrawHelper.postDraw();
            }

            this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            TransformedBlockRenderMap.TransformedBlockRender blockRender =
                    TransformedBlockRenderMap.INSTANCE.getBlockRender(te.getPos());

            if(blockRender == null || te.isUpdateVBO()) {

                BlockRenderLayer blockRenderLayer =
                        te.getLocalBlock().blockState.getBlock().getRenderLayer();

                blockRender = TransformedBlockRenderMap.TransformedBlockRender
                        .newBlockRender(blockRenderLayer);


                bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
                renderBlock(te, wrapper, bufferBuilder);


                float ex = (float)this.rendererDispatcher.entityX;
                float ey = (float)this.rendererDispatcher.entityY +
                        rendererDispatcher.entity.getEyeHeight();
                float ez = (float)this.rendererDispatcher.entityZ;

                bufferBuilder.finishDrawing();

                blockRender.vbo = TransformedBlockRenderMap.INSTANCE
                        .upload(te.getPos(),blockRenderLayer, bufferBuilder);
                te.setUpdateVBO(false);
            }

        }
        GlStateManager.pushMatrix();

        GlStateManager.translate(x,y,z);
        // TODO: 2020/1/26 some tileEntities need to skip transform in some direction-axises
        transform(te,true,!(tileEntityWrapper instanceof TileEntityEnchantmentTable),true);
        GlStateManager.translate(-x,-y,-z);

        if(tileEntityWrapper != null){
            rendererDispatcher.render(tileEntityWrapper,partialTicks,destroyStage);
        }
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    public void renderBlock(TileEntityTransformedBlock te, TransformedBlock.BlockWrapper wrapper, BufferBuilder bufferBuilder){
        GlStateManager.resetColor();

        BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
        BlockPos pos = te.getPos();
        bufferBuilder.setTranslation(-pos.getX(),-pos.getY(),-pos.getZ());
        renderBlock(blockRenderer,te.getLocalBlock().blockState,pos,
                new LightWorld(te.getWorld(),wrapper,te.getPos()),bufferBuilder);
    }


    public boolean renderBlock(BlockRendererDispatcher dispatcher,IBlockState state, BlockPos pos, IBlockAccess blockAccess, BufferBuilder bufferBuilderIn)
    {
        try
        {
            EnumBlockRenderType enumblockrendertype = state.getRenderType();

            if (enumblockrendertype == EnumBlockRenderType.INVISIBLE)
            {
                return false;
            }
            else
            {
                if (blockAccess.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES)
                {
                    try
                    {
                        state = state.getActualState(blockAccess, pos);
                    }
                    catch (Exception var8)
                    {
                        ;
                    }
                }

                switch (enumblockrendertype)
                {
                    case MODEL:
                        IBakedModel model = dispatcher.getModelForState(state);
                        state = state.getBlock().getExtendedState(state, blockAccess, pos);
                        return dispatcher.getBlockModelRenderer().renderModel(blockAccess, model, state, pos, bufferBuilderIn, false);
                    case ENTITYBLOCK_ANIMATED:
                        return false;
                    case LIQUID:
                        return dispatcher.renderBlock(state, pos, blockAccess, bufferBuilderIn);
                    default:
                        return false;
                }
            }
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Tesselating block in world");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being tesselated");
            CrashReportCategory.addBlockInfo(crashreportcategory, pos, state.getBlock(), state.getBlock().getMetaFromState(state));
            throw new ReportedException(crashreport);
        }
    }

    private void transform(TileEntityTransformedBlock te,boolean enableX,boolean enableY,boolean enableZ){
        AxisAlignedBB axisAlignedBB = te.getLocalBlock().blockState.getBoundingBox(te.getWorld(),te.getPos());

        Vec3d center = axisAlignedBB.getCenter();
        Vector3f vector3f = te.getRotVec();
        GlStateManager.translate(center.x, center.y, center.z);
        GlStateManager.rotate(vector3f.z, 0, 0, 1);
        GlStateManager.rotate(vector3f.y, 0, 1, 0);
        GlStateManager.rotate(vector3f.x, 1, 0, 0);
        GlStateManager.translate(-center.x, -center.y, -center.z);

        if(te.isEdited()) {
            setLightmapDisabled(true);
            GlStateManager.pushMatrix();
            GlStateManager.translate(center.x, center.y, center.z);
            DrawHelper.drawCoordinateAxis();
            GlStateManager.popMatrix();
            setLightmapDisabled(false);
        }
    }



}
