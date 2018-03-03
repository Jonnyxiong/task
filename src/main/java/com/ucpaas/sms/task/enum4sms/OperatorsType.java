package com.ucpaas.sms.task.enum4sms;

/**
 * 通道运营商类型
 * @description 
 * @author huangwenjie
 * @date 2017年2月27日 上午11:39:52
 */
public enum OperatorsType {
	/**
	 * 全网
	 */
	all(0, "全网"),
	/**
	 * 移动
	 */
	cmcc(1, "移动"),
	/**
	 * 联通
	 */
	unicom(2, "联通"),
	/**
	 * 电信
	 */
	telecom(3, "电信"),
	/**
	 * 国际
	 */
	icom(4, "国际"),
	/**
	 * 拦截
	 */
	interupt(-1, "拦截");

	Integer value;
	String desc;

	private OperatorsType(Integer value, String desc) {
		this.value = value;
		this.desc = desc;
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

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public static String getNameByValue(Integer value) {
		switch (value) {
		case 0:
			return all.desc;
		case 1:
			return cmcc.desc; 
		case 2:
			return unicom.desc; 
		case 3:
			return telecom.desc; 
		case 4:
			return icom.desc; 
		case -1:
			return interupt.desc; 
		default:
			return "";
		}
	}
}
