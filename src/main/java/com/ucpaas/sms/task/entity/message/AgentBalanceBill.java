package com.ucpaas.sms.task.entity.message;

import java.math.BigDecimal;
import java.util.Date;
 

/**
 * @description 代理商帐户余额收支明细管理
 * @author 黄文杰
 * @date 2017-07-27
 */ 
public class AgentBalanceBill {
    
    // 业务单号,自增序列
    private Integer id;
    // 代理商id
    private Integer agentId;
    // 业务类型，0：充值，1：扣减，2：佣金转余额，3：购买产品包，4：退款，5：赠送
    private Integer paymentType;
    // 财务类型，0：入账，1：出账
    private String financialType;
    // 金额
    private BigDecimal amount;
    // 余额剩余，单位：元
    private BigDecimal balance;
    // 生成时间
    private Date createTime;
    // 订单id
    private Long orderId;
    // 操作管理员id
    private Long adminId;
    // 用户账号：payment_type为3时记录订单的用户账号
    private String clientId;
    // 备注
    private String remark;
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id ;
    }
    
    public Integer getAgentId() {
        return agentId;
    }
    
    public void setAgentId(Integer agentId) {
        this.agentId = agentId ;
    }
    
    public Integer getPaymentType() {
        return paymentType;
    }
    
    public void setPaymentType(Integer paymentType) {
        this.paymentType = paymentType ;
    }
    
    public String getFinancialType() {
        return financialType;
    }
    
    public void setFinancialType(String financialType) {
        this.financialType = financialType ;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount ;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance ;
    }
    
    public Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime ;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId ;
    }
    
    public Long getAdminId() {
        return adminId;
    }
    
    public void setAdminId(Long adminId) {
        this.adminId = adminId ;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId ;
    }
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark ;
    }
    
}