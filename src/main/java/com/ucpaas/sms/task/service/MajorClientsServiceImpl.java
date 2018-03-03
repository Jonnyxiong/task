package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.aop.DataSource;
import com.ucpaas.sms.task.entity.access.AccessChannelStatistics;
import com.ucpaas.sms.task.entity.message.Account;
import com.ucpaas.sms.task.entity.message.Channel;
import com.ucpaas.sms.task.entity.message.ClientOrder;
import com.ucpaas.sms.task.entity.message.OemClientPool;
import com.ucpaas.sms.task.entity.record.RecordChannelStatistics;
import com.ucpaas.sms.task.enum4sms.DataSourceType;
import com.ucpaas.sms.task.enum4sms.OperatorsType;
import com.ucpaas.sms.task.enum4sms.PayType;
import com.ucpaas.sms.task.enum4sms.PropertiesParams;
import com.ucpaas.sms.task.mapper.access.AccessChannelStatisticsMapper;
import com.ucpaas.sms.task.mapper.message.AccountMapper;
import com.ucpaas.sms.task.mapper.message.ChannelMapper;
import com.ucpaas.sms.task.mapper.message.ClientOrderMapper;
import com.ucpaas.sms.task.mapper.message.OemClientPoolMapper;
import com.ucpaas.sms.task.mapper.record.RecordChannelStatisticsMapper;
import com.ucpaas.sms.task.model.TaskInfo;
import com.ucpaas.sms.task.model.ali.*;
import com.ucpaas.sms.task.util.PropertiesUtil;
import com.ucpaas.sms.task.util.StringUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.math.BigDecimal.ROUND_HALF_UP;

@Service
public class MajorClientsServiceImpl implements MajorClientsService {

    private static final Logger logger = LoggerFactory.getLogger("majorClientsDailyDetailSendMail");

    @Autowired
    private AccessChannelStatisticsMapper accessChannelStatisticsMapper;
    @Autowired
    private RecordChannelStatisticsMapper recordChannelStatisticsMapper;
    @Autowired
    private ChannelMapper channelMapper;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private OemClientPoolMapper oemClientPoolMapper;
    @Autowired
    private ClientOrderMapper clientOrderMapper;
    @Autowired
    private JavaMailSender javaMailSender;


    @Override
    /**
     * 预付费用户 单价, 需要从流水表中读取
     */
    @DataSource(DataSourceType.READ)
    public boolean prepaymentClientDailyDetail(TaskInfo taskInfo) {
        if (readEmail(PayType.prepayment).length == 1 && StringUtil.isEmpty(readEmail(PayType.prepayment)[0])) {
            return true;
        }
        long start = System.currentTimeMillis();
        List<Map> mapList = new ArrayList<>();
//        Map params = new HashMap<>();
        try {
            String propertis = PropertiesUtil.get(PropertiesParams.MAJORCLIENTSPREPAYMENTINFO.getType());
            mapList = readClientInfo(propertis, PayType.prepayment);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("解析配置文件中的账号失败");
            return true;
        }
        DateTime executeTime = taskInfo.getExecuteNextDate();
        try {
            for (Map params : mapList) {
            	params.put("paytype", PayType.prepayment.getValue());
                Map<String, Object> data = new HashMap<>();
                List<Overall> overallList = overall(params, executeTime, PayType.prepayment);
                data.put("overall", overallList); // 整体情况
                data.put("overallTotal", overallTotal(overallList));// 整体合计

                List<ChannelDetail> channelDetailList = channelDetails(params, executeTime, PayType.prepayment);
                data.put("channelDetails", channelDetailList);  // 昨日通道使用明细
                data.put("channelDetailTotal", channelDetailTotal(channelDetailList));  // 昨日通道使用合计

                data.put("channelOverall", format(commonlyChannel(params, executeTime, PayType.prepayment)));
                data.put("month", executeTime.minusDays(1).getMonthOfYear());
                data.put("day", executeTime.minusDays(1).getDayOfMonth());
                data.put("clientName", params.get("clientName"));
                String emailContent = loaderFreemaker(data, PayType.prepayment);

                if (StringUtil.isEmpty(emailContent)) {
                    continue;
                }
                boolean isSend = sendMail(emailContent, executeTime, (String) params.get("clientName"), PayType.prepayment);
                logger.debug("邮件发送 --> {}", isSend);
                long end = System.currentTimeMillis();
                logger.info("【邮件 - 重点客户报表（预付费）任务】结束，统计时间= {}，统计耗时={}ms", executeTime.toString("yyyy-MM-dd HH:mm:ss"), (end - start));
            }
        } catch (IOException e) {
            logger.error("未找到指定的模板文件 ----------> {}, {}", e.getMessage(), e);
        } catch (TemplateException e) {
            logger.error("FreeMaker 模板解析失败 ----------> {}, {}", e.getMessage(), e);
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("【邮件 - 重点客户报表（预付费）任务】失败 ----------> {}, {}", e.getMessage(), e);
            e.printStackTrace();
        }

        return true;
    }

    @Override
    /**
     * 后付费用户没有 单价, 需要在配置文件中读取
     */
    @DataSource(DataSourceType.READ)
    public boolean postpaidClientDailyDetail(TaskInfo taskInfo) {
        if (readEmail(PayType.postpay).length == 1 && StringUtil.isEmpty(readEmail(PayType.prepayment)[0])) {
            return true;
        }
        long start = System.currentTimeMillis();
        List<Map> mapList = new ArrayList<>();
        try {
            String propertis = PropertiesUtil.get(PropertiesParams.MAJORCLIENTSPOSTPAIDINFO.getType());
            mapList = readClientInfo(propertis, PayType.postpay);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("解析配置文件中的账号失败");
            return true;
        }

        DateTime executeTime = taskInfo.getExecuteNextDate();
        try {
            for (Map params : mapList) {
            	params.put("paytype", PayType.postpay.getValue());

                Map<String, Object> data = new HashMap<>();
                List<Overall> overallList = overall(params, executeTime, PayType.postpay);
                data.put("overall", overallList); // 整体情况
                data.put("overallTotal", overallTotal(overallList));// 整体合计

                List<ChannelDetail> channelDetailList = channelDetails(params, executeTime, PayType.postpay);
                data.put("channelDetails", channelDetailList); // 昨日通道使用明细
                data.put("channelDetailTotal", channelDetailTotal(channelDetailList));//昨日通道使用合计

                data.put("channelOverall", format(commonlyChannel(params, executeTime, PayType.postpay)));
                data.put("month", executeTime.minusDays(1).getMonthOfYear());
                data.put("day", executeTime.minusDays(1).getDayOfMonth());
                data.put("clientName", params.get("clientName"));
                String emailContent = loaderFreemaker(data, PayType.postpay);
                if (StringUtil.isEmpty(emailContent)) {
                    continue;
                }
                boolean isSend = sendMail(emailContent, executeTime, (String) params.get("clientName"), PayType.postpay);
                logger.debug("邮件发送 --> {}", isSend);
                long end = System.currentTimeMillis();
                logger.info("【邮件 - 重点客户报表（后付费）任务】结束，统计时间= {}，统计耗时={}ms", executeTime.toString("yyyy-MM-dd HH:mm:ss"), (end - start));
            }
        } catch (IOException e) {
            logger.error("未找到指定的模板文件 ----------> {}, {}", e.getMessage(), e);
        } catch (TemplateException e) {
            logger.error("FreeMaker 模板解析失败 ----------> {}, {}", e.getMessage(), e);
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("【邮件 - 重点客户报表（预付费）任务】失败 ----------> {}, {}", e.getMessage(), e);
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 读取配置文件
     */

    private List<Map> readClientInfo(String clientInfo, PayType payType) {
        List<Map> mapList = new ArrayList<>();
        String regex;
        if (payType.equals(PayType.prepayment)) { // 预付费参数配置
            regex = "clientName:[^\\:\\,]+(,\\w{6})+";
        } else if (payType.equals(PayType.postpay)) {
            regex = "clientName:[^\\:\\,]+(,\\w{6}:0.\\d{0,4})+";
        } else {
            return mapList;
        }
        Pattern pattern = Pattern.compile(regex);
//        Pattern pattern = Pattern.compile("^clientName:[^\\:\\,]+(,\\w{6}:0.\\d{0,4})+(;clientName:[^\\:\\,]+(,\\w{6}:0.\\d{0,4})+)*;?$");
        Matcher matcher = pattern.matcher(clientInfo);

        while (matcher.find()) {
            Map map = new HashMap();
            List clientids = new ArrayList();
            String[] strings = matcher.group().split(",");
            for (String str : strings) {
                String[] split = str.split(":");
                if (split.length > 1) {
                    map.put(split[0], split[1]);
                }
                if (split[0].equals("clientName")) {
                    continue;
                }
                clientids.add(split[0]);
            }
            map.put(PropertiesParams.CLIENTIDS.getType(), clientids);
            logger.debug("从配置文件中读取的 重要客户账号信息 ---> {}", map);
            mapList.add(map);
        }
        return mapList;
    }

    /**
     * 整体情况数据
     * 每月1日到昨日数据
     * access基本数据 + 销售收入 + 通道计费条数(1+2+3) + 通道成本 + 毛利
     */
    @DataSource(DataSourceType.READ)
    private List<Overall> overall(Map params, DateTime executeTime, PayType payType) {
        logger.debug("整体情况报表 ---> start");
        DateTime startTime = executeTime.minusDays(1).withDayOfMonth(1);
        int start = Integer.parseInt(startTime.toString("yyyyMMdd"));
        int end = Integer.parseInt(executeTime.toString("yyyyMMdd"));
        List<Overall> overallList = new ArrayList<>();
        int i = 1;
        while (start < end) {
            Overall overall = overallOneday(params, start, payType);
            if (overall == null) {
                overall = new Overall(String.valueOf(start), 0, 0, BigDecimal.ZERO, 0,
                        0, 0, 0, BigDecimal.ZERO, 0,
                        BigDecimal.ZERO, BigDecimal.ZERO);
            }
            overallList.add(overall);
            start = Integer.parseInt(startTime.plusDays(i).toString("yyyyMMdd"));
            ++i;
        }
        logger.debug("整体情况报表 ---> end");
        return overallList;
    }

    /**
     * 统计整体数据, 只统计一天
     */
    @DataSource(DataSourceType.READ)
    private Overall overallOneday(Map params, int dateTime, PayType payType) {
        logger.debug("整体情况报表 ---> start ---> {} 当前数据", dateTime);
        if (payType.getValue() < 0 || payType.getValue() > 1) {
            throw new RuntimeException("参数PayType只能选择prepayment和postpay");
        }
        params.put("stattype", 0); // 统计类型，0：每日，1：每月
        params.put("date", dateTime);
        if (payType.equals(PayType.prepayment)) { // 预付费参数配置
            params.put("groupParams", "clientid,sub_id");
//            params.put("groupParams", "sub_id");
        } else if (payType.equals(PayType.postpay)) {
            params.put("groupParams", "clientid");
        }

        List<AccessChannelStatistics> accessChannelStatisticss = accessChannelStatisticsMapper.querySumByClientids(params);

        Overall overall = new Overall();
        boolean flag = false;
        for (AccessChannelStatistics access : accessChannelStatisticss) {
            if (access == null) continue;
            /**
             * todo 部分账号可能会出现由预付费转为后付费, 所以预付费条件中需要加上 subid != 0
             */
//            if(payType.equals(PayType.prepayment) && "0".equals(access.getSubId())){ // 预付费参数配置
            if(!payType.getValue().equals(access.getPaytype())){ // 预付费参数配置
                continue;
            }

//             销售单价, 每个账号不同
//            BigDecimal unitPrice = new BigDecimal((String) params.get(access.getClientid()));

            logger.debug("access 信息 --->{}", access);
            BigDecimal saleIncome = BigDecimal.ZERO;
            // 销售单价, 每个账号不同
            BigDecimal unitPrice = BigDecimal.ZERO;
            if (payType.equals(PayType.postpay)) { // 后付费从配置参数中取值
                // 销售收入 = 销售单价 * 状态(3)
                unitPrice = new BigDecimal((String) params.get(access.getClientid()));
                saleIncome = unitPrice.multiply(new BigDecimal(access.getReportsuccess() + access.getSubmitsuccess()));
            } else {  // 预付费从数据中读取
                Account account = accountMapper.getByClientid(access.getClientid());
                // oem客户从 t_sms_oem_client_pool 中读取
                if (!StringUtil.isEmpty(access.getSubId()) && !access.getSubId().equals("0") && account.getAgentType().equals(5)) {
                    OemClientPool oemClientPool = oemClientPoolMapper.getById(Long.valueOf(access.getSubId()));
                    //产品类型，0：行业，1：营销，2：国际
                    logger.debug("OEM客户短信池信息 --> {}", oemClientPool);
                    saleIncome = oemClientPool.getUnitPrice().multiply(new BigDecimal(access.getReportsuccess() + access.getSubmitsuccess()));
                } else if (!StringUtil.isEmpty(access.getSubId()) && !access.getSubId().equals("0")) {
                    ClientOrder clientOrder = clientOrderMapper.getById(Long.valueOf(access.getSubId()));
                    logger.debug("品牌客户订单信息 --> {}", clientOrder);
                    saleIncome = clientOrder.getSalePrice().divide(clientOrder.getQuantity(), 4, ROUND_HALF_UP).multiply(new BigDecimal(access.getReportsuccess() + access.getSubmitsuccess()));
                }
            }


            // 销售收入 = 销售单价 * 状态(3) todo 预付费 单独处理
//            BigDecimal saleIncome = unitPrice.multiply(new BigDecimal(access.getReportsuccess()));
            if (!flag) {
                overall = new Overall(String.valueOf(access.getDate()),// 时间
                        access.getUsersmstotal(),       // usersmstotal 用户短信总量0+1+3+4+5+6+7+8+9+10
                        access.getSendtotal(),          // sendtotal 总发送量1+3+6
                        BigDecimal.ZERO,                // successrate 成功率(3/总发送量)
                        (access.getReportsuccess() + access.getSubmitsuccess()),    // successtotal 成功量(1+3)
                        access.getSubmitsuccess(),      // submitsuccess 提交成功(1)
                        access.getReportsuccess(),      // reportsuccess 明确成功(3)
                        access.getReportfail(),         // reportfail 明确失败(6)
                        saleIncome,                     // saleIncome 销售收入
                        0,                   // smscntTotal 通道计费条数(1+2+3)
                        BigDecimal.ZERO,                // costOutcome 通道成本(单价 * 计费)
                        saleIncome);                    // profit 毛利
                flag = true;
            } else {
                overall.setUsersmstotal(overall.getUsersmstotal() + access.getUsersmstotal());
                overall.setSendtotal(overall.getSendtotal() + access.getSendtotal());
                overall.setSuccesstotal(overall.getSuccesstotal() + access.getReportsuccess() + access.getSubmitsuccess());
                overall.setSubmitsuccess(overall.getSubmitsuccess() + access.getSubmitsuccess());
                overall.setReportsuccess(overall.getReportsuccess() + access.getReportsuccess());
                overall.setReportfail(overall.getReportfail() + access.getReportfail());
                overall.setSaleIncome(overall.getSaleIncome().add(saleIncome));
                overall.setProfit(overall.getProfit().add(saleIncome));
            }
        }

//        params.put("groupParams", "clientid");
        params.remove("groupParams");
        List<RecordChannelStatistics> recordChannelStatisticss = recordChannelStatisticsMapper.queryAllByClientids(params);

        for (RecordChannelStatistics record : recordChannelStatisticss) {
            // 通道计费条数 (1 + 2 +3)
            int smscntTotal = record.getSubmitsuccess() + record.getSubretsuccess() + record.getReportsuccess();
            Channel channel = channelMapper.getByCid(record.getChannelid());
            if (channel == null || !channel.getCid().equals(record.getChannelid())) {
                continue;
            }
            // 通道成本价
//            BigDecimal costOutcome = record.getCosttotal().divide(new BigDecimal("1000"), 4, ROUND_HALF_UP); // todo 实际成本
            BigDecimal costOutcome = new BigDecimal(smscntTotal).multiply(channel.getCostprice()).setScale(4, ROUND_HALF_UP); // todo 通道成本（单价*计费）
            if (!flag) {
                overall = new Overall(String.valueOf(record.getDate()),
                        0,                            // usersmstotal 用户短信总量0+1+3+4+5+6+7+8+9+10
                        0,                              // sendtotal 总发送量1+3+6
                        BigDecimal.ZERO,                         // successrate 成功率(3/总发送量)
                        0,                            // successtotal 成功量(1+3)
                        0,                          // submitsuccess 提交成功(1)
                        0,                          // reportsuccess 明确成功(3)
                        0,                             // reportfail 明确失败(6)
                        BigDecimal.ZERO,                        // saleIncome 销售收入
                        smscntTotal,                            // smscntTotal 通道计费条数(1+2+3)
                        costOutcome,                            // costOutcome 通道成本(实际成本, 系统累计成本)
                        BigDecimal.ZERO.subtract(costOutcome)); // profit 毛利
                flag = true;
            } else {
                overall.setSmscntTotal(overall.getSmscntTotal() + smscntTotal);
                overall.setCostOutcome(overall.getCostOutcome().add(costOutcome));
                overall.setProfit(overall.getProfit().subtract(costOutcome));
            }
        }
        if (!flag) {
            return null;
        } else {
            // 成功率 (3/总发送量)
            BigDecimal successrate = new BigDecimal(overall.getReportsuccess()).divide(excludeZero(overall.getSendtotal()), 4, ROUND_HALF_UP);
            overall.setSuccessrate(successrate);
            // 毛利率 (毛利÷销售收入)
            BigDecimal saleIncome = excludeZero(overall.getSaleIncome());
            BigDecimal profitRate = overall.getProfit().divide(saleIncome, 4, ROUND_HALF_UP);
            overall.setProfitRate(profitRate);

        }
        params.remove("date");

        logger.debug("整体情况报表 ---> end ---> {} 当前数据 ---> {}", dateTime, overall);
        return overall;
    }

    /**
     * 整体数据合计
     *
     * @param overallList
     * @return
     */
    private OverallTotal overallTotal(List<Overall> overallList) {
        OverallTotal overallTotal = OverallTotal.defaultOverallTotal();
        for (Overall overall : overallList) {
            overallTotal.setUsersmstotal(overallTotal.getUsersmstotal() + overall.getUsersmstotal());
            overallTotal.setSendtotal(overallTotal.getSendtotal() + overall.getSendtotal());
            overallTotal.setSuccesstotal(overallTotal.getSuccesstotal() + overall.getSuccesstotal());
            overallTotal.setSubmitsuccess(overallTotal.getSubmitsuccess() + overall.getSubmitsuccess());
            overallTotal.setReportsuccess(overallTotal.getReportsuccess() + overall.getReportsuccess());
            overallTotal.setReportfail(overallTotal.getReportfail() + overall.getReportfail());
            overallTotal.setSaleIncome(overallTotal.getSaleIncome().add(overall.getSaleIncome()));
            overallTotal.setSmscntTotal(overallTotal.getSmscntTotal() + overall.getSmscntTotal());
            overallTotal.setCostOutcome(overallTotal.getCostOutcome().add(overall.getCostOutcome()));
            overallTotal.setProfit(overallTotal.getProfit().add(overall.getProfit()));
        }
        overallTotal.setProfitRate(overallTotal.getProfit().divide(excludeZero(overallTotal.getSaleIncome()), 4, ROUND_HALF_UP));
        BigDecimal reportsuccess = new BigDecimal(overallTotal.getReportsuccess());
        overallTotal.setSuccessrate(reportsuccess.divide(excludeZero(overallTotal.getSendtotal()), 4, ROUND_HALF_UP));
        logger.debug("整体数据合计 ---> overallTotal --> {}", overallTotal);
        return overallTotal;
    }

    /**
     * 昨日通道使用明细
     * record 基础数据 +　销售成本 + 销售收入 + 通道单价 + 通道计费 + 通道成本（元）+(通道计费数-用户侧3)/通道计费数 + 毛利（元）
     * 条件：group by channelid
     */
    @DataSource(DataSourceType.READ)
    private List<ChannelDetail> channelDetails(Map params, DateTime executeTime, PayType payType) {
        logger.debug("昨日通道使用明细 ---> start");
        if (payType.getValue() < 0 || payType.getValue() > 1) {
            throw new RuntimeException("参数PayType只能选择prepayment和postpay");
        }

        params.put("stattype", 0);  // 统计类型，0：每日，1：每月
        params.put("date", Integer.parseInt(executeTime.minusDays(1).toString("yyyyMMdd")));
        if (payType.equals(PayType.postpay)) { // 后付费参数配置
            params.put("groupParams", "clientid,channelid");
        } else {  // 预付费参数配置
            params.put("groupParams", "clientid,channelid,sub_id");
        }

        List<AccessChannelStatistics> accessChannelStatisticss = accessChannelStatisticsMapper.querySumByClientids(params);
        params.put("groupParams", "clientid,channelid");
        List<RecordChannelStatistics> recordChannelStatisticss = recordChannelStatisticsMapper.querySumByClientids(params);

        Map<String, ChannelDetail> channelDetailMap = new TreeMap<>();
        for (AccessChannelStatistics access : accessChannelStatisticss) {
            // todo 单价查询向上提一层
            if (access == null) continue;
            /**
             * todo 部分账号可能会出现由预付费转为后付费, 所以预付费条件中需要加上 subid != 0
             */
            if(payType.equals(PayType.prepayment) && "0".equals(access.getSubId())){ // 预付费参数配置
                continue;
            }

            // 遍历账号 匹配 价格
            for (RecordChannelStatistics record : recordChannelStatisticss) {
                if (record != null && record.getClientid().equals(access.getClientid()) && record.getChannelid().equals(access.getChannelid())) {
                    Channel channel = channelMapper.getByCid(access.getChannelid());
                    Account account = accountMapper.getByClientid(record.getClientid());
                    if (channel == null || !channel.getCid().equals(record.getChannelid())) {
                        continue;
                    }
                    // 销售收入 = 销售单价 * 状态(1+3)
                    BigDecimal saleIncome = BigDecimal.ZERO;
                    // 销售单价, 每个账号不同
                    String hyUnitprice = new String();
                    String yxUnitprice = new String();
                    BigDecimal unitPrice = BigDecimal.ZERO;
                    if (payType.equals(PayType.postpay)) { // 后付费从配置参数中取值
                        // 销售收入 = 销售单价 * 状态(3)
                        unitPrice = new BigDecimal((String) params.get(access.getClientid()));
                        saleIncome = unitPrice.multiply(new BigDecimal(access.getReportsuccess() + access.getSubmitsuccess()));
                    } else {  // 预付费从数据中读取
                        // oem客户从 t_sms_oem_client_pool 中读取
                        if (!StringUtil.isEmpty(access.getSubId()) && !access.getSubId().equals("0") && account.getAgentType().equals(5)) {
                            OemClientPool oemClientPool = oemClientPoolMapper.getById(Long.valueOf(access.getSubId()));
                            //产品类型，0：行业，1：营销，2：国际
                            if (oemClientPool.getProductType().equals(0)) {
                                hyUnitprice = oemClientPool.getUnitPrice().toString();
                            } else if (oemClientPool.getProductType().equals(1)) {
                                yxUnitprice = oemClientPool.getUnitPrice().toString();
                            }
                            saleIncome = oemClientPool.getUnitPrice().multiply(new BigDecimal(access.getReportsuccess() + access.getSubmitsuccess()));
                        } else if (!StringUtil.isEmpty(access.getSubId()) && !access.getSubId().equals("0")) {
                            ClientOrder clientOrder = clientOrderMapper.getById(Long.valueOf(access.getSubId()));
                            // 产品类型，0：行业，1：营销，2：国际，7：USSD，8：闪信，9：挂机短信，其中0和1为普通短信，2为国际短信
                            if (clientOrder.getProductType().equals(0)) {
                                hyUnitprice = clientOrder.getSalePrice().divide(clientOrder.getQuantity(), 4, ROUND_HALF_UP).toString();
                            } else if (clientOrder.getProductType().equals(1)) {
                                yxUnitprice = clientOrder.getSalePrice().divide(clientOrder.getQuantity(), 4, ROUND_HALF_UP).toString();
                            }
                            saleIncome = clientOrder.getSalePrice().divide(clientOrder.getQuantity(), 4, ROUND_HALF_UP).multiply(new BigDecimal(access.getReportsuccess() + access.getSubmitsuccess()));
                        }
                    }
                    // 通道成本价
//                    BigDecimal costOutcome = new BigDecimal(smscntTotal).multiply(record.getCosttotal());
                    // 通道计费条数 (1 + 2 +3)
                    int smscntTotal = record.getSubmitsuccess() + record.getSubretsuccess() + record.getReportsuccess();
                    BigDecimal accumulatedCost = record.getCosttotal().divide(new BigDecimal("1000"), 4, ROUND_HALF_UP);
                    BigDecimal multiplyCost = new BigDecimal(smscntTotal).multiply(channel.getCostprice()).setScale(4, ROUND_HALF_UP); // todo 通道成本（单价*计费）

                    // (用户侧1+3-通道计费数)/通道计费数
                    if (channelDetailMap.get(access.getClientid() + access.getChannelid()) == null) {
                        //成功率(3/总发送量)
                        BigDecimal successrate = new BigDecimal(access.getReportsuccess()).divide(excludeZero(access.getSendtotal()), 4, ROUND_HALF_UP);
                        BigDecimal inOutRate = new BigDecimal(access.getReportsuccess() + access.getSubmitsuccess() - record.getChargetotal()).divide(excludeZero(record.getChargetotal()), 4, ROUND_HALF_UP);
                        ChannelDetail channelDetail = new ChannelDetail(access.getDate().toString(),
                                access.getClientid(),
                                account.getName(),
                                OperatorsType.getNameByValue(channel.getOperatorstype()),
                                channel.getCid().toString(),            // channelid 通道id
                                channel.getRemark(),                    // channelremark 通道说明
                                access.getSendtotal(),                  // sendtotal 总发送量1+3+6
                                successrate,                            // successrate 成功率(3/总发送量)
                                (access.getReportsuccess() + access.getSubmitsuccess()),    // successtotal 成功量(1+3)
                                access.getSubmitsuccess(),              // submitsuccess 提交成功(1)
                                access.getReportsuccess(),              // reportsuccess 明确成功(3)
                                access.getReportfail(),                 // reportfail 明确失败(6)
                                unitPrice,                              // unitPrice 销售单价
                                hyUnitprice,                            // hyUnitprice 行业单价 todo
                                yxUnitprice,                            // yxUnitPrice 营销单价 todo
                                saleIncome,                             // saleIncome 销售收入 (销售单价*成功量) todo
                                channel.getCostprice(),                 // costfee 通道单价
                                smscntTotal,                            // smscntTotal 通道计费条数
                                accumulatedCost,                        // accumulatedCost 通道成本1（系统累加） todo
                                multiplyCost,                           // multiplyCost 通道成本2（单价*计费） todo
                                inOutRate,                              // inOutRate (用户侧1+3-通道计费数)/通道计费数
                                saleIncome.subtract(multiplyCost),      // profit 毛利（收入-成本2）
                                saleIncome.subtract(multiplyCost).divide(excludeZero(saleIncome), 4, ROUND_HALF_UP)// profitRate 毛利率 (毛利÷销售收入)
                        );
                        channelDetailMap.put(access.getClientid() + access.getChannelid(), channelDetail);
                        logger.debug("昨日通道使用明细 ---> {} 单条通道的数据 channelDetail --> {}", access.getChannelid(), channelDetail.toString());
                    } else {
                        ChannelDetail channelDetail = channelDetailMap.get(access.getClientid() + access.getChannelid());
                        channelDetail.setSendtotal(channelDetail.getSendtotal() + access.getSendtotal());   // sendtotal 总发送量1+3+6
                        channelDetail.setSuccesstotal(channelDetail.getSuccesstotal() + access.getReportsuccess() + access.getSubmitsuccess());// successtotal 成功量(1+3)
                        channelDetail.setSubmitsuccess(channelDetail.getSubmitsuccess() + access.getSubmitsuccess());   // submitsuccess 提交成功(1)
                        channelDetail.setReportsuccess(channelDetail.getReportsuccess() + access.getReportsuccess());   // reportsuccess 明确成功(3)
                        channelDetail.setReportfail(channelDetail.getReportfail() + access.getReportfail());            // reportfail 明确失败(6)
                        channelDetail.setSaleIncome(channelDetail.getSaleIncome().add(saleIncome));                     // saleIncome 销售收入 (销售单价*成功量)

                        // todo 不能加 acces分组有多条记录, record只有一个
//                        channelDetail.setSmscntTotal(channelDetail.getSmscntTotal() + smscntTotal);                     // smscntTotal 通道计费条数
//                        channelDetail.setAccumulatedCost(channelDetail.getAccumulatedCost().add(accumulatedCost));      // accumulatedCost 通道成本1（系统累加）
//                        channelDetail.setMultiplyCost(channelDetail.getMultiplyCost().add(multiplyCost));               // multiplyCost 通道成本2（单价*计费）
                        // todo 不能加 acces分组有多条记录, record只有一个

                        channelDetail.setProfit(channelDetail.getProfit().add(saleIncome));      // profit 毛利（收入-成本2）
                        // inOutRate (用户侧1+3-通道计费数)/通道计费数
                        BigDecimal inOutRate = new BigDecimal(channelDetail.getReportsuccess() + channelDetail.getSubmitsuccess() - channelDetail.getSmscntTotal()).divide(excludeZero(channelDetail.getSmscntTotal()), 4, ROUND_HALF_UP);
                        channelDetail.setInOutRate(inOutRate);
                        // successrate 成功率(3/总发送量)
                        BigDecimal successrate = new BigDecimal(channelDetail.getReportsuccess()).divide(excludeZero(channelDetail.getSendtotal()), 4, ROUND_HALF_UP);
                        channelDetail.setSuccessrate(successrate);
                        channelDetail.setProfitRate(channelDetail.getProfit().divide(excludeZero(channelDetail.getSaleIncome()), 4, ROUND_HALF_UP)); // profitRate 毛利率 (毛利÷销售收入)
                        //hyUnitprice 行业单价
                        if (!channelDetail.getHyUnitprice().contains(hyUnitprice)) {
                            if (StringUtil.isEmpty(channelDetail.getHyUnitprice())) {
                                channelDetail.setHyUnitprice(hyUnitprice);
                            } else {
                                channelDetail.setHyUnitprice(channelDetail.getHyUnitprice() + "," + hyUnitprice);
                            }
                        }
                        //yxUnitprice 行业单价
                        if (!channelDetail.getYxUnitprice().contains(yxUnitprice)) {
                            if (StringUtil.isEmpty(channelDetail.getYxUnitprice())) {
                                channelDetail.setYxUnitprice(yxUnitprice);
                            } else {
                                channelDetail.setYxUnitprice(channelDetail.getYxUnitprice() + "," + yxUnitprice);
                            }
                        }
                        channelDetailMap.put(access.getClientid() + access.getChannelid(), channelDetail);
                        logger.debug("昨日通道使用明细 ---> {} 单条通道的数据 channelDetail --> {}", access.getChannelid(), channelDetail.toString());

                    }
                }
            }
        }
        params.remove("date");
        params.remove("groupParams");
        List<ChannelDetail> channelDetailList = new ArrayList<>();
        channelDetailList.addAll(channelDetailMap.values());
        logger.debug("昨日通道使用明细 ---> end , 数据channelDetailList --> {}", channelDetailList);
        if (channelDetailList.size() == 0) {
            channelDetailList.add(ChannelDetail.defaultChannelDetail());
        }
        return channelDetailList;
    }

    /**
     * 昨日通道使用合计
     *
     * @param channelDetailList
     * @return
     */
    private ChannelDetailTotal channelDetailTotal(List<ChannelDetail> channelDetailList) {
        ChannelDetailTotal channelDetailTotal = ChannelDetailTotal.defaultChannelDetail();
        for (ChannelDetail channelDetail : channelDetailList) {
            channelDetailTotal.setSendtotal(channelDetailTotal.getSendtotal() + channelDetail.getSendtotal()); // 总发送量
            channelDetailTotal.setSuccesstotal(channelDetailTotal.getSuccesstotal() + channelDetail.getSuccesstotal());// 成功量(1+3)
            channelDetailTotal.setSubmitsuccess(channelDetailTotal.getSubmitsuccess() + channelDetail.getSubmitsuccess()); // 提交成功(1)
            channelDetailTotal.setReportsuccess(channelDetailTotal.getReportsuccess() + channelDetail.getReportsuccess()); // 明确成功(3)
            channelDetailTotal.setReportfail(channelDetailTotal.getReportfail() + channelDetail.getReportfail());  // 明确失败(6)
            channelDetailTotal.setSaleIncome(channelDetailTotal.getSaleIncome().add(channelDetail.getSaleIncome()));// 销售收入
            channelDetailTotal.setSmscntTotal(channelDetailTotal.getSmscntTotal() + channelDetail.getSmscntTotal());   // 通道计费数
            channelDetailTotal.setAccumulatedCost(channelDetailTotal.getAccumulatedCost().add(channelDetail.getAccumulatedCost())); // 通道成本1(系统累加)
            channelDetailTotal.setMultiplyCost(channelDetailTotal.getMultiplyCost().add(channelDetail.getMultiplyCost()));  // 通道成本2 (单价*计费)
            channelDetailTotal.setProfit(channelDetailTotal.getProfit().add(channelDetail.getProfit()));    // 毛利
        }
        BigDecimal temp = new BigDecimal(channelDetailTotal.getSuccesstotal() - channelDetailTotal.getSmscntTotal());
        channelDetailTotal.setInOutRate(temp.divide(excludeZero(channelDetailTotal.getSmscntTotal()), 4, ROUND_HALF_UP));   // (用户侧1+3-通道计费数)/通道计费数
        channelDetailTotal.setProfitRate(channelDetailTotal.getProfit().divide(excludeZero(channelDetailTotal.getSaleIncome()), 4, ROUND_HALF_UP));

        logger.debug("昨日通道使用合计 ---> channelDetailTotal --> {}", channelDetailTotal);
        return channelDetailTotal;
    }

    /**
     * 本月常用通道信息
     * 运营商	 + 通道号 + 通道备注 + 通道成本价 + 本月成功率3/总发送量 + 本月明确成功率3/(1+3)
     * group by channelid
     */
    @DataSource(DataSourceType.READ)
    private Map<String, List<CommonlyChannelDetail>> commonlyChannel(Map params, DateTime executeTime, PayType payType) {
        logger.debug("本月常用通道信息 ---> start");
        if (payType.getValue() < 0 || payType.getValue() > 1) {
            throw new RuntimeException("参数PayType只能选择prepayment和postpay");
        }

        params.put("stattype", 0); // 统计类型，0：每日，1：每月
        DateTime startTime = executeTime.minusDays(1).withDayOfMonth(1);
        params.put("startTime", Integer.parseInt(startTime.toString("yyyyMMdd")));
        params.put("endTime", Integer.parseInt(executeTime.minusDays(1).toString("yyyyMMdd")));
        params.put("groupParams", "operatorstype,channelid");

        List<AccessChannelStatistics> accessChannelStatisticss = accessChannelStatisticsMapper.queryCommonlyChannel(params);
        List<RecordChannelStatistics> recordChannelStatisticss = recordChannelStatisticsMapper.queryCommonlyChannel(params);


        Map<String, List<CommonlyChannelDetail>> map = new HashMap();
        for (RecordChannelStatistics record : recordChannelStatisticss) {
            if (record == null) continue;
            for (AccessChannelStatistics access : accessChannelStatisticss) {
                // 遍历账号 匹配 价格
//                if (access != null && record.getClientid().equals(access.getClientid()) && record.getChannelid().equals(access.getChannelid())) {
                if (access != null && record.getChannelid().equals(access.getChannelid())) {
                    Channel channel = channelMapper.getByCid(access.getChannelid());
                    if (channel == null || !channel.getCid().equals(record.getChannelid())) {
                        continue;
                    }

                    // 通道侧 本月成功率3/总发送量(1+2+3+5+6)
                    BigDecimal successrate = new BigDecimal(record.getReportsuccess()).divide(excludeZero(record.getSendtotal()), 4, ROUND_HALF_UP);
                    // (用户侧1+3-通道计费数)/通道计费数
                    BigDecimal inOutRate = new BigDecimal(access.getSubmitsuccess() + access.getReportsuccess() - record.getChargetotal()).divide(excludeZero(record.getChargetotal()), 4, ROUND_HALF_UP);
                    CommonlyChannelDetail commonlyChannelDetail = new CommonlyChannelDetail(
                            String.valueOf(record.getChannelid()),
                            record.getRemark(),
//                            record.getCosttotal().divide(new BigDecimal("1000"), 4, ROUND_HALF_UP).toString(),
                            channel.getCostprice().setScale(4, ROUND_HALF_UP).toString(), // 通道单价
                            (record.getSubmitsuccess() + record.getSubretsuccess() + record.getReportsuccess()), // (1+2+3)
                            (successrate.multiply(new BigDecimal("100")).setScale(1, ROUND_HALF_UP).toString() + "%"),
                            (inOutRate.multiply(new BigDecimal("100")).setScale(1, ROUND_HALF_UP).toString()) + "%"); // 成功率(3/总发送量)
                    if (map.get(OperatorsType.getNameByValue(record.getOperatorstype())) == null) {
                        List<CommonlyChannelDetail> tempList = new ArrayList<>();
                        tempList.add(commonlyChannelDetail);
                        map.put(OperatorsType.getNameByValue(record.getOperatorstype()), tempList);
                    } else {
                        map.get(OperatorsType.getNameByValue(record.getOperatorstype())).add(commonlyChannelDetail);
                    }
                    logger.debug("本月常用通道信息 --> 添加一条通道信息 --> {}", commonlyChannelDetail);
                }
            }
        }
        params.remove("startTime");
        params.remove("endTime");
        params.remove("groupParams");

        logger.debug("本月常用通道信息 ---> end ,数据 ---> {}", map);
        return map;
    }

    private List<String> format(Map<String, List<CommonlyChannelDetail>> map) {
        logger.debug("本月常用通道信息 - 数据格式转换 ---> start ,数据转换前 --> {}", map);

        List<String> channelOverall = new ArrayList<>();
        for (String string : map.keySet()) {
            StringBuilder sb = new StringBuilder();
            sb.append("<td rowspan=\"").append(map.get(string).size()).append("\" height=\"180\" class=\"xl77\" style=\"border-style: solid solid solid; border-bottom-width: 1pt; height: " + (13.8 * map.get(string).size()) + "pt; border-top-width: 0.5pt; border-top-color: windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px; color: windowtext; font-size: 9pt; font-family: 微软雅黑, sans-serif; vertical-align: middle; border-right-width: 0.5pt; border-left-width: 0.5pt; border-right-color: windowtext; border-left-color: windowtext; white-space: nowrap; text-align: center;\">");
            sb.append(string).append("</td>");
            boolean isFirstTd = true;
            for (CommonlyChannelDetail ccd : map.get(string)) {
                if (!isFirstTd)
                    sb = new StringBuilder();
                sb.append("<td class=\"xl72\" style=\"border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px; color: windowtext; font-size: 9pt; font-family: 微软雅黑, sans-serif; vertical-align: bottom; white-space: nowrap; text-align: center;\">");
                sb.append(ccd.getChannelid()).append("</td>");
                sb.append("<td class=\"xl67\" align=\"left\" style=\"border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px; color: windowtext; font-size: 9pt; font-family: 微软雅黑, sans-serif; vertical-align: bottom; white-space: nowrap;\">");
                sb.append(ccd.getChannelremark()).append("</td>");
                sb.append("<td class=\"xl67\" align=\"right\" style=\"border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px; color: windowtext; font-size: 9pt; font-family: 微软雅黑, sans-serif; vertical-align: bottom; white-space: nowrap;\">");
                sb.append(ccd.getCostfee()).append("</td>");
                sb.append("<td class=\"xl73\" align=\"right\" style=\"border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px; color: windowtext; font-size: 9pt; font-family: 微软雅黑, sans-serif; vertical-align: bottom; white-space: nowrap;\">");
                sb.append(ccd.getSendtotal()).append("</td>");
                sb.append("<td class=\"xl73\" align=\"right\" style=\"border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px; color: windowtext; font-size: 9pt; font-family: 微软雅黑, sans-serif; vertical-align: bottom; white-space: nowrap;\">");
                sb.append(ccd.getSuccessrate()).append("</td>");
                sb.append(" <td class=\"xl75\" align=\"right\" style=\"border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px;");
                if (ccd.getInOutRate().contains("-")) {
                    sb.append(" color: red; font-size: 9pt; font-family: 微软雅黑, sans-serif; vertical-align: bottom; white-space: nowrap;\">");
                } else {
                    sb.append(" color: windowtext; font-size: 9pt; font-family: 微软雅黑, sans-serif; vertical-align: bottom; white-space: nowrap;\">");
                }
                sb.append(ccd.getInOutRate()).append("</td>");
                isFirstTd = false;
                channelOverall.add(sb.toString());
            }

        }

        logger.debug("本月常用通道信息 - 数据格式转换 ---> end ,数据转换后 --> {}", channelOverall);

        return channelOverall;
    }

    private String loaderFreemaker(Map<String, Object> data, PayType payType) throws IOException, TemplateException {
        logger.debug("开始加载 ---->  freemaker 模板");

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_25);
        cfg.setDirectoryForTemplateLoading(new File(this.getClass().getResource("/templates").getFile()));

        cfg.setDefaultEncoding("UTF-8");

        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        cfg.setLogTemplateExceptions(false);
        String templateName = new String();
        if (payType.equals(PayType.prepayment)) { // 预付费
            templateName = "prepayment-daily-template.ftl";
        } else {
            templateName = "postpaid-daily-template.ftl";
        }
        Template temp = cfg.getTemplate(templateName);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        Writer out = new OutputStreamWriter(buffer);
        temp.process(data, out);
        String emailContent = buffer.toString();
        out.close();
        return emailContent;
    }

    /**
     * @param emailContent
     * @return
     * @throws MessagingException
     * @desc 发送邮件
     */
    private boolean sendMail(String emailContent, DateTime dateTime, String clientName, PayType payType) {
        logger.debug("准备发送邮件 ---->  日期 : {}", dateTime.toString());


//        if(payType.equals(PayType.prepayment)){ // 预付费
//            templateName = "prepayment-daily-template.ftl";
//        }else {
//            templateName = "postpaid-daily-template.ftl";
//        }

        String[] receivers = readEmail(payType);

        StringBuilder subject = new StringBuilder("重要客户 ");
        subject.append("【")
                .append(clientName)
                .append("（")
                .append(payType.getDesc())
                .append("）")
                .append("】")
                .append(" 发送详情")
                .append(dateTime.minusDays(1).getMonthOfYear())
                .append("月 ")
                .append(dateTime.minusDays(1).getDayOfMonth())
                .append("日");
//        String subject = "大客户阿里发送详情 " + dateTime.minusDays(1).getMonthOfYear() + "月 " + dateTime.minusDays(1).getDayOfMonth() + "日";
        List<String> list = new ArrayList<>();
        for (String receiver : receivers) {
            boolean send = send(receiver, subject.toString(), emailContent);
            if (!send) {
                list.add(receiver);
            }
        }
        if (list.size() > 0) {
            for (String receiver : list) {
                boolean send = send(receiver, subject.toString(), emailContent);
                if (send) {
                    list.remove(receiver);
                }
            }
        }
        if (list.size() == receivers.length) {
            return false;
        }
        return true;
    }

    public boolean send(String receiver, String subject, String emailContent) {
        List<String> list = new ArrayList();
        try {
            MimeMessage msg = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "utf-8");
            helper.setFrom("admin@ucpaas.com");
            helper.setTo(receiver);
            helper.setSubject(subject);
            helper.setText(emailContent, true);
            javaMailSender.send(msg);

            logger.debug("发送文本格式的Email【成功】：to={}, subject={}, body={}", receiver, subject, emailContent);
        } catch (Throwable e) {
            logger.error("发送文本格式的Email【失败】：to={}, subject={}, body={}, 错误 --->", receiver, subject, emailContent, e);
            return false;
        }
        return true;
    }

    /**
     * 读取配置文件中的 收件人email
     *
     * @param payType
     * @return
     */
    private String[] readEmail(PayType payType) {
        if (payType.getValue() < 0 || payType.getValue() > 1) {
            throw new RuntimeException("参数PayType只能选择prepayment和postpay");
        }
        try {
            String majorClientsEmailReceivers = PropertiesUtil.get("majorClientsEmailReceivers");
            String[] split = majorClientsEmailReceivers.split(";");
            if (split.length == 1) {
                return split[0].substring(1, split[0].length() - 1).split(",");
            } else if (split.length > 1) {
                return split[payType.getValue()].substring(1, split[payType.getValue()].length() - 1).split(",");
            }
        } catch (IOException e1) {
            logger.error("majorClientsEmailReceivers配置错误,使用默认邮箱niutao@ucpaas.com", e1);
        }
        return new String[]{"niutao@ucpaas.com"};
    }

    /**
     * 除数排除0, 是0 返回 1
     *
     * @param bigDecimal
     * @return
     */
    private BigDecimal excludeZero(BigDecimal bigDecimal) {

        return bigDecimal.compareTo(BigDecimal.ZERO) != 0 ? bigDecimal : BigDecimal.ONE;
    }

    private BigDecimal excludeZero(int number) {
        return number != 0 ? new BigDecimal(number) : BigDecimal.ONE;
    }

    private BigDecimal excludeZero(String number) {
        if (number != null && number.matches("^\\d+$")) {
            return number.equals("0") ? new BigDecimal(number) : BigDecimal.ONE;
        } else {
            throw new ClassCastException("String cast BigDecimal error");
        }
    }

    public static void main(String[] args) throws IOException {

        MajorClientsServiceImpl thiss = new MajorClientsServiceImpl();
//        String[] strings = majorClientsService.readEmail(PayType.prepayment);
//
//        System.out.println(strings.length);
//        for(String string : strings){
//
//            System.out.println(string);
//        }

        BigDecimal bigDecimal = new BigDecimal(0);
        System.out.println(bigDecimal.compareTo(BigDecimal.ZERO));

//        BigDecimal bigDecimal1 = thiss.excludeZero("1");
//        System.out.println(bigDecimal1.toString());
//        System.out.println("".matches("^\\d+$"));

        String propertis = PropertiesUtil.get(PropertiesParams.MAJORCLIENTSPREPAYMENTINFO.getType());
        List<Map> mapList = thiss.readClientInfo(propertis, PayType.prepayment);
        for (Map<String, String> map : mapList) {

//            for(String string: map.keySet()){
//
//                System.out.println(string + " --> " + map.get(string));
//            }
            System.out.println("------------");
            System.out.println(map);
        }


//               boolean matches = "clientName:常州网胜,b000u2,b000r2,b13121".matches("^clientName:[^\\:\\,]+(,\\w{6})+$");
//
//
////        System.out.println(matches);
//
////        Pattern pattern = Pattern.compile("clientName:[^\\:\\,]+(,\\w{6})+(;clientName:[^\\:\\,]+(,\\w{6})+)*;?");
//        Pattern pattern = Pattern.compile("clientName:[^\\:\\,]+(,\\w{6})+");
////        Pattern pattern = Pattern.compile("^clientName:[^\\:\\,]+(,\\w{6}:0.\\d{0,4})+(;clientName:[^\\:\\,]+(,\\w{6}:0.\\d{0,4})+)*;?$");
//        Matcher matcher = pattern.matcher("clientName:常州网胜,b000u2,b000r2,b13121;clientName:常州网胜,b000u2,b000r2,b13121");
//        while(matcher.find())
//            System.out.println(matcher.group());

    }

}
