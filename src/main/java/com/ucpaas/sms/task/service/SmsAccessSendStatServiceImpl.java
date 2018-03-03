package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.entity.access.SmsAccessSendStat;
import com.ucpaas.sms.task.mapper.access.SmsAccessSendStatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @description 客户发送量表
 * @author 黄文杰
 * @date 2017-07-25
 */
@Service
public class SmsAccessSendStatServiceImpl implements SmsAccessSendStatService{

    @Autowired
    private SmsAccessSendStatMapper smsAccessSendStatMapper;
    
    @Override
	@Transactional
    public int insert(SmsAccessSendStat model) {
        return this.smsAccessSendStatMapper.insert(model);
    }

    @Override
	@Transactional
    public int insertBatch(List<SmsAccessSendStat> modelList) {
        return this.smsAccessSendStatMapper.insertBatch(modelList);
    }

    @Override
	@Transactional
    public int delete(Integer id) {
		SmsAccessSendStat model = this.smsAccessSendStatMapper.getById(id);
		if(model != null)
        	return this.smsAccessSendStatMapper.delete(id);
		return 0;
    }

	@Override
	@Transactional
    public int update(SmsAccessSendStat model) {
		SmsAccessSendStat old = this.smsAccessSendStatMapper.getById(model.getId());
		if(old == null){
			return 0;
		}
		return this.smsAccessSendStatMapper.update(model); 
    }

    @Override
	@Transactional
    public int updateSelective(SmsAccessSendStat model) {
        return this.smsAccessSendStatMapper.updateSelective(model);
    }

    @Override
	@Transactional
    public SmsAccessSendStat getById(Integer id) {
        SmsAccessSendStat model = this.smsAccessSendStatMapper.getById(id);
		return model;
    }
 
    @Override
	@Transactional
    public int count(Map<String,Object> params) {
		return this.smsAccessSendStatMapper.count(params);
    }

    @Override
    public int deleteByDate(Integer date){
        	return this.smsAccessSendStatMapper.deleteByDate(date);
    }

    @Override
    public List<SmsAccessSendStat> queryAll(Map params) {
        return this.smsAccessSendStatMapper.queryAll(params);
    }

    @Override
    public int deleteByDateLike(String yyyyMM) {
        return this.smsAccessSendStatMapper.deleteByDateLike(yyyyMM);
    }

    @Override
    public List<SmsAccessSendStat> queryByDateLike(String yyyyMM) {
        return this.smsAccessSendStatMapper.queryByDateLike(yyyyMM);
    }
}
