package com.ucpaas.sms.task.util.api;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ucpaas.sms.task.constant.SysConstant;
import com.ucpaas.sms.task.util.ConfigUtils;
import com.ucpaas.sms.task.util.JsonUtils;
import com.ucpaas.sms.task.util.encrypt.EncryptUtils;
import com.ucpaas.sms.task.util.rest.SSLHttpClient;

/**
 * Rest接口工具类
 * 
 * @author xiejiaan
 */
@SuppressWarnings("deprecation")
public class RestUtils {
	private static final Logger logger = LoggerFactory.getLogger(RestUtils.class);

	/**
	 * 短信模板的id
	 * 
	 * @author xiejiaan
	 */
	public enum SmsTemplateId {
		/**
		 * 短信验证码
		 */
		verify_code(100),
		/**
		 * 管理后台审核通知短信
		 */
		audit_notice(101),
		/**
		 * 自定义短信
		 */
		custom(106),
		/**
		 * 短信通道预警：到达成功率
		 */
		warning_reachrate(108),
		/**
		 * 短信通道预警：延时时长
		 */
		warning_delayavg(109),
		/**
		 * 短信通道预警：并发量
		 */
		warning_concurrency(110);
		private int value;

		SmsTemplateId(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	/**
	 * 使用post方式调用rest接口
	 * 
	 * @param operate
	 *            操作
	 * @param content
	 *            内容
	 * @return 接口的JSON响应字符串
	 */
	public static String post(String operate, String content) {
		String result = null;
		String contentType = ContentType.APPLICATION_JSON.getMimeType();
		String timestamp = DateTime.now().toString("yyyyMMddHHmmss");// 时间戳
		// url签名：md5(主账户Id+主账户授权令牌+时间戳)
		String signature = EncryptUtils.encodeMd5(
				SysConstant.super_admin_sid + SysConstant.super_admin_token + timestamp).toUpperCase();

		StringBuilder url = new StringBuilder();// 构造请求的url
		url.append(ConfigUtils.rest_domain);
		url.append("/");
		url.append(ConfigUtils.rest_version);
		url.append("/Accounts/");
		url.append(SysConstant.super_admin_sid);
		url.append(operate);
		url.append("?sig=");
		url.append(signature);
		String auth = EncryptUtils.encodeBase64(SysConstant.super_admin_sid + ":" + timestamp);// base64(主账户Id+冒号+时间戳)
		logger.debug("使用post方式调用rest接口【开始】：url={}, content={}", url, content);

		if (ConfigUtils.rest_domain.equals("https://api.ucpaas.com")) {
			result = postForProduction(contentType, url.toString(), auth, content);
		} else {
			result = postForOther(contentType, url.toString(), auth, content);
		}
		logger.debug("使用post方式调用rest接口【结束】：url={}, content={}, result={}", url, content, result);
		return result;
	}

	/**
	 * 调用线上环境的rest接口，即rest接口的域名为https://api.ucpaas.com
	 * 
	 * @param contentType
	 * @param url
	 * @param auth
	 * @param content
	 * @return
	 */
	private static String postForProduction(String contentType, String url, String auth, String content) {
		String result = null;
		Request request = Request.Post(url);
		request.setHeader("Accept", contentType);
		request.setHeader("Content-Type", contentType + ";charset=utf-8");
		request.setHeader("Authorization", auth);
		request.bodyString(content, ContentType.APPLICATION_JSON);
		try {
			result = request.execute().returnContent().asString();
		} catch (Throwable e) {
			logger.error("调用线上环境的rest接口【失败】", e);
		}
		return result;
	}

	/**
	 * 调用非线上环境的rest接口，即rest接口的域名不为https://api.ucpaas.com
	 * 
	 * @param contentType
	 * @param url
	 * @param auth
	 * @param content
	 * @return
	 */
	private static String postForOther(String contentType, String url, String auth, String content) {
		String result = null;

		DefaultHttpClient httpClient = null;
		String tmp = url.replace("https://", "");
		String ip = tmp.substring(0, tmp.indexOf(":"));
		int port = Integer.parseInt(tmp.substring(tmp.indexOf(":") + 1, tmp.indexOf("/")));
		try {
			SSLHttpClient chc = new SSLHttpClient();
			httpClient = chc.registerSSL(ip, "TLS", port, "https");
			HttpParams httpParams = new BasicHttpParams();
			httpParams.setParameter("https.protocols", "SSLv3,SSLv2Hello");
			httpClient.setParams(httpParams);

			// 创建HttpPost
			HttpPost httpPost = new HttpPost(url);
			httpPost.setHeader("Accept", contentType);
			httpPost.setHeader("Content-Type", contentType + ";charset=utf-8");
			httpPost.setHeader("Authorization", auth);
			BasicHttpEntity requestBody = new BasicHttpEntity();
			requestBody.setContent(new ByteArrayInputStream(content.getBytes("UTF-8")));
			requestBody.setContentLength(content.getBytes("UTF-8").length);
			httpPost.setEntity(requestBody);
			// 执行客户端请求
			HttpEntity entity = httpClient.execute(httpPost).getEntity();
			if (entity != null) {
				result = EntityUtils.toString(entity, "UTF-8");
				EntityUtils.consume(entity);
			}

		} catch (Throwable e) {
			logger.error("调用非线上环境的rest接口【失败】：url={}, auth={}, content={}", url, auth, content);
		} finally {
			httpClient.close();
		}
		return result;
	}
	
	/**
	 * 直接请求通道发送短信
	 * @param phone
	 * @param content
	 * @return true成功，false失败
	 */
	@SuppressWarnings("resource")
	public static Boolean postForChannel(String phone, String content) {
		String contentTempplate = ConfigUtils.post_channel_body;
		contentTempplate = contentTempplate.replace("%phone%", phone);
		try {
			contentTempplate = contentTempplate.replace("%content%", URLEncoder.encode(content, "gbk"));
		} catch (UnsupportedEncodingException e1) {
			logger.error("短信内容转码【失败】", e1);
		}
		
		// 创建HttpPost
		String result = null;
		HttpClient httpClient = new DefaultHttpClient();
		try {
			HttpPost httpPost = new HttpPost(ConfigUtils.post_channel_url);
			httpPost.setHeader("Content-Type", ContentType.APPLICATION_FORM_URLENCODED + ";charset=gbk");
			BasicHttpEntity requestBody = new BasicHttpEntity();
			requestBody.setContent(new ByteArrayInputStream(contentTempplate.getBytes("gbk")));
			requestBody.setContentLength(contentTempplate.getBytes("gbk").length);
			httpPost.setEntity(requestBody);
			// 执行客户端请求
			HttpEntity entity = httpClient.execute(httpPost).getEntity();
			
			if (entity != null) {
				result = EntityUtils.toString(entity, "gbk");
				EntityUtils.consume(entity);
			}

		} catch (Throwable e) {
			logger.error("请求通道发送短信【失败】", e);
		}
		
		if(result.contains("<code>03</code>")){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 发送模板短信
	 * 
	 * @param smsTemplateId
	 *            短信模板的id
	 * @param to
	 *            短信接收端手机号码集合，用英文逗号分开，每批发送的手机号数量不得超过100个
	 * @param param
	 *            内容数据，用于替换模板中{数字}，用英文逗号分开
	 * @return 是否发送成功
	 */
	@SuppressWarnings("unchecked")
	public static boolean sendTemplateSMS(SmsTemplateId smsTemplateId, String to, String param) {
		Map<String, Object> templateSMS = new HashMap<String, Object>();
		templateSMS.put("appId", SysConstant.sys_app_sid);
		templateSMS.put("templateId", smsTemplateId.value);
		templateSMS.put("to", to);
		templateSMS.put("param", param);
		templateSMS.put("free", 1);
		Map<String, Object> content = new HashMap<String, Object>();
		content.put("templateSMS", templateSMS);
		String result = post("/Messages/templateSMS", JsonUtils.toJson(content));

		Map<String, Map<String, Object>> data = JsonUtils.toObject(result, Map.class);
		if (data != null && data.containsKey("resp")) {
			String respCode = data.get("resp").get("respCode").toString();
			if (respCode.equals("000000")) {
				logger.debug("发送模板短信【成功】：smsTemplateId={}, to={}, param={}, result={}", smsTemplateId.value, to, param,
						result);
				return true;
			}
		}
		logger.error("发送模板短信【失败】：smsTemplateId={}, to={}, param={}, result={}", smsTemplateId.value, to, param, result);
		return false;
	}

	public static void main(String[] args) {
//		ConfigUtils.rest_domain = "https://api.ucpaas.com";
//		ConfigUtils.rest_version = "2014-06-30";
//		sendTemplateSMS(SmsTemplateId.verify_code, "18665922190", "1122");
		
		
		ConfigUtils.post_channel_url = "http://221.179.180.158:9007/QxtSms/QxtFirewall";
		ConfigUtils.post_channel_body = "OperID=yzxw3&OperPass=yzx7766&SendTime=&ValidTime=&AppendID=&DesMobile=%phone%&Content=%content%";
		if(postForChannel("18665922190","【云之讯】您的验证码8567")){
			System.out.println("send sms success!!!");
		}else{
			System.out.println("send sms fail!!!");
		}
		
	}
}
