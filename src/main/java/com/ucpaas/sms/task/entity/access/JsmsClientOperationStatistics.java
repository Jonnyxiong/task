package com.ucpaas.sms.task.entity.access;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description 用户属性
 * @author huangwenjie
 * @date 2018-01-13
 */
public class JsmsClientOperationStatistics {
    
    // 序号ID，自增长，主键
    private Integer id;
    // 客户ID，关联t_sms_account表中clientid字段
    private String clientId;
    // 短信类型，0：通知短信，4：验证码短信，5：营销短信，6：告警短信，7：USSD，8：闪信
    private Integer smstype;
    // 发送通道运营商类型0：全网1：移动2：联通3：电信4：国际
    private Integer operatorstype;
    // 提交条数（状态：1、3、6）
    private Integer submitTotal;
    // 明确成功条数（状态：3）
    private Integer reportsuccess;
    // 发送成功率，=（明确成功条数/提交条数）*100%
    private BigDecimal sendSuccessRatio;
    // 投诉个数
    private Integer complaintNumber;
    // 投诉率，单位：百万分之一，投诉个数/明确成功条数，
    private BigDecimal complaintRatio;
    // 投诉系数，单位：百万分之一
    private BigDecimal complaintCoefficient;
    // 投诉差异值，单位：百万分之一，=投诉系数-投诉率
    private BigDecimal complaintDifference;
    // 销售单价，单位：厘
    private BigDecimal salefee;
    // 归属销售，关联t_sms_user表中id字段
    private Long belongSale;
    // 统计时间，格式yyyyMM，如201801
    private Integer date;
    // 更新时间
    private Date updateTime;

    private Date updateTimeStart;
    private Date updateTimeEnd;

    public Date getUpdateTimeStart() {
        return updateTimeStart;
    }

    public void setUpdateTimeStart(Date updateTimeStart) {
        this.updateTimeStart = updateTimeStart;
    }

    public Date getUpdateTimeEnd() {
        return updateTimeEnd;
    }

    public void setUpdateTimeEnd(Date updateTimeEnd) {
        this.updateTimeEnd = updateTimeEnd;
    }

    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id ;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId ;
    }
    
    public Integer getSmstype() {
        return smstype;
    }
    
    public void setSmstype(Integer smstype) {
        this.smstype = smstype ;
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
    
    public BigDecimal getSalefee() {
        return salefee;
    }
    
    public void setSalefee(BigDecimal salefee) {
        this.salefee = salefee ;
    }
    
    public Long getBelongSale() {
        return belongSale;
    }
    
    public void setBelongSale(Long belongSale) {
        this.belongSale = belongSale ;
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