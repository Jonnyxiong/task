package com.ucpaas.sms.task.enum4sms;

/**
 * Created by lpjLiu on 2017/7/10. 短信类型
 */
public enum SMSType {
	通知短信(0, "通知短信"), 验证码短信(4, "验证码短信"), 营销短信(5, "营销短信"), 告警短信(6, "告警短信"), USSD(7, "USSD"), 闪信(8, "闪信");

	private Integer value;
	private String desc;

	private SMSType(Integer value, String desc) {
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
