package com.ucpaas.sms.task.service;

import com.jsmsframework.finance.entity.JsmsAgentAccount;
import com.jsmsframework.finance.entity.JsmsAgentBalanceAlarm;
import com.jsmsframework.finance.entity.JsmsTaskAlarmSetting;
import com.jsmsframework.finance.enums.TaskAlarmType;
import com.jsmsframework.finance.service.JsmsAgentAccountService;
import com.jsmsframework.finance.service.JsmsAgentBalanceAlarmService;
import com.jsmsframework.finance.service.JsmsAgentBalanceBillService;
import com.jsmsframework.finance.service.JsmsTaskAlarmSettingService;
import com.jsmsframework.user.entity.JsmsAgentInfo;
import com.jsmsframework.user.entity.JsmsUser;
import com.jsmsframework.user.service.JsmsAgentInfoService;
import com.jsmsframework.user.service.JsmsUserService;
import com.ucpaas.sms.task.constant.AlarmConstant;
import com.ucpaas.sms.task.entity.message.AgentBalanceBill;
import com.ucpaas.sms.task.mapper.message.AgentBalanceBillMapper;
import com.ucpaas.sms.task.model.TaskInfo;
import com.ucpaas.sms.task.util.AlarmCommonUtil;
import com.ucpaas.sms.task.util.JacksonUtil;
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
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 黄文杰
 * @description 代理商帐户余额收支明细管理
 * @date 2017-07-27
 */
@Service
public class AgentBalanceBillServiceImpl implements AgentBalanceBillService {
	private static final Logger logger = LoggerFactory.getLogger(AgentBalanceBillServiceImpl.class);

	@Autowired
	private AgentBalanceBillMapper agentBalanceBillMapper;
	@Autowired
	private JsmsTaskAlarmSettingService jsmsTaskAlarmSettingService;
	@Autowired
	private JsmsAgentBalanceAlarmService jsmsAgentBalanceAlarmService;
	@Autowired
	private JsmsAgentAccountService jsmsAgentAccountService;
	@Autowired
	private JsmsAgentBalanceBillService jsmsAgentBalanceBillService;
	@Autowired
	private JsmsUserService jsmsUserService;
	@Autowired
	private JsmsAgentInfoService jsmsAgentInfoService;
	@Autowired
	private BalanceAlarmService balanceAlarmService;

	@Override
	@Transactional
	public int insert(AgentBalanceBill model) {
		return this.agentBalanceBillMapper.insert(model);
	}

	@Override
	@Transactional
	public int insertBatch(List<AgentBalanceBill> modelList) {
		return this.agentBalanceBillMapper.insertBatch(modelList);
	}

	@Override
	@Transactional
	public int update(AgentBalanceBill model) {
		AgentBalanceBill old = this.agentBalanceBillMapper.getById(model.getId());
		if (old == null) {
			return 0;
		}
		return this.agentBalanceBillMapper.update(model);
	}

	@Override
	@Transactional
	public int updateSelective(AgentBalanceBill model) {
		AgentBalanceBill old = this.agentBalanceBillMapper.getById(model.getId());
		if (old != null)
			return this.agentBalanceBillMapper.updateSelective(model);
		return 0;
	}

	@Override
	@Transactional
	public AgentBalanceBill getById(Integer id) {
		AgentBalanceBill model = this.agentBalanceBillMapper.getById(id);
		return model;
	}

	@Override
	public List<AgentBalanceBill> queryAll(Map params) {
		return agentBalanceBillMapper.queryAll(params);
	}

	@Override
	@Transactional
	public int count(Map<String, Object> params) {
		return this.agentBalanceBillMapper.count(params);
	}

	/**
	 * 【预警】代理商余额预警
	 */
	@Override
	@Transactional
	public boolean agentBalanceAlarm(TaskInfo taskInfo) {
		List<JsmsTaskAlarmSetting> taskAlarmSettingList = jsmsTaskAlarmSettingService
				.getByTaskAlarmType(TaskAlarmType.AMOUNT_ALARM);
		for (JsmsTaskAlarmSetting taskAlarmSetting : taskAlarmSettingList) {
			doAlarm(taskAlarmSetting);
		}
		return true;
	}

	private boolean doAlarm(final JsmsTaskAlarmSetting taskAlarmSetting) {
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
		updateAlarmNum(now, taskAlarmSetting);

		Map params = new HashMap();
		List<JsmsAgentBalanceAlarm> agentBalanceAlarmList = jsmsAgentBalanceAlarmService.queryNeedAlarmList(params);
		logger.debug("设置告警的 【代理商】 --> {}", JacksonUtil.toJSON(agentBalanceAlarmList));

		if (agentBalanceAlarmList.isEmpty()) {
			logger.debug("设置告警且告警次数 大于 0 的 【代理商】 数量 = {} , 不告警", 0);
			return true;
		}

		Set agentIds = new HashSet();
		for (JsmsAgentBalanceAlarm agentBalanceAlarm : agentBalanceAlarmList) {
			if (agentBalanceAlarm == null && agentBalanceAlarm.getAgentId() == null) {
				continue;
			}
			agentIds.add(agentBalanceAlarm.getAgentId());
		}
		if (agentIds.isEmpty()) {
			logger.debug("没有查询到需要发送邮件的代理商 --> agentIds = {}", agentIds);
			return true;
		}
		List<JsmsAgentInfo> jsmsAgentInfoList = jsmsAgentInfoService.getByAgentIds(agentIds);
		final Map<Integer, JsmsAgentInfo> agentMap = new HashMap<>();
		Set<Long> saleIds = new HashSet<>();
		for (JsmsAgentInfo agentInfo : jsmsAgentInfoList) {
			agentMap.put(agentInfo.getAgentId(), agentInfo);
			if (agentInfo.getBelongSale() != null) {
				saleIds.add(agentInfo.getBelongSale());
			}
		}

		final List<JsmsUser> userList;
		if (!saleIds.isEmpty()) {
			userList = jsmsUserService.getByIds(saleIds);
		} else {
			userList = new ArrayList<>();
		}

		/**
		 * 获取代理商的(金额)账户信息
		 */
		List<JsmsAgentAccount> agentAccountList = jsmsAgentAccountService.getByAgentIds(agentIds);

		if (agentAccountList.isEmpty()) {
			logger.debug("没有查询到需要发送邮件的代理商 --> agentAccountList = {}", JacksonUtil.toJSON(agentAccountList));
			return true;
		}
		/**
		 * 增加线程池
		 */
		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1, 100, 60, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());

		for (final JsmsAgentBalanceAlarm agentBalanceAlarm : agentBalanceAlarmList) {
			if (agentBalanceAlarm == null && agentBalanceAlarm.getAgentId() == null
					|| !checkWebIdWithAgent(agentMap.get(agentBalanceAlarm.getAgentId()), taskAlarmSetting)) {
				continue;
			}
			logger.debug("agentId = {} ,告警次数 reminder_number --> {}, 告警阀值 alarm_amount --> {}",
					agentBalanceAlarm.getAgentId(), agentBalanceAlarm.getReminderNumber(),
					agentBalanceAlarm.getAlarmAmount());

			for (JsmsAgentAccount agentAccount : agentAccountList) {
				if (agentAccount == null && agentAccount.getAgentId() == null) {
					continue;
				}

				if (agentBalanceAlarm.getAgentId().equals(agentAccount.getAgentId())) {
					BigDecimal enableMoney = agentAccount.getBalance().add(agentAccount.getCreditBalance());
					logger.debug("agentId = {} ,账户余额(元) balance = {}, 信用额度(元) credit_balance --> {} , 可用额度 --> {}",
							agentAccount.getAgentId(), agentAccount.getBalance(), agentAccount.getCreditBalance(),
							enableMoney);
					if (agentBalanceAlarm.getReminderNumber() > 0 && agentBalanceAlarm.getAlarmAmount()
							.compareTo(enableMoney.setScale(2, BigDecimal.ROUND_DOWN)) >= 0) {
						final List<String> templateParam1 = new ArrayList<>();
						templateParam1.add(0, enableMoney.setScale(2, BigDecimal.ROUND_DOWN).toString()); // 余额
						threadPool.execute(new Runnable() {
							@Override
							public void run() {
								SendSMSUtil.sendAlarmSMS(taskAlarmSetting.getTaskAlarmContent(),
										agentBalanceAlarm.getAlarmPhone(), templateParam1);
							}
						});

						JsmsAgentBalanceAlarm tempParam = new JsmsAgentBalanceAlarm();
						tempParam.setAgentId(agentBalanceAlarm.getAgentId());
						tempParam.setReminderNumber(0);
						tempParam.setResetTime(now.toDate());
						tempParam.setUpdateTime(now.toDate());
						int reduce = jsmsAgentBalanceAlarmService.updateByAgentId(tempParam);
						logger.debug("短信发送后, 修改 agentId = {} 的 告警次数 , 影响数据库行数 = {}", agentBalanceAlarm.getAgentId(),
								reduce);

						final List<String> templateParam2 = new ArrayList<>();
						templateParam2.add(0, agentBalanceAlarm.getAgentId().toString()); // 代理商ID
						String agentName = agentMap.get(agentBalanceAlarm.getAgentId()).getAgentName();

						templateParam2.add(1, StringUtils.isEmpty(agentName) ? "" : agentName); // 代理商名称
						threadPool.execute(new Runnable() {
							@Override
							public void run() {
								sendAlarmSmsToSaler(userList,
										agentMap.get(agentBalanceAlarm.getAgentId()).getBelongSale(), templateParam2);
							}
						});
					}
					break;
				}
			}
		}
		threadPool.shutdown();
		try {
			threadPool.awaitTermination(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.debug("threadPool.awaitTermination(5, TimeUnit.SECONDS) 终止异常 ==> {}", e);
		}
		return true;
	}

	private boolean updateAlarmNum(DateTime now, JsmsTaskAlarmSetting taskAlarmSetting) {
		logger.debug("准备重置【有新购买的订单】的客户的告警次数");
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
			logger.debug("更新新充值的代理商【{}】的告警次数 , 更新成功 true or false ?--> {} ", agentInfo.getAgentId(),
					update > 0 ? true : false); // todo
		}

		return true;
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
		if (userList.isEmpty()) {
			logger.debug("没有归属销售信息不发送短信给销售");
			return true;
		}
		for (JsmsUser user : userList) {
			if (user != null && user.getId().equals(saleId)) {
				SendSMSUtil.sendAlarmSMS(AlarmConstant.agent_saler_sms_alarm_template, user.getMobile(), templateParam);
				return true;
			}
		}

		return false;
	}

	/**
	 * 判断 告警设置的webid是否和代理商类型相对应
	 * 
	 * @param agentInfo
	 * @param taskAlarmSetting
	 * @return
	 */
	private boolean checkWebIdWithAgent(JsmsAgentInfo agentInfo, JsmsTaskAlarmSetting taskAlarmSetting) {
		// 1短信调度系统 2代理商平台 3运营平台 4OEM代理商平台 5品牌客户端 6OEM客户端
		// 1:销售代理商,2:品牌代理商,3:资源合作商,4:代理商和资源合作,5:OEM代理商

		logger.debug("代理商类型= {}, 应用系统 = {} ", agentInfo.getAgentType(), taskAlarmSetting.getWebId());
		if (agentInfo.getAgentType().equals(2) && taskAlarmSetting.getWebId().equals(2)) { // 2:品牌代理商
																							// ==>
																							// 2代理商平台
			return true;
		} else if (agentInfo.getAgentType().equals(1) && taskAlarmSetting.getWebId().equals(2)) { // 1:销售代理商
																									// ==>
																									// 2代理商平台
			// todo 销售代理商是否归到 2代理商平台
			return true;
		} else if (agentInfo.getAgentType().equals(5) && taskAlarmSetting.getWebId().equals(4)) { // 5:OEM代理商
																									// ==>
																									// 4OEM代理商平台
			return true;
		}
		return false;
	}

	/**
	 * 比较 DateTime中的 时间部分大小, 只比较 时和分
	 * 
	 * @return DateTime > timeString
	 */
	/*
	 * private boolean compareTime(DateTime dateTime, String timeString){
	 * if(dateTime.get(DateTimeFieldType.hourOfDay()) >
	 * Integer.parseInt(timeString.substring(0,2))){ return true; }
	 * if(dateTime.get(DateTimeFieldType.hourOfDay()) ==
	 * Integer.parseInt(timeString.substring(0,2)) &&
	 * dateTime.get(DateTimeFieldType.minuteOfHour()) >=
	 * Integer.parseInt(timeString.substring(3,5))){ return true; } return
	 * false; }
	 */

	/**
	 * 判断当前分钟寺是否是扫描周期所在的分钟
	 * 
	 * @return
	 */
	/*
	 * private boolean isNowOnScan(String timeString,Integer scanFrequecy){
	 * DateTime beginTime =
	 * DateTime.now().withHourOfDay(Integer.parseInt(timeString.substring(0,
	 * 2))) .withMinuteOfHour(Integer.parseInt(timeString.substring(3,5)))
	 * .withSecondOfMinute(0); if((DateTime.now().getMillis() -
	 * beginTime.getMillis()) % scanFrequecy < 60000){ return true; } return
	 * false; }
	 */

	public static void main(String[] args) {
		// AgentBalanceBillServiceImpl thiss = new
		// AgentBalanceBillServiceImpl();
		// boolean nowOnScan = thiss.isNowOnScan("08:00", 120000);
		// System.out.println("--------> "+nowOnScan);
		//
		// DateTime now = DateTime.now();
		//
		// System.out.println(now.getMillis());
		// System.out.println(new Date().getTime());
		System.out.println(JsonUtils.toJson(null));
	}

}
