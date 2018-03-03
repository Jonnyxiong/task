package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.entity.message.User;

import java.util.List;
import java.util.Map;

/**
 * @description 用户管理
 * @author 黄文杰
 * @date 2017-07-27
 */
public interface UserService {

    public int insert(User model);
    
    public int insertBatch(List<User> modelList);
    
    public int update(User model);
    
    public int updateSelective(User model);
    
    public User getById(Long id);

    
    public int count(Map<String, Object> params);
    
}
