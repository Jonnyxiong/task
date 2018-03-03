package com.ucpaas.sms.task.entity.access;

import java.math.BigDecimal;
import java.util.Date;
 

/**
 * @description 回款金额统计表
 * @author 黄文杰
 * @date 2017-07-25
 */ 
public class SmsBackPaymenstat {
    
    // 序号id, 自增长
    private Integer id;
    // 代理商预付充值金额, 单位:元
    private BigDecimal prepayRecharge;
    // 直客消耗金额, 单位:元
    private BigDecimal directConsume;
    // 统计类型, 0:每日, 2: 每月
    private Integer stattype;
    // 统计的数据时间(天)
    private Integer date;
    // 创建时间, 信息保存时间
    private Date createtime;
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id ;
    }
    
    public BigDecimal getPrepayRecharge() {
        return prepayRecharge;
    }
    
    public void setPrepayRecharge(BigDecimal prepayRecharge) {
        this.prepayRecharge = prepayRecharge ;
    }
    
    public BigDecimal getDirectConsume() {
        return directConsume;
    }
    
    public void setDirectConsume(BigDecimal directConsume) {
        this.directConsume = directConsume ;
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