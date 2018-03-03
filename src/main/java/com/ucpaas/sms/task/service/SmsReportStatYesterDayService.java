package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.model.TaskInfo;

/**
 * 每日短信报表统计报表统计
 * 
 */
public interface SmsReportStatYesterDayService {

	boolean stat(TaskInfo taskInfo);
 
}
