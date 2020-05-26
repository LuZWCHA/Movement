package com.nowandfuture.mod.core.prefab;

import com.nowandfuture.mod.core.common.entities.TileEntityModule;
import com.nowandfuture.mod.core.movementbase.ModuleNode;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModuleUtils {

    public static ModuleNode buildEmptyModule(World world, long pid,long tid){
        ModuleNode module = new ModuleNode();
        module.setPrefabId(pid);
        module.setTimelineId(tid);
        buildModule(world,module,null,null);
        return module;
    }

    public static ModuleNode buildNewModuleNode(World world, long pid, long tid, @Nullable ItemStack prefabStack, @Nullable ItemStack timelineStack){
        ModuleNode module = new ModuleNode();
        module.setPrefabId(pid);
        module.setTimelineId(tid);
        buildModule(world,module,prefabStack,timelineStack);
        return module;
    }

    public static void buildModule(World world, @Nonnull TileEntityModule module, @Nullable ItemStack prefabStack, @Nullable ItemStack timelineStack){
        module.setWorld(world);

        if(prefabStack != null) {
            AbstractPrefab prefab = new NormalPrefab();
            NBTTagCompound prefabData = prefabStack.getTagCompound();
            if (prefabData != null) {
                prefab.readFromNBT(prefabData, world);
            }
            module.setPrefab(prefab);
            module.enable();
        }

        if(timelineStack != null) {
            NBTTagCompound timelineData = timelineStack.getTagCompound();
            if(timelineData != null)
                module.getLine().deserializeNBT(timelineData);
            else
                module.getLine().reset();
        }

        module.onLoad();
    }

    public static void setPrefab(World world, TileEntityModule module, ItemStack prefab){
        buildModule(world,module,prefab,null);
    }

    public static void setTimeline(World world,TileEntityModule module,ItemStack timeline){
        buildModule(world,module,null,timeline);
    }

    public static void removePrefab(TileEntityModule module){
        if(module == null) return;
        module.setPrefab(new NormalPrefab());
    }

    public static void removeTimeline(TileEntityModule module){
        if(module == null) return;
        module.getLine().reset();
    }
}
