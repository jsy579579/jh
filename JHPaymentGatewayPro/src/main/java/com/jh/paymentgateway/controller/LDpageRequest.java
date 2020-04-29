package com.jh.paymentgateway.controller;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Signature;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
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
import com.jh.paymentgateway.util.JsonUtil;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.LDRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.Util;
import com.jh.paymentgateway.util.bq.CertificateUtils;
import com.jh.paymentgateway.util.bq.Extra;
import com.jh.paymentgateway.util.bq.HttpClientUtil;

import cn.jh.common.tools.Log;
import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class LDpageRequest extends BaseChannel {

	private static final Logger LOG = LoggerFactory.getLogger(LDpageRequest.class);

	@Autowired
	private RedisUtil redisUtil;

	@Value("${payment.ipAddress}")
	private String ip;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private Util util;

	@Value("${ld.cerpath1}") // ==publicKey
	private String cerpath1;

	@Value("${ld.cerpath2}")
	private String cerpath2;

	@Value("${ld.jkspath}")
	private String jkspath; // ==privateKeyUrl

	private static final Charset UTF_8 = StandardCharsets.UTF_8;

	// 进件接口
	private String registerUrl = "http://47.96.160.164:8080/gatewaysite/p/regist";
	// 修改进件接口
	private String modifyUrl = "http://47.96.160.164:8080/gatewaysite/p/mchtModify";
	// 支付接口
	private String payUrl = "http://47.96.160.164:8080/gatewaysite/p/h5Pay";

	// 查询接口
	private String queryUrl = "http://47.96.160.164:8080/gatewaysite/p/transquery";

	// 机构号
	private String OrgCode = "1107";
	// 密码
	private String password = "1107@123";

	private static PrivateKey privateKey = null;

	// 商户进件注册
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ld/register")
	public @ResponseBody Object ldRegister(HttpServletRequest request,
			@RequestParam(value = "orderCode") String orderCode) throws Exception {

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		LDRegister ldRegister = topupPayChannelBusiness.getLDRegisterByIdCard(prp.getIdCard());

		Map<String, Object> maps = new HashMap<String, Object>();
		String ordercode = prp.getOrderCode();
		String BankCard = prp.getDebitCardNo();
		String bankCard = prp.getBankCard();
		String bankName = prp.getDebitBankName();
		String BankName = prp.getCreditCardBankName();
		String cardType = prp.getDebitCardCardType();
		String userName = prp.getUserName();
		String phone = prp.getDebitPhone();
		String rate = prp.getRate();
		String idCard = prp.getIdCard();
		String cardname = Util.queryBankNameByBranchName(bankName);
		String expired = prp.getExpiredTime();
		String expiredTime = this.expiredTimeToMMYY(expired);
		LOG.info("expiredTime:" + expiredTime);
		String cardtype = prp.getDebitCardCardType();
		String amount = prp.getAmount();
		String securityCode = prp.getSecurityCode();
		String rip = prp.getIpAddress();
		String Rate = new BigDecimal(rate).multiply(new BigDecimal("100")).toString();
		Rate = Rate.substring(0, Rate.indexOf(".") + 3);
		String extraFee1 = prp.getExtraFee();
		Extra extra = new Extra();
		String extraFee = Extra.extratrans(extraFee1);	
		SimpleDateFormat sd = new SimpleDateFormat("YYYYMMddHHmmss");
		String date = sd.format(new Date());
		Random random = new Random(6);
		if (BankName.contains("中国银行")) {

			if (new BigDecimal(amount).compareTo(new BigDecimal("5000")) > 0) {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "北京银行卡交易金额限制为5000以内,请核对重新输入金额!");

				this.addOrderCauseOfFailure(orderCode, "北京银行卡交易金额限制为5000以内,请核对重新输入金额!", rip);

				return maps;

			}

		} else if (BankName.contains("邮政")) {
			if (new BigDecimal(amount).compareTo(new BigDecimal("2000")) > 0) {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "邮政银行卡交易金额限制为2000以内,请核对重新输入金额!");

				this.addOrderCauseOfFailure(orderCode, "邮政银行卡交易金额限制为2000以内,请核对重新输入金额!", rip);

				return maps;
			}

			} else if (BankName.contains("建设")) {
				if (new BigDecimal(amount).compareTo(new BigDecimal("10000")) > 0) {

					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, "建设银行卡交易金额限制为10000以内,请核对重新输入金额!");

					this.addOrderCauseOfFailure(orderCode, "建设银行卡交易金额限制为10000以内,请核对重新输入金额!", rip);

					return maps;

				}
			}

			if (ldRegister == null) {
				Map<String, String> resp = new TreeMap<String, String>();

				resp.put("orderId", date+random);// 交易流水号
				resp.put("regOrgCode", OrgCode);// 注册机构号
				resp.put("settleName", userName);// 商户姓名
				resp.put("settleNum", BankCard);// 商户结算卡号
				resp.put("bankName", bankName);// 银行卡开户行
				resp.put("settlePhone", phone);// 商户结算手机号
				resp.put("settleIdNum", idCard);// 商户证件号
				resp.put("transChannel", "05");// 支付类型
				resp.put("transRate", Rate);// 商户交易费率
				resp.put("withDrawRate", extraFee);// 代付费用

				// 给数据进行加密
				String sign = this.sign(resp);

				LOG.info("sign=======" + sign);

				resp.put("sign", sign);

				LOG.info("上送报文======" + resp);

				String sign1 = null;
				String orgId = null;
				String respCode = null;
				String mchtId = null;
				String respMsg = null;
				Map<String, String> map = new HashMap<String, String>();
				try {
					String postJson = HttpClientUtil.postJson(registerUrl, JsonUtil.format(resp));

					LOG.info(postJson);
					Map<String, Object> parse = JsonUtil.parse(postJson, Map.class);

					sign1 = (String) parse.get("sign");
					orgId = (String) parse.get("orgId");
					respCode = (String) parse.get("respCode");
					respMsg = (String) parse.get("respMsg");
					mchtId = (String) parse.get("mchtId");
				} catch (Exception e1) {
					LOG.error("请求进件接口失败=======" + e1);
					maps.put("resp_code", "failed");
					maps.put("channel_type", "jf");
					maps.put("resp_message", "很抱歉,进件失败了,请稍后重试");
					this.addOrderCauseOfFailure(orderCode, respMsg,rip );
					return maps;

				}

				LOG.info("sign1=====" + sign1);
				LOG.info("orgId=====" + orgId);
				LOG.info("respCode=====" + respCode);
				LOG.info("mchtId=====" + mchtId);

				if ("0000".equals(respCode)) {

					LDRegister ldRegister1 = new LDRegister();
					ldRegister1.setPhone(phone);
					ldRegister1.setBankCard(bankCard);
					ldRegister1.setIdCard(idCard);
					ldRegister1.setOrgId(orgId);
					ldRegister1.setMerchantId(mchtId);
					ldRegister1.setRate(rate);
					ldRegister1.setExtraFee(extraFee);

					topupPayChannelBusiness.createLDRegister(ldRegister1);
					LOG.info("进件成功========");
					map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					map.put(CommonConstants.RESP_MESSAGE, "进件成功");
					map.put(CommonConstants.RESULT, ip + "/v1.0/paymentgateway/topup/toldbankinfo?bankName="
							+ URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + BankCard + "&bankCard=" + bankCard
							+ "&cardName=" + URLEncoder.encode(BankName, "UTF-8") + "&amount=" + amount + "&ordercode="
							+ ordercode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8") + "&cardtype="
							+ URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime + "&securityCode="
							+ securityCode + "&ipAddress=" + ip + "&isRegister=2");
					return map;
				} else {
					LOG.info("进件失败=====");
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, respMsg);
					return map;
				}
			} else if (!extraFee.equals(ldRegister.getExtraFee()) | !BankCard.equals(ldRegister.getBankCard())
					| !rate.equals(ldRegister.getRate())) {

				LOG.info("=====修改手续费,结算卡,费率,开通卡======");
				maps = (Map<String, Object>) updateCard(request, orderCode);

				if ("000000".equals(maps.get("resp_code"))) {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, "商户信息修改成功");
					maps.put(CommonConstants.RESULT, ip + "/v1.0/paymentgateway/topup/toldbankinfo?bankName="
							+ URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + BankCard + "&bankCard=" + bankCard
							+ "&cardName=" + URLEncoder.encode(BankName, "UTF-8") + "&amount=" + amount + "&ordercode="
							+ ordercode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8") + "&cardtype="
							+ URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime + "&securityCode="
							+ securityCode + "&ipAddress=" + ip + "&isRegister=2");
					LOG.info("=====修改手续费,结算卡,费率,开通卡成功======" + maps);
					return maps;

				}

			} else {
				LOG.info("支付链接获取====================");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "请求支付");
				maps.put(CommonConstants.RESULT,
						ip + "/v1.0/paymentgateway/topup/toldbankinfo?bankName=" + URLEncoder.encode(bankName, "UTF-8")
								+ "&bankNo=" + BankCard + "&bankCard=" + bankCard + "&cardName="
								+ URLEncoder.encode(BankName, "UTF-8") + "&amount=" + amount + "&ordercode=" + ordercode
								+ "&cardType=" + URLEncoder.encode(cardType, "UTF-8") + "&cardtype="
								+ URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime
								+ "&securityCode=" + securityCode + "&ipAddress=" + ip + "&isRegister=2");
				return maps;

			}
			return maps;

		}
	
	
	// 进件信息修改接口
		@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ld/updateRegister")
		public @ResponseBody Object updateCard(HttpServletRequest request,
				@RequestParam(value = "orderCode") String orderCode) throws Exception {

			LOG.info("开始进入进件信息修改接口========================");

			PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
			String userName = prp.getUserName();
			String bankCard = prp.getDebitCardNo();
			String phone = prp.getCreditCardPhone();
			String idCard = prp.getIdCard();
			String extraFee1 = prp.getExtraFee();

			String rate = prp.getRate();

			LDRegister ldRegister = topupPayChannelBusiness.getLDRegisterByIdCard(idCard);
			String Rate = new BigDecimal(rate).multiply(new BigDecimal("100")).setScale(2).toString();
			Rate = Rate.substring(0, Rate.indexOf(".") + 3);
			String rip = prp.getIpAddress();
			Map<String, String> resp = new TreeMap<String, String>();
			String extraFee = Extra.extratrans(extraFee1);
			SimpleDateFormat sd = new SimpleDateFormat("YYYYMMddHHmmss");
			String date = sd.format(new Date());
			Random random = new Random(6);
			resp.put("orderId",date+ random);// 交易流水号
			resp.put("regOrgCode", OrgCode);// 注册机构号
			resp.put("mchtId", ldRegister.getMerchantId());// 商户号
			resp.put("settleName", userName);// 商户姓名
			resp.put("settleNum", bankCard);// 商户结算卡号
			resp.put("settlePhone", phone);// 商户结算手机号
			resp.put("settleIdNum", idCard);// 商户证件号
			resp.put("transChannel", "05");// 支付类型
			resp.put("transRate", Rate);// 支付费率

			resp.put("withDrawRate", extraFee);// 代付费用

			// 给数据进行加密
			String sign = this.sign(resp);

			LOG.info("sign=======" + sign);

			resp.put("sign", sign);// 代付费用

			LOG.info("上送报文======" + resp);

			String respCode = null;
			String respMsg = null;

			Map<String, Object> maps = new HashMap<String, Object>();

			String postJson = HttpClientUtil.postJson(modifyUrl, JsonUtil.format(resp));

			LOG.info(postJson);

			Map<String, Object> parse = JsonUtil.parse(postJson, Map.class);
			respCode = (String) parse.get("respCode");
			respMsg = (String) parse.get("respMsg");

			LOG.info("respCode=====" + respCode);
			LOG.info("respMsg=====" + respMsg);

			if ("0000".equals(respCode)) {
				LDRegister ldRegister1 = topupPayChannelBusiness.getLDRegisterByIdCard(idCard);
				ldRegister1.setBankCard(bankCard);
				ldRegister1.setPhone(phone);
				ldRegister1.setRate(rate);
				ldRegister1.setExtraFee(extraFee1);
				ldRegister1.setUpdateTime(DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss"));

				topupPayChannelBusiness.createLDRegister(ldRegister1);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "商户修改进件成功");
				return maps;

			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);
				this.addOrderCauseOfFailure(orderCode, respMsg,rip);
				return maps;

			}
		}

		
	

	// 跳转结算卡页面的中转接口
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/toldbankinfo")
	public String tojfshangaobankinfo(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		LOG.info("/v1.0/paymentgateway/topup/toldbankinfo=========toldbankinfo");
		String bankName = request.getParameter("bankName");// 结算卡银行名称
		String bankNo = request.getParameter("bankNo");// 结算卡卡号
		String amount = request.getParameter("amount");
		String ordercode = request.getParameter("ordercode");
		String cardType = request.getParameter("cardType");// 结算卡的卡类型
		String isRegister = request.getParameter("isRegister");
		String cardtype = request.getParameter("cardtype");// 信用卡的卡类型
		String bankCard = request.getParameter("bankCard");// 充值卡卡号
		String cardName = request.getParameter("cardName");// 充值卡银行名称
		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("bankName", bankName);
		model.addAttribute("bankNo", bankNo);
		model.addAttribute("amount", amount);
		model.addAttribute("ordercode", ordercode);
		model.addAttribute("cardType", cardType);
		model.addAttribute("isRegister", isRegister);
		model.addAttribute("cardtype", cardtype);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("cardName", cardName);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("ipAddress", ipAddress);

		return "ldbankinfo";
	}

	// 支付链接获取
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ld/consume")
	public @ResponseBody Object ldPay(@RequestParam(value = "ordercode") String orderCode) throws Exception {
		LOG.info("开始进入无卡支付接口=======");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String amount = prp.getAmount();
		String rip = prp.getIpAddress();
		LDRegister ldRegister = topupPayChannelBusiness.getLDRegisterByIdCard(prp.getIdCard());
		String Amount = new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0).toString();
		
		SimpleDateFormat sd = new SimpleDateFormat("YYYYMMddHHmmss");
		String date = sd.format(new Date());
		Map<String, String> resp = new TreeMap<String, String>();
		
		LOG.info("交易流水号======================"+orderCode);
		resp.put("orderId",orderCode);// 交易流水号
		resp.put("orgId", ldRegister.getOrgId());// 机构号
		resp.put("mchtId", ldRegister.getMerchantId());// 商户号
		resp.put("transTime", DateUtil.getyyyyMMddHHmmssDateFormat(new Date()));// 交易时间
		resp.put("transChannel", "05");// 受理平台代码
		resp.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/ld/notify_call");// 通知地址
		resp.put("tranAmt", Amount);// 交易金额
		resp.put("mobile", prp.getCreditCardPhone());// 预留手机号
		resp.put("acct_no", prp.getBankCard());// 交易卡
		// 给数据进行加密
		String sign = this.sign(resp);

		LOG.info("sign=======" + sign);

		resp.put("sign", sign);

		LOG.info("上送报文======" + resp);

		String codeUrl = null;
		String orderId = null;
		String respCode = null;

		String respMsg = null;
		Map<String, Object> maps = new HashMap<String, Object>();
		try {
			String postJson = HttpClientUtil.postJson(payUrl, JsonUtil.format(resp));

			LOG.info(postJson);

			Map<String, Object> parse = JsonUtil.parse(postJson, Map.class);

			codeUrl = (String) parse.get("codeUrl");
			System.out.println(codeUrl + "-----------------------");
			orderId = (String) parse.get("orderId");
			respCode = (String) parse.get("respCode");

			respMsg = (String) parse.get("respMsg");

		} catch (Exception e) {
			LOG.error("请求无卡支付接口失败=======" + e.getMessage());
			return ResultWrap.init(CommonConstants.FALIED, "交易排队中,请稍后重试!");
		}
		if ("0000".equals(respCode)) {
			LOG.info("请求获取链接地址成功=======");
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "成功");
			maps.put("redirect_url", codeUrl);
			return maps;

		} else {
			LOG.info("请求支付确认接口失败=======");
			this.addOrderCauseOfFailure(orderCode, respMsg,rip);
			return ResultWrap.init(CommonConstants.FALIED, respMsg);
		

		}

	}
	
	// 支付接口异步通知
		@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ld/notify_call")
		public Object paynotifyCall(HttpServletRequest request, HttpServletResponse response

		) throws Exception {

			LOG.info("异步回调进来了");
		
			InputStream inputStream = request.getInputStream();
			ByteArrayOutputStream byteArray = null;
			byteArray = new ByteArrayOutputStream();
			byte[] dat = new byte[2048];
			int l = 0;
			while ((l = inputStream.read(dat, 0, 2048)) != -1) {
				byteArray.write(dat, 0, l);
			}
			byteArray.flush();
			LOG.info("ByteArrayOutputStream2String=============" + new String(byteArray.toByteArray(), "UTF-8"));
			String info = new String(byteArray.toByteArray(), "UTF-8");
			JSONObject jsonInfo;
			try {
				jsonInfo = JSONObject.fromObject(info);
			} catch (Exception e1) {
				return null;
			}
			LOG.info("jsonInfo=============" + jsonInfo.toString());
			inputStream.close();
			byteArray.close();

			String origOrderId = jsonInfo.getString("origOrderId");
			String origRespCode = jsonInfo.getString("origRespCode");
			String origSysOrderId = jsonInfo.getString("origSysOrderId");
			String origRespMsg = jsonInfo.getString("origRespMsg");
			PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(origOrderId);
			String rip = prp.getIpAddress();
			LOG.info("origOrderId======" + origOrderId);
			LOG.info("origRespCode======" + origRespCode);
			LOG.info("origSysOrderId======" + origSysOrderId);

				Log.println("---交易： 订单结果异步通知-------------------------");
				LOG.info("交易： 订单结果异步通知===================");
				if ("0000".equals(origRespCode)) { // 订单已支付;
					
					RestTemplate restTemplate = new RestTemplate();
					MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
					String URL = null;
					String result = null;
					LOG.info("*********************交易成功***********************");
					
					URL = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
					//URL = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";
					
					requestEntity = new LinkedMultiValueMap<String, String>();
					requestEntity.add("status", "1");
					requestEntity.add("order_code", origOrderId);
					requestEntity.add("third_code", origSysOrderId);
					try {
						result = restTemplate.postForObject(URL, requestEntity, String.class);
					} catch (Exception e) {
						e.printStackTrace();
						LOG.error("",e);
					}
					LOG.info("订单已支付!================================================");
						response.getWriter().write("000000");
						
						}else{
							LOG.info("订单支付失败!================================================");
							this.addOrderCauseOfFailure(origOrderId, origRespMsg,rip);	
							response.getWriter().write("000000");
						}
				return origRespMsg;

			
		
		}

	// 支付查询
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ld/query")
	public @ResponseBody Object ldquery(HttpServletRequest request, @RequestParam(value = "orderCode") String orderCode)
			throws Exception {
		LOG.info("开始进入H5支付查询接口=======");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String amount = prp.getAmount();

		LDRegister ldRegister = topupPayChannelBusiness.getLDRegisterByIdCard(prp.getIdCard());
		String Amount = new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0).toString();
		Map<String, String> resp = new TreeMap<String, String>();
		SimpleDateFormat sd = new SimpleDateFormat("YYYYMMddHHmmss");
		String date = sd.format(new Date());
		
		resp.put("orderId",date);// 交易流水号
		resp.put("orgId", ldRegister.getOrgId());// 机构号
		resp.put("mchtId", ldRegister.getMerchantId());// 商户号
		resp.put("transTime", DateUtil.getyyyyMMddHHmmssDateFormat(new Date()));// 交易时间
		resp.put("origOrderId",orderCode);// 原交易流水号

		// 给数据进行加密
		String sign = this.sign(resp);

		LOG.info("sign=======" + sign);

		resp.put("sign", sign);

		LOG.info("上送报文======" + resp);

		String sign1 = null;
		String origAmount = null;
		String origRespCode = null;
		String respCode = null;
		String origRespMsg = null;
		HashMap<String, Object> map = new HashMap<String, Object>();
		try {

			String postJson = HttpClientUtil.postJson(queryUrl, JsonUtil.format(resp));

			LOG.info(postJson);

			Map<String, Object> parse = JsonUtil.parse(postJson, Map.class);

			sign1 = (String) parse.get("sign");
			origAmount = (String) parse.get("origAmount");
			origRespCode = (String) parse.get("origRespCode");
			respCode = (String) parse.get("respCode");
			origRespMsg = (String) parse.get("origRespMsg");
		} catch (Exception e) {
			LOG.error("请求无卡支付接口失败=======" + e);
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "很抱歉,支付失败了,请稍后重试!");

			return map;
		}
		if ("0000".equals(respCode)) {
			LOG.info("请求支付查询接口成功=======");
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, origRespMsg);

		} else {
			LOG.info("请求支付查询接口失败,请重新请求=======");
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, origRespMsg);

		}

		return map;

	}

	
	
	// 加密方法
	private String sign(Map<String, String> resp) {
		StringBuffer sb = new StringBuffer();
		for (String key : resp.keySet()) {
			if (resp.get(key) != null && !resp.get(key).equals(""))
				sb.append(key + "=" + resp.get(key) + "&");
		}
		String queryString = sb.substring(0, sb.length() - 1);// 构造待签名字符串
		LOG.info("签名字符串==================" + queryString);
		FileInputStream fis = null;
		String sign = null;
		try {
			if (privateKey == null) {
				fis = new FileInputStream(jkspath);
				privateKey = CertificateUtils.getPrivateKey(fis, null, password);
			}
			Signature signature = Signature.getInstance("SHA1withRSA");
			signature.initSign(privateKey);
			signature.update(queryString.getBytes("UTF-8"));
			sign = Base64.encodeBase64String(signature.sign());
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("",e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("",e);
				}
			}
		}

		return sign;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/sdjpaysuccess")
	public String returnpaysuccess(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		return "sdjsuccess";
	}

}