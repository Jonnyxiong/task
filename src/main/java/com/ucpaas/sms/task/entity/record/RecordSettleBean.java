package com.ucpaas.sms.task.entity.record;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.ibatis.type.Alias;

@Alias("RecordSettleBean")
public class RecordSettleBean {
    // 通道号
    private Integer channelid;
    // 运营商类型
    private Integer operatorstype;

    // 发送总数
    private int sendtotal;
    // 0 待发送
    private int readyToSend;
    //1+2 未知
    private int unknow;
    
    // 成功条数/计费条数
    private int success;
    
    //5+6 失败条数
    private int fail;

	public Integer getChannelid() {
		return channelid;
	}

	public void setChannelid(Integer channelid) {
		this.channelid = channelid;
	}

	public Integer getOperatorstype() {
		return operatorstype;
	}

	public void setOperatorstype(Integer operatorstype) {
		this.operatorstype = operatorstype;
	}

	public int getSendtotal() {
		return sendtotal;
	}

	public void setSendtotal(int sendtotal) {
		this.sendtotal = sendtotal;
	}

	public int getReadyToSend() {
		return readyToSend;
	}

	public void setReadyToSend(int readyToSend) {
		this.readyToSend = readyToSend;
	}

	public int getUnknow() {
		return unknow;
	}

	public void setUnknow(int unknow) {
		this.unknow = unknow;
	}

	public int getSuccess() {
		return success;
	}

	public void setSuccess(int success) {
		this.success = success;
	}

	public int getFail() {
		return fail;
	}

	public void setFail(int fail) {
		this.fail = fail;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("channelid", channelid)
				.append("operatorstype", operatorstype)
				.append("sendtotal", sendtotal)
				.append("readyToSend", readyToSend)
				.append("unknow", unknow)
				.append("success", success)
				.append("fail", fail)
				.toString();
	}
}
