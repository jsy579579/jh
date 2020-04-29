package com.jh.paymentchannel.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import com.alibaba.druid.sql.visitor.functions.Trim;
import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.RepaymentSupportBank;
import com.jh.paymentchannel.pojo.WMYKBindCard;
import com.jh.paymentchannel.pojo.WMYKChooseCity;
import com.jh.paymentchannel.pojo.WMYKCity;
import com.jh.paymentchannel.pojo.WMYKProvince;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class WMYKpageRequest extends BaseChannel {

	private static final Logger LOG = LoggerFactory.getLogger(WMYKpageRequest.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	//private String Url = "http://pay.junet.tech/callpaypipe/com/callpaypipe/servlet/StableCallPayServlet";
	
	//绑卡请求地址
	private String bindCardUrl = "http://pay.junet.tech:28080/callpaypipe/com/callpaypipe/servlet/StableCallPayServlet";
	
	//支付请求地址
	private String fastPayUrl = "http://pay.junet.tech/callpaypipe/com/callpaypipe/servlet/StableCallPayServlet";
	
	//代付请求地址
	private String transferUrl = "http://pay.junet.tech:38080/callpaypipe/com/callpaypipe/servlet/StableCallPayServlet";
	
	//支付查询请求地址
	private String fastPayQueryUrl = "http://pay.junet.tech/callpaypipe/com/callpaypipe/servlet/StableCallPayServlet";
	
	//代付查询请求地址
	private String transferQueryUrl = "http://pay.junet.tech:38080/callpaypipe/com/callpaypipe/servlet/StableCallPayServlet";
	
	private String productUuid = "6b7b465f68474c20a5e3e2c9e5b86a67_dcea678b2a4845e79e9c5d9654e001b7_6c31f234bee44cd787588c79d7a505b0";

	@Autowired
	private RestTemplate restTemplate;
	
	//跟还款对接的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmyk/to/repayment")
	public @ResponseBody Object HLJCRegister(HttpServletRequest request,
			@RequestParam(value = "bankCard") String bankCard
			) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> maps = new HashMap<String, Object>();
		
		Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
		if (!"000000".equals(queryBankCardByCardNo.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", queryBankCardByCardNo.get("resp_message"));
			return maps;
		}

		Object object2 = queryBankCardByCardNo.get("result");
		JSONObject fromObject = JSONObject.fromObject(object2);

		String bankName = fromObject.getString("bankName");
		String cardtype = fromObject.getString("cardType");
		String securityCode = fromObject.getString("securityCode");
		String expiredTime = fromObject.getString("expiredTime");
		
		
		WMYKBindCard wmykBindCard = topupPayChannelBusiness.getWMYKBindCardByBankCard(bankCard);
		
		if(wmykBindCard == null) {
			LOG.info("用户需要绑卡======");
			
			maps.put(CommonConstants.RESP_CODE, "999996");
			maps.put(CommonConstants.RESULT,
					ipAddress + "/v1.0/paymentchannel/topup/towmyk/bindcard?bankName=" + URLEncoder.encode(bankName, "UTF-8")
							+ "&cardType=" + URLEncoder.encode(cardtype, "UTF-8") + "&bankCard=" + bankCard
							+ "&expiredTime=" + expiredTime + "&securityCode="
							+ securityCode + "&ipAddress=" + ipAddress);
			maps.put(CommonConstants.RESP_MESSAGE, "用户需要进行绑卡授权操作");
			return maps;
			
		}else if("".equals(wmykBindCard.getBindCardSeq()) || wmykBindCard.getBindCardSeq() == null){
			
			maps.put(CommonConstants.RESP_CODE, "999996");
			maps.put(CommonConstants.RESULT,
					ipAddress + "/v1.0/paymentchannel/topup/towmyk/bindcard?bankName=" + URLEncoder.encode(bankName, "UTF-8")
							+ "&cardType=" + URLEncoder.encode(cardtype, "UTF-8") + "&bankCard=" + bankCard
							+ "&expiredTime=" + expiredTime + "&securityCode="
							+ securityCode + "&ipAddress=" + ipAddress);
			maps.put(CommonConstants.RESP_MESSAGE, "用户需要进行绑卡授权操作");
			return maps;
			
		}else {
			
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "已完成绑卡");
			return maps;
			
		}
		
	}
	
	
	
	// 绑卡申请接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmyk/bindcard")
	public @ResponseBody Object wmykBindCard(HttpServletRequest request,
			@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode) throws Exception {
		LOG.info("开始进入绑卡接口========");
		Map<String, String> maps = new HashMap<String, String>();

		Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
		Object object = queryBankCardByCardNo.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);

		String userName = fromObject.getString("userName");
		String idCard = fromObject.getString("idcard");
		String phone = fromObject.getString("phone");
		//String securityCode = fromObject.getString("securityCode");
		//String expiredTime = fromObject.getString("expiredTime");

		expiredTime = this.expiredTimeToMMYY(expiredTime);

		String orderCode = UUID.randomUUID().toString().replace("-", "");

		String platUserId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

		Map<String, Object> map = new HashMap<String, Object>();

		map.put("productUuid", productUuid);
		map.put("merOrderId", orderCode);
		map.put("merPlatUserId", platUserId);
		map.put("cardType", "01");
		map.put("userRealName", userName);
		map.put("bindInfo", idCard + "-" + bankCard + "-" + phone + "-" + expiredTime + "-" + securityCode);
		map.put("busiType", "0");

		JSONObject fromObject3 = JSONObject.fromObject(map);

		LOG.info("绑卡申请的请求报文======" + fromObject3);

		RestTemplate restTemplate = new RestTemplate();

		String sendPost = restTemplate.postForObject(bindCardUrl + "?methodname=runBindCardApply&jsondata={fromObject3}", map,
				String.class, fromObject3.toString());

		LOG.info("绑卡申请请求返回的 sendPost======" + sendPost);

		JSONObject fromObject2 = JSONObject.fromObject(sendPost);

		LOG.info("fromObject2======" + fromObject2);

		String respCode = fromObject2.getString("F39");
		String respMsg = fromObject2.getString("F44");
		String hostLsNo = fromObject2.getString("HostLsNo");

		if ("0000".equals(respCode)) {

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put("channel_tag", "sdj");
			maps.put("hostLsNo", hostLsNo);
			maps.put("platUserId", platUserId);

			return maps;
		} else {

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_tag", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, "短信发送失败,请稍后重试!");

			return maps;
		}

	}

	// 绑卡确认接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmyk/bindcardconfirm")
	public @ResponseBody Object wmykBindCardConfirm(HttpServletRequest request,
			@RequestParam(value = "hostLsNo") String hostLsNo, @RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "platUserId") String platUserId, @RequestParam(value = "smsCode") String smsCode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode
			)throws Exception {

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
		Object object = queryBankCardByCardNo.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);

		String userName = fromObject.getString("userName");
		String idCard = fromObject.getString("idcard");
		String phone = fromObject.getString("phone");
		//String securityCode = fromObject.getString("securityCode");
		//String expiredTime = fromObject.getString("expiredTime");

		expiredTime = this.expiredTimeToMMYY(expiredTime);

		Map<String, Object> map = new HashMap<String, Object>();

		map.put("productUuid", productUuid);
		map.put("type", "0");
		map.put("merOrderId", UUID.randomUUID().toString().replace("-", ""));
		map.put("merPlatUserId", platUserId);
		map.put("cardType", "01");
		map.put("userRealName", userName);
		map.put("bindInfo", idCard + "-" + bankCard + "-" + phone + "-" + expiredTime + "-" + securityCode);
		map.put("busiType", "0");
		map.put("verifiyCode", smsCode);
		map.put("voidHostLsNo", hostLsNo);

		JSONObject fromObject3 = JSONObject.fromObject(map);

		LOG.info("消费卡绑卡确认的请求报文======" + fromObject3);

		RestTemplate restTemplate = new RestTemplate();

		String sendPost = restTemplate.postForObject(bindCardUrl + "?methodname=runBindCardOK&jsondata={fromObject3}", map,
				String.class, fromObject3.toString());

		LOG.info("消费卡绑卡请求返回的 sendPost======" + sendPost);

		JSONObject fromObject2 = JSONObject.fromObject(sendPost);

		LOG.info("fromObject2======" + fromObject2);

		String respCode = fromObject2.getString("F39");
		String respMsg = fromObject2.getString("F44");

		WMYKBindCard wmykBindCardByBankCard = topupPayChannelBusiness.getWMYKBindCardByBankCard(bankCard);
		
		if ("0000".equals(respCode)) {
			LOG.info("消费卡绑卡成功======");
			
			if("交易成功".equals(respMsg)) {
				
				String MerPlatCardSeq = fromObject2.getString("MerPlatCardSeq");

				if(wmykBindCardByBankCard == null) {
					
					WMYKBindCard wmykBindCard = new WMYKBindCard();
					wmykBindCard.setPhone(phone);
					wmykBindCard.setIdCard(idCard);
					wmykBindCard.setBankCard(bankCard);
					wmykBindCard.setPlatUserId(platUserId);
					wmykBindCard.setBindCardSeq(MerPlatCardSeq);

					topupPayChannelBusiness.createWMYKBindCard(wmykBindCard);
					
					maps.put(CommonConstants.RESP_CODE, "000000");
					maps.put("channel_type", "sdj");
					maps.put("redirect_url",ipAddress + "/v1.0/paymentchannel/topup/wmyk/bindcardsuccess");
					
					return maps;
				}else {
					
					wmykBindCardByBankCard.setPlatUserId(platUserId);
					wmykBindCardByBankCard.setBindCardSeq(MerPlatCardSeq);
					
					topupPayChannelBusiness.createWMYKBindCard(wmykBindCardByBankCard);
					
					maps.put(CommonConstants.RESP_CODE, "000000");
					maps.put("channel_type", "sdj");
					maps.put("redirect_url",ipAddress + "/v1.0/paymentchannel/topup/wmyk/bindcardsuccess");
					
					return maps;
				}
				
			}else {
				
				maps.put(CommonConstants.RESP_CODE, "999999");
				maps.put("channel_type", "sdj");
				maps.put("redirect_url",ipAddress + "/v1.0/paymentchannel/topup/wmyk/bindcardfaile");
				
				return maps;
			}
			
		} else {
			
			maps.put(CommonConstants.RESP_CODE, "999999");
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, respMsg);
			return maps;
		}

	}

	// 快捷支付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmyk/fastpay")
	public @ResponseBody Object wmykFastPay(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode,
			@RequestParam(value = "reqAddInfo") String reqAddInfo) throws Exception {
		LOG.info("开始进入快捷支付接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");
		String amount = resultObj.getString("amount");
		String realAmount = resultObj.getString("realAmount");

		String RealAmount = new BigDecimal(realAmount).multiply(new BigDecimal("100")).setScale(0).toString();

		WMYKBindCard wmykBindCard = topupPayChannelBusiness.getWMYKBindCardByBankCard(bankCard);

		Map<String, Object> map = new HashMap<String, Object>();

		map.put("productUuid", productUuid);
		map.put("merOrderId", ordercode);
		map.put("merPlatUserId", wmykBindCard.getPlatUserId());
		map.put("merPlatCardSeq", wmykBindCard.getBindCardSeq());

		map.put("reqAddInfo", reqAddInfo);
		map.put("type", "0");
		map.put("money", RealAmount);

		JSONObject fromObject3 = JSONObject.fromObject(map);

		LOG.info("快捷支付的请求报文======" + fromObject3);

		RestTemplate restTemplate1 = new RestTemplate();

		String sendPost = restTemplate1.postForObject(fastPayUrl + "?methodname=runLittlePay&jsondata={fromObject3}", map,
				String.class, fromObject3.toString());

		LOG.info("快捷支付的请求返回的 sendPost======" + sendPost);

		JSONObject fromObject2 = JSONObject.fromObject(sendPost);

		String respCode = fromObject2.getString("F39");
		String respMsg = fromObject2.getString("F44");

		if ("0000".equals(respCode)) {
			String HostLsNo = fromObject2.getString("HostLsNo");
			String State = fromObject2.getString("State");

			if ("0".equals(State)) {

				maps.put(CommonConstants.RESP_CODE, "000000");
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, "支付成功");
				
				String url = "http://creditcardmanager/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
				MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("orderCode", ordercode);
				requestEntity.add("version", "4");
				String result = null;
				JSONObject jsonObject;
				JSONObject resultObj1;
				try {
					result = restTemplate.postForObject(url, requestEntity, String.class);
					LOG.info("RESULT================" + result);
					jsonObject = JSONObject.fromObject(result);
					resultObj1 = jsonObject.getJSONObject("result");
				} catch (Exception e) {
					e.printStackTrace();LOG.error("",e);
				}

				url = "http://transactionclear/v1.0/transactionclear/payment/update";

				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("status", "1");
				requestEntity.add("order_code", ordercode);
				requestEntity.add("third_code", "");
				try {
					result = restTemplate.postForObject(url, requestEntity, String.class);
				} catch (Exception e) {
					e.printStackTrace();LOG.error("",e);
				}

				LOG.info("订单状态修改成功==================="+ordercode+"====================" + result);

				LOG.info("订单已支付!");
				
				
				return maps;
				
			} else if ("1".equals(State)) {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, "支付失败");
				return maps;
			} else {

				maps.put(CommonConstants.RESP_CODE, "999998");
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, "等待银行扣款中");
				return maps;
			}

		} else {

			maps.put(CommonConstants.RESP_CODE, "999998");
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, respMsg);
			return maps;
		}

	}

	// 快捷支付订单查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmyk/fastpayquery")
	public @ResponseBody Object wmykFastPayQuery(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode) throws Exception {

		LOG.info("开始进入快捷支付查询接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");
		String createTime = resultObj.getString("createTime");

		createTime = createTime.substring(0, 10).replace("-", "");

		WMYKBindCard wmykBindCard = topupPayChannelBusiness.getWMYKBindCardByBankCard(bankCard);

		Map<String, Object> map = new HashMap<String, Object>();

		map.put("productUuid", productUuid);
		map.put("merOrderId", ordercode);
		map.put("merPlatUserId", wmykBindCard.getPlatUserId());
		map.put("merOrderDate", createTime);

		JSONObject fromObject3 = JSONObject.fromObject(map);

		LOG.info("快捷支付查询的请求报文======" + fromObject3);

		RestTemplate restTemplate = new RestTemplate();

		String sendPost = restTemplate.postForObject(fastPayQueryUrl + "?methodname=runPaySearch&jsondata={fromObject3}", map,
				String.class, fromObject3.toString());

		LOG.info("快捷支付查询请求返回的 sendPost======" + sendPost);

		JSONObject fromObject2 = JSONObject.fromObject(sendPost);
		
		String respCode = fromObject2.getString("F39");
		String respMsg = fromObject2.getString("F44");

		if ("0000".equals(respCode)) {
			String State = fromObject2.getString("State");
		
			if ("0".equals(State)) {

				maps.put(CommonConstants.RESP_CODE, "000000");
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, "支付成功");
				return maps;
			} else if ("6".equals(State) || "8".equals(State)) {

				maps.put(CommonConstants.RESP_CODE, "999998");
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, "等待银行扣款中");
				return maps;
			}else if("7".equals(State)) {
				
				maps.put(CommonConstants.RESP_CODE, "999999");
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);
				return maps;
			} else {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, "支付失败");
				return maps;
			}
		
		}else {
			
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, respMsg);
			return maps;
		}
		
	}

	// 代付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmyk/transfer")
	public @ResponseBody Object wmykTransfer(HttpServletRequest request, @RequestParam(value = "ordercode") String orderCode)
			throws Exception {

		LOG.info("开始进入代付接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");
		String amount = resultObj.getString("amount");
		String realAmount = resultObj.getString("realAmount");

		String RealAmount = new BigDecimal(realAmount).multiply(new BigDecimal("100")).setScale(0).toString();

		WMYKBindCard wmykBindCard = topupPayChannelBusiness.getWMYKBindCardByBankCard(bankCard);

		Map<String, Object> map = new HashMap<String, Object>();

		map.put("productUuid", productUuid);
		map.put("merOrderId", orderCode);
		map.put("merPlatUserId", wmykBindCard.getPlatUserId());
		map.put("merPlatCardSeq", wmykBindCard.getBindCardSeq());
		map.put("money", RealAmount);

		JSONObject fromObject3 = JSONObject.fromObject(map);

		LOG.info("代付的请求报文======" + fromObject3);

		RestTemplate restTemplate1 = new RestTemplate();

		String sendPost = restTemplate1.postForObject(transferUrl + "?methodname=runRePayMent&jsondata={fromObject3}", map,
				String.class, fromObject3.toString());

		LOG.info("代付请求返回的 sendPost======" + sendPost);

		JSONObject fromObject2 = JSONObject.fromObject(sendPost);

		String respCode = fromObject2.getString("F39");
		String respMsg = fromObject2.getString("F44");

		if ("0000".equals(respCode)) {
			String HostLsNo = fromObject2.getString("HostLsNo");
			String State = fromObject2.getString("State");

			if ("0".equals(State)) {

				maps.put(CommonConstants.RESP_CODE, "000000");
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, "代付成功");
				
				
				String url = "http://transactionclear/v1.0/transactionclear/payment/update";

				MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("status", "1");
				requestEntity.add("order_code", orderCode);
				requestEntity.add("third_code", "");
				String result = null;
				try {
					result = restTemplate.postForObject(url, requestEntity, String.class);
				} catch (Exception e) {
					e.printStackTrace();LOG.error("",e);
				}

				LOG.info("订单状态修改成功==================="+orderCode+"====================" + result);

				LOG.info("订单已代付!");
				
				
				return maps;
			} else if ("1".equals(State)) {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, "代付失败");
				return maps;
			} else {

				maps.put(CommonConstants.RESP_CODE, "999998");
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, "等待银行出款中");
				return maps;
			}

		} else {

			maps.put(CommonConstants.RESP_CODE, "999998");
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, respMsg);
			return maps;
		}

	}

	
	
	
	
	// 代付订单查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmyk/transferquery")
	public @ResponseBody Object wmykTransferQuery(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode) throws Exception {

		LOG.info("开始进入代付查询接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");
		String createTime = resultObj.getString("createTime");

		createTime = createTime.substring(0, 10).replace("-", "");

		WMYKBindCard wmykBindCard = topupPayChannelBusiness.getWMYKBindCardByBankCard(bankCard);

		Map<String, Object> map = new HashMap<String, Object>();

		map.put("productUuid", productUuid);
		map.put("merOrderId", ordercode);
		map.put("merPlatUserId", wmykBindCard.getPlatUserId());
		map.put("merOrderDate", createTime);

		JSONObject fromObject3 = JSONObject.fromObject(map);

		LOG.info("代付查询的请求报文======" + fromObject3);

		RestTemplate restTemplate = new RestTemplate();

		String sendPost = restTemplate.postForObject(transferQueryUrl + "?methodname=runRePayMentSearch&jsondata={fromObject3}", map,
				String.class, fromObject3.toString());

		LOG.info("代付查询请求返回的 sendPost======" + sendPost);

		JSONObject fromObject2 = JSONObject.fromObject(sendPost);
		
		String respCode = fromObject2.getString("F39");
		String respMsg = fromObject2.getString("F44");

		if ("0000".equals(respCode)) {
			String State = fromObject2.getString("State");
		
			if ("0".equals(State)) {

				maps.put(CommonConstants.RESP_CODE, "000000");
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, "代付成功");
				return maps;
			} else if ("6".equals(State) || "8".equals(State)) {

				maps.put(CommonConstants.RESP_CODE, "999998");
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, "等待银行扣款中");
				return maps;
			}else if("7".equals(State)) {
				
				maps.put(CommonConstants.RESP_CODE, "999999");
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);
				return maps;
			} else {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, "代付失败");
				return maps;
			}
		
		}else {
			
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, respMsg);
			return maps;
		}
		
		
	}

	
	
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmykcitymerchant/querybycode")
	public @ResponseBody Object wmykCityMerchant(HttpServletRequest request,
			@RequestParam(value = "cityCode") String cityCode,
			@RequestParam(value = "judge", required = false, defaultValue = "2" )String judge
			) throws Exception {
		
		Map<String,Object> map = new HashMap<String, Object>();
		
		//judge为判断字段, 1 代表返回 商户名称, 2 代表返回 商户号
		if("1".equals(judge)) {
			
			List<String> cityMerchantName = topupPayChannelBusiness.getWMYKCityMerchantNameByCityCode(cityCode.trim());
			
			if(cityMerchantName != null && cityMerchantName.size()>0) {
				
				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", cityMerchantName);
			}else {
				
				return ResultWrap.init(CommonConstants.FALIED, "查询失败");
			}
			
		}else {
			
			List<String> merchantCode = topupPayChannelBusiness.getWMYKCityMerchantCodeByCityCode(cityCode.trim());
			
			if(merchantCode != null && merchantCode.size()>0) {
				
				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", merchantCode);
			}else {
				
				return ResultWrap.init(CommonConstants.FALIED, "查询失败");
			}
			
		}
		
	}
	
	
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmykchoosecity/create")
	public @ResponseBody Object wmykCreate(HttpServletRequest request,
			@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "cityCode") String cityCode
			) throws Exception {
		
		try {
			
			WMYKChooseCity wmykChooseCity = topupPayChannelBusiness.getWMYKChooseCityByBankCard(bankCard);
			
			if(wmykChooseCity == null) {
				WMYKChooseCity chooseCity = new WMYKChooseCity();
				
				chooseCity.setBankCard(bankCard.trim());
				chooseCity.setCityCode(cityCode.trim());
				chooseCity.setUpdateTime(DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss"));
				
				topupPayChannelBusiness.createWMYKChooseCity(chooseCity);
				
			}else {
				
				wmykChooseCity.setCityCode(cityCode.trim());
				wmykChooseCity.setUpdateTime(DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss"));
				
				topupPayChannelBusiness.createWMYKChooseCity(wmykChooseCity);
			}
		} catch (Exception e) {
			e.printStackTrace();
			
			return ResultWrap.init(CommonConstants.FALIED, "亲,选择地区出现了一点小问题,请稍后重试!");
		}
		
		
		return ResultWrap.init(CommonConstants.SUCCESS, "选择地区成功!");
	}
	
	
	//获取省份
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmykprovince/query")
	public @ResponseBody Object wmykProvince(
			) throws Exception {
		
		List<WMYKProvince> wmykProvince = topupPayChannelBusiness.getWMYKProvince();
		
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", wmykProvince);
	}
	
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wmykcity/query")
	public @ResponseBody Object wmykCity(@RequestParam(value = "provinceCode") String provinceCode
			)throws Exception {
		
		List<WMYKCity> wmykCity = topupPayChannelBusiness.getWMYKCityByProvinceCode(provinceCode);
		
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", wmykCity);
	}
	
	
	//跳转到绑卡页面
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/towmyk/bindcard")
	public String returnHLJCBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String bankName = request.getParameter("bankName");
		String cardType = request.getParameter("cardType");
		String bankCard = request.getParameter("bankCard");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("bankName", bankName);
		model.addAttribute("cardType", cardType);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("ipAddress", ipAddress);

		return "wmykbindcard";
	}
	
	
	//绑卡成功跳转页面
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/wmyk/bindcardsuccess")
	public String wmykBindCardSuccess(HttpServletRequest request, HttpServletResponse response) throws Exception {

		Map map = new HashMap();
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		return "cjbindcardsuccess";

	}
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/wmyk/bindcardfaile")
	public String wmykBindCardFaile(HttpServletRequest request, HttpServletResponse response) throws Exception {

		Map map = new HashMap();
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		return "cjbindcarderror";

	}
	
	
	//根据version获取还款通道支持银行列表
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/repaymentsupportbank/byversion")
	public @ResponseBody Object getSupportBank(@RequestParam(value = "version") String version
			) throws Exception {
		
		List<RepaymentSupportBank> repaymentSupportBankByVersion = topupPayChannelBusiness.getRepaymentSupportBankByVersion(version);
		
		if(repaymentSupportBankByVersion != null && repaymentSupportBankByVersion.size()>0) {
			
			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", repaymentSupportBankByVersion);
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, "暂无数据");
		}
		
	}
	
	
	
	public static String sendPost(String url, String param) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(conn.getOutputStream());
			// 发送请求参数
			out.print(param);
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "GBK"));

			String line;
			while ((line = in.readLine()) != null) {
				System.out.println(line);
				result += line;
			}
		} catch (Exception e) {
			System.out.println("发送 POST 请求出现异常！" + e);
			e.printStackTrace();
		}
		// 使用finally块来关闭输出流、输入流
		finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

}
