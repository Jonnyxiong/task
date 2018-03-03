package com.ucpaas.sms.task.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.ucpaas.sms.task.entity.record.RecordChannelStatistics;
import com.ucpaas.sms.task.model.TaskInfo;
import org.springframework.transaction.annotation.Transactional;

public interface RecordChannelStatisticsService {
	
int insert(RecordChannelStatistics model);
	
	int insertBatch(List<RecordChannelStatistics> modelList);
	 
	
	int update(RecordChannelStatistics model);
	
	int updateSelective(RecordChannelStatistics model);
	
	RecordChannelStatistics getById(Long id);

	List<RecordChannelStatistics> queryAll(Map<String, Object> params);


    List<RecordChannelStatistics> queryAllGroupBy(Map<String, Object> params);

    int count(Map<String, Object> params);

	int deleteByDate(String statTime);

	/**
	 * 第四天前的Record流水表统计
	 * @param taskInfo
	 * @return
	 */
	public boolean fourDaysAgo(TaskInfo taskInfo);

	/**
	 * 昨日的Record流水表数据统计
	 * @param taskInfo
	 * @return
	 */
	boolean yesterday(TaskInfo taskInfo);
	
	List<RecordChannelStatistics> queryMonthly(Map<String, Object> params);
	
	/**
	 * 查询clientId的通道总成本
	 * @param date
	 * @param clientId
	 * @param BelongSale
	 * @param smsType
	 * @param payType
	 * @param operatorstype
	 * @return
	 */
	public BigDecimal computeClientChannelCostFee(String clientId, Long BelongSale, Integer smsType, Integer payType, String yyyyMMoryyyyMMdd, Integer operatorstype);

}
