package com.nowandfuture.mod.handler;

import com.nowandfuture.mod.core.movecontrol.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.nowandfuture.mod.Movement.MODID;
import static org.lwjgl.input.Keyboard.KEY_K;
import static org.lwjgl.input.Keyboard.KEY_L;
import static org.lwjgl.input.Keyboard.KEY_S;

@SideOnly(Side.CLIENT)
public class KeyBindHandler {

    public static final KeyBinding L = new KeyBinding("show selection area",KEY_L,MODID);
    public static final KeyBinding C = new KeyBinding("constructTest test",KEY_K,MODID);
    public static final KeyBinding S = new KeyBinding("spawn test",KEY_S,MODID);

    public static void init(){

    }

    @SubscribeEvent
    public void keyDown(InputEvent.KeyInputEvent event){

    }
}
