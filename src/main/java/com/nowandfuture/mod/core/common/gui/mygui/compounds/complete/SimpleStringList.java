package com.nowandfuture.mod.core.common.gui.mygui.compounds.complete;

import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.ViewGroup;
import com.nowandfuture.mod.utils.DrawHelper;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SimpleStringList extends MyAbstractList<MyAbstractList.ViewHolder> {

    private Consumer<Integer> onItemClick;

    public SimpleStringList(@Nonnull RootView rootView) {
        super(rootView);
    }

    public SimpleStringList(@Nonnull RootView rootView, ViewGroup parent){
        super(rootView, parent);
    }

    @Override
    protected void onItemClicked(int index) {
        super.onItemClicked(index);
        if(onItemClick != null)
            onItemClick.accept(index);
    }

    public void setOnItemClick(Consumer<Integer> onItemClick) {
        this.onItemClick = onItemClick;
    }

    public static class StringViewHolder extends ViewHolder{
        private String string;

        public StringViewHolder(){

        }

        @Override
        public void draw(MyAbstractList list,int mouseX, int mouseY, float partialTicks,boolean isHover) {
            int frontColor = DrawHelper.colorInt(200,200,200,255);
            int backColor = DrawHelper.colorInt(0,0,0,200);
            if(isHover){
                frontColor = DrawHelper.colorInt(255,255,255,255);
                backColor = DrawHelper.colorInt(60,60,60,255);
            }

            drawRect(0,0,list.getWidth(),list.getAdapter().getHeight(),backColor);
            Minecraft.getMinecraft().fontRenderer.drawString(string,0,0,frontColor);
        }

        public void setString(String string) {
            this.string = string;
        }
    }

    public static class StringAdapter extends Adapter<SimpleStringList.StringViewHolder>{

        private List<String> strings;

        public StringAdapter(){
            strings = new ArrayList<>();
        }

        public void setStrings(List<String> strings) {
            this.strings = strings;
        }

        @Override
        public int getSize() {
            return strings.size();
        }

        @Override
        public int getHeight() {
            return 10;
        }

        @Override
        public StringViewHolder createHolder() {
            return new StringViewHolder();
        }

        @Override
        public void handle(StringViewHolder viewHolder, int index) {
            viewHolder.setString(strings.get(index));
        }
    }
}
