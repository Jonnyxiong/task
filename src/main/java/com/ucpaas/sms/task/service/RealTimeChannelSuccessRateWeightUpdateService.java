package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.model.TaskInfo;

import java.text.ParseException;

public interface RealTimeChannelSuccessRateWeightUpdateService {

	/**
	 * 实时更新通道属性实时区间权重
	 * @param taskInfo
	 * @return
	 * @throws ParseException 
	 * @throws InterruptedException 
	 */
	boolean doTheJob(TaskInfo taskInfo) throws ParseException, InterruptedException;
}
