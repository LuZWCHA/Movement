package com.nowandfuture.asm;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;

public class RenderHook {
    private final static Deque<IRender> renderModules = new LinkedList<>();

    public static void render(int pass,float p) {
        synchronized (renderModules) {
            while (!renderModules.isEmpty()) {
                IRender render = renderModules.removeLast();
                if (render.isRenderValid()) {
                    render.render(pass, p);
                }
            }
        }
    }



    public synchronized static void offer(IRender iRender){
        renderModules.offerFirst(iRender);
    }

    public static void forceClear(){
        renderModules.clear();
    }

}
