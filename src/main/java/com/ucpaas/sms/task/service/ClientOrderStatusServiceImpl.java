package com.ucpaas.sms.task.service;

import com.jsmsframework.finance.entity.JsmsTaskAlarmSetting;
import com.jsmsframework.finance.enums.TaskAlarmType;
import com.jsmsframework.finance.service.JsmsTaskAlarmSettingService;
import com.jsmsframework.user.entity.JsmsAccount;
import com.jsmsframework.user.entity.JsmsUser;
import com.jsmsframework.user.service.JsmsAccountService;
import com.jsmsframework.user.service.JsmsUserService;
import com.ucpaas.sms.task.constant.AlarmConstant;
import com.ucpaas.sms.task.dao.MessageMasterDao;
import com.ucpaas.sms.task.mapper.message.AgentClientParamMapper;
import com.ucpaas.sms.task.mapper.message.ClientBalanceAlarmMapper;
import com.ucpaas.sms.task.mapper.message.ClientOrderMapper;
import com.ucpaas.sms.task.model.TaskInfo;
import com.ucpaas.sms.task.util.AlarmCommonUtil;
import com.ucpaas.sms.task.util.JsonUtils;
import com.ucpaas.sms.task.util.SendSMSUtil;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class ClientOrderStatusServiceImpl implements ClientOrderStatusService {

	@Autowired
	private MessageMasterDao ucpaasMessageDao;
	@Autowired
	private AgentClientParamMapper agentClientParamMapper;
	@Autowired
	private ClientOrderMapper clientOrderMapper;
	@Autowired
	private ClientBalanceAlarmMapper clientBalanceAlarmMapper;
	@Autowired
	private JsmsTaskAlarmSettingService jsmsTaskAlarmSettingService;
	@Autowired
	private JsmsUserService jsmsUserService;
	@Autowired
	private JsmsAccountService jsmsAccountService;
	@Autowired
	private BalanceAlarmService balanceAlarmService;

	private static final Logger logger = LoggerFactory.getLogger(ClientOrderStatusServiceImpl.class);

	public boolean execute(TaskInfo taskInfo) {
		List<Map<String, Object>> expiredOrderList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();

		// 查询过期时间小于当前时间的订单
		tempList = ucpaasMessageDao.getSearchList("clientOrderStatus.getExpiredOrderIdsByEndTime", null);
		if (tempList.size() > 0 && tempList != null) {
			expiredOrderList.addAll(tempList);
			logger.debug("【短信客户订单表状态检查任务】，检查客户订单表中过期时间小于当前时间的订单，过期订单ID = {}", expiredOrderList);
		}

		// 查询产品类型为行业和营销的订单中剩余数量小于等于0的订单
		tempList = ucpaasMessageDao.getSearchList("clientOrderStatus.getExpiredOrderIds", null);
		if (tempList.size() > 0 && tempList != null) {
			expiredOrderList.addAll(tempList);
			logger.debug("【短信客户订单表状态检查任务】，查询产品类型为行业和营销的订单中剩余数量小于等于0的订单 ，过期订单ID = {}", expiredOrderList);
		}

		// 更新过期订单的状态
		if (expiredOrderList.size() > 0) {
			Map<String, Object> sqlParams = new HashMap<String, Object>();
			sqlParams.put("expiredOrderList", expiredOrderList);
			int updateNum = ucpaasMessageDao.update("clientOrderStatus.updateOrderStatusBySubId", sqlParams);
			if (updateNum == expiredOrderList.size()) {
				logger.info("【短信客户订单表状态检查任务】，更新过期订单状态[成功]，过期订单ID = {}", expiredOrderList);
			} else {
				logger.info("【短信客户订单表状态检查任务】，更新过期订单状态[失败]，过期订单ID = {}", expiredOrderList);
				return false;
			}
		}

		return true;
	}

	/**
	 * 预付费客户余额预警
	 *
	 * @param taskInfo
	 * @return
	 */
	@Override
	public boolean clientBalanceAlarm(TaskInfo taskInfo) {
		List<JsmsTaskAlarmSetting> taskAlarmSettingList = jsmsTaskAlarmSettingService
				.getByTaskAlarmType(TaskAlarmType.NUM_ALARM);
		for (JsmsTaskAlarmSetting taskAlarmSetting : taskAlarmSettingList) {
			doAlarm(taskAlarmSetting);
		}
		return true;
	}

	private Boolean doAlarm(JsmsTaskAlarmSetting taskAlarmSetting) {
		/*
		 * AgentClientParam agentClientParam =
		 * agentClientParamMapper.getByParamKey("CLIENT_BALANCE_ALARM"); String
		 * paramValue = agentClientParam.getParamValue();
		 * if(!paramValue.matches(
		 * "^(([0,1]\\d)|(2[0-3])):[0-5]\\d\\|(([0,1]\\d)|(2[0-3])):[0-5]\\d&\\d+$"
		 * )){
		 * logger.debug("CLIENT_BALANCE_ALARM参数配置不合法 --> {} ,不执行",paramValue);
		 * return true; }
		 *//**
			 * rate --> 发送短信的间隔 , 应和任务的执行间隔一致
			 *//*
			 * String rate = paramValue.substring(paramValue.indexOf("&") + 1);
			 * 
			 * String startTime = paramValue.substring(0, 5); String endTime =
			 * paramValue.substring(6, 11);
			 * logger.debug("短信发送时间为 [{} , {}]",startTime,endTime);
			 * 
			 * if(rate.matches("^0?$")){
			 * logger.debug("CLIENT_BALANCE_ALARM参数配置 频率 --> {} , 不执行短信发送",rate)
			 * ; return true; } DateTime now = new DateTime();
			 * if(!compareTime(now,startTime) || compareTime(now,endTime)){
			 * logger.debug("当前任务执行时间不在{} ~ {} 范围内, 不执行短信发送",startTime,endTime);
			 * return true; }
			 */
		logger.debug("TaskAlarmSetting 参数配置 --> {} ", JsonUtils.toJson(taskAlarmSetting));
		if (taskAlarmSetting == null || taskAlarmSetting.getStatus() != 1) {
			logger.debug("TaskAlarmSetting 参数配置为空或者无效, 不执行任务 --> {}", JsonUtils.toJson(taskAlarmSetting));
			return true;
		}
		// todo 判断任务短信发送的时间范围
		logger.debug("短信发送时间为 [{} , {}]", taskAlarmSetting.getBeginTime(), taskAlarmSetting.getEndTime());

		if (!taskAlarmSetting.getBeginTime().matches("^(([0,1]\\d)|(2[0-3])):[0-5]\\d$")
				|| !taskAlarmSetting.getEndTime().matches("^(([0,1]\\d)|(2[0-3])):[0-5]\\d$")) {
			logger.debug("TaskAlarmSetting 时间参数配置不合法或者无效, 不执行任务");
			return true;
		}
		DateTime now = new DateTime();
		if (!AlarmCommonUtil.compareTime(now, taskAlarmSetting.getBeginTime())
				|| AlarmCommonUtil.compareTime(now, taskAlarmSetting.getEndTime())) {
			logger.debug("当前任务执行时间不在{} ~ {} 范围内, 不执行短信发送", taskAlarmSetting.getBeginTime(),
					taskAlarmSetting.getEndTime());
			return true;
		}
		// todo 判断执行周期
		logger.debug("执行周期为: {} 分钟, 执行时间为beginTime:{} + (N*{})分钟", taskAlarmSetting.getScanFrequecy() / 60000,
				taskAlarmSetting.getBeginTime(), taskAlarmSetting.getScanFrequecy() / 60000);
		if (!AlarmCommonUtil.isNowOnScan(taskAlarmSetting.getBeginTime(), taskAlarmSetting.getScanFrequecy())) {
			logger.debug("执行周期未到,不执行...");
			return true;
		}

		updateAlarmNum();

		List<Map> clientAlarmInfoList = clientOrderMapper.getClientAlarmInfo();
		logger.debug("设置告警且告警次数 大于 0 的 【客户】 clientAlarmInfo --> {}", clientAlarmInfoList);

		if (clientAlarmInfoList.isEmpty()) {
			logger.debug("设置告警且告警次数 大于 0 的 【客户】 数量 = {} , 不告警", clientAlarmInfoList.size());
			return true;
		}

		Set<String> clientIds = new HashSet<>();
		for (Map map : clientAlarmInfoList) {
			if (map.get("clientId") != null) {
				clientIds.add((String) map.get("clientId"));
			}
		}

		List<JsmsAccount> jsmsAccounts = jsmsAccountService.getByIds(clientIds);
		Set<Long> saleIds = new HashSet<>();
		final Map<String, JsmsAccount> accountMap = new HashMap<>();

		for (JsmsAccount jsmsAccount : jsmsAccounts) {
			accountMap.put(jsmsAccount.getClientid(), jsmsAccount);
			if (jsmsAccount.getBelongSale() == null)
				continue;
			saleIds.add(jsmsAccount.getBelongSale());
		}

		final List<JsmsUser> userList;
		if (!saleIds.isEmpty()) {
			userList = jsmsUserService.getByIds(saleIds);
		} else {
			userList = new ArrayList<>();
		}
		/**
		 * 增加线程池
		 */
		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1, 100, 60, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());

		for (Map clientAlarmInfo : clientAlarmInfoList) {
			/**
			 * 营销 + 行业 剩余数量
			 */
			BigDecimal remainQuantity = (BigDecimal) clientAlarmInfo.get("remainQuantity");
			/**
			 * 告警数量
			 */
			Integer alarmNumber = (Integer) clientAlarmInfo.get("alarmNumber");
			logger.debug("告警数量 alarmNumber --> {}, 剩余数量 remainQuantity --> {}", alarmNumber, remainQuantity);
			if (remainQuantity.compareTo(new BigDecimal(alarmNumber.toString())) <= 0) {

				final String clientId = (String) clientAlarmInfo.get("clientId");
				logger.debug("即将发送的短信余量预警 对应的 clientid --> {}", clientId);
				if (!clientId.matches("^\\w{6}$")) {
					continue;
				}
				final String alarmPhone = (String) clientAlarmInfo.get("alarmPhone");
				logger.debug("即将发送的短信余量预警到客户手机号 --> {}", alarmPhone);
				if (alarmPhone.matches("^\\d{11}(,\\d{11})*,?$")) {
					final List<String> tempParam = new ArrayList<>();
					tempParam.add(0, clientId);
					tempParam.add(1, remainQuantity.setScale(0, BigDecimal.ROUND_DOWN).toString());
					tempParam.add(2, (String) clientAlarmInfo.get("agentName"));

					threadPool.execute(new Runnable() {
						@Override
						public void run() {
							SendSMSUtil.sendAlarmSMS(AlarmConstant.client_sms_alarm_template, alarmPhone, tempParam);
						}
					});
				} // todo ccAlarmPhone

				/*
				 * SendSMSUtil.sendAlarmSMS(AlarmConstant.
				 * client_sms_alarm_template, alarmPhone, params);
				 */

				final List<String> templateParam2 = new ArrayList<>();
				templateParam2.add(0, clientId);
				templateParam2.add(1, accountMap.get(clientId).getName());
				templateParam2.add(2, remainQuantity.setScale(0, BigDecimal.ROUND_DOWN).toString());
				threadPool.execute(new Runnable() {
					@Override
					public void run() {
						sendAlarmSmsToSaler(userList, accountMap.get(clientId).getBelongSale(), templateParam2);
					}
				});
				/*
				 * final List<String> templateParam3 = new ArrayList<>();
				 * templateParam3.add(0, clientId); templateParam3.add(1,
				 * accountMap.get(clientId).getName()); templateParam3.add(2,
				 * remainQuantity.setScale(0,
				 * BigDecimal.ROUND_DOWN).toString());
				 */

				final String operatorMobile = (String) clientAlarmInfo.get("ccAlarmPhone");

				logger.debug("准备发送短信给运营, 运营手机号 = {}", operatorMobile);
				if (StringUtils.isNoneBlank(operatorMobile)) {
					final List<String> templateParam3 = new ArrayList<>();

					for (JsmsUser user : userList) {
						if (user != null && user.getId().equals(accountMap.get(clientId).getBelongSale())) {
							/* "【云之讯】您的客户“{客户ID}（{客户名称}）”，当前短信余额为{余额}，请及时跟进充值确保使用通畅"; */
							templateParam3.add(0, user.getRealname());
							break;
						}
					}
					if (templateParam3.isEmpty()) {
						templateParam3.add(0, "无销售");
					}
					templateParam3.add(1, clientId);
					templateParam3.add(2, accountMap.get(clientId).getName());
					templateParam3.add(3, remainQuantity.setScale(0, BigDecimal.ROUND_DOWN).toString());
					threadPool.execute(new Runnable() {
						@Override
						public void run() {
							sendAlarmSmsToOperator(operatorMobile, templateParam3);
						}
					});
				}

				int reduce = clientBalanceAlarmMapper.reduceReminderNum(clientId);
				logger.debug("短信发送后, 修改 clientid = {} 的 告警次数 , 影响数据库行数 = {}", clientId, reduce);

			}
		}

		return true;
	}

	/**
	 * 比较 DateTime中的 时间部分大小, 只比较 时和分
	 *
	 * @return DateTime > timeString
	 */
	/*
	 * private boolean compareTime(DateTime dateTime,String timeString){
	 * if(dateTime.get(DateTimeFieldType.hourOfDay()) >
	 * Integer.parseInt(timeString.substring(0,2))){ return true; }
	 * if(dateTime.get(DateTimeFieldType.hourOfDay()) ==
	 * Integer.parseInt(timeString.substring(0,2)) &&
	 * dateTime.get(DateTimeFieldType.minuteOfHour()) >=
	 * Integer.parseInt(timeString.substring(3,5))){ return true; } return
	 * false; }
	 */
	private boolean updateAlarmNum() {
		logger.debug("准备重置【有新购买的订单】的客户的告警次数");
		Date now = new Date();
		List<String> newBuyers = clientOrderMapper.getNewBuyer(now);

		logger.debug("有新购买的订单的客户 --> {}", newBuyers);
		if (newBuyers.isEmpty()) {
			logger.debug("从上次重置时间到{},新购买订单的客户数 = {}, 所以不更新", DateFormat.getDateTimeInstance().format(now),
					newBuyers.size());
			return true;
		}
		Map params = new HashMap();
		params.put("date", now);
		params.put("clientIds", newBuyers);
		int update = clientBalanceAlarmMapper.updateReminderNum(params);
		logger.debug("更新 {} 位客户的告警次数", update);

		if (update > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 发送短信给归属销售
	 * 
	 * @param userList
	 * @param saleId
	 * @param templateParam
	 * @return
	 */
	private boolean sendAlarmSmsToSaler(List<JsmsUser> userList, Long saleId, List<String> templateParam) {
		logger.debug("准备发送短信给销售, 销售id = {}", saleId);
		if (userList.isEmpty()) {
			logger.debug("没有归属销售信息不发送短信给销售");
			return true;
		}
		for (JsmsUser user : userList) {
			if (user != null && user.getId().equals(saleId)) {
				/* "【云之讯】您的客户“{客户ID}（{客户名称}）”，当前短信余额为{余额}，请及时跟进充值确保使用通畅"; */
				logger.debug("准备发送短信给销售, 销售手机号 = {}", user.getMobile());
				SendSMSUtil.sendAlarmSMS(AlarmConstant.client_saler_sms_alarm_template, user.getMobile(),
						templateParam);
				return true;
			}
		}
		return false;
	}

	/**
	 * 发送短信给归属销售
	 * 
	 * @param operatorMobile
	 * @param templateParam
	 * @return
	 */
	private boolean sendAlarmSmsToOperator(String operatorMobile, List<String> templateParam) {
		if (StringUtils.isBlank(operatorMobile)) {
			logger.debug("没有设置运营人员手机号信息不发送短信给运营");
			return true;
		}

		/* 【云之讯】{销售}的客户“{客户ID}（{客户名称}）”，当前短信余额为{余额}，请及时跟进充值确保使用通畅 */
		try {
			SendSMSUtil.sendAlarmSMS(AlarmConstant.client_operator_sms_alarm_template, operatorMobile, templateParam);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
