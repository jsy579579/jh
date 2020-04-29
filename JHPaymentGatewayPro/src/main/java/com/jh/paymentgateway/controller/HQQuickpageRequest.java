package com.jh.paymentgateway.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.BankNumCode;
import com.jh.paymentgateway.pojo.HQBindCard;
import com.jh.paymentgateway.pojo.HQQuickRegister;
import com.jh.paymentgateway.pojo.HQRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.Util;
import com.jh.paymentgateway.util.hq.CommonBean;
import com.jh.paymentgateway.util.hq.CommonUtil;
import com.jh.paymentgateway.util.hq.JsonUtils;
import com.jh.paymentgateway.util.hq.SignUtil;
import com.jh.paymentgateway.util.hq.SubMer;
import com.jh.paymentgateway.util.hq.TransUtil;
import com.jh.paymentgateway.util.hqb.RepayPlanList;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


@Controller
@EnableAutoConfiguration
public class HQQuickpageRequest extends BaseChannel{
	private static final Logger LOG = LoggerFactory.getLogger(HQQuickpageRequest.class);
	static ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setSerializationInclusion(Inclusion.NON_NULL);
	}

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;

	public final static String DATE_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
	private static String merchno = "shbyt2019071016";
	private static String dskey = "dc3becc5e60764edaa6bb78e4ac05e0e";
	
	private static String registerUrl = "http://pay.huanqiuhuiju.com/authsys/weishua/api/merchant/register.do";

	private static String updateUrl = "http://pay.huanqiuhuiju.com/authsys/weishua/api/update/fee.do";
	
	private static String payUrl = "http://pay.huanqiuhuiju.com/authsys/weishua/api/quick/pay.do";
	
	private static String queryUrl = "http://pay.huanqiuhuiju.com/authsys/api/auth/execute.do";

	//进件接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqquick/register")
	public @ResponseBody Object HQQuickRegister(@RequestParam(value = "orderCode") String orderCode
			) throws Exception {
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		
		String rate = prp.getRate();
		String bigRate = new BigDecimal(rate).multiply(new BigDecimal("100")).setScale(2).stripTrailingZeros().toPlainString();
		String extraFee = prp.getExtraFee();
		String bigExtraFee = new BigDecimal(extraFee).multiply(new BigDecimal("100")).setScale(0).toString();
		
		String debitBankName = prp.getDebitBankName();
		String phone = prp.getDebitPhone();
		String debitCardNo = prp.getDebitCardNo();
		String userName = prp.getUserName();
		String idCard = prp.getIdCard();
		String bankCode;
		try {
			BankNumCode bankNumCode = topupPayChannelBusiness
					.getBankNumCodeByBankName(Util.queryBankNameByBranchName(debitBankName));

			bankCode = bankNumCode.getBankCode();
		} catch (Exception e) {
			e.printStackTrace();

			this.addOrderCauseOfFailure(orderCode, "查询银行编码出错!", prp.getIpAddress());
			
			return ResultWrap.init(CommonConstants.FALIED, "暂不支持该银行卡,请及时更换默认提现卡!");
		}
		
		if (debitBankName.contains("广发") || debitBankName.contains("广东发展")) {
			bankCode = "GDB";
		}
		if (debitBankName.contains("中信")) {
			bankCode = "CNCB";
		}
		
		CommonBean trans = new CommonBean();
		// 报文头
		trans.setVersion("0100");
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setMerchno(merchno);
		trans.setDsorderid("xl" + System.currentTimeMillis() + "");
		trans.setTranscode("025");
		// 业务参数
		trans.setFutureRateValue(bigRate);
		trans.setFixAmount(bigExtraFee);
		trans.setAccountName(userName);
		trans.setIdcard(idCard);
		trans.setSettleBankCard(debitCardNo);
		trans.setSettleBankName(debitBankName);
		trans.setSettleBankCode(bankCode);
		trans.setMobile(phone);
		
		LOG.info("请求进件的报文======" + trans);
		
		String result = sendRegister(trans);
		LOG.info("请求进件返回的result======" + result);
		JSONObject fromObject = JSONObject.fromObject(result);
		
		String returncode = fromObject.getString("returncode");
		String errtext = fromObject.getString("errtext");
		
		if("0000".equals(returncode)||"03008".equals(returncode)) {
			LOG.info("用户：" + userName + "进件成功======");
			
			String subMerchantNo = fromObject.getString("subMerchantNo");
			
			HQQuickRegister hqQuickRegister = new HQQuickRegister();
			hqQuickRegister.setPhone(phone);
			hqQuickRegister.setBankCard(debitCardNo);
			hqQuickRegister.setIdCard(idCard);
			hqQuickRegister.setRate(rate);
			hqQuickRegister.setExtraFee(extraFee);
			hqQuickRegister.setMerchantNo(subMerchantNo);
			
			topupPayChannelBusiness.createHQQuickRegister(hqQuickRegister);
			
			return ResultWrap.init(CommonConstants.SUCCESS, "进件成功!");
		}else {
			
			this.addOrderCauseOfFailure(orderCode, "进件失败,失败原因为: " + errtext, prp.getIpAddress());
			
			return ResultWrap.init(CommonConstants.FALIED, errtext);
		}
		
	}


	//修改进件的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqquick/updateregister")
	public @ResponseBody Object updateRegister(@RequestParam(value = "orderCode") String orderCode
			) throws Exception {
		Map<String, Object> maps = new HashMap<String, Object>();
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		
		String rate = prp.getRate();
		String bigRate = new BigDecimal(rate).multiply(new BigDecimal("100")).setScale(2).stripTrailingZeros().toPlainString();
		String extraFee = prp.getExtraFee();
		String bigExtraFee = new BigDecimal(extraFee).multiply(new BigDecimal("100")).setScale(0).toString();
		
		String debitBankName = prp.getDebitBankName();
		String phone = prp.getDebitPhone();
		String debitCardNo = prp.getDebitCardNo();
		String userName = prp.getUserName();
		String idCard = prp.getIdCard();
		String bankCode;
		try {
			BankNumCode bankNumCode = topupPayChannelBusiness
					.getBankNumCodeByBankName(Util.queryBankNameByBranchName(debitBankName));

			bankCode = bankNumCode.getBankCode();
		} catch (Exception e) {
			e.printStackTrace();

			this.addOrderCauseOfFailure(orderCode, "查询银行编码出错!", prp.getIpAddress());
			
			return ResultWrap.init(CommonConstants.FALIED, "暂不支持该银行卡,请及时更换默认提现卡!");
		}
		
		if (debitBankName.contains("广发") || debitBankName.contains("广东发展")) {

			bankCode = "GDB";
		}
		if (debitBankName.contains("中信")) {
			bankCode = "CNCB";
		}
		
		HQQuickRegister hqQuickRegister = topupPayChannelBusiness.getHQQuickRegisterByIdCard(idCard);
		
		CommonBean trans = new CommonBean();
		// 报文头
		trans.setVersion("0100");
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setMerchno(merchno);
		trans.setDsorderid("xl" + System.currentTimeMillis() + "");
		trans.setTranscode("027");
		// 业务参数
		trans.setFutureRateValue(bigRate);
		trans.setFixAmount(bigExtraFee);
		trans.setAccountName(userName);
		trans.setIdcard(idCard);
		trans.setSettleBankCard(debitCardNo);
		trans.setSettleBankName(debitBankName);
		trans.setSettleBankCode(bankCode);
		trans.setMobile(phone);
		trans.setSubMerchantNo(hqQuickRegister.getMerchantNo());
		
		LOG.info("请求修改进件信息的报文======" + trans);
		
		String result = sendUpdate(trans);
		LOG.info("请求修改进件信息返回的result======" + result);
		JSONObject fromObject = JSONObject.fromObject(result);
		String returncode = fromObject.getString("returncode");
		String errtext = fromObject.getString("errtext");
		
		if("0000".equals(returncode)) {
			
			hqQuickRegister.setBankCard(debitCardNo);
			hqQuickRegister.setRate(rate);
			hqQuickRegister.setExtraFee(extraFee);
			hqQuickRegister.setUpdateTime(DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss"));
			
			topupPayChannelBusiness.createHQQuickRegister(hqQuickRegister);
			
			return ResultWrap.init(CommonConstants.SUCCESS, "修改进件成功!");
		}else {
			
			this.addOrderCauseOfFailure(orderCode, "修改进件失败,失败原因为: " + errtext, prp.getIpAddress());
			
			return ResultWrap.init(CommonConstants.FALIED, errtext);
		}
		
	}

	//快捷支付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqquick/fastpay")
	public @ResponseBody Object hqFastPay(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "expiredTime") String expiredTime,
			@RequestParam(value = "securityCode") String securityCode
			) {
		Map<String, Object> maps = new HashMap<String, Object>();
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		
		CommonBean trans = new CommonBean();
		String amount = prp.getAmount();
		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();
		String userName = prp.getUserName();
		String creditCardPhone = prp.getCreditCardPhone();

		HQQuickRegister hqQuickRegister = topupPayChannelBusiness.getHQQuickRegisterByIdCard(idCard);
		
		String expiredTimeToMMYY = this.expiredTimeToMMYY(expiredTime);
		
		String bigAmount = new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0).toString();
		
		// 报文头
		trans.setVersion("0100");
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setMerchno(merchno);
		trans.setDsorderid(orderCode);
		trans.setTranscode("026");
		// 业务参数
		trans.setAmount(bigAmount);
		trans.setBankcard(bankCard);
		trans.setAccountName(userName);
		trans.setMobile(creditCardPhone);
		trans.setCvn2(securityCode);
		trans.setExpireDate(expiredTimeToMMYY);
		trans.setSubMerchantNo(hqQuickRegister.getMerchantNo());
		trans.setReturnUrl(ip+"/v1.0/paymentgateway/topup/quickpaysuccess");
		trans.setNotifyUrl(ip + "/v1.0/paymentgateway/topup/hqquick/fastpay/notifyurl");

		LOG.info("请求快捷支付的报文======" + trans);
		
		String result = sendPay(trans);
		LOG.info("请求快捷支付返回的result======" + result);
		JSONObject fromObject = JSONObject.fromObject(result);
		String returncode = fromObject.getString("returncode");
		String errtext = fromObject.getString("errtext");
		
		if("0000".equals(returncode)) {
			String pay_info = fromObject.getString("pay_info");
			String pay_code = fromObject.getString("pay_code");
			String redirectUrl = null;
			if("{}".equals(pay_code)){
				redirectUrl = pay_info;
				return ResultWrap.init(CommonConstants.SUCCESS, "请求支付成功!", redirectUrl);
			}
			JSONObject fromObject2 = JSONObject.fromObject(pay_code);
			
			Map<String,Object> map = new HashMap<String, Object>();
			
			map = fromObject2;
			
			Set<String> keySet = map.keySet();
			Iterator<String> it = keySet.iterator();
			StringBuffer sb = new StringBuffer();
			while(it.hasNext()) {
				String next = it.next();
				sb.append(next + "=" + map.get(next) + "&");
			}
			String substring = sb.substring(0, sb.length()-1);
			
			redirectUrl = pay_info + "?" + substring;
			
			LOG.info("redirectUrl======" + redirectUrl);
			
			return ResultWrap.init(CommonConstants.SUCCESS, "请求支付成功!", redirectUrl);
		}else {
			
			this.addOrderCauseOfFailure(orderCode, "请求支付失败,失败原因为: " + errtext, prp.getIpAddress());
			
			return ResultWrap.init(CommonConstants.FALIED, errtext);
		}
		
	}

	
	//交易查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqquick/orderquery")
	public @ResponseBody Object QuickOpen(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "transType") String transType
			) throws Exception {
		Map<String, Object> maps = new HashMap<String, Object>();
		CommonBean trans = new CommonBean();
		// 报文头
		trans.setVersion("0100");
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setMerchno(merchno);
		trans.setDsorderid(orderCode);
		trans.setTranscode("902");
		trans.setTranstype(transType);
		
		String resp = TransUtil.object2String(trans);
		Map<String, String> resMap = mapper.readValue(resp, Map.class);
		String sign = SignUtil.getSign(resMap, dskey);
		trans.setSign(sign);

		String result = sendQuery(trans);
		
		LOG.info("请求订单查询返回的result======" + result);
		
		JSONObject jsonobj = JSONObject.fromObject(result);
		
		String returnCode = jsonobj.getString("returncode");
		
		String message = null;
		if(jsonobj.containsKey("message")) {
			
			message = jsonobj.getString("message");
			
		}
		
		if("0000".equals(returnCode)) {
			String status = jsonobj.getString("status");
			if("00".equals(status)) {
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, message);
				
				return maps;
			}else if("01".equals(status)){
				
				maps.put(CommonConstants.RESP_CODE, "999998");
				maps.put(CommonConstants.RESP_MESSAGE, message);
				
				return maps;
			}else if("99".equals(status)){
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "订单号不存在");
				
				return maps;
			}else {
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message);
				
				return maps;
			}
			
		}else {
			
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, message);
			
			return maps;
		}
		
	}


	
	//交易异步通知
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqquick/fastpay/notifyurl")
	public void notifyUrl(HttpServletRequest request, HttpServletResponse response) throws Exception {

		LOG.info("快捷支付异步回调进来了======");
		
		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}
		
		String dsorderid = request.getParameter("dsorderid");
		String orderid = request.getParameter("orderid");
		String status = request.getParameter("status");
		String transtype = request.getParameter("transtype");
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(dsorderid);
		
		if ("00".equalsIgnoreCase(status)) {
			LOG.info("交易成功======");
			
			RestTemplate restTemplate = new RestTemplate();

			
			String url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
			//String url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", dsorderid);
			requestEntity.add("third_code", orderid + " + " + transtype);
			String result = null;
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				LOG.error("",e);
			}

			LOG.info("订单状态修改成功==================="+dsorderid+"====================" + result);

			LOG.info("订单已支付!");
			
		}else {
			LOG.info(dsorderid+"交易失败======");
			sendPushMessage(prp.getUserId(), new BigDecimal(prp.getRealAmount()));
			//LOG.info("订单已支付!");
		}
		
		PrintWriter pw = response.getWriter();
		pw.print("SUCCESS");
		pw.close();
	}


	
	public static String sendRegister(CommonBean trans) {
		String response = null;
		try {
			System.out.println("上传参数===>" + JsonUtils.objectToJson(trans));
			TransUtil tu = new TransUtil();
			byte[] reponse = tu.packet(trans, dskey);
			response = CommonUtil.post(registerUrl, reponse);
			System.out.println("返回参数===>" + response);
			Map<String, String> resMap = mapper.readValue(response, Map.class);
			String sign;
			sign = SignUtil.getSign(resMap, dskey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
	
	
	public static String sendUpdate(CommonBean trans) {
		String response = null;
		try {
			System.out.println("上传参数===>" + JsonUtils.objectToJson(trans));
			TransUtil tu = new TransUtil();
			byte[] reponse = tu.packet(trans, dskey);
			response = CommonUtil.post(updateUrl, reponse);
			System.out.println("返回参数===>" + response);
			Map<String, String> resMap = mapper.readValue(response, Map.class);
			String sign;
			sign = SignUtil.getSign(resMap, dskey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
	
	
	public static String sendPay(CommonBean trans) {
		String response = null;
		try {
			System.out.println("上传参数===>" + JsonUtils.objectToJson(trans));
			TransUtil tu = new TransUtil();
			byte[] reponse = tu.packet(trans, dskey);
			response = CommonUtil.post(payUrl, reponse);
			System.out.println("返回参数===>" + response);
			Map<String, String> resMap = mapper.readValue(response, Map.class);
			String sign;
			sign = SignUtil.getSign(resMap, dskey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
	
	
	public static String sendQuery(CommonBean trans) {
		String response = null;
		try {
			System.out.println("上传参数===>" + JsonUtils.objectToJson(trans));
			TransUtil tu = new TransUtil();
			byte[] reponse = tu.packet(trans, dskey);
			response = CommonUtil.post(queryUrl, reponse);
			System.out.println("返回参数===>" + response);
			Map<String, String> resMap = mapper.readValue(response, Map.class);
			String sign;
			sign = SignUtil.getSign(resMap, dskey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
	
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/tohqquickbankinfo")
	public String returnHQQuickBankInfo(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String bankName = request.getParameter("bankName");// 结算卡银行名称
		String bankNo = request.getParameter("bankNo");// 结算卡卡号
		String amount = request.getParameter("amount");
		String ordercode = request.getParameter("ordercode");
		String cardType = request.getParameter("cardType");// 结算卡的卡类型
		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String ipAddress = request.getParameter("ipAddress");
		String isRegister = request.getParameter("isRegister");

		model.addAttribute("bankName", bankName);
		model.addAttribute("bankNo", bankNo);
		model.addAttribute("amount", amount);
		model.addAttribute("ordercode", ordercode);
		model.addAttribute("cardType", cardType);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("ipAddress", ipAddress);
		model.addAttribute("isRegister", isRegister);

		return "hqquickbankinfo";
	}
	
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/tohqquickpay")
	public String returnHQQuickPay(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String bankName = request.getParameter("bankName");
		String bankCard = request.getParameter("bankCard");
		String amount = request.getParameter("amount");
		String ordercode = request.getParameter("ordercode");
		String cardType = request.getParameter("cardType");// 结算卡的卡类型
		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("bankName", bankName);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("amount", amount);
		model.addAttribute("ordercode", ordercode);
		model.addAttribute("cardType", cardType);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("ipAddress", ipAddress);

		
		return "hqquickpay";
	}
	private void sendPushMessage(String userId,BigDecimal realAmount) {
		/**
		 * 推送消息 /v1.0/user/jpush/tset
		 */
		String alert = "快捷支付";
		String content = "亲爱的会员，" + realAmount.setScale(2, BigDecimal.ROUND_DOWN) + "元订单已经失败，请稍后重试！";
		String btype = "balanceadd";
		String btypeval = "";
		/** 获取身份证实名信息 */
		// URI uri = util.getServiceUrl("user", "error url request!");
		// String url = uri.toString() + "/v1.0/user/jpush/tset";
		String url = "http://user/v1.0/user/jpush/tset";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("userId", userId);
		requestEntity.add("alert", alert + "");
		requestEntity.add("content", content + "");
		requestEntity.add("btype", btype + "");
		requestEntity.add("btypeval", btypeval + "");
		// RestTemplate restTemplate = new RestTemplate();
		try {
			restTemplate.postForObject(url, requestEntity, String.class);
		} catch (RestClientException e) {
			e.printStackTrace();
			LOG.error("",e);
		}
	}
	@RequestMapping(value = "/v1.0/paymentgateway/topup/quickpaysuccess", method = {RequestMethod.GET, RequestMethod.POST})
	String quickSuccessPagejump() {
		return "kuaijieasuccess";
	}
}
