package com.nowandfuture.mod.core.transformers.animation;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public enum TimeLineManager {
    INSTANCE;
    private final Map<String,TimeLine> lines;

    TimeLineManager(){
        lines = new LinkedHashMap<>();
    }

    public void update(){
        if(lines.isEmpty()) return;
        lines.values().forEach((Consumer<TimeLine>) timeLine -> {
            if(timeLine.isEnable())
                timeLine.update();
        });
    }

    public TimeLine registerTimeLine(String name){
        TimeLine timeLine = new TimeLine();
        lines.put(name, timeLine);
        return timeLine;
    }

    public void unRegisterTimeLine(String name){
        lines.remove(name);
    }

    @Nullable
    public TimeLine getLine(String name){
        return lines.get(name);
    }

    public void clear(){
        lines.clear();
    }
}
