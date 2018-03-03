package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.entity.access.CustomerStatTemp;
import com.ucpaas.sms.task.entity.message.Param;
import com.ucpaas.sms.task.mapper.access.CustomerStatTempMapper;
import com.ucpaas.sms.task.mapper.message.ParamMapper;
import com.ucpaas.sms.task.model.ResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @title
 * @description 用户运营、运维统计临时表
 * @author huangwenjie
 * @date 2017-02-21
 */
@Service
public class CustomerStatTempServiceImpl implements CustomerStatTempService {

	private static Logger logger = LoggerFactory.getLogger(CustomerStatTempServiceImpl.class);
	@Autowired
	private CustomerStatTempMapper customerStatTempMapper;
	@Autowired
	private ParamMapper paramMapper;

	@Override
	@Transactional(value= "access")
	public ResultVO insert(CustomerStatTemp model) {
		this.customerStatTempMapper.insert(model);
		return ResultVO.successDefault();
	}

	@Override
	@Transactional(value= "access")
	public ResultVO insertBatch(List<CustomerStatTemp> modelList) {
		this.customerStatTempMapper.insertBatch(modelList);
		return ResultVO.successDefault();
	}

	@Override
	@Transactional(value= "access")
	public ResultVO delete(Long id) {
		CustomerStatTemp model = this.customerStatTempMapper.getById(id);
		if (model != null)
			this.customerStatTempMapper.delete(id);
		return ResultVO.successDefault();
	}

	@Override
	@Transactional(value= "access")
	public ResultVO update(CustomerStatTemp model) {
		CustomerStatTemp old = this.customerStatTempMapper.getById(model.getId());
		if (old == null) {
			return ResultVO.failure();
		}
		this.customerStatTempMapper.update(model);
		CustomerStatTemp newModel = this.customerStatTempMapper.getById(model.getId());
		return ResultVO.successDefault(newModel);
	}

	@Override
	@Transactional(value= "access")
	public ResultVO updateSelective(CustomerStatTemp model) {
		CustomerStatTemp old = this.customerStatTempMapper.getById(model.getId());
		if (old != null)
			this.customerStatTempMapper.updateSelective(model);
		return ResultVO.successDefault();
	}

	@Override
	@Transactional(value= "access")
	public ResultVO getById(Long id) {
		CustomerStatTemp model = this.customerStatTempMapper.getById(id);
		return ResultVO.successDefault(model);
	}

	@Override
	@Transactional(value= "access")
	public ResultVO count(Map<String, Object> params) {
		return ResultVO.successDefault(this.customerStatTempMapper.count(params));
	}

	@Override
	@Transactional(value= "access")
	public List<CustomerStatTemp> generateData(String statTime) {
		Param param = paramMapper.getByParamKey("MAX_ACCESS_IDENTIFY");
		int max_identify = 0;
		try {
			max_identify = Integer.valueOf(param.getParamValue());
		} catch (Exception e) {
			logger.error("统计access库失败, 查询系统参数MAX_ACCESS_IDENTIFY为空",e);
			throw e;
		}
		
		List<CustomerStatTemp> customerStatTemps = new ArrayList<>();
		for(int i=0;i<=max_identify;i++){
			Map params = new HashMap<>();
			params.put("identify", i);
			params.put("statTime", statTime);
			customerStatTemps.addAll(customerStatTempMapper.generateDataFromAccess(params));
		}
		return customerStatTemps;
	}

	@Override
	@Transactional(value= "access")
	public List<CustomerStatTemp> generateDataIncludeAgentId(String statTime) {
		Param param = paramMapper.getByParamKey("MAX_ACCESS_IDENTIFY");
		int max_identify = 0;
		try {
			max_identify = Integer.valueOf(param.getParamValue());
		} catch (Exception e) {
			logger.error("统计access库失败, 查询系统参数MAX_ACCESS_IDENTIFY为空",e);
			throw e;
		}

		List<CustomerStatTemp> customerStatTemps = new ArrayList<>();
		for(int i=0;i<=max_identify;i++){
			Map params = new HashMap<>();
			params.put("identify", i);
			params.put("statTime", statTime);
			customerStatTemps.addAll(customerStatTempMapper.generateDataIncludeAgentId(params));
		}
		return customerStatTemps;
	}

	@Override
	public List<CustomerStatTemp> generateDataForCloud(String statTime) {
		
		Param param = paramMapper.getByParamKey("MAX_ACCESS_IDENTIFY");
		int max_identify = 0;
		try {
			max_identify = Integer.valueOf(param.getParamValue());
		} catch (Exception e) {
			logger.error("统计access库失败, 查询系统参数MAX_ACCESS_IDENTIFY为空",e);
			throw e;
		}
		
		List<CustomerStatTemp> customerStatTemps = new ArrayList<>();
		for(int i=0;i<=max_identify;i++){
			Map<String,Object> params = new HashMap<>();
			params.put("identify", i);
			params.put("statTime", statTime);
			customerStatTemps.addAll(customerStatTempMapper.generateDataFromAccessForCloud(params));
		}
		return customerStatTemps;
	}
	
	
	
	

}
