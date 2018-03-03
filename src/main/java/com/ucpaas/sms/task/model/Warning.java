package com.ucpaas.sms.task.model;

import com.ucpaas.sms.task.util.api.RestUtils.SmsTemplateId;

/**
 * 预警信息
 * 
 * @author xiejiaan
 */
public class Warning {
	private String name;// 名称
	private String column;// t_monitor_log表中的字段名
	private String condition;// 条件，如>、<
	private String conditionName;// 条件名称
	private String paramKey;// 参数表中配置的阀值key
	private String unit;// 单位
	private SmsTemplateId smsTemplateId;// 短信模板的id

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getConditionName() {
		return conditionName;
	}

	public void setConditionName(String conditionName) {
		this.conditionName = conditionName;
	}

	public String getParamKey() {
		return paramKey;
	}

	public void setParamKey(String paramKey) {
		this.paramKey = paramKey;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public SmsTemplateId getSmsTemplateId() {
		return smsTemplateId;
	}

	public void setSmsTemplateId(SmsTemplateId smsTemplateId) {
		this.smsTemplateId = smsTemplateId;
	}
}
