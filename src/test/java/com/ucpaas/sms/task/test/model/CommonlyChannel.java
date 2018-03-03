package com.ucpaas.sms.task.test.model;

import java.util.ArrayList;
import java.util.List;

public class CommonlyChannel {

	// 运营商类型
	private String channeloperatorstype;
	// 通道数量
	private Integer channelSize;

	List<CommonlyChannelDetail> list = new ArrayList<>();

	public CommonlyChannel(String channeloperatorstype, List<CommonlyChannelDetail> list) {
		super();
		this.channeloperatorstype = channeloperatorstype;
		this.channelSize = list.size();
		this.list = list;
	}

	public String getChanneloperatorstype() {
		return channeloperatorstype;
	}

	public void setChanneloperatorstype(String channeloperatorstype) {
		this.channeloperatorstype = channeloperatorstype;
	}

	public Integer getChannelSize() {
		if (channelSize == null)
			channelSize = list.size();
		return channelSize;
	}

	public void setChannelSize(Integer channelSize) {
		this.channelSize = channelSize;
	}

	public List<CommonlyChannelDetail> getList() {
		return list;
	}

	public void setList(List<CommonlyChannelDetail> list) {
		this.list = list;
	}

}
