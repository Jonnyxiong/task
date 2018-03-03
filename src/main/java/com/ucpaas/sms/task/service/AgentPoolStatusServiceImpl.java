package com.ucpaas.sms.task.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ucpaas.sms.task.dao.MessageMasterDao;
import com.ucpaas.sms.task.model.TaskInfo;

/**
 * OEM客户短信池状态处理服务实现
 */
@Service
@Transactional
public class AgentPoolStatusServiceImpl implements AgentPoolStatusService {
	
	@Autowired
	private MessageMasterDao messageMasterDao;

	private static final Logger logger = LoggerFactory.getLogger(AgentPoolStatusServiceImpl.class);
	
	
	public boolean execute(TaskInfo taskInfo) {
		List<Map<String, Object>> expiredAgentPoolList = null;

		// 查询过期时间小于当前时间的代理商短信池信息
		List<Map<String, Object>> tempList = messageMasterDao.getSearchList("agentPoolStatus.findExpiredAgentPoolList", null);
		if( null != tempList && tempList.size() > 0){
			expiredAgentPoolList = new ArrayList<>(tempList.size());
			expiredAgentPoolList.addAll(tempList);
			logger.debug("【OEM代理商短信池表状态检查任务】，检查OEM代理商短信池表中到期时间小于当前时间的短信，过期短信池ID = {}", expiredAgentPoolList);
		}

		
		// 更新OEM代理商短信池过期的池状态
		if(null != expiredAgentPoolList){
			Map<String, Object> sqlParams = new HashMap<>();
			sqlParams.put("expiredAgentPoolList", expiredAgentPoolList);
			int updateNum = messageMasterDao.update("agentPoolStatus.updateAgentPoolStatusByPoolIds", sqlParams);
			if(updateNum == expiredAgentPoolList.size()){
				logger.info("【OEM代理商短信池表状态检查任务】，更新OEM代理商短信池过期的池状态[成功]，过期池ID = {}", expiredAgentPoolList);
			}else{
				logger.info("【OEM代理商短信池表状态检查任务】，更新OEM代理商短信池过期的池状态[失败]，过期池ID = {}", expiredAgentPoolList);
				return false;
			}
		}
		
		return true;
	}

}
