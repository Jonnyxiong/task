package com.ucpaas.sms.task.entity.message;

import java.util.Date;
 

/**
 * @description 客户组信息管理
 * @author 黄文杰
 * @date 2017-07-27
 */ 
public class AccountGroup {
    
    // 客户组id，主键，自增长
    private Integer accountGid;
    // 客户组名称，唯一
    private String accountGname;
    // 客户类型，0：代理商子客户，1：直客
    private Integer type;
    // 状态，0：关闭，1：正常
    private Integer state;
    // 备注
    private String remarks;
    // 创建者ID，关联t_sms_user表中id字段
    private Long createId;
    // 创建时间
    private Date createTime;
    // 修改时间
    private Date updateTime;
    
    public Integer getAccountGid() {
        return accountGid;
    }
    
    public void setAccountGid(Integer accountGid) {
        this.accountGid = accountGid ;
    }
    
    public String getAccountGname() {
        return accountGname;
    }
    
    public void setAccountGname(String accountGname) {
        this.accountGname = accountGname ;
    }
    
    public Integer getType() {
        return type;
    }
    
    public void setType(Integer type) {
        this.type = type ;
    }
    
    public Integer getState() {
        return state;
    }
    
    public void setState(Integer state) {
        this.state = state ;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks ;
    }
    
    public Long getCreateId() {
        return createId;
    }
    
    public void setCreateId(Long createId) {
        this.createId = createId ;
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