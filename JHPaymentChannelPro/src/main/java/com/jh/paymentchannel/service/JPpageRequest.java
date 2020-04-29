package com.jh.paymentchannel.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.business.BranchbankBussiness;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.JPBindCard;
import com.jh.paymentchannel.pojo.JPRegister;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.jp.BindSettleCardParam;
import com.jh.paymentchannel.util.jp.BingCardEnsureParam;
import com.jh.paymentchannel.util.jp.BingCardMessageParam;
import com.jh.paymentchannel.util.jp.HttpUtils;
import com.jh.paymentchannel.util.jp.PayParam;
import com.jh.paymentchannel.util.jp.Rates;
import com.jh.paymentchannel.util.jp.RegisterParam;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class JPpageRequest extends BaseChannel {

	private static final Logger LOG = LoggerFactory.getLogger(JPpageRequest.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private BranchbankBussiness branchbankBussiness;

	@Autowired
	private Util util;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	@Autowired
	private RedisTemplate redisTemplate;

	private static final Charset UTF_8 = StandardCharsets.UTF_8;

	// 注册、绑卡获取token请求的URL
	private String RegisterAccessTokenUrl = "https://merchant-wallet.jiedaibao.com/gateway/distributor/main/getAccessToken";

	// 支付获取token请求的URL
	private String PayAccessTokenUrl = "https://merchant-wallet.jiedaibao.com/gateway/merchant/main/getAccessToken";

	private String merchantNum = "D000000003965266";

	private String Key = "96e2109082844cae924be708bf99694a0e37a565696b4f0b84f6b0cf13d29ae7";

	// 获取注册token的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/jp/registergettoken")
	public @ResponseBody Object jpGetRegisterToken(@RequestParam(value = "url") String url) throws Exception {

		Map<String, Object> maps = new HashMap<String, Object>();

		boolean hasKey = false;

		String key = "/v1.0/paymentchannel/topup/jp/registergettoken:url=" + url;
		ValueOperations<String, Object> operations = redisTemplate.opsForValue();
		hasKey = redisTemplate.hasKey(key);
		if (hasKey) {
			return operations.get(key);
		}

		Map<String, Object> map = new HashMap<String, Object>();

		String nonce = RandomStringUtils.randomNumeric(32);

		long timestamp = new Date().getTime();

		map.put("no", merchantNum);
		map.put("Key", Key);
		map.put("nonce", nonce);
		map.put("timestamp", String.valueOf(timestamp));

		String str = "no=" + merchantNum + "&nonce=" + nonce + "&timestamp=" + timestamp + "&key=" + Key;
		String sign = DigestUtils.md5Hex(str).toUpperCase();

		map.put("sign", sign);

		LOG.info("获取token的请求报文======" + map);

		JSONObject fromObject = JSONObject.fromObject(map);

		LOG.info("请求地址======" + url);

		String post = HttpUtils.sendPostjava(url, fromObject.toString());

		LOG.info("请求返回的post======" + post);

		JSONObject fromObject2 = JSONObject.fromObject(post);

		String success = fromObject2.getString("success");
		String errorCode = fromObject2.getString("errorCode");
		String messagemessage = fromObject2.getString("message");
		String notSuccess = fromObject2.getString("notSuccess");

		if ("true".equals(success)) {
			JSONObject value = fromObject2.getJSONObject("value");

			String accessToken = value.getString("accessToken");

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESULT, accessToken);
			maps.put(CommonConstants.RESP_MESSAGE, "查询成功");

			operations.set(key, maps, 2, TimeUnit.MINUTES);
			return maps;

		} else {

			return ResultWrap.init(CommonConstants.FALIED, "获取token失败");
		}

	}

	// 获取支付token的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/jp/paygettoken")
	public @ResponseBody Object jpGetPayToken(@RequestParam(value = "url") String url,
			@RequestParam(value = "merchantNo") String mchNo, @RequestParam(value = "key") String Key)
			throws Exception {

		Map<String, Object> maps = new HashMap<String, Object>();

		boolean hasKey = false;

		String key = "/v1.0/paymentchannel/topup/jp/paygettoken:url=" + url + ";merchantNo=" + mchNo + ";key=" + Key;
		ValueOperations<String, Object> operations = redisTemplate.opsForValue();
		hasKey = redisTemplate.hasKey(key);
		if (hasKey) {
			return operations.get(key);
		}

		Map<String, Object> map = new HashMap<String, Object>();

		String nonce = RandomStringUtils.randomNumeric(32);

		long timestamp = new Date().getTime();

		map.put("no", mchNo);
		map.put("Key", Key);
		map.put("nonce", nonce);
		map.put("timestamp", String.valueOf(timestamp));

		String str = "no=" + mchNo + "&nonce=" + nonce + "&timestamp=" + timestamp + "&key=" + Key;
		String sign = DigestUtils.md5Hex(str).toUpperCase();

		map.put("sign", sign);

		LOG.info("获取token的请求报文======" + map);

		JSONObject fromObject = JSONObject.fromObject(map);

		LOG.info("请求地址======" + url);

		String post = HttpUtils.sendPostjava(url, fromObject.toString());

		LOG.info("请求返回的post======" + post);

		JSONObject fromObject2 = JSONObject.fromObject(post);

		String success = fromObject2.getString("success");
		String errorCode = fromObject2.getString("errorCode");
		String messagemessage = fromObject2.getString("message");
		String notSuccess = fromObject2.getString("notSuccess");

		if ("true".equals(success)) {
			JSONObject value = fromObject2.getJSONObject("value");

			String accessToken = value.getString("accessToken");

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESULT, accessToken);
			maps.put(CommonConstants.RESP_MESSAGE, "查询成功");

			operations.set(key, maps, 2, TimeUnit.MINUTES);
			return maps;

		} else {

			return ResultWrap.init(CommonConstants.FALIED, "获取token失败");
		}

	}

	// 进件接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/jp/register")
	public @ResponseBody Object cjhkRegister(HttpServletRequest request,
			@RequestParam(value = "ordercode") String orderCode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode) throws Exception {
		LOG.info("开始进入进件接口========================");
		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		if (!"000000".equals(queryOrdercode.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", queryOrdercode.get("resp_message"));
			return maps;
		}

		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String userId = resultObj.getString("userid");
		String bankCard = resultObj.getString("bankcard");
		String rate = resultObj.getString("rate");
		String extraFee = resultObj.getString("extraFee");
		String mobile = resultObj.getString("phone");

		Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0", userId);

		Object object2 = queryBankCardByCardNoAndUserId.get("result");
		fromObject = JSONObject.fromObject(object2);

		String idcard = fromObject.getString("idcard");
		String phone = fromObject.getString("phone");
		String bankName = fromObject.getString("bankName");
		String cardtype = fromObject.getString("cardType");

		Map<String, Object> queryBankCardByUserId = this.queryBankCardByUserId(userId);
		if (!"000000".equals(queryBankCardByUserId.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", queryBankCardByUserId.get("resp_message"));
			return maps;
		}

		Object object3 = queryBankCardByUserId.get("result");
		fromObject = JSONObject.fromObject(object3);

		String cardNo = fromObject.getString("cardNo");
		String idcard1 = fromObject.getString("idcard");

		String resjson = null;
		Map<String, Object> reqMap = new HashMap<>();
		RegisterParam param = new RegisterParam();
		Rates rates = new Rates();
		Rates rate1 = new Rates();
		Gson gson = new Gson();

		Map token = (Map) jpGetRegisterToken(RegisterAccessTokenUrl);
		if (!"000000".equals(token.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", "获取token有误,请稍后重试!");
			
			this.addOrderCauseOfFailure(orderCode, "获取token有误,请稍后重试!");
			
			return maps;
		}

		String accessToken = (String) token.get("result");

		param.address = "上海宝山区逸仙路2816号";
		param.alias = "复归网络";
		param.businessClass = "个体户";
		param.businessType = "个体户";
		param.city = "上海";
		param.email = "q355023989@qq.com";
		param.idNumber = idcard;
		param.idType = "ID_CARD";
		param.merchantType = "PERSON";
		param.name = "复归";
		param.outNo = userId;
		param.phone = phone;
		param.province = "上海";
		rates.rate = new BigDecimal(rate).multiply(new BigDecimal("100")).setScale(2).toString();
		rates.type = "SAME_NAME_QUICK_PAY";

		rate1.rate = new BigDecimal(extraFee).multiply(new BigDecimal("100")).setScale(2).toString();
		rate1.type = "WITHDRAW_CASH";

		List<Rates> ratelist = new ArrayList<Rates>();
		ratelist.add(rates);
		ratelist.add(rate1);
		param.rates = ratelist;
		param.shopName = "复归";
		param.telephone = phone;

		reqMap.put("accessToken", accessToken);
		reqMap.put("param", param);

		LOG.info("进件的请求报文======" + reqMap);

		String data = gson.toJson(reqMap);
		String url = "https://merchant-wallet.jiedaibao.com/gateway/distributor/merchant/register";// 参见文档
		try {
			resjson = HttpUtils.sendPost(url, data);
		} catch (IOException e) {
			e.printStackTrace();
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", "交易排队中,请稍后重试!");
			
			this.addOrderCauseOfFailure(orderCode, "交易排队中,请稍后重试!");
			return maps;
		}

		LOG.info("请求进件返回的resjson======" + resjson);

		JSONObject fromObject2 = JSONObject.fromObject(resjson);

		String success = fromObject2.getString("success");
		String errorCode = fromObject2.getString("errorCode");
		String message = fromObject2.getString("message");
		String notSuccess = fromObject2.getString("notSuccess");

		if ("true".equals(success) && "0".equals(errorCode)) {
			LOG.info("进件成功======");

			JSONObject value = fromObject2.getJSONObject("value");
			String no = value.getString("no");
			String outNo = value.getString("outNo");
			String key = value.getString("key");

			JPRegister jpRegister = new JPRegister();

			jpRegister.setPhone(mobile);
			jpRegister.setIdCard(idcard1);
			jpRegister.setMerchantCode(no);
			jpRegister.setEncryptKey(key);
			jpRegister.setRate(rate);
			jpRegister.setExtraFee(extraFee);

			topupPayChannelBusiness.createJPRegister(jpRegister);
			
			Map bindCard = (Map) jpBindSettlementCard(userId, no);

			if ("000000".equals(bindCard.get("resp_code"))) {

				jpRegister.setBankCard(cardNo);

				topupPayChannelBusiness.createJPRegister(jpRegister);

				maps.put("resp_code", "success");
				maps.put("channel_type", "jf");
				maps.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/tojp/bindcard?bankName="
						+ URLEncoder.encode(bankName, "UTF-8") + "&cardType=" + URLEncoder.encode(cardtype, "UTF-8")
						+ "&bankCard=" + bankCard + "&ordercode=" + orderCode + "&expiredTime=" + expiredTime
						+ "&securityCode=" + securityCode + "&ipAddress=" + ipAddress);

				return maps;

			} else {
				this.addOrderCauseOfFailure(orderCode, message);
				
				return ResultWrap.init(CommonConstants.FALIED, message);
			}

		} else {
			this.addOrderCauseOfFailure(orderCode, message);
			
			return ResultWrap.init(CommonConstants.FALIED, message);
		}

	}

	// 绑定结算卡的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/jp/bindSettlementCard")
	public @ResponseBody Object jpBindSettlementCard(@RequestParam(value = "userId") String userId,
			@RequestParam(value = "merchantCode") String merchantCode) throws Exception {

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryBankCardByUserId = this.queryBankCardByUserId(userId);
		if (!"000000".equals(queryBankCardByUserId.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", queryBankCardByUserId.get("resp_message"));
			return maps;
		}

		Object object3 = queryBankCardByUserId.get("result");
		JSONObject fromObject = JSONObject.fromObject(object3);

		String cardNo = fromObject.getString("cardNo");
		String userName1 = fromObject.getString("userName");
		String idcard1 = fromObject.getString("idcard");
		String phone1 = fromObject.getString("phone");
		String bankName1 = fromObject.getString("bankName");

		Map<String, Object> bankUnitNo = this.getBankUnitNo(Util.queryBankNameByBranchName(bankName1));
		if (!"000000".equals(bankUnitNo.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", bankUnitNo.get("resp_message"));

			// this.addOrderCauseOfFailure(orderCode, bankUnitNo.get("resp_message"));

			return maps;
		}

		String inBankUnitNo = (String) bankUnitNo.get("result");

		String resjson1 = null;
		Map<String, Object> reqMap1 = new HashMap<>();
		Gson gson1 = new Gson();
		BindSettleCardParam param1 = new BindSettleCardParam();

		Map token1 = (Map) jpGetRegisterToken(RegisterAccessTokenUrl);
		if (!"000000".equals(token1.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", "获取token有误,请稍后重试!");
			
			this.addOrderCauseOfFailure(merchantCode, "获取token有误,请稍后重试!");
			return maps;
		}

		String accessToken1 = (String) token1.get("result");

		param1.no = merchantCode;
		param1.bankNo = inBankUnitNo;
		param1.bankName = bankName1;
		param1.bankCode = inBankUnitNo;
		param1.cardNo = cardNo;
		param1.cardName = userName1;
		param1.cardPhone = phone1;
		param1.cardIdentity = idcard1;
		reqMap1.put("accessToken", accessToken1);
		reqMap1.put("param", param1);

		LOG.info("绑定结算卡的请求报文======" + reqMap1);

		String data1 = gson1.toJson(reqMap1);
		String url1 = "https://merchant-wallet.jiedaibao.com/gateway/distributor/merchant/tiedSettleCard";// 参见文档
		try {
			resjson1 = HttpUtils.sendPost(url1, data1);
		} catch (IOException e) {
			
			e.printStackTrace();
		}

		LOG.info("请求绑定结算卡返回的resjson1======" + resjson1);

		JSONObject fromObject2 = JSONObject.fromObject(resjson1);

		String success = fromObject2.getString("success");
		String errorCode = fromObject2.getString("errorCode");
		String message = fromObject2.getString("message");
		String notSuccess = fromObject2.getString("notSuccess");

		if ("true".equals(success) && "0".equals(errorCode)) {

			return ResultWrap.init(CommonConstants.SUCCESS, "绑定结算卡成功");
		} else {
			this.addOrderCauseOfFailure(merchantCode, message);
			
			return ResultWrap.init(CommonConstants.FALIED, message);
		}

	}

	// 绑定充值卡获取短信的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/jp/bindcardsms")
	public @ResponseBody Object jpBindCardSMS(HttpServletRequest request,
			@RequestParam(value = "ordercode") String orderCode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode) throws Exception {
		LOG.info("开始进入绑定充值卡接口======");
		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		if (!"000000".equals(queryOrdercode.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", queryOrdercode.get("resp_message"));
			return maps;
		}

		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String userId = resultObj.getString("userid");
		String bankCard = resultObj.getString("bankcard");
		String mobile = resultObj.getString("phone");

		Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0", userId);
		Object object2 = queryBankCardByCardNoAndUserId.get("result");
		fromObject = JSONObject.fromObject(object2);

		String phone = fromObject.getString("phone");
		String idCard = fromObject.getString("idcard");
		String userName = fromObject.getString("userName");
		String bankName = fromObject.getString("bankName");
		String cardtype = fromObject.getString("cardType");

		expiredTime = this.expiredTimeToMMYY(expiredTime);

		LOG.info("转换过的有效期格式======" + expiredTime);

		Map<String, Object> bankUnitNo = this.getBankUnitNo(Util.queryBankNameByBranchName(bankName));
		if (!"000000".equals(bankUnitNo.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", bankUnitNo.get("resp_message"));

			// this.addOrderCauseOfFailure(orderCode, bankUnitNo.get("resp_message"));

			return maps;
		}

		String inBankUnitNo = (String) bankUnitNo.get("result");

		JPRegister jpRegister = topupPayChannelBusiness.getJPRegisterByIdCard(idCard);

		String resjson = null;
		Map<String, Object> reqMap = new HashMap<>();
		Gson gson = new Gson();

		Map token1 = (Map) jpGetRegisterToken(RegisterAccessTokenUrl);
		if (!"000000".equals(token1.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", "获取token有误,请稍后重试!");
			this.addOrderCauseOfFailure(orderCode, "获取token有误,请稍后重试!");
			
			return maps;
		}

		String accessToken1 = (String) token1.get("result");

		BingCardMessageParam param = new BingCardMessageParam();
		param.no = jpRegister.getMerchantCode();
		param.bankNo = inBankUnitNo;
		param.bankName = bankName;
		param.bankCode = inBankUnitNo;
		param.cardNo = bankCard;
		param.cardName = userName;
		param.cardPhone = phone;
		param.cardIdentity = idCard;
		param.validCode = securityCode;
		param.validDates = expiredTime;
		reqMap.put("accessToken", accessToken1);
		reqMap.put("param", param);

		LOG.info("请求绑卡短信的请求报文======param" + param.toString());

		String data = gson.toJson(reqMap);

		// JSONObject data = JSONObject.fromObject(reqMap);

		String url = "https://merchant-wallet.jiedaibao.com//gateway/distributor/merchant/tiedCardInit";
		try {
			resjson = HttpUtils.sendPost(url, data);
		} catch (IOException e) {
			e.printStackTrace();
		}

		LOG.info("请求获取绑定充值卡短信返回的resjson1======" + resjson);

		JSONObject fromObject2 = JSONObject.fromObject(resjson);

		String success = fromObject2.getString("success");
		String errorCode = fromObject2.getString("errorCode");
		String message = fromObject2.getString("message");

		if ("true".equals(success) && "0".equals(errorCode)) {
			JSONObject value = fromObject2.getJSONObject("value");
			String cardSts = value.getString("cardSts");
			String contractId = value.getString("contractId");

			maps.put("resp_code", "success");
			maps.put("channel_type", "jf");
			maps.put("contractId", contractId);
			return maps;
		} else {

			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", message);
			this.addOrderCauseOfFailure(orderCode, message);
			return maps;
		}

	}

	// 确认绑卡接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/jp/bindcardconfirm")
	public @ResponseBody Object BindCardConfirm(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "ordercode") String orderCode, @RequestParam(value = "smsCode") String smsCode,
			@RequestParam(value = "contractIds") String contractId) throws Exception {

		LOG.info("开始进入绑卡确认接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		if (!"000000".equals(queryOrdercode.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", queryOrdercode.get("resp_message"));
			return maps;
		}

		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String userId = resultObj.getString("userid");
		String bankCard = resultObj.getString("bankcard");
		String mobile = resultObj.getString("phone");

		Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0", userId);
		Object object2 = queryBankCardByCardNoAndUserId.get("result");
		fromObject = JSONObject.fromObject(object2);

		String phone = fromObject.getString("phone");
		String idCard = fromObject.getString("idcard");
		String userName = fromObject.getString("userName");
		String bankName = fromObject.getString("bankName");
		String cardtype = fromObject.getString("cardType");

		Map<String, Object> bankUnitNo = this.getBankUnitNo(Util.queryBankNameByBranchName(bankName));
		if (!"000000".equals(bankUnitNo.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", bankUnitNo.get("resp_message"));

			// this.addOrderCauseOfFailure(orderCode, bankUnitNo.get("resp_message"));

			return maps;
		}

		String inBankUnitNo = (String) bankUnitNo.get("result");

		JPRegister jpRegister = topupPayChannelBusiness.getJPRegisterByIdCard(idCard);

		String resjson = null;
		Map<String, Object> reqMap = new HashMap<>();
		Gson gson = new Gson();

		Map token1 = (Map) jpGetRegisterToken(RegisterAccessTokenUrl);
		if (!"000000".equals(token1.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", "获取token有误,请稍后重试!");
			return maps;
		}

		String accessToken1 = (String) token1.get("result");

		BingCardEnsureParam param = new BingCardEnsureParam();
		param.no = jpRegister.getMerchantCode();
		param.contractId = contractId;
		param.checkCode = smsCode;
		reqMap.put("accessToken", accessToken1);
		reqMap.put("param", param);

		String data = gson.toJson(reqMap);
		String url = "https://merchant-wallet.jiedaibao.com//gateway/distributor/merchant/tiedCardCommit";
		try {
			resjson = HttpUtils.sendPost(url, data);
		} catch (IOException e) {
			e.printStackTrace();
		}

		LOG.info("请求绑定充值卡返回的resjson1======" + resjson);

		JSONObject fromObject2 = JSONObject.fromObject(resjson);

		String success = fromObject2.getString("success");
		String errorCode = fromObject2.getString("errorCode");
		String message = fromObject2.getString("message");

		if ("true".equals(success) && "0".equals(errorCode)) {
			JSONObject value = fromObject2.getJSONObject("value");
			String cardSts = value.getString("cardSts");
			String contractId1 = value.getString("contractId");

			if ("0".equals(cardSts)) {

				JPBindCard jpBindCard = new JPBindCard();
				jpBindCard.setPhone(phone);
				jpBindCard.setBankCard(bankCard);
				jpBindCard.setIdCard(idCard);
				jpBindCard.setContractId(contractId1);
				jpBindCard.setStatus("1");

				topupPayChannelBusiness.createJPBindCard(jpBindCard);

				maps.put("resp_code", "success");
				maps.put("channel_type", "jf");
				maps.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/tojp/pay?bankName="
						+ URLEncoder.encode(bankName, "UTF-8") + "&cardType=" + URLEncoder.encode(cardtype, "UTF-8")
						+ "&bankCard=" + bankCard + "&ordercode=" + orderCode + "&ipAddress=" + ipAddress);

				return maps;

			} else {

				maps.put("resp_code", "failed");
				maps.put("channel_type", "jf");
				maps.put("resp_message", message);
				
				this.addOrderCauseOfFailure(orderCode, message);
				return maps;
			}

		} else {

			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", message);
			
			this.addOrderCauseOfFailure(orderCode, message);
			return maps;
		}

	}

	// 获取支付短信验证码接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/jp/getsmscode")
	public @ResponseBody Object jpFastPaySMS(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode) throws Exception {
		LOG.info("开始进入获取支付短信验证码接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");
		String amount = resultObj.getString("amount");
		String userId = resultObj.getString("userid");

		Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0", userId);
		Object object2 = queryBankCardByCardNoAndUserId.get("result");
		fromObject = JSONObject.fromObject(object2);

		String phone = fromObject.getString("phone");
		String idCard = fromObject.getString("idcard");
		String userName = fromObject.getString("userName");
		String bankName = fromObject.getString("bankName");

		JPRegister jpRegister = topupPayChannelBusiness.getJPRegisterByIdCard(idCard);
		JPBindCard jpBindCard = topupPayChannelBusiness.getJPBindCardByBankCard(bankCard);

		Map token1 = (Map) jpGetPayToken(PayAccessTokenUrl, jpRegister.getMerchantCode(), jpRegister.getEncryptKey());
		if (!"000000".equals(token1.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", "获取token有误,请稍后重试!");
			return maps;
		}

		// 将金额转换为以分为单位:
		String Amount = new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0).toString();

		String accessToken1 = (String) token1.get("result");

		String resjson = null;
		Map<String, Object> reqMap = new HashMap<>();
		Gson gson = new Gson();

		PayParam param = new PayParam();
		param.outTradeNo = ordercode;
		param.money = Long.parseLong(Amount);
		param.type = "T0";
		param.body = "充值缴费";
		param.detail = "充值缴费";
		param.notifyUrl = ipAddress + "/v1.0/paymentchannel/topup/jp/fastpay/notify_call";
		param.productId = UUID.randomUUID().toString().substring(0, 15).replace("-", "");
		param.cardName = userName;
		param.cardNo = bankCard;
		param.contractId = jpBindCard.getContractId();
		reqMap.put("accessToken", accessToken1);
		reqMap.put("param", param);

		String data = gson.toJson(reqMap);
		String url = "https://merchant-wallet.jiedaibao.com/gateway/merchant/sameNameQuickPay";
		try {
			resjson = HttpUtils.sendPost(url, data);
		} catch (IOException e) {
			e.printStackTrace();
		}

		LOG.info("请求获取支付短信返回的resjson1======" + resjson);

		JSONObject fromObject2 = JSONObject.fromObject(resjson);

		String success = fromObject2.getString("success");
		String errorCode = fromObject2.getString("errorCode");
		String message = fromObject2.getString("message");

		if ("true".equals(success) || "0".equals(errorCode)) {
			String orderId = fromObject2.getString("value");

			maps.put("resp_code", "success");
			maps.put("channel_type", "jf");
			maps.put("orderId", orderId);
			return maps;
		} else {

			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", message);
			
			this.addOrderCauseOfFailure(ordercode, message);
			return maps;
		}

	}

	// 确认支付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/jp/fastpay")
	public @ResponseBody Object consumeSMS(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "ordercode") String ordercode, @RequestParam(value = "smsCode") String smsCode,
			@RequestParam(value = "orderId") String orderId) throws Exception {

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String bankCard = resultObj.getString("bankcard");
		String amount = resultObj.getString("amount");
		String userId = resultObj.getString("userid");

		Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0", userId);
		Object object2 = queryBankCardByCardNoAndUserId.get("result");
		fromObject = JSONObject.fromObject(object2);

		String idCard = fromObject.getString("idcard");
		String userName = fromObject.getString("userName");

		JPRegister jpRegister = topupPayChannelBusiness.getJPRegisterByIdCard(idCard);
		JPBindCard jpBindCard = topupPayChannelBusiness.getJPBindCardByBankCard(bankCard);

		Map token1 = (Map) jpGetPayToken(PayAccessTokenUrl, jpRegister.getMerchantCode(), jpRegister.getEncryptKey());
		if (!"000000".equals(token1.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", "获取token有误,请稍后重试!");
			return maps;
		}

		// 将金额转换为以分为单位:
		String Amount = new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0).toString();

		String accessToken1 = (String) token1.get("result");

		String resjson = null;
		Map<String, Object> reqMap = new HashMap<>();
		Gson gson = new Gson();

		PayParam param = new PayParam();
		param.orderId = orderId;
		param.outTradeNo = ordercode;
		param.money = Long.parseLong(Amount);
		param.type = "T0";
		param.body = "充值缴费";
		param.detail = "充值缴费";
		param.notifyUrl = ipAddress + "/v1.0/paymentchannel/topup/jp/fastpay/notify_call";
		param.productId = UUID.randomUUID().toString().substring(0, 15).replace("-", "");
		param.cardName = userName;
		param.cardNo = bankCard;
		param.messageCode = smsCode;
		param.contractId = jpBindCard.getContractId();
		reqMap.put("accessToken", accessToken1);
		reqMap.put("param", param);

		String data = gson.toJson(reqMap);
		String url = "https://merchant-wallet.jiedaibao.com/gateway/merchant/paySameNameQuickPay";
		try {
			resjson = HttpUtils.sendPost(url, data);
		} catch (IOException e) {
			e.printStackTrace();
		}

		LOG.info("请求支付返回的resjson1======" + resjson);

		JSONObject fromObject21 = JSONObject.fromObject(resjson);

		String success = fromObject21.getString("success");
		String errorCode = fromObject21.getString("errorCode");
		String message = fromObject21.getString("message");

		if ("true".equals(success) && "0".equals(errorCode)) {
			LOG.info("请求支付成功======");

			maps.put("resp_code", "success");
			maps.put("channel_type", "jf");
			maps.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/sdjpaysuccess");

			return maps;

		} else {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", "");

			this.addOrderCauseOfFailure(ordercode, message);

			return maps;
		}

	}

	// 订单查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/jp/ordercodequery")
	public @ResponseBody Object orderCodeQuery(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode) throws Exception {
		LOG.info("开始进入订单查询接口======");
		Map<String, String> maps = new HashMap<String, String>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String bankCard = resultObj.getString("bankcard");

		Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
		Object object2 = queryBankCardByCardNo.get("result");
		fromObject = JSONObject.fromObject(object2);

		String idCard = fromObject.getString("idcard");

		JPRegister jpRegister = topupPayChannelBusiness.getJPRegisterByIdCard(idCard);

		Map token1 = (Map) jpGetPayToken(PayAccessTokenUrl, jpRegister.getMerchantCode(), jpRegister.getEncryptKey());
		if (!"000000".equals(token1.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", "获取token有误,请稍后重试!");
			return maps;
		}

		String accessToken1 = (String) token1.get("result");

		String resjson = null;
		Map<String, Object> reqMap = new HashMap<>();
		Gson gson = new Gson();

		reqMap.put("accessToken", accessToken1);
		reqMap.put("param", ordercode);

		String data = gson.toJson(reqMap);
		String url = "https://merchant-wallet.jiedaibao.com/gateway/merchant/order/getByOutNo";
		try {
			resjson = HttpUtils.sendPost(url, data);
		} catch (IOException e) {
			e.printStackTrace();
		}

		LOG.info("请求订单查询接口返回的resjson1======" + resjson);

		JSONObject fromObject2 = JSONObject.fromObject(resjson);

		return null;

	}

	// 跳转绑卡页面
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/tojp/bindcard")
	public String returnCJBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

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

		return "jpbindcard";
	}

	// 跳转支付页面
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/tojp/pay")
	public String returnHLJCQuickPay(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String ordercode = request.getParameter("ordercode");
		String bankName = request.getParameter("bankName");
		String cardType = request.getParameter("cardType");
		String bankCard = request.getParameter("bankCard");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("ordercode", ordercode);
		model.addAttribute("bankName", bankName);
		model.addAttribute("cardType", cardType);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("ipAddress", ipAddress);

		return "jppaymessage";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/tojp/bindcards")
	public @ResponseBody Object returnCJQuickBindCard(HttpServletRequest request,
			@RequestParam(value = "ordercode") String orderCode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode) throws IOException {

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		if (!"000000".equals(queryOrdercode.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", queryOrdercode.get("resp_message"));
			return maps;
		}

		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String bankCard = resultObj.getString("bankcard");

		Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
		if (!"000000".equals(queryBankCardByCardNo.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", queryBankCardByCardNo.get("resp_message"));
			return maps;
		}

		Object object2 = queryBankCardByCardNo.get("result");
		fromObject = JSONObject.fromObject(object2);

		String bankName = fromObject.getString("bankName");
		String cardtype = fromObject.getString("cardType");

		maps.put("resp_code", "success");
		maps.put("channel_type", "jf");
		maps.put("redirect_url",
				ipAddress + "/v1.0/paymentchannel/topup/tojp/bindcard?bankName=" + URLEncoder.encode(bankName, "UTF-8")
						+ "&cardType=" + URLEncoder.encode(cardtype, "UTF-8") + "&bankCard=" + bankCard + "&ordercode="
						+ orderCode + "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&ipAddress="
						+ ipAddress);

		return maps;
	}

	// 快捷支付异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/jp/fastpay/notify_call")
	public void hljcFastPayNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("快捷支付异步通知进来了=======");

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
		JSONObject jsonInfo = JSONObject.fromObject(info);
		LOG.info("jsonInfo=============" + jsonInfo.toString());
		inputStream.close();
		byteArray.close();

		String resString = "fail";
		String outTradeNo = jsonInfo.getString("outTradeNo");
		String no = jsonInfo.getString("no");

		this.updateOrderCode(outTradeNo, "1", no);

		LOG.info("订单状态修改成功===================" + outTradeNo + "====================" + "");

		LOG.info("订单已支付!");

		PrintWriter writer = response.getWriter();
		writer.print("SUCCESS");
		writer.close();

	}

	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/tojpbankinfo")
	public String toCJbankinfo(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		LOG.info("/v1.0/paymentchannel/topup/tocjbankinfo=========tocjbankinfo");
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

		return "jpbankinfo";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/tojp/paypage")
	public @ResponseBody Object returnHLJCQuickPayPage(HttpServletRequest request,
			@RequestParam(value = "ordercode") String orderCode) throws IOException {

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String bankCard = resultObj.getString("bankcard");
		String userId = resultObj.getString("userid");

		Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0", userId);
		Object object2 = queryBankCardByCardNoAndUserId.get("result");
		fromObject = JSONObject.fromObject(object2);

		String bankName = fromObject.getString("bankName");
		String cardtype = fromObject.getString("cardType");

		maps.put("resp_code", "success");
		maps.put("channel_type", "jf");
		maps.put("redirect_url",
				ipAddress + "/v1.0/paymentchannel/topup/tojp/pay?bankName=" + URLEncoder.encode(bankName, "UTF-8")
						+ "&cardType=" + URLEncoder.encode(cardtype, "UTF-8") + "&bankCard=" + bankCard + "&ordercode="
						+ orderCode + "&ipAddress=" + ipAddress);

		return maps;
	}

	
	//银行卡解绑接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/jp/unbindcard")
	public @ResponseBody Object jpUnBindCard(@RequestParam(value = "bankCard") String bankCard
			) throws IOException {

		Map<String, Object> maps = new HashMap<String, Object>();


		

	
		return maps;
	}
	
	
}