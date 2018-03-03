package com.ucpaas.sms.task.mapper.message;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ucpaas.sms.task.entity.message.Param;

@Repository
public interface ParamMapper{

	int insert(Param model);
	
	int insertBatch(List<Param> modelList);
	
	int delete(Long paramId);
	
	int update(Param model);
	
	int updateSelective(Param model);
	
	Param getByParamId(Long paramId);
	
	
	int count(Map<String,Object> params);

	Param getByParamKey(String paramKey);

}