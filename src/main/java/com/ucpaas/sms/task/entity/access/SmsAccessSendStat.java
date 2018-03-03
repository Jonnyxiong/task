package com.ucpaas.sms.task.entity.access;

import java.math.BigDecimal;
import java.util.Date;
 

/**
 * @description 客户发送量表
 * @author 黄文杰
 * @date 2017-07-25
 */ 
public class SmsAccessSendStat {
    
    // 主键, 自增长
    private Integer id;
    // 部门id(一级部门), 关联t_sms_department表中department_id字段
    private Integer departmentId;
    // 代理商id, 关联t_sms_agent_info表中agent_id字段
    private Integer agentId;
    // 客户组id, 关联t_sms_account_group表中的account_gid字段
    private Integer accountGid;
    // 用户账户(子账号), 关联t_sms_account表中clientid字段
    private String clientid;
    // 归属销售, 关联t_sms_user表中的id字段
    private Long belongSale;
    // 短信类型, 0:通知短信,4:验证码短信,5:营销短信,6:告警短信,7:USSD,8:闪信
    private Integer smstype;
    // 付费类型, 0:预付费,1:后付费
    private Integer paytype;
    // 未发送数量(状态:0)
    private Integer notsend;
    // 提交成功数量(状态:1)
    private Integer submitsuccess;
    // 明确成功数量(状态:3)
    private Integer reportsuccess;
    // 提交失败数量(状态:4)
    private Integer submitfail;
    // 发送失败数量(状态:5)
    private Integer subretfail;
    // 明确失败数量(状态:6)
    private Integer reportfail;
    // 审核不通过数量(状态:7)
    private Integer auditfail;
    // 网关接收拦截数量(状态:8)
    private Integer recvintercept;
    // 网关发送拦截数量(状态:9)
    private Integer sendintercept;
    // 超频拦截数量(状态:10)
    private Integer overrateintercept;
    // 通道总成本(单位:厘), 通道侧数据
    private BigDecimal costfee;
    // 客户总消耗(单位:厘)
    private BigDecimal salefee;
    // 统计类型, 0:每日,2:每月
    private Integer stattype;
    // 统计的数据时间(天)
    private Integer date;
    // 创建时间, 信息保存时间
    private Date createtime;

    // 通道运营商类型，0：全网，1：移动，2：联通，3：电信，4：国际，-1：拦截
    private Integer operatorstype;

    public Integer getOperatorstype() {
        return operatorstype;
    }

    public void setOperatorstype(Integer operatorstype) {
        this.operatorstype = operatorstype;
    }

    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id ;
    }
    
    public Integer getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId ;
    }
    
    public Integer getAgentId() {
        return agentId;
    }
    
    public void setAgentId(Integer agentId) {
        this.agentId = agentId ;
    }
    
    public Integer getAccountGid() {
        return accountGid;
    }
    
    public void setAccountGid(Integer accountGid) {
        this.accountGid = accountGid ;
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
    
    public Integer getSmstype() {
        return smstype;
    }
    
    public void setSmstype(Integer smstype) {
        this.smstype = smstype ;
    }
    
    public Integer getPaytype() {
        return paytype;
    }
    
    public void setPaytype(Integer paytype) {
        this.paytype = paytype ;
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
    
}