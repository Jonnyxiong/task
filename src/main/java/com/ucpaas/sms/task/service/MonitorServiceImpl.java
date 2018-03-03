package com.ucpaas.sms.task.service;

import com.alibaba.fastjson.JSON;
import com.jsmsframework.common.enums.TaskExecuteTypeEnum;
import com.jsmsframework.common.enums.TaskStatusEnum;
import com.jsmsframework.common.service.JsmsEmailService;
import com.jsmsframework.monitor.entity.JsmsTask;
import com.jsmsframework.monitor.pojo.JsmsAppServerInfo;
import com.jsmsframework.monitor.service.JsmsAppObserver;
import com.jsmsframework.monitor.service.JsmsTaskService;
import com.ucpaas.sms.task.constant.TaskConstant;
import com.ucpaas.sms.task.model.TaskInfo;
import com.ucpaas.sms.task.util.PropertiesUtil;
import com.ucpaas.sms.task.util.StringUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Service
public class MonitorServiceImpl implements MonitorService {
    private static final Logger logger = LoggerFactory.getLogger(MonitorServiceImpl.class);
    /**
     * 重要类型的任务, 按顺序
     */
    private static final int[] MAJORTASK = {5,35,38};
    private static final int OVERTIME = 3;
    @Autowired
    @Qualifier("javaMailSenderAlarm")
    private JavaMailSender javaMailSenderAlarm;
    @Autowired
    private JsmsTaskService jsmsTaskService;
    @Autowired
    private JsmsEmailService jsmsEmailService;
    @Autowired
    @Qualifier("jsmsAppObserverImpl")
    private JsmsAppObserver jsmsAppObserver;
    @Override
    public boolean taskMonitor(TaskInfo taskInfo) {
        long start = System.currentTimeMillis();
        logger.debug("【定时任务监控】开始 -----> {}",start);
        DateTime executeTime = taskInfo.getExecuteNextDate();

        try {
            List<JsmsTask> jsmsTaskList = jsmsTaskService.findList(new HashMap());
            Map<String, Object> data = new HashMap<>();
            List<JsmsTask> red = new ArrayList<>();
            List<JsmsTask> blue = new ArrayList<>();
            List<JsmsTask> green = new ArrayList<>();
            Map<Integer,JsmsTask> temp = new HashMap<>();
            for (JsmsTask jsmsTask : jsmsTaskList) {
                if(taskInfo.getTaskId().equals(jsmsTask.getTaskId())){
                    continue;
                }
                if (TaskStatusEnum.启用.getValue().equals(jsmsTask.getStatus())) {
                    if (compareTime(executeTime, jsmsTask.getExecuteNext(), TaskExecuteTypeEnum.getInstanceByValue(jsmsTask.getExecuteType()))) {
                        green.add(jsmsTask);
                    } else {
                        red.add(jsmsTask);
                    }
                } else if ( TaskStatusEnum.正在执行.getValue().equals(jsmsTask.getStatus())) {
                    if (compareTime(executeTime, jsmsTask.getExecuteNext(), TaskExecuteTypeEnum.getInstanceByValue(jsmsTask.getExecuteType()))) {
                        blue.add(jsmsTask);
                    } else {
                        temp.put(jsmsTask.getTaskId(),jsmsTask);
                    }
                }else if(Arrays.binarySearch(MAJORTASK, Integer.parseInt(jsmsTask.getTaskType())) > -1){
                    red.add(jsmsTask);
                }
            }
            List<JsmsTask> yellow = null;
            if(!temp.isEmpty()){
                Thread.sleep(1000 * 60 * OVERTIME); // 等到3分钟, 再查询判断结果
                Map<String, Object> params = new HashMap<>();
                params.put("taskIds", temp.keySet());
                List<JsmsTask> jsmsTasks = jsmsTaskService.findList(params);
                yellow = new ArrayList<>();
                for (JsmsTask task : jsmsTasks) {
                    if (compareTime(executeTime, task.getExecuteNext(), TaskExecuteTypeEnum.getInstanceByValue(task.getExecuteType()))) {
                        blue.add(task);
                    } else {
                        yellow.add(task);
                    }
                }
            }else{
                yellow = new ArrayList<>();
            }
            if(red.isEmpty()&& yellow.isEmpty() && TaskConstant.ExecuteType.minute.equals(taskInfo.getExecuteType())){
                logger.debug("【定时任务监控】邮件发送 --> 所有定时任务正常,并且任务执行类型是分钟, 不需要发送邮件");
                return true;
            }
            data.put("overtime", OVERTIME);
            data.put("red", copyProperties(red));
            data.put("blue", copyProperties(blue));
            data.put("green", copyProperties(green));
            data.put("yellow", copyProperties(yellow));
            data.put("date", executeTime.toString("yyyy-MM-dd"));
            logger.debug("【定时任务监控】分析结果数据 --> {}",JSON.toJSONString(data));

            String emailContent = loaderFreemaker(data,"task-monitor-template.ftl");

            if (StringUtil.isEmpty(emailContent)) {
                return true;
            }
            boolean isSend = false;
            isSend = sendMail(emailContent, executeTime,"定时任务监控");
            logger.debug("邮件发送 --> {}", isSend);
            return isSend;
        } catch (IOException e) {
            logger.error("未找到指定的模板文件 ----------> {}, {}", e.getMessage(), e);
        } catch (TemplateException e) {
            logger.error("FreeMaker 模板解析失败 ----------> {}, {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("发送邮件出现异常 ---> {}", e);
        }
        long end = System.currentTimeMillis();
        logger.info("【定时任务监控】结束，统计时间= {}，统计耗时={}ms", executeTime.toString("yyyy-MM-dd HH:mm:ss"), (end - start));

        return true;
    }

    private List<JsmsTask> copyProperties(List<JsmsTask> color){
        if(color.isEmpty()){
            return color;
        }else{
            List<JsmsTask> newColor = new ArrayList<>();
            for (JsmsTask jsmsTask : color) {
                JsmsTaskDto jsmsTaskDto = this.new JsmsTaskDto();
                BeanUtils.copyProperties(jsmsTask,jsmsTaskDto);
                newColor.add(jsmsTaskDto);
            }
            return newColor;
        }
        
    }

    /**
     * @param executeTime
     * @param executeNext
     * @param scanType
     * @return 如果 下次执行时间 (executeNext) >= 执行时间(executeTime)   返回 true
     */
    private boolean compareTime(DateTime executeTime, Long executeNext, TaskExecuteTypeEnum scanType) {
        if (scanType == null) {
            return false;
        }
        Long executeLong = Long.parseLong(executeTime.toString(scanType.getPattern()));
        if (executeLong > executeNext) {
            return false;
        } else {
            return true;
        }
    }

    private String loaderFreemaker(Map<String, Object> data,String templateName) throws IOException, TemplateException {
        logger.debug("开始加载 ---->  freemaker 模板");

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_25);
        cfg.setDirectoryForTemplateLoading(new File(this.getClass().getResource("/templates").getFile()));

        cfg.setDefaultEncoding("UTF-8");

        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        cfg.setLogTemplateExceptions(false);

        Template temp = cfg.getTemplate(templateName);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        Writer out = new OutputStreamWriter(buffer);
        temp.process(data, out);
        String emailContent = buffer.toString();
        out.close();
        return emailContent;
    }

    /**
     * @param emailContent
     * @return
     * @desc 发送邮件
     */
    private boolean sendMail(String emailContent, DateTime dateTime,String subject) {
        logger.debug("准备发送邮件 ---->  日期 : {}", dateTime.toString());

        String taskMonitorEmailReceivers = null;
        try {
            taskMonitorEmailReceivers = PropertiesUtil.get("taskMonitorEmailReceivers");
        } catch (IOException e1) {
            logger.error("taskMonitorEmailReceivers 配置错误,使用默认邮箱 niutao@ucpaas.com", e1);
            taskMonitorEmailReceivers = "niutao@ucpaas.com";
        }


        boolean sendHtmlEmail = jsmsEmailService.sendHtmlEmail(javaMailSenderAlarm,"alarm@ucpaas.com", taskMonitorEmailReceivers, subject, emailContent);

        if (!sendHtmlEmail) {
            logger.debug("监控邮件发送失败, 接收者 => {} , 即将拆分邮件接收者,逐一重试", taskMonitorEmailReceivers);
            String[] receivers = taskMonitorEmailReceivers.split(",");
            for (String receiver : receivers) {
                boolean send = jsmsEmailService.sendHtmlEmail(javaMailSenderAlarm,"alarm@ucpaas.com", receiver, subject, emailContent);
                if (!send) {
                    logger.debug("监控邮件发送失败, 接收者 => {}", receiver);
                }
            }
        }
        return true;
    }

    public class JsmsTaskDto extends JsmsTask{
        private String executeTypeStr;
        private String statusStr;
        private String executeNextStr;

        public String getExecuteTypeStr() {
            if (StringUtils.isNotBlank(getExecuteType())){
                executeTypeStr = TaskExecuteTypeEnum.getInstanceByValue(getExecuteType()).getDesc();
            }else{
                executeTypeStr = "";
            }
            return executeTypeStr;
        }

        public void setExecuteTypeStr(String executeTypeStr) {
            this.executeTypeStr = executeTypeStr;
        }

        public String getStatusStr() {
            if (StringUtils.isNotBlank(getStatus())){
                statusStr = TaskStatusEnum.getInstanceByValue(getStatus()).getDesc();
            }else {
                statusStr = "";
            }
            return statusStr;
        }

        public void setStatusStr(String statusStr) {
            this.statusStr = statusStr;
        }

        public String getExecuteNextStr() {
            if(getExecuteNext() != null){
                executeNextStr = getExecuteNext().toString();
            }else{
                executeNextStr = "";
            }
            return executeNextStr;
        }

        public void setExecuteNextStr(String executeNextStr) {
            this.executeNextStr = executeNextStr;
        }
    }

    @Override
    public boolean appMonitor(TaskInfo taskInfo) {
        long start = System.currentTimeMillis();
        logger.debug("【SMSA应用监控】开始 -----> {}",start);
        DateTime executeTime = taskInfo.getExecuteNextDate();

        try {
            List<JsmsAppServerInfo> jsmsAppServerInfoList = jsmsAppObserver.observerAppServer();

            Map<String, Object> data = new HashMap<>();
            List<JsmsAppServerInfo> red = new ArrayList<>();
            List<JsmsAppServerInfo> green = new ArrayList<>();
            Map<Integer,JsmsAppServerInfo> temp = new HashMap<>();
            for (JsmsAppServerInfo appServerInfo : jsmsAppServerInfoList) {
                if (appServerInfo.isAppException()){
                    red.add(appServerInfo);
                }else{
                    green.add(appServerInfo);
                }
            }
            if(red.isEmpty() && TaskConstant.ExecuteType.minute.equals(taskInfo.getExecuteType())){
                logger.debug("【SMSA应用监控】邮件发送 --> 所有程序正常,并且任务执行类型是分钟, 不需要发送邮件");
                return true;
            }
            data.put("red", red);
            data.put("green", green);
            data.put("date", executeTime.toString("yyyy-MM-dd HH:mm:ss"));
            logger.debug("【SMSA应用监控】分析结果数据 --> {}", JSON.toJSONString(data,true));
            String emailContent = loaderFreemaker(data,"app-monitor-template.ftl");
            if (StringUtil.isEmpty(emailContent)) {
                return true;
            }
            boolean isSend = false;
            isSend = sendMail(emailContent, executeTime,"SMSA应用监控");
            logger.debug("邮件发送 --> {}", isSend);
            return isSend;
        } catch (IOException e) {
            logger.error("未找到指定的模板文件 ----------> {}, {}", e.getMessage(), e);
        } catch (TemplateException e) {
            logger.error("FreeMaker 模板解析失败 ----------> {}, {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("发送邮件出现异常 ---> {}", e);
        }
        long end = System.currentTimeMillis();
        logger.info("【SMSA应用监控】结束，统计时间= {}，统计耗时={}ms", executeTime.toString("yyyy-MM-dd HH:mm:ss"), (end - start));

        return true;
    }


}
