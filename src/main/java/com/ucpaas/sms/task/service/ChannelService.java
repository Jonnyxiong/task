package com.ucpaas.sms.task.service;

import java.util.List;

import com.ucpaas.sms.task.entity.message.Channel;

/**
 * @description 通道表
 * @author 
 * @date 2017-07-27
 */
public interface ChannelService {

    public Channel getById(Integer id);
    
    public Channel getByCid(Integer cid);
    
    public List<Channel> queryList(Channel channel);
    
    public int count(Channel params);
    
}
