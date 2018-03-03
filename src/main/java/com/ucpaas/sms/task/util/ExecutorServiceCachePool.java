package com.ucpaas.sms.task.util;




import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExecutorServiceCachePool {


    private static ExecutorService executorService = Executors.newCachedThreadPool();


    public static <V> Future<V> submit(Callable<V> task){
        return executorService.submit(task);
    }
}
