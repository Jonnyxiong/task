package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.entity.message.AgentBalanceBill;
import com.ucpaas.sms.task.model.TaskInfo;

import java.util.List;
import java.util.Map;

/**
 * @description 代理商帐户余额收支明细管理
 * @author 黄文杰
 * @date 2017-07-27
 */
public interface AgentBalanceBillService {

    public int insert(AgentBalanceBill model);
    
    public int insertBatch(List<AgentBalanceBill> modelList);

    
    public int update(AgentBalanceBill model);
    
    public int updateSelective(AgentBalanceBill model);
    
    public AgentBalanceBill getById(Integer id);
    
    public List<AgentBalanceBill> queryAll(Map params);
    
    public int count(Map<String, Object> params);
    /**
     * 【预警】代理商余额预警
     *  @return 是否成功
     */
    boolean agentBalanceAlarm(TaskInfo taskInfo);

    
}
