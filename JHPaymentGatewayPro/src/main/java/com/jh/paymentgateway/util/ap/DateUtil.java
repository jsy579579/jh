package com.jh.paymentgateway.util.ap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 
 * The class DateUtil.
 *
 * Description:日期工具类
 *
 * @author: huanghao
 * @since: 2016年2月19日
 * @version: $Revision$ $Date$ $LastChangedBy$
 *
 */
public class DateUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateUtil.class);
    /** yyyy-MM-dd */
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    /** yyyyMMdd */
    public static final String YYYYMMDD = "yyyyMMdd";
    /**yyMMddHHmmss*/
    public static final String YYMMDDHHMMSS = "yyMMddHHmmss";
    /** yyMMddHHmmSSS */
    public static final String YYMMDDHHMMSSS = "yyMMddHHmmSSS";
    /** yyyyMM */
    public static final String YYYYMM = "yyyyMM";
    /** yyyyMMddHHmm */
    public static final String YYYYMMDDHHMM = "yyyyMMddHHmm";
    /** yyyyMMddHHmmss */
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    /** yyyy-MM-dd HH:mm:ss:SSS */
    public static final String YYYY_MM_DDHHMMSSSSS = "yyyy-MM-dd HH:mm:ss:SSS";
    /** yyyy-MM-dd HH:mm */
    public static final String YYYY_MM_DDHHMM = "yyyy-MM-dd HH:mm";
    /** yyyy-MM-dd HH:mm:ss */
    public static final String YYYY_MM_DDHHMMSS = "yyyy-MM-dd HH:mm:ss";
    /** yyyy-MM-dd HH:mm:ss.SSS */
    public static final String YYYY_MM_DDHHMMSSSSS_ = "yyyy-MM-dd HH:mm:ss.SSS";
    /** yyyy年MM月dd日 */
    public static final String _YYYY_MM_DD = "yyyy年MM月dd日";
    /** MM月dd日 */
    public static final String MM_DD = "MM月dd日";
    /** yyyy/MM/dd HH:mm:ss */
    public static final String _YYYY_MM_DDHHMMSS = "yyyy/MM/dd HH:mm:ss";
    /** yyyyMMddHHmmssSSS */
    public static final String YYYYMMDDHHMMSSSSS = "yyyyMMddHHmmssSSS";
    /** yyMMddHHmmssSSS */
    public static final String YYMMDDHHMMSSSSS = "yyMMddHHmmssSSS";
    /**yyMMddHHmm */
    public static final String YYMMDDHHMM = "yyMMddHHmm";
    /**MM/dd HH:mm */
    public static final String MM_DD_HH_MM = "MM/dd HH:mm";
    /**yyyy年MM月dd日 HH:mm */
    public static final String _YYYY_MM_DDHHMM = "yyyy年MM月dd日 HH:mm";
    /**yyyy/MM/dd*/
    public static final String yyxMMxdd = "yyyy/MM/dd";
    /**yyMMddsssss*/
    public static final String yyMMddsssss = "yyMMddsssss";
    /**HH*/
    public static final String HH = "HH";
    /**yyyy/MM/dd HH:mm:ss.SSS*/
    public static final String yyyyxMMxddx24HHsss = "yyyy/MM/dd HH:mm:ss.SSS";
    /** yyyy/MM/dd HH:mm */
    public static final String yyxMMxddxmm = "yyyy/MM/dd HH:mm";
    /**date format for oracle 'YYYY-MM-DD HH24'*/
    public static final String YYYYMMDDHH24 = "YYYY-MM-DD HH24";
    /**date format for oracle 'YYYY-MM-DD HH24:MI'*/
    public static final String YYYYMMDDHH24MI = "YYYY-MM-DD HH24:MI";
    /**date format for oracle 'YYYY-MM-DD HH24:MI:SS'*/
    public static final String YYYYMMDDHH24MISS = "YYYY-MM-DD HH24:MI:SS";
    /**date format for oracle 'HH24:MI:SS'*/
    public static final String HH24MISS = "HH24:MI:SS";
    /**date format for oracle 'HH24:MI'*/
    public static final String HH24MI = "HH24:MI";
    /**date format for oracle 'MI:SS'*/
    public static final String MISS = "MI:SS";

    /**星期列表常量*/
    private static final String[] WEEKS_CN = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };

    /**
     * 返回当前日期
     * 
     * @return
     */
    public static String getCurrentDate(String format) {
        if (StringUtils.isEmpty(format)) {
            return getCurrentDate();
        }
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String date = sdf.format(cal.getTime());
        return date;
    }

    /**
     * 返回当前日期 默认时间格式为 yyyy-MM-dd
     * 
     * @return
     */
    public static String getCurrentDate() {
        return getCurrentDate(YYYY_MM_DD);
    }

    /**
     * 格式化日期
     * @return String
     */
    public static String convertDateToString(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * 日期格式改变
     * @return Date
     */
    public static Date convertDateByFormat(Date date, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            String conStr = sdf.format(date);
            return sdf.parse(conStr);
        } catch (Exception e) {
            LOGGER.error("错误的日期转换", e);
            return date;
        }
    }

    /**
     * 返回给定天的下一天的字符串
     * 
     * @param curDay
     * @param dateFormat
     * @return
     */
    public static String getNextDay(String curDay, String dateFormat) {
        return getNextDay(curDay, dateFormat, 1);
    }

    /**
     * 返回给定天的num天后的字符串
     * 
     * @param curDay
     * @param dateFormat
     * @param num
     * @return
     */
    public static String getNextDay(String curDay, String dateFormat, int num) {
        SimpleDateFormat sdf = null;
        Date date = null;
        String dateStr = null;

        sdf = new SimpleDateFormat(dateFormat);
        sdf.setLenient(false);
        try {
            date = sdf.parse(curDay);
            date = getNextDay(date, num);
            dateStr = sdf.format(date);
        } catch (ParseException e) {
            LOGGER.error("错误的日期格式！", e);

            return curDay;
        }

        return dateStr;
    }

    /**
     * 返回给定天的下一天的日期
     * @param curDate
     * @return
     */
    public static Date getNextDay(Date curDate) {
        return getNextDay(curDate, 1);
    }

    /**
     * 返回给定天的num天后的日期
     * 
     * @param curDay
     * @param dateFormat
     * @param num
     * @return
     */
    public static Date getNextDay(Date curDate, int num) {
        Date date = null;
        Calendar cal = null;

        try {
            cal = Calendar.getInstance();
            cal.setTime(curDate);
            cal.add(Calendar.DAY_OF_MONTH, num);
            date = cal.getTime();
        } catch (Exception e) {
            LOGGER.error("错误的日期计算", e);
        }

        return date;
    }

    /**
     * 返回给定天的下一天的字符串
     * @param curDate
     * @param dateFormat
     * @return
     */
    public static String getNextDay(Date curDate, String dateFormat) {
        try {
            Date nextDate = getNextDay(curDate);
            return convertDateToString(nextDate, dateFormat);
        } catch (Exception e) {
            LOGGER.error("错误的日期格式！", e);
        }

        return null;
    }

    /**
     * 返回给定天的num天后的字符串
     * @param curDate
     * @param num
     * @param dateFormat
     * @return
     */
    public static String getNextDay(Date curDate, String dateFormat, int num) {
        Date date = null;
        String dateStr = null;
        try {
            date = getNextDay(curDate, num);
            dateStr = convertDateToString(date, dateFormat);
        } catch (Exception e) {
            LOGGER.error("错误的日期格式！", e);
        }

        return dateStr;
    }

    /**
     * 返回给定天的前一天的字符串
     * 
     * @param curDay
     * @param dateFormat
     * @return
     */
    public static String getPreDay(String curDate, String dateFormat) {

        return getPreDay(curDate, dateFormat, 1);
    }

    /**
     * 返回给定天的num天前的字符串
     * @param curDate
     * @param dateFormat
     * @param num
     * @return
     */
    public static String getPreDay(String curDate, String dateFormat, int num) {
        Date date = null;
        String dateStr = null;

        try {
            date = convertStringToDate(curDate, dateFormat);
            date = getPreDay(date, num);
            dateStr = date2String(date, dateFormat);
        } catch (Exception e) {
            LOGGER.error("错误的日期计算！", e);
        }

        return dateStr;
    }

    /**
     * 返回给定天的1天前的字符串
     * @param curDate
     * @param dateFormat
     * @return
     */
    public static String getPreDay(Date curDate, String dateFormat) {

        return getPreDay(curDate, dateFormat, 1);
    }

    /**
     * 返回给定天的num天前的字符串
     * 
     * @param curDay
     * @param dateFormat
     * @return
     */
    public static String getPreDay(Date curDate, String dateFormat, int num) {
        Date date = null;
        String dateStr = null;

        try {
            date = getPreDay(curDate, num);
            dateStr = convertDateToString(date, dateFormat);
        } catch (Exception e) {
            LOGGER.error("错误的日期格式！", e);
        }

        return dateStr;
    }

    /**
     * 返回给定天的1天前的日期
     * @param curDate
     * @return
     */
    public static Date getPreDay(Date curDate) {

        return getPreDay(curDate, 1);
    }

    /**
     * 返回给定天的num天前的日期
     * @param curDate
     * @param num
     * @return
     */
    public static Date getPreDay(Date curDate, int num) {
        Date date = null;
        Calendar cal = null;

        try {
            cal = Calendar.getInstance();
            cal.setTime(curDate);
            cal.add(Calendar.DAY_OF_MONTH, -num);

            date = cal.getTime();
        } catch (Exception e) {
            LOGGER.error("错误的日期计算", e);
        }

        return date;
    }

    /**
     * 将日期转换为字符串
     * 
     * @param date
     *            要转换为String的日期
     * @param format
     *            日期格式
     * @return
     */
    public static String date2String(Date date, String format) {
        if (null == date || StringUtils.isEmpty(format)) {
            return null;
        }
        SimpleDateFormat df = new SimpleDateFormat(format);
        df.setLenient(false);
        String time = df.format(date);
        return time;
    }

    /**
     * 返回两个日期相差的天数（ endDate - beginDate）
     * @param beginDate
     * @param endDate
     * @return
     */
    public static long getDaySub(Date beginDate, Date endDate) {
        long hour = getHourSub(beginDate, endDate);
        return (long) Math.round(hour / 24);
    }
    
    /**
     * 时间相减得到天数 endDateStr - beginDateStr
     * 
     * @param beginDateStr
     * @param endDateStr
     * @return long
     */
    public static long getDaySub(String beginDateStr, String endDateStr, String format) {
        long day = 0;
        try {
            Date beginDate = convertStringToDate(beginDateStr, format);
            Date endDate = convertStringToDate(endDateStr, format);

            day = getDaySub(beginDate, endDate);
        } catch (Exception e) {
            LOGGER.error("错误的日期计算", e);
        }

        return day;
    }

    /**
     * 返回两个日期相差的小时数（ endDate - beginDate）
     * @param beginDate
     * @param endDate
     * @return
     */
    public static long getHourSub(Date beginDate, Date endDate) {
        long minute = getMinuteSub(beginDate, endDate);
        return (long) Math.round(minute / 60);
    }

    /**
     * 时间相减得到小时数 endDateStr - beginDateStr
     * 
     * @param beginDateStr
     * @param endDateStr
     * @return long
     */
    public static long getHourSub(String beginDateStr, String endDateStr, String format) {
        long hour = 0;
        try {
            if (StringUtils.isBlank(beginDateStr) || StringUtils.isBlank(endDateStr)) {
                return 0;
            }
            Date beginDate = convertStringToDate(beginDateStr, format);
            Date endDate = convertStringToDate(endDateStr, format);

            hour = getHourSub(beginDate, endDate);
        } catch (Exception e) {
            LOGGER.error("错误的日期计算", e);
        }

        return hour;
    }

    /**
     * 返回两个日期相差的分钟数（ endDate - beginDate）
     * @param beginDate
     * @param endDate
     * @return 两个日期相差的分钟数
     */
    public static long getMinuteSub(Date beginDate, Date endDate) {
        long second = getSecondSub(beginDate, endDate);
        return (long) Math.round(second / 60);
    }

    /**
     * 比较两个时间的差，用 date1 - date2
     * 
     * @param date1
     * @param date2
     * @return 两个时间相差的分钟
     */
    public static long getMinuteSub(String beginDateStr, String endDateStr, String format) {
        long minute = 0;
        try {
            if (StringUtils.isBlank(beginDateStr) || StringUtils.isBlank(endDateStr)) {
                return 0;
            }
            Date beginDate = convertStringToDate(beginDateStr, format);
            Date endDate = convertStringToDate(endDateStr, format);
            minute = getMinuteSub(beginDate, endDate);
        } catch (Exception e) {
            LOGGER.error("错误的日期计算", e);
        }
        return minute;
    }

    /**
     * 返回两个日期相差的秒数（ endDate - beginDate）
     * 
     * @param date1
     * @param date2
     * @return 两个时间相差的秒
     */
    public static long getSecondSub(Date beginDate, Date endDate) {
        long milliseconds = getMillisecondsSub(beginDate, endDate);
        return (long) Math.round(milliseconds / 1000);
    }

    /**
     * 回两个日期相差的秒数，用 date2 - date1
     * 
     * @param date1
     * @param date2
     * @return 两个时间相差的秒数
     */
    public static long getSecondSub(String beginDateStr, String endDateStr, String format) {
        long second = 0;
        try {
            if (StringUtils.isBlank(beginDateStr) || StringUtils.isBlank(endDateStr)) {
                return 0;
            }
            Date beginDate = convertStringToDate(beginDateStr, format);
            Date endDate = convertStringToDate(endDateStr, format);
            second = getSecondSub(beginDate, endDate);
        } catch (Exception e) {
            LOGGER.error("错误的日期计算", e);
        }
        return second;
    }

    /**
     * 返回两个日期相差的毫秒数（ endDate - beginDate）
     * 
     * @param date1
     * @param date2
     * @return 两个时间相差的毫秒
     */
    public static long getMillisecondsSub(Date beginDate, Date endDate) {
        long milliseconds = 0;
        try {
            if (beginDate == null || endDate == null) {
                return 0;
            }
            milliseconds = endDate.getTime() - beginDate.getTime();
        } catch (Exception e) {
            LOGGER.error("错误的日期计算", e);
        }
        return milliseconds;
    }

    /**
     * 回两个日期相差的毫秒数，用 date2 - date1
     * 
     * @param date1
     * @param date2
     * @return 两个时间相差的毫秒数
     */
    public static long getMillisecondsSub(String beginDateStr, String endDateStr, String format) {
        long milliseconds = 0;
        try {
            if (StringUtils.isBlank(beginDateStr) || StringUtils.isBlank(endDateStr)) {
                return 0;
            }
            Date beginDate = convertStringToDate(beginDateStr, format);
            Date endDate = convertStringToDate(endDateStr, format);
            milliseconds = getMillisecondsSub(beginDate, endDate);
        } catch (Exception e) {
            LOGGER.error("错误的日期计算", e);
        }
        return milliseconds;
    }

    /**
     * 日期字符串转换成日期
     * 
     * @param date
     * @param format
     * 
     * @return Date
     * */
    public static Date convertStringToDate(String date, String format) {
        SimpleDateFormat sdf = null;
        Date d = null;

        try {
            sdf = new SimpleDateFormat(format);
            sdf.setLenient(false);

            d = sdf.parse(date);
        } catch (ParseException e) {
            LOGGER.error("错误的日期格式", e);
        }
        return d;
    }

    /**
     * 返回给定天的minute 分钟后的字符串
     * 
     * @param curDay
     * @param dateFormat
     * @return
     */
    public static String getNextMinute(String curDay, String dateFormat, int minute) {
        SimpleDateFormat sdf = null;
        Calendar cal = null;
        Date date = null;
        String dateStr = null;

        sdf = new SimpleDateFormat(dateFormat);
        sdf.setLenient(false);
        try {
            date = sdf.parse(curDay);
            cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.MINUTE, minute);
            date = cal.getTime();
            dateStr = sdf.format(date);
        } catch (ParseException e) {
            LOGGER.error("错误的日期格式！", e);
            return curDay;
        }

        return dateStr;
    }

    /**
     * 返回给定天的minute 分钟后的字符串
     * 
     * @param curDay
     * @param minute
     * @return
     */
    public static Date getNextMinute(Date date, int minute) {
        Calendar cal = null;
        cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, minute);
        date = cal.getTime();
        return date;
    }

    /**
     * 返回给定天的minute 分钟前的字符串
     * 
     * @param curDay
     * @param dateFormat
     * @return
     */
    public static Date getPreMinute(Date date, int minute) {
        Calendar cal = null;
        cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, -minute);
        date = cal.getTime();
        return date;
    }

    /**
     * 返回给定天的minute 分钟前的字符串
     * 
     * @param curDay
     * @param dateFormat
     * @return
     */
    public static String getPreMinute(String curDay, String dateFormat, int minute) {
        SimpleDateFormat sdf = null;
        Calendar cal = null;
        Date date = null;
        String dateStr = null;

        sdf = new SimpleDateFormat(dateFormat);
        sdf.setLenient(false);
        try {
            date = sdf.parse(curDay);
            cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.MINUTE, -minute);
            date = cal.getTime();
            dateStr = sdf.format(date);
        } catch (ParseException e) {
            LOGGER.error("错误的日期格式！", e);
            return curDay;
        }

        return dateStr;
    }

    /**
     * 返回当前日期是星期几
     * @param dt
     * @return 
     */
    public static String getWeekOfDate(Date date) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
            if (w < 0) w = 0;
            return WEEKS_CN[w];
        } catch (Exception e) {
            LOGGER.error("错误的日期转换", e);
        }

        return null;
    }

    /**
     * 返回当前日期是星期几
     * @param dateStr
     * @param format
     * @return
     */
    public static String getWeekOfDate(String dateStr, String format) {
        try {
            if (StringUtils.isBlank(dateStr) || StringUtils.isBlank(format)) {
                return null;
            }

            Date date = convertStringToDate(dateStr, format);
            return getWeekOfDate(date);
        } catch (Exception e) {
            LOGGER.error("错误的日期转换", e);
        }

        return null;
    }

    /**
     * 
     * @param originDate
     * @param originFormat
     * @param targetFormat
     * @return
     */
    public static Date convertDateToNewFormatDate(Date originDate, String originFormat,
            String targetFormat) {

        try {
            String newFormatDateStr = date2String(originDate, targetFormat);

            return convertStringToDate(newFormatDateStr, targetFormat);
        } catch (Exception e) {
            LOGGER.error("错误的日期格式！", e);
        }

        return null;
    }

    /**
     * 
     * @param originDateStr
     * @param originFormat
     * @param targetFormat
     * @return
     */
    public static Date convertDateToNewFormatDate(String originDateStr, String originFormat,
            String targetFormat) {

        try {
            Date orgDate = convertStringToDate(originDateStr, originFormat);
            return convertDateToNewFormatDate(orgDate, originFormat, targetFormat);
        } catch (Exception e) {
            LOGGER.error("错误的日期格式！", e);
        }

        return null;
    }

    /**
     * 获取年月日时分秒数字符
     * 
     * @param date
     * @return
     */
    public static List<Integer> splitDate(Date date) {
        List<Integer> list = new ArrayList<Integer>();

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        list.add(cal.get(Calendar.YEAR));
        list.add(cal.get(Calendar.MONTH) + 1);
        list.add(cal.get(Calendar.DAY_OF_MONTH));
        list.add(cal.get(Calendar.HOUR));
        list.add(cal.get(Calendar.MINUTE));
        list.add(cal.get(Calendar.SECOND));

        return list;
    }

    /**
     * 两个时间是否是 同年 同月 同日
     * 如果是，则返回true，否则返回false
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isEqualsInYMD(Date date1, Date date2) {

        Calendar c = null;

        try {
            c = Calendar.getInstance();

            c.setTime(date1);
            int year1 = c.get(Calendar.YEAR);
            int dayOfMonth1 = c.get(Calendar.DAY_OF_MONTH);
            int dayOfYear1 = c.get(Calendar.DAY_OF_YEAR);

            c.setTime(date2);
            int year2 = c.get(Calendar.YEAR);
            int dayOfMonth2 = c.get(Calendar.DAY_OF_MONTH);
            int dayOfYear2 = c.get(Calendar.DAY_OF_YEAR);

            if (year1 != year2) {
                return false;
            }
            if (dayOfMonth1 != dayOfMonth2) {
                return false;
            }
            if (dayOfYear1 != dayOfYear2) {
                return false;
            }

            return true;
        } catch (Exception e) {
            LOGGER.error("错误的日期比较", e);
        }

        return false;
    }

    /**  
     * 计算两个日期之间相差的天数  
     * @param smdate 较小的时间 
     * @param bdate  较大的时间 
     * @return 相差天数 
     * @throws ParseException  
     */
    public static int daysBetween(Date smdate, Date bdate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(YYYY_MM_DD);
        smdate = sdf.parse(sdf.format(smdate));
        bdate = sdf.parse(sdf.format(bdate));
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);

        return Integer.parseInt(String.valueOf(between_days));
    }

    /**
     * date2比date1多的天数
     * 只是通过日期来进行比较两个日期的相差天数的比较，没有精确到相差到一天的时间。如果是只是纯粹通过日期（年月日）
     * @param date1
     * @param date2
     * @return
     */
    public static int differentDays(Date date1,Date date2)
    {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        int day1= cal1.get(Calendar.DAY_OF_YEAR);
        int day2 = cal2.get(Calendar.DAY_OF_YEAR);

        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);
        if(year1 != year2) //同一年
        {
            int timeDistance = 0 ;
            for(int i = year1 ; i < year2 ; i ++)
            {
                if(i%4==0 && i%100!=0 || i%400==0) //闰年
                {
                    timeDistance += 366;
                }
                else //不是闰年
                {
                    timeDistance += 365;
                }
            }

            return timeDistance + (day2-day1) ;
        }
        else //不同年
        {
            return day2-day1;
        }
    }
    
    /**
     * 设置当前时分秒给指定的日期
     * @param target
     * @return
     */
    public static Date setNowToDate(Date target){
    	Calendar cal = Calendar.getInstance();
    	Calendar cal2 = Calendar.getInstance();
    	cal.setTime(target);
    	cal.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
    	cal.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
    	cal.set(Calendar.SECOND, cal2.get(Calendar.SECOND));
    	return cal.getTime();
    }
    
    /**
     * 设置时分秒
     * @param date
     * @param hour
     * @param min
     * @param second
     * @return
     */
    public static Date setTime(Date date, int hour, int min, int second){
    	Calendar c = Calendar.getInstance();
    	c.setTime(date);
    	c.set(Calendar.HOUR_OF_DAY, hour);
    	c.set(Calendar.MINUTE, min);	
    	c.set(Calendar.SECOND, second);
    	return c.getTime();
    }
}
