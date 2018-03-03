package com.ucpaas.sms.task.statistic;

import org.joda.time.DateTime;

/**
 *	通道侧统计数据策略接口<br>
 *
 *  具体统计的逻辑由不同的策略实现类完成，统计策略基本原则如下<br>
 *  1、统计算法会先计算day当天的统计数据<br>
 *  2、然后利用当月一号到day期间的每日数据计算出当月的统计数据
 */
public interface RecordStatisticStrategy {
	
	/**
	 * 通道侧统计数据策略<br>
	 * 
	 * @param day
	 */
	void statistics(DateTime day);
	
}
