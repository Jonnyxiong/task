package com.ucpaas.sms.task.service;


import com.ucpaas.sms.task.entity.message.User;
import com.ucpaas.sms.task.mapper.message.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @description 用户管理
 * @author 黄文杰
 * @date 2017-07-27
 */
@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserMapper userMapper;
    
    @Override
	@Transactional
    public int insert(User model) {
        return this.userMapper.insert(model);
    }

    @Override
	@Transactional
    public int insertBatch(List<User> modelList) {
        return this.userMapper.insertBatch(modelList);
    }

	@Override
	@Transactional
    public int update(User model) {
		User old = this.userMapper.getById(model.getId());
		if(old == null){
			return 0;
		}
		return this.userMapper.update(model); 
    }

    @Override
	@Transactional
    public int updateSelective(User model) {
		User old = this.userMapper.getById(model.getId());
		if(old != null)
        	return this.userMapper.updateSelective(model);
		return 0;
    }

    @Override
    public User getById(Long id) {
    	if(id != null)
    		return this.userMapper.getById(id);
    	else
    		return null;
    }


    @Override
    public int count(Map<String,Object> params) {
		return this.userMapper.count(params);
    }
    
}
