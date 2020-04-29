package com.jh.paymentchannel.service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PutMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.jh.paymentchannel.business.BranchbankBussiness;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.PaymentOrder;
import com.jh.paymentchannel.pojo.XJQuickRegister;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.xj.Bean2MapUtil;
import com.jh.paymentchannel.util.xj.MerchantUpdateDTO;
import com.jh.paymentchannel.util.xj.SignUtils;

import net.sf.json.JSONObject;

@Service
public class XJTopupRequest implements TopupRequest {

	private static final Logger log = LoggerFactory.getLogger(XJTopupRequest.class);

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

	@Override
	public Map<String, String> topupRequest(Map<String,Object> params) throws Exception {
		PaymentOrder paymentOrder = (PaymentOrder) params.get("paymentOrder");
		String ordercode = paymentOrder.getOrdercode();
		String amount = paymentOrder.getAmount().toString();

		Map<String, String> map = new HashMap<String, String>();
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
		String realamount = resultObj.getString("realAmount");
		String userid = resultObj.getString("userid");
		// 费率
		String rate = resultObj.getString("rate");
		// 额外费率
		String extraFee = resultObj.getString("extraFee");
		// 银行卡
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

		String cardNo = resultObj.getString("cardNo");
		String userName = resultObj.getString("userName");
		/** 身份证号 */
		String idcard = resultObj.getString("idcard");
		String phone = resultObj.getString("phone");
		// 银行名称
		String bankName = resultObj.getString("bankName");

		String cardType = resultObj.getString("cardType");

		String bankname = Util.queryBankNameByBranchName(bankName);

		XJQuickRegister xjQuickRegister = topupPayChannelBusiness.getXJQuickRegister(idcard);

		if (xjQuickRegister == null) {
			log.info("用户需要进件========");
			// xjQuickRegister为空表示用户需要进件
			map.put("resp_code", "success");
			map.put("channel_type", "jf");
			map.put("redirect_url",
					ipAddress + "/v1.0/paymentchannel/topup/toxjbankinfo" + "?bankName="
							+ URLEncoder.encode(bankname, "UTF-8") + "&bankNo=" + cardNo + "&amount=" + amount
							+ "&ordercode=" + ordercode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
							+ "&isRegister=0" + "&ipAddress=" + ipAddress);
			return map;

		} else {

			if (!cardNo.equals(xjQuickRegister.getBankCard()) || !rate.equals(xjQuickRegister.getRate())
					|| !extraFee.equals(xjQuickRegister.getExtraFee())) {
				log.info("需要修改进件信息====");
				boolean updateRegister = updateRegister(ordercode);
				if (updateRegister) {

					log.info("发起交易======");
					map.put("resp_code", "success");
					map.put("channel_type", "jf");
					map.put("redirect_url",
							ipAddress + "/v1.0/paymentchannel/topup/toxjbankinfo" + "?bankName="
									+ URLEncoder.encode(bankname, "UTF-8") + "&bankNo=" + cardNo + "&amount=" + amount
									+ "&ordercode=" + ordercode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
									+ "&isRegister=1" + "&ipAddress=" + ipAddress);
					return map;
				} else {
					map.put("resp_code", "failed");
					map.put("channel_type", "jf");
					map.put("resp_message", "亲，修改进件信息出错啦,请稍后重试！");
					
					restTemplate = new RestTemplate();
					uri = util.getServiceUrl("transactionclear", "error url request!");
					url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
					requestEntity = new LinkedMultiValueMap<String, String>();
					requestEntity.add("ordercode", ordercode);
					requestEntity.add("remark", "亲，修改进件信息出错啦,请稍后重试！");
					result = restTemplate.postForObject(url, requestEntity, String.class);
					
					return map;
				}

			} else {

				log.info("发起交易======");
				// xjQuickRegister不为空表示用户直接可以发起交易
				map.put("resp_code", "success");
				map.put("channel_type", "jf");
				map.put("redirect_url",
						ipAddress + "/v1.0/paymentchannel/topup/toxjbankinfo" + "?bankName="
								+ URLEncoder.encode(bankname, "UTF-8") + "&bankNo=" + cardNo + "&amount=" + amount
								+ "&ordercode=" + ordercode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
								+ "&isRegister=1" + "&ipAddress=" + ipAddress);
				return map;

			}

		}

	}

	// 修改进件信息
	public boolean updateRegister(String ordercode) {

		boolean isTrue = false;
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("RESULT================" + result);
		JSONObject jsonObject;
		JSONObject resultObj = null;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			return isTrue;
		}
		String userid = resultObj.getString("userid");
		// 费率
		String rate = resultObj.getString("rate");
		// 额外费率
		String extraFee = resultObj.getString("extraFee");
		// 银行卡
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
			return isTrue;
		}

		// 默认提现卡卡号
		String cardNo = resultObj.getString("cardNo");
		String userName = resultObj.getString("userName");
		/** 身份证号 */
		String idcard = resultObj.getString("idcard");
		String phone = resultObj.getString("phone");

		BigDecimal bigRate = new BigDecimal(rate);
		BigDecimal bigTen = new BigDecimal("1000");

		BigDecimal multiply = bigRate.multiply(bigTen);

		// 将金额转换为以分为单位:
		String ExtraFee = new BigDecimal(extraFee).multiply(new BigDecimal("100")).setScale(0).toString();

		UUID randomUUID = UUID.randomUUID();
		String replace = randomUUID.toString().replace("-", "").substring(0, 16);

		String merchantCode = null;
		XJQuickRegister xjQuickRegister;
		try {
			xjQuickRegister = topupPayChannelBusiness.getXJQuickRegister(idcard);
			merchantCode = xjQuickRegister.getMerchantCode();
		} catch (Exception e) {
			return isTrue;
		}

		// 随机获取商铺信息表中的商户数据
		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/query/randomuserid";
		requestEntity = new LinkedMultiValueMap<String, String>();

		String shopUserId;
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			jsonObject = JSONObject.fromObject(result);
			shopUserId = jsonObject.getString("result");
		} catch (Exception e1) {
			log.error("查询用户ID出错！！！！");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "查询用户ID出错");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return isTrue;
		}

		log.info("随机获取的userId" + shopUserId);

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
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "查询商铺省市出错");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return isTrue;
		}
		String province = jsonObject.getString("province");
		String city = jsonObject.getString("city");

		if (province.contains("省")) {
			province = province.substring(0, province.indexOf("省"));
		}
		if (province.contains("市")) {
			province = province.substring(0, province.indexOf("市"));
		}
		if (province.contains("广西")) {
			province = "广西";
		}
		if (province.contains("内蒙古")) {
			province = "内蒙古";
		}
		if (province.contains("宁夏")) {
			province = "宁夏";
		}
		if (province.contains("新疆")) {
			province = "新疆";
		}
		if (province.contains("西藏")) {
			province = "西藏";
		}
		if (province.contains("其他")) {
			province = "上海";
		}

		city = city.substring(0, city.indexOf("市"));

		if (city.contains("其他")) {
			city = "上海";
		}

		log.info("province======" + province);
		log.info("city======" + city);

		String AreaCodeOfProvince = topupPayChannelBusiness.getXJAreaCode(province, "2");
		String AreaCodeOfCity = topupPayChannelBusiness.getXJAreaCode(city, "3");

		log.info("AreaCodeOfProvince======" + AreaCodeOfProvince);
		log.info("AreaCodeOfCity======" + AreaCodeOfCity);

		MerchantUpdateDTO mer = new MerchantUpdateDTO();
		mer.setAppId(appId);
		mer.setNonceStr(replace);
		mer.setMchId(merchantCode);
		mer.setFee0(multiply + "");
		mer.setD0fee(ExtraFee);

		mer.setProvinceCode(AreaCodeOfProvince);
		mer.setCityCode(AreaCodeOfCity);

		String s = cardNo + "|" + userName + "|" + idcard + "|" + phone;
		String re = SignUtils.encode(s, key);
		log.info("re======" + re);

		mer.setCustomerInfo(re);

		Map params;
		try {
			params = Bean2MapUtil.convertBean(mer);
			String signResult = SignUtils.getSign(params, key);
			log.info(signResult);
			mer.setSign(signResult);
			log.info(signResult);
			String param = JSON.toJSONString(mer);
			log.info(param);
			PutMethod method = new PutMethod(xjurl + "/api/v1.0/debit");
			method.setRequestHeader("Content-type", "application/json; charset=UTF-8");
			method.setRequestHeader("Accept", "application/json; charset=UTF-8");
			method.setRequestBody(param);
			HttpClient client = new HttpClient();
			int rspCode = client.executeMethod(method);
			//
			log.info("rspCode:" + rspCode);
			String receive = method.getResponseBodyAsString();
			log.info("receive:" + receive);

			com.alibaba.fastjson.JSONObject parseObject = JSON.parseObject(receive);

			String isSuccess = parseObject.getString("isSuccess");

			log.info("isSuccess======" + isSuccess);

			if ("true".equals(isSuccess)) {
				log.info("修改费率成功=======");
				xjQuickRegister.setRate(rate);
				xjQuickRegister.setExtraFee(extraFee);
				xjQuickRegister.setPhone(phone);
				xjQuickRegister.setBankCard(cardNo);
				
				topupPayChannelBusiness.createXJQuickRegister(xjQuickRegister);

				isTrue = true;
				log.info("isTrue=====" + isTrue);
				return isTrue;
			} else {
				String message = parseObject.getString("message");
				
				restTemplate = new RestTemplate();
				uri = util.getServiceUrl("transactionclear", "error url request!");
				url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("ordercode", ordercode);
				requestEntity.add("remark", message);
				result = restTemplate.postForObject(url, requestEntity, String.class);
				
				return isTrue;
			}

		} catch (Exception e) {
			return isTrue;
		}

	}

}
