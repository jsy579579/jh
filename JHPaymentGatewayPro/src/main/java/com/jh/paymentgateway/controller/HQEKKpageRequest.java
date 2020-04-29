package com.jh.paymentgateway.controller;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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

import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.HQEBindCard;
import com.jh.paymentgateway.pojo.HQERegion;
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
public class HQEKKpageRequest {
	private static final Logger LOG = LoggerFactory.getLogger(HQEKKpageRequest.class);
	static ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setSerializationInclusion(Inclusion.NON_NULL);
	}

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;

	public final static String DATE_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
	private static String merchno = "xl2201811211124";
	private static String dskey = "eeecd8ec";
	private static String url = "http://pay.huanqiuhuiju.com/authsys/api/smart/repayment/execute.do";

	private static String url1 = "http://pay.huanqiuhuiju.com/authsys/api/auth/execute.do";

	// 绑卡接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqekk/bindCard")
	public @ResponseBody Object hqebindCard(@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "phone") String phone, @RequestParam(value = "userName") String userName,
			@RequestParam(value = "idCard") String idCard) throws Exception {

		HQEBindCard hqeBindCard = topupPayChannelBusiness.getHQEBindCardByBankCard(bankCard);
		if(hqeBindCard!=null &&hqeBindCard.getStatus().equals("1")) {
			return ResultWrap.init(CommonConstants.SUCCESS, "已成功鉴权绑卡");
		}
		CommonBean trans = new CommonBean();
		String disOrderId = "xl" + System.currentTimeMillis() + "";
		// 报文头
		trans.setMethodname("bindCard");
		trans.setTranscode("033");
		trans.setVersion("0100");
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setDsorderid(disOrderId);
		trans.setMerchno(merchno);
		// 业务参数
		trans.setUsername(userName);
		trans.setIdcard(idCard);
		trans.setBankcard(bankCard);
		trans.setCardType("2");
		trans.setMobile(phone);
		trans.setNotifyUrl(ip + "/v1.0/paymentgateway/topup/hqekk/opencard/notifyurl");
		trans.setReturnUrl(ip + "/v1.0/paymentgateway/topup/toght/bindcardsuccesspage");
		LOG.info("请求绑卡的报文======" + trans);
		String result = send(trans);
		LOG.info("请求绑卡返回的result======" + result);
		JSONObject jsonobj = JSONObject.fromObject(result);
		String returnCode = jsonobj.getString("returncode");
		String errtext = jsonobj.getString("errtext");
		if ("0000".equals(returnCode)) {
			String bindUrl = jsonobj.getString("bindUrl");
			if (hqeBindCard == null) {
				HQEBindCard hqBind = new HQEBindCard();
				hqBind.setPhone(phone);
				hqBind.setIdCard(idCard);
				hqBind.setBankCard(bankCard);
				hqBind.setOrderCode(disOrderId);
				hqBind.setStatus("0");
				topupPayChannelBusiness.createHQEBindCard(hqBind);
			} else {
				hqeBindCard.setOrderCode(disOrderId);
				hqeBindCard.setStatus("0");
				topupPayChannelBusiness.createHQEBindCard(hqeBindCard);
			}
			return ResultWrap.init("999996", "首次使用需进行绑卡授权,点击确定进入授权页面!", bindUrl);
		} else {
			if ("0001".equals(returnCode)) {
				if ("该卡已经签约，可直接发起交易".equals(errtext)) {
					String bindId = jsonobj.getString("bindId");
					if (hqeBindCard == null) {
						HQEBindCard hqBind = new HQEBindCard();
						hqBind.setPhone(phone);
						hqBind.setIdCard(idCard);
						hqBind.setBankCard(bankCard);
						hqBind.setOrderCode(disOrderId);
						hqBind.setBindId(bindId);
						hqBind.setStatus("1");
						topupPayChannelBusiness.createHQEBindCard(hqBind);
					} else {
						hqeBindCard.setOrderCode(disOrderId);
						hqeBindCard.setBindId(bindId);
						hqeBindCard.setStatus("1");
						topupPayChannelBusiness.createHQEBindCard(hqeBindCard);
					}
					return ResultWrap.init(CommonConstants.SUCCESS, "已成功鉴权绑卡");
				} else {
					return ResultWrap.init(CommonConstants.FALIED, errtext);
				}
			}else if("0099".equals(returnCode)) {
				String bindId = jsonobj.getString("bindId");
				if (hqeBindCard == null) {
					HQEBindCard hqBind = new HQEBindCard();
					hqBind.setPhone(phone);
					hqBind.setIdCard(idCard);
					hqBind.setBankCard(bankCard);
					hqBind.setOrderCode(disOrderId);
					hqBind.setBindId(bindId);
					hqBind.setStatus("1");
					topupPayChannelBusiness.createHQEBindCard(hqBind);
				} else {
					hqeBindCard.setOrderCode(disOrderId);
					hqeBindCard.setBindId(bindId);
					hqeBindCard.setStatus("1");
					topupPayChannelBusiness.createHQEBindCard(hqeBindCard);
				}
				
				return ResultWrap.init(CommonConstants.SUCCESS, "已成功鉴权绑卡");
			}
			return ResultWrap.init(CommonConstants.FALIED, errtext);
		}
	}

	// 支付预下单接口
	@SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqekk/prefastpay")
	public @ResponseBody Object hqePreFastPay(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "province") String province) {
		Map<String, String> map = new HashMap<String, String>();
		CommonBean trans = new CommonBean();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String realAmount = prp.getRealAmount();
		String amount = prp.getAmount();
		String bankCard = prp.getBankCard();
		String rate = prp.getRate();

		String fee = getUserFee1(rate, amount);

		HQEBindCard hqeBindCard = topupPayChannelBusiness.getHQEBindCardByBankCard(bankCard);

		String BigAmount = new BigDecimal(realAmount).multiply(new BigDecimal("100")).setScale(0).toString();
		String BigFee = new BigDecimal(fee).multiply(new BigDecimal("100")).setScale(0).toString();

		// 报文头
		trans.setMethodname("prePay");
		trans.setTranscode("033");
		trans.setVersion("0100");
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setDsorderid(orderCode);
		trans.setMerchno(merchno);
		// 业务参数
		trans.setBindId(hqeBindCard.getBindId());
		trans.setChannelType("1");
		trans.setAmount(BigAmount);
		trans.setUserFee(BigFee);
		trans.setBankcard(bankCard);
//		trans.setMcc(mcc);
		trans.setProvince(province);
		trans.setNotifyUrl(ip + "/v1.0/paymentgateway/topup/hqekk/fastpay/notifyurl");
		
		LOG.info("请求快捷支付预下单的报文======" + trans);
		
		String result = send(trans);
		
		LOG.info("请求快捷支付预下单返回的result======" + result);
		
		JSONObject jsonobj = JSONObject.fromObject(result);
		String returnCode = jsonobj.getString("returncode");
		String errtext = jsonobj.getString("errtext");
		if ("0000".equals(returnCode)) {
			map = (Map<String, String>) hqeConfirmFastPay(orderCode);
			return map;
		} else if ("0003".equals(returnCode))  {
			return ResultWrap.init("999998", errtext);
		}else {
			return ResultWrap.init(CommonConstants.FALIED, errtext);
		}

	}

	// 支付确认下单接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqekk/confirmfastpay")
	public @ResponseBody Object hqeConfirmFastPay(@RequestParam(value = "orderCode") String orderCode) {
		CommonBean trans = new CommonBean();

		// 报文头
		trans.setMethodname("confirmPay");
		trans.setTranscode("033");
		trans.setVersion("0100");
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setDsorderid(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setMerchno(merchno);
		// 业务参数
		trans.setChannelType("1");
		trans.setDeviceType("1");
		
		Random random = new Random();
		List<String> list = new ArrayList<String>();
		list.add("86");
		list.add("35");
		String str = list.get(random.nextInt(2));
		StringBuffer deviceId = new StringBuffer(str);
		for(int i=0; i<13; i++) {
			deviceId.append(random.nextInt(10));
		}
		
		trans.setDeviceId(deviceId + "");
		trans.setOriReqMsgId(orderCode);

		LOG.info("请求快捷支付确认下单的报文======" + trans);

		String result = send(trans);

		LOG.info("请求快捷支付确认下单返回的result======" + result);

		JSONObject jsonobj = JSONObject.fromObject(result);

		String returnCode = jsonobj.getString("returncode");
		String errtext = jsonobj.getString("errtext");

		if ("0000".equals(returnCode) || "0003".equals(returnCode)) {

			return ResultWrap.init("999998", "等待银行扣款中");
		} else {

			return ResultWrap.init(CommonConstants.FALIED, errtext);
		}

	}

	// 借款提现
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqekk/transfer")
	public @ResponseBody Object transfer(@RequestParam(value = "orderCode") String orderCode) {

		CommonBean trans = new CommonBean();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String realAmount = prp.getRealAmount();
		String bankCard = prp.getBankCard();
		String phone = prp.getCreditCardPhone();
		String extraFee = prp.getExtraFee();

		HQEBindCard hqeBindCard = topupPayChannelBusiness.getHQEBindCardByBankCard(bankCard);

		BigDecimal BigAmount = new BigDecimal(realAmount).multiply(new BigDecimal("100")).setScale(0);
//		BigDecimal BigExtraFee = new BigDecimal(extraFee).multiply(new BigDecimal("100")).setScale(0);
		BigDecimal BigExtraFee = new BigDecimal("0").multiply(new BigDecimal("100")).setScale(0);

		// 报文头
		trans.setMethodname("withDraw");		
		trans.setTranscode("033");
		trans.setVersion("0100");
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setDsorderid(orderCode);
		trans.setMerchno(merchno);
		
		trans.setBindId(hqeBindCard.getBindId());
		trans.setAmount(BigAmount+ "");
		trans.setUserFee(BigExtraFee + "");
		trans.setBankcard(bankCard);
		trans.setChannelType("1");
		trans.setMobile(phone);

		trans.setNotifyUrl(ip + "/v1.0/paymentgateway/topup/hqekk/transfer/notifyurl");

		LOG.info("发起代付的请求报文======" + trans);

		String result = send(trans);

		LOG.info("请求代付返回的result======" + result);

		JSONObject jsonobj = JSONObject.fromObject(result);

		String returnCode = jsonobj.getString("returncode");
		String errtext = jsonobj.getString("errtext");
		if ("0000".equals(returnCode) || "0003".equals(returnCode)) {

			return ResultWrap.init("999998", "等待银行扣款中");
		} else {

			return ResultWrap.init(CommonConstants.FALIED, errtext);
		}

	}
	
	
	// 人工借款提现
		@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqekk/transfer/manual")
		public @ResponseBody Object transferManual(@RequestParam(value = "orderCode") String orderCode,
				@RequestParam(value = "real_amount") String realAmount,
				@RequestParam(value = "bank_card") String bankCard,
				@RequestParam(value = "phone") String phone,
				@RequestParam(value = "extra_fee") String extraFee
				) {

			CommonBean trans = new CommonBean();
			HQEBindCard hqeBindCard = topupPayChannelBusiness.getHQEBindCardByBankCard(bankCard);
			BigDecimal BigAmount = new BigDecimal(realAmount).multiply(new BigDecimal("100")).setScale(0);
//			BigDecimal BigExtraFee = new BigDecimal(extraFee).multiply(new BigDecimal("100")).setScale(0);
			BigDecimal BigExtraFee = new BigDecimal("0").multiply(new BigDecimal("100")).setScale(0);

			// 报文头
			trans.setMethodname("withDraw");		
			trans.setTranscode("033");
			trans.setVersion("0100");
			trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
			trans.setDsorderid(orderCode);
			trans.setMerchno(merchno);
			
			trans.setBindId(hqeBindCard.getBindId());
			trans.setAmount(BigAmount+ "");
			trans.setUserFee(BigExtraFee + "");
			trans.setBankcard(bankCard);
			trans.setChannelType("1");
			trans.setMobile(phone);

			trans.setNotifyUrl(ip + "/v1.0/paymentgateway/topup/hqekk/transfer/notifyurl");

			LOG.info("发起代付的请求报文======" + trans);

			String result = send(trans);

			LOG.info("请求代付返回的result======" + result);

			JSONObject jsonobj = JSONObject.fromObject(result);

			String returnCode = jsonobj.getString("returncode");
			String errtext = jsonobj.getString("errtext");
			if ("0000".equals(returnCode) || "0003".equals(returnCode)) {

				return ResultWrap.init("999998", "等待银行扣款中");
			} else {

				return ResultWrap.init(CommonConstants.FALIED, errtext);
			}

		}
	

	// 代付的异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqekk/transfer/notifyurl")
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
		String orderid = request.getParameter("orderid");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(dsorderid);

		if (status.equals("00")) {
			LOG.info("交易成功");

			RestTemplate restTemplate = new RestTemplate();

			String url = prp.getIpAddress() + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();

			url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
			//url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", dsorderid);
			requestEntity.add("third_code", orderid);
			String result = null ;
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("",e);
			}

			LOG.info("订单状态修改成功===================" + dsorderid + "====================" + result);

			LOG.info("订单已代付!");

		}

		PrintWriter pw = response.getWriter();
		pw.print("SUCCESS");
		pw.close();
	}

	// 借款
	@SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqekk/loan")
	public @ResponseBody Object loan(
			@RequestParam(value = "orderCode") String orderCode
			) throws Exception {
		CommonBean trans = new CommonBean();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		if (prp == null) {
			return ResultWrap.init(CommonConstants.FALIED, "请求支付失败");
		}
		String realAmount = prp.getRealAmount();
		String bankCard = prp.getBankCard();
		HQEBindCard hqeBindCard = topupPayChannelBusiness.getHQEBindCardByBankCard(bankCard);
		BigDecimal BigAmount = new BigDecimal(realAmount).multiply(new BigDecimal("100")).setScale(0);

		// 报文头
		trans.setMethodname("loan");
		trans.setTranscode("033");
		trans.setVersion("0100");
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setDsorderid(orderCode+"loan");
		trans.setMerchno(merchno);
		trans.setBindId(hqeBindCard.getBindId());
		trans.setAmount(BigAmount+ "");
		trans.setNotifyUrl(ip + "/v1.0/paymentgateway/topup/hqekk/transfer/notifyurl");

		LOG.info("发起借款的请求报文======" + trans);

		String result = send(trans);

		LOG.info("请求借款返回的result======" + result);

		JSONObject jsonobj = JSONObject.fromObject(result);

		String returnCode = jsonobj.getString("returncode");
		String errtext = jsonobj.getString("errtext");
		if ("0000".equals(returnCode) ) {
			 Map<String, Object> transfer=  ( Map<String, Object>)this.transfer(orderCode);
			return transfer;
		} else if("0003".equals(returnCode)) {
			return ResultWrap.init("999998", errtext);
		}else {
			return ResultWrap.init(CommonConstants.FALIED, errtext);
			
		}


	}
	
	// 交易查询接口
	/**
	 * 89	借款
	 * 90	借款冲回
	 * 91	借款提现
	*/
	@SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqekk/orderquery")
	public @ResponseBody Object dealQuery(
			@RequestParam(value = "orderCode") String orderCode
			) throws Exception {
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		if (prp == null) {
			return ResultWrap.init(CommonConstants.FALIED, "请求支付失败");
		}
		String orderType=prp.getOrderType();
		if(orderType.equals("11")) {
			orderType="91";
		}else if(orderType.equals("10")) {
			orderType="90";
		}else {
			orderType="89";
		}
		// 报文头
		Map<String, Object>  dealQuery=this.dealQuery(orderCode, orderType);
		String returncode = (String) dealQuery.get(CommonConstants.RESP_CODE);
		String errtext = (String) dealQuery.get(CommonConstants.RESP_MESSAGE);
		if(orderType.equals("90")&&returncode.equals("99")) {
			dealQuery=this.dealQuery(orderCode+"loan", "89");
			returncode = (String) dealQuery.get(CommonConstants.RESP_CODE);
			errtext = (String) dealQuery.get(CommonConstants.RESP_MESSAGE);
			if(returncode.equals("00")) {
				Map<String, String> map = (Map<String, String>) this.transfer(orderCode);
				return map;	
			}
		}
		if(returncode.equals("00")) {
			return ResultWrap.init(CommonConstants.SUCCESS, errtext);
		}else if(returncode.equals("02")||returncode.equals("04")) {
			return ResultWrap.init(CommonConstants.FALIED, errtext);
		}else {
			return ResultWrap.init("999998", errtext);
		}
	}
	// 交易查询接口
	@SuppressWarnings("unchecked")
	public  Map<String, Object> dealQuery( String orderCode,String transType) throws Exception{
		
		CommonBean trans = new CommonBean();
		// 报文头
		trans.setTranscode("902");
		trans.setVersion("0100");
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setMerchno(merchno);
		trans.setDsorderid(orderCode);
		trans.setTranstype(transType);
		String resp = TransUtil.object2String(trans);
		Map<String, String> resMap = mapper.readValue(resp, Map.class);
		String sign = SignUtil.getSign(resMap, dskey);
		trans.setSign(sign);
		String result = send1(trans);
		LOG.info("请求订单查询返回的result======" + result);
		
		JSONObject jsonobj = JSONObject.fromObject(result);
		
		String returncode = jsonobj.getString("returncode");
		String errtext = jsonobj.getString("message");
		if(returncode.equals("0000")) {
			String status = jsonobj.getString("status");
			return ResultWrap.init(status, errtext);
		}else {
			return ResultWrap.init(returncode, errtext);
		}
		
	}
	// 余额查询接口
		@SuppressWarnings("unchecked")
		@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqekk/balancequery")
		public @ResponseBody Object balanceQuery(
				@RequestParam(value = "bankCard") String bankCard
				) throws Exception {

			HQEBindCard hqeBindCardByBankCard = topupPayChannelBusiness.getHQEBindCardByBankCard(bankCard);

			CommonBean trans = new CommonBean();
			// 报文头
			trans.setMethodname("queryBalance");
			trans.setTranscode("033");
			trans.setVersion("0100");
			trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
			trans.setDsorderid(UUID.randomUUID().toString().replaceAll("-", ""));
			trans.setMerchno(merchno);
			trans.setBindId(hqeBindCardByBankCard.getBindId());
			String resp = TransUtil.object2String(trans);
			Map<String, String> resMap = mapper.readValue(resp, Map.class);
			String sign = SignUtil.getSign(resMap, dskey);
			trans.setSign(sign);

			String result = send(trans);

			LOG.info("请求余额查询返回的result======" + result);

			JSONObject jsonobj = JSONObject.fromObject(result);

			String returncode = jsonobj.getString("returncode");
			String errtext = jsonobj.getString("errtext");
			if ("0000".equals(returncode)) {
				
				String balanceAmount = jsonobj.getString("balanceAmount");
				String freezeAmount = jsonobj.getString("freezeAmount");
				String loanAcBal = jsonobj.getString("loanAcBal");
				String loanFrozBal = jsonobj.getString("loanFrozBal");
				String loanQuota = jsonobj.getString("loanQuota");
				String loanUsebal = jsonobj.getString("loanUsebal");

				return ResultWrap.init(CommonConstants.SUCCESS, 
						"余额为： " + balanceAmount +
						" \n 冻结余额为：" + freezeAmount+
						" \n 借款账户余额：" + loanAcBal+
						" \n 借款冻结金额：" + loanFrozBal+
						" \n 借款额度：" + loanQuota+
						" \n 借款实际可用余额：" + loanUsebal
						,result);
			} else {

				return ResultWrap.init(CommonConstants.FALIED, errtext);
			}

		}

	// 代理商余额查询接口
	@SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqekk/allbalancequery")
	public @ResponseBody Object allbalanceQuery(
			) throws Exception {
		CommonBean trans = new CommonBean();
		// 报文头
		trans.setTranscode("904");
		trans.setVersion("0100");
		trans.setOrdersn(UUID.randomUUID().toString().replaceAll("-", ""));
		trans.setMerchno(merchno);
		trans.setDsorderid(UUID.randomUUID().toString().replaceAll("-", ""));
		String resp = TransUtil.object2String(trans);
		Map<String, String> resMap = mapper.readValue(resp, Map.class);
		String sign = SignUtil.getSign(resMap, dskey);
		trans.setSign(sign);
		String result = send1(trans);
		LOG.info("请求余额查询返回的result======" + result);
		JSONObject jsonobj = JSONObject.fromObject(result);
		String returncode = jsonobj.getString("returncode");
		String errtext = jsonobj.getString("errtext");
		if ("0000".equals(returncode)) {
			String currAccountBalance = jsonobj.getString("currAccountBalance");

			return ResultWrap.init(CommonConstants.SUCCESS, "余额为： " + currAccountBalance );
		} else {

			return ResultWrap.init(CommonConstants.FALIED, errtext);
		}

	}

	// 绑卡异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqekk/opencard/notifyurl")
	public void OpenCardBack(HttpServletRequest request, HttpServletResponse response) throws Exception {

		LOG.info("绑卡异步回调进来了======");

		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}

		String dsorderid = request.getParameter("dsorderid");
		String status = request.getParameter("status");
		String bindId = request.getParameter("bindId");

		if (status.equals("00")) {

			HQEBindCard hqeBindCardByOrderCode = topupPayChannelBusiness.getHQEBindCardByOrderCode(dsorderid);
			hqeBindCardByOrderCode.setBindId(bindId);
			hqeBindCardByOrderCode.setStatus("1");

			topupPayChannelBusiness.createHQEBindCard(hqeBindCardByOrderCode);

			LOG.info("同名卡开卡成功");
		}
		PrintWriter pw = response.getWriter();
		pw.print("SUCCESS");
		pw.close();

	}

	// 同名卡交易异步通知
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqekk/fastpay/notifyurl")
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
		String orderid = request.getParameter("orderid");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(dsorderid);

		if ("00".equalsIgnoreCase(status)) {
			LOG.info("同名卡交易成功");

			RestTemplate restTemplate = new RestTemplate();

			String result = null;

			String url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
			//String url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

			MultiValueMap<String, String>  requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", dsorderid);
			requestEntity.add("third_code", orderid);
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("",e);
			}

			LOG.info("订单状态修改成功===================" + dsorderid + "====================" + result);

			LOG.info("订单已支付!");

		}

		PrintWriter pw = response.getWriter();
		pw.print("SUCCESS");
		pw.close();
	}

	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqekk/queryregion")
	public @ResponseBody Object hqeQueryRegion(@RequestParam(value = "parentId", required = false, defaultValue = "1") String parentId
			) throws Exception {
		
		List<HQERegion> hqeRegionByParentId = topupPayChannelBusiness.getHQERegionByParentId(parentId);
		
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", hqeRegionByParentId);
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

}
