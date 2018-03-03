package com.ucpaas.sms.task.service;

import java.text.ParseException;

import com.ucpaas.sms.task.model.TaskInfo;

public interface HalfHourAccessRecordService {

	/**
	 * 每半小时统计流水并发邮件
	 * @param taskInfo
	 * @return
	 * @throws ParseException 
	 * @throws InterruptedException 
	 */
	boolean doTheJob(TaskInfo taskInfo) throws ParseException, InterruptedException;

    boolean doTheNewJob(TaskInfo taskInfo) throws ParseException;
}
