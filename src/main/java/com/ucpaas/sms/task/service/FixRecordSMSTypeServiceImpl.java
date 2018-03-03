package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.dao.AccessMasterDao;
import com.ucpaas.sms.task.dao.RecordMasterDao;
import com.ucpaas.sms.task.entity.message.Account;
import com.ucpaas.sms.task.mapper.message.AccountMapper;
import com.ucpaas.sms.task.model.TaskInfo;
import com.ucpaas.sms.task.util.DateUtilsNew;
import com.ucpaas.sms.task.util.JacksonUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 修复Record的SMSType
 * 
 */
@Service
public class FixRecordSMSTypeServiceImpl implements FixRecordSMSTypeService {

	@Autowired
	private AccountMapper accountMapper;

	@Autowired
	private RecordMasterDao recordMasterDao;

	@Autowired
	private AccessMasterDao accessMasterDao;

	private static final Logger logger = LoggerFactory.getLogger("FixRecordSMSTypeService");

	/**
	 * 修复Record的smstype
	 * 
	 * @return 是否成功
	 */
	public boolean fixSMSType(TaskInfo taskInfo) {
		Calendar begin = Calendar.getInstance();
		logger.debug("【修复Record的SMSType】开始 = {}", DateUtilsNew.formatDateTime(begin.getTime()));

		Map<String, Object> sqlParams = new HashMap<>();

		Map<String, String> clients = new HashMap<>();

		// 修复20170801 - 20170809 之间的所有表的短信类型
		String prefix = "2017080";
		int size = 50000;
		for (int i = 1; i < 10; i++) {
			String date = prefix + i;
			logger.debug("【修复Record的SMSType】==============");
			logger.debug("【修复Record的SMSType】开始处理{}的记录", date);

			for (int j = 0; j < 10; j++) {
				logger.debug("【修复Record的SMSType】============================");
				logger.debug("【修复Record的SMSType】开始处理{}的{}表记录", date, j);

				String table = j + "_" + date;

				sqlParams.clear();
				sqlParams.put("table", table);

				// 查询表记录
				Map<String, Object> objectMap = recordMasterDao.selectOne("fixRecordSMSType.count", sqlParams);

				int totalCount = Integer.parseInt(objectMap.get("count").toString());

				logger.debug("【修复Record的SMSType】总记录数 {}", totalCount);

				int totalDealCount = 0;

				// 处理数量小于总数的时候继续处理
				int dealCount = 0;
				while (dealCount < totalCount) {
					// 查询限制参数
					String limit = dealCount + "," + size;

					sqlParams.clear();
					sqlParams.put("table", table);
					sqlParams.put("limit", limit);
					List<Map<String, Object>> records = recordMasterDao.selectList("fixRecordSMSType.findRecordList",
							sqlParams);
					if (records != null && records.size() > 0) {
						for (int k = 0; k < records.size(); k++) {
							Map<String, Object> record = records.get(k);
							if (record == null) {
								continue;
							}

							// 若存在smstype， 跳过
							String smstype = record.get("smstype") == null ? null : record.get("smstype").toString();
							if (StringUtils.isNotBlank(smstype)) {
								continue;
							}

							String smsuuid = record.get("smsuuid") == null ? null : record.get("smsuuid").toString();
							if (StringUtils.isBlank(smsuuid)) {
								continue;
							}

							String smsfrom = record.get("smsfrom") == null ? null : record.get("smsfrom").toString();
							if (StringUtils.isBlank(smsfrom)) {
								logger.debug("【修复Record的SMSType】开始处理{}的{}表的记录{}, 的smsfrom为空，跳过", date, j, smsuuid);
								continue;
							}

							String clientid = record.get("clientid") == null ? null : record.get("clientid").toString();
							if (StringUtils.isBlank(clientid)) {
								logger.debug("【修复Record的SMSType】开始处理{}的{}表的记录{}的clientid为空，跳过", date, j, smsuuid);
								continue;
							}

							if (smsfrom.equals("6")) {
								String channelid = record.get("channelid") == null ? null
										: record.get("channelid").toString();
								if (StringUtils.isBlank(channelid)) {
									logger.debug("【修复Record的SMSType】开始处理{}的{}表的记录{}的channelid为空，跳过", date, j, smsuuid);
									continue;
								}

								String smsid = record.get("smsid") == null ? null : record.get("smsid").toString();
								if (StringUtils.isBlank(smsid)) {
									logger.debug("【修复Record的SMSType】开始处理{}的{}表的记录{}的smsid为空，跳过", date, j, smsuuid);
									continue;
								}

								String phone = record.get("phone") == null ? null : record.get("phone").toString();
								if (StringUtils.isBlank(phone)) {
									logger.debug("【修复Record的SMSType】开始处理{}的{}表的记录{}的phone为空，跳过", date, j, smsuuid);
									continue;
								}

								// 查询Access表的记录
								sqlParams.clear();
								Account account = accountMapper.getByClientid(clientid);
								sqlParams.put("table", account.getIdentify()+ "_" + date);
								sqlParams.put("channelid", channelid);
								sqlParams.put("clientid", clientid);
								sqlParams.put("smsid", smsid);
								sqlParams.put("phone", phone);

								Map<String, Object> objectMap1 = accessMasterDao
										.selectOne("fixRecordSMSType.getAccessSMSType", sqlParams);
								if(objectMap1==null){
									logger.debug("此record找不到对应的acess，record={}", JacksonUtil.toJSON(record));
									continue;
								}


								String accessSMSType = objectMap1.get("smstype") == null ? null
										: objectMap1.get("smstype").toString();

								if (StringUtils.isBlank(accessSMSType)) {
									logger.debug("【修复Record的SMSType】开始处理{}的{}表的记录{}的clientid对应access的smstype为空，跳过",
											date, j, smsuuid);
									continue;
								}

								// 更新短信类型
								sqlParams.clear();
								sqlParams.put("table", table);
								sqlParams.put("smstype", accessSMSType);
								sqlParams.put("smsuuid", smsuuid);
								recordMasterDao.update("fixRecordSMSType.updateRecordSMSType", sqlParams);
								totalDealCount++;

							} else {
								// 查询客户
								String clientSMSType = clients.get(clientid);
								if (StringUtils.isBlank(clientSMSType)) {
									clientSMSType = accountMapper.getSMSTypeByClientid(clientid);
									clients.put(clientid, clientSMSType);
								}

								if (StringUtils.isBlank(clientSMSType)) {
									logger.debug("【修复Record的SMSType】开始处理{}的{}表的记录{}的clientid对应的smstype为空，跳过", date, j,
											smsuuid);
									continue;
								}

								// 更新短信类型
								sqlParams.clear();
								sqlParams.put("table", table);
								sqlParams.put("smstype", clientSMSType);
								sqlParams.put("smsuuid", smsuuid);
								recordMasterDao.update("fixRecordSMSType.updateRecordSMSType", sqlParams);
								totalDealCount++;
							}
						}
					}

					// 处理数量需要加上每次处理的限制
					dealCount += size;
				}

				logger.debug("【修复Record的SMSType】总处理记录数 {}", totalDealCount);

				logger.debug("【修复Record的SMSType】结束处理{}的{}表记录", date, j);
				logger.debug("【修复Record的SMSType】============================");
			}

			logger.debug("【修复Record的SMSType】结束处理{}的记录", date);
			logger.debug("【修复Record的SMSType】==============");
		}

		Calendar end = Calendar.getInstance();
		logger.debug("【修复Record的SMSType】结束 = {}", DateUtilsNew.formatDateTime(end.getTime()));
		return true;
	}

}
