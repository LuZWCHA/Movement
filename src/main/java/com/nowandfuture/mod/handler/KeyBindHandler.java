package com.nowandfuture.mod.handler;

import com.nowandfuture.mod.core.common.blocks.TransformedBlock;
import com.nowandfuture.mod.core.common.entities.TileEntityTransformedBlock;
import net.java.games.input.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class KeyBindHandler {
    public static KeyBinding keyXRotP = new KeyBinding("key.rot.x.plus", Keyboard.KEY_UP,"movement.key.justify");
    public static KeyBinding keyXRotN = new KeyBinding("key.rot.x.neg", Keyboard.KEY_DOWN,"movement.key.justify");
    public static KeyBinding keyYRotP = new KeyBinding("key.rot.y.plus", Keyboard.KEY_LEFT,"movement.key.justify");
    public static KeyBinding keyYRotN = new KeyBinding("key.rot.y.neg", Keyboard.KEY_RIGHT,"movement.key.justify");
    public static KeyBinding keyZRotP = new KeyBinding("key.rot.z.plus", Keyboard.KEY_HOME,"movement.key.justify");
    public static KeyBinding keyZRotN = new KeyBinding("key.rot.z.neg", Keyboard.KEY_END,"movement.key.justify");
//
    private List<IKeyListener> listeners;
    private static KeyBindHandler instance;

    private KeyBindHandler(){
        listeners = new LinkedList<>();
    }

    public static void register(IKeyListener listener){
        instance.listeners.add(listener);
    }

    public static void unregister(IKeyListener listener){
        instance.listeners.remove(listener);
    }

    public static void unregisterAll(){
        instance.listeners.clear();
    }

    public static KeyBindHandler getInstance() {
        if(instance == null){
            instance = new KeyBindHandler();
        }
        return instance;
    }


    @SubscribeEvent
    public void keyDown(InputEvent.KeyInputEvent event){
        for (IKeyListener kl :
                listeners) {
            kl.onKeyDown();
        }
    }
}
