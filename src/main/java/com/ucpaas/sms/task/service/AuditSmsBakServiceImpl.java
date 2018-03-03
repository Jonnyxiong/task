package com.ucpaas.sms.task.service;

import com.jsmsframework.audit.dto.JsmsAuditidAndCreatetime;
import com.jsmsframework.audit.dto.JsmsIdAndCreatetime;
import com.jsmsframework.audit.entity.JsmsAudit;
import com.jsmsframework.audit.entity.JsmsAuditBak;
import com.jsmsframework.audit.entity.JsmsAuditSms;
import com.jsmsframework.audit.entity.JsmsAuditSmsBak;
import com.jsmsframework.audit.exception.JsmsAuditException;
import com.jsmsframework.audit.service.JsmsAuditBakService;
import com.jsmsframework.audit.service.JsmsAuditService;
import com.jsmsframework.audit.service.JsmsAuditSmsBakService;
import com.jsmsframework.audit.service.JsmsAuditSmsService;
import com.jsmsframework.common.util.BeanUtil;
import com.jsmsframework.common.util.DateUtil;
import com.ucpaas.sms.task.entity.message.Task;
import com.ucpaas.sms.task.mapper.message.TaskMapper;
import com.ucpaas.sms.task.model.TaskInfo;
import com.ucpaas.sms.task.thread.AuditBakTask;
import com.ucpaas.sms.task.util.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 每日审核记录备份
 * 
 */
@Service
public class AuditSmsBakServiceImpl implements AuditSmsBakService {
	private static final Logger logger = LoggerFactory.getLogger("AuditSmsBakService");

	private static final int size = 100000;

	@Autowired
	private TaskMapper taskMapper;

	@Autowired
	private JsmsAuditService auditService;
	@Autowired
	private JsmsAuditBakService jsmsAuditBakService;

	@Autowired
	private JsmsAuditSmsService auditSmsService;
	@Autowired
	private JsmsAuditSmsBakService jsmsAuditSmsBakService;


	private final AuditBakTask auditBakTask;

	List<JsmsAuditidAndCreatetime> auditToRemoves = null;

	List<JsmsIdAndCreatetime> auditSmsToRemoves = null;

	private Lock auditLock = new ReentrantLock();

	private Lock auditSmsLock = new ReentrantLock();



	@Autowired
	public AuditSmsBakServiceImpl(AuditBakTask auditBakTask) {
		this.auditBakTask = auditBakTask;
	}

	/**
	 * 检查任务是否停止
	 */
	private boolean isStop(int iday) {
		Task task = taskMapper.getByTaskType("31");
		if (task == null) {
			return true;
		}

		boolean isStop = task.getStatus().equals("0");
		if (isStop) {
			return true;
		}

		Map<String, Object> auditMaps = auditService.getNeedBakCount(iday);
		int auditCount = Integer.parseInt(auditMaps.get("count").toString());

		Map<String, Object> auditSmsMaps = auditSmsService.getNeedBakCount(iday);
		int auditSmsCount = Integer.parseInt(auditSmsMaps.get("count").toString());

		if (auditCount <= 0 && auditSmsCount <= 0) {
			return true;
		}

		return false;
	}

	private boolean doBak() {
		int iday = Integer.valueOf(ConfigUtils.audit_sms_bak_before_day);

		// 查询审核记录
		Map<String, Object> auditMaps = auditService.getNeedBakCount(iday);
		int auditCount = Integer.parseInt(auditMaps.get("count").toString());
		// int auditMax = Integer.parseInt(auditMaps.get("max").toString());
		int auditMin = Integer.parseInt(auditMaps.get("min").toString());

		// 查询
		Map<String, Object> auditSMSMaps = auditSmsService.getNeedBakCount(iday);
		int auditSMSCount = Integer.parseInt(auditSMSMaps.get("count").toString());
		// int auditSMSMax =
		// Integer.parseInt(auditSMSMaps.get("max").toString());
		int auditSMSMin = Integer.parseInt(auditSMSMaps.get("min").toString());

		if (auditCount == 0 && auditSMSCount == 0) {
			return true;
		}

		Future<String> task1 = null;
		Future<String> task2 = null;
		Future<String> task3 = null;
		Future<String> task4 = null;
		Future<String> task5 = null;
		Future<String> task6 = null;
		Future<String> task7 = null;
		Future<String> task8 = null;
		Future<String> task9 = null;

		// 创建线程1：
		if (auditCount > 0) {
			auditCount = auditCount - size; // 审核记录本次减去每次处理数量1万
			task1 = auditBakTask.doBakAudit(auditMin, auditMin + size - 1);
			auditMin = auditMin + size; // 最小数量加上本次处理的1万
		}

		// 创建线程2
		if (auditCount > 0) {
			auditCount = auditCount - size; // 审核记录本次减去每次处理数量1万
			task2 = auditBakTask.doBakAudit(auditMin, auditMin + size - 1);
			auditMin = auditMin + size; // 最小数量加上本次处理的1万
		}

		// 创建线程3
		if (auditCount > 0) {
			auditCount = auditCount - size; // 审核记录本次减去每次处理数量1万
			task3 = auditBakTask.doBakAudit(auditMin, auditMin + size - 1);
			auditMin = auditMin + size; // 最小数量加上本次处理的1万
		}

		// 创建线程4
		if (auditCount > 0) {
			auditCount = auditCount - size; // 审核记录本次减去每次处理数量1万
			task4 = auditBakTask.doBakAudit(auditMin, auditMin + size - 1);
		}

		// 下面的线程用于处理审核明细
		// 创建线程5
		if (auditSMSCount > 0) {
			auditSMSCount = auditSMSCount - size; // 审核明细记录本次减去每次处理数量1万
			task5 = auditBakTask.doBakAuditSMS(auditSMSMin, auditSMSMin + size - 1);
			auditSMSMin = auditSMSMin + size; // 最小数量加上本次处理的1万
		}

		// 创建线程6
		if (auditSMSCount > 0) {
			auditSMSCount = auditSMSCount - size; // 审核明细记录本次减去每次处理数量1万
			task6 = auditBakTask.doBakAuditSMS(auditSMSMin, auditSMSMin + size - 1);
			auditSMSMin = auditSMSMin + size; // 最小数量加上本次处理的1万
		}

		// 创建线程7
		if (auditSMSCount > 0) {
			auditSMSCount = auditSMSCount - size; // 审核明细记录本次减去每次处理数量1万
			task7 = auditBakTask.doBakAuditSMS(auditSMSMin, auditSMSMin + size - 1);
			auditSMSMin = auditSMSMin + size; // 最小数量加上本次处理的1万
		}

		// 创建线程8
		if (auditSMSCount > 0) {
			auditSMSCount = auditSMSCount - size; // 审核明细记录本次减去每次处理数量1万
			task8 = auditBakTask.doBakAuditSMS(auditSMSMin, auditSMSMin + size - 1);
		}

		// 不需要处理
		//task9 = auditBakTask.doDealRepeatRecord(size);

		// 判断线程是否完成，完成返回
		while (true) {
			// 全部完成，跳出循环
			if (isDone(task1) && isDone(task2) && isDone(task3) && isDone(task4) && isDone(task5) && isDone(task6)
					&& isDone(task7) && isDone(task8)) {
				//cancel(task9); // 将9线程取消，重新处理
				if (isStop(iday)) {
					break;
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// 继续处理
				doBak();
			}
		}

		return true;
	}

	private boolean isDone(Future<String> task) {
		return task == null || task.isDone();
	}

	private void cancel(Future<String> task) {
		if (task == null) {
			return;
		}
		task.cancel(true);
	}

	/**
	 *
	 * 存在漏洞的代码，不再使用  --黄文杰
	 *
	 * 每日审核记录备份
	 * 
	 * @return 是否成功
	 */
	@Transactional
	@Deprecated
	public boolean bakAudit(TaskInfo taskInfo) {
		Calendar begin = Calendar.getInstance();
		logger.debug("【每日审核记录备份】开始 = {}", DateUtilsNew.formatDateTime(begin.getTime()));
		doBak();
		Calendar end = Calendar.getInstance();
		logger.debug("【每日审核记录备份】结束 = {}", DateUtilsNew.formatDateTime(end.getTime()));
		return true;
	}

	@Override
	public boolean bakAuditAndSms(TaskInfo taskInfo) {
		long begin = System.currentTimeMillis();
		String format = taskInfo.getExecuteType().getFormat();
		DateTime executeNext = UcpaasDateUtils.parseDate(taskInfo.getExecuteNext(), format);

		String excuteNextStr = executeNext.toString("yyyyMMdd");

		logger.info("【每日审核记录备份】开始,task时间={},程序运行时间段为{}-{}", excuteNextStr,ConfigUtils.audit_sms_bak_running_begin,ConfigUtils.audit_sms_bak_running_end);
		if(auditToRemoves!=null|| auditSmsToRemoves!=null){
			throw new JsmsAuditException("AuditSmsBakServiceImpl初始化失败");
		}


		int iday = Integer.valueOf(ConfigUtils.audit_sms_bak_before_day);
		int threadSize = Integer.valueOf(ConfigUtils.audit_sms_bak_thread_size);
		int size = Integer.valueOf(ConfigUtils.audit_sms_bak_size);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date dateDate = null;
		try {
			dateDate =  sdf.parse(executeNext.minusDays(iday).toString("yyyy-MM-dd 00:00:00"));
		} catch (ParseException e) {
			logger.error("数据日期初始化失败",e);
			return false;
		}

		while(!AuditBakExecutorService.canRunning()){
			logger.info("运行时间不满足(休眠5分钟),配置为{}-{}",ConfigUtils.audit_sms_bak_running_begin,ConfigUtils.audit_sms_bak_running_end);
			try {
				Thread.sleep(300 * 1000);
			}catch (InterruptedException e){
				return false;
			}
		}

		String dateDateStr = sdf.format(dateDate);
		logger.info("开始备份审核表数据,dateDate={},线程数{},单任务(线程)最大处理记录数{}",dateDateStr,threadSize,size);
		long queryAllAuditBegin = System.currentTimeMillis();
		auditToRemoves = auditService.queryAllRemoveAuditidAndCreatetime(dateDate);
		long queryAllAuditEnd = System.currentTimeMillis();
		long queryAllAuditCost = queryAllAuditEnd-queryAllAuditBegin;


		AtomicLong auditAllBackupTime = new AtomicLong();
//		long insertAllAuditBegin = System.currentTimeMillis();
//		for(JsmsAuditidAndCreatetime atr:auditToRemoves){
//			long oneAuditRecordBegin = System.currentTimeMillis();
//			this.backupAudit(atr.getAuditid(),atr.getCreatetime());
//			long oneAuditRecordEnd = System.currentTimeMillis();
//			logger.debug("backup one audit cost {}ms",(oneAuditRecordEnd-oneAuditRecordBegin));
//		}
//		long insertAllAuditEnd = System.currentTimeMillis();
		List<Future<String>> futureAudit = new ArrayList<>();
		for(int offset=0;offset<auditToRemoves.size();offset=offset+size){
			int s = Math.min(size,auditToRemoves.size()-offset);
			Future<String> future = AuditBakExecutorService.submit(new BackupAudit(offset,s,auditAllBackupTime));
			futureAudit.add(future);
		}
		for(Future<String> future:futureAudit){
			try {
				logger.debug("futureAudit result={}",future.get());
			} catch (Exception e) {
				logger.error("get futureAudit fail");
			}
		}
		long auditCost = System.currentTimeMillis()-queryAllAuditBegin;
		logger.info("结束备份审核表数据,dateDate={},总数{},总用时{}ms,cpu用时{}ms,其中queryAll={}ms,insertAll={}ms",dateDateStr,auditToRemoves.size(),auditCost,auditAllBackupTime.get()+queryAllAuditCost,queryAllAuditCost,auditAllBackupTime.get());
		auditToRemoves.clear();
		auditToRemoves = null;
		if(AuditBakExecutorService.isShutdown){
			logger.error("tomcat已关闭，审核备份任务停止");
			return false;
		}

		while(!AuditBakExecutorService.canRunning()){
			logger.info("运行时间不满足(休眠5分钟),配置为{}-{}",ConfigUtils.audit_sms_bak_running_begin,ConfigUtils.audit_sms_bak_running_end);
			try {
				Thread.sleep(300 * 1000);
			}catch (InterruptedException e){
				return false;
			}
		}

		logger.info("开始备份审核短信表数据,dateDate={}",dateDateStr);
		long queryAllAuditSmsBegin = System.currentTimeMillis();
		auditSmsToRemoves = auditSmsService.queryAllRemoveIdAndCreatetime(dateDate);
		long queryAllAuditSmsEnd = System.currentTimeMillis();
		long queryAllAuditSmsCost = queryAllAuditSmsEnd - queryAllAuditSmsBegin;

		AtomicLong auditSmsAllBackTime = new AtomicLong();
//		long insertAllAuditSmsBegin = System.currentTimeMillis();
//		for(JsmsIdAndCreatetime iac:auditSmsToRemoves){
//			long oneAuditSmsRecordBegin = System.currentTimeMillis();
//			this.backupAuditSms(iac.getId(),iac.getCreatetime());
//			long oneAuditSmsRecordEnd = System.currentTimeMillis();
//			logger.debug("backup one auditSms cost {}ms",(oneAuditSmsRecordEnd-oneAuditSmsRecordBegin));
//		}
//		long insertAllAuditSmsEnd = System.currentTimeMillis();


		List<Future<String>> futureAuditSms = new ArrayList<>();
		for(int offset=0;offset<auditSmsToRemoves.size();offset=offset+size){
			int s = Math.min(size,auditSmsToRemoves.size()-offset);
			Future<String> future = AuditBakExecutorService.submit(new BackupAuditSms(offset,s,auditSmsAllBackTime));
			futureAuditSms.add(future);
		}
		for(Future<String> future:futureAuditSms) {
			try {
				logger.debug("futureAuditSms result={}", future.get());
			} catch (Exception e) {
				logger.error("get futureAuditSms fail");
			}
		}
		long auditSmsCost = System.currentTimeMillis()-queryAllAuditSmsBegin;
		logger.info("结束备份审核短信表数据,dateDate={},总数{},总用时{}ms,cpu用时{}ms,其中queryAll={}ms,insertAll={}ms",dateDateStr,auditSmsToRemoves.size(),auditSmsCost,auditSmsAllBackTime.get()+queryAllAuditSmsCost,queryAllAuditSmsCost,auditSmsAllBackTime.get());
		auditSmsToRemoves.clear();
		auditSmsToRemoves = null;



		if(AuditBakExecutorService.isShutdown){
			logger.error("tomcat已关闭，审核备份任务停止");
			return false;
		}

		logger.info("【每日审核记录备份】结束,task时间={},总用时{}ms", excuteNextStr,(System.currentTimeMillis()-begin));
		return true;
	}

	@Override
	@Transactional("access")
	public void backupAuditSms(Long id, Date createtime) {
		long qBegin = System.currentTimeMillis();
		JsmsAuditSms jsmsAuditSms = auditSmsService.getByIdAndCreatetime(id,createtime);
		long qEnd = System.currentTimeMillis();
		logger.debug("query auditSms cost {}ms",(qEnd-qBegin));
		logger.debug("backup auditsms={}",JacksonUtil.toJSON(jsmsAuditSms));

		JsmsAuditSmsBak jsmsAuditSmsBak = new JsmsAuditSmsBak();
		BeanUtil.copyProperties(jsmsAuditSms,jsmsAuditSmsBak);
		long iBegin = System.currentTimeMillis();
		int i = jsmsAuditSmsBakService.insert(jsmsAuditSmsBak);
		if(i!=1){
			throw new JsmsAuditException("备份auditSms数据失败[insert阶段]audit="+ JacksonUtil.toJSON(jsmsAuditSms));
		}
		long iEnd = System.currentTimeMillis();
		logger.debug("insert auditSmsBak cost {}ms",(iEnd-iBegin));

		long dBegin = System.currentTimeMillis();
		int d = auditSmsService.deleteByIdAndCreatetime(id,createtime);
		long dENd = System.currentTimeMillis();
		logger.debug("delete audit cost {}ms",(dENd-dBegin));

		if(d!=1){
			throw new JsmsAuditException("备份auditSms数据失败[delete阶段]audit="+ JacksonUtil.toJSON(jsmsAuditSms));
		}

	}


	@Override
	@Transactional("access")
	public void backupAudit(Long auditid, Date createtime){
		long qBegin = System.currentTimeMillis();
		JsmsAudit jsmsAudit = auditService.getByAuditidAndCreatetime(auditid,createtime);
		long qEnd = System.currentTimeMillis();
		logger.debug("query audit cost {}ms",(qEnd-qBegin));
		logger.debug("backup audit={}",JacksonUtil.toJSON(jsmsAudit));

		JsmsAuditBak jsmsAuditBak = new JsmsAuditBak();
		BeanUtil.copyProperties(jsmsAudit,jsmsAuditBak);
		long iBegin = System.currentTimeMillis();
//		int i =jsmsAuditBakService.insert(jsmsAuditBak);
		String tableName = "t_sms_audit_"+ new DateTime(DateUtil.getThisWeekMonday(jsmsAudit.getCreatetime())).toString("yyyyMMdd");
		int i = jsmsAuditBakService.insertWithTableName(jsmsAuditBak,tableName);
		if(i!=1){
			throw new JsmsAuditException("备份audit数据失败[insert阶段]audit="+ JacksonUtil.toJSON(jsmsAudit));
		}
		long iEnd = System.currentTimeMillis();
		logger.debug("insert auditBak cost {}ms",(iEnd-iBegin));

		long dBegin = System.currentTimeMillis();
		int d = auditService.deleteByAuditidAndCreatetime(auditid,createtime);
		long dENd = System.currentTimeMillis();
		logger.debug("delete audit cost {}ms",(dENd-dBegin));
		if(d!=1){
			throw new JsmsAuditException("备份audit数据失败[delete阶段]audit="+ JacksonUtil.toJSON(jsmsAudit));
		}
	}



	class BackupAudit implements Callable<String> {

		int offset;
		int size;
		AtomicLong auditAllBackTime;

		public BackupAudit(int offset, int size,AtomicLong auditAllBackupTime) {
			this.offset = offset;
			this.size = size;
			this.auditAllBackTime = auditAllBackupTime;
		}

		@Override
		public String call() throws Exception {
			int successCount = 0;
			int failCount = 0;
			String msg="";
			List<Long> toBeRemoves = new ArrayList<>();
			for(int i=offset;i<offset+size;i++){
				if(AuditBakExecutorService.isShutdown) { //容器停止时则停止任务
					msg = interruptTask(toBeRemoves, i);
					break;
				}
				while(!AuditBakExecutorService.canRunning()){
					logger.info("运行时间不满足(休眠5分钟),配置为{}-{}",ConfigUtils.audit_sms_bak_running_begin,ConfigUtils.audit_sms_bak_running_end);
					try {
						Thread.sleep(300 * 1000);
					}catch (InterruptedException e){
						msg = interruptTask(toBeRemoves, i);
						break;
					}
				}


				try {
					JsmsAuditidAndCreatetime atr = auditToRemoves.get(i);
					long oneAuditRecordBegin = System.currentTimeMillis();
					backupAudit(atr.getAuditid(), atr.getCreatetime());
					long oneAuditRecordEnd = System.currentTimeMillis();
					long cost = oneAuditRecordEnd - oneAuditRecordBegin;
					logger.debug("backup one audit cost {}ms", cost);
					auditAllBackTime.addAndGet(cost);
				}catch (Exception e){
					logger.error("backupAudit task fail one record",e);
					failCount++;
					continue;
				}
				successCount++;
			}

			return size+","+successCount+","+failCount+","+msg+toBeRemoves;
		}

		private String interruptTask(List<Long> toBeRemoves, int i) {
			String msg="";
			msg="task interrupted, not remove auditids[]=";
			for(int k=i;k<offset+size;k++){
                try {
                    JsmsAuditidAndCreatetime atr = auditToRemoves.get(k);
                    toBeRemoves.add(atr.getAuditid());
                }catch (Exception e){
                    logger.error("",e);
                    continue;
                }
            }
			return msg;
		}
	}
	class BackupAuditSms implements Callable<String> {
		int offset;
		int size;
		AtomicLong auditSmsAllBackTime;

		public BackupAuditSms(int offset, int size,AtomicLong auditSmsAllBackTime) {
			this.offset = offset;
			this.size = size;
			this.auditSmsAllBackTime = auditSmsAllBackTime;
		}

		@Override
		public String call() throws Exception {
			int successCount = 0;
			int failCount = 0;
			String msg="";
			List<Long> toBeRemoves = new ArrayList<>();
			for(int i=offset;i<offset+size;i++){
				if(AuditBakExecutorService.isShutdown) { //容器停止时则停止任务
					msg = interruptTask(toBeRemoves, i);
					break;
				}
				while(!AuditBakExecutorService.canRunning()){
					logger.info("运行时间不满足(休眠5分钟),配置为{}-{}",ConfigUtils.audit_sms_bak_running_begin,ConfigUtils.audit_sms_bak_running_end);
					try {
						Thread.sleep(300 * 1000);
					}catch (InterruptedException e){
						msg = interruptTask(toBeRemoves, i);
						break;
					}
				}
				try {
					JsmsIdAndCreatetime atr = auditSmsToRemoves.get(i);
					long oneAuditSmsRecordBegin = System.currentTimeMillis();
					backupAuditSms(atr.getId(), atr.getCreatetime());
					long oneAuditSmsRecordEnd = System.currentTimeMillis();
					long cost = oneAuditSmsRecordEnd - oneAuditSmsRecordBegin;
					logger.debug("backup one auditSms cost {}ms", cost);
					auditSmsAllBackTime.addAndGet(cost);
				}catch (Exception e){
					logger.error("backupAuditSms task fail one record",e);
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
					JsmsIdAndCreatetime atr = auditSmsToRemoves.get(k);
					toBeRemoves.add(atr.getId());
				} catch (Exception e) {
					logger.error("", e);
					continue;
				}
			}
			return msg;
		}
	}
}
