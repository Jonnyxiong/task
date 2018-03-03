package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.entity.access.AccessChannelStatistics;
import com.ucpaas.sms.task.entity.access.CustomerStatTemp;
import com.ucpaas.sms.task.entity.access.SmsAccessSendStat;
import com.ucpaas.sms.task.entity.record.RecordConsumeStatistics;
import com.ucpaas.sms.task.enum4sms.AccessChannelStatisticsType;
import com.ucpaas.sms.task.exception.AccessChannelStatisticsException;
import com.ucpaas.sms.task.mapper.access.AccessChannelStatisticsMapper;
import com.ucpaas.sms.task.model.ResultVO;
import com.ucpaas.sms.task.model.TaskInfo;
import com.ucpaas.sms.task.statistic.AccessStatisticStrategy;
import com.ucpaas.sms.task.util.JacksonUtil;
import com.ucpaas.sms.task.util.JsonUtils;
import com.ucpaas.sms.task.util.StringUtil;
import com.ucpaas.sms.task.util.UcpaasDateUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author huangwenjie
 * @title
 * @description 客户(用户)运营、运维报表统计
 * @date 2017-02-21
 */
@Service
public class AccessChannelStatisticsServiceImpl implements AccessChannelStatisticsService {
    private static final Logger LOGGER = LoggerFactory.getLogger("AccessChannelStatisticsService");

    @Autowired
    private AccessChannelStatisticsMapper accessChannelStatisticsMapper;
    @Autowired
    private CustomerStatTempService customerStatTempService;
    @Autowired
    private SmsAccessSendStatService smsAccessSendStatService;

    @Autowired
    private RecordConsumeStatisticsService recordConsumeStatisticsService;
    @Autowired
    private RecordChannelStatisticsService recordChannelStatisticsService;

    @Autowired
    @Qualifier("deman1point5AccessStatistic")
    private AccessStatisticStrategy accessStatisticStrategy;
    @Resource(name = "access_transactionManager_new")
    private DataSourceTransactionManager txManager;

    @Override
    @Transactional(value = "access")
    public ResultVO insert(AccessChannelStatistics model) {
        this.accessChannelStatisticsMapper.insert(model);
        return ResultVO.successDefault();
    }

    @Override
    @Transactional(value = "access")
    public ResultVO insertBatch(List<AccessChannelStatistics> modelList) {
        this.accessChannelStatisticsMapper.insertBatch(modelList);
        return ResultVO.successDefault();
    }

    @Override
    @Transactional(value = "access")
    public ResultVO delete(Long id) {
        AccessChannelStatistics model = this.accessChannelStatisticsMapper.getById(id);
        if (model != null)
            this.accessChannelStatisticsMapper.delete(id);
        return ResultVO.successDefault();
    }

    @Override
    @Transactional(value = "access")
    public ResultVO update(AccessChannelStatistics model) {
        AccessChannelStatistics old = this.accessChannelStatisticsMapper.getById(model.getId());
        if (old == null) {
            return ResultVO.failure();
        }
        this.accessChannelStatisticsMapper.update(model);
        AccessChannelStatistics newModel = this.accessChannelStatisticsMapper.getById(model.getId());
        return ResultVO.successDefault(newModel);
    }

    @Override
    @Transactional(value = "access")
    public ResultVO updateSelective(AccessChannelStatistics model) {
        AccessChannelStatistics old = this.accessChannelStatisticsMapper.getById(model.getId());
        if (old != null)
            this.accessChannelStatisticsMapper.updateSelective(model);
        return ResultVO.successDefault();
    }

    @Override
    @Transactional(value = "access")
    public ResultVO getById(Long id) {
        AccessChannelStatistics model = this.accessChannelStatisticsMapper.getById(id);
        return ResultVO.successDefault(model);
    }

    @Override
    @Transactional(value = "access")
    public ResultVO count(Map<String, Object> params) {
        return ResultVO.successDefault(this.accessChannelStatisticsMapper.count(params));
    }

    @Override
    @Transactional(value = "access")
    public List<AccessChannelStatistics> queryAll(Map<String, Object> params) {
        return this.accessChannelStatisticsMapper.queryAll(params);
    }

    @Override
    @Transactional(value = "access")
    public List<AccessChannelStatistics> queryAllGroupBy(Map<String, Object> params) {
        return this.accessChannelStatisticsMapper.queryAllGroupBy(params);
    }

    @Override
    @Transactional(value = "access")
    public int deleteByDate(String statTime) {
        return accessChannelStatisticsMapper.deleteByDate(statTime);
    }

    @Override
    public List<AccessChannelStatistics> queryMonthly(Map<String, Object> params) {
        return accessChannelStatisticsMapper.queryMonthly(params);
    }

    @Override
    public boolean fourDaysAgo(TaskInfo taskInfo) {
        String format = taskInfo.getExecuteType().getFormat();
        DateTime executeNext = UcpaasDateUtils.parseDate(taskInfo.getExecuteNext(), format);
        DateTime statDay = executeNext.minusDays(4);
        LOGGER.debug("第前四天的客户运营运维统计报表任务【开始】：统计日期 = {} ------------------", statDay.toString("yyyyMMdd"));
        long begainTime = System.currentTimeMillis();
//        boolean result = false;
//        result = generateDataIn(statDay);

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
// explicitly setting the transaction name is something that can only be done programmatically
        def.setName("SomeTxName");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txManager.getTransaction(def);
        try {
            accessStatisticStrategy.staticAlgorithm(statDay);
        } catch (Exception e) {
            LOGGER.error("第前四天的用户侧统计失败(进行回滚 )", e);
            txManager.rollback(status);
            LOGGER.error("回滚成功 ");
            return false;
        }
        txManager.commit(status);
        LOGGER.debug("第前四天的客户运营运维统计报表任务【结束】：耗时 = {}", System.currentTimeMillis() - begainTime);
        return true;
    }


    @Override
    public boolean yesterday(TaskInfo taskInfo) {
        String format = taskInfo.getExecuteType().getFormat();
        DateTime executeNext = UcpaasDateUtils.parseDate(taskInfo.getExecuteNext(), format);
        DateTime statDay = executeNext.minusDays(1);
        LOGGER.debug("第前一天(昨天)的客户运营运维统计报表任务【开始】：统计日期 = {} ------------------", statDay.toString("yyyyMMdd"));
        long begainTime = System.currentTimeMillis();
//        boolean result = false;
//        result = generateDataIn(statDay);
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
// explicitly setting the transaction name is something that can only be done programmatically
        def.setName("SomeTxName");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txManager.getTransaction(def);
        try {
            accessStatisticStrategy.staticAlgorithm(statDay);
        } catch (Exception e) {
            LOGGER.error("第前一天的用户侧统计失败", e);
            txManager.rollback(status);
            LOGGER.error("回滚成功 ");
            return false;
        }
        txManager.commit(status);
        LOGGER.debug("第前一天(昨天)的客户运营运维统计报表任务【结束】：耗时 = {}", System.currentTimeMillis() - begainTime);
        return true;
    }


    @Override
    public boolean fixAccessRecordStatistic(TaskInfo taskInfo) {
        String format = taskInfo.getExecuteType().getFormat();
        DateTime executeNext = UcpaasDateUtils.parseDate(taskInfo.getExecuteNext(), format);
        DateTime statDay = executeNext.minusDays(1);
        fixAccessRecordStatistic(statDay); //纠正昨天的数据
        if(executeNext.minusDays(4).getMonthOfYear() !=statDay.getMonthOfYear()) { //需另外修复上月的数据
            statDay = executeNext.minusDays(4);
            fixAccessRecordStatistic(statDay);//纠正第前四天的数据
        }

        return true;
    }

    private void fixAccessRecordStatistic(DateTime statDay) {
        LOGGER.debug("【补偿】纠正用户销售和通道成本 任务【开始】：数据日期 = {} ------------------", statDay.toString("yyyyMMdd"));
        long begainTime = System.currentTimeMillis();
        //1. 获取当月的客户发送量表所有数据
        String yyyyMM = statDay.toString("yyyyMM");
        LOGGER.debug("获取当月的客户发送量表所有数据,yyyyMM={}",yyyyMM);
        List<SmsAccessSendStat> smsAccessSendStats = smsAccessSendStatService.queryByDateLike(yyyyMM);

        for (SmsAccessSendStat smsAccessSendStat : smsAccessSendStats) {
            //1.1 获取通道成本
            LOGGER.debug("客户发送量{}", JacksonUtil.toJSON(smsAccessSendStat));
            if(smsAccessSendStat.getOperatorstype()==null){  //由于access数据容易存在异常，出现通道运营商类型为null，所以需设置个异常值，免得少了这个where语句
                smsAccessSendStat.setOperatorstype(Integer.MIN_VALUE);
            }

            BigDecimal costfee = recordChannelStatisticsService.computeClientChannelCostFee(smsAccessSendStat.getClientid(),smsAccessSendStat.getBelongSale(),smsAccessSendStat.getSmstype(),smsAccessSendStat.getPaytype(),smsAccessSendStat.getDate().toString(),smsAccessSendStat.getOperatorstype());
            smsAccessSendStat.setCostfee(costfee);
            if(smsAccessSendStat.getOperatorstype().equals(Integer.MIN_VALUE)){
                smsAccessSendStat.setOperatorstype(null);
            }
            smsAccessSendStatService.updateSelective(smsAccessSendStat);
        }
        //2. 获取当月的通道消耗量表所有数据
        LOGGER.debug("获取当月的通道消耗量表所有数据,yyyyMM={}",yyyyMM);
        List<RecordConsumeStatistics> recordConsumeStatistics = recordConsumeStatisticsService.queryByDateLike(yyyyMM);
        for(RecordConsumeStatistics rcs:recordConsumeStatistics){
            //2.1 获取销售收入
            //1.1 获取通道成本
            LOGGER.debug("通道消耗量{}", JacksonUtil.toJSON(rcs));
            BigDecimal saletotal = calculateSaleTotal(rcs.getChannelid(),rcs.getBelongBusiness(),rcs.getDepartmentId(),rcs.getSmstype(),rcs.getPaytype(),rcs.getOperatorstype(),rcs.getDate().toString());
            rcs.setSaletotal(saletotal);
            recordConsumeStatisticsService.updateSelective(rcs);
        }


        LOGGER.debug("【补偿】纠正用户销售和通道成本 任务【结束】：耗时 = {}", System.currentTimeMillis() - begainTime);
    }

    @Override
    public BigDecimal calculateSaleTotal(Integer channelid, Long belongBusiness, Integer departmentId, Integer smstype, Integer paytype, Integer operatorstype, String yyyyMMoryyyyMMdd) {
        if (channelid == null)
            throw new AccessChannelStatisticsException("channelid通道ID不能为空");
//        if (smstype == null)
//            throw new AccessChannelStatisticsException("smstype短信类型不能为空");
        if (paytype == null)
            throw new AccessChannelStatisticsException("paytype付费类型不能为空");
        if (operatorstype == null)
            throw new AccessChannelStatisticsException("operatorstype运营商类型不能为空");
        if (StringUtil.isEmpty(yyyyMMoryyyyMMdd) || (yyyyMMoryyyyMMdd.length() != 8 && yyyyMMoryyyyMMdd.length() != 6))
            throw new AccessChannelStatisticsException("统计日期yyyyMMoryyyyMMdd不能为空或格式不对");
        BigDecimal result = BigDecimal.ZERO;
        if (yyyyMMoryyyyMMdd.length() == 8) { //查询日数据
            Map params = new HashMap();
            params.put("channelid", channelid);
            params.put("belongBusiness", belongBusiness);
            params.put("departmentId", departmentId);
            params.put("smstype", smstype);
            params.put("paytype", paytype);
            params.put("operatorstype", operatorstype);
            params.put("date", yyyyMMoryyyyMMdd);
            List<AccessChannelStatistics> accessChannelStatistics = this.queryAll(params);

            for (AccessChannelStatistics acs : accessChannelStatistics) {
                result = result.add(acs.getSalefee());
            }

        } else {//查询月数据
            Map params = new HashMap();
            params.put("channelid", channelid);
            params.put("belongBusiness", belongBusiness);
            params.put("departmentId", departmentId);
            params.put("smstype", smstype);
            params.put("paytype", paytype);
            params.put("operatorstype", operatorstype);
            params.put("stattype", AccessChannelStatisticsType.daily.getValue());
            params.put("likeDate", yyyyMMoryyyyMMdd);
            List<AccessChannelStatistics> accessChannelStatistics = this.queryAll(params);

            for (AccessChannelStatistics acs : accessChannelStatistics) {
                result = result.add(acs.getSalefee());
            }
        }
        return result;
    }

    /**
     * statDay 要统计的日期
     *
     * @param statDay
     * @return
     */
    @Deprecated
    public boolean generateDataIn(DateTime statDay) {

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
            return true;
        }

        LOGGER.debug("清除统计的数据时间date={}的数据 ", statTime);
        accessChannelStatisticsMapper.deleteByDate(statTime);
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
        for (Entry<String, AccessChannelStatistics> tempEntry : temp.entrySet()) {
            dailyStatistics.add(tempEntry.getValue());
        }

        LOGGER.debug("完成每日详细数据的两种统计（通道不为0和state=4） 数据{} ", JsonUtils.toJson(dailyStatistics));
        /**
         * 每日详细数据统计：拦截数据，状态等于0/5/7/8/9/10
         */
        temp = new HashMap<>();
        for (CustomerStatTemp cst : customerStatTemps) {
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
        for (Entry<String, AccessChannelStatistics> tempEntry : temp.entrySet()) {
            dailyStatistics.add(tempEntry.getValue());
        }

        LOGGER.debug("完成每日详细数据的三种统计（通道不为0、state=4、拦截），三种统计数据不应该互相有重叠  数据{} ", JsonUtils.toJson(dailyStatistics));

        if (dailyStatistics.size() > 0) {
            accessChannelStatisticsMapper.insertBatch(dailyStatistics);
        } else {
            LOGGER.info("客户运营运维统计报表任务【结束】：计算出来的每日统计数据为空");
            return true;
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
        for (Entry<String, AccessChannelStatistics> tempEntry : temp.entrySet()) {
            dailySumStatistics.add(tempEntry.getValue());
        }
        LOGGER.debug(" 完成每日合计数据的统计  数据{} ", JsonUtils.toJson(dailySumStatistics));
        accessChannelStatisticsMapper.insertBatch(dailySumStatistics);


        // 清除t_sms_access_channel_statistics 表统计月的老数据
        statTime = statDay.toString("yyyyMM");
        LOGGER.debug(" 清除t_sms_access_channel_statistics 表统计月的老数据 , date={} ", statTime);
        accessChannelStatisticsMapper.deleteByDate(statTime);


        // 获得当月所有已经存在的每日明细数据
        Map<String, Object> params = new HashMap<>();
        params.put("stattype", AccessChannelStatisticsType.daily.getValue());
        params.put("dataTimePrix", statTime.substring(0, 6));

        dailyStatistics = accessChannelStatisticsMapper.queryMonthly(params);


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
        for (Entry<String, AccessChannelStatistics> tempEntry : temp.entrySet()) {
            monthlyStatistics.add(tempEntry.getValue());
        }
        LOGGER.debug(" 完成每月详细数据的统计  数据{} ", JsonUtils.toJson(monthlyStatistics));
        accessChannelStatisticsMapper.insertBatch(monthlyStatistics);


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
        for (Entry<String, AccessChannelStatistics> tempEntry : temp.entrySet()) {
            monthlySumStatistics.add(tempEntry.getValue());
        }
        LOGGER.debug(" 完成每月合计数据的统计  数据{} ", JsonUtils.toJson(monthlySumStatistics));
        accessChannelStatisticsMapper.insertBatch(monthlySumStatistics);

        return true;
    }


}
