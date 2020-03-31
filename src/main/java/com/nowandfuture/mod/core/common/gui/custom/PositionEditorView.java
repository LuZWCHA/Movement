package com.nowandfuture.mod.core.common.gui.custom;

import com.google.common.base.Predicate;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.ViewGroup;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.EditorView;
import joptsimple.internal.Strings;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class PositionEditorView extends ViewGroup {

    private EditorView positionX,positionY,positionZ;
    private int padding = 10;
    private Predicate<String> predicate;
    private Consumer<EditorView> consumer;

    public PositionEditorView(@Nonnull RootView rootView) {
        super(rootView);
        positionX = new EditorView(rootView);
        positionY = new EditorView(rootView);
        positionZ = new EditorView(rootView);

        predicate = input -> {
            if(input == null) input = Strings.EMPTY;

            try {
                Integer.parseInt(input);
            }catch (NumberFormatException e){
                return input.isEmpty();
            }
            return true;
        };

        consumer = new Consumer<EditorView>() {
            @Override
            public void accept(EditorView editorView) {
                if(editorView.getText().isEmpty()){
                    editorView.setText("0");
                }
            }
        };

        positionX.setOnLoseFocus(consumer);
        positionY.setOnLoseFocus(consumer);
        positionZ.setOnLoseFocus(consumer);

        positionX.setValidator(predicate);
        positionY.setValidator(predicate);
        positionZ.setValidator(predicate);
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        positionX.setX(0);
        positionX.setWidth(24);
        positionX.setHeight(16);
        positionY.setX(24 + padding);
        positionY.setWidth(24);
        positionY.setHeight(16);
        positionZ.setX(48 + 2 * padding);
        positionZ.setWidth(24);
        positionZ.setHeight(16);

        addChildren(positionX,positionY,positionZ);
    }

    @Override
    protected void onLayout(int parentWidth, int parentHeight) {

    }

    @Override
    protected void onDraw(int mouseX, int mouseY, float partialTicks) {
    }

    @Override
    protected boolean onClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    @Override
    protected boolean onLongClicked(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    @Override
    protected void onReleased(int mouseX, int mouseY, int state) {

    }

    @Override
    protected boolean onPressed(int mouseX, int mouseY, int state) {
        return false;
    }

    @Override
    public boolean onKeyType(char typedChar, int keyCode) {
        return super.onKeyType(typedChar, keyCode);
    }

    @Override
    public void loseFocus() {
        super.loseFocus();
    }

    public BlockPos getPos(){
        int x = Integer.parseInt(positionX.getText());
        int y = Integer.parseInt(positionY.getText());
        int z = Integer.parseInt(positionZ.getText());

        return new BlockPos(x,y,z);
    }

    public void setPos(BlockPos pos){
        positionX.setText(String.valueOf(pos.getX()));
        positionY.setText(String.valueOf(pos.getY()));
        positionZ.setText(String.valueOf(pos.getZ()));
    }

}
