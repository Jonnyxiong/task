package com.ucpaas.sms.task.service;

import java.util.List;
import java.util.Map;

import com.ucpaas.sms.task.model.TaskInfo;

public interface SendReprotMonthService {
	
	
	/** 
	 * @Title: sendReprotMonth 
	 * @Description: 每月5号给客户发送上一个月月报
	 * @param taskInfo
	 * @return
	 * @return: boolean
	 */
	boolean sendReprotMonth(TaskInfo taskInfo);
	
	/** 
	 * @Title: queryAgentListByAgentType 
	 * @Description: 查询代理商列表
	 * @param params 代理商类型type
	 * @return
	 * @return: List<Map<String,Object>>
	 */
	List<Map<String,Object>> queryAgentIdListByAgentType(Map<String,Object> params);
	
	/** 
	 * @Title: queryLoginMailByAgentID 
	 * @Description: 查询代理商的登录邮箱
	 * @param agent_id
	 * @return
	 * @return: String
	 */
	String queryLoginMailByAgentID(Integer agent_id);
	
	/** 
	 * @Title: queryLoginMailByClientId 
	 * @Description: 查询客户的登录邮箱
	 * @param client_id
	 * @return
	 * @return: String
	 */
	String queryLoginMailByClientId(String client_id);
	
	/** 
	 * @Title: queryPaytypeByClientId 
	 * @Description: 查询客户的付费类型，通过客户id
	 * @param client_id
	 * @return
	 * @return: String
	 */
	String queryPaytypeByClientId(String client_id);
	
	
	/** 
	 * @Title: queryAgentClientIdListByAgentId 
	 * @Description: 查询客户列表
	 * @param params agentId
	 * @return
	 * @return: List<String>
	 */
	List<String> queryAgentClientIdListByAgentId(Map<String,Object> params);
	
	/** 
	 * @Title: queryCancelSubscribeAgentIdList 
	 * @Description: 查询已经取消订阅的代理商id列表
	 * @return
	 * @return: List<String>
	 */
	List<String> queryCancelSubscribeAgentIdList();
	
	/** 
	 * @Title: queryCancelSubscribeClientIdList 
	 * @Description: 查询已经取消订阅的客户id列表
	 * @return
	 * @return: List<String>
	 */
	List<String> queryCancelSubscribeClientIdList();

}
