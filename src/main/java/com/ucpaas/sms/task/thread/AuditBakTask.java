package com.ucpaas.sms.task.thread;

import com.alibaba.fastjson.JSON;
import com.jsmsframework.audit.entity.JsmsAudit;
import com.jsmsframework.audit.entity.JsmsAuditBak;
import com.jsmsframework.audit.entity.JsmsAuditSms;
import com.jsmsframework.audit.entity.JsmsAuditSmsBak;
import com.jsmsframework.audit.service.JsmsAuditBakService;
import com.jsmsframework.audit.service.JsmsAuditService;
import com.jsmsframework.audit.service.JsmsAuditSmsBakService;
import com.jsmsframework.audit.service.JsmsAuditSmsService;
import com.ucpaas.sms.common.util.Collections3;
import com.ucpaas.sms.task.util.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.Future;

@Component
public class AuditBakTask {

	private static final Logger logger = LoggerFactory.getLogger("AutitSmsBakService");

	@Autowired
	private JsmsAuditService auditService;

	@Autowired
	private JsmsAuditBakService auditBakService;

	@Autowired
	private JsmsAuditSmsService auditSmsService;

	@Autowired
	private JsmsAuditSmsBakService auditSmsBakService;

	private void addAndDelAudit(List<JsmsAudit> audits) {
		List<Long> ids = null;
		for (JsmsAudit audit : audits) {
			try {
				int i = auditBakService.insertFromAudit(audit);
				if (i > 0) {
					int j = auditService.delete(audit.getAuditid());
					if (j <= 0) {
						logger.error("删除审核记录失败： auditid = {}", audit.getAuditid());

						// 若删除主表失败，就将备份表的记录也删除
						auditBakService.delete(audit.getAuditid());
					}
				}

				if (ids == null) {
					ids = new ArrayList<>();
				}
				ids.add(audit.getAuditid());
			} catch (Exception e) {
				logger.error("=====备份审核记录失败 异常信息 {}", e);
			}
		}

		logger.debug("=====备份审核记录 AuditId列表 {}", ids);
	}

	private void batchAddAndDelAudit(List<JsmsAudit> audits) {
		// 获取ID列表
		int[] ids = new int[audits.size()];
		for (int i = 0; i < audits.size(); i++) {
			ids[i] = audits.get(i).getAuditid().intValue();
		}

		// 批量插入备份表
		int addCount = 0;
		try {
			long start = System.currentTimeMillis();
			addCount = auditBakService.insertBatchFromAudit(audits);
			long end = System.currentTimeMillis();

			logger.debug("=====备份审核记录 批量插入成功 耗时{}", end - start);
		} catch (Exception e) {
			logger.error("=====备份审核记录 批量插入失败 异常信息 {} ID列表 {}", e, ids);
		}

		// 批量删除主表
		if (addCount == audits.size()) {
			try {
				long start = System.currentTimeMillis();
				auditService.batchDeleteAudit(ids);
				long end = System.currentTimeMillis();
				logger.debug("=====备份审核记录 批量删除成功 耗时{}", end - start);
			} catch (Exception e) {
				logger.error("=====备份审核记录 批量删除失败 异常信息 {} ID列表 {}", e, ids);
			}
		}

		logger.debug("=====备份审核记录 ID列表 {}", ids);
	}

	private void addAndDelAuditSms(List<JsmsAuditSms> auditSmsList) {
		List<Long> ids = null;
		for (JsmsAuditSms auditSms : auditSmsList) {
			try {
				int i = auditSmsBakService.insertFromAuditSms(auditSms);
				if (i > 0) {
					int j = auditSmsService.delete(auditSms.getId());
					if (j <= 0) {
						logger.error("删除审核明细记录失败： id = {}", auditSms.getId());

						// 若删除主表失败，就将备份表的记录也删除
						auditSmsBakService.delete(auditSms.getId());
					}
				}

				if (ids == null) {
					ids = new ArrayList<>();
				}
				ids.add(auditSms.getId());
			} catch (Exception e) {
				logger.error("≡≡≡≡≡备份审核明细记录失败 异常信息 {}", e);
			}
		}

		logger.debug("≡≡≡≡≡备份审核明细记录 Id列表 {}", ids);
	}

	private void batchAddAndDelAuditSms(List<JsmsAuditSms> auditSmsList) {
		// 获取ID列表
		int[] ids = new int[auditSmsList.size()];
		for (int i = 0; i < auditSmsList.size(); i++) {
			ids[i] = auditSmsList.get(i).getId().intValue();
		}

		// 批量插入备份表
		int addCount = 0;
		try {
			long start = System.currentTimeMillis();
			addCount = auditSmsBakService.insertBatchFromAuditSms(auditSmsList);
			long end = System.currentTimeMillis();
			logger.debug("=====备份审核明细记录 批量插入成功 耗时{}", end - start);
		} catch (Exception e) {
			logger.error("=====备份审核明细记录 批量插入失败 异常信息 {} ID列表 {}", e, ids);
		}

		// 批量删除主表
		if (addCount == auditSmsList.size()) {
			try {
				long start = System.currentTimeMillis();
				auditSmsService.batchDeleteAuditSms(ids);
				long end = System.currentTimeMillis();
				logger.debug("=====备份审核明细记录 批量删除成功 耗时{}", end - start);
			} catch (Exception e) {
				logger.error("=====备份审核明细记录 批量删除失败 异常信息 {} ID列表 {}", e, ids);
			}
		}

		logger.debug("=====备份审核明细记录 ID列表 {}", ids);
	}

	@Async
	public Future<String> doBakAudit(int min, int max) {
		int size = Integer.valueOf(ConfigUtils.audit_sms_bak_batch_count);

		long start = System.currentTimeMillis();
		logger.debug("=====开始备份审核记录 时间{} 开始ID= {} 结束ID= {}", start, min, max);

		int dealCount = min;
		while (dealCount < max) {
			// 若当前处理数量 + 本次处理处理 大于 最大数
			int dealSize = (dealCount + size) > max ? max - dealCount : size;

			// 一次处理200条记录
			List<JsmsAudit> audits = auditService.findNeedBakList(dealCount, dealCount + dealSize);
			if (audits == null || audits.size() <= 0) {
				dealCount += dealSize;
				continue;
			}

			batchAddAndDelAudit(audits);

			dealCount += dealSize;
		}
		long end = System.currentTimeMillis();
		logger.debug("=====结束备份记录 时间{} 开始ID= {} 结束ID= {}", end, min, max);

		return new AsyncResult<>("任务一完成");
	}

	@Async
	public Future<String> doBakAuditSMS(int min, int max) {
		// 循环一万条处理
		int size = Integer.valueOf(ConfigUtils.audit_sms_bak_batch_count);

		long start = System.currentTimeMillis();
		logger.debug("≡≡≡≡≡开始备份审核明细 时间{} 开始ID{} 结束ID{}", start, min, max);

		int dealCount = min;
		while (dealCount < max) {
			// 若当前处理数量 + 本次处理处理 大于 最大数
			int dealSize = (dealCount + size) > max ? max - dealCount : size;

			// 一次处理200条记录
			List<JsmsAuditSms> auditSmsList = auditSmsService.findNeedBakList(dealCount, dealCount + dealSize);
			if (auditSmsList == null || auditSmsList.size() <= 0) {
				dealCount += dealSize;
				continue;
			}

			batchAddAndDelAuditSms(auditSmsList);

			dealCount += dealSize;
		}
		long end = System.currentTimeMillis();
		logger.debug("≡≡≡≡≡结束备份审核明细 时间{} 开始ID= {} 结束ID= {}", end, min, max);

		return new AsyncResult<>("任务一完成");
	}

	@Async
	public Future<String> doDealRepeatRecord(int threadCount) {
		int size = Integer.valueOf(ConfigUtils.audit_sms_bak_batch_count);

		long start = System.currentTimeMillis();
		logger.debug("≡≡≡≡≡开始处理重复记录 时间{}", start);

		int dealCount = 0;

		// 审核表主表未删除记录
		List<Long> auditNotDel = auditService.hasBakButNotDel();
		Set<Integer> hasDealList = new HashSet<>();
		if (!Collections3.isEmpty(auditNotDel)) {
			int tempDealAuditNotDel = auditNotDel.size();
			while (tempDealAuditNotDel > 0) {
				// 清空已处理数量
				hasDealList.clear();
				if (dealCount >= threadCount) {
					break;
				}

				// 本次处理数量
				int currDeal = tempDealAuditNotDel > size ? size : tempDealAuditNotDel;
				dealCount += currDeal; // 总处理数量累加

				int count = 0;
				Iterator iter = auditNotDel.iterator();
				while (iter.hasNext()) {
					count++;
					Long id = (Long) iter.next();
					hasDealList.add(id.intValue());
					iter.remove();

					// 处理数量大于当前批次处理数量的时候跳出
					if (count >= currDeal) {
						break;
					}
				}

				// 转换为数组
				Integer[] ids = hasDealList.toArray(new Integer[hasDealList.size()]);
				int[] idsArr = new int[ids.length];
				for (int i = 0; i < ids.length; i++) {
					idsArr[i] = ids[i];
				}
				auditService.batchDeleteAudit(idsArr);

				logger.debug("≡≡≡≡≡删除已备份的审核记录 id {}", idsArr);
				tempDealAuditNotDel = tempDealAuditNotDel - currDeal;
			}
		}

		long end;
		if (dealCount >= threadCount) {
			end = System.currentTimeMillis();
			logger.debug("≡≡≡≡≡结束处理重复记录 时间{}", end);
			return new AsyncResult<>("任务一完成");
		}

		// 审核备份重复记录
		List<JsmsAuditBak> auditBakList = auditBakService.findRepeatList();
		if (!Collections3.isEmpty(auditBakList)) {
			for (JsmsAuditBak jsmsAuditBak : auditBakList) {
				// 删除所有
				auditBakService.delete(jsmsAuditBak.getAuditid());

				// 再插入
				int addAuditBakCount = auditBakService.insert(jsmsAuditBak);
				if (addAuditBakCount <= 0) {
					logger.debug("≡≡≡≡≡t_sms_audit_bak重复记录删除仅插入一条记录失败 {}", JSON.toJSONString(jsmsAuditBak));
				}

				dealCount++;
			}
		}

		if (dealCount >= threadCount) {
			end = System.currentTimeMillis();
			logger.debug("≡≡≡≡≡结束处理重复记录 时间{}", end);
			return new AsyncResult<>("任务一完成");
		}

		// 审核表主表未删除记录
		List<Long> auditSmsNotDel = auditSmsService.hasBakButNotDel();
		if (!Collections3.isEmpty(auditSmsNotDel)) {
			int tempDealAuditNotDel = auditSmsNotDel.size();
			while (tempDealAuditNotDel > 0) {
				// 清空已处理数量
				hasDealList.clear();
				if (dealCount >= threadCount) {
					break;
				}

				// 本次处理数量
				int currDeal = tempDealAuditNotDel > size ? size : tempDealAuditNotDel;
				dealCount += currDeal; // 总处理数量累加

				int count = 0;
				Iterator iter = auditSmsNotDel.iterator();
				while (iter.hasNext()) {
					count++;
					Long id = (Long) iter.next();
					hasDealList.add(id.intValue());
					iter.remove();

					// 处理数量大于当前批次处理数量的时候跳出
					if (count >= currDeal) {
						break;
					}
				}

				// 转换为数组
				Integer[] ids = hasDealList.toArray(new Integer[hasDealList.size()]);
				int[] idsArr = new int[ids.length];
				for (int i = 0; i < ids.length; i++) {
					idsArr[i] = ids[i];
				}
				auditSmsService.batchDeleteAuditSms(idsArr);

				logger.debug("≡≡≡≡≡删除已备份的审核记录 id {}", idsArr);
				tempDealAuditNotDel = tempDealAuditNotDel - currDeal;
			}
		}

		// 审核明细主表未删除记录
		if (dealCount >= threadCount) {
			end = System.currentTimeMillis();
			logger.debug("≡≡≡≡≡结束处理重复记录 时间{}", end);
			return new AsyncResult<>("任务一完成");
		}

		// 审核明细备份重复记录
		List<JsmsAuditSmsBak> auditBakSmsList = auditSmsBakService.findRepeatList();
		if (!Collections3.isEmpty(auditBakSmsList)) {
			for (JsmsAuditSmsBak jsmsAuditSmsBak : auditBakSmsList) {
				// 删除所有
				auditSmsBakService.delete(jsmsAuditSmsBak.getId());

				// 再插入
				int addAuditSmsBakCount = auditSmsBakService.insert(jsmsAuditSmsBak);
				if (addAuditSmsBakCount <= 0) {
					logger.debug("≡≡≡≡≡t_sms_audit_sms_bak重复记录删除仅插入一条记录失败 {}", JSON.toJSONString(jsmsAuditSmsBak));
				}

				dealCount++;
			}
		}

		end = System.currentTimeMillis();
		logger.debug("≡≡≡≡≡结束处理重复记录 时间{}", end);
		return new AsyncResult<>("任务一完成");
	}

}
