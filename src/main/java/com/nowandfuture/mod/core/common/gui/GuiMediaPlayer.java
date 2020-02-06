package com.nowandfuture.mod.core.common.gui;

import com.nowandfuture.mod.Movement;
import com.nowandfuture.mod.core.common.entities.TileEntitySimplePlayer;
import com.nowandfuture.mod.core.common.gui.mygui.AbstractGuiContainer;
import com.nowandfuture.mod.core.common.gui.mygui.MyGui;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyButton;
import com.nowandfuture.mod.core.common.gui.mygui.compounds.compatible.MyTextField;
import com.nowandfuture.mod.network.NetworkHandler;
import com.nowandfuture.mod.network.message.MovementMessage;
import com.nowandfuture.mod.utils.SyncTasks;
import joptsimple.internal.Strings;

import java.util.concurrent.*;

//client only
public class GuiMediaPlayer extends AbstractGuiContainer {
    public static final int GUI_ID = 0x107;
    private TileEntitySimplePlayer player;

    private MyTextField myTextField;
    private MyButton playBtn,stopBtn,rotYBtn;

//    private FrameLayout btnLayout;

    public GuiMediaPlayer(TileEntitySimplePlayer player) {
        super(new ContainerSimplePlayer());
        this.player = player;

//        btnLayout = new FrameLayout(getRootView());
//        playBtn = new Button(getRootView(),btnLayout);
//        stopBtn = new Button(getRootView(),btnLayout);
//        btnLayout.addChildren(playBtn,stopBtn);

        xSize = 200;
        ySize = 100;
    }

    @Override
    public void onLoad() {
        myTextField = createMyTextField(20,20,160,14,"video url");
        playBtn = createMyButton(20,40,30,14,"play");
        stopBtn = createMyButton(60,40,30,14,"stop");
        rotYBtn = createMyButton(100,40,30,14,"rotate");
//        btnLayout.setX(20);
//        btnLayout.setY(40);
//        btnLayout.setWidth(100);
//        btnLayout.setHeight(20);
//        playBtn.setX(0);
//        playBtn.setY(0);
//        stopBtn.setX(20);
//        stopBtn.setY(0);
//        playBtn.setWidth(20);
//        playBtn.setHeight(14);
//        stopBtn.setWidth(20);
//        stopBtn.setHeight(14);

//        playBtn.setText("play");
//        stopBtn.setText("stop");

//        playBtn.setActionListener(new View.ActionListener() {
//            @Override
//            public void onClicked(View v) {
//                String url = myTextField.getText();
//                if(!Strings.isNullOrEmpty(url)){
//                    try {
//                        if(player.touchSource(url)) {
//                            player.setUrl(url);
//                            player.play();
//                        }
//                    } catch (Exception e) {
////                        NetworkHandler.INSTANCE.sendClientCommandMessage(e.getMessage());
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//
//        stopBtn.setActionListener(new View.ActionListener() {
//            @Override
//            public void onClicked(View v) {
//                try {
//                    player.end();
//                } catch (Exception e) {
////                    NetworkHandler.INSTANCE.sendClientChatMessage(e.getMessage());
//                    e.printStackTrace();
//                }
//            }
//        });


        bind(playBtn, new ActionClick() {
            @Override
            public void clicked(MyGui gui, int button) {
                String url = myTextField.getText();
                if(!Strings.isNullOrEmpty(url)){
                    SyncTasks.INSTANCE.addTask(new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            playBtn.enabled = false;
                            boolean rs = player.touchSource(url);
                            playBtn.enabled = true;
                            if(rs) Movement.proxy.addScheduledTaskClient(new Runnable() {
                                @Override
                                public void run() {
                                    player.setUrl(url);
                                    MovementMessage.StringDataSyncMessage message =
                                            new MovementMessage.StringDataSyncMessage(MovementMessage.StringDataSyncMessage.GUI_PLAYER_URL,
                                                    url);
                                    NetworkHandler.INSTANCE.sendMessageToServer(message);
                                    try {
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

        bind(stopBtn, new ActionClick() {
            @Override
            public void clicked(MyGui gui, int button) {
                try {
                    player.end();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        bind(rotYBtn, new ActionClick() {
            @Override
            public void clicked(MyGui gui, int button) {

                player.setFacing(player.getFacing().rotateY());
                MovementMessage.IntDataSyncMessage message =
                        new MovementMessage.IntDataSyncMessage(MovementMessage.IntDataSyncMessage.GUI_PLAYER_FACING_ROTATE,
                                player.getFacing().ordinal());
                NetworkHandler.INSTANCE.sendMessageToServer(message);
            }
        });

        addGuiCompoundsRelative(myTextField,playBtn,stopBtn,rotYBtn);

        myTextField.setMaxStringLength(512);
        myTextField.setText(player.getUrl() == null ? Strings.EMPTY : player.getUrl());
        if(player.getSimplePlayer().isLoading()){
            playBtn.enabled = false;
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
