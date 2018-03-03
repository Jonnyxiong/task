 
-- USE `smsp_message`;


INSERT INTO `t_sms_dict` (`param_id`, `param_type`, `param_type_name`, `param_key`, `param_value`, `param_order`) VALUES (NULL, 'task_type', NULL, '24', '【邮件】半小时流水表统计结果', '24');
INSERT INTO `t_sms_task` (`task_id`, `task_name`, `task_type`, `db_type`, `procedure_name`, `execute_type`, `execute_next`, `execute_period`, `scan_type`, `scan_next`, `scan_period`, `scan_execute`, `dependency`, `group`, `order`, `status`, `create_date`, `update_date`) VALUES ('56', '【邮件】半小时流水统计结果', '24', '6', NULL, '1', '201706152033', '1', '1', '2017-06-15 20:33:00', '1', '0', NULL, '24', '24', '1', '2017-06-15 14:48:52', '2017-06-15 20:37:47');

-- USE `ucpaas_message_stats`;

CREATE TABLE ucpaas_v5.5_stats.`t_sms_channel_success_rate_by_clientid` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `channel_id` varchar(30) DEFAULT NULL COMMENT '通道id',
  `channel_name` varchar(255) DEFAULT NULL COMMENT '通道名称',
  `client_id` varchar(6) DEFAULT NULL COMMENT '用户id',
  `client_name` varchar(50) DEFAULT NULL COMMENT '用户名称',
  `iden` varchar(255) DEFAULT NULL COMMENT '标识',
  `send_total` int(11) DEFAULT NULL COMMENT '发送总数 1 2 3 5 6',
  `success_total` int(11) DEFAULT '0' COMMENT '明确成功 3',
  `submit_fail` int(11) DEFAULT '0' COMMENT '提交失败 4',
  `send_fail` int(11) DEFAULT '0' COMMENT '发送失败 5 6',
  `undetermined1` int(11) DEFAULT '0' COMMENT '成功待定 1',
  `undetermined2` int(11) DEFAULT '0' COMMENT '成功待定 2',
  `nosend` int(11) DEFAULT '0' COMMENT '未发送 0',
  `success_rate` decimal(7,4) DEFAULT '0.0000' COMMENT '成功率',
  `fake_success_rate` decimal(7,4) DEFAULT '0.0000' COMMENT '未知率',
  `really_fail_rate` decimal(7,4) DEFAULT '0.0000' COMMENT '失败率',
  `data_time` datetime DEFAULT NULL COMMENT '数据时间',
  `create_time` datetime DEFAULT NULL COMMENT '数据采集时间',
  PRIMARY KEY (`id`),
  KEY `time_index` (`data_time`)
)  DEFAULT CHARSET=utf8 COMMENT='record流水表发送总量查询结果(按用户区分)';

