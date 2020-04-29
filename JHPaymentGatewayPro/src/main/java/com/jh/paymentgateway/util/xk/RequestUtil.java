package com.jh.paymentgateway.util.xk;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * request操作工具类
 * @author huangqiang
 *
 */
public class RequestUtil {
	/**
	 * 获取客户端真实IP
	 * @param request
	 * @return
	 */
	public static String getIpAddress(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
	
	/**
	 * 从request获取参数转换为可操作map
	 * @param request
	 * @return
	 */
	public static Map<String, String> getParmsMap(HttpServletRequest request) {
		Map<String, String[]> parameterMap = request.getParameterMap();
		Map<String, String> parms = new HashMap<String, String>();
		Iterator<Entry<String, String[]>> entries = parameterMap.entrySet().iterator();  
		Entry<String, String[]> entry;  
        String name = "";  
        String value = "";  
        while (entries.hasNext()) {  
            entry = (Entry<String, String[]>) entries.next();  
            name = (String) entry.getKey();  
            String[] valueArr = entry.getValue();  
            if(null == valueArr || valueArr.length == 0){  
                value = "";  
            }else{  
            	//页面提交参数不会有一个参数多个值的情况，所以直接取第一个值
                value = valueArr[0];  
            } 
            parms.put(name, value);  
        } 
        return parms;
	}
}
