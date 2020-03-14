package com.nowandfuture.mod.core.common.gui.mygui.compounds.complete;

import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.ViewGroup;
import com.nowandfuture.mod.utils.DrawHelper;
import net.minecraft.client.Minecraft;
import org.lwjgl.util.Color;

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
    protected void onItemClicked(int index,int x,int y) {
        super.onItemClicked(index,x,y);
        if(onItemClick != null)
            onItemClick.accept(index);
    }

    public void setOnItemClick(Consumer<Integer> onItemClick) {
        this.onItemClick = onItemClick;
    }

    @Override
    protected void drawSplitLine() {
//        drawHorizontalLine(0,getWidth(),0, DrawHelper.colorInt(255,0,0,255));
    }

    public static class StringViewHolder extends ViewHolder{
        private String string;
        private TextView textView;

        public StringViewHolder(@Nonnull RootView rootView) {
            super(rootView);
            init();
        }

        public StringViewHolder(@Nonnull RootView rootView, ViewGroup parent) {
            super(rootView, parent);
            init();
        }

        public StringViewHolder(@Nonnull RootView rootView, ViewGroup parent, @Nonnull List list) {
            super(rootView, parent, list);
            init();
        }

        private void init(){
            textView = new TextView(getRoot(),this);
            textView.setWidth(80);
            textView.setHeight(12);
            textView.setX(0);
            textView.setY(0);
            textView.setClickable(false);
            addChild(textView);
        }

        public void setString(String string) {
            textView.setText(string);
            this.string = string;
        }

        @Override
        protected boolean onPressed(int mouseX, int mouseY, int state) {
            return false;
        }

        @Override
        protected void onDraw(int mouseX, int mouseY, float partialTicks) {
            drawBackground();
            int frontColor = DrawHelper.colorInt(200,200,200,255);
            int backColor = DrawHelper.colorInt(0,0,0,200);
            if(textView.isHover()) {
                frontColor = DrawHelper.colorInt(255, 255, 255, 255);
                backColor = DrawHelper.colorInt(60, 60, 60, 255);
            }
            textView.setBackgroundColor(backColor);
            textView.setTextColor(frontColor);
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
        public StringViewHolder createHolder(RootView rootView) {
            return new StringViewHolder(rootView);
        }

        @Override
        public void handle(MyAbstractList list,StringViewHolder viewHolder, int index) {
            viewHolder.setString(strings.get(index));
        }
    }
}
