package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.model.TaskInfo;

/**
 * 阿里相关数据统计
 * 
 */
public interface MajorClientsService {
	/**
	 * 预付费客户发送详情
	 */
	boolean prepaymentClientDailyDetail(TaskInfo taskInfo);
	/**
	 * 后付费客户发送详情
	 */
	boolean postpaidClientDailyDetail(TaskInfo taskInfo);

}
