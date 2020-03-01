package com.nowandfuture.mod.core.movecontrol;

import com.nowandfuture.mod.api.IModule;
import com.nowandfuture.mod.core.common.entities.TileEntityTimelineEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

//test
public enum ModuleManager {
    INSTANCE;
    private final Map<BlockPos,IModule> modules = new HashMap<>();
    private final Deque<IModule> renderModules = new LinkedList<>();

    public void initModules(){

    }

    public void add(BlockPos id,IModule movementModule){
        modules.put(id,movementModule);
    }

    public Deque<IModule> getModules() {
        return renderModules;
    }

}
