package com.ucpaas.sms.task.entity.record;

import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.util.Date;

@Alias("RecordChannelStatistics")
public class RecordChannelStatistics {
    
    // 主键id
    private Long id;
    // 通道号
    private Integer channelid;
    // 用户帐号，关联t_sms_record_0表中clientid字段
    private String clientid;
    // 通道说明
    private String remark;
    // 运营商类型
    private Integer operatorstype;
    // 计费条数(短信拆分发送后状态为1、2、3、6的总条数)
    private Integer chargetotal;
    // 通道成本价（厘）
    private BigDecimal costtotal;
    // 发送总数(短信拆分发送后状态为1、2、3、5、6的总条数)
    private Integer sendtotal;
    // 未发送数量（状态：0）
    private Integer notsend;
    // 提交成功数量（状态：1）
    private Integer submitsuccess;
    // 发送成功数量（状态：2）
    private Integer subretsuccess;
    // 明确成功数量（状态：3）
    private Integer reportsuccess;
    // 提交失败数量（状态：4）
    private Integer submitfail;
    // 发送失败数量（状态：5）
    private Integer subretfail;
    // 明确失败数量（状态：6）
    private Integer reportfail;
    // 统计类型，0：每日，1：每月
    private Integer stattype;
    // 统计的数据时间
    private Integer date;
    // 创建时间，信息保存时间
    private Date createtime;

    private Date createtimeStart;
    private Date createtimeEnd;

    public Date getCreatetimeStart() {
        return createtimeStart;
    }

    public void setCreatetimeStart(Date createtimeStart) {
        this.createtimeStart = createtimeStart;
    }

    public Date getCreatetimeEnd() {
        return createtimeEnd;
    }

    public void setCreatetimeEnd(Date createtimeEnd) {
        this.createtimeEnd = createtimeEnd;
    }

    // 付费类型，0：预付费，1：后付费
    private Integer paytype;
    // 所属销售，关联t_sms_access_0表中belong_sale字段
    private Long belongSale;
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

	public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id ;
    }
    
    public Integer getChannelid() {
        return channelid;
    }
    
    public void setChannelid(Integer channelid) {
        this.channelid = channelid ;
    }
    
    public String getClientid() {
        return clientid;
    }
    
    public void setClientid(String clientid) {
        this.clientid = clientid ;
    }
    
    public Long getBelongSale() {
        return belongSale;
    }
    
    public void setBelongSale(Long belongSale) {
        this.belongSale = belongSale ;
    }
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark ;
    }
    
    public Integer getOperatorstype() {
        return operatorstype;
    }
    
    public void setOperatorstype(Integer operatorstype) {
        this.operatorstype = operatorstype ;
    }
    
    public Integer getChargetotal() {
        return chargetotal;
    }
    
    public void setChargetotal(Integer chargetotal) {
        this.chargetotal = chargetotal ;
    }
    
    public BigDecimal getCosttotal() {
        return costtotal;
    }
    
    public void setCosttotal(BigDecimal costtotal) {
        this.costtotal = costtotal ;
    }
    
    public Integer getSendtotal() {
        return sendtotal;
    }
    
    public void setSendtotal(Integer sendtotal) {
        this.sendtotal = sendtotal ;
    }
    
    public Integer getNotsend() {
        return notsend;
    }
    
    public void setNotsend(Integer notsend) {
        this.notsend = notsend ;
    }
    
    public Integer getSubmitsuccess() {
        return submitsuccess;
    }
    
    public void setSubmitsuccess(Integer submitsuccess) {
        this.submitsuccess = submitsuccess ;
    }
    
    public Integer getSubretsuccess() {
        return subretsuccess;
    }
    
    public void setSubretsuccess(Integer subretsuccess) {
        this.subretsuccess = subretsuccess ;
    }
    
    public Integer getReportsuccess() {
        return reportsuccess;
    }
    
    public void setReportsuccess(Integer reportsuccess) {
        this.reportsuccess = reportsuccess ;
    }
    
    public Integer getSubmitfail() {
        return submitfail;
    }
    
    public void setSubmitfail(Integer submitfail) {
        this.submitfail = submitfail ;
    }
    
    public Integer getSubretfail() {
        return subretfail;
    }
    
    public void setSubretfail(Integer subretfail) {
        this.subretfail = subretfail ;
    }
    
    public Integer getReportfail() {
        return reportfail;
    }
    
    public void setReportfail(Integer reportfail) {
        this.reportfail = reportfail ;
    }
    
    public Integer getStattype() {
        return stattype;
    }
    
    public void setStattype(Integer stattype) {
        this.stattype = stattype ;
    }
    
    public Integer getDate() {
        return date;
    }
    
    public void setDate(Integer date) {
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