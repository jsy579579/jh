package com.jh.paymentgateway.controller;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentgateway.business.MHTopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.MHHQBindCard;
import com.jh.paymentgateway.pojo.MHHQRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.hq.CommonBean;
import com.jh.paymentgateway.util.hq.CommonUtil;
import com.jh.paymentgateway.util.hq.JsonUtils;
import com.jh.paymentgateway.util.hq.SignUtil;
import com.jh.paymentgateway.util.hq.SubMer;
import com.jh.paymentgateway.util.hq.TransUtil;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.UUIDGenerator;
import net.sf.json.JSONObject;


@Controller
@EnableAutoConfiguration
public class MHHQpageRequest {
	private static final Logger LOG = LoggerFactory.getLogger(MHHQpageRequest.class);
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
	private static String url = "http://pay.huanqiuhuiju.com/authsys/api/hc/execute.do";

	private static String url1 = "http://pay.huanqiuhuiju.com/authsys/api/auth/execute.do";
	
	// 跟还款对接的接口
		@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhq/torepayment1")
		public @ResponseBody Object HLJCRegister(HttpServletRequest request,
				@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "idCard") String idCard,
				@RequestParam(value = "phone") String phone, @RequestParam(value = "userName") String userName,
				@RequestParam(value = "bankName") String bankName) throws Exception {

			Map<String, Object> map = new HashMap<String, Object>();

			MHHQRegister hqRegister = topupPayChannelBusiness.getMHHQRegisterByIdCard(idCard);

			MHHQBindCard hqBindCard = topupPayChannelBusiness.getMHHQBindCardByBankCard(bankCard);
			
			if (hqRegister == null) {

				map = (Map<String, Object>) HQRegister(bankName, phone, idCard, bankCard, userName);
				Object respCode = map.get("resp_code");
				Object respMessage = map.get("resp_message");
				LOG.info("respCode=====" + respCode);

				if ("000000".equals(respCode.toString())) {

					map = (Map<String, Object>) OpenCard(bankCard, phone, userName, idCard);

					return map;

				} else {

					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, respMessage);
					return map;

				}

			} else {

				if (hqBindCard == null || !"1".equals(hqBindCard.getStatus())) {

					map = (Map<String, Object>) OpenCard(bankCard, phone, userName, idCard);

					return map;

				}else if(!"1".equals(hqBindCard.getStatuss())) {
					
					map = (Map<String, Object>) hqBindCard(bankCard, idCard, bankName);
					
					return map;
					
				} else {

					map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					map.put(CommonConstants.RESP_MESSAGE, "已完成绑卡");
					return map;

				}

			}

		}
	
	

	//进件接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhq/register")
	public @ResponseBody Object HQRegister(@RequestParam(value = "bankName") String bankName,
			@RequestParam(value = "phone") String phone,
			@RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "userName") String userName
			) throws Exception {
		CommonBean trans = new CommonBean();
		
		trans.setVersion("0100");
		String orderSn = UUID.randomUUID().toString().replaceAll("-", "");
		trans.setOrdersn(orderSn);
		trans.setMerchno(merchno);
		String merchantOrder = UUID.randomUUID().toString().replaceAll("-", "");
		trans.setDsorderid(merchantOrder);
		trans.setTranscode("404");

		SubMer subMer = new SubMer();
		subMer.setSubMerType("3");
		subMer.setLegalName(userName);
		subMer.setLegalMobile(phone);
		subMer.setLegalIdNo(idCard);
		subMer.setLegalValidityFlag("1");
		subMer.setLegalBankCard(bankCard);
		subMer.setLegalBankName(bankName);
		subMer.setLegalBankCardType("credit");
		subMer.setMinSettleAmout("10");
		subMer.setCompanyAddress("上海市宝山区");

		trans.setNotifyUrl(ip + "/v1.0/paymentgateway/topup/mhhq/register/notifyurl");
		trans.setSubMer(subMer);
		trans.setMethodname("network");

		LOG.info("进件请求报文======" + trans);
		
		String result = send(trans);
		
		LOG.info("请求进件返回的result======" + result);
		
		JSONObject jsonobj = JSONObject.fromObject(result);
		String message = null;
		String MESSAGE = null;
		if("0000".equals(jsonobj.getString("returncode"))) {
			
			message = jsonobj.getString("message");
			JSONObject messageobj = JSONObject.fromObject(message);
			String code = messageobj.getString("code");
			MESSAGE = messageobj.getString("message");
			String merchantCode = messageobj.getString("subMerchantNo");
			
			if ("SUCCESS".equalsIgnoreCase(code)) {
				
				MHHQRegister hqRegister = new MHHQRegister();
				
				hqRegister.setPhone(phone);
				hqRegister.setBankCard(bankCard);
				hqRegister.setIdCard(idCard);
				hqRegister.setMerchantCode(merchantCode);
				hqRegister.setMerchantOrder(merchantOrder);
				hqRegister.setStatus("0");
				
				topupPayChannelBusiness.createMHHQRegister(hqRegister);
				
				return ResultWrap.init(CommonConstants.SUCCESS, MESSAGE);
				
			} else {
				
				return ResultWrap.init(CommonConstants.FALIED, MESSAGE);
			}
			
		}else {
			message = jsonobj.getString("message");
			
			return ResultWrap.init(CommonConstants.FALIED, MESSAGE);
		}
		
	}

	//进件查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhq/queryregister")
	public @ResponseBody Object queryRegister(@RequestParam(value = "idCard") String idCard
			) throws Exception {
		
		CommonBean trans = new CommonBean();
		// 报文头
		trans.setVersion("0100");
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setMerchno(merchno);
		trans.setDsorderid("xl" + System.currentTimeMillis() + "");
		trans.setTranscode("404");
		// 业务参数
		trans.setMethodname("queryNetwork");
		trans.setIdCardNo("idCard");
		
		LOG.info("请求进件查询的报文======" + trans);
		
		String result = send(trans);
		
		LOG.info("请求进件查询返回的result======" + result);
		
		JSONObject jsonobj = JSONObject.fromObject(result);
		String code = jsonobj.getString("code");
		String subMerchantNo = jsonobj.getString("subMerchantNo");
		if ("code".equals("SUCCESS")) {
			

		}

		return result;
	}

	//同名卡开卡接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhq/opencard")
	public @ResponseBody Object OpenCard(@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "phone") String phone,
			@RequestParam(value = "userName") String userName,
			@RequestParam(value = "idCard") String idCard
			) throws Exception {
		Map<String, Object> maps = new HashMap<String, Object>();
		
		MHHQRegister hqRegister = topupPayChannelBusiness.getMHHQRegisterByIdCard(idCard);
		
		CommonBean trans = new CommonBean();
		// 报文头
		trans.setVersion("0100");
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setMerchno(merchno);
		trans.setDsorderid("xl" + System.currentTimeMillis() + "");
		trans.setTranscode("404");
		// 业务参数
		trans.setMethodname("openCard");
		trans.setBankCardNo(bankCard);
		trans.setSubMerchantNo(hqRegister.getMerchantCode());
		trans.setMobile(phone);
		trans.setName(userName);
		trans.setIdCardNo(idCard);

		trans.setCardType("credit");

		trans.setNotifyUrl(ip + "/v1.0/paymentgateway/topup/mhhq/opencard/notifyurl");
		trans.setReturnUrl("http://www.shanqi111.cn/v1.0/paymentchannel/topup/wmyk/bindcardsuccess");

		LOG.info("请求同名卡开卡的报文======" + trans);
		
		String result = send(trans);
		
		LOG.info("请求同名卡开卡返回的result======" + result);
		
		JSONObject jsonobj = JSONObject.fromObject(result);
		String returnCode = jsonobj.getString("returncode");
		String message = jsonobj.getString("message");
		
		if("0000".equals(returnCode)) {
			
			MHHQBindCard hqBindCard = topupPayChannelBusiness.getMHHQBindCardByBankCard(bankCard);
			
			if(hqBindCard == null) {
				MHHQBindCard hqBind = new MHHQBindCard();
				hqBind.setPhone(phone);
				hqBind.setIdCard(idCard);
				hqBind.setBankCard(bankCard);
				hqBind.setStatus("0");
				
				topupPayChannelBusiness.createMHHQBindCard(hqBind);
				
			}else {
				
				hqBindCard.setStatus("0");
				
				topupPayChannelBusiness.createMHHQBindCard(hqBindCard);
			}
			
			maps.put(CommonConstants.RESP_CODE, "999996");
			maps.put(CommonConstants.RESP_MESSAGE, "首次使用需进行绑卡授权,点击确定进入授权页面!");
			maps.put(CommonConstants.RESULT, message);
			
			return maps;
		}else {
			
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, message);
			
			return maps;
		}
		
	}

	//快捷支付接口
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/mhhq/fastpay")
	public @ResponseBody Object hqFastPay(@RequestParam(value = "orderCode") String orderCode) {
		Map<String, Object> maps = new HashMap<String, Object>();
		CommonBean trans = new CommonBean();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String realAmount = prp.getRealAmount();
		String amount = prp.getAmount();
		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();
		String rate = prp.getRate();
		String extraFee = prp.getExtraFee();
		//String fee = getUserFee(rate, amount, extraFee);

		String fee = getUserFee1(rate, amount);
		
		MHHQRegister hqRegister = topupPayChannelBusiness.getMHHQRegisterByIdCard(idCard);
		
		// 报文头
		trans.setVersion("0100");
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setMerchno(merchno);
		trans.setDsorderid(orderCode);
		trans.setTranscode("404");
		// 业务参数
		trans.setMethodname("trade");
		trans.setSubMerchantNo(hqRegister.getMerchantCode());
		trans.setAmount(realAmount);
		trans.setUserFee(fee);
		trans.setBankCardNo(bankCard);
		trans.setNotifyUrl(ip + "/v1.0/paymentgateway/topup/mhhq/fastpay/notifyurl");

		LOG.info("请求快捷支付的报文======" + trans);
		
		String result = send(trans);
		
		LOG.info("请求快捷支付返回的result======" + result);
		
		JSONObject jsonobj = JSONObject.fromObject(result);
		String message = jsonobj.getString("message");
		JSONObject messageobj = JSONObject.fromObject(message);
		String code = messageobj.getString("code");
		String MESSAGE = messageobj.getString("message");
		
		String returnCode = jsonobj.getString("returncode");
		
		if("0000".equals(returnCode) || "0002".equals(returnCode) || "0003".equals(returnCode) || "0011".equals(returnCode)) {
			
			return ResultWrap.init("999998", "等待银行扣款中");
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, MESSAGE);
		}
		

	}


	
	

	
	//代付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhq/transfer")
	public @ResponseBody Object transfer(@RequestParam(value = "orderCode") String orderCode) {

		CommonBean trans = new CommonBean();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String realAmount = prp.getRealAmount();
		String bankCard = prp.getBankCard();
		String idCard = prp.getIdCard();
		String rate = prp.getRate();
		String extraFee = prp.getExtraFee();
		/*String fee = getUserFee(rate, amount, extraFee);
		String pay_amount = getAmount(fee, amount);*/

		MHHQRegister hqRegister = topupPayChannelBusiness.getMHHQRegisterByIdCard(idCard);
		
		// 报文头
		trans.setVersion("0100");
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setMerchno(merchno);
		trans.setDsorderid(orderCode);
		trans.setTranscode("404");

		trans.setMethodname("withdraw");
		trans.setSubMerchantNo(hqRegister.getMerchantCode());
		//trans.setSubMerchantNo(merchantCode);
		trans.setBankCardNo(bankCard);
		trans.setAmount(realAmount);
		trans.setUserFee(extraFee);
		trans.setNotifyUrl(ip + "/v1.0/paymentgateway/topup/mhhq/transfer/notifyurl");

		LOG.info("发起代付的请求报文======" + trans);
		
		String result = send(trans);
		
		LOG.info("请求代付返回的result======" + result);
		
		JSONObject jsonobj = JSONObject.fromObject(result);
		String message = jsonobj.getString("message");
		JSONObject messageobj = JSONObject.fromObject(message);
		String code = messageobj.getString("code");
		String MESSAGE = messageobj.getString("message");
		
		String returnCode = jsonobj.getString("returncode");
		if("0000".equals(returnCode) || "0002".equals(returnCode) || "0003".equals(returnCode) || "0011".equals(returnCode)) {
			
			return ResultWrap.init("999998", "等待银行扣款中");
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, MESSAGE);
		}
		
		/*if ("SUCCESS".equals(code)) {
			
			return ResultWrap.init("999998", "等待银行出款中");
		} else {
			
			return ResultWrap.init(CommonConstants.FALIED, MESSAGE);
		}*/

	}

	
	
	//子商户绑卡接口
		@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhq/bindCard")
		public @ResponseBody Object hqBindCard(@RequestParam(value = "bankCard") String bankCard,
				@RequestParam(value = "idCard") String idCard,
				@RequestParam(value = "bankName") String bankName
				) {
			Map<String, Object> maps = new HashMap<String, Object>();
			
			MHHQRegister hqRegister = topupPayChannelBusiness.getMHHQRegisterByIdCard(idCard);
			
			CommonBean trans = new CommonBean();
			
			// 报文头
			trans.setVersion("0100");
			trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
			trans.setMerchno(merchno);
			trans.setDsorderid("xl" + System.currentTimeMillis() + "");
			trans.setTranscode("404");
			// 业务参数
			trans.setMethodname("bindCard");
			trans.setSubMerchantNo(hqRegister.getMerchantCode());
			trans.setBankCardNo(bankCard);
			trans.setBankName(bankName);
			
			LOG.info("请求子商户绑卡的报文======" + trans);
			
			String result = send(trans);
			
			LOG.info("请求子商户绑卡返回的result======" + result);

			JSONObject jsonobj = JSONObject.fromObject(result);
			String message = jsonobj.getString("message");
			JSONObject messageobj = JSONObject.fromObject(message);
			String code = messageobj.getString("code");
			String MESSAGE = messageobj.getString("message");
			if ("SUCCESS".equals(code)) {
				
				LOG.info("子商户绑卡成功");

				MHHQBindCard hqBindCard = topupPayChannelBusiness.getMHHQBindCardByBankCard(bankCard);
				
				hqBindCard.setStatuss("1");
				
				topupPayChannelBusiness.createMHHQBindCard(hqBindCard);
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "子商户绑卡成功");

			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, MESSAGE);
			}
			return maps;
		}

		
		//手动代付到储蓄卡的接口
		@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhq/transferbymanual")
		public @ResponseBody Object transferByManual(
				String phone,
				String brandId,
				String realAmount,
				@RequestParam(value = "extraFee", required = false, defaultValue = "1") String extraFee,
				@RequestParam(value = "ipAddress", required = false, defaultValue = "http://106.15.47.73") String ipAddress
				) {
			
			RestTemplate restTemplate = new RestTemplate();
			String url = ipAddress + "/v1.0/user/query/phone";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("phone", phone);
			requestEntity.add("brandId", brandId);
			String result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================" + result);
			JSONObject jsonObject;
			JSONObject resultObj;
			long userId;
			try {
				jsonObject = JSONObject.fromObject(result);
				resultObj = jsonObject.getJSONObject("result");
				userId = resultObj.getLong("id");
			} catch (Exception e) {
				LOG.error("根据手机号查询用户信息失败=============================", e);

				return ResultWrap.init(CommonConstants.FALIED, "根据手机号查询用户信息失败,请确认手机号是否正确!");
			}
			
			url = ipAddress + "/v1.0/user/bank/default/userid";
			requestEntity.add("user_id", userId + "");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("接口/v1.0/user/bank/default/userid====RESULT=========" + result);
			
			jsonObject = JSONObject.fromObject(result);
			if(CommonConstants.SUCCESS.equals(jsonObject.getString(CommonConstants.RESP_CODE))) {
				resultObj = jsonObject.getJSONObject(CommonConstants.RESULT);
				String bankCard = resultObj.getString("cardNo");
				String bankName = resultObj.getString("bankName");
				String idCard = resultObj.getString("idcard");
			
				MHHQRegister hqRegister = topupPayChannelBusiness.getMHHQRegisterByIdCard(idCard);
				
				CommonBean trans = new CommonBean();
				trans.setVersion("0100");
				trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
				trans.setMerchno(merchno);
				trans.setDsorderid("xl" + System.currentTimeMillis() + "");
				trans.setTranscode("404");
				// 业务参数
				trans.setMethodname("bindCard");
				trans.setSubMerchantNo(hqRegister.getMerchantCode());
				trans.setBankCardNo(bankCard);
				trans.setBankName(bankName);
				
				LOG.info("请求子商户绑卡的报文======" + trans);
				
				String send = send(trans);
				
				LOG.info("请求子商户绑卡返回的result======" + send);

				JSONObject jsonobj = JSONObject.fromObject(send);
				String message = jsonobj.getString("message");
				JSONObject messageobj = JSONObject.fromObject(message);
				String code = messageobj.getString("code");
				String MESSAGE = messageobj.getString("message");
				if ("SUCCESS".equals(code)) {
					LOG.info("子商户绑卡成功");
					
					String uuid = UUIDGenerator.getUUID();
					LOG.info("生成的代付订单号=====" + uuid);
					
					trans.setVersion("0100");
					trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
					trans.setMerchno(merchno);
					trans.setDsorderid(uuid);
					trans.setTranscode("404");

					trans.setMethodname("withdraw");
					trans.setSubMerchantNo(hqRegister.getMerchantCode());
					trans.setBankCardNo(bankCard);
					trans.setAmount(realAmount);
					trans.setUserFee(extraFee);
					trans.setNotifyUrl(ip + "/v1.0/paymentgateway/topup/mhhq/transfer/notifyurl");

					LOG.info("发起代付的请求报文======" + trans);
					
					send = send(trans);
					
					LOG.info("请求代付返回的result======" + result);
					
					jsonobj = JSONObject.fromObject(result);
					message = jsonobj.getString("message");
					messageobj = JSONObject.fromObject(message);
					code = messageobj.getString("code");
					MESSAGE = messageobj.getString("message");
					
					String returnCode = jsonobj.getString("returncode");
					if("0000".equals(returnCode) || "0002".equals(returnCode) || "0003".equals(returnCode) || "0011".equals(returnCode)) {
						
						return ResultWrap.init("999998", "等待银行扣款中", uuid);
					}else {
						
						return ResultWrap.init(CommonConstants.FALIED, MESSAGE);
					}
				} else {
					
					return ResultWrap.init(CommonConstants.FALIED, MESSAGE);
				}
			}else {
				
				return jsonObject;
			}
		}
	
	
	
	//代付的异步通知接口 
	@RequestMapping(method = { RequestMethod.POST,
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/mhhq/transfer/notifyurl")
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
		
		String code = request.getParameter("code");
		String dsorderid = request.getParameter("dsorderid");
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(dsorderid);
		
		if (code.equals("SUCCESS")) {
			LOG.info("交易成功");
			
			RestTemplate restTemplate = new RestTemplate();
			
			String url = prp.getIpAddress() + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("orderCode", dsorderid);
			requestEntity.add("version", "8");
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

	
	//交易查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhq/orderquery")
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
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhhq/balancequery")
	public @ResponseBody Object balanceQuery(
			@RequestParam(value = "idCard") String idCard
			//@RequestParam(value = "merchantCode") String merchantCode
			) throws Exception {
		
		MHHQRegister hqRegister = topupPayChannelBusiness.getMHHQRegisterByIdCard(idCard);
		
		CommonBean trans = new CommonBean();
		// 报文头
		trans.setVersion("0100");
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setMerchno(merchno);
		trans.setDsorderid(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setTranscode("404");
		trans.setMethodname("queryBalance");
		trans.setSubMerchantNo(hqRegister.getMerchantCode());
		//trans.setSubMerchantNo(merchantCode);
		
		String resp = TransUtil.object2String(trans);
		Map<String, String> resMap = mapper.readValue(resp, Map.class);
		String sign = SignUtil.getSign(resMap, dskey);
		trans.setSign(sign);

		String result = send1(trans);
		
		LOG.info("请求余额查询返回的result======" + result);
		
		JSONObject jsonobj = JSONObject.fromObject(result);
		
		
		String returncode = jsonobj.getString("returncode");
		
		if("0000".equals(returncode)) {
			String message = jsonobj.getString("message");
			JSONObject messageobj = JSONObject.fromObject(message);
			String code = messageobj.getString("code");
			String MESSAGE = messageobj.getString("message");
			String amount = messageobj.getString("amount");
			
			return ResultWrap.init(CommonConstants.SUCCESS, "余额为： " + amount);
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, "查询失败");
		}
		
	}
	
	
	//入网异步通知
	@RequestMapping(method = { RequestMethod.POST,
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/mhhq/register/notifyurl")
	public void home(HttpServletRequest request, HttpServletResponse response) throws Exception {

		LOG.info("进件异步回调进来了======");
		
		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}
		
		String code = request.getParameter("code");
		String subMerchantNo = request.getParameter("subMerchantNo");
		String dsorderid = request.getParameter("dsorderid");
		if ("SUCCESS".equalsIgnoreCase(code)) {
			LOG.info("入驻商户成功");
			
			MHHQRegister hqRegister = topupPayChannelBusiness.getMHHQRegisterByMerchantOrder(dsorderid);
			hqRegister.setStatus("1");
			
			topupPayChannelBusiness.createMHHQRegister(hqRegister);
			
		}

		PrintWriter pw = response.getWriter();
		pw.print("SUCCESS");
		pw.close();
	}

	
	//同名卡开卡异步通知
	@RequestMapping(method = { RequestMethod.POST,
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/mhhq/opencard/notifyurl")
	public void OpenCardBack(HttpServletRequest request, HttpServletResponse response) throws Exception {

		LOG.info("同名卡开卡异步回调进来了======");
		
		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}
		
		String code = request.getParameter("code");
		String bankCard = request.getParameter("cardNo");
		String dsorderid = request.getParameter("dsorderid");
		
		if (code.equals("SUCCESS")) {
			
			MHHQBindCard hqBindCard = topupPayChannelBusiness.getMHHQBindCardByBankCard(bankCard);
			hqBindCard.setStatus("1");
			
			topupPayChannelBusiness.createMHHQBindCard(hqBindCard);
			
			LOG.info("同名卡开卡成功");
		}
		PrintWriter pw = response.getWriter();
		pw.print("SUCCESS");
		pw.close();

	}

	
	//同名卡交易异步通知
	@RequestMapping(method = { RequestMethod.POST,
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/mhhq/fastpay/notifyurl")
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
		
		String code = request.getParameter("code");
		String dsorderid = request.getParameter("dsorderid");
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(dsorderid);
		
		if ("SUCCESS".equalsIgnoreCase(code)) {
			LOG.info("同名卡交易成功");
			
			RestTemplate restTemplate = new RestTemplate();
			
			String url = prp.getIpAddress() + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("orderCode", dsorderid);
			requestEntity.add("version", "8");
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
