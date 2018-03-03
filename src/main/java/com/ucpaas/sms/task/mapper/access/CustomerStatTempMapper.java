package com.ucpaas.sms.task.mapper.access;

import com.ucpaas.sms.task.entity.access.CustomerStatTemp;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Repository
public interface CustomerStatTempMapper{

	int insert(CustomerStatTemp model);
	
	int insertBatch(List<CustomerStatTemp> modelList);
	
	int delete(Long id);
	
	int update(CustomerStatTemp model);
	
	int updateSelective(CustomerStatTemp model);
	
	CustomerStatTemp getById(Long id);
	
	
	int count(Map<String,Object> params);

	List<CustomerStatTemp> generateDataFromAccess(Map params);
	
	List<CustomerStatTemp> generateDataFromAccessForCloud(Map<String,Object> params);

    Collection<CustomerStatTemp> generateDataIncludeAgentId(Map params);
}