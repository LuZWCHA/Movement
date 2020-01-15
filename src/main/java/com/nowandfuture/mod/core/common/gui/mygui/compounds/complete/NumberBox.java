package com.nowandfuture.mod.core.common.gui.mygui.compounds.complete;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.View;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.ViewGroup;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyButton;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyTextField;
import com.nowandfuture.mod.utils.DrawHelper;
import org.apache.http.util.TextUtils;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class NumberBox extends ViewGroup {

    private Button plusBtn,subBtn;
    private MyTextField textField;
    private int textFieldWidth = 0;

    private int max,min;
    private int defaultValue;

    private int curValue;

    private Consumer<Boolean> focusChangedListener;
    private Consumer<Integer> valueChangedListener;

    public NumberBox(@Nonnull RootView rootView) {
        super(rootView);
    }

    public NumberBox(@Nonnull RootView rootView, ViewGroup parent) {
        super(rootView, parent);
        plusBtn = new Button(rootView,this);
        subBtn = new Button(rootView,this);

        plusBtn.setText("+");
        subBtn.setText("-");

        plusBtn.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v) {
                int temp = curValue;
                curValue ++;
                if(curValue > max) curValue = max;
                if(temp != curValue)
                    valueChanged();
            }
        });

        subBtn.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v) {
                int temp = curValue;
                curValue --;
                if(curValue < min) curValue = min;
                if(temp != curValue)
                    valueChanged();
            }
        });

        this.addChildren(plusBtn,subBtn);

        textField = new MyTextField(2,rootView.context.fontRenderer,plusBtn.getWidth() + textFieldWidth,0,textFieldWidth,this.getHeight());
        max = 1;
        min = 0;
        defaultValue = 0;
        curValue = defaultValue;

        textField.setFilter(new Function<Character, Boolean>() {
            @Override
            public Boolean apply(@Nullable Character input) {
                int keyCode = Keyboard.getEventKey();
                return Character.isDigit(input)|| keyCode == Keyboard.KEY_DELETE ||
                        keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_LEFT ||
                        keyCode == Keyboard.KEY_RIGHT || input == '-';
            }
        });

        textField.setValidator(new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String input) {
                int value = Integer.MIN_VALUE;
                try {
                    value = Integer.parseInt(input);
                }catch (NumberFormatException e){

                }
                return TextUtils.isEmpty(input) || (value <= max && value >= min);
            }
        });

        textField.setText(String.valueOf(curValue));
//        textField.setTextColor(DrawHelper.colorInt(0,0,0,255));
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        subBtn.setHeight(this.getHeight());
        subBtn.setWidth(this.getHeight());
        plusBtn.setHeight(this.getHeight());
        plusBtn.setWidth(this.getHeight());
        textFieldWidth = this.getWidth() - 2 * this.getHeight() - 4;
        if(textFieldWidth < 0) textFieldWidth = 0;
        textField.setWidth(textFieldWidth);
        textField.setHeight(this.getHeight());
        subBtn.setX(0);
        subBtn.setY(0);
        textField.setX(subBtn.getWidth() + 2);
        textField.setY(0);
        plusBtn.setX(textFieldWidth + textField.getX() + 2);
        plusBtn.setY(0);
        textField.setText(String.valueOf(curValue));
    }

    @Override
    protected void onLayout(int parentWidth, int parentHeight) {

    }

    @Override
    protected void onDraw(int mouseX, int mouseY, float partialTicks) {
        plusBtn.draw(mouseX,mouseY,partialTicks);
        subBtn.draw(mouseX, mouseY, partialTicks);
        textField.draw(mouseX, mouseY, partialTicks);
    }

    @Override
    protected boolean onClicked(int mouseX, int mouseY, int mouseButton) {
        textField.mouseClicked(mouseX, mouseY, mouseButton);
        return true;
    }

    private void textFocusChanged(boolean v){
        if(focusChangedListener != null){
            focusChangedListener.accept(v);
        }
    }

    private void valueChanged(){
        System.out.println("curValue = " + curValue);
        textField.setText(String.valueOf(curValue));
        if(valueChangedListener != null){
            valueChangedListener.accept(curValue);
        }
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public void setDefaultValue(int defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void reset(){
        if(defaultValue <= max && defaultValue >= min)
            curValue = defaultValue;
        else{
            defaultValue = min;
            curValue = defaultValue;
        }
    }

    public void setCurValue(int curValue) {
        if(curValue <= max && curValue >= min) {
            this.curValue = curValue;
            textField.setText(String.valueOf(curValue));
        }
    }

    public int getCurValue() {
        return curValue;
    }

    public void setFocusChangedListener(Consumer<Boolean> focusChangedListener) {
        this.focusChangedListener = focusChangedListener;
    }

    @Override
    protected boolean onLongClicked(int mouseX, int mouseY, int mouseButton) {
        return true;
    }

    @Override
    protected void onReleased(int mouseX, int mouseY, int state) {

    }

    @Override
    protected boolean onPressed(int mouseX, int mouseY, int state) {
        return true;
    }

    @Override
    public boolean onKeyType(char typedChar, int keyCode) {
        System.out.println("type");
        return textField.keyTyped2(typedChar, keyCode);
    }

    @Override
    public void loseFocus() {
        super.loseFocus();
        System.out.println("lose");
        textField.setFocused(false);
        if(TextUtils.isEmpty(textField.getText())){
            textField.setText(String.valueOf(curValue));
        }else{
            int temp = Integer.parseInt(textField.getText());
            if(temp != curValue)
                curValue = temp;
                valueChanged();
        }

    }

    @Override
    public void focused() {
        super.focused();
        System.out.println("focus");
    }

    @Override
    public void onUpdate() {
        textField.update();
    }

    public void setValueChangedListener(Consumer<Integer> valueChangedListener) {
        this.valueChangedListener = valueChangedListener;
    }
}
