package com.ucpaas.sms.task.entity.message;

import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.util.Date;

@Alias("ClientOrder")
public class ClientOrder {
    
    // 订单子id，唯一id,递增 
    private Long subId;
    // 订单id号，15位规则：YYMMDDHHMM（年月日时分，10位）+ 类型（1位）+ 序号（4位，0000－9999），其中：客户下订单时，类型为0，品牌代理下订单时，类型为1，OEM代理下订单时，类型为2
    private Long orderId;
    // 用户帐号
    private String clientId;
    // 代理商id
    private Integer agentId;
    // 产品id号
    private Integer productId;
    // 产品类型，0：行业，1：营销，2：国际，7：USSD，8：闪信，9：挂机短信，其中0和1为普通短信，2为国际短信
    private Integer productType;
    // 产品名称
    private String productName;
    // 购买件数
    private Integer productNumber;
    // 订单类型，0：客户购买，1：代理商代买
    private Integer orderType;
    // 状态， 0：待审核，1：订单生效，2：订单完成（订单中数量已用完），3：订单失败（代理商余额不足）,4：订单取消（客户取消订单）
    private Integer status;
    // 有效期，单位为天，0为无限期
    private Integer activePeriod;
    // 数量，普通短信：条，国际短信：元
    private BigDecimal quantity;
    // 剩余数量，普通短信：条，国际短信：元
    private BigDecimal remainQuantity;
    // 产品总销售价，针对客户  普通短信：元= 产品定价*客户销售折扣率*购买件数，国际短信：折扣率,产品销售价=产品定价*客户销售折扣率
    private BigDecimal salePrice;
    // 产品总成本（元），针对代理商 ,普通短信：元，= 产品成本*购买件数,国际短信：折扣率，= 产品成本
    private BigDecimal productCost;
    // 到期时间,到期时间=生效时间+有效期，有效期为0时，使用默认值
    private Date endTime;
    // 创建时间
    private Date createTime;
    // 生效时间
    private Date effectiveTime;
    // 更新时间
    private Date updateTime;
    // 审核类型，0：代理商余额账户支付，1：代理商待结算账户支付
    private Integer auditType;
    // 审核人，1：代理商，2：运营
    private Integer auditor;
    // 审核ID（管理员id）（关联t_sms_user表中id字段）

    private Long adminId;
    
    public Long getSubId() {
        return subId;
    }
    
    public void setSubId(Long subId) {
        this.subId = subId ;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId ;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId ;
    }
    
    public Integer getAgentId() {
        return agentId;
    }
    
    public void setAgentId(Integer agentId) {
        this.agentId = agentId ;
    }
    
    public Integer getProductId() {
        return productId;
    }
    
    public void setProductId(Integer productId) {
        this.productId = productId ;
    }
    
    public Integer getProductType() {
        return productType;
    }
    
    public void setProductType(Integer productType) {
        this.productType = productType ;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName ;
    }
    
    public Integer getProductNumber() {
        return productNumber;
    }
    
    public void setProductNumber(Integer productNumber) {
        this.productNumber = productNumber ;
    }
    
    public Integer getOrderType() {
        return orderType;
    }
    
    public void setOrderType(Integer orderType) {
        this.orderType = orderType ;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status ;
    }
    
    public Integer getActivePeriod() {
        return activePeriod;
    }
    
    public void setActivePeriod(Integer activePeriod) {
        this.activePeriod = activePeriod ;
    }
    
    public BigDecimal getQuantity() {
        return quantity;
    }
    
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity ;
    }
    
    public BigDecimal getRemainQuantity() {
        return remainQuantity;
    }
    
    public void setRemainQuantity(BigDecimal remainQuantity) {
        this.remainQuantity = remainQuantity ;
    }
    
    public BigDecimal getSalePrice() {
        return salePrice;
    }
    
    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice ;
    }
    
    public BigDecimal getProductCost() {
        return productCost;
    }
    
    public void setProductCost(BigDecimal productCost) {
        this.productCost = productCost ;
    }
    
    public Date getEndTime() {
        return endTime;
    }
    
    public void setEndTime(Date endTime) {
        this.endTime = endTime ;
    }
    
    public Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime ;
    }
    
    public Date getEffectiveTime() {
        return effectiveTime;
    }
    
    public void setEffectiveTime(Date effectiveTime) {
        this.effectiveTime = effectiveTime ;
    }
    
    public Date getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime ;
    }
    
    public Integer getAuditType() {
        return auditType;
    }
    
    public void setAuditType(Integer auditType) {
        this.auditType = auditType ;
    }
    
    public Integer getAuditor() {
        return auditor;
    }
    
    public void setAuditor(Integer auditor) {
        this.auditor = auditor ;
    }
    
    public Long getAdminId() {
        return adminId;
    }
    
    public void setAdminId(Long adminId) {
        this.adminId = adminId ;
    }

    @Override
    public String toString() {
        return "ClientOrder{" +
                "subId=" + subId +
                ", orderId=" + orderId +
                ", clientId='" + clientId + '\'' +
                ", agentId=" + agentId +
                ", productId=" + productId +
                ", productType=" + productType +
                ", productName='" + productName + '\'' +
                ", productNumber=" + productNumber +
                ", orderType=" + orderType +
                ", status=" + status +
                ", activePeriod=" + activePeriod +
                ", quantity=" + quantity +
                ", remainQuantity=" + remainQuantity +
                ", salePrice=" + salePrice +
                ", productCost=" + productCost +
                ", endTime=" + endTime +
                ", createTime=" + createTime +
                ", effectiveTime=" + effectiveTime +
                ", updateTime=" + updateTime +
                ", auditType=" + auditType +
                ", auditor=" + auditor +
                ", adminId=" + adminId +
                '}';
    }
}