package com.ucpaas.sms.task.enum4sms;

public enum ComplaintEnum {
	有效(0, "有效数据"), 无效(1, "无效数据"),日统计(0,"日统计"),月统计(1,"月统计");
	private Integer value;
	private String desc;

	private ComplaintEnum(Integer value, String desc) {
		this.value = value;
		this.desc = desc;
	}

	public Integer getValue() {
		return value;
	}

	public String getDesc() {
		return desc;
	}

	public static String getDescByValue(int value) {
		String result = null;
		for (SMSType s : SMSType.values()) {
			if (value == s.getValue()) {
				result = s.getDesc();
				break;
			}
		}
		return result;
	}
}
