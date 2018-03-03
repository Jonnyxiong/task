package com.ucpaas.sms.task.entity.message;

import org.apache.ibatis.type.Alias;

import java.util.Date;

@Alias("ClientBalanceAlarm")
public class ClientBalanceAlarm {
    
    // 序号ID,自增长,主键
    private Integer id;
    // 用户账号,关联t_sms_account表中clientid字段
    private String clientid;
    // 接收告警手机号,多个手机号以","分隔
    private String alarmPhone;
    // 告警阀值,单位:条
    private Integer alarmNumber;
    // 告警可提醒次数
    private Integer reminderNumber;
    // 告警次数重置时间
    private Date resetTime;
    // 创建时间
    private Date createTime;
    // 更新时间
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
    
    public String getAlarmPhone() {
        return alarmPhone;
    }
    
    public void setAlarmPhone(String alarmPhone) {
        this.alarmPhone = alarmPhone ;
    }
    
    public Integer getAlarmNumber() {
        return alarmNumber;
    }
    
    public void setAlarmNumber(Integer alarmNumber) {
        this.alarmNumber = alarmNumber ;
    }
    
    public Integer getReminderNumber() {
        return reminderNumber;
    }
    
    public void setReminderNumber(Integer reminderNumber) {
        this.reminderNumber = reminderNumber ;
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