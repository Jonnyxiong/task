package com.ucpaas.sms.task.entity.message;

import java.util.Date;

/**
 * Created by lpjLiu on 2017/6/12.
 */
public class AgentInfo {

	private String agentId; // '代理商id,YYYYMM(年月)+序号（0000－9999），10位',
	private String adminId; // '管理员id',
	private String agentName; // '代理商名称',
	private String shorterName; // '代理商简称',
	private String agentType; // '代理商类型,1:销售代理商,2:品牌代理商,3:资源合作商,4:代理商和资源合作,5:OEM代理商
								// 配置tb_sms_params.param_type=agent_type',
	private String status; // '代理商状态，1：注册完成，5：冻结，6：注销',
	private String oauthStatus; // '认证状态，2：待认证 ，3：证件已认证(正常)，4：认证不通过',
	private Date oauthDate; // '认证时间',
	private String address; // '联系地址/公司地址',
	private String company; // '公司名称',
	private String companyNbr; // '公司电话',
	private String mobile; // '手机号码',
	private String belongSale; // '所属销售，关联t_sms_user表中id字段\r\n',
	private Date createTime; // '创建时间',
	private Date updateTime; // '更新时间',
	private String remark; // '备注',
	private String rebateUseRadio; // '代理商返点使用比例，每个季度末根据代理商本季度总消耗进行更新（激励返点需要在用户有新订单时才能使用，每一笔新订单最多可以使用的返点额度为成交额的N%，N值为此字段值）',

	public AgentInfo() {
	}

	public AgentInfo(String agentId) {
		this.agentId = agentId;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public String getAdminId() {
		return adminId;
	}

	public void setAdminId(String adminId) {
		this.adminId = adminId;
	}

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public String getShorterName() {
		return shorterName;
	}

	public void setShorterName(String shorterName) {
		this.shorterName = shorterName;
	}

	public String getAgentType() {
		return agentType;
	}

	public void setAgentType(String agentType) {
		this.agentType = agentType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getOauthStatus() {
		return oauthStatus;
	}

	public void setOauthStatus(String oauthStatus) {
		this.oauthStatus = oauthStatus;
	}

	public Date getOauthDate() {
		return oauthDate;
	}

	public void setOauthDate(Date oauthDate) {
		this.oauthDate = oauthDate;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getCompanyNbr() {
		return companyNbr;
	}

	public void setCompanyNbr(String companyNbr) {
		this.companyNbr = companyNbr;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getBelongSale() {
		return belongSale;
	}

	public void setBelongSale(String belongSale) {
		this.belongSale = belongSale;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getRebateUseRadio() {
		return rebateUseRadio;
	}

	public void setRebateUseRadio(String rebateUseRadio) {
		this.rebateUseRadio = rebateUseRadio;
	}
}
