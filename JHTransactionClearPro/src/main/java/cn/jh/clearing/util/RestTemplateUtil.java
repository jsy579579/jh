package cn.jh.clearing.util;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;

@Component
public class RestTemplateUtil {
	private static final Logger LOG = LoggerFactory.getLogger(RestTemplateUtil.class);

	@Autowired
	private LoadBalancerClient loadBalancer;
	
	@Autowired
	private RestTemplate restTemplate;

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
			LOG.warn("Failed to resolve serviceId '{}'. Fallback to URL '{}'.", serviceId, uri);
		}

		return uri;
	}


	public Map<String, Object> restTemplateDoPost(String serviceName, String apiUrl,MultiValueMap<String, String> requestEntity) {
		Map<String, Object> map = new HashMap<String, Object>();
//		RestTemplate restTemplate = new RestTemplate();
//		URI uri = this.getServiceUrl(serviceName, "error url request");
		String url = "http://" + serviceName + apiUrl;
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
			map.put(CommonConstants.RESP_MESSAGE, resultJSONObject.getString(CommonConstants.RESP_MESSAGE).isEmpty()? "请求失败,请重试!" : resultJSONObject.getString(CommonConstants.RESP_MESSAGE));
			return map;
		}
		resultJSONObject = resultJSONObject.getJSONObject(CommonConstants.RESULT);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "请求成功");
		map.put(CommonConstants.RESULT, resultJSONObject);
		return map;
	}
	
	public Map<String, Object> restTemplateDoGet(String serviceName, String apiUrl) {
		Map<String, Object> map = new HashMap<String, Object>();
//		RestTemplate restTemplate = new RestTemplate();
//		URI uri = this.getServiceUrl(serviceName, "error url request");
		String url =  "http://" + serviceName + apiUrl;
		JSONObject resultJSONObject;
		try {
			String resultString = restTemplate.getForObject(url,String.class);
			resultJSONObject = JSONObject.fromObject(resultString);
		} catch (Exception e) {
			e.printStackTrace();
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "服务器繁忙,请稍后重试!");
			return map;
		}
		if (!CommonConstants.SUCCESS.equalsIgnoreCase(resultJSONObject.getString(CommonConstants.RESP_CODE))) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, resultJSONObject.getString(CommonConstants.RESP_MESSAGE).isEmpty()? "请求失败,请重试!" : resultJSONObject.getString(CommonConstants.RESP_MESSAGE));
			return map;
		}
		resultJSONObject = resultJSONObject.getJSONObject(CommonConstants.RESULT);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "请求成功");
		map.put(CommonConstants.RESULT, resultJSONObject);
		return map;
	}
}