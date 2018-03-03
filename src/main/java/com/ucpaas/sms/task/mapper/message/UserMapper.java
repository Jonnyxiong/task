package com.ucpaas.sms.task.mapper.message;

import com.ucpaas.sms.task.entity.message.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @description 用户管理
 * @author 黄文杰
 * @date 2017-07-27
 */
@Repository
public interface UserMapper{

	int insert(User model);
	
	int insertBatch(List<User> modelList);

	int update(User model);
	
	int updateSelective(User model);
	
	User getById(Long id);

	
	int count(Map<String, Object> params);

}