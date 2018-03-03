package com.ucpaas.sms.task.constant;

import java.util.ArrayList;
import java.util.List;

import com.ucpaas.sms.task.model.Warning;
import com.ucpaas.sms.task.util.api.RestUtils.SmsTemplateId;

/**
 * 预警常量
 * 
 * @author xiejiaan
 */
public class WarningConstant {

	/**
	 * 预警标题
	 */
	public static final String warning_msg_title = "【云之讯】短信通道[%s]发生预警";
	/**
	 * 预警内容
	 */
	public static final String warning_msg_content = "【云之讯】短信通道[%s]发生预警：%s，请及时登录处理（%s）";

	/**
	 * 预警信息列表
	 */
	public static final List<Warning> warning_list = new ArrayList<Warning>();

	/*static {
		Warning warning = new Warning();
		warning.setName("通道成功率");
		warning.setColumn("reachrate");
		warning.setCondition("<");
		warning.setConditionName("低于");
		warning.setParamKey("ARRIVAL_RATE_WARN");
		warning.setUnit("%");
		warning.setSmsTemplateId(SmsTemplateId.warning_reachrate);
		warning_list.add(warning);

		warning = new Warning();
		warning.setName("通道及时率");
		warning.setColumn("timelyrate");
		warning.setCondition("<");
		warning.setConditionName("低于");
		warning.setParamKey("DELAY_TIME_WARN");
		warning.setUnit("秒");
		warning.setSmsTemplateId(SmsTemplateId.warning_delayavg);
		warning_list.add(warning);

		warning = new Warning();
		warning.setName("并发量");
		warning.setColumn("concurrency");
		warning.setCondition(">");
		warning.setConditionName("超过");
		warning.setParamKey("PEAK_EARLY_WARN");
		warning.setUnit("条/秒");
		warning.setSmsTemplateId(SmsTemplateId.warning_concurrency);
		warning_list.add(warning);
	}*/

}
