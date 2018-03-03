package com.ucpaas.sms.task.entity.message;

import java.util.Date;
 

/**
 * @description 用户管理
 * @author 黄文杰
 * @date 2017-07-27
 */ 
public class User {
    
    // 
    private Long id;
    // 主账号sid
    private String sid;
    // 昵称(保留)
    private String username;
    // 邮件
    private String email;
    // 密码
    private String password;
    // 用户类型 1:系统管理员，保留字段 配置t_sms_dict.param_type=user_type
    private String userType;
    // 用户状态：1:正常, 2:锁定 3:关闭，配置t_sms_dict.param_type=user_status
    private String status;
    // 手机
    private String mobile;
    // 真实姓名
    private String realname;
    // 
    private Date createDate;
    // 
    private Date updateDate;
    // 
    private Integer loginTimes;
    // web应用ID：1短信调度系统 2代理商平台 3运营平台 4OEM代理商平台
    private Integer webId;
    // 部门id，适用于运营平台
    private Integer departmentId;
    // 数据权限，0：仅看自己的数据，1：所在部门及下级部门，适用于运营平台
    private Integer dataAuthority;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id ;
    }
    
    public String getSid() {
        return sid;
    }
    
    public void setSid(String sid) {
        this.sid = sid ;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username ;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email ;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password ;
    }
    
    public String getUserType() {
        return userType;
    }
    
    public void setUserType(String userType) {
        this.userType = userType ;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status ;
    }
    
    public String getMobile() {
        return mobile;
    }
    
    public void setMobile(String mobile) {
        this.mobile = mobile ;
    }
    
    public String getRealname() {
        return realname;
    }
    
    public void setRealname(String realname) {
        this.realname = realname ;
    }
    
    public Date getCreateDate() {
        return createDate;
    }
    
    public void setCreateDate(Date createDate) {
        this.createDate = createDate ;
    }
    
    public Date getUpdateDate() {
        return updateDate;
    }
    
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate ;
    }
    
    public Integer getLoginTimes() {
        return loginTimes;
    }
    
    public void setLoginTimes(Integer loginTimes) {
        this.loginTimes = loginTimes ;
    }
    
    public Integer getWebId() {
        return webId;
    }
    
    public void setWebId(Integer webId) {
        this.webId = webId ;
    }
    
    public Integer getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId ;
    }
    
    public Integer getDataAuthority() {
        return dataAuthority;
    }
    
    public void setDataAuthority(Integer dataAuthority) {
        this.dataAuthority = dataAuthority ;
    }
    
}