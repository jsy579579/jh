package com.jh.paymentchannel.service;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
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

import com.jh.paymentchannel.business.BranchbankBussiness;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.LDRegister;
import com.jh.paymentchannel.util.JsonUtil;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.ld.CertificateUtils;
import com.jh.paymentchannel.util.ld.HttpClientUtil;

import cn.jh.common.tools.Log;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class LDpageRequest {

	private static final Logger LOG = LoggerFactory.getLogger(LDpageRequest.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private BranchbankBussiness branchbankBussiness;

	@Autowired
	private Util util;

	@Value("${payment.ipAddress}")
	private String ipAddress;

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
	private String payUrl = "http://47.96.160.164:8080/gatewaysite/c/createNoCardOrder";
	// 确认支付接口
	private String sendUrl = "http://47.96.160.164:8080/gatewaysite/c/sendNoCardPay";
	// 查询接口
	private String queryUrl = "http://47.96.160.164:8080/gatewaysite/p/transquery";

	// 机构号
	private String OrgCode = "1107";
	// 密码
	private String password = "1107@123";
	
	private static PrivateKey privateKey = null;

	// 商户进件注册
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ld/register")
	public @ResponseBody Object ldRegister(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode) throws Exception {
		LOG.info("开始进入进件接口========================");
		Map map = new HashMap();
		Map<String, String> maps = new HashMap<String, String>();
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================" + result);
		JSONObject jsonObject;
		JSONObject resultObj;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			LOG.error("查询订单信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jfcoin");
			map.put("resp_message", "没有该订单信息");
			return map;
		}

		String userid = resultObj.getString("userid");
		// 费率
		String rate = resultObj.getString("rate");
		// 额外费率
		String extraFee = resultObj.getString("extraFee");
		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/userid";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("user_id", userid);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		LOG.info("RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			LOG.error("查询默认结算卡出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jfcoin");
			map.put("resp_message", "查询默认结算卡有误");
			return map;
		}
		String respcode = jsonObject.getString("resp_code");

		// 默认提现卡卡号
		String cardNo = resultObj.getString("cardNo");
		String userName = resultObj.getString("userName");
		/** 身份证号 */
		String idcard = resultObj.getString("idcard");
		String phone = resultObj.getString("phone");
		String bankName = resultObj.getString("bankName");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/cardno";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("cardno", bankCard);
		requestEntity.add("type", "0");
		result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("接口/v1.0/user/bank/default/cardno--RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("查询银行卡信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询银行卡信息有误");
			return map;
		}

		String cardName = resultObj.getString("bankName");

		String cardname = Util.queryBankNameByBranchName(cardName);
		String cardtype = resultObj.getString("cardType");

		String Rate = new BigDecimal(rate).multiply(new BigDecimal("100")).toString();

		Rate = Rate.substring(0, Rate.indexOf(".") + 3);

		Map<String, String> resp = new TreeMap<String, String>();

		resp.put("orderId", ordercode);// 交易流水号
		resp.put("regOrgCode", OrgCode);// 注册机构号
		resp.put("settleName", userName);// 商户姓名
		resp.put("settleNum", cardNo);// 商户结算卡号
		resp.put("settlePhone", phone);// 商户结算手机号
		resp.put("settleIdNum", idcard);// 商户证件号
		resp.put("transChannel", "04");// 支付类型
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
		try {
			String postJson = HttpClientUtil.postJson(registerUrl, JsonUtil.format(resp));

			LOG.info(postJson);

			Map<String, Object> parse = JsonUtil.parse(postJson, Map.class);

			sign1 = (String) parse.get("sign");
			orgId = (String) parse.get("orgId");
			respCode = (String) parse.get("respCode");
			mchtId = (String) parse.get("mchtId");
		} catch (Exception e1) {
			LOG.error("请求进件接口失败=======" + e1);
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "很抱歉,进件失败了,请稍后重试");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "很抱歉,进件失败了,请稍后重试");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}

		LOG.info("sign1=====" + sign1);
		LOG.info("orgId=====" + orgId);
		LOG.info("respCode=====" + respCode);
		LOG.info("mchtId=====" + mchtId);


		if ("0000".equals(respCode)) {
			LOG.info("进件成功========");

			LDRegister ldRegister = new LDRegister();
			ldRegister.setPhone(phone);
			ldRegister.setBankCard(bankCard);
			ldRegister.setIdCard(idcard);
			ldRegister.setOrgId(orgId);
			ldRegister.setMerchantId(mchtId);
			ldRegister.setRate(rate);
			ldRegister.setExtraFee(extraFee);

			try {
				topupPayChannelBusiness.createLDRegister(ldRegister);
			} catch (Exception e) {
				LOG.error("保存用户进件信息出错啦======");
				map.put("resp_code", "failed");
				map.put("channel_type", "jf");
				map.put("resp_message", "保存用户进件信息出错,请稍后重试!");
				
				restTemplate = new RestTemplate();
				uri = util.getServiceUrl("transactionclear", "error url request!");
				url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("ordercode", ordercode);
				requestEntity.add("remark", "保存用户进件信息出错,请稍后重试");
				result = restTemplate.postForObject(url, requestEntity, String.class);
				
				return map;
			}

			LOG.info("保存用户进件信息成功,开始进入支付接口=======");

			map.put("resp_code", "success");
			map.put("channel_type", "jf");
			map.put("redirect_url",
					ipAddress + "/v1.0/paymentchannel/topup/ldpay?bankName=" + URLEncoder.encode(cardname, "UTF-8")
							+ "&cardType=" + URLEncoder.encode(cardtype, "UTF-8") + "&bankCard=" + bankCard
							+ "&ordercode=" + ordercode + "&expiredTime=" + expiredTime + "&securityCode="
							+ securityCode + "&ipAddress=" + ipAddress);
			return map;

		} else {
			LOG.info("进件失败=====");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "很抱歉,进件失败了,请稍后重试");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", respCode+" : 很抱歉,进件失败了,请稍后重试");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}

	}

	// 无卡支付
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ld/consume")
	public @ResponseBody Object ldPay(HttpServletRequest request, @RequestParam(value = "ordercode") String ordercode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode) throws Exception {
		LOG.info("开始进入无卡支付接口=======");
		Map map = new HashMap();
		Map<String, String> maps = new HashMap<String, String>();
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================" + result);
		JSONObject jsonObject;
		JSONObject resultObj;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			LOG.error("查询订单信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jfcoin");
			map.put("resp_message", "没有该订单信息");
			return map;
		}

		String amount = resultObj.getString("amount");
		String userid = resultObj.getString("userid");
		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/userid";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("user_id", userid);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		LOG.info("RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			LOG.error("查询默认结算卡出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jfcoin");
			map.put("resp_message", "查询默认结算卡有误");
			return map;
		}
		String respcode = jsonObject.getString("resp_code");

		// 默认提现卡卡号
		String cardNo = resultObj.getString("cardNo");
		String phone = resultObj.getString("phone");
		// 身份证号
		String idCard = resultObj.getString("idcard");

		LDRegister ldRegister = topupPayChannelBusiness.getLDRegisterByIdCard(idCard);

		// 查询信用卡信息
		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/cardno";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("cardno", bankCard);
		requestEntity.add("type", "0");
		result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("接口/v1.0/user/bank/default/cardno--RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("查询银行卡信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询银行卡信息有误");
			return map;
		}

		String userName = resultObj.getString("userName");
		String idcard = resultObj.getString("idcard");
		String phone1 = resultObj.getString("phone");
		String bankName = resultObj.getString("bankName");

		// 将金额变成以分为单位
		String Amount = new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0).toString();

		try {
			String before = expiredTime.substring(0, 2);
			String after = expiredTime.substring(2, 4);

			BigDecimal big = new BigDecimal(before);
			BigDecimal times = new BigDecimal("12");

			int compareTo = big.compareTo(times);
			// 如果前两位大于12，,代表是年/月的格式
			if (compareTo == 1) {
				expiredTime = after + before;
			} else {
				expiredTime = before + after;
			}
		} catch (Exception e) {
			LOG.error("转换有效期格式有误======="+e);
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "您的信用卡有效期信息不正确,请仔细核对并重新输入!");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "您的信用卡有效期信息不正确,请仔细核对并重新输入!");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}

		LOG.info("转换过的有效期格式======" + expiredTime);

		Map<String, String> resp = new TreeMap<String, String>();

		resp.put("orderId", ordercode);// 交易流水号
		resp.put("orgId", ldRegister.getOrgId());// 机构号
		resp.put("mchtId", ldRegister.getMerchantId());// 商户号
		resp.put("transTime", DateUtil.getyyyyMMddHHmmssDateFormat(new Date()));// 交易时间
		resp.put("transChannel", "04");// 受理平台代码
		resp.put("notifyUrl", ipAddress + "/v1.0/paymentchannel/topup/ld/notify_call");// 通知地址
		resp.put("tranAmt", Amount);// 交易金额
		resp.put("acct_no", bankCard);// 交易卡
		resp.put("acct_name", userName);// 持卡人姓名
		resp.put("acct_phone", phone1);// 预留手机号
		resp.put("idNum", idcard);// 持卡人身份证号
		resp.put("acct_cvv2", securityCode);// cvn2
		resp.put("acct_validdate", expiredTime);// 卡有效期

		// 给数据进行加密
		String sign = this.sign(resp);

		LOG.info("sign=======" + sign);

		resp.put("sign", sign);

		LOG.info("上送报文======" + resp);

		String sign1 = null;
		String orderId = null;
		String respCode = null;
		String sysOrderId = null;
		String respMsg = null;
		try {
			String postJson = HttpClientUtil.postJson(payUrl, JsonUtil.format(resp));

			LOG.info(postJson);

			Map<String, Object> parse = JsonUtil.parse(postJson, Map.class);

			sign1 = (String) parse.get("sign");
			orderId = (String) parse.get("orderId");
			respCode = (String) parse.get("respCode");
			sysOrderId = (String) parse.get("sysOrderId");
			respMsg = (String) parse.get("respMsg");
			
		} catch (Exception e) {
			LOG.error("请求无卡支付接口失败=======" + e);
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "很抱歉,支付失败了,请稍后重试!");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "很抱歉,支付失败了,请稍后重试");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}

		LOG.info("sign1=====" + sign1);
		LOG.info("orderId=====" + orderId);
		LOG.info("respCode=====" + respCode);
		LOG.info("sysOrderId=====" + sysOrderId);
		LOG.info("respMsg=====" + respMsg);


		if ("0000".equals(respCode)) {
			LOG.info("请求发送短信接口成功======");
			/*restTemplate = new RestTemplate();
    		uri = util.getServiceUrl("user", "error url request!");
    		url = uri.toString() + "/v1.0/user/bank/update/bynewcardno";
    		requestEntity = new LinkedMultiValueMap<String, String>();
    		requestEntity.add("cardNo", bankCard);
    		requestEntity.add("type", "0");
    		requestEntity.add("securityCode", securityCode);
    		requestEntity.add("expiredTime", expiredTime);
    		result = restTemplate.postForObject(url, requestEntity, String.class);

    		log.info("RESULT================" + result);
    		jsonObject = JSONObject.fromObject(result);
    		resultObj = jsonObject.getJSONObject("result");*/
			
			
			map.put("resp_code", "success");
			map.put("channel_type", "jf");
			map.put("sysOrderId", sysOrderId);
			return map;
		} else {
			
			if("2326".equals(respCode)) {
				map.put("resp_code", "failed");
				map.put("channel_type", "jf");
				map.put("resp_message", "通道系统异常,请及时联系管理员!");
				
				restTemplate = new RestTemplate();
				uri = util.getServiceUrl("transactionclear", "error url request!");
				url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("ordercode", ordercode);
				requestEntity.add("remark", "通道系统异常,请及时联系管理员!");
				result = restTemplate.postForObject(url, requestEntity, String.class);
				
				return map;
			}else if("2210".equals(respCode)) {
				if("操作成功".equals(respMsg)) {
					map.put("resp_code", "failed");
					map.put("channel_type", "jf");
					map.put("resp_message", "交易拒绝,您的交易金额超过网银交易金额限制!");
					
					restTemplate = new RestTemplate();
					uri = util.getServiceUrl("transactionclear", "error url request!");
					url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
					requestEntity = new LinkedMultiValueMap<String, String>();
					requestEntity.add("ordercode", ordercode);
					requestEntity.add("remark", respCode+" : 交易拒绝,您的交易金额超过网银交易金额限制!");
					result = restTemplate.postForObject(url, requestEntity, String.class);
					
					return map;
				}else if("今日渠道额度用尽".equals(respMsg)) {
					map.put("resp_code", "failed");
					map.put("channel_type", "jf");
					map.put("resp_message", "交易拒绝,该通道今日额度已达上限!");
					
					restTemplate = new RestTemplate();
					uri = util.getServiceUrl("transactionclear", "error url request!");
					url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
					requestEntity = new LinkedMultiValueMap<String, String>();
					requestEntity.add("ordercode", ordercode);
					requestEntity.add("remark", "交易拒绝,您的交易金额超过网银交易金额限制或该通道额度已达上限!");
					result = restTemplate.postForObject(url, requestEntity, String.class);
					
					return map;
				}else if("订单路由失败".equals(respMsg)) {
					map.put("resp_code", "failed");
					map.put("channel_type", "jf");
					map.put("resp_message", "交易拒绝,您的银行卡卡种不支持!");
					
					restTemplate = new RestTemplate();
					uri = util.getServiceUrl("transactionclear", "error url request!");
					url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
					requestEntity = new LinkedMultiValueMap<String, String>();
					requestEntity.add("ordercode", ordercode);
					requestEntity.add("remark", respCode+" : 交易拒绝,您的银行卡卡种不支持!");
					result = restTemplate.postForObject(url, requestEntity, String.class);
					
					return map;
				}else {
					map.put("resp_code", "failed");
					map.put("channel_type", "jf");
					map.put("resp_message", respMsg);
					
					restTemplate = new RestTemplate();
					uri = util.getServiceUrl("transactionclear", "error url request!");
					url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
					requestEntity = new LinkedMultiValueMap<String, String>();
					requestEntity.add("ordercode", ordercode);
					requestEntity.add("remark", respCode+" : "+respMsg);
					result = restTemplate.postForObject(url, requestEntity, String.class);
					
					return map;
				}
				
			}else {
				map.put("resp_code", "failed");
				map.put("channel_type", "jf");
				map.put("resp_message", respMsg);
				
				restTemplate = new RestTemplate();
				uri = util.getServiceUrl("transactionclear", "error url request!");
				url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("ordercode", ordercode);
				requestEntity.add("remark", respCode+" : "+respMsg);
				result = restTemplate.postForObject(url, requestEntity, String.class);
				
				return map;
			}
			
			
		}

	}

	// 支付确认接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ld/consumesms")
	public @ResponseBody Object consumeSMS(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "ordercode") String ordercode, @RequestParam(value = "smsCode") String smsCode)
			throws Exception {

		LOG.info("开始进入支付确认接口========================");
		Map map = new HashMap();
		Map<String, String> maps = new HashMap<String, String>();
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================" + result);
		JSONObject jsonObject;
		JSONObject resultObj;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			LOG.error("查询订单信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jfcoin");
			map.put("resp_message", "没有该订单信息");
			return map;
		}

		String userid = resultObj.getString("userid");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/userid";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("user_id", userid);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		LOG.info("RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			LOG.error("查询默认结算卡出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jfcoin");
			map.put("resp_message", "查询默认结算卡有误");
			return map;
		}
		String respcode = jsonObject.getString("resp_code");

		// 身份证号
		String idCard = resultObj.getString("idcard");

		LDRegister ldRegister = topupPayChannelBusiness.getLDRegisterByIdCard(idCard);

		Map<String, String> resp = new TreeMap<String, String>();

		resp.put("orderId", ordercode.substring(0, 29));// 交易流水号
		resp.put("orgId", ldRegister.getOrgId());// 机构号
		resp.put("mchtId", ldRegister.getMerchantId());// 商户号
		resp.put("transTime", DateUtil.getyyyyMMddHHmmssDateFormat(new Date()));// 交易时间
		resp.put("origOrderId", ordercode);// 原交易交易流水号
		resp.put("verify_code", smsCode);// 短信验证码

		// 给数据进行加密
		String sign = this.sign(resp);

		LOG.info("sign=======" + sign);

		resp.put("sign", sign);// 代付费用

		LOG.info("上送报文======" + resp);

		String orderId = null;
		String respCode = null;
		// String sysOrderId = (String) parse.get("sysOrderId");
		String respMsg = null;
		try {
			String postJson = HttpClientUtil.postJson(sendUrl, JsonUtil.format(resp));

			LOG.info(postJson);

			Map<String, Object> parse = JsonUtil.parse(postJson, Map.class);

			String sign1 = (String) parse.get("sign");
			orderId = (String) parse.get("orderId");
			respCode = (String) parse.get("respCode");
			respMsg = (String) parse.get("respMsg");
		} catch (Exception e) {
			LOG.error("请求支付确认接口失败=======" + e);
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "很抱歉,支付确认失败了,请稍后重试");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "很抱歉,支付确认失败了,请稍后重试");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}

		// log.info("sign1=====" + sign1);
		LOG.info("orderId=====" + orderId);
		LOG.info("respCode=====" + respCode);
		// log.info("sysOrderId=====" + sysOrderId);
		LOG.info("respMsg=====" + respMsg);

		if ("0000".equals(respCode)) {
			LOG.info("请求支付确认接口成功=======");
			map.put("resp_code", "success");
			map.put("channel_type", "jf");
			map.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/sdjpaysuccess");

		} else {
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			//map.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/sdjpayerror");
			map.put("resp_message", "支付失败！ 失败原因: "+respMsg);
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", respCode+" : "+respMsg);
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
		}

		return map;
	}

	// 进件信息修改接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ld/updateRegister")
	public @ResponseBody Object updateCard(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode) throws Exception {
		LOG.info("开始进入进件信息修改接口========================");
		Map map = new HashMap();
		Map<String, String> maps = new HashMap<String, String>();
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================" + result);
		JSONObject jsonObject = JSONObject.fromObject(result);
		JSONObject resultObj = jsonObject.getJSONObject("result");

		String userid = resultObj.getString("userid");
		// 费率
		String rate = resultObj.getString("rate");
		// 额外费率
		String extraFee = resultObj.getString("extraFee");

		String bankCard = resultObj.getString("bankcard");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/userid";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("user_id", userid);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		LOG.info("RESULT================" + result);
		jsonObject = JSONObject.fromObject(result);
		resultObj = jsonObject.getJSONObject("result");

		String cardNo = resultObj.getString("cardNo");
		String userName = resultObj.getString("userName");
		/** 身份证号 */
		String idcard = resultObj.getString("idcard");
		String phone = resultObj.getString("phone");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/cardno";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("cardno", bankCard);
		requestEntity.add("type", "0");
		result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("接口/v1.0/user/bank/default/cardno--RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("查询银行卡信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询银行卡信息有误");
			return map;
		}

		String cardName = resultObj.getString("bankName");

		String cardname = Util.queryBankNameByBranchName(cardName);
		String cardtype = resultObj.getString("cardType");

		LDRegister ldRegister = topupPayChannelBusiness.getLDRegisterByIdCard(idcard);

		String Rate = new BigDecimal(rate).multiply(new BigDecimal("100")).toString();

		Rate = Rate.substring(0, Rate.indexOf(".") + 3);

		Map<String, String> resp = new TreeMap<String, String>();

		resp.put("orderId", ordercode);// 交易流水号
		resp.put("regOrgCode", OrgCode);// 注册机构号
		resp.put("mchtId", ldRegister.getMerchantId());// 商户号
		resp.put("settleName", userName);// 商户姓名
		resp.put("settleNum", cardNo);// 商户结算卡号
		resp.put("settlePhone", phone);// 商户结算手机号
		resp.put("settleIdNum", idcard);// 商户证件号
		resp.put("transChannel", "04");// 支付类型
		resp.put("transRate", Rate);// 支付费率
		resp.put("withDrawRate", extraFee);// 代付费用

		// 给数据进行加密
		String sign = this.sign(resp);

		LOG.info("sign=======" + sign);

		resp.put("sign", sign);// 代付费用

		LOG.info("上送报文======" + resp);

		String respCode = null;
		String respMsg = null;
		try {
			String postJson = HttpClientUtil.postJson(modifyUrl, JsonUtil.format(resp));

			LOG.info(postJson);

			Map<String, Object> parse = JsonUtil.parse(postJson, Map.class);

			String sign1 = (String) parse.get("sign");
			String mchtId = (String) parse.get("mchtId");
			respCode = (String) parse.get("respCode");
			respMsg = (String) parse.get("respMsg");
		} catch (Exception e) {
			LOG.error("请求进件接口失败=======" + e);
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "很抱歉,进件信息修改失败了,请稍后重试!");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "很抱歉,进件信息修改失败了,请稍后重试");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}

		LOG.info("respCode=====" + respCode);
		LOG.info("respMsg=====" + respMsg);

		if ("0000".equals(respCode)) {

			ldRegister.setBankCard(cardNo);
			ldRegister.setPhone(phone);
			ldRegister.setRate(rate);
			ldRegister.setExtraFee(extraFee);
			ldRegister.setUpdateTime(DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss"));
			
			topupPayChannelBusiness.createLDRegister(ldRegister);

			map.put("resp_code", "success");
			map.put("channel_type", "jf");
			map.put("redirect_url",
					ipAddress + "/v1.0/paymentchannel/topup/ldpay?bankName=" + URLEncoder.encode(cardname, "UTF-8")
							+ "&cardType=" + URLEncoder.encode(cardtype, "UTF-8") + "&bankCard=" + bankCard
							+ "&ordercode=" + ordercode + "&expiredTime=" + expiredTime + "&securityCode="
							+ securityCode + "&ipAddress=" + ipAddress);
			return map;

		} else {
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", respMsg);
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", respCode+" : "+respMsg);
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;

		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ld/turntopaypage")
	public @ResponseBody Object turnToCJPayPage(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode) throws Exception {

		Map map = new HashMap();
		Map<String, String> maps = new HashMap<String, String>();
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================" + result);
		JSONObject jsonObject;
		JSONObject resultObj;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			LOG.error("查询订单信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jfcoin");
			map.put("resp_message", "没有该订单信息");
			return map;
		}

		String userid = resultObj.getString("userid");
		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");

		// 查询信用卡信息
		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/cardno";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("cardno", bankCard);
		requestEntity.add("type", "0");
		result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("接口/v1.0/user/bank/default/cardno--RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("查询银行卡信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询银行卡信息有误");
			return map;
		}
		if (resultObj == null) {
			return null;
		}

		String cardName = resultObj.getString("bankName");
		String cardtype = resultObj.getString("cardType");

		String cardname = Util.queryBankNameByBranchName(cardName);

		map.put("resp_code", "success");
		map.put("channel_type", "jf");
		map.put("redirect_url",
				ipAddress + "/v1.0/paymentchannel/topup/ldpay?ordercode=" + ordercode + "&expiredTime=" + expiredTime
						+ "&securityCode=" + securityCode + "&bankName=" + URLEncoder.encode(cardname, "UTF-8")
						+ "&cardType=" + URLEncoder.encode(cardtype, "UTF-8") + "&bankCard=" + bankCard + "&ipAddress="
						+ ipAddress);
		return map;

	}

	// 中转接口
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/ldpay")
	public String returnjfapipay(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String ordercode = request.getParameter("ordercode");
		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String bankName = request.getParameter("bankName");
		String cardType = request.getParameter("cardType");
		String bankCard = request.getParameter("bankCard");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("ordercode", ordercode);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("bankName", bankName);
		model.addAttribute("cardType", cardType);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("ipAddress", ipAddress);

		return "ldpaymessage";
	}

	// 跳转确认提现卡页面的中转接口
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/toldbankinfo")
	public String tojfshangaobankinfo(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		LOG.info("/v1.0/paymentchannel/topup/toldbankinfo=========toldbankinfo");
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

	// 支付接口异步通知
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ld/notify_call")
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

		LOG.info("origOrderId======" + origOrderId);
		LOG.info("origRespCode======" + origRespCode);
		LOG.info("origSysOrderId======" + origSysOrderId);

		try {
			Log.setLogFlag(true);
			Log.println("---交易： 订单结果异步通知-------------------------");

			LOG.info("交易： 订单结果异步通知===================");
			if ("0000".equals(origRespCode)) { // 订单已支付;

				// 1、检查Amount和商户系统的订单金额是否一致
				// 2、订单支付成功的业务逻辑处理请在本处增加（订单通知可能存在多次通知的情况，需要做多次通知的兼容处理）；
				// 3、返回响应内容

				synchronized (this) {
					// **更新订单状态*//*
					// **调用下单，需要得到用户的订单信息*//*
					RestTemplate restTemplate = new RestTemplate();
					URI uri = util.getServiceUrl("transactionclear", "error url request!");
					String url = uri.toString() + "/v1.0/transactionclear/payment/update";
					// **根据的用户手机号码查询用户的基本信息*//*
					MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
					requestEntity.add("status", "1");
					requestEntity.add("third_code", origSysOrderId);
					requestEntity.add("order_code", origOrderId);
					String result = restTemplate.postForObject(url, requestEntity, String.class);
					LOG.info("订单状态修改成功===================");
					LOG.info("订单已支付!");
					response.getWriter().write("000000");
				}
			} else {
				// 1、订单支付失败的业务逻辑处理请在本处增加（订单通知可能存在多次通知的情况，需要做多次通知的兼容处理，避免成功后又修改为失败）；
				// 2、返回响应内容

				LOG.info("订单支付失败!");
			}
		} catch (Exception e) {
			LOG.error("",e);
		}
		LOG.info("-----处理完成----");
		return null;
	}

	// 加密方法
	private String sign(Map<String, String> resp) {
		StringBuffer sb = new StringBuffer();
		for (String key : resp.keySet()) {
			if (resp.get(key) != null && !resp.get(key).equals(""))
				sb.append(key + "=" + resp.get(key) + "&");
		}
		String queryString = sb.substring(0, sb.length() - 1);// 构造待签名字符串
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
			e.printStackTrace();LOG.error("",e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
					e.printStackTrace();LOG.error("",e);
				}
			}
		}

		return sign;
	}

	
	
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/paymentchannel/topup/sdjpaysuccess")
	public  String returnpaysuccess(HttpServletRequest request, HttpServletResponse response, Model model)throws IOException {
		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
	
		return "sdjsuccess";
	}
	
}