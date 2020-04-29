package com.jh.paymentgateway.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.HQHBindCard;
import com.jh.paymentgateway.pojo.HQHRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.hqg.CommonBean;
import com.jh.paymentgateway.util.hqg.CommonUtil;
import com.jh.paymentgateway.util.hqg.JsonUtils;
import com.jh.paymentgateway.util.hqg.SignUtil;
import com.jh.paymentgateway.util.hqg.TransUtil;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class HQHpageRequest extends BaseChannel {
	private static final Logger LOG = LoggerFactory.getLogger(HQHpageRequest.class);
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
	private static String merchno = "sl2018080218221";
	private static String dskey = "043b1eaa";
	private static String url = "http://pay.huanqiuhuiju.com/authsys/api/xh/pay/execute.do";
	private static String url1 = "http://pay.huanqiuhuiju.com/authsys/api/auth/execute.do";

	// 跟还款对接的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqh/torepayment")
	public @ResponseBody Object HLJCRegister(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "bankCard") String bankCard, 
			@RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "phone") String phone, 
			@RequestParam(value = "userName") String userName,
			@RequestParam(value = "cardType") String cardType, 
			@RequestParam(value = "rate") String rate,
			@RequestParam(value = "extraFee") String extraFee, 
			@RequestParam(value = "bankName") String bankName)
					throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();

		HQHRegister hqRegister = topupPayChannelBusiness.getHQHRegisterByIdCard(idCard);

		HQHBindCard hqBindCard = topupPayChannelBusiness.getHQHBindCardByBankCard(bankCard);

		if (hqRegister == null) {
			map = (Map<String, Object>) this.Register(bankCard, idCard, phone, userName, bankName, rate, extraFee);
			if (!"000000".equals(map.get("resp_code"))) {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, map.get("resp_message"));
				return map;
			}
		}
		if (hqBindCard == null) {
			map=ResultWrap.init("999996", "需要绑卡", ip + "/v1.0/paymentgateway/topup/hqh/jump/bindcard?bankCard="+bankCard
					+ "&idCard=" + idCard
					+ "&phone=" + phone 
					+ "&userName=" + URLEncoder.encode(userName, "UTF-8")
					+ "&bankName=" + URLEncoder.encode(bankName, "UTF-8"));
			return map;
		}
			
		if (!hqBindCard.getStatus().equals("1")) {
			map=ResultWrap.init("999996", "需要绑卡", ip + "/v1.0/paymentgateway/topup/hqh/jump/bindcard?bankCard="+bankCard
					+ "&idCard=" + idCard
					+ "&phone=" + phone 
					+ "&userName=" + URLEncoder.encode(userName, "UTF-8")
					+ "&bankName=" + URLEncoder.encode(bankName, "UTF-8"));
			return map;
		}
		return ResultWrap.init(CommonConstants.SUCCESS, "已完成鉴权验证!");
	}

	// 开户接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqh/registet")
	public @ResponseBody Object Register(@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "phone") String phone,
			@RequestParam(value = "userName") String userName, 
			@RequestParam(value = "bankName") String bankName,
			@RequestParam(value = "rate") String rate, 
			@RequestParam(value = "extraFee") String extraFee) {
		
		Map<String, Object> maps = new HashMap<String, Object>();
		HQHRegister hqRegister = topupPayChannelBusiness.getHQHRegisterByIdCard(idCard);

		CommonBean trans = new CommonBean();

		trans.setMethodname("register");
		trans.setTranscode("037");
		trans.setVersion("0100");
		String orderSn = UUID.randomUUID().toString().replaceAll("-", "");
		trans.setOrdersn(orderSn);// 流水号
		String merchantOrder = UUID.randomUUID().toString().replaceAll("-", "");
		trans.setDsorderid(merchantOrder);// 商户订单号
		trans.setMerchno(merchno);
		trans.setUsername(userName);
		trans.setIdcard(idCard);

		LOG.info("进件请求报文======" + trans);

		String result = send(trans);

		LOG.info("请求进件返回的result======" + result);

		JSONObject jsonobj = JSONObject.fromObject(result);
		String returncode = jsonobj.getString("returncode");
		String errtext = jsonobj.getString("errtext");

		if ("0000".equals(returncode)) {
			String subMerchantNo = jsonobj.getString("subMerchantNo");

			HQHRegister HQHRegister = new HQHRegister();
			HQHRegister.setBankCard(bankCard);
			HQHRegister.setIdCard(idCard);
			HQHRegister.setPhone(phone);
			HQHRegister.setRate(rate);
			HQHRegister.setExtraFee(extraFee);
			HQHRegister.setMerchantCode(subMerchantNo);

			topupPayChannelBusiness.createHQHRegister(HQHRegister);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "进件成功");
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, errtext);
		}
		return maps;
	}
	
	// 绑卡接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqh/bindCard")
	public @ResponseBody Object HQHBindCard(
			@RequestParam(value = "bankCard") String bankCard, 
			@RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "phone") String phone, 
			@RequestParam(value = "userName") String userName) {
		Map<String, Object> maps = new HashMap<String, Object>();
		HQHBindCard hqxgmBindCard = topupPayChannelBusiness.getHQHBindCardByBankCard(bankCard);
		HQHRegister hqRegister = topupPayChannelBusiness.getHQHRegisterByIdCard(idCard);

		CommonBean trans = new CommonBean();

		trans.setMethodname("bindCard");
		trans.setTranscode("037");
		trans.setVersion("0100");
		String orderSn = UUID.randomUUID().toString().replaceAll("-", "");
		trans.setOrdersn(orderSn);// 流水号
		String merchantOrder = UUID.randomUUID().toString().replaceAll("-", "");
		trans.setDsorderid(merchantOrder);// 商户订单号
		trans.setMerchno(merchno);
		trans.setSubMerchantNo(hqRegister.getMerchantCode());
		trans.setBankcard(bankCard);
		trans.setUsername(userName);
		trans.setIdcard(idCard);
		trans.setMobile(phone);
		String returnUrl = ip + "/v1.0/paymentgateway/topup/hqh/bindcard/front";
		trans.setReturnUrl(returnUrl);// 绑卡请求同步
		String notifyUrl = ip + "/v1.0/paymentgateway/topup/hqh/opencard/notifyurl";
		trans.setNotifyUrl(notifyUrl);// 绑卡请求异步

		LOG.info("绑卡请求报文======" + trans);

		String result = send(trans);

		LOG.info("请求绑卡返回的result======" + result);

		JSONObject jsonobj = JSONObject.fromObject(result);
		String returncode = jsonobj.getString("returncode");
		String errtext = jsonobj.getString("errtext");
		if ("0000".equals(returncode)) {
			if (hqxgmBindCard==null) {
				hqxgmBindCard = new HQHBindCard();
				hqxgmBindCard.setBankCard(bankCard);
				hqxgmBindCard.setIdCard(idCard);
				hqxgmBindCard.setMerchantOrder(merchantOrder);
				hqxgmBindCard.setPhone(phone);
				hqxgmBindCard.setStatus("0");
				hqxgmBindCard.setCreateTime(new Date());
				topupPayChannelBusiness.createHQHBindCard(hqxgmBindCard);
			}

			String bindUrl = jsonobj.getString("bindUrl");
			
			/* 找出指定的2个字符在 该字符串里面的 位置 */
			String strStart = "<body>";
			String strEnd = "</body>";
			int strStartIndex = bindUrl.indexOf(strStart);
			int strEndIndex = bindUrl.indexOf(strEnd);
			String html = bindUrl.substring(strStartIndex + 6, strEndIndex);
			System.out.println("截取后的form开始--------------------------");
			System.out.println(html);
			System.out.println("截取后的form结束--------------------------");
			
			LOG.info("小花猫环球跳转绑卡页面 ===========================");
			
			maps.put("pageContent", html);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "申请绑卡成功");
		}else if("该卡已绑定成功".equals(errtext)){
			LOG.info("小花猫环球卡第二次绑卡 ===========================");
			String bindId = jsonobj.getString("bindId");
			hqxgmBindCard.setBindId(bindId);
			hqxgmBindCard.setStatus("1");
			topupPayChannelBusiness.createHQHBindCard(hqxgmBindCard);
			
			maps.put(CommonConstants.RESULT, ip + "/v1.0/paymentgateway/topup/hqh/bindcard/front");
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, errtext);
		} else {
			LOG.info("小花猫环球申请绑卡异常 ===========================");
			
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, errtext);
		}
		return maps;
	}
	
	// 解除绑卡
		@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqh/Relieve")
		public @ResponseBody Object Relieve(@RequestParam(value = "bankCard") String bankCard,
				@RequestParam(value = "idCard") String idCard) {
			
			Map<String, Object> maps = new HashMap<String, Object>();
			HQHRegister hqRegister = topupPayChannelBusiness.getHQHRegisterByIdCard(idCard);
			HQHBindCard hqxgmBindCard = topupPayChannelBusiness.getHQHBindCardByBankCard(bankCard);
			
			CommonBean trans = new CommonBean();

			trans.setMethodname("unBindCard");
			trans.setTranscode("037");
			trans.setVersion("0100");
			String orderSn = UUID.randomUUID().toString().replaceAll("-", "");
			trans.setOrdersn(orderSn);// 流水号
			String merchantOrder = UUID.randomUUID().toString().replaceAll("-", "");
			trans.setDsorderid(merchantOrder);// 商户订单号
			trans.setMerchno(merchno);
			trans.setSubMerchantNo(hqRegister.getMerchantCode());
			trans.setBankcard(bankCard);
			trans.setBindId(hqxgmBindCard.getBindId());;

			LOG.info("解除绑卡请求报文======" + trans);

			String result = send(trans);

			LOG.info("解除绑卡返回的result======" + result);

			JSONObject jsonobj = JSONObject.fromObject(result);
			String returncode = jsonobj.getString("returncode");
			String errtext = jsonobj.getString("errtext");

			if ("0000".equals(returncode)) {
				hqxgmBindCard.setStatus("0");
				topupPayChannelBusiness.createHQHBindCard(hqxgmBindCard);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "解除绑卡成功");
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, errtext);
			}
			return maps;
		}
	
	/**
	 * 中转页面
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/hqh/jump/bindcard")
	public String jumpPay(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		LOG.info("跳转到到账卡页面-----------------");
		//        
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		
		String bankCard = request.getParameter("bankCard");
		String idCard = request.getParameter("idCard");
		String phone = request.getParameter("phone");
		String userName = request.getParameter("userName");
		String bankName = request.getParameter("bankName");

		model.addAttribute("bankCard", bankCard);
		model.addAttribute("idCard", idCard);
		model.addAttribute("phone", phone);
		model.addAttribute("userName", userName);
		model.addAttribute("bankName", bankName);
		model.addAttribute("ipAddress", ip);

		return "hqhbindcard";
	}

	// 消费接口
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/hqh/fastpay")
	public @ResponseBody Object fastpay(HttpServletRequest request,
			@RequestParam(value = "orderCode") String orderCode) {
		Map<String, Object> maps = new HashMap<String, Object>();
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();
		String amount = prp.getAmount();
		String phone = prp.getCreditCardPhone();
		String rate = prp.getRate();
		
		HQHBindCard hqxgmBindCard = topupPayChannelBusiness.getHQHBindCardByBankCard(bankCard);
		HQHRegister HQHRegister = topupPayChannelBusiness.getHQHRegisterByIdCard(idCard);

		CommonBean trans = new CommonBean();

		trans.setMethodname("pay");
		trans.setTranscode("037");
		trans.setVersion("0100");
		String orderSn = UUID.randomUUID().toString().replaceAll("-", "");
		trans.setOrdersn(orderSn);// 流水号
		trans.setDsorderid(orderCode);// 商户订单号
		trans.setMerchno(merchno);
		trans.setSubMerchantNo(HQHRegister.getMerchantCode());
		
		String Amount = new BigDecimal(amount).multiply(new BigDecimal(100)).toString();
		String relAmount = Amount.substring(0 , Amount.lastIndexOf("."));
		trans.setAmount(relAmount);// 单位分
		
		Double a = Double.valueOf(amount);
		Double r = Double.valueOf(rate);
		String Fee = String.valueOf(a*r);
		BigDecimal fee = new BigDecimal("0.00");
		BigDecimal f =  new BigDecimal(Fee).subtract(fee).setScale(2, BigDecimal.ROUND_HALF_DOWN);
		f =  new BigDecimal(f.toString()).multiply(new BigDecimal(100));
		String F = f.toString();
		F = F.substring(0 , F.lastIndexOf("."));
		LOG.info("交易手续费" + F + "：单位分");
		trans.setUserFee(F);// 交易手续费
		
		trans.setBankcard(bankCard);
		trans.setMobile(phone);
		trans.setBindId(hqxgmBindCard.getBindId());;

		String userIP=getRemoteIP(request);
		String deviceId= getMACAddress(userIP);
		if(deviceId.length()==0){
			deviceId=userIP;
		}
		trans.setDeviceId(deviceId);
		trans.setDeviceType("1");
		trans.setUserIP(userIP);  
		
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i<10; i++) {
			int nextInt = random.nextInt(10);
			sb.append(nextInt);
		}
		String userId = sb.toString();
		trans.setUserId(userId);  
		
		String notifyUrl = ip + "/v1.0/paymentgateway/topup/hqh/fastpay/notifyurl";
		trans.setNotifyUrl(notifyUrl);// 交易异步

		LOG.info("交易请求报文======" + trans);

		String result = send(trans);

		LOG.info("请求交易返回的result======" + result);

		JSONObject jsonobj = JSONObject.fromObject(result);
		String returncode = jsonobj.getString("returncode");
		String errtext = jsonobj.getString("errtext");
		if ("0000".equals(returncode)) {
			LOG.info("小花猫环球交易成功===========================");

			maps.put(CommonConstants.RESP_CODE, "999998");
			maps.put(CommonConstants.RESP_MESSAGE, "等待银行扣款中");
		} else if("0003".equals(returncode)){
			LOG.info("小花猫环球交易处理中 ===========================");
			
			maps.put(CommonConstants.RESP_CODE, "999998");
			maps.put(CommonConstants.RESP_MESSAGE, "等待银行扣款中");
		} else if("0001".equals(returncode)){
			LOG.info("小花猫环球交易失败 ===========================");
			
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, errtext);
		}
		return maps;
	}
	
	// 代付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqh/transfer")
	public @ResponseBody Object transfer(HttpServletRequest request,
			@RequestParam(value = "orderCode") String orderCode) {
		Map<String, Object> maps = new HashMap<String, Object>();
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();
		String realamount = prp.getRealAmount();
		String phone = prp.getCreditCardPhone();
		String extraFee = prp.getExtraFee();
		
		HQHBindCard hqxgmBindCard = topupPayChannelBusiness.getHQHBindCardByBankCard(bankCard);
		HQHRegister HQHRegister = topupPayChannelBusiness.getHQHRegisterByIdCard(idCard);

		CommonBean trans = new CommonBean();

		trans.setMethodname("withDraw");
		trans.setTranscode("037");
		trans.setVersion("0100");
		String orderSn = UUID.randomUUID().toString().replaceAll("-", "");
		trans.setOrdersn(orderSn);// 流水号
		trans.setDsorderid(orderCode);// 商户订单号
		trans.setMerchno(merchno);
		trans.setSubMerchantNo(HQHRegister.getMerchantCode());
		trans.setBindId(hqxgmBindCard.getBindId());;
		
		String Amount = new BigDecimal(realamount).multiply(new BigDecimal(100)).toString();
		String relAmount = Amount.substring(0 , Amount.lastIndexOf("."));
		trans.setAmount(relAmount);// 单位分
		
		String fee = new BigDecimal(extraFee).multiply(new BigDecimal(100)).toString();
		fee = fee.substring(0 , fee.lastIndexOf("."));
		LOG.info("环球H代付手续费  + " + fee + "单位分");
		trans.setUserFee(fee);// 提现手续费
		
		trans.setBankcard(bankCard);
		/*
		String notifyUrl = ip + "/v1.0/paymentgateway/topup/hqh/fastpay/notifyurl";
		trans.setNotifyUrl(notifyUrl);// 交易异步
		 */
		LOG.info("代付请求报文======" + trans);

		String result = send(trans);

		LOG.info("请求代付返回的result======" + result);

		JSONObject jsonobj = JSONObject.fromObject(result);
		String returncode = jsonobj.getString("returncode");
		String errtext = jsonobj.getString("errtext");
		if ("0000".equals(returncode)) {
			LOG.info("小花猫环球代付成功===========================");

			maps.put(CommonConstants.RESP_CODE, "999998");
			maps.put(CommonConstants.RESP_MESSAGE, "等待银行扣款中");
		} else if("0003".equals(returncode)){
			LOG.info("小花猫环球代付处理中 ===========================");
			
			maps.put(CommonConstants.RESP_CODE, "999998");
			maps.put(CommonConstants.RESP_MESSAGE, "等待银行扣款中");
		} else if("0001".equals(returncode)){
			LOG.info("小花猫环球代付失败 ===========================");
			
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, errtext);
		}
		return maps;
	}

	// 余额查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqh/balancequery")
	public @ResponseBody Object balanceQuery(@RequestParam(value = "idCard") String idCard) throws Exception {
		Map<String, Object> maps = new HashMap<String, Object>();
		HQHRegister HQHRegister = topupPayChannelBusiness.getHQHRegisterByIdCard(idCard);

		CommonBean trans = new CommonBean();

		trans.setMethodname("queryBalance");
		trans.setTranscode("037");
		trans.setVersion("0100");
		String orderSn = UUID.randomUUID().toString().replaceAll("-", "");
		trans.setOrdersn(orderSn);// 流水号
		String merchantOrder = UUID.randomUUID().toString().replaceAll("-", "");
		trans.setDsorderid(merchantOrder);// 商户订单号
		trans.setMerchno(merchno);
		trans.setSubMerchantNo(HQHRegister.getMerchantCode());
		
		LOG.info("余额请求报文======" + trans);

		String result = send(trans);

		LOG.info("请求余额返回的result======" + result);

		JSONObject jsonobj = JSONObject.fromObject(result);
		String returncode = jsonobj.getString("returncode");
		String errtext = jsonobj.getString("errtext");
		if ("0000".equals(returncode)) {
			String balanceAmount = jsonobj.getString("balanceAmount");
			/*String d0Balance = jsonobj.getString("d0Balance");
			String t1Balance = jsonobj.getString("t1Balance");*/
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "商户余额为： " + balanceAmount);
		} else {
			return ResultWrap.init(CommonConstants.FALIED, "查询失败");
		}
		return maps;
	}

	// 代付查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqh/queryPutStatus")
	public @ResponseBody Object hqhQueryOrderPut(@RequestParam(value = "orderCode") String orderCode) throws Exception {
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String idCard = prp.getIdCard();
		// 107:交易   108:代付
		String transtype = "108";
		Map<String, Object> maps = new HashMap<String, Object>();
		HQHRegister HQHRegister = topupPayChannelBusiness.getHQHRegisterByIdCard(idCard);

		CommonBean trans = new CommonBean();

		trans.setTranscode("902");
		trans.setVersion("0100");
		String orderSn = UUID.randomUUID().toString().replaceAll("-", "");
		trans.setOrdersn(orderSn);// 流水号
		trans.setMerchno(merchno);
		trans.setDsorderid(orderCode);// 商户订单号
		trans.setTranstype(transtype);
		
		LOG.info("订单查询请求报文======" + trans);

		String result = send1(trans);

		LOG.info("请求订单查询返回的result======" + result);

		JSONObject jsonobj = JSONObject.fromObject(result);
		String returncode = jsonobj.getString("returncode");
		String errtext = jsonobj.getString("errtext");
		String status = jsonobj.getString("status");
		if ("0000".equals(returncode) && status.equals("00")) {
			LOG.info("环球H查询代付成功");
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, errtext);
		} else if ("0000".equals(returncode) && status.equals("01")){
			maps.put(CommonConstants.RESP_CODE, "999998");
			maps.put(CommonConstants.RESP_MESSAGE, "处理中");
		} else if ("0000".equals(returncode) && status.equals("99")){
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "订单号不存在");
		} else if ("0000".equals(returncode) && status.equals("04")){
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "订单关闭");
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "失败");
		}
		return maps;	
	}
	
	// 交易查询接口
		@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqh/orderquery")
		public @ResponseBody Object hqhQueryOrderPost(@RequestParam(value = "orderCode") String orderCode) throws Exception {
			PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
			String idCard = prp.getIdCard();
			// 107:交易   108:代付
			String transtype = "107";
			Map<String, Object> maps = new HashMap<String, Object>();
			HQHRegister HQHRegister = topupPayChannelBusiness.getHQHRegisterByIdCard(idCard);

			CommonBean trans = new CommonBean();

			trans.setTranscode("902");
			trans.setVersion("0100");
			String orderSn = UUID.randomUUID().toString().replaceAll("-", "");
			trans.setOrdersn(orderSn);// 流水号
			trans.setMerchno(merchno);
			trans.setDsorderid(orderCode);// 商户订单号
			trans.setTranstype(transtype);
			
			LOG.info("交易订单查询请求报文======" + trans);

			String result = send1(trans);

			LOG.info("请求交易订单查询返回的result======" + result);

			JSONObject jsonobj = JSONObject.fromObject(result);
			String returncode = jsonobj.getString("returncode");
			String errtext = jsonobj.getString("errtext");
			String status = jsonobj.getString("status");
			if ("0000".equals(returncode) && status.equals("00")) {
				LOG.info("环球H查询交易成功");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, errtext);
			} else if ("0000".equals(returncode) && status.equals("01")){
				maps.put(CommonConstants.RESP_CODE, "999998");
				maps.put(CommonConstants.RESP_MESSAGE, "处理中");
			} else if ("0000".equals(returncode) && status.equals("99")){
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "订单号不存在");
			} else if ("0000".equals(returncode) && status.equals("04")){
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "订单关闭");
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "失败");
			}
			return maps;	
		}

	/***
	 * 绑卡请求同步
	 **/
	@RequestMapping(method = { RequestMethod.POST,
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/hqh/bindcard/front")
	public String BindCardFrontGET(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("HQHBindCardFrontGET -------------------------------------------");
		return "bqdhbindcardsuccess";
	}

	// 同名卡开卡异步通知
	@RequestMapping(method = { RequestMethod.POST,
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/hqh/opencard/notifyurl")
	public void OpenCardBack(HttpServletRequest request, HttpServletResponse response) throws Exception {

		LOG.info("同名卡开卡异步回调进来了======");

		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		Map<String, Object> maps = new HashMap<String, Object>();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
				maps.put(key, s);
			}
		}
		
		String status = request.getParameter("status");
		String dsorderid = request.getParameter("dsorderid");
		
		String bindId = (String) maps.get("bindId");
	
		HQHBindCard HQHBindCard = topupPayChannelBusiness.getHQHBindCardbyMerchantOrder(dsorderid);
		if (status.equals("00")) {
			HQHBindCard.setStatus("1");
			HQHBindCard.setBindId(bindId);
			topupPayChannelBusiness.createHQHBindCard(HQHBindCard);
			LOG.info("同名卡开卡成功");
		} else if (status.equals("02")) {
			LOG.info("同名卡开卡失败");
		}
		PrintWriter pw = response.getWriter();
		pw.print("success");
		pw.close();

	}

	// 同名卡交易异步通知
	@RequestMapping(method = { RequestMethod.POST,
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/hqh/fastpay/notifyurl")
	public void tradeBack(HttpServletRequest request, HttpServletResponse response) throws Exception {

		LOG.info("环球H快捷支付异步回调进来了======");

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
		String message = request.getParameter("message");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(dsorderid);

		if ("00".equals(status)) {
			this.updateSuccessPaymentOrder(prp.getIpAddress(), dsorderid, orderid);
		} else {
			this.addOrderCauseOfFailure(dsorderid, message, prp.getIpAddress());
		}
		PrintWriter pw = response.getWriter();
		pw.print("success");
		pw.close();
	}

	// 手动代付
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqh/transfer/manual")
	public @ResponseBody Object transfer(HttpServletRequest request,
			// 订单号
			@RequestParam(value = "orderCode") String orderCode,
			// 金额
			@RequestParam(value = "real_account") String realAmount,
			// 银行卡号
			@RequestParam(value = "bank_card") String bankCard,
			// 银行名称
			@RequestParam(value = "bank_name") String bankName,
			// 身份证号
			@RequestParam(value = "idcard") String idCard,
			// 代付类型
			@RequestParam(value = "extra") String extra) {
		String handleType = "Y";
		if (extra.equals("T1")) {
			handleType = "N";
		}
		HQHRegister HQHRegister = topupPayChannelBusiness.getHQHRegisterByIdCard(idCard);
		Map<String, String> withDraw = hqhWithDraw(orderCode, HQHRegister.getMerchantCode(), realAmount, bankCard,
				bankName, handleType);

		String returnCode = withDraw.get("returncode");

		String errtext = withDraw.get("errtext");

		if ("0000".equals(returnCode) || "0003".equals(returnCode)) {

			return ResultWrap.init("999998", "等待银行出款中");
		} else {
			return ResultWrap.init(CommonConstants.FALIED, errtext);
		}

	}

	// 代付withDraw
	public Map<String, String> hqhWithDraw(String merchantOrder, String subMerchantNo, String amount, String bankcard,
			String bankName, String handleType) {
		Map<String, String> withDraw = new HashMap<String, String>();
		CommonBean trans = new CommonBean();
		amount = new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_DOWN).toString();
		trans.setMethodname("withDraw");
		trans.setTranscode("036");
		trans.setVersion("0100");
		String orderSn = UUID.randomUUID().toString().replaceAll("-", "");
		trans.setOrdersn(orderSn);
		// String merchantOrder = UUID.randomUUID().toString().replaceAll("-",
		// "");
		trans.setDsorderid(merchantOrder);
		trans.setMerchno(merchno);
		trans.setSubMerchantNo(subMerchantNo);
		trans.setAmount(amount);
		trans.setBankcard(bankcard);
		trans.setBankName(bankName);
		trans.setHandleType(handleType);
		LOG.info("进件请求报文======" + trans);
		String result = send(trans);
		LOG.info("请求进件返回的result======" + result);
		JSONObject jsonobj = JSONObject.fromObject(result);
		withDraw = jsonobj;
		return withDraw;
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
	// public static void main(String[] args) throws Exception {
	// hqhpageRequest hqhpageRequest =new hqhpageRequest();
	//// hqhpageRequest.hqhPay("105108100005149", "50", "6225768681617732",
	// "deviceId", "127.0.0.1", "127.0.0.1",
	// "http://106.15.56.208/v1.0/paymentgateway/topup/hqh/fastpay/notifyurl");
	// hqhpageRequest.hqhQueryBalance("105108100005149");
	// Random ra =new Random();
	// for(int i=0;i<100;i++){
	// System.out.println(ra.nextInt(5)+1);
	// }
	// String province="";
	// String city="";
	// String extra="消费计划|福建省-泉州市";
	//
	//
	//
	// System.out.println(province+"========="+city);
	// }

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

	/**
	 * 获取ip
	 **/
	public String getRemoteIP(HttpServletRequest request) {
		String ipAddress = request.getHeader("x-forwarded-for");
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("Proxy-Client-ipAddress");
		}
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("WL-Proxy-Client-ipAddress");
		}
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getRemoteAddr();
		}
		String[] strs = ipAddress.split(",");
		if (strs.length > 0) {
			ipAddress = strs[0];
		}
		return ipAddress.trim();
	}

	public String getMACAddress(String ip) {
		String str = "";
		String macAddress = "";
		/*
		 * try { Process p = Runtime.getRuntime().exec("nbtstat -A " + ip);
		 * InputStreamReader ir = new InputStreamReader(p.getInputStream());
		 * LineNumberReader input = new LineNumberReader(ir); for (int i = 1; i
		 * < 100; i++) { str = input.readLine(); if (str != null) { if
		 * (str.indexOf("MAC Address") > 1) { macAddress = str.substring(
		 * str.indexOf("MAC Address") + 14, str.length()); break; } } } } catch
		 * (IOException e) { e.printStackTrace(System.out); }
		 */
		return macAddress;
	}

}
