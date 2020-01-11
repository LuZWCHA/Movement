package com.nowandfuture.mod.utils;

import com.nowandfuture.mod.core.selection.AABBSelectArea;
import com.nowandfuture.mod.core.selection.OBBox;
import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.List;

public class DrawHelper {

    public static class Cuboid{
        public Vector3f x;
        public Vector3f y;
        public Vector3f z;
        public Vector3f origin;
    }

    public static void render(AABBSelectArea selectArea, float r, float b, float g){
            if(selectArea.isShow()) {
                GlStateManager.enablePolygonOffset();
                GlStateManager.doPolygonOffset(-1, -1);
                DrawHelper.drawCube(selectArea.getBox(), r, b, g);
                GlStateManager.disablePolygonOffset();
            }
        }

    public static void drawBuffer(BufferBuilder bufferBuilderIn){
        if (bufferBuilderIn.getVertexCount() > 0)
        {
            VertexFormat vertexformat = bufferBuilderIn.getVertexFormat();
            int i = vertexformat.getNextOffset();
            ByteBuffer bytebuffer = bufferBuilderIn.getByteBuffer();
            List<VertexFormatElement> list = vertexformat.getElements();

            for (int j = 0; j < list.size(); ++j)
            {
                VertexFormatElement vertexformatelement = list.get(j);
                VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();
                //int k = vertexformatelement.getType().getGlConstant();
                //int l = vertexformatelement.getIndex();
                bytebuffer.position(vertexformat.getOffset(j));

                // moved to VertexFormatElement.preDraw
                vertexformatelement.getUsage().preDraw(vertexformat, j, i, bytebuffer);
            }

            GlStateManager.glDrawArrays(bufferBuilderIn.getDrawMode(), 0, bufferBuilderIn.getVertexCount());
            int i1 = 0;

            for (int j1 = list.size(); i1 < j1; ++i1)
            {
                VertexFormatElement vertexformatelement1 = list.get(i1);
                VertexFormatElement.EnumUsage vertexformatelement$enumusage1 = vertexformatelement1.getUsage();
                //int k1 = vertexformatelement1.getIndex();

                // moved to VertexFormatElement.postDraw
                vertexformatelement1.getUsage().postDraw(vertexformat, i1, i, bytebuffer);
            }
        }

        bufferBuilderIn.reset();
    }

    public static void render(TileEntity tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        TileEntitySpecialRenderer<TileEntity> tileentityspecialrenderer = TileEntityRendererDispatcher.instance.getRenderer(tileEntityIn);

        if (tileentityspecialrenderer != null)
        {
            try
            {
                tileentityspecialrenderer.render(tileEntityIn, x, y, z, partialTicks, destroyStage, alpha);
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering Block Entity");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Block Entity Details");
                tileEntityIn.addInfoToCrashReport(crashreportcategory);
                throw new ReportedException(crashreport);
            }
        }
    }

    public static void drawCube(AxisAlignedBB bb, float r, float b, float g)
    {
        GlStateManager.pushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(1F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
//        GL11.glColor4d(r, b, g, 0.04F);
//        drawBoundingBox(bb);
        GL11.glColor4d(r, b, g, 1.0F);
        drawOutlinedBoundingBox(bb);
        GL11.glLineWidth(2.0F);

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GlStateManager.popMatrix();
    }

    public static void drawLine(float sx,float sy,float sz,float ex,float ey,float ez,float r, float b, float g,float a,float width){
        GlStateManager.pushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(width);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        GL11.glColor4d(r, b, g, a);
        GL11.glLineWidth(2.0F);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(GL11.GL_LINES,DefaultVertexFormats.POSITION);
        bufferBuilder.pos(sx,sy,sz).endVertex();
        bufferBuilder.pos(ex,ey,ez).endVertex();
        Tessellator.getInstance().draw();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GlStateManager.popMatrix();
    }

    public static void drawLine(float sx,float sy,float sz,float ex,float ey,float ez,float r, float b, float g){
        GlStateManager.pushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(1F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        GL11.glColor4d(r, b, g, 1.0F);
        GL11.glLineWidth(2.0F);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(GL11.GL_LINES,DefaultVertexFormats.POSITION);
        bufferBuilder.pos(sx,sy,sz).endVertex();
        bufferBuilder.pos(ex,ey,ez).endVertex();
        Tessellator.getInstance().draw();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GlStateManager.popMatrix();
    }

    public static void drawBoundingBox(AxisAlignedBB axisalignedbb)
    {

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        Tessellator tessellatorr = Tessellator.getInstance();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        bufferBuilder.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
        bufferBuilder.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
        bufferBuilder.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
        bufferBuilder.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
        bufferBuilder.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
        bufferBuilder.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
        bufferBuilder.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
        bufferBuilder.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
        tessellatorr.draw();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        bufferBuilder.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
        bufferBuilder.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
        bufferBuilder.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
        bufferBuilder.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
        bufferBuilder.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
        bufferBuilder.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
        bufferBuilder.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
        bufferBuilder.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
        tessellatorr.draw();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        bufferBuilder.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
        bufferBuilder.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
        bufferBuilder.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
        bufferBuilder.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
        bufferBuilder.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
        bufferBuilder.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
        bufferBuilder.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
        bufferBuilder.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
        tessellatorr.draw();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        bufferBuilder.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
        bufferBuilder.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
        bufferBuilder.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
        bufferBuilder.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
        bufferBuilder.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
        bufferBuilder.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
        bufferBuilder.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
        bufferBuilder.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
        tessellatorr.draw();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        bufferBuilder.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
        bufferBuilder.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
        bufferBuilder.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
        bufferBuilder.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
        bufferBuilder.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
        bufferBuilder.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
        bufferBuilder.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
        bufferBuilder.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
        tessellatorr.draw();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        bufferBuilder.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
        bufferBuilder.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
        bufferBuilder.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
        bufferBuilder.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
        bufferBuilder.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).endVertex();
        bufferBuilder.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).endVertex();
        bufferBuilder.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).endVertex();
        bufferBuilder.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).endVertex();
        tessellatorr.draw();
    }

    public static void drawOutlinedBoundingBox(AxisAlignedBB par1AxisAlignedBB)
    {
        BufferBuilder var2 = Tessellator.getInstance().getBuffer();
        Tessellator tessellator = Tessellator.getInstance();

        var2.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
        var2.pos(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ).endVertex();
        var2.pos(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ).endVertex();
        var2.pos(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ).endVertex();
        var2.pos(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ).endVertex();
        var2.pos(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ).endVertex();

        var2.pos(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ).endVertex();
        var2.pos(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ).endVertex();
        var2.pos(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ).endVertex();
        var2.pos(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ).endVertex();
        var2.pos(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ).endVertex();
        tessellator.draw();

        var2.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        var2.pos(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ).endVertex();
        var2.pos(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ).endVertex();

        var2.pos(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ).endVertex();
        var2.pos(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ).endVertex();

        var2.pos(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ).endVertex();
        var2.pos(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ).endVertex();

        var2.pos(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ).endVertex();
        var2.pos(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ).endVertex();
        tessellator.draw();
    }

    public static void preDraw(){
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GlStateManager.disableLighting();
    }

    public static void postDraw(){
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
    }

    public static void drawFace(OBBox.Facing facing){
        preDraw();
        GlStateManager.color(1.0F, 0.0F, 0.0F, 1.0F);

        BufferBuilder var2 = Tessellator.getInstance().getBuffer();
        Tessellator tessellator = Tessellator.getInstance();

        var2.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
        var2.pos(facing.getV0().getX(), facing.getV0().getY(), facing.getV0().getZ()).endVertex();
        var2.pos(facing.getV1().getX(), facing.getV1().getY(), facing.getV1().getZ()).endVertex();
        var2.pos(facing.getV2().getX(), facing.getV2().getY(), facing.getV2().getZ()).endVertex();
        var2.pos(facing.getV3().getX(), facing.getV3().getY(), facing.getV3().getZ()).endVertex();
        var2.pos(facing.getV0().getX(), facing.getV0().getY(), facing.getV0().getZ()).endVertex();
        tessellator.draw();

        GlStateManager.color(0.0F, 0.0F, 0.0F, 1.0F);
        var2.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        Vector3f center = facing.getCenter();
        Vector3f end = Vector3f.add(center, (Vector3f) facing.getDirectionVector().normalise(),new Vector3f());

        var2.pos(center.getX(),center.getY(),center.getZ()).endVertex();
        var2.pos(end.getX(), end.getY(), end.getZ()).endVertex();
        tessellator.draw();

        postDraw();
    }

    public static void drawCoordinateAxis()
    {
        preDraw();
        BufferBuilder var2 = Tessellator.getInstance().getBuffer();
        Tessellator tessellator = Tessellator.getInstance();

        GlStateManager.color(1,0,0);
        var2.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        var2.pos(0, 0, 0).endVertex();
        var2.pos(0, 5, 0).endVertex();
        var2.pos(0,5, 0).endVertex();
        var2.pos(0, 0, 0.1).endVertex();
        var2.pos(0,0,0).endVertex();

        var2.pos(0, 0, 0).endVertex();
        var2.pos(0, 0, 0.1).endVertex();
        var2.pos(0,5, 0.1).endVertex();
        var2.pos(0, 5, 0).endVertex();
        var2.pos(0,0,0).endVertex();
        tessellator.draw();

        GlStateManager.color(0,1,0);
        var2.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        var2.pos(0, 0, 0).endVertex();
        var2.pos(0, 0, 5).endVertex();
        var2.pos(0,0, 5).endVertex();
        var2.pos(0,0.1, 0).endVertex();
        var2.pos(0,0,0).endVertex();

        var2.pos(0, 0, 0).endVertex();
        var2.pos(0,0.1, 0).endVertex();
        var2.pos(0,0.1, 5).endVertex();
        var2.pos(0, 0, 5).endVertex();
        var2.pos(0,0,0).endVertex();
        tessellator.draw();

        GlStateManager.color(0,0,1);
        var2.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        var2.pos(0, 0, 0).endVertex();
        var2.pos(5, 0, 0).endVertex();
        var2.pos(5,0, 0).endVertex();
        var2.pos(0,0.1, 0).endVertex();
        var2.pos(0,0,0).endVertex();

        var2.pos(0, 0, 0).endVertex();
        var2.pos(0,0.1, 0).endVertex();
        var2.pos(5,0.1, 0).endVertex();
        var2.pos(5, 0, 0).endVertex();
        var2.pos(0,0,0).endVertex();
        tessellator.draw();

        postDraw();
    }

    public static void drawOutlinedBoundingBox(OBBox bounding)
    {
        preDraw();
        BufferBuilder var2 = Tessellator.getInstance().getBuffer();
        Tessellator tessellator = Tessellator.getInstance();

        var2.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
        var2.pos(bounding.getXyz000().getX(), bounding.getXyz000().getY(), bounding.getXyz000().getZ()).endVertex();
        var2.pos(bounding.getXyz001().getX(), bounding.getXyz001().getY(), bounding.getXyz001().getZ()).endVertex();
        var2.pos(bounding.getXyz011().getX(), bounding.getXyz011().getY(), bounding.getXyz011().getZ()).endVertex();
        var2.pos(bounding.getXyz010().getX(), bounding.getXyz010().getY(), bounding.getXyz010().getZ()).endVertex();
        var2.pos(bounding.getXyz000().getX(), bounding.getXyz000().getY(), bounding.getXyz000().getZ()).endVertex();

        var2.pos(bounding.getXyz100().getX(), bounding.getXyz100().getY(), bounding.getXyz100().getZ()).endVertex();
        var2.pos(bounding.getXyz101().getX(), bounding.getXyz101().getY(), bounding.getXyz101().getZ()).endVertex();
        var2.pos(bounding.getXyz111().getX(), bounding.getXyz111().getY(), bounding.getXyz111().getZ()).endVertex();
        var2.pos(bounding.getXyz110().getX(), bounding.getXyz110().getY(), bounding.getXyz110().getZ()).endVertex();
        var2.pos(bounding.getXyz100().getX(), bounding.getXyz100().getY(), bounding.getXyz100().getZ()).endVertex();
        tessellator.draw();

        var2.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        var2.pos(bounding.getXyz000().getX(), bounding.getXyz000().getY(), bounding.getXyz000().getZ()).endVertex();
        var2.pos(bounding.getXyz100().getX(), bounding.getXyz100().getY(), bounding.getXyz100().getZ()).endVertex();

        var2.pos(bounding.getXyz001().getX(), bounding.getXyz001().getY(), bounding.getXyz001().getZ()).endVertex();
        var2.pos(bounding.getXyz101().getX(), bounding.getXyz101().getY(), bounding.getXyz101().getZ()).endVertex();

        var2.pos(bounding.getXyz011().getX(), bounding.getXyz011().getY(), bounding.getXyz011().getZ()).endVertex();
        var2.pos(bounding.getXyz111().getX(), bounding.getXyz111().getY(), bounding.getXyz111().getZ()).endVertex();

        var2.pos(bounding.getXyz010().getX(), bounding.getXyz010().getY(), bounding.getXyz010().getZ()).endVertex();
        var2.pos(bounding.getXyz110().getX(), bounding.getXyz110().getY(), bounding.getXyz110().getZ()).endVertex();
        tessellator.draw();

        postDraw();
    }


    public static void renderOffsetAABB(AxisAlignedBB boundingBox, double x, double y, double z)
    {
        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        bufferbuilder.setTranslation(x, y, z);
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        tessellator.draw();
        bufferbuilder.setTranslation(0.0D, 0.0D, 0.0D);
        GlStateManager.enableTexture2D();
    }

    //-------------------------------------------GUI draw----------------------------------------------

    //draw texture not in a square region,for
    //vanilla width and height is default 256
    public static void drawTexturedModalRect(int x, int y,float zLevel, int u, int v, int maxU, int maxV, int textureWidth, int textureHeight)
    {
        double f = 1f/(float)(textureWidth);
        double f1 = 1f/(float)(textureHeight);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL11.GL_QUADS,DefaultVertexFormats.POSITION_TEX);
        bufferBuilder.pos((double)x, (double)(y + maxV), (double)zLevel).tex(u*f, maxV*f1).endVertex();
        bufferBuilder.pos((double)(x + maxU), (double)(y + maxV), (double)zLevel).tex(maxU*f, maxV*f1).endVertex();
        bufferBuilder.pos((double)(x + maxU), (double)y, (double)zLevel).tex(maxU*f, v*f1).endVertex();
        bufferBuilder.pos((double)x, (double)y, (double)zLevel).tex(u*f, v*f1).endVertex();
        tessellator.draw();
    }

    public static void drawRhombus(int left, int top, int right, int bottom, int color)
    {
        if (left < right)
        {
            int i = left;
            left = right;
            right = i;
        }

        if (top < bottom)
        {
            int j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(f, f1, f2, f3);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos((double)left, (double)(bottom + top) / 2, 0.0D).endVertex();
        bufferbuilder.pos((double)(right + left) / 2, (double)bottom, 0.0D).endVertex();
        bufferbuilder.pos((double)right, (double)(bottom + top) / 2, 0.0D).endVertex();
        bufferbuilder.pos((double)(right + left) / 2, (double)top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static int colorInt(int r,int g,int b,int a){
        a = (a & 255) << 24;
        r = (r & 255) << 16;
        g = (g & 255) << 8;
        b &= 255;
        return a | r | g | b;
    }
}
