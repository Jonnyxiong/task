package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.entity.message.AccountGroup;

import java.util.List;
import java.util.Map;

/**
 * @description 客户组信息管理
 * @author 黄文杰
 * @date 2017-07-27
 */
public interface AccountGroupService {

    public int insert(AccountGroup model);
    
    public int insertBatch(List<AccountGroup> modelList);

    
    public int update(AccountGroup model);
    
    public int updateSelective(AccountGroup model);
    
    public AccountGroup getByAccountGid(Integer accountGid);

    
    public int count(Map<String, Object> params);

}
