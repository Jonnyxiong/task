package com.ucpaas.sms.task.service;

import com.alibaba.fastjson.JSON;
import com.jsmsframework.common.enums.*;
import com.jsmsframework.finance.entity.JsmsAgentAccount;
import com.jsmsframework.finance.entity.JsmsAgentBalanceAlarm;
import com.jsmsframework.finance.entity.JsmsClientBalanceAlarm;
import com.jsmsframework.finance.entity.JsmsTaskAlarmSetting;
import com.jsmsframework.finance.service.JsmsAgentAccountService;
import com.jsmsframework.finance.service.JsmsAgentBalanceAlarmService;
import com.jsmsframework.finance.service.JsmsAgentBalanceBillService;
import com.jsmsframework.finance.service.JsmsClientBalanceAlarmService;
import com.jsmsframework.order.entity.JsmsOemClientPool;
import com.jsmsframework.order.service.JsmsOemClientOrderService;
import com.jsmsframework.order.service.JsmsOemClientPoolService;
import com.jsmsframework.user.entity.JsmsAccount;
import com.jsmsframework.user.entity.JsmsAgentInfo;
import com.jsmsframework.user.entity.JsmsUser;
import com.jsmsframework.user.service.JsmsAccountService;
import com.jsmsframework.user.service.JsmsAgentInfoService;
import com.jsmsframework.user.service.JsmsUserService;
import com.ucpaas.sms.common.util.Collections3;
import com.ucpaas.sms.common.util.StringUtils;
import com.ucpaas.sms.task.constant.AlarmConstant;
import com.ucpaas.sms.task.dao.MessageMasterDao;
import com.ucpaas.sms.task.mapper.message.ClientBalanceAlarmMapper;
import com.ucpaas.sms.task.mapper.message.ClientOrderMapper;
import com.ucpaas.sms.task.service.common.EmailService;
import com.ucpaas.sms.task.util.SendSMSUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.*;

@Service
public class BalanceAlarmDoService {
	private static final Logger logger = LoggerFactory.getLogger(BalanceAlarmDoService.class);

	@Autowired
	private JsmsAgentInfoService jsmsAgentInfoService;

	@Autowired
	private JsmsAgentBalanceAlarmService jsmsAgentBalanceAlarmService;

	@Autowired
	private JsmsAgentBalanceBillService jsmsAgentBalanceBillService;

	@Autowired
	private JsmsAgentAccountService jsmsAgentAccountService;

	@Autowired
	private JsmsUserService jsmsUserService;

	@Autowired
	private JsmsClientBalanceAlarmService jsmsClientBalanceAlarmService;

	@Autowired
	private JsmsAccountService jsmsAccountService;

	@Autowired
	private JsmsOemClientOrderService jsmsOemClientOrderService;

	@Autowired
	private JsmsOemClientPoolService jsmsOemClientPoolService;

	@Autowired
	private ClientOrderMapper clientOrderMapper;

	@Autowired
	private ClientBalanceAlarmMapper clientBalanceAlarmMapper;

	@Autowired
	private EmailService emailService;

	@Autowired
	private MessageMasterDao messageMasterDao;

	private JsmsAgentBalanceAlarm getAgentBalanceAlarm(List<JsmsAgentBalanceAlarm> agentBalanceAlarmList,
			Integer agentId) {
		JsmsAgentBalanceAlarm balanceAlarm = null;
		for (JsmsAgentBalanceAlarm jsmsAgentBalanceAlarm : agentBalanceAlarmList) {
			if (jsmsAgentBalanceAlarm.getAgentId() == null) {
				continue;
			}

			if (jsmsAgentBalanceAlarm.getAgentId().intValue() == agentId.intValue()) {
				balanceAlarm = jsmsAgentBalanceAlarm;
				break;
			}
		}
		return balanceAlarm;
	}

	private JsmsClientBalanceAlarm getClientBalanceAlarm(List<JsmsClientBalanceAlarm> clientBalanceAlarms,
			String clientid, int type) {
		JsmsClientBalanceAlarm balanceAlarm = null;
		for (JsmsClientBalanceAlarm jsmsClientBalanceAlarm : clientBalanceAlarms) {
			if (jsmsClientBalanceAlarm.getClientid() == null) {
				continue;
			}

			if (jsmsClientBalanceAlarm.getClientid().equals(clientid)
					&& jsmsClientBalanceAlarm.getAlarmType().intValue() == type) {
				balanceAlarm = jsmsClientBalanceAlarm;
				break;
			}
		}
		return balanceAlarm;
	}

	private JsmsUser getBelongSale(List<JsmsUser> userList, Long saleId) {
		JsmsUser user = null;
		if (saleId == null) {
			return user;
		}

		for (JsmsUser jsmsUser : userList) {
			if (jsmsUser.getId().intValue() == saleId.intValue()) {
				user = jsmsUser;
				break;
			}
		}
		return user;
	}

	private JsmsUser getAgentBelongSale(List<JsmsUser> userList, Long adminId) {
		JsmsUser user = null;
		if (adminId == null) {
			return user;
		}

		for (JsmsUser jsmsUser : userList) {
			if (jsmsUser.getId().intValue() == adminId.intValue()) {
				user = jsmsUser;
				break;
			}
		}

		if (user == null) {
			user = jsmsUserService.getById(adminId.toString());
			userList.add(user);
		}

		return user;
	}

	private JsmsAgentInfo getAgentInfo(List<JsmsAgentInfo> jsmsAgentInfoList, Integer agentId) {
		JsmsAgentInfo agentInfo = null;
		if (agentId == null) {
			return agentInfo;
		}

		for (JsmsAgentInfo jsmsAgentInfo : jsmsAgentInfoList) {
			if (jsmsAgentInfo.getAgentId().intValue() == agentId.intValue()) {
				agentInfo = jsmsAgentInfo;
				break;
			}
		}
		return agentInfo;
	}

	private boolean checkWebIdWithAgent(JsmsAgentInfo agentInfo, JsmsTaskAlarmSetting taskAlarmSetting) {
		// 1短信调度系统 2代理商平台 3运营平台 4OEM代理商平台 5品牌客户端 6OEM客户端
		// 1:销售代理商,2:品牌代理商,3:资源合作商,4:代理商和资源合作,5:OEM代理商

		logger.debug("代理商余额提醒：代理商类型= {}, 应用系统 = {} ", agentInfo.getAgentType(), taskAlarmSetting.getWebId());
		if (agentInfo.getAgentType().equals(2) && taskAlarmSetting.getWebId().equals(2)) { // 2:品牌代理商
			return true;
		} else if (agentInfo.getAgentType().equals(1) && taskAlarmSetting.getWebId().equals(2)) { // 1:销售代理商
			return true;
		} else if (agentInfo.getAgentType().equals(5) && taskAlarmSetting.getWebId().equals(4)) { // 5:OEM代理商
			return true;
		}
		return false;
	}

	private boolean updateAgentAlarmNum(DateTime now, JsmsTaskAlarmSetting taskAlarmSetting) {
		logger.debug("代理商余额提醒：准备重置【有新购买的订单】的客户的告警次数");
		List<Integer> newBuyers = jsmsAgentBalanceBillService.getJustChargeAgentIds(now.toDate());
		logger.debug("有新充值的代理商 --> {}", newBuyers);
		if (newBuyers.isEmpty()) {
			logger.debug("从上次重置时间到{},有新充值的代理商数 = {}, 所以不更新", now.toString(), 0);
			return true;
		}
		/**
		 * 此处判断设置的应用类型是否和webid是否对用
		 */
		Set<Integer> tempIds = new HashSet<>();
		tempIds.addAll(newBuyers);
		List<JsmsAgentInfo> tempAgentInfoList = jsmsAgentInfoService.getByAgentIds(tempIds);

		for (JsmsAgentInfo agentInfo : tempAgentInfoList) {
			if (agentInfo == null || !checkWebIdWithAgent(agentInfo, taskAlarmSetting)) {
				continue;
			}
			JsmsAgentBalanceAlarm temp = new JsmsAgentBalanceAlarm();
			temp.setAgentId(agentInfo.getAgentId());
			temp.setReminderNumber(taskAlarmSetting.getTaskAlarmFrequecy());
			temp.setResetTime(now.toDate());
			int update = jsmsAgentBalanceAlarmService.updateByAgentId(temp);
			logger.debug("代理商余额提醒：更新新充值的代理商【{}】的告警次数 , 更新成功 true or false ?--> {} ", agentInfo.getAgentId(),
					update > 0 ? true : false); // todo
		}

		return true;
	}

	private boolean updateClientAlarmNum() {
		logger.debug("品牌客户短信余额提醒: 准备重置【有新购买的订单】的客户的告警次数");
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

	private boolean updateOemClientAlarmNum(DateTime date, Set<String> clientIds) {
		logger.debug("OEM客户短信余额提醒: 准备重置Oem预警设置【有新购买的订单】的客户的告警次数");
		Date now = date.toDate();

		List<String> newBuyers;

		// 验证码
		newBuyers = jsmsOemClientOrderService.getNewBuyerNew(now, Arrays.asList(ProductType.验证码.getValue().toString(), ProductType.行业.getValue().toString()),
				ClientAlarmType.验证码.getValue().toString(), clientIds);
		if (Collections3.isEmpty(newBuyers)) {
			logger.debug("从上次重置时间到{}, 新购买验证码订单的客户数 = {}, 所以不更新重置次数", DateFormat.getDateTimeInstance().format(now),
					newBuyers.size());
		} else {
			Map params = new HashMap();
			params.put("date", now);
			params.put("clientIds", newBuyers);
			params.put("alarmType", ClientAlarmType.验证码.getValue().toString());
			int update = clientBalanceAlarmMapper.updateReminderNum(params);
			logger.debug("更新 {} 位验证码订单客户的告警次数", update);
		}

		// 通知
		newBuyers = jsmsOemClientOrderService.getNewBuyerNew(now, Arrays.asList(ProductType.通知.getValue().toString(), ProductType.行业.getValue().toString()),
				ClientAlarmType.通知.getValue().toString(), clientIds);
		if (Collections3.isEmpty(newBuyers)) {
			logger.debug("从上次重置时间到{}, 新购买通知订单的客户数 = {}, 所以不更新重置次数", DateFormat.getDateTimeInstance().format(now),
					newBuyers.size());
		} else {
			Map params = new HashMap();
			params.put("date", now);
			params.put("clientIds", newBuyers);
			params.put("alarmType", ClientAlarmType.通知.getValue().toString());
			int update = clientBalanceAlarmMapper.updateReminderNum(params);
			logger.debug("更新 {} 位通知订单客户的告警次数", update);
		}

		// 营销
		newBuyers = jsmsOemClientOrderService.getNewBuyerNew(now, Arrays.asList(ProductType.营销.getValue().toString()),
				ClientAlarmType.营销.getValue().toString(), clientIds);
		if (Collections3.isEmpty(newBuyers)) {
			logger.debug("从上次重置时间到{}, 新购买营销订单的客户数 = {}, 所以不更新重置次数", DateFormat.getDateTimeInstance().format(now),
					newBuyers.size());
		} else {
			Map params = new HashMap();
			params.put("date", now);
			params.put("clientIds", newBuyers);
			params.put("alarmType", ClientAlarmType.营销.getValue().toString());
			int update = clientBalanceAlarmMapper.updateReminderNum(params);
			logger.debug("更新 {} 位营销订单客户的告警次数", update);
		}

		// 国际
		newBuyers = jsmsOemClientOrderService.getNewBuyerNew(now, Arrays.asList(ProductType.国际.getValue().toString()),
				ClientAlarmType.国际.getValue().toString(), clientIds);
		if (Collections3.isEmpty(newBuyers)) {
			logger.debug("从上次重置时间到{}, 新购买国际订单的客户数 = {}, 所以不更新重置次数", DateFormat.getDateTimeInstance().format(now),
					newBuyers.size());
		} else {
			Map params = new HashMap();
			params.put("date", now);
			params.put("clientIds", newBuyers);
			params.put("alarmType", ClientAlarmType.国际.getValue().toString());
			int update = clientBalanceAlarmMapper.updateReminderNum(params);
			logger.debug("更新 {} 位国际订单客户的告警次数", update);
		}

		return true;
	}

	/**
	 * 代理商进行短信提醒
	 * 
	 * @param now
	 * @param taskAlarmSetting
	 * @param agentType
	 * @return
	 */
	@Transactional("message")
	public boolean doAlarmAgent(DateTime now, JsmsTaskAlarmSetting taskAlarmSetting, AgentType agentType) {
		String desc = agentType.getValue().intValue() == AgentType.品牌代理商.getValue().intValue() ? "品牌代理商可用额度提醒"
				: "OEM代理商可用额度提醒";

		// 若有代理商已进行充值或授信并且时间在 告警提醒配置后面的代理商，将告警次数重置时间更新为现在的时间
		updateAgentAlarmNum(now, taskAlarmSetting);

		// 查询所有的代理商
		JsmsAgentInfo queryAgentInfo = new JsmsAgentInfo();
		queryAgentInfo.setAgentType(agentType.getValue());
		queryAgentInfo.setStatus("1"); // 正常的
		queryAgentInfo.setOauthStatus(3); // 认证通过

		// 查询需要告警的代理商
		List<JsmsAgentInfo> jsmsAgentInfoList = jsmsAgentInfoService.findList(queryAgentInfo);
		if (Collections3.isEmpty(jsmsAgentInfoList)) {
			logger.debug("{}: 无代理商", desc);
			return true;
		}

		// 查询所有的代理商对应的预警配置项
		Set<Integer> agentIds = new HashSet<>();
		for (JsmsAgentInfo agentInfo : jsmsAgentInfoList) {
			agentIds.add(agentInfo.getAgentId());
		}
		List<JsmsAgentBalanceAlarm> agentBalanceAlarmList = jsmsAgentBalanceAlarmService
				.findList(new JsmsAgentBalanceAlarm(), agentIds);

		// 构造代理商ID列表、销售列表
		Map<Integer, JsmsAgentInfo> agentMap = new HashMap<>();
		Set<Long> saleIds = new HashSet<>();
		for (JsmsAgentInfo agentInfo : jsmsAgentInfoList) {
			agentMap.put(agentInfo.getAgentId(), agentInfo);
			if (agentInfo.getBelongSale() != null) {
				saleIds.add(agentInfo.getBelongSale());
			}
		}

		// 获取代理商的(金额)账户信息
		List<JsmsAgentAccount> agentAccountList = jsmsAgentAccountService.getByAgentIds(agentIds);
		if (agentAccountList.isEmpty()) {
			logger.debug("{}: 没有查询到需要提醒的代理商 --> agentAccountList = {}", desc, JSON.toJSONString(agentAccountList));
			return true;
		}

		// 查询归属销售信息
		List<JsmsUser> userList = null;
		if (!Collections3.isEmpty(saleIds)) {
			userList = jsmsUserService.getByIds(saleIds);
		}

		// 循环处理所有的代理商信息
		for (JsmsAgentAccount agentAccount : agentAccountList) {
			JsmsAgentInfo agentInfo = agentMap.get(agentAccount.getAgentId());

			// 获取预警配置
			JsmsAgentBalanceAlarm agentBalanceAlarm = getAgentBalanceAlarm(agentBalanceAlarmList,
					agentAccount.getAgentId());
			logger.debug("{}: 代理商信息 = {} 提醒配置 {}", desc, JSON.toJSONString(agentAccount),
					JSON.toJSONString(agentBalanceAlarm));

			boolean isExist = true;
			if (agentBalanceAlarm == null) {
				isExist = false;

				// 查询电话
				agentBalanceAlarm = new JsmsAgentBalanceAlarm();
				agentBalanceAlarm.setAgentId(agentAccount.getAgentId());
				agentBalanceAlarm.setAlarmPhone(agentInfo.getMobile());
				agentBalanceAlarm.setAlarmAmount(taskAlarmSetting.getTaskAlarmAmount()); // 设置为默认的
				agentBalanceAlarm.setUpdateTime(now.toDate());
				agentBalanceAlarm.setCreateTime(now.toDate());
				agentBalanceAlarm.setResetTime(now.toDate());
				agentBalanceAlarm.setReminderNumber(1); // 本次提醒，所以不设置为1
			}

			if (agentBalanceAlarm.getReminderNumber() == 0) {
				logger.debug("已经提醒过");
				continue;
			}

			// 算出可用额度
			BigDecimal enableMoney = BigDecimal.ZERO;
			if (agentAccount.getBalance().compareTo(BigDecimal.ZERO) < 0) {
				enableMoney = agentAccount.getCurrentCredit();
			} else {
				enableMoney = agentAccount.getBalance().add(agentAccount.getCurrentCredit());
			}

			logger.debug(
					"{}: agentId = {} ,账户余额(元) balance = {}, 历史信用额度(元) credit_balance --> {} ,  当前信用额度(元) current_credit --> {} , 可用额度 --> {}",
					desc, agentAccount.getAgentId(), agentAccount.getBalance(), agentAccount.getCreditBalance(),
					agentAccount.getCurrentCredit(), enableMoney);

			Boolean isContinue = false;

			// 判断是否达到预警阀值，若配置的预警阀值为空，设置成公用的值
			BigDecimal alarmAmount = agentBalanceAlarm.getAlarmAmount();
			if (alarmAmount != null) {
				// 若提醒次数为0，提醒金额为0，不再提醒
				if (agentBalanceAlarm.getReminderNumber().intValue() == 0
						|| alarmAmount.compareTo(BigDecimal.ZERO) == 0) {
					logger.debug("{}: agentId = {} , 提醒次数为0或提醒金额为0，不再提醒", desc, agentAccount.getAgentId());
					isContinue = true;
				}
			}

			// 判断余额是否可以告警
			if (agentBalanceAlarm.getReminderNumber() <= 0
					|| alarmAmount.compareTo(enableMoney.setScale(2, BigDecimal.ROUND_DOWN)) < 0) {
				isContinue = true;
			}

			if (isContinue) {
				// 若不存在告警，插入数据
				if (!isExist) {
					logger.debug("{}: 提醒配置不存在，插入 {}", desc, JSON.toJSONString(agentBalanceAlarm));
					jsmsAgentBalanceAlarmService.insert(agentBalanceAlarm);
				}

				continue;
			}

			List<String> templateParam = new ArrayList<>();

			// 发送给销售
			JsmsUser user = getBelongSale(userList, agentInfo.getBelongSale());
			if (user == null || StringUtils.isBlank(user.getMobile())) {
				logger.debug("{}: 归属销售或归属销售的电话为空 {}", desc, JSON.toJSONString(user));
			} else {
				templateParam.clear();
				templateParam.add(0, agentBalanceAlarm.getAgentId().toString()); // 代理商ID
				templateParam.add(1, StringUtils.isBlank(agentInfo.getAgentName()) ? "" : agentInfo.getAgentName()); // 代理商名称
				SendSMSUtil.sendAlarmSMS(taskAlarmSetting.getSaleAlarmContent(), user.getMobile(), templateParam);
			}

			// 发送给运营
			if (StringUtils.isBlank(taskAlarmSetting.getTaskAlarmPhone())) {
				logger.debug("{}: 运营配置手机号为空，不再发送运营的提醒", desc);
			} else {
				templateParam.clear();
				if (user == null) {
					templateParam.add(0, "");
				} else {
					templateParam.add(0, StringUtils.isBlank(user.getRealname()) ? "" : user.getRealname());
				}

				templateParam.add(1, agentBalanceAlarm.getAgentId().toString()); // 代理商ID
				templateParam.add(2, StringUtils.isBlank(agentInfo.getAgentName()) ? "" : agentInfo.getAgentName()); // 代理商名称
				SendSMSUtil.sendAlarmSMS(taskAlarmSetting.getTaskAlarmContent(), taskAlarmSetting.getTaskAlarmPhone(),
						templateParam);
			}

			// 发送给客户
			if (StringUtils.isBlank(agentBalanceAlarm.getAlarmPhone())) {
				logger.debug("{}: 客户的提醒手机号为空，不再发送客户的提醒", desc);
			} else {
				// 可用余额
				templateParam.clear();
				templateParam.add(0, enableMoney.setScale(2, BigDecimal.ROUND_DOWN).toString()); // 余额

				// 发送短信
				SendSMSUtil.sendAlarmSMS(taskAlarmSetting.getUserAlarmContent(), agentBalanceAlarm.getAlarmPhone(),
						templateParam);
			}

			if (isExist) {
				// 更新该代理商的告警次数为0
				JsmsAgentBalanceAlarm tempParam = new JsmsAgentBalanceAlarm();
				tempParam.setAgentId(agentBalanceAlarm.getAgentId());
				tempParam.setReminderNumber(0);
				tempParam.setResetTime(now.toDate());
				tempParam.setUpdateTime(now.toDate());
				int reduce = jsmsAgentBalanceAlarmService.updateByAgentId(tempParam);
				logger.debug("{}: 短信发送后, 修改 agentId = {} 的 告警次数 , 影响数据库行数 = {}", desc, agentBalanceAlarm.getAgentId(),
						reduce);
			} else {
				logger.debug("{}: 提醒配置不存在，插入 {}", desc, JSON.toJSONString(agentBalanceAlarm));
				agentBalanceAlarm.setReminderNumber(0);
				int reduce = jsmsAgentBalanceAlarmService.insert(agentBalanceAlarm);
				logger.debug("{}: 短信发送后, 修改 agentId = {} 的 告警次数 , 影响数据库行数 = {}", desc, agentBalanceAlarm.getAgentId(),
						reduce);
			}
		}

		return true;
	}

	@Transactional("message")
	public boolean doAlarmClient(DateTime now, JsmsTaskAlarmSetting taskAlarmSetting) {
		updateClientAlarmNum();

		List<Map> clientAlarmInfoList = clientOrderMapper.getClientAlarmInfo();
		logger.debug("品牌客户短信余额提醒: 设置告警且告警次数 大于 0 的 【客户】 clientAlarmInfo --> {}", clientAlarmInfoList);

		if (clientAlarmInfoList.isEmpty()) {
			logger.debug("品牌客户短信余额提醒: 设置告警且告警次数 大于 0 的 【客户】 数量 = {} , 不告警", clientAlarmInfoList.size());
			return true;
		}

		Set<String> clientIds = new HashSet<>();
		for (Map map : clientAlarmInfoList) {
			if (map.get("clientId") != null) {
				clientIds.add(map.get("clientId").toString());
			}
		}

		List<JsmsAccount> jsmsAccounts = jsmsAccountService.getByIds(clientIds);
		Set<Long> saleIds = new HashSet<>();
		Map<String, JsmsAccount> accountMap = new HashMap<>();

		for (JsmsAccount jsmsAccount : jsmsAccounts) {
			accountMap.put(jsmsAccount.getClientid(), jsmsAccount);
			if (jsmsAccount.getBelongSale() == null)
				continue;
			saleIds.add(jsmsAccount.getBelongSale());
		}

		List<JsmsUser> userList;
		if (!saleIds.isEmpty()) {
			userList = jsmsUserService.getByIds(saleIds);
		} else {
			userList = new ArrayList<>();
		}

		// 查询邮箱模版
		Map<String, Object> smsMailPropParams = new HashMap<>();
		smsMailPropParams.put("id", 100025);
		Map<String, Object> smsMailpropMap = this.messageMasterDao.getOneInfo("sendReprotMonth.querySmsMailprop",
				smsMailPropParams);

		for (Map clientAlarmInfo : clientAlarmInfoList) {
			/**
			 * 营销 + 行业 剩余数量
			 */
			BigDecimal remainQuantity = (BigDecimal) clientAlarmInfo.get("remainQuantity");

			/**
			 * 告警数量
			 */
			Integer alarmNumber = (Integer) clientAlarmInfo.get("alarmNumber");

			logger.debug("品牌客户短信余额提醒: 告警数量 alarmNumber --> {}, 剩余数量 remainQuantity --> {}", alarmNumber,
					remainQuantity);
			if (remainQuantity.compareTo(new BigDecimal(alarmNumber.toString())) <= 0) {
				String clientId = clientAlarmInfo.get("clientId").toString();

				logger.debug("品牌客户短信余额提醒: 即将发送的短信余量预警 对应的 clientid --> {}", clientId);
				if (!clientId.matches("^\\w{6}$")) {
					logger.debug("品牌客户短信余额提醒: clientid错误 --> {}", clientId);
					continue;
				}

				List<String> tempParam = new ArrayList<>();

				// 提醒客户
				String alarmPhone = clientAlarmInfo.get("alarmPhone") != null
						? clientAlarmInfo.get("alarmPhone").toString() : null;
				logger.debug("即将发送的短信余量预警到客户手机号 --> {}", alarmPhone);
				if (StringUtils.isNotBlank(alarmPhone) && alarmPhone.matches("^\\d{11}(,\\d{11})*,?$")) {
					tempParam.clear();
					tempParam.add(0, clientId);
					tempParam.add(1, remainQuantity.setScale(0, BigDecimal.ROUND_DOWN).toString());
					tempParam.add(2, (String) clientAlarmInfo.get("agentName"));

					SendSMSUtil.sendAlarmSMS(AlarmConstant.client_sms_alarm_template, alarmPhone, tempParam);
				}

				// 提醒销售
				tempParam.clear();
				tempParam.add(0, clientId);
				tempParam.add(1, accountMap.get(clientId).getName());
				tempParam.add(2, remainQuantity.setScale(0, BigDecimal.ROUND_DOWN).toString());
				JsmsUser user = getBelongSale(userList, accountMap.get(clientId).getBelongSale());
				if (user == null || StringUtils.isBlank(user.getMobile())) {
					logger.debug("没有归属销售信息不发送短信给销售");
				} else {
					logger.debug("准备发送短信给销售, 销售手机号 = {}", user.getMobile());
					SendSMSUtil.sendAlarmSMS(AlarmConstant.client_saler_sms_alarm_template, user.getMobile(),
							tempParam);
				}

				// 提醒运营
				logger.debug("品牌客户短信余额提醒: 准备发送短信给运营, 运营手机号 = {}", taskAlarmSetting.getTaskAlarmPhone());
				if (StringUtils.isNotBlank(taskAlarmSetting.getTaskAlarmPhone())) {
					tempParam.clear();
					if (user == null) {
						tempParam.add(0, "无销售");
					} else {
						tempParam.add(0, StringUtils.isBlank(user.getRealname()) ? "" : user.getRealname());
					}
					tempParam.add(1, clientId);
					tempParam.add(2, accountMap.get(clientId).getName());
					tempParam.add(3, remainQuantity.setScale(0, BigDecimal.ROUND_DOWN).toString());
					SendSMSUtil.sendAlarmSMS(AlarmConstant.client_operator_sms_alarm_template,
							taskAlarmSetting.getTaskAlarmPhone(), tempParam);
				} else {
					logger.debug("品牌客户短信余额提醒: 没有设置运营人员手机号信息不发送短信给运营");
				}

				// 发送邮件
				Object alarmEmail = clientAlarmInfo.get("alarmEmail");
				if (alarmEmail != null && StringUtils.isNotBlank(alarmEmail.toString()) && smsMailpropMap != null
						&& smsMailpropMap.size() > 0) {
					String frm = (String) smsMailpropMap.get("frm");
					String subject = (String) smsMailpropMap.get("subject");
					String body = (String) smsMailpropMap.get("text");

					body = body.replace("clientId", clientId);
					body = body.replace("smsCount", remainQuantity.setScale(0, BigDecimal.ROUND_DOWN).toString());
					body = body.replace("agentName", (String) clientAlarmInfo.get("agentName"));

					this.emailService.sendHtmlEmail(frm, alarmEmail.toString(), subject, body);
				}

				int reduce = clientBalanceAlarmMapper.reduceReminderNum(clientId);
				logger.debug("品牌客户短信余额提醒: 短信发送后, 修改 clientid = {} 的 告警次数 , 影响数据库行数 = {}", clientId, reduce);

			}
		}

		return true;
	}

	@Transactional("message")
	public boolean doAlarmOemClient(DateTime now, JsmsTaskAlarmSetting taskAlarmSetting) {
		// 查询所有的OEM代理商
		JsmsAgentInfo queryAgentInfo = new JsmsAgentInfo();
		queryAgentInfo.setAgentType(AgentType.OEM代理商.getValue());
		queryAgentInfo.setStatus("1"); // 正常的
		queryAgentInfo.setOauthStatus(3); // 认证通过
		List<JsmsAgentInfo> jsmsAgentInfoList = jsmsAgentInfoService.findList(queryAgentInfo);
		if (Collections3.isEmpty(jsmsAgentInfoList)) {
			logger.debug("OEM客户短信余额提醒: 无代理商");
			return true;
		}

		Set<Integer> agentIds = new HashSet<>();
		for (JsmsAgentInfo agentInfo : jsmsAgentInfoList) {
			agentIds.add(agentInfo.getAgentId());
		}

		// 查询所有的OEM客户
		JsmsAccount queryAccount = new JsmsAccount();
		queryAccount.setOauthStatus(3); // 认证通过的
		queryAccount.setPaytype(PayType.预付费.getValue()); // 预付费
		List<JsmsAccount> accounts = jsmsAccountService.findList(queryAccount, agentIds);

		if (Collections3.isEmpty(accounts)) {
			logger.debug("OEM客户为空");
			return true;
		}

		// 移除注销的
		Iterator<JsmsAccount> it = accounts.iterator();
		while (it.hasNext()) {
			JsmsAccount account = it.next();
			if (account.getStatus().intValue() == 6) {
				it.remove();
			}
		}

		Set<Long> saleIds = new HashSet<>();
		for (JsmsAccount account : accounts) {
			if (account.getBelongSale() != null) {
				saleIds.add(account.getBelongSale());
			}
		}

		Set<String> clientIds = new HashSet<>();
		for (JsmsAccount account : accounts) {
			clientIds.add(account.getClientid());
		}

		// 若有刚刚购买订单的客户，重置预警信息
		updateOemClientAlarmNum(now, clientIds);

		// 查询所有的客户余额设置
		List<JsmsClientBalanceAlarm> clientBalanceAlarmList = jsmsClientBalanceAlarmService
				.findListAlarm(new JsmsClientBalanceAlarm(), clientIds);

		// 构造代理商ID列表、销售列表
		Map<Integer, JsmsAgentInfo> agentMap = new HashMap<>();
		for (JsmsAgentInfo agentInfo : jsmsAgentInfoList) {
			agentMap.put(agentInfo.getAgentId(), agentInfo);
		}

		// 获取客户池信息
		List<JsmsOemClientPool> clientPools = jsmsOemClientPoolService.findSUMTotal(clientIds);
		if (clientPools.isEmpty()) {
			logger.debug("OEM客户短信余额提醒: 客户池为空 ");
			return true;
		}

		// 查询归属销售信息
		List<JsmsUser> userList = null;
		if (!Collections3.isEmpty(saleIds)) {
			userList = jsmsUserService.getByIds(saleIds);
		}

		// 查询邮箱模版
		Map<String, Object> smsMailPropParams = new HashMap<>();
		smsMailPropParams.put("id", 100026);
		Map<String, Object> smsMailpropMap = this.messageMasterDao.getOneInfo("sendReprotMonth.querySmsMailprop",
				smsMailPropParams);

		for (JsmsAccount account : accounts) {
			JsmsClientBalanceAlarm balanceAlarm;
			JsmsAgentInfo agentInfo = getAgentInfo(jsmsAgentInfoList, account.getAgentId());
			JsmsUser agengUser = getAgentBelongSale(userList, agentInfo.getAdminId());
			JsmsUser user = getBelongSale(userList, account.getBelongSale());

			// 通知
			balanceAlarm = getClientBalanceAlarm(clientBalanceAlarmList, account.getClientid(),
					ClientAlarmType.通知.getValue().intValue());
			dealOemClientBalance(balanceAlarm, account, agentInfo, user, agengUser, clientPools, ProductType.通知,
					SmsTypeEnum.通知,
					ClientAlarmType.通知, taskAlarmSetting, smsMailpropMap, now);

			// 验证码
			balanceAlarm = getClientBalanceAlarm(clientBalanceAlarmList, account.getClientid(),
					ClientAlarmType.验证码.getValue().intValue());
			dealOemClientBalance(balanceAlarm, account, agentInfo, user, agengUser, clientPools, ProductType.验证码,
					SmsTypeEnum.验证码,
					ClientAlarmType.验证码, taskAlarmSetting, smsMailpropMap, now);

			// 营销
			balanceAlarm = getClientBalanceAlarm(clientBalanceAlarmList, account.getClientid(),
					ClientAlarmType.营销.getValue().intValue());
			dealOemClientBalance(balanceAlarm, account, agentInfo, user, agengUser, clientPools, ProductType.营销,
					SmsTypeEnum.营销,
					ClientAlarmType.营销, taskAlarmSetting, smsMailpropMap, now);

			// 国际
			balanceAlarm = getClientBalanceAlarm(clientBalanceAlarmList, account.getClientid(),
					ClientAlarmType.国际.getValue().intValue());
			dealOemClientBalance(balanceAlarm, account, agentInfo, user, agengUser, clientPools, ProductType.国际,
					null,
					ClientAlarmType.国际, taskAlarmSetting, smsMailpropMap, now);
		}

		return true;
	}

	private void dealOemClientBalance(JsmsClientBalanceAlarm balanceAlarm, JsmsAccount account, JsmsAgentInfo agentInfo,
			JsmsUser user, JsmsUser agengUser, List<JsmsOemClientPool> clientPools, ProductType productType, SmsTypeEnum smsTypeEnum,
			ClientAlarmType alarmType, JsmsTaskAlarmSetting taskAlarmSetting, Map<String, Object> smsMailpropMap,
			DateTime now) {
		// 获取预警配置
		logger.debug("OEM客户短信余额提醒: 客户信息 = {} 提醒类型 {} 提醒配置 {}", JSON.toJSONString(account), alarmType.getDesc(),
				JSON.toJSONString(balanceAlarm));

		boolean isGj = alarmType.getValue().intValue() == ClientAlarmType.国际.getValue().intValue();

		// 算出短信余额，算出池数量
		int poolCount = 0;
		BigDecimal count = BigDecimal.ZERO;
		for (JsmsOemClientPool clientPool : clientPools) {
			if (!clientPool.getClientId().equals(account.getClientid())) {
				continue;
			}

			// 行业和对应产品类型的进来
			if (clientPool.getProductType().intValue() == ProductType.行业.getValue().intValue()
					|| clientPool.getProductType().intValue() == productType.getValue().intValue()) {

				if (isGj) {
					// 若是行业的跳过
					if (clientPool.getProductType().intValue() == ProductType.行业.getValue().intValue()) {
						continue;
					}

					poolCount++;
					count = count.add(clientPool.getRemainAmount());
					continue;
				} else {
					// 营销不加行业
					if (alarmType.getValue().intValue() == ClientAlarmType.营销.getValue().intValue()) {
						// 若是行业的跳过
						if (clientPool.getProductType().intValue() == ProductType.行业.getValue().intValue()) {
							continue;
						}

						poolCount++;
						count = count.add(new BigDecimal(clientPool.getRemainNumber().toString()));
					}
					// 通知和验证码需要累加行业
					else {
						poolCount++;
						count = count.add(new BigDecimal(clientPool.getRemainNumber().toString()));
						continue;
					}
				}
			}
		}

		// 算出短信余额
		String balance = isGj ? count.setScale(2, BigDecimal.ROUND_DOWN).toString() : String.valueOf(count.intValue());

		// 若余额设置为空，初始化余额设置
		if (balanceAlarm == null) {
			balanceAlarm = new JsmsClientBalanceAlarm();
			balanceAlarm.setClientid(account.getClientid());
			if (StringUtils.isNotBlank(agentInfo.getMobile())) {
				balanceAlarm.setAlarmPhone(agentInfo.getMobile());
			}
			if (StringUtils.isNotBlank(account.getMobile())) {
				if (StringUtils.isNotBlank(balanceAlarm.getAlarmPhone())) {
					balanceAlarm.setAlarmPhone(balanceAlarm.getAlarmPhone() + "," + account.getMobile());
				} else {
					balanceAlarm.setAlarmPhone(account.getMobile());
				}

			}

			if (StringUtils.isNotBlank(agengUser.getEmail())) {
				balanceAlarm.setAlarmEmail(agengUser.getEmail());
			}

			if (StringUtils.isNotBlank(account.getEmail())) {
				if (StringUtils.isNotBlank(balanceAlarm.getAlarmEmail())) {
					balanceAlarm.setAlarmEmail(balanceAlarm.getAlarmEmail() + "," + account.getEmail());
				} else {
					balanceAlarm.setAlarmEmail(account.getEmail());
				}
			}

			if (isGj) {
				balanceAlarm.setAlarmNumber(null);
				balanceAlarm.setAlarmAmount(new BigDecimal("10"));
			} else {
				balanceAlarm.setAlarmNumber(500);
				balanceAlarm.setAlarmAmount(null);
			}

			balanceAlarm.setAlarmType(alarmType.getValue());
			balanceAlarm.setUpdateTime(now.toDate());
			balanceAlarm.setCreateTime(now.toDate());
			balanceAlarm.setResetTime(now.toDate());

			balanceAlarm.setReminderNumber(0); // 默认为0
			if (poolCount > 0)
			{
				if (isGj)
				{
					balanceAlarm.setReminderNumber(1);
				} else {
					if (account.getSmsfrom().intValue() == SmsFrom.HTTPS.getValue().intValue())
					{
						balanceAlarm.setReminderNumber(1); // 本次提醒，所以不设置为1
					} else {
						if (account.getSmstype() == smsTypeEnum.getValue().intValue())
						{
							balanceAlarm.setReminderNumber(1); // 本次提醒，所以不设置为1
						}
					}
				}
			}

			int addCount = jsmsClientBalanceAlarmService.insert(balanceAlarm);
			logger.debug("OEM客户短信余额提醒: 客户的{}短信余额预警未设置，添加记录 {}, 影响数据库行数 = {}", alarmType.getDesc(),
					JSON.toJSONString(balanceAlarm), addCount);
		}

		if (balanceAlarm.getReminderNumber() <= 0) {
			logger.debug("OEM客户短信余额提醒: 客户ID = {} 提醒类型 {} 可提醒次数为0 ", account.getClientid(), alarmType.getDesc());
			return;
		}

		boolean needSend = false;
		if (isGj) {
			if (balanceAlarm.getAlarmAmount() == null
					|| balanceAlarm.getAlarmAmount().compareTo(BigDecimal.ZERO) <= 0) {
				logger.debug("OEM客户短信余额提醒: 客户ID = {} {}预警为0，不发送短信", account.getClientid(), alarmType.getDesc());
				return;
			}

			// 判断余额是否可以告警
			BigDecimal gj = new BigDecimal(balance);
			if (gj.compareTo(balanceAlarm.getAlarmAmount()) > 0) {
				logger.debug("OEM客户短信余额提醒: 客户ID = {} {} 未达到预警值，不发送短信", account.getClientid(), alarmType.getDesc());
				return;
			}

			// 金额大于0小于阀值
			if (gj.compareTo(BigDecimal.ZERO) > 0)
			{
				needSend = true;
			} else {
				if (poolCount > 0)
				{
					needSend = true;
				}
			}
		} else {
			if (balanceAlarm.getAlarmNumber() == null || balanceAlarm.getAlarmNumber().intValue() <= 0) {
				logger.debug("OEM客户短信余额提醒: 客户ID = {} {}预警为0，不发送短信", account.getClientid(), alarmType.getDesc());
				return;
			}

			// 判断普通短信余额是否可以告警
			int pt = Integer.parseInt(balance);
			if (pt > balanceAlarm.getAlarmNumber().intValue()) {
				logger.debug("OEM客户短信余额提醒: 客户ID = {} {} 未达到预警值，不发送短信", account.getClientid(), alarmType.getDesc());
				return;
			}

			// 金额大于0小于阀值，或者池数量大于0
			if (pt > 0 || poolCount > 0)
			{
				if (account.getSmsfrom().intValue() == SmsFrom.HTTPS.getValue().intValue())
				{
					// HTTP协议，都可以发
					needSend = true;
				} else {
					if (account.getSmstype() == smsTypeEnum.getValue().intValue())
					{
						needSend = true;
					}
				}
			}
		}

		if (needSend)
		{
			doSendSMS(productType.getDesc(), user, account, taskAlarmSetting, balanceAlarm, balance, smsMailpropMap,
					isGj ? "元" : "条");
		}

		// 重置该类型的告警次数
		int reduce = clientBalanceAlarmMapper.reduceReminderNumOem(account.getClientid(),
				alarmType.getValue().toString());
		logger.debug("OEM客户短信余额提醒: 短信发送后, 修改 clientid = {} 的 {} 告警次数 , 影响数据库行数 = {}", account.getClientid(),
				alarmType.getDesc(), reduce);
	}

	private String buildMobile(Set<String> phones)
	{
		if (Collections3.isEmpty(phones))
		{
			return "";
		}

		StringBuilder phone = new StringBuilder("");
		for (String str : phones) {
			if (phone.length() > 0)
			{
				phone.append(",");
			}

			phone.append(str);
		}

		return phone.toString();
	}

	private void doSendSMS(String smsType, JsmsUser user, JsmsAccount account, JsmsTaskAlarmSetting taskAlarmSetting,
			JsmsClientBalanceAlarm balanceAlarm, String ye, Map<String, Object> smsMailpropMap, String unit) {
		List<String> templateParam = new ArrayList<>();

		Set<String> yyPhones = new HashSet<>();

		// 发送给运营
		if (StringUtils.isBlank(taskAlarmSetting.getTaskAlarmPhone())) {
			logger.debug("OEM客户短信余额提醒: 运营配置手机号为空，不再发送运营的提醒");
		} else {
			// 去重
			String [] list = taskAlarmSetting.getTaskAlarmPhone().split(",");
			for (String s : list) {
				if(yyPhones.contains(s))
				{
					continue;
				}
				yyPhones.add(s);
			}

			if (yyPhones.size() > 0)
			{
				templateParam.clear();
				// 销售名字
				if (user == null) {
					templateParam.add(0, "");
				} else {
					templateParam.add(0, StringUtils.isBlank(user.getRealname()) ? "" : user.getRealname());
				}

				templateParam.add(1, account.getClientid()); // 客户ID
				templateParam.add(2, StringUtils.isBlank(account.getName()) ? "" : account.getName()); // 客户名称
				templateParam.add(3, smsType); // 短信类型
				templateParam.add(4, ye); // 余额
				templateParam.add(5, unit); // 单位

				SendSMSUtil.sendAlarmSMS(taskAlarmSetting.getTaskAlarmContent(), buildMobile(yyPhones),
						templateParam);
			}
		}

		Set<String> xsPhones = new HashSet<>();

		// 发送给销售
		if (user == null || StringUtils.isBlank(user.getMobile())) {
			logger.debug("OEM客户短信余额提醒: 归属销售或归属销售的电话为空 {}", JSON.toJSONString(user));
		} else {
			// 去重
			String [] list = user.getMobile().split(",");
			for (String s : list) {
				if(yyPhones.contains(s) || xsPhones.contains(s))
				{
					continue;
				}
				xsPhones.add(s);
			}

			if (xsPhones.size() > 0)
			{
				templateParam.clear();
				templateParam.add(0, account.getClientid()); // 客户ID
				templateParam.add(1, StringUtils.isBlank(account.getName()) ? "" : account.getName()); // 客户名称
				templateParam.add(2, smsType); // 短信类型
				templateParam.add(3, ye); // 余额
				templateParam.add(4, unit); // 单位
				SendSMSUtil.sendAlarmSMS(taskAlarmSetting.getSaleAlarmContent(), buildMobile(xsPhones), templateParam);
			}

		}

		Set<String> khPhones = new HashSet<>();

		// 发送给客户
		if (StringUtils.isBlank(balanceAlarm.getAlarmPhone())) {
			logger.debug("OEM客户短信余额提醒: 客户的提醒手机号为空，不再发送客户的提醒");
		} else {
			String [] list = balanceAlarm.getAlarmPhone().split(",");
			for (String s : list) {
				if(yyPhones.contains(s) || xsPhones.contains(s) || khPhones.contains(s))
				{
					continue;
				}
				khPhones.add(s);
			}

			if (khPhones.size() > 0)
			{
				// 可用余额
				templateParam.clear();
				templateParam.add(0, account.getClientid()); // 客户ID
				templateParam.add(1, StringUtils.isBlank(account.getName()) ? "" : account.getName()); // 客户名称
				templateParam.add(2, smsType); // 短信类型
				templateParam.add(3, ye); // 余额
				templateParam.add(4, unit); // 单位

				// 发送短信
				SendSMSUtil.sendAlarmSMS(taskAlarmSetting.getUserAlarmContent(), buildMobile(khPhones),
						templateParam);
			}
		}

		// 发送邮件
		if (StringUtils.isBlank(balanceAlarm.getAlarmEmail()) || smsMailpropMap == null || smsMailpropMap.size() <= 0) {
			logger.debug("OEM客户短信余额提醒: 客户的提醒邮箱为空，不再发送客户的邮件提醒");
		} else {
			Set<String> khEmails = new HashSet<>();
			String [] list = balanceAlarm.getAlarmEmail().split(",");
			for (String s : list) {
				if(khEmails.contains(s))
				{
					continue;
				}
				khEmails.add(s);
			}

			if (khEmails.size() > 0)
			{
				String frm = (String) smsMailpropMap.get("frm");
				String subject = (String) smsMailpropMap.get("subject");
				String body = (String) smsMailpropMap.get("text");

				body = body.replace("clientId", account.getClientid());
				body = body.replace("clientName", StringUtils.isBlank(account.getName()) ? "" : account.getName());
				body = body.replace("smsType", smsType);
				body = body.replace("smsCount", ye);

				this.emailService.sendHtmlEmail(frm, buildMobile(khEmails), subject, body);
			}
		}
	}
}
