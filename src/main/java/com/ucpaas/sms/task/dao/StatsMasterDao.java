package com.ucpaas.sms.task.dao;

import javax.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

/**
 * ucpaas_message_stats 主库的dao类
 * 
 */
@Repository
public class StatsMasterDao extends BaseDao {

	@Override
	@Resource(name = "stats_master_sqlSessionTemplate")
	protected void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

}
