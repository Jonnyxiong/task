package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.model.TaskInfo;

/**
 * 代理商定时任务业务
 * 
 */
public interface AgentTaskService {

	/**
	 * 代理商佣金计算（每日）
	 * @param taskInfo
	 * @return 是否成功
	 */
	boolean commissionCompute(TaskInfo taskInfo);
	
	/**
	 * 代理商返利（每季度）
	 * @param taskInfo
	 * @return
	 */
	boolean rebateCompute(TaskInfo taskInfo);

}
