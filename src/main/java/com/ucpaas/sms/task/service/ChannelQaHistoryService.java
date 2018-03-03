package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.model.TaskInfo;

/**
 * 通道质量历史监控统计任务
 */

public interface ChannelQaHistoryService {
	
	/**
	 * 短信通道质量指数统计
	 * <br>每天统计前天的数据，如：1月3日统计1月1日的数据</br>
	 * 
	 * @param taskInfo
	 * @return
	 */
	boolean channelQualityIndexesStat(TaskInfo taskInfo);

	/**
	 * 短信用户质量指数统计
	 * <br>每天统计昨天的数据，如：1月3日统计1月1日的数据</br>
	 * 
	 * @param taskInfo
	 * @return
	 */
	boolean clientQualityIndexesStat(TaskInfo taskInfo);

}
