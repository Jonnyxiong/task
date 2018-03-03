package com.ucpaas.sms.task.statistic;

import org.joda.time.DateTime;

public interface AccessStatisticStrategy {
    /**
     *
     * @param statDay 数据日期，并更新当月的数据
     */
    void staticAlgorithm(DateTime statDay);
}
