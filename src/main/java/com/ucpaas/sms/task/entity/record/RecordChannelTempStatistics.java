package com.ucpaas.sms.task.entity.record;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.ibatis.type.Alias;

@Alias("RecordChannelTempStatistics")
public class RecordChannelTempStatistics {
    
    // 通道号
    private Integer channelid;
    // 通道备注
    private String remark;
    // 
    private String operatorstype;
    // 
    private BigDecimal chargetotal;
    // 
    private BigDecimal costtotal;
    // 
    private BigDecimal sendtotal;
    // 
    private BigDecimal notsend;
    // 
    private BigDecimal submitsuccess;
    // 
    private BigDecimal subretsuccess;
    // 
    private BigDecimal reportsuccess;
    // 
    private BigDecimal submitfail;
    // 
    private BigDecimal subretfail;
    // 
    private BigDecimal reportfail;
    // 
    private String date;
    // 
    private Date createtime;
    // 用户账号
    private String clientid;
    // 归属销售
    private Long belongSale;
    // 付费类型，0：预付费，1：后付费
    private Integer paytype;
    // 归属商务
    private Long belongBusiness;
    // 短信类型， 0：通知短信，    4：验证码短信，    5：营销短信，    6：告警短信，    7：USSD，    8：闪信
    private Integer smstype;
    
	public Integer getPaytype() {
		return paytype;
	}

	public void setPaytype(Integer paytype) {
		this.paytype = paytype;
	}

	public String getClientid() {
		return clientid;
	}

	public void setClientid(String clientid) {
		this.clientid = clientid;
	}

	public Long getBelongSale() {
		return belongSale;
	}

	public void setBelongSale(Long belongSale) {
		this.belongSale = belongSale;
	}

	public Integer getChannelid() {
        return channelid;
    }
    
    public void setChannelid(Integer channelid) {
        this.channelid = channelid ;
    }
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark ;
    }
    
    public String getOperatorstype() {
        return operatorstype;
    }
    
    public void setOperatorstype(String operatorstype) {
        this.operatorstype = operatorstype ;
    }
    
    public BigDecimal getChargetotal() {
        return chargetotal;
    }
    
    public void setChargetotal(BigDecimal chargetotal) {
        this.chargetotal = chargetotal ;
    }
    
    public BigDecimal getCosttotal() {
        return costtotal;
    }
    
    public void setCosttotal(BigDecimal costtotal) {
        this.costtotal = costtotal ;
    }
    
    public BigDecimal getSendtotal() {
        return sendtotal;
    }
    
    public void setSendtotal(BigDecimal sendtotal) {
        this.sendtotal = sendtotal ;
    }
    
    public BigDecimal getNotsend() {
        return notsend;
    }
    
    public void setNotsend(BigDecimal notsend) {
        this.notsend = notsend ;
    }
    
    public BigDecimal getSubmitsuccess() {
        return submitsuccess;
    }
    
    public void setSubmitsuccess(BigDecimal submitsuccess) {
        this.submitsuccess = submitsuccess ;
    }
    
    public BigDecimal getSubretsuccess() {
        return subretsuccess;
    }
    
    public void setSubretsuccess(BigDecimal subretsuccess) {
        this.subretsuccess = subretsuccess ;
    }
    
    public BigDecimal getReportsuccess() {
        return reportsuccess;
    }
    
    public void setReportsuccess(BigDecimal reportsuccess) {
        this.reportsuccess = reportsuccess ;
    }
    
    public BigDecimal getSubmitfail() {
        return submitfail;
    }
    
    public void setSubmitfail(BigDecimal submitfail) {
        this.submitfail = submitfail ;
    }
    
    public BigDecimal getSubretfail() {
        return subretfail;
    }
    
    public void setSubretfail(BigDecimal subretfail) {
        this.subretfail = subretfail ;
    }
    
    public BigDecimal getReportfail() {
        return reportfail;
    }
    
    public void setReportfail(BigDecimal reportfail) {
        this.reportfail = reportfail ;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date ;
    }
    
    public Date getCreatetime() {
        return createtime;
    }
    
    public void setCreatetime(Date createtime) {
        this.createtime = createtime ;
    }
    
    public Long getBelongBusiness() {
		return belongBusiness;
	}

	public void setBelongBusiness(Long belongBusiness) {
		this.belongBusiness = belongBusiness;
	}
	
	public Integer getSmstype() {
		return smstype;
	}

	public void setSmstype(Integer smstype) {
		this.smstype = smstype;
	}
    
}