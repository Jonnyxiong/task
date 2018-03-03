package com.ucpaas.sms.task.service;

import java.util.List;
import java.util.Map;

import com.ucpaas.sms.task.entity.access.CustomerStatTemp;
import com.ucpaas.sms.task.model.ResultVO;

public interface CustomerStatTempService {

    public ResultVO insert(CustomerStatTemp model);
    
    public ResultVO insertBatch(List<CustomerStatTemp> modelList);
    
    public ResultVO delete(Long id);
    
    public ResultVO update(CustomerStatTemp model);
    
    public ResultVO updateSelective(CustomerStatTemp model);
    
    public ResultVO getById(Long id);
    
    
    public ResultVO count(Map<String,Object> params);

    /**
     * 根据时间段，遍历所有的access表，生成CustomerStatTemp临时数据
     * @param statTime
     * @return
     */
	public List<CustomerStatTemp> generateData(String statTime);
	
	
	/** 
	 * @Title: generateDataForCloud 
	 * @Description: 根据时间段，遍历所有的access表，生成CustomerStatTemp临时数据
	 * @param statTime
	 * @return
	 * @return: List<CustomerStatTemp>
	 */
	public List<CustomerStatTemp> generateDataForCloud(String statTime);

    /**
     * 根据时间段，遍历所有的access表，生成CustomerStatTemp临时数据，加多代理商id分组
     * @param statTime
     * @return
     */
    List<CustomerStatTemp> generateDataIncludeAgentId(String statTime);
}
