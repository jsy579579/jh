package com.jh.paymentgateway.controller;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
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
import com.jh.paymentgateway.pojo.MHHQDHRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.hq.CommonBean;
import com.jh.paymentgateway.util.hq.CommonUtil;
import com.jh.paymentgateway.util.hq.JsonUtils;
import com.jh.paymentgateway.util.hq.SignUtil;
import com.jh.paymentgateway.util.hq.TransUtil;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;


@Controller
@EnableAutoConfiguration
public class MHHQDHpageRequest extends BaseChannel{
	private static final Logger LOG = LoggerFactory.getLogger(MHHQDHpageRequest.class);
	static ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setSerializationInclusion(Inclusion.NON_NULL);
	}

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private MHTopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	
	@Value("${payment.ipAddress}")
	private String ip;

	public final static String DATE_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
	private static String merchno = "xydz20181203150";
	private static String dskey = "dac7533a";
	private static String url = "http://pay.huanqiuhuiju.com/authsys/api/repay/execute.do";

	private static String url1 = "http://pay.huanqiuhuiju.com/authsys/api/auth/execute.do";
	
	// 跟还款对接的接口
		@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhqdh/torepayment")
		public @ResponseBody Object HLJCRegister(HttpServletRequest request,
				@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "idCard") String idCard,
				@RequestParam(value = "phone") String phone, @RequestParam(value = "userName") String userName,
				@RequestParam(value = "bankName") String bankName) throws Exception {

			Map<String, Object> map = new HashMap<String, Object>();

			boolean hasKey = false;
			String key = "/v1.0/paymentgateway/topup/mhhqdh/torepayment:bankCard=" + bankCard + ";idCard=" + idCard + ";phone=" + phone + ";userName=" + userName + ";bankName=" + bankName;
			ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
			hasKey = redisTemplate.hasKey(key);
			if(hasKey) {
				return ResultWrap.init(CommonConstants.FALIED, "操作过于频繁,请10秒后重试!");
			}
			opsForValue.set(key, key, 10, TimeUnit.SECONDS);
			
			MHHQDHRegister hqdhRegister = topupPayChannelBusiness.getMHHQDHRegisterByIdCard(idCard);

			if (hqdhRegister == null) {

				map = (Map<String, Object>) HQRegister(bankName, phone, idCard, bankCard, userName);
				Object respCode = map.get("resp_code");
				Object respMessage = map.get("resp_message");
				LOG.info("respCode=====" + respCode);

				if ("000000".equals(respCode.toString())) {

					redisTemplate.delete(key);
					return ResultWrap.init(CommonConstants.SUCCESS, respMessage.toString());
				} else {

					return ResultWrap.init(CommonConstants.FALIED, respMessage.toString());
				}
			} else {

				redisTemplate.delete(key);
				return ResultWrap.init(CommonConstants.SUCCESS, "已完成鉴权验证!");
			}

		}
	

	//进件接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhqdh/register")
	public @ResponseBody Object HQRegister(@RequestParam(value = "bankName") String bankName,
			@RequestParam(value = "phone") String phone,
			@RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "userName") String userName
			) throws Exception {
		CommonBean trans = new CommonBean();
		
		trans.setVersion("0100");
		String orderSn = UUID.randomUUID().toString().replaceAll("-", "");
		trans.setOrdersn(orderSn);//流水号
		trans.setMerchno(merchno);
		String merchantOrder = UUID.randomUUID().toString().replaceAll("-", "");
		trans.setDsorderid(merchantOrder);//商户订单号
		trans.setTranscode("015");
		trans.setMethodname("register");
		trans.setAccountName(userName);
		trans.setIdcard(idCard);
		trans.setMobile(phone);

		LOG.info("进件请求报文======" + trans);
		
		String result = send(trans);
		
		LOG.info("请求进件返回的result======" + result);
		
		JSONObject jsonobj = JSONObject.fromObject(result);
		String returncode = jsonobj.getString("returncode");
		String errtext = jsonobj.getString("errtext");
		
		if("0000".equals(returncode)) {
			String subMerchantNo = jsonobj.getString("subMerchantNo");
			String dsorderid = jsonobj.getString("dsorderid");
			
			MHHQDHRegister hqdhRegister = new MHHQDHRegister();
			hqdhRegister.setIdCard(idCard);
			hqdhRegister.setMerchantCode(subMerchantNo);
			hqdhRegister.setPhone(phone);
			hqdhRegister.setMerchantOrder(dsorderid);
			hqdhRegister.setStatus("1");
			
			topupPayChannelBusiness.createMHHQDHRegister(hqdhRegister);
			
			return ResultWrap.init(CommonConstants.SUCCESS, "进件成功");
			
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, errtext);
		}
		
	}


	//快捷支付接口
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/mhhqdh/fastpay")
	public @ResponseBody Object hqFastPay(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "province") String province) {
		CommonBean trans = new CommonBean();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String realAmount = prp.getRealAmount();
		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();
		String rate = prp.getRate();
		String extraFee = prp.getExtraFee();
		String userName = prp.getUserName();
		String phone = prp.getCreditCardPhone();
		String securityCode = prp.getSecurityCode();
		String expiredTime = prp.getExpiredTime();
		
		if(securityCode == null && "".equals(securityCode)) {
			
			return ResultWrap.init(CommonConstants.FALIED, "安全码为空,需要完善信用卡信息!");
		}
		
		if(expiredTime == null && "".equals(expiredTime)) {
			
			return ResultWrap.init(CommonConstants.FALIED, "有效期为空,需要完善信用卡信息!");
		}
		
		MHHQDHRegister hqdhRegister = topupPayChannelBusiness.getMHHQDHRegisterByIdCard(idCard);
		
		String bigRealAmount = new BigDecimal(realAmount).multiply(new BigDecimal("100")).setScale(0).toString();
		String bigRate = new BigDecimal(rate).multiply(new BigDecimal("100")).setScale(2).stripTrailingZeros().toPlainString();
		String bigExtraFee = new BigDecimal(extraFee).multiply(new BigDecimal("100")).divide(BigDecimal.valueOf(2)).setScale(0).toString();
		
		// 报文头
		trans.setVersion("0100");
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setMerchno(merchno);
		trans.setDsorderid(orderCode);
		trans.setTranscode("015");
		// 业务参数
		trans.setMethodname("pay");
		trans.setSubMerchantNo(hqdhRegister.getMerchantCode());
		trans.setAmount(bigRealAmount);
		trans.setFutureRateValue(bigRate);
		//trans.setFixAmount(bigExtraFee);
		trans.setFixAmount("0");
		trans.setBankcard(bankCard);
		trans.setAccountName(userName);
		trans.setIdcard(idCard);
		trans.setMobile(phone);
		trans.setCvn2(securityCode);
		trans.setExpireDate(this.expiredTimeToMMYY(expiredTime));
		trans.setProvince(province);
		
		trans.setNotifyUrl(ip + "/v1.0/paymentgateway/topup/mhhqdh/fastpay/notifyurl");

		LOG.info("请求快捷支付的报文======" + trans);
		
		String result = send(trans);
		
		LOG.info("请求快捷支付返回的result======" + result);
		
		JSONObject jsonobj = JSONObject.fromObject(result);
		String returnCode = jsonobj.getString("returncode");
		String errtext = jsonobj.getString("errtext");
		
		if("0003".equals(returnCode) || "0000".equals(returnCode)) {
			
			return ResultWrap.init("999998", "等待银行扣款中");
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, errtext);
		}
		
	}


	
	//代付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhqdh/transfer")
	public @ResponseBody Object transfer(@RequestParam(value = "orderCode") String orderCode) {

		CommonBean trans = new CommonBean();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String realAmount = prp.getRealAmount();
		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();
		String rate = prp.getRate();
		String phone = prp.getCreditCardPhone();
		String extraFee = prp.getExtraFee();
		
		MHHQDHRegister hqdhRegister = topupPayChannelBusiness.getMHHQDHRegisterByIdCard(idCard);
		
		String bigRealAmount = new BigDecimal(realAmount).add(new BigDecimal(extraFee)).multiply(new BigDecimal("100")).setScale(0).toString();
		String bigRate = new BigDecimal(rate).multiply(new BigDecimal("100")).setScale(2).stripTrailingZeros().toPlainString();
		String bigExtraFee = new BigDecimal(extraFee).multiply(new BigDecimal("100")).setScale(0).toString();
		
		// 报文头
		trans.setVersion("0100");
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setMerchno(merchno);
		trans.setDsorderid(orderCode);
		trans.setTranscode("015");
		// 业务参数
		trans.setMethodname("withDraw");
		trans.setSubMerchantNo(hqdhRegister.getMerchantCode());
		trans.setAmount(bigRealAmount);
		trans.setFutureRateValue(bigRate);
		trans.setFixAmount(bigExtraFee);
		trans.setBankcard(bankCard);
		trans.setMobile(phone);
		trans.setNotifyUrl(ip + "/v1.0/paymentgateway/topup/mhhqdh/transfer/notifyurl");

		LOG.info("请求代付的报文======" + trans);
		
		String result = send(trans);
		
		LOG.info("请求代付返回的result======" + result);
		
		JSONObject jsonobj = JSONObject.fromObject(result);
		String returnCode = jsonobj.getString("returncode");
		String errtext = jsonobj.getString("errtext");
		
		if("0000".equals(returnCode) || "0003".equals(returnCode)) {
			
			return ResultWrap.init("999998", "等待银行付款中");
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, errtext);
		}

	}

	
	
	//交易查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhqdh/orderquery")
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

		String result = send1(trans);
		
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
	
	
	//余额查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhqdh/balancequery")
	public @ResponseBody Object balanceQuery(@RequestParam(value = "idCard") String idCard
			) throws Exception {
		
		MHHQDHRegister hqdhRegister = topupPayChannelBusiness.getMHHQDHRegisterByIdCard(idCard);
		
		CommonBean trans = new CommonBean();
		// 报文头
		trans.setVersion("0100");
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setMerchno(merchno);
		trans.setDsorderid(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setTranscode("015");
		trans.setMethodname("queryBalance");
		trans.setSubMerchantNo(hqdhRegister.getMerchantCode());
		
		String resp = TransUtil.object2String(trans);
		Map<String, String> resMap = mapper.readValue(resp, Map.class);
		String sign = SignUtil.getSign(resMap, dskey);
		trans.setSign(sign);

		String result = send(trans);
		
		LOG.info("请求余额查询返回的result======" + result);
		
		JSONObject jsonobj = JSONObject.fromObject(result);
		String returncode = jsonobj.getString("returncode");
		String message = jsonobj.getString("errtext");
		
		if("0000".equals(returncode)) {
			String balanceAmount = jsonobj.getString("balanceAmount");
			
			return ResultWrap.init(CommonConstants.SUCCESS, "余额为： " + balanceAmount);
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, message);
		}
		
	}
	

	
	//同名卡交易异步通知
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhqdh/fastpay/notifyurl")
	public void tradeBack(HttpServletRequest request, HttpServletResponse response) throws Exception {

		LOG.info("快捷支付异步回调进来了======");
		
		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}
		
		String status = request.getParameter("status");
		String dsorderid = request.getParameter("dsorderid");
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(dsorderid);
		String channelTag = prp.getChannelTag();
		if ("00".equalsIgnoreCase(status)) {
			LOG.info("同名卡交易成功");
			
			String version = "13";
			if("HQDH_QUICK".equalsIgnoreCase(channelTag)) {
				version = "12";
			}
			LOG.info("version======" + version);
			
			RestTemplate restTemplate = new RestTemplate();
			
			String url = prp.getIpAddress() + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("orderCode", dsorderid);
			requestEntity.add("version", version);
			String result = null;
			JSONObject jsonObject;
			JSONObject resultObj;
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("RESULT================" + result);
				jsonObject = JSONObject.fromObject(result);
				resultObj = jsonObject.getJSONObject("result");
			} catch (Exception e) {
				e.printStackTrace();LOG.error("",e);
			}

			url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
			//url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", dsorderid);
			requestEntity.add("third_code", "");
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();LOG.error("",e);
			}

			LOG.info("订单状态修改成功==================="+dsorderid+"====================" + result);

			LOG.info("订单已支付!");
			
		}
		
		PrintWriter pw = response.getWriter();
		pw.print("SUCCESS");
		pw.close();
	}

	
	//代付的异步通知接口 
		@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhqdh/transfer/notifyurl")
		public void topupBack(HttpServletRequest request, HttpServletResponse response) throws Exception {

			LOG.info("代付异步回调进来了======");
			
			Map<String, String[]> parameterMap = request.getParameterMap();
			Set<String> keySet = parameterMap.keySet();
			for (String key : keySet) {
				String[] strings = parameterMap.get(key);
				for (String s : strings) {
					LOG.info(key + "=============" + s);
				}
			}
			
			String status = request.getParameter("status");
			String dsorderid = request.getParameter("dsorderid");
			
			PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(dsorderid);
			String channelTag = prp.getChannelTag();
			
			if ("00".equals("status")) {
				LOG.info("交易成功");
				
				String version = "13";
				if("HQDH_QUICK".equalsIgnoreCase(channelTag)) {
					version = "12";
				}
				LOG.info("version======" + version);
				
				RestTemplate restTemplate = new RestTemplate();
				
				String url = prp.getIpAddress() + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
				MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("orderCode", dsorderid);
				requestEntity.add("version", version);
				String result = null;
				JSONObject jsonObject;
				JSONObject resultObj;
				try {
					result = restTemplate.postForObject(url, requestEntity, String.class);
					LOG.info("RESULT================" + result);
					jsonObject = JSONObject.fromObject(result);
					resultObj = jsonObject.getJSONObject("result");
				} catch (Exception e) {
					e.printStackTrace();LOG.error("",e);
				}

				url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
				//url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("status", "1");
				requestEntity.add("order_code", dsorderid);
				requestEntity.add("third_code", "");
				try {
					result = restTemplate.postForObject(url, requestEntity, String.class);
				} catch (Exception e) {
					e.printStackTrace();LOG.error("",e);
				}
				
				
				
				LOG.info("订单状态修改成功==================="+dsorderid+"====================" + result);

				LOG.info("订单已代付!");
				
			}

			PrintWriter pw = response.getWriter();
			pw.print("SUCCESS");
			pw.close();
		}
	
	

	public String getUserFee(String rate, String amount, String extraFee) {
		BigDecimal b1 = new BigDecimal(rate);
		BigDecimal b2 = new BigDecimal(amount);
		BigDecimal b3 = new BigDecimal(extraFee);
		BigDecimal num1 = b1.multiply(b2).setScale(2, BigDecimal.ROUND_UP);
		LOG.info("本金*费率=" + num1);
		BigDecimal num2 = num1.add(b3);
		LOG.info("额外手续费：" + num1);
		String fee = num2.toString();
		return fee;

	}
	
	
	public String getUserFee1(String rate, String amount) {
		BigDecimal b1 = new BigDecimal(rate);
		BigDecimal b2 = new BigDecimal(amount);
		BigDecimal num1 = b1.multiply(b2).setScale(2, BigDecimal.ROUND_UP);
		LOG.info("手续费======" + num1);
		String fee = num1.toString();
		return fee;

	}

	public String getAmount(String fee, String amount) {
		BigDecimal b1 = new BigDecimal(fee);
		BigDecimal b2 = new BigDecimal(amount);
		String pay_amount = b1.add(b2).toString();
		return pay_amount;
	}

	
	public static String send(CommonBean trans) {
		String response = null;
		try {
			System.out.println("上传参数===>" + JsonUtils.objectToJson(trans));
			TransUtil tu = new TransUtil();
			byte[] reponse = tu.packet(trans, dskey);
			response = CommonUtil.post(url, reponse);
			System.out.println("返回参数===>" + response);
			Map<String, String> resMap = mapper.readValue(response, Map.class);
			String sign;
			sign = SignUtil.getSign(resMap, dskey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
	
	
	public static String send1(CommonBean trans) {
		String response = null;
		try {
			System.out.println("上传参数===>" + JsonUtils.objectToJson(trans));
			TransUtil tu = new TransUtil();
			byte[] reponse = tu.packet(trans, dskey);
			response = CommonUtil.post(url1, reponse);
			System.out.println("返回参数===>" + response);
			Map<String, String> resMap = mapper.readValue(response, Map.class);
			String sign;
			sign = SignUtil.getSign(resMap, dskey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
	
	
	
	//==========================================
	
	
	
	
	
	
	
	
}
