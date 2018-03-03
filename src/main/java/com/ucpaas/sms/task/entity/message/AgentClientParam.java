package com.ucpaas.sms.task.entity.message;

import org.apache.ibatis.type.Alias;

import java.util.Date;

@Alias("AgentClientParam")
public class AgentClientParam {
    
    // 序号，自增长
    private Long paramId;
    // 参数键值
    private String paramKey;
    // 参数值
    private String paramValue;
    // 描述
    private String description;
    // 创建时间
    private Date createDate;
    // 更新时间
    private Date updateDate;
    // 是否有效1有效,2无效
    private Byte paramStatus;
    
    public Long getParamId() {
        return paramId;
    }
    
    public void setParamId(Long paramId) {
        this.paramId = paramId ;
    }
    
    public String getParamKey() {
        return paramKey;
    }
    
    public void setParamKey(String paramKey) {
        this.paramKey = paramKey ;
    }
    
    public String getParamValue() {
        return paramValue;
    }
    
    public void setParamValue(String paramValue) {
        this.paramValue = paramValue ;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description ;
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
    
    public Byte getParamStatus() {
        return paramStatus;
    }
    
    public void setParamStatus(Byte paramStatus) {
        this.paramStatus = paramStatus ;
    }
    
}