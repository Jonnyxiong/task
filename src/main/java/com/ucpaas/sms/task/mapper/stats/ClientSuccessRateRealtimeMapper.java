package com.ucpaas.sms.task.mapper.stats;

import org.springframework.stereotype.Repository;

import com.ucpaas.sms.task.entity.stats.ClientSuccessRateRealtime;

import java.util.List;
import java.util.Map;

@Repository
public interface ClientSuccessRateRealtimeMapper{

	int insert(ClientSuccessRateRealtime model);
	
	int insertBatch(List<ClientSuccessRateRealtime> modelList);
	
	int delete(Long id);
	
	ClientSuccessRateRealtime getById(Long id);
	
	int update(ClientSuccessRateRealtime model);
	
	
	int updateStatus(ClientSuccessRateRealtime model);
	
	
	int updateSelective(ClientSuccessRateRealtime model);
	
	int count(Map parmas);

	int deleteByDataTime(Map parmas);

	List<ClientSuccessRateRealtime> query(Map channelSuccessRateRealtimeParams);

}