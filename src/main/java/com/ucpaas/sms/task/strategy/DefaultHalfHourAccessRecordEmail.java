package com.ucpaas.sms.task.strategy;

import com.jsmsframework.common.util.BeanUtil;
import com.jsmsframework.common.util.DateUtil;
import com.sun.mail.smtp.SMTPAddressFailedException;
import com.ucpaas.sms.task.dao.AccessSlaveDao;
import com.ucpaas.sms.task.dao.RecordSlaveDao;
import com.ucpaas.sms.task.entity.stats.ChannelSuccessRateByClientid;
import com.ucpaas.sms.task.entity.stats.ChannelSuccessRateRealtime;
import com.ucpaas.sms.task.entity.stats.ClientSuccessRateRealtime;
import com.ucpaas.sms.task.mapper.stats.ChannelSuccessRateByClientidMapper;
import com.ucpaas.sms.task.mapper.stats.ChannelSuccessRateRealtimeMapper;
import com.ucpaas.sms.task.mapper.stats.ClientSuccessRateRealtimeMapper;
import com.ucpaas.sms.task.model.TaskInfo;
import com.ucpaas.sms.task.model.halfhour.*;
import com.ucpaas.sms.task.util.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

@SuppressWarnings("ALL")
@Service
public class DefaultHalfHourAccessRecordEmail implements  HalfHourAccessRecordEmail{
    private static final Logger logger = LoggerFactory.getLogger("halfHourAccessRecordService");
    private static final long SLEEP_TIME = 5000; //5s



    @Autowired
    private RecordSlaveDao recordSlaveDao;
    @Autowired
    private AccessSlaveDao accessSlaveDao;
    @Autowired
    private ChannelSuccessRateByClientidMapper channelSuccessRateByClientidMapper;
    @Autowired
    private ChannelSuccessRateRealtimeMapper channelSuccessRateRealtimeMapper;
    @Autowired
    private ClientSuccessRateRealtimeMapper clientSuccessRateRealtimeMapper;
    @Autowired
    @Qualifier("javaMailSenderAlarm")
    private JavaMailSender javaMailSenderAlarm;
    @Override
    public boolean doJob(TaskInfo taskInfo) {
        logger.info("【半小时access和record统计邮件通知任务】开始");
        long PERIOD_TIME = 300; //1800s
        try {
            PERIOD_TIME =Long.valueOf(PropertiesUtil.get("halfEmailPeriod"));
        } catch (Exception e) {
            PERIOD_TIME = 1800;
        }

        final DateTime now = new DateTime();
        long start = System.currentTimeMillis();

        String timeFormart = taskInfo.getExecuteType().getFormat();
        final DateTime executeNext = UcpaasDateUtils.parseDate(taskInfo.getExecuteNext(), timeFormart);
        logger.info("data time is {}",executeNext.toString("yyyy-MM-dd HH:mm:ss"));
        logger.info("execute(now) time is {}",now.toString("yyyy-MM-dd HH:mm:ss"));

        String m = PERIOD_TIME+"s执行一次";
        if((executeNext.getMillis()/1000%PERIOD_TIME)!=0){
            logger.info(m+"时间不满足，任务结束");
            return true;
        }else{
            logger.info(m+"时间满足，任务继续");
        }
        try {
            if (executeNext.getMinuteOfDay() == 0) { //需跑昨天整体的数据
                logger.info("零点执行，需统计昨天的数据另发一封邮件");
                doBussiness(now, executeNext.minusSeconds(1));
            }
            doBussiness(now, executeNext);
        }catch (Exception e){
            logger.error("【半小时access和record统计邮件通知任务】异常结束",e);
        }

        long end = System.currentTimeMillis();
        logger.info("【半小时access和record统计邮件通知任务】结束，统计时间= {}，统计耗时={}ms", executeNext.toString("yyyy-MM-dd HH:mm:ss"), (end - start));

        return true;

    }

    public boolean doBussiness(final DateTime now, final DateTime executeNext) {
        long start = System.currentTimeMillis();
        logger.info("【半小时access和record统计邮件通知任务】开始");

        /**********************************1. record流水表发送总量查数据(按用户区分) begin**********************************/
        Future<List<ChannelSuccessRateByClientid>> channelSuccessRateByClientidsFuture =ExecutorServiceCachePool.submit(new Callable<List<ChannelSuccessRateByClientid>>() {
            @Override
            public List<ChannelSuccessRateByClientid> call() throws Exception {
                logger.info("统计数据（开始）:record流水表发送总量查数据(按用户区分) ");
                long beginThread = System.currentTimeMillis();
                List<ChannelSuccessRateByClientid> allTableDataList = new ArrayList<ChannelSuccessRateByClientid>(); // 10张record表的统计数据
                final String date = executeNext.toString("yyyyMMdd");
                // 根据indentify和时间遍历统计十张流水表中的用户成功率数据
                final List<Future<List<ChannelSuccessRateByClientid>>> oneTableFutures = new ArrayList<>();
                for (int identify = 0; identify < 10; identify++) {
                    final int finalIdentify = identify;
                    oneTableFutures.add(ExecutorServiceCachePool.submit(new Callable<List<ChannelSuccessRateByClientid>>() {
                        @Override
                        public List<ChannelSuccessRateByClientid> call() throws Exception {
                            Map<String, Object> sqlParams = new HashMap<String, Object>();
                            sqlParams.put("identify", finalIdentify);
                            sqlParams.put("date", date);
                            sqlParams.put("boundData",executeNext.toDate());
                            long beginTable = System.currentTimeMillis();
                            List<ChannelSuccessRateByClientid> oneTableDataList = recordSlaveDao.queryAll("smsMonitor.getChannelSuccessRateByClientid", sqlParams);
                            logger.debug("ChannelSuccessRateByClientid统计表t_sms_record_*, params={}, cost {}ms", JacksonUtil.toJSON(sqlParams),System.currentTimeMillis()-beginTable);
                            for(ChannelSuccessRateByClientid csrr:oneTableDataList){
                                csrr.setDataTime(executeNext.toDate());
                                csrr.setCreateTime(now.toDate());
                            }
                            return oneTableDataList;
                        }
                    }));
                }

                for(Future<List<ChannelSuccessRateByClientid>> f:oneTableFutures){
                    allTableDataList.addAll(f.get());
                }


                if(allTableDataList.size() > 0){
                    Map delParams = new HashMap<>();
                    delParams.put("dataTime", executeNext.toDate());
                    // 老数据删除
                    long beginDelete = System.currentTimeMillis();
                    channelSuccessRateByClientidMapper.deleteByDataTime(delParams);
                    logger.debug("delete ChannelSuccessRateByClientid cost {}ms",System.currentTimeMillis()-beginDelete);
                    long beginInsert = System.currentTimeMillis();
                    channelSuccessRateByClientidMapper.insertBatch(allTableDataList);
                    logger.debug("insertBatch ChannelSuccessRateByClientid cost {}ms",System.currentTimeMillis()-beginInsert);
                }

                logger.info("record流水表发送总量查询结果(按用户区分) 入库完毕");




                String todayStr = executeNext.toString("yyyy-MM-dd")+" 00:00:00";
                final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date todayDate = sdf.parse(todayStr);
                Map channelSuccessRateRealtimeParams = new HashMap<>();
                channelSuccessRateRealtimeParams.put("dataTimeStart", todayDate);   //从今天00:00开始
                channelSuccessRateRealtimeParams.put("dataTimeEnd", executeNext.toDate());

                logger.info("获取一天截止到目前的 record流水表发送总量查数据(按用户区分)");
                long beginQuery = System.currentTimeMillis();
                List<ChannelSuccessRateByClientid> list = channelSuccessRateByClientidMapper.query(channelSuccessRateRealtimeParams);
                logger.debug("query ChannelSuccessRateByClientid cost {}ms",System.currentTimeMillis()-beginQuery);
                logger.info("统计数据（结束）:record流水表发送总量查数据(按用户区分)，用时{}ms ",System.currentTimeMillis()-beginThread);
                return list;
            }
        });


        /**********************************1. record流水表发送总量查数据(按用户区分) end**********************************/

        /**********************************2. 通道成功率统计数据(record按通道区分) begin**********************************/
        Future<List<ChannelSuccessRateRealtime>> channelSuccessRateRealtimesFuture = null;
        channelSuccessRateRealtimesFuture = ExecutorServiceCachePool.submit(new Callable<List<ChannelSuccessRateRealtime>>() {
            @Override
            public List<ChannelSuccessRateRealtime> call() throws Exception {
                logger.info("统计数据（开始）:通道成功率统计数据(record按通道区分) ");
                long beginThread = System.currentTimeMillis();
                String todayStr = executeNext.toString("yyyy-MM-dd")+" 00:00:00";
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date todayDate = sdf.parse(todayStr);
                Map channelSuccessRateRealtimeParams = new HashMap<>();
                channelSuccessRateRealtimeParams.put("dataTimeStart", todayDate);   //从今天00:00开始
                channelSuccessRateRealtimeParams.put("dataTimeEnd",executeNext.toDate());
                long beginQuery = System.currentTimeMillis();
                List<ChannelSuccessRateRealtime> channelSuccessRateRealtimes = channelSuccessRateRealtimeMapper.query(channelSuccessRateRealtimeParams);
                logger.debug("query ChannelSuccessRateRealtime cost {}ms",System.currentTimeMillis()-beginQuery);


                ChannelSuccessRateRealtime csrrLast = null;
                if(!channelSuccessRateRealtimes.isEmpty()) {
                    csrrLast = channelSuccessRateRealtimes.get(channelSuccessRateRealtimes.size() - 1);
                }
                logger.debug("通道成功率统计的数据，最新一条是{}",JacksonUtil.toJSON(csrrLast));
                if(csrrLast==null||csrrLast.getDataTime().getTime()!=executeNext.getMillis()){
                    logger.debug("通道成功率统计的最新一条数据(不是半小时统计要的数据)，最新一条是{}",JacksonUtil.toJSON(csrrLast));
                    logger.info("重新统计通道成功率");

                    List<Future<List<ChannelSuccessRateRealtime> >> oneTableDataListFutrues = new ArrayList<>();

                    for (int identify = 0; identify < 10; identify++) {
                        final int finalIdentify = identify;
                        final String date = executeNext.toString("yyyyMMdd");
                        oneTableDataListFutrues.add(ExecutorServiceCachePool.submit(new Callable<List<ChannelSuccessRateRealtime>>() {
                            @Override
                            public List<ChannelSuccessRateRealtime> call() throws Exception {
                                Map<String, Object> sqlParams = new HashMap<String, Object>();
                                sqlParams.put("identify", finalIdentify);
                                sqlParams.put("date", date);
                                sqlParams.put("boundData",executeNext.toDate());
                                long beginTable = System.currentTimeMillis();
                                List<ChannelSuccessRateRealtime> oneTableDataList = recordSlaveDao.queryAll("smsMonitor.getChannelSuccessRate", sqlParams);
                                logger.debug("ChannelSuccessRateRealtime统计表t_sms_record_*, params={}, cost {}ms",JacksonUtil.toJSON(sqlParams),System.currentTimeMillis()-beginTable);
                                for(ChannelSuccessRateRealtime csrr:oneTableDataList){
                                    csrr.setDataTime(executeNext.toDate());
                                    csrr.setCreateTime(now.toDate());
                                }
                                return oneTableDataList;
                            }
                        }));


                    }
                    for(Future<List<ChannelSuccessRateRealtime>> oneTableDataFuture:oneTableDataListFutrues){
                        channelSuccessRateRealtimes.addAll(oneTableDataFuture.get());
                    }

                }


                //只抽半个小时的数据
                Iterator<ChannelSuccessRateRealtime> csrrIt = channelSuccessRateRealtimes.iterator();
                while(csrrIt.hasNext()){
                    ChannelSuccessRateRealtime csrr =  csrrIt.next();
                    Date dataTime = csrr.getDataTime();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("mm");
                    int asInt = Integer.parseInt(dateFormat.format(dataTime));
                    //分钟在此区间，说明是整点数据
                    if (!((asInt >= 0 && asInt <= 4) || (asInt >= 30 && asInt <= 34) || (asInt >=56 && asInt <= 59))) {
                        csrrIt.remove();
                    }
                }




                logger.info("统计数据（结束）:通道成功率统计数据(record按通道区分) ，用时{}ms ",System.currentTimeMillis()-beginThread);
                return channelSuccessRateRealtimes;
            }
        });
        /**********************************2. 通道成功率统计数据(record按通道区分) end**********************************/

        /**********************************3.  客户成功率统计数据(access按用户区分) begin**********************************/
        Future<List<ClientSuccessRateRealtime>> clientSuccessRateRealtimesFuture = null;
        clientSuccessRateRealtimesFuture = ExecutorServiceCachePool.submit(new Callable<List<ClientSuccessRateRealtime>>() {
            @Override
            public List<ClientSuccessRateRealtime> call() throws Exception {
                logger.info("统计数据（开始）:客户成功率统计数据(access按用户区分) ");
                long beginThread = System.currentTimeMillis();
                String todayStr = executeNext.toString("yyyy-MM-dd")+" 00:00:00";
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date todayDate = sdf.parse(todayStr);
                Map clientSuccessRateRealtimeParams = new HashMap<>();
                clientSuccessRateRealtimeParams.put("dataTimeStart", todayDate);   //从今天00:
                clientSuccessRateRealtimeParams.put("dataTimeEnd",executeNext.toDate());
                long beginQuery = System.currentTimeMillis();
                List<ClientSuccessRateRealtime> clientSuccessRateRealtimes =  clientSuccessRateRealtimeMapper.query(clientSuccessRateRealtimeParams);
                logger.debug("query ClientSuccessRateRealtime cost {}ms",System.currentTimeMillis()-beginQuery);
                ClientSuccessRateRealtime csrrLast = null;
                if(!clientSuccessRateRealtimes.isEmpty()) {
                    csrrLast = clientSuccessRateRealtimes.get(clientSuccessRateRealtimes.size() - 1);
                }
                logger.debug("客户成功率统计数据，最新一条是{}",JacksonUtil.toJSON(csrrLast));
                if(csrrLast==null||csrrLast.getDataTime().getTime()!=executeNext.getMillis()){
                    logger.debug("客户成功率统计的最新一条数据(不是半小时统计要的数据)，最新一条是{}",JacksonUtil.toJSON(csrrLast));
                    logger.info("重新统计客户成功率");

                    List<Future<List<ClientSuccessRateRealtime> >> oneTableDataListFutrues = new ArrayList<>();

                    for (int identify = 0; identify < 10; identify++) {
                        final int finalIdentify = identify;
                        final String date = executeNext.toString("yyyyMMdd");
                        oneTableDataListFutrues.add(ExecutorServiceCachePool.submit(new Callable<List<ClientSuccessRateRealtime>>() {
                            @Override
                            public List<ClientSuccessRateRealtime> call() throws Exception {
                                Map<String, Object> sqlParams = new HashMap<String, Object>();
                                sqlParams.put("identify", finalIdentify);
                                sqlParams.put("date", date);
                                sqlParams.put("boundData",executeNext.toDate());
                                long beginTable = System.currentTimeMillis();
                                List<ClientSuccessRateRealtime> oneTableDataList = accessSlaveDao.queryAll("smsMonitor.getClientSuccessRate", sqlParams);
                                logger.debug("ClientSuccessRateRealtime统计表t_sms_access_*, params={}, cost {}ms",JacksonUtil.toJSON(sqlParams),System.currentTimeMillis()-beginTable);
                                for(ClientSuccessRateRealtime csrr:oneTableDataList){
                                    csrr.setDataTime(executeNext.toDate());
                                    csrr.setCreateTime(now.toDate());
                                }
                                return oneTableDataList;
                            }
                        }));

                    }
                    for(Future<List<ClientSuccessRateRealtime>> oneTableDataFuture:oneTableDataListFutrues){
                        List<ClientSuccessRateRealtime> successRateRealtimes = oneTableDataFuture.get();
                        clientSuccessRateRealtimes.addAll(successRateRealtimes);
                    }

                }


                //只抽半个小时的数据
                Iterator<ClientSuccessRateRealtime> csrrIt = clientSuccessRateRealtimes.iterator();
                while(csrrIt.hasNext()){
                    ClientSuccessRateRealtime csrr =  csrrIt.next();
                    Date dataTime = csrr.getDataTime();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("mm");
                    int asInt = Integer.parseInt(dateFormat.format(dataTime));
                    //分钟在此区间，说明是整点数据
                    if (!((asInt >= 0 && asInt <= 4) || (asInt >= 30 && asInt <= 34) || (asInt >=56 && asInt <= 59))) {
                        csrrIt.remove();
                    }
                }



                logger.info("统计数据（结束）:客户成功率统计数据(access按用户区分) ，用时{}ms ",System.currentTimeMillis()-beginThread);
                return clientSuccessRateRealtimes;
            }
        });
        /**********************************3.  客户成功率统计数据(access按用户区分) end**********************************/
        logger.info("数据获取分配任务完成");



        /************************************获取个线程的结果********************************/
        long beginChannelClient = System.currentTimeMillis();
        List<ChannelSuccessRateByClientid> channelSuccessRateByClientids = null;
        try {
            channelSuccessRateByClientids = channelSuccessRateByClientidsFuture.get();
        } catch (Exception e) {
            logger.error("record流水表发送总量查数据(按用户区分)",e);
        }
        logger.debug("ChannelSuccessRateByClientid future get result cost {}ms",System.currentTimeMillis()-beginChannelClient);

        long beginChannel = System.currentTimeMillis();
        List<ChannelSuccessRateRealtime> channelSuccessRateRealtimes = null;
        try {
            channelSuccessRateRealtimes = channelSuccessRateRealtimesFuture.get();
        }  catch (Exception e) {
            logger.error("通道成功率统计数据(record按通道区分) ",e);
        }
        logger.debug("ChannelSuccessRateRealtime future get result cost {}ms",System.currentTimeMillis()-beginChannel);

        long beginClient = System.currentTimeMillis();
        List<ClientSuccessRateRealtime> clientSuccessRateRealtimes = null;
        try {
            clientSuccessRateRealtimes = clientSuccessRateRealtimesFuture.get();
        }  catch (Exception e) {
            logger.error("通道成功率统计数据(record按通道区分) ",e);
        }

        logger.debug("ChannelSuccessRateRealtime future get result cost {}ms",System.currentTimeMillis()-beginClient);

        long beginFormatAndSend = System.currentTimeMillis();
        formatDataAndSend(executeNext,channelSuccessRateByClientids, channelSuccessRateRealtimes, clientSuccessRateRealtimes);
        logger.debug("formatDataAndSend cost {}ms",System.currentTimeMillis()-beginFormatAndSend);





        long end = System.currentTimeMillis();
        logger.info("【半小时access和record统计邮件通知任务】结束，统计时间= {}，统计耗时={}ms", executeNext.toString("yyyy-MM-dd HH:mm:ss"), (end - start));

        return true;
    }



    private void formatDataAndSend(DateTime executeNext,List<ChannelSuccessRateByClientid> channelSuccessRateByClientids, List<ChannelSuccessRateRealtime> channelSuccessRateRealtimes, List<ClientSuccessRateRealtime> clientSuccessRateRealtimes) {
        //数据获取完后开始组装
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //1 先组装access流水表发送总量查询结果(按用户区分)
        logger.info("1.先组装access流水表发送总量查询结果(按用户区分)");
        long begin1 = System.currentTimeMillis();
//		Map<Long,AccessAliHeJi> accessAliHeJiMap = new TreeMap<>();
//        Map<Long,AccessAllHeJi> accessAllHeJiMap = new TreeMap<>();
        Map<Long,AccessAliHeJi> accessAliHeJiMap = getSortMap();
        Map<Long,AccessAllHeJi> accessAllHeJiMap = getSortMap();
        for(ClientSuccessRateRealtime csrr: clientSuccessRateRealtimes){
            long key = csrr.getDataTime().getTime();
            if(isAliEfficient(csrr.getClientId())){
                AccessAliHeJi accessAliHeJi = accessAliHeJiMap.get(key);
                if(accessAliHeJi == null)
                    accessAliHeJi = createAccessAliHeJi();
                plusClientSuccessRateRealtimeValueTo1(csrr, accessAliHeJi);
                accessAliHeJiMap.put(key, accessAliHeJi);

            }

            AccessAllHeJi all = accessAllHeJiMap.get(key);
            if(all == null)
                all = createAccessAllHeJi();
            plusClientSuccessRateRealtimeValueTo1(csrr, all);
            accessAllHeJiMap.put(key, all);
        }
        //如果缺少某个时候点的数据，则补上全为0的数据
        accessAliHeJiMap = completeData(accessAliHeJiMap, 1);
        accessAllHeJiMap = completeData(accessAllHeJiMap, 2);
        List<ClientSuccessRateRealtime> table1Data = new ArrayList<>();
        for(Map.Entry<Long, AccessAliHeJi> entry:accessAliHeJiMap.entrySet()){
            table1Data.add(entry.getValue());
        }
        for(Map.Entry<Long, AccessAllHeJi> entry:accessAllHeJiMap.entrySet()){
            table1Data.add(entry.getValue());
        }
        logger.info("1.先组装access流水表发送总量查询结果(按用户区分)-完毕，生成table, 用时{}ms",System.currentTimeMillis()-begin1);
        String content1 = formTable(table1Data);

        logger.info("2.record流水表发送总量查询结果(按用户区分)");
        long begin2 = System.currentTimeMillis();
        //2 record流水表发送总量查询结果(按用户区分)[20170608]
        Map<Long,RecordByClientAliHeJi> recordByClientAliHeJiMap = getSortMap();
        Map<Long,RecordByClientAllHeJi> recordByClientAllHeJiMap = getSortMap();
        for(ChannelSuccessRateByClientid csrbc: channelSuccessRateByClientids){
            long key = csrbc.getDataTime().getTime();
            if(isAliEfficient(csrbc.getClientId())){
                RecordByClientAliHeJi rbca = recordByClientAliHeJiMap.get(key);
                if(rbca == null)
                    rbca =  new RecordByClientAliHeJi();
                plusChannelSuccessRateByClientidValueTo1(csrbc, rbca);
                rbca.setReceiveTotal(rbca.getSendTotal()+rbca.getSubmitFail());//接收access 1 2 3 4 5 6
                recordByClientAliHeJiMap.put(key, rbca);
            }

            RecordByClientAllHeJi all = recordByClientAllHeJiMap.get(key);
            if(all == null)
                all =  new RecordByClientAllHeJi();
//			all.setSendTotal(csrbc.getSendTotal()+all.getSendTotal());
//			all.setSuccessTotal(csrbc.getSuccessTotal()+all.getSuccessTotal());
//			all.setSubmitFail(csrbc.getSubmitFail()+all.getSubmitFail());
//			all.setSendFail(csrbc.getSendFail()+all.getSendFail());
//			all.setUndetermined1(csrbc.getUndetermined1()+all.getUndetermined1());
//			all.setUndetermined2(csrbc.getUndetermined2()+all.getUndetermined2());
//			all.setNosend(csrbc.getNosend()+all.getNosend());
//			BigDecimal sendTotal = new BigDecimal(all.getSendTotal());
//			BigDecimal successRate = new BigDecimal(all.getSuccessTotal()).divide(sendTotal);
//			all.setSuccessRate(successRate);
//			BigDecimal fakeSuccessRate = new BigDecimal(all.getUndetermined1()+all.getUndetermined2()).divide(sendTotal);
//			all.setFakeSuccessRate(fakeSuccessRate);
//			BigDecimal reallyFailRate = new BigDecimal(all.getSendFail()).divide(sendTotal);
//			all.setReallyFailRate(reallyFailRate);
//			all.setReceiveTotal(all.getSendTotal()+all.getSubmitFail());//接收access 1 2 3 4 5 6
            plusChannelSuccessRateByClientidValueTo1(csrbc, all);
            all.setReceiveTotal(all.getSendTotal()+all.getSubmitFail());//接收access 1 2 3 4 5 6
            recordByClientAllHeJiMap.put(key, all);
        }
//        如果缺少某个时候点的数据，则补上全为0的数据
        recordByClientAliHeJiMap = completeData(recordByClientAliHeJiMap,3);
        recordByClientAllHeJiMap = completeData(recordByClientAllHeJiMap,4);
        List<ChannelSuccessRateByClientid> table2Data = new ArrayList<>();
        for(Map.Entry<Long, RecordByClientAliHeJi> entry:recordByClientAliHeJiMap.entrySet()){
            table2Data.add(entry.getValue());
        }
        for(Map.Entry<Long, RecordByClientAllHeJi> entry:recordByClientAllHeJiMap.entrySet()){
            table2Data.add(entry.getValue());
        }
        logger.info("2.record流水表发送总量查询结果(按用户区分)-完毕，生成table, 用时{}ms",System.currentTimeMillis()-begin2);
        String content2 = formTable2(table2Data);
        //3 record流水表发送总量查询结果(按通道区分)[20170608]
        logger.info("3.record流水表发送总量查询结果(按通道区分)");
        long begin3 = System.currentTimeMillis();
        Map<Long,RecordAllHeJi> recordAllHeJiMap = getSortMap();
        for(ChannelSuccessRateRealtime csrr: channelSuccessRateRealtimes){
            long key = csrr.getDataTime().getTime();
            RecordAllHeJi csrrr=new RecordAllHeJi();
            RecordAllHeJi all = recordAllHeJiMap.get(key);
            if(all == null)
                all =  new RecordAllHeJi();
            BeanUtil.copyProperties(csrr,csrrr);
            plusChannelSuccessRateRealtimeValueTo(csrrr, all);
            recordAllHeJiMap.put(key, all);
        }
        //如果缺少某个时候点的数据，则补上全为0的数据
        recordAllHeJiMap = completeData(recordAllHeJiMap,5);
        List<ChannelSuccessRateRealtime> table3Data = new ArrayList<>();
        for(Map.Entry<Long, RecordAllHeJi> entry:recordAllHeJiMap.entrySet()){
            table3Data.add(entry.getValue());
        }
        logger.info("3.record流水表发送总量查询结果(按通道区分)-完毕，生成table, 用时{}ms",System.currentTimeMillis()-begin3);
        String content3 = formTable3(table3Data);

        //4. access流水表实时查询结果(按用户区分)[20170608]
        logger.info("4. access流水表实时查询结果(按用户区分)");
        long begin4 = System.currentTimeMillis();
        List<AccessAllHeJi> aliAccessList = new ArrayList<>();
        List<AccessAllHeJi> noaliAccessList = new ArrayList<>();
        List<AccessAllHeJi> newlastAccessList = new ArrayList<>();
        Map<String,List<ClientSuccessRateRealtime>> groupByClientId = new TreeMap<>();
        for(ClientSuccessRateRealtime csrr:clientSuccessRateRealtimes){
            String key = csrr.getClientId();
            List<ClientSuccessRateRealtime> list = groupByClientId.get(key);
            if(list == null)
                list = new ArrayList<ClientSuccessRateRealtime>();
            list.add(csrr);
            groupByClientId.put(key, list);


        }
        for(Map.Entry<String,List<ClientSuccessRateRealtime>> entry:groupByClientId.entrySet()){
            AccessAllHeJi acess =new AccessAllHeJi();
            //找最新一条
            //	ClientSuccessRateRealtime aceess=entry.getValue().get(entry.getValue().size()-1);
            int beforetotal=0;
            double dtimevalue=0;
            for (ClientSuccessRateRealtime c :entry.getValue()){
                dtimevalue= DateUtils.getMinutiefTwoDate(c.getDataTime(),entry.getValue().get(entry.getValue().size()-1).getDataTime());
                if(dtimevalue<31 && dtimevalue>=29){
                    beforetotal=c.getSendTotal();
                }
            }
            BeanUtil.copyProperties(entry.getValue().get(entry.getValue().size()-1),acess);
            acess.setBeSendTotal(beforetotal);
            //阿里账号显示在前面
            if(isAliEfficient(acess.getClientId())){
                aliAccessList.add(acess);
            }else {
                noaliAccessList.add(acess);
            }
        }
        //阿里排序
        Collections.sort(aliAccessList, new Comparator<AccessAllHeJi>() {
            @Override
            public int compare(AccessAllHeJi o1, AccessAllHeJi o2) {
                if(o1.getSendTotal()>o2.getSendTotal()){
                    return  -1;
                }else if(o1.getSendTotal()<o2.getSendTotal()){
                    return 1;
                }else {
                    return 0;
                }
            }
        });
        //非阿里排序
        Collections.sort(noaliAccessList, new Comparator<AccessAllHeJi>() {
            @Override
            public int compare(AccessAllHeJi o1, AccessAllHeJi o2) {
                if(o1.getSendTotal()>o2.getSendTotal()){
                    return  -1;
                }else if(o1.getSendTotal()<o2.getSendTotal()){
                    return 1;
                }else {
                    return 0;
                }
            }
        });
        newlastAccessList.addAll(aliAccessList);
        newlastAccessList.addAll(noaliAccessList);
        //合计计算
        AccessAllHeJi aliHejiClient = null;
        AccessAllHeJi allHejiClient = null;
        for(AccessAllHeJi csrr:newlastAccessList){
            if(allHejiClient == null)
                allHejiClient = new AccessAllHeJi();
            if(isAliEfficient(csrr.getClientId())){
                if(aliHejiClient == null){
                    aliHejiClient = new AccessAllHeJi();
                }
                plusClientSuccessRateRealtimeValueTo(csrr, aliHejiClient);
            }
            plusClientSuccessRateRealtimeValueTo(csrr, allHejiClient);
        }

        if(aliHejiClient != null){
            aliHejiClient.setClientId("阿里合计");
            aliHejiClient.setClientName(null);
            newlastAccessList.add(aliHejiClient);
        }
        if(allHejiClient != null){
            allHejiClient.setClientId("全部合计");
            allHejiClient.setClientName(null);
            newlastAccessList.add(allHejiClient);
        }
        logger.info("4. access流水表实时查询结果(按用户区分)-完毕，生成table, 用时{}ms",System.currentTimeMillis()-begin4);
        String content4 = formTable4(newlastAccessList);


        //5. record流水表实时查询结果(按用户区分)[20170608]
        logger.info("5. record流水表实时查询结果(按用户区分)");
        long begin5 = System.currentTimeMillis();
        List<RecordByClientVo> alilastRecordByClientList = new ArrayList<>();
        List<RecordByClientVo> noalilastRecordByClientList = new ArrayList<>();
        List<RecordByClientVo> newlastRecordByClientList = new ArrayList<>();
        Map<String,List<ChannelSuccessRateByClientid>> groupByClientId2 = new TreeMap<>();
        for(ChannelSuccessRateByClientid csrr:channelSuccessRateByClientids){
            String key = csrr.getClientId();
            List<ChannelSuccessRateByClientid> list = groupByClientId2.get(key);
            if(list == null)
                list = new ArrayList<ChannelSuccessRateByClientid>();
            list.add(csrr);
            groupByClientId2.put(key, list);


        }
        //找最新一条
        for(Map.Entry<String,List<ChannelSuccessRateByClientid>> entry:groupByClientId2.entrySet()){
            //计算半小时
            RecordByClientVo  recordByClientVo=new RecordByClientVo();
            int beforetotal=0;
            double dtimevalue=0;
            ChannelSuccessRateByClientid identity=entry.getValue().get(entry.getValue().size()-1);
//			for (int i = entry.getValue().size() - 2; i >= 0; i--) {
//				ChannelSuccessRateByClientid c=entry.getValue().get(i);
//				if(identity.getDataTime().compareTo(c.getDataTime())==0){
//					plusChannelSuccessRateByClientidValueTo(c,identity);
//				}
//			}
            ChannelSuccessRateByClientid  beforedata=new ChannelSuccessRateByClientid();
            for (ChannelSuccessRateByClientid c :entry.getValue()){
                //多个
                if(identity.getDataTime().compareTo(c.getDataTime())==0 && identity.getId()!=c.getId()){
                    plusChannelSuccessRateByClientidValueTo1(c,identity);
                }
                //多个30分钟之前数据
                dtimevalue=DateUtils.getMinutiefTwoDate(c.getDataTime(),entry.getValue().get(entry.getValue().size()-1).getDataTime());
                if(dtimevalue<34 && dtimevalue>26){
                    plusChannelSuccessRateByClientidValueTo1(c,beforedata);
                    beforetotal=beforedata.getSendTotal();
                }
            }
            BeanUtil.copyProperties(identity,recordByClientVo);
            recordByClientVo.setBeSendTotal(beforetotal);
            if(isAliEfficient(recordByClientVo.getClientId())){
                alilastRecordByClientList.add(recordByClientVo);
            }else {
                noalilastRecordByClientList.add(recordByClientVo);
            }
            //	newlastRecordByClientList.add(recordByClientVo);
        }
        //排序
        Collections.sort(alilastRecordByClientList, new Comparator<RecordByClientVo>() {
            @Override
            public int compare(RecordByClientVo o1, RecordByClientVo o2) {
                if(o1.getSendTotal()>o2.getSendTotal()){
                    return -1;
                }else if(o1.getSendTotal()<o2.getSendTotal()){
                    return 1;
                }else {
                    return 0;
                }
            }
        });
        Collections.sort(noalilastRecordByClientList, new Comparator<RecordByClientVo>() {
            @Override
            public int compare(RecordByClientVo o1, RecordByClientVo o2) {
                if(o1.getSendTotal()>o2.getSendTotal()){
                    return -1;
                }else if(o1.getSendTotal()<o2.getSendTotal()){
                    return 1;
                }else {
                    return 0;
                }
            }
        });
        newlastRecordByClientList.addAll(alilastRecordByClientList);
        newlastRecordByClientList.addAll(noalilastRecordByClientList);
        //合计计算
        RecordByClientAliHeJi aliHejiClient2 = null;
        RecordByClientAllHeJi allHejiClient2 = null;
        for(RecordByClientVo csrr:newlastRecordByClientList){
            if(allHejiClient2==null)
                allHejiClient2 = new RecordByClientAllHeJi();
            if(isAliEfficient(csrr.getClientId())){
                if(aliHejiClient2==null)
                    aliHejiClient2 = new RecordByClientAliHeJi();
                plusChannelSuccessRateByClientidValueTo2(csrr, aliHejiClient2);
                aliHejiClient2.setReceiveTotal(aliHejiClient2.getSendTotal()+aliHejiClient2.getSubmitFail());//接收access 1 2 3 4 5 6
            }
            plusChannelSuccessRateByClientidValueTo(csrr, allHejiClient2);
            allHejiClient2.setReceiveTotal(allHejiClient2.getSendTotal()+allHejiClient2.getSubmitFail());//接收access 1 2 3 4 5 6
        }
        if(aliHejiClient2 != null)
            newlastRecordByClientList.add(aliHejiClient2);
        if(allHejiClient2 != null)
            newlastRecordByClientList.add(allHejiClient2);
        logger.info("5. record流水表实时查询结果(按用户区分)-完毕，生成table, 用时{}ms",System.currentTimeMillis()-begin5);
        String content5 = formTable5(newlastRecordByClientList);

        //6. record流水表实时查询结果(按通道区分)[20170608]
        logger.info("6. record流水表实时查询结果(按通道区分)");
        long begin6 = System.currentTimeMillis();
        List<ChannelSuccessRateRealtime> lastRecordList = new ArrayList<>();
        List<RecordAllHeJi> newlastRecordList = new ArrayList<>();
        Map<String,List<ChannelSuccessRateRealtime>> groupByChannelId = new TreeMap<>();
        RecordAllHeJi allHejiChannel = null;
        for(ChannelSuccessRateRealtime csrr:channelSuccessRateRealtimes){
            String key = csrr.getChannelId();
            List<ChannelSuccessRateRealtime> list = groupByChannelId.get(key);
            if(list == null)
                list = new ArrayList<ChannelSuccessRateRealtime>();
            list.add(csrr);
            groupByChannelId.put(key, list);

        }
        //找最新一条
        for(Map.Entry<String,List<ChannelSuccessRateRealtime>> entry:groupByChannelId.entrySet()){
            RecordAllHeJi allHejiChannel1 = new RecordAllHeJi();
            //	lastRecordList.add(entry.getValue().get(entry.getValue().size()-1));
            //计算半小时发送总量
            int beforetotal=0;
            double dtimevalue=0;
            for (ChannelSuccessRateRealtime c :entry.getValue()){
                dtimevalue=DateUtils.getMinutiefTwoDate(c.getDataTime(),entry.getValue().get(entry.getValue().size()-1).getDataTime());
                if(dtimevalue<34 && dtimevalue>26){
                    beforetotal=c.getSendTotal();
                }
            }
            BeanUtil.copyProperties(entry.getValue().get(entry.getValue().size()-1),allHejiChannel1);
            allHejiChannel1.setBeSendTotal(beforetotal);
//			allHejiChannel1.setReallyFailRate(allHejiChannel1.getReallyFailRate().multiply(new BigDecimal("100")));
//
//			allHejiChannel1.setFakeSuccessRate(allHejiChannel1.getFakeSuccessRate().multiply(new BigDecimal("100")));
//			allHejiChannel1.setSuccessRate(allHejiChannel1.getSuccessRate().multiply(new BigDecimal("100")));
//			logger.error("循环结束是否百分比 {}---------------------> ",allHejiChannel1.getSuccessRate(),allHejiChannel1.getSuccessRate().setScale(2, BigDecimal.ROUND_HALF_UP));
            newlastRecordList.add(allHejiChannel1);
        }
        Collections.sort(newlastRecordList, new Comparator<RecordAllHeJi>() {
            public int compare(RecordAllHeJi r1, RecordAllHeJi r2){
                if(r1.getSendTotal()>r2.getSendTotal()){
                    return -1;
                }else if(r1.getSendTotal()<r2.getSendTotal()){
                    return 1;
                }else{
                    return 0;
                }
            }
        });
        //合计计算
        for(RecordAllHeJi csrr:newlastRecordList){
            if(allHejiChannel==null)
                allHejiChannel = new RecordAllHeJi();
//			logger.error("合計之前是否是百分比 {}---------------------> ",csrr.getSuccessRate(),csrr.getSuccessRate().setScale(2, BigDecimal.ROUND_HALF_UP));
            plusChannelSuccessRateRealtimeValueTo(csrr, allHejiChannel);
//			logger.error("合計之後是否是百分比 {}---------------------> ",csrr.getSuccessRate(),csrr.getSuccessRate().setScale(2, BigDecimal.ROUND_HALF_UP));
        }
        if(allHejiChannel!=null)
            newlastRecordList.add(allHejiChannel);
        logger.info("6. record流水表实时查询结果(按通道区分)-完毕，生成table, 用时{}ms",System.currentTimeMillis()-begin6);
        String content6 = formTable6(newlastRecordList);


        long beginEmail= System.currentTimeMillis();
        final StringBuilder body = new StringBuilder();
        body.append(content1);
        body.append("<br/>");
        body.append(content2);
        body.append("<br/>");
        body.append(content3);
        body.append("<br/>");
        body.append(content4);
        body.append("<br/>");
        body.append(content5);
        body.append("<br/>");
        body.append(content6);
        String halfEmailReceivers = "";
        try {
            halfEmailReceivers = PropertiesUtil.get("halfEmailReceivers");
        } catch (IOException e1) {
            logger.error("halfEmailReceivers配置错误,使用默认邮箱huangwenjie@ucpaas.com",e1);
            halfEmailReceivers = "huangwenjie@ucpaas.com";
        }

        String[] receivers = halfEmailReceivers.split(",");

		/*for(final String receiver:receivers){
			new Thread(new Runnable() {
				@Override
				public void run() {
					try{
						MimeMessage msg = javaMailSenderAlarm.createMimeMessage();
						MimeMessageHelper helper = new MimeMessageHelper(msg, false, "utf-8");
						helper.setFrom("alarm@ucpaas.com");
						helper.setTo(receiver);
						helper.setSubject("新短信平台["+sdf.format(new Date())+"]流水表统计结果(告警值：成功率<"+getSuccessRateValue()+"%/未知率>"+getFakeRateValue()+"%/失败率>"+getFailRateValue()+"%)");
						helper.setText(body.toString(), true);
						javaMailSenderAlarm.send(msg);
						logger.info("发送成功邮件成功,receiver={}",receiver);
					}catch (Exception e) {
						logger.error("发送邮件失败,receiver="+receiver, e);
					}

				}
			}).start();
		}*/
        try{
            InternetAddress[] addresses = new InternetAddress[receivers.length];
            for (int i = 0;i<receivers.length;i++) {
                addresses[i] = new InternetAddress(receivers[i]);
            }
            MimeMessage msg = javaMailSenderAlarm.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "utf-8");
            helper.setFrom("alarm@ucpaas.com");
            helper.setSubject("新短信平台["+sdf.format(executeNext.toDate())+"]流水表统计结果(告警值：成功率<"+getSuccessRateValue()+"%/未知率>"+getFakeRateValue()+"%/失败率>"+getFailRateValue()+"%)");
            helper.setText(body.toString(), true);
            helper.setTo(addresses);
            try {
                //第一次发送
                javaMailSenderAlarm.send(msg);
            } catch (MailSendException e2) {
                ArrayList<InternetAddress> internetAddresses = new ArrayList<>(Arrays.asList(addresses));
                Map<Object, Exception> failedMessages = e2.getFailedMessages();
                Iterator<Map.Entry<Object, Exception>> iterator = failedMessages.entrySet().iterator();
                for (Map.Entry<Object, Exception> objectExceptionEntry : failedMessages.entrySet()) {
                    Exception value = objectExceptionEntry.getValue();
                    removeFailEmailByException(value,internetAddresses);
                    //去掉错误的地址重新发送
                    InternetAddress [] fixAddress = new InternetAddress[internetAddresses.size()];
                    for (int i = 0;i<internetAddresses.size();i++) {
                        InternetAddress internetAddress = internetAddresses.get(i);
                        fixAddress[i] = internetAddress;
                    }
                    helper.setTo(fixAddress);
                    //失败重发
                    javaMailSenderAlarm.send(msg);
                }
            }
            logger.info("发送成功邮件成功,receiver={}，用时{}ms",receivers,System.currentTimeMillis()-beginEmail);
        }catch (Exception e) {
            logger.error("发送邮件失败,receiver={}", e);
        }
    }

    private AccessAllHeJi createAccessAllHeJi() {
        return new AccessAllHeJi();
    }
    private AccessAliHeJi createAccessAliHeJi() {
        return new AccessAliHeJi();
    }
    private Double getSuccessRateValue(){
        Double successRateValue;
        try {
            successRateValue=Double.valueOf(PropertiesUtil.get("halfEmail_SuccessRateValue"));
        } catch (IOException e) {
            logger.error("成功率权重配置错误,使用默认值80",e);
            successRateValue= Double.valueOf(80);//默认值
        }
        return successRateValue;
    }
    private Double getFakeRateValue(){
        Double fakeRateValue;
        try {
            fakeRateValue=Double.valueOf(PropertiesUtil.get("halfEmail_FakeRateValue"));
        } catch (IOException e) {
            logger.error("未知率权重配置错误,使用默认值5",e);
            fakeRateValue= Double.valueOf(5);//默认值
        }
        return fakeRateValue;
    }
    private Double getFailRateValue(){
        Double failRateValue;
        try {
            failRateValue=Double.valueOf(PropertiesUtil.get("halfEmail_FailRateValue"));
        } catch (IOException e) {
            logger.error("失败率权重配置错误,使用默认值8",e);
            failRateValue= Double.valueOf(8);//默认值
        }
        return failRateValue;
    }
    @Deprecated
    private boolean isAli(String clientId) {
        if(StringUtils.isEmpty(clientId)){
            return false;
        }
        String[] aliClients = {"a00101","a00102","a00103","a00104","a00105","a00106"};
        try {
            String aliclientids = PropertiesUtil.get("aliclientids");
            aliClients = aliclientids.split(",");
        } catch (IOException e) {
            logger.error("阿里clientid配置错误",e);
        }
        for(String aliClient:aliClients){
            if(aliClient.equals(clientId))
                return true;
        }
        return false;
    }
    private boolean isAliEfficient(String clientId) {
        if(StringUtils.isEmpty(clientId)){
            return false;
        }
        String[] aliClients = {"a00101","a00102","a00103","a00104","a00105","a00106"};
        String aliclientids = ConfigUtils.ali_clients;
        if(StringUtil.isNotEmpty(aliclientids))
            aliClients = aliclientids.split(",");
        for(String aliClient:aliClients){
            if(aliClient.equals(clientId))
                return true;
        }
        return false;
    }

    private static final String TO_BE_REPALCE = "to_be_replace";

    //access流水表发送总量查询结果(按用户区分)[20170608]
    private String formTable(List<ClientSuccessRateRealtime> table1Data) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
        StringBuilder table = new StringBuilder("<h2>access流水表发送总量查询结果(按用户区分)[");
        table.append(TO_BE_REPALCE);
        table.append("]</h2>");
        Date dateTime = null;

        table.append("<table border=\"1\" cellspacing=\"0\" style=\"border-collapse:collapse\"><tbody><tr><th style=\"background-color:#FFE66F\"></th><th style=\"background-color:#FFE66F\">统计时间</th><th style=\"background-color:#FFE66F\">客户发送总数</th><th style=\"background-color:#FFE66F\">半小时总数</th><th style=\"background-color:#FFE66F\">明确成功 3</th><th style=\"background-color:#FFE66F\">成功待定 1</th><th style=\"background-color:#FFE66F\">计费条数 1+3+4+6</th><th style=\"background-color:#FFE66F\">计费条数 10</th><th style=\"background-color:#FFE66F\">明确失败 6</th><th style=\"background-color:#FFE66F\">审核不通过 7</th><th style=\"background-color:#FFE66F\">提交失败 5</th><th style=\"background-color:#FFE66F\">拦截条数 8 9 10</th><th style=\"background-color:#FFE66F\">未发送 0</th><th style=\"background-color:#FFE66F\">提交SEND失败 4</th><th style=\"background-color:#FFE66F\">发送到SEND总数 1 3 6</th><th width=\"60px\" style=\"background-color:#FFE66F\">成功率</th><th width=\"60px\" style=\"background-color:#FFE66F\">未知率</th><th width=\"60px\" style=\"background-color:#FFE66F\">失败率</th><th width=\"60px\" style=\"background-color:#FFE66F\">拦截率</th></tr>");
        int sendTotal = 0;
        boolean reset = false;
        for(ClientSuccessRateRealtime csrr:table1Data){
            String color = "";
            if(csrr instanceof AccessAliHeJi){
                color="color:red";
            }
            else{
                if(!reset){
                    reset = true;
                    sendTotal= 0;
                }
                color="color:blue";
            }
            if(dateTime==null)
                dateTime = csrr.getDataTime();
            table.append("<tr><td style=\"font-size:15px;").append(color).append("\">");
            if(csrr instanceof AccessAliHeJi)
                table.append("阿里合计");
            else
                table.append("全部合计");
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            //如果时间分钟不为半整点，则设置为整点
            String format = sdf.format(csrr.getDataTime());
            String time = formatTimeForZero(format);
            table.append(time);
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSendTotal());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSendTotal()-sendTotal);
            sendTotal=csrr.getSendTotal();
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getReallySuccessTotal());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getFakeSuccessFail());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getCharge1());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getCharge2());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getReallyFailTotal());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getAuditFailTotal());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSubmitFailTotal());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getInterceptTotal());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getNosend());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSendFailToatl());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSendAll());
            if((csrr.getSuccessRate()).compareTo(new BigDecimal(this.getSuccessRateValue()))==-1){
                table.append("</td><td style=\"font-size:15px;").append("background-color:red;color:#ffffff").append("\">");
            }else {
                table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            }
            table.append(csrr.getSuccessRate().setScale(2, BigDecimal.ROUND_HALF_UP)).append("%");
            if((csrr.getFakeSuccessRate()).compareTo(new BigDecimal(this.getFakeRateValue()))==1){
                table.append("</td><td style=\"font-size:15px;").append("background-color:#FF580A;color:#ffffff").append("\">");
            }else {
                table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            }
            table.append(csrr.getFakeSuccessRate().setScale(2, BigDecimal.ROUND_HALF_UP)).append("%");
            if((csrr.getReallyFailRate()).compareTo(new BigDecimal(this.getFailRateValue()))==1){
                table.append("</td><td style=\"font-size:15px;").append("background-color:#FF42FF;color:#ffffff").append("\">");
            }else {
                table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            }
            table.append(csrr.getReallyFailRate().setScale(2, BigDecimal.ROUND_HALF_UP)).append("%");
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(getInterceptPercent(csrr));
            table.append("</td></tr>");
        }
        table.append("</table>");
        if(dateTime==null)
            dateTime = new Date();
        return table.toString().replace(TO_BE_REPALCE, sdf2.format(dateTime));
    }

    private String getInterceptPercent(ClientSuccessRateRealtime csrr) {

        BigDecimal sendT = new BigDecimal(csrr.getSendTotal());
        if(csrr.getSendTotal()==0)
            sendT= new BigDecimal("1");
        return new BigDecimal((csrr.getSubmitFailTotal()+csrr.getAuditFailTotal()+csrr.getInterceptTotal())).divide(sendT,6, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).toString()+"%";
    }

    private String formTable2(List<ChannelSuccessRateByClientid> table2Data) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
        StringBuilder table = new StringBuilder("<h2>record流水表发送总量查询结果(按用户区分)[");
        table.append(TO_BE_REPALCE);
        table.append("]</h2>");
        Date dateTime = null;
        table.append("<table border=\"1\" cellspacing=\"0\" style=\"border-collapse:collapse\"><tbody><tr><th style=\"background-color:#FFE66F\"></th><th style=\"background-color:#FFE66F\">统计时间</th><th style=\"background-color:#FFE66F\">接收access 1 2 3 4 5 6</th><th style=\"background-color:#FFE66F\">半小时总数</th><th style=\"background-color:#FFE66F\">发送通道 1 2 3 5 6</th><th style=\"background-color:#FFE66F\">明确成功 3</th><th style=\"background-color:#FFE66F\">提交失败 4</th><th style=\"background-color:#FFE66F\">发送失败 5 6</th><th style=\"background-color:#FFE66F\">成功待定 1</th><th style=\"background-color:#FFE66F\">成功待定 2</th><th style=\"background-color:#FFE66F\">未发送 0</th><th width=\"60px\" style=\"background-color:#FFE66F\">成功率</th><th width=\"60px\" style=\"background-color:#FFE66F\">未知率</th><th width=\"60px\" style=\"background-color:#FFE66F\">失败率</th></tr>");
        int lastTotal = 0;
        boolean reset = false;
        for(ChannelSuccessRateByClientid csrr:table2Data){
            String color = "";
            if(csrr instanceof RecordByClientAliHeJi){
                color="color:red";
            }else{
                if(!reset){
                    reset = true;
                    lastTotal=0;
                }
                color="color:blue";
            }
            if(dateTime==null)
                dateTime = csrr.getDataTime();
            table.append("<tr><td style=\"font-size:15px;").append(color).append("\">");;
            if(csrr instanceof RecordByClientAliHeJi)
                table.append("阿里合计");
            else
                table.append("全部合计");
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            //如果时间分钟不为半整点，则设置为整点
            String format = sdf.format(csrr.getDataTime());
            String time = formatTimeForZero(format);
            table.append(time);
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSendTotal()+csrr.getSubmitFail());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSendTotal()+csrr.getSubmitFail()-lastTotal);
            lastTotal = csrr.getSendTotal()+csrr.getSubmitFail();
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSendTotal());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSuccessTotal());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSubmitFail());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSendFail());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getUndetermined1());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getUndetermined2());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getNosend());
		/*	table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
			table.append(csrr.getSuccessRate().multiply(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_UP)).append("%");
			table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
			table.append(csrr.getFakeSuccessRate().multiply(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_UP)).append("%");
			table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
			table.append(csrr.getReallyFailRate().multiply(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_UP)).append("%");*/
            if((csrr.getSuccessRate()).compareTo(new BigDecimal(this.getSuccessRateValue()))==-1){
                table.append("</td><td style=\"font-size:15px;").append("background-color:red;color:#ffffff").append("\">");
            }else {
                table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            }
            table.append(csrr.getSuccessRate().setScale(2, BigDecimal.ROUND_HALF_UP)).append("%");
            if((csrr.getFakeSuccessRate()).compareTo(new BigDecimal(this.getFakeRateValue()))==1){
                table.append("</td><td style=\"font-size:15px;").append("background-color:#FF580A;color:#ffffff").append("\">");
            }else {
                table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            }
            table.append(csrr.getFakeSuccessRate().setScale(2, BigDecimal.ROUND_HALF_UP)).append("%");
            if((csrr.getReallyFailRate()).compareTo(new BigDecimal(this.getFailRateValue()))==1){
                table.append("</td><td style=\"font-size:15px;").append("background-color:#FF42FF;color:#ffffff").append("\">");
            }else {
                table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            }
            table.append(csrr.getReallyFailRate().setScale(2, BigDecimal.ROUND_HALF_UP)).append("%");
            table.append("</td></tr>");
        }
        table.append("</table>");
        if(dateTime==null)
            dateTime = new Date();
        return table.toString().replace(TO_BE_REPALCE, sdf2.format(dateTime));
    }

    private String formTable3(List<ChannelSuccessRateRealtime> table3Data) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
        StringBuilder table = new StringBuilder("<h2>record流水表发送总量查询结果(按通道区分)[");
        table.append(TO_BE_REPALCE);
        table.append("]</h2>");
        Date dateTime = null;
        table.append("<table border=\"1\" cellspacing=\"0\" style=\"border-collapse:collapse\"><tbody><tr><th style=\"background-color:#FFE66F\"></th><th style=\"background-color:#FFE66F\">统计时间</th><th style=\"background-color:#FFE66F\">发送通道 1 2 3 5 6</th><th style=\"background-color:#FFE66F\">半小时总数</th><th style=\"background-color:#FFE66F\">明确成功 3</th><th style=\"background-color:#FFE66F\">提交失败 4</th><th style=\"background-color:#FFE66F\">发送失败 5 6</th><th style=\"background-color:#FFE66F\">成功待定 1</th><th style=\"background-color:#FFE66F\">成功待定 2</th><th style=\"background-color:#FFE66F\">未发送 0</th><th width=\"60px\" style=\"background-color:#FFE66F\">成功率</th><th width=\"60px\" style=\"background-color:#FFE66F\">未知率</th><th width=\"60px\" style=\"background-color:#FFE66F\">失败率</th></tr><tr>");
        int count = 0;
        int lastTotal = 0;
        for(ChannelSuccessRateRealtime csrr:table3Data){
            count++;
            String color = "";
            if(count==table3Data.size())
                color="color:blue";
            if(dateTime==null)
                dateTime = csrr.getDataTime();
            table.append("<tr><td style=\"font-size:15px;").append(color).append("\">");
            table.append("全部合计");
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            //如果时间分钟不为半整点，则设置为整点
            String format = sdf.format(csrr.getDataTime());
            String time = formatTimeForZero(format);
            table.append(time);
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSendTotal());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSendTotal()-lastTotal);
            lastTotal = csrr.getSendTotal();
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSuccessTotal());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSubmitFail());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSendFail());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getUndetermined1());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getUndetermined2());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getNosend());
            if((csrr.getSuccessRate()).compareTo(new BigDecimal(this.getSuccessRateValue()))==-1){
                table.append("</td><td style=\"font-size:15px;").append("background-color:red;color:#ffffff").append("\">");
            }else {
                table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            }
            table.append(csrr.getSuccessRate().setScale(2, BigDecimal.ROUND_HALF_UP)).append("%");
            if((csrr.getFakeSuccessRate()).compareTo(new BigDecimal(this.getFakeRateValue()))==1){
                table.append("</td><td style=\"font-size:15px;").append("background-color:#FF580A;color:#ffffff").append("\">");
            }else {
                table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            }
            table.append(csrr.getFakeSuccessRate().setScale(2, BigDecimal.ROUND_HALF_UP)).append("%");
            if((csrr.getReallyFailRate()).compareTo(new BigDecimal(this.getFailRateValue()))==1){
                table.append("</td><td style=\"font-size:15px;").append("background-color:#FF42FF;color:#ffffff").append("\">");
            }else {
                table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            }
            table.append(csrr.getReallyFailRate().setScale(2, BigDecimal.ROUND_HALF_UP)).append("%");
            table.append("</td></tr>");
        }
        table.append("</table>");
        if(dateTime==null)
            dateTime = new Date();
        return table.toString().replace(TO_BE_REPALCE, sdf2.format(dateTime));
    }

    private String formTable4(List<AccessAllHeJi> lastAccessList) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
        StringBuilder table = new StringBuilder("<h2> access流水表实时查询结果(按用户区分)[");
        table.append(TO_BE_REPALCE);
        table.append("]</h2>");
        Date dateTime = null;
        table.append("<table border=\"1\" cellspacing=\"0\" style=\"border-collapse:collapse\"><tbody><tr><th style=\"background-color:#FFE66F\">ACCESS用户帐号</th><th style=\"background-color:#FFE66F\">统计时间</th><th style=\"background-color:#FFE66F\">客户发送总数</th><th style=\"background-color:#FFE66F\">半小时发送总数</th><th style=\"background-color:#FFE66F\">明确成功 3</th><th style=\"background-color:#FFE66F\">成功待定 1</th><th style=\"background-color:#FFE66F\">计费条数 1+3+4+6</th><th style=\"background-color:#FFE66F\">计费条数 10</th><th style=\"background-color:#FFE66F\">明确失败 6</th><th style=\"background-color:#FFE66F\">审核不通过 7</th><th style=\"background-color:#FFE66F\">提交失败 5</th><th style=\"background-color:#FFE66F\">拦截条数 8 9 10</th><th style=\"background-color:#FFE66F\">未发送 0</th><th style=\"background-color:#FFE66F\">提交SEND失败 4</th><th style=\"background-color:#FFE66F\">发送到SEND总数 1 3 6</th><th width=\"60px\" style=\"background-color:#FFE66F\">成功率</th><th width=\"60px\" style=\"background-color:#FFE66F\">未知率</th><th width=\"60px\" style=\"background-color:#FFE66F\">失败率</th><th width=\"60px\" style=\"background-color:#FFE66F\">拦截率</th></tr>");
        for(AccessAllHeJi csrr:lastAccessList){
            String color = "";
            if(isAliEfficient(csrr.getClientId()))
                color = "background-color:#B8B8DC";
            if("阿里合计".equals(csrr.getClientId()))
                color = "color:red";
            if("全部合计".equals(csrr.getClientId()))
                color = "color:blue";
            if(dateTime==null)
                dateTime = csrr.getDataTime();
            table.append("<tr><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getClientId());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            //如果时间分钟不为半整点，则设置为整点
            String format = sdf.format(csrr.getDataTime());
            String time = formatTimeForZero(format);
            table.append(time);
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSendTotal());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSendTotal()-csrr.getBeSendTotal());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getReallySuccessTotal());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getFakeSuccessFail());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getCharge1());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getCharge2());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getReallyFailTotal());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getAuditFailTotal());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSubmitFailTotal());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getInterceptTotal());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getNosend());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSendFailToatl());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSendAll());
            if((csrr.getSuccessRate()).compareTo(new BigDecimal(this.getSuccessRateValue()))==-1){
                table.append("</td><td style=\"font-size:15px;").append("background-color:red;color:#ffffff").append("\">");
            }else {
                table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            }//.multiply(new BigDecimal("100"))
            table.append(csrr.getSuccessRate().setScale(2, BigDecimal.ROUND_HALF_UP)).append("%");
            if((csrr.getFakeSuccessRate()).compareTo(new BigDecimal(this.getFakeRateValue()))==1){
                table.append("</td><td style=\"font-size:15px;").append("background-color:#FF580A;color:#ffffff").append("\">");
            }else {
                table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            }
            table.append(csrr.getFakeSuccessRate().setScale(2, BigDecimal.ROUND_HALF_UP)).append("%");
            if((csrr.getReallyFailRate()).compareTo(new BigDecimal(this.getFailRateValue()))==1){
                table.append("</td><td style=\"font-size:15px;").append("background-color:#FF42FF;color:#ffffff").append("\">");
            }else {
                table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            }
            table.append(csrr.getReallyFailRate().setScale(2, BigDecimal.ROUND_HALF_UP)).append("%");
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");

            table.append(getInterceptPercent(csrr));
            table.append("</td></tr>");
        }
        table.append("</table>");
        if(dateTime==null)
            dateTime = new Date();
        return table.toString().replace(TO_BE_REPALCE, sdf2.format(dateTime));
    }

    private String formTable5(List<RecordByClientVo> newlastRecordByClientList) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
        StringBuilder table = new StringBuilder("<h2>record流水表实时查询结果(按用户区分)[");
        table.append(TO_BE_REPALCE);
        table.append("]</h2>");
        Date dateTime = null;
        table.append("<table border=\"1\" cellspacing=\"0\" style=\"border-collapse:collapse\"><tbody><tr><th style=\"background-color:#FFE66F\">record-用户帐号</th><th style=\"background-color:#FFE66F\">统计时间</th><th style=\"background-color:#FFE66F\">接收access 1 2 3 4 5 6</th><th style=\"background-color:#FFE66F\">半小时接收量</th><th style=\"background-color:#FFE66F\">发送通道 1 2 3 5 6</th><th style=\"background-color:#FFE66F\">明确成功 3</th><th style=\"background-color:#FFE66F\">提交失败 4</th><th style=\"background-color:#FFE66F\">发送失败 5 6</th><th style=\"background-color:#FFE66F\">成功待定 1</th><th style=\"background-color:#FFE66F\">成功待定 2</th><th style=\"background-color:#FFE66F\">未发送 0</th><th width=\"60px\" style=\"background-color:#FFE66F\">成功率</th><th width=\"60px\" style=\"background-color:#FFE66F\">未知率</th><th width=\"60px\" style=\"background-color:#FFE66F\">失败率</th></tr><tr>");
        for(RecordByClientVo csrr:newlastRecordByClientList){
            String color = "";

            if(csrr instanceof RecordByClientAliHeJi){
                color="color:red";
            }else if(csrr instanceof RecordByClientAllHeJi){
                color="color:blue";
            }else if(isAliEfficient(csrr.getClientId())){
                color="background-color:#B8B8DC";
            }
            if(dateTime==null)
                dateTime = csrr.getDataTime();
            table.append("<tr><td style=\"font-size:15px;").append(color).append("\">");
            if(csrr instanceof RecordByClientAliHeJi){
                table.append("阿里合计");
            }else if(csrr instanceof RecordByClientAllHeJi){
                table.append("全部合计");
            }else{
                table.append(csrr.getClientId());
            }
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            //如果时间分钟不为半整点，则设置为整点
            String format = sdf.format(csrr.getDataTime());
            String time = formatTimeForZero(format);
            table.append(time);
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSendTotal()+csrr.getSubmitFail());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSendTotal()-csrr.getBeSendTotal());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSendTotal());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSuccessTotal());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSubmitFail());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSendFail());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getUndetermined1());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getUndetermined2());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getNosend());
//			table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
//			table.append(csrr.getSuccessRate().multiply(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_UP)).append("%");
//			table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
//			table.append(csrr.getFakeSuccessRate().multiply(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_UP)).append("%");
//			table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
//			table.append(csrr.getReallyFailRate().multiply(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_UP)).append("%");
            if((csrr.getSuccessRate()).compareTo(new BigDecimal(this.getSuccessRateValue()))==-1){
                table.append("</td><td style=\"font-size:15px;").append("background-color:red;color:#ffffff").append("\">");
            }else {
                table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            }
            table.append(csrr.getSuccessRate().setScale(2, BigDecimal.ROUND_HALF_UP)).append("%");
            if((csrr.getFakeSuccessRate()).compareTo(new BigDecimal(this.getFakeRateValue()))==1){
                table.append("</td><td style=\"font-size:15px;").append("background-color:#FF580A;color:#ffffff").append("\">");
            }else {
                table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            }
            table.append(csrr.getFakeSuccessRate().setScale(2, BigDecimal.ROUND_HALF_UP)).append("%");
            if((csrr.getReallyFailRate()).compareTo(new BigDecimal(this.getFailRateValue()))==1){
                table.append("</td><td style=\"font-size:15px;").append("background-color:#FF42FF;color:#ffffff").append("\">");
            }else {
                table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            }
            table.append(csrr.getReallyFailRate().setScale(2, BigDecimal.ROUND_HALF_UP)).append("%");
            table.append("</td></tr>");
        }
        table.append("</table>");
        if(dateTime==null)
            dateTime = new Date();
        return table.toString().replace(TO_BE_REPALCE, sdf2.format(dateTime));
    }

    private String formTable6(List<RecordAllHeJi> lastRecordList) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
        StringBuilder table = new StringBuilder("<h2>record流水表实时查询结果(按通道区分)[");
        table.append(TO_BE_REPALCE);
        table.append("]</h2>");
        Date dateTime = null;
        table.append("<table border=\"1\" cellspacing=\"0\" style=\"border-collapse:collapse\"><tbody><tr><th style=\"background-color:#FFE66F\">record-通道号</th><th style=\"background-color:#FFE66F\">统计时间</th><th style=\"background-color:#FFE66F\">标识</th><th style=\"background-color:#FFE66F\">发送通道 1 2 3 5 6</th><th style=\"background-color:#FFE66F\">半小时总数</th><th style=\"background-color:#FFE66F\">明确成功 3</th><th style=\"background-color:#FFE66F\">提交失败 4</th><th style=\"background-color:#FFE66F\">发送失败 5 6</th><th style=\"background-color:#FFE66F\">成功待定 1</th><th style=\"background-color:#FFE66F\">成功待定 2</th><th style=\"background-color:#FFE66F\">未发送 0</th><th width=\"60px\" style=\"background-color:#FFE66F\">成功率</th><th width=\"60px\" style=\"background-color:#FFE66F\">未知率</th><th width=\"60px\" style=\"background-color:#FFE66F\">失败率</th></tr><tr>");
        int count=0;
        int lastTotal = 0;
        for(RecordAllHeJi csrr:lastRecordList){
            count++;
            String color = "";
            if(count==lastRecordList.size()){
                color="color:blue";
                csrr.setChannelId("全部合计");
                csrr.setChannelName(null);
            }
            if(dateTime==null)
                dateTime = csrr.getDataTime();
            table.append("<tr><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getChannelId());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            //如果时间分钟不为半整点，则设置为整点
            String format = sdf.format(csrr.getDataTime());
            String time = formatTimeForZero(format);
            table.append(time);
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getIden());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSendTotal());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSendTotal()-csrr.getBeSendTotal());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSuccessTotal());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSubmitFail());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getSendFail());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getUndetermined1());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getUndetermined2());
            table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            table.append(csrr.getNosend());
            if((csrr.getSuccessRate()).compareTo(new BigDecimal(this.getSuccessRateValue()))==-1){
                table.append("</td><td style=\"font-size:15px;").append("background-color:red;color:#ffffff").append("\">");
            }else {
                table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            }
            table.append(csrr.getSuccessRate().setScale(2, BigDecimal.ROUND_HALF_UP)).append("%");
            if((csrr.getFakeSuccessRate()).compareTo(new BigDecimal(this.getFakeRateValue()))==1){
                table.append("</td><td style=\"font-size:15px;").append("background-color:#FF580A;color:#ffffff").append("\">");
            }else {
                table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            }
            table.append(csrr.getFakeSuccessRate().setScale(2, BigDecimal.ROUND_HALF_UP)).append("%");
            if((csrr.getReallyFailRate()).compareTo(new BigDecimal(this.getFailRateValue()))==1){
                table.append("</td><td style=\"font-size:15px;").append("background-color:#FF42FF;color:#ffffff").append("\">");
            }else {
                table.append("</td><td style=\"font-size:15px;").append(color).append("\">");
            }
            table.append(csrr.getReallyFailRate().setScale(2, BigDecimal.ROUND_HALF_UP)).append("%");
            table.append("</td></tr>");
        }
        table.append("</table>");
        if(dateTime==null)
            dateTime = new Date();
        return table.toString().replace(TO_BE_REPALCE, sdf2.format(dateTime));
    }
    /**
     * 如果时间不为半个整点，设置为半整点
     * @param time
     * @return
     */
    private String formatTimeForZero(String time){
        String minutes = time.substring(time.length()-1);
        if("0".equals(minutes)){
            time=time.substring(0,time.length()-1)+"0";
        }
        return time;
    }
    /**
     * 获取应该填充的次数
     * @param date
     * @return
     */
    private int  getEmptyTime(Date date){
        String minTime = DateUtil.dateToStr(date, "HH:mm");
        String[] split = minTime.split(":");
        String hour = split[0];
        String minuses = split[1];
        int cycleTime = Integer.parseInt(hour,10) * 2;
        int i1 = Integer.parseInt(minuses);
        if (i1 > 25) {
            cycleTime += 1;
        }
        return cycleTime;
    }
    /**
     * 如果数据缺少某个时间点的数据，则填充数据为0的数据
     * @param dataMap
     * @param type
     * @return
     * @Deprected 已废弃，会导致NullPointException
     */
    @Deprecated
    private Map completeData(Map dataMap,int type) {
        if(dataMap==null||dataMap.isEmpty())
            return dataMap;
        Date minTimes = null;
        TreeMap<Long, Object> transacMap = (TreeMap<Long, Object>) dataMap;
        //获取第一个
        if(type == 1){
            AccessAliHeJi accessAliHeJi = (AccessAliHeJi) transacMap.firstEntry().getValue();
            minTimes = accessAliHeJi.getDataTime();
        }else  if(type == 2){
            AccessAllHeJi accessAllHeJi = (AccessAllHeJi) transacMap.firstEntry().getValue();
            minTimes = accessAllHeJi.getDataTime();
        }else  if(type == 3){
            RecordByClientAliHeJi recordByClientAliHeJi = (RecordByClientAliHeJi) transacMap.firstEntry().getValue();
            minTimes = recordByClientAliHeJi.getDataTime();
        }else  if(type == 4){
            RecordByClientAllHeJi recordByClientAllHeJi = (RecordByClientAllHeJi) transacMap.firstEntry().getValue();
            minTimes = recordByClientAllHeJi.getDataTime();
        }else if(type == 5){
            ChannelSuccessRateRealtime channelSuccessRateRealtime = (ChannelSuccessRateRealtime) transacMap.firstEntry().getValue();
            minTimes = channelSuccessRateRealtime.getDataTime();
        }
        int emptyTime = getEmptyTime(minTimes);
        //当天的零点
        Date beginDate = DateUtil.strToDateLong(DateUtil.dateToStr(minTimes, "yyyy-MM-dd") + " 00:00:00");
        for (int i = 0; i < emptyTime; i++) {
            //下个时间点
            Date nextTime = new Date(beginDate.getTime() + 30 * 60 * 1000 * i);
            if(type == 1){
                AccessAliHeJi newAccessAliHeJi = createAccessAliHeJi();
                newAccessAliHeJi.setDataTime(nextTime);
                newAccessAliHeJi.setSendTotal(0);
                newAccessAliHeJi.setReallySuccessTotal(0);
                newAccessAliHeJi.setSuccessRate(new BigDecimal(0));
                newAccessAliHeJi.setFakeSuccessRate(new BigDecimal(0));
                newAccessAliHeJi.setReallyFailRate(new BigDecimal(0));
                transacMap.put(nextTime.getTime(),newAccessAliHeJi);
            }else  if(type==2){
                AccessAllHeJi newAccessAllHeJi = createAccessAllHeJi();
                newAccessAllHeJi.setDataTime(nextTime);
                newAccessAllHeJi.setSendTotal(0);
                newAccessAllHeJi.setReallySuccessTotal(0);
                newAccessAllHeJi.setSuccessRate(new BigDecimal(0));
                newAccessAllHeJi.setFakeSuccessRate(new BigDecimal(0));
                newAccessAllHeJi.setReallyFailRate(new BigDecimal(0));
                transacMap.put(nextTime.getTime(),newAccessAllHeJi);
            }else  if(type==3){
                RecordByClientAliHeJi recordByClientAliHeJi = new RecordByClientAliHeJi();
                recordByClientAliHeJi.setDataTime(nextTime);
                recordByClientAliHeJi.setSendTotal(0);
                recordByClientAliHeJi.setSuccessRate(new BigDecimal(0));
                recordByClientAliHeJi.setFakeSuccessRate(new BigDecimal(0));
                recordByClientAliHeJi.setReallyFailRate(new BigDecimal(0));
                transacMap.put(nextTime.getTime(),recordByClientAliHeJi);
            }else  if(type==4){
                RecordByClientAllHeJi recordByClientAllHeJi = new RecordByClientAllHeJi();
                recordByClientAllHeJi.setDataTime(nextTime);
                recordByClientAllHeJi.setSendTotal(0);
                recordByClientAllHeJi.setSuccessRate(new BigDecimal(0));
                recordByClientAllHeJi.setFakeSuccessRate(new BigDecimal(0));
                recordByClientAllHeJi.setReallyFailRate(new BigDecimal(0));
                transacMap.put(nextTime.getTime(),recordByClientAllHeJi);
            }else if(type==5){
                ChannelSuccessRateRealtime channelSuccessRateRealtime = new ChannelSuccessRateRealtime();
                channelSuccessRateRealtime.setDataTime(nextTime);
                channelSuccessRateRealtime.setSendTotal(0);
                channelSuccessRateRealtime.setSuccessRate(new BigDecimal(0));
                channelSuccessRateRealtime.setFakeSuccessRate(new BigDecimal(0));
                channelSuccessRateRealtime.setReallyFailRate(new BigDecimal(0));
                transacMap.put(nextTime.getTime(),channelSuccessRateRealtime);
            }
        }
        return  transacMap;
    }
    /**
     * 获取一个正序的treeMap
     * @return
     */
    public TreeMap getSortMap(){
        TreeMap<Long, Object> sortMap = new TreeMap<>(new Comparator<Long>() {
            @Override
            public int compare(Long o1, Long o2) {
                return o1.compareTo(o2);
            }
        });
        return sortMap;
    }
    /**
     * 根据异常过滤无效的邮件地址
     * @param exception 发送时的异常信息
     * @param internetAddresses 发送地址列表
     */
    public void removeFailEmailByException(Exception exception, ArrayList<InternetAddress> internetAddresses) {
        //判断是否是地址异常
        if (exception instanceof MessagingException) {
            if (exception instanceof SMTPAddressFailedException) {
                SMTPAddressFailedException smtpAddressFailedException = (SMTPAddressFailedException) exception;
                //获取错误的地址
                InternetAddress address = smtpAddressFailedException.getAddress();
                logger.error("发送邮件失败包含无效的地址:" + address);
                //去掉错误的地址
                internetAddresses.remove(address);
            }
            MessagingException nextException = (MessagingException) exception;
            MessagingException nextException1 = (MessagingException) nextException.getNextException();
            if (nextException1 != null) {
                removeFailEmailByException(nextException1, internetAddresses);
            }
        }
    }
    public void plusChannelSuccessRateRealtimeValueTo(RecordAllHeJi value, RecordAllHeJi to) {
        to.setChannelId(value.getChannelId());
        to.setChannelName(value.getChannelName());
        to.setIden(value.getIden());
        to.setSendTotal(value.getSendTotal()+to.getSendTotal());
        to.setBeSendTotal(value.getBeSendTotal()+to.getBeSendTotal());
        to.setSuccessTotal(value.getSuccessTotal()+to.getSuccessTotal());
        to.setSubmitFail(value.getSubmitFail()+to.getSubmitFail());
        to.setSendFail(value.getSendFail()+to.getSendFail());
        to.setUndetermined1(value.getUndetermined1()+to.getUndetermined1());
        to.setUndetermined2(value.getUndetermined2()+to.getUndetermined2());
        to.setNosend(value.getNosend()+to.getNosend());
        BigDecimal sendTotal = new BigDecimal(to.getSendTotal());
        BigDecimal successRate = new BigDecimal(to.getSuccessTotal()).divide(sendTotal,5, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        to.setSuccessRate(successRate);
        BigDecimal fakeSuccessRate = new BigDecimal(to.getUndetermined1()+to.getUndetermined2()).divide(sendTotal,5, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        to.setFakeSuccessRate(fakeSuccessRate);
        BigDecimal reallyFailRate = new BigDecimal(to.getSendFail()).divide(sendTotal,5, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        to.setReallyFailRate(reallyFailRate);
        to.setDataTime(value.getDataTime());
    }
    public void plusChannelSuccessRateByClientidValueTo1(ChannelSuccessRateByClientid value, ChannelSuccessRateByClientid to) {
        to.setChannelId(value.getChannelId());
        to.setChannelName(value.getChannelName());
        to.setClientId(value.getClientId());
        to.setClientName(value.getClientName());
        to.setSendTotal(value.getSendTotal()+to.getSendTotal());
        to.setSuccessTotal(value.getSuccessTotal()+to.getSuccessTotal());
        to.setSubmitFail(value.getSubmitFail()+to.getSubmitFail());
        to.setSendFail(value.getSendFail()+to.getSendFail());
        to.setUndetermined1(value.getUndetermined1()+to.getUndetermined1());
        to.setUndetermined2(value.getUndetermined2()+to.getUndetermined2());
        to.setNosend(value.getNosend()+to.getNosend());
        BigDecimal sendTotal = new BigDecimal(to.getSendTotal());
        BigDecimal successRate = new BigDecimal(to.getSuccessTotal()).divide(sendTotal,5, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        to.setSuccessRate(successRate);
        BigDecimal fakeSuccessRate = new BigDecimal(to.getUndetermined1()+to.getUndetermined2()).divide(sendTotal,5, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        to.setFakeSuccessRate(fakeSuccessRate);
        BigDecimal reallyFailRate = new BigDecimal(to.getSendFail()).divide(sendTotal,5, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        to.setReallyFailRate(reallyFailRate);
        to.setDataTime(value.getDataTime());
    }
    public void plusChannelSuccessRateByClientidValueTo(RecordByClientVo value, RecordByClientAllHeJi to) {
        to.setChannelId(value.getChannelId());
        to.setChannelName(value.getChannelName());
        to.setClientId(value.getClientId());
        to.setClientName(value.getClientName());
        to.setSendTotal(value.getSendTotal()+to.getSendTotal());
        to.setBeSendTotal(value.getBeSendTotal()+to.getBeSendTotal());
        to.setSuccessTotal(value.getSuccessTotal()+to.getSuccessTotal());
        to.setSubmitFail(value.getSubmitFail()+to.getSubmitFail());
        to.setSendFail(value.getSendFail()+to.getSendFail());
        to.setUndetermined1(value.getUndetermined1()+to.getUndetermined1());
        to.setUndetermined2(value.getUndetermined2()+to.getUndetermined2());
        to.setNosend(value.getNosend()+to.getNosend());
        BigDecimal sendTotal = new BigDecimal(to.getSendTotal());
        BigDecimal successRate = new BigDecimal(to.getSuccessTotal()).divide(sendTotal,5, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        to.setSuccessRate(successRate);
        BigDecimal fakeSuccessRate = new BigDecimal(to.getUndetermined1()+to.getUndetermined2()).divide(sendTotal,5, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        to.setFakeSuccessRate(fakeSuccessRate);
        BigDecimal reallyFailRate = new BigDecimal(to.getSendFail()).divide(sendTotal,5, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        to.setReallyFailRate(reallyFailRate);
        to.setDataTime(value.getDataTime());
    }
    /**
     * 阿里合计
     * @param value
     * @param to
     */
    public void plusChannelSuccessRateByClientidValueTo2(RecordByClientVo value, RecordByClientAliHeJi to) {
        to.setChannelId(value.getChannelId());
        to.setChannelName(value.getChannelName());
        to.setClientId(value.getClientId());
        to.setClientName(value.getClientName());
        to.setSendTotal(value.getSendTotal()+to.getSendTotal());
        to.setBeSendTotal(value.getBeSendTotal()+to.getBeSendTotal());
        to.setSuccessTotal(value.getSuccessTotal()+to.getSuccessTotal());
        to.setSubmitFail(value.getSubmitFail()+to.getSubmitFail());
        to.setSendFail(value.getSendFail()+to.getSendFail());
        to.setUndetermined1(value.getUndetermined1()+to.getUndetermined1());
        to.setUndetermined2(value.getUndetermined2()+to.getUndetermined2());
        to.setNosend(value.getNosend()+to.getNosend());
        BigDecimal sendTotal = new BigDecimal(to.getSendTotal());
        BigDecimal successRate = new BigDecimal(to.getSuccessTotal()).divide(sendTotal,5, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        to.setSuccessRate(successRate);
        BigDecimal fakeSuccessRate = new BigDecimal(to.getUndetermined1()+to.getUndetermined2()).divide(sendTotal,5, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        to.setFakeSuccessRate(fakeSuccessRate);
        BigDecimal reallyFailRate = new BigDecimal(to.getSendFail()).divide(sendTotal,5, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        to.setReallyFailRate(reallyFailRate);
        to.setDataTime(value.getDataTime());
    }
    public void plusClientSuccessRateRealtimeValueTo1(ClientSuccessRateRealtime value, ClientSuccessRateRealtime to) {
        to.setClientId(value.getClientId());
        to.setClientName(value.getClientName());
        to.setSendTotal(value.getSendTotal()+to.getSendTotal());
        //	to.setBeSendTotal(value.getBeSendTotal()+to.getBeSendTotal());
        to.setReallySuccessTotal(value.getReallySuccessTotal()+to.getReallySuccessTotal());
        to.setFakeSuccessFail(value.getFakeSuccessFail()+to.getFakeSuccessFail());
        to.setCharge1(value.getCharge1()+to.getCharge1());
        to.setCharge2(value.getCharge2()+to.getCharge2());
        to.setReallyFailTotal(value.getReallyFailTotal()+to.getReallyFailTotal());
        to.setAuditFailTotal(value.getAuditFailTotal()+to.getAuditFailTotal());
        to.setSubmitFailTotal(value.getSubmitFailTotal()+to.getSubmitFailTotal());
        to.setInterceptTotal(value.getInterceptTotal()+to.getInterceptTotal());
        to.setNosend(value.getNosend()+to.getNosend());
        to.setSendFailToatl(value.getSendFailToatl()+to.getSendFailToatl());
        to.setSendAll(value.getSendAll()+to.getSendAll());
        BigDecimal sendTotal = new BigDecimal(to.getSendTotal());
        BigDecimal successRate = new BigDecimal(to.getReallySuccessTotal()).divide(sendTotal,5, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        to.setSuccessRate(successRate );
        BigDecimal fakeSuccessRate = new BigDecimal(to.getFakeSuccessFail()).divide(sendTotal,5, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        to.setFakeSuccessRate(fakeSuccessRate );
        BigDecimal reallyFailRate = new BigDecimal(to.getReallyFailTotal()).divide(sendTotal,5, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        to.setReallyFailRate(reallyFailRate);
        to.setDataTime(value.getDataTime());
    }
    public void plusClientSuccessRateRealtimeValueTo(AccessAllHeJi value, AccessAllHeJi to) {
        to.setClientId(value.getClientId());
        to.setClientName(value.getClientName());
        to.setSendTotal(value.getSendTotal()+to.getSendTotal());
        to.setBeSendTotal(value.getBeSendTotal()+to.getBeSendTotal());
        to.setReallySuccessTotal(value.getReallySuccessTotal()+to.getReallySuccessTotal());
        to.setFakeSuccessFail(value.getFakeSuccessFail()+to.getFakeSuccessFail());
        to.setCharge1(value.getCharge1()+to.getCharge1());
        to.setCharge2(value.getCharge2()+to.getCharge2());
        to.setReallyFailTotal(value.getReallyFailTotal()+to.getReallyFailTotal());
        to.setAuditFailTotal(value.getAuditFailTotal()+to.getAuditFailTotal());
        to.setSubmitFailTotal(value.getSubmitFailTotal()+to.getSubmitFailTotal());
        to.setInterceptTotal(value.getInterceptTotal()+to.getInterceptTotal());
        to.setNosend(value.getNosend()+to.getNosend());
        to.setSendFailToatl(value.getSendFailToatl()+to.getSendFailToatl());
        to.setSendAll(value.getSendAll()+to.getSendAll());
        BigDecimal sendTotal = new BigDecimal(to.getSendTotal());
        BigDecimal successRate = new BigDecimal(to.getReallySuccessTotal()).divide(sendTotal,5, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        to.setSuccessRate(successRate );
        BigDecimal fakeSuccessRate = new BigDecimal(to.getFakeSuccessFail()).divide(sendTotal,5, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        to.setFakeSuccessRate(fakeSuccessRate );
        BigDecimal reallyFailRate = new BigDecimal(to.getReallyFailTotal()).divide(sendTotal,5, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        to.setReallyFailRate(reallyFailRate);
        to.setDataTime(value.getDataTime());
    }
}
