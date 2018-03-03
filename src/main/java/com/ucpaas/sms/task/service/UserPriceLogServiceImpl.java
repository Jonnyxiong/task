package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.entity.message.UserPriceLog;
import com.ucpaas.sms.task.mapper.message.UserPriceLogMapper;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description 后付费用户价格变更记录管理
 * @author 黄文杰
 * @date 2017-07-27
 */
@Service
public class UserPriceLogServiceImpl implements UserPriceLogService{

    @Autowired
    private UserPriceLogMapper userPriceLogMapper;
    
    @Override
	@Transactional
    public int insert(UserPriceLog model) {
        return this.userPriceLogMapper.insert(model);
    }

    @Override
	@Transactional
    public int insertBatch(List<UserPriceLog> modelList) {
        return this.userPriceLogMapper.insertBatch(modelList);
    }


	@Override
	@Transactional
    public int update(UserPriceLog model) {
		UserPriceLog old = this.userPriceLogMapper.getById(model.getId());
		if(old == null){
			return 0;
		}
		return this.userPriceLogMapper.update(model); 
    }

    @Override
	@Transactional
    public int updateSelective(UserPriceLog model) {
		UserPriceLog old = this.userPriceLogMapper.getById(model.getId());
		if(old != null)
        	return this.userPriceLogMapper.updateSelective(model);
		return 0;
    }

    @Override
	@Transactional
    public UserPriceLog getById(Integer id) {
        UserPriceLog model = this.userPriceLogMapper.getById(id);
		return model;
    }


    @Override
	@Transactional
    public int count(Map<String,Object> params) {
		return this.userPriceLogMapper.count(params);
    }

    @Override
    public UserPriceLog getByPrice(String clientid, Integer smstype, DateTime statDay) {
        Map params = new HashMap() ;
        params.put("clientid",clientid);
        params.put("smstype",smstype);
        List<UserPriceLog> userPriceLogs =  this.userPriceLogMapper.queryAll(params);
        if(userPriceLogs==null||userPriceLogs.isEmpty())
            return null;
        UserPriceLog result = null;
        int dayOfYear = 0;
        for (UserPriceLog upl : userPriceLogs) {
            DateTime  effectDate = new DateTime(upl.getEffectDate());
            if(effectDate.isAfter(statDay)){
                continue;
            }
            if(effectDate.getDayOfYear()==statDay.getDayOfYear()){
                result = upl;
                break;
            }
            if(effectDate.getDayOfYear()>dayOfYear){
                dayOfYear = effectDate.getDayOfYear();
                result = upl;
            }
        }

        return result;
    }
}
