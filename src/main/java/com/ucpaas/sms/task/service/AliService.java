package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.model.TaskInfo;

/**
 * 阿里相关数据统计
 * 
 */
public interface AliService {

	boolean dailyDetail(TaskInfo taskInfo);
 
}
