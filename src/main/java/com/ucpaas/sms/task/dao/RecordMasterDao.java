package com.ucpaas.sms.task.dao;

import javax.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

/**
 * ucpaas_message_record 主库DAO
 *
 */
@Repository
public class RecordMasterDao extends BaseDao {

	@Override
	@Resource(name = "record_master_sqlSessionTemplate")
	protected void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

}
