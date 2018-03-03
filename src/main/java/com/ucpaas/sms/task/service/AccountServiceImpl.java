package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.entity.message.Account;
import com.ucpaas.sms.task.mapper.message.AccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author 黄文杰
 * @description 客户管理
 * @date 2017-07-26
 */
@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountMapper accountMapper;

    @Override
    @Transactional
    public int insert(Account model) {
        return this.accountMapper.insert(model);
    }

    @Override
    @Transactional
    public int insertBatch(List<Account> modelList) {
        return this.accountMapper.insertBatch(modelList);
    }


    @Override
    @Transactional
    public int update(Account model) {
        Account old = this.accountMapper.getByClientid(model.getClientid());
        if (old == null) {
            return 0;
        }
        return this.accountMapper.update(model);
    }

    @Override
    @Transactional
    public int updateSelective(Account model) {
        Account old = this.accountMapper.getByClientid(model.getClientid());
        if (old != null)
            return this.accountMapper.updateSelective(model);
        return 0;
    }

    @Override
    @Transactional
    public int count(Map<String, Object> params) {
        return this.accountMapper.count(params);
    }

    @Override
    public Account getByClientid(String clientid) {
        return accountMapper.getByClientid(clientid);
    }
}
