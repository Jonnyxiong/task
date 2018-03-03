package com.ucpaas.sms.task.service.common;

import java.util.List;
import java.util.Map;

import com.ucpaas.sms.task.constant.TaskConstant.TaskStatus;
import com.ucpaas.sms.task.model.TaskInfo;

/**
 * 定时任务业务
 * 
 * @author xiejiaan
 */
public interface TaskRunService {

	/**
	 * 查询需要执行的任务
	 * 
	 * @return
	 */
	List<Map<String, Object>> queryTask();

	/**
	 * 添加任务日志
	 * 
	 * @param taskInfo
	 * @param remark
	 * @return 返回日志主键log_id
	 */
	Long insertLog(TaskInfo taskInfo, String remark);

	/**
	 * 修改任务日志
	 * 
	 * @param logId
	 * @param result
	 *            执行结果：是否执行成功
	 * @param remark
	 */
	void updateLog(Long logId, boolean result, String remark);

	/**
	 * 修改任务状态
	 * 
	 * @param taskId
	 * @param taskStatus
	 */
	void updateStatus(Integer taskId, TaskStatus taskStatus);

	/**
	 * 修改下次执行时间
	 * 
	 * @param taskInfo
	 */
	void updateExecuteNext(TaskInfo taskInfo);

	/**
	 * 修改下次扫描时间
	 * 
	 * @param taskInfo
	 */
	void updateScanNext(TaskInfo taskInfo);

}
