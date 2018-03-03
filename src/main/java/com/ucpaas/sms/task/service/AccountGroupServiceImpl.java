package com.ucpaas.sms.task.service;


import com.ucpaas.sms.task.entity.message.AccountGroup;
import com.ucpaas.sms.task.mapper.message.AccountGroupMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @description 客户组信息管理
 * @author 黄文杰
 * @date 2017-07-27
 */
@Service
public class AccountGroupServiceImpl implements AccountGroupService{

    @Autowired
    private AccountGroupMapper accountGroupMapper;
    
    @Override
	@Transactional
    public int insert(AccountGroup model) {
        return this.accountGroupMapper.insert(model);
    }

    @Override
	@Transactional
    public int insertBatch(List<AccountGroup> modelList) {
        return this.accountGroupMapper.insertBatch(modelList);
    }


	@Override
	@Transactional
    public int update(AccountGroup model) {
		AccountGroup old = this.accountGroupMapper.getByAccountGid(model.getAccountGid());
		if(old == null){
			return 0;
		}
		return this.accountGroupMapper.update(model); 
    }

    @Override
	@Transactional
    public int updateSelective(AccountGroup model) {
		AccountGroup old = this.accountGroupMapper.getByAccountGid(model.getAccountGid());
		if(old != null)
        	return this.accountGroupMapper.updateSelective(model);
		return 0;
    }

    @Override
	@Transactional
    public AccountGroup getByAccountGid(Integer accountGid) {
        AccountGroup model = this.accountGroupMapper.getByAccountGid(accountGid);
		return model;
    }

    @Override
	@Transactional
    public int count(Map<String,Object> params) {
		return this.accountGroupMapper.count(params);
    }

}
