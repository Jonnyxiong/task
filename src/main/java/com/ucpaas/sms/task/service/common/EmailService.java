package com.ucpaas.sms.task.service.common;

/**
 * Email业务
 * 
 * @author xiejiaan
 */
public interface EmailService {

	/**
	 * 发送文本格式的Email
	 * 
	 * @param to
	 *            接收者，用,分割
	 * @param subject
	 *            主题
	 * @param body
	 *            内容
	 * @return 是否发送成功
	 */
	boolean sendTextEmail(String to, String subject, String body);

	/**
	 * 发送html格式的Email
	 * 
	 * @param to
	 *            接收者，用,分割
	 * @param subject
	 *            主题
	 * @param body
	 *            内容
	 * @return 是否发送成功
	 */
	boolean sendHtmlEmail(String to, String subject, String body);

	boolean sendHtmlEmail(String form, String to, String subject, String body);

}
