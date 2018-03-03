package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.aop.DataSource;
import com.ucpaas.sms.task.entity.access.AccessChannelStatistics;
import com.ucpaas.sms.task.entity.message.Account;
import com.ucpaas.sms.task.entity.message.Channel;
import com.ucpaas.sms.task.entity.record.RecordChannelStatistics;
import com.ucpaas.sms.task.enum4sms.DataSourceType;
import com.ucpaas.sms.task.enum4sms.OperatorsType;
import com.ucpaas.sms.task.enum4sms.PropertiesParams;
import com.ucpaas.sms.task.mapper.access.AccessChannelStatisticsMapper;
import com.ucpaas.sms.task.mapper.message.AccountMapper;
import com.ucpaas.sms.task.mapper.message.ChannelMapper;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.math.BigDecimal.ROUND_HALF_DOWN;
import static java.math.BigDecimal.ROUND_HALF_UP;

@Service
public class AliServiceImpl implements AliService {

    private static final Logger logger = LoggerFactory.getLogger("aliDailyDetailSendMail");

    @Autowired
    private AccessChannelStatisticsMapper accessChannelStatisticsMapper;
    @Autowired
    private RecordChannelStatisticsMapper recordChannelStatisticsMapper;
    @Autowired
    private ChannelMapper channelMapper;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private JavaMailSender javaMailSender;


    @Override
    /**
     * 后付费用户没有 单价, 需要在配置文件中读取
     */
    @DataSource(DataSourceType.READ)
    public boolean dailyDetail(TaskInfo taskInfo) {
        long start = System.currentTimeMillis();
        Map params = new HashMap<>();
        try {
            String propertis = PropertiesUtil.get(PropertiesParams.ALICLIENTINFO.getType());
            params = readClientInfo(propertis);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("解析配置文件中的账号失败");
            return true;
        }

        DateTime executeTime = taskInfo.getExecuteNextDate();

        Map<String, Object> data = new HashMap<>();
        try {
            List<Overall> overallList = overall(params, executeTime);
            data.put("overall", overallList); // 整体情况
            data.put("overallTotal", overallTotal(overallList));// 整体合计

            List<ChannelDetail> channelDetailList = channelDetails(params, executeTime);
            data.put("channelDetails", channelDetailList);  // 昨日通道使用明细
            data.put("channelDetailTotal", channelDetailTotal(channelDetailList));  // 昨日通道使用合计
            
            data.put("channelOverall", format(commonlyChannel(params, executeTime)));
            data.put("month", executeTime.minusDays(1).getMonthOfYear());
            data.put("day", executeTime.minusDays(1).getDayOfMonth());

            String emailContent = loaderFreemaker(data);

            if (StringUtil.isEmpty(emailContent)) {
                return true;
            }
            boolean isSend = false;
            isSend = sendMail(emailContent, executeTime);
            logger.debug("邮件发送 --> {}", isSend);
            return isSend;
        } catch (IOException e) {
            logger.error("未找到指定的模板文件 ----------> {}, {}", e.getMessage(), e);
        } catch (TemplateException e) {
            logger.error("FreeMaker 模板解析失败 ----------> {}, {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("发送邮件出现异常 ---> {}", e);
        }
        long end = System.currentTimeMillis();
        logger.info("【大客户阿里统计报表任务】结束，统计时间= {}，统计耗时={}ms", executeTime.toString("yyyy-MM-dd HH:mm:ss"), (end - start));

        return true;
    }

    /**
     * 读取配置文件
     */

    private Map readClientInfo(String clientInfo) {
        String[] strings = clientInfo.split(",");
        Map map = new HashMap();
        List clientids = new ArrayList();
        String regex = "\\w{6}:0.\\d{0,4}";
        Pattern pattern = Pattern.compile(regex);
//        Pattern pattern = Pattern.compile("^clientName:[^\\:\\,]+(,\\w{6}:0.\\d{0,4})+(;clientName:[^\\:\\,]+(,\\w{6}:0.\\d{0,4})+)*;?$");
        Matcher matcher = pattern.matcher(clientInfo);

        while (matcher.find()) {
            String[] split = matcher.group().split(":");
            map.put(split[0], split[1]);
            clientids.add(split[0]);
        }
        map.put(PropertiesParams.CLIENTIDS.getType(), clientids);
        logger.debug("从配置文件中读取的 阿里账号信息 ---> {}", map);
        return map;
    }

    /**
     * 整体情况数据
     * 每月1日到昨日数据
     * access基本数据 + 销售收入 + 通道计费条数(1+2+3) + 通道成本 + 毛利
     */
    @DataSource(DataSourceType.READ)
    private List<Overall> overall(Map params, DateTime executeTime) {
        logger.debug("整体情况报表 ---> start");
        DateTime startTime = executeTime.minusDays(1).withDayOfMonth(1);
        int start = Integer.parseInt(startTime.toString("yyyyMMdd"));
        int end = Integer.parseInt(executeTime.toString("yyyyMMdd"));
        List<Overall> overallList = new ArrayList<>();
        int i = 1;
        while (start < end) {
            Overall overall = overallOneday(params, start);
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
    private Overall overallOneday(Map params, int dateTime) {
        logger.debug("整体情况报表 ---> start ---> {} 当前数据", dateTime);
        params.put("stattype", 0); // 统计类型，0：每日，1：每月
        params.put("date", dateTime);

        List<AccessChannelStatistics> accessChannelStatisticss = accessChannelStatisticsMapper.queryAllByClientids(params);
        List<RecordChannelStatistics> recordChannelStatisticss = recordChannelStatisticsMapper.queryAllByClientids(params);
        Overall overall = new Overall();
        boolean flag = false;
        for (AccessChannelStatistics access : accessChannelStatisticss) {
            if (access == null) continue;
            // 销售单价, 每个账号不同
            BigDecimal unitPrice = new BigDecimal((String) params.get(access.getClientid()));
            // 销售收入 = 销售单价 * 状态(3)
            BigDecimal saleIncome = unitPrice.multiply(new BigDecimal(access.getReportsuccess()));
            if (!flag) {
                overall = new Overall(String.valueOf(access.getDate()),
                        access.getUsersmstotal(),
                        access.getSendtotal(),
                        BigDecimal.ZERO,
                        (access.getReportsuccess() + access.getSubmitsuccess()),
                        access.getSubmitsuccess(),
                        access.getReportsuccess(),
                        access.getReportfail(),
                        saleIncome,
                        0,
                        BigDecimal.ZERO,
                        saleIncome);
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

        for (RecordChannelStatistics record : recordChannelStatisticss) {
            // 通道计费条数 (1 + 2 +3)
            int smscntTotal = record.getSubmitsuccess() + record.getSubretsuccess() + record.getReportsuccess();
            // 通道成本价
            BigDecimal costOutcome = record.getCosttotal().divide(new BigDecimal("1000"), 4, ROUND_HALF_DOWN);
            if (!flag) {
                overall = new Overall(String.valueOf(record.getDate()),
                        0,
                        0,
                        BigDecimal.ZERO,
                        0,
                        0,
                        0,
                        0,
                        BigDecimal.ZERO,
                        smscntTotal,
                        costOutcome,
                        BigDecimal.ZERO.subtract(costOutcome));
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
            //成功率(3/总发送量)
            BigDecimal successrate = new BigDecimal(overall.getReportsuccess()).divide(excludeZero(overall.getSendtotal()), 4, ROUND_HALF_UP);
            overall.setSuccessrate(successrate);
            // 毛利率 (毛利÷销售收入)
            BigDecimal saleIncome = excludeZero(overall.getSaleIncome());
            BigDecimal profitRate = overall.getProfit().divide(excludeZero(saleIncome), 4, ROUND_HALF_UP);
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
    private List<ChannelDetail> channelDetails(Map params, DateTime executeTime) {
        logger.debug("昨日通道使用明细 ---> start");
        params.put("stattype", 0);  // 统计类型，0：每日，1：每月
        params.put("date", Integer.parseInt(executeTime.minusDays(1).toString("yyyyMMdd")));
        params.put("groupParams", "clientid,channelid");

        List<AccessChannelStatistics> accessChannelStatisticss = accessChannelStatisticsMapper.querySumByClientids(params);
        List<RecordChannelStatistics> recordChannelStatisticss = recordChannelStatisticsMapper.querySumByClientids(params);

        List<ChannelDetail> channelDetailList = new ArrayList<>();
        for (AccessChannelStatistics access : accessChannelStatisticss) {
            if (access == null) continue;
            // 销售收入 = 销售单价 * 状态(3)
            // 遍历账号 匹配 价格
            for (RecordChannelStatistics record : recordChannelStatisticss) {
                if (record != null && record.getClientid().equals(access.getClientid()) && record.getChannelid().equals(access.getChannelid())) {
                    Channel channel = channelMapper.getByCid(access.getChannelid());
                    Account account = accountMapper.getByClientid(record.getClientid());
                    if (channel == null || !channel.getCid().equals(record.getChannelid())) {
                        continue;
                    }
                    // 销售单价, 每个账号不同
                    BigDecimal unitPrice = new BigDecimal((String) params.get(access.getClientid()));
                    //成功率(3/总发送量)
                    BigDecimal successrate = new BigDecimal(access.getReportsuccess()).divide(excludeZero(access.getUsersmstotal()), 4, ROUND_HALF_DOWN);
                    // 通道计费条数 (1 + 2 +3)
                    int smscntTotal = record.getSubmitsuccess() + record.getSubretsuccess() + record.getReportsuccess();
                    // 销售收入 = 销售单价 * 状态(3)
                    BigDecimal saleIncome = unitPrice.multiply(new BigDecimal(access.getReportsuccess()));
                    // 通道成本价
                    BigDecimal accumulatedCost = record.getCosttotal().divide(new BigDecimal("1000"), 4, ROUND_HALF_UP);  //  通道成本1（系统累加）
                    BigDecimal multiplyCost = new BigDecimal(smscntTotal).multiply(channel.getCostprice()).setScale(4, ROUND_HALF_UP); // 通道成本2（单价*计费）
                    // (用户侧3-通道计费数)/通道计费数
                    BigDecimal inOutRate = new BigDecimal(access.getReportsuccess() - record.getChargetotal()).divide(excludeZero(record.getChargetotal()), 4, ROUND_HALF_DOWN);

                    ChannelDetail channelDetail = new ChannelDetail(
                            access.getDate().toString(),// 时间
                            access.getClientid(),       // 用户ID
                            account.getName(),          // 用户名称
                            OperatorsType.getNameByValue(channel.getOperatorstype()),   // 运营商类型
                            channel.getCid().toString(),// 通道号
                            channel.getRemark(),        // 通道备注
                            access.getSendtotal(),      // 总发送量(1+3+6)
                            successrate,                // 成功率(3/总发送量)
                            (access.getReportsuccess() + access.getSubmitsuccess()), // 成功量(1+3)
                            access.getSubmitsuccess(),  // 提交成功(1)
                            access.getReportsuccess(),  // 明确成功(3)
                            access.getReportfail(),     // 明确失败(6)
                            unitPrice,                  // 销售单价
                            saleIncome,                 // 销售收入
                            channel.getCostprice(),     // 通道单价
                            smscntTotal,                // 通道计费条数
                            accumulatedCost,            // accumulatedCost 通道成本1（系统累加）
                            multiplyCost,               // multiplyCost 通道成本2（单价*计费）
                            inOutRate,                  // (用户3-通道计费数)/通道计费数
                            saleIncome.subtract(multiplyCost),       // profit 毛利（收入-成本2）
                            saleIncome.subtract(multiplyCost).divide(excludeZero(saleIncome), 4, ROUND_HALF_UP)// profitRate 毛利率 (毛利÷销售收入)
                    );
                    channelDetailList.add(channelDetail);
                    logger.debug("昨日通道使用明细 ---> {} 单条通道的数据 channelDetail --> {}", access.getChannelid(), channelDetail.toString());

                }
            }
        }
        params.remove("date");
        params.remove("groupParams");
        logger.debug("昨日通道使用明细 ---> end , 数据channelDetailList --> {}", channelDetailList);
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
    private Map<String, List<CommonlyChannelDetail>> commonlyChannel(Map params, DateTime executeTime) {
        logger.debug("本月常用通道信息 ---> start");

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
                    BigDecimal successrate = new BigDecimal(record.getReportsuccess()).divide(excludeZero(record.getSendtotal()), 4, ROUND_HALF_DOWN);
                    // (用户侧3-通道计费数)/通道计费数
                    BigDecimal inOutRate = new BigDecimal(access.getReportsuccess() - record.getChargetotal()).divide(excludeZero(record.getChargetotal()), 4, ROUND_HALF_DOWN);
                    CommonlyChannelDetail commonlyChannelDetail = new CommonlyChannelDetail(
                            String.valueOf(record.getChannelid()),
                            record.getRemark(),
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

//        for (List list : map.values()){
//
//        }
//        for (CommonlyChannel cc : ccList) {
//            StringBuilder sb = new StringBuilder();
//            sb.append("<td rowspan=\"").append(cc.getChannelSize()).append("\" height=\"180\" class=\"xl77\" style=\"border-style: solid solid solid; border-bottom-width: 1pt; height: " + (13.8 * cc.getChannelSize()) + "pt; border-top-width: 0.5pt; border-top-color: windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px; color: windowtext; font-size: 9pt; font-family: 微软雅黑, sans-serif; vertical-align: middle; border-right-width: 0.5pt; border-left-width: 0.5pt; border-right-color: windowtext; border-left-color: windowtext; white-space: nowrap; text-align: center;\">");
//            sb.append(cc.getChanneloperatorstype()).append("</td>");
//            boolean isFirstTd = true;
//            for (CommonlyChannelDetail ccd : cc.getList()) {
//                if (!isFirstTd)
//                    sb = new StringBuilder();
//                sb.append("<td class=\"xl72\" style=\"border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px; color: windowtext; font-size: 9pt; font-family: 微软雅黑, sans-serif; vertical-align: bottom; white-space: nowrap; text-align: center;\">");
//                sb.append(ccd.getChannelid()).append("</td>");
//                sb.append("<td class=\"xl67\" align=\"left\" style=\"border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px; color: windowtext; font-size: 9pt; font-family: 微软雅黑, sans-serif; vertical-align: bottom; white-space: nowrap;\">");
//                sb.append(ccd.getChannelremark()).append("</td>");
//                sb.append("<td class=\"xl67\" align=\"right\" style=\"border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px; color: windowtext; font-size: 9pt; font-family: 微软雅黑, sans-serif; vertical-align: bottom; white-space: nowrap;\">");
//                sb.append(ccd.getCostfee()).append("</td>");
//                sb.append("<td class=\"xl73\" align=\"right\" style=\"border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px; color: windowtext; font-size: 9pt; font-family: 微软雅黑, sans-serif; vertical-align: bottom; white-space: nowrap;\">");
//                sb.append(ccd.getSuccessrate()).append("</td>");
//                sb.append(" <td class=\"xl75\" align=\"right\" style=\"border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px; color: windowtext; font-size: 9pt; font-family: 微软雅黑, sans-serif; vertical-align: bottom; white-space: nowrap;\">");
//                sb.append(ccd.getInOutRate()).append("</td>");
//                isFirstTd = false;
//                channelOverall.add(sb.toString());
//            }
//        }
        logger.debug("本月常用通道信息 - 数据格式转换 ---> end ,数据转换后 --> {}", channelOverall);

        return channelOverall;
    }

    private String loaderFreemaker(Map<String, Object> data) throws IOException, TemplateException {
        logger.debug("开始加载 ---->  freemaker 模板");

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_25);
        cfg.setDirectoryForTemplateLoading(new File(this.getClass().getResource("/templates").getFile()));

        cfg.setDefaultEncoding("UTF-8");

        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        cfg.setLogTemplateExceptions(false);

        Template temp = cfg.getTemplate("ali-daily-template.ftl");

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
    private boolean sendMail(String emailContent, DateTime dateTime) {
        logger.debug("准备发送邮件 ---->  日期 : {}", dateTime.toString());

        String aliDailyDetailEmailReceivers = "";
        try {
            aliDailyDetailEmailReceivers = PropertiesUtil.get("aliDailyDetailEmailReceivers");
        } catch (IOException e1) {
            logger.error("aliDailyDetailEmailReceivers配置错误,使用默认邮箱niutao@ucpaas.com", e1);
            aliDailyDetailEmailReceivers = "niutao@ucpaas.com";
        }
        String[] receivers = aliDailyDetailEmailReceivers.split(",");

        String subject = "大客户阿里发送详情 " + dateTime.minusDays(1).getMonthOfYear() + "月 " + dateTime.minusDays(1).getDayOfMonth() + "日";
        List<String> list = new ArrayList<>();
        for (String receiver : receivers) {
            boolean send = send(receiver, subject, emailContent);
            if (!send) {
                list.add(receiver);
            }
        }
        if (list.size() > 0) {
            for (String receiver : list) {
                boolean send = send(receiver, subject, emailContent);
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
}
