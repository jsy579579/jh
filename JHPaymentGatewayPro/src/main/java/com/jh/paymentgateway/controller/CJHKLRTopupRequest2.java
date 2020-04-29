package com.jh.paymentgateway.controller;

import cjhkchannel.utils.RequestURL;
import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import com.alibaba.fastjson.JSONObject;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.*;
import com.jh.paymentgateway.util.Util;
import com.jh.paymentgateway.util.cjx.*;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author 作者:lirui
 * @version 创建时间：2019年5月27日 下午1:55:40 类说明
 */
@Controller
public class CJHKLRTopupRequest2 extends BaseChannel {

	private static final Logger log = LoggerFactory.getLogger(CJHKLRTopupRequest2.class);
	private static final String key = "6A73693085FB2912EC28F69FBE1C3562D49045AACDDB09DC"; // 服务商密钥
	private static final String spCode = "10205839"; // 服务商编号
	private static final String getServerUrl = "https://qkapi.chanpay.com";
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	@Autowired
	RedisUtil redisUtil;

	// 跟还款对接的接口
	@SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjhklr/dockingEntrance2")
	public @ResponseBody Object docking1(@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "dbankCard") String dbankCard, @RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "phone") String phone, @RequestParam(value = "dphone") String dphone,
			@RequestParam(value = "userName") String userName, @RequestParam(value = "bankName") String bankName1,
			@RequestParam(value = "dbankName") String dbankName, @RequestParam(value = "extraFee") String extraFee,
			@RequestParam(value = "securityCode") String securityCode, @RequestParam(value = "rate") String rate,
			@RequestParam(value = "expiredTime") String expired) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String expiredTime = this.expiredTimeToYYMM(expired);
		String bankName = Util.queryBankNameByBranchName(bankName1);
		CJXChannelCode cjxChannelCode;
		cjxChannelCode = topupPayChannelBusiness.getCJXChannelCode(bankName);
		if (cjxChannelCode == null) {
			return ResultWrap.init(CommonConstants.FALIED, "抱歉,该渠道银行维护,暂不支持该银行！");
		}
		/*log.info("全国落地C===============" + bankName + "=============" + cjxChannelCode.getChannelCode());
		if (!"101001".equals(cjxChannelCode.getChannelCode()) & !"110001".equals(cjxChannelCode.getChannelCode()) & !"1110".equals(cjxChannelCode.getChannelCode())) {
			return ResultWrap.init(CommonConstants.FALIED, "抱歉,该渠道银行维护,暂不支持该银行！");
		}*/
		String channelCode = cjxChannelCode.getChannelCode();
		log.info("全国落地C===============卡号" + bankName + "=============渠道号" + channelCode);
		CJHKLRChannelWhite cjhklrChannelWhite = topupPayChannelBusiness.getChannelWhite("2","1",channelCode);
		if(cjhklrChannelWhite == null){
			return ResultWrap.init(CommonConstants.FALIED, "抱歉,该渠道银行维护,暂不支持该银行！");
		}
		CJHKLRRegister cjhklr = topupPayChannelBusiness.getRegister(idCard);
		CJHKLRBindCard cjhklrBind = topupPayChannelBusiness.getBindCard(bankCard);
		try {
			// 判断用户进件
			if (cjhklr == null) {
				log.info("全国落地C=======身份证号{}===========当前用户未进件",idCard);
				map = (Map<String, Object>) this.register(userName, phone, idCard, extraFee, bankName, bankCard, rate,
						expiredTime, securityCode, dbankName, dphone, dbankCard);
				return map;
			}
			if (cjhklr.getRate2() == null || "".equals(cjhklr.getRate2())) {
				try {
					map = (Map<String, Object>) addChannelRate(idCard, rate, "101001");
					log.info("身份证：{}===================新增渠道:101001============{}",idCard,map.toString());
					/*if (!"000000".equals(map.get("resp_code"))) {
						log.info("========================新增渠道:101001失败");
						log.info("身份证：{}===================新增渠道:101001============{}",idCard,map.toString());
						return map;
					}*/
					map = (Map<String, Object>) addChannelRate(idCard, rate, "110001");
					log.info("身份证：{}===================新增渠道:110001============{}",idCard,map.toString());
					/*if (!"000000".equals(map.get("resp_code"))) {
						log.info("========================新增渠道:110001失败");
						return map;
					}*/
					// 存储新增渠道费率rate2:101001和110001
					cjhklr.setRate2(rate);
					topupPayChannelBusiness.createRegister(cjhklr);
				} catch (Exception e) {
					log.info("============身份证：{}===========================新增渠道101001 110001 1110失败:{}",idCard,e);
				}
			}
			CJHKLRChannelCodeRelation channelRelation = topupPayChannelBusiness.getCJHKLRChannelCodeRelation(bankCard);
			if (channelRelation == null || !channelCode.equals(channelRelation.getChanelCode()) || "0".equals(channelRelation.getStatus())|| cjhklrBind == null || "0".equals(cjhklrBind.getStatus())) {
				map.put(CommonConstants.RESP_CODE, "999996");
				map.put(CommonConstants.RESP_MESSAGE, "进入签约");
				map.put(CommonConstants.RESULT,
						ipAddress + "/v1.0/paymentgateway/topup/tocjquick/bindcard/lr?bankName="
								+ URLEncoder.encode(bankName, "UTF-8") + "&cardType="
								+ URLEncoder.encode("贷记卡", "UTF-8") + "&bankCard=" + bankCard + "&phone=" + phone
								+ "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&idCard=" + idCard
								+ "&userName=" + userName + "&channelCode=" + channelCode + "&ipAddress=" + ipAddress);
				return map;
			} else {
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "已签约");
				return map;
			}
		} catch (Exception e) {
			log.error("与还款对接接口出现异常======", e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "与还款对接失败");
			return map;
		}

	}

	// 注册接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjhk/register/lr2")
	public @ResponseBody Object register(@RequestParam(value = "userName") String userName,
			@RequestParam(value = "phone") String phone, @RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "extraFee") String extraFee, @RequestParam(value = "bankName") String bankName,
			@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "rate") String rate,
			@RequestParam(value = "expiredTime") String expired,
			@RequestParam(value = "securityCode") String securityCode,
			@RequestParam(value = "dbankName") String dbankName, @RequestParam(value = "dphone") String dphone,
			@RequestParam(value = "dbankCard") String dbankCard) throws Exception {

		log.info("全国落地C开始进入进件接口======身份证：{}============手机号：{}==========姓名：{}=========银行卡：{}=========银行名称:{}",idCard,phone,userName,bankCard,bankName);
		String expiredTime = this.expiredTimeToYYMM(expired);
		Map<String, Object> maps;
		String extra = new BigDecimal(extraFee).multiply(new BigDecimal("100")).setScale(0).toString();
		log.info("全国落地B固定单笔附加手续费金额，单位分:{}" ,extra);
		String bankName1 = Util.queryBankNameByBranchName(bankName);
		log.info("全国落地B====================================utils银行名称:" + bankName1);

		CJXChannelCode channelCode = topupPayChannelBusiness.getCJXChannelCode(bankName1);
		if (channelCode == null) {
			return ResultWrap.init(CommonConstants.FALIED, "抱歉,暂不支持该信用卡银行");
		}
		BankNumCode dbankNumCode = topupPayChannelBusiness.getBankNumCodeByBankName(dbankName);
		if (dbankNumCode == null) {
			return ResultWrap.init(CommonConstants.FALIED, "您的到账卡不支持,请更换默认到账卡!");
		}
		String dbankCode = "301290000007";// 交通银行联行号
		String dbankNum = dbankNumCode.getBankNum();
		String dbankcode = dbankNumCode.getBankCode();
		String dBankName = Util.queryBankNameByBranchName(dbankNum);
		log.info("结算卡银行缩写" + dBankName);
		log.info("发送入网请求的联行号" + dbankCode);
		log.info("发送入网请求的银行代号" + dbankcode);
		log.info("发送入网请求的银行代码" + dbankNum);

		// 获取令牌
		BaseResMessage<TokenRes> tokenRes = new GetSpToken().token(key, spCode);
		log.info("===================获取的令牌:" + tokenRes);
		String token = tokenRes.getData().getToken();
		log.info("===================获取的令牌:" + token);
		// 解密令牌
		String tokenClearText = EncryptUtil.desDecrypt(token, key);
		log.info("===================解密的令牌:" + tokenClearText);
		// 敏感数据3DES加密
		String dbankAccountNoCipher = EncryptUtil.desEncrypt(dbankCard, key);
		String dmobileCipher = EncryptUtil.desEncrypt(dphone, key);
		String idCardNoCipher = EncryptUtil.desEncrypt(idCard, key);
		// 构建签名参数
		TreeMap<String, Object> signParams = new TreeMap<String, Object>();
		signParams.put("token", tokenClearText);
		signParams.put("spCode", spCode);
		signParams.put("channelCode", "101001");// 默认渠道一（0.45%）
		signParams.put("merName", userName);
		signParams.put("merAbbr", userName);
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
		signParams.put("withdrawDepositRateT1", "0");
		signParams.put("withdrawDepositSingleFeeT1", extra);
		signParams.put("reqFlowNo", String.valueOf(System.currentTimeMillis()));
		// 构建请求参数
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("token", tokenClearText);
		jsonObj.put("spCode", spCode);
		jsonObj.put("channelCode", "101001");// 默认渠道一（0.45%）
		jsonObj.put("merName", userName);
		jsonObj.put("merAbbr", userName);
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
		jsonObj.put("withdrawDepositRateT1", "0");
		jsonObj.put("withdrawDepositSingleFeeT1", extra);
		jsonObj.put("reqFlowNo", String.valueOf(System.currentTimeMillis()));

		jsonObj.put("sign", SignUtil.signByMap(key, signParams));

		// 接口访问
		String jsonReq = jsonObj.toJSONString();
		log.info("进件接口请求信息========身份证号：{}======手机号：{}=======: {}",idCard,phone,jsonReq);

		// 响应信息:
		Response response;
		try {
			response = HttpUtil.sendPost(getServerUrl + RequestURL.IS_Register, jsonReq);
			String message1 = response.message();
			String jsonRsp = response.body().string();
			log.info("CJHK_QUICK进件响应信息========身份证号：{}======手机号：{}==========: {}",idCard,phone,jsonRsp);

			if (response.isSuccessful()) {
				JSONObject js = JSONObject.parseObject(jsonRsp);
				String code = js.getString("code");
				String message = js.getString("message");
				// 判断进件状态成功
				if ("000000".equals(code)) {
					String data = js.getString("data");
					JSONObject json_cj = JSONObject.parseObject(data);
					String merchantCode = json_cj.getString("merchantCode");
					log.info("身份证号：{}==========手机号：{}==========子商户号:{}========进件成功!",idCard,phone,merchantCode);
					CJHKLRRegister cjhklr = new CJHKLRRegister();
					cjhklr.setBankCard(dbankCard);
					cjhklr.setIdCard(idCard);
					cjhklr.setMerchantCode(merchantCode);
					cjhklr.setPhone(phone);
					cjhklr.setRate2(rate);
					cjhklr.setExtraFee(extraFee);
					cjhklr.setUserName(userName);
					cjhklr.setStatus("1");
					topupPayChannelBusiness.createRegister(cjhklr);
					maps = (Map<String, Object>) addChannelRate(idCard, rate, "110001");
					if (!"000000".equals(maps.get("resp_code"))) {
						log.info("========================新增渠道:110001失败");
						return maps;
					} else {
						// 全国落地C:判断两渠道都开通,在保存费率
						CJHKLRRegister cjlr = topupPayChannelBusiness.getRegister(idCard);
						cjlr.setRate2(rate);
						topupPayChannelBusiness.createRegister(cjlr);
					}
					log.info("开始进入签约======");
					maps.put(CommonConstants.RESP_CODE, "999996");
					maps.put(CommonConstants.RESP_MESSAGE, "进入签约");
					maps.put(CommonConstants.RESULT,
							ipAddress + "/v1.0/paymentgateway/topup/tocjquick/bindcard/lr?bankName="
									+ URLEncoder.encode(bankName, "UTF-8") + "&cardType="
									+ URLEncoder.encode("0", "UTF-8") + "&bankCard=" + bankCard + "&phone=" + phone
									+ "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&idCard="
									+ idCard + "&userName=" + userName + "&channelCode=" + channelCode + "&ipAddress="
									+ ipAddress);
					return maps;

				} else {
					log.error("===============CJHK_QUICK进件状态异常");
					return ResultWrap.init(CommonConstants.FALIED, message);
				}
			} else {
				log.error("==============CJHK_QUICK进件请求失败");
				return ResultWrap.init(CommonConstants.FALIED, message1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("=====================CJHK_QUICK商户进件接口响应response异常");
		}
		return ResultWrap.init(CommonConstants.FALIED, "400注册请求异常");

	}

	// 交易费率新增:新增渠道号
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjhk/add/channel/rate2")
	public @ResponseBody Object addChannelRate(@RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "rate") String rate, @RequestParam(value = "channelCode") String channelCode)
					throws Exception {

		log.info("开始进入交易费率新增变更渠道费率变更接口========================");

		Map<String, Object> maps = new HashMap<String, Object>();

		CJHKLRRegister cjhklrRegister = topupPayChannelBusiness.getRegister(idCard);
		// 获取当前子商户编号
		String merchantCode = cjhklrRegister.getMerchantCode();
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

		log.info("CJHK_QUICK交易费率新增渠道号请求信息:=============== " + jsonReq);

		Response response;
		try {
			response = HttpUtil.sendPost(getServerUrl + RequestURL.IS_Change_Merchant, jsonReq);
			String message1 = response.message();
			String jsonRsp = response.body().string();
			log.info("CJHK_QUICK交易费率新增渠道号响应信息:============== " + jsonRsp);
			if (response.isSuccessful()) {
				JSONObject js = JSONObject.parseObject(jsonRsp);
				String code = js.getString("code");
				String message = js.getString("message");
				if ("000000".equals(code)) {
					log.info("=====交易费率新增渠道号:========" + channelCode + "========" + rate + "======身份证:" + idCard);
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
			e.printStackTrace();
			log.error("CJHK_QUICK交易费率新增渠道接口出现异常================", e);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "交易费率新增渠道失败");
			return maps;
		}
	}

	// 交易费率变更
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjhk/modify/channel/rate2")
	public @ResponseBody Object cjhkChangeRate(
									@RequestParam(value = "idCard") String idCard,
									@RequestParam(value = "rate") String rate,
									@RequestParam(value = "channelCode") String channelCode,
									@RequestParam(value = "extraFee") String extraFee,
									@RequestParam(value = "changeType",required = false,defaultValue = "1") String changeType) throws Exception {

		log.info("开始进入畅捷交易费率变更接口========================");

		Map<String, Object> maps = new HashMap<String, Object>();

		String extra = new BigDecimal(extraFee).multiply(new BigDecimal("100")).setScale(0).toString();
		log.info("固定单笔附加手续费金额，单位分:" + extra);

		CJHKLRRegister cjhklr = topupPayChannelBusiness.getRegister(idCard);
		String merchantCode = cjhklr.getMerchantCode();

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
		signParams.put("changeType", changeType);
		signParams.put("debitRate", rate);
		signParams.put("debitCapAmount", "99999900");
		signParams.put("creditRate", rate);
		signParams.put("creditCapAmount", "99999900");
		signParams.put("withdrawDepositRate", "0");
		signParams.put("withdrawDepositSingleFee", extra);
		signParams.put("withdrawDepositRateT1", "0");
		signParams.put("withdrawDepositSingleFeeT1", extra);
		signParams.put("withdrawDepositRateD1", "0");
		signParams.put("withdrawDepositSingleFeeD1", extra);
		// 构建请求参数
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("token", tokenClearText);
		jsonObj.put("spCode", spCode);
		jsonObj.put("merchantCode", merchantCode);
		jsonObj.put("channelCode", channelCode);
		jsonObj.put("changeType", changeType);
		jsonObj.put("debitRate", rate);
		jsonObj.put("debitCapAmount", "99999900");
		jsonObj.put("creditRate", rate);
		jsonObj.put("creditCapAmount", "99999900");
		jsonObj.put("withdrawDepositRate", "0");
		jsonObj.put("withdrawDepositSingleFee", extra);
		jsonObj.put("withdrawDepositRateT1", "0");
		jsonObj.put("withdrawDepositSingleFeeT1", extra);
		jsonObj.put("withdrawDepositRateD1", "0");
		jsonObj.put("withdrawDepositSingleFeeD1", extra);
		jsonObj.put("sign", SignUtil.signByMap(key, signParams));

		// 接口访问
		String jsonReq = jsonObj.toJSONString();

		log.info("CJHK_QUICK交易费率变更请求信息:============ " + jsonReq);

		Response response;
		try {
			response = HttpUtil.sendPost(getServerUrl + RequestURL.IS_Change_Merchant, jsonReq);
			String jsonRsp = response.body().string();
			log.info("CJHK_QUICK交易费率变更响应信息:====================== " + jsonRsp);
			if (response.isSuccessful()) {
				JSONObject js = JSONObject.parseObject(jsonRsp);
				String code = js.getString("code");
				String message = js.getString("message");
				if ("000000".equals(code)) {
					cjhklr.setRate2(rate);
					cjhklr.setExtraFee(extraFee);
					topupPayChannelBusiness.createRegister(cjhklr);
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
			log.error("CJHK_QUICK交易费率变更接口出现异常======", e);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "交易费率变更失败");
			return maps;
		}
	}

	public List<CJHKFactory> chooseCityIp(String provinceName, String cityName) {
		if(provinceName==null ||provinceName==""|| cityName==null|| cityName=="") {
			return null;
		}
		List<CJHKFactory> cjhkCityIP = topupPayChannelBusiness.getCJHKChooseCityIPBycityName(cityName);
		if (cjhkCityIP == null) {
			cjhkCityIP = topupPayChannelBusiness.getCJHKChooseCityIPBycityName(provinceName);
			if (cjhkCityIP == null) {
				return null;
			}
		}
		return cjhkCityIP;

	}

}