package com.nowandfuture.mod.core.prefab;

import com.nowandfuture.mod.core.common.gui.mygui.AbstractContainer;
import com.nowandfuture.mod.core.common.gui.mygui.api.IDynamicInventory;
import com.nowandfuture.mod.core.movementbase.ModuleNode;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ModuleNodeMap {
    public interface ModuleMapChangedListener{
        void onModuleAdded(ModuleNode moduleNode);
        void onModuleRemoved(int index,ModuleNode moduleNode);
    }

    private ArrayList<ModuleNode> modules;
    private ArrayList<Long> prefabList;
    private ArrayList<Long> timelineList;
    private List<ModuleMapChangedListener> listeners;

    public ModuleNodeMap(){
        modules = new ArrayList<>();
        prefabList = new ArrayList<>();
        timelineList = new ArrayList<>();
        listeners = new LinkedList<>();
    }

    public ArrayList<ModuleNode> getModules() {
        return modules;
    }

    public void addListener(ModuleMapChangedListener listener){
        listeners.add(listener);
    }

    public void removeListener(ModuleMapChangedListener listener){
        listeners.remove(listener);
    }

    public static ModuleNodeMap buildNewMap(World world, IDynamicInventory inventory){
        ModuleNodeMap map = new ModuleNodeMap();
        Iterator<AbstractContainer.ProxySlot> it = inventory.getSlots().values().iterator();
        while (it.hasNext()){
            AbstractContainer.ProxySlot pbs = it.next();
            if(it.hasNext()) {
                AbstractContainer.ProxySlot tls = it.next();

                long pid = pbs.getSlotIndex();
                long tid = tls.getSlotIndex();
                map.addNewToMap(world,pid,tid);
            }
        }
        return map;
    }

    public static ModuleNode buildModule(World world,long prefabId,long timelineId){
        ModuleNode moduleNode = new ModuleNode();
        moduleNode.setWorld(world);
        moduleNode.setPrefabId(prefabId);
        moduleNode.setTimelineId(timelineId);
        return moduleNode;
    }

    public ModuleNode prefabToModule(long id){
        int index = prefabList.indexOf(id);
        if(index > -1){
            return modules.get(index);
        }
        return null;
    }

    public ModuleNode timelineToModule(long id){
        int index = timelineList.indexOf(id);
        if(index > -1){
            return modules.get(index);
        }
        return null;
    }

    public void addNewToMap(World world,long prefabId,long timelineId){
        prefabList.add(prefabId);
        timelineList.add(timelineId);
        ModuleNode node = buildModule(world,prefabId,timelineId);
        modules.add(node);
        markDirty(modules.size() - 1,node,false);
    }

    public void addModule(ModuleNode moduleNode){
        if(!prefabList.contains(moduleNode.getPrefabId()) &&
                !timelineList.contains(moduleNode.getTimelineId())) {
            prefabList.add(moduleNode.getPrefabId());
            timelineList.add(moduleNode.getTimelineId());
            modules.add(moduleNode);
            markDirty(modules.size() - 1,moduleNode,false);
        }
    }

    public void removeMap(ModuleNode tileEntityModule){
        int index = modules.indexOf(tileEntityModule);
        if(index > -1){
            prefabList.remove(index);
            timelineList.remove(index);
            modules.remove(index);
            markDirty(index,tileEntityModule,true);
        }
    }

    public ModuleNode getNodeById(long prefabId,long timelineId){
        int pindex = prefabList.indexOf(prefabId);
        int tindex = timelineList.indexOf(timelineId);
        if(pindex == tindex && pindex >= 0){
            return modules.get(pindex);
        }
        return null;
    }

    public boolean contains(BlockPos pos){
        for (ModuleNode node :
                modules) {
            if (node.getOffset().equals(pos)) {
                return true;
            }
        }
        return false;
    }

    public ModuleNode get(BlockPos pos){
        for (ModuleNode node :
                modules) {
            if (node.getOffset().equals(pos)) {
                return node;
            }
        }
        return null;
    }

    public int size(){
        return modules.size();
    }

    public boolean isEmpty(){
        return modules.isEmpty();
    }

    public void removeByPrefabId(long id){
        int index = prefabList.indexOf(id);
        if(index > -1){
            prefabList.remove(index);
            timelineList.remove(index);
            ModuleNode node = modules.get(index);
            modules.remove(index);
            markDirty(index,node,true);
        }
    }

    public void removeByTimelineId(long id){
        int index = timelineList.indexOf(id);
        if(index > -1){
            prefabList.remove(index);
            timelineList.remove(index);
            ModuleNode node = modules.get(index);
            modules.remove(index);
            markDirty(index,node,true);
        }
    }

    private void markDirty(int index,ModuleNode moduleNode,boolean remove){
        if(!listeners.isEmpty()){
            for (ModuleMapChangedListener l :
                    listeners) {
                if(!remove)
                    l.onModuleAdded(moduleNode);
                else
                    l.onModuleRemoved(index, moduleNode);
            }
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound){
        NBTTagList nbtTagList = new NBTTagList();
        for (ModuleNode module : modules) {
            nbtTagList.appendTag(module.writeToNBT(new NBTTagCompound()));
        }
        compound.setTag("Modules",nbtTagList);
        return compound;
    }

    public void readFromNBT(NBTTagCompound compound,World world){
        NBTTagList nbtTagList = compound.getTagList("Modules",10);
        List<ModuleNode> moduleNodes = new LinkedList<>();

        for(int i = 0;i < nbtTagList.tagCount();i++) {
            NBTTagCompound moduleNBT = nbtTagList.getCompoundTagAt(i);

            ModuleNode moduleNode = new ModuleNode();
            moduleNode.setWorld(world);
            moduleNode.readFromNBT(moduleNBT);
            ModuleNode orgNode = getNodeById(moduleNode.getPrefabId(),moduleNode.getTimelineId());
            moduleNodes.add(moduleNode);

            if(orgNode == null) {
                addModule(moduleNode);
            }else{
                //update
                int index = modules.indexOf(orgNode);
                moduleNode.setParent(orgNode.getParent());
                modules.set(index,moduleNode);
            }
        }

        List<ModuleNode> removeNodes = new LinkedList<>();
        for (ModuleNode node :
                modules) {
            if (!moduleNodes.contains(node)) {
                removeNodes.add(node);
            }
        }

        for (ModuleNode node :
                removeNodes) {
            removeMap(node);
        }
    }
}
