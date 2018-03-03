package com.ucpaas.sms.task.service;

import com.jsmsframework.audit.entity.JsmsAuditkeywordRecord;
import com.jsmsframework.audit.exception.JsmsAuditException;
import com.jsmsframework.audit.exception.JsmsAuditkeywordRecordException;
import com.jsmsframework.audit.service.JsmsAuditkeywordRecordService;
import com.jsmsframework.common.util.DateUtil;
import com.ucpaas.sms.task.model.TaskInfo;
import com.ucpaas.sms.task.util.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 每日备份短信审核与关键字记录
 */
@Service
public class AuditKeywordRecordBakServiceImpl implements AuditKeywordRecordBakService {

    @Autowired
    private JsmsAuditkeywordRecordService jsmsAuditkeywordRecordService;

    private static final Logger logger = LoggerFactory.getLogger("AuditKeywordRecordBakService");

    List<JsmsAuditkeywordRecord> auditKeywordRecordRemoves = null;

    @Override
    public boolean bakAuditKeywordRecord(TaskInfo taskInfo) {
        long begin = System.currentTimeMillis();
        String format = taskInfo.getExecuteType().getFormat();
        DateTime executeNext = UcpaasDateUtils.parseDate(taskInfo.getExecuteNext(), format);
        //下次提交时间
        String excuteNextStr = executeNext.toString("yyyyMMdd");

        logger.info("【每日短信审核与关键字记录备份】开始,task时间={},程序运行时间段为{}-{}", excuteNextStr, ConfigUtils.audit_keyword_record_bak_running_begin, ConfigUtils.audit_keyword_record_bak_running_end);
        if (auditKeywordRecordRemoves != null) {
            throw new JsmsAuditkeywordRecordException("AuditKeywordRecordBakServiceImpl初始化失败");
        }

        int iday = Integer.valueOf(ConfigUtils.audit_keyword_record_bak_before_day);
        int threadSize = Integer.valueOf(ConfigUtils.audit_keyword_record_bak_thread_size);
        int size = Integer.valueOf(ConfigUtils.audit_keyword_record_bak_size);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dateDate = null;
        try {
            dateDate = sdf.parse(executeNext.minusDays(iday).toString("yyyy-MM-dd 00:00:00"));
        } catch (ParseException e) {
            logger.error("数据日期初始化失败", e);
            return false;
        }
        //检测运行时间是否在时间段内
        while (!AuditKeywordRecordBakExecutorService.canRunning()) {
            logger.info("运行时间不满足(休眠5分钟),配置为{}-{}", ConfigUtils.audit_keyword_record_bak_running_begin, ConfigUtils.audit_keyword_record_bak_running_end);
            try {
                Thread.sleep(300 * 1000);
            } catch (InterruptedException e) {
                return false;
            }
        }

        String dateDateStr = sdf.format(dateDate);
        logger.info("开始备份短信审核与关键字表数据,dateDate={},线程数{},单任务(线程)最大处理记录数{}", dateDateStr, threadSize, size);
        long queryAllAuditBegin = System.currentTimeMillis();
        //根据设置查询需要备份的数据(3天前的数据)
        auditKeywordRecordRemoves = jsmsAuditkeywordRecordService.queryAllRemoveRecordAndCreatetime(dateDate);
        long queryAllAuditEnd = System.currentTimeMillis();
        //查询所花费的时间
        long queryAllAuditCost = queryAllAuditEnd - queryAllAuditBegin;


        AtomicLong auditAllBackupTime = new AtomicLong();
        List<Future<String>> futureAudit = new ArrayList<>();
        //对需要备份的数据循环提交线程处理
        for (int offset = 0; offset < auditKeywordRecordRemoves.size(); offset = offset + size) {
            int s = Math.min(size, auditKeywordRecordRemoves.size() - offset);
            Future<String> future = AuditKeywordRecordBakExecutorService.submit(new AuditKeywordRecordBakServiceImpl.BackupAuditKeywordRecord(offset, s, auditAllBackupTime));
            futureAudit.add(future);
        }
        for (Future<String> future : futureAudit) {
            try {
                logger.debug("futureAuditKeywordRecord result={}", future.get());
            } catch (Exception e) {
                logger.error("get futureAuditKeywordRecord fail");
            }
        }
        long auditCost = System.currentTimeMillis() - queryAllAuditBegin;
        logger.info("结束备份短信审核与关键字表数据,dateDate={},总数{},总用时{}ms,cpu用时{}ms,其中queryAll={}ms,insertAll={}ms", dateDateStr, auditKeywordRecordRemoves.size(), auditCost, auditAllBackupTime.get() + queryAllAuditCost, queryAllAuditCost, auditAllBackupTime.get());
        //清空数据
        auditKeywordRecordRemoves.clear();
        auditKeywordRecordRemoves = null;

        if (AuditKeywordRecordBakExecutorService.isShutdown) {
            logger.error("tomcat已关闭，短信审核与关键字备份任务停止");
            return false;
        }

        logger.info("【每日短信审核与关键字记录备份】结束,task时间={},总用时{}ms", excuteNextStr, (System.currentTimeMillis() - begin));
        return true;
    }

    @Override
    @Transactional("access")
    public void backupAuditKeywordRecord(long recordId, Date createtime) {
        long qBegin = System.currentTimeMillis();
        JsmsAuditkeywordRecord jsmsAuditkeywordRecord = jsmsAuditkeywordRecordService.queryByIdAndCreateTime(recordId, createtime);
        long qEnd = System.currentTimeMillis();

        logger.debug("query auditKeywordRecord cost {}ms",(qEnd-qBegin));
        logger.debug("backup auditKeywordRecord={}", JacksonUtil.toJSON(jsmsAuditkeywordRecord));

        long iBegin = System.currentTimeMillis();
        //确定插入的是哪个分表
        String tableName = "t_sms_auditkeyword_record_"+ new DateTime(DateUtil.getThisWeekMonday(jsmsAuditkeywordRecord.getAuditCreateTime())).toString("yyyyMMdd");
        //插入数据
        int i = jsmsAuditkeywordRecordService.insertWithTableName(jsmsAuditkeywordRecord,tableName);
        if(i!=1){
            throw new JsmsAuditkeywordRecordException("备份AuditKeyworkdRecord数据失败[insert阶段]record="+ JacksonUtil.toJSON(jsmsAuditkeywordRecord));
        }
        long iEnd = System.currentTimeMillis();
        logger.debug("insert auditBak cost {}ms",(iEnd-iBegin));
        long dBegin = System.currentTimeMillis();
        //删除数据
        int d = jsmsAuditkeywordRecordService.delByIdAndCreateTime(recordId,createtime);
        long dENd = System.currentTimeMillis();
        logger.debug("delete audit cost {}ms",(dENd-dBegin));
        if(d!=1){
            throw new JsmsAuditkeywordRecordException("备份AuditKeyworkdRecord数据失败[delete阶段]record="+ JacksonUtil.toJSON(jsmsAuditkeywordRecord));
        }

    }

    class BackupAuditKeywordRecord implements Callable<String>{
        int offset;
        int size;
        AtomicLong auditKeywordRecordAllBackTime;

        public BackupAuditKeywordRecord(int offset, int size, AtomicLong auditKeywordRecordAllBackTime) {
            this.offset = offset;
            this.size = size;
            this.auditKeywordRecordAllBackTime = auditKeywordRecordAllBackTime;
        }

        @Override
        public String call() throws Exception {
            int successCount = 0;
            int failCount = 0;
            String msg="";
            List<Long> toBeRemoves = new ArrayList<>();
            for(int i=offset;i<offset+size;i++){
                if(AuditKeywordRecordBakExecutorService.isShutdown) { //容器停止时则停止任务
                    msg = interruptTask(toBeRemoves, i);
                    break;
                }
                while(!AuditKeywordRecordBakExecutorService.canRunning()){
                    logger.info("运行时间不满足(休眠5分钟),配置为{}-{}",ConfigUtils.audit_sms_bak_running_begin,ConfigUtils.audit_sms_bak_running_end);
                    try {
                        Thread.sleep(300 * 1000);
                    }catch (InterruptedException e){
                        msg = interruptTask(toBeRemoves, i);
                        break;
                    }
                }
                try {
                    //从List取出需要处理的数据
                    JsmsAuditkeywordRecord record = auditKeywordRecordRemoves.get(i);
                    long oneAuditSmsRecordBegin = System.currentTimeMillis();
                    //备份数据
                    backupAuditKeywordRecord(record.getId(), record.getAuditCreateTime());
                    long oneAuditSmsRecordEnd = System.currentTimeMillis();
                    long cost = oneAuditSmsRecordEnd - oneAuditSmsRecordBegin;
                    //备份一条数据所花费的时间
                    logger.debug("backup one auditKeywordRecord cost {}ms", cost);
                    auditKeywordRecordAllBackTime.addAndGet(cost);
                }catch (Exception e){
                    logger.error("backup auditKeywordRecord task fail one record",e);
                    failCount++;
                    continue;
                }
                successCount++;
            }

            return size+","+successCount+","+failCount+","+msg+toBeRemoves;
        }

        private String interruptTask(List<Long> toBeRemoves, int i) {
            String msg = "";
            msg = "task interrupted, not remove ids[]=";
            for (int k = i; k < offset + size; k++) {
                try {
                    JsmsAuditkeywordRecord jsmsAuditkeywordRecord = auditKeywordRecordRemoves.get(k);
                    toBeRemoves.add(Long.valueOf(jsmsAuditkeywordRecord.getId()));
                } catch (Exception e) {
                    logger.error("", e);
                    continue;
                }
            }
            return msg;
        }
    }
}
