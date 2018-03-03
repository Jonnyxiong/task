package com.ucpaas.sms.task.mapper.message;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.ucpaas.sms.task.entity.message.AgentBalanceBill;

@Repository
public interface AgentInfoMapper {
	List<String> findOemAgentInfoByAgentIds(@Param("set") Set<String> ids);

	String getClientNameByClientId(String clientId);

	String getAgentBalanceByAgentId(String agentId);

	String getUserPriceByClientId(Map params);

	int checkAccessChannelStatDone(String date);

	int subAgentAccountBalance(Map params);

	int addAgentBalanceBill(AgentBalanceBill agentBalanceBill);
}