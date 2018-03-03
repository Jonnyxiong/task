package com.ucpaas.sms.task.test.model;

import java.math.BigDecimal;

import static java.math.BigDecimal.ROUND_HALF_UP;

public class Overall {
	// 时间
	private String date;
	// 用户短信总量0+1+3+4+5+6+7+8+9+10
	private Long usersmstotal;
	// 总发送量1+3+6
	private Long sendtotal;
	// 成功率(3/总发送量)
	private BigDecimal successrate;
	// 成功量(1+3)
	private Long successtotal;
	// 提交成功(1)
	private Long submitsuccess;
	// 明确成功(3)
	private Long reportsuccess;
	// 明确失败(6)
	private Long reportfail;

	// 销售收入
	private BigDecimal saleIncome;
	// 通道计费条数
	private Long smscntTotal;
	// 通道成本
	private BigDecimal costOutcome;

	// 毛利
	private BigDecimal profit;
	
	private String profitStr;

	 

	public Overall(String date, Long usersmstotal, Long sendtotal, BigDecimal successrate, Long successtotal,
			Long submitsuccess, Long reportsuccess, Long reportfail, BigDecimal saleIncome, Long smscntTotal,
			BigDecimal costOutcome, BigDecimal profit) {
		super();
		this.date = date;
		this.usersmstotal = usersmstotal;
		this.sendtotal = sendtotal;
		this.successrate = successrate;
		this.successtotal = successtotal;
		this.submitsuccess = submitsuccess;
		this.reportsuccess = reportsuccess;
		this.reportfail = reportfail;
		this.saleIncome = saleIncome;
		this.smscntTotal = smscntTotal;
		this.costOutcome = costOutcome;
		this.profit = profit;

		if(BigDecimal.ZERO.compareTo(profit.setScale(1,ROUND_HALF_UP))>0){//负数标志位红色
			this.profitStr = "<font color=\"#FF0000\">"+profit.toString()+"</font>";
		}else{
			this.profitStr = profit.toString();
		}
	}

	public void setProfit(BigDecimal profit) {
		this.profit = profit;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
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

	public Long getReportfail() {
		return reportfail;
	}

	public void setReportfail(Long reportfail) {
		this.reportfail = reportfail;
	}

	public BigDecimal getSaleIncome() {
		return saleIncome;
	}

	public void setSaleIncome(BigDecimal saleIncome) {
		this.saleIncome = saleIncome;
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

	public BigDecimal getProfit() {
		return profit;
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
