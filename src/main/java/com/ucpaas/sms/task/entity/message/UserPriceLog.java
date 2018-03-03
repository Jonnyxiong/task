package com.ucpaas.sms.task.entity.message;

import java.util.Date;
import java.math.BigDecimal;
 

/**
 * @description 后付费用户价格变更记录管理
 * @author 黄文杰
 * @date 2017-07-27
 */ 
public class UserPriceLog {
    
    // 序号ID，自增长，主键
    private Integer id;
    // 用户帐号，关联t_sms_account表中clientid字段
    private String clientid;
    // 短信类型，0：通知短信，4：验证码短信，5：营销短信，6：告警短信，7：USSD，8：闪信
    private Integer smstype;
    // 生效日期
    private Date effectDate;
    // 计费模式，0：提交成功计费（用户侧：1+3+6）1：发送成功计费（用户侧：1+3）2：明确成功计费（用户侧：3）
    private Integer chargeMode;
    // 短信单价，单位：元/条
    private BigDecimal userPrice;
    // 创建时间
    private Date createTime;
    // 修改时间
    private Date updateTime;
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id ;
    }
    
    public String getClientid() {
        return clientid;
    }
    
    public void setClientid(String clientid) {
        this.clientid = clientid ;
    }
    
    public Integer getSmstype() {
        return smstype;
    }
    
    public void setSmstype(Integer smstype) {
        this.smstype = smstype ;
    }
    
    public Date getEffectDate() {
        return effectDate;
    }
    
    public void setEffectDate(Date effectDate) {
        this.effectDate = effectDate ;
    }
    
    public Integer getChargeMode() {
        return chargeMode;
    }
    
    public void setChargeMode(Integer chargeMode) {
        this.chargeMode = chargeMode ;
    }
    
    public BigDecimal getUserPrice() {
        return userPrice;
    }
    
    public void setUserPrice(BigDecimal userPrice) {
        this.userPrice = userPrice ;
    }
    
    public Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime ;
    }
    
    public Date getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime ;
    }
    
}