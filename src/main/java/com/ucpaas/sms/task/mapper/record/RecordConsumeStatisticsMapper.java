package com.ucpaas.sms.task.mapper.record;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ucpaas.sms.task.entity.record.RecordChannelStatistics;
import com.ucpaas.sms.task.entity.record.RecordConsumeStatistics;

@Repository
public interface RecordConsumeStatisticsMapper{

	int insert(RecordConsumeStatistics model);
	
	int insertBatch(List<RecordConsumeStatistics> modelList);
	
	int delete(Integer id);
	
	int deleteByDate(String day);
	
	int update(RecordConsumeStatistics model);
	
	int updateSelective(RecordConsumeStatistics model);
	
	RecordConsumeStatistics getById(Integer id);
	
	List<RecordConsumeStatistics> queryList(Map page);
	
	int count(Map<String,Object> params);

	List<RecordConsumeStatistics> queryMonthly(Map<String, Object> sqlParams);
	
	List<RecordConsumeStatistics> queryByDateLike(String yyyyMM);

}