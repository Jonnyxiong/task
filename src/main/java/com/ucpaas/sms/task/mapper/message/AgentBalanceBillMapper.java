package com.ucpaas.sms.task.mapper.message;

import com.ucpaas.sms.task.entity.message.AgentBalanceBill;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @description 代理商帐户余额收支明细管理
 * @author 黄文杰
 * @date 2017-07-27
 */
@Repository
public interface AgentBalanceBillMapper{

	int insert(AgentBalanceBill model);
	
	int insertBatch(List<AgentBalanceBill> modelList);

	
	int update(AgentBalanceBill model);
	
	int updateSelective(AgentBalanceBill model);
	
	AgentBalanceBill getById(Integer id);

	
	int count(Map<String, Object> params);

	List<AgentBalanceBill> queryAll(Map params);
}