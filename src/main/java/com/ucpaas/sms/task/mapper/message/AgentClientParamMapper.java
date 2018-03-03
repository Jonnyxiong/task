package com.ucpaas.sms.task.mapper.message;


import com.ucpaas.sms.task.entity.message.AgentClientParam;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentClientParamMapper{

	int insert(AgentClientParam agentClientParam);
	
	int insertBatch(List<AgentClientParam> agentClientParamList);
	
	int delete(long id);
	
	int update(AgentClientParam agentClientParam);
	
	int updateSelective(AgentClientParam agentClientParam);
	
	AgentClientParam getByParamKey(String paramKey);
	
	int count(AgentClientParam agentClientParam);

}