package com.ucpaas.sms.task.entity.access;

import java.math.BigDecimal;

import org.apache.ibatis.type.Alias;

@Alias("CustomerStatTemp")
public class CustomerStatTemp {
    
    // 自增长ID
    private Long id;
    // 用户账号（子账号）
    private String clientid;
    // 用户名称
    private String username;
    // 平台账号
    private String sid;
    // 代理商ID
    private Long agentId;
    // 付费类型，0：预付费，1：后付费
    private Integer paytype;
    // 通道号
    private Integer channelid;
    // 通道运营商类型
    private Integer operatorstype;
    // 通道说明
    private String channelremark;
    // 订单子ID
    private String subId;
    // 产品类型，0：行业，1：营销，2：国际，7：USSD，8：闪信，9：挂机短信，其中0和1为普通短信，2为国际短信
    private Integer productType;
    // 未发送数量（状态：0）
    private Integer notsend;
    // 提交成功数量（状态：1）
    private Integer submitsuccess;
    // 明确成功数量（状态：3）
    private Integer reportsuccess;
    // 提交失败数量（状态：4）
    private Integer submitfail;
    // 发送失败数量（状态：5）
    private Integer subretfail;
    // 明确失败数量（状态：6）
    private Integer reportfail;
    // 审核不通过数量（状态：7）
    private Integer auditfail;
    // 网关接收拦截数量（状态：8）
    private Integer recvintercept;
    // 网关发送拦截数量（状态：9）
    private Integer sendintercept;
    // 超频拦截数量（状态：10）
    private Integer overrateintercept;
    // 超频计费条数
    private Integer overrateChargeTotal;
    // 通道成本价(单位：厘)
    private BigDecimal costfee;
    // 消费金额(单位：厘)  针对客户
    private BigDecimal salefee;
    // 消耗成本(单位：厘)针对代理商
    private BigDecimal productfee;
    // 统计数据的时间
    private Integer date;
    // 归属销售
    private Long belongSale = null;
    // 短信类型，0：通知短信，4：验证码短信，5：营销短信，6：告警短信，7：USSD，8：闪信
    private Integer smstype;
    
    public Integer getSmstype() {
		return smstype;
	}

	public void setSmstype(Integer smstype) {
		this.smstype = smstype;
	}

	public Long getBelongSale() {
		return belongSale;
	}

	public void setBelongSale(Long belongSale) {
		this.belongSale = belongSale;
	}

	public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id ;
    }
    
    public String getClientid() {
        return clientid;
    }
    
    public void setClientid(String clientid) {
        this.clientid = clientid ;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username ;
    }
    
    public String getSid() {
        return sid;
    }
    
    public void setSid(String sid) {
        this.sid = sid ;
    }
    
    public Long getAgentId() {
        return agentId;
    }
    
    public void setAgentId(Long agentId) {
        this.agentId = agentId ;
    }
    
    public Integer getPaytype() {
        return paytype;
    }
    
    public void setPaytype(Integer paytype) {
        this.paytype = paytype ;
    }
    
    public Integer getChannelid() {
        return channelid;
    }
    
    public void setChannelid(Integer channelid) {
        this.channelid = channelid ;
    }
    
    public Integer getOperatorstype() {
        return operatorstype;
    }
    
    public void setOperatorstype(Integer operatorstype) {
        this.operatorstype = operatorstype ;
    }
    
    public String getChannelremark() {
        return channelremark;
    }
    
    public void setChannelremark(String channelremark) {
        this.channelremark = channelremark ;
    }
    
    public String getSubId() {
		return subId;
	}

	public void setSubId(String subId) {
		this.subId = subId;
	}

	public Integer getProductType() {
        return productType;
    }
    
    public void setProductType(Integer productType) {
        this.productType = productType ;
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
    
    public Integer getAuditfail() {
        return auditfail;
    }
    
    public void setAuditfail(Integer auditfail) {
        this.auditfail = auditfail ;
    }
    
    public Integer getRecvintercept() {
        return recvintercept;
    }
    
    public void setRecvintercept(Integer recvintercept) {
        this.recvintercept = recvintercept ;
    }
    
    public Integer getSendintercept() {
        return sendintercept;
    }
    
    public void setSendintercept(Integer sendintercept) {
        this.sendintercept = sendintercept ;
    }
    
    public Integer getOverrateintercept() {
        return overrateintercept;
    }
    
    public void setOverrateintercept(Integer overrateintercept) {
        this.overrateintercept = overrateintercept ;
    }
    
    public Integer getOverrateChargeTotal() {
        return overrateChargeTotal;
    }
    
    public void setOverrateChargeTotal(Integer overrateChargeTotal) {
        this.overrateChargeTotal = overrateChargeTotal ;
    }
    
    public BigDecimal getCostfee() {
        return costfee;
    }
    
    public void setCostfee(BigDecimal costfee) {
        this.costfee = costfee ;
    }
    
    public BigDecimal getSalefee() {
        return salefee;
    }
    
    public void setSalefee(BigDecimal salefee) {
        this.salefee = salefee ;
    }
    
    public BigDecimal getProductfee() {
        return productfee;
    }
    
    public void setProductfee(BigDecimal productfee) {
        this.productfee = productfee ;
    }
    
    public Integer getDate() {
        return date;
    }
    
    public void setDate(Integer date) {
        this.date = date ;
    }
    
}