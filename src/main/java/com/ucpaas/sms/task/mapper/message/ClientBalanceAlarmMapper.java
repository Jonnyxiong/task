package com.ucpaas.sms.task.mapper.message;


import com.ucpaas.sms.task.entity.message.ClientBalanceAlarm;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ClientBalanceAlarmMapper{

	int insert(ClientBalanceAlarm clientBalanceAlarm);
	
	int insertBatch(List<ClientBalanceAlarm> clientBalanceAlarmList);
	
	int delete(int id);
	
	int update(ClientBalanceAlarm clientBalanceAlarm);

	int updateSelective(ClientBalanceAlarm clientBalanceAlarm);
	
	ClientBalanceAlarm getById(int id);
	
	int count(ClientBalanceAlarm clientBalanceAlarm);

	int updateReminderNum(Map params);

	int reduceReminderNum(String clientId);

	int reduceReminderNumOem(@Param("clientId") String clientId, @Param("alarmType") String alarmType);

}