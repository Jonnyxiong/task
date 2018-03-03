package com.ucpaas.sms.task.entity.access;

import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.util.Date;

@Alias("AccessChannelStatistics")
public class AccessChannelStatistics {
    
    // 主键id
    private Long id;
    // 代理商id
    private Integer agentId;
    // 用户账号（子账号）
    private String clientid;
    // 用户名称
    private String name;
    // 平台账号
    private String sid;
    // 付费类型，0：预付费，1：后付费
    private Integer paytype;
    // 通道运营商类型
    private Integer operatorstype;
    // 通道号
    private Integer channelid;
    // 通道说明
    private String remark;
    // 计费条数（有通道时，状态为1/3/6的记录的计费条数,无通道时，状态为10的记录的计费条数,不需要smsid去重)
    private Integer chargetotal;
    // 超频计费条数
    private Integer overrateChargeTotal;
    // 唯一id，订单子id
    private String subId;
    // 通道成本价(单位：厘)
    private BigDecimal costfee;
    // 消费金额(单位：厘)  针对客户
    private BigDecimal salefee;
    // 消耗成本(单位：厘)针对代理商
    private BigDecimal productfee;
    // 产品类型，0：行业，1：营销，2：国际
    private Integer productType;
    // 总发送量 状态为1/3/6，按拆分后算
    private Integer sendtotal;
    // 用户短信总量 状态为1/3/4/5/6/7/8/9/10，按拆分后算
    private Integer usersmstotal;
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
    // 所属销售，关联t_sms_access_0表中belong_sale字段
    private Long belongSale;
    // 统计记录类型，0：日统计数据，1：日合计数据
    private Integer stattype;
    // 统计的数据时间（天）
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

    // 短信类型
    private Integer smstype;

    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id ;
    }
    
    public Integer getAgentId() {
        return agentId;
    }
    
    public void setAgentId(Integer agentId) {
        this.agentId = agentId ;
    }
    
    public String getClientid() {
        return clientid;
    }
    
    public void setClientid(String clientid) {
        this.clientid = clientid ;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name ;
    }
    
    public String getSid() {
        return sid;
    }
    
    public void setSid(String sid) {
        this.sid = sid ;
    }
    
    public Integer getPaytype() {
        return paytype;
    }
    
    public void setPaytype(Integer paytype) {
        this.paytype = paytype ;
    }
    
    public Integer getOperatorstype() {
        return operatorstype;
    }
    
    public void setOperatorstype(Integer operatorstype) {
        this.operatorstype = operatorstype ;
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
    
    public Integer getChargetotal() {
        return chargetotal;
    }
    
    public void setChargetotal(Integer chargetotal) {
        this.chargetotal = chargetotal ;
    }
    
    public Integer getOverrateChargeTotal() {
        return overrateChargeTotal;
    }
    
    public void setOverrateChargeTotal(Integer overrateChargeTotal) {
        this.overrateChargeTotal = overrateChargeTotal ;
    }
    
    public String getSubId() {
        return subId;
    }
    
    public void setSubId(String subId) {
        this.subId = subId ;
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
    
    public Integer getProductType() {
        return productType;
    }
    
    public void setProductType(Integer productType) {
        this.productType = productType ;
    }
    
    public Integer getSendtotal() {
        return sendtotal;
    }
    
    public void setSendtotal(Integer sendtotal) {
        this.sendtotal = sendtotal ;
    }
    
    public Integer getUsersmstotal() {
        return usersmstotal;
    }
    
    public void setUsersmstotal(Integer usersmstotal) {
        this.usersmstotal = usersmstotal ;
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
    
    public Long getBelongSale() {
        return belongSale;
    }
    
    public void setBelongSale(Long belongSale) {
        this.belongSale = belongSale ;
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

    public Integer getSmstype() {
        return smstype;
    }

    public void setSmstype(Integer smstype) {
        this.smstype = smstype;
    }

    @Override
    public String toString() {
        return "AccessChannelStatistics{" +
                "id=" + id +
                ", agentId=" + agentId +
                ", clientid='" + clientid + '\'' +
                ", name='" + name + '\'' +
                ", sid='" + sid + '\'' +
                ", paytype=" + paytype +
                ", operatorstype=" + operatorstype +
                ", channelid=" + channelid +
                ", remark='" + remark + '\'' +
                ", chargetotal=" + chargetotal +
                ", overrateChargeTotal=" + overrateChargeTotal +
                ", subId='" + subId + '\'' +
                ", costfee=" + costfee +
                ", salefee=" + salefee +
                ", productfee=" + productfee +
                ", productType=" + productType +
                ", sendtotal=" + sendtotal +
                ", usersmstotal=" + usersmstotal +
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
                ", belongSale=" + belongSale +
                ", stattype=" + stattype +
                ", date=" + date +
                ", createtime=" + createtime +
                ", smstype=" + smstype +
                '}';
    }
}