package com.ucpaas.sms.task.util;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FmtUtils
{
    private static final String pattern = "yyyy-MM-dd HH:mm:ss";

    private static final String pattern1 = "yyyyww";

    private static final String pattern2 = "yyyy-MM-dd";

    private static final String pattern3 = "MM/dd";

    private static final String pattern4 = "MM/dd HH:mm";

    private static SimpleDateFormat sdf = new SimpleDateFormat(pattern);

    private static SimpleDateFormat sdf1 = new SimpleDateFormat(pattern1);

    private static SimpleDateFormat sdf2 = new SimpleDateFormat(pattern2);

    private static SimpleDateFormat sdf3 = new SimpleDateFormat(pattern3);

    private static SimpleDateFormat sdf4 = new SimpleDateFormat(pattern4);

    static final String M = "M";

    static final String K = "K";

    public static String formatDate(Date d)
    {
        if (d == null)
        {
            return "";
        }
        return sdf.format(d);
    }

    public static String formatDateWeekInYear(Date d)
    {
        if (d == null)
        {
            return "";
        }
        return sdf1.format(d);
    }

    public static String formatDateWithoutTime(Date d)
    {
        if (d == null)
        {
            return "";
        }
        return sdf2.format(d);
    }

    public static String formatDateShort(Date d)
    {
        if (d == null)
        {
            return "";
        }
        return sdf3.format(d);
    }

    public static String formatDateMiddle(Date d)
    {
        if (d == null)
        {
            return "";
        }
        return sdf4.format(d);
    }

    public static String blank(Object o)
    {
        if (o == null)
        {
            return "";
        }
        if (StringUtils.isEmpty(String.valueOf(o)))
        {
            return "";
        }
        return StringUtils.isEmpty(o.toString()) ? "" : o.toString();
    }

    public static String empty(Object o)
    {
        if (o == null)
        {
            return "";
        }
        if (StringUtils.isEmpty(String.valueOf(o)))
        {
            return "";
        }
        return StringUtils.isEmpty(o.toString()) ? "" : o.toString();
    }

    public static String getFilePath(String rootPath,String realPath)
    {
        char[] realPathChs = realPath.toCharArray();
        StringBuffer rals = new StringBuffer(realPathChs.length);
        //替换路径格式
        for (char c : realPathChs)
        {
            if(c=='/'){
                rals.append(File.separator);
            }else{
                rals.append(c);
            }
        }
        return rootPath+rals.substring(0, rals.length());
    }

    public static String hold4Decimal(BigDecimal d)
    { // 保留3位小数位，按四舍五入
        try
        {
            String pattern = "0.0000";
            DecimalFormat df = new DecimalFormat(pattern);
            String s = df.format(d);
            if (s.equals("-0.0000"))
            {
                s = "0.0000";
            }
            return s;
        }
        catch (Exception ex)
        {
            return "";
        }
    }

    public static String hold3Decimal(String sd)
    { // 保留3位小数位，按四舍五入
        try
        {
            String pattern = "0.000";
            DecimalFormat df = new DecimalFormat(pattern);
            String s = df.format(sd);
            if (s.equals("-0.000"))
            {
                s = "0.000";
            }
            return s;
        }
        catch (Exception ex)
        {
            return "";
        }

    }

    public static String hold3Decimal(BigDecimal d)
    { // 保留3位小数位，按四舍五入
        try
        {
            String pattern = "0.000";
            DecimalFormat df = new DecimalFormat(pattern);
            String s = df.format(d);
            if (s.equals("-0.000"))
            {
                s = "0.000";
            }
            return s;
        }
        catch (Exception ex)
        {
            return "";
        }
    }

    public static String hold2Decimal(BigDecimal d)
    { // 保留2位小数位，按四舍五入
        try
        {
            String pattern = "0.00";
            DecimalFormat df = new DecimalFormat(pattern);
            String s = df.format(d);
            if (s.equals("-0.00"))
            {
                s = "0.00";
            }
            return s;
        }
        catch (Exception ex)
        {
            return "";
        }
    }

    public static String hold1Decimal(BigDecimal d)
    { // 保留1位小数位，按四舍五入
        try
        {
            String pattern = "0.0";
            DecimalFormat df = new DecimalFormat(pattern);
            String s = df.format(d);
            if (s.equals("-0.00"))
            {
                s = "0.00";
            }
            return s;
        }
        catch (Exception ex)
        {
            return "";
        }
    }

    public static String formatDecimal(BigDecimal d)
    { // 格式化数字显示
        // 如2342565.555->2,342,565.56
        try
        {
            String pattern = "##,##0.00";
            DecimalFormat declimalFormat = new DecimalFormat(pattern);
            String s = declimalFormat.format(d.doubleValue());
            if (s.equals("-0.00"))
            {
                s = "0.00";
            }
            return s;
        }
        catch (Exception ex)
        {
            return "";
        }

    }

    public static String hold0Decimal(BigDecimal d)
    { // 格式化数字显示
        try
        {
            String pattern = "####0.##";
            DecimalFormat declimalFormat = new DecimalFormat(pattern);
            String s = declimalFormat.format(d.doubleValue());
            if (s.equals("-0.00"))
            {
                s = "0.00";
            }
            return s;
        }
        catch (Exception ex)
        {
            return "";
        }
    }

    public static String noDecimal(BigDecimal d)
    { // 只保留整数
        try
        {
            String pattern = "####0";
            DecimalFormat declimalFormat = new DecimalFormat(pattern);
            String s = declimalFormat.format(d.doubleValue());
            if (s.equals("-0"))
            {
                s = "0";
            }
            return s;
        }
        catch (Exception ex)
        {
            return "";
        }
    }

    public static String formatInt(BigDecimal d)
    { // 格式化数字显示
        // 如2342565.555->2,342,565.56
        try
        {
            String pattern = "##,##0";
            DecimalFormat declimalFormat = new DecimalFormat(pattern);
            String s = declimalFormat.format(d.doubleValue());
            if (s.equals("-0"))
            {
                s = "0";
            }
            return s;
        }
        catch (Exception ex)
        {
            return "";
        }
    }

    public static String formatInt(Integer d)
    { // 格式化数字显示
        // 如2342565.555->2,342,565.56
        try
        {
            String pattern = "##,##0";
            DecimalFormat declimalFormat = new DecimalFormat(pattern);
            String s = declimalFormat.format(d);
            if (s.equals("-0"))
            {
                s = "0";
            }
            return s;
        }
        catch (Exception ex)
        {
            return "";
        }
    }

    public static String formatLong(Long d)
    { // 格式化数字显示
        // 如2342565.555->2,342,565.56
        try
        {
            String pattern = "##,##0";
            DecimalFormat declimalFormat = new DecimalFormat(pattern);
            String s = declimalFormat.format(d);
            if (s.equals("-0"))
            {
                s = "0";
            }
            return s;
        }
        catch (Exception ex)
        {
            return "";
        }
    }

    public static String shortQty(Integer qty)
    { // 将数量转换格式，1000->1K，1000000->1M
        try
        {
            String retVal = "";
            double d = 0.0;
            if (qty.doubleValue() >= 1000000)
            { // 大于1000000
                d = qty.doubleValue() / 1000000;
                if ((d * 10) / ((int)d * 10) != 1)
                    retVal = (new Double(d).toString() + M);
                    // retVal=formatDecimal(new BigDecimal(d))+M;
                else
                    retVal = new Integer((int)d).toString() + M;
                return retVal;
            }
            if (qty.doubleValue() >= 1000)
            { // 大于1000
                d = qty.doubleValue() / 1000;
                if ((d * 10) / ((int)d * 10) != 1)
                    retVal = (new Double(d).toString() + K);
                else
                    retVal = new Integer((int)d).toString() + K;
                return retVal;
            }
            if (qty.doubleValue() <= -1000000)
            { // 小于-1000000
                d = qty.doubleValue() / 1000000;
                if ((d * 10) / ((int)d * 10) != 1)
                    retVal = (new Double(d).toString() + M);
                else
                    retVal = new Integer((int)d).toString() + M;
                return retVal;
            }
            if (qty.doubleValue() <= -1000)
            { // 小于1000
                d = qty.doubleValue() / 1000;
                if ((d * 10) / ((int)d * 10) != 1)
                    retVal = (new Double(d).toString() + K);
                else
                    retVal = new Integer((int)d).toString() + K;
                return retVal;
            }
            if (qty.doubleValue() - Math.floor(qty.doubleValue()) == 0) // 整数返回整数，有小数返回小数
                retVal = (new Long(qty.longValue()).toString());
            else
                retVal = (new Double(qty.doubleValue()).toString());
            return retVal;
        }
        catch (Exception ex)
        {
            return "";
        }
    }
}
