package com.nowandfuture.mod.core.common.gui.custom;

import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.View;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.ViewGroup;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.Trackball;
import com.nowandfuture.mod.core.prefab.AbstractPrefab;
import com.nowandfuture.mod.core.prefab.BlockRenderHelper;
import com.nowandfuture.mod.core.transformers.RotationTransformNode;
import com.nowandfuture.mod.utils.DrawHelper;
import com.nowandfuture.mod.utils.math.MathHelper;
import com.nowandfuture.mod.utils.math.Quaternion;
import com.nowandfuture.mod.utils.math.Vector3f;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PreviewView extends View {

    private RotationTransformNode.RotationKeyFrame nextKeyFrame;
    private RotationTransformNode.RotationKeyFrame preKeyFrame;

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
    private boolean isPressed;

    public PreviewView(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView,parent);
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

    public void setOffsetX(int x) {
        this.axisDisplacement.x = x;
    }

    public void setOffsetY(int y) {
        this.axisDisplacement.y = y;
    }

    public void setOffsetZ(int z) {
        this.axisDisplacement.z = z;
    }

    public void saveToFrame(){
        if(nextKeyFrame == null) nextKeyFrame = new RotationTransformNode.RotationKeyFrame();
        nextKeyFrame.center = new BlockPos(axisDisplacement.x,axisDisplacement.y,axisDisplacement.z);
        nextKeyFrame.rotX = xAngle;
        nextKeyFrame.rotY = yAngle;
        nextKeyFrame.rotZ = zAngle;
    }

    public float getXAngle() {
        return xAngle;
    }

    public float getYAngle() {
        return yAngle;
    }

    public float getZAngle() {
        return zAngle;
    }

    public float getRelativeXAngle() {
        return xAngle + preKeyFrame.rotX;
    }

    public float getRelativeYAngle() {
        return yAngle + preKeyFrame.rotY;
    }

    public float getRelativeZAngle() {
        return zAngle + preKeyFrame.rotZ;
    }

    public void setRelativeXAngle(float xAngle) {
        this.xAngle = preKeyFrame.rotX + xAngle;
    }

    public void setRelativeYAngle(float yAngle) {
        this.yAngle = preKeyFrame.rotY + yAngle;
    }

    public void setRelativeZAngle(float zAngle) {
        this.zAngle = preKeyFrame.rotZ + zAngle;
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

    public void setPreKeyFrame(RotationTransformNode.RotationKeyFrame preKeyFrame) {
        this.preKeyFrame = preKeyFrame;
    }

    public void setNextKeyFrame(RotationTransformNode.RotationKeyFrame nextKeyFrame) {
        this.nextKeyFrame = nextKeyFrame;
        resetModel(nextKeyFrame);
    }

    /**
     * set the prefab to draw at this view
     */
    public void setPrefab(@Nullable AbstractPrefab prefab){
        if(this.prefab != prefab && prefab != null) {//change one to anther
            clearPreview();
            this.prefab = prefab;
            reload();
        }else if(this.prefab != null && prefab == null){//one to empty
            clearPreview();
            this.prefab = null;
        } else{
            this.prefab = prefab;
        }
    }

    public AbstractPrefab getPrefab() {
        return prefab;
    }

    public void resetModel(RotationTransformNode.RotationKeyFrame keyFrame){
        xAngle = keyFrame.rotX;
        yAngle = keyFrame.rotY;
        zAngle = keyFrame.rotZ;
        axisDisplacement = new Vector3f(keyFrame.center.getX(),keyFrame.center.getY(),keyFrame.center.getZ());
    }

    public void resetView(){
        trackball.curquat.setIdentity();
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        setScissor(false);
        isPressed = false;
        reload();
    }

    public void reload(){
        if(checkNotNull()) {
            blockRenderHelper = new BlockRenderHelper(prefab.getLocalWorld());
            blockRenderHelper.init();

            if(preKeyFrame == null){
                preKeyFrame = new RotationTransformNode.RotationKeyFrame();
            }
            resetModel(preKeyFrame);
        }
    }

    @Override
    public void layout(int parentWidth, int parentHeight) {
        super.layout(parentWidth, parentHeight);
        ScaledResolution scaledresolution = new ScaledResolution(getRoot().context);

        trackball.tbReshape(getWidth() * scaledresolution.getScaleFactor(),getHeight() * scaledresolution.getScaleFactor());
    }

    @Override
    protected void onDraw(int mouseX, int mouseY, float partialTicks) {
        drawRect(0,0, getWidth(), getHeight(),DrawHelper.colorInt(128,128,128,128));
    }

    @Override
    protected void onDrawAtScreenCoordinate(int mouseX, int mouseY, float partialTicks) {
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
        nextKeyFrame = new RotationTransformNode.RotationKeyFrame();
        nextKeyFrame.rotX = xAngle;
        nextKeyFrame.rotY = yAngle;
        nextKeyFrame.rotZ = zAngle;
        nextKeyFrame.center = new BlockPos(axisDisplacement.x,axisDisplacement.y,axisDisplacement.z);
    }

    @Override
    protected boolean onPressed(int mouseX, int mouseY, int state) {
        lastX = mouseX;
        lastY = mouseY;
        trackball.mousePressed(state,mouseX,mouseY);

        isPressed = true;
        return super.onPressed(mouseX, mouseY, state);
    }

    @Override
    protected void onReleased(int mouseX, int mouseY, int state) {
        trackball.mouseReleased(state,mouseX,mouseY);

        x = mouseX;
        y = mouseY;

        x = (2.0f * x - getWidth()) / getWidth();
        y = (getHeight() - 2.0f * y) / getHeight();

        z = trackball.projectToSphere(trackball.TRACKBALLSIZE,x,y);

        isPressed = false;
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

        z = trackball.projectToSphere(trackball.TRACKBALLSIZE,x,y);

        lastX = mouseX;
        lastY = mouseY;

        isPressed = true;
        return super.onMousePressedMove(mouseX, mouseY, state);
    }

    private void drawModule(){

        final ScaledResolution res = new ScaledResolution(getRoot().context);

        final int ax = getAbsoluteX();
        final int ay = getAbsoluteY();

        GlStateManager.matrixMode(GL11.GL_PROJECTION);

        GlStateManager.viewport(ax * res.getScaleFactor(),  (getRoot().context.displayHeight - (ay + getHeight())* res.getScaleFactor()) ,
                getWidth() * res.getScaleFactor(), getHeight() * res.getScaleFactor());
        GlStateManager.loadIdentity();
        Project.gluPerspective(60, getWidth()/(float)getHeight(), 0.05F, 256);


        //---------------------------------------------draw----------------------------------------------------

        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();

        Project.gluLookAt(0,0,oZ,0,0, 0,0,1,0);

        GlStateManager.pushMatrix();

        GlStateManager.rotate(new org.lwjgl.util.vector.Quaternion(trackball.curquat.x,
                trackball.curquat.y,trackball.curquat.z,trackball.curquat.w));

        Vector3f p1 = new Vector3f(x, y, z);
        Quaternion inverseQ = MathHelper.inverse(trackball.curquat);
        p1 = MathHelper.mult(p1,inverseQ);

        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager.enableDepth();

        if(isPressed)
            DrawHelper.drawLine(0,0,0,p1.x,p1.y,p1.z,0.5f,0,0);
        DrawHelper.drawLine(0,0,0,0,0,5,1,1,1,0.6f,4);//z
        DrawHelper.drawLine(0,0,0,0,5,0,1,1,1,0.6f,4);//y
        DrawHelper.drawLine(0,0,0,5,0,0,1,1,1,0.6f,4);//x

        GlStateManager.pushMatrix();
        GlStateManager.disableCull();

        drawString3D("Z",0,0,5,255,255,255,200,new Vector3f(0,1,0));
        drawString3D("Y",0,5,0,255,255,255,200,new Vector3f(0,1,0));
        drawString3D("X",5,0,0,255,255,255,200,new Vector3f(0,1,0));

        GlStateManager.enableCull();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();

        GlStateManager.translate(axisDisplacement.x + .5,axisDisplacement.y + .5,axisDisplacement.z + .5);
        DrawHelper.drawLine(0,0,-5,0,0,5,1,0,0,0.8f,3);//z
        DrawHelper.drawLine(0,-5,0,0,5,0,0,1,0,0.8f,3);//y
        DrawHelper.drawLine(-5,0,0,5,0,0,0,0,1,0.8f,3);//x
        GlStateManager.rotate(zAngle,0,0,1);
        GlStateManager.rotate(yAngle,0,1,0);
        GlStateManager.rotate(xAngle,1,0,0);
        GlStateManager.translate(-axisDisplacement.x - .5,-axisDisplacement.y - .5,-axisDisplacement.z - .5);

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

    private void clearPreview(){
        if(blockRenderHelper != null && blockRenderHelper.isInit()) {
            blockRenderHelper.clear();
            blockRenderHelper = null;
        }
    }

    @Override
    public void destroy() {
        clearPreview();
        super.destroy();
    }
}
