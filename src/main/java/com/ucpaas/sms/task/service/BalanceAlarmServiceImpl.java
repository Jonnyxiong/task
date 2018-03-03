package com.ucpaas.sms.task.service;

import com.alibaba.fastjson.JSON;
import com.jsmsframework.common.enums.AgentType;
import com.jsmsframework.common.enums.AlarmWebId;
import com.jsmsframework.finance.entity.JsmsTaskAlarmSetting;
import com.jsmsframework.finance.enums.TaskAlarmType;
import com.jsmsframework.finance.service.JsmsTaskAlarmSettingService;
import com.ucpaas.sms.task.constant.TaskConstant;
import com.ucpaas.sms.task.model.TaskInfo;
import com.ucpaas.sms.task.util.AlarmCommonUtil;
import com.ucpaas.sms.task.util.UcpaasDateUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BalanceAlarmServiceImpl implements BalanceAlarmService {
	private static final Logger logger = LoggerFactory.getLogger(BalanceAlarmServiceImpl.class);

	@Autowired
	private BalanceAlarmDoService balanceAlarmDoService;

	@Autowired
	private JsmsTaskAlarmSettingService jsmsTaskAlarmSettingService;

	private boolean isNotRunning(JsmsTaskAlarmSetting taskAlarmSetting) {
		return taskAlarmSetting.getStatus().intValue() != 1;
	}

	private boolean alarmTimeRangeConfigError(JsmsTaskAlarmSetting taskAlarmSetting) {
		return !taskAlarmSetting.getBeginTime().matches("^(([0,1]\\d)|(2[0-3])):[0-5]\\d$")
				|| !taskAlarmSetting.getEndTime().matches("^(([0,1]\\d)|(2[0-3])):[0-5]\\d$");
	}

	private boolean notInTimeRange(DateTime now, JsmsTaskAlarmSetting taskAlarmSetting) {
		return !AlarmCommonUtil.compareTime(now, taskAlarmSetting.getBeginTime())
				|| AlarmCommonUtil.compareTime(now, taskAlarmSetting.getEndTime());
	}

	private void updateNextTime(TaskInfo taskInfo, JsmsTaskAlarmSetting taskAlarmSetting, DateTime now) {
		// 如果当前时间大于等于结束区间，将下次扫描时间更新 下一天的早上起始时间减去扫描区间
		String format = taskInfo.getExecuteType().getFormat();
		DateTime dateTime = UcpaasDateUtils.parseDate(taskInfo.getExecuteNext(), taskInfo.getExecuteType().getFormat());

		// 构造结束时间
		String endTime = taskAlarmSetting.getEndTime();
		DateTime endDate = DateTime.now().withHourOfDay(Integer.parseInt(endTime.substring(0, 2)))
				.withMinuteOfHour(Integer.parseInt(endTime.substring(3, 5))).withSecondOfMinute(0);

		// 当前时间 + 区间， 大于结束时间
		int qj = (taskAlarmSetting.getScanFrequecy() / 60000);
		DateTime nextExec = DateTime.now().plusMinutes(qj);
		if (dateTime.plusMinutes(qj).getMillis() >= endDate.getMillis()
				|| nextExec.getMillis() >= endDate.getMillis()) {
			// 构造开始时间
			String beginTime = taskAlarmSetting.getBeginTime();
			DateTime beginDate = DateTime.now().plusDays(1).withHourOfDay(Integer.parseInt(beginTime.substring(0, 2)))
					.withMinuteOfHour(Integer.parseInt(beginTime.substring(3, 5))).withSecondOfMinute(0);

			String newNextExec = beginDate.toString(format);
			taskInfo.setNewExecuteNext(newNextExec);
		} else {
			// 若时间不同，将执行时间更新为当前时间
			if (!dateTime.toString("yyyyMMdd").equals(now.toString("yyyyMMdd"))) {
				taskInfo.setNewExecuteNext(now.toString(format));
			}
		}
	}

	@Override
	public boolean doAlarm(TaskInfo taskInfo) {
		Boolean result = false;
		if (taskInfo.getTaskType() == TaskConstant.TaskType.client_balance_alarm) {
			List<JsmsTaskAlarmSetting> taskAlarmSettingList = jsmsTaskAlarmSettingService
					.getByTaskAlarmType(TaskAlarmType.NUM_ALARM);
			JsmsTaskAlarmSetting taskAlarmSetting = null;
			for (JsmsTaskAlarmSetting jsmsTaskAlarmSetting : taskAlarmSettingList) {

				if (jsmsTaskAlarmSetting.getWebId().intValue() == AlarmWebId.品牌客户端.getValue().intValue()) {
					taskAlarmSetting = jsmsTaskAlarmSetting;
					break;
				}
			}

			if (taskAlarmSetting != null) {
				return doClientBalanceAlarm(taskAlarmSetting, taskInfo);
			}

		} else if (taskInfo.getTaskType() == TaskConstant.TaskType.oem_client_balance_alarm) {
			List<JsmsTaskAlarmSetting> taskAlarmSettingList = jsmsTaskAlarmSettingService
					.getByTaskAlarmType(TaskAlarmType.NUM_ALARM);
			JsmsTaskAlarmSetting taskAlarmSetting = null;
			for (JsmsTaskAlarmSetting jsmsTaskAlarmSetting : taskAlarmSettingList) {

				if (jsmsTaskAlarmSetting.getWebId().intValue() == AlarmWebId.OEM客户端.getValue().intValue()) {
					taskAlarmSetting = jsmsTaskAlarmSetting;
					break;
				}
			}

			if (taskAlarmSetting != null) {
				return doOemClientBalanceAlarm(taskAlarmSetting, taskInfo);
			}
		} else if (taskInfo.getTaskType() == TaskConstant.TaskType.agent_balance_alarm) {
			List<JsmsTaskAlarmSetting> taskAlarmSettingList = jsmsTaskAlarmSettingService
					.getByTaskAlarmType(TaskAlarmType.AMOUNT_ALARM);
			JsmsTaskAlarmSetting taskAlarmSetting = null;
			for (JsmsTaskAlarmSetting jsmsTaskAlarmSetting : taskAlarmSettingList) {

				if (jsmsTaskAlarmSetting.getWebId().intValue() == AlarmWebId.代理商平台.getValue().intValue()) {
					taskAlarmSetting = jsmsTaskAlarmSetting;
					break;
				}
			}

			if (taskAlarmSetting != null) {
				return doAgentBalanceAlarm(taskAlarmSetting, taskInfo);
			}
		} else if (taskInfo.getTaskType() == TaskConstant.TaskType.oem_agent_balance_alarm) {
			List<JsmsTaskAlarmSetting> taskAlarmSettingList = jsmsTaskAlarmSettingService
					.getByTaskAlarmType(TaskAlarmType.AMOUNT_ALARM);
			JsmsTaskAlarmSetting taskAlarmSetting = null;
			for (JsmsTaskAlarmSetting jsmsTaskAlarmSetting : taskAlarmSettingList) {

				if (jsmsTaskAlarmSetting.getWebId().intValue() == AlarmWebId.OEM代理商平台.getValue().intValue()) {
					taskAlarmSetting = jsmsTaskAlarmSetting;
					break;
				}
			}

			if (taskAlarmSetting != null) {
				return doOemAgentBalanceAlarm(taskAlarmSetting, taskInfo);
			}
		}

		return result;
	}

	public boolean doClientBalanceAlarm(JsmsTaskAlarmSetting taskAlarmSetting, TaskInfo taskInfo) {
		DateTime now = new DateTime();

		long start = System.currentTimeMillis();
		logger.debug("=====开始品牌客户短信余额提醒: 当前时间 {} 提醒配置 {}", start, JSON.toJSONString(taskAlarmSetting));

		// 若一到临界点，就更新下次的信息
		updateNextTime(taskInfo, taskAlarmSetting, now);

		if (isNotRunning(taskAlarmSetting)) {
			logger.debug("预警任务禁用 --> 任务信息：{}", JSON.toJSONString(taskAlarmSetting));
			return true;
		}

		if (alarmTimeRangeConfigError(taskAlarmSetting)) {
			logger.debug("品牌客户短信余额提醒: 提醒时间段配置错误");
			return true;
		}

		if (notInTimeRange(now, taskAlarmSetting)) {
			logger.debug("品牌客户短信余额提醒: 当前时段未在提醒时间段内");
			return true;
		}

		logger.debug("品牌客户短信余额提醒: 执行周期为: {} 分钟, 执行时间为beginTime:{} + (N*{})分钟",
				taskAlarmSetting.getScanFrequecy() / 60000, taskAlarmSetting.getBeginTime(),
				taskAlarmSetting.getScanFrequecy() / 60000);

		// 执行品牌代理商的余额告警
		return balanceAlarmDoService.doAlarmClient(now, taskAlarmSetting);
	}

	public boolean doOemClientBalanceAlarm(JsmsTaskAlarmSetting taskAlarmSetting, TaskInfo taskInfo) {
		DateTime now = new DateTime();

		long start = System.currentTimeMillis();
		logger.debug("=====开始OEM客户短信余额提醒: 当前时间 {} 提醒配置 {}", start, JSON.toJSONString(taskAlarmSetting));

		// 若一到临界点，就更新下次的信息
		updateNextTime(taskInfo, taskAlarmSetting, now);

		if (isNotRunning(taskAlarmSetting)) {
			logger.debug("预警任务禁用 --> 任务信息：{}", JSON.toJSONString(taskAlarmSetting));
			return true;
		}

		if (alarmTimeRangeConfigError(taskAlarmSetting)) {
			logger.debug("OEM客户短信余额提醒: 提醒时间段配置错误");
			return true;
		}

		if (notInTimeRange(now, taskAlarmSetting)) {
			logger.debug("OEM客户短信余额提醒: 当前时段未在提醒时间段内");
			return true;
		}

		logger.debug("OEM客户短信余额提醒: 执行周期为: {} 分钟, 执行时间为beginTime:{} + (N*{})分钟",
				taskAlarmSetting.getScanFrequecy() / 60000, taskAlarmSetting.getBeginTime(),
				taskAlarmSetting.getScanFrequecy() / 60000);

		// 执行品牌代理商的余额告警
		return balanceAlarmDoService.doAlarmOemClient(now, taskAlarmSetting);
	}

	public boolean doAgentBalanceAlarm(JsmsTaskAlarmSetting taskAlarmSetting, TaskInfo taskInfo) {
		DateTime now = new DateTime();

		long start = System.currentTimeMillis();
		logger.debug("=====开始品牌代理商可用额度提醒: 当前时间 {} 提醒配置 {}", start, JSON.toJSONString(taskAlarmSetting));

		// 若一到临界点，就更新下次的信息
		updateNextTime(taskInfo, taskAlarmSetting, now);

		// 下次扫描

		if (isNotRunning(taskAlarmSetting)) {
			logger.debug("预警任务禁用 --> 任务信息：{}", JSON.toJSONString(taskAlarmSetting));
			return true;
		}

		if (alarmTimeRangeConfigError(taskAlarmSetting)) {
			logger.debug("品牌代理商可用额度提醒: 提醒时间段配置错误");
			return true;
		}

		if (notInTimeRange(now, taskAlarmSetting)) {
			logger.debug("品牌代理商可用额度提醒: 当前时段未在提醒时间段内");
			return true;
		}

		logger.debug("品牌代理商可用额度提醒: 执行周期为: {} 分钟, 执行时间为beginTime:{} + (N*{})分钟",
				taskAlarmSetting.getScanFrequecy() / 60000, taskAlarmSetting.getBeginTime(),
				taskAlarmSetting.getScanFrequecy() / 60000);

		// 执行品牌代理商的余额告警
		return balanceAlarmDoService.doAlarmAgent(now, taskAlarmSetting, AgentType.品牌代理商);
	}

	public boolean doOemAgentBalanceAlarm(JsmsTaskAlarmSetting taskAlarmSetting, TaskInfo taskInfo) {
		DateTime now = new DateTime();

		long start = System.currentTimeMillis();
		logger.debug("=====开始OEM代理商可用额度提醒: 当前时间 {} 提醒配置 {}", start, JSON.toJSONString(taskAlarmSetting));

		// 若一到临界点，就更新下次的信息
		updateNextTime(taskInfo, taskAlarmSetting, now);

		if (isNotRunning(taskAlarmSetting)) {
			logger.debug("预警任务禁用 --> 任务信息：{}", JSON.toJSONString(taskAlarmSetting));
			return true;
		}

		if (alarmTimeRangeConfigError(taskAlarmSetting)) {
			logger.debug("OEM代理商可用额度提醒: 提醒时间段配置错误");
			return true;
		}

		if (notInTimeRange(now, taskAlarmSetting)) {
			logger.debug("OEM代理商可用额度提醒: 当前时段未在提醒时间段内");
			return true;
		}

		logger.debug("OEM代理商可用额度提醒: 执行周期为: {} 分钟, 执行时间为beginTime:{} + (N*{})分钟",
				taskAlarmSetting.getScanFrequecy() / 60000, taskAlarmSetting.getBeginTime(),
				taskAlarmSetting.getScanFrequecy() / 60000);

		// 执行品牌代理商的余额告警
		return balanceAlarmDoService.doAlarmAgent(now, taskAlarmSetting, AgentType.OEM代理商);
	}
}
