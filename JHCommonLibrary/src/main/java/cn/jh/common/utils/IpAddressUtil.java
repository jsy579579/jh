package cn.jh.common.utils;

import javax.servlet.http.HttpServletRequest;

public class IpAddressUtil {
    public String getRemoteIP(HttpServletRequest request) {  
		String ipAddress = request.getHeader("x-forwarded-for");  
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {  
            ipAddress = request.getHeader("Proxy-Client-ipAddress");  
        }  
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {  
            ipAddress = request.getHeader("WL-Proxy-Client-ipAddress");  
        }  
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {  
            ipAddress = request.getHeader("HTTP_CLIENT_IP");  
        }  
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {  
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");  
        }  
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {  
            ipAddress = request.getRemoteAddr();  
        }
        String[] strs = ipAddress.split(",");
        if (strs.length > 0) {
        	ipAddress = strs[0];
		}
        return ipAddress.trim();  
    }  
}
