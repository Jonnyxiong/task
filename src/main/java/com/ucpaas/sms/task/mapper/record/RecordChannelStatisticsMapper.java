package com.ucpaas.sms.task.mapper.record;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ucpaas.sms.task.entity.record.RecordChannelStatistics;
import com.ucpaas.sms.task.entity.record.RecordConsumeStatistics;

@Repository
public interface RecordChannelStatisticsMapper{

	int insert(RecordChannelStatistics model);
	
	int insertBatch(List<RecordChannelStatistics> modelList);
	
	int update(RecordChannelStatistics model);
	
	int updateSelective(RecordChannelStatistics model);
	
	RecordChannelStatistics getById(Long id);

	List<RecordChannelStatistics> queryAll(Map<String, Object> params);
	List<RecordChannelStatistics> queryAllGroupBy(Map<String, Object> params);


	int count(Map<String, Object> params);

	int deleteByDate(String statTime);

	List<RecordChannelStatistics> queryMonthly(Map<String, Object> params);

	List<RecordChannelStatistics> queryCommonlyChannel(Map<String, Object> params);

	List<RecordChannelStatistics> queryAllByClientids(Map<String, Object> params);

	List<RecordChannelStatistics> querySumByClientids(Map<String, Object> params);
	
	List<RecordConsumeStatistics> generateDataForRecordConsume(String statDay);

}