package com.ucpaas.sms.task.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joda.time.DateTime;

import com.ucpaas.sms.task.constant.DbConstant.DbType;
import com.ucpaas.sms.task.constant.TaskConstant.ExecuteType;
import com.ucpaas.sms.task.constant.TaskConstant.ScanType;
import com.ucpaas.sms.task.constant.TaskConstant.TaskType;

/**
 * 任务信息
 * 
 * @author xiejiaan
 */
public class TaskInfo {
	private Integer taskId;// 任务id
	private String taskName;// 任务名称
	private TaskType taskType;// 任务类型
	private DbType dbType;// 数据库类型
	private String procedureName;// 存储过程名称
	private ExecuteType executeType;// 执行类型
	private String executeNext;// 下次执行时间
	private Integer executePeriod;// 执行周期
	private ScanType scanType;// 扫描类型
	private DateTime scanNext;// 下次扫描时间
	private Integer scanPeriod;// 扫描周期
	private boolean scanExecute;// 是否每次扫描都执行

	private String executePrev;// 上次执行时间
	private DateTime executeNextDate;// 下次执行时间
	private String newExecuteNext;// 新的executeNext

	private String toString;

	@Override
	public String toString() {
		if (toString == null) {
			toString = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("taskId", taskId)
					.append("taskName", taskName).append("taskType", taskType).append("procedureName", procedureName)
					.append("executeType", executeType).append("executeNext", executeNext)
					.append("executePeriod", executePeriod).append("scanType", scanType).append("scanNext", scanNext)
					.append("scanPeriod", scanPeriod).append("scanExecute", scanExecute)
					.append("executePrev", executePrev).append("executeNextDate", executeNextDate)
					.append("newExecuteNext", newExecuteNext).toString();
		}
		return toString;
	}

	public Integer getTaskId() {
		return taskId;
	}

	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public TaskType getTaskType() {
		return taskType;
	}

	public void setTaskType(TaskType taskType) {
		this.taskType = taskType;
	}

	public String getProcedureName() {
		return procedureName;
	}

	public void setProcedureName(String procedureName) {
		this.procedureName = procedureName;
	}

	public ExecuteType getExecuteType() {
		return executeType;
	}

	public void setExecuteType(ExecuteType executeType) {
		this.executeType = executeType;
	}

	public String getExecuteNext() {
		return executeNext;
	}

	public void setExecuteNext(String executeNext) {
		this.executeNext = executeNext;
	}

	public Integer getExecutePeriod() {
		return executePeriod;
	}

	public void setExecutePeriod(Integer executePeriod) {
		this.executePeriod = executePeriod;
	}

	public ScanType getScanType() {
		return scanType;
	}

	public void setScanType(ScanType scanType) {
		this.scanType = scanType;
	}

	public DateTime getScanNext() {
		return scanNext;
	}

	public void setScanNext(DateTime scanNext) {
		this.scanNext = scanNext;
	}

	public Integer getScanPeriod() {
		return scanPeriod;
	}

	public void setScanPeriod(Integer scanPeriod) {
		this.scanPeriod = scanPeriod;
	}

	public boolean isScanExecute() {
		return scanExecute;
	}

	public void setScanExecute(boolean scanExecute) {
		this.scanExecute = scanExecute;
	}

	public String getExecutePrev() {
		return executePrev;
	}

	public void setExecutePrev(String executePrev) {
		this.executePrev = executePrev;
	}

	public DateTime getExecuteNextDate() {
		return executeNextDate;
	}

	public void setExecuteNextDate(DateTime executeNextDate) {
		this.executeNextDate = executeNextDate;
	}

	public String getNewExecuteNext() {
		return newExecuteNext;
	}

	public void setNewExecuteNext(String newExecuteNext) {
		this.newExecuteNext = newExecuteNext;
	}

	public String getToString() {
		return toString;
	}

	public void setToString(String toString) {
		this.toString = toString;
	}

	public DbType getDbType() {
		return dbType;
	}

	public void setDbType(DbType dbType) {
		this.dbType = dbType;
	}

}
