package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.entity.access.AccessChannelStatistics;
import com.ucpaas.sms.task.entity.message.AgentBalanceBill;
import com.ucpaas.sms.task.enum4sms.SMSType;
import com.ucpaas.sms.task.mapper.access.AccessChannelStatisticsMapper;
import com.ucpaas.sms.task.mapper.message.AgentInfoMapper;
import com.ucpaas.sms.task.model.TaskInfo;
import com.ucpaas.sms.task.util.DateUtilsNew;
import com.ucpaas.sms.task.util.FmtUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * OEM代理商计费
 * 
 */
@Service
public class OemAgentClientChargeServiceImpl implements OemAgentClientChargeService {

	@Autowired
	private AccessChannelStatisticsMapper accessChannelStatisticsMapper;

	@Autowired
	private AgentInfoMapper agentInfoMapper;

	private static final Logger logger = LoggerFactory.getLogger("OemAgentClientChargeService");

	/**
	 * 后付费计费
	 * 
	 * @return 是否成功
	 */
	@Transactional("message")
	public boolean houfufeiCharge(TaskInfo taskInfo) {
		Calendar begin = Calendar.getInstance();
		logger.debug("【每日OEM代理商后付费扣费任务】开始 = {}", DateUtilsNew.formatDateTime(begin.getTime()));

		// 检查access统计是否完成
		int accessChannelTask = agentInfoMapper
				.checkAccessChannelStatDone(DateUtilsNew.formatDate(begin.getTime(), "yyyyMMdd"));
		if (accessChannelTask == 0) {
			logger.debug("【每日OEM代理商后付费扣费任务】每日创建ACCESS流水表(昨日数据)任务正在运行或还未运行，等待其完成...");
			return false;
		}

		// 获取昨日
		Calendar zuori = Calendar.getInstance();
		zuori.set(Calendar.DATE, begin.get(Calendar.DATE) - 1);
		String date = DateUtilsNew.formatDate(zuori.getTime(), "yyyyMMdd");

		// 查询昨日的统计数据
		List<AccessChannelStatistics> statList = accessChannelStatisticsMapper
				.findZuoriHoufufeiClientList(Integer.parseInt(date));
		if (statList == null || statList.size() <= 0) {
			logger.debug("【每日OEM代理商后付费扣费任务】结束(未查询到access昨日{}统计记录) = {}", date,
					DateUtilsNew.formatDateTime(Calendar.getInstance().getTime()));
			return true;
		}

		// 查询OEM代理商
		Set<String> ids = new HashSet<>();
		for (AccessChannelStatistics item : statList) {
			if (ids.contains(item.getAgentId().toString())) {
				continue;
			}
			ids.add(item.getAgentId().toString());
		}

		List<String> agentIds = agentInfoMapper.findOemAgentInfoByAgentIds(ids);
		if (agentIds == null || agentIds.size() < 0) {
			Calendar end = Calendar.getInstance();
			logger.debug("【每日OEM代理商后付费扣费任务】结束(查询到access昨日统计记录不存在OEM代理商) = {}",
					DateUtilsNew.formatDateTime(end.getTime()));
			return true;
		}

		// 移除非OEM代理商的统计项
		for (Iterator<AccessChannelStatistics> iterator = statList.iterator(); iterator.hasNext();) {
			AccessChannelStatistics item = iterator.next();
			if (!agentIds.contains(item.getAgentId().toString())) {
				iterator.remove();
			}
		}

		// 客户名称及代理商Id
		Map<String, String[]> clientInfo = new HashMap<>();

		// 查询客户的单价，参数是： 统计日期、客户端ID，短信类型
		Map<String, Object> queryPrice = new HashMap<>();

		// 对数据进行分组 key1为clientid，key2为smstype
		Map<String, Map<Integer, Object[]>> data = new HashMap<>();
		for (AccessChannelStatistics item : statList) {
			Map<Integer, Object[]> clientSmsTypes = data.get(item.getClientid());
			if (clientSmsTypes == null) {
				clientSmsTypes = new HashMap<>();

				// 查询出客户名称，放入clientInfo
				String name = agentInfoMapper.getClientNameByClientId(item.getClientid());
				String agentId = item.getAgentId().toString();
				String[] params = new String[] { name, agentId };
				clientInfo.put(item.getClientid(), params);

				data.put(item.getClientid(), clientSmsTypes);
			}

			// 客户的每一个短信类型，对应值为数组，数组里保存 金额、条数、单价
			Object[] objects = clientSmsTypes.get(item.getSmstype());
			if (objects == null) {
				BigDecimal money = item.getSalefee();
				if (money == null){
					money = BigDecimal.ZERO;
				}
				Integer chargeTotal = item.getChargetotal();

				// 查询出单价，统计报表计算后付费用户费用时，取统计数据对应的发送时间>=生效日期，且生效日期与发送时间最接近的一条记录中的短信单价
				queryPrice.clear(); // 先清空条件
				queryPrice.put("date", date);
				queryPrice.put("clientId", item.getClientid());
				queryPrice.put("smsType", item.getSmstype());
				String unitPrice = agentInfoMapper.getUserPriceByClientId(queryPrice);
				if (StringUtils.isBlank(unitPrice)) {
					unitPrice = "0";
				}

				objects = new Object[] { money, chargeTotal, unitPrice };
				clientSmsTypes.put(item.getSmstype(), objects);
			} else {
				// 统计表自带的金额累加
				BigDecimal money = (BigDecimal) objects[0];
				money = money.add(item.getSalefee());

				// 统计表计费条数累加
				Integer chargeTotal = (Integer) objects[1];
				chargeTotal += item.getChargetotal();

				// 替换原来的金额以及条数
				objects[0] = money;
				objects[1] = chargeTotal;
			}
		}

		// 扣除代理商余额，增加短信账单
		Map<String, Object> params = new HashMap<>(); // 扣费的参数
		AgentBalanceBill agentBalanceBill; // 代理商账单
		StringBuilder remark = new StringBuilder(); // 账单备注

		// 循环处理所有客户
		for (String clientId : data.keySet()) {
			logger.debug("【每日OEM代理商后付费扣费任务】客户{}开始扣费", clientId);

			// 取出客户的名称及代理商ID
			String[] info = clientInfo.get(clientId);
			String clientName = info[0];
			String agentId = info[1];

			// 取出客户的所有短信类型对应的信息
			Map<Integer, Object[]> smsTypes = data.get(clientId);
			for (Integer smsType : smsTypes.keySet()) {
				// 短信类型对应的 金额、条数、单价
				Object[] objects = smsTypes.get(smsType);

				BigDecimal statMoney = (BigDecimal) objects[0];
				Integer chargeTotal = (Integer) objects[1];
				String unitPrice = objects[2].toString();
//				BigDecimal selfMoney; // 自己算算的金额

				// 查询出代理商余额
				String balance = agentInfoMapper.getAgentBalanceByAgentId(agentId);

				// 计算统计表的价格，统计表是厘所以除以1000，并保留5位小数
				statMoney = statMoney.divide(new BigDecimal(1000), 5, BigDecimal.ROUND_HALF_UP);

				// 自己计算的价格，条数*单价（单位是元）
//				BigDecimal tempMoney = new BigDecimal(chargeTotal).multiply(new BigDecimal(unitPrice));
//				String tempMoneyStr = String
//						.valueOf(tempMoney.divide(new BigDecimal(1), 5, BigDecimal.ROUND_HALF_UP).doubleValue());
//				selfMoney = new BigDecimal(tempMoneyStr);

				// 目前确定取自己计算的金额
				if (statMoney.compareTo(BigDecimal.ZERO) <= 0) {
					logger.debug("【每日OEM代理商后付费扣费任务】客户{}开始扣费-短信类型{}, 扣费金额为0跳过扣费", clientId, unitPrice);
					continue;
				}
				BigDecimal money = statMoney;

				logger.debug("【每日OEM代理商后付费扣费任务】客户{}开始计费: 代理商{} 代理商当前余额{}  短信类型{} 短信单价{} 扣费条数{} 扣费金额{} 扣费日期{}", clientId,
						agentId, balance, smsType, unitPrice, chargeTotal, money, date);

				// 对代理商进行扣费
				params.clear();
				params.put("money", money);
				params.put("agentId", Integer.valueOf(agentId));
				agentInfoMapper.subAgentAccountBalance(params);

				// 增加短信账单
				agentBalanceBill = new AgentBalanceBill();
				agentBalanceBill.setAdminId(0l);
				agentBalanceBill.setAgentId(Integer.valueOf(agentId));
				agentBalanceBill.setAmount(money);

				// 算出减去本次后的余额
				BigDecimal currBalance = new BigDecimal(balance).subtract(money);
//				String currBalanceStr = String
//						.valueOf(currBalance.divide(new BigDecimal(1), 5, BigDecimal.ROUND_HALF_UP).doubleValue());
				agentBalanceBill.setBalance(currBalance.divide(new BigDecimal(1), 5, BigDecimal.ROUND_HALF_UP));
				agentBalanceBill.setCreateTime(Calendar.getInstance().getTime());
				agentBalanceBill.setFinancialType("1");
				agentBalanceBill.setOrderId(null);
				agentBalanceBill.setClientId(clientId);
				agentBalanceBill.setPaymentType(6);
				remark.setLength(0);
				remark.append(clientId);
				remark.append(":");
				remark.append(clientName);
				remark.append(";");
				remark.append(SMSType.getDescByValue(smsType));
				// 单价
				remark.append(FmtUtils.hold4Decimal(new BigDecimal(unitPrice)));
				remark.append("元/条");
				agentBalanceBill.setRemark(remark.toString());
				agentInfoMapper.addAgentBalanceBill(agentBalanceBill);
			}
			logger.debug("【每日OEM代理商后付费扣费任务】客户{}结束扣费", clientId);
		}

		Calendar end = Calendar.getInstance();
		logger.debug("【每日OEM代理商后付费扣费任务】结束 = {}", DateUtilsNew.formatDateTime(end.getTime()));
		return true;
	}

}
