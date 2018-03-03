package com.ucpaas.sms.task.model.ali;

import java.math.BigDecimal;

import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.math.BigDecimal.ZERO;

public class OverallTotal {
	// 用户短信总量0+1+3+4+5+6+7+8+9+10
	private Integer usersmstotal;
	// 总发送量1+3+6
	private Integer sendtotal;
	// 成功率(3/总发送量)
	private BigDecimal successrate;
	private String successrateStr;
	// 成功量(1+3)
	private Integer successtotal;
	// 提交成功(1)
	private Integer submitsuccess;
	// 明确成功(3)
	private Integer reportsuccess;
	// 明确失败(6)
	private Integer reportfail;
	// 销售收入
	private BigDecimal saleIncome;
	// 通道计费条数(1+2+3)
	private Integer smscntTotal;
	// 通道成本(单价 * 计费)
	private BigDecimal costOutcome;
	// 毛利
	private BigDecimal profit;

	private BigDecimal profitRate;

	private String profitStr;

	private String profitRateStr;

	public OverallTotal() {
	}

	public OverallTotal(Integer usersmstotal, Integer sendtotal,BigDecimal successrate, Integer successtotal, Integer submitsuccess, Integer reportsuccess, Integer reportfail, BigDecimal saleIncome, Integer smscntTotal, BigDecimal costOutcome, BigDecimal profit, BigDecimal profitRate) {
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
			this.profitStr = "<font color=\"#FF0000\">"+profit.setScale(1,ROUND_HALF_UP).toString()+"</font>";
		}else{
			this.profitStr = profit.setScale(1,ROUND_HALF_UP).toString();
		}
		this.profitRate = profitRate;
	}

	public static OverallTotal defaultOverallTotal(){
		return new OverallTotal(0, 0,ZERO, 0, 0, 0, 0,ZERO, 0, ZERO, ZERO, ZERO) ;
	}

	public Integer getUsersmstotal() {
		return usersmstotal;
	}

	public void setUsersmstotal(Integer usersmstotal) {
		this.usersmstotal = usersmstotal;
	}

	public Integer getSendtotal() {
		return sendtotal;
	}

	public void setSendtotal(Integer sendtotal) {
		this.sendtotal = sendtotal;
	}

	public Integer getSuccesstotal() {
		return successtotal;
	}

	public void setSuccesstotal(Integer successtotal) {
		this.successtotal = successtotal;
	}

	public Integer getSubmitsuccess() {
		return submitsuccess;
	}

	public void setSubmitsuccess(Integer submitsuccess) {
		this.submitsuccess = submitsuccess;
	}

	public Integer getReportsuccess() {
		return reportsuccess;
	}

	public void setReportsuccess(Integer reportsuccess) {
		this.reportsuccess = reportsuccess;
	}

	public Integer getReportfail() {
		return reportfail;
	}

	public void setReportfail(Integer reportfail) {
		this.reportfail = reportfail;
	}

	public BigDecimal getSaleIncome() {
		return saleIncome;
	}

	public void setSaleIncome(BigDecimal saleIncome) {
		this.saleIncome = saleIncome;
	}

	public Integer getSmscntTotal() {
		return smscntTotal;
	}

	public void setSmscntTotal(Integer smscntTotal) {
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

	public void setProfit(BigDecimal profit) {
		this.profit = profit;
		if(BigDecimal.ZERO.compareTo(profit.setScale(1,ROUND_HALF_UP))>0){//负数标志位红色
			this.profitStr = "<font color=\"#FF0000\">"+profit.setScale(1,ROUND_HALF_UP).toString()+"</font>";
		}else{
			this.profitStr = profit.setScale(1,ROUND_HALF_UP).toString();
		}
	}

	public BigDecimal getProfitRate() {
		return profitRate;
	}

	public void setProfitRate(BigDecimal profitRate) {
		this.profitRate = profitRate;
		if(BigDecimal.ZERO.compareTo(profitRate.setScale(3,ROUND_HALF_UP))>0){//负数标志位红色
			this.profitRateStr = "<font color=\"#FF0000\">"+profitRate.multiply(new BigDecimal("100")).setScale(1,ROUND_HALF_UP).toString()+"%</font>";
		}else{
			this.profitRateStr = profitRate.multiply(new BigDecimal("100")).setScale(1,ROUND_HALF_UP).toString()+"%";
		}
	}

	public String getProfitStr() {
		return profitStr;
	}

	public void setProfitStr(String profitStr) {
		this.profitStr = profitStr;
	}

	public String getProfitRateStr() {
		return profitRateStr;
	}

	public void setProfitRateStr(String profitRateStr) {
		this.profitRateStr = profitRateStr;
	}

	public BigDecimal getSuccessrate() {
		return successrate;
	}

	public void setSuccessrate(BigDecimal successrate) {
		this.successrate = successrate;
	}

	public String getSuccessrateStr() {
		return successrate.multiply(new BigDecimal("100")).setScale(1,ROUND_HALF_UP).toString() + "%";
	}

	public void setSuccessrateStr(String successrateStr) {
		this.successrateStr = successrateStr;
	}

	@Override
	public String toString() {
		return "OverallTotal{" +
				"usersmstotal=" + usersmstotal +
				", sendtotal=" + sendtotal +
				", successrate=" + successrate +
				", successrateStr=" + successrateStr +
				", successtotal=" + successtotal +
				", submitsuccess=" + submitsuccess +
				", reportsuccess=" + reportsuccess +
				", reportfail=" + reportfail +
				", saleIncome=" + saleIncome +
				", smscntTotal=" + smscntTotal +
				", costOutcome=" + costOutcome +
				", profit=" + profit +
				", profitRate=" + profitRate +
				", profitStr='" + profitStr + '\'' +
				", profitRateStr='" + profitRateStr + '\'' +
				'}';
	}
}
