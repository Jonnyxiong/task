package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.model.TaskInfo;

/**
 * OEM代理商计费
 * 
 */
public interface OemAgentClientChargeService {

	/**
	 * 后付费计费
	 * 
	 * @return 是否成功
	 */
	boolean houfufeiCharge(TaskInfo taskInfo);

}
