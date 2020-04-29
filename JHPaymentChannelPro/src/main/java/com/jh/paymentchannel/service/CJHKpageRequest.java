package com.jh.paymentchannel.service;

import java.io.PrintWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import com.alibaba.fastjson.JSON;
import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.business.BranchbankBussiness;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.CJHKBindCard;
import com.jh.paymentchannel.pojo.CJHKRegister;
import com.jh.paymentchannel.pojo.CJQuickBindCard;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.cjhk.AESUtil;
import com.jh.paymentchannel.util.cjhk.ApiClient;
import com.jh.paymentchannel.util.cjhk.ApiRequest;
import com.jh.paymentchannel.util.cjhk.ApiResponse;
import com.jh.paymentchannel.util.cjhk.EncryptTypeEnum;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class CJHKpageRequest extends BaseChannel {

	private static final Logger LOG = LoggerFactory.getLogger(CJHKpageRequest.class);

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

	private String merchantNo = "121804261562";

	private String Url = "http://120.55.52.144:8899/";

	private String key = "914a8687201f4134a426faa603c6e06b";

	private String channelProductCode = "CHANPAYNEWCARD_04";

	private static final Charset UTF_8 = StandardCharsets.UTF_8;
	
	
	// 还款对接的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cjhk/to/repayment")
	public @ResponseBody Object HLJCRegister(HttpServletRequest request,
			@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "rate") String rate,
			@RequestParam(value = "extraFee") String extraFee, @RequestParam(value = "userId") String userId,
			@RequestParam(value = "returnUrl", required = false) String returnUrl) throws Exception {

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

		String idCard = fromObject.getString("idcard");

		CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idCard);

		CJHKBindCard cjhkBindCard = topupPayChannelBusiness.getCJHKBindCardByBankCard(bankCard);

		if (cjhkRegister == null) {
			map = (Map<String, Object>) cjhkRegister(request, bankCard, rate, extraFee, userId);
			Object respCode = map.get("resp_code");
			Object respMessage = map.get("resp_message");
			LOG.info("respCode=====" + respCode);

			if ("000000".equals(respCode.toString())) {
				LOG.info("进件成功,开始绑卡======");
				map = null;
				Object respCode1 = map.get("resp_code");
				Object respMessage1 = map.get("resp_message");
				if ("000000".equals(respCode1.toString())) {

					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, respMessage1);
					return maps;
				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, respMessage1);
					return maps;
				}

			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			}

		} else {
			CJHKRegister cjhkRegister2 = topupPayChannelBusiness.getCJHKRegisterByIdCardAndVersion(idCard, "1");

			if (cjhkRegister2 == null) {

				LOG.info("需要开通产品======");
				map = (Map<String, Object>) cjhkProduct(request, bankCard, rate, extraFee);
				Object respCode = map.get("resp_code");
				Object respMessage = map.get("resp_message");
				LOG.info("respCode=====" + respCode);

				if ("000000".equals(respCode.toString())) {
					LOG.info("开通产品成功,开始绑卡======");
					maps = null;
					return maps;

				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, respMessage);
					return maps;
				}

			} else {

				if (cjhkBindCard == null || !"1".equals(cjhkBindCard.getStatus())) {
					maps = null;
					return maps;
				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, "已完成进件和绑卡");
					return maps;
				}
			}

		}

	}

	// 进件接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cjhk/register")
	public @ResponseBody Object cjhkRegister(HttpServletRequest request,
			@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "rate") String rate,
			@RequestParam(value = "extraFee") String extraFee, @RequestParam(value = "userId") String userId)
			throws Exception {
		LOG.info("开始进入进件接口========================");
		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryBankCardByUserId = this.queryBankCardByUserId(userId);
		if (!"000000".equals(queryBankCardByUserId.get("resp_code"))) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put("resp_message", "该通道需绑定默认提现借记卡!");
			return maps;
		}

		Object object3 = queryBankCardByUserId.get("result");
		JSONObject fromObject = JSONObject.fromObject(object3);

		String cardNo = fromObject.getString("cardNo");
		String userName1 = fromObject.getString("userName");
		String idcard1 = fromObject.getString("idcard");
		String phone1 = fromObject.getString("phone");
		String bankName1 = fromObject.getString("bankName");
		// String bankBranchName1 = fromObject.getString("bankBranchName");
		// String province1 = fromObject.getString("province");
		// String city1 = fromObject.getString("city");

		Map<String, Object> bankUnitNo = this.getBankUnitNo(Util.queryBankNameByBranchName(bankName1));
		if (!"000000".equals(bankUnitNo.get("resp_code"))) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put("resp_message", bankUnitNo.get("resp_message"));
			return maps;
		}

		String inBankUnitNo = (String) bankUnitNo.get("result");

		Map<String, Object> bankCodeByBankName = this.getBankCodeByBankName(Util.queryBankNameByBranchName(bankName1));
		if (!"000000".equals(bankCodeByBankName.get("resp_code"))) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put("resp_message", bankCodeByBankName.get("resp_message"));
			return maps;
		}

		String code = (String) bankCodeByBankName.get("result");

		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("requestNo", String.valueOf(System.currentTimeMillis()));
		apiRequest.addParam("merchantName", "上海莘丽网络");
		apiRequest.addParam("shortName", "莘丽");
		apiRequest.addParam("channelProductCode", channelProductCode);
		apiRequest.addParam("bindMobile", phone1);
		apiRequest.addParam("bindEmail", "q355023989@qq.com");
		apiRequest.addParam("address", "上海宝山区逸仙路2816号");
		apiRequest.addParam("idCardNo", idcard1);
		apiRequest.addParam("settleBankAccountNo", cardNo);
		apiRequest.addParam("settleBankAccountName", userName1);
		apiRequest.addParam("settleBankAccountType", "PRIVATE");
		apiRequest.addParam("settleBankName", bankName1);
		apiRequest.addParam("settleBankSubName", "上海宝山支行");
		apiRequest.addParam("settleBankAbbr", code);
		apiRequest.addParam("settleBankChannelNo", inBankUnitNo);
		apiRequest.addParam("settleBankCardProvince", "上海");
		apiRequest.addParam("settleBankCardCity", "上海");
		apiRequest.addParam("settlementType", "D0");
		apiRequest.addParam("debitRate", rate);
		apiRequest.addParam("creditRate", rate);
		apiRequest.addParam("withdrawDepositRate", "0");
		apiRequest.addParam("withdrawDepositSingleFee", extraFee);

		LOG.info("进件的请求报文 apiRequest======" + apiRequest.toString());

		ApiResponse post = ApiClient.post(Url + "/rest/v1.0/paybar/registMerchant", apiRequest);

		LOG.info("请求返回的 post======" + JSON.toJSONString(post));

		JSONObject fromObject2 = JSONObject.fromObject(post);

		String state = fromObject2.getString("state");
		LOG.info("state======" + state);
		JSONObject resultMap = fromObject2.getJSONObject("resultMap");

		JSONObject fromObject3 = JSONObject.fromObject(resultMap);
		LOG.info("fromObject3======" + fromObject3);
		String bizCode = fromObject3.getString("bizCode");
		String bizMsg = fromObject3.getString("bizMsg");
		String code1 = fromObject3.getString("code");

		if ("200".equals(code1) && "1".equals(bizCode)) {
			if ("SUCCESS".equals(state)) {
				String encryptKey = fromObject3.getString("encryptKey");
				String merchantCode = fromObject3.getString("merchantNo");

				CJHKRegister cjhkRegister = new CJHKRegister();
				cjhkRegister.setPhone(phone1);
				cjhkRegister.setBankCard(cardNo);
				cjhkRegister.setIdCard(idcard1);
				cjhkRegister.setMerchantCode(merchantCode);
				cjhkRegister.setEncryptKey(encryptKey);
				cjhkRegister.setRate(rate);
				cjhkRegister.setExtraFee(extraFee);
				cjhkRegister.setVersion("1");

				topupPayChannelBusiness.createCJHKRegister(cjhkRegister);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);

				return maps;
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;
			}
		} else {
			LOG.info("请求失败====");
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
			return maps;
		}

	}

	// 开通产品的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cjhk/product")
	public @ResponseBody Object cjhkProduct(HttpServletRequest request,
			@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "rate") String rate,
			@RequestParam(value = "extraFee") String extraFee) throws Exception {

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

		String idCard = fromObject.getString("idcard");

		CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idCard);

		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("merchantNo ", cjhkRegister.getMerchantCode());
		apiRequest.addParam("channelProductCode", channelProductCode);
		apiRequest.addParam("debitRate", rate);
		apiRequest.addParam("debitCapAmount", "999999");
		apiRequest.addParam("creditRate", rate);
		apiRequest.addParam("creditCapAmount", "999999");

		LOG.info("开通产品的请求报文 apiRequest======" + apiRequest.toString());

		ApiResponse post = ApiClient.post(Url + "/rest/v1.0/paybar/registMerchantProduct", apiRequest);

		LOG.info("请求返回的 post======" + JSON.toJSONString(post));

		JSONObject fromObject2 = JSONObject.fromObject(post);

		String state = fromObject2.getString("state");
		LOG.info("state======" + state);
		JSONObject resultMap = fromObject2.getJSONObject("resultMap");

		JSONObject fromObject3 = JSONObject.fromObject(resultMap);
		LOG.info("fromObject3======" + fromObject3);
		String bizCode = fromObject3.getString("bizCode");
		String bizMsg = fromObject3.getString("bizMsg");
		String code1 = fromObject3.getString("code");

		if ("200".equals(code1) && "1".equals(bizCode)) {
			if ("SUCCESS".equals(state)) {

				cjhkRegister.setRate(rate);
				cjhkRegister.setExtraFee(extraFee);
				cjhkRegister.setVersion("1");

				topupPayChannelBusiness.createCJHKRegister(cjhkRegister);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);

				return maps;
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put("channel_type", "sdj");
				maps.put("resp_message", bizMsg);

				return maps;
			}
		} else {
			LOG.info("请求失败====");
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put("resp_message", bizMsg);

			return maps;
		}

	}



	
	
	// 获取绑卡短信接口
		@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cjhk/bindcardsms")
		public @ResponseBody Object cjhkBindCardSMS(HttpServletRequest request,
				@RequestParam(value = "orderCode") String orderCode, @RequestParam(value = "bankCard") String bankCard,
				@RequestParam(value = "expiredTime", required = false) String expiredTime,
				@RequestParam(value = "securityCode", required = false) String securityCode) throws Exception {
			LOG.info("开始进入获取绑卡短信的接口======");
			Map<String, Object> maps = new HashMap<String, Object>();

			Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
			Object object2 = queryBankCardByCardNo.get("result");
			JSONObject fromObject = JSONObject.fromObject(object2);

			String phone = fromObject.getString("phone");
			String idCard = fromObject.getString("idcard");
			String userName = fromObject.getString("userName");
			String bankName = fromObject.getString("bankName");
			String cardtype = fromObject.getString("cardType");

			expiredTime = this.expiredTimeToYYMM(expiredTime);

			LOG.info("转换过的有效期格式======" + expiredTime);

			Map<String, Object> bankCodeByBankName = this.getBankCodeByBankName(Util.queryBankNameByBranchName(bankName));
			if (!"000000".equals(bankCodeByBankName.get("resp_code"))) {
				maps.put("resp_code", "failed");
				maps.put("channel_type", "jf");
				maps.put("resp_message", bankCodeByBankName.get("resp_message"));

				return maps;
			}

			String code = (String) bankCodeByBankName.get("result");

			CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idCard);

			// String orderNo = UUID.randomUUID().toString().replace("-", "");

			String requestNo = String.valueOf(System.currentTimeMillis());

			ApiRequest apiRequest = new ApiRequest(merchantNo, key);
			apiRequest.setSupportSign(false);
			apiRequest.setEncryptType(EncryptTypeEnum.AES);
			apiRequest.addParam("merchantNo", cjhkRegister.getMerchantCode());
			apiRequest.addParam("requestNo", orderCode);
			apiRequest.addParam("channelProductCode", channelProductCode);
			apiRequest.addParam("bankCardNo", bankCard);
			apiRequest.addParam("bankAccountName", userName);
			apiRequest.addParam("cardType", "CREDIT");
			apiRequest.addParam("bankMobile ", phone);
			apiRequest.addParam("certType", "PRC_ID");
			apiRequest.addParam("certNo", idCard);
			apiRequest.addParam("cvn2", securityCode);
			apiRequest.addParam("expired", expiredTime);
			apiRequest.addParam("bankAbbr", code);
			apiRequest.addParam("serverCallbackUrl", ipAddress + "/v1.0/paymentchannel/topup/cjquick/bindcard/notify_call");

			LOG.info("获取绑卡短信的请求报文 apiRequest======" + apiRequest.toString());

			ApiResponse post = ApiClient.post(Url + "/rest/v1.0/paybar/bindCard", apiRequest);

			LOG.info("请求获取绑卡短信返回的 post======" + JSON.toJSONString(post));

			JSONObject fromObject2 = JSONObject.fromObject(post);

			String state = fromObject2.getString("state");

			JSONObject resultMap = fromObject2.getJSONObject("resultMap");

			JSONObject fromObject3 = JSONObject.fromObject(resultMap);
			LOG.info("fromObject3======" + fromObject3);
			String bizCode = fromObject3.getString("bizCode");
			String bizMsg = fromObject3.getString("bizMsg");
			String code1 = fromObject3.getString("code");

			if ("200".equals(code1) && "1".equals(bizCode)) {
				if ("SUCCESS".equals(state)) {

					maps.put("resp_code", "success");
					maps.put("channel_type", "jf");
					maps.put("resp_message", "短信发送成功");

					return maps;
				} else {
					maps.put("resp_code", "failed");
					maps.put("channel_type", "jf");
					maps.put("resp_message", bizMsg);

					return maps;
				}

			} else {
				maps.put("resp_code", "failed");
				maps.put("channel_type", "jf");
				maps.put("resp_message", bizMsg);

				return maps;
			}

		}

		// 绑卡确认接口
		@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cjhk/bindcardconfirm")
		public @ResponseBody Object BindCardConfirm(HttpServletRequest request, HttpServletResponse response,
				@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "orderCode") String orderCode,
				@RequestParam(value = "smsCode") String smsCode,
				@RequestParam(value = "expiredTime", required = false) String expiredTime,
				@RequestParam(value = "securityCode", required = false) String securityCode) throws Exception {

			Map<String, Object> maps = new HashMap<String, Object>();

			Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
			Object object2 = queryBankCardByCardNo.get("result");
			JSONObject fromObject = JSONObject.fromObject(object2);

			String phone = fromObject.getString("phone");
			String idCard = fromObject.getString("idcard");
			String bankName = fromObject.getString("bankName");
			String cardtype = fromObject.getString("cardType");

			CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idCard);

			ApiRequest apiRequest = new ApiRequest(merchantNo, key);
			apiRequest.setSupportSign(false);
			apiRequest.setEncryptType(EncryptTypeEnum.AES);
			apiRequest.addParam("merchantNo", cjhkRegister.getMerchantCode());
			apiRequest.addParam("bindRequestNo", orderCode);
			apiRequest.addParam("smsCode", smsCode);

			LOG.info("确认绑卡的请求报文 apiRequest======" + apiRequest.toString());

			ApiResponse post = ApiClient.post(Url + "/rest/v1.0/paybar/confirmBindCard", apiRequest);

			LOG.info("请求返回的 post======" + JSON.toJSONString(post));

			JSONObject fromObject2 = JSONObject.fromObject(post);

			String state = fromObject2.getString("state");

			JSONObject resultMap = fromObject2.getJSONObject("resultMap");

			JSONObject fromObject3 = JSONObject.fromObject(resultMap);
			LOG.info("fromObject3======" + fromObject3);

			String bizCode = fromObject3.getString("bizCode");
			String code = fromObject3.getString("code");
			String bizMsg = fromObject3.getString("bizMsg");

			if ("200".equals(code) && "1".equals(bizCode)) {
				if ("SUCCESS".equals(state)) {
					LOG.info("请求绑卡成功======");
					String bindStatus = fromObject3.getString("bindStatus");

					if ("SUCCESS".equalsIgnoreCase(bindStatus)) {

						CJHKBindCard cjQuickBindCard = new CJHKBindCard();
						cjQuickBindCard.setPhone(phone);
						cjQuickBindCard.setIdCard(idCard);
						cjQuickBindCard.setBankCard(bankCard);
						cjQuickBindCard.setStatus("1");

						topupPayChannelBusiness.createCJHKBindCard(cjQuickBindCard);

						maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						maps.put("channel_type", "sdj");
						maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
						return maps;
					} else {

						CJHKBindCard cjQuickBindCard = new CJHKBindCard();
						cjQuickBindCard.setPhone(phone);
						cjQuickBindCard.setIdCard(idCard);
						cjQuickBindCard.setBankCard(bankCard);
						cjQuickBindCard.setStatus("0");

						topupPayChannelBusiness.createCJHKBindCard(cjQuickBindCard);

						maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						maps.put("channel_type", "sdj");
						maps.put(CommonConstants.RESP_MESSAGE, "等待绑卡成功!");
						return maps;
					}

				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put("channel_type", "sdj");
					maps.put(CommonConstants.RESP_MESSAGE, "绑卡失败请重试!");
					return maps;
				}

			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, "绑卡失败请重试!");
				return maps;
			}

		}
	
	
	

	// 修改交易费率接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cjhk/updatemerchant")
	public @ResponseBody Object cjhkUpdateMerchant(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode) throws Exception {

		Map<String, String> maps = new HashMap<String, String>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		// 费率
		String rate = resultObj.getString("rate");
		// 额外费率
		String extraFee = resultObj.getString("extraFee");
		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");

		Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
		Object object2 = queryBankCardByCardNo.get("result");
		fromObject = JSONObject.fromObject(object2);

		String idcard = fromObject.getString("idcard");

		CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idcard);

		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("merchantNo", cjhkRegister.getMerchantCode());
		apiRequest.addParam("channelProductCode", channelProductCode);

		apiRequest.addParam("cardType", "CREDIT");
		apiRequest.addParam("bizType", "TRADE");

		apiRequest.addParam("feeValue ", rate);
		apiRequest.addParam("capAmount", "999999");

		LOG.info("修改费率的请求报文 apiRequest======" + apiRequest.toString());

		ApiResponse post = ApiClient.post(Url + "/rest/v1.0/paybar/modifyMerchantFeeInfo", apiRequest);

		LOG.info("请求返回的 post======" + JSON.toJSONString(post));

		JSONObject fromObject2 = JSONObject.fromObject(post);

		String state = fromObject2.getString("state");
		LOG.info("state======" + state);
		JSONObject resultMap = fromObject2.getJSONObject("resultMap");

		JSONObject fromObject3 = JSONObject.fromObject(resultMap);
		LOG.info("fromObject3======" + fromObject3);

		String bizCode = fromObject3.getString("bizCode");
		String bizMsg = fromObject3.getString("bizMsg");
		String code1 = fromObject3.getString("code");

		if ("200".equals(code1) && "1".equals(bizCode)) {

			cjhkRegister.setRate(rate);
			//cjhkRegister.setExtraFee(extraFee);
			
			topupPayChannelBusiness.createCJHKRegister(cjhkRegister);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
			return maps;

		} else {

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
			return maps;
		}

	}

	
	// 修改代付费用接口
		@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cjhk/updatewithdrawfee")
		public @ResponseBody Object cjhkUpdateWithDrawFee(HttpServletRequest request,
				@RequestParam(value = "ordercode") String ordercode) throws Exception {

			Map<String, String> maps = new HashMap<String, String>();

			Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
			Object object = queryOrdercode.get("result");
			JSONObject fromObject = JSONObject.fromObject(object);
			JSONObject resultObj = fromObject.getJSONObject("result");

			// 费率
			String rate = resultObj.getString("rate");
			// 额外费率
			String extraFee = resultObj.getString("extraFee");
			// 充值卡卡号
			String bankCard = resultObj.getString("bankcard");

			Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
			Object object2 = queryBankCardByCardNo.get("result");
			fromObject = JSONObject.fromObject(object2);

			String idcard = fromObject.getString("idcard");

			CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idcard);

			ApiRequest apiRequest = new ApiRequest(merchantNo, key);
			apiRequest.setSupportSign(false);
			apiRequest.setEncryptType(EncryptTypeEnum.AES);
			apiRequest.addParam("merchantNo", cjhkRegister.getMerchantCode());
			apiRequest.addParam("channelProductCode", channelProductCode);

			apiRequest.addParam("cardType", "DEBIT");
			apiRequest.addParam("bizType", "WITHDRAW_SIGLE");

			apiRequest.addParam("feeValue ", extraFee);
			apiRequest.addParam("capAmount", "999999");

			LOG.info("修改费率的请求报文 apiRequest======" + apiRequest.toString());

			ApiResponse post = ApiClient.post(Url + "/rest/v1.0/paybar/modifyMerchantFeeInfo", apiRequest);

			LOG.info("请求返回的 post======" + JSON.toJSONString(post));

			JSONObject fromObject2 = JSONObject.fromObject(post);

			String state = fromObject2.getString("state");
			LOG.info("state======" + state);
			JSONObject resultMap = fromObject2.getJSONObject("resultMap");

			JSONObject fromObject3 = JSONObject.fromObject(resultMap);
			LOG.info("fromObject3======" + fromObject3);

			String bizCode = fromObject3.getString("bizCode");
			String bizMsg = fromObject3.getString("bizMsg");
			String code1 = fromObject3.getString("code");

			if ("200".equals(code1) && "1".equals(bizCode)) {

				//cjhkRegister.setRate(rate);
				cjhkRegister.setExtraFee(extraFee);
				
				topupPayChannelBusiness.createCJHKRegister(cjhkRegister);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;

			} else {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;
			}

		}
	
	
	
	// 快捷支付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cjhk/fastpay")
	public @ResponseBody Object cjhkFastPay(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode, @RequestParam(value = "product") String product,
			@RequestParam(value = "goods") String goods) throws Exception {
		LOG.info("开始进入快捷支付接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");
		String realAmount = resultObj.getString("realAmount");
		String amount = resultObj.getString("amount");

		Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
		Object object2 = queryBankCardByCardNo.get("result");
		fromObject = JSONObject.fromObject(object2);

		String phone = fromObject.getString("phone");
		String idCard = fromObject.getString("idcard");
		String securityCode = fromObject.getString("securityCode");
		String expiredTime = fromObject.getString("expiredTime");

		expiredTime = this.expiredTimeToYYMM(expiredTime);

		LOG.info("转换过的有效期格式======" + expiredTime);

		CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idCard);

		// String Amount = new BigDecimal(realAmount).multiply(new
		// BigDecimal("100")).setScale(0).toString();

		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("merchantNo", cjhkRegister.getMerchantCode());
		apiRequest.addParam("requestNo", ordercode);
		apiRequest.addParam("channelProductCode", channelProductCode);
		apiRequest.addParam("bankCardNo", bankCard);
		apiRequest.addParam("cvn2", securityCode);
		apiRequest.addParam("expired", expiredTime);
		apiRequest.addParam("amount", realAmount);

		apiRequest.addParam("productName", product);
		apiRequest.addParam("productDesc", goods);
		apiRequest.addParam("serverCallbackUrl ", ipAddress + "/v1.0/paymentchannel/topup/cjhk/fastpay/notify_call");

		LOG.info("支付的请求报文 apiRequest======" + apiRequest.toString());

		ApiResponse post = ApiClient.post(Url + "/rest/v1.0/paybar/pay", apiRequest);

		LOG.info("请求返回的 post======" + JSON.toJSONString(post));

		JSONObject fromObject2 = JSONObject.fromObject(post);

		String state = fromObject2.getString("state");

		JSONObject resultMap = fromObject2.getJSONObject("resultMap");

		JSONObject fromObject3 = JSONObject.fromObject(resultMap);
		LOG.info("fromObject3======" + fromObject3);

		String bizCode = fromObject3.getString("bizCode");
		String bizMsg = fromObject3.getString("bizMsg");
		String code1 = fromObject3.getString("code");

		if ("200".equals(code1) && "1".equals(bizCode)) {
			if ("SUCCESS".equals(state)) {
				String status = fromObject3.getString("status");
				if ("PROCESS".equals(status)) {
					LOG.info("支付中 status======" + status);
					maps.put(CommonConstants.RESP_CODE, "999998");
					maps.put("channel_type", "sdj");
					maps.put(CommonConstants.RESP_MESSAGE, "等待银行扣款中");
					return maps;

				} else if ("UMPAY".equals(status)) {
					LOG.info("待确认 status======" + status);
					maps.put(CommonConstants.RESP_CODE, "999998");
					maps.put("channel_type", "sdj");
					maps.put(CommonConstants.RESP_MESSAGE, "等待银行扣款中");
					return maps;

				} else if ("SUCCESS".equals(status)) {
					LOG.info("支付成功 status======" + status);
					maps.put(CommonConstants.RESP_CODE, "999998");
					maps.put("channel_type", "sdj");
					maps.put(CommonConstants.RESP_MESSAGE, "等待银行扣款中");
					return maps;

				} else if ("FAILURE".equals(status)) {
					LOG.info("支付失败 status======" + status);
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put("channel_type", "sdj");
					maps.put(CommonConstants.RESP_MESSAGE, "等待银行扣款中");
					return maps;

				} else {
					LOG.info("未知 status======" + status);
					maps.put(CommonConstants.RESP_CODE, "999998");
					maps.put("channel_type", "sdj");
					maps.put(CommonConstants.RESP_MESSAGE, "等待银行扣款中");
					return maps;
				}

			} else {
				maps.put(CommonConstants.RESP_CODE, "999998");
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;
			}

		} else {
			maps.put(CommonConstants.RESP_CODE, "999998");
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
			return maps;
		}

	}

	// 代付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cjhk/transfer")
	public @ResponseBody Object cjhkTransfer(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode, @RequestParam(value = "extra") String extra)
			throws Exception {
		LOG.info("开始进入代付接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");
		String realAmount = resultObj.getString("realAmount");
		String amount = resultObj.getString("amount");

		Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
		Object object2 = queryBankCardByCardNo.get("result");
		fromObject = JSONObject.fromObject(object2);

		String phone = fromObject.getString("phone");
		String idCard = fromObject.getString("idcard");
		String userName = fromObject.getString("userName");
		String idcard = fromObject.getString("idcard");
		String bankName = fromObject.getString("bankName");
		String bankBranchName = fromObject.getString("bankBranchName");
		String province = fromObject.getString("province");
		String city = fromObject.getString("city");

		Map<String, Object> bankUnitNo = this.getBankUnitNo(Util.queryBankNameByBranchName(bankName));
		if (!"000000".equals(bankUnitNo.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", bankUnitNo.get("resp_message"));
			return maps;
		}

		String inBankUnitNo = (String) bankUnitNo.get("result");

		Map<String, Object> bankCodeByBankName = this.getBankCodeByBankName(Util.queryBankNameByBranchName(bankName));
		if (!"000000".equals(bankCodeByBankName.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", bankCodeByBankName.get("resp_message"));
			return maps;
		}

		String code = (String) bankCodeByBankName.get("result");

		String url = "http://user/v1.0/user/bankcode/getbankcode/byname";
		MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<String, String>();
		multiValueMap.add("name", Util.queryBankNameByBranchName(bankName));
		String result = restTemplate.postForObject(url, multiValueMap, String.class);
		LOG.info("接口/v1.0/user/bankcode/getcodebyname====RESULT=========" + result);

		JSONObject jsonObject;
		try {
			fromObject = JSONObject.fromObject(result);
			jsonObject = fromObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("查询银行编号出错======");
			e.printStackTrace();
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", "暂不支持该结算银行,请及时更换结算银行卡!");
			return maps;
		}

		String num = jsonObject.getString("num");

		CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idCard);

		// String Amount = new BigDecimal(realAmount).multiply(new
		// BigDecimal("100")).setScale(0).toString();

		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("merchantNo", cjhkRegister.getMerchantCode());
		apiRequest.addParam("requestNo", ordercode);
		apiRequest.addParam("channelCode", "CHANPAY");

		apiRequest.addParam("amount", realAmount);
		apiRequest.addParam("bankAccountNo", bankCard);
		apiRequest.addParam("bankAccountName", userName);
		apiRequest.addParam("bankName", bankName);
		apiRequest.addParam("bankSubName", "上海宝山支行");
		apiRequest.addParam("bankChannelNo", inBankUnitNo);
		apiRequest.addParam("bankCode", num);
		apiRequest.addParam("bankAbbr", code);
		apiRequest.addParam("bankProvince", "上海");
		apiRequest.addParam("bankCity", "上海");
		apiRequest.addParam("bankArea", "上海");

		int i = 400;
		if ("T1".equals(extra)) {
			i = 402;
		}

		apiRequest.addParam("walletType", i);
		apiRequest.addParam("bankAccountType", "PRIVATE");

		LOG.info("代付的请求报文 apiRequest======" + apiRequest.toString());

		ApiResponse post = ApiClient.post(Url + "/rest/v1.0/paybar/withdrawDeposit", apiRequest);

		LOG.info("请求返回的 post======" + JSON.toJSONString(post));

		JSONObject fromObject2 = JSONObject.fromObject(post);

		String state = fromObject2.getString("state");

		JSONObject resultMap = fromObject2.getJSONObject("resultMap");

		JSONObject fromObject3 = JSONObject.fromObject(resultMap);
		LOG.info("fromObject3======" + fromObject3);

		String bizCode = fromObject3.getString("bizCode");
		String bizMsg = fromObject3.getString("bizMsg");
		String code1 = fromObject3.getString("code");

		if ("200".equals(code1) && "1".equals(bizCode)) {
			if ("SUCCESS".equals(state)) {

				maps.put(CommonConstants.RESP_CODE, "999998");
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, "等待银行出款中");
				return maps;

			} else {

				maps.put(CommonConstants.RESP_CODE, "999998");
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;
			}

		} else {
			maps.put(CommonConstants.RESP_CODE, "999998");
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
			return maps;
		}

	}
	
	
	
	//代付到储蓄卡的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cjhk/transferto/debitcard")
	public @ResponseBody Object cjhkTransferToDebitCard(HttpServletRequest request,
			@RequestParam(value = "userId") String userId, 
			@RequestParam(value = "amount") String amount,
			@RequestParam(value = "extra") String extra)
			throws Exception {
		LOG.info("开始进入代付到储蓄卡接口======");

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
		String bankName = fromObject.getString("bankName");
		String idCard = fromObject.getString("idcard");
		String phone = fromObject.getString("phone");
		String userName = fromObject.getString("userName");

		Map<String, Object> bankUnitNo = this.getBankUnitNo(Util.queryBankNameByBranchName(bankName));
		if (!"000000".equals(bankUnitNo.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", bankUnitNo.get("resp_message"));
			return maps;
		}

		String inBankUnitNo = (String) bankUnitNo.get("result");

		Map<String, Object> bankCodeByBankName = this.getBankCodeByBankName(Util.queryBankNameByBranchName(bankName));
		if (!"000000".equals(bankCodeByBankName.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", bankCodeByBankName.get("resp_message"));
			return maps;
		}

		String code = (String) bankCodeByBankName.get("result");

		String url = "http://user/v1.0/user/bankcode/getbankcode/byname";
		MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<String, String>();
		multiValueMap.add("name", Util.queryBankNameByBranchName(bankName));
		String result = restTemplate.postForObject(url, multiValueMap, String.class);
		LOG.info("接口/v1.0/user/bankcode/getcodebyname====RESULT=========" + result);

		JSONObject jsonObject;
		try {
			fromObject = JSONObject.fromObject(result);
			jsonObject = fromObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("查询银行编号出错======");
			e.printStackTrace();
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", "暂不支持该结算银行,请及时更换结算银行卡!");
			return maps;
		}

		String num = jsonObject.getString("num");

		String requestNo = String.valueOf(System.currentTimeMillis());
		
		CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idCard);

		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("merchantNo", cjhkRegister.getMerchantCode());
		apiRequest.addParam("requestNo", requestNo);
		apiRequest.addParam("channelCode", "CHANPAY");

		apiRequest.addParam("amount", amount);
		apiRequest.addParam("bankAccountNo", cardNo);
		apiRequest.addParam("bankAccountName", userName);
		apiRequest.addParam("bankName", bankName);
		apiRequest.addParam("bankSubName", "上海宝山支行");
		apiRequest.addParam("bankChannelNo", inBankUnitNo);
		apiRequest.addParam("bankCode", num);
		apiRequest.addParam("bankAbbr", code);
		apiRequest.addParam("bankProvince", "上海");
		apiRequest.addParam("bankCity", "上海");
		apiRequest.addParam("bankArea", "上海");

		int i = 400;
		if ("T1".equals(extra)) {
			i = 402;
		}

		apiRequest.addParam("walletType", i);
		apiRequest.addParam("bankAccountType", "PRIVATE");

		LOG.info("代付的请求报文 apiRequest======" + apiRequest.toString());

		ApiResponse post = ApiClient.post(Url + "/rest/v1.0/paybar/withdrawDeposit", apiRequest);

		LOG.info("请求返回的 post======" + JSON.toJSONString(post));

		JSONObject fromObject2 = JSONObject.fromObject(post);

		String state = fromObject2.getString("state");

		JSONObject resultMap = fromObject2.getJSONObject("resultMap");

		JSONObject fromObject3 = JSONObject.fromObject(resultMap);
		LOG.info("fromObject3======" + fromObject3);

		String bizCode = fromObject3.getString("bizCode");
		String bizMsg = fromObject3.getString("bizMsg");
		String code1 = fromObject3.getString("code");

		if ("200".equals(code1) && "1".equals(bizCode)) {
			if ("SUCCESS".equals(state)) {

				maps.put(CommonConstants.RESP_CODE, "999998");
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, "等待银行出款中");
				return maps;

			} else {

				maps.put(CommonConstants.RESP_CODE, "999998");
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;
			}

		} else {
			maps.put(CommonConstants.RESP_CODE, "999998");
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
			return maps;
		}

	}
	

	// 订单查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cjhk/ordercodequery")
	public @ResponseBody Object orderCodeQuery(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode) throws Exception {
		LOG.info("开始进入订单查询接口======");
		Map<String, String> maps = new HashMap<String, String>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");

		Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
		Object object2 = queryBankCardByCardNo.get("result");
		fromObject = JSONObject.fromObject(object2);

		String idCard = fromObject.getString("idcard");

		CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idCard);

		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("merchantNo", cjhkRegister.getMerchantCode());
		apiRequest.addParam("requestNo", ordercode);

		LOG.info("订单查询的请求报文 apiRequest======" + apiRequest.toString());

		ApiResponse post = ApiClient.post(Url + "/rest/v1.0/paybar/queryPay", apiRequest);

		LOG.info("请求返回的 post======" + JSON.toJSONString(post));

		JSONObject fromObject2 = JSONObject.fromObject(post);

		String state = fromObject2.getString("state");

		JSONObject resultMap = fromObject2.getJSONObject("resultMap");

		JSONObject fromObject3 = JSONObject.fromObject(resultMap);
		LOG.info("fromObject3======" + fromObject3);

		String bizCode = fromObject3.getString("bizCode");
		String bizMsg = fromObject3.getString("bizMsg");
		String code1 = fromObject3.getString("code");

		if ("200".equals(code1) && "1".equals(bizCode)) {
			if ("SUCCESS".equals(state)) {

				String status = fromObject3.getString("status");

				if ("SUCCESS".equals(status)) {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, "支付成功");
					return maps;
				} else if ("UNPAY".equals(status) && "PROCESS".equals(status)) {
					maps.put(CommonConstants.RESP_CODE, "999998");
					maps.put(CommonConstants.RESP_MESSAGE, "等待支付");
					return maps;
				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, "支付失败");
					return maps;
				}

			} else {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;
			}

		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
			return maps;
		}

	}

	// 代付查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cjhk/transferquery")
	public @ResponseBody Object transferQuery(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode) throws Exception {
		LOG.info("开始进入代付查询接口======");

		Map<String, String> maps = new HashMap<String, String>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");

		Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
		Object object2 = queryBankCardByCardNo.get("result");
		fromObject = JSONObject.fromObject(object2);

		String idCard = fromObject.getString("idcard");

		CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idCard);

		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("merchantNo", cjhkRegister.getMerchantCode());
		apiRequest.addParam("withdrawRequestNo", ordercode);

		LOG.info("代付查询的请求报文 apiRequest======" + apiRequest.toString());

		ApiResponse post = ApiClient.post(Url + "/rest/v1.0/paybar/queryWithdraw", apiRequest);

		LOG.info("请求返回的 post======" + JSON.toJSONString(post));

		JSONObject fromObject2 = JSONObject.fromObject(post);

		String state = fromObject2.getString("state");

		if ("SUCCESS".equals(state)) {

			JSONObject resultMap = fromObject2.getJSONObject("resultMap");

			JSONObject fromObject3 = JSONObject.fromObject(resultMap);
			LOG.info("fromObject3======" + fromObject3);

			String bizCode = fromObject3.getString("bizCode");
			String bizMsg = fromObject3.getString("bizMsg");
			String code1 = fromObject3.getString("code");

			if ("200".equals(code1) && "1".equals(bizCode)) {
				String remitStatus = fromObject3.getString("remitStatus");

				if ("SUCCESS".equals(remitStatus)) {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, "出款成功");
					return maps;
				} else if ("REMITING".equals(remitStatus)) {
					maps.put(CommonConstants.RESP_CODE, "999998");
					maps.put(CommonConstants.RESP_MESSAGE, "等待出款");
					return maps;
				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, "出款失败");
					return maps;
				}

			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put("channel_type", "sdj");
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;
			}

		} else {

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, "查询失败");
			return maps;
		}

	}

	// 代付异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cjhk/transfer/notify_call")
	public void hljcTransferNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("代付异步通知进来了=======");

		String respMessage = request.getParameter("respMessage");
		String respCode = request.getParameter("respCode");
		String orderNo = request.getParameter("orderNo");
		String orderNum = request.getParameter("orderNum");
		String sign = request.getParameter("sign");

		if ("10000".equals(respCode)) {

			String url = "http://creditcardmanager/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("orderCode", orderNo);
			requestEntity.add("version", "3");
			String result = null;
			JSONObject jsonObject;
			JSONObject resultObj;
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("RESULT================" + result);
				jsonObject = JSONObject.fromObject(result);
				resultObj = jsonObject.getJSONObject("result");
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("",e);
			}

			url = "http://transactionclear/v1.0/transactionclear/payment/update";

			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", orderNo);
			requestEntity.add("third_code", orderNum);
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("",e);
			}

			LOG.info("订单状态修改成功===================" + orderNo + "====================" + result);

			LOG.info("订单已代付!");

			PrintWriter writer = response.getWriter();
			writer.print("success");
			writer.close();

		}

	}

	// 快捷支付异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cjhk/fastpay/notify_call")
	public void hljcFastPayNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("快捷支付异步通知进来了=======");

		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}

		String bizType = request.getParameter("bizType");
		String data = request.getParameter("data");

		String decrypt = AESUtil.decrypt(data, key);

		JSONObject fromObject = JSONObject.fromObject(decrypt);

		String code = fromObject.getString("code");
		String bizCode = "0";
		if (fromObject.has("bizCode")) {

			bizCode = fromObject.getString("bizCode");
		}

		String requestNo = fromObject.getString("requestNo");
		String status = fromObject.getString("status");

		if ("200".equals(code) && "1".equals(bizCode)) {

			String url = "http://creditcardmanager/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("orderCode", requestNo);
			requestEntity.add("version", "3");
			String result = null;
			JSONObject jsonObject;
			JSONObject resultObj;
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("RESULT================" + result);
				jsonObject = JSONObject.fromObject(result);
				resultObj = jsonObject.getJSONObject("result");
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("",e);
			}

			url = "http://transactionclear/v1.0/transactionclear/payment/update";

			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", requestNo);
			requestEntity.add("third_code", "");
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("",e);
			}

			LOG.info("订单状态修改成功===================" + requestNo + "====================" + result);

			LOG.info("订单已支付!");

		}

	}

	// 绑卡异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cjhk/bindcard/notify_call")
	public void hljcBindCardNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("银联绑卡异步通知进来了=======");

		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}

	}

	// 商户余额查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cjhk/merchantwalletquery")
	public @ResponseBody Object merchantWalletQuery(HttpServletRequest request,
			@RequestParam(value = "bankCard") String bankCard
	// @RequestParam(value = "merchantNo") String merchantNo1
	) throws Exception {

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

		String idCard = fromObject.getString("idcard");

		CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idCard);

		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("merchantNo", cjhkRegister.getMerchantCode());
		// apiRequest.addParam("merchantNo", merchantNo1);

		LOG.info("商户余额查询的请求报文 apiRequest======" + apiRequest.toString());

		ApiResponse post = ApiClient.post(Url + "/rest/v1.0/paybar/queryMerchantWallet", apiRequest);

		LOG.info("请求返回的 post======" + JSON.toJSONString(post));

		JSONObject fromObject2 = JSONObject.fromObject(post);

		String state = fromObject2.getString("state");

		if ("SUCCESS".equals(state)) {

			JSONObject resultMap = fromObject2.getJSONObject("resultMap");

			JSONObject fromObject3 = JSONObject.fromObject(resultMap);
			LOG.info("fromObject3======" + fromObject3);

			String code1 = fromObject3.getString("code");
			if ("200".equals(code1)) {
				String amount = fromObject3.getString("amount");
				String amountD0 = fromObject3.getString("amountD0");
				String amountT1 = fromObject3.getString("amountT1");

				Map<String, Object> map = new HashMap<String, Object>();

				map.put("钱包余额", amount);
				map.put("T1余额", amountT1);
				map.put("D0余额", amountD0);

				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", map);

			} else {

				return ResultWrap.init(CommonConstants.FALIED, "查询失败");
			}

		} else {

			return ResultWrap.init(CommonConstants.FALIED, "查询失败");
		}

	}

}