package com.ucpaas.sms.task.constant;

/**
 * 预警常量
 * 
 * @author xiejiaan
 */
public class AlarmConstant {

	/**
	 * 客户短信余量预警模板
	 */
	public static final String client_sms_alarm_template = "【云之讯】尊敬的用户，您的账户{}短信余额为{}条，为不影响您的正常使用，请联系{}及时充值购买";
	/**
	 * 销售预警模板
	 */
	public static final String agent_saler_sms_alarm_template = "【云之讯】您的代理商“{代理商ID}（{代理商名称}）”当前可用额度较低，请及时跟进充值";
	/**
	 * 销售预警模板
	 */
	public static final String client_saler_sms_alarm_template = "【云之讯】您的客户“{客户ID}（{客户名称}）”，当前短信余额为{余额}，请及时跟进充值确保使用通畅";
	/**
	 * 客户运营预警模板
	 */
	public static final String client_operator_sms_alarm_template = "【云之讯】{销售}的客户“{客户ID}（{客户名称}）”，当前短信余额为{余额}，请及时跟进充值确保使用通畅";


	public static final String agent_task_alarm_content = "【云之讯】{销售名字}的代理商“{代理商ID}-{代理商名称}”当前可用额度较低，请及时跟进充值确保使用通畅。";
	public static final String agent_sale_alarm_content = "【云之讯】您的代理商“{代理商ID}-{代理商名称}”当前可用额度较低，请及时跟进充值确保使用通畅。";
	public static final String agent_user_alarm_content = "【云之讯】尊敬的用户，您当前可用额度为{}，为避免影响您的客户群的发送量需求，请及时充值。";

	public static final String oem_client_task_alarm_content = "【余额提醒】{销售名字}的客户“{客户ID}（{客户名称}）”，当前{短信类型}短信余额为{}，请及时跟进充值确保使用通畅。";
	public static final String oem_client_sale_alarm_content = "【余额提醒】您的客户“{客户ID}（{客户名称}）”，当前短信余额为{}，请及时跟进充值确保使用通畅。";
	public static final String oem_client_user_alarm_content = "【余额提醒】您好，账户“{客户ID}-{客户名称}”的{短信类型}短信剩余量已低于{}{}，请及时充值。";

	/**
	 * 短信类型
	 */
	public enum SmsType {
		NOTIFY("0", "通知短信"),
		VERIFICATION_CODE("4", "验证码短信"),
		MARKETING("5", "营销短信"),
		USSD("7", "USSD短信"),
		FLASH("8", "闪信短信");

		private String value;
		private String desc;

		private SmsType(String value, String desc) {
			this.value = value;
			this.desc = desc;
		}

		public String getValue() {
			return value;
		}

		public String getDesc() {
			return desc;
		}

		@Override
		public String toString(){
			return this.value;
		}
	}
}
