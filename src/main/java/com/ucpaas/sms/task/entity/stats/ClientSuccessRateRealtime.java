package com.ucpaas.sms.task.entity.stats;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.ibatis.type.Alias;

@Alias("ClientSuccessRateRealtime")
public class  ClientSuccessRateRealtime {

	//
	private Long id;
	// 用户id
	private String clientId;
	// 用户名称
	private String clientName;
	// 发送总数
	private int sendTotal;
	// 明确成功 3
	private int reallySuccessTotal;
	// 成功待定 1
	private int fakeSuccessFail;
	// 计费条数 1+3+4+6
	private int charge1;
	// 计费条数 10
	private int charge2;
	// 明确失败 6
	private int reallyFailTotal;
	// 审核不通过 7
	private int auditFailTotal;
	// 提交失败 5
	private int submitFailTotal;
	// 拦截条数 8 9 10
	private int interceptTotal;
	// 未发送 0
	private int nosend;
	// 提交SEND失败 4
	private int sendFailToatl;
	// 发送到SEND总数 1 3 6
	private int sendAll;
	// 成功率
	private BigDecimal successRate;
	// 未知率
	private BigDecimal fakeSuccessRate;
	// 失败率
	private BigDecimal reallyFailRate;
	// 数据时间
	private Date dataTime;
	// 数据采集时间
	private Date createTime;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	public int getSendTotal() {
		return sendTotal;
	}
	public void setSendTotal(int sendTotal) {
		this.sendTotal = sendTotal;
	}
	public int getReallySuccessTotal() {
		return reallySuccessTotal;
	}
	public void setReallySuccessTotal(int reallySuccessTotal) {
		this.reallySuccessTotal = reallySuccessTotal;
	}
	public int getFakeSuccessFail() {
		return fakeSuccessFail;
	}
	public void setFakeSuccessFail(int fakeSuccessFail) {
		this.fakeSuccessFail = fakeSuccessFail;
	}
	public int getCharge1() {
		return charge1;
	}
	public void setCharge1(int charge1) {
		this.charge1 = charge1;
	}
	public int getCharge2() {
		return charge2;
	}
	public void setCharge2(int charge2) {
		this.charge2 = charge2;
	}
	public int getReallyFailTotal() {
		return reallyFailTotal;
	}
	public void setReallyFailTotal(int reallyFailTotal) {
		this.reallyFailTotal = reallyFailTotal;
	}
	public int getAuditFailTotal() {
		return auditFailTotal;
	}
	public void setAuditFailTotal(int auditFailTotal) {
		this.auditFailTotal = auditFailTotal;
	}
	public int getSubmitFailTotal() {
		return submitFailTotal;
	}
	public void setSubmitFailTotal(int submitFailTotal) {
		this.submitFailTotal = submitFailTotal;
	}
	public int getInterceptTotal() {
		return interceptTotal;
	}
	public void setInterceptTotal(int interceptTotal) {
		this.interceptTotal = interceptTotal;
	}
	public int getNosend() {
		return nosend;
	}
	public void setNosend(int nosend) {
		this.nosend = nosend;
	}
	public int getSendFailToatl() {
		return sendFailToatl;
	}
	public void setSendFailToatl(int sendFailToatl) {
		this.sendFailToatl = sendFailToatl;
	}
	public int getSendAll() {
		return sendAll;
	}
	public void setSendAll(int sendAll) {
		this.sendAll = sendAll;
	}
	public BigDecimal getSuccessRate() {
		return successRate;
	}
	public void setSuccessRate(BigDecimal successRate) {
		this.successRate = successRate;
	}
	public BigDecimal getFakeSuccessRate() {
		return fakeSuccessRate;
	}
	public void setFakeSuccessRate(BigDecimal fakeSuccessRate) {
		this.fakeSuccessRate = fakeSuccessRate;
	}
	public BigDecimal getReallyFailRate() {
		return reallyFailRate;
	}
	public void setReallyFailRate(BigDecimal reallyFailRate) {
		this.reallyFailRate = reallyFailRate;
	}
	public Date getDataTime() {
		return dataTime;
	}
	public void setDataTime(Date dataTime) {
		this.dataTime = dataTime;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	 

}