package com.ucpaas.sms.task.enum4sms;/**
 * Created by Dylan on 2017/3/14.
 */

/**
 * 参数
 * @author : Niu.T
 * @date: 2017年03月14 17:35
 */
public enum PropertiesParams {
    /** 阿里客户信息 */
    ALICLIENTINFO("AliClientInfo"),
    /** 客户id */
    CLIENTIDS("clientids"),
    /** 单价 */
    UNITPRICE("unitPrice"),
    /** 重要客户的预付费信息 */
    MAJORCLIENTSPREPAYMENTINFO("majorClientsPrepaymentInfo"),
    /** 重要客户的后付费信息 */
    MAJORCLIENTSPOSTPAIDINFO("majorClientsPostpaidInfo")
    ;
    private String type;

    PropertiesParams(String type){
        this.type = type;
    }

    public String getType() {
        return type;
    }
    
    @Override
    public String toString() {
    	return type;
    }
    
}
