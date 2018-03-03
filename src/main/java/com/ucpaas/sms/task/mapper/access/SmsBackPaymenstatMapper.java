package com.ucpaas.sms.task.mapper.access;

import com.ucpaas.sms.task.entity.access.SmsBackPaymenstat;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @description 回款金额统计表
 * @author 黄文杰
 * @date 2017-07-25
 */
@Repository
public interface SmsBackPaymenstatMapper{

	int insert(SmsBackPaymenstat model);
	
	int insertBatch(List<SmsBackPaymenstat> modelList);
	
	int delete(Integer id);
	
	int update(SmsBackPaymenstat model);
	
	int updateSelective(SmsBackPaymenstat model);
	
	SmsBackPaymenstat getById(Integer id);
	
	
	int count(Map<String,Object> params);

    int deleteByDateLike(String yyyyMM);
}