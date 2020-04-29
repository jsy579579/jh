package com.jh.paymentchannel.service;

import java.math.BigDecimal;
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
import com.jh.paymentchannel.pojo.BankNumCode;
import com.jh.paymentchannel.pojo.PaymentOrder;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.zb.HttpSendUtil;
import com.jh.paymentchannel.util.zb.MD5Util;

import net.sf.json.JSONObject;

@Service
public class ZBTopupRequest implements TopupRequest {

	private static final Logger log = LoggerFactory.getLogger(ZBTopupRequest.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private BranchbankBussiness branchbankBussiness;

	@Autowired
	private Util util;

	@Value("${payment.ipAddress}")
	private String ipAddress;
	@Value("${zb.appid}")
	private String appId ;
	@Value("${zb.appkey}")
	private String appKey ;
	@Value("${zb.pay_url}")
	private String payUrl ;
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
		String cardphone = resultObj.getString("phone");

		String cardname = Util.queryBankNameByBranchName(cardName);

		String expiredTime = resultObj.getString("expiredTime");
		String securityCode = resultObj.getString("securityCode");
		String cardtype = resultObj.getString("cardType");
		String bankNum;
		String bankBranchcode;
		try {
			BankNumCode bankNumCode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
			
			bankNum = "0"+bankNumCode.getBankNum().trim()+"0000";
		} catch (Exception e) {
			e.printStackTrace();
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "该通道暂不支持该结算银行,请及时更换结算银行卡!");
			return map;
		}

		Map<String,String> parameterMap=new HashMap<String, String>();
		try{
			parameterMap.put("service", "service.api.cash.apply");//接口类型
			parameterMap.put("appid",appId);//应用编号
			parameterMap.put("mchntOrderNo",ordercode);//商户订单号
			parameterMap.put("amount",new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_HALF_UP).toString());//交易金额 单位：分
			parameterMap.put("powerId", "9000000001");//功能编号
			parameterMap.put("subject", paymentOrder.getDesc());//商品标题
			parameterMap.put("cardId",paymentOrder.getBankcard());//支付卡号
			parameterMap.put("mobileNo", cardphone);//银行预留手机号                                                          
			parameterMap.put("acctName", userName);//结算卡持卡人姓名 
			parameterMap.put("acctIdcard", idcard);//结算卡持卡人身份证号
			parameterMap.put("bankNum",  bankNum);//结算卡银行联行号
			parameterMap.put("acctCardno", cardNo);//结算卡卡号
			parameterMap.put("tradeRate",rate);//商户费率
			parameterMap.put("drawFee", extraFee);//单笔代付手续费
			parameterMap.put("notifyUrl", URLEncoder.encode(ipAddress+"/v1.0/paymentchannel/topup/zb/notify_call","utf-8"));
			parameterMap.put("returnUrl", URLEncoder.encode(ipAddress+"/v1.0/paymentchannel/topup/zb/ret_url","utf-8"));
			parameterMap.put("version", "01");
			//parameterMap.put("merPriv", "01");//商户透传字段
			parameterMap.put("cvn2",securityCode);//支付卡背面 最后三位安全码
			parameterMap.put("expDt", expiredTime);//支付卡有效期
			parameterMap.put("busType","WKPAY");//交易类型 ：WK002:无卡有积分 WKPAY:快捷有积分
			parameterMap.put("signature",MD5Util.doEncrypt(parameterMap,appKey));
			
			 String  mapJsonObject= JSON.toJSONString(parameterMap);
			 log.info("请求报文=="+mapJsonObject);
			 String returnJosn=HttpSendUtil.doHttpAndHttps(payUrl, mapJsonObject);
			 log.info("返回报文=="+returnJosn);
			 jsonObject = JSONObject.fromObject(returnJosn);
			 //状态码
			 String code=jsonObject.getString("code");
			 Map maps = new HashMap();
			 if(code.endsWith("10000")) {
				maps.put("resp_code", "success");
				maps.put("channel_type", "zb");
				maps.put("redirect_url", jsonObject.getString("payInfo"));
			 }else {
				maps.put("resp_code", "failed");
				maps.put("channel_type", "zb");
				maps.put("resp_message", "请求交易失败");
			 }
		     return maps;
			 
		}catch(Exception e){
			 Map maps = new HashMap();
			 maps.put("resp_code", "failed");
			 maps.put("channel_type", "zb");
			 maps.put("resp_message", "请求交易失败");
			 return maps;
		}		
		
		

	}



}
