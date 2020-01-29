package com.nowandfuture.mod.handler;

import com.nowandfuture.mod.core.common.blocks.TransformedBlock;
import com.nowandfuture.mod.core.common.entities.TileEntityTransformedBlock;
import net.java.games.input.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.client.registry.ClientRegistry;
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
    public static KeyBinding keyXRotP;
    public static KeyBinding keyXRotN;
    public static KeyBinding keyYRotP;
    public static KeyBinding keyYRotN;
    public static KeyBinding keyZRotP;
    public static KeyBinding keyZRotN;

    private List<IKeyListener> listeners;
    private static KeyBindHandler instance;

    private KeyBindHandler(){
        listeners = new LinkedList<>();

        keyXRotP = new KeyBinding("movement.key.rot.x.plus", Keyboard.KEY_UP,"movement.key.adjust");
        keyXRotN = new KeyBinding("movement.key.rot.x.neg", Keyboard.KEY_DOWN,"movement.key.adjust");
        keyYRotP = new KeyBinding("movement.key.rot.y.plus", Keyboard.KEY_LEFT,"movement.key.adjust");
        keyYRotN = new KeyBinding("movement.key.rot.y.neg", Keyboard.KEY_RIGHT,"movement.key.adjust");
        keyZRotP = new KeyBinding("movement.key.rot.z.plus", Keyboard.KEY_HOME,"movement.key.adjust");
        keyZRotN = new KeyBinding("movement.key.rot.z.neg", Keyboard.KEY_END,"movement.key.adjust");

        ClientRegistry.registerKeyBinding(keyXRotN);
        ClientRegistry.registerKeyBinding(keyXRotP);
        ClientRegistry.registerKeyBinding(keyYRotP);
        ClientRegistry.registerKeyBinding(keyYRotN);
        ClientRegistry.registerKeyBinding(keyZRotP);
        ClientRegistry.registerKeyBinding(keyZRotN);
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
