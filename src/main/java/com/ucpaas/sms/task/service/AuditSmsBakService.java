package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.model.TaskInfo;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 每日审核记录备份
 * 
 */
public interface AuditSmsBakService {

	/**
	 * 存在漏洞的代码，不再使用  --黄文杰
	 * 每日审核记录备份
	 * 
	 * @return 是否成功
	 */
	@Deprecated
	boolean bakAudit(TaskInfo taskInfo);

	/**
	 * 每日审核记录备份
	 * @param taskInfo
	 * @return
	 */
	boolean bakAuditAndSms(TaskInfo taskInfo);

    void backupAuditSms(Long id, Date createtime);

	@Transactional("access")
	void backupAudit(Long auditid, Date createtime);
}
