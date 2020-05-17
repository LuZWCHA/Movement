package com.nowandfuture.mod.core.common.gui;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.common.entities.TileEntitySimplePlayer;
import com.nowandfuture.mod.core.common.gui.mygui.AbstractGuiContainer;
import com.nowandfuture.mod.core.common.gui.mygui.JEIGuiHandler;
import com.nowandfuture.mod.core.common.gui.mygui.api.MyGui;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.RootView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.View;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyTextField;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.Button;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.FileViewerView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.NumberBox;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.SliderView;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.complete.layouts.FrameLayout;
import com.nowandfuture.mod.network.NetworkHandler;
import com.nowandfuture.mod.network.message.LMessage;
import com.nowandfuture.mod.utils.SyncTasks;
import joptsimple.internal.Strings;
import net.minecraft.client.Minecraft;
import org.lwjgl.util.Color;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

//client only
public class GuiMediaPlayer extends AbstractGuiContainer {
    public static final int GUI_ID = 0x107;
    private TileEntitySimplePlayer player;

    private MyTextField urlTextField;
    private Button fileBowerBtn;

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

        xSize = 200;
        ySize = 100;
    }

    @Override
    public void onLoad() {
        urlTextField = createMyTextField(20,20,160,14,"video url");
        fileBowerBtn = GuiBuilder.wrap(new Button(getRootView()))
                .setX(190).setY(20).setWidth(26).setHeight(14).build();
        fileBowerBtn.setText("...");
        fileBowerBtn.setActionListener(new View.ActionListener() {
            @Override
            public void onClicked(View v) {
                File root = new File(Minecraft.getMinecraft().gameDir.getAbsolutePath());
                FileViewerView view = new FileViewerView(getRootView(),root);
                view.setBackgroundColor(new Color(255,255,255));
                view.setFileFilter(new FileFilter() {
                    String[] suffixes = new String[]{"mp4","avi","mov","mkv","flv","gif","mp3"};

                    @Override
                    public boolean accept(File pathname) {
                        if(!pathname.isDirectory()) {
                            String name = pathname.getName();
                            for (final String suffix : this.suffixes) {
                                if (name.endsWith(suffix)) {
                                    return true;
                                }
                            }
                            return false;
                        }else {
                            return true;
                        }
                    }
                });
                view.setX(0);
                view.setY(0);
                view.setWidth(160);
                view.setHeight(100);
                view.setActionListener(new FileViewerView.AcceptFile() {
                    @Override
                    public void onAcceptFile(List<File> files) {
                        if(files != null && !files.isEmpty()){
                            urlTextField.setText(files.get(0).getAbsolutePath());
                        }
                    }
                });
                RootView.DialogBuilder builder = getRootView().createDialogBuilder(view);
                builder.build().setCenter().show();
            }
        });

        addGuiCompounds(fileBowerBtn);

        btnLayout.addChildren(stopBtn, playBtn, rotateBtn,volumeView,widthBox,heightBox);

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

        playBtn.setText(R.name(R.id.text_player_btn_play_id));
        stopBtn.setText(R.name(R.id.text_player_btn_stop_id));
        rotateBtn.setText(R.name(R.id.text_player_btn_screen_rotate_id));

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
                String url = urlTextField.getText();
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

                                    LMessage.StringDataSyncMessage message =
                                            new LMessage.StringDataSyncMessage(
                                                    LMessage.StringDataSyncMessage.GUI_PLAYER_URL, url);
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
                            else{
                                Exception exception = player.getLastException();
                                if(exception != null){
                                    Movement.logger.warn(exception.getMessage());
                                }
                            }
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
                LMessage.IntDataSyncMessage message =
                        new LMessage.IntDataSyncMessage(
                                LMessage.IntDataSyncMessage.GUI_PLAYER_FACING_ROTATE,
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
                    LMessage.IntDataSyncMessage intDataSyncMessage =
                            new LMessage.IntDataSyncMessage(
                                    LMessage.IntDataSyncMessage.GUI_PLAYER_SIZE_X,integer
                            );
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
                    LMessage.IntDataSyncMessage intDataSyncMessage =
                            new LMessage.IntDataSyncMessage(LMessage.IntDataSyncMessage.GUI_PLAYER_SIZE_Y,integer);
                    intDataSyncMessage.setPos(player.getPos());
                    NetworkHandler.INSTANCE.sendMessageToServer(intDataSyncMessage);
                }
            }
        });

        addView(btnLayout);

        addGuiCompoundsRelative(urlTextField);

        urlTextField.setMaxStringLength(512);
        urlTextField.setText(player.getUrl() == null ? Strings.EMPTY : player.getUrl());
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
        //sync from server
        if(widthBox.getCurValue() != player.getWidth()){
            widthBox.setCurValue(player.getWidth());
        }
        if(heightBox.getCurValue() != player.getHeight()){
            heightBox.setCurValue(player.getHeight());
        }
    }

    @Override
    protected void childFocused(MyGui gui) {
        if(gui == urlTextField){
            urlTextField.setFocused(true);
        }
    }

    @Override
    protected List<GuiRegion> getExtraRegion() {
        return null;
    }

    @Override
    protected void childLoseFocus(MyGui gui) {
        if(gui == urlTextField){
            urlTextField.setFocused(false);
        }
    }

    @Override
    public long getId() {
        return GUI_ID;
    }

    @Override
    public JEIGuiHandler<? extends AbstractGuiContainer> createJEIGuiHandler() {
        return new JEIGuiHandler<GuiMediaPlayer>() {
            @Override
            public Class<GuiMediaPlayer> getGuiContainerClass() {
                return GuiMediaPlayer.class;
            }
        };
    }
}
