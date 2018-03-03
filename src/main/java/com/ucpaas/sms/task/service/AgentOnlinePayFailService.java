package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.entity.access.AccessChannelStatistics;
import com.ucpaas.sms.task.model.ResultVO;
import com.ucpaas.sms.task.model.TaskInfo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface AgentOnlinePayFailService {
	/**
	 * 代理商在线支付是否失败任务
	 *
	 * @return 是否成功
	 */
	boolean execute(TaskInfo taskInfo);
}
