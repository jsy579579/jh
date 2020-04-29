
package com.jh.paymentchannel.util.ump.util;

import com.jh.paymentchannel.util.ump.log.ILogger;
import com.jh.paymentchannel.util.ump.log.LogManager;

/**
 * ***********************************************************************
 * <br>description : 字符串工具类
 * @author      umpay
 * @date        2014-8-1 上午09:34:23
 * @version     1.0  
 ************************************************************************
 */
public class StringUtil {

	private static final ILogger log = LogManager.getLogger();
	/**
	 * 将对象数据转换为String，并去除首尾空格
	 * @param obj
	 * @return
	 */
	public static String trim(Object obj){
		if(null == obj)return "";
		else return obj.toString().trim();
	}
	
	/**
	 * 判断字符串是否为空
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str){
		return null==str || "".equals(str.trim());
	}
	
	/**
	 * 判断字符串是否不为空
	 * @param str
	 * @return
	 */
	public static boolean isNotEmpty(String str){
		return !isEmpty(str);
	}
}
