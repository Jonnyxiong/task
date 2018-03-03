package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.entity.message.Account;

import java.util.List;
import java.util.Map;

/**
 * @author 黄文杰
 * @description 客户管理
 * @date 2017-07-26
 */
public interface AccountService {

    public int insert(Account model);

    public int insertBatch(List<Account> modelList);

    public Account getByClientid(String clientid);
    
    public int update(Account model);

    public int updateSelective(Account model);

    public int count(Map<String, Object> params);

}
