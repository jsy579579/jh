package com.jh.paymentchannel.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.business.CarBusiness;
import com.jh.paymentchannel.pojo.CarQueryHistory;
import com.jh.paymentchannel.pojo.CarSupportProvince;
import com.jh.paymentchannel.pojo.Province;
import com.jh.paymentchannel.pojo.UserQueryCount;
import com.jh.paymentchannel.util.AlipayServiceEnvConstants;
import com.jh.paymentchannel.util.Util;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.tools.Tools;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class CarServicePageRequest extends BaseChannel {

	private static final Logger LOG = LoggerFactory.getLogger(CarServicePageRequest.class);

	@Autowired
	Util util;

	@Autowired
	private CarBusiness carBusiness;

	@Autowired
	private TopupService topupService;
	
	@Value("${payment.ipAddress}")
	private String ipAddress;
	
	//private String ipAddress = "http://106.15.56.208";

	private String key = "ec6d08f214b19b552c84ad2888bdc8be";

	// 支付宝下单接口
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/purchase/querytimes")
	public @ResponseBody Object purchaseQueryTimes(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "brandId", required = false, defaultValue = "-1") String brandId,
			@RequestParam(value = "phone") String phone,
			@RequestParam(value = "channe_tag", required = false, defaultValue = "SPALI_PAY") String channeltag,
			@RequestParam(value = "order_desc") String orderdesc, Model model) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();

		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/brand/query/id?brand_id=" + brandId;
		RestTemplate restTemplate = new RestTemplate();
		JSONObject resultObjb = null;
		String result;
		JSONObject jsonObject;
		try {
			result = restTemplate.getForObject(url, String.class);
			LOG.info("RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			resultObjb = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("==========/v1.0/user/brand/query/id查询用户异常===========" + e);
			return "error";
		}

		String amount = resultObjb.getString("carQueryPrice");

		if (Tools.checkAmount(amount) == false) {

			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "支付金额有错");
			return map;
		}

		/*long brandId = -1;
		try {
			brandId = Long.valueOf(sbrandId);
		} catch (NumberFormatException e1) {
			brandId = -1;
		}*/
		
		uri = util.getServiceUrl("transactionclear", "error url request!");
		url = uri.toString() + "/v1.0/transactionclear/payment/add";

		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("type", "0");
		requestEntity.add("phone", phone);
		requestEntity.add("amount", amount);
		requestEntity.add("channel_tag", channeltag);
		requestEntity.add("desc", orderdesc);
		requestEntity.add("notify_url", ipAddress + "/v1.0/paymentchannel/purchase/alipay/notify_call");
		String order;
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================purchase" + result);
			jsonObject = JSONObject.fromObject(result);
			JSONObject resultObj = jsonObject.getJSONObject("result");
			order = resultObj.getString("ordercode");
		} catch (Exception e) {
			LOG.error("==========/v1.0/transactionclear/payment/add添加订单异常===========" + e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
			map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message="
					+ URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
			response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message="
					+ URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
			return map;
		}

		
		//Object topupRequest = topupService.topupRequest(request, order, orderdesc, amount, "", "", brandId, "1", channeltag);
		
		//LOG.info("topupRequest======" + topupRequest);
		
		uri = util.getServiceUrl("paymentchannel", "error url request!");
		url = uri.toString() + "/v1.0/paymentchannel/topup/request";

		//** 根据的用户手机号码查询用户的基本信息 *//*
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("amount", amount);
		requestEntity.add("ordercode", order);
		requestEntity.add("brandcode", brandId);
		requestEntity.add("orderdesc", orderdesc);
		requestEntity.add("channel_tag", channeltag);
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================purchase" + result);
			jsonObject = JSONObject.fromObject(result);
		} catch (Exception e) {
			LOG.error("==========/v1.0/paymentchannel/topup/request请求支付异常===========" + e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
			map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message="
					+ URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
			response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message="
					+ URLEncoder.encode("亲.网络出错了哦,臣妾已经尽力了,请重试~", "UTF-8"));
			return map;
		}
		String respCode = jsonObject.getString(CommonConstants.RESP_CODE);
		if (!CommonConstants.SUCCESS.equals(respCode)) {
			if ("999990".equals(respCode)) {
				map.put(CommonConstants.RESP_CODE, "999990");
				map.put(CommonConstants.RESP_MESSAGE, jsonObject.getString(CommonConstants.RESP_MESSAGE));
				// map.put(CommonConstants.RESULT,
				// ipAddress+"/v1.0/facade/purchase/to/set/bankcard/info?bankCard=" +
				// bankcard+"&userId=" + userId);
				return map;
			} else {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "亲.支付失败!");
				map.put(CommonConstants.RESULT, ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message="
						+ URLEncoder.encode("亲,支付失败!", "UTF-8"));
				response.sendRedirect(ipAddress + "/v1.0/facade/purchase/tofailepage?resp_message="
						+ URLEncoder.encode("亲,支付失败!", "UTF-8"));
				return map;
			}
		}

		try {
			response.setContentType("text/html;charset=utf-8");
			response.getWriter().println(jsonObject.getString(CommonConstants.RESULT));
			//response.getWriter().println(topupRequest);
			response.getWriter().flush();
			response.getWriter().close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;

	}

	// 用作省市联动查询省份的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/car/queryprovince")
	public @ResponseBody Object queryProvince() {

		List<String> province = carBusiness.getProvince();

		if (province != null && province.size() > 0) {

			province.remove("山西");
			province.remove("内蒙古");
			province.remove("黑龙江");
			province.remove("广西");
			province.remove("云南");
			province.remove("西藏");
			province.remove("甘肃");
			province.remove("宁夏");
			
			return ResultWrap.init(CommonConstants.SUCCESS, "查询省份成功!", province);
		} else {

			return ResultWrap.init(CommonConstants.FALIED, "暂无数据!");
		}

	}

	// 用作省市联动查询城市的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/car/querycityby/province")
	public @ResponseBody Object queryCityByProvinceId(@RequestParam(value = "province") String province) {

		Province provinceByProcince = carBusiness.getProvinceByProcince(province);

		if (provinceByProcince != null) {
			List<String> city = carBusiness.getCityByProvinceId(provinceByProcince.getProvinceid());

			if (city != null && city.size() > 0) {

				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", city);
			} else {

				return ResultWrap.init(CommonConstants.FALIED, "暂无城市数据!");
			}

		} else {

			return ResultWrap.init(CommonConstants.FALIED, "暂无该省份数据!");
		}

	}

	// 查询支持城市的接口
	@SuppressWarnings("deprecation")
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/car/querysupport/city")
	public @ResponseBody Object querySupportCity(HttpServletRequest request,
			@RequestParam(value = "brandId") String brandId,
			@RequestParam(value = "province") String province, 
			@RequestParam(value = "city") String city) {

		CarSupportProvince carSupportProvinceByProvince = carBusiness.getCarSupportProvinceByProvince(province);

		if (carSupportProvinceByProvince != null) {

			String url = "http://v.juhe.cn/wz/citys" + "?key=" + key;
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> resultStr = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
			String responseCode = resultStr.getBody();

			LOG.info("responseCode======" + responseCode);

			JSONObject fromObject = JSONObject.fromObject(responseCode);
			String resultcode = fromObject.getString("resultcode");
			String reason = fromObject.getString("reason");

			if ("200".equals(resultcode)) {

				JSONObject result = fromObject.getJSONObject("result");

				JSONObject jsonObject = result.getJSONObject(carSupportProvinceByProvince.getProvince_abbr());

				LOG.info("jsonObject======" + jsonObject);

				if (city.contains("市")) {
					city = city.substring(0, city.indexOf("市"));

				}
				LOG.info("city======" + city);

				URI uri = util.getServiceUrl("user", "error url request!");
				String url1 = uri.toString() + "/v1.0/user/brand/query/id?brand_id=" + brandId;
				JSONObject resultObjb = null;
				try {
					String result1 = restTemplate.getForObject(url1, String.class);
					LOG.info("RESULT================" + result1);
					JSONObject jsonObject1 = JSONObject.fromObject(result1);
					resultObjb = jsonObject1.getJSONObject("result");
				} catch (Exception e) {
					LOG.error("==========/v1.0/user/brand/query/id查询用户异常===========" + e);
					return "error";
				}

				String amount = resultObjb.getString("carQueryPrice");
				
				JSONArray jsonArray = jsonObject.getJSONArray("citys");
				for (int i = 0; i < jsonArray.size(); i++) {
					Object object = jsonArray.get(i);
					JSONObject fromObject2 = JSONObject.fromObject(object);
					fromObject2.put("money", amount);
					if (city.equals(fromObject2.getString("city_name"))) {

						return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", fromObject2);
					}

				}

				return ResultWrap.init(CommonConstants.FALIED, "暂不支持查询该省市车辆违章记录!");
			} else {

				return ResultWrap.init(CommonConstants.FALIED, reason);
			}
		} else {

			return ResultWrap.init(CommonConstants.FALIED, "暂不支持查询该省份!");
		}

	}

	
	//判断是否需要购买的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/car/violation/queryisaddordercode")
	public @ResponseBody Object queryIsAddOrderCode(@RequestParam(value = "phone") String phone, 
			@RequestParam(value = "brandId") String brandId
			) {
		
		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/brand/query/id?brand_id=" + brandId;
		RestTemplate restTemplate = new RestTemplate();
		JSONObject resultObjb = null;
		String result;
		JSONObject jsonObject;
		try {
			result = restTemplate.getForObject(url, String.class);
			LOG.info("RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			resultObjb = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("==========/v1.0/user/brand/query/id查询用户异常===========" + e.getMessage());
			return ResultWrap.init(CommonConstants.FALIED, "查询用户异常!");
		}

		String amount = resultObjb.getString("carQueryPrice");

		if ("0".equals(amount)) {
			LOG.info("免费查询======");

			return ResultWrap.init(CommonConstants.SUCCESS, "免费!");
		} else {
			
			uri = util.getServiceUrl("user", "error url request!");
			url = uri.toString() + "/v1.0/user/query/phone";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("phone", phone);
			requestEntity.add("brandId", brandId + "");
			JSONObject resultObju;
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("RESULT================purchase" + result);
				jsonObject = JSONObject.fromObject(result);
				resultObju = jsonObject.getJSONObject("result");
			} catch (Exception e) {
				LOG.error("==========/v1.0/user/query/phone查询用户异常===========" + e);
				return ResultWrap.init(CommonConstants.FALIED, "查询用户异常!");
			}
			String userId = "0";
			if (resultObju.containsKey("id")) {
				userId = resultObju.getString("id");
			} else {
				userId = "0";
			}

			UserQueryCount userQueryCountByUserId = carBusiness.getUserQueryCountByUserId(userId);

			if (userQueryCountByUserId != null) {
				int queryCount = userQueryCountByUserId.getCarQueryCount();
				if (queryCount > 0) {

					return ResultWrap.init(CommonConstants.SUCCESS, "已购买过查询次数!");
				} else {

					return ResultWrap.init("666666", "需要充值!");
				}

			} else {

				return ResultWrap.init("666666", "需要充值!");
			}
			
		}
		
	}
	
	
	// 下单接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/car/violation/addordercode")
	public @ResponseBody Object addOrderCode(HttpServletRequest request,
			@RequestParam(value = "brandId") String brandId, @RequestParam(value = "phone") String phone) {

		URI uri = util.getServiceUrl("user", "error url request!");
		String url = uri.toString() + "/v1.0/user/brand/query/id?brand_id=" + brandId;
		RestTemplate restTemplate = new RestTemplate();
		JSONObject resultObjb = null;
		String result;
		JSONObject jsonObject;
		try {
			result = restTemplate.getForObject(url, String.class);
			LOG.info("RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			resultObjb = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("==========/v1.0/user/brand/query/id查询用户异常===========" + e.getMessage());
			return ResultWrap.init(CommonConstants.FALIED, "查询用户异常!");
		}

		String amount = resultObjb.getString("carQueryPrice");

		if ("0".equals(amount)) {
			LOG.info("免费查询======");

			String orderCode = this.addOrder(phone, amount);

			if (orderCode != null) {

				return ResultWrap.init(CommonConstants.SUCCESS, "下单成功!", orderCode);
			} else {

				return ResultWrap.init(CommonConstants.FALIED, "下单失败!");
			}
		} else {
			LOG.info("收费查询======");

			uri = util.getServiceUrl("user", "error url request!");
			url = uri.toString() + "/v1.0/user/query/phone";
			/** 根据的用户手机号码查询用户的基本信息 */
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("phone", phone);
			requestEntity.add("brandId", brandId + "");
			JSONObject resultObju;
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("RESULT================purchase" + result);
				jsonObject = JSONObject.fromObject(result);
				resultObju = jsonObject.getJSONObject("result");
			} catch (Exception e) {
				LOG.error("==========/v1.0/user/query/phone查询用户异常===========" + e);
				return ResultWrap.init(CommonConstants.FALIED, "查询用户异常!");
			}
			String userId = "0";
			if (resultObju.containsKey("id")) {
				userId = resultObju.getString("id");
			} else {
				userId = "0";
			}

			UserQueryCount userQueryCountByUserId = carBusiness.getUserQueryCountByUserId(userId);

			if (userQueryCountByUserId != null) {
				int queryCount = userQueryCountByUserId.getCarQueryCount();
				if (queryCount > 0) {

					String orderCode = this.addOrder(phone, amount);

					if (orderCode != null) {

						return ResultWrap.init(CommonConstants.SUCCESS, "下单成功!", orderCode);
					} else {

						return ResultWrap.init(CommonConstants.FALIED, "下单失败!");
					}
				} else {

					return ResultWrap.init("666666", "需要充值!");
				}

			} else {

				return ResultWrap.init("666666", "需要充值!");
			}

		}

	}

	// 车辆违章查询的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/car/violation/inquiry")
	public @ResponseBody Object ViolationInquiry(HttpServletRequest request,
			@RequestParam(value = "orderCode") String orderCode, @RequestParam(value = "cityCode") String cityCode,
			@RequestParam(value = "carNum") String carNum, @RequestParam(value = "carType") String carType,
			@RequestParam(value = "engineNo") String engineNo, @RequestParam(value = "classNo") String classNo) {

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		// 充值卡卡号
		String userId = resultObj.getString("userid");
		String amount = resultObj.getString("amount");
		
		LOG.info("amount======" + amount);
		if("0.00".equals(amount) || "0".equals(amount) || "0.0".equals(amount)) {
			
			RestTemplate restTemplate = new RestTemplate();
			String url1 = "http://v.juhe.cn/wz/query?city=" + cityCode + "&hphm=" + URLEncoder.encode(carNum) + "&hpzl="
					+ carType + "&engineno=" + engineNo + "&classno=" + classNo + "&key=" + key;
			ResponseEntity<String> resultStr = restTemplate.exchange(url1, HttpMethod.GET, null, String.class);
			String responseCode = resultStr.getBody();

			LOG.info("responseCode======" + responseCode);

			JSONObject fromObject1 = JSONObject.fromObject(responseCode);
			String resultcode = fromObject1.getString("resultcode");
			String reason = fromObject1.getString("reason");

			if ("200".equals(resultcode)) {
				JSONObject result1 = fromObject1.getJSONObject("result");
				String hpzl = result1.getString("hpzl");
				JSONArray jsonArray = result1.getJSONArray("lists");
				
				List<Object> list = new ArrayList<>();
				for(int i = 0; i<jsonArray.size(); i++) {
					
					JSONObject jsonObject = (JSONObject) jsonArray.get(i);
					jsonObject.put("hpzl", hpzl);
					list.add(jsonObject);
				}
				
				CarQueryHistory cqh = new CarQueryHistory();
				cqh.setUserId(userId);
				cqh.setCarNum(carNum);
				cqh.setQueryHistory(list + "");
				
				carBusiness.createCarQueryHistory(cqh);
				
				this.updateOrderCode(orderCode);

				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", list);
			} else {
				
				this.addOrderCauseOfFailure(orderCode, reason);
				
				return ResultWrap.init(CommonConstants.FALIED, reason);
			}
			
		}else {
			
			UserQueryCount userQueryCountByUserId = carBusiness.getUserQueryCountByUserId(userId);

			int queryCount;
			if (userQueryCountByUserId != null) {
				queryCount = userQueryCountByUserId.getCarQueryCount();
				LOG.info("当前可用查询次数为：  " + queryCount);
				if (queryCount > 0) {
					queryCount = queryCount - 1;

					userQueryCountByUserId.setCarQueryCount(queryCount);

					carBusiness.createUserQueryCount(userQueryCountByUserId);
					
					LOG.info("当前可用查询次数为：  " + queryCount);
				}else {
					
					return ResultWrap.init("666666", "需要充值!");
				}
				
			}else {
				
				return ResultWrap.init("666666", "需要充值!");
			}

			RestTemplate restTemplate = new RestTemplate();
			String url1 = "http://v.juhe.cn/wz/query?city=" + cityCode + "&hphm=" + URLEncoder.encode(carNum) + "&hpzl="
					+ carType + "&engineno=" + engineNo + "&classno=" + classNo + "&key=" + key;
			ResponseEntity<String> resultStr = restTemplate.exchange(url1, HttpMethod.GET, null, String.class);
			String responseCode = resultStr.getBody();

			LOG.info("responseCode======" + responseCode);

			JSONObject fromObject1 = JSONObject.fromObject(responseCode);
			String resultcode = fromObject1.getString("resultcode");
			String reason = fromObject1.getString("reason");

			if ("200".equals(resultcode)) {
				JSONObject result1 = fromObject1.getJSONObject("result");
				String hpzl = result1.getString("hpzl");
				JSONArray jsonArray = result1.getJSONArray("lists");
				
				List<Object> list = new ArrayList<>();
				for(int i = 0; i<jsonArray.size(); i++) {
					
					JSONObject jsonObject = (JSONObject) jsonArray.get(i);
					jsonObject.put("hpzl", hpzl);
					list.add(jsonObject);
				}
				
				CarQueryHistory cqh = new CarQueryHistory();
				cqh.setUserId(userId);
				cqh.setCarNum(carNum);
				cqh.setQueryHistory(list + "");
				
				carBusiness.createCarQueryHistory(cqh);
				
				this.updateOrderCode(orderCode);

				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", list);
			} else {
				String errorCode = fromObject1.getString("error_code");
				if("203606".equalsIgnoreCase(errorCode)) {
					
					this.addOrderCauseOfFailure(orderCode, reason);
					
					return ResultWrap.init(CommonConstants.FALIED, reason);
					
				}else {
					
					queryCount = queryCount + 1;

					userQueryCountByUserId.setCarQueryCount(queryCount);;

					carBusiness.createUserQueryCount(userQueryCountByUserId);
					
					LOG.info("当前可用查询次数为：  " + queryCount);
					
					this.addOrderCauseOfFailure(orderCode, reason);
					
					return ResultWrap.init(CommonConstants.FALIED, reason);
				}
				
			}
			
		}
		
	}

	
	//查询历史记录总览的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/car/queryhistory")
	public @ResponseBody Object queryHistory(@RequestParam(value = "userId") String userId,
			@RequestParam(value = "id", required = false, defaultValue = "-1") long id
			) {
		
		if(id != -1) {
			CarQueryHistory carQueryHistory = carBusiness.getCarQueryHistoryByUserIdAndId(userId, id);
			
			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", carQueryHistory);
		}else {
			List<CarQueryHistory> carQueryHistoryByUserId = carBusiness.getCarQueryHistoryByUserId(userId);
			
			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", carQueryHistoryByUserId);
		}
		
	}
	
	
	// 支付宝异步回调接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/purchase/alipay/notify_call")
	public @ResponseBody Object aliPayWapNotify(HttpServletRequest request) {
		// 获取支付宝POST过来反馈信息
		Map<String, String> params = new HashMap<String, String>();
		Map requestParams = request.getParameterMap();

		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
			}

			params.put(name, valueStr);
		}
		LOG.info("支付宝WAP回调进来了============params:" + params);

		String realChannelOrderCode = params.get("trade_no");
		String orderCode = params.get("out_trade_no");
		String amount = params.get("total_amount");
		String tradeStatus = params.get("trade_status");

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");
		LOG.info("resultObj======" + resultObj);
		String userId = resultObj.getString("userid");
		
		AlipayServiceEnvConstants alipayClient = new AlipayServiceEnvConstants();
		boolean flag = false;
		if (alipayClient != null) {
			try {
				flag = AlipaySignature.rsaCheckV1(params, AlipayServiceEnvConstants.ALIPAY_PUBLIC_KEY,
						AlipayServiceEnvConstants.CHARSET, "RSA2");
			} catch (AlipayApiException e) {
				e.printStackTrace();
				return ResultWrap.err(LOG, CommonConstants.FALIED, "验签异常");
			}
		} else {
			return ResultWrap.err(LOG, CommonConstants.FALIED, "验签失败,无支付宝密钥配置");
		}

		if (!flag) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, "验签失败");
		}

		if (!"TRADE_SUCCESS".equalsIgnoreCase(tradeStatus) && !"TRADE_FINISHED".equalsIgnoreCase(tradeStatus)) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, "非成功回调");
		}

		if (new BigDecimal(resultObj.getString("amount")).compareTo(new BigDecimal(amount)) != 0) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, "验证金额失败");
		}
		if ("1".equals(resultObj.getString("status"))) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, "订单已处理");
		}

		RestTemplate restTemplate = new RestTemplate();

		UserQueryCount userQueryCountByUserId = carBusiness.getUserQueryCountByUserId(userId);
		if (userQueryCountByUserId != null) {
			LOG.info("111111111");
			userQueryCountByUserId.setCarQueryCount(userQueryCountByUserId.getCarQueryCount() + 1);
			userQueryCountByUserId
					.setUpdateTime(DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss"));

			carBusiness.createUserQueryCount(userQueryCountByUserId);
		} else {
			LOG.info("2222222");
			UserQueryCount userQueryCount = new UserQueryCount();

			userQueryCount.setUserId(userId);
			userQueryCount.setPhone(resultObj.getString("phone"));
			userQueryCount.setCarQueryCount(1);

			carBusiness.createUserQueryCount(userQueryCount);

		}

		this.updateOrderCode(orderCode);

		LOG.info("订单状态修改成功===================");

		LOG.info("订单已支付!");

		return "SUCCESS";
	}

	// 修改订单状态的方法
	public void updateOrderCode(String orderCode) {

		RestTemplate restTemplate = new RestTemplate();

		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/updateordercode";

		// **根据的用户手机号码查询用户的基本信息*//*
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("status", "1");
		requestEntity.add("order_code", orderCode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);

	}

	// 生成订单的方法
	public String addOrder(String phone, String amount) {

		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/add";

		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("type", "1");
		requestEntity.add("phone", phone);
		requestEntity.add("amount", amount);
		requestEntity.add("channel_tag", "PECCANCY_QUERY");
		requestEntity.add("desc", "车辆违章查询");
		String order;
		long brandid;
		try {
			String result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================purchase" + result);
			JSONObject jsonObject = JSONObject.fromObject(result);
			JSONObject resultObj = jsonObject.getJSONObject("result");
			order = resultObj.getString("ordercode");
			brandid = resultObj.getLong("brandid");
		} catch (Exception e) {
			LOG.error("==========/v1.0/transactionclear/payment/add添加订单异常===========" + e.getMessage());
			return null;
		}

		return order;
	}

}
