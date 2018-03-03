package com.ucpaas.sms.task.service;


import com.ucpaas.sms.task.entity.message.AccountgrRefAccount;
import com.ucpaas.sms.task.mapper.message.AccountgrRefAccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @description 客户组客户关联管理
 * @author 黄文杰
 * @date 2017-07-27
 */
@Service
public class AccountgrRefAccountServiceImpl implements AccountgrRefAccountService{

    @Autowired
    private AccountgrRefAccountMapper accountgrRefAccountMapper;
    
    @Override
	@Transactional
    public int insert(AccountgrRefAccount model) {
        return this.accountgrRefAccountMapper.insert(model);
    }

    @Override
	@Transactional
    public int insertBatch(List<AccountgrRefAccount> modelList) {
        return this.accountgrRefAccountMapper.insertBatch(modelList);
    }

	@Override
	@Transactional
    public int update(AccountgrRefAccount model) {
		AccountgrRefAccount old = this.accountgrRefAccountMapper.getByAccountGid(model.getAccountGid());
		if(old == null){
			return 0;
		}
		return this.accountgrRefAccountMapper.update(model); 
    }

    @Override
	@Transactional
    public int updateSelective(AccountgrRefAccount model) {
		AccountgrRefAccount old = this.accountgrRefAccountMapper.getByAccountGid(model.getAccountGid());
		if(old != null)
        	return this.accountgrRefAccountMapper.updateSelective(model);
		return 0;
    }

    @Override
	@Transactional
    public AccountgrRefAccount getByAccountGid(Integer accountGid) {
        AccountgrRefAccount model = this.accountgrRefAccountMapper.getByAccountGid(accountGid);
		return model;
    }


    @Override
	@Transactional
    public int count(Map<String,Object> params) {
		return this.accountgrRefAccountMapper.count(params);
    }

    @Override
    public AccountgrRefAccount getByClientid(String clientid) {
        return this.accountgrRefAccountMapper.getByClientid(clientid);
    }
}
