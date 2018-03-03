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
public class ClientPoolStatusServiceImpl implements ClientPoolStatusService {
	
	@Autowired
	private MessageMasterDao ucpaasMessageDao;

	private static final Logger logger = LoggerFactory.getLogger(ClientPoolStatusServiceImpl.class);
	
	
	public boolean execute(TaskInfo taskInfo) {
		List<Map<String, Object>> expiredClientPoolList = null;

		// 查询过期时间小于当前时间的客户池信息
		List<Map<String, Object>> tempList = ucpaasMessageDao.getSearchList("clientPoolStatus.findExpiredClientPoolList", null);
		if( null != tempList && tempList.size() > 0){
			expiredClientPoolList = new ArrayList<Map<String, Object>>(tempList.size());
			expiredClientPoolList.addAll(tempList);
			logger.debug("【OEM短信客户池表状态检查任务】，检查OEM客户短信池表中到期时间小于当前时间的客户池，过期短信池ID = {}", expiredClientPoolList);
		}

		
		// 更新OEM客户短信池过期的客户池状态
		if(null != expiredClientPoolList){
			Map<String, Object> sqlParams = new HashMap<String, Object>();
			sqlParams.put("expiredClientPoolList", expiredClientPoolList);
			int updateNum = ucpaasMessageDao.update("clientPoolStatus.updateClientPoolStatusByPoolIds", sqlParams);
			if(updateNum == expiredClientPoolList.size()){
				logger.info("【OEM短信客户池表状态检查任务】，更新OEM客户短信池过期的客户池状态[成功]，过期池ID = {}", expiredClientPoolList);
			}else{
				logger.info("【OEM短信客户池表状态检查任务】，更新OEM客户短信池过期的客户池状态[失败]，过期池ID = {}", expiredClientPoolList);
				return false;
			}
		}
		
		return true;
	}

}
