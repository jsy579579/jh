package cn.jh.common.tools;

import cn.jh.common.utils.DateUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

public class Tools {
  public Tools() {
  }

  public static Date generatorStartDate(String startTime, String format, int preDays) {
    Date startTimeDate = null;
    if (startTime != null && !startTime.equalsIgnoreCase("")) {
      startTimeDate = DateUtil.getDateFromStr(startTime);
    } else {
      startTime = new SimpleDateFormat(format).format(new Date());
      startTimeDate = format.length() == 7 ? DateUtil.getYYMMDateFromStr(startTime) : DateUtil.getDateFromStr(startTime);
    }
    if (preDays < 0) {
      Calendar calendar = new GregorianCalendar();
      calendar.setTime(startTimeDate);
      calendar.add(format.length() == 7 ? Calendar.MONTH : Calendar.DATE, preDays);//把日期往后增加一天.整数往后推,负数往前移动
      startTimeDate = calendar.getTime();   //这个时间就是日期往后推一天的结果
    }
    return startTimeDate;
  }
  /**
   * @param startTimeDate
   * @param ismonth
   * @return
   */
  public static Date generatorEndDate(Date startTimeDate, boolean ismonth) {
    Date endTimeDate = null;
    Calendar calendar = new GregorianCalendar();
    calendar.setTime(startTimeDate);
    calendar.add(ismonth ? Calendar.MONTH : Calendar.DATE, 1);//把日期往后增加一天.整数往后推,负数往前移动
    endTimeDate = calendar.getTime();   //这个时间就是日期往后推一天的结果
    return endTimeDate;
  }

  public static boolean isStrEmpty(String str) {
    if ((str != null) && (str.trim().length() > 0)) {
      return false;
    } else {
      return true;
    }
  }

 
  public static String ruleStr(String str) {
    if (str == null) {
      return "";
    } else {
      return str.trim();
    }
  }
  

  public static String GBK2Unicode(String str) {
    try {
      str = new String(str.getBytes("GBK"), "ISO-8859-1");
    } catch (java.io.UnsupportedEncodingException e) {}
    ;
    return str;
  }

 
  public static String Unicode2GBK(String str) {
    try {
      str = new String(str.getBytes("ISO-8859-1"), "GBK");
    } catch (java.io.UnsupportedEncodingException e) {}
    ;
    return str;
  }


  public static String getSysTime() {
    java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
    return df.format(new java.util.Date());
  }

 
  public static String getSysDate() {
    java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyyMMdd");
    return df.format(new java.util.Date());
  }
  

  public static String getSysTimeFormat(String format) {
    java.text.SimpleDateFormat df = new java.text.SimpleDateFormat(format);
    return df.format(new java.util.Date());
  }
  

  public static boolean isDay(String d, String format){
    try{
      SimpleDateFormat sdf = new SimpleDateFormat(format);
      sdf.setLenient(false);
      sdf.parse(d);
    }catch (Exception e){
      return false;
    }
    return true;
  }
 

  public static boolean checkAmount(String amount){
	  if(amount==null){
	  		return false;
	  	}
	  String checkExpressions;
		checkExpressions="^([1-9]\\d*|[0])\\.\\d{1,2}$|^[1-9]\\d*$|^0$";
		return Pattern.matches(checkExpressions, amount);
	}
  
 
  public static String getXMLValue(String srcXML, String element) {
    String ret = "";
    try {
      String begElement = "<" + element + ">";
      String endElement = "</" + element + ">";
      int begPos = srcXML.indexOf(begElement);
      int endPos = srcXML.indexOf(endElement);
      if (begPos != -1 && endPos != -1 && begPos <= endPos) {
        begPos += begElement.length();
        ret = srcXML.substring(begPos, endPos);
      } else {
        ret = "";
      }
    } catch (Exception e) {
      ret = "";
    }
    return ret;
  }  
}
