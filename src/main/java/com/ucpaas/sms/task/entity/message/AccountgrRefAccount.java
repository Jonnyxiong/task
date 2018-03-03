package com.ucpaas.sms.task.entity.message;

import java.util.Date;
 

/**
 * @description 客户组客户关联管理
 * @author 黄文杰
 * @date 2017-07-27
 */ 
public class AccountgrRefAccount {
    
    // 序号ID，主键，自增长
    private Integer id;
    // 客户组id，关联t_sms_account_group表中account_gid字段
    private Integer accountGid;
    // 用户帐号（子帐号），关联t_sms_account表中clientid字段
    private String clientid;
    // 修改时间
    private Date updateTime;
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id ;
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
    
    public Date getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime ;
    }
    
}