package com.ucpaas.sms.task.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ucpaas.sms.task.constant.DbConstant.DbType;

/**
 * 公共的dao类
 * 
 * @author xiejiaan
 */
@Repository
public class CommonDao {
	@Autowired
	private MessageMasterDao messageMasterDao;
	@Autowired
	private AccessMasterDao accessMasterDao; 
	@Autowired
	private RecordMasterDao recordMasterDao;
	@Autowired
	private AccessSlaveDao accessSlaveDao;
	@Autowired
	private RecordSlaveDao recordSlaveDao;
	@Autowired
	private StatsMasterDao statsMasterDao;

	/**
	 * 根据DbType获取dao类
	 * 
	 * @param dbType
	 * @return
	 */
	public BaseDao getDao(DbType dbType) {
		switch (dbType) {
		case ucpaas_message_master:
			return messageMasterDao;
		case ucpaas_message_access_master:
			return accessMasterDao; 
		case ucpaas_message_record_master:
			return recordMasterDao;
		case ucpaas_message_access_slave:
			return accessSlaveDao;
		case ucpaas_message_record_slave:
			return recordSlaveDao;
		case ucpaas_message_stats_master:
			return statsMasterDao;
		default:
			return null;
		}
	}

	public MessageMasterDao getUcpaasMessageDao() {
		return messageMasterDao;
	}
	
	public AccessMasterDao getAccessMasterDao() {
		return accessMasterDao;
	}

 
}
