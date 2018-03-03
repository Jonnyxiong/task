package com.ucpaas.sms.task.mapper.stats;

import org.springframework.stereotype.Repository;

import com.ucpaas.sms.task.entity.stats.ChannelSuccessRateRealtime;

import java.util.List;
import java.util.Map;

@Repository
public interface ChannelSuccessRateRealtimeMapper{

	int insert(ChannelSuccessRateRealtime model);
	
	int insertBatch(List<ChannelSuccessRateRealtime> modelList);
	
	int delete(Long id);
	
	ChannelSuccessRateRealtime getById(Long id);
	
	int update(ChannelSuccessRateRealtime model);
	
	
	int updateStatus(ChannelSuccessRateRealtime model);
	
	
	int updateSelective(ChannelSuccessRateRealtime model);
	
	int count(Map parmas);

	int deleteByDataTime(Map delParams);

	List<ChannelSuccessRateRealtime> query(Map channelSuccessRateRealtimeParams);

}