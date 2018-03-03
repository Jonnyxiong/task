package com.ucpaas.sms.task.statistic;

import com.ucpaas.sms.task.entity.access.AccessChannelStatistics;
import com.ucpaas.sms.task.entity.access.CustomerStatTemp;
import com.ucpaas.sms.task.enum4sms.AccessChannelStatisticsType;
import com.ucpaas.sms.task.service.AccessChannelStatisticsService;
import com.ucpaas.sms.task.service.CustomerStatTempService;
import com.ucpaas.sms.task.util.JsonUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 原本的用户侧统计access，统计日、月的流水
 * @author huangwenjie
 *
 */
@Service
public class OriginalAccessStatistic implements AccessStatisticStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger("AccessChannelStatisticsService");

    @Autowired
    private AccessChannelStatisticsService accessChannelStatisticsService;
    @Autowired
    private CustomerStatTempService customerStatTempService;


    @Override
    public void staticAlgorithm(DateTime statDay) {
        String statTime = statDay.toString("yyyyMMdd");
        Date now = new Date();


        /**
         * 每个客户在每个通道下面的明细数据(通道0和不为0是分开统计)
         * 通道为0的情况下，也分两种状态去统计，一种是失败(state=4)另一种是拦截(state=0/5/7/8/9/10)
         */
        List<AccessChannelStatistics> dailyStatistics = new ArrayList<>(); // 每日详细数据
        // stattype=0
        List<AccessChannelStatistics> dailySumStatistics = new ArrayList<>(); // 每日合计数据 stattype=1

        List<AccessChannelStatistics> monthlyStatistics = new ArrayList<>(); // 每日详细数据 stattype=2
        //
        List<AccessChannelStatistics> monthlySumStatistics = new ArrayList<>(); // 每日合计数据  stattype=3

        List<CustomerStatTemp> customerStatTemps = customerStatTempService.generateData(statTime);

        if (customerStatTemps == null || customerStatTemps.size() == 0) {
            LOGGER.debug("根据时间段，遍历所有的access表，生成CustomerStatTemp临时数据为空，统计结束 ");
            return;
        }

        LOGGER.debug("清除统计的数据时间date={}的数据 ", statTime);
        accessChannelStatisticsService.deleteByDate(statTime);
        LOGGER.debug("根据时间段，遍历所有的access表，生成CustomerStatTemp临时数据{} ", JsonUtils.toJson(customerStatTemps));


        Map<String, AccessChannelStatistics> temp = new HashMap<>();
        for (CustomerStatTemp cst : customerStatTemps) {
            // Integer chargeTotal = 0; //计费条数，状态是1 + 3 + 4 + 6
            // Integer overrateChargeTotal = 0;//超频计费条数
            // 分组条件clientid + product_type + channelid + paytype + belong_sale + smstype + sub_id
            String key = cst.getClientid() + "-" + cst.getProductType() + "-" + cst.getChannelid() + "-"
                    + cst.getPaytype() + "-" + cst.getBelongSale() + "-" + cst.getSmstype() + "-" + cst.getSubId();
            AccessChannelStatistics acs = temp.get(key);
            if (acs == null) {
                acs = new AccessChannelStatistics();
                acs.setAgentId(cst.getAgentId().intValue());
                acs.setClientid(cst.getClientid());
                acs.setName(cst.getUsername());
                acs.setSid(cst.getSid());
                acs.setPaytype(cst.getPaytype());
                acs.setOperatorstype(cst.getOperatorstype());
                acs.setChannelid(cst.getChannelid());
                acs.setRemark(cst.getChannelremark());
                acs.setProductType(cst.getProductType());
                acs.setSubId(cst.getSubId());
                acs.setStattype(AccessChannelStatisticsType.daily.getValue());
                acs.setDate(cst.getDate());
                acs.setCreatetime(now);
                acs.setChargetotal(0);
                acs.setOverrateChargeTotal(0);
                acs.setCostfee(BigDecimal.ZERO);
                acs.setSalefee(BigDecimal.ZERO);
                acs.setProductfee(BigDecimal.ZERO);
                acs.setUsersmstotal(0);
                acs.setSendtotal(0);
                acs.setNotsend(0);
                acs.setSubmitsuccess(0);
                acs.setReportsuccess(0);
                acs.setSubmitfail(0);
                acs.setSubretfail(0);
                acs.setReportfail(0);
                acs.setAuditfail(0);
                acs.setRecvintercept(0);
                acs.setSendintercept(0);
                acs.setOverrateintercept(0);
                acs.setBelongSale(cst.getBelongSale());
                acs.setSmstype(cst.getSmstype());
            }

            if (!cst.getChannelid().equals(0)) {

                //计费条数  chargeTotal = 1 + 3 + 4 + 6，通道不为0的情况下，不应该存在4，因为4需要收费，所以怕漏了，这里也计算上去
                int chargetTotal = cst.getSubmitsuccess() + cst.getReportsuccess()
                        + cst.getSubmitfail() + cst.getReportfail();
                acs.setChargetotal(acs.getChargetotal() + chargetTotal);

                //超频计费条数，在正常数据中，通道id=0，所以这里会为0
                acs.setOverrateChargeTotal(acs.getOverrateChargeTotal() + cst.getOverrateChargeTotal());

                //通道成本 *计费条数，由于在通道id=0的情况下，超频计费条数为0，所以不用算超频计费
                BigDecimal costfee = cst.getCostfee().multiply(new BigDecimal(chargetTotal));
                acs.setCostfee(acs.getCostfee().add(costfee));

                //消费金额(针对客户) = 产品销售价 *计费条数，由于在通道id=0的情况下，超频计费条数为0，所以不用算超频计费
                BigDecimal salefee = cst.getSalefee().multiply(new BigDecimal(chargetTotal));
                acs.setSalefee(acs.getSalefee().add(salefee));

                //消耗成本(针对代理商) = 产品成本价 *计费条数，由于在通道id=0的情况下，超频计费条数为0，所以不用算超频计费
                BigDecimal productfee = cst.getProductfee().multiply(new BigDecimal(chargetTotal));
                acs.setProductfee(acs.getProductfee().add(productfee));

                //接受短信总条数  usersmstotal = 0 + 1 + 3 + 4 + 5 + 6 + 7 + 8 + 9 + 10
                //通道不为0的情况下，正常情况下不应该存在0 + 5 + 6 + 7 + 8 + 9 + 10，存在则为异常
                int usersmstotal = cst.getNotsend() + cst.getSubmitsuccess() + cst.getReportsuccess()
                        + cst.getSubmitfail() + cst.getSubretfail() + cst.getReportfail() + cst.getAuditfail()
                        + cst.getRecvintercept() + cst.getSendintercept() + cst.getOverrateintercept();
                acs.setUsersmstotal(acs.getUsersmstotal() + usersmstotal);

                //发送短信总条数  sendtotal = 1 + 3 + 6
                int sendTotal = cst.getSubmitsuccess() + cst.getReportsuccess() + cst.getReportfail();
                acs.setSendtotal(acs.getSendtotal() + sendTotal);

                acs.setNotsend(acs.getNotsend() + cst.getNotsend());
                acs.setSubmitsuccess(acs.getSubmitsuccess() + cst.getSubmitsuccess());
                acs.setReportsuccess(acs.getReportsuccess() + cst.getReportsuccess());
                acs.setSubmitfail(acs.getSubmitfail() + cst.getSubmitfail());
                acs.setSubretfail(acs.getSubretfail() + cst.getSubretfail());
                acs.setReportfail(acs.getReportfail() + cst.getReportfail());
                acs.setAuditfail(acs.getAuditfail() + cst.getAuditfail());
                acs.setRecvintercept(acs.getRecvintercept() + cst.getRecvintercept());
                acs.setSendintercept(acs.getSendintercept() + cst.getSendintercept());
                acs.setOverrateintercept(acs.getOverrateintercept() + cst.getOverrateintercept());

            } else {
                /**
                 * 通道为0状态为4的情况
                 * 需单独展示数据，所以跟上面另外算
                 */
                //计费条数  chargeTotal = 4
                int chargetTotal = cst.getSubmitfail();
                acs.setChargetotal(acs.getChargetotal() + chargetTotal);
                acs.setOverrateChargeTotal(acs.getOverrateChargeTotal() + 0);

                BigDecimal costfee = cst.getCostfee().multiply(new BigDecimal(chargetTotal));
                acs.setCostfee(acs.getCostfee().add(costfee));

                BigDecimal salefee = cst.getSalefee().multiply(new BigDecimal(chargetTotal));
                acs.setSalefee(acs.getSalefee().add(salefee));

                BigDecimal productfee = cst.getProductfee().multiply(new BigDecimal(chargetTotal));
                acs.setProductfee(acs.getProductfee().add(productfee));

                acs.setUsersmstotal(acs.getUsersmstotal() + cst.getSubmitfail());
                acs.setSendtotal(acs.getSendtotal() + 0);
                acs.setNotsend(acs.getNotsend() + 0);
                acs.setSubmitsuccess(acs.getSubmitsuccess() + 0);
                acs.setReportsuccess(acs.getReportsuccess() + 0);
                acs.setSubmitfail(acs.getSubmitfail() + cst.getSubmitfail());
                acs.setSubretfail(acs.getSubretfail() + 0);
                acs.setReportfail(acs.getReportfail() + 0);
                acs.setAuditfail(acs.getAuditfail() + 0);
                acs.setRecvintercept(acs.getRecvintercept() + 0);
                acs.setSendintercept(acs.getSendintercept() + 0);
                acs.setOverrateintercept(acs.getOverrateintercept() + 0);

            }

            temp.put(key, acs);
        }

        /**
         * 完成每日详细数据的两种统计（通道不为0和state=4）
         */
        for (Map.Entry<String, AccessChannelStatistics> tempEntry : temp.entrySet()) {
            dailyStatistics.add(tempEntry.getValue());
        }

        LOGGER.debug("完成每日详细数据的两种统计（通道不为0和state=4） 数据{} ", JsonUtils.toJson(dailyStatistics));
        /**
         * 每日详细数据统计：拦截数据，状态等于0/5/7/8/9/10
         */
        temp = new HashMap<>();
        for (CustomerStatTemp cst : customerStatTemps) {
            String key = cst.getClientid() + "-" + cst.getProductType() + "-" + cst.getChannelid() + "-"
                    + cst.getPaytype() + "-" + cst.getBelongSale() + "-" + cst.getSmstype() + "-" + cst.getSubId();

            AccessChannelStatistics acs = temp.get(key);
            if (acs == null) {
                acs = new AccessChannelStatistics();
                acs.setAgentId(cst.getAgentId().intValue());
                acs.setClientid(cst.getClientid());
                acs.setName(cst.getUsername());
                acs.setSid(cst.getSid());
                acs.setPaytype(cst.getPaytype());
                acs.setOperatorstype(-1);   // 通道运营商类型:0->全网，1->移动，2->联通，3->电信，4->国际；（为负数时表示数据类型，-1表示拦截
                // -2表示合计）
                acs.setChannelid(cst.getChannelid());
                acs.setRemark(" - ");
                acs.setProductType(cst.getProductType());
                acs.setSubId(cst.getSubId());
                acs.setStattype(AccessChannelStatisticsType.daily.getValue());
                acs.setDate(cst.getDate());
                acs.setCreatetime(now);

                acs.setChargetotal(0);
                acs.setOverrateChargeTotal(0);
                acs.setCostfee(BigDecimal.ZERO);
                acs.setSalefee(BigDecimal.ZERO);
                acs.setProductfee(BigDecimal.ZERO);
                acs.setUsersmstotal(0);
                acs.setSendtotal(0);
                acs.setNotsend(0);
                acs.setSubmitsuccess(0);
                acs.setReportsuccess(0);
                acs.setSubmitfail(0);
                acs.setSubretfail(0);
                acs.setReportfail(0);
                acs.setAuditfail(0);
                acs.setRecvintercept(0);
                acs.setSendintercept(0);
                acs.setOverrateintercept(0);
                acs.setBelongSale(cst.getBelongSale());
                acs.setSmstype(cst.getSmstype());
            }

            if (cst.getChannelid().equals(0)) {
                /**
                 * 通道为0，状态等于0/5/7/8/9/10，为 拦截数据
                 */
                //此时超频计费条数不为0
                //此时计费条数就等于超频计费条数
                int chargetTotal = cst.getOverrateChargeTotal();
                acs.setChargetotal(acs.getChargetotal() + chargetTotal);
                acs.setOverrateChargeTotal(acs.getOverrateChargeTotal() + chargetTotal);

                BigDecimal costfee = cst.getCostfee().multiply(new BigDecimal(chargetTotal));
                acs.setCostfee(acs.getCostfee().add(costfee));

                BigDecimal salefee = cst.getSalefee().multiply(new BigDecimal(chargetTotal));
                acs.setSalefee(acs.getSalefee().add(salefee));

                BigDecimal productfee = cst.getProductfee().multiply(new BigDecimal(chargetTotal));
                acs.setProductfee(acs.getProductfee().add(productfee));


                //接受短信总条数  usersmstotal = 0 +  5 + 6 + 7 + 8 + 9 + 10
                Integer usersmstotal = cst.getNotsend() + cst.getSubretfail() + cst.getAuditfail()
                        + cst.getRecvintercept() + cst.getSendintercept() + cst.getOverrateintercept();
                acs.setUsersmstotal(acs.getUsersmstotal() + usersmstotal);

                acs.setSendtotal(acs.getSendtotal() + 0);
                acs.setNotsend(acs.getNotsend() + cst.getNotsend());
                acs.setSubmitsuccess(acs.getSubmitsuccess() + 0);
                acs.setReportsuccess(acs.getReportsuccess() + 0);
                acs.setSubmitfail(acs.getSubmitfail() + 0);
                acs.setSubretfail(acs.getSubretfail() + cst.getSubretfail());
                acs.setReportfail(acs.getReportfail() + 0);
                acs.setAuditfail(acs.getAuditfail() + cst.getAuditfail());
                acs.setRecvintercept(acs.getRecvintercept() + cst.getRecvintercept());
                acs.setSendintercept(acs.getSendintercept() + cst.getSendintercept());
                acs.setOverrateintercept(acs.getOverrateintercept() + cst.getOverrateintercept());
                temp.put(key, acs);
            }

        }

        /**
         * 完成每日详细数据的三种统计（通道不为0、state=4、拦截），三种统计数据不应该互相有重叠
         */
        for (Map.Entry<String, AccessChannelStatistics> tempEntry : temp.entrySet()) {
            dailyStatistics.add(tempEntry.getValue());
        }

        LOGGER.debug("完成每日详细数据的三种统计（通道不为0、state=4、拦截），三种统计数据不应该互相有重叠  数据{} ", JsonUtils.toJson(dailyStatistics));

        if (dailyStatistics.size() > 0) {
            accessChannelStatisticsService.insertBatch(dailyStatistics);
        } else {
            LOGGER.info("客户运营运维统计报表任务【结束】：计算出来的每日统计数据为空");
            return;
        }

        /**
         * 每日合计数据统计
         */
        temp = new HashMap<>();
        for (AccessChannelStatistics cst : dailyStatistics) {
            String key = cst.getClientid();

            AccessChannelStatistics acs = temp.get(key);
            if (acs == null) {
                acs = new AccessChannelStatistics();
                acs.setAgentId(cst.getAgentId().intValue());
                acs.setClientid(cst.getClientid());
                acs.setName(cst.getName());
                acs.setSid("");
                acs.setPaytype(-1); // 付费类型，0：预付费，1：后付费，负数则为 -1:合计
                acs.setOperatorstype(-2);  // 通道运营商类型:0->全网，1->移动，2->联通，3->电信，4->国际；（为负数时表示数据类型，-1表示拦截
                // -2表示合计）
                acs.setChannelid(-1); // 负数则为 -1:合计
                acs.setRemark(" - ");
                acs.setProductType(-1);
                acs.setSubId(" - ");
                acs.setStattype(AccessChannelStatisticsType.dailySum.getValue());
                acs.setDate(cst.getDate());
                acs.setCreatetime(now);

                acs.setChargetotal(0);
                acs.setOverrateChargeTotal(0);
                acs.setCostfee(BigDecimal.ZERO);
                acs.setSalefee(BigDecimal.ZERO);
                acs.setProductfee(BigDecimal.ZERO);
                acs.setUsersmstotal(0);
                acs.setSendtotal(0);
                acs.setNotsend(0);
                acs.setSubmitsuccess(0);
                acs.setReportsuccess(0);
                acs.setSubmitfail(0);
                acs.setSubretfail(0);
                acs.setReportfail(0);
                acs.setAuditfail(0);
                acs.setRecvintercept(0);
                acs.setSendintercept(0);
                acs.setOverrateintercept(0);
                acs.setBelongSale(null);
                acs.setSmstype(null);
            }

            acs.setChargetotal(acs.getChargetotal() + cst.getChargetotal());
            acs.setOverrateChargeTotal(acs.getOverrateChargeTotal() + cst.getOverrateChargeTotal());
            acs.setCostfee(acs.getCostfee().add(cst.getCostfee()));
            acs.setSalefee(acs.getSalefee().add(cst.getSalefee()));
            acs.setProductfee(acs.getProductfee().add(cst.getProductfee()));
            acs.setUsersmstotal(acs.getUsersmstotal() + cst.getUsersmstotal());
            acs.setSendtotal(acs.getSendtotal() + cst.getSendtotal());
            acs.setNotsend(acs.getNotsend() + cst.getNotsend());
            acs.setSubmitsuccess(acs.getSubmitsuccess() + cst.getSubmitsuccess());
            acs.setReportsuccess(acs.getReportsuccess() + cst.getReportsuccess());
            acs.setSubmitfail(acs.getSubmitfail() + cst.getSubmitfail());
            acs.setSubretfail(acs.getSubretfail() + cst.getSubretfail());
            acs.setReportfail(acs.getReportfail() + cst.getReportfail());
            acs.setAuditfail(acs.getAuditfail() + cst.getAuditfail());
            acs.setRecvintercept(acs.getRecvintercept() + cst.getRecvintercept());
            acs.setSendintercept(acs.getSendintercept() + cst.getSendintercept());
            acs.setOverrateintercept(acs.getOverrateintercept() + cst.getOverrateintercept());
            temp.put(key, acs);

        }

        /**
         * 完成每日合计数据的统计
         */
        for (Map.Entry<String, AccessChannelStatistics> tempEntry : temp.entrySet()) {
            dailySumStatistics.add(tempEntry.getValue());
        }
        LOGGER.debug(" 完成每日合计数据的统计  数据{} ", JsonUtils.toJson(dailySumStatistics));
        accessChannelStatisticsService.insertBatch(dailySumStatistics);


        // 清除t_sms_access_channel_statistics 表统计月的老数据
        statTime = statDay.toString("yyyyMM");
        LOGGER.debug(" 清除t_sms_access_channel_statistics 表统计月的老数据 , date={} ", statTime);
        accessChannelStatisticsService.deleteByDate(statTime);


        // 获得当月所有已经存在的每日明细数据
        Map<String, Object> params = new HashMap<>();
        params.put("stattype", AccessChannelStatisticsType.daily.getValue());
        params.put("dataTimePrix", statTime.substring(0, 6));

        dailyStatistics = accessChannelStatisticsService.queryMonthly(params);


        /**
         * 每月详细数据统计
         */
        temp = new HashMap<>();
        for (AccessChannelStatistics cst : dailyStatistics) {
            String key = cst.getClientid() + "-" + cst.getChannelid() + "-" + cst.getPaytype() + "-" + cst.getOperatorstype()
                    + "-" + cst.getBelongSale() + "-" + cst.getSubId() + "-" + cst.getSmstype();

            AccessChannelStatistics acs = temp.get(key);
            if (acs == null) {
                acs = new AccessChannelStatistics();
                acs.setAgentId(cst.getAgentId().intValue());
                acs.setClientid(cst.getClientid());
                acs.setName(cst.getName());
                acs.setSid(cst.getSid());
                acs.setPaytype(cst.getPaytype()); // 付费类型，0：预付费，1：后付费，负数则为 -1:合计
                acs.setOperatorstype(cst.getOperatorstype());  // 通道运营商类型:0->全网，1->移动，2->联通，3->电信，4->国际；（为负数时表示数据类型，-1表示拦截
                // -2表示合计）
                acs.setChannelid(cst.getChannelid()); // 负数则为 -1:合计
                acs.setRemark(" - ");
                acs.setProductType(-1);
                acs.setSubId(cst.getSubId());
                acs.setStattype(AccessChannelStatisticsType.monthly.getValue());
                acs.setDate(cst.getDate() / 100);
                acs.setCreatetime(now);

                acs.setChargetotal(0);
                acs.setOverrateChargeTotal(0);
                acs.setCostfee(BigDecimal.ZERO);
                acs.setSalefee(BigDecimal.ZERO);
                acs.setProductfee(BigDecimal.ZERO);
                acs.setUsersmstotal(0);
                acs.setSendtotal(0);
                acs.setNotsend(0);
                acs.setSubmitsuccess(0);
                acs.setReportsuccess(0);
                acs.setSubmitfail(0);
                acs.setSubretfail(0);
                acs.setReportfail(0);
                acs.setAuditfail(0);
                acs.setRecvintercept(0);
                acs.setSendintercept(0);
                acs.setOverrateintercept(0);
                acs.setBelongSale(cst.getBelongSale());
                acs.setSmstype(cst.getSmstype());
            }

//			if(cst.getOperatorstype().equals(-1)){//拦截数据  没用
//				acs.setOperatorstype(-1); // -1表示拦截
//			}

            acs.setChargetotal(acs.getChargetotal() + cst.getChargetotal());
            acs.setOverrateChargeTotal(acs.getOverrateChargeTotal() + cst.getOverrateChargeTotal());
            acs.setCostfee(acs.getCostfee().add(cst.getCostfee()));
            acs.setSalefee(acs.getSalefee().add(cst.getSalefee()));
            acs.setProductfee(acs.getProductfee().add(cst.getProductfee()));
            acs.setUsersmstotal(acs.getUsersmstotal() + cst.getUsersmstotal());
            acs.setSendtotal(acs.getSendtotal() + cst.getSendtotal());
            acs.setNotsend(acs.getNotsend() + cst.getNotsend());
            acs.setSubmitsuccess(acs.getSubmitsuccess() + cst.getSubmitsuccess());
            acs.setReportsuccess(acs.getReportsuccess() + cst.getReportsuccess());
            acs.setSubmitfail(acs.getSubmitfail() + cst.getSubmitfail());
            acs.setSubretfail(acs.getSubretfail() + cst.getSubretfail());
            acs.setReportfail(acs.getReportfail() + cst.getReportfail());
            acs.setAuditfail(acs.getAuditfail() + cst.getAuditfail());
            acs.setRecvintercept(acs.getRecvintercept() + cst.getRecvintercept());
            acs.setSendintercept(acs.getSendintercept() + cst.getSendintercept());
            acs.setOverrateintercept(acs.getOverrateintercept() + cst.getOverrateintercept());
            temp.put(key, acs);

        }


        /**
         * 完成每月详细数据的统计
         */
        for (Map.Entry<String, AccessChannelStatistics> tempEntry : temp.entrySet()) {
            monthlyStatistics.add(tempEntry.getValue());
        }
        LOGGER.debug(" 完成每月详细数据的统计  数据{} ", JsonUtils.toJson(monthlyStatistics));
        accessChannelStatisticsService.insertBatch(monthlyStatistics);


        /**
         * 每月合计数据统计
         */
        temp = new HashMap<>();
        for (AccessChannelStatistics cst : monthlyStatistics) {
            String key = cst.getClientid();

            AccessChannelStatistics acs = temp.get(key);
            if (acs == null) {
                acs = new AccessChannelStatistics();
                acs.setAgentId(cst.getAgentId().intValue());
                acs.setClientid(cst.getClientid());
                acs.setName(cst.getName());
                acs.setSid(cst.getSid());
                acs.setPaytype(-1); // 付费类型，0：预付费，1：后付费，负数则为 -1:合计
                acs.setOperatorstype(-2); // -1表示拦截 -2表示合计
                // 通道运营商类型:0->全网，1->移动，2->联通，3->电信，4->国际；（为负数时表示数据类型，-1表示拦截
                // -2表示合计）
                acs.setChannelid(-1); // 负数则为 -1:合计
                acs.setRemark(" - ");
                acs.setProductType(-1);
                acs.setSubId(" - ");
                acs.setStattype(AccessChannelStatisticsType.monthlySum.getValue());
                acs.setDate(cst.getDate());
                acs.setCreatetime(now);

                acs.setChargetotal(0);
                acs.setOverrateChargeTotal(0);
                acs.setCostfee(BigDecimal.ZERO);
                acs.setSalefee(BigDecimal.ZERO);
                acs.setProductfee(BigDecimal.ZERO);
                acs.setUsersmstotal(0);
                acs.setSendtotal(0);
                acs.setNotsend(0);
                acs.setSubmitsuccess(0);
                acs.setReportsuccess(0);
                acs.setSubmitfail(0);
                acs.setSubretfail(0);
                acs.setReportfail(0);
                acs.setAuditfail(0);
                acs.setRecvintercept(0);
                acs.setSendintercept(0);
                acs.setOverrateintercept(0);
                acs.setBelongSale(null);
                acs.setSmstype(null);
            }


            acs.setChargetotal(acs.getChargetotal() + cst.getChargetotal());
            acs.setOverrateChargeTotal(acs.getOverrateChargeTotal() + cst.getOverrateChargeTotal());
            acs.setCostfee(acs.getCostfee().add(cst.getCostfee()));
            acs.setSalefee(acs.getSalefee().add(cst.getSalefee()));
            acs.setProductfee(acs.getProductfee().add(cst.getProductfee()));
            acs.setUsersmstotal(acs.getUsersmstotal() + cst.getUsersmstotal());
            acs.setSendtotal(acs.getSendtotal() + cst.getSendtotal());
            acs.setNotsend(acs.getNotsend() + cst.getNotsend());
            acs.setSubmitsuccess(acs.getSubmitsuccess() + cst.getSubmitsuccess());
            acs.setReportsuccess(acs.getReportsuccess() + cst.getReportsuccess());
            acs.setSubmitfail(acs.getSubmitfail() + cst.getSubmitfail());
            acs.setSubretfail(acs.getSubretfail() + cst.getSubretfail());
            acs.setReportfail(acs.getReportfail() + cst.getReportfail());
            acs.setAuditfail(acs.getAuditfail() + cst.getAuditfail());
            acs.setRecvintercept(acs.getRecvintercept() + cst.getRecvintercept());
            acs.setSendintercept(acs.getSendintercept() + cst.getSendintercept());
            acs.setOverrateintercept(acs.getOverrateintercept() + cst.getOverrateintercept());
            temp.put(key, acs);

        }


        /**
         * 完成每月合计数据的统计
         */
        for (Map.Entry<String, AccessChannelStatistics> tempEntry : temp.entrySet()) {
            monthlySumStatistics.add(tempEntry.getValue());
        }
        LOGGER.debug(" 完成每月合计数据的统计  数据{} ", JsonUtils.toJson(monthlySumStatistics));
        accessChannelStatisticsService.insertBatch(monthlySumStatistics);
    }
}
