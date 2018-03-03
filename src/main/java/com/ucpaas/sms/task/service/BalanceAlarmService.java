package com.ucpaas.sms.task.service;

import com.jsmsframework.finance.entity.JsmsTaskAlarmSetting;
import com.ucpaas.sms.task.model.TaskInfo;

public interface BalanceAlarmService {

	boolean doAlarm(TaskInfo taskInfo);

	boolean doClientBalanceAlarm(JsmsTaskAlarmSetting taskAlarmSetting, TaskInfo taskInfo);

	boolean doOemClientBalanceAlarm(JsmsTaskAlarmSetting taskAlarmSetting, TaskInfo taskInfo);

	boolean doAgentBalanceAlarm(JsmsTaskAlarmSetting taskAlarmSetting, TaskInfo taskInfo);

	boolean doOemAgentBalanceAlarm(JsmsTaskAlarmSetting taskAlarmSetting, TaskInfo taskInfo);

}
