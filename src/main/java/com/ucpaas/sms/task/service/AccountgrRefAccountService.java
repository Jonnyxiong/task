package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.entity.message.AccountgrRefAccount;

import java.util.List;
import java.util.Map;
/**
 * @description 客户组客户关联管理
 * @author 黄文杰
 * @date 2017-07-27
 */
public interface AccountgrRefAccountService {

    public int insert(AccountgrRefAccount model);
    
    public int insertBatch(List<AccountgrRefAccount> modelList);

    public int update(AccountgrRefAccount model);
    
    public int updateSelective(AccountgrRefAccount model);
    
    public AccountgrRefAccount getByAccountGid(Integer accountGid);

    
    public int count(Map<String, Object> params);

    AccountgrRefAccount getByClientid(String clientid);
}
