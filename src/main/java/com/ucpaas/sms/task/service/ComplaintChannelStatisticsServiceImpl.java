package com.ucpaas.sms.task.service;

import com.jsmsframework.channel.entity.JsmsChannel;
import com.jsmsframework.channel.entity.JsmsChannelPropertyLog;
import com.jsmsframework.channel.entity.JsmsComplaintList;
import com.jsmsframework.channel.service.JsmsChannelPropertyLogService;
import com.jsmsframework.channel.service.JsmsChannelService;
import com.jsmsframework.channel.service.JsmsComplaintListService;
import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.entity.JsmsParam;
import com.jsmsframework.common.service.JsmsParamService;
import com.jsmsframework.common.util.JsonUtil;
import com.jsmsframework.user.entity.JsmsAccount;
import com.jsmsframework.user.service.JsmsAccountService;
import com.ucpaas.sms.task.entity.access.AccessChannelStatistics;
import com.ucpaas.sms.task.entity.access.JsmsClientOperationStatistics;
import com.ucpaas.sms.task.entity.record.JsmsChannelOperationStatistics;
import com.ucpaas.sms.task.entity.record.RecordChannelStatistics;
import com.ucpaas.sms.task.enum4sms.ComplaintEnum;
import com.ucpaas.sms.task.mapper.access.AccessChannelStatisticsMapper;
import com.ucpaas.sms.task.model.TaskInfo;
import com.ucpaas.sms.task.statistic.AccessStatisticStrategy;
import com.ucpaas.sms.task.util.DataUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author 方明智
 * @title
 * @description 客户(用户)运营、运维报表统计
 */
@Service
public class ComplaintChannelStatisticsServiceImpl implements ComplaintChannelStatisticsService {
    private static final Logger LOGGER = LoggerFactory.getLogger("ComplaintChannelStatisticsService");

    @Autowired
    private AccessChannelStatisticsMapper accessChannelStatisticsMapper;

    @Autowired
    private JsmsChannelOperationStatisticsService jsmsChannelOperationStatisticsService;

    @Autowired
    private JsmsClientOperationStatisticsService jsmsClientOperationStatisticsService;

    @Autowired
    private JsmsComplaintListService jsmsComplaintListService;

    @Autowired
    private JsmsAccountService jsmsAccountService;

    @Autowired
    private JsmsChannelService jsmsChannelService;

    @Autowired
    private RecordConsumeStatisticsService recordConsumeStatisticsService;

    @Autowired
    private JsmsChannelPropertyLogService jsmsChannelPropertyLogService;

    @Autowired
    private AccessChannelStatisticsService accessChannelStatisticsService;

    @Autowired
    private RecordChannelStatisticsService recordChannelStatisticsService;

    @Autowired
    private JsmsParamService jsmsParamService;

    @Override
    public boolean execute(TaskInfo taskInfo) {
        LOGGER.info("投诉率任务开始");
        LOGGER.info("执行明细表数据去重");
        deleteData();
        LOGGER.info("执行通道侧数据统计");
        statisticsRecord();
        LOGGER.info("执行用户侧数据统计");
        statisticsAccess();

        return true;
    }
    /*
     *通道侧统计
     */
    public void statisticsRecord(){
        LOGGER.info("通道侧统计");
        Map params=new HashMap();
        params.put("createtimeStart",getFirstDayDate(getNewTime()));
        params.put("createtimeEnd",getNewTime());
        params.put("stattype",ComplaintEnum.日统计.getValue());
        params.put("status", ComplaintEnum.有效.getValue());
        List<RecordChannelStatistics> recordChannelStatisticssGroup=recordChannelStatisticsService.queryAllGroupBy(params);
        LOGGER.info("groupBy查询到{}条记录",recordChannelStatisticssGroup.size());
        int i=0;
        if(recordChannelStatisticssGroup!=null)
        for(RecordChannelStatistics recordChannelStatistics:recordChannelStatisticssGroup){
            LOGGER.info("通道侧：总的{}条数据，目前执行第{}条",recordChannelStatisticssGroup.size(),++i);
            if(recordChannelStatistics.getOperatorstype()<0){
                LOGGER.info("查询到的数据运营商类型为{}，小于0跳过统计",recordChannelStatistics.getOperatorstype());
                continue;
            }
            JsmsChannelOperationStatistics model=new JsmsChannelOperationStatistics();
            model.setChannelId(recordChannelStatistics.getChannelid());
            model.setOperatorstype(recordChannelStatistics.getOperatorstype());
            model.setDate(getDataDay(new Date()));
            model.setBelongBusiness(getChannelBussiness(recordChannelStatistics.getChannelid()));
            model.setOwnerType(getOwnerType(recordChannelStatistics.getChannelid()));
            model.setUpdateTime(new Date());

            Map paramsComplain=new HashMap();
            paramsComplain.put("channelId",recordChannelStatistics.getChannelid());
            paramsComplain.put("sendTimeStart",params.get("createtimeStart"));
            paramsComplain.put("sendTimeEnd",params.get("createtimeEnd"));
            //paramsComplain.put("smstype",recordChannelStatistics.getSmstype());
           // paramsComplain.put("operatorstype",recordChannelStatistics.getOperatorstype());
            paramsComplain.put("status",ComplaintEnum.有效.getValue());
            //投诉个数
            model.setComplaintNumber(jsmsComplaintListService.count4Channel(paramsComplain));

            params.put("channelid",recordChannelStatistics.getChannelid());
            List<RecordChannelStatistics> recordChannelStatisticss=recordChannelStatisticsService.queryAll(params);
            LOGGER.info("select参数：查询到{}条统计数据记录",params,recordChannelStatisticss.size());

            //成本价
            model.setCostprice(setCostprice(recordChannelStatistics));

            for(RecordChannelStatistics recordChannelStatistics2:recordChannelStatisticss ){
                setRecord(recordChannelStatistics2,model);
            }

            //投诉率
            model.setComplaintRatio(integerDivideInteger(model.getComplaintNumber()*1000000,model.getReportsuccess()));
            //低消条数
            model.setLowConsumeNumber(getPropertyLog(recordChannelStatistics.getChannelid(),"low_consume_number"));

            //低消完成率
            model.setLowConsumeRatio(integerDividerBig(model.getReportsuccess(),model.getLowConsumeNumber()));
            //投诉系数
            model.setComplaintCoefficient(getPropertyLog(recordChannelStatistics.getChannelid(),"complaint_coefficient"));
            //投诉差异值
            model.setComplaintDifference(model.getComplaintCoefficient().subtract(model.getComplaintRatio()));
            //成功率
            model.setSendSuccessRatio(integerDivideInteger(model.getReportsuccess()*100,model.getSubmitTotal()));

            LOGGER.info("通道侧插入数据{}", JsonUtil.toJson(model));
            jsmsChannelOperationStatisticsService.insertOrUpdateByDate(model);
        }
    }

    /*
    明细表数据去重
     */
    private void deleteData() {

        Map param=new HashMap();
        List<JsmsComplaintList> complaintList=jsmsComplaintListService.getDuplicateData(param);
        LOGGER.info("查询到重复数据{}", JsonUtil.toJson(complaintList));
        for(JsmsComplaintList jsmsComplaint:complaintList){
            jsmsComplaint.setStatus(ComplaintEnum.无效.getValue());//状态1为不可用
            LOGGER.info("删除重复数据{}", JsonUtil.toJson(jsmsComplaint));
            jsmsComplaintListService.updateSelective(jsmsComplaint);
        }
        if(complaintList!=null&&complaintList.size()>0){
            deleteData();
        }
    }


    /*
     *用户统计
     */
    public void statisticsAccess(){

        LOGGER.info("用户侧统计");
        Map params=new HashMap();
        params.put("createtimeStart",getFirstDayDate(getNewTime()));
        params.put("createtimeEnd",getNewTime());
        params.put("stattype",ComplaintEnum.日统计.getValue());
        params.put("status",ComplaintEnum.有效.getValue());
        List<AccessChannelStatistics> accessChannelStatisticssGroupBy=accessChannelStatisticsService.queryAllGroupBy(params);
        LOGGER.info("groupBy查询到{}条记录",accessChannelStatisticssGroupBy.size());
        int i=0;
        for(AccessChannelStatistics accessChannelStatistics:accessChannelStatisticssGroupBy){
            LOGGER.info("用户侧：总的{}条数据，目前执行第{}条",accessChannelStatisticssGroupBy.size(),++i);
            if(accessChannelStatistics.getOperatorstype()<0){
                LOGGER.info("查询到的数据运营商类型为{}，小于0跳过统计",accessChannelStatistics.getOperatorstype());
                continue;
            }
            JsmsClientOperationStatistics model=new JsmsClientOperationStatistics();
            model.setClientId(accessChannelStatistics.getClientid());
            model.setSmstype(accessChannelStatistics.getSmstype());
            model.setOperatorstype(accessChannelStatistics.getOperatorstype());
            model.setDate(getDataDay(new Date()));
            model.setUpdateTime(new Date());
            model.setBelongSale(getAccountBussiness(accessChannelStatistics.getClientid()));

            Map paramsComplain=new HashMap();
            paramsComplain.put("clientId",accessChannelStatistics.getClientid());
            paramsComplain.put("status",ComplaintEnum.有效.getValue());
            paramsComplain.put("operatorstype",accessChannelStatistics.getOperatorstype());
            paramsComplain.put("sendTimeStart",params.get("createtimeStart"));
            paramsComplain.put("sendTimeEnd",params.get("createtimeEnd"));
            //投诉个数
            model.setComplaintNumber(jsmsComplaintListService.count(paramsComplain));

            params.put("clientid",accessChannelStatistics.getClientid());

            params.put("operatorstype",accessChannelStatistics.getOperatorstype());
            List<AccessChannelStatistics> accessChannelStatisticss=accessChannelStatisticsService.queryAll(params);
            LOGGER.info("select参数{}:查询到{}条统计数据记录",params,accessChannelStatisticss.size());

            //单价
            model.setSalefee(setSalefee(accessChannelStatistics));

            for(AccessChannelStatistics accessChannelStatistics2:accessChannelStatisticss){
                setAccess(accessChannelStatistics2,model);
            }

            //投诉系数
            model.setComplaintCoefficient(getParamValue("CLIENT_COMPLAINT_COEFFICIENT"));

            //投诉率
            model.setComplaintRatio(integerDivideInteger(model.getComplaintNumber()*1000000,model.getReportsuccess()));
            //投诉差异值
            model.setComplaintDifference(model.getComplaintCoefficient().subtract(model.getComplaintRatio()));
            //成功率
            model.setSendSuccessRatio(integerDivideInteger(model.getReportsuccess()*100,model.getSubmitTotal()));
            LOGGER.info("用户侧插入数据{}", JsonUtil.toJson(model));
            jsmsClientOperationStatisticsService.insertOrUpdateUnique(model);

        }
    }
    private void setRecord(RecordChannelStatistics recordChannelStatistics,JsmsChannelOperationStatistics model){
        //总提交条数
        Integer total=recordChannelStatistics.getSubmitsuccess()+recordChannelStatistics.getSubretsuccess()+recordChannelStatistics.getReportsuccess()+recordChannelStatistics.getReportfail()+recordChannelStatistics.getSubretfail();
        model.setSubmitTotal(DataUtils.sumInteger(model.getSubmitTotal(),total));
        //明确成功条数
        model.setReportsuccess(DataUtils.sumInteger(model.getReportsuccess(),recordChannelStatistics.getReportsuccess()));
    }

    private void setAccess(AccessChannelStatistics accessChannelStatistics,JsmsClientOperationStatistics model){
        //总提交条数
        Integer total=accessChannelStatistics.getSubmitsuccess()+accessChannelStatistics.getReportsuccess()+accessChannelStatistics.getReportfail();
        model.setSubmitTotal(DataUtils.sumInteger(model.getSubmitTotal(),total));
        //明确成功条数
        model.setReportsuccess(DataUtils.sumInteger(model.getReportsuccess(),accessChannelStatistics.getReportsuccess()));
    }

    private BigDecimal setSalefee(AccessChannelStatistics accessChannelStatistics){
        BigDecimal r= BigDecimal.ZERO;
        JsmsAccount jsmsAccount=jsmsAccountService.getByClientId(accessChannelStatistics.getClientid());
        Map params=new HashMap();
        params.put("clientid",accessChannelStatistics.getClientid());
        params.put("identify",jsmsAccount.getIdentify().toString());
        params.put("date",accessChannelStatistics.getDate().toString());
        try{
            r=jsmsClientOperationStatisticsService.getSalefee(params);
        }catch (Exception e){
            LOGGER.info("统计单价异常问题，{}：{}", JsonUtil.toJson(params),e.toString());
        }
        return r;
    }

    private BigDecimal setCostprice(RecordChannelStatistics recordChannelStatistics){
        BigDecimal r= BigDecimal.ZERO;
        JsmsChannel jsmsChannel=jsmsChannelService.getByCid(recordChannelStatistics.getChannelid());
        Map params=new HashMap();
        params.put("channelid",recordChannelStatistics.getChannelid());
        params.put("identify",jsmsChannel.getIdentify().toString());
        params.put("date",recordChannelStatistics.getDate().toString());
        try{
            r=jsmsChannelOperationStatisticsService.getCostfee(params);
        }catch (Exception e){
            LOGGER.info("统计销售价异常问题，{}：{}", JsonUtil.toJson(params),e.toString());
        }

        return r;

    }


    public static BigDecimal integerDivideInteger(Integer dividend,Integer divisor) {
        BigDecimal dividendBig=new BigDecimal(dividend);
        BigDecimal divisorBig=new BigDecimal(divisor);
        if(divisor==0){
            return BigDecimal.ZERO;
        }
        return dividendBig.divide(divisorBig,4,BigDecimal.ROUND_HALF_UP);
    }
    public static BigDecimal integerDividerBig(Integer dividend,BigDecimal divisor) {
        BigDecimal dividendBig=new BigDecimal(dividend);
        if(divisor.compareTo(BigDecimal.ZERO)==0){
            return BigDecimal.ZERO;
        }
        return dividendBig.divide(divisor,4,BigDecimal.ROUND_HALF_UP);
    }


    /*
    获取当月第一天日期
     */
    public static Date getFirstDayDate(Date date) {
        Date firstDate=new Date();
        try {
            SimpleDateFormat month = new SimpleDateFormat("yyyyMM");
            SimpleDateFormat day = new SimpleDateFormat("yyyyMMdd");
            String firstDay = month.format(date).toString() + "01";
            firstDate = day.parse(firstDay);
        }catch (Exception e){

        }
        return firstDate;
    }
    public static Integer getDataDay(Date date) {
        String time="";
        try{
            SimpleDateFormat month = new SimpleDateFormat("yyyyMM");

            time=month.format(date).toString();

        }catch (Exception e){
            LOGGER.info("获取当月第一天失败：{}",e.toString());
        }
        return Integer.parseInt(time);
    }

    private BigDecimal getPropertyLog(Integer channelId,String property){
        JsmsPage page=new JsmsPage();
        page.getParams().put("channelId",channelId);
        page.getParams().put("property",property);
        page.getParams().put("dateIndex",getNewTime());
        page.setOrderByClause("create_time desc");

        page =jsmsChannelPropertyLogService.queryList(page);
        List<JsmsChannelPropertyLog> jsmsChannelPropertyLogs=page.getData();
        if(CollectionUtils.isEmpty(jsmsChannelPropertyLogs)){
            return BigDecimal.ZERO;
        }else {
            return  new BigDecimal(jsmsChannelPropertyLogs.get(0).getValue());
        }

    }
    private BigDecimal getParamValue(String key){
        List<JsmsParam> jsmsParams=jsmsParamService.getByParamKey("CLIENT_COMPLAINT_COEFFICIENT");

        if(CollectionUtils.isEmpty(jsmsParams)){
            return new BigDecimal(0.52);
        }else {
            return new BigDecimal(jsmsParams.get(0).getParamValue());
        }
    }

    private Long getChannelBussiness(Integer channelId){
        JsmsChannel jsmsChannel=jsmsChannelService.getByCid(channelId);
        if(jsmsChannel!=null){
            return jsmsChannel.getBelongBusiness();
        }else {
            return  0L;
        }

    }
    private Integer getOwnerType(Integer channelId){
        JsmsChannel jsmsChannel=jsmsChannelService.getByCid(channelId);
        if(jsmsChannel!=null){
            return jsmsChannel.getOwnerType();
        }else {
            return 0;
        }

    }

    private Long getAccountBussiness(String clientId){
        JsmsAccount jsmsAccount=jsmsAccountService.getByClientId(clientId);
        if(jsmsAccount!=null){
            return jsmsAccount.getBelongSale();
        }else{
            return  0L;
        }
    }
    //如果是1号，则以上个月最后一天统计
    private Date getNewTime(){
        Calendar c = Calendar.getInstance();

        int today = c.get(c.DAY_OF_MONTH);

        if(today ==1){
            Calendar cal = Calendar.getInstance();
            //下面可以设置月份，注：月份设置要减1，所以设置1月就是1-1，设置2月就是2-1，如此类推
            cal.set(Calendar.MONTH, 1-1);
            //调到上个月
            cal.add(Calendar.MONTH, -1);
            //得到一个月最最后一天日期(31/30/29/28)
            int MaxDay=cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            //按你的要求设置时间
            cal.set( cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), MaxDay, 23, 59, 59);
            //按格式输出
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
            return cal.getTime();
        }else{
            return new Date();
        }
    }


}
