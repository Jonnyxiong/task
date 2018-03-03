package com.ucpaas.sms.task.listener;

import com.ucpaas.sms.task.util.AuditBakExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.List;

public class InitListener implements ServletContextListener {

    private static Logger logger = LoggerFactory.getLogger(InitListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("正在关闭审核备份的线程");
        List<Runnable> notRuntasks = AuditBakExecutorService.shutdown();

        logger.info("关闭审核备份的线程完毕，未完成任务有{}项",notRuntasks==null?0:notRuntasks.size());
    }
}