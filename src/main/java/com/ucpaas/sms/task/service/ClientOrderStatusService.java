package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.model.TaskInfo;

/**
 * 订单状态处理服务
 * 
 */
public interface ClientOrderStatusService {

	/**
	 * 订单状态处理服务
	 * 
	 * @return 是否成功
	 */
	boolean execute(TaskInfo taskInfo);

	/**
	 * 预付费客户余额预警
	 * @return 是否成功
	 */
	boolean clientBalanceAlarm(TaskInfo taskInfo);
}
