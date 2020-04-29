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

import com.jh.paymentchannel.business.BranchbankBussiness;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.LDRegister;
import com.jh.paymentchannel.pojo.PaymentOrder;
import com.jh.paymentchannel.util.Util;

import net.sf.json.JSONObject;

@Service
public class LDTopupRequest implements TopupRequest {

	private static final Logger log = LoggerFactory.getLogger(LDTopupRequest.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private BranchbankBussiness branchbankBussiness;

	@Autowired
	private Util util;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	private static final Charset UTF_8 = StandardCharsets.UTF_8;

	@Override
	public Map<String, String> topupRequest(Map<String,Object> params) throws Exception {
		PaymentOrder paymentOrder = (PaymentOrder) params.get("paymentOrder");
		String ordercode = paymentOrder.getOrdercode();
		String amount = paymentOrder.getAmount().toString();
		
		Map<String, String> map = new HashMap<String, String>();
		/** 首先拿到用户的userid, 如果用户已经存在，那么直接拿用户的sdjuserid, 如果用户的默认结算卡已经和当前不一样了。 需要重新注册 **/
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
		// 银行名称
		String bankName = resultObj.getString("bankName");

		String cardType = resultObj.getString("cardType");

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
		
		
		LDRegister ldRegister = topupPayChannelBusiness.getLDRegisterByIdCard(idcard);
		
		log.info("为了测试ldRegister有没有值===="+ldRegister);
		
		if (expiredTime == null || "".equals(expiredTime) || "null".equals(expiredTime)) {
			expiredTime = "";
		}

		if (securityCode == null || "".equals(securityCode) || "null".equals(securityCode)) {
			securityCode = "";
		}

		if (ldRegister == null) {
			log.info("用户需要进件========");
			// ldRegister为空表示用户需要进件
			Map maps = new HashMap();
			maps.put("resp_code", "success");
			maps.put("channel_type", "jf");
			maps.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/toldbankinfo?bankName="
							+ URLEncoder.encode(bankname, "UTF-8") + "&bankNo="
							+ cardNo + "&bankCard=" + bankCard + "&cardName=" + URLEncoder.encode(cardname, "UTF-8")
							+ "&amount=" + amount + "&ordercode=" + ordercode + "&cardType="
							+ URLEncoder.encode(cardType, "UTF-8") + "&cardtype=" + URLEncoder.encode(cardtype, "UTF-8")
							+ "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&ipAddress="+ipAddress + "&isRegister=0");
			return maps;
		}else if(!rate.equals(ldRegister.getRate()) || !extraFee.equals(ldRegister.getExtraFee()) || !cardNo.equals(ldRegister.getBankCard())){
			log.info("用户需要修改进件========");
			Map maps = new HashMap();
			maps.put("resp_code", "success");
			maps.put("channel_type", "jf");
			maps.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/toldbankinfo?bankName="
							+ URLEncoder.encode(bankname, "UTF-8") + "&bankNo="
							+ cardNo + "&bankCard=" + bankCard + "&cardName=" + URLEncoder.encode(cardname, "UTF-8")
							+ "&amount=" + amount + "&ordercode=" + ordercode + "&cardType="
							+ URLEncoder.encode(cardType, "UTF-8") + "&cardtype=" + URLEncoder.encode(cardtype, "UTF-8")
							+ "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&ipAddress="+ipAddress + "&isRegister=2");
			return maps;
			
		}else {
			log.info("发起支付======");
			Map maps = new HashMap();
			maps.put("resp_code", "success");
			maps.put("channel_type", "jf");
			maps.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/toldbankinfo?bankName="
							+ URLEncoder.encode(bankname, "UTF-8") + "&bankNo="
							+ cardNo + "&bankCard=" + bankCard + "&cardName=" + URLEncoder.encode(cardname, "UTF-8")
							+ "&amount=" + amount + "&ordercode=" + ordercode + "&cardType="
							+ URLEncoder.encode(cardType, "UTF-8") + "&cardtype=" + URLEncoder.encode(cardtype, "UTF-8")
							+ "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&ipAddress="+ipAddress + "&isRegister=1");
			return maps;
			
		}
		
		
	}
}
