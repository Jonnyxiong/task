package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.model.TaskInfo;

/**
 * 定时任务监控程序
 */
public interface MonitorService {

	boolean taskMonitor(TaskInfo taskInfo);

	/**
	 * 【邮件】监控应用运行情况, 应用异常则发送邮件
	 * @param taskInfo
	 * @return
	 */
	boolean appMonitor(TaskInfo taskInfo);

}
