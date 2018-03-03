package com.ucpaas.sms.task.entity.message;

import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.util.Date;

@Alias("Channel")
public class Channel {
    
    // 自增加的主键
    private Integer id;
    // 通道id
    private Integer cid;
    // 通道账号
    private String clientid;
    // 密码
    private String password;
    // 通道名称
    private String channelname;
    // 通道说明
    private String remark;
    // 运营商类型：0：全网1：移动2：联通3：电信4：国际
    private Integer operatorstype;
    // 行业类型，0：行业，1：营销
    private Integer industrytype;
    // 通道是否支持长短信，0：不支持，1：支持
    private Byte longsms;
    // WAP PUSH 是否支持wappush，0：不支持 1：支持
    private Boolean wappush;
    // 请求方式，1：http get2：http post3：移动短信协议4：电信短信协议5：联通短信协议6：国际短信协议
    private Byte httpmode;
    // url
    private String url;
    // 编码格式
    private String coding;
    // 
    private String postdata;
    // 剩余金额
    private BigDecimal balance;
    // 花费金额
    private BigDecimal expenditure;
    // 成本价
    private BigDecimal costprice;
    // 查询状态地址
    private String querystateurl;
    // 
    private String querystatepostdata;
    // 查询上行地址
    private String queryupurl;
    // 查询上行POST数据
    private String queryuppostdata;
    // 显示号码
    private Integer shownum;
    // 显示签名
    private String showsign;
    // 显示签名的方式，0：不限制前后，1：前置，2：后置，3：不带签名 配置t_sms_dict.param_type=showsigntype
    private Byte showsigntype;
    // 接入号
    private String accessid;
    // 节点编码
    private Long node;
    // 企业代码
    private String spid;
    // 通道状态，0：关闭，1：开启，2：暂停说明：关闭－路由不再选择此通道，已路由到此通道的数据需要做异常处理；开启－可路由到此通道，通道数据正常发送；暂停－重启通道时使用，暂时不路由此通道，已路由到此通道的数据保留，通道关闭
    private Integer state;
    // 创建时间
    private Date createtime;
    // 修改时间
    private Date updatetime;
    // 成功率预警值
    private Integer warnSuccRate;
    // 上行端口
    private String serviceid;
    // 通道挂载的smsp_send组件id
    private Integer sendid;
    // 通道发送速度，取值范围(0，1000]
    private Integer speed;
    // 上行端口
    private Integer moport;
    // smsp提供给运营商IP（上行）
    private String moip;
    // 通道类型，0：自签平台用户端口，1：固签无自扩展，2：固签有自扩展，3：自签通道用户端口
    private Integer channeltype;
    // 是否白名单通道，0：否，1：是
    private Integer iswhitelist;
    // 是否支持170号段，0：不支持，1：支持
    private Byte supportOhas;
    // 通道支持的自扩展位数
    private Integer extendsize;
    // 及时率预警值
    private Integer warnTimeRate;
    // 发送号码前是否需要加86，0：否，1：是
    private Integer needprefix;
    // 通道的发送时间区间，如：08:00:00|20:00:00.  时间区域用|分隔， 精确到秒
    private String sendtimearea;
    // 标识;调度系统新增通道时取值范围在（系统参数－通道标识范围）中配置，对应record流水表中的表名序号
    private Integer identify;
    // 通道发单缓存队列告警阈值， 取值范围[1, 1000*1000*1000]
    private Integer maxqueuesize;
    // 滑动窗口，取值范围[0,10000]
    private Integer sliderwindow;
    // 通道所属类型：1->自有，2->直连，3->第三方
    private Integer ownerType;
    // 选通道时需要用到的speed
    private Integer accessSpeed;
    // 认证地址
    private String oauthUrl;
    // 认证POST数据
    private String oauthPostData;
    // 增加模板地址
    private String addTempUrl;
    // 增加模板POST数据
    private String addTempPostData;
    // 查询模板审核状态地址
    private String getTempListUrl;
    // 查询模板审核状态POST数据
    private String getTempListPostData;
    // 单条内容发送长度（包括签名），目前仅适用于发送USSD和闪信的通道
    private Integer contentlen;
    // mq_id用于关联通道与MQ队列
    private Integer mqId;
    // 通道支持的号段类型，0：全国，1：省网
    private Integer segcodeType;
    // 归属商务，关联t_sms_user表中id字段
    private Long belongBusiness;

    
	public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id ;
    }
    
    public Integer getCid() {
        return cid;
    }
    
    public void setCid(Integer cid) {
        this.cid = cid ;
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
    
    public String getChannelname() {
        return channelname;
    }
    
    public void setChannelname(String channelname) {
        this.channelname = channelname ;
    }
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark ;
    }
    
    public Integer getOperatorstype() {
        return operatorstype;
    }
    
    public void setOperatorstype(Integer operatorstype) {
        this.operatorstype = operatorstype ;
    }
    
    public Integer getIndustrytype() {
        return industrytype;
    }
    
    public void setIndustrytype(Integer industrytype) {
        this.industrytype = industrytype ;
    }
    
    public Byte getLongsms() {
        return longsms;
    }
    
    public void setLongsms(Byte longsms) {
        this.longsms = longsms ;
    }
    
    public Boolean getWappush() {
        return wappush;
    }
    
    public void setWappush(Boolean wappush) {
        this.wappush = wappush ;
    }
    
    public Byte getHttpmode() {
        return httpmode;
    }
    
    public void setHttpmode(Byte httpmode) {
        this.httpmode = httpmode ;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url ;
    }
    
    public String getCoding() {
        return coding;
    }
    
    public void setCoding(String coding) {
        this.coding = coding ;
    }
    
    public String getPostdata() {
        return postdata;
    }
    
    public void setPostdata(String postdata) {
        this.postdata = postdata ;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance ;
    }
    
    public BigDecimal getExpenditure() {
        return expenditure;
    }
    
    public void setExpenditure(BigDecimal expenditure) {
        this.expenditure = expenditure ;
    }
    
    public BigDecimal getCostprice() {
        return costprice;
    }
    
    public void setCostprice(BigDecimal costprice) {
        this.costprice = costprice ;
    }
    
    public String getQuerystateurl() {
        return querystateurl;
    }
    
    public void setQuerystateurl(String querystateurl) {
        this.querystateurl = querystateurl ;
    }
    
    public String getQuerystatepostdata() {
        return querystatepostdata;
    }
    
    public void setQuerystatepostdata(String querystatepostdata) {
        this.querystatepostdata = querystatepostdata ;
    }
    
    public String getQueryupurl() {
        return queryupurl;
    }
    
    public void setQueryupurl(String queryupurl) {
        this.queryupurl = queryupurl ;
    }
    
    public String getQueryuppostdata() {
        return queryuppostdata;
    }
    
    public void setQueryuppostdata(String queryuppostdata) {
        this.queryuppostdata = queryuppostdata ;
    }
    
    public Integer getShownum() {
        return shownum;
    }
    
    public void setShownum(Integer shownum) {
        this.shownum = shownum ;
    }
    
    public String getShowsign() {
        return showsign;
    }
    
    public void setShowsign(String showsign) {
        this.showsign = showsign ;
    }
    
    public Byte getShowsigntype() {
        return showsigntype;
    }
    
    public void setShowsigntype(Byte showsigntype) {
        this.showsigntype = showsigntype ;
    }
    
    public String getAccessid() {
        return accessid;
    }
    
    public void setAccessid(String accessid) {
        this.accessid = accessid ;
    }
    
    public Long getNode() {
        return node;
    }
    
    public void setNode(Long node) {
        this.node = node ;
    }
    
    public String getSpid() {
        return spid;
    }
    
    public void setSpid(String spid) {
        this.spid = spid ;
    }
    
    public Integer getState() {
        return state;
    }
    
    public void setState(Integer state) {
        this.state = state ;
    }
    
    public Date getCreatetime() {
        return createtime;
    }
    
    public void setCreatetime(Date createtime) {
        this.createtime = createtime ;
    }
    
    public Date getUpdatetime() {
        return updatetime;
    }
    
    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime ;
    }
    
    public Integer getWarnSuccRate() {
        return warnSuccRate;
    }
    
    public void setWarnSuccRate(Integer warnSuccRate) {
        this.warnSuccRate = warnSuccRate ;
    }
    
    public String getServiceid() {
        return serviceid;
    }
    
    public void setServiceid(String serviceid) {
        this.serviceid = serviceid ;
    }
    
    public Integer getSendid() {
        return sendid;
    }
    
    public void setSendid(Integer sendid) {
        this.sendid = sendid ;
    }
    
    public Integer getSpeed() {
        return speed;
    }
    
    public void setSpeed(Integer speed) {
        this.speed = speed ;
    }
    
    public Integer getMoport() {
        return moport;
    }
    
    public void setMoport(Integer moport) {
        this.moport = moport ;
    }
    
    public String getMoip() {
        return moip;
    }
    
    public void setMoip(String moip) {
        this.moip = moip ;
    }
    
    public Integer getChanneltype() {
        return channeltype;
    }
    
    public void setChanneltype(Integer channeltype) {
        this.channeltype = channeltype ;
    }
    
    public Integer getIswhitelist() {
        return iswhitelist;
    }
    
    public void setIswhitelist(Integer iswhitelist) {
        this.iswhitelist = iswhitelist ;
    }
    
    public Byte getSupportOhas() {
        return supportOhas;
    }
    
    public void setSupportOhas(Byte supportOhas) {
        this.supportOhas = supportOhas ;
    }
    
    public Integer getExtendsize() {
        return extendsize;
    }
    
    public void setExtendsize(Integer extendsize) {
        this.extendsize = extendsize ;
    }
    
    public Integer getWarnTimeRate() {
        return warnTimeRate;
    }
    
    public void setWarnTimeRate(Integer warnTimeRate) {
        this.warnTimeRate = warnTimeRate ;
    }
    
    public Integer getNeedprefix() {
        return needprefix;
    }
    
    public void setNeedprefix(Integer needprefix) {
        this.needprefix = needprefix ;
    }
    
    public String getSendtimearea() {
        return sendtimearea;
    }
    
    public void setSendtimearea(String sendtimearea) {
        this.sendtimearea = sendtimearea ;
    }
    
    public Integer getIdentify() {
        return identify;
    }
    
    public void setIdentify(Integer identify) {
        this.identify = identify ;
    }
    
    public Integer getMaxqueuesize() {
        return maxqueuesize;
    }
    
    public void setMaxqueuesize(Integer maxqueuesize) {
        this.maxqueuesize = maxqueuesize ;
    }
    
    public Integer getSliderwindow() {
        return sliderwindow;
    }
    
    public void setSliderwindow(Integer sliderwindow) {
        this.sliderwindow = sliderwindow ;
    }
    
    public Integer getOwnerType() {
        return ownerType;
    }
    
    public void setOwnerType(Integer ownerType) {
        this.ownerType = ownerType ;
    }
    
    public Integer getAccessSpeed() {
        return accessSpeed;
    }
    
    public void setAccessSpeed(Integer accessSpeed) {
        this.accessSpeed = accessSpeed ;
    }
    
    public String getOauthUrl() {
        return oauthUrl;
    }
    
    public void setOauthUrl(String oauthUrl) {
        this.oauthUrl = oauthUrl ;
    }
    
    public String getOauthPostData() {
        return oauthPostData;
    }
    
    public void setOauthPostData(String oauthPostData) {
        this.oauthPostData = oauthPostData ;
    }
    
    public String getAddTempUrl() {
        return addTempUrl;
    }
    
    public void setAddTempUrl(String addTempUrl) {
        this.addTempUrl = addTempUrl ;
    }
    
    public String getAddTempPostData() {
        return addTempPostData;
    }
    
    public void setAddTempPostData(String addTempPostData) {
        this.addTempPostData = addTempPostData ;
    }
    
    public String getGetTempListUrl() {
        return getTempListUrl;
    }
    
    public void setGetTempListUrl(String getTempListUrl) {
        this.getTempListUrl = getTempListUrl ;
    }
    
    public String getGetTempListPostData() {
        return getTempListPostData;
    }
    
    public void setGetTempListPostData(String getTempListPostData) {
        this.getTempListPostData = getTempListPostData ;
    }
    
    public Integer getContentlen() {
        return contentlen;
    }
    
    public void setContentlen(Integer contentlen) {
        this.contentlen = contentlen ;
    }
    
    public Integer getMqId() {
        return mqId;
    }
    
    public void setMqId(Integer mqId) {
        this.mqId = mqId ;
    }
    
    public Integer getSegcodeType() {
        return segcodeType;
    }
    
    public void setSegcodeType(Integer segcodeType) {
        this.segcodeType = segcodeType ;
    }
    
    public Long getBelongBusiness() {
		return belongBusiness;
	}

	public void setBelongBusiness(Long belongBusiness) {
		this.belongBusiness = belongBusiness;
	}

}