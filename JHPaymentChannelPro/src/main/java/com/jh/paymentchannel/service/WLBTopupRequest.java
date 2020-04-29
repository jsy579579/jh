package com.jh.paymentchannel.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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
import com.jh.paymentchannel.pojo.BranchNo;
import com.jh.paymentchannel.pojo.PaymentOrder;
import com.jh.paymentchannel.pojo.WLBBindCard;
import com.jh.paymentchannel.pojo.WLBRegister;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.wlb.HttpUtil;
import com.jh.paymentchannel.util.wlb.Md5Util;

import net.sf.json.JSONObject;

@Service
public class WLBTopupRequest implements TopupRequest {

	private static final Logger log = LoggerFactory.getLogger(WLBTopupRequest.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private BranchbankBussiness branchbankBussiness;

	@Autowired
	private Util util;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	private static final Charset UTF_8 = StandardCharsets.UTF_8;

	private String signKey = "922gp16Xob99Hdl36g923QaWBB371S0u";

	private String desKey = "u92149F402kl5As71y4YlF04";

	private String channelName = "上海复规网络信息有限公司";

	private String channelNo = "C2544328796";

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

		if (resultObj.isNullObject()) {

			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "没有结算卡");
			return map;
		}

		String cardNo = resultObj.getString("cardNo");
		String userName = resultObj.getString("userName");
		/** 身份证号 */
		String idcard = resultObj.getString("idcard");
		String phone = resultObj.getString("phone");
		// 开户行所在省
		String provinceOfBank = resultObj.getString("province");

		log.info("provinceOfBank====" + provinceOfBank);
		// 开户行所在市
		String cityOfBank = resultObj.getString("city");

		log.info("cityOfBank====" + cityOfBank);
		// 银行名称
		String bankName = resultObj.getString("bankName");
		// 支行名称
		String bankBranchName = resultObj.getString("bankBranchName");

		String cardType = resultObj.getString("cardType");

		String bankBranchNo;
		try {
			bankBranchNo = branchbankBussiness.getNumByName(bankBranchName);
		} catch (Exception e1) {
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询支行联行号有误");
			return map;
		}
		log.info("bankBranchNo" + bankBranchNo);

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
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询银行卡信息有误");
			return map;
		}

		String cardName = resultObj.getString("bankName");
		String expiredTime = resultObj.getString("expiredTime");

		String securityCode = resultObj.getString("securityCode");

		String cardtype = resultObj.getString("cardType");

		String bankname = Util.queryBankNameByBranchName(bankName);
		String cardname = Util.queryBankNameByBranchName(cardName);

		// 根据银行名称查询得到银行信息
		BranchNo findByBankName;
		try {
			findByBankName = branchbankBussiness.findByBankName(bankname);
			// 银行总行联行号
			String inBankUnitNo = findByBankName.getBankNo();
		} catch (Exception e) {
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询银行信息有误");
			return map;
		}

		WLBRegister wlbRegister;
		try {
			wlbRegister = topupPayChannelBusiness.getWLBRegisterByIdCard(idcard);
		} catch (Exception e) {
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询用户进件信息有误");
			return map;
		}

		WLBBindCard wlbBindCard = topupPayChannelBusiness.getWLBBindCardByBankCard(bankCard);

		if (provinceOfBank == null || "".equals(provinceOfBank) || "null".equals(provinceOfBank)) {
			provinceOfBank = "";
		}

		if (cityOfBank == null || "".equals(cityOfBank) || "null".equals(cityOfBank)) {
			cityOfBank = "";
		}

		if (bankBranchNo == null || "".equals(bankBranchNo) || "".equals(bankBranchNo)) {
			bankBranchNo = "";
		}

		if (bankBranchName == null || "".equals(bankBranchName) || "null".equals(bankBranchName)) {
			bankBranchName = "";
		}

		if (expiredTime == null || "".equals(expiredTime) || "null".equals(expiredTime)) {
			expiredTime = "";
		}

		if (securityCode == null || "".equals(securityCode) || "null".equals(securityCode)) {
			securityCode = "";
		}

		if (wlbRegister == null) {
			log.info("用户需要进件======");
			map.put("resp_code", "success");
			map.put("channel_type", "jf");
			map.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/towlbbankinfo?bankName="
					+ URLEncoder.encode(bankname, "UTF-8") + "&bankBranchId=" + bankBranchNo + "&bankNo=" + cardNo
					+ "&bankCard=" + bankCard + "&cardName=" + URLEncoder.encode(cardname, "UTF-8") + "&amount="
					+ amount + "&ordercode=" + ordercode + "&provinceOfBank="
					+ URLEncoder.encode(provinceOfBank, "UTF-8") + "&cityOfBank="
					+ URLEncoder.encode(cityOfBank, "UTF-8") + "&bankBranchName="
					+ URLEncoder.encode(bankBranchName, "UTF-8") + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
					+ "&cardtype=" + URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime
					+ "&securityCode=" + securityCode + "&ipAddress=" + ipAddress + "&isRegister=0");
			return map;

		} else {

			if (!rate.equals(wlbRegister.getRate())) {
				log.info("用户费率需要变更=====");
				boolean updateRate = updateRate(ordercode);
				if (updateRate) {
					if (!cardNo.equals(wlbRegister.getBankCard())) {
						log.info("结算卡需要变更=====");
						map.put("resp_code", "success");
						map.put("channel_type", "jf");
						map.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/towlbbankinfo?bankName="
								+ URLEncoder.encode(bankname, "UTF-8") + "&bankBranchId=" + bankBranchNo + "&bankNo="
								+ cardNo + "&bankCard=" + bankCard + "&cardName=" + URLEncoder.encode(cardname, "UTF-8")
								+ "&amount=" + amount + "&ordercode=" + ordercode + "&provinceOfBank="
								+ URLEncoder.encode(provinceOfBank, "UTF-8") + "&cityOfBank="
								+ URLEncoder.encode(cityOfBank, "UTF-8") + "&bankBranchName="
								+ URLEncoder.encode(bankBranchName, "UTF-8") + "&cardType="
								+ URLEncoder.encode(cardType, "UTF-8") + "&cardtype="
								+ URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime
								+ "&securityCode=" + securityCode + "&ipAddress=" + ipAddress + "&isRegister=2");
						return map;
					} else if (wlbBindCard == null) {
						log.info("用户需要绑卡=====");
						map.put("resp_code", "success");
						map.put("channel_type", "jf");
						map.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/towlbbankinfo?bankName="
								+ URLEncoder.encode(bankname, "UTF-8") + "&bankBranchId=" + bankBranchNo + "&bankNo="
								+ cardNo + "&bankCard=" + bankCard + "&cardName=" + URLEncoder.encode(cardname, "UTF-8")
								+ "&amount=" + amount + "&ordercode=" + ordercode + "&provinceOfBank="
								+ URLEncoder.encode(provinceOfBank, "UTF-8") + "&cityOfBank="
								+ URLEncoder.encode(cityOfBank, "UTF-8") + "&bankBranchName="
								+ URLEncoder.encode(bankBranchName, "UTF-8") + "&cardType="
								+ URLEncoder.encode(cardType, "UTF-8") + "&cardtype="
								+ URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime
								+ "&securityCode=" + securityCode + "&ipAddress=" + ipAddress + "&isRegister=1");
						return map;
					} else {
						log.info("发起交易===");
						map.put("resp_code", "success");
						map.put("channel_type", "jf");
						map.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/towlbbankinfo?bankName="
								+ URLEncoder.encode(bankname, "UTF-8") + "&bankBranchId=" + bankBranchNo + "&bankNo="
								+ cardNo + "&bankCard=" + bankCard + "&cardName=" + URLEncoder.encode(cardname, "UTF-8")
								+ "&amount=" + amount + "&ordercode=" + ordercode + "&provinceOfBank="
								+ URLEncoder.encode(provinceOfBank, "UTF-8") + "&cityOfBank="
								+ URLEncoder.encode(cityOfBank, "UTF-8") + "&bankBranchName="
								+ URLEncoder.encode(bankBranchName, "UTF-8") + "&cardType="
								+ URLEncoder.encode(cardType, "UTF-8") + "&cardtype="
								+ URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime
								+ "&securityCode=" + securityCode + "&ipAddress=" + ipAddress + "&isRegister=3");
						return map;
					}

				} else {
					map.put("resp_code", "failed");
					map.put("channel_type", "jf");
					map.put("resp_message", "亲，修改费率出错啦");
					return map;
				}

			} else {
				 if (!cardNo.equals(wlbRegister.getBankCard())) {
					log.info("结算卡需要变更=====");
					map.put("resp_code", "success");
					map.put("channel_type", "jf");
					map.put("redirect_url",
							ipAddress + "/v1.0/paymentchannel/topup/towlbbankinfo?bankName="
									+ URLEncoder.encode(bankname, "UTF-8") + "&bankBranchId=" + bankBranchNo
									+ "&bankNo=" + cardNo + "&bankCard=" + bankCard + "&cardName="
									+ URLEncoder.encode(cardname, "UTF-8") + "&amount=" + amount + "&ordercode="
									+ ordercode + "&provinceOfBank=" + URLEncoder.encode(provinceOfBank, "UTF-8")
									+ "&cityOfBank=" + URLEncoder.encode(cityOfBank, "UTF-8") + "&bankBranchName="
									+ URLEncoder.encode(bankBranchName, "UTF-8") + "&cardType="
									+ URLEncoder.encode(cardType, "UTF-8") + "&cardtype="
									+ URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime
									+ "&securityCode=" + securityCode + "&ipAddress=" + ipAddress + "&isRegister=2");
					return map;
				}else if (wlbBindCard == null) {
					log.info("用户需要绑卡=====");
					map.put("resp_code", "success");
					map.put("channel_type", "jf");
					map.put("redirect_url",
							ipAddress + "/v1.0/paymentchannel/topup/towlbbankinfo?bankName="
									+ URLEncoder.encode(bankname, "UTF-8") + "&bankBranchId=" + bankBranchNo
									+ "&bankNo=" + cardNo + "&bankCard=" + bankCard + "&cardName="
									+ URLEncoder.encode(cardname, "UTF-8") + "&amount=" + amount + "&ordercode="
									+ ordercode + "&provinceOfBank=" + URLEncoder.encode(provinceOfBank, "UTF-8")
									+ "&cityOfBank=" + URLEncoder.encode(cityOfBank, "UTF-8") + "&bankBranchName="
									+ URLEncoder.encode(bankBranchName, "UTF-8") + "&cardType="
									+ URLEncoder.encode(cardType, "UTF-8") + "&cardtype="
									+ URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime
									+ "&securityCode=" + securityCode + "&ipAddress=" + ipAddress + "&isRegister=1");
					return map;
				}else {
					log.info("发起交易===");
					map.put("resp_code", "success");
					map.put("channel_type", "jf");
					map.put("redirect_url",
							ipAddress + "/v1.0/paymentchannel/topup/towlbbankinfo?bankName="
									+ URLEncoder.encode(bankname, "UTF-8") + "&bankBranchId=" + bankBranchNo
									+ "&bankNo=" + cardNo + "&bankCard=" + bankCard + "&cardName="
									+ URLEncoder.encode(cardname, "UTF-8") + "&amount=" + amount + "&ordercode="
									+ ordercode + "&provinceOfBank=" + URLEncoder.encode(provinceOfBank, "UTF-8")
									+ "&cityOfBank=" + URLEncoder.encode(cityOfBank, "UTF-8") + "&bankBranchName="
									+ URLEncoder.encode(bankBranchName, "UTF-8") + "&cardType="
									+ URLEncoder.encode(cardType, "UTF-8") + "&cardtype="
									+ URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime
									+ "&securityCode=" + securityCode + "&ipAddress=" + ipAddress + "&isRegister=3");
					return map;
				}

			}

		}

	}

	// 修改费率
	public boolean updateRate(String ordercode) throws Exception {

		Map map = new HashMap();
		Map<String, String> maps = new HashMap<String, String>();
		boolean istrue = false;
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("RESULT================" + result);
		JSONObject jsonObject = JSONObject.fromObject(result);
		JSONObject resultObj = jsonObject.getJSONObject("result");

		String userid = resultObj.getString("userid");
		// 费率
		String rate = resultObj.getString("rate");
		// 额外费率
		String extraFee = resultObj.getString("extraFee");

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
		}

		String idcard = resultObj.getString("idcard");

		WLBRegister wlbRegister = topupPayChannelBusiness.getWLBRegisterByIdCard(idcard);

		com.alibaba.fastjson.JSONObject reqJson = new com.alibaba.fastjson.JSONObject(); // 请注意fastjson的版本，需要用自带排序功能的
		reqJson.put("channelName", channelName);
		reqJson.put("channelNo", channelNo);
		reqJson.put("merchantNo", wlbRegister.getMerchantNO());// 每次请求订单号唯一
		reqJson.put("productType", "QUICKPAY");// 需要添加类型的费率 QUICKPAY
		reqJson.put("t0Fee", rate);
		reqJson.put("t1Fee", rate);

		String sign = Md5Util.MD5(reqJson.toJSONString() + signKey);
		reqJson.put("sign", sign);
		String reqData = reqJson.toJSONString();
		log.info("修改费率接口提交的数据##" + reqData);
		String url1 = "https://pay.feifanzhichuang.com/middlepayportal/merchant/modifyProductFee";//
		// 修改费率
		try {
			String resultStr = HttpUtil.sendPost(url1, reqData, HttpUtil.CONTENT_TYPE_JSON);
			log.info("修改费率返回的报文" + resultStr);

			com.alibaba.fastjson.JSONObject respJson = JSON.parseObject(resultStr);

			String respCode = respJson.getString("respCode");
			String respMsg = respJson.getString("respMsg");

			if ("0000".equals(respCode)) {
				wlbRegister.setRate(rate);
				wlbRegister.setExtraFee(extraFee);

				topupPayChannelBusiness.createWLBRegister(wlbRegister);

				istrue = true;
			}else {
				
				restTemplate = new RestTemplate();
				uri = util.getServiceUrl("transactionclear", "error url request!");
				url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("ordercode", ordercode);
				requestEntity.add("remark", respCode+" : "+respMsg);
				result = restTemplate.postForObject(url, requestEntity, String.class);
				
			}

		} catch (Exception e) {
			log.error("修改用户费率出错啦======");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "修改用户费率出错");
			result = restTemplate.postForObject(url, requestEntity, String.class);
		}

		return istrue;

	}

}
