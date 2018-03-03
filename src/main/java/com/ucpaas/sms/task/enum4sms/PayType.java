package com.ucpaas.sms.task.enum4sms;

/**
 * 短信报表中付费类型
 *
 */
public enum PayType {
	/**
	 * 预付费
	 */
	prepayment(0,"预付费"),
	/**
	 * 后付费
	 */
	postpay(1,"后付费"),
	/**
	 * 日合计数据的paytype
	 */
	daily(-1,"日合计数据"),
	/**
	 * 月合计数据的paytype
	 */
	montyly(-1,"月合计数据");
	
	Integer value;
	String desc;

	
	private PayType(Integer value,String desc) {

		this.value = value;
		this.desc= desc;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	public String getDesc() {
		return desc;
	}
}
