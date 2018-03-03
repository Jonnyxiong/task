package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.entity.access.AccessChannelStatistics;
import com.ucpaas.sms.task.model.ResultVO;
import com.ucpaas.sms.task.model.TaskInfo;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface AccessChannelStatisticsService {

	public ResultVO insert(AccessChannelStatistics model);

	public ResultVO insertBatch(List<AccessChannelStatistics> modelList);

	public ResultVO delete(Long id);

	public ResultVO update(AccessChannelStatistics model);

	public ResultVO updateSelective(AccessChannelStatistics model);

	public ResultVO getById(Long id);

	public ResultVO count(Map<String, Object> params);

	/**
	 * 第三天的用户(客户) 运营、运维报表 如2017-02-21跑的任务，则统计计算2017-02-18的数据
	 * @return
	 */
	public boolean fourDaysAgo(TaskInfo taskInfo);

	/**
	 * 昨日的用户(客户) 运营、运维报表 如2017-02-21跑的任务，则统计计算2017-02-20的数据
	 * @return
	 */
	public boolean yesterday(TaskInfo taskInfo);

	List<AccessChannelStatistics> queryAll(Map<String, Object> params);

	@Transactional(value = "access")
	List<AccessChannelStatistics> queryAllGroupBy(Map<String, Object> params);

	int deleteByDate(String statTime);

	List<AccessChannelStatistics> queryMonthly(Map<String, Object> params);

	/**
	 * 需在用户侧统计和通道侧统计跑完之后才能跑
	 * 因为是为了填补客户发送量表 t_sms_access_send_stat的costfee通道成本
	 * 因为是为了填补通道消耗量表 t_sms_record_consume_stat的saletotal销售收入
	 * @param taskInfo
	 * @return
	 */
    boolean fixAccessRecordStatistic(TaskInfo taskInfo);

	/**
	 * 根据通道id、归属销售、部门、短信类型、付费类型、运营商类型、统计日期查询用户侧的销售收入
	 * @param channelid
	 * @param belongBusiness
	 * @param departmentId
	 * @param smstype
	 * @param paytype
	 * @param operatorstype
	 * @param yyyyMMoryyyyMMdd
	 * @return
	 */
	BigDecimal calculateSaleTotal(Integer channelid, Long belongBusiness, Integer departmentId, Integer smstype, Integer paytype, Integer operatorstype, String yyyyMMoryyyyMMdd);
}
