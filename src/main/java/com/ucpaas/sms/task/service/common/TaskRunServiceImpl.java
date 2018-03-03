package com.ucpaas.sms.task.service.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ucpaas.sms.task.constant.TaskConstant.TaskStatus;
import com.ucpaas.sms.task.dao.MessageMasterDao;
import com.ucpaas.sms.task.model.TaskInfo;
import com.ucpaas.sms.task.util.TaskUtils;

/**
 * 定时任务业务
 * 
 * @author xiejiaan
 */
@Service
public class TaskRunServiceImpl implements TaskRunService {
	@Autowired
	private MessageMasterDao ucpaasMessageDao;

	@Override
	public List<Map<String, Object>> queryTask() {
		return ucpaasMessageDao.getSearchList("task.query_task", null);
	}

	@Override
	public Long insertLog(TaskInfo taskInfo, String remark) {
		Map<String, Object> sqlParams = new HashMap<String, Object>();
		sqlParams.put("task_id", taskInfo.getTaskId());
		sqlParams.put("data_date", taskInfo.getExecutePrev());
		sqlParams.put("remark", remark);
		ucpaasMessageDao.insert("task.insert_log", sqlParams);
		return Long.valueOf(sqlParams.get("log_id").toString());
	}

	@Override
	public void updateLog(Long logId, boolean result, String remark) {
		Map<String, Object> sqlParams = new HashMap<String, Object>();
		sqlParams.put("log_id", logId);
		sqlParams.put("remark", remark);
		sqlParams.put("status", result ? 2 : 3);
		ucpaasMessageDao.update("task.update_log", sqlParams);
	}

	@Override
	public void updateStatus(Integer taskId, TaskStatus taskStatus) {
		Map<String, Object> sqlParams = new HashMap<String, Object>();
		sqlParams.put("task_id", taskId);
		sqlParams.put("status", taskStatus.getValue());
		ucpaasMessageDao.update("task.update_task", sqlParams);
	}

	@Override
	public void updateExecuteNext(TaskInfo taskInfo) {
		Map<String, Object> sqlParams = new HashMap<String, Object>();
		sqlParams.put("task_id", taskInfo.getTaskId());
		sqlParams.put("execute_next", taskInfo.getNewExecuteNext());
		ucpaasMessageDao.update("task.update_task", sqlParams);
	}

	@Override
	public void updateScanNext(TaskInfo taskInfo) {
		Map<String, Object> sqlParams = new HashMap<String, Object>();
		sqlParams.put("task_id", taskInfo.getTaskId());
		sqlParams.put("scan_next", TaskUtils.getNewScanNext(taskInfo));
		ucpaasMessageDao.update("task.update_task", sqlParams);
	}

}
