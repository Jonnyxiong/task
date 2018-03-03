package com.ucpaas.sms.task.util;


import com.jsmsframework.common.util.DateUtil;

import java.util.List;
import java.util.concurrent.*;

public class AuditKeywordRecordBakExecutorService {


    public static volatile boolean isShutdown = false;

    private static ExecutorService executorService = Executors.newFixedThreadPool(Integer.valueOf(ConfigUtils.audit_keyword_record_bak_thread_size));

    public static <T> Future<T> submit(Callable<T> task) {
        return executorService.submit(task);
    }


    public static List<Runnable> shutdown() {
        isShutdown = true;
        List<Runnable> notRunTask = null;
        executorService.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                notRunTask = executorService.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executorService.shutdownNow();
        }
        return notRunTask;
    }

    public static boolean canRunning(){
        return DateUtil.isBetween(ConfigUtils.audit_keyword_record_bak_running_begin,ConfigUtils.audit_keyword_record_bak_running_end);
    }
}
