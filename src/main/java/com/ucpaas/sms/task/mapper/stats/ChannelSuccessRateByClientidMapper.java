package com.ucpaas.sms.task.mapper.stats;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ucpaas.sms.task.entity.stats.ChannelSuccessRateByClientid;

/**
 * @description record流水表发送总量查询结果(按用户区分)
 * @author huangwenjie
 * @date 2017-06-14
 */
@Repository
public interface ChannelSuccessRateByClientidMapper{

	int insert(ChannelSuccessRateByClientid model);
	
	int insertBatch(List<ChannelSuccessRateByClientid> modelList);
	
	int delete(Long id);
	
	int update(ChannelSuccessRateByClientid model);
	
	int updateSelective(ChannelSuccessRateByClientid model);
	
	ChannelSuccessRateByClientid getById(Long id);
	
	int count(Map<String,Object> params);

	int deleteByDataTime(Map delParams);

	List<ChannelSuccessRateByClientid> query(Map channelSuccessRateRealtimeParams);

}