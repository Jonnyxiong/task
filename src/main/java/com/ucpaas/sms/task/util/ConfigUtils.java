package com.ucpaas.sms.task.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 系统配置工具类
 * 
 * @author xiejiaan
 */
@Component
public class ConfigUtils {
	private static final Logger logger = LoggerFactory.getLogger(ConfigUtils.class);

	public static String system_version;
	/**
	 * 配置文件路径
	 */
	public static String config_file_path;

	/**
	 * rest接口的域名
	 */
	public static String rest_domain;
	
	/**
	 * rest接口的版本
	 */
	public static String rest_version;
	
	/**
	 * 请求通道的url
	 */
	public static String post_channel_url;
	
	/**
	 * 请求通道的body
	 */
	public static String post_channel_body;
	
	/**
	 * 季度返利日期(几号季度返利)，由于3天后流水才正确，所以一般都定在3号结算
	 */
	public static String session_day;
	
	/**
	 * 季度返利，是否可以每月重跑，默认是不重跑，重跑风险在于代理商账户金额会发生改变
	 */
	public static String repeat_rebate_compute = "false";
	
	/**
	 * RabbitMq 页面控制台api地址、账号、密码
	 */
	public static String rabbit_api_url;
	
	/**
	 * RabbitMq 页面控制台api账号
	 */
	public static String rabbit_api_username;
	
	/**
	 * RabbitMq 页面控制台api密码
	 */
	public static String rabbit_api_password;
	
	/**
	 * 图片服务器地址
	 */
	public static String img_service_url;
	
	/**
	 * 图片服务器：上传图片地址
	 */
	public static String img_service_upload_url;
	
	/**
	 * 图片服务器：获取图片地址
	 */
	public static String img_service_scan_url;
	
	/**
	 * 一个月的第几天发送邮件
	 */
	public static String send_day;
	
	/**
	 * 图片临时保存地址
	 */
	public static String img_temp_path;
	
	/**
	 * 品牌代理商系统的地址
	 */
	public static String agent_site_url;

	/**
	 * 告警短信请求URL
	 */
	public static String smsp_access_url;
	/**
	 * 告警短信使用的短信账号
	 */
	public static String sms_alarm_clientid;
	/**
	 * 告警短信使用的短信账号对应的密码
	 */
	public static String sms_alarm_paasword;
	/**
	 * 文件本地保存路径
	 */
	public static String save_path;

	/**
	 * 每日备份审核记录，备份往前的时间间隔
	 */
	public static String audit_sms_bak_before_day;

	/**
	 * 每日备份审核记录，备份批次处理数量
	 */
	public static String audit_sms_bak_batch_count;

	/**
	 * #每日备份审核记录任务的线程数
	 */
    public static String audit_sms_bak_thread_size;
	/**
	 * 每日备份审核记录任务的记录数(单个线程)
	 */
	public static String audit_sms_bak_size;


	public static String audit_sms_bak_running_begin;

	public static String audit_sms_bak_running_end;
	public static String pay_cancel_time;
    public static String pay_fail_time;


	/**
	 * 每日备份短信审核与关键字记录，备份往前的时间间隔
	 */
	public static String audit_keyword_record_bak_before_day;

	/**
	 * 每日备份短信审核与关键字记录，备份批次处理数量
	 */
	public static String audit_keyword_record_bak_batch_count;

	/**
	 * 每日备份短信审核与关键字记录任务的线程数
	 */
	public static String audit_keyword_record_bak_thread_size;
	/**
	 * 每日备份短信审核与关键字记录任务的记录数(单个线程)
	 */
	public static String audit_keyword_record_bak_size;

    public static String audit_keyword_record_bak_running_begin;

    public static String audit_keyword_record_bak_running_end;

	/**
	 * 阿里账号配置
	 */
    public static String ali_clients;

	/**
	 * 初始化
	 */
	@PostConstruct
	public void setSpring_profiles_active() {
		String path = ConfigUtils.class.getClassLoader().getResource("").getPath() ;
		config_file_path = path + "system.properties";

		initValue();
		
		logger.info("\n\n-------------------------【smsp-task-{} 启动】\n加载配置文件：\n{}\n", system_version,
				config_file_path);
		
	}

	
	/**
	 * 初始化配置项的值
	 */
	private void initValue() {
		Field[] fields = ConfigUtils.class.getFields();
		Object fieldValue = null;
		String name = null, value = null, tmp = null;
		Class<?> type = null;
		Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}");
		Matcher matcher = null;
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream(config_file_path));

			for (Field field : fields) {
				name = field.getName();
				value = properties.getProperty(name);
				if (StringUtils.isNotBlank(value)) {
					matcher = pattern.matcher(value);
					while (matcher.find()) {
						tmp = properties.getProperty(matcher.group(1));
						if (StringUtils.isBlank(tmp)) {
							logger.error("配置{}存在其它配置{}，请检查您的配置文件", name, matcher.group(1));
						}
						value = value.replace(matcher.group(0), tmp);
					}

					type = field.getType();
					if (String.class.equals(type)) {
						fieldValue = value;
					} else if (Integer.class.equals(type)) {
						fieldValue = Integer.valueOf(value);
					} else if (Boolean.class.equals(type)) {
						fieldValue = Boolean.valueOf(value);
					} else {
						fieldValue = value;
					}
					field.set(this, fieldValue);
				}
				logger.debug("加载配置：{}={}", name, field.get(this));
			}
		} catch (Throwable e) {
			logger.error("初始化配置项的值失败：" + name + "=" + value, e);
		}
	}
	

}
