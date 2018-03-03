package com.ucpaas.sms.task.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ucpaas.sms.task.constant.DbConstant.DbType;
import com.ucpaas.sms.task.constant.SendReprotConstant;
import com.ucpaas.sms.task.dao.AccessSlaveDao;
import com.ucpaas.sms.task.dao.MessageMasterDao;
import com.ucpaas.sms.task.model.MonthReport;
import com.ucpaas.sms.task.model.TaskInfo;
import com.ucpaas.sms.task.service.common.EmailService;
import com.ucpaas.sms.task.util.ConfigUtils;
import com.ucpaas.sms.task.util.FilePathUtils;
import com.ucpaas.sms.task.util.HttpUtils;
import com.ucpaas.sms.task.util.JFreeChartUtils;
import com.ucpaas.sms.task.util.JsonUtils;
import com.ucpaas.sms.task.util.encrypt.Des3Utils;

@Service
public class SendReprotMonthServiceImpl implements SendReprotMonthService {
	
	private static final Logger logger = LoggerFactory.getLogger(SendReprotMonthServiceImpl.class);
	
	@Autowired
	private MessageMasterDao messageMasterDao;
	
	@Autowired
	private AccessSlaveDao accessSlaveDao;
	
	@Autowired
	private EmailService emailService;
	
	@Override
	public boolean sendReprotMonth(TaskInfo taskInfo) {
		logger.debug("开始执行:方法={}=========================================>","sendReprotMonth");
		
		logger.debug("开始执行:判断数据库是否选对----------------------->");
		//判断数据库类是否选对
		if(taskInfo.getDbType() != DbType.ucpaas_message_master){
			logger.debug("数据库类型选择错误----------------------->");
			return true;
		}
		logger.debug("结束执行:判断数据库是否选对----------------------->");
		
		
		logger.debug("开始执行:判断今天是否可以执行----------------------->");
		//判断今天是否5号(做成可配置，方便测试)
		boolean flag = this.checkCurrentTime();
		if(flag == false){
			logger.debug("今天不等于5号，或者不等于设置的时间----------------------->");
			return true;
		}
		logger.debug("结束执行:判断今天是否可以执行----------------------->");
		
		logger.debug("开始执行:发送邮件-------------------------------->");
		try {
			this.sendMailMonth();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("发送邮件失败，异常={}------------------------->",e.getMessage());
			return true;
		}
		logger.debug("结束执行:发送邮件-------------------------------->");
		
		
		logger.debug("结束执行:方法={}=========================================>","sendReprotMonth");
		return true;
	}
	
	private boolean checkCurrentTime(){
		int execute_day = 5;
		
		String send_day = ConfigUtils.send_day;
		if( send_day != null && !"".equals(send_day)){
			execute_day = Integer.valueOf(send_day);
		}
		
		DateTime dtNow = new DateTime();
		int dayNow = dtNow.getDayOfMonth();
		if(dayNow != execute_day){
			return false;
		}
		return true;
	}
	
	//给代理商和客户发送邮件
	private void sendMailMonth(){
		logger.debug("开始执行:方法={}=========================================>","sendMailMonth");
		
		logger.debug("开始执行:上传公共的图片----------------------------->");
		
		//上传公共的图片
		if(SendReprotConstant.img_encrypt_path_arr == null || SendReprotConstant.img_encrypt_path_arr.length == 0){ 
			//如果没有上传过公共的图片，则上传；已经上传过了，就不在上传
			this.uploadPublicImageForAll();
		}
		
		//删除上一个月的统计记录和点击记录
		this.deleteLastMonthData();
		
		logger.debug("结束执行:上传公共的图片----------------------------->");
		
		logger.debug("开始执行:给全部代理商发送邮件----------------------------->");
		//给代理商发送邮件
		this.sendMailToAgentForAll();
		logger.debug("结束执行:给全部代理商发送邮件----------------------------->");

		logger.debug("开始执行:给全部客户发送邮件----------------------------->");
		//给客户发送邮件
		this.sendMailToClientForAll();
		logger.debug("结束执行:给全部客户发送邮件----------------------------->");
		
		logger.debug("结束执行:方法={}=========================================>","sendMailMonth");
	}
	
	//上传所有的公共图片的时候用到
	private void uploadPublicImageForAll(){
		
		String[] img_name_arr = SendReprotConstant.img_name_arr;
		SendReprotConstant.img_encrypt_path_arr = new String[img_name_arr.length];
		
		for(int i=0 ;i < img_name_arr.length; i++){
			String img_name = img_name_arr[i];
			String temp_encrypt_path = this.uploadPublicImageForOne(img_name);
			SendReprotConstant.img_encrypt_path_arr[i] = temp_encrypt_path;
		}
	}
	
	/** 
	 * @Title: uploadPublicImageForOne 
	 * @Description: 图片的名字
	 * @param img_name
	 * @return
	 * @return: String 图片的全地址
	 */
	private String uploadPublicImageForOne(String img_name){
		
		String img_service_url = ConfigUtils.img_service_url;
		String img_service_upload_url = ConfigUtils.img_service_upload_url;
		String img_service_scan_url = ConfigUtils.img_service_scan_url;
//		String localPath = ConfigUtils.img_temp_path + img_name;
		String localPath = FilePathUtils.getClassPath()+"img/"+img_name;
		
		String upload_img_path_url = img_service_url + img_service_upload_url;
		String res_str = HttpUtils.sendHttpPostForImge(localPath, upload_img_path_url);
		Map<String,String> consume_res_map = JsonUtils.toObject(res_str, Map.class);
		
		String common_path = consume_res_map.get("path");
		String encrypt_path = Des3Utils.encodeDes3(common_path);
		String scan_img_path_url = img_service_url + img_service_scan_url;
		String last_path = scan_img_path_url+"?path=" + encrypt_path;
		
		return last_path;
	}
	
	//======================================================================
	//=============== 给代理商发送邮件
	//======================================================================
	
	private void sendMailToAgentForAll(){
		
		logger.debug("开始执行:方法={}=========================================>","sendMailToAgentForAll");
		
		Map<String,Object> parmas = new HashMap<>();
		//查询已经取消订阅代理商id
		List<String> notSendAgentIdList = this.queryCancelSubscribeAgentIdList();
		if(notSendAgentIdList != null && notSendAgentIdList.size() != 0){
			parmas.put("notSendAgentIdList", notSendAgentIdList);
		}
		logger.debug("不需要发送邮件的代理商列表notSendAgentIdList={}----------------------------->",notSendAgentIdList==null?null:notSendAgentIdList.toString());
		//查看所有的品牌代理商和销售代理商
		List<String> agentTypeList = new ArrayList<>();
		agentTypeList.add("1"); //1:销售代理商
		agentTypeList.add("2"); //2:品牌代理商
		parmas.put("agentTypeList", agentTypeList);
		
		logger.debug("查询代理商列表的参数={}----------------------------->",parmas.toString());
		List<Map<String, Object>> agentIdList = this.queryAgentIdListByAgentType(parmas);
		if(agentIdList == null || agentIdList.size() == 0){
			//如果代理商列表为空，则不发
			return;
		}
		
		System.out.println("--------------->"+agentIdList.toString());
		logger.debug("返回的参数列表agentIdList={}----------------------------->",agentIdList==null?null:agentIdList.toString());
		
		logger.debug("开始执行:循环给每一个代理商发送邮件----------------------------->");
		for(Map<String, Object> map : agentIdList){
			Integer agent_id = (Integer) map.get("agent_id");
			this.sendMailToAgentForOne(agent_id);
		}
		
		/*//测试使用======开始===========
		
		this.sendMailToAgentForOne(2016090015);
		
		//测试使用======结束===========
*/		
		logger.debug("结束执行:循环给每一个代理商发送邮件----------------------------->");
		
		logger.debug("结束执行:方法={}=========================================>","sendMailToAgentForAll");
		
		
	}
	
	
	/** 
	 * @Title: sendMailToAgentForOne 
	 * @Description: 给某一个代理商发送邮件
	 * @param agent_id
	 * @return: void
	 */
	private void sendMailToAgentForOne(Integer agent_id){
		
		logger.debug("开始执行:方法={}=========================================>","sendMailToAgentForOne");
		
		String img_service_url = ConfigUtils.img_service_url;
		String img_service_upload_url = ConfigUtils.img_service_upload_url;
		String img_service_scan_url = ConfigUtils.img_service_scan_url;
		
		String upload_img_path_url = img_service_url + img_service_upload_url;
		String scan_img_path_url = img_service_url + img_service_scan_url;
		
		//装入参数的实体
		MonthReport monthReport = new MonthReport();
		
		//点击记录url(http://127.0.0.1:8090/agent/common/updateClickRecord?type=0&id=2016090015&date=201701)
		String clickRecordUrlStr =  ConfigUtils.agent_site_url+"/agent/common/updateClickRecord?type=0&id="+agent_id+"&date="+this.getLastMonthStr();
		monthReport.setClickRecordUrlStr(clickRecordUrlStr);
		
		//公共图片的加密地址
		String[] img_encrypt_path_arr = SendReprotConstant.img_encrypt_path_arr;
		monthReport.setIcon01UrlStr(img_encrypt_path_arr[0]);
		monthReport.setIcon02UrlStr(img_encrypt_path_arr[1]);
		monthReport.setIcon03UrlStr(img_encrypt_path_arr[2]);
		monthReport.setIcon04UrlStr(img_encrypt_path_arr[3]);
		monthReport.setIcon05UrlStr(img_encrypt_path_arr[4]);
		
		logger.debug("开始执行:查询代理商的登录邮箱----------------------------->");
		String loginMail = this.queryLoginMailByAgentID(agent_id);
		monthReport.setLoginAccount(loginMail);
		logger.debug("结束执行:查询代理商的登录邮箱----------------------------->");
		
		logger.debug("开始执行:查询上一月的字符串----------------------------->");
		Map<String,String> lastMonthStrMap = this.getLastMonthContentStr();
		monthReport.setMonthNumStr(lastMonthStrMap.get("monthNumStr"));
		monthReport.setYearNumStr(lastMonthStrMap.get("yearNumStr"));
		monthReport.setYearMonthStr(lastMonthStrMap.get("yearMonthStr"));
		monthReport.setBillCycleStart(lastMonthStrMap.get("billCycleStart"));
		monthReport.setBillCycleEnd(lastMonthStrMap.get("billCycleEnd"));
		logger.debug("结束执行:查询上一月的字符串----------------------------->");
		
		
		logger.debug("开始执行:查询发送条数和实际消耗金额(客户购买价)-------------------->");
		//代理商：从统计表查出发送条数和客户实际消耗金额(客户购买价)
		Map<String,Object> data_1 = this.queryBillDataForAgent_1(agent_id);
		monthReport.setChargeNum(data_1.get("total_chargetotal").toString());
		monthReport.setActualConsume(data_1.get("total_salefee").toString());
		System.out.println("data_1---------------->"+data_1==null?null:data_1.toString());
		logger.debug("返回来的数据，data_1={}-------------------->",data_1==null?null:data_1.toString());
		logger.debug("结束执行:查询发送条数和实际消耗金额(客户购买价)-------------------->");
		
		
		logger.debug("开始执行:剩余条数/剩余量----------------------------->");
		//代理商：从订单表里面查询剩余条数/剩余量
		Map<String,Object> data_2 = this.queryBillDataForAgent_2(agent_id);
		monthReport.setRemainNum(data_2.get("remail_num").toString());
		monthReport.setRemainAmount(data_2.get("remail_amount").toString());
		System.out.println("data_2---------------->"+data_2==null?null:data_2.toString());
		logger.debug("返回来的数据，data_2={}-------------------->",data_2==null?null:data_2.toString());
		logger.debug("结束执行:剩余条数/剩余量----------------------------->");
		
		
		//从统计表查询代理商统计发送量===============开始=========================
		logger.debug("从统计表查询代理商统计发送量===============开始=========================");
		List<Map<String,Object>> list_data_3 = this.queryBillDataForAgent_3(agent_id);
		logger.debug("短信发送量数据，list_data_3={}--------------------->",list_data_3==null?null:list_data_3.toString());
		//生成图片(短信发送量概览)
		String send_local_path = JFreeChartUtils.createImageAndSave(list_data_3, "send");
		System.out.println("短信发送量概览图片-本地路径：-------------------------->"+send_local_path);
		logger.debug("短信发送量概览图片-本地路径={}-------------------------->",send_local_path);
		
		//保存图片服务器(返回加密后的路径，最后装进实体里面)
		String send_res_str = HttpUtils.sendHttpPostForImge(send_local_path, upload_img_path_url);
		Map<String,String> send_res_map = JsonUtils.toObject(send_res_str, Map.class);
		String send_common_path = send_res_map.get("path");
		String send_encrypt_path = Des3Utils.encodeDes3(send_common_path);
		logger.debug("返回的加密的地址send_encrypt_path={}-------------------------->",send_encrypt_path);
		
		//最后的路径(可以直接访问图片服务器)
		String send_last_path = scan_img_path_url+"?path=" + send_encrypt_path;
		logger.debug("完整的地址访问图片服务器地址，send_last_path={}-------------------------->",send_last_path);
		monthReport.setSmsSendImgUrl(send_last_path);
		
		//从统计表查询代理商统计发送量===============结束=========================
		logger.debug("从统计表查询代理商统计发送量===============结束=========================");
		
		
		logger.debug("计算击败其他代理商的百分比===============开始=========================");
		//计算击败其他代理商的百分比
		BigDecimal beat_percent = calculateBeatPercentForAgent(agent_id);
		beat_percent = beat_percent.multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_HALF_UP);
		logger.debug("百分比，beat_percent={}----------------------->",beat_percent==null?null:beat_percent.toString());
		monthReport.setBeatPercent(beat_percent==null?"0":beat_percent.toString());
		logger.debug("计算击败其他代理商的百分比===============结束=========================");
		
		logger.debug("从统计表查看代理商短信发送金额==============开始==========================");
		//从统计表查看代理商短信发送金额==============开始==========================
		List<Map<String,Object>> list_data_4 = this.queryBillDataForAgent_4(agent_id);
		logger.debug("短信发送金额，list_data_4={}--------------------->",list_data_4==null?null:list_data_4.toString());
		
		//生成图片(短信消耗金额概览)
		String consume_local_path = JFreeChartUtils.createImageAndSave(list_data_4, "consume");
		System.out.println("短信消耗金额概览-本地路径：-------------------------->"+consume_local_path);
		logger.debug("短信消耗金额概览图片-本地路径={}-------------------------->",consume_local_path);
		
		//保存图片服务器
		String consume_res_str = HttpUtils.sendHttpPostForImge(consume_local_path, upload_img_path_url);
		Map<String,String> consume_res_map = JsonUtils.toObject(consume_res_str, Map.class);
		String consume_common_path = consume_res_map.get("path");
		String consume_encrypt_path = Des3Utils.encodeDes3(consume_common_path);
		logger.debug("返回的加密的地址consume_encrypt_path={}-------------------------->",consume_encrypt_path);
		
		//最后的路径(可以直接访问图片服务器)
		String consume_last_path = scan_img_path_url+"?path=" + consume_encrypt_path;
		logger.debug("完整的地址访问图片服务器地址，consume_last_path={}-------------------------->",consume_last_path);
		monthReport.setSmsConsumeImgUrl(consume_last_path);
		
		//从统计表查看代理商短信发送金额==============结束==========================
		logger.debug("从统计表查看代理商短信发送金额==============结束==========================");
		
		System.out.println("---------------->"+monthReport.toString());
		logger.debug("生成的实体，monthReport={}----------------------------->",monthReport.toString());
		
		//填好模板并发送邮件
		this.startSendMailToAgent(monthReport);
		
		logger.debug("更新统计表点击率======================开始=======================");
		//更新统计表点击率======================开始=======================
		Map<String,Object> params = new HashMap<>();
		params.put("agent_id", agent_id);
		Map<String,Object> agentInfoMap = this.messageMasterDao.getOneInfo("sendReprotMonth.queryAgentInfoByAgentId", params);
		//查询代理商的类型
		int agent_type = (int) agentInfoMap.get("agent_type"); //1销售代理商(含客户)，2品牌代理商
		logger.debug("代理商的类型，agent_type={}------------------------------->",agent_type);
		this.calculateClickStatistics(agent_type);
		//更新统计表点击率======================结束=======================
		logger.debug("更新统计表点击率======================结束=======================");
		
		logger.debug("结束执行:方法={}=========================================>","sendMailToAgentForOne");
		
		
		
	}
	
	private void startSendMailToAgent(MonthReport monthReport){
		
		String to = monthReport.getLoginAccount(); //代理商登录邮箱
		String subject = "【云之讯】请查收你的"+monthReport.getMonthNumStr()+"月短信账单";
		
		Map<String,Object> smsMailPropParams = new HashMap<>();
		
		String remainAmount = monthReport.getRemainAmount();//国际金额
		BigDecimal bgRemainAmount = new BigDecimal(remainAmount);
		if(bgRemainAmount.compareTo(BigDecimal.ZERO) == 0){
			//国际价格为0
			smsMailPropParams.put("id", 100018); //只显示剩余条数
		}else{
			//国际价格不为0
			smsMailPropParams.put("id", 100019);//显示剩余条数和剩余价格
		}
		Map<String,Object> smsMailpropMap = this.messageMasterDao.getOneInfo("sendReprotMonth.querySmsMailprop", smsMailPropParams);
		String body = (String) smsMailpropMap.get("text");
		body = body.replace("loginAccount", monthReport.getLoginAccount());
		body = body.replace("billCycleStart", monthReport.getBillCycleStart());
		body = body.replace("billCycleEnd", monthReport.getBillCycleEnd());
		
		body = body.replace("chargeNum", monthReport.getChargeNum());
		body = body.replace("actualConsume", monthReport.getActualConsume());
		body = body.replace("remainNum", monthReport.getRemainNum());
		body = body.replace("remainAmount", monthReport.getRemainAmount());
		body = body.replace("yearMonthStr", monthReport.getYearMonthStr());
		body = body.replace("beatPercent", monthReport.getBeatPercent());
		
		body = body.replace("monthNumStr", monthReport.getMonthNumStr());
		body = body.replace("yearNumStr", monthReport.getYearNumStr());
		
		//替换公共图片
		body = body.replace("icon01UrlStr", monthReport.getIcon01UrlStr());
		body = body.replace("icon02UrlStr", monthReport.getIcon02UrlStr());
		body = body.replace("icon03UrlStr", monthReport.getIcon03UrlStr());
		body = body.replace("icon04UrlStr", monthReport.getIcon04UrlStr());
		body = body.replace("icon05UrlStr", monthReport.getIcon05UrlStr());
		body = body.replace("smsSendImgUrl", monthReport.getSmsSendImgUrl());
		body = body.replace("smsConsumeImgUrl", monthReport.getSmsConsumeImgUrl());
		
		//隐藏链接地址(计算点击率)
		body = body.replace("clickRecordUrlStr", monthReport.getClickRecordUrlStr());
		
		
		this.emailService.sendHtmlEmail(to, subject, body);
	}
	
	
	
	/** 
	 * @Title: queryAgentListByAgentType 
	 * @Description: 查询代理商列表
	 * @param params 代理商类型type
	 * @return
	 * @return: List<Map<String,Object>>
	 */
	@Override
	public List<Map<String, Object>> queryAgentIdListByAgentType(Map<String, Object> params) {
		
		String lastDate = this.getLastMonthStr();
		params.put("lastDate", lastDate);
		List<Map<String, Object>> agentList = this.messageMasterDao.getSearchList("sendReprotMonth.queryAgentIdListByAgentType", params);
		return agentList;
	}

	/** 
	 * @Title: queryLoginMailByAgentID 
	 * @Description: 查询代理商的登录邮箱
	 * @param agent_id
	 * @return
	 * @return: String
	 */
	@Override
	public String queryLoginMailByAgentID(Integer agent_id) {
		Map<String,Object> params = new HashMap<>();
		params.put("agent_id", agent_id);
		String loginMail = this.messageMasterDao.getOneInfo("sendReprotMonth.queryLoginMailByAgentID", agent_id);
		return loginMail;
	}

	/** 
	 * @Title: queryLoginMailByClientId 
	 * @Description: 查询客户的登录邮箱
	 * @param client_id
	 * @return
	 * @return: String
	 */
	@Override
	public String queryLoginMailByClientId(String client_id) {
		Map<String,Object> params = new HashMap<>();
		params.put("client_id", client_id);
		String loginMail = this.messageMasterDao.getOneInfo("sendReprotMonth.queryLoginMailByClientId", client_id);
		return loginMail;
	}

	/** 
	 * @Title: queryPaytypeByClientId 
	 * @Description: 查询客户的付费类型，通过客户id
	 * @param client_id
	 * @return
	 * @return: String
	 */
	@Override
	public String queryPaytypeByClientId(String client_id) {
		Map<String,Object> params = new HashMap<>();
		params.put("client_id", client_id);
		String paytype = this.messageMasterDao.getOneInfo("sendReprotMonth.queryPaytypeByClientId", client_id);
		return paytype;
	}

	/** 
	 * @Title: queryAgentClientIdListByAgentId 
	 * @Description: 查询客户列表
	 * @param params agentId
	 * @return
	 * @return: List<String>
	 */
	@Override
	public List<String> queryAgentClientIdListByAgentId(Map<String, Object> params) {
		List<String> clientIdList = this.messageMasterDao.selectList("sendReprotMonth.queryAgentClientIdListByAgentId", params);
		return clientIdList;
	}

	/** 
	 * @Title: queryCancelSubscribeAgentIdList 
	 * @Description: 查询已经取消订阅的代理商id列表
	 * @return
	 * @return: List<String>
	 */
	@Override
	public List<String> queryCancelSubscribeAgentIdList() {
		List<String> agentIdList = this.messageMasterDao.selectList("sendReprotMonth.queryCancelSubscribeAgentIdList", null);
		return agentIdList;
	}

	/** 
	 * @Title: queryCancelSubscribeClientIdList 
	 * @Description: 查询已经取消订阅的客户id列表
	 * @return
	 * @return: List<String>
	 */
	@Override
	public List<String> queryCancelSubscribeClientIdList() {
		List<String> clientIdList = this.messageMasterDao.selectList("sendReprotMonth.queryCancelSubscribeClientIdList", null);
		return clientIdList;
	}

	
	//代理商：从统计表查出发送条数和客户实际消耗金额(客户购买价)
	private Map<String,Object> queryBillDataForAgent_1(Integer agent_id){
		Map<String,Object> data = null;
		Map<String,Object> params = new HashMap<>();
		params.put("agent_id", agent_id);
		params.put("last_month_str", this.getLastMonthStr());
		data = this.accessSlaveDao.getOneInfo("sendReprotMonth.queryBillDataForAgent_1", params);
		if(data == null) data = new HashMap<>();
		if(data.get("total_chargetotal") == null) data.put("total_chargetotal", 0);
		if(data.get("total_salefee") == null) data.put("total_salefee", 0);
		return data;
	}
	
	//代理商：从订单表里面查询剩余条数/剩余量
	private Map<String,Object> queryBillDataForAgent_2(Integer agent_id){
		
		Map<String,Object> data = new HashMap<>();
		
		//查询普通短信剩余条数
		Map<String,Object> map_1 = new HashMap<>();
		map_1.put("agent_id", agent_id);
		List<Integer> product_type_list_1 = new ArrayList<>();
		product_type_list_1.add(0);
		product_type_list_1.add(1);
		product_type_list_1.add(7);
		product_type_list_1.add(8);
		product_type_list_1.add(9);
		map_1.put("product_type_list", product_type_list_1);
		map_1.put("lastDate", this.getLastMonthStr());
		
		Map<String,Object> data_1 = this.messageMasterDao.getOneInfo("sendReprotMonth.queryBillDataForAgent_2", map_1);
		if(data_1 == null) data_1 = new HashMap<>();
		if(data_1.get("total_remain_quantity") == null) data_1.put("total_remain_quantity", 0);
		BigDecimal bg_remail_num = new BigDecimal(data_1.get("total_remain_quantity").toString());
		bg_remail_num = bg_remail_num.setScale(0, BigDecimal.ROUND_HALF_UP);
		data.put("remail_num", bg_remail_num.toString());
		
		//查询国际剩余元
		Map<String,Object> map_2 = new HashMap<>();
		map_2.put("agent_id", agent_id);
		List<Integer> product_type_list_2 = new ArrayList<>();
		product_type_list_2.add(2);
		map_2.put("product_type_list", product_type_list_2);
		map_2.put("lastDate", this.getLastMonthStr());
		Map<String,Object> data_2 = this.messageMasterDao.getOneInfo("sendReprotMonth.queryBillDataForAgent_2", map_2);
		if(data_2 == null) data_2 = new HashMap<>();
		if(data_2.get("total_remain_quantity") == null) data_2.put("total_remain_quantity", 0);
		
		BigDecimal bg_remail_amount = new BigDecimal(data_2.get("total_remain_quantity").toString());
		bg_remail_amount = bg_remail_amount.setScale(2, BigDecimal.ROUND_HALF_UP);
		
		String bg_str = bg_remail_amount.toString();
		
		if(bg_str.indexOf(".") > 0){  
			bg_str= bg_str.replaceAll("0+?$", "");//去掉多余的0  
			bg_str= bg_str.replaceAll("[.]$", "");//如最后一位是.则去掉  
		}
		
		data.put("remail_amount", bg_str);
		return data;
	}
	
	//从统计表查询上一个月代理商统计发送量
	private List<Map<String,Object>> queryBillDataForAgent_3(Integer agent_id){
		Map<String,Object> params = new HashMap<>();
		params.put("agent_id", agent_id);
		params.put("last_month_str", this.getLastMonthStr());
		List<Map<String,Object>> list = this.accessSlaveDao.getSearchList("sendReprotMonth.queryBillDataForAgent_3", params);
		list = this.completeContent(list);
		return list;
	}
	
	//拼接内容
	private List<Map<String, Object>> completeContent(List<Map<String, Object>> dataList){
		
		DateTime dt = new DateTime();
		DateTime dt_beforemonth = dt.minusMonths(1);
		
		//上一个月的第一天
		DateTime begin = dt_beforemonth.dayOfMonth().withMinimumValue();
		
		//上一个月的最后一天
		DateTime end = dt_beforemonth.dayOfMonth().withMaximumValue();
		Period p = new Period(begin, end, PeriodType.days());
		int days = p.getDays();
		
		//构造开始时间和结束时间的区间，值全部为0
		DateTime tempDate = null;
		String tempStr = null;
		List<Map<String, Object>> dataListNew = new ArrayList<>();
		for(int i=0; i<=days; i++){
			tempDate = begin.plusDays(i);
			tempStr = tempDate.toString("yyyy-MM-dd");
			Map<String,Object> map_temp = new HashMap<>();
			map_temp.put("date_value", 0);
			map_temp.put("date", tempStr);
			dataListNew.add(map_temp);
		}
		
		for(int i=0;i<dataList.size();i++){
			for(int j=0;j<dataListNew.size();j++){
				
				Map<String,Object> map_old = dataList.get(i);
				String date_str_old = map_old.get("date").toString();
				String value_old = map_old.get("date_value").toString();
				
				Map<String,Object> map_new = dataListNew.get(j);
				String date_str_new = map_new.get("date").toString();
				
				if(date_str_old.equals(date_str_new)){
					map_new.put("date_value", value_old);
				}
			}
		}
		
		return dataListNew;
	}
	
	//计算击败其他代理商的百分比
	private BigDecimal calculateBeatPercentForAgent(Integer agent_id){
		logger.debug("开始执行:方法={}=========================================>","calculateBeatPercentForAgent");
		logger.debug("方法参数，:agent_id={}=========================================>",agent_id.toString());
		
		BigDecimal beat_percent = null;
		
		Map<String,Object> params = new HashMap<>();
		params.put("agent_id", agent_id);
		Map<String,Object> agentInfoMap = this.messageMasterDao.getOneInfo("sendReprotMonth.queryAgentInfoByAgentId", params);
		//查询代理商的类型
		int agent_type = (int) agentInfoMap.get("agent_type");
		logger.debug("代理商类型，:agent_type={}=========================================>",agent_type);
		
		//查询对应类型的代理商列表
		Map<String,Object> typeParams = new HashMap<>();
		typeParams.put("agent_type", agent_type);
		typeParams.put("lastDate", this.getLastMonthStr());
		List<Map<String,Object>> agentIdMapList = this.messageMasterDao.getSearchList("sendReprotMonth.queryAgentIDListByType", typeParams);
		logger.debug("代理商列表ID列表，:agentIdMapList={}=========================================>",agentIdMapList==null?null:agentIdMapList.toString());
		
		//如果同类型的代理商只有一个，则百分比为100%
		if(agentIdMapList.size() == 1){
			beat_percent = new BigDecimal("1");
			return beat_percent;
		}
		
		//代理商总数(除数)
		Integer totalNum = agentIdMapList.size();
		logger.debug("代理商总数，:totalNum={}=========================================>",totalNum==null?null:totalNum.toString());
		//当前的代理商超过代理商数目(被除数)
		Integer dividend = calculateDividendForAgent(agentIdMapList,agent_id);
		logger.debug("超过代理商数目(被除数)，:dividend={}=========================================>",dividend==null?null:dividend.toString());
		
		//计算百分比
		BigDecimal bigTotalNum = new BigDecimal(totalNum.toString());
		BigDecimal bigDividend = new BigDecimal(dividend.toString());
		beat_percent = bigDividend.divide(bigTotalNum, 2,BigDecimal.ROUND_HALF_UP);
		
		logger.debug("结束执行:方法={}=========================================>","calculateBeatPercentForAgent");
		
		return beat_percent;
	}
	
	private int calculateDividendForAgent(List<Map<String,Object>> agentIdMapList,Integer agent_id){
		
		logger.debug("开始执行:方法={}=========================================>","calculateDividendForAgent");
		logger.debug("方法参数，:agentIdMapList={}，agent_id={}=========================================>",agentIdMapList.toString(),agent_id.toString());
		
		//被除数
		int dividend = 0;
		
		List<Integer> idList = new ArrayList<>();
		for(Map<String,Object> map : agentIdMapList){
			int agentId = (int) map.get("agent_id");
			idList.add(agentId);
		}
		
		//查询当前代理商在统计表的记录
		Map<String,Object> params = new HashMap<>();
		params.put("last_month_str", this.getLastMonthStr());
		params.put("agent_id", agent_id);
		
		Map<String,Object> currentAgentInfo = this.accessSlaveDao.getOneInfo("sendReprotMonth.queryLastMonthChargetotalForAgentOne", params);
		
		logger.debug("返回当前代理商的信息，currentAgentInfo={}=========================================>",currentAgentInfo == null ? null:currentAgentInfo.toString());
		
		if(currentAgentInfo == null || currentAgentInfo.size() == 0){
			dividend = 0;
			return dividend;
		}
		
		//当前代理商上一个月获取的计费条数
		String currentTotalChargetotalStr = currentAgentInfo.get("total_chargetotal").toString();
		BigDecimal bg_currentTotalChargetotal = new BigDecimal(currentTotalChargetotalStr);
		int currentTotalChargetotal = bg_currentTotalChargetotal.intValue();
		
		logger.debug("当前代理商上一个月获取的计费条数，currentTotalChargetotal={}=========================================>",currentTotalChargetotal);
		
		//从统计表里面查询上一个月所有品牌/销售代理商的计费记录
		Map<String,Object> paramsAll = new HashMap<>();
		paramsAll.put("idList", idList);
		paramsAll.put("last_month_str", this.getLastMonthStr());
		
		List<Map<String,Object>> lastMonthAgentList = 
				this.accessSlaveDao.getSearchList("sendReprotMonth.queryLastMonthChargetotalForAgentAll", paramsAll);
		
		logger.debug("从统计表里面查询上一个月所有品牌/销售代理商的计费记录，lastMonthAgentList={}=========================================>",lastMonthAgentList == null? null:lastMonthAgentList.toString());
		
		if(lastMonthAgentList == null || lastMonthAgentList.size() ==0){
			dividend = 0;
			return dividend;
		}
		
		lastMonthAgentList = completeContentForLastMonthChargetotalAgent(lastMonthAgentList,agentIdMapList);
		logger.debug("补全所有品牌/销售代理商的计费记录，lastMonthAgentList={}=========================================>",lastMonthAgentList == null ? null : lastMonthAgentList.toString());
		
		for(Map<String,Object> map : lastMonthAgentList){
			
			String tempChargetotalStr = map.get("total_chargetotal").toString();
			BigDecimal bg_tempChargetotal = new BigDecimal(tempChargetotalStr);
			int tempChargetotal = bg_tempChargetotal.intValue();
			
			if(currentTotalChargetotal > tempChargetotal){
				dividend++;
			}
		}
		logger.debug("最后的被除数，dividend={}=========================================>",dividend);
		logger.debug("开始执行:方法={}=========================================>","calculateDividendForAgent");
		
		return dividend;
		
	}
	
	private List<Map<String,Object>> completeContentForLastMonthChargetotalAgent(List<Map<String,Object>> lastMonthAgentList,List<Map<String,Object>> agentIdMapList){
		
		List<Map<String,Object>> dataListNew = new ArrayList<>();
		for(Map<String,Object> map : agentIdMapList){
			String agent_id = map.get("agent_id").toString();
			Map<String,Object> map_temp = new HashMap<>();
			map_temp.put("agent_id", agent_id);
			map_temp.put("total_chargetotal", 0);
			dataListNew.add(map_temp);
		}
		
		for(int i=0; i<lastMonthAgentList.size();i++){
			for(int j=0;j<dataListNew.size();j++){
				
				Map<String,Object> map_old = lastMonthAgentList.get(i);
				String agent_id_old = map_old.get("agent_id").toString();
				String total_chargetotal_old = map_old.get("total_chargetotal").toString();
				
				Map<String,Object> map_new = dataListNew.get(j);
				String agent_id_new = map_new.get("agent_id").toString();
				
				if(agent_id_old.equals(agent_id_new)){
					map_new.put("total_chargetotal", total_chargetotal_old);
				}
			}
		}
		return dataListNew;
	}
	
	
	//从统计表查看代理商上一个月短信发送金额
	private List<Map<String,Object>> queryBillDataForAgent_4(Integer agent_id){
		
		Map<String,Object> params = new HashMap<>();
		params.put("agent_id", agent_id);
		params.put("last_month_str", this.getLastMonthStr());
		List<Map<String,Object>> list = this.accessSlaveDao.getSearchList("sendReprotMonth.queryBillDataForAgent_4", params);
		
		list = this.completeContent(list);
		return list;
	}
	
	/**
	 * 
	 * @Title: calculateClickStatistics 
	 * @Description: 发送邮件的时候更新发送总数和点击率
	 * @param type
	 * @return: void
	 */
	private void calculateClickStatistics(int type){
		
		Date nowDate = new Date();
		//判断统计表是否存在记录
		Map<String,Object> isExistParams = new HashMap<>();
		isExistParams.put("type", type);
		isExistParams.put("data_date", this.getLastMonthStr());
		int num = this.messageMasterDao.getOneInfo("sendReprotMonth.querySendMailClickStatisticsNum", isExistParams);
		if(num == 0){
			
			DateTime dtNow = new DateTime();
			DateTime before1month = dtNow.minusMonths(1);
			String monthNumStr = Integer.valueOf(before1month.getMonthOfYear()).toString(); //月份
			String title = monthNumStr + "月份短信账单";
			
			//插入记录
			Map<String,Object> insertParams = new HashMap<>();
			insertParams.put("type", type);
			insertParams.put("send_date", nowDate);
			insertParams.put("data_date", this.getLastMonthStr());
			insertParams.put("title", title);
			insertParams.put("send_quantity", 1);
			insertParams.put("click_quantity", 0);
			insertParams.put("click_rate", 0);
			this.messageMasterDao.insert("sendReprotMonth.insertSendMailClickStatisticsNum", insertParams);
			
		}else{
			//更新记录
			this.updateSendMailClickStatisticsForSendMail(type, this.getLastMonthStr());
		}
		
	}
	
	//重新发送邮件，删除统计记录和点击记录
	private void deleteLastMonthData(){
		Map<String,Object> params = new HashMap<>();
		params.put("data_date", this.getLastMonthStr());
		this.messageMasterDao.delete("sendReprotMonth.deleteLastMonthStatistics", params);
		this.messageMasterDao.delete("sendReprotMonth.deleteClickRecord", params);
	}
	
	
	/** 
	 * @Title: updateSendMailClickStatisticsForSendMail 
	 * @Description: 更新点击率和发送次数
	 * @return: void
	 */
	private void updateSendMailClickStatisticsForSendMail(int type,String date){
		
		Map<String,Object> queryParams = new HashMap<>();
		queryParams.put("type", type);
		queryParams.put("data_date", date);
		Map<String,Object> result = this.messageMasterDao.getOneInfo("sendReprotMonth.querySendMailClickStatistics", queryParams);
		String send_quantity = result.get("send_quantity").toString();
		String click_quantity = result.get("click_quantity").toString();
		
		BigDecimal bg_send_quantity = new BigDecimal(send_quantity);
		BigDecimal bg_click_quantity = new BigDecimal(click_quantity);
		
		bg_send_quantity = bg_send_quantity.add(new BigDecimal("1")); //点击加1
		BigDecimal click_rate = bg_click_quantity.divide(bg_send_quantity,2, BigDecimal.ROUND_HALF_UP);
		
		Map<String,Object> updateParmas = new HashMap<>();
		updateParmas.put("type", type);
		updateParmas.put("data_date", date);
		updateParmas.put("send_quantity", bg_send_quantity);
		updateParmas.put("click_rate", click_rate);
		
		this.messageMasterDao.update("sendReprotMonth.updateSendMailClickStatisticsForSendMail", updateParmas);
		
	}
	
	//======================================================================
	//=============== 给客户发送邮件
	//======================================================================
	
	
	/** 
	 * @Title: sendMailToClientForAll 
	 * @Description: 给所有的客户发送邮件
	 * @return: void
	 */
	private void sendMailToClientForAll(){
		logger.debug("开始执行:方法={}=========================================>","sendMailToClientForAll");
		
		//查询已经取消订阅的客户id
		List<String> notSendClientIdList = this.queryCancelSubscribeClientIdList();
		
		logger.debug("不需要发送邮件的客户列表notSendClientIdList={}----------------------------->",notSendClientIdList.toString());
		
		Map<String,Object> parmas2 = new HashMap<>();
		List<Map<String, Object>> agentClientIdList = new ArrayList<>();
		
		List<String> agentTypeList2 = new ArrayList<>();
		agentTypeList2.add("1");
		parmas2.put("agentTypeList", agentTypeList2);
		List<Map<String, Object>> agentList2 = this.queryAgentIdListByAgentType(parmas2);
		System.out.println("对应的销售的代理商列表----------------------------->"+agentList2==null?null:agentList2.toString());
		logger.debug("对应的销售的代理商列表agentList2={}----------------------------->",agentList2==null?null:agentList2.toString());
		
		//如果没有销售代理商，则就不发邮件
		if(agentList2 == null || agentList2.size() == 0){
			return;
		}
		
		for(Map<String, Object> map : agentList2){
			
			Integer agent_id = (Integer) map.get("agent_id");
			Map<String,Object> params3 = new HashMap<>();
			params3.put("agent_id", agent_id);
			if(notSendClientIdList != null && notSendClientIdList.size() !=0){
				params3.put("notSendClientIdList", notSendClientIdList);
			}
			params3.put("lastDate", this.getLastMonthStr());
			List<String> clientIdList = this.queryAgentClientIdListByAgentId(params3);
			
			Map<String,Object> mapTemp = new HashMap<>();
			mapTemp.put("agentId", agent_id);
			mapTemp.put("clientIdList", clientIdList);
			agentClientIdList.add(mapTemp);
		}
		logger.debug("需要发送的对应的agentClientIdList={}----------------------------->",agentClientIdList==null?null:agentClientIdList.toString());
		
		for(Map<String, Object> map : agentClientIdList){
			Integer agent_id = (Integer) map.get("agentId");
			System.out.println("agentId:---------------------->"+agent_id);
			List<String> clientIdList = (List<String>) map.get("clientIdList");
			System.out.println("clientIdList----------------->"+clientIdList==null?null:clientIdList.toString());
			
			logger.debug("对应的agent_id={}----------------------------->",agent_id);
			logger.debug("对应的clientIdList={}----------------------------->",clientIdList==null?null:clientIdList.toString());
			logger.debug("循环给每一个客户发送邮件------------------------------------------>");
			if(clientIdList != null && clientIdList.size() != 0){
				for(String str : clientIdList){
					String client_id = str;
					this.sendMailToClientForOne(agent_id, client_id);
				}
			}
		}
		
		//测试使用
//		this.sendMailToClientForOne(2016090015, "a00100");
		//测试使用
		
		logger.debug("结束执行:方法={}=========================================>","sendMailToClientForAll");
		
	}
	
	/** 
	 * @Title: sendMailToClientForOne 
	 * @Description: 给某一个客户发送邮件
	 * @param agent_id
	 * @param client_id
	 * @return: void
	 */
	private void sendMailToClientForOne(Integer agent_id,String client_id){
		
		logger.debug("开始执行:方法={}=========================================>","sendMailToClientForOne");
			
		String img_service_url = ConfigUtils.img_service_url;
		String img_service_upload_url = ConfigUtils.img_service_upload_url;
		String img_service_scan_url = ConfigUtils.img_service_scan_url;
		
		String upload_img_path_url = img_service_url + img_service_upload_url;
		String scan_img_path_url = img_service_url + img_service_scan_url;
		
		MonthReport monthReport = new MonthReport();
		
		//点击记录url(http://127.0.0.1:8090/agent/common/updateClickRecord?type=1&id=2016090015&date=201701)
		String clickRecordUrlStr =  ConfigUtils.agent_site_url+"/agent/common/updateClickRecord?type=1&id="+client_id+"&date="+this.getLastMonthStr();
		monthReport.setClickRecordUrlStr(clickRecordUrlStr);
		
		//公共图片的加密地址
		String[] img_encrypt_path_arr = SendReprotConstant.img_encrypt_path_arr;
		monthReport.setIcon01UrlStr(img_encrypt_path_arr[0]);
		monthReport.setIcon02UrlStr(img_encrypt_path_arr[1]);
		monthReport.setIcon03UrlStr(img_encrypt_path_arr[2]);
		monthReport.setIcon04UrlStr(img_encrypt_path_arr[3]);
		monthReport.setIcon05UrlStr(img_encrypt_path_arr[4]);
		
		logger.debug("开始执行:查询账号的登录邮箱--------------------------------------->");
		//查询账号登陆邮箱账号
		String loginMail = this.queryLoginMailByClientId(client_id);
		monthReport.setLoginAccount(loginMail);
		logger.debug("客户的登录邮箱账号，loginMail={}--------------------------------------->",loginMail);
		logger.debug("结束执行:查询账号的登录邮箱--------------------------------------->");
		
		logger.debug("开始执行:查询上一个月的字符串表示--------------------------------------->");
		Map<String,String> lastMonthStrMap = this.getLastMonthContentStr();
		monthReport.setMonthNumStr(lastMonthStrMap.get("monthNumStr"));
		monthReport.setYearNumStr(lastMonthStrMap.get("yearNumStr"));
		monthReport.setYearMonthStr(lastMonthStrMap.get("yearMonthStr"));
		monthReport.setBillCycleStart(lastMonthStrMap.get("billCycleStart"));
		monthReport.setBillCycleEnd(lastMonthStrMap.get("billCycleEnd"));
		logger.debug("返回上一个月的字符串，lastMonthStrMap={}--------------------------------------->",lastMonthStrMap==null?null:lastMonthStrMap.toString());
		logger.debug("结束执行:查询上一个月的字符串表示--------------------------------------->");
		
		
		logger.debug("开始执行:查询发送条数和实际消耗金额--------------------------------------->");
		//客户：从统计表查出发送条数和客户实际消耗金额(客户购买价)
		Map<String,Object> data_1 = this.queryBillDataForClient_1(client_id);
		monthReport.setChargeNum(data_1.get("total_chargetotal").toString());
		monthReport.setActualConsume(data_1.get("total_salefee").toString());
		System.out.println("data_1---------------->"+data_1==null?null:data_1.toString());
		logger.debug("返回的数据，data_1={}--------------------------------------->",data_1==null?null:data_1.toString());
		logger.debug("结束执行:查询发送条数和实际消耗金额--------------------------------------->");
		
		
		logger.debug("开始执行:查询剩余条数/剩余量--------------------------------------->");
		//客户：从订单表里面查询剩余条数/剩余量
		Map<String,Object> data_2 = this.queryBillDataForClient_2(client_id);
		monthReport.setRemainNum(data_2.get("remail_num").toString());
		monthReport.setRemainAmount(data_2.get("remail_amount").toString());
		System.out.println("data_2---------------->"+data_2==null?null:data_2.toString());
		logger.debug("返回的数据，data_2={}--------------------------------------->",data_2==null?null:data_2.toString());
		logger.debug("结束执行:查询剩余条数/剩余量--------------------------------------->");
		
		
		logger.debug("从统计表查询客户统计发送量===============开始=========================");
		//从统计表查询客户统计发送量===============开始=========================
		List<Map<String,Object>> list_data_3 = this.queryBillDataForClient_3(client_id);
		logger.debug("返回的统计发送量数据,list_data_3={}----------------------->",list_data_3==null?null:list_data_3.toString());
		
		//生成图片(短信发送量概览)
		String send_local_path = JFreeChartUtils.createImageAndSave(list_data_3, "send");
		System.out.println("短信发送量概览图片-本地路径：-------------------------->"+send_local_path);
		
		//保存图片服务器(返回加密后的路径，最后装进实体里面)
		String send_res_str = HttpUtils.sendHttpPostForImge(send_local_path, upload_img_path_url);
		Map<String,String> send_res_map = JsonUtils.toObject(send_res_str, Map.class);
		String send_common_path = send_res_map.get("path");
		String send_encrypt_path = Des3Utils.encodeDes3(send_common_path);
		logger.debug("加密之后的地址,send_encrypt_path={}----------------------->",send_encrypt_path);
		
		//最后的路径(可以直接访问图片服务器)
		String send_last_path = scan_img_path_url+"?path=" + send_encrypt_path;
		logger.debug("完整的地址,send_last_path={}----------------------->",send_last_path);
		monthReport.setSmsSendImgUrl(send_last_path);
		
		//从统计表查询客户统计发送量===============结束=========================
		logger.debug("从统计表查询客户统计发送量===============结束=========================");
		
		logger.debug("开始执行:计算击败其他客户百分比--------------------------------------->");
		//计算击败其他客户百分比
		BigDecimal beat_percent = calculateBeatPercentForClient(agent_id,client_id);
		logger.debug("返回的百分比，beat_percent={}--------------------------------------->",beat_percent==null?null:beat_percent.toString());
		beat_percent = beat_percent.multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_HALF_UP);
		monthReport.setBeatPercent(beat_percent==null?"0":beat_percent.toString());
		logger.debug("结束执行:计算击败其他客户百分比--------------------------------------->");
		
		
		logger.debug("从统计表查看客户短信发送金额==============开始==========================");
		//从统计表查看客户短信发送金额==============开始==========================
		List<Map<String,Object>> list_data_4 = this.queryBillDataForClient_4(client_id);
		logger.debug("返回的短信发送金额，list_data_4={}------------------------>",list_data_4==null?null:list_data_4.toString());
		
		//生成图片(短信消耗金额概览)
		String consume_local_path = JFreeChartUtils.createImageAndSave(list_data_4, "consume");
		System.out.println("短信消耗金额概览-本地路径：-------------------------->"+consume_local_path);
		
		//保存图片服务器
		String consume_res_str = HttpUtils.sendHttpPostForImge(consume_local_path, upload_img_path_url);
		Map<String,String> consume_res_map = JsonUtils.toObject(consume_res_str, Map.class);
		String consume_common_path = consume_res_map.get("path");
		String consume_encrypt_path = Des3Utils.encodeDes3(consume_common_path);
		logger.debug("加密之后的地址，consume_encrypt_path={}------------------------>",consume_encrypt_path);
		
		//最后的路径(可以直接访问图片服务器)
		String consume_last_path = scan_img_path_url+"?path=" + consume_encrypt_path;
		monthReport.setSmsConsumeImgUrl(consume_last_path);
		logger.debug("完整的地址，consume_last_path={}------------------------>",consume_last_path);
		
		//从统计表查看客户短信发送金额==============结束==========================
		logger.debug("从统计表查看客户短信发送金额==============结束==========================");
		
		System.out.println("---------------->"+monthReport.toString());
		
		//填好模板并发送邮件
		
		this.startSendMailToClient(monthReport, client_id);
		
		logger.debug("更新统计表点击率======================开始=======================");
		//更新统计表点击率======================开始=======================
		int type = 1; //客户一定输入销售代理商type为1
		this.calculateClickStatistics(type);
		//更新统计表点击率======================结束=======================
		logger.debug("更新统计表点击率======================结束=======================");
		
		logger.debug("结束执行:方法={}=========================================>","sendMailToClientForOne");
		
	}
	
	private void startSendMailToClient(MonthReport monthReport,String client_id){
		
		
		String to = monthReport.getLoginAccount(); //代理商登录邮箱
		String subject = "【云之讯】请查收你的"+monthReport.getMonthNumStr()+"月短信账单";
		
		Map<String,Object> smsMailPropParams = new HashMap<>();
		String paytype = this.queryPaytypeByClientId(client_id);
		if("1".equals(paytype)){ //后付费用户
			smsMailPropParams.put("id", 100020);
		}else{
			String remainAmount = monthReport.getRemainAmount();//国际金额
			BigDecimal bgRemainAmount = new BigDecimal(remainAmount);
			if(bgRemainAmount.compareTo(BigDecimal.ZERO) == 0){
				//国际价格为0
				smsMailPropParams.put("id", 100018); //只显示剩余条数
			}else{
				//国际价格不为0
				smsMailPropParams.put("id", 100019);//显示剩余条数和剩余价格
			}
		}
		
		Map<String,Object> smsMailpropMap = this.messageMasterDao.getOneInfo("sendReprotMonth.querySmsMailprop", smsMailPropParams);
		String body = (String) smsMailpropMap.get("text");
		body = body.replace("loginAccount", monthReport.getLoginAccount());
		body = body.replace("billCycleStart", monthReport.getBillCycleStart());
		body = body.replace("billCycleEnd", monthReport.getBillCycleEnd());
		
		body = body.replace("chargeNum", monthReport.getChargeNum());
		body = body.replace("actualConsume", monthReport.getActualConsume());
		body = body.replace("remainNum", monthReport.getRemainNum());
		body = body.replace("remainAmount", monthReport.getRemainAmount());
		body = body.replace("yearMonthStr", monthReport.getYearMonthStr());
		body = body.replace("beatPercent", monthReport.getBeatPercent());
		
		body = body.replace("monthNumStr", monthReport.getMonthNumStr());
		body = body.replace("yearNumStr", monthReport.getYearNumStr());
		
		//替换公共图片
		body = body.replace("icon01UrlStr", monthReport.getIcon01UrlStr());
		body = body.replace("icon02UrlStr", monthReport.getIcon02UrlStr());
		body = body.replace("icon03UrlStr", monthReport.getIcon03UrlStr());
		body = body.replace("icon04UrlStr", monthReport.getIcon04UrlStr());
		body = body.replace("icon05UrlStr", monthReport.getIcon05UrlStr());
		body = body.replace("smsSendImgUrl", monthReport.getSmsSendImgUrl());
		body = body.replace("smsConsumeImgUrl", monthReport.getSmsConsumeImgUrl());
		
		//隐藏链接(计算点击次数)
		body = body.replace("clickRecordUrlStr", monthReport.getClickRecordUrlStr());
		
		this.emailService.sendHtmlEmail(to, subject, body);
	}
	
	//客户：从统计表查出客户上一个月发送条数和客户实际消耗金额(客户购买价)
	private Map<String,Object> queryBillDataForClient_1(String client_id){
		Map<String,Object> data = null;
		Map<String,Object> params = new HashMap<>();
		params.put("client_id", client_id);
		params.put("last_month_str", this.getLastMonthStr());
		data = this.accessSlaveDao.getOneInfo("sendReprotMonth.queryBillDataForClient_1", params);
		if(data == null) data = new HashMap<>();
		if(data.get("total_chargetotal") == null) data.put("total_chargetotal", 0);
		if(data.get("total_salefee") == null) data.put("total_salefee", 0);
		return data;
	}
	
	//客户：从订单表里面查询剩余条数/剩余量
	private Map<String,Object> queryBillDataForClient_2(String client_id){
		
		Map<String,Object> data = new HashMap<>();
		
		//查询普通短信剩余条数
		Map<String,Object> map_1 = new HashMap<>();
		map_1.put("client_id", client_id);
		List<Integer> product_type_list_1 = new ArrayList<>();
		product_type_list_1.add(0);
		product_type_list_1.add(1);
		product_type_list_1.add(7);
		product_type_list_1.add(8);
		product_type_list_1.add(9);
		map_1.put("product_type_list", product_type_list_1);
		map_1.put("lastDate", this.getLastMonthStr());
		
		Map<String,Object> data_1 = this.messageMasterDao.getOneInfo("sendReprotMonth.queryBillDataForClient_2", map_1);
		if(data_1 == null) data_1 = new HashMap<>();
		if(data_1.get("total_remain_quantity") == null) data_1.put("total_remain_quantity", 0);
		BigDecimal bg_remail_num = new BigDecimal(data_1.get("total_remain_quantity").toString());
		bg_remail_num = bg_remail_num.setScale(0, BigDecimal.ROUND_HALF_UP);
		data.put("remail_num", bg_remail_num.toString());
		
		//查询国际剩余元
		Map<String,Object> map_2 = new HashMap<>();
		map_2.put("client_id", client_id);
		List<Integer> product_type_list_2 = new ArrayList<>();
		product_type_list_2.add(2);
		map_2.put("product_type_list", product_type_list_2);
		map_2.put("lastDate", this.getLastMonthStr());
		
		Map<String,Object> data_2 = this.messageMasterDao.getOneInfo("sendReprotMonth.queryBillDataForClient_2", map_2);
		if(data_2 == null) data_2 = new HashMap<>();
		if(data_2.get("total_remain_quantity") == null) data_2.put("total_remain_quantity", 0);
		
		BigDecimal bg_remail_amount = new BigDecimal(data_2.get("total_remain_quantity").toString());
		bg_remail_amount = bg_remail_amount.setScale(2, BigDecimal.ROUND_HALF_UP);
		
		String bg_str = bg_remail_amount.toString();
		if(bg_str.indexOf(".") > 0){  
			bg_str= bg_str.replaceAll("0+?$", "");//去掉多余的0  
			bg_str= bg_str.replaceAll("[.]$", "");//如最后一位是.则去掉  
		}
		data.put("remail_amount", bg_str);
		return data;
	}
	
	//从统计表查询客户统计发送量
	private List<Map<String,Object>> queryBillDataForClient_3(String client_id){
		
		Map<String,Object> params = new HashMap<>();
		params.put("client_id", client_id);
		params.put("last_month_str", this.getLastMonthStr());
		
		List<Map<String,Object>> list = this.accessSlaveDao.getSearchList("sendReprotMonth.queryBillDataForClient_3", params);
		list = this.completeContent(list);
		return list;
	}
	
	//计算击败其他客户百分比
	private BigDecimal calculateBeatPercentForClient(Integer agent_id,String client_id){
		logger.debug("开始执行:方法={}=========================================>","calculateBeatPercentForClient");
		logger.debug("方法参数，agent_id={}，client_id={}----------------------------------------->",agent_id,client_id);
		
		//查询该销售代理商下面的客户数目
		Map<String,Object> params = new HashMap<>();
		params.put("agent_id", agent_id);
		List<String> notSendClientIdList = this.queryCancelSubscribeClientIdList();
		if(notSendClientIdList != null && notSendClientIdList.size() != 0){
			params.put("notSendClientIdList", notSendClientIdList);
		}
		params.put("lastDate", this.getLastMonthStr());
		List<Map<String,Object>> clientIdMapList = this.messageMasterDao.getSearchList("sendReprotMonth.queryAgentClientIdMapByAgentId", params);
		logger.debug("该销售代理商下面的客户数目，clientIdMapList={}------------------------------->",clientIdMapList==null?null:clientIdMapList.toString());
		
		if(clientIdMapList != null && clientIdMapList.size() == 1){
			//如果该销售代理商下只有一个客户，则百分比为100%
			return new BigDecimal("1");
		}
		
		//该销售代理商下面的客户总数(除数)
		Integer totalNum = clientIdMapList.size();
		logger.debug("该销售代理商下面的客户总数(除数)，totalNum={}------------------------------->",totalNum.toString());
		//计算被除数
		Integer dividend = calculateDividendForClient(clientIdMapList,client_id);
		logger.debug("被除数，dividend={}---------------------------------------------------->",dividend.toString());
		
		//计算百分比
		BigDecimal bigTotalNum = new BigDecimal(totalNum.toString());
		BigDecimal bigDividend = new BigDecimal(dividend.toString());
		BigDecimal beat_percent = bigDividend.divide(bigTotalNum,2, BigDecimal.ROUND_HALF_UP);
		
		logger.debug("结束执行:方法={}=========================================>","calculateBeatPercentForClient");
		
		return beat_percent;
	}
	
	private int calculateDividendForClient(List<Map<String,Object>> clientIdMapList,String client_id){
		
		logger.debug("开始执行:方法={}=========================================>","calculateDividendForClient");
		//被除数
		int dividend = 0;
		
		List<String> idList = new ArrayList<>();
		
		for(Map<String,Object> map : clientIdMapList){
			String clientId = map.get("client_id").toString();
			idList.add(clientId);
		}
		
		//查询当前客户在统计表的记录
		Map<String,Object> params = new HashMap<>();
		params.put("last_month_str", this.getLastMonthStr());
		params.put("client_id", client_id);
		
		Map<String,Object> currentClientInfo = this.accessSlaveDao.getOneInfo("sendReprotMonth.queryLastMonthChargetotalForClientOne", params);
		logger.debug("查询当前客户在统计表的记录，currentClientInfo={}-------------------------------->",currentClientInfo==null?null:currentClientInfo.toString());
		
		
		if(currentClientInfo == null || currentClientInfo.size() == 0){
			dividend = 0;
			return dividend;
		}
		
		//当前代理商上一个月获取的计费条数
		
		String currentTotalChargetotalStr = currentClientInfo.get("total_chargetotal").toString();
		BigDecimal bg_currentTotalChargetotal = new BigDecimal(currentTotalChargetotalStr);
		int currentTotalChargetotal = bg_currentTotalChargetotal.intValue();
		
		logger.debug("当前客户计费条数，currentTotalChargetotal={}-------------------------------->",currentTotalChargetotal);
		
		//从统计表里面查询上一个月所有客户的计费记录
		Map<String,Object> paramsAll = new HashMap<>();
		paramsAll.put("idList", idList);
		paramsAll.put("last_month_str", this.getLastMonthStr());
		
		List<Map<String,Object>> lastMonthClientList = 
				this.accessSlaveDao.getSearchList("sendReprotMonth.queryLastMonthChargetotalForClientAll", paramsAll);
		logger.debug("上一个月所有客户的计费记录，lastMonthClientList={}-------------------------------->",lastMonthClientList==null?null:lastMonthClientList.toString());
		
		if(lastMonthClientList == null || lastMonthClientList.size() ==0){
			dividend = 0;
			return dividend;
		}
		
		lastMonthClientList = completeContentForLastMonthChargetotalClient(lastMonthClientList,clientIdMapList);
		logger.debug("补全，所有客户的计费记录，lastMonthClientList={}-------------------------------->",lastMonthClientList==null?null:lastMonthClientList.toString());
		
		for(Map<String,Object> map : lastMonthClientList){
			
			String tempChargetotalStr = map.get("total_chargetotal").toString();
			BigDecimal bg_tempChargetotal = new BigDecimal(tempChargetotalStr);
			int tempChargetotal = bg_tempChargetotal.intValue();
			if(currentTotalChargetotal > tempChargetotal){
				dividend++;
			}
		}
		logger.debug("最后的被除数，dividend={}-------------------------------->",dividend);
		
		logger.debug("结束执行:方法={}=========================================>","calculateDividendForClient");
		return dividend;
		
	}
	
	private List<Map<String,Object>> completeContentForLastMonthChargetotalClient(List<Map<String,Object>> lastMonthClientList,List<Map<String,Object>> clientIdMapList){
		
		List<Map<String,Object>> dataListNew = new ArrayList<>();
		
		for(Map<String,Object> map : clientIdMapList){
			String client_id = map.get("client_id").toString();
			Map<String,Object> map_temp = new HashMap<>();
			map_temp.put("client_id", client_id);
			map_temp.put("total_chargetotal", 0);
			dataListNew.add(map_temp);
		}
		
		for(int i=0; i<lastMonthClientList.size();i++){
			for(int j=0; j< dataListNew.size();j++){
				
				Map<String,Object> map_old = lastMonthClientList.get(i);
				String client_id_old = map_old.get("client_id").toString();
				String total_chargetotal_old = map_old.get("total_chargetotal").toString();
				Map<String,Object> map_new = dataListNew.get(j);
				String client_id_new = map_new.get("client_id").toString();
				if(client_id_old.equals(client_id_new)){
					map_new.put("total_chargetotal", total_chargetotal_old);
				}
			}
		}
		
		return dataListNew;
	}
	
	//从统计表查看客户短信发送金额
	private List<Map<String,Object>> queryBillDataForClient_4(String client_id){
		
		Map<String,Object> params = new HashMap<>();
		params.put("client_id", client_id);
		params.put("last_month_str", this.getLastMonthStr());
		List<Map<String,Object>> list = this.accessSlaveDao.getSearchList("sendReprotMonth.queryBillDataForClient_4", params);
		
		list = this.completeContent(list);
		return list;
	}
	
	/*
	 * 返回上一个月的格式字符串
	 */
	private Map<String,String> getLastMonthContentStr(){
		Map<String,String> dataMap = new HashMap<>();
		DateTime dt = new DateTime();
		DateTime before1month = dt.minusMonths(1);
		String yearNumStr = Integer.valueOf(before1month.getYear()).toString(); //年份
		String monthNumStr = Integer.valueOf(before1month.getMonthOfYear()).toString(); //月份
		String yearMonthStr = before1month.toString("yyyy年MM月份");
		String billCycleStart = before1month.dayOfMonth().withMinimumValue().toString("yyyy/MM/dd");
		String billCycleEnd = before1month.dayOfMonth().withMaximumValue().toString("yyyy/MM/dd");
		
		dataMap.put("yearNumStr", yearNumStr);
		dataMap.put("monthNumStr", monthNumStr);
		dataMap.put("yearMonthStr", yearMonthStr);
		dataMap.put("billCycleStart", billCycleStart);
		dataMap.put("billCycleEnd", billCycleEnd);
		
		return dataMap;
	}
	
	//返回上一个月字符串(201612)
	private String getLastMonthStr(){
		DateTime dt = new DateTime();
		DateTime before1month = dt.minusMonths(1);
		String ret_str = before1month.toString("yyyyMM");
		return ret_str;
	}
	
	

}
