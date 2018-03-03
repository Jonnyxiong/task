package com.ucpaas.sms.task.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ucpaas.sms.task.mapper.record.JsmsChannelOperationStatisticsMapper;
import com.ucpaas.sms.task.entity.record.JsmsChannelOperationStatistics;


import com.jsmsframework.common.dto.JsmsPage;

/**
 * @description 通道属性
 * @author huangwenjie
 * @date 2018-01-13
 */
@Service
public class JsmsChannelOperationStatisticsServiceImpl implements JsmsChannelOperationStatisticsService{

    @Autowired
    private JsmsChannelOperationStatisticsMapper channelOperationStatisticsMapper;
    
    @Override
    public int insert(JsmsChannelOperationStatistics model) {
        return this.channelOperationStatisticsMapper.insert(model);
    }
    @Override
    public int insertOrUpdateByDate(JsmsChannelOperationStatistics model) {
        if(isExistByDate(model)){
            return this.channelOperationStatisticsMapper.updateSelectiveByDate(model);
        }else {
            return this.channelOperationStatisticsMapper.insert(model);
        }
    }
    @Override
    public Boolean isExistByDate(JsmsChannelOperationStatistics model) {
        Map params=new HashMap();
        params.put("channelId",model.getChannelId());
        params.put("date",model.getDate());
        List<JsmsChannelOperationStatistics> list=findList(params);
        if(CollectionUtils.isEmpty(list)){
            return false;
        }else {
            return true;
        }
    }

    @Override
    public int insertBatch(List<JsmsChannelOperationStatistics> modelList) {
        return this.channelOperationStatisticsMapper.insertBatch(modelList);
    }

	@Override
    public int update(JsmsChannelOperationStatistics model) {
		JsmsChannelOperationStatistics old = this.channelOperationStatisticsMapper.getById(model.getId());
		if(old == null){
			return 0;
		}
		return this.channelOperationStatisticsMapper.update(model); 
    }

    @Override
    public int updateSelective(JsmsChannelOperationStatistics model) {
		JsmsChannelOperationStatistics old = this.channelOperationStatisticsMapper.getById(model.getId());
		if(old != null)
        	return this.channelOperationStatisticsMapper.updateSelective(model);
		return 0;
    }

    @Override
    public JsmsChannelOperationStatistics getById(Integer id) {
        JsmsChannelOperationStatistics model = this.channelOperationStatisticsMapper.getById(id);
		return model;
    }

    @Override
    public JsmsPage queryList(JsmsPage page) {
        List<JsmsChannelOperationStatistics> list = this.channelOperationStatisticsMapper.queryList(page);
        page.setData(list);
        return page;
    }

    @Override
    public List<JsmsChannelOperationStatistics> findList(Map params) {
        List<JsmsChannelOperationStatistics> list = this.channelOperationStatisticsMapper.findList(params);
        return list;
    }

    @Override
    public int count(Map<String,Object> params) {
		return this.channelOperationStatisticsMapper.count(params);
    }
    @Override
    public BigDecimal getCostfee(Map params){
        return this.channelOperationStatisticsMapper.getCostfee(params);
    }
}
