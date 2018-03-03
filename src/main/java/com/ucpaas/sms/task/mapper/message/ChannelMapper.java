package com.ucpaas.sms.task.mapper.message;


import com.ucpaas.sms.task.entity.message.Channel;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ChannelMapper{

//	int insert(Channel channel);
	
//	int insertBatch(List<Channel> channelList);
	
	Channel getById(Integer id);

	Channel getByCid(Integer cid);

	List<Channel> queryList(Channel channel);
	
	int count(Channel channel);

}