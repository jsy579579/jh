package com.jh.paymentgateway.util;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;


@Component
public class Util {
    private static final Logger LOG = LoggerFactory.getLogger(Util.class);

    @Autowired
    private LoadBalancerClient loadBalancer;
    
    @Autowired
    private RestTemplate restTemplate;

    /**
	 * 获取请求主机IP地址,如果通过代理进来，则透过防火墙获取真实IP地址;
	 *
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public final static String getIpAddress(HttpServletRequest request) throws IOException {
		// 获取请求主机IP地址,如果通过代理进来，则透过防火墙获取真实IP地址

		String ip = request.getHeader("X-Forwarded-For");
		if (LOG.isInfoEnabled()) {
			LOG.info("getIpAddress(HttpServletRequest) - X-Forwarded-For - String ip=" + ip);
		}

		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("Proxy-Client-IP");
				if (LOG.isInfoEnabled()) {
					LOG.info("getIpAddress(HttpServletRequest) - Proxy-Client-IP - String ip=" + ip);
				}
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("WL-Proxy-Client-IP");
				if (LOG.isInfoEnabled()) {
					LOG.info("getIpAddress(HttpServletRequest) - WL-Proxy-Client-IP - String ip=" + ip);
				}
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("HTTP_CLIENT_IP");
				if (LOG.isInfoEnabled()) {
					LOG.info("getIpAddress(HttpServletRequest) - HTTP_CLIENT_IP - String ip=" + ip);
				}
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("HTTP_X_FORWARDED_FOR");
				if (LOG.isInfoEnabled()) {
					LOG.info("getIpAddress(HttpServletRequest) - HTTP_X_FORWARDED_FOR - String ip=" + ip);
				}
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getRemoteAddr();
				if (LOG.isInfoEnabled()) {
					LOG.info("getIpAddress(HttpServletRequest) - getRemoteAddr - String ip=" + ip);
				}
			}
		} else if (ip.length() > 15) {
			String[] ips = ip.split(",");
			for (int index = 0; index < ips.length; index++) {
				String strIp = (String) ips[index];
				if (!("unknown".equalsIgnoreCase(strIp))) {
					ip = strIp;
					break;
				}
			}
		}
		return ip;
	}
    
    /**
     * TODO: Complement this with a simpler version without fallback-url!
     *
     * @param serviceId
     * @param fallbackUri
     * @return
     */
    public URI getServiceUrl(String serviceId, String fallbackUri) {
        URI uri = null;
        try {
            ServiceInstance instance = loadBalancer.choose(serviceId);
            System.out.println(instance);
            uri = instance.getUri();
            LOG.debug("Resolved serviceId '{}' to URL '{}'.", serviceId, uri);

        } catch (RuntimeException e) {
            e.printStackTrace();
            uri = URI.create(fallbackUri);
            LOG.warn("Failed to resolve serviceId '{}'. Fallback to URL '{}'.", serviceId, uri);
        }

        return uri;
    }
    
    public Date StrToDate(String str) {
    	  
    	   SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	   Date date = null;
    	   try {
    	    date = format.parse(str);
    	   } catch (ParseException e) {
    	    e.printStackTrace();
    	   }
    	   return date;
    	}
    
    public String DateToStr(Date date) {
    	  
    	   SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	   String str = format.format(date);
    	   return str;
    	} 

    public <T> ResponseEntity<T> createOkResponse(T body) {
        return createResponse(body, HttpStatus.OK);
    }

    public <T> ResponseEntity<T> createResponse(T body, HttpStatus httpStatus) {
        return new ResponseEntity<T>(body, httpStatus);
        
    }
    
    public String blogTrim(String blog){
    	
    	String str1="";
    	String str2="";
    	String str3="";
    	
    	//如果blog中不包含图片，则不用处理，直接返回
    	if(!blog.contains("src=")){
    		System.out.println("不包含src的blog"+blog);
    		return blog;
    	}
    	str1=blog.substring(0,blog.indexOf("src="));
    	str2=blog.substring(blog.indexOf("src="),blog.indexOf(">")).replace(" ", "+");
    	str3=blog.substring(blog.indexOf(">"));
    	
    	blog=str1+str2+str3;
    	
    	return blog;
    }
    
    //给银行名称做处理
    public static String queryBankNameByBranchName(String bankbranchName) {
		String bankname = null;
		if (bankbranchName.equals("中国银行")||bankbranchName.equals("农业银行") ||bankbranchName.equals("中国农业银行")) {
			bankname = bankbranchName;
		}else {
			// 银行名是包含中国
			if (bankbranchName.contains("中国")) {
				// 银行名包含中国银行
				if (bankbranchName.contains("中国银行")) {
					int Front = bankbranchName.indexOf("中国");
					int after = bankbranchName.indexOf("银行");
					bankname = bankbranchName.substring(0, after + 2);
				// 银行名不包含中国银行
				} else {
					int Front = bankbranchName.indexOf("中国");
					int after = bankbranchName.indexOf("银行");
					bankname = bankbranchName.substring(Front + 2, after + 2);
				}
			// 银行名包含银行
			} else if (bankbranchName.contains("银行")) {
				// 银行名包含浦发银行或者广发银行
				if (bankbranchName.contains("浦发银行") || bankbranchName.contains("广发银行") || bankbranchName.contains("邮储银行") || bankbranchName.contains("深发银行") || bankbranchName.contains("浙商银行")) {
					// 银行名包含浦发银行
					if (bankbranchName.contains("浦发银行")) {
						bankname = "浦东发展银行";
					// 银行名包含广发银行
					}
					if (bankbranchName.contains("广发银行")) {
						bankname = "广东发展银行";
					}
					// 银行名包含邮储银行
					if (bankbranchName.contains("邮储银行")) {
						bankname = "邮政储蓄银行";
					}
					if (bankbranchName.contains("深发银行")) {
						bankname = "深圳发展银行";
					}
					if (bankbranchName.contains("浙商银行")) {
						bankname = "浙江商业银行";
					}
					if(bankbranchName.contains("交通")) {
						bankname="交通银行";
					}
				// 银行名不包含浦发和广发和邮储
				} else {
					int after = bankbranchName.indexOf("银行");
					bankname = bankbranchName.substring(0, after + 2);
				}
			// 银行名不包含中国和银行
			} else {
				bankname = bankbranchName;
			}
		}
		return bankname;
	}
    
    
    //给银行名称过滤的方法
    public static String strSub(String bankname) {
		if (bankname.length() <= 4) {
			return bankname;
		}else {
			String[] strNum = {"上","农","广","邮","储","银","华","北","南","徽"};
			String str0 = "" + bankname.charAt(0);
			String str1 = "" + bankname.charAt(2);
			if ("浦".equals(str0) || "浦".equals(str1)) {
				return "浦发银行";
			}
			if (strNum[0].equals(str0) && strNum[1].equals(str1)) {
				return "上海农商银行";
			}
			if (strNum[2].equals(str0) && strNum[1].equals(str1)) {
				return "广州农商银行";
			}
			if (strNum[3].equals(str0) && strNum[4].equals(str1)) {
				return "邮储银行";
			}
			if (strNum[6].equals(str0) && strNum[5].equals(str1)) {
				return "华夏银行";
			}
			if (strNum[7].equals(str0) && strNum[5].equals(str1)) {
				return "北京银行";
			}
			if (strNum[8].equals(str0) && strNum[5].equals(str1)) {
				return "南京银行";
			}
			if (strNum[9].equals(str0) && strNum[5].equals(str1)) {
				return "徽商银行";
			}
			if (strNum[2].equals(str0) && strNum[5].equals(str1)) {
				return "广州银行";
			}
			if (bankname.contains("福建省农村信用社")) {
				return "福建省农村信用社联合社";
			}
		}
		return "城市商业银行";
	}
    
    
    public Map<String, Object> restTemplateDoPost(String serviceName, String apiUrl,
			LinkedMultiValueMap<String, String> requestEntity) {
		Map<String, Object> map = new HashMap<>();
		RestTemplate restTemplate = new RestTemplate();
		URI uri = this.getServiceUrl(serviceName, "error url request");
		String url = uri.toString() + apiUrl;
		JSONObject resultJSONObject;
		try {
			String resultString = restTemplate.postForObject(url, requestEntity, String.class);
			resultJSONObject = JSONObject.fromObject(resultString);
		} catch (Exception e) {
			e.printStackTrace();
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "服务器繁忙,请稍后重试!");
			return map;
		}
		if (!CommonConstants.SUCCESS.equalsIgnoreCase(resultJSONObject.getString(CommonConstants.RESP_CODE))) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, resultJSONObject.getString(CommonConstants.RESP_MESSAGE).isEmpty()
					? "请求失败,请重试!" : resultJSONObject.getString(CommonConstants.RESP_MESSAGE));
			return map;
		}
		resultJSONObject = resultJSONObject.getJSONObject(CommonConstants.RESULT);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "请求成功");
		map.put(CommonConstants.RESULT, resultJSONObject);
		return map;
	}
    
	public void pushMessage(String userId,String message,String alert){
		LinkedMultiValueMap<String,String> requestEntity = new LinkedMultiValueMap<String,String>();
		requestEntity.add("userId", userId);
		requestEntity.add("alert", alert);
		requestEntity.add("content", "亲爱的会员,"+message);
		requestEntity.add("btype", "balanceadd");
		requestEntity.add("btypeval", "");
		String url = "http://user/v1.0/user/jpush/tset";
		restTemplate.postForObject(url, requestEntity,String.class);
	}

}
