package com.ucpaas.sms.task.util;

import java.util.Map;
import java.util.Objects;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Months;
import org.joda.time.Weeks;
import org.joda.time.Years;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ucpaas.sms.task.constant.DbConstant.DbType;
import com.ucpaas.sms.task.constant.TaskConstant.ExecuteType;
import com.ucpaas.sms.task.constant.TaskConstant.ScanType;
import com.ucpaas.sms.task.constant.TaskConstant.TaskType;
import com.ucpaas.sms.task.model.TaskInfo;

/**
 * 任务工具类
 * 
 * @author xiejiaan
 */
public class TaskUtils {
	private static final Logger logger = LoggerFactory.getLogger(TaskUtils.class);

	/**
	 * 根据taskMap生成TaskInfo
	 * 
	 * @param taskMap
	 * @return
	 */
	public static TaskInfo getTaskInfo(Map<String, Object> taskMap) {
		TaskInfo taskInfo = new TaskInfo();
		taskInfo.setTaskId(Integer.valueOf(taskMap.get("task_id").toString()));
		taskInfo.setTaskName(taskMap.get("task_name").toString());
		taskInfo.setDbType(DbType.getInstance(Integer.valueOf(taskMap.get("db_type").toString())));
		taskInfo.setTaskType(TaskType.getInstance(Integer.parseInt(taskMap.get("task_type").toString())));
		taskInfo.setProcedureName(Objects.toString(taskMap.get("procedure_name"), null));
		taskInfo.setExecuteType(ExecuteType.getInstance(Integer.parseInt(taskMap.get("execute_type").toString())));

		if (taskInfo.getExecuteType() != ExecuteType.empty) {
			taskInfo.setExecuteNext(taskMap.get("execute_next").toString());
			taskInfo.setExecutePeriod(Integer.valueOf(taskMap.get("execute_period").toString()));
			taskInfo = setNewExecuteNext(taskInfo);
		}
		taskInfo.setScanType(ScanType.getInstance(Integer.parseInt(taskMap.get("scan_type").toString())));
		taskInfo.setScanNext(UcpaasDateUtils.parseDate(taskMap.get("scan_next").toString(), "yyyy-MM-dd HH:mm:ss"));
		taskInfo.setScanPeriod(Integer.valueOf(taskMap.get("scan_period").toString()));
		taskInfo.setScanExecute(taskMap.get("scan_execute").toString().equals("1") ? true : false);
		return taskInfo;
	}

	/**
	 * 设置executePrev、executeNextDate、newExecuteNext
	 * 
	 * @param taskInfo
	 * @return
	 */
	public static TaskInfo setNewExecuteNext(TaskInfo taskInfo) {
		ExecuteType executeType = taskInfo.getExecuteType();
		if (executeType != ExecuteType.empty) {
			DateTime executePrev = null;
			DateTime executeNextDate = null;
			DateTime newExecuteNext = null;

			String format = executeType.getFormat();
			String executeNext = taskInfo.getExecuteNext();
			int executePeriod = taskInfo.getExecutePeriod();

			switch (executeType) {
			case minute:
				executeNextDate = UcpaasDateUtils.parseDate(executeNext, format);
				executePrev = executeNextDate.minusMinutes(executePeriod);
				newExecuteNext = executeNextDate.plusMinutes(executePeriod);
				break;
			case hour:
				executeNextDate = UcpaasDateUtils.parseDate(executeNext, format);
				executePrev = executeNextDate.minusHours(executePeriod);
				newExecuteNext = executeNextDate.plusHours(executePeriod);
				break;
			case day:
				executeNextDate = UcpaasDateUtils.parseDate(executeNext, format);
				executePrev = executeNextDate.minusDays(executePeriod);
				newExecuteNext = executeNextDate.plusDays(executePeriod);
				break;
			case week:
				executeNextDate = UcpaasDateUtils.parseDate(executeNext, format);
				executePrev = executeNextDate.minusWeeks(executePeriod);
				newExecuteNext = executeNextDate.plusWeeks(executePeriod);
				break;
			case month:
				executeNextDate = UcpaasDateUtils.parseDate(executeNext, format);
				executePrev = executeNextDate.minusMonths(executePeriod);
				newExecuteNext = executeNextDate.plusMonths(executePeriod);
				break;
			case season:
				executeNextDate = UcpaasDateUtils.parseDate(executeNext, format);
				executePrev = executeNextDate.minusMonths(3 * executePeriod);
				newExecuteNext = executeNextDate.plusMonths(3 * executePeriod);
				break;
			case year:
				executeNextDate = UcpaasDateUtils.parseDate(executeNext, format);
				executePrev = executeNextDate.minusYears(executePeriod);
				newExecuteNext = executeNextDate.plusYears(executePeriod);
				break;
			default:
				logger.error("执行类型错误：taskInfo={}", taskInfo);
				return null;
			}
			taskInfo.setExecutePrev(executePrev.toString(format));
			taskInfo.setExecuteNextDate(executeNextDate);
			taskInfo.setNewExecuteNext(newExecuteNext.toString(format));
		}
		return taskInfo;
	}

	/**
	 * 获取newScanNext（新的scanNext）
	 * 
	 * @param taskInfo
	 * @return
	 */
	public static String getNewScanNext(TaskInfo taskInfo) {
		ScanType scanType = taskInfo.getScanType();
		DateTime scanNext = taskInfo.getScanNext();
		int scanPeriod = taskInfo.getScanPeriod();
		DateTime newScanNext = null;

		DateTime now = DateTime.now();
		int between;// scanNext（下次扫描时间）距离当前时间

		switch (scanType) {
		case minute:
			between = Minutes.minutesBetween(scanNext, now).getMinutes();
			newScanNext = scanNext.plusMinutes((between / scanPeriod + 1) * scanPeriod);
			break;
		case hour:
			between = Hours.hoursBetween(scanNext, now).getHours();
			newScanNext = scanNext.plusHours((between / scanPeriod + 1) * scanPeriod);
			break;
		case day:
			between = Days.daysBetween(scanNext, now).getDays();
			newScanNext = scanNext.plusDays((between / scanPeriod + 1) * scanPeriod);
			break;
		case week:
			between = Weeks.weeksBetween(scanNext, now).getWeeks();
			newScanNext = scanNext.plusWeeks((between / scanPeriod + 1) * scanPeriod);
			break;
		case month:
			between = Months.monthsBetween(scanNext, now).getMonths();
			newScanNext = scanNext.plusMonths((between / scanPeriod + 1) * scanPeriod);
			break;
		case season:
			between = Months.monthsBetween(scanNext, now).getMonths();
			scanPeriod = scanPeriod * 3;
			newScanNext = scanNext.plusMonths((between / scanPeriod + 1) * scanPeriod);
			break;
		case year:
			between = Years.yearsBetween(scanNext, now).getYears();
			newScanNext = scanNext.plusYears((between / scanPeriod + 1) * scanPeriod);
			break;
		default:
			logger.error("扫描类型错误：taskInfo={}", taskInfo);
			return null;
		}

		return newScanNext.toString("yyyy-MM-dd HH:mm:ss");
	}

	public static void main(String[] args) {
		TaskInfo taskInfo = new TaskInfo();
		taskInfo.setScanType(ScanType.minute);
		taskInfo.setScanNext(UcpaasDateUtils.parseDate("2014-10-17 12:05:00", "yyyy-MM-dd HH:mm:ss"));
		taskInfo.setScanPeriod(5);

		String newScanNext = getNewScanNext(taskInfo);
		System.out.println(taskInfo.getScanNext());
		System.out.println(newScanNext);
	}

}
