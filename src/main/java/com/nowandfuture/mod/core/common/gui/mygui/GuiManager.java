package com.nowandfuture.mod.core.common.gui.mygui;

import mezz.jei.api.gui.IAdvancedGuiHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public enum GuiManager {
    INSTANCE;
    private Map<AbstractGuiContainer,Long> guiContainerLongMap;

    GuiManager(){
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
