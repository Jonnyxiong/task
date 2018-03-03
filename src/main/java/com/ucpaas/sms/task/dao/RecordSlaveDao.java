package com.ucpaas.sms.task.dao;

import javax.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

/**
 * ucpaas_message_record 从库DAO
 *
 */
@Repository
public class RecordSlaveDao extends BaseDao {

	@Override
	@Resource(name = "record_slave_sqlSessionTemplate")
	protected void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

}
