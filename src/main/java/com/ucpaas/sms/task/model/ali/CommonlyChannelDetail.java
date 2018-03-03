package com.ucpaas.sms.task.model.ali;

public class CommonlyChannelDetail {

	// 通道号
	private String channelid;
	// 通道备注
	private String channelremark;
	// 通道成本价
	private String costfee;
	// 发送量(1+2+3)
	private Integer sendtotal;
	// 成功率(3/总发送量)
	private String successrate;

	// (通道计费数-用户侧3)/通道计费数
	private String inOutRate;

	public CommonlyChannelDetail(String channelid, String channelremark, String costfee,Integer sendtotal, String successrate,
			String inOutRate) {
		super();
		this.channelid = channelid;
		this.channelremark = channelremark;
		this.sendtotal = sendtotal;
		this.costfee = costfee;
		this.successrate = successrate;
		this.inOutRate = inOutRate;
	}

	public String getChannelid() {
		return channelid;
	}

	public void setChannelid(String channelid) {
		this.channelid = channelid;
	}

	public String getChannelremark() {
		return channelremark;
	}

	public void setChannelremark(String channelremark) {
		this.channelremark = channelremark;
	}

	public String getCostfee() {
		return costfee;
	}

	public void setCostfee(String costfee) {
		this.costfee = costfee;
	}

	public String getSuccessrate() {
		return successrate;
	}

	public void setSuccessrate(String successrate) {
		this.successrate = successrate;
	}

	public String getInOutRate() {
		return inOutRate;
	}

	public void setInOutRate(String inOutRate) {
		this.inOutRate = inOutRate;
	}

	public Integer getSendtotal() {
		return sendtotal;
	}

	public void setSendtotal(Integer sendtotal) {
		this.sendtotal = sendtotal;
	}

	@Override
	public String toString() {
		return "CommonlyChannelDetail{" +
				"channelid='" + channelid + '\'' +
				", channelremark='" + channelremark + '\'' +
				", costfee='" + costfee + '\'' +
				", sendtotal=" + sendtotal +
				", successrate='" + successrate + '\'' +
				", inOutRate='" + inOutRate + '\'' +
				'}';
	}
}
