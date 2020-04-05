package com.nowandfuture.mod.core.common.gui.mygui.compounds;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Collection;

@SideOnly(Side.CLIENT)
public abstract class View extends ViewGroup {

    public View(@Nonnull RootView rootView) {
        super(rootView);
    }

    public View(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
    }

    @Override
    public final void layout(int parentWidth, int parentHeight) {
        super.layout(parentWidth, parentHeight);
    }

    @Override
    protected final void onChildrenLayout() {
        //do nothing
    }

    @Override
    public void onLayout(int parentWidth, int parentHeight) {

    }

    @Override
    protected boolean onPressed(int mouseX, int mouseY, int state) {
        return true;
    }

    @Override
    protected boolean onInterceptClickAction(int mouseX, int mouseY, int button) {
        return true;
    }

    @Override
    public boolean onKeyType(char typedChar, int keyCode) {
        return false;
    }

    /**
     * this method would execute at the first time of the view been load (at parent view's onLoad())
     * and it only execute one time on a view's life
     */
    @Override
    protected void onLoad() {

    }

    //------------------------------disable all children function----------------------------------

    @Override
    public final void addAll(Collection<ViewGroup> viewGroups) {

    }

    @Override
    public final void addChild(ViewGroup viewGroup) {

    }

    @Override
    public final void removeAllChildren() {

    }

    @Override
    public final void addChild(int index, ViewGroup viewGroup) {

    }

    @Override
    public final void addChildren(ViewGroup... viewGroup) {

    }

    @Override
    public final void removeChild(int index) {

    }

    @Override
    public final void removeChild(ViewGroup viewGroup) {

    }

    @Override
    public final ViewGroup getChild(int index) {
        return null;
    }

    @Override
    public final int getChildrenSize() {
        return 0;
    }

    //--------------------------------------------------------------------------------------------

    @Override
    protected void onReleased(int mouseX, int mouseY, int state) {

    }

    @Override
    public boolean handleMouseInput(int mouseX, int mouseY) {
        return false;
    }

    interface ClickListener{
        void onClicked(View v);
        void onLongClicked(View v);
    }

    public static abstract class ActionListener implements ClickListener{
        @Override
        public void onClicked(View v) {

        }

        @Override
        public void onLongClicked(View v) {

        }
    }
}
