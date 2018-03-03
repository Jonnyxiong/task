package com.ucpaas.sms.task.model.halfhour;

import com.ucpaas.sms.task.entity.stats.ChannelSuccessRateRealtime;

import java.math.BigDecimal;

public class RecordAllHeJi extends ChannelSuccessRateRealtime{
    private int beSendTotal;

    private int beSuccessTotal;

    private int bThirtyTotal;

    private int bThirtySuccessTotal;
    private BigDecimal bSuccessRate;

    public int getBeSuccessTotal() {
        return beSuccessTotal;
    }

    public void setBeSuccessTotal(int beSuccessTotal) {
        this.beSuccessTotal = beSuccessTotal;
    }

    public int getBeSendTotal() {
        return beSendTotal;
    }

    public void setBeSendTotal(int beSendTotal) {
        this.beSendTotal = beSendTotal;
    }

    public int getbThirtyTotal() {
        return bThirtyTotal;
    }

    public void setbThirtyTotal(int bThirtyTotal) {
        this.bThirtyTotal = bThirtyTotal;
    }

    public BigDecimal getbSuccessRate() {
        return bSuccessRate;
    }

    public void setbSuccessRate(BigDecimal bSuccessRate) {
        this.bSuccessRate = bSuccessRate;
    }

    public int getbThirtySuccessTotal() {
        return bThirtySuccessTotal;
    }

    public void setbThirtySuccessTotal(int bThirtySuccessTotal) {
        this.bThirtySuccessTotal = bThirtySuccessTotal;
    }
}
