package com.ucpaas.sms.task.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ucpaas.sms.task.constant.WarningConstant;
import com.ucpaas.sms.task.dao.MessageMasterDao;
import com.ucpaas.sms.task.service.common.EmailService;
import com.ucpaas.sms.task.util.api.RestUtils;

/**
 * 预警任务
 * 
 * @author xiejiaan
 */
@Service
public class WarningServiceImpl implements WarningService {
	private static final Logger logger = LoggerFactory.getLogger(WarningServiceImpl.class);
	@Autowired
	private MessageMasterDao ucpaasMessageDao;
	@Autowired
	private EmailService emailService;

	@Override
	public boolean execute() {
		logger.debug("预警任务【开始】");
		Map<String, Object> noticeUser = ucpaasMessageDao.getOneInfo("warning.getNoticeUser", null);
		if (noticeUser == null) {
			logger.debug("当前没有需要通知的用户");
			return true;
		}
		String mobile = noticeUser.get("mobile").toString();
		String email = noticeUser.get("email").toString();
		int alarmType = Integer.parseInt(noticeUser.get("alarm_type").toString());

		List<Map<String, Object>> warningList = ucpaasMessageDao.getSearchList("warning.checkChannelLatestWarning", null);// 获取通道预警信息
		for (Map<String, Object> warning : warningList) {
			Integer id = Integer.parseInt(warning.get("id").toString());
			String channelid = warning.get("channelid").toString();
			Double reachrate = Double.parseDouble(warning.get("reachrate").toString());
			Double timelyrate = Double.parseDouble(warning.get("timelyrate").toString());
			Double warnreachrate = Double.parseDouble(warning.get("warnreachrate").toString());
			Double warntimelyrate = Double.parseDouble(warning.get("warntimelyrate").toString());
			String datatime = warning.get("datatime").toString();
			
			// 发送告警频率范围内不再告警
			Map<String, Object> isNeedWarning = ucpaasMessageDao.getOneInfo("warning.checkWarningIsNeed", warning);
			if(isNeedWarning != null){
				logger.debug("短信通道在告警时间范围内发生预警，不发送短信和邮件");
				continue;
			}
			
			StringBuilder smsSb = new StringBuilder();
			if(reachrate < warnreachrate){
				smsSb.append("成功率");
				smsSb.append(reachrate);
				smsSb.append("%");
				smsSb.append("低于告警值");
				smsSb.append(warnreachrate);
				smsSb.append("%");
			}
			
			if(timelyrate < warntimelyrate){
				smsSb.append("，");
				smsSb.append("及时率");
				smsSb.append(timelyrate);
				smsSb.append("%");
				smsSb.append("低于告警值");
				smsSb.append(warntimelyrate);
				smsSb.append("%");
			}
			
			boolean smsResult = false;
			String msgContent = String.format(WarningConstant.warning_msg_content, channelid, smsSb, datatime);
			// alarmType:1、2、3分别代表Email、短信、短信&Email
			if(alarmType == 2 || alarmType == 3){
				// 发送短信
				if (RestUtils.postForChannel(mobile, msgContent)) {// 发送短信
					smsResult = true;
				}
				logger.debug("短信通道发生预警【发送短信】：mobile={},msgContent={}", mobile, msgContent);
			}
			
			boolean emailResult = false;
			if(alarmType == 1 || alarmType == 3){
				// 发送邮件
				String msgTitle = String.format(WarningConstant.warning_msg_title, channelid);
				emailResult = emailService.sendTextEmail(email, msgTitle, msgContent);// 发送Email
				logger.debug("短信通道发生预警【发送邮件】：email={}, msgContent={}", email, msgContent);
			}

			Map<String, Object> warnResult = new HashMap<String, Object>();
			warnResult.put("iswarn", alarmType);
			warnResult.put("id", id);
			warnResult.put("channelid", channelid);
			warnResult.put("datatime", datatime);
			if (smsResult || emailResult) {// 短信、Email有一个发送成功就算预警成功
				ucpaasMessageDao.update("warning.updateChannelWarn", warnResult);// 更新短信通道信息
				ucpaasMessageDao.delete("warning.deleteExpiredWarn", warnResult);// 删除过期的预警信息
			}
		}
		logger.debug("预警任务【结束】");
		return true;
	}
}
