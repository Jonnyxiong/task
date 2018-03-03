package com.ucpaas.sms.task.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ucpaas.sms.task.entity.record.RecordChannelStatistics;
import com.ucpaas.sms.task.entity.record.RecordChannelTempStatistics;
import com.ucpaas.sms.task.enum4sms.PayType;
import com.ucpaas.sms.task.enum4sms.RecordChannelStatisticsType;
import com.ucpaas.sms.task.mapper.record.RecordChannelStatisticsMapper;
import com.ucpaas.sms.task.model.TaskInfo;
import com.ucpaas.sms.task.statistic.RecordStatisticStrategy;
import com.ucpaas.sms.task.util.JsonUtils;
import com.ucpaas.sms.task.util.UcpaasDateUtils;

/**
 * @author huangwenjie
 * @title
 * @description 通道发送统计(分享者通道运营统计)
 * @date 2017-02-26
 */
@Service
public class RecordChannelStatisticsServiceImpl implements RecordChannelStatisticsService {
    private static final Logger LOGGER = LoggerFactory.getLogger("RecordChannelStatisticsService");


    @Autowired
    private RecordChannelStatisticsMapper recordChannelStatisticsMapper;

    @Autowired
    private RecordChannelTempDataStatisticsService recordChannelTempDataStatisticsService;

    @Autowired
    @Qualifier("demand1dot5RecordStatistic")
    private RecordStatisticStrategy recordStatisticStrategy;

    @Override
    public int insert(RecordChannelStatistics model) {
        return this.recordChannelStatisticsMapper.insert(model);
    }

    @Override
    public int insertBatch(List<RecordChannelStatistics> modelList) {
        return this.recordChannelStatisticsMapper.insertBatch(modelList);
    }

    @Override
    public int update(RecordChannelStatistics model) {
        return this.recordChannelStatisticsMapper.update(model);
    }

    @Override
    public int updateSelective(RecordChannelStatistics model) {
        return this.recordChannelStatisticsMapper.updateSelective(model);
    }

    @Override
    public RecordChannelStatistics getById(Long id) {
        return this.recordChannelStatisticsMapper.getById(id);
    }

    @Override
    @Transactional(value = "record")
    public List<RecordChannelStatistics> queryAll(Map<String, Object> params) {
        return this.recordChannelStatisticsMapper.queryAll(params);
    }

    @Override
    @Transactional(value = "record")
    public List<RecordChannelStatistics> queryAllGroupBy(Map<String, Object> params) {
        return this.recordChannelStatisticsMapper.queryAllGroupBy(params);
    }

    @Override
    public int count(Map<String, Object> params) {
        return this.recordChannelStatisticsMapper.count(params);
    }

    @Override
    public int deleteByDate(String statTime) {
        return this.recordChannelStatisticsMapper.deleteByDate(statTime);
    }

    @Override
    public List<RecordChannelStatistics> queryMonthly(Map<String, Object> params) {
        return this.recordChannelStatisticsMapper.queryMonthly(params);
    }

    @Override
    @Transactional
    public boolean fourDaysAgo(TaskInfo taskInfo) {
        String format = taskInfo.getExecuteType().getFormat();
        DateTime executeNext = UcpaasDateUtils.parseDate(taskInfo.getExecuteNext(), format);

        // 任务统计第前四天的Record流水表，所以任务的执行时间需要减去4天
        DateTime statDay = executeNext.minusDays(4);
        long begainTime = System.currentTimeMillis();
        LOGGER.debug("第前四天的通道统计报表任务【开始】：统计日期 = {} ------------------", statDay.toString("yyyyMMdd"));

        try {
            recordStatisticStrategy.statistics(statDay);
        } catch (Exception e) {
            LOGGER.error("第前四天的通道侧统计任务【失败】：", e);
            return false;
        }

        LOGGER.debug("第前四天的通道统计报表任务【结束】：耗时 = {}", System.currentTimeMillis() - begainTime);
        return true;
    }

    @Override
    public boolean yesterday(TaskInfo taskInfo) {
        String format = taskInfo.getExecuteType().getFormat();
        DateTime executeNext = UcpaasDateUtils.parseDate(taskInfo.getExecuteNext(), format);

        // 任务统计昨天的Record流水表，所以任务的执行时间需要减去1天
        DateTime statDay = executeNext.minusDays(1);
        long begainTime = System.currentTimeMillis();
        LOGGER.debug("第前一天(昨天)的通道统计报表任务：统计日期 = {} ------------------", statDay.toString("yyyyMMdd"));

        try {
            recordStatisticStrategy.statistics(statDay);
        } catch (Exception e) {
            LOGGER.error("第前一天(昨天)的通道侧统计任务【失败】：", e);
            return false;
        }

        LOGGER.debug("第前一天(昨天)的通道统计报表任务【结束】：耗时 = {}", System.currentTimeMillis() - begainTime);
        return true;
    }


    /**
     * 老的通道侧统计任务
     *
     * @param statDay
     * @return
     */
    @Deprecated
    private boolean generateDataIn(DateTime statDay) {
        String statTime = statDay.toString("yyyyMMdd");
        Date now = new Date();

        LOGGER.debug("清除统计的数据时间date={}的每日明细数据和每日合计数据 ", statTime);
        recordChannelStatisticsMapper.deleteByDate(statTime);
        List<RecordChannelTempStatistics> tempList = recordChannelTempDataStatisticsService.generateData(statTime);

        LOGGER.debug("根据时间段，遍历所有的record表，生成TmpSmsRecordChannelStatistics临时数据{} ", JsonUtils.toJson(tempList));

        if (tempList == null || tempList.isEmpty()) {
            LOGGER.debug("date={}的record统计数据为空", statTime);
            return true;
        }

        List<RecordChannelStatistics> dailyStatistics = new ArrayList<>(); // 每日详细数据  stattype=0

        RecordChannelStatistics dailySumStatistics = new RecordChannelStatistics(); // 每日合计数据
        dailySumStatistics.setChannelid(-1);
        dailySumStatistics.setRemark(" - ");
        dailySumStatistics.setOperatorstype(-2);
        dailySumStatistics.setStattype(RecordChannelStatisticsType.dailySum.getValue());
        dailySumStatistics.setDate(Integer.valueOf(statTime));
        dailySumStatistics.setCreatetime(now);
        dailySumStatistics.setChargetotal(0);
        dailySumStatistics.setCosttotal(BigDecimal.ZERO);
        dailySumStatistics.setSendtotal(0);
        dailySumStatistics.setNotsend(0);
        dailySumStatistics.setSubmitsuccess(0);
        dailySumStatistics.setSubretsuccess(0);
        dailySumStatistics.setReportsuccess(0);
        dailySumStatistics.setSubmitfail(0);
        dailySumStatistics.setSubretfail(0);
        dailySumStatistics.setReportfail(0);
        dailySumStatistics.setClientid(" - ");
        dailySumStatistics.setBelongSale(null);
        dailySumStatistics.setPaytype(PayType.daily.getValue());
        dailySumStatistics.setBelongBusiness(null);
        dailySumStatistics.setSmstype(null);

        for (RecordChannelTempStatistics temp : tempList) {
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
            dailyStatistics.add(rcs);

            // 日合计
            dailySumStatistics.setChargetotal(dailySumStatistics.getChargetotal() + temp.getChargetotal().intValue());
            dailySumStatistics.setCosttotal(dailySumStatistics.getCosttotal().add(temp.getCosttotal()));
            dailySumStatistics.setSendtotal(dailySumStatistics.getSendtotal() + temp.getSendtotal().intValue());
            dailySumStatistics.setNotsend(dailySumStatistics.getNotsend() + temp.getNotsend().intValue());
            dailySumStatistics.setSubmitsuccess(dailySumStatistics.getSubmitsuccess() + temp.getSubmitsuccess().intValue());
            dailySumStatistics.setSubretsuccess(dailySumStatistics.getSubretsuccess() + temp.getSubretsuccess().intValue());
            dailySumStatistics.setReportsuccess(dailySumStatistics.getReportsuccess() + temp.getReportsuccess().intValue());
            dailySumStatistics.setSubmitfail(dailySumStatistics.getSubmitfail() + temp.getSubmitfail().intValue());
            dailySumStatistics.setSubretfail(dailySumStatistics.getSubretfail() + temp.getSubretfail().intValue());
            dailySumStatistics.setReportfail(dailySumStatistics.getReportfail() + temp.getReportfail().intValue());
        }

        LOGGER.debug("根据临时数据,计算出日明细数据{} ", JsonUtils.toJson(dailyStatistics));
        LOGGER.debug("根据临时数据,计算出日合计数据{} ", JsonUtils.toJson(dailySumStatistics));
        // 保存日明细数据
        recordChannelStatisticsMapper.insertBatch(dailyStatistics);
        // 保存日合计数据
        recordChannelStatisticsMapper.insert(dailySumStatistics);


        // 清除t_sms_record_channel_statistic 表统计月的老数据
        statTime = statDay.toString("yyyyMM");
        LOGGER.debug(" 清除t_sms_record_channel_statistic 表统计月的老数据 , date={} ", statTime);
        recordChannelStatisticsMapper.deleteByDate(statTime);

        // 获得当月所有已经存在的每日明细数据
        Map<String, Object> params = new HashMap<>();
        String dataTimePrix = statTime.substring(0, 6);
        params.put("stattype", RecordChannelStatisticsType.daily.getValue());
        params.put("dataTimePrix", dataTimePrix);

        List<RecordChannelStatistics> rcss = recordChannelStatisticsMapper.queryMonthly(params);
        LOGGER.debug("找出本月所有t_sms_record_channel_statistic表中 {} 月的每日明细数据 {} ", dataTimePrix, JsonUtils.toJson(rcss));

        List<RecordChannelStatistics> monthlyStatistics = new ArrayList<>(); // 每月明细数据

        RecordChannelStatistics monthlySumStatistics = new RecordChannelStatistics(); // 每月合计数据
        monthlySumStatistics.setChannelid(-1);
        monthlySumStatistics.setRemark("-");
        monthlySumStatistics.setOperatorstype(-2);
        monthlySumStatistics.setChargetotal(0);
        monthlySumStatistics.setCosttotal(BigDecimal.ZERO);
        monthlySumStatistics.setSendtotal(0);
        monthlySumStatistics.setNotsend(0);
        monthlySumStatistics.setSubmitsuccess(0);
        monthlySumStatistics.setSubretsuccess(0);
        monthlySumStatistics.setReportsuccess(0);
        monthlySumStatistics.setSubmitfail(0);
        monthlySumStatistics.setSubretfail(0);
        monthlySumStatistics.setReportfail(0);
        monthlySumStatistics.setStattype(RecordChannelStatisticsType.monthlySum.getValue());
        monthlySumStatistics.setDate(Integer.valueOf(statTime.substring(0, 6)));
        monthlySumStatistics.setCreatetime(now);
        monthlySumStatistics.setClientid(" - ");
        monthlySumStatistics.setBelongSale(null);
        monthlySumStatistics.setPaytype(PayType.montyly.getValue());
        monthlySumStatistics.setBelongBusiness(null);
        monthlySumStatistics.setSmstype(null);

        // 月明细数据
        Map<String, RecordChannelStatistics> temp = new HashMap<>();
        for (RecordChannelStatistics rcs : rcss) {

            String key = rcs.getChannelid() + "-" + rcs.getClientid() + "-" + rcs.getPaytype() + "-" + rcs.getBelongSale()
                    + "-" + rcs.getBelongBusiness() + "-" + rcs.getSmstype();

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
                tempMonthly.setDate(Integer.valueOf(statTime.substring(0, 6)));
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
            temp.put(key, tempMonthly);

            // 合计
            monthlySumStatistics.setChargetotal(monthlySumStatistics.getChargetotal() + rcs.getChargetotal());
            monthlySumStatistics.setCosttotal(monthlySumStatistics.getCosttotal().add(rcs.getCosttotal()));
            monthlySumStatistics.setSendtotal(monthlySumStatistics.getSendtotal() + rcs.getSendtotal());
            monthlySumStatistics.setNotsend(monthlySumStatistics.getNotsend() + rcs.getNotsend());
            monthlySumStatistics.setSubmitsuccess(monthlySumStatistics.getSubmitsuccess() + rcs.getSubmitsuccess());
            monthlySumStatistics.setSubretsuccess(monthlySumStatistics.getSubretsuccess() + rcs.getSubretsuccess());
            monthlySumStatistics.setReportsuccess(monthlySumStatistics.getReportsuccess() + rcs.getReportsuccess());
            monthlySumStatistics.setSubmitfail(monthlySumStatistics.getSubmitfail() + rcs.getSubmitfail());
            monthlySumStatistics.setSubretfail(monthlySumStatistics.getSubretfail() + rcs.getSubretfail());
            monthlySumStatistics.setReportfail(monthlySumStatistics.getReportfail() + rcs.getReportfail());
        }

        /**
         * 完成每月明细数据统计
         */
        for (Entry<String, RecordChannelStatistics> tempEntry : temp.entrySet()) {
            monthlyStatistics.add(tempEntry.getValue());
        }

        LOGGER.debug("根据临时数据,计算出月明细数据{} ", JsonUtils.toJson(monthlyStatistics));
        LOGGER.debug("根据临时数据,计算出月合计数据{} ", JsonUtils.toJson(monthlySumStatistics));
        // 保存月明细数据
        recordChannelStatisticsMapper.insertBatch(monthlyStatistics);
        // 保存月合计数据
        recordChannelStatisticsMapper.insert(monthlySumStatistics);
        return true;
    }

    @Override
    public BigDecimal computeClientChannelCostFee(String clientId, Long belongSale, Integer smsType,
                                                  Integer payType, String yyyyMMoryyyyMMdd, Integer operatorstype) {

        // 参数校验（因为线上数据存在为null的情况，这里的参数校验暂时不用）
    	/*if(clientId == null)
    		throw new RecordChannelStatisticsException("clientId不能为null");
    	if(belongSale == null)
    		throw new RecordChannelStatisticsException("belongSale不能为null");
    	if(smsType == null)
    		throw new RecordChannelStatisticsException("smsType不能为null");
    	if(payType == null)
    		throw new RecordChannelStatisticsException("payType不能为null");
    	if(yyyyMMoryyyyMMdd == null){
    		throw new RecordChannelStatisticsException("yyyyMMoryyyyMMdd不能为null");
    	}
    	if(yyyyMMoryyyyMMdd.length() != 8 && yyyyMMoryyyyMMdd.length() != 6){
    		throw new RecordChannelStatisticsException("yyyyMMoryyyyMMdd长度非法");
    	}*/
    		

        // 通道总成本价
        BigDecimal channelCostFee = BigDecimal.ZERO;
        if (yyyyMMoryyyyMMdd.length() == 8) { //查询日数据
            Map<String, Object> sqlParams = new HashMap<String, Object>();
            sqlParams.put("clientid", clientId);
            sqlParams.put("belongSale", belongSale);
            sqlParams.put("smstype", smsType);
            sqlParams.put("paytype", payType);
            sqlParams.put("stattype", RecordChannelStatisticsType.daily.getValue().intValue());
            sqlParams.put("date", yyyyMMoryyyyMMdd);
            sqlParams.put("operatorstype", operatorstype);
            List<RecordChannelStatistics> recordChannelStatisticsList = this.queryAll(sqlParams);

            for (RecordChannelStatistics rcs : recordChannelStatisticsList) {
                channelCostFee = channelCostFee.add(rcs.getCosttotal());
            }

        } else {//查询月数据
            Map<String, Object> sqlParams = new HashMap<String, Object>();
            sqlParams.put("clientid", clientId);
            sqlParams.put("belongSale", belongSale);
            sqlParams.put("smstype", smsType);
            sqlParams.put("paytype", payType);
            sqlParams.put("stattype", RecordChannelStatisticsType.daily.getValue().intValue());
            sqlParams.put("likeDate", yyyyMMoryyyyMMdd);
            List<RecordChannelStatistics> recordChannelStatisticsList = this.queryAll(sqlParams);

            for (RecordChannelStatistics rcs : recordChannelStatisticsList) {
                channelCostFee = channelCostFee.add(rcs.getCosttotal());
            }
        }
        return channelCostFee;

    }

}
