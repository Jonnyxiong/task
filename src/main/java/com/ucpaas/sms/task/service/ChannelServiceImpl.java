package com.ucpaas.sms.task.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ucpaas.sms.task.entity.message.Channel;
import com.ucpaas.sms.task.mapper.message.ChannelMapper;

/**
 * @description 通道表
 * @author 
 * @date 2017-07-27
 */
@Service
public class ChannelServiceImpl implements ChannelService{

    @Autowired
    private ChannelMapper channelMapper;


    @Override
	@Transactional
    public Channel getById(Integer id) {
        Channel model = this.channelMapper.getById(id);
		return model;
    }

    @Override
	@Transactional
    public List<Channel> queryList(Channel channel) {
        List<Channel> list = this.channelMapper.queryList(channel);
        return list;
    }

    @Override
	@Transactional
    public int count(Channel channel) {
		return this.channelMapper.count(channel);
    }

	@Override
	public Channel getByCid(Integer cid) {
		if(cid != null)
			return this.channelMapper.getByCid(cid);
		else
			return null;
	}
    
}
