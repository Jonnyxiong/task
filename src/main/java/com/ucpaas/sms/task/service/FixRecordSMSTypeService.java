package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.model.TaskInfo;

/**
 * 修复Record的SMSType
 * 
 */
public interface FixRecordSMSTypeService {

	/**
	 * 修复Record的SMSType
	 * 
	 * @return 是否成功
	 */
	boolean fixSMSType(TaskInfo taskInfo);

}
