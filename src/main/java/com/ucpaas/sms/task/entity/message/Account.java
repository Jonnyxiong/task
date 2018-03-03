package com.ucpaas.sms.task.entity.message;

import java.util.Date;

import org.apache.ibatis.type.Alias;

@Alias("Account")
public class Account {
    
    // 主键id,UUID
    private String id;
    // 用户帐号（子帐号）
    private String clientid;
    // 用户密码
    private String password;
    // 用户名称
    private String name;
    // 平台帐号
    private String sid;
    // 1：注册完成，5：冻结，6：注销，7：锁定
    private Integer status;
    // 代理商id（关联t_sms_agent_info表中agent_id字段）
    private Integer agentId;
    // 认证状态，2：待认证 ，3：证件已认证(正常)，4：认证不通过
    private Integer oauthStatus;
    // 认证时间
    private Date oauthDate;
    // 手机号码
    private String mobile;
    // 邮箱地址
    private String email;
    // 省
    private String province;
    // 市
    private String city;
    // 区县
    private String area;
    // 个人地址/公司地址
    private String address;
    // 个人姓名/公司名称
    private String realname;
    // 用户等级，1：普通客户（6－8位用户扩展），2：中小企业大型企业（4－5位用户扩展），3：大型企业（2－3位用户扩展）
    private Integer clientLevel;
    // 用户类型，1：个人用户，2：企业用户
    private Integer clientType;
    // 是否需要状态报告，0：不需要，1：需要简单状态报告，2：需要透传状态报告
    private Integer needreport;
    // 是否需要上行，0：不需要，1：需要，3：用户拉取上行
    private Integer needmo;
    // 是否需要审核，0：不需要，1：营销需要，2：全部需要，3：关键字审核
    private Integer needaudit;
    // 创建时间 
    private Date createtime;
    // 验证IP（可以有多个，用逗号分隔：192.168.0.*，*，192.168.0.0/16
    private String ip;
    // 状态报告回调地址
    private String deliveryurl;
    // 上行回调地址
    private String mourl;
    // 连接节点数
    private Integer nodenum;
    // 付费类型，0：预付费，1：后付费
    private Integer paytype;
    // 是否支持自扩展，0：不支持，1：支持
    private Integer needextend;
    // 是否支持签名对应签名端口，0：不支持，1：支持
    private Integer signextend;
    // 所属销售，关联t_sms_user表中id字段
    private Long belongSale;
    // 是否代理商自有用户帐号，0：否，1：是
    private Integer agentOwned;
    // 备注
    private String remarks;
    // 短信类型，0：通知短信，4：验证码短信，5：营销短信（适用于标准协议）
    private Integer smstype;
    // 客户接入使用协议类型，2为SMPP协议，3为CMPP协议，4为SGIP协议，5为SMGP协议，6为HTTPS协议
    private Integer smsfrom;
    // 是否超频计费，0：不需要，1：需要
    private Integer isoverratecharge;
    // 更新时间
    private Date updatetime;
    // 提供给客户的sp号
    private String spnum;
    // 拉取状态报告的最小时间间隔，单位秒 
    private Integer getreportInterval;
    // 单个请求拉取状态报告的最大条数 
    private Integer getreportMaxsize;
    // SGIP协议接入客户提供的上行IP
    private String moip;
    // SGIP协议接入客户提供的上行端口
    private String moport;
    // 提供给SGIP协议接入客户的节点编码
    private Long nodeid;
    // 标识，取值范围[0，9]，对应access流水表中的表名序号
    private Integer identify;
    // 客户接入速度，取值范围[1,10000]
    private Integer accessSpeed;
    // 通知回调地址，适用于http协议模板短信接入客户
    private String noticeurl;
    // 支持扩展位数
    private Integer extendSize;
    // 用户归属，0：阿里，1：代理商，2：云平台
    private Integer clientAscription;
    // 用户端口扩展类型
    private Integer extendtype;
    // 用户端口
    private String extendport;
    // 签名端口长度, 取值范围[0,5]
    private Integer signportlen;
    // 代理商类型
    private Integer agentType;

    private Integer groupId;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id ;
    }
    
    public String getClientid() {
        return clientid;
    }
    
    public void setClientid(String clientid) {
        this.clientid = clientid ;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password ;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name ;
    }
    
    public String getSid() {
        return sid;
    }
    
    public void setSid(String sid) {
        this.sid = sid ;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status ;
    }
    
    public Integer getAgentId() {
        return agentId;
    }
    
    public void setAgentId(Integer agentId) {
        this.agentId = agentId ;
    }
    
    public Integer getOauthStatus() {
        return oauthStatus;
    }
    
    public void setOauthStatus(Integer oauthStatus) {
        this.oauthStatus = oauthStatus ;
    }
    
    public Date getOauthDate() {
        return oauthDate;
    }
    
    public void setOauthDate(Date oauthDate) {
        this.oauthDate = oauthDate ;
    }
    
    public String getMobile() {
        return mobile;
    }
    
    public void setMobile(String mobile) {
        this.mobile = mobile ;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email ;
    }
    
    public String getProvince() {
        return province;
    }
    
    public void setProvince(String province) {
        this.province = province ;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city ;
    }
    
    public String getArea() {
        return area;
    }
    
    public void setArea(String area) {
        this.area = area ;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address ;
    }
    
    public String getRealname() {
        return realname;
    }
    
    public void setRealname(String realname) {
        this.realname = realname ;
    }
    
    public Integer getClientLevel() {
        return clientLevel;
    }
    
    public void setClientLevel(Integer clientLevel) {
        this.clientLevel = clientLevel ;
    }
    
    public Integer getClientType() {
        return clientType;
    }
    
    public void setClientType(Integer clientType) {
        this.clientType = clientType ;
    }
    
    public Integer getNeedreport() {
        return needreport;
    }
    
    public void setNeedreport(Integer needreport) {
        this.needreport = needreport ;
    }
    
    public Integer getNeedmo() {
        return needmo;
    }
    
    public void setNeedmo(Integer needmo) {
        this.needmo = needmo ;
    }
    
    public Integer getNeedaudit() {
        return needaudit;
    }
    
    public void setNeedaudit(Integer needaudit) {
        this.needaudit = needaudit ;
    }
    
    public Date getCreatetime() {
        return createtime;
    }
    
    public void setCreatetime(Date createtime) {
        this.createtime = createtime ;
    }
    
    public String getIp() {
        return ip;
    }
    
    public void setIp(String ip) {
        this.ip = ip ;
    }
    
    public String getDeliveryurl() {
        return deliveryurl;
    }
    
    public void setDeliveryurl(String deliveryurl) {
        this.deliveryurl = deliveryurl ;
    }
    
    public String getMourl() {
        return mourl;
    }
    
    public void setMourl(String mourl) {
        this.mourl = mourl ;
    }
    
    public Integer getNodenum() {
        return nodenum;
    }
    
    public void setNodenum(Integer nodenum) {
        this.nodenum = nodenum ;
    }
    
    public Integer getPaytype() {
        return paytype;
    }
    
    public void setPaytype(Integer paytype) {
        this.paytype = paytype ;
    }
    
    public Integer getNeedextend() {
        return needextend;
    }
    
    public void setNeedextend(Integer needextend) {
        this.needextend = needextend ;
    }
    
    public Integer getSignextend() {
        return signextend;
    }
    
    public void setSignextend(Integer signextend) {
        this.signextend = signextend ;
    }
    
    public Long getBelongSale() {
        return belongSale;
    }
    
    public void setBelongSale(Long belongSale) {
        this.belongSale = belongSale ;
    }
    
    public Integer getAgentOwned() {
        return agentOwned;
    }
    
    public void setAgentOwned(Integer agentOwned) {
        this.agentOwned = agentOwned ;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks ;
    }
    
    public Integer getSmstype() {
        return smstype;
    }
    
    public void setSmstype(Integer smstype) {
        this.smstype = smstype ;
    }
    
    public Integer getSmsfrom() {
        return smsfrom;
    }
    
    public void setSmsfrom(Integer smsfrom) {
        this.smsfrom = smsfrom ;
    }
    
    public Integer getIsoverratecharge() {
        return isoverratecharge;
    }
    
    public void setIsoverratecharge(Integer isoverratecharge) {
        this.isoverratecharge = isoverratecharge ;
    }
    
    public Date getUpdatetime() {
        return updatetime;
    }
    
    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime ;
    }
    
    public String getSpnum() {
        return spnum;
    }
    
    public void setSpnum(String spnum) {
        this.spnum = spnum ;
    }
    
    public Integer getGetreportInterval() {
        return getreportInterval;
    }
    
    public void setGetreportInterval(Integer getreportInterval) {
        this.getreportInterval = getreportInterval ;
    }
    
    public Integer getGetreportMaxsize() {
        return getreportMaxsize;
    }
    
    public void setGetreportMaxsize(Integer getreportMaxsize) {
        this.getreportMaxsize = getreportMaxsize ;
    }
    
    public String getMoip() {
        return moip;
    }
    
    public void setMoip(String moip) {
        this.moip = moip ;
    }
    
    public String getMoport() {
        return moport;
    }
    
    public void setMoport(String moport) {
        this.moport = moport ;
    }
    
    public Long getNodeid() {
        return nodeid;
    }
    
    public void setNodeid(Long nodeid) {
        this.nodeid = nodeid ;
    }
    
    public Integer getIdentify() {
        return identify;
    }
    
    public void setIdentify(Integer identify) {
        this.identify = identify ;
    }
    
    public Integer getAccessSpeed() {
        return accessSpeed;
    }
    
    public void setAccessSpeed(Integer accessSpeed) {
        this.accessSpeed = accessSpeed ;
    }
    
    public String getNoticeurl() {
        return noticeurl;
    }
    
    public void setNoticeurl(String noticeurl) {
        this.noticeurl = noticeurl ;
    }
    
    public Integer getExtendSize() {
        return extendSize;
    }
    
    public void setExtendSize(Integer extendSize) {
        this.extendSize = extendSize ;
    }
    
    public Integer getClientAscription() {
        return clientAscription;
    }
    
    public void setClientAscription(Integer clientAscription) {
        this.clientAscription = clientAscription ;
    }
    
    public Integer getExtendtype() {
        return extendtype;
    }
    
    public void setExtendtype(Integer extendtype) {
        this.extendtype = extendtype ;
    }
    
    public String getExtendport() {
        return extendport;
    }
    
    public void setExtendport(String extendport) {
        this.extendport = extendport ;
    }
    
    public Integer getSignportlen() {
        return signportlen;
    }
    
    public void setSignportlen(Integer signportlen) {
        this.signportlen = signportlen ;
    }

    public Integer getAgentType() {
        return agentType;
    }

    public void setAgentType(Integer agentType) {
        this.agentType = agentType;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }
}