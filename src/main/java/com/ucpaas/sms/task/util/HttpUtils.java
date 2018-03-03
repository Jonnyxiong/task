package com.ucpaas.sms.task.util;


import com.ucpaas.sms.task.util.rest.SSLHttpClient;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;




@SuppressWarnings("deprecation")
public class HttpUtils {

	private static Logger logger = LoggerFactory.getLogger(HttpUtils.class);
	
	/** 
	 * @Title: sendHttpPostForImge 
	 * @Description: 发送图片到图片服务器
	 * @param path 图片的本地路径
	 * @param url 图片服务器的url地址
	 * @return
	 * @return: String
	 */
	public static String sendHttpPostForImge(String path,String url){

		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);  
		
		FileBody fileBody = new FileBody(new File(path));
		
		MultipartEntityBuilder meBuilder = MultipartEntityBuilder.create();
		meBuilder.addPart("photoFile", fileBody);
		HttpEntity reqEntity = meBuilder.build();
		httpPost.setEntity(reqEntity);
		
		HttpEntity repentity = null;
		String responseContent = null;

		try {
			HttpResponse response = client.execute(httpPost);
			repentity = response.getEntity();
			responseContent = EntityUtils.toString(repentity, "UTF-8");

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return responseContent;
	}

	/**
	 * Http 请求方法
	 * @param url
	 * @param content
	 * @return
	 */
	public static String httpPost(String url, String content, boolean needSSL) {
		// 创建HttpPost
		String result = null;
		HttpClient httpClient = getHttpClient(needSSL, StringUtil.getHostFromURL(url));
		try {
			HttpPost httpPost = new HttpPost(url);
			httpPost.setHeader("Content-Type", ContentType.APPLICATION_FORM_URLENCODED + ";charset=utf-8");
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();
			httpPost.setConfig(requestConfig);
			BasicHttpEntity requestBody = new BasicHttpEntity();
			requestBody.setContent(new ByteArrayInputStream(content.getBytes("utf-8")));
			requestBody.setContentLength(content.getBytes("utf-8").length);
			httpPost.setEntity(requestBody);
			// 执行客户端请求
			HttpEntity entity = httpClient.execute(httpPost).getEntity();

			if (entity != null) {
				result = EntityUtils.toString(entity, "utf-8");
				EntityUtils.consume(entity);
			}

		} catch (Throwable e) {
			logger.error("【HTTP请求失败】: url={}, content={}", url, content );
		}

		return result;
	}


	public static DefaultHttpClient getHttpClient(boolean sslClient, String host){
		DefaultHttpClient httpclient=null;
		if (sslClient) {
			try {
				SSLHttpClient chc = new SSLHttpClient();
				InetAddress address = null;
				String ip;
				try {
					address = InetAddress.getByName(host);
					ip = address.getHostAddress().toString();
					httpclient = chc.registerSSL(ip,"TLS",443,"https");
				} catch (UnknownHostException e) {
					logger.error("获取请求服务器地址失败：host = {} " + host);
					e.getStackTrace().toString();
				}
				HttpParams hParams=new BasicHttpParams();
				hParams.setParameter("https.protocols", "SSLv3,SSLv2Hello");
				httpclient.setParams(hParams);
			} catch (KeyManagementException e) {
				logger.error(e.getStackTrace().toString());
			}catch (NoSuchAlgorithmException e) {
				logger.error(e.getStackTrace().toString());
			}
		}else {
			httpclient=new DefaultHttpClient();
		}
		return httpclient;
	}


	public static void main(String[] args) throws UnknownHostException {
		
	}
}
