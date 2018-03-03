package com.ucpaas.sms.task.service;

/**
 * 预警任务
 * 
 * @author xiejiaan
 */
public interface WarningService {

	/**
	 * smsp v3.3 通道成功率、及时率预警任务
	 * 
	 * @return 是否成功
	 */
	boolean execute();
}
