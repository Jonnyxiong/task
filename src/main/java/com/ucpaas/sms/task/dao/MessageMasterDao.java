package com.ucpaas.sms.task.dao;

import javax.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

/**
 * ucpaas_message库的dao类
 * 
 * @author xiejiaan
 */
@Repository
public class MessageMasterDao extends BaseDao {

	@Override
	@Resource(name = "ucpaas_message_sqlSessionTemplate")
	protected void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

}
