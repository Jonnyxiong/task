package com.ucpaas.sms.task.service;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.ucpaas.sms.task.util.ExecutorServiceCachePool;
import com.ucpaas.sms.task.util.JacksonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ucpaas.sms.task.dao.AccessSlaveDao;
import com.ucpaas.sms.task.dao.MessageMasterDao;
import com.ucpaas.sms.task.dao.RecordSlaveDao;
import com.ucpaas.sms.task.dao.StatsMasterDao;
import com.ucpaas.sms.task.entity.stats.ChannelSuccessRateRealtime;
import com.ucpaas.sms.task.entity.stats.ClientSuccessRateRealtime;
import com.ucpaas.sms.task.mapper.stats.ChannelSuccessRateRealtimeMapper;
import com.ucpaas.sms.task.mapper.stats.ClientSuccessRateRealtimeMapper;
import com.ucpaas.sms.task.model.ChannelQualityIndex;
import com.ucpaas.sms.task.model.RabbitMqQueue;
import com.ucpaas.sms.task.model.TaskInfo;
import com.ucpaas.sms.task.util.ConfigUtils;
import com.ucpaas.sms.task.util.UcpaasDateUtils;
import com.ucpaas.sms.task.util.encrypt.EncryptUtils;



@SuppressWarnings("ALL")
@Service
@Transactional
public class ChannelQaRealTimeServiceImpl implements ChannelQaRealTimeService {
	
	@Autowired
	private AccessSlaveDao accessSlaveDao;
	@Autowired
	private StatsMasterDao statsMasterDao;
	@Autowired
	private RecordSlaveDao recordSlaveDao;
	@Autowired
	private MessageMasterDao messageMasterDao;
	@Autowired
	private ClientSuccessRateRealtimeMapper clientSuccessRateRealtimeMapper;
	@Autowired
	private ChannelSuccessRateRealtimeMapper channelSuccessRateRealtimeMapper;

	private static final Logger logger = LoggerFactory.getLogger(ChannelQaRealTimeServiceImpl.class);
	
	private final static Map<String, Integer> queueTypeMap;
	static {
		queueTypeMap = new HashMap<String, Integer>();
		queueTypeMap.put("YDhangye", 1); // 移动行业
		queueTypeMap.put("YDyingxiao", 2); // 移动营销
		queueTypeMap.put("LThangye", 3); // 联通行业
		queueTypeMap.put("LTyingxiao", 4); // 联通营销
		queueTypeMap.put("DXhangye", 5); // 电信行业
		queueTypeMap.put("DXyingxiao", 6); // 电信营销
	};
	
	private static final int GREEN = 0; //	绿色(正常)
	private static final int YELLOW = 1; //	黄色(注意)
	private static final int RED = 2;  // 红色(故障)
	
	
	@Override
	public boolean clientSendSpeed(TaskInfo taskInfo) {
		
		logger.info("【短信账号发送速率统计任务】开始");
		long start = System.currentTimeMillis();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		Map<String, Object> statTimeMap = new HashMap<String, Object>();
		Map<String, Object> statDataMap = new HashMap<String, Object>();
		List<Map<String, Object>> allTableDataList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> oneTableDataList = new ArrayList<Map<String, Object>>();
		
		// 获得任务的统计时间范围
		statTimeMap = getStatTime(taskInfo);
		paramsMap.putAll(statTimeMap);
		
		try {
			// 根据indentify和时间遍历统计十张流水表中clientId的发送速率
			for (int identify = 0; identify < 10; identify++) {
				paramsMap.put("identify", identify);
				oneTableDataList = accessSlaveDao.getSearchList("smsMonitor.getClientSendSpeedByTime", paramsMap);
				allTableDataList.addAll(oneTableDataList);
			}
			
			if(allTableDataList.size() > 0){
				statDataMap.put("date", statTimeMap.get("date"));
				statDataMap.put("data_time", statTimeMap.get("endTime"));
				
				// 删除老数据
				statsMasterDao.delete("smsMonitor.deleteClientSendSpeedDataByTime", statDataMap);
				
				// 保存统计数据
				statDataMap.put("allTableDataList", allTableDataList);
				statsMasterDao.insert("smsMonitor.saveClientSendSpeedData", statDataMap);
			}else{
				logger.info("【短信账号发送速率统计任务】统计数据为空");
			}
			
		} catch (Exception e) {
			logger.error("【短信账号发送速率统计任务】结束：" + e);
			return false;
		}
		long end = System.currentTimeMillis();
		logger.info("【短信账号发送速率统计任务】结束，统计时间区间 = {}，统计耗时={}ms", statTimeMap, (end - start));
		return true;
	}
	

	@Override
	public boolean mqQueueMessageStat(TaskInfo taskInfo) {
		boolean result = false;
		String rabbit_api_url = ConfigUtils.rabbit_api_url;
		String rabbit_api_username = ConfigUtils.rabbit_api_username;
		String rabbit_api_password = ConfigUtils.rabbit_api_password;
		String timeFormart = taskInfo.getExecuteType().getFormat();
		DateTime executeNext = UcpaasDateUtils.parseDate(taskInfo.getExecuteNext(), timeFormart);
		String dataTime = executeNext.toString("yyyy-MM-dd HH:mm");
		
		CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(rabbit_api_url);
		request.addHeader(HttpHeaders.AUTHORIZATION, getBasicAuthorization(rabbit_api_username, rabbit_api_password));
		
		String jsonData = "";
		try {
			CloseableHttpResponse response = closeableHttpClient.execute(request);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				jsonData = EntityUtils.toString(response.getEntity());
			}
			
		} catch (ClientProtocolException e) {
			logger.error("【每1分钟统计MQ队列消息数】异常： ", e);
		} catch (IOException e) {
			logger.error("【每1分钟统计MQ队列消息数】异常： ", e);
		} finally {
			try {
				closeableHttpClient.close();
			} catch (IOException e) {
				logger.error("【每1分钟统计MQ队列消息数】异常： ", e);
			}
		}
		
		if(StringUtils.isNotBlank(jsonData)){
			Gson gson = new Gson();
			List<RabbitMqQueue> queueList = gson.fromJson(jsonData, new TypeToken<List<RabbitMqQueue>>() { }.getType());
			logger.debug("【每1分钟统计MQ队列消息数】查询到存在队列数：" + queueList.size());
			
			List<Map<String, Object>> queueDataList = new ArrayList<Map<String, Object>>();
			for (int pos = 0; pos < queueList.size(); pos++) {
				RabbitMqQueue queueObj = queueList.get(pos);
				Map<String, Object> queueData = new HashMap<String, Object>();
				queueData.put("queue_name", queueObj.getName());
				queueData.put("queue_type", getQueueType(queueObj.getName()));
				queueData.put("message_number", queueObj.getMessages());
				queueData.put("data_time", dataTime);
				
				// 不识别的队列类型会返回null,直接忽略
				if(null != queueData.get("queue_type")){
					queueDataList.add(queueData);
				}else{
					continue;
				}
			}
			
			Map<String, Object> sqlParamMap = new HashMap<String, Object>();
			sqlParamMap.put("date", executeNext.toString("yyyyMMdd"));
			sqlParamMap.put("data_time", dataTime);
			statsMasterDao.delete("smsMonitor.deleteAccessQueueDataByTime", sqlParamMap);
			
			sqlParamMap.put("queueDataList", queueDataList);
			statsMasterDao.insert("smsMonitor.saveAccessQueueData", sqlParamMap);
			
			result = true;
		}
		
		return result;
	}
	
	
	@Override
	public boolean clientQualityIndexesStat(TaskInfo taskInfo) {
		logger.info("【客户发送质量指数统计任务】统计5分钟内数据开始");
		long start = System.currentTimeMillis();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		Map<String, Object> statTimeMap = new HashMap<String, Object>();
		Map<String, Object> indexesDataMap = new HashMap<String, Object>();
		List<Map<String, Object>> allTableDataList = new ArrayList<Map<String, Object>>(); // 10张access表的统计数据
		List<Map<String, Object>> oneTableDataList = new ArrayList<Map<String, Object>>(); // 1张access表的统计数据
		
		// 获得任务的统计时间范围
		statTimeMap = getStatTime(taskInfo);
		paramsMap.putAll(statTimeMap);
		
		// 根据indentify和时间遍历统计十张流水表中的通道质量指数
		for (int identify = 0; identify < 10; identify++) {
			paramsMap.put("identify", identify);
			oneTableDataList = accessSlaveDao.getSearchList("smsMonitor.get5MinClientIndexesByTime", paramsMap);
			allTableDataList.addAll(oneTableDataList);
		}
		
		if(allTableDataList.size() > 0){
			// a.将客户发送质量指数统计数据保存到监控库
			indexesDataMap.put("date", statTimeMap.get("date"));
			indexesDataMap.put("data_time", statTimeMap.get("endTime"));
			// 老数据删除
			statsMasterDao.delete("smsMonitor.deleteClientIndexesDataByTime", indexesDataMap);
			indexesDataMap.put("dataList", allTableDataList);
			statsMasterDao.insert("smsMonitor.saveClientIndexesData", indexesDataMap);
			
		}
		
		long end = System.currentTimeMillis();
		logger.info("【客户发送质量指数统计任务】统计5分钟内数据结束，统计时间区间 = {}，统计耗时={}ms", statTimeMap, (end - start));
		
		return true;
	}

	@Override
	public boolean channelQualityIndexesStat(TaskInfo taskInfo) {
		
		logger.info("【短信通道质量指数统计任务】统计5分钟内数据开始");
		long start = System.currentTimeMillis();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		Map<String, Object> statTimeMap = new HashMap<String, Object>();
		Map<String, Object> indexesDataMap = new HashMap<String, Object>();
		List<Map<String, Object>> allTableDataList = new ArrayList<Map<String, Object>>(); // 10张record表的统计数据
		List<Map<String, Object>> oneTableDataList = new ArrayList<Map<String, Object>>(); // 1张流水表的统计数据
		
		// 获得任务的统计时间范围
		statTimeMap = getStatTime(taskInfo);
		paramsMap.putAll(statTimeMap);
		
		// 根据indentify和时间遍历统计十张流水表中的通道质量指数
		for (int identify = 0; identify < 10; identify++) {
			paramsMap.put("identify", identify);
			oneTableDataList = recordSlaveDao.getSearchList("smsMonitor.get5MinChannelIndexesByTime", paramsMap);
			allTableDataList.addAll(oneTableDataList);
		}
		
		// 从通道表中查询通道属性记录到统计流水中
		for (Map<String, Object> map : allTableDataList) {
			Map<String, Object> channelInfoMap = messageMasterDao.getOneInfo("smsMonitor.getChannelInfoById", map.get("channel_id"));
			if(null != channelInfoMap){
				map.putAll(channelInfoMap);
			}else{
				channelInfoMap = new HashMap<String, Object>();
				channelInfoMap.put("channel_name", "已删除通道");
				channelInfoMap.put("operator_type", 1);
				channelInfoMap.put("industry_type", 1);
				channelInfoMap.put("owner_type", 1);
				map.putAll(channelInfoMap);
			}
		}
		
		if(allTableDataList.size() > 0){
			// a.将通道质量指数统计数据保存到监控库
			indexesDataMap.put("date", statTimeMap.get("date"));
			indexesDataMap.put("data_time", statTimeMap.get("endTime"));
			// 老数据删除
			statsMasterDao.delete("smsMonitor.deleteChannelIndexesDataByTime", indexesDataMap);
			indexesDataMap.put("dataList", allTableDataList);
			statsMasterDao.insert("smsMonitor.saveChannelIndexesData", indexesDataMap);
			
			// b.基于通道质量指数数据统计通道状态图谱数据
			List<Map<String, Object>> allChannelStatusGraphList = channelStatusGraphStat(allTableDataList);
			if(allChannelStatusGraphList == null){
				logger.error("【短信通道质量指数统计任务】统计通道质量图谱数据为空，任务中断");
				return false;
			}else{
				Map<String, Object> sqlParams = new HashMap<String, Object>();
				sqlParams.put("date", statTimeMap.get("date"));
				sqlParams.put("data_time", statTimeMap.get("endTime"));
				// 老数据删除
				statsMasterDao.delete("smsMonitor.deleteChannelStatusGraphDataByTime", sqlParams);
				sqlParams.put("dataList", allChannelStatusGraphList);
				statsMasterDao.insert("smsMonitor.saveChannelStatusGraphData", sqlParams);
			}
		}
		
		long end = System.currentTimeMillis();
		logger.info("【短信通道质量指数统计任务】统计5分钟内数据结束，统计时间区间 = {}，统计耗时={}ms", statTimeMap, (end - start));
		
		return true;
	}
	
	@Override
	public boolean channelErrorStat(TaskInfo taskInfo) {
		logger.info("【短信通道错误统计任务】开始");
		long start = System.currentTimeMillis();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		Map<String, Object> statTimeMap = new HashMap<String, Object>();
		Map<String, Object> statDataMap = new HashMap<String, Object>();
		List<Map<String, Object>> allTableDataList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> oneTableDataList = new ArrayList<Map<String, Object>>();
		
		// 获得任务的统计时间范围
		statTimeMap = getStatTime(taskInfo);
		paramsMap.putAll(statTimeMap);
		
		// 根据indentify和时间遍历统计十张流水表统计
		for (int identify = 0; identify < 10; identify++) {
			paramsMap.put("identify", identify);
			oneTableDataList = recordSlaveDao.getSearchList("smsMonitor.getChannelErrorDataBytime", paramsMap);
			allTableDataList.addAll(oneTableDataList);
		}
		
		// 从通道表中查询通道属性记录到统计流水中
		for (Map<String, Object> map : allTableDataList) {
			Map<String, Object> channelInfoMap = messageMasterDao.getOneInfo("smsMonitor.getChannelInfoById", map.get("channel_id"));
			if(null != channelInfoMap){
				map.putAll(channelInfoMap);
			}else{
				channelInfoMap = new HashMap<String, Object>();
				channelInfoMap.put("channel_name", "已删除通道");
				channelInfoMap.put("operator_type", 1);
				channelInfoMap.put("industry_type", 1);
				channelInfoMap.put("owner_type", 1);
				map.putAll(channelInfoMap);
			}
		}
		
		if(allTableDataList.size() > 0){
			statDataMap.put("date", statTimeMap.get("date"));
			statDataMap.put("data_time", statTimeMap.get("endTime"));
			statsMasterDao.delete("smsMonitor.deleteChannelErrorDataByTime", statDataMap);
			statDataMap.put("dataList", allTableDataList);
			statsMasterDao.insert("smsMonitor.saveChannelErrorData", statDataMap);
		}
			
		long end = System.currentTimeMillis();
		logger.info("【短信通道错误统计任务】结束，统计时间区间 = {}，统计耗时={}ms", statTimeMap, (end - start));
		return true;
	}
	
	/**
	 * 通道状态图谱数据统计
	 * 
	 * @param indexesDataList
	 * @return
	 */
	private List<Map<String, Object>> channelStatusGraphStat(List<Map<String, Object>> indexesDataList){
		
		Map<String,Object> upperLimitMap = statsMasterDao.getOneInfo("smsMonitor.getChannelIndexesUpperLimit", null);
		Map<String,Object> lowerLimitMap = statsMasterDao.getOneInfo("smsMonitor.getChannelIndexesLowerLimit", null);
		
		if(upperLimitMap == null || lowerLimitMap == null){
			logger.error("【短信通道质量指数统计任务】查询通道质量指数阈值为空，统计通道状态中断");
		}
		
		// 大于下面的值时通道状态颜色为绿色
		double respRateUpLimit = toDouble(upperLimitMap.get("resp_rate"));
		double reportRateUpLimit = toDouble(upperLimitMap.get("report_rate"));
		double sendSccussUpLimit = toDouble(upperLimitMap.get("send_success_rate"));
		double sendFailureUpLimit = toDouble(upperLimitMap.get("send_failure_rate"));
		double submitFailureNumUpLimit = toDouble(upperLimitMap.get("submit_failure_num"));
		
		// 小于下面的值时通道状态颜色为黄色
		double respRateLoLimit = toDouble(lowerLimitMap.get("resp_rate"));
		double reportRateLoLimit = toDouble(lowerLimitMap.get("report_rate"));
		double sendSccussLoLimit = toDouble(lowerLimitMap.get("send_success_rate"));
		double sendFailureLoLimit = toDouble(lowerLimitMap.get("send_failure_rate"));
		double submitFailureLoLimit = toDouble(lowerLimitMap.get("submit_failure_num"));
		
		List<Map<String, Object>> allChannelStatusGraphList = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> map : indexesDataList) {
			// 通道质量指标基数
			double sendTotalNum = toDouble(map.get("send_total_num"));
			double respNum = toDouble(map.get("resp_num_1"));
			// 回执率计算0-10秒内
			double reportNum = toDouble(map.get("report_num_1")) + toDouble(map.get("report_num_2"));
			double sendSccussNum = toDouble(map.get("send_sccuss_num"));
			double sendFailureNum = toDouble(map.get("send_failure_num"));
			double submitFailureNum = toDouble(map.get("submit_failure_num"));
			
			// 通道质量指标计算出来的比率
			double respRate = toDouble(respNum / sendTotalNum);
			double reportRate = toDouble(reportNum / sendTotalNum);
			double sendSccussRate = toDouble(sendSccussNum / sendTotalNum);
			double sendFailureRate = toDouble(sendFailureNum / sendTotalNum);
			
			ChannelQualityIndex respIndex = new ChannelQualityIndex("应答率", respRate, respRateUpLimit, respRateLoLimit);
			ChannelQualityIndex reportIndex = new ChannelQualityIndex("回执率", reportRate, reportRateUpLimit, reportRateLoLimit);
			ChannelQualityIndex sendSccussIndex = new ChannelQualityIndex("发送成功率", sendSccussRate, sendSccussUpLimit, sendSccussLoLimit);
			ChannelQualityIndex sendFailureIndex = new ChannelQualityIndex("发送失败率", sendFailureRate, sendFailureUpLimit, sendFailureLoLimit);
			ChannelQualityIndex submitFailureIndex = new ChannelQualityIndex("提交失败条数", submitFailureNum, submitFailureNumUpLimit, submitFailureLoLimit);
			
			List<ChannelQualityIndex> indexList = new ArrayList<ChannelQualityIndex>();
			indexList.add(respIndex);
			indexList.add(reportIndex);
			indexList.add(sendSccussIndex);
			indexList.add(sendFailureIndex);
			indexList.add(submitFailureIndex);
			
			// 根据通道质量指标计算通道状态
			Map<String, Object> channelStatusMap = getChannelStatusByIndexes(indexList);
			
			Map<String, Object> channelInfoMap = new HashMap<String, Object>();
			channelInfoMap.put("channel_id", map.get("channel_id"));
			channelInfoMap.put("channel_name", map.get("channel_name"));
			channelInfoMap.put("operator_type", map.get("operator_type"));
			channelInfoMap.put("industry_type", map.get("industry_type"));
			channelInfoMap.put("owner_type", map.get("owner_type"));
			channelInfoMap.put("data_time", map.get("data_time"));
			
			channelStatusMap.putAll(channelInfoMap);
			allChannelStatusGraphList.add(channelStatusMap);
		}
		
		return allChannelStatusGraphList;
	}
	
	/**
	 * 根据通道质量指标计算通道状态
	 * 
	 * @param indexList
	 * @return
	 */
	private static Map<String, Object> getChannelStatusByIndexes(List<ChannelQualityIndex> indexList){
		// 通道质量指标状态： 0->绿色(正常)，1->黄色(注意)，2->红色(故障)
		Map<String, Object> channelStatusMap = new HashMap<String, Object>();
		
		for (ChannelQualityIndex index : indexList) {
			int status = GREEN;
			StringBuffer errorDespBuff = new StringBuffer();
			if(null != channelStatusMap.get("status")){
				status = (Integer) channelStatusMap.get("status");
			}
			if(null != channelStatusMap.get("error_desp")){
				errorDespBuff.append(channelStatusMap.get("error_desp"));
			}
			
			// “提交失败条数的校验”、“发送失败率”与其他指数的判断不同单独处理
			if(index.getIndexName().equals("提交失败条数") || index.getIndexName().endsWith("发送失败率")){
				if(index.getIndexValue() <= index.getUpLimit()){ // 绿色
					if(status == GREEN && StringUtils.isBlank(errorDespBuff)){
						errorDespBuff.append("良好");
						channelStatusMap.put("status", GREEN);
						channelStatusMap.put("error_desp", errorDespBuff.toString());
					}
				}else if(index.getIndexValue() >= index.getLowerLimit()){ // 红色
					if(status == RED){
						if(StringUtils.isNotBlank(errorDespBuff)){
							errorDespBuff.append("；");
						}
						errorDespBuff.append(index.getIndexErrorDesp());
					}else{
						errorDespBuff = new StringBuffer();
						errorDespBuff.append(index.getIndexErrorDesp());
					}
					channelStatusMap.put("status", RED);
					channelStatusMap.put("error_desp", errorDespBuff.toString());
				}else{
					// 如果已经存在“红色”直接忽略“黄色”
					if(status == RED){
						continue;
					}else{
						if(StringUtils.isNotBlank(errorDespBuff)){
							errorDespBuff.append("；");
						}
						errorDespBuff.append(index.getIndexWarningDesp());
						channelStatusMap.put("status", YELLOW);
						channelStatusMap.put("error_desp", errorDespBuff.toString());
					}
				}
			}else{
				if(index.getIndexValue() >= index.getUpLimit()){ // 绿色
					if(status == GREEN){ 
						errorDespBuff.append("");
						channelStatusMap.put("status", GREEN);
						channelStatusMap.put("error_desp", errorDespBuff.toString());
					}
				}else if(index.getIndexValue() <= index.getLowerLimit()){ // 红色
					if(status == RED){
						if(StringUtils.isNotBlank(errorDespBuff)){
							errorDespBuff.append("；");
						}
						errorDespBuff.append(index.getIndexErrorDesp());
					}else{
						errorDespBuff = new StringBuffer();
						errorDespBuff.append(index.getIndexErrorDesp());
					}
					channelStatusMap.put("status", RED);
					channelStatusMap.put("error_desp", errorDespBuff.toString());
				}else{
					// 如果已经存在“红色”直接忽略“黄色”
					if(status == RED){
						continue;
					}else{
						if(StringUtils.isNotBlank(errorDespBuff)){
							errorDespBuff.append("；");
						}
						errorDespBuff.append(index.getIndexWarningDesp());
						channelStatusMap.put("status", YELLOW);
						channelStatusMap.put("error_desp", errorDespBuff.toString());
					}
				}
			}
			
		}
		
		return channelStatusMap;
	}
	
	/**
	 * 获得5分钟的统计时间区间
	 * @param taskInfo
	 * @return
	 */
	private Map<String, Object> getStatTime(TaskInfo taskInfo){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		String formart = taskInfo.getExecuteType().getFormat();
		DateTime executeNext = UcpaasDateUtils.parseDate(taskInfo.getExecuteNext(), formart);
		String startTime = executeNext.minusMinutes(5).toString("yyyy-MM-dd HH:mm"); // 统计的开始时间
		String endTime = executeNext.toString("yyyy-MM-dd HH:mm"); // 统计的结束时间
		String startTimeDay = startTime.substring(0, 10);
		String endTimeDay = endTime.substring(0, 10);
		String date = "";  // 统计的天，用于拼接数据库表
		
		// 处理凌晨跨天时特殊情况
		// 如：startTime = 2016-12-11 23:55  endTime = 2016-12-12 00:00 这条数据应该保持在20161211的流水表中
		if(startTimeDay.equals(endTimeDay)){
			date = endTimeDay.replace("-", "");
		}else{
			date = startTimeDay.replace("-", "");
		}
		
		resultMap.put("date", date);
		resultMap.put("startTime", startTime);
		resultMap.put("endTime", endTime);
		
		return resultMap;
	}
	
	private Integer getQueueType(String name) {
		for (Map.Entry<String, Integer> entry : queueTypeMap.entrySet()) {
			if(name.contains(entry.getKey())){
				return entry.getValue();
			}
		}
		return null;
	}
	
	private String getBasicAuthorization(String username, String password){
		String basicAuthorization = "";
		String authHeader = "Basic ";
		String authContent = EncryptUtils.encodeBase64(username + ":" + password);
		basicAuthorization = authHeader + authContent;
		
		return basicAuthorization;
	}
	
	private double toDouble(Object value){
		double result = 0;
		try {
			DecimalFormat df = new DecimalFormat("#0.000"); 
			result = NumberUtils.toDouble(df.format(value));
		} catch (Exception e) {
			logger.error("数据格式转换异常" + e);
		}
		return result;
	}
	
	
	@Override
	public boolean clientSuccessRateRealtime(TaskInfo taskInfo) {
		
		logger.info("【短信用户成功率指数统计任务】开始");
		long start = System.currentTimeMillis();
		
		String timeFormart = taskInfo.getExecuteType().getFormat();
		final DateTime executeNext = UcpaasDateUtils.parseDate(taskInfo.getExecuteNext(), timeFormart);
		final String date = executeNext.toString("yyyyMMdd");
		final DateTime now = new DateTime();
		// 根据indentify和时间遍历统计十张流水表中的用户成功率数据
		Map<String, Object> sqlParams = new HashMap<String, Object>();
		List<ClientSuccessRateRealtime> allTableDataList = new ArrayList<ClientSuccessRateRealtime>(); // 10张access表的统计数据
//		List<ClientSuccessRateRealtime> oneTableDataList = new ArrayList<ClientSuccessRateRealtime>(); // 1张流水表的统计数据
//		for (int identify = 0; identify < 10; identify++) {
//			sqlParams.put("identify", identify);
//			sqlParams.put("date", date);
//			oneTableDataList = accessSlaveDao.queryAll("smsMonitor.getClientSuccessRate", sqlParams);
//			for(ClientSuccessRateRealtime csrr:oneTableDataList){
//				csrr.setDataTime(executeNext.toDate());
//				csrr.setCreateTime(now.toDate());
//			}
//			allTableDataList.addAll(oneTableDataList);
//		}
//

		List<Future<List<ClientSuccessRateRealtime> >> oneTableDataListFutrues = new ArrayList<>();

		for (int identify = 0; identify < 10; identify++) {
			final int finalIdentify = identify;
			oneTableDataListFutrues.add(ExecutorServiceCachePool.submit(new Callable<List<ClientSuccessRateRealtime>>() {
				@Override
				public List<ClientSuccessRateRealtime> call() throws Exception {
					Map<String, Object> sqlParams = new HashMap<String, Object>();
					sqlParams.put("identify", finalIdentify);
					sqlParams.put("date", date);
					sqlParams.put("boundData",executeNext.toDate());
					long beginTable = System.currentTimeMillis();
					List<ClientSuccessRateRealtime> oneTableDataList = accessSlaveDao.queryAll("smsMonitor.getClientSuccessRate", sqlParams);
					logger.debug("ClientSuccessRateRealtime统计表t_sms_access_*, params={}, cost {}ms",JacksonUtil.toJSON(sqlParams),System.currentTimeMillis()-beginTable);
					for(ClientSuccessRateRealtime csrr:oneTableDataList){
						csrr.setDataTime(executeNext.toDate());
						csrr.setCreateTime(now.toDate());
					}
					return oneTableDataList;
				}
			}));

		}
		for(Future<List<ClientSuccessRateRealtime>> oneTableDataFuture:oneTableDataListFutrues){
			List<ClientSuccessRateRealtime> successRateRealtimes = null;
			try {
				successRateRealtimes = oneTableDataFuture.get();
			} catch (Exception e) {
				logger.error("",e);
			}
			allTableDataList.addAll(successRateRealtimes);
		}
		
		if(allTableDataList.size() > 0){
			Map delParams = new HashMap<>();
			delParams.put("dataTime", executeNext.toDate());
			// 老数据删除
			clientSuccessRateRealtimeMapper.deleteByDataTime(delParams);
			clientSuccessRateRealtimeMapper.insertBatch(allTableDataList);
		}
		
		long end = System.currentTimeMillis();
		logger.info("【短信用户成功率指数统计任务】结束，统计时间= {}，统计耗时={}ms", executeNext.toString("yyyy-MM-dd HH:mm:ss"), (end - start));
		
		return true;
	}
	
	

	
	@Override
	public boolean channelSuccessRateRealtime(TaskInfo taskInfo) {
		
		logger.info("【短信通道成功率指数统计任务】开始");
		long start = System.currentTimeMillis();
		
		String timeFormart = taskInfo.getExecuteType().getFormat();
		final DateTime executeNext = UcpaasDateUtils.parseDate(taskInfo.getExecuteNext(), timeFormart);
		final String date = executeNext.toString("yyyyMMdd");
		final DateTime now = new DateTime();
		// 根据indentify和时间遍历统计十张流水表中的用户成功率数据
		Map<String, Object> sqlParams = new HashMap<String, Object>();
		List<ChannelSuccessRateRealtime> allTableDataList = new ArrayList<ChannelSuccessRateRealtime>(); // 10张record表的统计数据
//		List<ChannelSuccessRateRealtime> oneTableDataList = new ArrayList<ChannelSuccessRateRealtime>(); // 1张流水表的统计数据
//		for (int identify = 0; identify < 10; identify++) {
//			sqlParams.put("identify", identify);
//			sqlParams.put("date", date);
//			oneTableDataList = recordSlaveDao.queryAll("smsMonitor.getChannelSuccessRate", sqlParams);
//			for(ChannelSuccessRateRealtime csrr:oneTableDataList){
//				csrr.setDataTime(executeNext.toDate());
//				csrr.setCreateTime(now.toDate());
////				Map<String, Object> channelInfoMap = messageMasterDao.getOneInfo("smsMonitor.getChannelInfoById", csrr.getChannelId());
////				if(null != channelInfoMap){
////					csrr.setChannelName((String) channelInfoMap.get("channel_name"));
////				}else{
////					csrr.setChannelName("已删除通道");
////				}
////
//			}
//			allTableDataList.addAll(oneTableDataList);
//		}

		List<Future<List<ChannelSuccessRateRealtime> >> oneTableDataListFutrues = new ArrayList<>();

		for (int identify = 0; identify < 10; identify++) {
			final int finalIdentify = identify;
			oneTableDataListFutrues.add(ExecutorServiceCachePool.submit(new Callable<List<ChannelSuccessRateRealtime>>() {
				@Override
				public List<ChannelSuccessRateRealtime> call() throws Exception {
					Map<String, Object> sqlParams = new HashMap<String, Object>();
					sqlParams.put("identify", finalIdentify);
					sqlParams.put("date", date);
					sqlParams.put("boundData",executeNext.toDate());
					long beginTable = System.currentTimeMillis();
					List<ChannelSuccessRateRealtime> oneTableDataList = recordSlaveDao.queryAll("smsMonitor.getChannelSuccessRate", sqlParams);
					logger.debug("ChannelSuccessRateRealtime统计表t_sms_record_*, params={}, cost {}ms", JacksonUtil.toJSON(sqlParams),System.currentTimeMillis()-beginTable);
					for(ChannelSuccessRateRealtime csrr:oneTableDataList){
						csrr.setDataTime(executeNext.toDate());
						csrr.setCreateTime(now.toDate());
					}
					return oneTableDataList;
				}
			}));


		}
		for(Future<List<ChannelSuccessRateRealtime>> oneTableDataFuture:oneTableDataListFutrues){
			try {
				allTableDataList.addAll(oneTableDataFuture.get());
			} catch (Exception e) {
				logger.error("",e);
			}
		}
		
		
		if(allTableDataList.size() > 0){
			Map delParams = new HashMap<>();
			delParams.put("dataTime", executeNext.toDate());
			// 老数据删除
			channelSuccessRateRealtimeMapper.deleteByDataTime(delParams);
			channelSuccessRateRealtimeMapper.insertBatch(allTableDataList);
		}
		
		long end = System.currentTimeMillis();
		logger.info("【短信通道成功率指数统计任务】结束，统计时间= {}，统计耗时={}ms", executeNext.toString("yyyy-MM-dd HH:mm:ss"), (end - start));
		
		return true;
	}
}
