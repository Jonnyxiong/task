package com.ucpaas.sms.task.thread;

import com.ucpaas.sms.task.constant.TaskConstant.ExecuteType;
import com.ucpaas.sms.task.constant.TaskConstant.TaskStatus;
import com.ucpaas.sms.task.model.TaskInfo;
import com.ucpaas.sms.task.service.*;
import com.ucpaas.sms.task.service.common.CommonService;
import com.ucpaas.sms.task.service.common.TaskRunService;
import com.ucpaas.sms.task.task.TableTask;
import com.ucpaas.sms.task.util.TaskUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import java.util.List;

/**
 * 任务处理线程
 * 
 * @author xiejiaan
 */
public class ProcessThread implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(ProcessThread.class);
	private List<TaskInfo> taskList;
	@Autowired
	private TaskRunService taskService;
	@Autowired
	private CommonService commonService;
	@Autowired
	private WarningService warningService;
	@Autowired
	private AgentTaskService agentTaskService;
	@Autowired
	private ClientOrderStatusService clientOrderStatusService;
	@Autowired
	private ChannelQaRealTimeService channelQaRealTimeService;
	@Autowired
	private ChannelQaHistoryService channelQaHistoryService;
	@Autowired
	private SendReprotMonthService sendReprotMonthService;
	@Autowired
	private ClientPoolStatusService clientPoolStatusService;
	@Autowired
	private AliService aliService;
	@Autowired
	private MajorClientsService majorClientsService;
	@Autowired
	private RecordChannelStatisticsService recordChannelStatisticsService;
	@Autowired
	private HalfHourAccessRecordService halfHourAccessRecordService;
	@Autowired
	private AccessChannelStatisticsService accessChannelStatisticsService;
	@Autowired
	private AgentPoolStatusService agentPoolStatusService;
	@Autowired
	private OemAgentClientChargeService oemAgentClientChargeService;
	@Autowired
	private AgentBalanceBillService agentBalanceBillService;
	@Autowired
	private FixRecordSMSTypeService fixRecordSMSTypeService;
	@Autowired
	private AuditSmsBakService autitSmsBakService;
	@Autowired
	private SmsSaleStatMailReportService smsSaleStatMailReportService;
	@Autowired
	private RealTimeChannelSuccessRateWeightUpdateService realTimeChannelSuccessRateWeightUpdateService;
	@Autowired
	private FixAgentCreditHisService fixAgentCreditHisService;
	@Autowired
	private BalanceAlarmService balanceAlarmService;
	@Autowired
	private AgentOnlinePayCancelService agentOnlinePayCancelService;

	@Autowired
	private AgentOnlinePayFailService agentOnlinePayFailService;
	@Autowired
	private MonitorService monitorService;
	@Autowired
	private AuditKeywordRecordBakService auditKeywordRecordBakService;




	@Autowired
	private ComplaintChannelStatisticsService complaintChannelStatisticsService;



	public ProcessThread(List<TaskInfo> taskList) {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);// 手动注入@Autowired修饰的bean
		this.taskList = taskList;
	}

	@Override
	public void run() {
		if (taskList != null && taskList.size() > 0) {
			logger.debug("分组任务开始：taskList={}", taskList);

			for (TaskInfo taskInfo : taskList) {
				runTask(taskInfo);
			}
			logger.debug("分组任务结束：taskList={}", taskList);
		}
	}

	/**
	 * 执行任务
	 * 
	 * @param taskInfo
	 */
	public void runTask(TaskInfo taskInfo) {
		boolean result = false;
		try {
			do {
				logger.debug("执行任务【开始】：taskInfo={}", taskInfo);
				Long logId = taskService.insertLog(taskInfo, null);

				switch (taskInfo.getTaskType()) {
					case procedure:// 调用存储过程
						result = commonService.callProcedure(taskInfo);
						break;
					case warning:// 预警任务
						result = warningService.execute();
						break;
					case agent_commision_compute:// 代理商佣金计算
						result = agentTaskService.commissionCompute(taskInfo);
						break;
					case clientid_seq_release:// clientid 序列释放锁
						result = commonService.releaseClientIdExpiredLock();
						break;
					case create_table:// 每日建分区表
						result = commonService.createTable(taskInfo);
						break;
					case access_stat:// 每日统计ACCESS报表（第4天前的数据）
						result = accessChannelStatisticsService.fourDaysAgo(taskInfo);
						break;
					case access_stat_yesterday:// 每日统计ACCESS报表（昨日数据）
						result = accessChannelStatisticsService.yesterday(taskInfo);
						break;
					case client_order_status:// 检查短信订单状态
						result = clientOrderStatusService.execute(taskInfo);
						break;
					case agent_rebate_compute:// 每季度代理商返利计算
						result = agentTaskService.rebateCompute(taskInfo);
						break;
					case client_send_speed:// 每5分钟客户短信发送速率统计
						result = channelQaRealTimeService.clientSendSpeed(taskInfo);
						break;
					case mq_queue_message_stat:// ACCESS队列消息数量监控
						result = channelQaRealTimeService.mqQueueMessageStat(taskInfo);
						break;
					case channel_quality_indexes_stat:// 每5分钟短信通道质量指数统计
						result = channelQaRealTimeService.channelQualityIndexesStat(taskInfo);
						break;
					case channel_error_stat:// 每5分钟短信通道错误统计
						result = channelQaRealTimeService.channelErrorStat(taskInfo);
						break;
					case client_quality_indexes_stat:// 客户发送质量指数统计
						result = channelQaRealTimeService.clientQualityIndexesStat(taskInfo);
						break;
					case channel_quality_indexes_history_stat:// 通道历史质量指数统计
						result = channelQaHistoryService.channelQualityIndexesStat(taskInfo);
						break;
					case send_reprot_month:
						result = sendReprotMonthService.sendReprotMonth(taskInfo);
						break;
					case client_quality_indexes_history_stat:// 用户历史质量指数统计
						result = channelQaHistoryService.clientQualityIndexesStat(taskInfo);
						break;
					case client_success_rate_realtime_stat:// 用户成功率统计
						result = channelQaRealTimeService.clientSuccessRateRealtime(taskInfo);
						break;
					case channel_success_rate_realtime_stat:// 通道成功率统计
						result = channelQaRealTimeService.channelSuccessRateRealtime(taskInfo);
						break;
					case client_pool_status:// OEM客户短信池过期池的状态处理，每天
						result = clientPoolStatusService.execute(taskInfo);
						break;
					case ali_daily_detail:// 大客户阿里统计报表任务, 大客户阿里每日发送详情
						result = aliService.dailyDetail(taskInfo);
						break;
					case channel_stat:// 通道运维运营报表统计（第4天前的数据）
						result = recordChannelStatisticsService.fourDaysAgo(taskInfo);
						break;
					case channel_stat_yesterday:// 通道运维运营报表统计（昨日数据）
						result = recordChannelStatisticsService.yesterday(taskInfo);
						break;
					case half_hour_access_record_service:
						result = halfHourAccessRecordService.doTheNewJob(taskInfo); //每半小时统计流水并发邮件
						break;
					case prepayment_client_daily_detail:// 预付费客户统计报表任务, 预付费客户发送详情
						result = majorClientsService.prepaymentClientDailyDetail(taskInfo);
						break;
					case postpaid_client_daily_detail:// 后付费客户统计报表任务, 后付费客户发送详情
						result = majorClientsService.postpaidClientDailyDetail(taskInfo);
						break;
					case agent_pool_status:// OEM代理商短信池过期的状态处理，每天
						result = agentPoolStatusService.execute(taskInfo);
						break;
					case client_balance_alarm:	// 【预警】预付费客户余额预警
						// result = clientOrderStatusService.clientBalanceAlarm(taskInfo);
						result = balanceAlarmService.doAlarm(taskInfo);
						break;
					case fix_access_record_statistic:	// 【补偿】纠正用户销售和通道成本
						result = accessChannelStatisticsService.fixAccessRecordStatistic(taskInfo);
						break;
					case agent_balance_alarm:		// 【预警】代理商可用额度预警
						// result = agentBalanceBillService.agentBalanceAlarm(taskInfo);
						result = balanceAlarmService.doAlarm(taskInfo);
						break;
					case oemagent_houfufei_client_chargeOnAgent:// OEM代理商后付费计费，扣除代理商的余额
						result = oemAgentClientChargeService.houfufeiCharge(taskInfo);
						break;
					case fix_record_SMSType:// 修复Record的SMSType
						result = fixRecordSMSTypeService.fixSMSType(taskInfo);
						break;
					case sms_audit_bak:// 每日审核记录备份
//						result = autitSmsBakService.bakAudit(taskInfo);
						result = autitSmsBakService.bakAuditAndSms(taskInfo);
						break;
					case sms_sale_stat_email_report:// 每日短信销售情况邮件报表 v5.19.1.0
						result = smsSaleStatMailReportService.saleReportAndSend(taskInfo);
						break;
					case realtime_channel_success_rate_wieght_update:// 每日短信销售情况邮件报表 v5.14.1.0
						result = realTimeChannelSuccessRateWeightUpdateService.doTheJob(taskInfo);
						break;
					case fix_agentCredit_hisBill://修复代理商授信历史记录并生成对应销售授信账单 5.16.3
						result=fixAgentCreditHisService.fixAgentCreditHis(taskInfo);
						break;
                    case sunday_create_table_sms_audit:// 每周创建sms_audit表 5.17.3
                        result = commonService.sundayCreateTable(taskInfo,"t_sms_audit");
                        break;
					case oem_client_balance_alarm:// 预付费OEM客户余额预警 v5.17.0.0
						result = balanceAlarmService.doAlarm(taskInfo);
						break;
					case oem_agent_balance_alarm:// 【预警】OEM代理商可用额度预警 v5.17.0.0
						result = balanceAlarmService.doAlarm(taskInfo);
						break;
					case jsms_online_payment_pay_cancel:// 支付取消
						result = agentOnlinePayCancelService.execute(taskInfo);
						break;
					case jsms_online_payment_pay_fail:// 支付失败
						result = agentOnlinePayFailService.execute(taskInfo);
						break;
					case complain_statistics:
						result = complaintChannelStatisticsService.execute(taskInfo);// 投诉率
						break;
					case task_monitor:// 【邮件】每天监控定时任务执行情况 v5.18.0.0
						result = monitorService.taskMonitor(taskInfo);
						break;
					case app_monitor:// 【邮件】监控应用运行情况
						result = monitorService.appMonitor(taskInfo);
						break;
					case sms_audit_keyword_record_bak:
						result = auditKeywordRecordBakService.bakAuditKeywordRecord(taskInfo);//每天短信审核关键字数据备份 v5.17.7
						break;
					case sunday_create_table_sms_timer_send_phones:// 每周创建t_sms_timer_send_phones_xxx表
						result = commonService.weeklyCreateTable(taskInfo,"t_sms_timer_send_phones");
						break;
					case month_create_table_sms_send_audit_intercept:// 每月创建t_sms_send_audit_intercept_xxx表
						result = commonService.monthlyCreateTable(taskInfo, "t_sms_send_audit_intercept");
						break;
                    case month_create_t_sms_access_molog://【短信发送记录】客户上行明细(每月创建t_sms_access_molog表) v5.18.7
                        result = commonService.monthlyCreateTable(taskInfo,"t_sms_access_molog");
                        break;
                    case month_create_t_sms_record_molog: //【短信发送记录】通道上行明细(每月创建t_sms_record_molog表) v5.18.7
                        result = commonService.monthlyCreateTable(taskInfo,"t_sms_record_molog");
                        break;
					default:
						break;
				}
				
				taskService.updateLog(logId, result, null);
				logger.debug("执行任务【结束】：result={}, taskInfo={}", result, taskInfo);
				if (result) {
					if (taskInfo.getExecuteType() != ExecuteType.empty && taskInfo.getExecuteNextDate().isBeforeNow()) {
						taskService.updateExecuteNext(taskInfo);// 修改下次执行时间

						taskInfo.setExecuteNext(taskInfo.getNewExecuteNext());
						taskInfo = TaskUtils.setNewExecuteNext(taskInfo);
						if (taskInfo.isScanExecute() || taskInfo.getExecuteNextDate().isBeforeNow()) {
							continue;
						}
					}
				}
				break;
			} while (true);
		} catch (Throwable e) {
			logger.error("执行任务【失败】：taskInfo=" + taskInfo, e);
		} finally {
			if (result) {
				taskService.updateScanNext(taskInfo);// 修改下次扫描时间
			}
			Integer taskId = taskInfo.getTaskId();
			taskService.updateStatus(taskId, TaskStatus.enabled);
			TableTask.running_task_set.remove(taskId);
		}
	}

}
