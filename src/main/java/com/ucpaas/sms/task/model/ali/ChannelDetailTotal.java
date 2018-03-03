package com.ucpaas.sms.task.model.ali;

import java.math.BigDecimal;

import static java.math.BigDecimal.ROUND_HALF_UP;

public class ChannelDetailTotal {

	// 用户短信总量0+1+3+4+5+6+7+8+9+10
	private Integer usersmstotal;
	// 总发送量1+3+6
	private Integer sendtotal;
	// 成功率(3/总发送量)
	private BigDecimal successrate;
	private String successrateStr;
	// 成功量(1+3)
	private Integer successtotal;
	// 未发送(0)
	private Integer notsend;
	// 提交成功(1)
	private Integer submitsuccess;
	// 明确成功(3)
	private Integer reportsuccess;
	// 提交失败(4)
	private Integer submitfail;
	// 发送失败(5)
	private Integer subretfail;
	// 明确失败(6)
	private Integer reportfail;
	// 审核不通过(7)
	private Integer auditfail;
	// 网关接收拦截(8)
	private Integer recvintercept;
	// 网关发送拦截(9)
	private Integer sendintercept;
	// 超频拦截(10)
	private Integer overrateintercept;

	// 销售单价
	private BigDecimal unitprice;
	// 行业单价
	private String hyUnitprice;
	// 营销单价
	private String yxUnitprice;

	// 销售收入
	private BigDecimal saleIncome;
	// 通道单价
	private BigDecimal costfee;
	// 通道计费条数
	private Integer smscntTotal;
	// 通道成本
	private BigDecimal costOutcome;
	// 通道成本1（系统累加）
	private BigDecimal accumulatedCost;
	// 通道成本2（单价*计费）
	private BigDecimal multiplyCost;
	// 阿里 		(用户侧3-通道计费数)/通道计费数
	// 重要客户	(用户侧1+3-通道计费数)/通道计费数
	private BigDecimal inOutRate;
	// 毛利(收入-成本2)
	private BigDecimal profit;
	// 毛利率(毛利÷销售收入)
	private BigDecimal profitRate;

	private String inOutRateStr;

	private String profitStr;

	private String profitRateStr;

	public ChannelDetailTotal(Integer usersmstotal, Integer sendtotal, BigDecimal successrate, Integer successtotal, Integer notsend, Integer submitsuccess, Integer reportsuccess, Integer submitfail, Integer subretfail, Integer reportfail, Integer auditfail, Integer recvintercept, Integer sendintercept, Integer overrateintercept, BigDecimal unitprice, String hyUnitprice, String yxUnitprice, BigDecimal saleIncome, BigDecimal costfee, Integer smscntTotal, BigDecimal costOutcome, BigDecimal accumulatedCost, BigDecimal multiplyCost, BigDecimal inOutRate, BigDecimal profit, BigDecimal profitRate, String inOutRateStr, String profitStr, String profitRateStr) {

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
		this.unitprice = unitprice;
		this.hyUnitprice = hyUnitprice;
		this.yxUnitprice = yxUnitprice;
		this.saleIncome = saleIncome;
		this.costfee = costfee;
		this.smscntTotal = smscntTotal;
		this.costOutcome = costOutcome;
		this.accumulatedCost = accumulatedCost;
		this.multiplyCost = multiplyCost;
		this.inOutRate = inOutRate;
		this.profit = profit;
		this.profitRate = profitRate;
		this.inOutRateStr = inOutRateStr;
		this.profitStr = profitStr;
		this.profitRateStr = profitRateStr;
	}

	/**
	 * 重要客户的报表的构造器
	 * @param date
	 * @param clientid
	 * @param username
	 * @param channeloperatorstype
	 * @param channelid
	 * @param channelremark
	 * @param sendtotal
	 * @param successrate
	 * @param successtotal
	 * @param submitsuccess
	 * @param reportsuccess
	 * @param reportfail
	 * @param hyUnitprice
	 * @param yxUnitprice
	 * @param saleIncome
	 * @param costfee
	 * @param smscntTotal
	 * @param accumulatedCost
	 * @param multiplyCost
	 * @param inOutRate
	 * @param profit
	 * @param profitRate
	 */
	public ChannelDetailTotal(String date, String clientid, String username, String channeloperatorstype, String channelid,
                              String channelremark, Integer sendtotal, BigDecimal successrate, Integer successtotal, Integer submitsuccess,
                              Integer reportsuccess, Integer reportfail, BigDecimal unitprice, String hyUnitprice, String yxUnitprice, BigDecimal saleIncome,
                              BigDecimal costfee, Integer smscntTotal, BigDecimal accumulatedCost, BigDecimal multiplyCost, BigDecimal inOutRate,
                              BigDecimal profit, BigDecimal profitRate) {
		super();
		this.sendtotal = sendtotal;
		this.successrate = successrate;
		this.successtotal = successtotal;
		this.submitsuccess = submitsuccess;
		this.reportsuccess = reportsuccess;
		this.reportfail = reportfail;
		this.unitprice = unitprice;
		this.hyUnitprice = hyUnitprice;
		this.yxUnitprice = yxUnitprice;
		this.saleIncome = saleIncome;
		this.costfee = costfee;
		this.smscntTotal = smscntTotal;
		this.accumulatedCost = accumulatedCost;
		this.multiplyCost = multiplyCost;
		this.inOutRate = inOutRate;
		this.profit = profit;
		this.profitRate = profitRate;
		if(BigDecimal.ZERO.compareTo(inOutRate.setScale(3,ROUND_HALF_UP))>0){//负数标志位红色
			this.inOutRateStr = "<font color=\"#FF0000\">"+inOutRate.multiply(new BigDecimal("100")).setScale(1,ROUND_HALF_UP).toString()+"%</font>";
		}else{
			this.inOutRateStr = inOutRate.multiply(new BigDecimal("100")).setScale(1,ROUND_HALF_UP).toString()+"%";
		}

		if(BigDecimal.ZERO.compareTo(profit.setScale(1,ROUND_HALF_UP))>0){//负数标志位红色
			this.profitStr = "<font color=\"#FF0000\">"+profitRate.setScale(1,ROUND_HALF_UP).toString()+"% </font>";
		}else{
			this.profitStr = profit.setScale(1,ROUND_HALF_UP).toString();
		}
		if(BigDecimal.ZERO.compareTo(profitRate.setScale(3,ROUND_HALF_UP))>0){//负数标志位红色
			this.profitRateStr = "<font color=\"#FF0000\">"+profitRate.multiply(new BigDecimal("100")).setScale(1,ROUND_HALF_UP).toString()+"%</font>";
		}else{
			this.profitRateStr = profitRate.multiply(new BigDecimal("100")).setScale(1,ROUND_HALF_UP).toString()+"%";
		}
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
	public BigDecimal getSuccessrate() {
		return successrate;
	}
	public void setSuccessrate(BigDecimal successrate) {
		this.successrate = successrate;
	}
	public Integer getSuccesstotal() {
		return successtotal;
	}
	public void setSuccesstotal(Integer successtotal) {
		this.successtotal = successtotal;
	}
	public Integer getNotsend() {
		return notsend;
	}
	public void setNotsend(Integer notsend) {
		this.notsend = notsend;
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
	public Integer getSubmitfail() {
		return submitfail;
	}
	public void setSubmitfail(Integer submitfail) {
		this.submitfail = submitfail;
	}
	public Integer getSubretfail() {
		return subretfail;
	}
	public void setSubretfail(Integer subretfail) {
		this.subretfail = subretfail;
	}
	public Integer getReportfail() {
		return reportfail;
	}
	public void setReportfail(Integer reportfail) {
		this.reportfail = reportfail;
	}
	public Integer getAuditfail() {
		return auditfail;
	}
	public void setAuditfail(Integer auditfail) {
		this.auditfail = auditfail;
	}
	public Integer getRecvintercept() {
		return recvintercept;
	}
	public void setRecvintercept(Integer recvintercept) {
		this.recvintercept = recvintercept;
	}
	public Integer getSendintercept() {
		return sendintercept;
	}
	public void setSendintercept(Integer sendintercept) {
		this.sendintercept = sendintercept;
	}
	public Integer getOverrateintercept() {
		return overrateintercept;
	}
	public void setOverrateintercept(Integer overrateintercept) {
		this.overrateintercept = overrateintercept;
	}
	public BigDecimal getUnitprice() {
		return unitprice;
	}
	public void setUnitprice(BigDecimal unitprice) {
		this.unitprice = unitprice;
	}

	public String getHyUnitprice() {
		return hyUnitprice;
	}

	public void setHyUnitprice(String hyUnitprice) {
		this.hyUnitprice = hyUnitprice;
	}

	public String getYxUnitprice() {
		return yxUnitprice;
	}

	public void setYxUnitprice(String yxUnitprice) {
		this.yxUnitprice = yxUnitprice;
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
	public BigDecimal getInOutRate() {
		return inOutRate;
	}
	public void setInOutRate(BigDecimal inOutRate) {
		this.inOutRate = inOutRate;
		if(BigDecimal.ZERO.compareTo(inOutRate.setScale(3,ROUND_HALF_UP))>0){//负数标志位红色
			this.inOutRateStr = "<font color=\"#FF0000\">"+inOutRate.multiply(new BigDecimal("100")).setScale(1,ROUND_HALF_UP).toString()+"%</font>";
		}else{
			this.inOutRateStr = inOutRate.multiply(new BigDecimal("100")).setScale(1,ROUND_HALF_UP).toString()+"%";
		}
	}
	public BigDecimal getProfit() {
		return profit;
	}
	public void setProfit(BigDecimal profit) {
		this.profit = profit;
	}
	public String getProfitStr() {
		if(BigDecimal.ZERO.compareTo(profit.setScale(1,ROUND_HALF_UP))>0){//负数标志位红色
			this.profitStr = "<font color=\"#FF0000\">"+profit.setScale(1,ROUND_HALF_UP).toString()+"</font>";
		}else{
			this.profitStr = profit.setScale(1,ROUND_HALF_UP).toString();
		}
		return profitStr;
	}
	public void setProfitStr(String profitStr) {
		this.profitStr = profitStr;
	}

	public String getInOutRateStr() {

		return inOutRateStr;
	}

	public void setInOutRateStr(String inOutRateStr) {
		this.inOutRateStr = inOutRateStr;
	}

	public BigDecimal getAccumulatedCost() {
		return accumulatedCost;
	}

	public void setAccumulatedCost(BigDecimal accumulatedCost) {
		this.accumulatedCost = accumulatedCost;
	}

	public BigDecimal getMultiplyCost() {
		return multiplyCost;
	}

	public void setMultiplyCost(BigDecimal multiplyCost) {
		this.multiplyCost = multiplyCost;
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

	public String getProfitRateStr() {
		return profitRateStr;
	}

	public void setProfitRateStr(String profitRateStr) {
		this.profitRateStr = profitRateStr;
	}

	public String getSuccessrateStr() {
		return successrate.multiply(new BigDecimal("100")).setScale(1,ROUND_HALF_UP).toString() + "%";
	}

	public void setSuccessrateStr(String successrateStr) {
		this.successrateStr = successrateStr;
	}

	public static ChannelDetailTotal defaultChannelDetail(){
		return new ChannelDetailTotal(0,0, BigDecimal.ZERO,0,0,0,0,0,0,0,0,0,0,0, BigDecimal.ZERO,"-","-", BigDecimal.ZERO, BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "-", "-", "-") ;
	}

	@Override
	public String toString() {
		return "ChannelDetail{" +
				", usersmstotal=" + usersmstotal +
				", sendtotal=" + sendtotal +
				", successrate=" + successrate +
				", successrateStr=" + successrateStr +
				", successtotal=" + successtotal +
				", notsend=" + notsend +
				", submitsuccess=" + submitsuccess +
				", reportsuccess=" + reportsuccess +
				", submitfail=" + submitfail +
				", subretfail=" + subretfail +
				", reportfail=" + reportfail +
				", auditfail=" + auditfail +
				", recvintercept=" + recvintercept +
				", sendintercept=" + sendintercept +
				", overrateintercept=" + overrateintercept +
				", unitprice=" + unitprice +
				", hyUnitprice='" + hyUnitprice + '\'' +
				", yxUnitprice='" + yxUnitprice + '\'' +
				", saleIncome=" + saleIncome +
				", costfee=" + costfee +
				", smscntTotal=" + smscntTotal +
				", costOutcome=" + costOutcome +
				", accumulatedCost=" + accumulatedCost +
				", multiplyCost=" + multiplyCost +
				", inOutRate=" + inOutRate +
				", profit=" + profit +
				", profitRate=" + profitRate +
				", inOutRateStr='" + inOutRateStr + '\'' +
				", profitStr='" + profitStr + '\'' +
				", profitRateStr='" + profitRateStr + '\'' +
				'}';
	}
}
