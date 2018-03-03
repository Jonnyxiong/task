package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.model.TaskInfo;

/**
 * 短信监控相关定时任务
 * 
 */
public interface ChannelQaRealTimeService {

	/**
	 * 短信客户发送速率统计
	 * <br>每5分钟统计一次</br>
	 * 
	 * @param taskInfo
	 * @return
	 */
	boolean clientSendSpeed(TaskInfo taskInfo);
	
	/**
	 * 短信Access MQ队列消息数统计
	 * <br>每1分钟统计一次</br>
	 * 
	 * @param taskInfo
	 * @return
	 */
	boolean mqQueueMessageStat(TaskInfo taskInfo);
	
	/**
	 * 短信通道质量指数统计
	 * <br>每5分钟统计一次</br>
	 * 
	 * @param taskInfo
	 * @return
	 */
	boolean channelQualityIndexesStat(TaskInfo taskInfo);
	
	/**
	 * 短信通道错误统计
	 * <br>每5分钟统计一次</br>
	 * 
	 * @param taskInfo
	 * @return
	 */
	boolean channelErrorStat(TaskInfo taskInfo);
	
	/**
	 * 客户发送质量指数统计
	 * <br>每5分钟统计一次</br>
	 * 
	 * @param taskInfo
	 * @return
	 */
	boolean clientQualityIndexesStat(TaskInfo taskInfo);
	
	/**
	 * 客户成功率指数统计
	 * <br>每5分钟统计一次</br>
	 * 
	 * @param taskInfo
	 * @return
	 */
	boolean clientSuccessRateRealtime(TaskInfo taskInfo);
	
	/**
	 * 通道成功率指数统计
	 * <br>每5分钟统计一次</br>
	 * 
	 * @param taskInfo
	 * @return
	 */
	boolean channelSuccessRateRealtime(TaskInfo taskInfo);

}
