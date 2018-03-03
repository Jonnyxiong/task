package com.ucpaas.sms.task.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ucpaas.sms.task.mapper.access.JsmsClientOperationStatisticsMapper;
import com.ucpaas.sms.task.entity.access.JsmsClientOperationStatistics;


import com.jsmsframework.common.dto.JsmsPage;

/**
 * @description 通道属性
 * @author huangwenjie
 * @date 2018-01-13
 */
@Service
public class JsmsClientOperationStatisticsServiceImpl implements JsmsClientOperationStatisticsService{

    @Autowired
    private JsmsClientOperationStatisticsMapper clientOperationStatisticsMapper;
    
    @Override
    public int insert(JsmsClientOperationStatistics model) {
        return this.clientOperationStatisticsMapper.insert(model);
    }
    @Override
    @Transactional(value = "access")
    public int insertOrUpdateUnique(JsmsClientOperationStatistics model) {
        if(isExistByUnique(model)){
            return this.clientOperationStatisticsMapper.updateSelectiveUnique(model);
        }else {
            return this.clientOperationStatisticsMapper.insert(model);

        }
    }
    @Override
    public Boolean isExistByUnique(JsmsClientOperationStatistics model) {
        Map params=new HashMap();
        params.put("clientId",model.getClientId());
        params.put("smstype",model.getSmstype());
        params.put("operatorstype",model.getOperatorstype());
        params.put("date",model.getDate());
        List<JsmsClientOperationStatistics> list=findList(params);
        if(CollectionUtils.isEmpty(list)){
            return false;
        }else {
            return true;
        }


    }

    @Override
    public int insertBatch(List<JsmsClientOperationStatistics> modelList) {
        return this.clientOperationStatisticsMapper.insertBatch(modelList);
    }

	@Override
    public int update(JsmsClientOperationStatistics model) {
		JsmsClientOperationStatistics old = this.clientOperationStatisticsMapper.getById(model.getId());
		if(old == null){
			return 0;
		}
		return this.clientOperationStatisticsMapper.update(model); 
    }

    @Override
    public int updateSelective(JsmsClientOperationStatistics model) {
		JsmsClientOperationStatistics old = this.clientOperationStatisticsMapper.getById(model.getId());
		if(old != null)
        	return this.clientOperationStatisticsMapper.updateSelective(model);
		return 0;
    }

    @Override
    public JsmsClientOperationStatistics getById(Integer id) {
        JsmsClientOperationStatistics model = this.clientOperationStatisticsMapper.getById(id);
		return model;
    }

    @Override
    public JsmsPage queryList(JsmsPage page) {
        List<JsmsClientOperationStatistics> list = this.clientOperationStatisticsMapper.queryList(page);
        page.setData(list);
        return page;
    }

    @Override
    @Transactional(value = "access")
    public List<JsmsClientOperationStatistics> findList(Map params) {
        List<JsmsClientOperationStatistics> list = this.clientOperationStatisticsMapper.findList(params);
        return list;
    }

    @Override
    public int count(Map<String,Object> params) {
		return this.clientOperationStatisticsMapper.count(params);
    }
    @Override
    public BigDecimal getSalefee(Map params){
        return this.clientOperationStatisticsMapper.getSalefee(params);
    }
    
}
