package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.entity.access.SmsBackPaymenstat;

import java.util.List;
import java.util.Map;

/**
 * @description 回款金额统计表
 * @author 黄文杰
 * @date 2017-07-25
 */
public interface SmsBackPaymenstatService {

    public int insert(SmsBackPaymenstat model);
    
    public int insertBatch(List<SmsBackPaymenstat> modelList);
    
    public int delete(Integer id);
    
    public int update(SmsBackPaymenstat model);
    
    public int updateSelective(SmsBackPaymenstat model);
    
    public SmsBackPaymenstat getById(Integer id);
    
    
    public int count(Map<String,Object> params);

    int deleteByDateLike(String yyyyMM);
}
