package com.nowandfuture.mod.core.common.gui.mygui.compounds.complete;

import com.nowandfuture.mod.core.client.renders.CubesRenderer;
import com.nowandfuture.mod.core.client.renders.ModuleRenderManager;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.View;
import com.nowandfuture.mod.core.prefab.AbstractPrefab;
import com.nowandfuture.mod.core.prefab.BlockRenderHelper;
import com.nowandfuture.mod.core.transformers.animation.KeyFrame;
import com.nowandfuture.mod.utils.DrawHelper;
import com.nowandfuture.mod.utils.MathHelper;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;

public class PreviewView extends View {

    private KeyFrame nextKeyFrame;
    private KeyFrame preKeyFrame;
    private AbstractPrefab prefab;
    private Trackball trackball;

    private Vector3f axisDisplacement;
    //x-y-z
    private float xAngle;
    private float yAngle;
    private float zAngle;

    private float oX,oY,oZ;

    private int lastX,lastY;

    private float x,y,z;

    private BlockRenderHelper blockRenderHelper;

    public PreviewView(@Nonnull RootView rootView) {
        super(rootView);
        axisDisplacement = new Vector3f();
        trackball = new Trackball();
        trackball.tbInit(0);
        xAngle = 0;
        yAngle = 0;
        zAngle = 0;
        oX = 0;
        oY = 0;
        oZ = 8;
        lastX = -1;
        lastY = -1;
    }

    public void setAxisDisplacement(Vector3f axisDisplacement) {
        this.axisDisplacement = axisDisplacement;
    }

    public void setXAngle(float xAngle) {
        this.xAngle = xAngle;
    }

    public void setYAngle(float yAngle) {
        this.yAngle = yAngle;
    }

    public void setZAngle(float zAngle) {
        this.zAngle = zAngle;
    }

    /**
     * set the prefab to draw at this view
     */
    public void setPrefab(AbstractPrefab prefab){
        this.prefab = prefab;
    }

    public AbstractPrefab getPrefab() {
        return prefab;
    }

    public void resetState(){

    }

    @Override
    protected void onLoad() {
        super.onLoad();
        setScissor(false);
        if(checkNotNull()) {
            blockRenderHelper = new BlockRenderHelper(prefab.getLocalWorld());
            blockRenderHelper.init();
        }
    }

    public void reload(){
        clear();
        onLoad();
    }

    @Override
    public void layout(int parentWidth, int parentHeight) {
        super.layout(parentWidth, parentHeight);
        ScaledResolution scaledresolution = new ScaledResolution(getRoot().context);

        trackball.tbReshape(getWidth() * scaledresolution.getScaleFactor(),getHeight() * scaledresolution.getScaleFactor());
    }

    @Override
    protected void onDraw(int mouseX, int mouseY, float partialTicks) {
        drawRect(0,0, getWidth(), getHeight(),DrawHelper.colorInt(0,0,0,255));

    }

    @Override
    protected void onDraw2(int mouseX, int mouseY, float partialTicks) {
        if(checkNotNull()){
            drawModule();
        }
    }

    private boolean checkNotNull(){
        return prefab != null && prefab.isReady();
    }

    @Override
    protected boolean onClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    @Override
    protected boolean onLongClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    protected void saveAsFrame(){

    }

    @Override
    protected boolean onPressed(int mouseX, int mouseY, int state) {
        lastX = mouseX;
        lastY = mouseY;
        trackball.mousePressed(state,mouseX,mouseY);
        return super.onPressed(mouseX, mouseY, state);
    }

    @Override
    protected void onReleased(int mouseX, int mouseY, int state) {
        trackball.mouseReleased(state,mouseX,mouseY);

        x = mouseX;
        y = mouseY;

        x = (2.0f * x - getWidth()) / getWidth();
        y = (getHeight() - 2.0f * y) / getHeight();

        z = trackball.tb_project_to_sphere(trackball.TRACKBALLSIZE,x,y);
        super.onReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean handleMouseInput(int mouseX, int mouseY) {
        float scroll = (float)Mouse.getEventDWheel() / 128f;
        oZ += scroll;
        if(oZ < 1) oZ = 1f;
        trackball.TRACKBALLSIZE = oZ;
        return true;
    }

    @Override
    protected boolean onMousePressedMove(int mouseX, int mouseY, int state) {
        int dX = (mouseX  - lastX);
        int dY = (mouseY  - lastY);

        trackball.mouseMoved(mouseX,mouseY,dX,dY);

        x = mouseX;
        y = mouseY;

        x = (2.0f * x - getWidth()) / getWidth();
        y = (getHeight() - 2.0f * y) / getHeight();

        z = trackball.tb_project_to_sphere(trackball.TRACKBALLSIZE,x,y);

        lastX = mouseX;
        lastY = mouseY;

        return super.onMousePressedMove(mouseX, mouseY, state);
    }

    private void drawModule(){

        //CubesRenderer renderer = ModuleRenderManager.INSTANCE.getRenderer(prefab);

        final ScaledResolution res = new ScaledResolution(getRoot().context);

        final int ax = getAbsoluteX();
        final int ay = getAbsoluteY();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.cullFace(GlStateManager.CullFace.BACK);

        GlStateManager.matrixMode(GL11.GL_PROJECTION);

        GlStateManager.viewport(ax * res.getScaleFactor(),  (getRoot().context.displayHeight - (ay + getHeight())* res.getScaleFactor()) ,
                getWidth() * res.getScaleFactor(), getHeight() * res.getScaleFactor());
        GlStateManager.loadIdentity();
        Project.gluPerspective(60, getWidth()/(float)getHeight(), 0.05F, 256);


        //-------------------------------------------draw----------------------------------------------------

        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();

        Project.gluLookAt(0,0,oZ,0,0, 0,0,1,0);

        GlStateManager.pushMatrix();

        GlStateManager.rotate(trackball.curquat);

        GlStateManager.translate(axisDisplacement.x,axisDisplacement.y,axisDisplacement.z);
        GlStateManager.rotate(zAngle,0,0,1);
        GlStateManager.rotate(yAngle,0,1,0);
        GlStateManager.rotate(xAngle,1,0,0);
        GlStateManager.translate(-axisDisplacement.x,-axisDisplacement.y,-axisDisplacement.z);

        Vector3f p1 = new Vector3f(x, y, z);
        Quaternion inverseQ = MathHelper.inverse(trackball.curquat);
        p1 = MathHelper.mult(p1,inverseQ);

        DrawHelper.drawLine(0,0,0,p1.x,p1.y,p1.z,1,0,0);
        DrawHelper.drawLine(0,0,0,0,0,5,0,1,1,0.5f,3);//z
        DrawHelper.drawLine(0,0,0,0,5,0,0,1,0,0.5f,3);//y
        DrawHelper.drawLine(0,0,0,5,0,0,0,0,1,0.5f,3);//x

        GlStateManager.pushMatrix();
        GlStateManager.disableCull();

        drawString3D("Z",0,0,5,100,100,100,255,new Vector3f(0,1,0));
        drawString3D("Y",0,5,0,100,100,100,255,new Vector3f(0,1,0));
        drawString3D("X",5,0,0,100,100,255,255,new Vector3f(0,1,0));

        GlStateManager.enableCull();
        GlStateManager.popMatrix();

        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager.enableDepth();
        GlStateManager.pushMatrix();

        if(blockRenderHelper != null && blockRenderHelper.isInit()){
            GlStateManager.disableLighting();
            TextureManager textureManager = getRoot().context.renderEngine;
            textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            blockRenderHelper.setLightChanging(true);
            blockRenderHelper.doRender();
        }

        GlStateManager.popMatrix();
        GlStateManager.disableLighting();

        GlStateManager.disableDepth();

        GlStateManager.popMatrix();

      //restore init viewport
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.viewport(0,0,getRoot().context.displayWidth,getRoot().context.displayHeight);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        setupOverlayRendering();
    }

    public void setupOverlayRendering()
    {
        ScaledResolution scaledresolution = new ScaledResolution(getRoot().context);
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
    }

    @Override
    public void clear() {
        if(blockRenderHelper != null)
            blockRenderHelper.clear();
        super.clear();
    }
}
