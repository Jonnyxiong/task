package com.ucpaas.sms.task.constant;

/**
 * 数据库常量
 * 
 * @author xiejiaan
 */
public class DbConstant {

	/**
	 * 数据库类型
	 * 
	 * @author xiejiaan
	 */
	public enum DbType {
		/**
		 * 短信主库
		 */
		ucpaas_message_master,
		
		/**
		 * 短信access流水数据主库
		 */
		ucpaas_message_access_master,
		
		/**
		 * 短信record流水数据主库
		 */
		ucpaas_message_record_master,
		
		/**
		 * 短信access流水数据从库
		 */
		ucpaas_message_access_slave,
		
		/**
		 * 短信record流水数据从库
		 */
		ucpaas_message_record_slave,
		
		/**
		 * 短信统计数据主库
		 */
		ucpaas_message_stats_master;
		
		public static DbType getInstance(int value){
			switch (value) {
			case 1:
				return ucpaas_message_master;
			case 2:
				return ucpaas_message_access_master;
			case 3:
				return ucpaas_message_record_master;
			case 4:
				return ucpaas_message_access_slave;
			case 5:
				return ucpaas_message_record_slave;
			case 6:
				return ucpaas_message_stats_master;
			default:
				return null;
			}
		}
	}

}
