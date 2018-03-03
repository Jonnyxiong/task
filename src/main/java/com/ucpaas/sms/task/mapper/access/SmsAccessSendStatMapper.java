package com.ucpaas.sms.task.mapper.access;

import com.ucpaas.sms.task.entity.access.SmsAccessSendStat;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
 
/**
 * @description 客户发送量表
 * @author 黄文杰
 * @date 2017-07-25
 */
@Repository
public interface SmsAccessSendStatMapper{

	int insert(SmsAccessSendStat model);
	
	int insertBatch(List<SmsAccessSendStat> modelList);
	
	int delete(Integer id);
	
	int update(SmsAccessSendStat model);
	
	int updateSelective(SmsAccessSendStat model);
	
	SmsAccessSendStat getById(Integer id); 
	
	int count(Map<String,Object> params);

    int deleteByDate(Integer date);

	List<SmsAccessSendStat> queryAll(Map params);

	int deleteByDateLike(@Param("yyyyMM") String yyyyMM);

    List<SmsAccessSendStat> queryByDateLike(String yyyyMM);
}