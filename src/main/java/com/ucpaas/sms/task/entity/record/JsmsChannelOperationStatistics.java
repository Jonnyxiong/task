package com.ucpaas.sms.task.entity.record;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description 通道属性
 * @author huangwenjie
 * @date 2018-01-13
 */
public class JsmsChannelOperationStatistics {
    
    // 序号ID，自增长，主键
    private Integer id;
    // 通道号，关联t_sms_channel表中cid字段
    private Integer channelId;
    // 运营商类型0：全网1：移动2：联通3：电信4：国际
    private Integer operatorstype;
    // 提交条数（状态：1+2+3+5+6，拆分后）
    private Integer submitTotal;
    // 明确成功条数（状态：3）
    private Integer reportsuccess;
    // 发送成功率，明确成功条数/提交条数*100%
    private BigDecimal sendSuccessRatio;
    // 低消条数
    private BigDecimal lowConsumeNumber;
    // 低消完成率，明确成功条数/低消条数*100%
    private BigDecimal lowConsumeRatio;
    // 投诉个数
    private Integer complaintNumber;
    // 投诉率，单位：百万分之一，投诉个数/明确成功条数，
    private BigDecimal complaintRatio;
    // 投诉系数，单位：百万分之一
    private BigDecimal complaintCoefficient;
    // 投诉差异值，单位：百万分之一，投诉系数-投诉率
    private BigDecimal complaintDifference;
    // 成本价，单位：厘
    private BigDecimal costprice;
    // 归属商务，关联t_sms_user表中id字段
    private Long belongBusiness;
    // 通道所属类型，1：自有，2：直连，3：第三方
    private Integer ownerType;
    // 统计时间，格式yyyyMM，如201801
    private Integer date;
    // 更新时间
    private Date updateTime;
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id ;
    }
    
    public Integer getChannelId() {
        return channelId;
    }
    
    public void setChannelId(Integer channelId) {
        this.channelId = channelId ;
    }
    
    public Integer getOperatorstype() {
        return operatorstype;
    }
    
    public void setOperatorstype(Integer operatorstype) {
        this.operatorstype = operatorstype ;
    }
    
    public Integer getSubmitTotal() {
        return submitTotal;
    }
    
    public void setSubmitTotal(Integer submitTotal) {
        this.submitTotal = submitTotal ;
    }
    
    public Integer getReportsuccess() {
        return reportsuccess;
    }
    
    public void setReportsuccess(Integer reportsuccess) {
        this.reportsuccess = reportsuccess ;
    }
    
    public BigDecimal getSendSuccessRatio() {
        return sendSuccessRatio;
    }
    
    public void setSendSuccessRatio(BigDecimal sendSuccessRatio) {
        this.sendSuccessRatio = sendSuccessRatio ;
    }
    
    public BigDecimal getLowConsumeNumber() {
        return lowConsumeNumber;
    }
    
    public void setLowConsumeNumber(BigDecimal lowConsumeNumber) {
        this.lowConsumeNumber = lowConsumeNumber ;
    }
    
    public BigDecimal getLowConsumeRatio() {
        return lowConsumeRatio;
    }
    
    public void setLowConsumeRatio(BigDecimal lowConsumeRatio) {
        this.lowConsumeRatio = lowConsumeRatio ;
    }
    
    public Integer getComplaintNumber() {
        return complaintNumber;
    }
    
    public void setComplaintNumber(Integer complaintNumber) {
        this.complaintNumber = complaintNumber ;
    }
    
    public BigDecimal getComplaintRatio() {
        return complaintRatio;
    }
    
    public void setComplaintRatio(BigDecimal complaintRatio) {
        this.complaintRatio = complaintRatio ;
    }
    
    public BigDecimal getComplaintCoefficient() {
        return complaintCoefficient;
    }
    
    public void setComplaintCoefficient(BigDecimal complaintCoefficient) {
        this.complaintCoefficient = complaintCoefficient ;
    }
    
    public BigDecimal getComplaintDifference() {
        return complaintDifference;
    }
    
    public void setComplaintDifference(BigDecimal complaintDifference) {
        this.complaintDifference = complaintDifference ;
    }
    
    public BigDecimal getCostprice() {
        return costprice;
    }
    
    public void setCostprice(BigDecimal costprice) {
        this.costprice = costprice ;
    }
    
    public Long getBelongBusiness() {
        return belongBusiness;
    }
    
    public void setBelongBusiness(Long belongBusiness) {
        this.belongBusiness = belongBusiness ;
    }
    
    public Integer getOwnerType() {
        return ownerType;
    }
    
    public void setOwnerType(Integer ownerType) {
        this.ownerType = ownerType ;
    }
    
    public Integer getDate() {
        return date;
    }
    
    public void setDate(Integer date) {
        this.date = date ;
    }
    
    public Date getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime ;
    }
    
}