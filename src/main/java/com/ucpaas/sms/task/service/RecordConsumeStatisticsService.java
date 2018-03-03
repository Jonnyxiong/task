package com.ucpaas.sms.task.service;

import java.util.List;
import java.util.Map;

import com.ucpaas.sms.task.entity.record.RecordConsumeStatistics;

/**
 * @description 通道表
 * @author 
 * @date 2017-07-28
 */
public interface RecordConsumeStatisticsService {

    int insert(RecordConsumeStatistics model);
    
    int insertBatch(List<RecordConsumeStatistics> modelList);
    
    int delete(Integer id);
    
    int deleteByDate(String day);
    
    int update(RecordConsumeStatistics model);
    
    int updateSelective(RecordConsumeStatistics model);
    
    RecordConsumeStatistics getById(Integer id);
    
    List<RecordConsumeStatistics> queryList(Map page);
    
    int count(Map<String,Object> params);
    
    List<RecordConsumeStatistics> queryByDateLike(String yyyyMM);
    
    List<RecordConsumeStatistics> queryMonthly(Map<String, Object> sqlParams);
    
}
