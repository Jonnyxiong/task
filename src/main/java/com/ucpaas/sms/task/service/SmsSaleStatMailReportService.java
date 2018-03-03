package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.model.TaskInfo;

/**
 * 每日短信销售情况邮件报表
 * 
 */
public interface SmsSaleStatMailReportService {

	/**
	 * 每日短信销售情况邮件报表
	 * 
	 * @return 是否成功
	 */
	boolean statReportAndSend(TaskInfo taskInfo);

	boolean saleReportAndSend(TaskInfo taskInfo);

}
