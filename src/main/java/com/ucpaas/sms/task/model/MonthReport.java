package com.ucpaas.sms.task.model;

public class MonthReport {
	
	//短信账单月份(11) -> 邮件标题
	private String monthNumStr;
	
	//年份(2017)
	private String yearNumStr;
	
	//代理商邮箱 -> 代理商登录账号
	private String loginAccount;
	
	//账单周期开始(2016/11/01)
	private String billCycleStart;
	
	//账单周期结束(2016/11/01)
	private String billCycleEnd;
	
	//计费条数
	private String chargeNum;
	
	//实际消耗
	private String actualConsume;
	
	//剩余条数(国内)
	private String remainNum;
	
	//剩余钱(国际)
	private String remainAmount;
	
	//年月(2016年11月份)
	private String yearMonthStr;
	
	//短信发送量概览图片地址
	private String smsSendImgUrl;
	
	//击败的百分比
	private String beatPercent;
	
	//短信消耗金额图片地址
	private String smsConsumeImgUrl;
	
	//云之讯logo
	private String icon01UrlStr;
	
	//云之讯短信产品
	private String icon02UrlStr;
	
	//小手图片
	private String icon03UrlStr;
	
	//二维码图片地址
	private String icon04UrlStr;
	
	//微信图片
	private String icon05UrlStr;
	
	//点击记录的url
	private String clickRecordUrlStr;
	

	public String getMonthNumStr() {
		return monthNumStr;
	}

	public void setMonthNumStr(String monthNumStr) {
		this.monthNumStr = monthNumStr;
	}

	public String getYearNumStr() {
		return yearNumStr;
	}

	public void setYearNumStr(String yearNumStr) {
		this.yearNumStr = yearNumStr;
	}

	public String getLoginAccount() {
		return loginAccount;
	}

	public void setLoginAccount(String loginAccount) {
		this.loginAccount = loginAccount;
	}

	public String getBillCycleStart() {
		return billCycleStart;
	}

	public void setBillCycleStart(String billCycleStart) {
		this.billCycleStart = billCycleStart;
	}

	public String getBillCycleEnd() {
		return billCycleEnd;
	}

	public void setBillCycleEnd(String billCycleEnd) {
		this.billCycleEnd = billCycleEnd;
	}

	public String getChargeNum() {
		return chargeNum;
	}

	public void setChargeNum(String chargeNum) {
		this.chargeNum = chargeNum;
	}

	public String getActualConsume() {
		return actualConsume;
	}

	public void setActualConsume(String actualConsume) {
		this.actualConsume = actualConsume;
	}

	public String getRemainNum() {
		return remainNum;
	}

	public void setRemainNum(String remainNum) {
		this.remainNum = remainNum;
	}

	public String getRemainAmount() {
		return remainAmount;
	}

	public void setRemainAmount(String remainAmount) {
		this.remainAmount = remainAmount;
	}


	public String getYearMonthStr() {
		return yearMonthStr;
	}

	public void setYearMonthStr(String yearMonthStr) {
		this.yearMonthStr = yearMonthStr;
	}

	public String getSmsSendImgUrl() {
		return smsSendImgUrl;
	}

	public void setSmsSendImgUrl(String smsSendImgUrl) {
		this.smsSendImgUrl = smsSendImgUrl;
	}

	public String getBeatPercent() {
		return beatPercent;
	}

	public void setBeatPercent(String beatPercent) {
		this.beatPercent = beatPercent;
	}

	public String getSmsConsumeImgUrl() {
		return smsConsumeImgUrl;
	}

	public void setSmsConsumeImgUrl(String smsConsumeImgUrl) {
		this.smsConsumeImgUrl = smsConsumeImgUrl;
	}

	public String getIcon01UrlStr() {
		return icon01UrlStr;
	}

	public void setIcon01UrlStr(String icon01UrlStr) {
		this.icon01UrlStr = icon01UrlStr;
	}

	public String getIcon02UrlStr() {
		return icon02UrlStr;
	}

	public void setIcon02UrlStr(String icon02UrlStr) {
		this.icon02UrlStr = icon02UrlStr;
	}

	public String getIcon03UrlStr() {
		return icon03UrlStr;
	}

	public void setIcon03UrlStr(String icon03UrlStr) {
		this.icon03UrlStr = icon03UrlStr;
	}

	public String getIcon04UrlStr() {
		return icon04UrlStr;
	}

	public void setIcon04UrlStr(String icon04UrlStr) {
		this.icon04UrlStr = icon04UrlStr;
	}

	public String getIcon05UrlStr() {
		return icon05UrlStr;
	}

	public void setIcon05UrlStr(String icon05UrlStr) {
		this.icon05UrlStr = icon05UrlStr;
	}

	public String getClickRecordUrlStr() {
		return clickRecordUrlStr;
	}

	public void setClickRecordUrlStr(String clickRecordUrlStr) {
		this.clickRecordUrlStr = clickRecordUrlStr;
	}

	@Override
	public String toString() {
		return "MonthReport [monthNumStr=" + monthNumStr + ", yearNumStr=" + yearNumStr + ", loginAccount="
				+ loginAccount + ", billCycleStart=" + billCycleStart + ", billCycleEnd=" + billCycleEnd
				+ ", chargeNum=" + chargeNum + ", actualConsume=" + actualConsume + ", remainNum=" + remainNum
				+ ", remainAmount=" + remainAmount + ", yearMonthStr=" + yearMonthStr + ", smsSendImgUrl="
				+ smsSendImgUrl + ", beatPercent=" + beatPercent + ", smsConsumeImgUrl=" + smsConsumeImgUrl
				+ ", icon01UrlStr=" + icon01UrlStr + ", icon02UrlStr=" + icon02UrlStr + ", icon03UrlStr=" + icon03UrlStr
				+ ", icon04UrlStr=" + icon04UrlStr + ", icon05UrlStr=" + icon05UrlStr + ", clickRecordUrlStr="
				+ clickRecordUrlStr + "]";
	}
	
}
