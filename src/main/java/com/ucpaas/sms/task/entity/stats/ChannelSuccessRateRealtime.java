package com.ucpaas.sms.task.entity.stats;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.ibatis.type.Alias;

@Alias("ChannelSuccessRateRealtime")
public class ChannelSuccessRateRealtime {
    
    // 
    private Long id;
    // 通道id
    private String channelId;
    // 通道名称
    private String channelName;
    // 标识
    private String iden;
    // 发送总数 1 2 3 5 6
    private int sendTotal;
    // 明确成功 3
    private int successTotal;
    // 提交失败 4
    private int submitFail;
    // 发送失败 5 6
    private int sendFail;
    // 成功待定 1
    private int undetermined1;
    // 成功待定 2
    private int undetermined2;
    // 未发送 0
    private int nosend;
    // 成功率
    private BigDecimal successRate;
    // 未知率
    private BigDecimal fakeSuccessRate;
    // 失败率
    private BigDecimal reallyFailRate;
    // 数据时间
    private Date dataTime;
    // 数据采集时间
    private Date createTime;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id ;
    }
    
    public String getChannelId() {
        return channelId;
    }
    
    public void setChannelId(String channelId) {
        this.channelId = channelId ;
    }
    
    public String getChannelName() {
        return channelName;
    }
    
    public void setChannelName(String channelName) {
        this.channelName = channelName ;
    }
    
    public String getIden() {
        return iden;
    }
    
    public void setIden(String iden) {
        this.iden = iden ;
    }
    
    public Integer getSendTotal() {
        return sendTotal;
    }
    
    public void setSendTotal(Integer sendTotal) {
        this.sendTotal = sendTotal ;
    }
    
    public Integer getSuccessTotal() {
        return successTotal;
    }
    
    public void setSuccessTotal(Integer successTotal) {
        this.successTotal = successTotal ;
    }
    
    public Integer getSubmitFail() {
        return submitFail;
    }
    
    public void setSubmitFail(Integer submitFail) {
        this.submitFail = submitFail ;
    }
    
    public Integer getSendFail() {
        return sendFail;
    }
    
    public void setSendFail(Integer sendFail) {
        this.sendFail = sendFail ;
    }
    
    public Integer getUndetermined1() {
        return undetermined1;
    }
    
    public void setUndetermined1(Integer undetermined1) {
        this.undetermined1 = undetermined1 ;
    }
    
    public Integer getUndetermined2() {
        return undetermined2;
    }
    
    public void setUndetermined2(Integer undetermined2) {
        this.undetermined2 = undetermined2 ;
    }
    
    public Integer getNosend() {
        return nosend;
    }
    
    public void setNosend(Integer nosend) {
        this.nosend = nosend ;
    }
    
    public BigDecimal getSuccessRate() {
        return successRate;
    }
    
    public void setSuccessRate(BigDecimal successRate) {
        this.successRate = successRate ;
    }
    
    public BigDecimal getFakeSuccessRate() {
        return fakeSuccessRate;
    }
    
    public void setFakeSuccessRate(BigDecimal fakeSuccessRate) {
        this.fakeSuccessRate = fakeSuccessRate ;
    }
    
    public BigDecimal getReallyFailRate() {
        return reallyFailRate;
    }
    
    public void setReallyFailRate(BigDecimal reallyFailRate) {
        this.reallyFailRate = reallyFailRate ;
    }
    
    public Date getDataTime() {
        return dataTime;
    }
    
    public void setDataTime(Date dataTime) {
        this.dataTime = dataTime ;
    }
    
    public Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime ;
    }
    
}