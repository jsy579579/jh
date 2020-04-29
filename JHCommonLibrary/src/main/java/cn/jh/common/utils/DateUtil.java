package cn.jh.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期转化类
 */
public class DateUtil {
	/**
	 *
	 * @param str
	 * @return 年-月-日
	 */
	public static Date getDateFromStr(String str) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date result = null;
		try {
			result = sdf.parse(str);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			throw new RuntimeException(e);
		}

		return result;
	}

	/**
	 *
	 * @param str
	 * @return	年-月-日 时:分:秒
	 */
	public static Date getYYMMHHMMSSDateFromStr(String str) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date result = null;
		try {
			result = sdf.parse(str);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		return result;
	}

	public static Date getYYMMDateFromStr(String str) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		Date result = null;
		try {
			result = sdf.parse(str);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		return result;
	}

	public static Date getHHMMSSDateFormat(String str) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		Date result = null;
		try {
			result = sdf.parse(str);
		} catch (Exception e) {
			throw e;
		}

		return result;
	}

	public static String getyyyyMMddHHmmssDateFormat(Date date) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String result = null;
		try {
			result = sdf.format(date);
		} catch (Exception e) {
			throw e;
		}

		return result;
	}

	public static String getyyyyMMddHHmmssSSSDateFormat(Date date) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String result = null;
		try {
			result = sdf.format(date);
		} catch (Exception e) {
			throw e;
		}

		return result;
	}

	public static String getYesterday(String fomat) throws Exception {
		Calendar cld = Calendar.getInstance();
		// System.out.println(cld);
		// System.out.println(cld.get(Calendar.YEAR) + "年");
		// System.out.println(cld.get(Calendar.MONTH) + "月");
		// System.out.println(cld.get(Calendar.DAY_OF_MONTH) + "日");
		// cld.set(Calendar.DAY_OF_MONTH, 1);
		// cld.set(Calendar.MONTH, 3);
		// cld.set(Calendar.YEAR, 2020);

		int year;
		int month;
		int dayOfMonth;
		// 拿到当前年月日
		year = cld.get(Calendar.YEAR);
		month = cld.get(Calendar.MONTH) + 1;
		dayOfMonth = cld.get(Calendar.DAY_OF_MONTH);

		// 判断当前日期是否为1日,是则月份减1,否则不减
		if (dayOfMonth == 1) {
			month = month - 1;
			// 判断减完之后是否为0,是则改为12月,年份减1,否则不改
			if (month == 0) {
				month = 12;
				year = year - 1;
			}

			// 判断当前年份是否是闰年
			Calendar cld2 = Calendar.getInstance();
			cld2.set(Calendar.YEAR, year);
			cld2.set(Calendar.MONTH, 11);
			cld2.set(Calendar.DAY_OF_MONTH, 31);
			if (cld2.get(Calendar.DAY_OF_YEAR) == 366) {
				// 是闰年
				if (month == 2) {
					dayOfMonth = 29;
				} else if (month == 4 || month == 6 || month == 9 || month == 11) {
					dayOfMonth = 30;
				} else {
					dayOfMonth = 31;
				}
			} else {
				// 不是闰年
				if (month == 2) {
					dayOfMonth = 28;
				} else if (month == 4 || month == 6 || month == 9 || month == 11) {
					dayOfMonth = 30;
				} else {
					dayOfMonth = 31;
				}
			}

		} else {
			dayOfMonth = dayOfMonth - 1;
		}
		// LOG.info("前一天日期是:" + year + "年" + month + "月" + dayOfMonth + "日");

		if ("00000000".equals(fomat)) {
			return (year + "" + month + "" + dayOfMonth + "");
		} else if ("0000-00-00".equals(fomat)) {
			return (year + "-" + month + "-" + dayOfMonth);
		} else {
			throw new Exception("格式化异常");
		}
	}

	public static String getDateFromStr(Date date) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String result = null;
		try {
			result = sdf.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String getYYMMHHMMSSDateFromStr(Date date) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String result = null;
		try {
			result = sdf.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static <T, U> T  getDateStringConvert(T target, U source,String parrten){
		SimpleDateFormat sdf = new SimpleDateFormat(parrten);
		if(target instanceof Date){
			if(source instanceof String){
				try {
					return (T) sdf.parse((String)source);
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
			}else if(source instanceof Date){
				try {
					return (T) sdf.parse(sdf.format(source));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}else{
			if(source instanceof Date){
				try {
					return (T) sdf.format((Date)source);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}else if(source instanceof String){
				try {
					return (T) sdf.format(sdf.parse((String)source));
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return null;
	}
	
}
