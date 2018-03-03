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

import com.ucpaas.sms.task.constant.DbConstant.DbType;
import com.ucpaas.sms.task.dao.AccessMasterDao;
import com.ucpaas.sms.task.model.TaskInfo;
import com.ucpaas.sms.task.util.UcpaasDateUtils;

@Service
public class SmsReportStatServiceImpl implements SmsReportStatService {
	
	private static final Logger logger = LoggerFactory.getLogger(SmsReportStatServiceImpl.class);
	
	@Autowired
	private AccessMasterDao statDao;

	@Override
	public boolean stat(TaskInfo taskInfo) {

		if(taskInfo.getDbType() == DbType.ucpaas_message_access_master){
			
			return customerReportStat(taskInfo);
			
		}else if(taskInfo.getDbType() == DbType.ucpaas_message_record_master){
			return false;
		}else{
			return false;
		}
		
		
	}
	
	/**
	 * 客户运营运维统计报表
	 * @return
	 */
	private boolean customerReportStat(TaskInfo taskInfo){
		
		String statTime = null; // 统计数据的时间：yyyyMMdd或者yyyyMM
		String format = taskInfo.getExecuteType().getFormat();
		DateTime executePrev = UcpaasDateUtils.parseDate(taskInfo.getExecutePrev(), format);
		executePrev = executePrev.minusDays(3); //第前四天，也就是如果25号跑任务，则跑的是21号那天的数据
		statTime = executePrev.toString("yyyyMMdd");
		long begainTime = System.currentTimeMillis();
		logger.debug("客户运营运维统计报表任务：统计日期 = {} ------------------", statTime);
		
		// 按clientid,channelid,costfee,salefee,productfee 分组统计每个用户在每个通道下面各个价格的消耗情况以及当时的短信状态
		// 数据保存在 t_sms_customer_stat_temp
		taskInfo.setProcedureName("p_access_stat_data_generate");
		boolean result = statDao.callProcedure(taskInfo);
		if(!result){
			logger.info("客户运营运维统计报表任务【失败】：调用 p_access_stat_data_generate 统计失败");
			return false;
		}
		
		
		// 清除t_sms_access_channel_statistics 表统计日的老数据
		statDao.delete("smsReportStat.clearStatDataByTime", statTime);
		
		// 每日数据统计：每个客户在每个通道下面的明细数据(不包括通道0)
		List<Map<String, Object>> dailyDetailList = statDao.getSearchList("smsReportStat.getDailyDetail", null);
		
		// 每日数据统计：通道为0状态为4的情况
		List<Map<String, Object>> dailyDetailStateEq4List = statDao.getSearchList("smsReportStat.getDailyStateEq4", null);
		
		// 每日数据统计：拦截数据，状态等于0/5/7/8/9/10
		List<Map<String, Object>> dailyDailyInterceptList = statDao.getSearchList("smsReportStat.getDailyIntercept", null);
		
		List<Map<String, Object>> dailyDataList = new ArrayList<Map<String, Object>>();
		dailyDataList.addAll(dailyDetailList);
		dailyDataList.addAll(dailyDetailStateEq4List);
		dailyDataList.addAll(dailyDailyInterceptList);
		
		Map<String, Object> sqlParams = new HashMap<String, Object>();
		sqlParams.put("dataList", dailyDataList);
		if(dailyDataList.size() > 0){
			statDao.insert("smsReportStat.saveCustomerStatData", sqlParams);
		}else{
			logger.info("客户运营运维统计报表任务【结束】：计算出来的每日统计数据为空");
			return true;
		}
		
		// 每日数据统计：合计数据
		statDao.insert("smsReportStat.getDailyTotal", statTime);
		
		// 清除t_sms_access_channel_statistics 表统计月的老数据
		statTime = executePrev.toString("yyyyMM");
		statDao.delete("smsReportStat.clearStatDataByTime", statTime);
		
		// 清空t_sms_customer_stat_temp临时数据表
		statDao.delete("smsReportStat.clearStatTempTable", null);
		
		// 每月数据统计：生成统计每月数据的基础数据
		statDao.insert("smsReportStat.generateMonthTempData", statTime);
		
		// 每月数据统计：每个客户在每个通道下面的明细数据
		List<Map<String, Object>> monthlyDetailList = statDao.getSearchList("smsReportStat.getMonthlyDetail", null);
		
		sqlParams = new HashMap<String, Object>();
		sqlParams.put("statTime", statTime);
		// 每月数据统计：拦截数据，状态等于0/5/7/8/9/10
		List<Map<String, Object>> monthlyInterceptList = statDao.getSearchList("smsReportStat.getMonthlyIntercept", sqlParams);
		
		List<Map<String, Object>> monthlyDataList = new ArrayList<Map<String, Object>>();
		monthlyDataList.addAll(monthlyDetailList);
		monthlyDataList.addAll(monthlyInterceptList);
		sqlParams = new HashMap<String, Object>();
		sqlParams.put("dataList", monthlyDataList);
		if(dailyDataList.size() > 0){
			statDao.insert("smsReportStat.saveCustomerStatData", sqlParams);
		}else{
			logger.info("客户运营运维统计报表任务【结束】：计算出来的每月统计数据为空");
			return true;
		}
		
		// 每月数据统计：合计数据
		statDao.insert("smsReportStat.getMonthlyTotal", statTime);
		
		logger.debug("客户运营运维统计报表任务【结束】：耗时 = {}", System.currentTimeMillis() - begainTime);
		return true;
	}
	

}
