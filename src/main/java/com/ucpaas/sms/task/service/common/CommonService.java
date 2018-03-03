package com.ucpaas.sms.task.service.common;

import com.ucpaas.sms.task.model.TaskInfo;

/**
 * 公共业务
 * 
 * @author xiejiaan
 */
public interface CommonService {

	/**
	 * 调用存储过程
	 * 
	 * @param taskInfo
	 * @return 是否成功
	 */
	boolean callProcedure(TaskInfo taskInfo);
 

	boolean releaseClientIdExpiredLock();

	/**
	 * 每日建表任务
	 * 根据数据库类型建ACCESS或则RECORD流水表
	 * 
	 * @param taskInfo
	 * @return
	 */
	boolean createTable(TaskInfo taskInfo);

	/**
	 * 每周一建表
	 */
	boolean sundayCreateTable(TaskInfo taskInfo , String tableName);

	/**
	 *  每日建表任务 表名格式为 tableName_yyyyMMdd
	 *  创建7天后的表
	 * @param taskInfo
	 * @return
	 */
	boolean dailyCreateTable(TaskInfo taskInfo, String tableName);

	/**
	 *  每月建表任务 表名格式为 tableName_yyyyMM
	 *  创建一个月后的表
	 * @param taskInfo
	 * @return
	 */
	boolean monthlyCreateTable(TaskInfo taskInfo, String tableName);


	/**
	 *  每周建表任务 表名格式为 tableName_yyyyMMdd  周一的日期
	 *  创建7天后的表
	 * @param taskInfo
	 * @return
	 */
	boolean weeklyCreateTable(TaskInfo taskInfo, String tableName);

}
