
package com.jh.paymentchannel.util.ump.util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.jh.paymentchannel.util.ump.exception.ParameterCheckException;

/**
 * ***********************************************************************
 * <br>description : 解析数据工具类
 * @author      umpay
 * @date        2014-7-25 上午10:14:22
 * @version     1.0  
 ************************************************************************
 */
public class DataUtil {

	public static Map getData(Object obj){
		if(obj==null){
			throw new RuntimeException("请求对象为NULL");
		}
		if(obj instanceof Map) {
			return (Map) obj;
		}else if(obj instanceof HttpServletRequest){
			HttpServletRequest request = (HttpServletRequest) obj;
			Map fieldMap = new HashMap();
			Enumeration names = request.getParameterNames();
			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
				String values = request.getParameter(name);
				if(null!=values)values = values.trim();
				fieldMap.put(name, values);
			}
			return fieldMap;
		}else{
			throw new ParameterCheckException("数据集合只支持java.util.Map 和 javax.servlet.http.HttpServletRequest");
		}
	}
}
