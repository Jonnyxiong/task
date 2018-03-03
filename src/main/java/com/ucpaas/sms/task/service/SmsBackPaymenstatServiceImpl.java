package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.entity.access.SmsBackPaymenstat;
import com.ucpaas.sms.task.mapper.access.SmsBackPaymenstatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @description 回款金额统计表
 * @author 黄文杰
 * @date 2017-07-25
 */
@Service
public class SmsBackPaymenstatServiceImpl implements SmsBackPaymenstatService{

    @Autowired
    private SmsBackPaymenstatMapper smsBackPaymenstatMapper;
    
    @Override
	@Transactional
    public int insert(SmsBackPaymenstat model) {
        return this.smsBackPaymenstatMapper.insert(model);
    }

    @Override
	@Transactional
    public int insertBatch(List<SmsBackPaymenstat> modelList) {
        return this.smsBackPaymenstatMapper.insertBatch(modelList);
    }

    @Override
	@Transactional
    public int delete(Integer id) {
		SmsBackPaymenstat model = this.smsBackPaymenstatMapper.getById(id);
		if(model != null)
        	return this.smsBackPaymenstatMapper.delete(id);
		return 0;
    }

	@Override
	@Transactional
    public int update(SmsBackPaymenstat model) {
		SmsBackPaymenstat old = this.smsBackPaymenstatMapper.getById(model.getId());
		if(old == null){
			return 0;
		}
		return this.smsBackPaymenstatMapper.update(model); 
    }

    @Override
	@Transactional
    public int updateSelective(SmsBackPaymenstat model) {
		SmsBackPaymenstat old = this.smsBackPaymenstatMapper.getById(model.getId());
		if(old != null)
        	return this.smsBackPaymenstatMapper.updateSelective(model);
		return 0;
    }

    @Override
	@Transactional
    public SmsBackPaymenstat getById(Integer id) {
        SmsBackPaymenstat model = this.smsBackPaymenstatMapper.getById(id);
		return model;
    } 

    @Override
	@Transactional
    public int count(Map<String,Object> params) {
		return this.smsBackPaymenstatMapper.count(params);
    }

    @Override
    public int deleteByDateLike(String yyyyMM) {
        return this.smsBackPaymenstatMapper.deleteByDateLike(yyyyMM);
    }
}
