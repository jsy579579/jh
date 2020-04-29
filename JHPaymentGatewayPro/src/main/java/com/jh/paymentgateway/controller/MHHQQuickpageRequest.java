package com.jh.paymentgateway.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.springframework.web.client.RestTemplate;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.MHTopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.BankNumCode;
import com.jh.paymentgateway.pojo.MHHQQuickRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.Util;
import com.jh.paymentgateway.util.hq.CommonBean;
import com.jh.paymentgateway.util.hq.CommonUtil;
import com.jh.paymentgateway.util.hq.JsonUtils;
import com.jh.paymentgateway.util.hq.SignUtil;
import com.jh.paymentgateway.util.hq.TransUtil;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import net.sf.json.JSONObject;


@Controller
@EnableAutoConfiguration
public class MHHQQuickpageRequest extends BaseChannel{
	private static final Logger LOG = LoggerFactory.getLogger(MHHQQuickpageRequest.class);
	static ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setSerializationInclusion(Inclusion.NON_NULL);
	}

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private MHTopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;

	public final static String DATE_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
	private static String merchno = "xydz20181203150";
	private static String dskey = "dac7533a";
	
	private static String registerUrl = "http://pay.huanqiuhuiju.com/authsys/weishua/api/nocard/merchant/register.do";

	private static String updateUrl = "http://pay.huanqiuhuiju.com/authsys/weishua/api/nocard/update/fee.do";
	
	private static String payUrl = "http://pay.huanqiuhuiju.com/authsys/weishua/api/nocard/quick/pay.do";
	
	private static String queryUrl = "http://pay.huanqiuhuiju.com/authsys/api/auth/execute.do";

	//进件接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhqquick/register")
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
		trans.setTranscode("040");
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
		
		if("0000".equals(returncode)) {
			LOG.info("用户：" + userName + "进件成功======");
			
			String subMerchantNo = fromObject.getString("subMerchantNo");
			
			MHHQQuickRegister hqQuickRegister = new MHHQQuickRegister();
			hqQuickRegister.setPhone(phone);
			hqQuickRegister.setBankCard(debitCardNo);
			hqQuickRegister.setIdCard(idCard);
			hqQuickRegister.setRate(rate);
			hqQuickRegister.setExtraFee(extraFee);
			hqQuickRegister.setMerchantNo(subMerchantNo);
			
			topupPayChannelBusiness.createMHHQQuickRegister(hqQuickRegister);
			
			return ResultWrap.init(CommonConstants.SUCCESS, "进件成功!");
		}else {
			
			this.addOrderCauseOfFailure(orderCode, "进件失败,失败原因为: " + errtext, prp.getIpAddress());
			
			return ResultWrap.init(CommonConstants.FALIED, errtext);
		}
		
	}


	//修改进件的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhqquick/updateregister")
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
		
		MHHQQuickRegister hqQuickRegister = topupPayChannelBusiness.getMHHQQuickRegisterByIdCard(idCard);
		
		CommonBean trans = new CommonBean();
		// 报文头
		trans.setVersion("0100");
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setMerchno(merchno);
		trans.setDsorderid("xl" + System.currentTimeMillis() + "");
		trans.setTranscode("042");
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
			
			topupPayChannelBusiness.createMHHQQuickRegister(hqQuickRegister);
			
			return ResultWrap.init(CommonConstants.SUCCESS, "修改进件成功!");
		}else {
			
			this.addOrderCauseOfFailure(orderCode, "修改进件失败,失败原因为: " + errtext, prp.getIpAddress());
			
			return ResultWrap.init(CommonConstants.FALIED, errtext);
		}
		
	}

	//快捷支付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhqquick/fastpay")
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

		MHHQQuickRegister hqQuickRegister = topupPayChannelBusiness.getMHHQQuickRegisterByIdCard(idCard);
		
		String expiredTimeToMMYY = this.expiredTimeToMMYY(expiredTime);
		
		String bigAmount = new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0).toString();
		
		// 报文头
		trans.setVersion("0100");
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setMerchno(merchno);
		trans.setDsorderid(orderCode);
		trans.setTranscode("041");
		// 业务参数
		trans.setAmount(bigAmount);
		trans.setBankcard(bankCard);
		trans.setAccountName(userName);
		trans.setMobile(creditCardPhone);
		trans.setCvn2(securityCode);
		trans.setExpireDate(expiredTimeToMMYY);
		trans.setSubMerchantNo(hqQuickRegister.getMerchantNo());
		trans.setReturnUrl("http://106.15.47.73/v1.0/paymentchannel/topup/sdjpaysuccess");
		trans.setNotifyUrl(ip + "/v1.0/paymentgateway/topup/mhhqquick/fastpay/notifyurl");

		LOG.info("请求快捷支付的报文======" + trans);
		
		String result = sendPay(trans);
		LOG.info("请求快捷支付返回的result======" + result);
		JSONObject fromObject = JSONObject.fromObject(result);
		String returncode = fromObject.getString("returncode");
		String errtext = fromObject.getString("errtext");
		
		if("0000".equals(returncode)) {
			String pay_info = fromObject.getString("pay_info");
			String pay_code = fromObject.getString("pay_code");
			JSONObject fromObject2 = JSONObject.fromObject(pay_code);
			
			Map<String,Object> map = new HashMap<String, Object>();
			
			map = fromObject2;
			
			Set<String> keySet = map.keySet();
			Iterator<String> it = keySet.iterator();
			String redirectUrl = null;
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
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhqquick/orderquery")
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
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhqquick/fastpay/notifyurl")
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
	
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/tomhhqquickbankinfo")
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

		return "mhhqquickbankinfo";
	}
	
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/tomhhqquickpay")
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

		return "mhhqquickpay";
	}
	
	
}
