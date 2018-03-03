package com.ucpaas.sms.task.entity.onlinepay;

import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.util.Date;

@Alias("JsmsOnlinePaymentTask")
public class JsmsOnlinePayment extends com.jsmsframework.finance.entity.JsmsOnlinePayment {
    private Date submitTimeStart;

    public void setSubmitTimeStart(Date submitTimeStart) {
        this.submitTimeStart = submitTimeStart;
    }

    public Date getSubmitTimeStart() {
        return submitTimeStart;
    }
}