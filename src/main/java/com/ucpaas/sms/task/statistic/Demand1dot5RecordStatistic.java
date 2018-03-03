package com.ucpaas.sms.task.statistic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.jsmsframework.user.entity.JsmsDepartment;
import com.jsmsframework.user.service.JsmsDepartmentService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ucpaas.sms.task.entity.message.Channel;
import com.ucpaas.sms.task.entity.message.User;
import com.ucpaas.sms.task.entity.record.RecordChannelStatistics;
import com.ucpaas.sms.task.entity.record.RecordChannelTempStatistics;
import com.ucpaas.sms.task.entity.record.RecordConsumeStatistics;
import com.ucpaas.sms.task.enum4sms.PayType;
import com.ucpaas.sms.task.enum4sms.RecordChannelStatisticsType;
import com.ucpaas.sms.task.service.ChannelService;
import com.ucpaas.sms.task.service.RecordChannelStatisticsService;
import com.ucpaas.sms.task.service.RecordChannelTempDataStatisticsService;
import com.ucpaas.sms.task.service.RecordConsumeStatisticsService;
import com.ucpaas.sms.task.service.UserService;
import com.ucpaas.sms.task.util.JsonUtils;

/**
 * 需求版本1.5的通道侧统计策略<br>
 * 对应开发版本5.8.0
 */
@Service
public class Demand1dot5RecordStatistic implements RecordStatisticStrategy {
	
	private static final Logger LOGGER = LoggerFactory.getLogger("RecordChannelStatisticsService");
	
	@Autowired
	private RecordChannelStatisticsService recordChannelStatisticsService;
	@Autowired
	private RecordConsumeStatisticsService recordConsumeStatisticsService;
	@Autowired
	private ChannelService channelService;
	
	@Autowired
	private UserService userService;

	@Autowired
	private RecordChannelTempDataStatisticsService recordChannelTempDataStatisticsService;
	@Autowired
	private JsmsDepartmentService jsmsDepartmentService;
	

	@Override
	public void statistics(DateTime statDay) {
		
		// 1、执行统计将数据保存到 t_sms_record_channel_statistics 
		List<RecordChannelStatistics> recordChannelStatisticsDaliyList = this.recordChannelStatistic(statDay);
		
		// 2、执行统计将数据保存到 t_sms_record_consume_stat // TODO
		this.recordConsumeStatistic(statDay, recordChannelStatisticsDaliyList);
	}
	

	/**
	 * 统计Record流水表并将统计结果保存到 t_sms_record_channel_statistics 表
	 * @param statDay
	 */
	private List<RecordChannelStatistics> recordChannelStatistic(DateTime statDay){
		String statDayStr = statDay.toString("yyyyMMdd");
		Date now = new Date();
		
		List<RecordChannelStatistics> dailyDataList = new ArrayList<>(); // 每日详细数据  stattype = 0
		RecordChannelStatistics dailySumData = new RecordChannelStatistics(); // 每日合计数据 stattype = 1
		
		
		List<RecordChannelTempStatistics> tempDataList = recordChannelTempDataStatisticsService.generateData(statDayStr);

		LOGGER.debug("统计 {} 当天所有的record表，生成临时统计数据 {} ", statDayStr, JsonUtils.toJson(tempDataList));

		if (tempDataList == null || tempDataList.isEmpty()) {
			LOGGER.debug("统计 {} 当天所有的record表，生成临时统计数据为空", statDayStr);
			return dailyDataList;
		}

		// 初始化每日合计数据
		dailySumData.setChannelid(-1);
		dailySumData.setRemark(" - ");
		dailySumData.setOperatorstype(-2);
		dailySumData.setStattype(RecordChannelStatisticsType.dailySum.getValue());
		dailySumData.setDate(Integer.valueOf(statDayStr));
		dailySumData.setCreatetime(now);
		dailySumData.setChargetotal(0);
		dailySumData.setCosttotal(BigDecimal.ZERO);
		dailySumData.setSendtotal(0);
		dailySumData.setNotsend(0);
		dailySumData.setSubmitsuccess(0);
		dailySumData.setSubretsuccess(0);
		dailySumData.setReportsuccess(0);
		dailySumData.setSubmitfail(0);
		dailySumData.setSubretfail(0);
		dailySumData.setReportfail(0);
		dailySumData.setClientid(" - ");
		dailySumData.setBelongSale(null);
		dailySumData.setPaytype(PayType.daily.getValue());
		dailySumData.setBelongBusiness(null);
		dailySumData.setSmstype(null);

		// 遍历临时统计数据生成每日明细数据和每日合计数据
		for (RecordChannelTempStatistics temp : tempDataList) {
			// 日明细数据
			RecordChannelStatistics rcs = new RecordChannelStatistics();
			rcs.setChannelid(temp.getChannelid());
			rcs.setRemark(temp.getRemark());
			rcs.setOperatorstype(Integer.valueOf(temp.getOperatorstype()));
			rcs.setChargetotal(temp.getChargetotal().intValue());
			rcs.setCosttotal(temp.getCosttotal());
			rcs.setSendtotal(temp.getSendtotal().intValue());
			rcs.setNotsend(temp.getNotsend().intValue());
			rcs.setSubmitsuccess(temp.getSubmitsuccess().intValue());
			rcs.setSubretsuccess(temp.getSubretsuccess().intValue());
			rcs.setReportsuccess(temp.getReportsuccess().intValue());
			rcs.setSubmitfail(temp.getSubmitfail().intValue());
			rcs.setSubretfail(temp.getSubretfail().intValue());
			rcs.setReportfail(temp.getReportfail().intValue());
			rcs.setStattype(RecordChannelStatisticsType.daily.getValue());
			rcs.setDate(Integer.valueOf(temp.getDate()));
			rcs.setCreatetime(now);
			rcs.setClientid(temp.getClientid());
			rcs.setBelongSale(temp.getBelongSale());
			rcs.setPaytype(temp.getPaytype().intValue());
			rcs.setBelongBusiness(temp.getBelongBusiness());
			rcs.setSmstype(temp.getSmstype());
			dailyDataList.add(rcs);

			// 日合计数据（累加每日明细生成）
			dailySumData.setChargetotal(dailySumData.getChargetotal() + temp.getChargetotal().intValue());
			dailySumData.setCosttotal(dailySumData.getCosttotal().add(temp.getCosttotal()));
			dailySumData.setSendtotal(dailySumData.getSendtotal() + temp.getSendtotal().intValue());
			dailySumData.setNotsend(dailySumData.getNotsend() + temp.getNotsend().intValue());
			dailySumData.setSubmitsuccess(dailySumData.getSubmitsuccess() + temp.getSubmitsuccess().intValue());
			dailySumData.setSubretsuccess(dailySumData.getSubretsuccess() + temp.getSubretsuccess().intValue());
			dailySumData.setReportsuccess(dailySumData.getReportsuccess() + temp.getReportsuccess().intValue());
			dailySumData.setSubmitfail(dailySumData.getSubmitfail() + temp.getSubmitfail().intValue());
			dailySumData.setSubretfail(dailySumData.getSubretfail() + temp.getSubretfail().intValue());
			dailySumData.setReportfail(dailySumData.getReportfail() + temp.getReportfail().intValue());
		}

		LOGGER.debug("根据临时数据,计算出日明细数据{} ", JsonUtils.toJson(dailyDataList));
		LOGGER.debug("根据临时数据,计算出日合计数据{} ", JsonUtils.toJson(dailySumData));
		
		
		// 清除t_sms_record_channel_statistic 表统计日的老数据
		LOGGER.debug("写入每日统计数据前，清除统计表中老的每日明细数据，日期 = {} ", statDayStr);
		recordChannelStatisticsService.deleteByDate(statDayStr);
		
		// 保存日明细数据
		recordChannelStatisticsService.insertBatch(dailyDataList);
		// 保存日合计数据
		recordChannelStatisticsService.insert(dailySumData);

		
		
		statDayStr = statDay.toString("yyyyMM");
		LOGGER.debug("每日统计数据写入完毕，开始统计 {} 月的每月数据", statDayStr);
		
		// 获得当月所有已经存在的每日明细数据用于计算每月数据
		Map<String, Object> sqlParams = new HashMap<>();
		String dataTimePrix = statDayStr.substring(0, 6);
		sqlParams.put("stattype", RecordChannelStatisticsType.daily.getValue());
		sqlParams.put("dataTimePrix", dataTimePrix);

		List<RecordChannelStatistics> allDailyDataList = recordChannelStatisticsService.queryMonthly(sqlParams); // 当月所有每日明细数据
		LOGGER.debug("找出本月所有t_sms_record_channel_statistic表中 {} 月的每日明细数据 {} ", dataTimePrix, JsonUtils.toJson(allDailyDataList));

		List<RecordChannelStatistics> monthlyDataList = new ArrayList<>(); // 每月明细数据
		RecordChannelStatistics monthlySumData = new RecordChannelStatistics(); // 每月合计数据
		
		// 初始化每月合计数据
		monthlySumData.setChannelid(-1);
		monthlySumData.setRemark("-");
		monthlySumData.setOperatorstype(-2);
		monthlySumData.setChargetotal(0);
		monthlySumData.setCosttotal(BigDecimal.ZERO);
		monthlySumData.setSendtotal(0);
		monthlySumData.setNotsend(0);
		monthlySumData.setSubmitsuccess(0);
		monthlySumData.setSubretsuccess(0);
		monthlySumData.setReportsuccess(0);
		monthlySumData.setSubmitfail(0);
		monthlySumData.setSubretfail(0);
		monthlySumData.setReportfail(0);
		monthlySumData.setStattype(RecordChannelStatisticsType.monthlySum.getValue());
		monthlySumData.setDate(Integer.valueOf(statDayStr.substring(0, 6)));
		monthlySumData.setCreatetime(now);
		monthlySumData.setClientid(" - ");
		monthlySumData.setBelongSale(null);
		monthlySumData.setPaytype(PayType.montyly.getValue());
		monthlySumData.setBelongBusiness(null);
		monthlySumData.setSmstype(null);

		// 月明细数据
		Map<String, RecordChannelStatistics> temp = new HashMap<>();
		for (RecordChannelStatistics rcs : allDailyDataList) {

			// 分组条件： channelId + clientId + payType + belongSale + belongBusiness + smsType
			String key = rcs.getChannelid() + "-" + rcs.getClientid() + "-" + rcs.getPaytype() + "-" +  rcs.getBelongSale()
						 + "-" +  rcs.getBelongBusiness() + "-" +  rcs.getSmstype();

			RecordChannelStatistics tempMonthly = temp.get(key);
			if (tempMonthly == null) {
				tempMonthly = new RecordChannelStatistics();
				tempMonthly.setChannelid(rcs.getChannelid());
				tempMonthly.setRemark(rcs.getRemark());
				tempMonthly.setOperatorstype(rcs.getOperatorstype());
				tempMonthly.setChargetotal(0);
				tempMonthly.setCosttotal(BigDecimal.ZERO);
				tempMonthly.setSendtotal(0);
				tempMonthly.setNotsend(0);
				tempMonthly.setSubmitsuccess(0);
				tempMonthly.setSubretsuccess(0);
				tempMonthly.setReportsuccess(0);
				tempMonthly.setSubmitfail(0);
				tempMonthly.setSubretfail(0);
				tempMonthly.setReportfail(0);
				tempMonthly.setStattype(RecordChannelStatisticsType.monthly.getValue());
				tempMonthly.setDate(Integer.valueOf(statDayStr.substring(0, 6)));
				tempMonthly.setCreatetime(now);
				tempMonthly.setClientid(rcs.getClientid());
				tempMonthly.setBelongSale(rcs.getBelongSale());
				tempMonthly.setPaytype(rcs.getPaytype().intValue());
				tempMonthly.setBelongBusiness(rcs.getBelongBusiness());
				tempMonthly.setSmstype(rcs.getSmstype());
			}
			
			// 明细
			tempMonthly.setChargetotal(tempMonthly.getChargetotal() + rcs.getChargetotal());
			tempMonthly.setCosttotal(tempMonthly.getCosttotal().add(rcs.getCosttotal()));
			tempMonthly.setSendtotal(tempMonthly.getSendtotal() + rcs.getSendtotal());
			tempMonthly.setNotsend(tempMonthly.getNotsend() + rcs.getNotsend());
			tempMonthly.setSubmitsuccess(tempMonthly.getSubmitsuccess() + rcs.getSubmitsuccess());
			tempMonthly.setSubretsuccess(tempMonthly.getSubretsuccess() + rcs.getSubretsuccess());
			tempMonthly.setReportsuccess(tempMonthly.getReportsuccess() + rcs.getReportsuccess());
			tempMonthly.setSubmitfail(tempMonthly.getSubmitfail() + rcs.getSubmitfail());
			tempMonthly.setSubretfail(tempMonthly.getSubretfail() + rcs.getSubretfail());
			tempMonthly.setReportfail(tempMonthly.getReportfail() + rcs.getReportfail());
			temp.put(key, tempMonthly);// 按分组保存每月明细

			// 合计（累加每月明细）
			monthlySumData.setChargetotal(monthlySumData.getChargetotal() + rcs.getChargetotal());
			monthlySumData.setCosttotal(monthlySumData.getCosttotal().add(rcs.getCosttotal()));
			monthlySumData.setSendtotal(monthlySumData.getSendtotal() + rcs.getSendtotal());
			monthlySumData.setNotsend(monthlySumData.getNotsend() + rcs.getNotsend());
			monthlySumData.setSubmitsuccess(monthlySumData.getSubmitsuccess() + rcs.getSubmitsuccess());
			monthlySumData.setSubretsuccess(monthlySumData.getSubretsuccess() + rcs.getSubretsuccess());
			monthlySumData.setReportsuccess(monthlySumData.getReportsuccess() + rcs.getReportsuccess());
			monthlySumData.setSubmitfail(monthlySumData.getSubmitfail() + rcs.getSubmitfail());
			monthlySumData.setSubretfail(monthlySumData.getSubretfail() + rcs.getSubretfail());
			monthlySumData.setReportfail(monthlySumData.getReportfail() + rcs.getReportfail());
		}

		// 将所有分组统计出来的每月明细数据取出放到一个List中
		for (Entry<String, RecordChannelStatistics> tempEntry : temp.entrySet()) {
			monthlyDataList.add(tempEntry.getValue());
		}

		LOGGER.debug("根据临时数据,计算出月明细数据{} ", JsonUtils.toJson(monthlyDataList));
		LOGGER.debug("根据临时数据,计算出月合计数据{} ", JsonUtils.toJson(monthlySumData));
		
		// 清除t_sms_record_channel_statistic 表统计月的老数据
		LOGGER.debug(" 写入每月统计数据前，清除统计表中老的每月数据，月份 = {}",statDayStr);
		recordChannelStatisticsService.deleteByDate(statDayStr);
		
		// 保存月明细数据
		recordChannelStatisticsService.insertBatch(monthlyDataList);
		// 保存月合计数据
		recordChannelStatisticsService.insert(monthlySumData);
		
		return dailyDataList;
	}
	
	/**
	 * 基于 t_sms_record_channel_statistics 的数据计算<br>
	 * 将统计结果保存到 t_sms_record_consume_stat
	 * @param statDay
	 */
	private void recordConsumeStatistic(DateTime statDay, List<RecordChannelStatistics> RecordChannelStatisticsDataList) {
		String statDayStr = statDay.toString("yyyyMMdd");
		Date now = new Date();
		LOGGER.debug("通道消耗量统计方法开始，统计日期={} ----------------", statDayStr);
		
		if (RecordChannelStatisticsDataList == null || RecordChannelStatisticsDataList.isEmpty()) {
			LOGGER.debug("由于当天的通道侧基础统计数据为空，通道消耗量统计方法直接结束");
			return;
		}
		
		// 数据库查询统计方式
		/*List<RecordConsumeStatistics> statDataList = recordChannelStatisticsMapper.generateDataForRecordConsume(statDayStr);
		for (RecordConsumeStatistics rcs : statDataList) {
			Long belongBusiness = rcs.getBelongBusiness();
			Integer channelId = rcs.getChannelid();
			Channel channelBO = channelService.getByCid(channelId);
			User userBO = userService.getById(belongBusiness);
			
			// 还有 saletotal（客户侧销售收入）字段没有计算改字段交给其他任务计算
			rcs.setDepartmentId(userBO.getDepartmentId());
			rcs.setCostprice(channelBO.getCostprice());
			rcs.setStattype(0); // stattype 0:每日  2:每月
			rcs.setDate(Integer.valueOf(statDayStr));
			rcs.setCreatetime(now);
		}
		
		LOGGER.debug("写入每日统计数据前，清除统计表中老的每日明细数据，日期 = {} ", statDayStr);
		recordConsumeStatisticsService.deleteByDate(statDayStr);
		recordConsumeStatisticsMapper.insertBatch(statDataList);*/
		
		
		List<RecordConsumeStatistics> daliyDataList = new ArrayList<>();
		Map<String, RecordConsumeStatistics> daliyTempMap = new HashMap<>();
		// 根据分组条件计算每日数据统计
		for (RecordChannelStatistics rcs : RecordChannelStatisticsDataList) {
			// 确定分组条件 ： channelid + belong_business + smstype + paytype + operatorstype
			String key = rcs.getChannelid() + "-" + rcs.getBelongBusiness() + "-" + rcs.getSmstype() + "-"
					   + rcs.getPaytype() + "-" + rcs.getOperatorstype();
			
			RecordConsumeStatistics tempDaliy = daliyTempMap.get(key);
			if (tempDaliy == null) {
				tempDaliy = new RecordConsumeStatistics();
				tempDaliy.setChannelid(rcs.getChannelid());
				tempDaliy.setBelongBusiness(rcs.getBelongBusiness());
				tempDaliy.setDepartmentId(null);
				tempDaliy.setSmstype(rcs.getSmstype());
				tempDaliy.setPaytype(rcs.getPaytype());
				tempDaliy.setRemark(rcs.getRemark());
				tempDaliy.setOperatorstype(rcs.getOperatorstype());
				tempDaliy.setCostprice(BigDecimal.ZERO);
				tempDaliy.setCosttotal(BigDecimal.ZERO);
				tempDaliy.setSaletotal(BigDecimal.ZERO);
				tempDaliy.setNotsend(0);
				tempDaliy.setSubmitsuccess(0);
				tempDaliy.setSubretsuccess(0);
				tempDaliy.setReportsuccess(0);
				tempDaliy.setSubmitfail(0);
				tempDaliy.setSubretfail(0);
				tempDaliy.setReportfail(0);
				tempDaliy.setStattype(0);
				tempDaliy.setDate(Integer.valueOf(statDayStr));
				tempDaliy.setCreatetime(now);
			}
			
			// 计算分组条件后需要累计的字段
			tempDaliy.setCosttotal(tempDaliy.getCosttotal().add(rcs.getCosttotal()));
			tempDaliy.setNotsend(tempDaliy.getNotsend() + rcs.getNotsend());
			tempDaliy.setSubmitsuccess(tempDaliy.getSubmitsuccess() + rcs.getSubmitsuccess());
			tempDaliy.setSubretsuccess(tempDaliy.getSubretsuccess() + rcs.getSubretsuccess());
			tempDaliy.setReportsuccess(tempDaliy.getReportsuccess() + rcs.getReportsuccess());
			tempDaliy.setSubmitfail(tempDaliy.getSubmitfail() + rcs.getSubmitfail());
			tempDaliy.setSubretfail(tempDaliy.getSubretfail() + rcs.getSubretfail());
			tempDaliy.setReportfail(tempDaliy.getReportfail() + rcs.getReportfail());
			
			daliyTempMap.put(key, tempDaliy);
		}
		
		// 获得分组计算后每日数据保存到 daliyDataList
		for (Entry<String, RecordConsumeStatistics> tempEntry : daliyTempMap.entrySet()) {
			daliyDataList.add(tempEntry.getValue());
		}
		
		// 补充每日数据 daliyDataList查询 “通道单价”、“归属商务对应的部门信息”
		for (RecordConsumeStatistics rcs : daliyDataList) {
			Long belongBusiness = rcs.getBelongBusiness();
			Integer channelId = rcs.getChannelid();
			Channel channelBO = channelService.getByCid(channelId);
			User userBO = userService.getById(belongBusiness);

			// saletotal（客户侧销售收入）字段这里没有计算
//			rcs.setDepartmentId(userBO != null ? userBO.getDepartmentId() : null);
			if(userBO!=null) {
				JsmsDepartment fistLevel = jsmsDepartmentService.getFistLevelDeparment(userBO.getDepartmentId());
				if (fistLevel != null)
					rcs.setDepartmentId(fistLevel.getDepartmentId());
			}

			// 通道成本价在message库保存的单位是元，在流水库保存的是厘
//			rcs.setCostprice(channelBO != null ? channelBO.getCostprice() : BigDecimal.ZERO);
			if(channelBO != null)
				rcs.setCostprice(channelBO.getCostprice().multiply(new BigDecimal(1000)));
			else
				rcs.setCostprice(BigDecimal.ZERO);

			rcs.setStattype(0); // stattype 0:每日  2:每月
			rcs.setDate(Integer.valueOf(statDayStr));
			rcs.setCreatetime(now);
		}
		LOGGER.debug("通道消耗量统计:计算出{}日明细数据{} ", statDayStr, JsonUtils.toJson(daliyDataList));
		
		LOGGER.debug("通道消耗量统计:写入{}日统计数据前，清除统计表中老的每日明细数据", statDayStr);
		recordConsumeStatisticsService.deleteByDate(statDayStr);
		recordConsumeStatisticsService.insertBatch(daliyDataList);
		
		
		// 获得当月所有已经存在的每日数据用于计算每月数据
		Map<String, Object> sqlParams = new HashMap<>();
		String statMothStr = statDay.toString("yyyyMM");
		sqlParams.put("stattype", 0);
		sqlParams.put("statMothStr", statMothStr);
		List<RecordConsumeStatistics> allDailyDataList = recordConsumeStatisticsService.queryMonthly(sqlParams);
		

		// 统计每月数据
		List<RecordConsumeStatistics> monthlyDataList = new ArrayList<>();
		Map<String, RecordConsumeStatistics> temp = new HashMap<>();
		for (RecordConsumeStatistics rcs : allDailyDataList) {
			// 分组条件 ： channelid + belong_business + smstype + paytype + operatorstype
			String key = rcs.getChannelid() + "-" + rcs.getBelongBusiness() + "-" + rcs.getSmstype() + "-"
					   + rcs.getPaytype() + "-" + rcs.getOperatorstype();

			RecordConsumeStatistics tempMonthly = temp.get(key);
			if (tempMonthly == null) {
				tempMonthly = new RecordConsumeStatistics();
				tempMonthly.setChannelid(rcs.getChannelid());
				tempMonthly.setBelongBusiness(rcs.getBelongBusiness());
				tempMonthly.setDepartmentId(rcs.getDepartmentId());
				tempMonthly.setSmstype(rcs.getSmstype());
				tempMonthly.setPaytype(rcs.getPaytype());
				tempMonthly.setRemark(rcs.getRemark());
				tempMonthly.setOperatorstype(rcs.getOperatorstype());
				tempMonthly.setCostprice(BigDecimal.ZERO);
				tempMonthly.setCosttotal(BigDecimal.ZERO);
				tempMonthly.setSaletotal(BigDecimal.ZERO);
				tempMonthly.setNotsend(0);
				tempMonthly.setSubmitsuccess(0);
				tempMonthly.setSubretsuccess(0);
				tempMonthly.setReportsuccess(0);
				tempMonthly.setSubmitfail(0);
				tempMonthly.setSubretfail(0);
				tempMonthly.setReportfail(0);
				tempMonthly.setStattype(2); // stattype 0:每日  2:每月
				tempMonthly.setDate(Integer.valueOf(statMothStr));
				tempMonthly.setCreatetime(now);
			}
			
			// 计算分组条件后需要累计的字段
			tempMonthly.setCostprice(tempMonthly.getCostprice().add(rcs.getCostprice()));
			tempMonthly.setCosttotal(tempMonthly.getCosttotal().add(rcs.getCosttotal()));
			tempMonthly.setSaletotal(tempMonthly.getSaletotal().add(rcs.getSaletotal()));
			tempMonthly.setNotsend(tempMonthly.getNotsend() + rcs.getNotsend());
			tempMonthly.setSubmitsuccess(tempMonthly.getSubmitsuccess() + rcs.getSubmitsuccess());
			tempMonthly.setSubretsuccess(tempMonthly.getSubretsuccess() + rcs.getSubretsuccess());
			tempMonthly.setReportsuccess(tempMonthly.getReportsuccess() + rcs.getReportsuccess());
			tempMonthly.setSubmitfail(tempMonthly.getSubmitfail() + rcs.getSubmitfail());
			tempMonthly.setSubretfail(tempMonthly.getSubretfail() + rcs.getSubretfail());
			tempMonthly.setReportfail(tempMonthly.getReportfail() + rcs.getReportfail());
			
			temp.put(key, tempMonthly);
		}
		
		// 获得分组计算后每月数据保存到 monthlyDataList
		for (Entry<String, RecordConsumeStatistics> tempEntry : temp.entrySet()) {
			monthlyDataList.add(tempEntry.getValue());
		}
		LOGGER.debug("通道消耗量统计:计算出{}月明细数据 = {}",statMothStr, JsonUtils.toJson(monthlyDataList));
		
		// 清除 t_sms_record_consume_stat 表统计月的老数据
		LOGGER.debug("通道消耗量统计:写入{}月统计数据前，清除统计表中老的每月数据",statMothStr);
		recordConsumeStatisticsService.deleteByDate(statMothStr);
		
		// 保存月明细数据
		recordConsumeStatisticsService.insertBatch(monthlyDataList);
		
		LOGGER.debug("通道消耗量统计方法结束，统计日期={}----------------", statDayStr);
	}

}
