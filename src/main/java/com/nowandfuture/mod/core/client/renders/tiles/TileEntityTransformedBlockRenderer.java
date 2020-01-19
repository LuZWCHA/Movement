package com.nowandfuture.mod.core.client.renders.tiles;

import com.nowandfuture.mod.core.common.blocks.TransformedBlock;
import com.nowandfuture.mod.core.common.entities.TileEntityTransformedBlock;
import com.nowandfuture.mod.core.prefab.BlockRenderHelper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.animation.FastTESR;
import org.lwjgl.opengl.GL11;

public class TileEntityTransformedBlockRenderer extends FastTESR<TileEntityTransformedBlock> {

    TileEntity tileEntityWrapper;

    @Override
    public void renderTileEntityFast(TileEntityTransformedBlock te, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {
        TransformedBlock.BlockWrapper wrapper = te.getLocalBlock();

        BlockPos blockPos = new BlockPos(rendererDispatcher.entityX,rendererDispatcher.entityY,rendererDispatcher.entityZ);

        BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

        GlStateManager.pushMatrix();

        GlStateManager.rotate(1.7f,5f,.5f,.5f);

        if(wrapper != null) {
            tileEntityWrapper = wrapper.tileEntity;
            dispatcher.renderBlock(wrapper.blockState,new BlockPos(x,y,z),getWorld(),buffer);
        }else{
            dispatcher.renderBlock(Blocks.SAND.getDefaultState(),new BlockPos(x,y,z),getWorld(),buffer);
        }

        if(tileEntityWrapper != null){
            rendererDispatcher.render(tileEntityWrapper,x,y,z,partialTicks);
        }

        GlStateManager.popMatrix();
    }
}
