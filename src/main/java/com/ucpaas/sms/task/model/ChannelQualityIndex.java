package com.ucpaas.sms.task.model;

import java.text.DecimalFormat;

public class ChannelQualityIndex {
	
	private String indexName;
	
	private double indexValue;
	
	private double upLimit;
	
	private double lowerLimit;

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public double getIndexValue() {
		return indexValue;
	}
	
	public String getIdexValuePercent() {
		DecimalFormat df = new DecimalFormat("#0.0");
		return df.format(indexValue * 100) + "%";
	}

	public void setIndexValue(double indexValue) {
		this.indexValue = indexValue;
	}

	public double getUpLimit() {
		return upLimit;
	}

	public void setUpLimit(double upLimit) {
		this.upLimit = upLimit;
	}

	public double getLowerLimit() {
		return lowerLimit;
	}

	public void setLowerLimit(double lowerLimit) {
		this.lowerLimit = lowerLimit;
	}
	
	public ChannelQualityIndex() {
	}
	

	public ChannelQualityIndex(String indexName) {
		super();
		this.indexName = indexName;
	}

	public ChannelQualityIndex(String indexName, double indexValue, double upLimit, double lowerLimit) {
		super();
		this.indexName = indexName;
		this.indexValue = indexValue;
		this.upLimit = upLimit;
		this.lowerLimit = lowerLimit;
	}
	
	public String getIndexErrorDesp(){
		StringBuffer result = new StringBuffer();
		
		if(this.getIndexName().equals("应答率")){
			result.append(this.getIndexName());
			result.append("0-2s内");
			result.append(this.getIdexValuePercent());
			result.append("小于阈值");
			result.append(this.getLowerLimit() * 100);
			result.append("%");
		}else if(this.getIndexName().equals("回执率")){
			result.append(this.getIndexName());
			result.append("0-10s内");
			result.append(this.getIdexValuePercent());
			result.append("小于阈值");
			result.append(this.getLowerLimit() * 100);
			result.append("%");
		}else if(this.getIndexName().equals("提交失败条数")){
			result.append(this.getIndexName());
			result.append("5分钟中内");
			result.append(this.getIndexValue());
			result.append("大于阈值");
			result.append((int)this.getLowerLimit());
		}else if(this.getIndexName().equals("发送失败率")){
			result.append(this.getIndexName());
			result.append("5分钟中内");
			result.append(this.getIdexValuePercent());
			result.append("大于阈值");
			result.append(this.getLowerLimit() * 100);
			result.append("%");
		}else {
			result.append(this.getIndexName());
			result.append("5分钟中内");
			result.append(this.getIdexValuePercent());
			result.append("小于阈值");
			result.append(this.getLowerLimit() * 100);
			result.append("%");
		}
		
		return result.toString();
	}
	
	public String getIndexWarningDesp(){
		StringBuffer result = new StringBuffer();
		
		if(this.getIndexName().equals("应答率")){
			result.append(this.getIndexName());
			result.append("0-2s内");
			result.append(this.getIdexValuePercent());
			result.append("小于阈值");
			result.append(this.getUpLimit() * 100);
			result.append("%");
		}else if(this.getIndexName().equals("回执率")){
			result.append(this.getIndexName());
			result.append("0-10s内");
			result.append(this.getIdexValuePercent());
			result.append("小于阈值");
			result.append(this.getUpLimit() * 100);
			result.append("%");
		}else if(this.getIndexName().equals("提交失败条数")){
			result.append(this.getIndexName());
			result.append("5分钟中内");
			result.append(this.getIndexValue());
			result.append("大于阈值");
			result.append((int)this.getUpLimit());
		}else if(this.getIndexName().equals("发送失败率")){
			result.append(this.getIndexName());
			result.append("5分钟中内");
			result.append(this.getIdexValuePercent());
			result.append("大于阈值");
			result.append(this.getUpLimit() * 100);
			result.append("%");
		}else{
			result.append(this.getIndexName());
			result.append("5分钟中内");
			result.append(this.getIdexValuePercent());
			result.append("小于阈值");
			result.append(this.getUpLimit() * 100);
			result.append("%");
		}
		
		return result.toString();
	}

}
