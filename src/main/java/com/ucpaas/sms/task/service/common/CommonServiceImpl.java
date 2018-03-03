package com.ucpaas.sms.task.service.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.jsmsframework.common.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ucpaas.sms.task.constant.DbConstant.DbType;
import com.ucpaas.sms.task.dao.CommonDao;
import com.ucpaas.sms.task.model.TaskInfo;

/**
 * 公共业务
 * 
 * @author xiejiaan
 */
@Service
public class CommonServiceImpl implements CommonService {
	
	private static final Logger logger = LoggerFactory.getLogger(CommonServiceImpl.class);
	
	@Autowired
	private CommonDao commonDao;

	@Override
	public boolean callProcedure(TaskInfo taskInfo) {
		return commonDao.getDao(taskInfo.getDbType()).callProcedure(taskInfo);
	}
	
	@Override
	public boolean createTable(TaskInfo taskInfo) {
		String max_identify = null;
		// MAX_ACCESS_IDENTIFY 为分表范围标识
		if(taskInfo.getDbType() == DbType.ucpaas_message_record_master){
			max_identify = (String) getSysParams("MAX_RECORD_IDENTIFY").get("param_value");
		}else if(taskInfo.getDbType() == DbType.ucpaas_message_access_master){
			max_identify = (String) getSysParams("MAX_ACCESS_IDENTIFY").get("param_value");
		}
		
		if(StringUtils.isNoneBlank(max_identify)){
			return commonDao.getDao(taskInfo.getDbType()).callProcedureCreateTable(taskInfo, Integer.valueOf(max_identify));
		}else{
			logger.error("每日建表任务【失败】，DbType={}, 查询系统参数MAX_ACCESS_IDENTIFY为空", taskInfo.getDbType());
			return false;
		}
		
	}


	/**
	 * 每周一建表
	 */
	@Override
	public boolean sundayCreateTable(TaskInfo taskInfo , String tableName) {
		try {

			//判断时间是否是周一
			DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd");
			DateTime dt = DateTime.parse(taskInfo.getExecuteNext(), dtf);
			if(DateTimeConstants.MONDAY != dt.getDayOfWeek()){
				logger.error("每周一建表任务【失败】， 建表日期不是星期一");
				return true;
			}

			//执行日期加七天
			taskInfo.setExecuteNext(dt.plusDays(7).toString("yyyyMMdd"));

			//创建表
			Map<String, Object> sqlParam = new HashMap<String, Object>();
			sqlParam.put("tableName", tableName);
			sqlParam.put("executeNext", taskInfo.getExecuteNext());
			commonDao.getDao(taskInfo.getDbType()).insert("common.sundayCreateTable",sqlParam);
			//创建短信审核与关键字记录表
			tableName = "t_sms_auditkeyword_record";
			sqlParam.put("tableName", tableName);
			sqlParam.put("executeNext", taskInfo.getExecuteNext());
			commonDao.getDao(taskInfo.getDbType()).insert("common.sundayCreateTable",sqlParam);
			return true;
		} catch (Exception e) {
			logger.error("每周一建表任务【失败】，DbType={}, 创建{}表失败 ,错误报文 : {}", taskInfo.getDbType(),tableName,e.fillInStackTrace());
			return false;
		}
	}

	@Override
	public boolean releaseClientIdExpiredLock() {
		try {
			commonDao.getUcpaasMessageDao().update("common.releaseClientIdExpiredLock", null);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * 获取系统参数
	 */
	public Map<String,Object> getSysParams(String paramKey){
		return commonDao.getUcpaasMessageDao().getOneInfo("common.getSysParam", paramKey);
	}

	@Override
	public boolean dailyCreateTable(TaskInfo taskInfo, String tableName) {
		try {
			DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd");
			DateTime dt = DateTime.parse(taskInfo.getExecuteNext(), dtf);


			//创建表
			Map<String, Object> sqlParam = new HashMap<String, Object>();
			sqlParam.put("tableName", tableName);
			sqlParam.put("tableDate", dt.plusDays(7).toString("yyyyMMdd"));
			commonDao.getDao(taskInfo.getDbType()).insert("common.createTableWithBaseTable",sqlParam);
			return true;
		} catch (Exception e) {
			logger.error("每周日建表任务【失败】，DbType={}, 创建{}表失败 ,错误报文 : {}", taskInfo.getDbType(),tableName,e.fillInStackTrace());
			return false;
		}
	}

	@Override
	public boolean monthlyCreateTable(TaskInfo taskInfo, String tableName) {
		try {

			DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd");
			DateTime dt = DateTime.parse(taskInfo.getExecuteNext(), dtf);
			//创建表
			Map<String, Object> sqlParam = new HashMap<String, Object>();
			sqlParam.put("tableName", tableName);
			sqlParam.put("tableDate", dt.plusMonths(1).toString("yyyyMM"));
			commonDao.getDao(taskInfo.getDbType()).insert("common.createTableWithBaseTable",sqlParam);
			return true;
		} catch (Exception e) {
			logger.error("每周月建表任务【失败】，DbType={}, 创建{}表失败 ,错误报文 : {}", taskInfo.getDbType(),tableName,e.fillInStackTrace());
			return false;
		}
	}

	@Override
	public boolean weeklyCreateTable(TaskInfo taskInfo, String tableName) {

		try {
			//判断时间是否是周一
			DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd");
			DateTime dt = DateTime.parse(taskInfo.getExecuteNext(), dtf);
			if(DateTimeConstants.MONDAY != dt.getDayOfWeek()){
				logger.info("跳过每周建表任务， 建表日期不是星期一");
				return true;
			}

			//创建表
			Map<String, Object> sqlParam = new HashMap<String, Object>();
			sqlParam.put("tableName", tableName);
			sqlParam.put("tableDate", dt.plusDays(7).toString("yyyyMMdd"));
			commonDao.getDao(taskInfo.getDbType()).insert("common.createTableWithBaseTable",sqlParam);
			return true;
		} catch (Exception e) {
			logger.error("每周月建表任务【失败】，DbType={}, 创建{}表失败 ,错误报文 : {}", taskInfo.getDbType(),tableName,e.fillInStackTrace());
			return false;
		}
	}

	public static Date getAfterMonth(String beginMonth, int months, String fmt) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(fmt);
		Date date = sdf.parse(beginMonth);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, months); // 日期减 如果不够减会将月变动
		Date time = calendar.getTime();
		return time;
	}
}
