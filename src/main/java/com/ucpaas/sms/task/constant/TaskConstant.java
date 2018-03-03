package com.ucpaas.sms.task.constant;

/**
 * 任务常量
 *
 * @author xiejiaan
 */
public class TaskConstant {

    /**
     * 任务类型
     *
     * @author xiejiaan
     */
    public enum TaskType {
        /**
         * 调用存储过程
         */
        procedure,
        /**
         * 每日建表
         */
        create_table,
        /**
         * 预警任务
         */
        warning,
        /**
         * 代理商佣金计算
         */
        agent_commision_compute,
        /**
         * 释放clientid序列锁
         */
        clientid_seq_release,
        /**
         * 客户运维运营报表数据统计(第三天的数据)
         */
        access_stat,
        /**
         * 检查短信订单状态
         */
        client_order_status,
        /**
         * 代理商返利计算
         */
        agent_rebate_compute,
        /**
         * 短信账号发送速率
         */
        client_send_speed,
        /**
         * 客户运维运营报表数据统计(昨日的数据)
         */
        access_stat_yesterday,
        /**
         * MQ队列消息数统计
         */
        mq_queue_message_stat,
        /**
         * 短信通道质量指数统计
         */
        channel_quality_indexes_stat,
        /**
         * 短信通道错误统计
         */
        channel_error_stat,
        /**
         * 客户发送质量指数统计
         */
        client_quality_indexes_stat,
        /**
         * 通道历史质量指数统计
         */
        channel_quality_indexes_history_stat,
        /**
         * 代理商、客户月报
         */
        send_reprot_month,
        /**
         * 用户历史质量指数统计
         */
        client_quality_indexes_history_stat,
        /**
         * 用户成功率指数统计
         */
        client_success_rate_realtime_stat,
        /**
         * 通道成功率指数统计
         */
        channel_success_rate_realtime_stat,
        /**
         * Add by lpjLiu 2017-05-26 5.4.0.0
         * 客户短信池的状态到期检查(仅OEM)
         */
        client_pool_status,
        /**
         * 大客户阿里每日发送详情
         */
        ali_daily_detail,
        /**
         * 通道运维运营报表统计（第前三天的数据）
         */
        channel_stat,
        /**
         * 通道运维运营报表统计（昨日数据）
         */
        channel_stat_yesterday,
        /**
         * 每半小时统计流水并发邮件
         */
        half_hour_access_record_service,
        /**
         * 预付费客户发送详情
         */
        prepayment_client_daily_detail,
        /**
         * 后付费客户发送详情
         */
        postpaid_client_daily_detail,
        /**
         * Add by lpjLiu 2017-07-10 5.6.3.0
         * 代理商短信池的状态到期检查(仅OEM)
         */
        agent_pool_status,
        /**
         * 【预警】预付费客户余额预警
         */
        client_balance_alarm,
        /**
         * 【预警】预付费客户余额预警
         */
        oem_client_balance_alarm,
        /**
         * 【补偿】纠正用户销售和通道成本
         */
        fix_access_record_statistic,
        /**
         * 【预警】代理商可用额度预警
         */
        agent_balance_alarm,
        /**
         * 【预警】OEM代理商可用额度预警
         */
        oem_agent_balance_alarm,
        /**
         * OEM代理商后付费计费，扣除代理商的余额
         */
        oemagent_houfufei_client_chargeOnAgent,
        /**
         * 修复Record的SMSType
         */
        fix_record_SMSType,

        /**
         * 修复代理商授信历史记录并生成对应销售授信账单
         */
        fix_agentCredit_hisBill,
        /**
         * 每日审核记录备份
         */
        sms_audit_bak,
        /**
         * 每日短信销售情况邮件报表
         */
        sms_sale_stat_email_report,
        /**
         * 实时通道成功率区间权重更新
         */
        realtime_channel_success_rate_wieght_update,
        /**
         * 每周一创建sms_audit表
         */
        sunday_create_table_sms_audit,
        /**
         * 支付取消
         */
        jsms_online_payment_pay_cancel,
        /**
         * 支付失败
         */
        jsms_online_payment_pay_fail,

        /**
         * 每天监控定时任务执行情况
         */
        task_monitor,
        /**
         * 监控应用运行情况
         */
        app_monitor,
        /**
         * 每日短信审核与关键字记录表
         */
        sms_audit_keyword_record_bak,
        /**
         * 每周创建sms_timer_send_phones表
         */
        sunday_create_table_sms_timer_send_phones,
        /**
         * 每月创建sms_send_audit_intercept表
         */
        month_create_table_sms_send_audit_intercept,
        /**
         * 投诉率
         */
        complain_statistics,

        /**
         *【短信发送记录】客户上行明细
         */
        month_create_t_sms_access_molog,

        /**
         *【短信发送记录】通道上行明细
         */
        month_create_t_sms_record_molog;


        public static TaskType getInstance(int value) {
            switch (value) {
                case 1:
                    return procedure;
                case 2:
                    return warning;
                case 3:
                    return agent_commision_compute;
                case 4:
                    return clientid_seq_release;
                case 5:
                    return create_table;
                case 6:
                    return access_stat;
                case 7:
                    return client_order_status;
                case 8:
                    return agent_rebate_compute;
                case 9:
                    return client_send_speed;
                case 10:
                    return access_stat_yesterday;
                case 11:
                    return mq_queue_message_stat;
                case 12:
                    return channel_quality_indexes_stat;
                case 13:
                    return channel_error_stat;
                case 14:
                    return client_quality_indexes_stat;
                case 15:
                    return channel_quality_indexes_history_stat;
                case 16:
                    return send_reprot_month;
                case 17:
                    return client_quality_indexes_history_stat;
                case 18:
                    return client_success_rate_realtime_stat;
                case 19:
                    return channel_success_rate_realtime_stat;
                case 20:
                    return client_pool_status;
                case 21:
                    return ali_daily_detail;
                case 22:
                    return channel_stat_yesterday;
                case 23:
                    return channel_stat;
                case 24:
                    return half_hour_access_record_service;
                case 25:
                    return prepayment_client_daily_detail;
                case 26:
                    return postpaid_client_daily_detail;
                case 27:
                    return agent_pool_status;
                case 28:
                    return client_balance_alarm;
                case 29:
                    return fix_access_record_statistic;
                case 30:
                    return agent_balance_alarm;
                case 31:
                    return sms_audit_bak;
                case 62:
                    return oemagent_houfufei_client_chargeOnAgent;
                case 99:
                    return fix_record_SMSType;
                case 98: //只能两位数任务
                    return fix_agentCredit_hisBill;
                case 32:
                    return sms_sale_stat_email_report;
                case 34:
                    return realtime_channel_success_rate_wieght_update;
                case 35:
                    return sunday_create_table_sms_audit;
                case 36:
                    return oem_client_balance_alarm;
                case 37:
                    return oem_agent_balance_alarm;
                case 38:
                    return sms_audit_keyword_record_bak;
                case 40:
                    return task_monitor; // 每天监控定时任务执行情况
                case 41:
                    return app_monitor; // 每天监控定时任务执行情况
                case 42:
                    return jsms_online_payment_pay_cancel;
                case 43:
                    return jsms_online_payment_pay_fail;
                case 44:
                    return sunday_create_table_sms_timer_send_phones;
                case 45:
                    return month_create_table_sms_send_audit_intercept;
                case 51:
                    return complain_statistics;
                case 52:
                    return month_create_t_sms_access_molog; //【短信发送记录】客户上行明细
                case 53:
                    return month_create_t_sms_record_molog; //【短信发送记录】通道上行明细
                default:
                    return null;
            }
        }
    }

    /**
     * 执行类型
     */
    public enum ExecuteType {
        /**
         * 执行类型：空
         */
        empty(null),
        /**
         * 执行类型：分
         */
        minute("yyyyMMddHHmm"),
        /**
         * 执行类型：时
         */
        hour("yyyyMMddHH"),
        /**
         * 执行类型：日
         */
        day("yyyyMMdd"),
        /**
         * 执行类型：周
         */
        week("yyyyMMdd"),
        /**
         * 执行类型：月
         */
        month("yyyyMM"),
        /**
         * 执行类型：季
         */
        season("yyyyMM"),
        /**
         * 执行类型：年
         */
        year("yyyy");
        private String format;// 时间格式

        ExecuteType(String format) {
            this.format = format;
        }

        public static ExecuteType getInstance(int value) {
            switch (value) {
                case 0:
                    return empty;
                case 1:
                    return minute;
                case 2:
                    return hour;
                case 3:
                    return day;
                case 4:
                    return week;
                case 5:
                    return month;
                case 6:
                    return season;
                case 7:
                    return year;
                default:
                    return null;
            }
        }

        public String getFormat() {
            return format;
        }
    }

    /**
     * 扫描类型
     */
    public enum ScanType {
        /**
         * 扫描类型：分
         */
        minute,
        /**
         * 扫描类型：时
         */
        hour,
        /**
         * 扫描类型：日
         */
        day,
        /**
         * 扫描类型：周
         */
        week,
        /**
         * 扫描类型：月
         */
        month,
        /**
         * 扫描类型：季
         */
        season,
        /**
         * 扫描类型：年
         */
        year;

        public static ScanType getInstance(int value) {
            switch (value) {
                case 1:
                    return minute;
                case 2:
                    return hour;
                case 3:
                    return day;
                case 4:
                    return week;
                case 5:
                    return month;
                case 6:
                    return season;
                case 7:
                    return year;
                default:
                    return null;
            }
        }
    }

    /**
     * 任务状态
     *
     * @author xiejiaan
     */
    public enum TaskStatus {
        close(0), enabled(1), running(2), delete(3);
        private int value;

        TaskStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static TaskStatus getInstance(int value) {
            switch (value) {
                case 0:
                    return close;
                case 1:
                    return enabled;
                case 2:
                    return running;
                case 3:
                    return delete;
                default:
                    return null;
            }
        }
    }

}
