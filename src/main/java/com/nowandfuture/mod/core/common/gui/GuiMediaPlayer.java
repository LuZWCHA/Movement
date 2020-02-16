package com.nowandfuture.mod.core.common.gui;

import com.google.common.base.Utf8;
import com.nowandfuture.ffmpeg.player.SimplePlayer;
import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.common.entities.TileEntitySimplePlayer;
import com.nowandfuture.mod.core.common.gui.mygui.AbstractGuiContainer;
import com.nowandfuture.mod.core.common.gui.mygui.MyGui;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.View;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyTextField;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.*;
import com.nowandfuture.mod.network.NetworkHandler;
import com.nowandfuture.mod.network.message.MovementMessage;
import com.nowandfuture.mod.utils.SyncTasks;
import joptsimple.internal.Strings;
import org.lwjgl.util.Color;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.function.Consumer;

//client only
public class GuiMediaPlayer extends AbstractGuiContainer {
    public static final int GUI_ID = 0x107;
    private TileEntitySimplePlayer player;

    private MyTextField myTextField;

    private Button playBtn, stopBtn, rotateBtn;
    private SliderView volumeView;
    private NumberBox widthBox,heightBox;
    private FrameLayout btnLayout;

    public GuiMediaPlayer(TileEntitySimplePlayer player) {
        super(new ContainerSimplePlayer());
        this.player = player;

        btnLayout = new FrameLayout(getRootView());
        playBtn = new Button(getRootView(),btnLayout);
        stopBtn = new Button(getRootView(),btnLayout);
        rotateBtn = new Button(getRootView(),btnLayout);
        volumeView = new SliderView(getRootView(),btnLayout);
        widthBox = new NumberBox(getRootView(),btnLayout);
        heightBox = new NumberBox(getRootView(),btnLayout);
        btnLayout.addChildren(stopBtn, playBtn, rotateBtn,volumeView,widthBox,heightBox);

        xSize = 200;
        ySize = 100;
    }

    @Override
    public void onLoad() {
        myTextField = createMyTextField(20,20,160,14,"video url");

        btnLayout.setX(20);
        btnLayout.setY(40);
        btnLayout.setWidth(200);
        btnLayout.setHeight(40);
        playBtn.setX(0);
        playBtn.setY(0);
        stopBtn.setX(40);
        stopBtn.setY(0);
        rotateBtn.setX(80);
        rotateBtn.setY(0);
        playBtn.setWidth(30);
        playBtn.setHeight(14);
        stopBtn.setWidth(30);
        stopBtn.setHeight(14);
        rotateBtn.setWidth(30);
        rotateBtn.setHeight(14);
        volumeView.setX(0);
        volumeView.setY(20);
        volumeView.setWidth(50);
        volumeView.setHeight(10);
        volumeView.setRange(1,0,0);

        playBtn.setText("play!");
        stopBtn.setText("stop!");
        rotateBtn.setText("rotate");

        widthBox.setX(60);
        widthBox.setY(20);
        widthBox.setWidth(60);
        widthBox.setHeight(14);

        heightBox.setX(130);
        heightBox.setY(20);
        heightBox.setWidth(60);
        heightBox.setHeight(14);

        //from 1x1 to 16x16
        widthBox.setMax(16);
        heightBox.setMax(16);
        widthBox.setMin(1);
        heightBox.setMin(1);

        widthBox.setEditable(false);
        heightBox.setEditable(false);

        playBtn.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v) {
                String url = myTextField.getText();
                if(!Strings.isNullOrEmpty(url)){
                    SyncTasks.INSTANCE.addTask(new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            playBtn.setEnable(false);
                            boolean rs = player.touchSource(url);
                            playBtn.setEnable(true);
                            if(rs) Movement.proxy.addScheduledTaskClient(new Runnable() {
                                @Override
                                public void run() {
                                    player.setUrl(url);
                                    String sendUrl = new String(url.getBytes(), StandardCharsets.UTF_8);
                                    MovementMessage.StringDataSyncMessage message =
                                            new MovementMessage.StringDataSyncMessage(MovementMessage.StringDataSyncMessage.GUI_PLAYER_URL,
                                                    sendUrl);
                                    message.setPos(player.getPos());
                                    NetworkHandler.INSTANCE.sendMessageToServer(message);
                                    try {
                                        player.setVolume(volumeView.getProgress());
                                        player.play();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            return rs;
                        }
                    });

                }
            }
        });

        stopBtn.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v) {
                try {
                    player.end();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        rotateBtn.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v) {
                player.setFacing(player.getFacing().rotateY());
                MovementMessage.IntDataSyncMessage message =
                        new MovementMessage.IntDataSyncMessage(MovementMessage.IntDataSyncMessage.GUI_PLAYER_FACING_ROTATE,
                                player.getFacing().ordinal());
                message.setPos(player.getPos());
                NetworkHandler.INSTANCE.sendMessageToServer(message);
            }
        });

        volumeView.setProgressChanged(new Consumer<Float>() {
            @Override
            public void accept(Float aFloat) {
                if(player != null){
                    player.setVolume(aFloat);
                }
            }
        });

        widthBox.setValueChangedListener(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                if(player !=null) {
                    player.setWidth(integer.shortValue());
                    MovementMessage.IntDataSyncMessage intDataSyncMessage =
                            new MovementMessage.IntDataSyncMessage(MovementMessage.IntDataSyncMessage.GUI_PLAYER_SIZE_X,integer);
                    intDataSyncMessage.setPos(player.getPos());
                    NetworkHandler.INSTANCE.sendMessageToServer(intDataSyncMessage);
                }
            }
        });

        heightBox.setValueChangedListener(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                if(player !=null) {
                    player.setHeight(integer.shortValue());
                    MovementMessage.IntDataSyncMessage intDataSyncMessage =
                            new MovementMessage.IntDataSyncMessage(MovementMessage.IntDataSyncMessage.GUI_PLAYER_SIZE_Y,integer);
                    intDataSyncMessage.setPos(player.getPos());
                    NetworkHandler.INSTANCE.sendMessageToServer(intDataSyncMessage);
                }
            }
        });

        addView(btnLayout);

        addGuiCompoundsRelative(myTextField);

        myTextField.setMaxStringLength(512);
        myTextField.setText(player.getUrl() == null ? Strings.EMPTY : player.getUrl());
        if(player.getSimplePlayer().isLoading()){
            playBtn.setEnable(false);
        }

        volumeView.setProgress(player.getVolume());
        widthBox.setCurValue(player.getWidth());
        heightBox.setCurValue(player.getHeight());
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if(widthBox.getCurValue() != player.getWidth()){
            widthBox.setCurValue(player.getWidth());
        }
        if(heightBox.getCurValue() != player.getHeight()){
            heightBox.setCurValue(player.getHeight());
        }
    }

    @Override
    protected void childFocused(MyGui gui) {
        if(gui == myTextField){
            myTextField.setFocused(true);
        }
    }

    @Override
    protected void childLoseFocus(MyGui gui) {
        if(gui == myTextField){
            myTextField.setFocused(false);
        }
    }

    @Override
    public long getId() {
        return GUI_ID;
    }
}
