package com.ucpaas.sms.task.mapper.access;

import com.ucpaas.sms.task.entity.access.AccessChannelStatistics;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AccessChannelStatisticsMapper{

	int insert(AccessChannelStatistics model);
	
	int insertBatch(List<AccessChannelStatistics> modelList);
	
	int delete(Long id);
	
	int update(AccessChannelStatistics model);
	
	int updateSelective(AccessChannelStatistics model);
	
	AccessChannelStatistics getById(Long id);
	
	
	int count(Map<String, Object> params);

	int deleteByDate(String statTime);
	
	List<AccessChannelStatistics> queryAll(Map params);
	List<AccessChannelStatistics> queryAllGroupBy(Map params);


	List<AccessChannelStatistics> queryCommonlyChannel(Map params);

	List<AccessChannelStatistics> queryAllByClientids(Map params);

	List<AccessChannelStatistics> querySumByClientids(Map params);

	List<AccessChannelStatistics> queryMonthly(Map<String, Object> params);

	List<AccessChannelStatistics> findZuoriHoufufeiClientList(Integer date);

	/**
	 * 查询客户月入发送短信（客户每天发送及总数）
	 * @param ym 年月，例201708
	 * @param ymr 当月的第一天，例20170801
	 * @return
	 */
	List<Map<String, Object>> findSaleEveryDaySendSMSOfMonth(@Param("ym") String ym, @Param("ymr") String ymr);
}