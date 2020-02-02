package com.nowandfuture.mod.utils;

import java.util.List;
import java.util.concurrent.*;

public enum SyncTasks {
    INSTANCE;
    private ThreadPoolExecutor executor;
    private BlockingQueue<Runnable> queue;

    SyncTasks(){
        init();
    }

    public void init(){
        queue = new LinkedBlockingQueue<>();
        executor = new ThreadPoolExecutor(2,10,10, TimeUnit.MILLISECONDS,queue);
    }

    public void addTask(Runnable runnable){
        executor.submit(runnable);
    }

    public <T>Future addTask(Callable<T> callable){
        return executor.submit(callable);
    }

    public List<Runnable> showdownNow(){
        return executor.shutdownNow();
    }

}
