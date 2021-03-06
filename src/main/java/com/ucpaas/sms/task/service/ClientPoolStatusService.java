package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.model.TaskInfo;

/**
 * 客户短信池状态处理服务
 * 
 */
public interface ClientPoolStatusService {

	/**
	 * 客户短信池处理服务
	 * 
	 * @return 是否成功
	 */
	boolean execute(TaskInfo taskInfo);

}
