package com.nowandfuture.mod.core.common.gui.mygui;

import java.util.HashMap;
import java.util.Map;

public enum  GuiIdManager {
    INSTANCE;
    private Map<AbstractGuiContainer,Long> guiContainerLongMap;

    GuiIdManager(){
        guiContainerLongMap = new HashMap<>();
    }

    public void register(AbstractGuiContainer guiContainer,Long startId){
        guiContainerLongMap.put(guiContainer,startId);
    }

    public Long getSuggestId(AbstractGuiContainer guiContainer){
        if(!guiContainerLongMap.containsKey(guiContainer)){
            register(guiContainer,0L);
        }

        Long id = guiContainerLongMap.get(guiContainer);
        guiContainerLongMap.replace(guiContainer,++id);

        return id;
    }
}
