package com.ucpaas.sms.task.mapper.message;


import com.ucpaas.sms.task.entity.message.OemClientPool;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface OemClientPoolMapper{

	int insert(OemClientPool oemClientPool);
	
	int insertBatch(List<OemClientPool> oemClientPoolList);
	
	int delete(Long id);
	
	int update(OemClientPool oemClientPool);
	
	int updateSelective(OemClientPool oemClientPool);
	
	OemClientPool getById(Long id);

	OemClientPool getByParams(Map params);

	List<OemClientPool> queryList(Map params);

	int count(OemClientPool oemClientPool);

}