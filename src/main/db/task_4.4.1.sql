ALTER TABLE `ucpaas_message_stats_4.4`.`t_sms_client_indexes_stat_0` 
CHANGE COLUMN `order_delay_num_1` `order_delay_num_1` INT(11) NULL DEFAULT '0' COMMENT '订单延时0-1秒（包括0s和1s）' ,
CHANGE COLUMN `order_delay_num_2` `order_delay_num_2` INT(11) NULL DEFAULT '0' COMMENT '订单延时1-3秒（不包括1s，包括3s）' ,
CHANGE COLUMN `order_delay_num_3` `order_delay_num_3` INT(11) NULL DEFAULT '0' COMMENT '订单延时3-5秒（不包括3s，包括5s）' ;
