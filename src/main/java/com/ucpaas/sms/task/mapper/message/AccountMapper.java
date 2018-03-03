package com.ucpaas.sms.task.mapper.message;


import com.ucpaas.sms.task.entity.message.Account;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AccountMapper{

	int insert(Account account);
	
	int insertBatch(List<Account> accountList);

	
	int update(Account account);
	
	int updateSelective(Account account);

	
	List<Account> queryList(Map params);
	
	int count(Map params);

	Account getByClientid(String clientid);

	String getSMSTypeByClientid(String clientid);

	List<Account> findClientCompanyAndBelongSale();
}