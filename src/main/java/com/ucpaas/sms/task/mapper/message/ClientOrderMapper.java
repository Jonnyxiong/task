package com.ucpaas.sms.task.mapper.message;


import com.ucpaas.sms.task.entity.message.ClientOrder;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface ClientOrderMapper{

	int insert(ClientOrder clientOrder);
	
	int insertBatch(List<ClientOrder> clientOrderList);
	
	int delete(Long id);
	
	int update(ClientOrder clientOrder);
	
	int updateSelective(ClientOrder clientOrder);
	
	ClientOrder getById(Long id);

	List<ClientOrder> queryList(Map params);
	
	int count(ClientOrder clientOrder);

	List<String> getNewBuyer(Date checkTime);

	List<Map> getClientAlarmInfo();

}