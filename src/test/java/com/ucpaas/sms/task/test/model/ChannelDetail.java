package com.ucpaas.sms.task.test.model;

import java.math.BigDecimal;

import static java.math.BigDecimal.ROUND_HALF_UP;

public class ChannelDetail {
	// 时间
	private String date;
	// 用户ID
	private String clientid;
	// 用户名称
	private String username;
	// 运营商类型
	private String channeloperatorstype;
	// 通道号
	private String channelid;
	// 通道备注
	private String channelremark;
	// 用户短信总量0+1+3+4+5+6+7+8+9+10
	private Long usersmstotal;
	// 总发送量1+3+6
	private Long sendtotal;
	// 成功率(3/总发送量)
	private BigDecimal successrate;
	// 成功量(1+3)
	private Long successtotal;
	// 未发送(0)
	private Long notsend;
	// 提交成功(1)
	private Long submitsuccess;
	// 明确成功(3)
	private Long reportsuccess;
	// 提交失败(4)
	private Long submitfail;
	// 发送失败(5)
	private Long subretfail;
	// 明确失败(6)
	private Long reportfail;
	// 审核不通过(7)
	private Long auditfail;
	// 网关接收拦截(8)
	private Long recvintercept;
	// 网关发送拦截(9)
	private Long sendintercept;
	// 超频拦截(10)
	private Long overrateintercept;

	// 销售成本价
	private BigDecimal salefee;

	// 销售收入
	private BigDecimal saleIncome;
	// 通道单价
	private BigDecimal costfee;
	// 通道计费条数
	private Long smscntTotal;
	// 通道成本
	private BigDecimal costOutcome;
	// (通道计费数-用户侧3)/通道计费数
	private BigDecimal inOutRate;
	// 毛利
	private BigDecimal profit;
	

	private String profitStr;

	public ChannelDetail(String date, String clientid, String username, String channeloperatorstype, String channelid,
			String channelremark, Long usersmstotal, Long sendtotal, BigDecimal successrate, Long successtotal,
			Long notsend, Long submitsuccess, Long reportsuccess, Long submitfail, Long subretfail, Long reportfail,
			Long auditfail, Long recvintercept, Long sendintercept, Long overrateintercept, BigDecimal salefee,
			BigDecimal saleIncome, BigDecimal costfee, Long smscntTotal, BigDecimal costOutcome, BigDecimal inOutRate,
			BigDecimal profit) {
		super();
		this.date = date;
		this.clientid = clientid;
		this.username = username;
		this.channeloperatorstype = channeloperatorstype;
		this.channelid = channelid;
		this.channelremark = channelremark;
		this.usersmstotal = usersmstotal;
		this.sendtotal = sendtotal;
		this.successrate = successrate;
		this.successtotal = successtotal;
		this.notsend = notsend;
		this.submitsuccess = submitsuccess;
		this.reportsuccess = reportsuccess;
		this.submitfail = submitfail;
		this.subretfail = subretfail;
		this.reportfail = reportfail;
		this.auditfail = auditfail;
		this.recvintercept = recvintercept;
		this.sendintercept = sendintercept;
		this.overrateintercept = overrateintercept;
		this.salefee = salefee;
		this.saleIncome = saleIncome;
		this.costfee = costfee;
		this.smscntTotal = smscntTotal;
		this.costOutcome = costOutcome;
		this.inOutRate = inOutRate;
		this.profit = profit;
		

		if(BigDecimal.ZERO.compareTo(profit.setScale(1,ROUND_HALF_UP))>0){//负数标志位红色
			this.profitStr = "<font color=\"#FF0000\">"+profit.toString()+"</font>";
		}else{
			this.profitStr = profit.toString();
		}
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getClientid() {
		return clientid;
	}
	public void setClientid(String clientid) {
		this.clientid = clientid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getChanneloperatorstype() {
		return channeloperatorstype;
	}
	public void setChanneloperatorstype(String channeloperatorstype) {
		this.channeloperatorstype = channeloperatorstype;
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
	public Long getUsersmstotal() {
		return usersmstotal;
	}
	public void setUsersmstotal(Long usersmstotal) {
		this.usersmstotal = usersmstotal;
	}
	public Long getSendtotal() {
		return sendtotal;
	}
	public void setSendtotal(Long sendtotal) {
		this.sendtotal = sendtotal;
	}
	public BigDecimal getSuccessrate() {
		return successrate;
	}
	public void setSuccessrate(BigDecimal successrate) {
		this.successrate = successrate;
	}
	public Long getSuccesstotal() {
		return successtotal;
	}
	public void setSuccesstotal(Long successtotal) {
		this.successtotal = successtotal;
	}
	public Long getNotsend() {
		return notsend;
	}
	public void setNotsend(Long notsend) {
		this.notsend = notsend;
	}
	public Long getSubmitsuccess() {
		return submitsuccess;
	}
	public void setSubmitsuccess(Long submitsuccess) {
		this.submitsuccess = submitsuccess;
	}
	public Long getReportsuccess() {
		return reportsuccess;
	}
	public void setReportsuccess(Long reportsuccess) {
		this.reportsuccess = reportsuccess;
	}
	public Long getSubmitfail() {
		return submitfail;
	}
	public void setSubmitfail(Long submitfail) {
		this.submitfail = submitfail;
	}
	public Long getSubretfail() {
		return subretfail;
	}
	public void setSubretfail(Long subretfail) {
		this.subretfail = subretfail;
	}
	public Long getReportfail() {
		return reportfail;
	}
	public void setReportfail(Long reportfail) {
		this.reportfail = reportfail;
	}
	public Long getAuditfail() {
		return auditfail;
	}
	public void setAuditfail(Long auditfail) {
		this.auditfail = auditfail;
	}
	public Long getRecvintercept() {
		return recvintercept;
	}
	public void setRecvintercept(Long recvintercept) {
		this.recvintercept = recvintercept;
	}
	public Long getSendintercept() {
		return sendintercept;
	}
	public void setSendintercept(Long sendintercept) {
		this.sendintercept = sendintercept;
	}
	public Long getOverrateintercept() {
		return overrateintercept;
	}
	public void setOverrateintercept(Long overrateintercept) {
		this.overrateintercept = overrateintercept;
	}
	public BigDecimal getSalefee() {
		return salefee;
	}
	public void setSalefee(BigDecimal salefee) {
		this.salefee = salefee;
	}
	public BigDecimal getSaleIncome() {
		return saleIncome;
	}
	public void setSaleIncome(BigDecimal saleIncome) {
		this.saleIncome = saleIncome;
	}
	public BigDecimal getCostfee() {
		return costfee;
	}
	public void setCostfee(BigDecimal costfee) {
		this.costfee = costfee;
	}
	public Long getSmscntTotal() {
		return smscntTotal;
	}
	public void setSmscntTotal(Long smscntTotal) {
		this.smscntTotal = smscntTotal;
	}
	public BigDecimal getCostOutcome() {
		return costOutcome;
	}
	public void setCostOutcome(BigDecimal costOutcome) {
		this.costOutcome = costOutcome;
	}
	public BigDecimal getInOutRate() {
		return inOutRate;
	}
	public void setInOutRate(BigDecimal inOutRate) {
		this.inOutRate = inOutRate;
	}
	public BigDecimal getProfit() {
		return profit;
	}
	public void setProfit(BigDecimal profit) {
		this.profit = profit;
	}
	public String getProfitStr() {
		if(BigDecimal.ZERO.compareTo(profit.setScale(1,ROUND_HALF_UP))>0){//负数标志位红色
			this.profitStr = "<font color=\"#FF0000\">"+profit.toString()+"</font>";
		}else{
			this.profitStr = profit.toString();
		}
		return profitStr;
	}
	public void setProfitStr(String profitStr) {
		this.profitStr = profitStr;
	}

 

}
