package com.ucpaas.sms.task.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ucpaas.sms.task.dao.AccessSlaveDao;
import com.ucpaas.sms.task.dao.MessageMasterDao;
import com.ucpaas.sms.task.dao.RecordSlaveDao;
import com.ucpaas.sms.task.dao.StatsMasterDao;
import com.ucpaas.sms.task.model.TaskInfo;
import com.ucpaas.sms.task.util.UcpaasDateUtils;

/**
 * 通道质量历史监控统计任务
 */
@Service
@Transactional
public class ChannelQaHistoryServiceImpl implements ChannelQaHistoryService {

	private static final Logger logger = LoggerFactory.getLogger(ChannelQaHistoryServiceImpl.class);

	@Autowired
	private AccessSlaveDao accessSlaveDao;
	@Autowired
	private StatsMasterDao statsMasterDao;
	@Autowired
	private RecordSlaveDao recordSlaveDao;
	@Autowired
	private MessageMasterDao messageMasterDao;
	
	@Override
	public boolean channelQualityIndexesStat(TaskInfo taskInfo) {
		
		logger.info("【短信通道历史质量指数统计任务】开始");
		long start = System.currentTimeMillis();
		
		String timeFormart = taskInfo.getExecuteType().getFormat();
		DateTime executeNext = UcpaasDateUtils.parseDate(taskInfo.getExecuteNext(), timeFormart);
		executeNext = executeNext.minusDays(2);
		String date = executeNext.toString("yyyyMMdd");
		
		// 根据indentify和时间遍历统计十张流水表中的通道质量指数
		Map<String, Object> sqlParams = new HashMap<String, Object>();
		List<Map<String, Object>> allTableDataList = new ArrayList<Map<String, Object>>(); // 10张record表的统计数据
		List<Map<String, Object>> oneTableDataList = new ArrayList<Map<String, Object>>(); // 1张流水表的统计数据
		for (int identify = 0; identify < 10; identify++) {
			sqlParams.put("identify", identify);
			sqlParams.put("date", date);
			oneTableDataList = recordSlaveDao.getSearchList("channelQaHistory.getChannelIndexes", sqlParams);
			allTableDataList.addAll(oneTableDataList);
		}
		
		// 从通道表中查询通道属性记录到统计流水中
		for (Map<String, Object> map : allTableDataList) {
			Map<String, Object> channelInfoMap = messageMasterDao.getOneInfo("channelQaHistory.getChannelInfoById", map.get("channel_id"));
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
		
		Map<String, Object> indexesDataMap = new HashMap<String, Object>();
		if(allTableDataList.size() > 0){
			// a.将通道质量指数统计数据保存到监控库
			indexesDataMap.put("data_time", date);
			// 老数据删除
			statsMasterDao.delete("channelQaHistory.deleteChannelIndexesDataByTime", indexesDataMap);
			indexesDataMap.put("dataList", allTableDataList);
			statsMasterDao.insert("channelQaHistory.saveChannelIndexesData", indexesDataMap);
		}
		
		long end = System.currentTimeMillis();
		logger.info("【短信通道历史质量指数统计任务】结束，统计时间= {}，统计耗时={}ms", date, (end - start));
		
		return true;
	}
	
	@Override
	public boolean clientQualityIndexesStat(TaskInfo taskInfo) {
		
		logger.info("【短信用户历史质量指数统计任务】开始");
		long start = System.currentTimeMillis();
		
		String timeFormart = taskInfo.getExecuteType().getFormat();
		DateTime executeNext = UcpaasDateUtils.parseDate(taskInfo.getExecuteNext(), timeFormart);
		executeNext = executeNext.minusDays(2);
		String date = executeNext.toString("yyyyMMdd");
		
		// 根据indentify和时间遍历统计十张流水表中的用户质量指数
		Map<String, Object> sqlParams = new HashMap<String, Object>();
		List<Map<String, Object>> allTableDataList = new ArrayList<Map<String, Object>>(); // 10张access表的统计数据
		List<Map<String, Object>> oneTableDataList = new ArrayList<Map<String, Object>>(); // 1张流水表的统计数据
		for (int identify = 0; identify < 10; identify++) {
			sqlParams.put("identify", identify);
			sqlParams.put("date", date);
			oneTableDataList = accessSlaveDao.getSearchList("channelQaHistory.getClientIndexes", sqlParams);
			allTableDataList.addAll(oneTableDataList);
		}
		
		
		Map<String, Object> indexesDataMap = new HashMap<String, Object>();
		if(allTableDataList.size() > 0){
			// a.将用户质量指数统计数据保存到监控库
			indexesDataMap.put("data_time", date);
			// 老数据删除
			statsMasterDao.delete("channelQaHistory.deleteClientIndexesDataByTime", indexesDataMap);
			indexesDataMap.put("dataList", allTableDataList);
			statsMasterDao.insert("channelQaHistory.saveClientIndexesData", indexesDataMap);
		}
		
		long end = System.currentTimeMillis();
		logger.info("【短信通道历史质量指数统计任务】结束，统计时间= {}，统计耗时={}ms", date, (end - start));
		
		return true;
	}

}
