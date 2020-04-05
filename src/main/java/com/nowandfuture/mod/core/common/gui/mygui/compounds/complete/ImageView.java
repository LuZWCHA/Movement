package com.nowandfuture.mod.core.common.gui.mygui.compounds.complete;

import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.View;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.ViewGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ImageView extends View {
    private ResourceLocation location;

    private boolean initImageInfo;
    private BufferedImage image;

    public ImageView(@Nonnull RootView rootView) {
        super(rootView);
    }

    public ImageView(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
    }

    @Override
    protected void onLoad() {
        if(!initImageInfo) {
            IResource iresource;
            try {
                iresource = Minecraft.getMinecraft().getResourceManager().getResource(location);
                image = TextureUtil.readBufferedImage(iresource.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            initImageInfo = true;
        }
    }

    @Override
    protected void onDraw(int mouseX, int mouseY, float partialTicks) {
        if(location != null && image != null) {
            GlStateManager.color(1,1,1,1);
            getRoot().context.getTextureManager().bindTexture(location);
            int aw = image.getWidth(),ah = image.getHeight();
            int imageWidth = getWidth() - padRight - padLeft,imageHeight = getHeight() - padTop - padBottom;
            int padLeft = this.padLeft,padTop = this.padTop;
            if(imageWidth > aw && (padLeft != 0 || padRight != 0)) {
                padLeft = (imageWidth - aw) / 2 + this.padLeft;
                imageWidth = aw;
            }
            if(imageHeight > ah && (padTop != 0 || padBottom != 0)) {
                padTop = (imageHeight - ah) / 2 + this.padTop;
                imageHeight = ah;
            }
            drawModalRectWithCustomSizedTexture(padLeft, padTop, 0, 0, imageWidth, imageHeight,imageWidth,imageHeight);
        }
    }

    @Override
    protected boolean onClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    @Override
    protected boolean onLongClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    public void setImageLocation(ResourceLocation location){
        this.location = location;
    }
}
