package com.ucpaas.sms.task.entity.record;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.ibatis.type.Alias;

@Alias("RecordConsumeStatistics")
public class RecordConsumeStatistics {
    
    // 主键，自增长
    private Integer id;
    // 通道号，关联t_sms_channel表中cid字段
    private Integer channelid;
    // 归属商务，关联t_sms_user表中id字段
    private Long belongBusiness;
    // 部门id（一级部门）关联t_sms_department表中department_id字段
    private Integer departmentId;
    // 短信类型，0：通知短信，4：验证码短信，5：营销短信，6：告警短信，7：USSD，8：闪信
    private Integer smstype;
    // 付费类型，0：预付费，1：后付费
    private Integer paytype;
    // 通道说明
    private String remark;
    // 运营商类型，0：全网，1：移动，2：联通，3：电信，4：国际
    private Integer operatorstype;
    // 通道单价，单位：厘
    private BigDecimal costprice;
    // 通道总成本，单位：厘
    private BigDecimal costtotal;
    // 销售收入，单位：厘，客户侧
    private BigDecimal saletotal;
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
    // 明确成功数量（状态：5）
    private Integer subretfail;
    // 明确失败数量（状态：6）
    private Integer reportfail;
    // 统计类型，0：每日，2：每月 
    private Integer stattype;
    // 统计的数据时间 
    private Integer date;
    // 创建时间，信息保存时间
    private Date createtime;
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id ;
    }
    
    public Integer getChannelid() {
        return channelid;
    }
    
    public void setChannelid(Integer channelid) {
        this.channelid = channelid ;
    }
    
    public Long getBelongBusiness() {
        return belongBusiness;
    }
    
    public void setBelongBusiness(Long belongBusiness) {
        this.belongBusiness = belongBusiness ;
    }
    
    public Integer getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId ;
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
    
    public BigDecimal getCostprice() {
        return costprice;
    }
    
    public void setCostprice(BigDecimal costprice) {
        this.costprice = costprice ;
    }
    
    public BigDecimal getCosttotal() {
        return costtotal;
    }
    
    public void setCosttotal(BigDecimal costtotal) {
        this.costtotal = costtotal ;
    }
    
    public BigDecimal getSaletotal() {
        return saletotal;
    }
    
    public void setSaletotal(BigDecimal saletotal) {
        this.saletotal = saletotal ;
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
    
}