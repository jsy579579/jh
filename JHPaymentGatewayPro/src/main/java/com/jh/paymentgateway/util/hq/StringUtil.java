package com.jh.paymentgateway.util.hq;

/**
 * 字符串工具类
 * @author wumf
 *
 */
public class StringUtil {

	/**
	 * 判断字符串是否为数字
	 * @param str
	 * @return
	 */
	public static boolean isNum(String str){
		try{
			return str.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
		}catch(Exception e){
			return false;
		}
	}
	
	/**
	 * 判断字符串是否非空。<br>
	 * 包括：null、"" 或"null"
	 * @param str
	 * @return
	 */
	public static boolean isNotBlank(String str){
		if(str == null){
			return false;
		}
		if(str.trim().length()<1){
			return false;
		}
		if("null".equals(str.trim().toLowerCase())){
			return false;
		}
		return true;
	}
	
	/**
	 * 判断字符串是否为空。<br>
	 * 包括：null、"" 或"null"
	 * @param str
	 * @return
	 */
	public static boolean isBlank(String str){
		if(str == null){
			return true;
		}
		if(str.trim().length()<1){
			return true;
		}
		if("null".equals(str.trim().toLowerCase())){
			return true;
		}
		return false;
	}
}
