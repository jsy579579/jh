package com.jh.paymentgateway.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import com.alibaba.fastjson.JSONObject;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.BankNumCode;
import com.jh.paymentgateway.pojo.CJHKFactory;
import com.jh.paymentgateway.pojo.CJHKRegister;
import com.jh.paymentgateway.pojo.CJQuickBindCard;
import com.jh.paymentgateway.pojo.CJXChannelCode;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.Util;
import com.jh.paymentgateway.util.cjhk.AESUtil;
import com.jh.paymentgateway.util.cjx.BaseResMessage;
import com.jh.paymentgateway.util.cjx.Constants;
import com.jh.paymentgateway.util.cjx.EncryptUtil;
import com.jh.paymentgateway.util.cjx.GetSpToken;
import com.jh.paymentgateway.util.cjx.HttpUtil;
import com.jh.paymentgateway.util.cjx.SignUtil;
import com.jh.paymentgateway.util.cjx.TokenRes;
import com.jh.paymentgateway.util.ght.HttpClientUtil;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import cn.jh.common.utils.UUIDGenerator;
import okhttp3.Response;

@Controller
@EnableAutoConfiguration
public class CJHKXpageRequest extends BaseChannel {

	private static final Logger LOG = LoggerFactory.getLogger(CJHKXpageRequest.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	RedisUtil redisUtil;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	//private static final String key = "188EC00DDFE85F2E97505CAAD2FD670506A2CBEB47E509BF"; // 服务商密钥188EC00DDFE85F2E97505CAAD2FD670506A2CBEB47E509BF是复归的
	private static final String key = "";
	//private static final String spCode = "10000082"; // 服务商编号 10000082是复归的
	private static final String spCode = "";
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	// 跟还款对接的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjx/dockingEntrance")
	public @ResponseBody Object docking1(@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "dbankCard") String dbankCard, @RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "phone") String phone, @RequestParam(value = "dphone") String dphone,
			@RequestParam(value = "userName") String userName, @RequestParam(value = "bankName") String bankName1,
			@RequestParam(value = "dbankName") String dbankName, @RequestParam(value = "extraFee") String extraFee,
			@RequestParam(value = "securityCode") String securityCode, @RequestParam(value = "rate") String rate,
			@RequestParam(value = "expiredTime") String expired) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String expiredTime = this.expiredTimeToYYMM(expired);
		CJQuickBindCard cjk = topupPayChannelBusiness.getCJQuickBindCardByBankCard(bankCard);
		CJHKRegister cjr = topupPayChannelBusiness.getCJHKRegisterByIdCard(idCard);
		String bankName = Util.queryBankNameByBranchName(bankName1);
		CJXChannelCode cjxChannelCode;
		cjxChannelCode = topupPayChannelBusiness.getCJXChannelCode(bankName);
		if (cjxChannelCode == null) {
			return ResultWrap.init(CommonConstants.FALIED, "抱歉,暂不支持该银行");
		}
		// List<CJQuickBindCard> cjqBindCards=
		// topupPayChannelBusiness.findCJQuickBindCardByIdCard(idCard);
		ArrayList<String> arrayList = new ArrayList<String>();
		arrayList.add("101001");
		arrayList.add("101002");
		arrayList.add("110002");
		arrayList.add("110003");
		arrayList.add("1000");
		String channelCode;
		try {
			channelCode = cjxChannelCode.getChannelCode();

			LOG.info("渠道编号channnelCode====================" + channelCode);
			if (cjr == null) {
				map = (Map<String, Object>) this.cjhkRegister(userName, phone, idCard, extraFee, bankName, bankCard,
						rate, expiredTime, securityCode, dbankName, dphone, dbankCard);

				LOG.info("开始绑定的银行卡====================" + bankCard);
				if ("000000".equals(map.get("resp_code"))) {
					if (cjk == null || !"2".equals(cjk.getStatus())) {
						map.put(CommonConstants.RESP_CODE, "999996");
						map.put(CommonConstants.RESP_MESSAGE, "进入签约");
						map.put(CommonConstants.RESULT,
								ipAddress + "/v1.0/paymentgateway/topup/tocjquick/bindcard1?bankName="
										+ URLEncoder.encode(bankName, "UTF-8") + "&cardType="
										+ URLEncoder.encode("贷记卡", "UTF-8") + "&bankCard=" + bankCard + "&phone="
										+ phone + "&expiredTime=" + expiredTime + "&securityCode=" + securityCode
										+ "&idCard=" + idCard + "&userName=" + userName + "&channelCode=" + channelCode
										+ "&ipAddress=" + ipAddress);
					} else {
						map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						map.put(CommonConstants.RESP_MESSAGE, "已签约");
					}

				} else {
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "进件失败");
				}
			}

			else if (cjk == null) {
				for (String channelCode1 : arrayList) {

					try {
						map = (Map<String, Object>) this.cjhkmerChange2(idCard, rate, channelCode1);
					} catch (Exception e) {

						map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						map.put(CommonConstants.RESP_MESSAGE, "网络有问题,请稍后重试");
						return map;
					}
					LOG.info("为新增的渠道号开通费率=============" + channelCode + "===========费率" + rate);

				}

			}
		else if (!rate.equals(cjr.getRate())) {

				for (String channelCode1 : arrayList) {
					map = (Map<String, Object>) this.cjhkmerChange1(idCard, bankName, rate, channelCode1);
					LOG.info("修改费率时改变所有费率========================" + channelCode + "========费率" + rate);
				}

			} else if (!extraFee.equals(cjr.getExtraFee())) {

				for (String channelCode1 : arrayList) {
					LOG.info("修改提现费========================" + channelCode1);
					map = (Map<String, Object>) cjhkmerChange3(idCard, bankName, rate, extraFee, channelCode1);

				}

			}

			if (cjk == null || !"2".equals(cjk.getStatus())) {
				map.put(CommonConstants.RESP_CODE, "999996");
				map.put(CommonConstants.RESP_MESSAGE, "进入签约");
				map.put(CommonConstants.RESULT,
						ipAddress + "/v1.0/paymentgateway/topup/tocjquick/bindcard1?bankName="
								+ URLEncoder.encode(bankName, "UTF-8") + "&cardType="
								+ URLEncoder.encode("贷记卡", "UTF-8") + "&bankCard=" + bankCard + "&phone=" + phone
								+ "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&idCard=" + idCard
								+ "&userName=" + userName + "&channelCode=" + channelCode + "&ipAddress=" + ipAddress);
			} else {
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "已签约");
				return map;
			}

		} catch (

		Exception e) {
			LOG.error("与还款对接接口出现异常======", e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "与还款对接失败");
			return map;
		}

		return map;
	}

	// 进件接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjx/register")
	public @ResponseBody Object cjhkRegister(@RequestParam(value = "userName") String userName,
			@RequestParam(value = "phone") String phone, @RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "extraFee") String extraFee, @RequestParam(value = "bankName") String bankName,
			@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "rate") String rate,
			@RequestParam(value = "expiredTime") String expired,
			@RequestParam(value = "securityCode") String securityCode,
			@RequestParam(value = "dbankName") String dbankName, @RequestParam(value = "dphone") String dphone,
			@RequestParam(value = "dbankCard") String dbankCard) throws Exception {

		LOG.info("开始进入进件接口========================");
		String expiredTime = this.expiredTimeToYYMM(expired);
		Map<String, Object> maps = new HashMap<String, Object>();

		String extra = new BigDecimal(extraFee).multiply(new BigDecimal("100")).setScale(0).toString();
		LOG.info("固定单笔附加手续费金额，单位分:" + extra);

		BankNumCode dbankNumCode = topupPayChannelBusiness.getBankNumCodeByBankName(dbankName);
		if (dbankNumCode == null) {
			return ResultWrap.init(CommonConstants.FALIED, "您的到账卡不支持,请更换默认到账卡!");
		}
		String dbankCode = dbankNumCode.getBankBranchcode();
		LOG.info("发送入网请求获取的" + dbankCode);
		String dbankNum = dbankNumCode.getBankNum();
		String dbankcode = dbankNumCode.getBankCode();
		String dBankName = Util.queryBankNameByBranchName(dbankNum);
		LOG.info("结算卡银行缩写" + dBankName);
		LOG.info("发送入网请求的联行号" + dbankCode);
		LOG.info("发送入网请求的银行代号" + dbankcode);
		LOG.info("发送入网请求的银行代码" + dbankNum);
		CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idCard);
		CJXChannelCode cjxChannelCode = topupPayChannelBusiness.getCJXChannelCode(bankName);
		String channelCode = cjxChannelCode.getChannelCode();
		LOG.info("获取渠道编号========================" + channelCode);
		if (cjhkRegister == null) {

			// 获取令牌
			BaseResMessage<TokenRes> tokenRes = new GetSpToken().token(key, spCode);

			System.out.println("获取的令牌" + tokenRes);

			String token = tokenRes.getData().getToken();

			System.out.println("获取的令牌" + token);

			// 解密令牌
			String tokenClearText = EncryptUtil.desDecrypt(token, key);

			System.out.println("解密令牌" + tokenClearText);
			// 敏感数据3DES加密
			String dbankAccountNoCipher = EncryptUtil.desEncrypt(dbankCard, key);
			String dmobileCipher = EncryptUtil.desEncrypt(dphone, key);
			String idCardNoCipher = EncryptUtil.desEncrypt(idCard, key);

			// 构建签名参数
			TreeMap<String, Object> signParams = new TreeMap<String, Object>();
			signParams.put("token", tokenClearText);
			signParams.put("spCode", spCode);
			signParams.put("channelCode", channelCode);
			signParams.put("merName", "上海莘丽网络");
			signParams.put("merAbbr", "莘丽");
			signParams.put("idCardNo", idCard);
			signParams.put("bankAccountNo", dbankCard);
			signParams.put("mobile", dphone);
			signParams.put("bankAccountName", userName);
			signParams.put("bankAccountType", "2");
			signParams.put("bankName", dbankName);
			signParams.put("bankSubName", "上海宝山支行");
			signParams.put("bankCode", dbankNum);
			signParams.put("bankAbbr", dbankcode);
			signParams.put("bankChannelNo", dbankCode);
			signParams.put("bankProvince", "上海市");
			signParams.put("bankCity", "上海市");
			signParams.put("debitRate", rate);
			signParams.put("debitCapAmount", "99999900");
			signParams.put("creditRate", rate);
			signParams.put("creditCapAmount", "99999900");
			signParams.put("withdrawDepositRate", "0");
			signParams.put("withdrawDepositSingleFee", extra);
			signParams.put("reqFlowNo", String.valueOf(System.currentTimeMillis()));
			// 构建请求参数
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("token", tokenClearText);
			jsonObj.put("spCode", spCode);
			jsonObj.put("channelCode", channelCode);
			jsonObj.put("merName", "上海莘丽网络");
			jsonObj.put("merAbbr", "莘丽");
			jsonObj.put("idCardNo", idCardNoCipher);
			jsonObj.put("bankAccountNo", dbankAccountNoCipher);
			jsonObj.put("mobile", dmobileCipher);
			jsonObj.put("bankAccountName", userName);
			jsonObj.put("bankAccountType", "2");
			jsonObj.put("bankName", dbankName);
			jsonObj.put("bankSubName", "上海宝山支行");
			jsonObj.put("bankCode", dbankNum);
			jsonObj.put("bankAbbr", dbankcode);
			jsonObj.put("bankChannelNo", dbankCode);
			jsonObj.put("bankProvince", "上海市");
			jsonObj.put("bankCity", "上海市");
			jsonObj.put("debitRate", rate);
			jsonObj.put("debitCapAmount", "99999900");
			jsonObj.put("creditRate", rate);
			jsonObj.put("creditCapAmount", "99999900");
			jsonObj.put("withdrawDepositRate", "0");
			jsonObj.put("withdrawDepositSingleFee", extra);
			jsonObj.put("reqFlowNo", String.valueOf(System.currentTimeMillis()));

			jsonObj.put("sign", SignUtil.signByMap(key, signParams));

			// 接口访问
			String jsonReq = jsonObj.toJSONString();
			LOG.info("进件接口请求信息=====================: " + jsonReq);
			// 响应信息:
			Response response;
			try {
				response = HttpUtil.sendPost(Constants.getServerUrl() + "/v2/merchant/merchantReg", jsonReq);
				String message1 = response.message();
				String jsonRsp = response.body().string();
				LOG.info("进件响应信息========================: " + jsonRsp);

				if (response.isSuccessful()) {
					com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(jsonRsp);

					String code = js.getString("code");
					String message = js.getString("message");

					if ("000000".equals(code)) {
						String data = js.getString("data");
						com.alibaba.fastjson.JSONObject js1 = com.alibaba.fastjson.JSONObject.parseObject(data);
						String merchantCode = js1.getString("merchantCode");
						LOG.info("取出的商户号: " + merchantCode);
						CJHKRegister cjhkRegister1 = new CJHKRegister();
						cjhkRegister1.setPhone(phone);
						cjhkRegister1.setIdCard(idCard);
						cjhkRegister1.setBankCard(dbankCard);
						cjhkRegister1.setMerchantCode(merchantCode);
						cjhkRegister1.setRate(rate);
						cjhkRegister1.setExtraFee(extraFee);
						cjhkRegister1.setUserName(userName);
						topupPayChannelBusiness.createCJHKRegister(cjhkRegister1);
						LOG.info("进件成功,开始绑卡======");

						maps.put(CommonConstants.RESP_CODE, "000000");
						maps.put(CommonConstants.RESP_MESSAGE, "进入签约");
						maps.put(CommonConstants.RESULT,
								ipAddress + "/v1.0/paymentgateway/topup/tocjquick/bindcard1?bankName="
										+ URLEncoder.encode(bankName, "UTF-8") + "&cardType="
										+ URLEncoder.encode("0", "UTF-8") + "&bankCard=" + bankCard + "&phone=" + phone
										+ "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&idCard="
										+ idCard + "&userName=" + userName + "&channelCode=" + channelCode
										+ "&ipAddress=" + ipAddress);
						return maps;
					} else {
						maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						maps.put(CommonConstants.RESP_MESSAGE, message);
						return maps;

					}

				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, message1);
					return maps;
				}
			} catch (Exception e) {
				LOG.error("进件接口出现异常======", e);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "进件失败");
				return maps;
			}
		} else {
			LOG.info("开始进入签约======");
			maps.put(CommonConstants.RESP_CODE, "000000");
			maps.put(CommonConstants.RESP_MESSAGE, "进入签约");
			maps.put(CommonConstants.RESULT,
					ipAddress + "/v1.0/paymentgateway/topup/tocjquick/bindcard1?bankName="
							+ URLEncoder.encode(bankName, "UTF-8") + "&cardType=" + URLEncoder.encode("0", "UTF-8")
							+ "&bankCard=" + bankCard + "&phone=" + phone + "&expiredTime=" + expiredTime
							+ "&securityCode=" + securityCode + "&idCard=" + idCard + "&userName=" + userName
							+ "&channelCode=" + channelCode + "&ipAddress=" + ipAddress);
			return maps;

		}

	}

	// 商户测签约短信
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjx/merchantSignSms1")
	public @ResponseBody Object cjhkSignSms(HttpServletRequest request, @RequestParam(value = "phone") String phone,
			@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "userName") String userName,
			@RequestParam(value = "idCard") String idCard, @RequestParam(value = "expiredTime") String expiredTime,
			@RequestParam(value = "securityCode") String securityCode,
			@RequestParam(value = "channelCode") String channelCode) throws Exception {

		LOG.info("开始进入商户测签约短信接口========================");

		Map<String, Object> maps = new HashMap<String, Object>();
		CJQuickBindCard cjQuickBindCard = topupPayChannelBusiness.getCJQuickBindCardByBankCard(bankCard);
		CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idCard);
		String merchantCode = cjhkRegister.getMerchantCode();

		// 获取令牌
		BaseResMessage<TokenRes> tokenRes = new GetSpToken().token(key, spCode);
		String token = tokenRes.getData().getToken();
		// 解密令牌
		String tokenClearText = EncryptUtil.desDecrypt(token, key);

		// 敏感数据3DES加密
		String bankAccountNoCipher = EncryptUtil.desEncrypt(bankCard, key);
		String mobileCipher = EncryptUtil.desEncrypt(phone, key);
		String idCardNoCipher = EncryptUtil.desEncrypt(idCard, key);
		String requestNo = UUID.randomUUID().toString();
		LOG.info("绑卡的订单编号==================" + requestNo);
		// 构建签名参数
		TreeMap<String, Object> signParams = new TreeMap<String, Object>();
		signParams.put("token", tokenClearText);
		signParams.put("spCode", spCode);
		signParams.put("orderNo", requestNo);
		signParams.put("merchantCode", merchantCode);
		signParams.put("channelCode", channelCode);
		signParams.put("bankAccountName", userName);
		signParams.put("bankAccountNo", bankCard);
		signParams.put("idCardNo", idCard);
		signParams.put("mobile", phone);

		// 构建请求参数
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("token", tokenClearText);
		jsonObj.put("spCode", spCode);
		jsonObj.put("orderNo", requestNo);
		jsonObj.put("channelCode", channelCode);
		jsonObj.put("merchantCode", merchantCode);
		jsonObj.put("bankAccountName", userName);
		jsonObj.put("bankAccountNo", bankAccountNoCipher);
		jsonObj.put("idCardNo", idCardNoCipher);
		jsonObj.put("mobile", mobileCipher);
		jsonObj.put("sign", SignUtil.signByMap(key, signParams));

		// 接口访问
		String jsonReq = jsonObj.toJSONString();

		LOG.info("商户侧签约短信请求信息:======================== " + jsonReq);

		Response response;
		try {
			response = HttpUtil.sendPost(Constants.getServerUrl() + "/v2/sign/merchantSignSms", jsonReq);
			String message1 = response.message();
			String jsonRsp = response.body().string();
			LOG.info("商户侧签约短信响应信息:=================== " + jsonRsp);

			if (response.isSuccessful()) {
				com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(jsonRsp);
				String code = js.getString("code");
				String data = js.getString("data");
				com.alibaba.fastjson.JSONObject js1 = com.alibaba.fastjson.JSONObject.parseObject(data);
				String respMsg = js1.getString("respMsg");
				if ("000000".equals(code)) {
					if (cjQuickBindCard == null) {
						CJQuickBindCard cjb = new CJQuickBindCard();
						cjb.setBankCard(bankCard);
						cjb.setUserName(userName);
						cjb.setPhone(phone);
						cjb.setIdCard(idCard);
						cjb.setStatus("0");
						cjb.setRequestNo(requestNo);
						topupPayChannelBusiness.createCJQuickBindCard(cjb);
					} else {
						LOG.info("此卡预签约，未审核" + bankCard);
					}
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put("requestNo", requestNo);
					return maps;
				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, respMsg);
					return maps;

				}

			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message1);
				return maps;
			}
		} catch (Exception e) {
			LOG.error("侧签约接口出现异常======", e);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "侧签约短信失败");
			return maps;

		}

	}

	// 银行卡签约
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjx/merchantSign1")
	public @ResponseBody Object cjhkSign(HttpServletRequest request, @RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "smsCode") String smsCode, @RequestParam(value = "userName") String userName,
			@RequestParam(value = "idCard") String idCard, @RequestParam(value = "phone") String phone,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode,
			@RequestParam(value = "requestNo") String requestNo,
			@RequestParam(value = "channelCode") String channelCode) throws Exception {

		LOG.info("开始进入商户签约接口========================");

		Map<String, Object> maps = new HashMap<String, Object>();

		CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idCard);
		String merchantCode = cjhkRegister.getMerchantCode();

		CJQuickBindCard cjQuickBindCard = topupPayChannelBusiness.getCJQuickBindCardByBankCard(bankCard);
		// 获取令牌
		BaseResMessage<TokenRes> tokenRes = new GetSpToken().token(key, spCode);
		String token = tokenRes.getData().getToken();
		// 解密令牌
		String tokenClearText = EncryptUtil.desDecrypt(token, key);
		String expired = this.expiredTimeToYYMM(expiredTime);
		// 敏感数据3DES加密
		String bankAccountNoCipher = EncryptUtil.desEncrypt(bankCard, key);
		String mobileCipher = EncryptUtil.desEncrypt(phone, key);
		String idCardNoCipher = EncryptUtil.desEncrypt(idCard, key);
		String cvn2Cipher = EncryptUtil.desEncrypt(securityCode, key);
		String expiredCipher = EncryptUtil.desEncrypt(expired, key);

		// 构建签名参数
		TreeMap<String, Object> signParams = new TreeMap<String, Object>();
		signParams.put("token", tokenClearText);
		signParams.put("spCode", spCode);
		signParams.put("orderNo", requestNo);
		signParams.put("channelCode", channelCode);
		signParams.put("merchantCode", merchantCode);
		signParams.put("bankAccountName", userName);
		signParams.put("bankAccountNo", bankCard);
		signParams.put("idCardNo", idCard);
		signParams.put("mobile", phone);
		signParams.put("cvn2", securityCode);
		signParams.put("expired", expired);
		signParams.put("smsCode", smsCode);
		signParams.put("isNeedSms", "1");

		// 构建请求参数
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("token", tokenClearText);
		jsonObj.put("spCode", spCode);
		jsonObj.put("orderNo", requestNo);
		jsonObj.put("channelCode", channelCode);
		jsonObj.put("merchantCode", merchantCode);
		jsonObj.put("bankAccountName", userName);
		jsonObj.put("bankAccountNo", bankAccountNoCipher);
		jsonObj.put("idCardNo", idCardNoCipher);
		jsonObj.put("mobile", mobileCipher);
		jsonObj.put("cvn2", cvn2Cipher);
		jsonObj.put("expired", expiredCipher);
		jsonObj.put("smsCode", smsCode);
		jsonObj.put("isNeedSms", "1");
		jsonObj.put("sign", SignUtil.signByMap(key, signParams));

		// 接口访问
		String jsonReq = jsonObj.toJSONString();
		LOG.info("商户签约请求信息:================== " + jsonReq);

		Response response;
		try {
			response = HttpUtil.sendPost(Constants.getServerUrl() + "/v2/sign/merchantSign", jsonReq);

			String jsonRsp = response.body().string();
			LOG.info("商户签约响应信息=======================:" + jsonRsp);
			com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(jsonRsp);
			String code = js.getString("code");
			String message = js.getString("message");
			String data = js.getString("data");
			if (response.isSuccessful()) {
				com.alibaba.fastjson.JSONObject js1 = com.alibaba.fastjson.JSONObject.parseObject(data);
				String respMsg = js1.getString("respMsg");
				if ("000000".equals(code)) {
					HashMap<Object, Object> hashw = new HashMap<>();
					hashw = (HashMap<Object, Object>) this.cjhkSignQuery(idCard, requestNo);
					if (hashw != null) {
						Object a = hashw.get("bindStatus");
						if (a.toString().equals("2")) {
							cjQuickBindCard.setStatus(a.toString());
							cjQuickBindCard.setChangeTime(new Date());
							cjQuickBindCard.setCreateTime(new Date());
							topupPayChannelBusiness.createCJQuickBindCard(cjQuickBindCard);
							maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
							maps.put("redirect_url",
									ipAddress+"/v1.0/paymentchannel/topup/wmyk/bindcardsuccess");
							return maps;
						} else {
							maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
							maps.put(CommonConstants.RESP_MESSAGE, respMsg);
							return maps;
						}
					} else {
						maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						maps.put(CommonConstants.RESP_MESSAGE, respMsg);
						return maps;
					}

				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, respMsg);

					return maps;
				}
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message);

				return maps;

			}

		} catch (Exception e) {
			LOG.error("银行卡签约接口出现异常======", e);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "银行卡签约失败");

			return maps;
		}

	}

	// 签约状态查询
	public Object cjhkSignQuery(String idCard, String requestNo) throws Exception {

		LOG.info("开始进入签约状态查询接口========================");

		Map<String, Object> maps = new HashMap<String, Object>();
		CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idCard);
		String merchantCode = cjhkRegister.getMerchantCode();
		// 获取令牌
		BaseResMessage<TokenRes> tokenRes = new GetSpToken().token(key, spCode);
		String token = tokenRes.getData().getToken();
		// 解密令牌
		String tokenClearText = EncryptUtil.desDecrypt(token, key);

		// 构建签名参数
		TreeMap<String, Object> signParams = new TreeMap<String, Object>();
		signParams.put("token", tokenClearText);
		signParams.put("spCode", spCode);
		signParams.put("orderNo", requestNo);
		signParams.put("merchantCode", merchantCode);

		// 构建请求参数
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("token", tokenClearText);
		jsonObj.put("spCode", spCode);
		jsonObj.put("orderNo", requestNo);
		jsonObj.put("merchantCode", merchantCode);
		jsonObj.put("sign", SignUtil.signByMap(key, signParams));

		// 接口访问
		String jsonReq = jsonObj.toJSONString();
		LOG.info(sdf.format(new Date()) + "请求信息: " + jsonReq);

		Response response;
		try {
			response = HttpUtil.sendPost(Constants.getServerUrl() + "/v2/sign/signQuery", jsonReq);

			String message = null;
			String signStatus;
			String respMsg;
			String jsonRsp = response.body().string();
			LOG.info("响应信息:======================== " + jsonRsp);
			com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(jsonRsp);
			String data = js.getString("data");
			String code = js.getString("code");
			message = js.getString("message");
			if (response.isSuccessful()) {
				com.alibaba.fastjson.JSONObject js1 = com.alibaba.fastjson.JSONObject.parseObject(data);
				signStatus = js1.getString("signStatus");
				respMsg = js1.getString("respMsg");
				if ("000000".equals(code)) {
					LOG.info("簽約返回的狀態碼" + signStatus);
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put("bindStatus", signStatus);
					return maps;
				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, respMsg);
					return maps;

				}

			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message);
			}
			return maps;

		} catch (Exception e) {
			LOG.error("签约状态查询接口出现异常", e);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "卡签约失败");
			return maps;
		}

	}

	// 商户侧消费
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjx/fastpay1")
	public @ResponseBody Object fastpay1(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "channelCode") String channelCode, @RequestParam(value = "cityName") String cityName,
			@RequestParam(value = "provinceName") String provinceName) throws Exception {
		LOG.info("开始进入侧消费接口========================");
		List<CJHKFactory> cjhkCityIP = this.chooseCityIp(provinceName, cityName);
		String city = null;
		String serviceIp = null;
		if (cjhkCityIP == null || cjhkCityIP.size() <= 0) {
			city = null;
			serviceIp = null;
		} else {
			city = cityName;
			serviceIp = cjhkCityIP.get(0).getStartIP();
		}
		Map<String, Object> maps = new HashMap<String, Object>();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String userName = prp.getUserName();
		String phone = prp.getCreditCardPhone();
		String idCard = prp.getIdCard();
		String bankName = prp.getCreditCardBankName();
		String bankCard = prp.getBankCard();
		String expiredTime = prp.getExpiredTime();
		String expired = this.expiredTimeToYYMM(expiredTime);
		String securityCode = prp.getSecurityCode();
		String realAmount = prp.getRealAmount();

		// 金额 单位分
		String Amount = new BigDecimal(realAmount).multiply(new BigDecimal("100")).setScale(0).toString();

		LOG.info("订单总金额，单位分:" + Amount);

		try {
			String bankName1 =Util.queryBankNameByBranchName(bankName);
			BankNumCode bankNumCode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName1);

			String bankCode = bankNumCode.getBankBranchcode();
			String bankNum = bankNumCode.getBankNum();
			String bankcode = bankNumCode.getBankCode();
			String bankNum1 = bankNumCode.getBankNum();
			String BankName = Util.queryBankNameByBranchName(bankNum);
			LOG.info("结算卡银行缩写" + BankName);
			LOG.info("发送入网请求的联行号" + bankCode);
			LOG.info("发送入网请求的银行代号" + bankcode);
			LOG.info("发送入网请求的银行代码" + bankNum1);
		} catch (Exception e) {
			LOG.info("获取银行编码失败================");
		}


		CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idCard);
		String merchantCode = cjhkRegister.getMerchantCode();

		// 获取令牌
		BaseResMessage<TokenRes> tokenRes = new GetSpToken().token(key, spCode);
		String token = tokenRes.getData().getToken();
		// 解密令牌
		String tokenClearText = EncryptUtil.desDecrypt(token, key);

		// 敏感数据3DES加密
		String bankAccountNoCipher = EncryptUtil.desEncrypt(bankCard, key);
		String mobileCipher = EncryptUtil.desEncrypt(phone, key);
		String idCardNoCipher = EncryptUtil.desEncrypt(idCard, key);
		String cvn2Cipher = EncryptUtil.desEncrypt(securityCode, key);
		String expiredCipher = EncryptUtil.desEncrypt(expired, key);

		// 构建签名参数
		TreeMap<String, Object> signParams = new TreeMap<String, Object>();
		signParams.put("token", tokenClearText);
		signParams.put("spCode", spCode);
		signParams.put("orderNo", orderCode);
		signParams.put("channelCode", channelCode);
		signParams.put("merchantCode", merchantCode);
		signParams.put("orderAmount", Amount);
		signParams.put("bankAccountName", userName);
		signParams.put("bankAccountNo", bankCard);
		signParams.put("idCardNo", idCard);
		signParams.put("mobile", phone);
		signParams.put("cvn2", securityCode);
		signParams.put("expired", expired);
		signParams.put("smsCode", "");
		signParams.put("isNeedSms", "0");
		signParams.put("isNeedSign", "1");
		signParams.put("productName", "时尚裤子外套");
		signParams.put("productDesc", "万达广场");
		signParams.put("trxCtNm", city);// 消费城市
		signParams.put("trxSourceIp", serviceIp);// 城市服务ip

		// 构建请求参数
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("token", tokenClearText);
		jsonObj.put("spCode", spCode);
		jsonObj.put("orderNo", orderCode);
		jsonObj.put("channelCode", channelCode);
		jsonObj.put("merchantCode", merchantCode);
		jsonObj.put("orderAmount", Amount);
		jsonObj.put("bankAccountName", userName);
		jsonObj.put("bankAccountNo", bankAccountNoCipher);
		jsonObj.put("idCardNo", idCardNoCipher);
		jsonObj.put("mobile", mobileCipher);
		jsonObj.put("cvn2", cvn2Cipher);
		jsonObj.put("expired", expiredCipher);
		jsonObj.put("smsCode", "");
		jsonObj.put("isNeedSms", "0");
		jsonObj.put("isNeedSign", "1");
		jsonObj.put("productName", "时尚裤子外套");
		jsonObj.put("productDesc", "万达广场");
		jsonObj.put("trxCtNm", city);// 消费城市
		jsonObj.put("trxSourceIp", serviceIp);// 城市服务ip
		jsonObj.put("sign", SignUtil.signByMap(key, signParams));

		// 接口访问
		String jsonReq = jsonObj.toJSONString();

		LOG.info("商户侧消费请求信息:==================== " + jsonReq);
		Response response;
		try {
			response = HttpUtil.sendPost(Constants.getServerUrl() + "/v2/trans/merchantConsume", jsonReq);
			String jsonRsp = response.body().string();
			LOG.info("响应信息:=================== " + jsonRsp);
			if (response.isSuccessful()) {

				com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(jsonRsp);
				String data = js.getString("data");
				String code = js.getString("code");
				com.alibaba.fastjson.JSONObject js1 = com.alibaba.fastjson.JSONObject.parseObject(data);
				String respMsg = js1.getString("respMsg");
				if ("000000".equals(code)) {
					maps.put(CommonConstants.RESP_CODE, "999998");
					maps.put(CommonConstants.RESP_MESSAGE, "等待银行扣款中");
					return maps;
				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, respMsg);
					this.addOrderCauseOfFailure(orderCode, respMsg, prp.getIpAddress());
					return maps;

				}
			} else {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "请求消费失败");
				return maps;
			}

		} catch (Exception e) {
			LOG.error("商户侧消费接口出现异常======", e);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "消费失败");
			return maps;
		}
	}

	// 快捷支付异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjquick/fastpay/notify_call")
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
		String data = request.getParameter("data");
		String respCode = request.getParameter("respCode");
		String decrypt = AESUtil.decrypt(data, key);

		net.sf.json.JSONObject fromObject = net.sf.json.JSONObject.fromObject(decrypt);

		LOG.info("fromObject======" + fromObject);

		if ("000000".equals(respCode)) {

			String orderCode = fromObject.getString("requestNo");
			String merchantNo = fromObject.getString("merchantNo");
			String orderStatus = fromObject.getString("orderStatus");
			String amount = fromObject.getString("amount");

			LOG.info("交易流水号orderCode-----------" + orderCode + ",交易金额：" + amount);
			LOG.info("交易商户号merchantNo-----------" + merchantNo);
			LOG.info("交易状态orderStatus-----------" + orderStatus);

			PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
			if ("2".equalsIgnoreCase(orderStatus)) {

				LOG.info("*********************交易成功***********************");

				RestTemplate restTemplate = new RestTemplate();
				MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
				String url = null;
				String result = null;

				url = prp.getIpAddress() + ChannelUtils.getCallBackUrl(prp.getIpAddress());
				// url = prp.getIpAddress() +
				// "/v1.0/transactionclear/payment/update";

				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("status", "1");
				requestEntity.add("order_code", orderCode);
				requestEntity.add("third_code", "");
				try {
					result = restTemplate.postForObject(url, requestEntity, String.class);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("", e);
				}

				LOG.info("订单状态修改成功===================" + orderCode + "====================" + result);

				LOG.info("订单已交易成功!");

				PrintWriter pw = response.getWriter();
				pw.print("success");
				pw.close();

			} else {
				LOG.info("交易异常!");

				PrintWriter pw = response.getWriter();
				pw.print("failed");
				pw.close();
			}
		}
	}

	// 消费状态查询
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjx/consumeQuery")
	public @ResponseBody Object cjhkconsumeQuery(@RequestParam(value = "orderCode") String orderCode) throws Exception {
		LOG.info("开始进入消费状态查询接口========================");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		if (prp == null) {
			return ResultWrap.init(CommonConstants.FALIED, "请求支付失败");
		}
		String idCard = prp.getIdCard();
		Map<String, Object> maps = new HashMap<String, Object>();
		CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idCard);
		String merchantCode = cjhkRegister.getMerchantCode();

		// 获取令牌
		BaseResMessage<TokenRes> tokenRes = new GetSpToken().token(key, spCode);
		String token = tokenRes.getData().getToken();
		// 解密令牌
		String tokenClearText = EncryptUtil.desDecrypt(token, key);

		// 构建签名参数
		TreeMap<String, Object> signParams = new TreeMap<String, Object>();
		signParams.put("token", tokenClearText);
		signParams.put("spCode", spCode);
		signParams.put("orderNo", orderCode);
		signParams.put("merchantCode", merchantCode);

		// 构建请求参数
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("token", tokenClearText);
		jsonObj.put("spCode", spCode);
		jsonObj.put("orderNo", orderCode);
		jsonObj.put("merchantCode", merchantCode);
		jsonObj.put("sign", SignUtil.signByMap(key, signParams));

		// 接口访问
		String jsonReq = jsonObj.toJSONString();

		LOG.info("消费状态查询请求信息: " + jsonReq);

		Response response = HttpUtil.sendPost(Constants.getServerUrl() + "/v2/trans/consumeQuery", jsonReq);
		String message1 = response.message();
		String jsonRsp = response.body().string();
		LOG.info("消费状态查询响应信息: " + jsonRsp);
		if (response.isSuccessful()) {

			com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(jsonRsp);
			String code = js.getString("code");
			String data = js.getString("data");

			com.alibaba.fastjson.JSONObject js1 = com.alibaba.fastjson.JSONObject.parseObject(data);

			if ("000000".equals(code)) {
				String orderStatus = js1.getString("orderStatus");
				String respMsg = js1.getString("respMsg");
				if ("0".equals(orderStatus)) {
					maps.put(CommonConstants.RESP_CODE, "999998");
					maps.put(CommonConstants.RESP_MESSAGE, respMsg);
					return maps;
				} else if ("1".equals(orderStatus)) {
					maps.put(CommonConstants.RESP_CODE, "999998");
					maps.put(CommonConstants.RESP_MESSAGE, respMsg);
					return maps;
				} else if ("2".equals(orderStatus)) {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, respMsg);
					return maps;
				} else if ("3".equals(orderStatus)) {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, respMsg);
					return maps;
				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, respMsg);
					return maps;
				}

			} else {
				maps.put(CommonConstants.RESP_CODE, "999999");
				maps.put(CommonConstants.RESP_MESSAGE, message1);
				return maps;

			}
		} else {
			maps.put(CommonConstants.RESP_CODE, "999999");
			maps.put(CommonConstants.RESP_MESSAGE, message1);
			return maps;
		}

	}

	// 交易费率变更
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjx/merchantChange1-1")
	public @ResponseBody Object cjhkmerChange1(@RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "bankName") String bankName, @RequestParam(value = "rate") String rate,
			@RequestParam(value = "channelCode") String channelCode) throws Exception {

		LOG.info("开始进入交易费率变更接口========================");

		Map<String, Object> maps = new HashMap<String, Object>();

		BankNumCode bankNumCode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
		String bankCode = bankNumCode.getBankBranchcode();
		String bankNum = bankNumCode.getBankNum();
		String bankcode = bankNumCode.getBankCode();
		String bankNum1 = bankNumCode.getBankNum();
		String BankName = Util.queryBankNameByBranchName(bankNum);

		LOG.info("结算卡银行缩写" + BankName);
		LOG.info("发送入网请求的联行号" + bankCode);
		LOG.info("发送入网请求的银行代号" + bankcode);
		LOG.info("发送入网请求的银行代码" + bankNum1);
		CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idCard);
		String merchantCode = cjhkRegister.getMerchantCode();

		// 获取令牌
		BaseResMessage<TokenRes> tokenRes = new GetSpToken().token(key, spCode);
		String token = tokenRes.getData().getToken();
		// 解密令牌
		String tokenClearText = EncryptUtil.desDecrypt(token, key);

		// 构建签名参数
		TreeMap<String, Object> signParams = new TreeMap<String, Object>();
		signParams.put("token", tokenClearText);
		signParams.put("spCode", spCode);
		signParams.put("merchantCode", merchantCode);
		signParams.put("channelCode", channelCode);
		signParams.put("changeType", 1);
		signParams.put("debitRate", rate);
		signParams.put("debitCapAmount", "99999900");
		signParams.put("creditRate", rate);
		signParams.put("creditCapAmount", "99999900");

		// 构建请求参数
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("token", tokenClearText);
		jsonObj.put("spCode", spCode);
		jsonObj.put("merchantCode", merchantCode);
		jsonObj.put("channelCode", channelCode);
		jsonObj.put("changeType", 1);
		jsonObj.put("debitRate", rate);
		jsonObj.put("debitCapAmount", "99999900");
		jsonObj.put("creditRate", rate);
		jsonObj.put("creditCapAmount", "99999900");
		jsonObj.put("sign", SignUtil.signByMap(key, signParams));

		// 接口访问
		String jsonReq = jsonObj.toJSONString();

		LOG.info("交易费率变更请求信息:============ " + jsonReq);

		Response response;
		try {
			response = HttpUtil.sendPost(Constants.getServerUrl() + "/v2/merchant/merchantChange", jsonReq);
			String jsonRsp = response.body().string();
			LOG.info("交易费率变更响应信息:====================== " + jsonRsp);
			if (response.isSuccessful()) {

				com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(jsonRsp);
				String code = js.getString("code");
				String message = js.getString("message");

				if ("000000".equals(code)) {
					cjhkRegister.setRate(rate);
					topupPayChannelBusiness.createCJHKRegister(cjhkRegister);

					this.cjhkmerChange2(idCard, rate, channelCode);
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, message);
					return maps;
				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, message);

					return maps;

				}
			} else {
				String message = response.message();
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message);
				return maps;
			}
		} catch (Exception e) {
			LOG.error("交易费率变更接口出现异常======", e);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "交易费率变更失败");
			return maps;
		}
	}

	 //交易费率新增新增渠道号
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjx/merchantChange1-2")
	public @ResponseBody Object cjhkmerChange2(@RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "rate") String rate, @RequestParam(value = "channelCode") String channelCode)
					throws Exception {

		LOG.info("开始进入交易费率新增变更渠道费率变更接口========================");

		Map<String, Object> maps = new HashMap<String, Object>();

		CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idCard);
		String merchantCode = cjhkRegister.getMerchantCode();

		// 获取令牌
		BaseResMessage<TokenRes> tokenRes = new GetSpToken().token(key, spCode);
		String token = tokenRes.getData().getToken();
		// 解密令牌
		String tokenClearText = EncryptUtil.desDecrypt(token, key);

		// 构建签名参数
		TreeMap<String, Object> signParams = new TreeMap<String, Object>();
		signParams.put("token", tokenClearText);
		signParams.put("spCode", spCode);
		signParams.put("merchantCode", merchantCode);
		signParams.put("channelCode", channelCode);
		signParams.put("changeType", 2);
		signParams.put("debitRate", rate);
		signParams.put("debitCapAmount", "99999900");
		signParams.put("creditRate", rate);
		signParams.put("creditCapAmount", "99999900");

		// 构建请求参数
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("token", tokenClearText);
		jsonObj.put("spCode", spCode);
		jsonObj.put("merchantCode", merchantCode);
		jsonObj.put("channelCode", channelCode);
		jsonObj.put("changeType", 2);
		jsonObj.put("debitRate", rate);
		jsonObj.put("debitCapAmount", "99999900");
		jsonObj.put("creditRate", rate);
		jsonObj.put("creditCapAmount", "99999900");
		jsonObj.put("sign", SignUtil.signByMap(key, signParams));

		// 接口访问
		String jsonReq = jsonObj.toJSONString();

		LOG.info("交易费率新增渠道号请求信息:=============== " + jsonReq);

		Response response;
		try {
			response = HttpUtil.sendPost(Constants.getServerUrl() + "/v2/merchant/merchantChange", jsonReq);
			String message1 = response.message();
			String jsonRsp = response.body().string();
			LOG.info("交易费率新增渠道号响应信息:============== " + jsonRsp);
			if (response.isSuccessful()) {
				com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(jsonRsp);
				String code = js.getString("code");
				String message = js.getString("message");
				if ("000000".equals(code)) {
					cjhkRegister.setRate(rate);
					topupPayChannelBusiness.createCJHKRegister(cjhkRegister);
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, message);
					return maps;
				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, message);
					return maps;

				}
			} else {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message1);
				return maps;

			}
		} catch (Exception e) {
			LOG.error("交易费率新增渠道接口出现异常======", e);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "交易费率新增渠道失败");
			return maps;
		}
	}

	// 提现费率变更
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjx/merchantChange1-3")
	public @ResponseBody Object cjhkmerChange3(@RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "bankName") String bankName, @RequestParam(value = "rate") String rate,
			@RequestParam(value = "extraFee") String extraFee, @RequestParam(value = "channelCode") String channelCode)
					throws Exception {

		LOG.info("开始进入提现费率变更接口========================");

		Map<String, Object> maps = new HashMap<String, Object>();

		BankNumCode bankNumCode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
		String bankCode = bankNumCode.getBankBranchcode();
		String bankNum = bankNumCode.getBankNum();
		String bankcode = bankNumCode.getBankCode();
		String bankNum1 = bankNumCode.getBankNum();
		String BankName = Util.queryBankNameByBranchName(bankNum);
		String extra = new BigDecimal(extraFee).multiply(new BigDecimal("100")).setScale(0).toString();
		LOG.info("固定单笔附加手续费金额，单位分:" + extra);

		LOG.info("结算卡银行缩写" + BankName);
		LOG.info("发送入网请求的联行号" + bankCode);
		LOG.info("发送入网请求的银行代号" + bankcode);
		LOG.info("发送入网请求的银行代码" + bankNum1);
		CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idCard);
		String merchantCode = cjhkRegister.getMerchantCode();

		// 获取令牌
		BaseResMessage<TokenRes> tokenRes = new GetSpToken().token(key, spCode);
		String token = tokenRes.getData().getToken();
		// 解密令牌
		String tokenClearText = EncryptUtil.desDecrypt(token, key);

		// 构建签名参数
		TreeMap<String, Object> signParams = new TreeMap<String, Object>();
		signParams.put("token", tokenClearText);
		signParams.put("spCode", spCode);
		signParams.put("merchantCode", merchantCode);
		signParams.put("channelCode", channelCode);
		signParams.put("changeType", 3);
		signParams.put("debitRate", rate);
		signParams.put("debitCapAmount", "99999900");
		signParams.put("creditRate", rate);
		signParams.put("creditCapAmount", "99999900");
		signParams.put("withdrawDepositRate", "0");
		signParams.put("withdrawDepositSingleFee", extra);
		// 构建请求参数
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("token", tokenClearText);
		jsonObj.put("spCode", spCode);
		jsonObj.put("merchantCode", merchantCode);
		jsonObj.put("channelCode", channelCode);
		jsonObj.put("changeType", 3);
		jsonObj.put("debitRate", rate);
		jsonObj.put("debitCapAmount", "99999900");
		jsonObj.put("creditRate", rate);
		jsonObj.put("creditCapAmount", "99999900");
		jsonObj.put("withdrawDepositRate", "0");
		jsonObj.put("withdrawDepositSingleFee", extra);
		jsonObj.put("sign", SignUtil.signByMap(key, signParams));

		// 接口访问
		String jsonReq = jsonObj.toJSONString();

		LOG.info("提现费率变更请求信息:=============== " + jsonReq);

		Response response;
		try {
			response = HttpUtil.sendPost(Constants.getServerUrl() + "/v2/merchant/merchantChange", jsonReq);
			String message1 = response.message();
			String jsonRsp = response.body().string();
			LOG.info("提现费率变更响应信息:================== " + jsonRsp);
			if (response.isSuccessful()) {
				com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(jsonRsp);
				String code = js.getString("code");
				String message = js.getString("message");
				if ("000000".equals(code)) {
					cjhkRegister.setExtraFee(extraFee);
					topupPayChannelBusiness.createCJHKRegister(cjhkRegister);
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, message);
					return maps;
				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, message);
					return maps;

				}
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message1);
				return maps;
			}
		} catch (Exception e) {
			LOG.info("提现费率变更接口出现异常======", e);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "提现费率变更失败");
			return maps;
		}
	}

	// 体现
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjx/withdrawDeposit")
	public @ResponseBody Object withdrawDeposit(HttpServletRequest request,
			@RequestParam(value = "orderCode") String orderCode) throws Exception {

		LOG.info("开始体现接口========================");

		Map<String, Object> maps = new HashMap<String, Object>();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String userName = prp.getUserName();
		String idCard = prp.getIdCard();
		String bankName = prp.getCreditCardBankName();
		String bankCard = prp.getBankCard();
		String realamount = prp.getRealAmount();
		String extra = prp.getExtra();

		BankNumCode bankNumCode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
		String Amount = new BigDecimal(realamount).multiply(new BigDecimal("100")).setScale(0).toString();

		LOG.info("订单总金额，单位分:" + Amount);
		String bankCode = bankNumCode.getBankBranchcode();
		LOG.info("银行联行号:" + bankCode);
		CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idCard);
		String merchantCode = cjhkRegister.getMerchantCode();

		// 获取令牌
		BaseResMessage<TokenRes> tokenRes = new GetSpToken().token(key, spCode);
		String token = tokenRes.getData().getToken();
		// 解密令牌
		String tokenClearText = EncryptUtil.desDecrypt(token, key);

		// 敏感数据3DES加密
		String bankAccountNoCipher = EncryptUtil.desEncrypt(bankCard, key);

		int i = 400;
		if ("T1".equals(extra)) {
			i = 402;
		}
		// 构建签名参数
		TreeMap<String, Object> signParams = new TreeMap<String, Object>();
		signParams.put("token", tokenClearText);
		signParams.put("spCode", spCode);
		signParams.put("reqFlowNo", orderCode);
		signParams.put("merchantCode", merchantCode);
		signParams.put("walletType", i);
		signParams.put("amount", Amount);
		signParams.put("bankAccountName", userName);
		signParams.put("bankAccountNo", bankCard);
		signParams.put("bankName", bankName);
		signParams.put("bankSubName", "上海宝山支行");
		signParams.put("bankChannelNo", bankCode);

		// 构建请求参数
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("token", tokenClearText);
		jsonObj.put("spCode", spCode);
		jsonObj.put("reqFlowNo", orderCode);
		jsonObj.put("merchantCode", merchantCode);
		jsonObj.put("walletType", i);
		jsonObj.put("amount", Amount);
		jsonObj.put("bankAccountName", userName);
		jsonObj.put("bankAccountNo", bankAccountNoCipher);
		jsonObj.put("bankName", bankName);
		jsonObj.put("bankSubName", "上海宝山支行");
		jsonObj.put("bankChannelNo", bankCode);
		jsonObj.put("sign", SignUtil.signByMap(key, signParams));

		// 接口访问
		String jsonReq = jsonObj.toJSONString();
		LOG.info("提现请求信息========================" + jsonReq);
		Response response;
		try {
			response = HttpUtil.sendPost(Constants.getServerUrl() + "/v2/trans/withdraw", jsonReq);
			String jsonRsp = response.body().string();
			LOG.info("提现返回信息========================" + jsonRsp);
			if (response.isSuccessful()) {
				com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(jsonRsp);
				String code = js.getString("code");
				String message = js.getString("message");
				if ("000000".equals(code)) {
					maps.put(CommonConstants.RESP_CODE, "999998");
					maps.put(CommonConstants.RESP_MESSAGE, message);
					return maps;
				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, message);
					this.addOrderCauseOfFailure(orderCode, message, prp.getIpAddress());
					return maps;

				}

			} else {
				String message = response.message();
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message);
				return maps;

			}

		} catch (Exception e) {
			LOG.error("提现接口出现异常======", e);
			maps.put(CommonConstants.RESP_CODE, "999998");
			maps.put(CommonConstants.RESP_MESSAGE, "请求异常，等待查询");
			return maps;
		}
	}

	// 手动代付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjx/transferbymanual")
	public @ResponseBody Object transferByManual(@RequestParam(value = "phone") String phone,
			@RequestParam(value = "brandId") String brandId, @RequestParam(value = "realAmount") String realAmount,
			@RequestParam(value = "isT1", required = false, defaultValue = "0") String isT1,
			@RequestParam(value = "ipAddress", required = false, defaultValue = "http://106.15.47.73") String ipAddress)
					throws Exception {

		Map<String, String> maps = new HashMap<String, String>();

		String uuid = UUIDGenerator.getUUID();
		LOG.info("生成的代付订单号=====" + uuid);

		RestTemplate restTemplate = new RestTemplate();
		String url = ipAddress + "/v1.0/user/query/phone";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("phone", phone);
		requestEntity.add("brandId", brandId);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================" + result);
		net.sf.json.JSONObject jsonObject;
		net.sf.json.JSONObject resultObj;
		long userId;
		try {
			jsonObject = net.sf.json.JSONObject.fromObject(result);
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

		jsonObject = net.sf.json.JSONObject.fromObject(result);
		String respCode = jsonObject.getString(CommonConstants.RESP_CODE);
		if (CommonConstants.SUCCESS.equals(respCode)) {
			resultObj = jsonObject.getJSONObject(CommonConstants.RESULT);
			String bankCard = resultObj.getString("cardNo");
			String bankName = resultObj.getString("bankName");
			String userName = resultObj.getString("userName");
			String idcard = resultObj.getString("idcard");

			BankNumCode bankNumCode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
			String Amount = new BigDecimal(realAmount).multiply(new BigDecimal("100")).setScale(0).toString();

			LOG.info("订单总金额，单位分:" + Amount);
			String bankCode = bankNumCode.getBankBranchcode();
			LOG.info("银行联行号:" + bankCode);
			CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idcard);
			String merchantCode = cjhkRegister.getMerchantCode();

			// 获取令牌
			BaseResMessage<TokenRes> tokenRes = new GetSpToken().token(key, spCode);
			String token = tokenRes.getData().getToken();
			// 解密令牌
			String tokenClearText = EncryptUtil.desDecrypt(token, key);

			// 敏感数据3DES加密
			String bankAccountNoCipher = EncryptUtil.desEncrypt(bankCard, key);

			int i = 400;
			if ("1".equals(isT1)) {
				i = 402;
			}
			// 构建签名参数
			TreeMap<String, Object> signParams = new TreeMap<String, Object>();
			signParams.put("token", tokenClearText);
			signParams.put("spCode", spCode);
			signParams.put("reqFlowNo", uuid);
			signParams.put("merchantCode", merchantCode);
			signParams.put("walletType", i);
			signParams.put("amount", Amount);
			signParams.put("bankAccountName", userName);
			signParams.put("bankAccountNo", bankCard);
			signParams.put("bankName", bankName);
			signParams.put("bankSubName", "上海宝山支行");
			signParams.put("bankChannelNo", bankCode);

			// 构建请求参数
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("token", tokenClearText);
			jsonObj.put("spCode", spCode);
			jsonObj.put("reqFlowNo", uuid);
			jsonObj.put("merchantCode", merchantCode);
			jsonObj.put("walletType", i);
			jsonObj.put("amount", Amount);
			jsonObj.put("bankAccountName", userName);
			jsonObj.put("bankAccountNo", bankAccountNoCipher);
			jsonObj.put("bankName", bankName);
			jsonObj.put("bankSubName", "上海宝山支行");
			jsonObj.put("bankChannelNo", bankCode);
			jsonObj.put("sign", SignUtil.signByMap(key, signParams));

			// 接口访问
			String jsonReq = jsonObj.toJSONString();
			LOG.info("提现请求信息========================" + jsonReq);
			Response response;
			try {
				response = HttpUtil.sendPost(Constants.getServerUrl() + "/v2/trans/withdraw", jsonReq);
				String jsonRsp = response.body().string();
				LOG.info("提现返回信息========================" + jsonRsp);
				if (response.isSuccessful()) {
					com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(jsonRsp);
					String code = js.getString("code");
					String message = js.getString("message");
					if ("000000".equals(code)) {
						maps.put(CommonConstants.RESP_CODE, "999998");
						maps.put(CommonConstants.RESP_MESSAGE, message);
						return maps;
					} else {
						maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						maps.put(CommonConstants.RESP_MESSAGE, message);
						return maps;
					}
				} else {
					String message = response.message();
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, message);
					return maps;
				}
			} catch (Exception e) {
				LOG.error("提现接口出现异常======", e);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "提现失败");
			}
		}
		return maps;
	}

	// 体现结果查询
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjx/withdrawQuery")
	public @ResponseBody Object cjhkwithdrawQuery(@RequestParam(value = "orderCode") String orderCode)
			throws Exception {

		LOG.info("开始体现接口========================");

		Map<String, Object> maps = new HashMap<String, Object>();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		if (prp == null) {
			return ResultWrap.init(CommonConstants.FALIED, "请求支付失败");
		}
		String idCard = prp.getIdCard();

		CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idCard);
		String merchantCode = cjhkRegister.getMerchantCode();

		// 获取令牌
		BaseResMessage<TokenRes> tokenRes = new GetSpToken().token(key, spCode);
		String token = tokenRes.getData().getToken();
		// 解密令牌
		String tokenClearText = EncryptUtil.desDecrypt(token, key);

		// 构建签名参数
		TreeMap<String, Object> signParams = new TreeMap<String, Object>();
		signParams.put("token", tokenClearText);
		signParams.put("spCode", spCode);
		signParams.put("reqFlowNo", orderCode);
		signParams.put("merchantCode", merchantCode);

		// 构建请求参数
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("token", tokenClearText);
		jsonObj.put("spCode", spCode);
		jsonObj.put("reqFlowNo", orderCode);
		jsonObj.put("merchantCode", merchantCode);
		jsonObj.put("sign", SignUtil.signByMap(key, signParams));

		// 接口访问
		String jsonReq = jsonObj.toJSONString();

		LOG.info("提现查询请求信息:============== " + jsonReq);
		Response response;
		try {
			response = HttpUtil.sendPost(Constants.getServerUrl() + "/v2/trans/withdrawQuery", jsonReq);
			String jsonRsp = response.body().string();
			LOG.info("提现结果查询响应信息:================== " + jsonRsp);

			if (response.isSuccessful()) {
				com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(jsonRsp);
				String data = js.getString("data");
				String code = js.getString("code");
				String message = js.getString("message");
				com.alibaba.fastjson.JSONObject js1 = com.alibaba.fastjson.JSONObject.parseObject(data);
				if ("000000".equals(code)) {
					String remitStatus = js1.getString("remitStatus");

					if ("1".equalsIgnoreCase(remitStatus)) {
						maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						maps.put(CommonConstants.RESP_MESSAGE, "出款成功");
					} else if ("2".equals(remitStatus)) {
						maps.put(CommonConstants.RESP_CODE, "999998");
						maps.put(CommonConstants.RESP_MESSAGE, "等待出款");
						return maps;

					} else {
						maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						this.addOrderCauseOfFailure(orderCode, message, prp.getIpAddress());
						return maps;
					}

				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, message);
					return maps;

				}
			} else {
				String message1 = response.message();
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message1);
				return maps;

			}
		} catch (Exception e) {
			LOG.error("提现结果查询出现异常======", e);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "提现结果查询失败");
			return maps;
		}
		return maps;
	}

	// 商户钱包查询
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjx/walletQuery")
	public @ResponseBody Object cjhkwalletQuery(HttpServletRequest request,
			@RequestParam(value = "idCard") String idCard) throws Exception {

		LOG.info("开始体现接口========================");

		Map<String, Object> maps = new HashMap<String, Object>();

		CJHKRegister cjhkRegister = topupPayChannelBusiness.getCJHKRegisterByIdCard(idCard);
		String merchantCode = cjhkRegister.getMerchantCode();

		// 获取令牌
		BaseResMessage<TokenRes> tokenRes = new GetSpToken().token(key, spCode);
		String token = tokenRes.getData().getToken();
		// 解密令牌
		String tokenClearText = EncryptUtil.desDecrypt(token, key);

		// 构建签名参数
		TreeMap<String, Object> signParams = new TreeMap<String, Object>();
		signParams.put("token", tokenClearText);
		signParams.put("spCode", spCode);
		signParams.put("merchantCode", merchantCode);

		// 构建请求参数
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("token", tokenClearText);
		jsonObj.put("spCode", spCode);
		jsonObj.put("merchantCode", merchantCode);
		jsonObj.put("sign", SignUtil.signByMap(key, signParams));

		// 接口访问
		String jsonReq = jsonObj.toJSONString();
		LOG.info("商户钱包查询请求信息: " + jsonReq);

		Response response = HttpUtil.sendPost(Constants.getServerUrl() + "/v2/wallet/walletQuery", jsonReq);
		String jsonRsp = response.body().string();
		LOG.info("商户钱包查询接口响应信息,金额为分: " + jsonRsp);

		if (response.isSuccessful()) {
			com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(jsonRsp);
			String code = js.getString("code");
			String message = js.getString("message");
			String data = js.getString("data");
			if ("000000".equals(code)) {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "金额为分" + data);

				return maps;
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message);
				return maps;

			}
		} else {
			String message1 = response.message();
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, message1);
			return maps;

		}
	}

	// 商户信息查询
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjx/queryQkMerchant")
	public @ResponseBody Object cjhkqueryQkMerchant(HttpServletRequest request,
			@RequestParam(value = "orderCode") String orderCode) throws Exception {

		LOG.info("开始进入商户信息查询接口========================");

		Map<String, Object> maps = new HashMap<String, Object>();

		/*
		 * // 获取令牌 BaseResMessage<TokenRes> tokenRes = new
		 * GetSpToken().token(key, spCode); String token =
		 * tokenRes.getData().getToken(); // 解密令牌 String tokenClearText =
		 * EncryptUtil.desDecrypt(token, key);
		 *
		 * // 构建签名参数 TreeMap<String, Object> signParams = new TreeMap<String,
		 * Object>(); signParams.put("token", tokenClearText);
		 * signParams.put("spCode", spCode); signParams.put("reqFlowNo",
		 * reqFlowNo);
		 *
		 * // 构建请求参数 JSONObject jsonObj = new JSONObject(); jsonObj.put("token",
		 * tokenClearText); jsonObj.put("spCode", spCode);
		 * jsonObj.put("reqFlowNo", reqFlowNo); jsonObj.put("sign",
		 * SignUtil.signByMap(key, signParams));
		 *
		 * // 接口访问 String jsonReq = jsonObj.toJSONString();
		 * System.out.println(sdf.format(new Date()) + "请求信息: " + jsonReq);
		 *
		 * Response response = HttpUtil.sendPost(url, jsonReq); if
		 * (response.isSuccessful()) { String jsonRsp =
		 * response.body().string(); System.out.println(sdf.format(new Date()) +
		 * "响应信息: " + jsonRsp); } else { System.out.println(sdf.format(new
		 * Date()) + "响应码: " + response.code()); throw new IOException(
		 * "Unexpected code " + message); } }
		 */

		// 获取令牌
		BaseResMessage<TokenRes> tokenRes = new GetSpToken().token(key, spCode);
		String token = tokenRes.getData().getToken();
		// 解密令牌
		String tokenClearText = EncryptUtil.desDecrypt(token, key);

		// 构建签名参数
		TreeMap<String, Object> signParams = new TreeMap<String, Object>();
		signParams.put("token", tokenClearText);
		signParams.put("spCode", spCode);
		signParams.put("reqFlowNo", "10000022");

		// 构建请求参数
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("token", tokenClearText);
		jsonObj.put("spCode", spCode);
		signParams.put("reqFlowNo", "10000022");
		jsonObj.put("sign", SignUtil.signByMap(key, signParams));

		// 接口访问
		String jsonReq = jsonObj.toJSONString();
		LOG.info(sdf.format(new Date()) + "请求信息: " + jsonReq);

		Response response = HttpUtil.sendPost(Constants.getServerUrl() + "/v2/merchant/queryQkMerchant", jsonReq);
		String message1 = response.message();
		if (response.isSuccessful()) {
			String jsonRsp = response.body().string();
			LOG.info(sdf.format(new Date()) + "响应信息: " + jsonRsp);
			com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(jsonRsp);
			String code = js.getString("code");
			String message = js.getString("message");

			if ("000000".equals(code)) {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, message);

				return maps;
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, message);
				return maps;

			}
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, message1);
			return maps;

		}
	}

	// 跳转绑卡页面
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/tocjquick/bindcard1")
	public String returnCJBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String bankName = request.getParameter("bankName");
		String cardType = request.getParameter("cardType");
		String bankCard = request.getParameter("bankCard");
		String ipAddress = request.getParameter("ipAddress");
		String userName = request.getParameter("userName");
		String phone = request.getParameter("phone");
		String idCard = request.getParameter("idCard");
		String channelCode = request.getParameter("channelCode");

		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("bankName", bankName);
		model.addAttribute("cardType", cardType);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("ipAddress", ipAddress);
		model.addAttribute("userName", userName);
		model.addAttribute("phone", phone);
		model.addAttribute("idCard", idCard);
		model.addAttribute("channelCode", channelCode);
		return "cjquickbindcard";
	}

	public List<CJHKFactory> chooseCityIp(String provinceName, String cityName) {
		if(provinceName==null ||provinceName==""|| cityName==null|| cityName=="") {
			return null;
		}
		List<CJHKFactory> cjhkCityIP = topupPayChannelBusiness
				.getCJHKChooseCityIPBycityName(provinceName + cityName + "");
		if (cjhkCityIP == null) {
			cjhkCityIP = topupPayChannelBusiness.getCJHKChooseCityIPBycityName(cityName);
		}
		return cjhkCityIP;

	}

}
