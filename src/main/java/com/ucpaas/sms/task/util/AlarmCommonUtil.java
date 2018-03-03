package com.ucpaas.sms.task.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;

/**
 * Created by dylan on 2017/8/10.
 */
public class AlarmCommonUtil {

    /**
     * 比较 DateTime中的 时间部分大小, 只比较 时和分
     * @param timeString
     * @param dateTime
     * @return DateTime > timeString
     */
    public static boolean compareTime(DateTime dateTime, String timeString){
        if(dateTime.get(DateTimeFieldType.hourOfDay()) > Integer.parseInt(timeString.substring(0,2))){
            return true;
        }
        if(dateTime.get(DateTimeFieldType.hourOfDay()) == Integer.parseInt(timeString.substring(0,2)) &&
                dateTime.get(DateTimeFieldType.minuteOfHour()) >= Integer.parseInt(timeString.substring(3,5))){
            return true;
        }
        return false;
    }

    /**
     * 判断当前分钟寺是否是扫描周期所在的分钟
     * @param timeString
     * @param scanFrequecy
     * @return
     */
    public static boolean isNowOnScan(String timeString,Integer scanFrequecy){
        DateTime beginTime = DateTime.now().withHourOfDay(Integer.parseInt(timeString.substring(0, 2)))
                .withMinuteOfHour(Integer.parseInt(timeString.substring(3,5)))
                .withSecondOfMinute(0);
        if((DateTime.now().getMillis() - beginTime.getMillis()) % scanFrequecy < 60000){
            return true;
        }
        return false;
    }
}
