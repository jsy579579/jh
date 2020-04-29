package com.jh.paymentchannel.service;

import java.beans.IntrospectionException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.jh.paymentchannel.business.BranchbankBussiness;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.BranchNo;
import com.jh.paymentchannel.pojo.XJAreaCode;
import com.jh.paymentchannel.pojo.XJQuickRegister;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.xj.Bean2MapUtil;
import com.jh.paymentchannel.util.xj.MerchantDTO;
import com.jh.paymentchannel.util.xj.OrderDTO;
import com.jh.paymentchannel.util.xj.SignUtils;

import cn.jh.common.utils.DateUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class XJpageRequest {

	private static final Logger log = LoggerFactory.getLogger(XJpageRequest.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private BranchbankBussiness branchbankBussiness;

	@Autowired
	private Util util;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	private String appId = "10058908";
	
	private String key = "bed905db16ee4024a89b3b8367595b35";
	
	private String xjurl = "https://api.xjpay.cc";
	
	private static final Charset UTF_8 = StandardCharsets.UTF_8;
	
	
	//进件接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/xj/register")
	public @ResponseBody Object XJRegister(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "ordercode") String ordercode) throws Exception {
		log.info("开始进入进件接口========================");
		Map map = new HashMap();
		Map<String, String> maps = new HashMap<String, String>();
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("RESULT================" + result);
		JSONObject jsonObject;
		JSONObject resultObj;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			log.error("查询订单信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "没有该订单信息");
			return map;
		}
		String userid = resultObj.getString("userid");
		// 费率
		String rate = resultObj.getString("rate");
		// 额外费率
		String extraFee = resultObj.getString("extraFee");
		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/userid";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("user_id", userid);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		log.info("RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			log.error("查询默认结算卡出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询默认结算卡有误");
			return map;
		}
		String respcode = jsonObject.getString("resp_code");
		// 默认提现卡卡号
		String cardNo = resultObj.getString("cardNo");
		String userName = resultObj.getString("userName");
		/** 身份证号 */
		String idcard = resultObj.getString("idcard");
		String phone = resultObj.getString("phone");
		String bankName = resultObj.getString("bankName");

		//随机获取商铺信息表中的商户数据
		restTemplate=new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/query/randomuserid";
		requestEntity  = new LinkedMultiValueMap<String, String>();
		
		String shopUserId;
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			jsonObject =  JSONObject.fromObject(result);
			shopUserId = jsonObject.getString("result");
		} catch (Exception e1) {
			log.error("查询用户ID出错！！！！");
			maps.put("resp_code", "failed");
			maps.put("channel_type", "sdj");
			maps.put("resp_message", "没有查询到用户ID");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "没有查询到用户ID");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return maps;
		}
		
		log.info("随机获取的userId"+shopUserId);
		
		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/shops/query/uid";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("userid", shopUserId);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		log.info("RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			log.error("查询商铺信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询商铺信息有误");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "查询商铺信息有误");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}
		String address = resultObj.getString("address");// 注册地址
		String shopsAddress = resultObj.getString("shopsaddress");

		String Address = address + shopsAddress;


		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/shops/find/province";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("userId", shopUserId);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		log.info("RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
		} catch (Exception e) {
			log.error("查询商铺省市出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "该商铺省市信息有误");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "查询该商铺省市信息信息有误");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}
		String province = jsonObject.getString("province");
		String city = jsonObject.getString("city");
		
		if(province.contains("省")) {
			province = province.substring(0, province.indexOf("省"));
		}
		if(province.contains("市")) {
			province = province.substring(0, province.indexOf("市"));
		}
		if(province.contains("广西")) {
			province = "广西";
		}
		if(province.contains("内蒙古")) {
			province = "内蒙古";
		}
		if(province.contains("宁夏")) {
			province = "宁夏";
		}
		if(province.contains("新疆")) {
			province = "新疆";
		}
		if(province.contains("西藏")) {
			province = "西藏";
		}
		if (province.contains("其他")) {
			province = "上海";
		}
		
		city = city.substring(0, city.indexOf("市"));
		
		if (city.contains("其他")) {
			city = "上海";
		}
		
		log.info("province======"+province);
		log.info("city======"+city);
		
		String AreaCodeOfProvince = null;
		String AreaCodeOfCity = null;
		try {
			AreaCodeOfProvince = topupPayChannelBusiness.getXJAreaCode(province,"2");
			AreaCodeOfCity = topupPayChannelBusiness.getXJAreaCode(city,"3");
		} catch (Exception e) {
			log.error("查询省市区编号出错啦===="+e);
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "交易排队中，请稍后重试!");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "查询省市区编号出错");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}
		
		log.info("AreaCodeOfProvince======"+AreaCodeOfProvince);
		log.info("AreaCodeOfCity======"+AreaCodeOfCity);
		
		BigDecimal bigRate = new BigDecimal(rate);
		BigDecimal bigTen = new BigDecimal("1000");
		
		BigDecimal multiply = bigRate.multiply(bigTen);
		
		//将金额转换为以分为单位:
		String ExtraFee = new BigDecimal(extraFee).multiply(new BigDecimal("100")).setScale(0).toString();
		
		UUID randomUUID = UUID.randomUUID();
		String replace = randomUUID.toString().replace("-", "").substring(0, 16);
		
		MerchantDTO mer = new MerchantDTO();
		mer.setAppId(appId);
		mer.setAddress(Address);
		mer.setProvinceCode(AreaCodeOfProvince);
		mer.setCityCode(AreaCodeOfCity);
		mer.setPointsType("0");
		mer.setD0fee(Integer.valueOf(ExtraFee));
		mer.setFee0(multiply+"");
		mer.setNonceStr(replace);
		
		String s = cardNo+"|"+userName+"|"+idcard+"|"+phone;
		String re = SignUtils.encode(s, key);
		log.info("re======"+re);
		
		mer.setCustomerInfo(re);
		
		Map params;
		try {
           
            params = Bean2MapUtil.convertBean(mer);
			String signResult = SignUtils.getSign(params, key);
			log.info("signResult====="+signResult);
			mer.setSign(signResult);
			String param = JSON.toJSONString(mer);
			log.info("请求报文======"+param);
			log.info("请求地址======"+xjurl+"/api/v1.0/debit");
            PostMethod method = new PostMethod(xjurl+"/api/v1.0/debit");
            method.setRequestHeader("Content-type", "application/json; charset=UTF-8");
            method.setRequestHeader("Accept", "application/json; charset=UTF-8");
            method.setRequestBody(param);
            HttpClient client = new HttpClient();
            int rspCode = client.executeMethod(method);
            
            log.info("rspCode====="+rspCode);
            String receive = method.getResponseBodyAsString();
            log.info("receive====="+receive);
            
            com.alibaba.fastjson.JSONObject parseObject = JSON.parseObject(receive);
            
            String isSuccess = parseObject.getString("isSuccess");
            
            log.info("isSuccess======"+isSuccess);
            
            //进件成功
            if("true".equals(isSuccess)) {
            	log.info("进件成功======");
            	String data = parseObject.getString("data");
            	log.info("data======"+data);
            	
            	XJQuickRegister xjQuickRegister = new XJQuickRegister();
            	xjQuickRegister.setPhone(phone);
            	xjQuickRegister.setBankCard(cardNo);
            	xjQuickRegister.setIdCard(idcard);
            	xjQuickRegister.setMerchantCode(data);
            	xjQuickRegister.setRate(rate);
            	xjQuickRegister.setExtraFee(extraFee);
            	
            	try {
					topupPayChannelBusiness.createXJQuickRegister(xjQuickRegister);
				} catch (Exception e) {
					log.error("保存用户成功进件的信息失败======"+e);
					map.put("resp_code", "failed");	
			        map.put("channel_type", "jf");
					map.put("resp_message", "亲，进件失败啦，请稍后重试");
					
					restTemplate = new RestTemplate();
					uri = util.getServiceUrl("transactionclear", "error url request!");
					url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
					requestEntity = new LinkedMultiValueMap<String, String>();
					requestEntity.add("ordercode", ordercode);
					requestEntity.add("remark", "保存用户进件的信息失败");
					result = restTemplate.postForObject(url, requestEntity, String.class);
					
					return map;
				}
            	
            	log.info("开始进入支付接口=====");
            	
            	map = (Map) this.XJPay(request, response, ordercode);
            	
            }else {
            	log.info("进件失败======");
            	String message = parseObject.getString("message");
            	log.info("message======"+message);
            	
            	map.put("resp_code", "failed");	
		        map.put("channel_type", "jf");
				map.put("resp_message", message);
				
				restTemplate = new RestTemplate();
				uri = util.getServiceUrl("transactionclear", "error url request!");
				url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("ordercode", ordercode);
				requestEntity.add("remark", message);
				result = restTemplate.postForObject(url, requestEntity, String.class);
				
            }
            
            
            
		} catch (Exception e) {
			log.error("进件出现异常啦======"+e);
			map.put("resp_code", "failed");	
	        map.put("channel_type", "jf");
			map.put("resp_message", "亲，进件失败啦，请稍后重试");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "进件失败,请稍后重试");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
		}
		
		return map;
	}
	
	
	
	//支付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/xj/pay")
	public @ResponseBody Object XJPay(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "ordercode") String ordercode) throws Exception {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		log.info("开始进入支付接口========================");
		Map<String, String> maps = new HashMap<String, String>();
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("RESULT================" + result);
		JSONObject jsonObject;
		JSONObject resultObj;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			log.error("查询订单信息出错");
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", "没有该订单信息");
			return maps;
		}
		String userid = resultObj.getString("userid");
		// 费率
		String rate = resultObj.getString("rate");
		// 额外费用
		String extraFee = resultObj.getString("extraFee");
		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");

		String amount = resultObj.getString("amount");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("transactionclear", "error url request!");
		url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("ordercode", ordercode);
		requestEntity.add("remark", "开始进入支付接口");
		result = restTemplate.postForObject(url, requestEntity, String.class);
		
		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/cardno";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("cardno", bankCard);
		requestEntity.add("type", "0");
		result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("接口/v1.0/user/bank/default/cardno--RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			log.error("查询银行卡信息出错");
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", "查询银行卡信息有误");
			return maps;
		}
		String phone = resultObj.getString("phone"); // 预留信用卡手机号码
		String idcard = resultObj.getString("idcard");// 身份证号
		String userName = resultObj.getString("userName");// 用户姓名


		// 将订单总金额转换为以分为单位
		String Amount = new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0).toString();

		Double drate = Double.valueOf(rate.trim());
		log.info("drate=============" + drate);
		Double damount = Double.valueOf(amount.trim());
		log.info("damount=============" + damount);

		// 手续费
		BigDecimal bigamount = new BigDecimal(amount);
		BigDecimal bigrate = new BigDecimal(rate);
		BigDecimal bigextraFee = new BigDecimal(extraFee);
		// Bigcecimal类型的运算
		BigDecimal multiply = bigamount.multiply(bigrate);
		log.info("手续费" + multiply);
		// 小数点后2位向上进1位
		BigDecimal setScale = multiply.setScale(2, RoundingMode.CEILING);

		BigDecimal add = setScale.add(bigextraFee);
		log.info("用户总手续费======" + add);

		String Fee = new BigDecimal(add + "").multiply(new BigDecimal("100")).setScale(0).toString();
		log.info("用户总手续费(以分为单位)======" + Fee);

		UUID randomUUID = UUID.randomUUID();
		String replace = randomUUID.toString().replace("-", "").substring(0, 16);
		
		String merchantCode = null;
		try {
			XJQuickRegister xjQuickRegister = topupPayChannelBusiness.getXJQuickRegister(idcard);
			merchantCode = xjQuickRegister.getMerchantCode();
		} catch (Exception e) {
			log.error("查询用户进件信息出错======"+e);
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", "查询用户进件信息出错啦");
			return maps;
		}
		
		OrderDTO order = new OrderDTO();
		order.setAppId(appId);
		String s = bankCard+"|"+userName+"|"+idcard+"|"+phone;;
		String re = SignUtils.encode(s, key);
		order.setCustomerInfo(re);
		order.setAgentOrderNo(ordercode);
		order.setNonceStr(replace);
		order.setTotalFee(Amount);
		order.setNotifyUrl(ipAddress+"/v1.0/paymentchannel/topup/xj/notify_call");
		order.setMchId(merchantCode);
		order.setReturnUrl(ipAddress+"/v1.0/paymentchannel/topup/xj/return_call");
		
		Map params;
		try {
			params = Bean2MapUtil.convertBean(order);
			String signResult = SignUtils.getSign(params, key);
			log.info("signResult======="+signResult);
			order.setSign(signResult);
			String param = JSON.toJSONString(order);
			log.info("param======"+param);
			
			PostMethod method = new PostMethod(xjurl+"/api/v1.0/preOrder");
            method.setRequestHeader("Content-type", "application/json; charset=UTF-8");
            method.setRequestHeader("Accept", "application/json; charset=UTF-8");
            method.setRequestBody(param);
            HttpClient client = new HttpClient();
            int rspCode = client.executeMethod(method);
            
            log.info("rspCode======"+rspCode);
            String receive = method.getResponseBodyAsString();
            log.info("receive======"+receive);

            com.alibaba.fastjson.JSONObject parseObject = JSON.parseObject(receive);
            
            String isSuccess = parseObject.getString("isSuccess");
            
            log.info("isSuccess======"+isSuccess);
            
            if("true".equals(isSuccess)) {
            	log.info("请求支付接口成功======");
            	
            	restTemplate = new RestTemplate();
				uri = util.getServiceUrl("transactionclear", "error url request!");
				url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("ordercode", ordercode);
				requestEntity.add("remark", "开始跳转");
				result = restTemplate.postForObject(url, requestEntity, String.class);
            	
            	com.alibaba.fastjson.JSONObject data = parseObject.getJSONObject("data");
            	log.info("data======"+data);
            	String returnHtml = data.getString("returnHtml");
            	log.info("returnHtml======"+returnHtml);
            	
            	maps.put("resp_code", "success");
    			maps.put("channel_type", "jf");
    			maps.put("pageContent", returnHtml);	
    			return maps;
            	
            }else {
            	log.info("请求支付接口失败======");
            	
            	String message = parseObject.getString("message");
            	log.info("message====="+message);
            	
            	maps.put("resp_code", "failed");	
		        maps.put("channel_type", "jf");
				maps.put("resp_message", message);
				
				restTemplate = new RestTemplate();
				uri = util.getServiceUrl("transactionclear", "error url request!");
				url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("ordercode", ordercode);
				requestEntity.add("remark", message);
				result = restTemplate.postForObject(url, requestEntity, String.class);
				
				return maps;
            }
            
            
		} catch (Exception e) {
			log.error("请求支付接口出错啦====="+e);
			maps.put("resp_code", "failed");	
	        maps.put("channel_type", "jf");
			maps.put("resp_message", "交易排队中，请稍后重试！");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "请求支付接口出错");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return maps;
			
		}
		
	}
	
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/toxjbankinfo")
	public String toxjbankinfo(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String bankName = request.getParameter("bankName");// 结算卡银行名称
		String bankNo = request.getParameter("bankNo");// 结算卡卡号
		String amount = request.getParameter("amount");
		String ordercode = request.getParameter("ordercode");
		String cardType = request.getParameter("cardType");
		String isRegister = request.getParameter("isRegister");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("bankName", bankName);
		model.addAttribute("bankNo", bankNo);
		model.addAttribute("amount", amount);
		model.addAttribute("ordercode", ordercode);
		model.addAttribute("cardType", cardType);
		model.addAttribute("isRegister", isRegister);
		model.addAttribute("ipAddress", ipAddress);

		return "xjbankinfo";
	}

	
	
	// 交易同步通知
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/xj/return_call")
	public String xjReturnCallback(HttpServletRequest request, HttpServletResponse response

	) throws Exception {

		log.info("支付同步通知进来了");
		
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		
		return "sdjsuccess";
	}
	
	
	
	
	// 交易异步通知
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/xj/notify_call")
	public void xjNotifyCallback(HttpServletRequest request, HttpServletResponse response

	) throws Exception {

		log.info("支付异步通知进来了");

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		
		InputStream inputStream = request.getInputStream();
		ByteArrayOutputStream byteArray = null;
		byteArray = new ByteArrayOutputStream();
		byte[] dat = new byte[2048];
		int l = 0;
		while ((l = inputStream.read(dat, 0, 2048)) != -1) {
			byteArray.write(dat, 0, l);
		}
		byteArray.flush();
		log.info("ByteArrayOutputStream2String=============" + new String(byteArray.toByteArray(), "UTF-8"));
		String info = new String(byteArray.toByteArray(), "UTF-8");
		JSONObject jsonInfo = JSONObject.fromObject(info);
		log.info("jsonInfo=============" + jsonInfo.toString());
		inputStream.close();
		byteArray.close();
		
		String agentOrderNo = jsonInfo.getString("agentOrderNo");
		String state = jsonInfo.getString("state");
		
		log.info("订单号  agentOrderNo======"+agentOrderNo);
		log.info("订单状态 state======"+state);
       
		if("4".equals(state)) {
			
			RestTemplate restTemplate = new RestTemplate();

			URI uri = util.getServiceUrl("transactionclear", "error url request!");
			String url = uri.toString() + "/v1.0/transactionclear/payment/update";

			// **根据的用户手机号码查询用户的基本信息*//*
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", agentOrderNo);
			String result = restTemplate.postForObject(url, requestEntity, String.class);

			log.info("订单状态修改成功===================");
			
			log.info("订单已支付并代付成功!");
			
			PrintWriter writer = response.getWriter();
			writer.print("success");
			writer.close();
			
		}else if("1".equals(state)){
			
			log.info("订单支付失败!");
		}else {
			
			log.info("订单支付结算处理中!");
		}
        
	}

}