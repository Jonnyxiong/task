package com.ucpaas.sms.task.statistic;

import com.jsmsframework.user.entity.JsmsDepartment;
import com.jsmsframework.user.service.JsmsDepartmentService;
import com.ucpaas.sms.task.entity.access.AccessChannelStatistics;
import com.ucpaas.sms.task.entity.access.CustomerStatTemp;
import com.ucpaas.sms.task.entity.access.SmsAccessSendStat;
import com.ucpaas.sms.task.entity.access.SmsBackPaymenstat;
import com.ucpaas.sms.task.entity.message.*;
import com.ucpaas.sms.task.enum4sms.AccessChannelStatisticsType;
import com.ucpaas.sms.task.enum4sms.PayType;
import com.ucpaas.sms.task.service.*;
import com.ucpaas.sms.task.util.JacksonUtil;
import com.ucpaas.sms.task.util.JsonUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 需求版本1.5
 *
 * @author huangwenjie
 */
//@Service("deman1point5AccessStatistic")
@Service
public class Deman1point5AccessStatistic implements AccessStatisticStrategy {


    private static final Logger LOGGER = LoggerFactory.getLogger("AccessChannelStatisticsService");

    @Autowired
    private AccessChannelStatisticsService accessChannelStatisticsService;
    @Autowired
    private CustomerStatTempService customerStatTempService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private UserPriceLogService userPriceLogService;
    @Autowired
    private UserService userService;
    @Autowired
    private AccountGroupService accountGroupService;
    @Autowired
    private AccountgrRefAccountService accountgrRefAccountService;
    @Autowired
    private SmsAccessSendStatService smsAccessSendStatService;
    @Autowired
    private AgentBalanceBillService agentBalanceBillService;
    @Autowired
    private SmsBackPaymenstatService smsBackPaymenstatService;
    @Autowired
    private JsmsDepartmentService jsmsDepartmentService;

    @SuppressWarnings("Duplicates")
    @Override
    public void staticAlgorithm(DateTime statDay) {

        String statTime = statDay.toString("yyyyMMdd");
        Date now = new Date();
        List<AccessChannelStatistics> dailyStatistics = new ArrayList<>(); // 每日详细数据
        //1. 计算基础数据 客户发送统计t_sms_access_channel_statistics
        LOGGER.info("1. 计算基础数据 客户发送统计t_sms_access_channel_statistics");
        dailyStatistics = accessChannelStatistics(statDay, now);
        //2.计算客户发送量t_sms_access_send_stat
        LOGGER.info("2.计算客户发送量t_sms_access_send_stat");
        smsAccessSendStat(statDay, statTime, dailyStatistics, now);
        //3.计算回款金额统计t_sms_back_payment_stat
        Map params = new HashMap();
        DateTime beginCreatTime = new DateTime(statDay.getYear(), statDay.getMonthOfYear(), 1, 0, 0, 0);
        int endMonth = statDay.getMonthOfYear() + 1;
        int endYear = statDay.getYear();
        if (endMonth > 12) {
            endMonth = endMonth % 12;
            endYear += 1;
        }

        DateTime endCreatTime = new DateTime(endYear, endMonth, 1, 0, 0, 0);

        params.put("beginCreatTime", beginCreatTime.toDate());
        params.put("endCreatTime", endCreatTime.toDate());
        params.put("paymentType", 0); //业务类型，0：充值，1：扣减，2：佣金转余额，3：购买产品包，4：退款，5：赠送，6：后付费客户消耗
        List<AgentBalanceBill> agentBalanceBills = agentBalanceBillService.queryAll(params);
        BigDecimal prepayRechargeMonth = BigDecimal.ZERO;


        Map<String, BigDecimal> prepayRechargeMap = new HashMap<>();
        Map<String, BigDecimal> directConsumeMap = new HashMap<>();
        String nowTime = new DateTime().toString("yyyyMMdd");
        for (AgentBalanceBill abb : agentBalanceBills) {
            String createTimeStr = new DateTime(abb.getCreateTime()).toString("yyyyMMdd");
            if (!createTimeStr.equals(nowTime)) { //不计算任务的时间的当天
                prepayRechargeMonth = prepayRechargeMonth.add(abb.getAmount());

                BigDecimal prepayRechargeDaily = prepayRechargeMap.get(createTimeStr);
                if (prepayRechargeDaily == null)
                    prepayRechargeDaily = BigDecimal.ZERO;

                prepayRechargeDaily = prepayRechargeDaily.add(abb.getAmount());
                prepayRechargeMap.put(createTimeStr, prepayRechargeDaily);
            }
        }
        BigDecimal directConsumeMonth = BigDecimal.ZERO;
        for (AccessChannelStatistics acs : dailyStatistics) {

            if (acs.getAgentId() == null || acs.getAgentId().intValue() == 0 || acs.getAgentId().intValue() == 1 || acs.getAgentId().intValue() == 2) {  //直客
                String dateStr = acs.getDate().toString();
                if (!dateStr.equals(nowTime)) { //不计算任务的时间的当天
                    directConsumeMonth = directConsumeMonth.add(acs.getSalefee().divide(new BigDecimal("1000")));

                    BigDecimal directConsumeDaily = directConsumeMap.get(dateStr);
                    if (directConsumeDaily == null)
                        directConsumeDaily = BigDecimal.ZERO;

                    directConsumeDaily = directConsumeDaily.add(acs.getSalefee().divide(new BigDecimal("1000")));
                    directConsumeMap.put(dateStr, directConsumeDaily);
                }
            }
        }

        smsBackPaymenstatService.deleteByDateLike(statDay.toString("yyyyMM"));
        DateTime temp = beginCreatTime;
        while (temp.isBefore(endCreatTime)) {
            String dateStr = temp.toString("yyyyMMdd");
            if (dateStr.equals(nowTime)) { //不计算任务的时间的当天e
                break;
            }
            BigDecimal prepayRechargeDaily = prepayRechargeMap.get(dateStr);
            BigDecimal directConsumeDaily = directConsumeMap.get(dateStr);
            if (prepayRechargeDaily == null)
                prepayRechargeDaily = BigDecimal.ZERO;
            if (directConsumeDaily == null)
                directConsumeDaily = BigDecimal.ZERO;
            SmsBackPaymenstat smsBackPaymenstatDaily = new SmsBackPaymenstat();
            smsBackPaymenstatDaily.setPrepayRecharge(prepayRechargeDaily);
            smsBackPaymenstatDaily.setDirectConsume(directConsumeDaily);
            smsBackPaymenstatDaily.setStattype(AccessChannelStatisticsType.daily.getValue());
            smsBackPaymenstatDaily.setDate(Integer.valueOf(dateStr));
            smsBackPaymenstatDaily.setCreatetime(now);
            smsBackPaymenstatService.insert(smsBackPaymenstatDaily);
            temp = temp.plusDays(1);
        }


        SmsBackPaymenstat smsBackPaymenstaMonth = new SmsBackPaymenstat();
        smsBackPaymenstaMonth.setPrepayRecharge(prepayRechargeMonth);
        smsBackPaymenstaMonth.setDirectConsume(directConsumeMonth);
        smsBackPaymenstaMonth.setStattype(AccessChannelStatisticsType.monthly.getValue());
        smsBackPaymenstaMonth.setDate(Integer.valueOf(statDay.toString("yyyyMM")));
        smsBackPaymenstaMonth.setCreatetime(now);
        smsBackPaymenstatService.insert(smsBackPaymenstaMonth);

    }

    private void smsAccessSendStat(DateTime statDay, String statTime, List<AccessChannelStatistics> dailyStatistics, Date now) {
        LOGGER.debug("smsAccessSendStat, dailyStatistics={}", JacksonUtil.toJSON(dailyStatistics));
        //2.1 删除当天的客户发送量统计
//        int delRow = smsAccessSendStatService.deleteByDate(Integer.valueOf(statTime)); //删除当天的，考虑到兼容历史没有数据，所以先弄全部的
        int delRow = smsAccessSendStatService.deleteByDateLike(statDay.toString("yyyyMM"));
        LOGGER.info("删除date=" + statTime + "的客户发送量,共" + delRow + "条记录");
        Map<String, SmsAccessSendStat> dailyAccessSendStatMap = new HashMap<>(); //只计算当天的数据
        for (AccessChannelStatistics acs : dailyStatistics) {
//            if (!acs.getDate().toString().equals(statTime)) { //只计算当天的
//                continue;

            Integer departmentId = null;
            User user = userService.getById(acs.getBelongSale());
            if (user != null) {
                JsmsDepartment fistLevel = jsmsDepartmentService.getFistLevelDeparment(user.getDepartmentId());
                if (fistLevel != null)
                    departmentId = fistLevel.getDepartmentId();
            }

            AccountgrRefAccount ref = accountgrRefAccountService.getByClientid(acs.getClientid());
            Integer accountGid = null;
            if (ref != null)
                accountGid = ref.getAccountGid();

            String key = acs.getClientid() + "-" + acs.getAgentId() + "-" + acs.getBelongSale() + "-" + acs.getSmstype() + "-" + acs.getPaytype() + "-" + acs.getDate()+"-"+accountGid +"-"+departmentId;
            SmsAccessSendStat smsAccessSendStat = dailyAccessSendStatMap.get(key);
            if (smsAccessSendStat == null) {
                smsAccessSendStat = new SmsAccessSendStat();
                smsAccessSendStat.setAgentId(acs.getAgentId());
                smsAccessSendStat.setDepartmentId(departmentId);
                smsAccessSendStat.setAccountGid(accountGid);
                smsAccessSendStat.setClientid(acs.getClientid());
                smsAccessSendStat.setBelongSale(acs.getBelongSale());
                smsAccessSendStat.setSmstype(acs.getSmstype());
                smsAccessSendStat.setPaytype(acs.getPaytype());

                smsAccessSendStat.setNotsend(0);
                smsAccessSendStat.setSubmitsuccess(0);
                smsAccessSendStat.setReportsuccess(0);
                smsAccessSendStat.setSubmitfail(0);
                smsAccessSendStat.setSubretfail(0);
                smsAccessSendStat.setReportfail(0);
                smsAccessSendStat.setAuditfail(0);
                smsAccessSendStat.setRecvintercept(0);
                smsAccessSendStat.setSendintercept(0);
                smsAccessSendStat.setOverrateintercept(0);
                smsAccessSendStat.setCostfee(BigDecimal.ZERO);
                smsAccessSendStat.setSalefee(BigDecimal.ZERO);
                smsAccessSendStat.setStattype(AccessChannelStatisticsType.daily.getValue());
                smsAccessSendStat.setDate(acs.getDate());
                smsAccessSendStat.setCreatetime(now);
            }

            smsAccessSendStat.setNotsend(smsAccessSendStat.getNotsend() + acs.getNotsend());
            smsAccessSendStat.setSubmitsuccess(smsAccessSendStat.getSubmitsuccess() + acs.getSubmitsuccess());
            smsAccessSendStat.setReportsuccess(smsAccessSendStat.getReportsuccess() + acs.getReportsuccess());
            smsAccessSendStat.setSubmitfail(smsAccessSendStat.getSubmitfail() + acs.getSubmitfail());
            smsAccessSendStat.setSubretfail(smsAccessSendStat.getSubretfail() + acs.getSubretfail());
            smsAccessSendStat.setReportfail(smsAccessSendStat.getReportfail() + acs.getReportfail());
            smsAccessSendStat.setAuditfail(smsAccessSendStat.getAuditfail() + acs.getAuditfail());
            smsAccessSendStat.setRecvintercept(smsAccessSendStat.getRecvintercept() + acs.getRecvintercept());
            smsAccessSendStat.setSendintercept(smsAccessSendStat.getSendintercept() + acs.getSendintercept());
            smsAccessSendStat.setOverrateintercept(smsAccessSendStat.getOverrateintercept() + acs.getOverrateintercept());
            smsAccessSendStat.setCostfee(BigDecimal.ZERO);
            smsAccessSendStat.setSalefee(smsAccessSendStat.getSalefee().add(acs.getSalefee()));
            dailyAccessSendStatMap.put(key, smsAccessSendStat);
        }
        List<SmsAccessSendStat> smsAccessSendStats = new ArrayList<>();
        Iterator<Map.Entry<String, SmsAccessSendStat>> sasIterator = dailyAccessSendStatMap.entrySet().iterator();
        while (sasIterator.hasNext()) {
            Map.Entry<String, SmsAccessSendStat> entry = sasIterator.next();
            smsAccessSendStats.add(entry.getValue());
        }
        if (smsAccessSendStats != null && !smsAccessSendStats.isEmpty()) {
            LOGGER.debug("保存每日客户发送量smsAccessSendStats={}", JacksonUtil.toJSON(smsAccessSendStats));
            smsAccessSendStatService.insertBatch(smsAccessSendStats);
        } else {
            LOGGER.info("smsAccessSendStats每日客户发送量为空");
        }

        String monthDate = statDay.toString("yyyyMM");
        delRow = smsAccessSendStatService.deleteByDate(Integer.valueOf(monthDate));
        LOGGER.info("删除date=" + monthDate + "的客户发送量,共" + delRow + "条记录");
        Map params = new HashMap();
        params.put("stattype", AccessChannelStatisticsType.daily.getValue());
        params.put("likeDate", monthDate);
        List<SmsAccessSendStat> dailySmsAccessSendStats = smsAccessSendStatService.queryAll(params);

        Map<String, SmsAccessSendStat> monthlyAccessSendStatMap = new HashMap<>(); //只计算当月s的数据
        for (SmsAccessSendStat sass : dailySmsAccessSendStats) {

            String key = sass.getClientid() + "-" + sass.getAgentId() + "-" + sass.getBelongSale() + "-" + sass.getSmstype() + "-" + sass.getPaytype() +"-" + sass.getAccountGid() +"-" + sass.getDepartmentId();
            SmsAccessSendStat smsAccessSendStat = monthlyAccessSendStatMap.get(key);
            if (smsAccessSendStat == null) {
                smsAccessSendStat = new SmsAccessSendStat();
                smsAccessSendStat.setDepartmentId(sass.getDepartmentId());
                smsAccessSendStat.setAgentId(sass.getAgentId());
                smsAccessSendStat.setAccountGid(sass.getAccountGid());
                smsAccessSendStat.setClientid(sass.getClientid());
                smsAccessSendStat.setBelongSale(sass.getBelongSale());
                smsAccessSendStat.setSmstype(sass.getSmstype());
                smsAccessSendStat.setPaytype(sass.getPaytype());

                smsAccessSendStat.setNotsend(0);
                smsAccessSendStat.setSubmitsuccess(0);
                smsAccessSendStat.setReportsuccess(0);
                smsAccessSendStat.setSubmitfail(0);
                smsAccessSendStat.setSubretfail(0);
                smsAccessSendStat.setReportfail(0);
                smsAccessSendStat.setAuditfail(0);
                smsAccessSendStat.setRecvintercept(0);
                smsAccessSendStat.setSendintercept(0);
                smsAccessSendStat.setOverrateintercept(0);
                smsAccessSendStat.setCostfee(BigDecimal.ZERO);
                smsAccessSendStat.setSalefee(BigDecimal.ZERO);
                smsAccessSendStat.setStattype(AccessChannelStatisticsType.monthly.getValue());
                smsAccessSendStat.setDate(Integer.valueOf(monthDate));
                smsAccessSendStat.setCreatetime(now);
            }

            smsAccessSendStat.setNotsend(smsAccessSendStat.getNotsend() + sass.getNotsend());
            smsAccessSendStat.setSubmitsuccess(smsAccessSendStat.getSubmitsuccess() + sass.getSubmitsuccess());
            smsAccessSendStat.setReportsuccess(smsAccessSendStat.getReportsuccess() + sass.getReportsuccess());
            smsAccessSendStat.setSubmitfail(smsAccessSendStat.getSubmitfail() + sass.getSubmitfail());
            smsAccessSendStat.setSubretfail(smsAccessSendStat.getSubretfail() + sass.getSubretfail());
            smsAccessSendStat.setReportfail(smsAccessSendStat.getReportfail() + sass.getReportfail());
            smsAccessSendStat.setAuditfail(smsAccessSendStat.getAuditfail() + sass.getAuditfail());
            smsAccessSendStat.setRecvintercept(smsAccessSendStat.getRecvintercept() + sass.getRecvintercept());
            smsAccessSendStat.setSendintercept(smsAccessSendStat.getSendintercept() + sass.getSendintercept());
            smsAccessSendStat.setOverrateintercept(smsAccessSendStat.getOverrateintercept() + sass.getOverrateintercept());
            smsAccessSendStat.setCostfee(smsAccessSendStat.getCostfee().add(sass.getCostfee()));
            smsAccessSendStat.setSalefee(smsAccessSendStat.getSalefee().add(sass.getSalefee()));
            monthlyAccessSendStatMap.put(key, smsAccessSendStat);

        }


        smsAccessSendStats = new ArrayList<>();
        sasIterator = monthlyAccessSendStatMap.entrySet().iterator();
        while (sasIterator.hasNext()) {
            Map.Entry<String, SmsAccessSendStat> entry = sasIterator.next();
            smsAccessSendStats.add(entry.getValue());
        }
        if (smsAccessSendStats != null && !smsAccessSendStats.isEmpty()) {
            LOGGER.debug("保存每月客户发送量smsAccessSendStats={}", JacksonUtil.toJSON(smsAccessSendStats));
            smsAccessSendStatService.insertBatch(smsAccessSendStats);
        } else {
            LOGGER.info("smsAccessSendStats每月客户发送量为空");
        }
    }

    private List<AccessChannelStatistics> accessChannelStatistics(DateTime statDay, Date now) {

        String statTime = statDay.toString("yyyyMMdd");


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

        List<CustomerStatTemp> customerStatTemps = customerStatTempService.generateDataIncludeAgentId(statTime);

        if (customerStatTemps == null || customerStatTemps.size() == 0) {
            LOGGER.debug("根据时间段，遍历所有的access表，生成CustomerStatTemp临时数据为空，统计结束 ");
            return dailyStatistics;
        }

        LOGGER.debug("清除统计的数据时间date={}的数据 ", statTime);
        accessChannelStatisticsService.deleteByDate(statTime);
        LOGGER.debug("根据时间段，遍历所有的access表，生成CustomerStatTemp临时数据{} ", JsonUtils.toJson(customerStatTemps));


        Map<String, AccessChannelStatistics> temp = new HashMap<>();
        for (CustomerStatTemp cst : customerStatTemps) {
            // Integer chargeTotal = 0; //计费条数，状态是1 + 3 + 4 + 6
            // Integer overrateChargeTotal = 0;//超频计费条数
            // 分组条件clientid + agent_id + product_type + channelid + paytype + belong_sale + smstype + sub_id
            String key = cst.getClientid() + "-" + "-" + cst.getAgentId() + cst.getProductType() + "-" + cst.getChannelid() + "-"
                    + cst.getPaytype() + "-" + cst.getBelongSale() + "-" + cst.getSmstype() + "-" + cst.getSubId() + cst.getOperatorstype();
            Account account = accountService.getByClientid(cst.getClientid());
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

            Tuple<Integer, BigDecimal> chanrgeAndSalefee = decideChargeAndSalefee(cst, statDay);
            Tuple<Integer, BigDecimal> chanrgeAndPrductfee = decideChargeAndProductfee(cst, statDay);

            //消费金额(针对客户) = 产品销售价 *计费条数，由于在通道id=0的情况下，超频计费条数为0，所以不用算超频计费
            int chargetTotal = chanrgeAndSalefee.getFirst();
            BigDecimal salefee = chanrgeAndSalefee.getSecond();
            int productChargetTotal = chanrgeAndPrductfee.getFirst();
            BigDecimal productfee = chanrgeAndPrductfee.getSecond();
            int costChargeTotal = cst.getSubmitsuccess() + cst.getReportsuccess();  //成本的计费条数=1、3


            acs.setChargetotal(acs.getChargetotal() + chargetTotal);

            //超频计费条数，在正常数据中，通道id=0，所以这里会为0
            acs.setOverrateChargeTotal(acs.getOverrateChargeTotal() + cst.getOverrateChargeTotal());

            //通道成本 *计费条数，由于在通道id=0的情况下，超频计费条数为0，所以不用算超频计费
            BigDecimal costfee = cst.getCostfee().multiply(new BigDecimal(costChargeTotal));
            acs.setCostfee(acs.getCostfee().add(costfee));

            //消费金额(针对客户) = 产品销售价 *计费条数  计费条数和产品销售价上面有定义
            acs.setSalefee(acs.getSalefee().add(salefee.multiply(new BigDecimal(chargetTotal))));

            //消耗成本(针对代理商) = 产品成本价 *计费条数，由于在通道id=0的情况下，超频计费条数为0，所以不用算超频计费
//            BigDecimal productfee = cst.getProductfee().multiply(new BigDecimal(chargetTotal));
            acs.setProductfee(acs.getProductfee().add(productfee.multiply(new BigDecimal(productChargetTotal))));

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


            temp.put(key, acs);
        }


        /**
         * 完成每日详细数据
         */
        for (Map.Entry<String, AccessChannelStatistics> tempEntry : temp.entrySet()) {
            dailyStatistics.add(tempEntry.getValue());
        }

        LOGGER.debug("完成每日详细数据的三种统计（通道不为0、state=4、拦截），三种统计数据不应该互相有重叠  数据{} ", JsonUtils.toJson(dailyStatistics));

        if (dailyStatistics.size() > 0) {
            accessChannelStatisticsService.insertBatch(dailyStatistics);
        } else {
            LOGGER.info("客户运营运维统计报表任务【结束】：计算出来的每日统计数据为空");
            return dailyStatistics;
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
            String key = cst.getClientid() + "-" + "-" + cst.getAgentId() + cst.getProductType() + "-" + cst.getChannelid() + "-"
                    + cst.getPaytype() + "-" + cst.getBelongSale() + "-" + cst.getSmstype() + "-" + cst.getSubId() + cst.getOperatorstype();
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
                acs.setRemark(cst.getRemark());
                acs.setProductType(cst.getProductType());
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
        return dailyStatistics;
    }

    private Tuple<Integer, BigDecimal> decideChargeAndProductfee(CustomerStatTemp cst, DateTime statDay) {

        int productChargeTotal = 0;

        BigDecimal productfee = BigDecimal.ZERO;
        if (cst.getPaytype().intValue() == PayType.prepayment.getValue().intValue()) { //预付费客户
            //计费条数  productChargeTotal = 1 + 3 + 4 + 6，通道不为0的情况下，不应该存在4，因为4需要收费，所以怕漏了，这里也计算上去
            productChargeTotal = cst.getSubmitsuccess() + cst.getReportsuccess()
                    + cst.getSubmitfail() + cst.getReportfail() + cst.getOverrateChargeTotal();   //计费条数=1、3、4、6、10（超频计费）
            productfee = cst.getProductfee();
        } else { //后付费客户
            if (cst.getAgentId() == null || cst.getAgentId().intValue() == 0 || cst.getAgentId().intValue() == 1 || cst.getAgentId().intValue() == 2) {  //直客
                productChargeTotal = cst.getSubmitsuccess() + cst.getReportsuccess();  //计费条数=1、3
            } else { //代理商子客户
                productChargeTotal = cst.getSubmitsuccess() + cst.getReportsuccess()
                        + cst.getSubmitfail() + cst.getReportfail() + cst.getOverrateChargeTotal();  //计费条数=1、3、4、6、10（超频计费）
            }
            //国际与非国际的价格不一样
            if (cst.getOperatorstype() != null && cst.getOperatorstype().intValue() == 4) {  //国际
                productfee = cst.getProductfee();
            } else { //非国际的取0
                productfee = BigDecimal.ZERO;
            }
        }
        return new Tuple<>(productChargeTotal, productfee);
    }

    private Tuple<Integer, BigDecimal> decideChargeAndSalefee(CustomerStatTemp cst, DateTime statDay) {

        int chargeTotal = 0;

        BigDecimal salefee = BigDecimal.ZERO;
        if (cst.getPaytype().intValue() == PayType.prepayment.getValue().intValue()) { //预付费客户
            //计费条数  chargeTotal = 1 + 3 + 4 + 6 + 10，通道不为0的情况下，不应该存在4，因为4需要收费，所以怕漏了，这里也计算上去
            chargeTotal = cst.getSubmitsuccess() + cst.getReportsuccess()
                    + cst.getSubmitfail() + cst.getReportfail() + cst.getOverrateChargeTotal();  //计费条数=1、3、4、6、10（超频计费）
            salefee = cst.getSalefee();
        } else { //后付费客户
            if (cst.getAgentId() == null || cst.getAgentId().intValue() == 0 || cst.getAgentId().intValue() == 1 || cst.getAgentId().intValue() == 2) {  //直客
                chargeTotal = cst.getSubmitsuccess() + cst.getReportsuccess();  //计费条数=1、3
            } else { //代理商子客户
                chargeTotal = cst.getSubmitsuccess() + cst.getReportsuccess()
                        + cst.getSubmitfail() + cst.getReportfail() + cst.getOverrateChargeTotal();  //计费条数=1、3、4、6、10（超频计费）
            }
            //国际与非国际的价格不一样
            if (cst.getOperatorstype() != null && cst.getOperatorstype().intValue() == 4) {  //国际
                salefee = cst.getSalefee();
            } else { //非国际的取生效日期的价格
                UserPriceLog userPriceLog = userPriceLogService.getByPrice(cst.getClientid(), cst.getSmstype(), statDay);
                if (userPriceLog == null)
                    salefee = BigDecimal.ZERO;
                else {
                    salefee = userPriceLog.getUserPrice().multiply(new BigDecimal("1000"));
                }
            }
        }
        return new Tuple<>(chargeTotal, salefee);
    }


    class Tuple<T, K> {
        private T t;
        private K k;

        public Tuple(T t, K k) {
            this.t = t;
            this.k = k;
        }

        public T getFirst() {
            return t;
        }

        public K getSecond() {
            return k;
        }
    }
}
