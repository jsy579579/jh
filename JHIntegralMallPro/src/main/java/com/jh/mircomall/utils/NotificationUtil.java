package com.jh.mircomall.utils;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

@SuppressWarnings("all")
@Component
public class NotificationUtil {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationUtil.class);

    @Autowired
    private LoadBalancerClient loadBalancer;

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
            uri = instance.getUri();
            LOG.debug("Resolved serviceId '{}' to URL '{}'.", serviceId, uri);

        } catch (RuntimeException e) {
            e.printStackTrace();
            uri = URI.create(fallbackUri);
            LOG.error("Failed to resolve serviceId '{}'. Fallback to URL '{}'.", serviceId, uri);
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
    
    
    //给银行名称做处理
    public static String queryBankNameByBranchName(String bankbranchName) {
		String bankname = null;
		if (bankbranchName.equals("中国银行")) {
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
						bankname = "中国邮政储蓄银行";
					}
					if (bankbranchName.contains("深发银行")) {
						bankname = "深圳发展银行";
					}
					if (bankbranchName.contains("浙商银行")) {
						bankname = "浙江商业银行";
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
    
    
}
