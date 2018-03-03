package com.ucpaas.sms.task.enum4sms;

public enum RecordChannelStatisticsType {
	/**
	 * 每日详细数据
	 */
	daily(0),
	/**
	 * 每日合计数据
	 */
	dailySum(1),
	/**
	 * 每月详细数据
	 */
	monthly(2),
	/**
	 * 每月合计数据
	 */
	monthlySum(3);
	
	Integer value;

	
	private RecordChannelStatisticsType(Integer value) {
		this.value = value;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}
	
	
}
