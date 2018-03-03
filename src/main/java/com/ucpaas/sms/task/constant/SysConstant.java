package com.ucpaas.sms.task.constant;

/**
 * 系统常量
 * 
 * @author xiejiaan
 */
public class SysConstant {

	/**
	 * 项目中的金额费率，1元=1000000
	 */
	public static final String money_rate = "1000000";
	public static final int money_rate_int = 1000000;

	/**
	 * 系统应用的app_sid
	 */
	public static final String sys_app_sid = "0";
	/**
	 * 超级管理员的sid
	 */
	public static final String super_admin_sid = "d137a9184dd1b84a6eae1ff5ccbc6bc9";
	/**
	 * 超级管理员的token
	 */
	public static final String super_admin_token = "d137a9184dd1b84a6eae1ff5ccbc6bc8";

	public static final String SALE_MAIL_REPORT_TEXT = "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
			"<title>销售报表</title>\n" +
			"<div class=\"email_box\">\n" +
			"<div  style=\"width:830px; border-bottom:1px #aaa dashed; overflow:hidden;\">\n" +
			"<p>你好！</p>\n" +
			"<p>DateTime的新短信销售情况通报请查看附件内容(统计规则为拆分后的1+3)，谢谢！</p>\n" +
			"<p>其中：</p>\n" +
			"<p>销售数据总计如下：</p>\n" +
			"<table border=\"1\">\n" +
			"<thead>\n" +
			"<tr>\n" +
			"<td>销售名称</td>\n" +
			"<td>销售数量</td>\n" +
			"<td>目标</td>\n" +
			"<td>月加权总数</td>\n" +
			"</tr>\n" +
			"</thead>\n" +
			"<tbody>\n" +
			"<tr>\n" +
			"belongsaleText\n" +
			"</tbody>\n" +
			"</table>\n" +
			"<p>本月实际已完成销售总条数total条，加权结果总量为weightingTotal条，绝对平均值后得到的总数为averageTotal条；</p>\n" +
			"</body></html>\n";

	public static final String BELONGSALE_TEXT = "<tr>\n" +
			"<td>belongSaleName</td><td>belongSaleNumber</td><td>belongSaleTargetNumber</td><td>belongSaleWeightingTotal</td>\n"
			+"</tr>";

}
