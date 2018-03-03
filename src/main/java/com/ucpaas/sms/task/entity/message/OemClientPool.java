package com.ucpaas.sms.task.entity.message;

import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.util.Date;

@Alias("OemClientPool")
public class OemClientPool {
    
    // 客户短信池id,自增长
    private Long clientPoolId;
    // 用户帐号
    private String clientId;
    // 产品类型，0：行业，1：营销，2：国际
    private Integer productType;
    // 到期时间
    private Date dueTime;
    // 状态，0：正常，1：停用
    private Integer status;
    // 普通短信累计总条数，单位：条
    private Integer totalNumber;
    // 普通短信剩余条数，单位：条
    private Integer remainNumber;
    // 普通短信单价，单位：元
    private BigDecimal unitPrice;
    // 国际短信累计总金额，单位：元
    private BigDecimal totalAmount;
    // 国际短信剩余金额，单位：元
    private BigDecimal remainAmount;
    // 赠送的测试短信剩余条数，单位：条

    private Integer remainTestNumber;
    // 更新时间
    private Date updateTime;
    // 备注
    private String remark;
    
    public Long getClientPoolId() {
        return clientPoolId;
    }
    
    public void setClientPoolId(Long clientPoolId) {
        this.clientPoolId = clientPoolId ;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId ;
    }
    
    public Integer getProductType() {
        return productType;
    }
    
    public void setProductType(Integer productType) {
        this.productType = productType ;
    }
    
    public Date getDueTime() {
        return dueTime;
    }
    
    public void setDueTime(Date dueTime) {
        this.dueTime = dueTime ;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status ;
    }
    
    public Integer getTotalNumber() {
        return totalNumber;
    }
    
    public void setTotalNumber(Integer totalNumber) {
        this.totalNumber = totalNumber ;
    }
    
    public Integer getRemainNumber() {
        return remainNumber;
    }
    
    public void setRemainNumber(Integer remainNumber) {
        this.remainNumber = remainNumber ;
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice ;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount ;
    }
    
    public BigDecimal getRemainAmount() {
        return remainAmount;
    }
    
    public void setRemainAmount(BigDecimal remainAmount) {
        this.remainAmount = remainAmount ;
    }
    
    public Integer getRemainTestNumber() {
        return remainTestNumber;
    }
    
    public void setRemainTestNumber(Integer remainTestNumber) {
        this.remainTestNumber = remainTestNumber ;
    }
    
    public Date getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime ;
    }
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark ;
    }

    @Override
    public String toString() {
        return "OemClientPool{" +
                "clientPoolId=" + clientPoolId +
                ", clientId='" + clientId + '\'' +
                ", productType=" + productType +
                ", dueTime=" + dueTime +
                ", status=" + status +
                ", totalNumber=" + totalNumber +
                ", remainNumber=" + remainNumber +
                ", unitPrice=" + unitPrice +
                ", totalAmount=" + totalAmount +
                ", remainAmount=" + remainAmount +
                ", remainTestNumber=" + remainTestNumber +
                ", updateTime=" + updateTime +
                ", remark='" + remark + '\'' +
                '}';
    }
}