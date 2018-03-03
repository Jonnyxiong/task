package com.ucpaas.sms.task.model.halfhour;

import com.ucpaas.sms.task.entity.stats.ChannelSuccessRateByClientid;

public class RecordByClientVo extends ChannelSuccessRateByClientid {
	// 接收access 1 2 3 4 5 6
	private int receiveTotal;
	private int beSendTotal;

	public int getBeSendTotal() {
		return beSendTotal;
	}

	public void setBeSendTotal(int beSendTotal) {
		this.beSendTotal = beSendTotal;
	}

	public int getReceiveTotal() {
		return receiveTotal;
	}

	public void setReceiveTotal(int receiveTotal) {
		this.receiveTotal = receiveTotal;
	}

}
