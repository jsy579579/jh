package com.jh.paymentgateway.controller;

import cjhkchannel.utils.RequestURL;
import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import com.alibaba.fastjson.JSONObject;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.*;
import com.jh.paymentgateway.util.Util;
import com.jh.paymentgateway.util.cjhk.AESUtil;
import com.jh.paymentgateway.util.cjx.*;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author 作者:lirui
 * @version 创建时间：2019年5月27日 下午1:55:40 类说明
 */
@Controller
public class CJHKLRTopupRequest extends BaseChannel {

	private static final Logger log = LoggerFactory.getLogger(CJHKLRTopupRequest.class);
	//	private static final String key = "AF3C14B0B32F6AF9624CE00598C7178CBCE67FF65E91EFC6"; // 服务商密钥
	private static final String key = "6A73693085FB2912EC28F69FBE1C3562D49045AACDDB09DC"; // 服务商密钥
	//	private static final String spCode = "10040923"; // 服务商编号
	private static final String spCode = "10205839"; // 服务商编号
	private static final String getServerUrl = "https://qkapi.chanpay.com";
	//private static final String getServerUrl = "http://47.107.104.250:8099";
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	@Autowired
	RedisUtil redisUtil;

	// 跟还款对接的接口
	@SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjhklr/dockingEntrance")
	public @ResponseBody Object docking1(@RequestParam(value = "bankCard") String bankCard,
										 @RequestParam(value = "dbankCard") String dbankCard, @RequestParam(value = "idCard") String idCard,
										 @RequestParam(value = "phone") String phone, @RequestParam(value = "dphone") String dphone,
										 @RequestParam(value = "userName") String userName, @RequestParam(value = "bankName") String bankName1,
										 @RequestParam(value = "dbankName") String dbankName, @RequestParam(value = "extraFee") String extraFee,
										 @RequestParam(value = "securityCode") String securityCode, @RequestParam(value = "rate") String rate,
										 @RequestParam(value = "expiredTime") String expired) throws Exception {
		Map<String, Object> map = new HashMap<>();
		String expiredTime = this.expiredTimeToYYMM(expired);
		String bankName = Util.queryBankNameByBranchName(bankName1);
		CJXChannelCode cjxChannelCode = topupPayChannelBusiness.getCJXChannelCode(bankName);
		if (cjxChannelCode == null) {
			return ResultWrap.init(CommonConstants.FALIED, "抱歉,该渠道银行维护,暂不支持该银行！");
		}
		String channelCode = cjxChannelCode.getChannelCode();
		log.info("全国落地B===============卡号" + bankName + "=============渠道号" + channelCode);
		CJHKLRChannelWhite cjhklrChannelWhite = topupPayChannelBusiness.getChannelWhite("1","1",channelCode);
		if(cjhklrChannelWhite == null){
			return ResultWrap.init(CommonConstants.FALIED, "抱歉,该渠道银行维护,暂不支持该银行！");
		}
		CJHKLRRegister cjhklr = topupPayChannelBusiness.getRegister(idCard);
		CJHKLRBindCard cjhklrBind = topupPayChannelBusiness.getBindCard(bankCard);
		try {
			// 判断用户进件
			if (cjhklr == null) {
				log.info("全国落地B=======身份证号{}===========当前用户未进件",idCard);
				map = (Map<String, Object>) this.register(userName, phone, idCard, extraFee, bankName, bankCard, rate,
						expiredTime, securityCode, dbankName, dphone, dbankCard);
				return map;
			}
			if (cjhklr.getRate() == null || "".equals(cjhklr.getRate()) || cjhklr.getRate().trim().length() == 0) {
				try {
					/*map = (Map<String, Object>) addChannelRate(idCard, rate, "110002");
					if (!"000000".equals(map.get("resp_code"))) {
						log.info("身份证：{}===================新增渠道:110002============{}",idCard,map.get("resp_message"));
						return map;
					}
					map = (Map<String, Object>) addChannelRate(idCard, rate, "101002");
					if (!"000000".equals(map.get("resp_code"))) {
						log.info("身份证：{}===================新增渠道:101002============{}",idCard,map.get("resp_message"));
						return map;
					}*/
					map = (Map<String, Object>) addChannelRate(idCard, rate, "110002");
					log.info("身份证：{}===================新增渠道:110002============{}",idCard,map.toString());
					map = (Map<String, Object>) addChannelRate(idCard, rate, "101002");
					log.info("身份证：{}===================新增渠道:110002============{}",idCard,map.toString());
					// 存储新增渠道费率rate:110002和101002
					cjhklr.setRate(rate);
					topupPayChannelBusiness.createRegister(cjhklr);
				} catch (Exception e) {
					log.info("========身份证：{}==================新增渠道110002或101002失败:{}",idCard,e.getMessage());
				}

			}
			// 新增150001 渠道费率
			if(cjhklr.getRate3() == null || "".equals(cjhklr.getRate3()) || cjhklr.getRate3().trim().length() == 0){
				try {
					/*map = (Map<String, Object>) addChannelRate(idCard, rate, "150001");
					if (!"000000".equals(map.get("resp_code"))) {
						log.info("========================新增渠道:150001失败");
						return map;
					}
					map = (Map<String, Object>) addChannelRate(idCard, rate, "1110");
					if (!"000000".equals(map.get("resp_code"))) {
						log.info("========================新增渠道:1110失败");
						return map;
					}
					map = (Map<String, Object>) addChannelRate(idCard, rate, "1000");
					if (!"000000".equals(map.get("resp_code"))) {
						log.info("========================新增渠道:1000失败");
						return map;
					}*/
					map = (Map<String, Object>) addChannelRate(idCard, rate, "150001");
					log.info("身份证：{}===================新增渠道:150001============{}",idCard,map.toString());
					map = (Map<String, Object>) addChannelRate(idCard, rate, "1000");
					log.info("身份证：{}===================新增渠道:1000============{}",idCard,map.toString());
					map = (Map<String, Object>) addChannelRate(idCard, rate, "1110");
					log.info("身份证：{}===================新增渠道:1110============{}",idCard,map.toString());
					// 存储新增渠道费率rate:150001
					cjhklr.setRate3(rate);
					topupPayChannelBusiness.createRegister(cjhklr);
				} catch (Exception e) {
					log.info("============身份证：{}===========================新增渠道150001 1110 1000失败:{}",idCard,e);
				}
			}
			CJHKLRChannelCodeRelation channelRelation = topupPayChannelBusiness.getCJHKLRChannelCodeRelation(bankCard);
			if (channelRelation == null || !channelCode.equals(channelRelation.getChanelCode()) || "0".equals(channelRelation.getStatus())|| cjhklrBind == null || "0".equals(cjhklrBind.getStatus())) {
				map.put(CommonConstants.RESP_CODE, "999996");
				map.put(CommonConstants.RESP_MESSAGE, "进入签约");
				log.info("全国落地B绑卡==================================================" + bankCard);
				log.info(ipAddress + "/v1.0/paymentgateway/topup/tocjquick/bindcard/lr?bankName="
						+ URLEncoder.encode(bankName, "UTF-8") + "&cardType="
						+ URLEncoder.encode("贷记卡", "UTF-8") + "&bankCard=" + bankCard + "&phone=" + phone
						+ "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&idCard=" + idCard
						+ "&userName=" + userName + "&channelCode=" + channelCode + "&ipAddress=" + ipAddress);
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
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjhk/register/lr")
	public @ResponseBody Object register(@RequestParam(value = "userName") String userName,
										 @RequestParam(value = "phone") String phone, @RequestParam(value = "idCard") String idCard,
										 @RequestParam(value = "extraFee") String extraFee, @RequestParam(value = "bankName") String bankName,
										 @RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "rate") String rate,
										 @RequestParam(value = "expiredTime") String expired,
										 @RequestParam(value = "securityCode") String securityCode,
										 @RequestParam(value = "dbankName") String dbankName, @RequestParam(value = "dphone") String dphone,
										 @RequestParam(value = "dbankCard") String dbankCard) throws Exception {

		log.info("全国落地B开始进入进件接口======身份证：{}============手机号：{}==========姓名：{}=========银行卡：{}=========银行名称:{}",idCard,phone,userName,bankCard,bankName);
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
		signParams.put("channelCode", "110002");// 默认渠道一（0.55%）
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
		jsonObj.put("channelCode", "110002");// 默认渠道一（0.55%）
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
					cjhklr.setUserName(userName);
					cjhklr.setExtraFee(extraFee);
					cjhklr.setStatus("1");
					topupPayChannelBusiness.createRegister(cjhklr);
					maps = (Map<String, Object>) addChannelRate(idCard, rate, "101002");
					if (!"000000".equals(maps.get("resp_code"))) {
						log.info("========================新增渠道:101002失败");
						return maps;
					} else {
						// 全国落地B:当两渠道都开通,才存费率
						CJHKLRRegister cjlr = topupPayChannelBusiness.getRegister(idCard);
						cjlr.setRate(rate);
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

	// 商户测签约短信
	@SuppressWarnings("null")
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjhk/send/sms")
	public @ResponseBody Object cjhkSendSms(@RequestParam(value = "phone") String phone,
											@RequestParam(value = "bankCard") String bankCard,
											@RequestParam(value = "userName") String userName,
											@RequestParam(value = "idCard") String idCard,
											@RequestParam(value = "expiredTime") String expiredTime,
											@RequestParam(value = "securityCode") String securityCode,
											@RequestParam(value = "channelCode") String channelCode) throws Exception {

		log.info("开始进入畅捷商户测签约短信接口===========idCard:{}=======bankCard:{}======phone：{}",idCard,bankCard,phone);

		Map<String, Object> maps = new HashMap<>();
		CJHKLRRegister cjhklr = topupPayChannelBusiness.getRegister(idCard);
		CJHKLRBindCard cjhkbind = topupPayChannelBusiness.getBindCard(bankCard);
		CJHKLRChannelCodeRelation channelCodeRelation = topupPayChannelBusiness.getCJHKLRChannelCodeRelation(bankCard);
		String merchantCode = cjhklr.getMerchantCode();

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
		log.info("======================CJHK_QUICK绑卡的请求编号:" + requestNo);
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
		log.info("CJHK_QUICK商户侧签约短信请求信息:======银行卡：{}======手机号：{}======= {}",bankCard,phone,jsonReq);
		Response response;
		try {
			response = HttpUtil.sendPost(getServerUrl + RequestURL.IS_Send_SMS, jsonReq);
			String message1 = response.message();
			String jsonRsp = response.body().string();
			log.info("CJHK_QUICK商户侧签约短信响应信息:======银行卡：{}======手机号：{}======= {}",bankCard,phone,jsonRsp);
			if (response.isSuccessful()) {
				com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(jsonRsp);
				String code = js.getString("code");
				String data = js.getString("data");
				com.alibaba.fastjson.JSONObject js1 = com.alibaba.fastjson.JSONObject.parseObject(data);
				String respMsg = js1.getString("respMsg");
				if ("000000".equals(code)) {
					if(channelCodeRelation == null){
						channelCodeRelation = new CJHKLRChannelCodeRelation();
						channelCodeRelation.setUserName(userName);
						channelCodeRelation.setPhone(phone);
						channelCodeRelation.setIdcard(idCard);
						channelCodeRelation.setBankCard(bankCard);
						channelCodeRelation.setChanelCode(channelCode);
						channelCodeRelation.setStatus("0");
						channelCodeRelation.setCreateTime(new Date());
						topupPayChannelBusiness.createCJHKLRChannelCodeRelation(channelCodeRelation);
					}else if ("0".equals(channelCodeRelation.getStatus())){
						channelCodeRelation.setChanelCode(channelCode);
						topupPayChannelBusiness.createCJHKLRChannelCodeRelation(channelCodeRelation);
						log.info("================CJHK_QUICK此卡首次签约未通过:" + bankCard);
					}else{
						channelCodeRelation.setChanelCode(channelCode);
						channelCodeRelation.setStatus("0");
						topupPayChannelBusiness.createCJHKLRChannelCodeRelation(channelCodeRelation);
						log.info("================CJHK_QUICK此卡渠道号更换，重新添加绑卡关系:" + bankCard);
					}
					if (cjhkbind == null) {
						CJHKLRBindCard cjb = new CJHKLRBindCard();
						cjb.setBankCard(bankCard);
						cjb.setUserName(userName);
						cjb.setPhone(phone);
						cjb.setIdCard(idCard);
						cjb.setStatus("0");
						cjb.setRequestNo(requestNo);
						topupPayChannelBusiness.createBindCard(cjb);
					} else if ("0".equals(cjhkbind.getStatus())) {
						cjhkbind.setRequestNo(requestNo);
						topupPayChannelBusiness.createBindCard(cjhkbind);
						log.info("================CJHK_QUICK此卡首次签约未通过:" + bankCard);
					}else {
						cjhkbind.setRequestNo(requestNo);
						cjhkbind.setStatus("0");
						topupPayChannelBusiness.createBindCard(cjhkbind);
						log.info("================CJHK_QUICK此卡渠道号更换，重新添加绑卡记录:" + bankCard);
					}
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put("requestNo", requestNo);
					return maps;
				} else if ("-400001".equals(code)) {
					maps = (Map<String, Object>) addChannelRate(idCard, cjhklr.getRate(), channelCode);
					log.info("===========银行卡：{}======手机号：{}===================新增渠道:{}结果：{}" ,bankCard,phone,channelCode,maps.toString());
					maps.put(CommonConstants.RESP_MESSAGE,"渠道费率更新成功，请重新获取验证码！");
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
			log.error("CJHK_QUICK侧签约接口出现异常======", e);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "侧签约短信失败");
			return maps;

		}

	}

	// 银行卡签约
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjhk/confirm/sms")
	public @ResponseBody Object cjhkSign(@RequestParam(value = "bankCard") String bankCard,
										 @RequestParam(value = "smsCode") String smsCode, @RequestParam(value = "userName") String userName,
										 @RequestParam(value = "idCard") String idCard, @RequestParam(value = "phone") String phone,
										 @RequestParam(value = "expiredTime", required = false) String expiredTime,
										 @RequestParam(value = "securityCode", required = false) String securityCode,
										 @RequestParam(value = "requestNo") String requestNo,
										 @RequestParam(value = "channelCode") String channelCode) throws Exception {

		log.info("开始进入畅捷商户签约短信接口===========idCard:{}=======bankCard:{}======phone：{}",idCard,bankCard,phone);

		Map<String, Object> maps = new HashMap<>();
		CJHKLRRegister cjhklr = topupPayChannelBusiness.getRegister(idCard);
		String merchantCode = cjhklr.getMerchantCode();

		CJHKLRBindCard cjhkbind = topupPayChannelBusiness.getBindCard(bankCard);
		CJHKLRChannelCodeRelation channelCodeRelation = topupPayChannelBusiness.getCJHKLRChannelCodeRelation(bankCard);
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
		log.info("CJHK_QUICK商户签约短信请求信息:======银行卡：{}======手机号：{}======= {}",bankCard,phone,jsonReq);

		Response response;
		try {
			response = HttpUtil.sendPost(getServerUrl + RequestURL.IS_Confirm_SMS, jsonReq);

			String jsonRsp = response.body().string();
			log.info("CJHK_QUICK商户签约短信请求信息:======银行卡：{}======手机号：{}======= {}",bankCard,phone,jsonRsp);
			com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(jsonRsp);
			String code = js.getString("code");
			String message = js.getString("message");
			String data = js.getString("data");
			if (response.isSuccessful()) {
				com.alibaba.fastjson.JSONObject js1 = com.alibaba.fastjson.JSONObject.parseObject(data);
				String respMsg = js1.getString("respMsg");
				if ("000000".equals(code)) {
					JSONObject json2 = JSONObject.parseObject(data);
					String getstatus = json2.getString("signStatus");
					if ("2".equals(getstatus)) {
						log.info("CJHK_QUICK商户签约成功=======================:" + merchantCode);
						channelCodeRelation.setStatus("1");
						topupPayChannelBusiness.createCJHKLRChannelCodeRelation(channelCodeRelation);
						cjhkbind.setStatus("1");
						topupPayChannelBusiness.createBindCard(cjhkbind);
						maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						maps.put("redirect_url", "http://www.shanqi111.cn/v1.0/paymentchannel/topup/wmyk/bindcardsuccess");
						return maps;
					} else {
						maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						maps.put(CommonConstants.RESP_MESSAGE, message);
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
			log.error("CJHK_QUICK银行卡签约接口出现异常======", e);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "银行卡签约失败");
			return maps;
		}
		return null;
	}

	// 交易费率新增:新增渠道号
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjhk/add/channel/rate")
	public @ResponseBody Object addChannelRate(@RequestParam(value = "idCard") String idCard,
											   @RequestParam(value = "rate") String rate, @RequestParam(value = "channelCode") String channelCode)
			throws Exception {

		log.info("开始进入交易费率新增变更渠道费率变更接口============idCard:{}========channelCode:{}=======rate:=",idCard,channelCode,rate);

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
		TreeMap<String, Object> signParams = new TreeMap<>();
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
				com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(jsonRsp);
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
			log.error("CJHK_QUICK交易费率新增渠道接口出现异常================", e);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "交易费率新增渠道失败");
			return maps;
		}
	}

	// 商户侧消费
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjhk/consume")
	public @ResponseBody Object cjhkconsume(@RequestParam(value = "orderCode") String orderCode,
											@RequestParam(value = "channelCode") String channelCode, @RequestParam(value = "cityName") String cityName,
											@RequestParam(value = "provinceName") String provinceName) throws Exception {
		log.info("开始进入畅捷侧消费接口========================orderCode:{}=======channelCode:{}=============cityName:{}=============provinceName:{}" ,orderCode,channelCode,cityName,provinceName);
		List<CJHKFactory> cjhkCityIP = this.chooseCityIp(provinceName, cityName, orderCode);
		String serviceIp = null;
		String city = null;
		if (cjhkCityIP != null && cjhkCityIP.size()>0) {
			int index = (int) (Math.random() * cjhkCityIP.size());
			serviceIp = cjhkCityIP.get(index).getStartIP();
			city = cityName;
		}else {
			serviceIp = "202.109.108.0";
			city = "上海市";
		}
		log.info("serviceIp==================:{}===================city:{}",serviceIp,city);
		Map<String, Object> maps = new HashMap<>();
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

		log.info("==================订单总金额，单位分:" + Amount);
		log.info("======================================================银行名称:" + bankName);
		String bankName1 = Util.queryBankNameByBranchName(bankName);
		log.info("===============================================utils银行名称:" + bankName1);
		BankNumCode bankNumCode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName1);
		String bankCode = bankNumCode.getBankBranchcode();
		String bankNum = bankNumCode.getBankNum();
		String bankcode = bankNumCode.getBankCode();
		String bankNum1 = bankNumCode.getBankNum();
		String BankName = Util.queryBankNameByBranchName(bankNum);
		log.info("结算卡银行缩写" + BankName);
		log.info("发送入网请求的联行号" + bankCode);
		log.info("发送入网请求的银行代号" + bankcode);
		log.info("发送入网请求的银行代码" + bankNum1);

		CJHKLRRegister cjhklrRegister = topupPayChannelBusiness.getRegister(idCard);
		String merchantCode = cjhklrRegister.getMerchantCode();

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
		signParams.put("productName", "时尚男装");
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
		jsonObj.put("productName", "时尚男装");
		jsonObj.put("productDesc", "万达广场");
		jsonObj.put("trxCtNm", city);// 消费城市
		jsonObj.put("trxSourceIp", serviceIp);// 城市服务ip
		jsonObj.put("sign", SignUtil.signByMap(key, signParams));

		// 接口访问
		String jsonReq = jsonObj.toJSONString();

		log.info("CJHK_QUICK商户侧消费请求信息:==================== " + jsonReq);
		Response response;
		String message = null;
		try {
			response = HttpUtil.sendPost(getServerUrl + RequestURL.IS_Consume, jsonReq);
			String jsonRsp = response.body().string();
			log.info("CJHK_QUICK商户侧消费响应信息:========="+ orderCode +"========== " + jsonRsp);
			if (response.isSuccessful()) {
				com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(jsonRsp);
				String data = js.getString("data");
				message = js.getString("message");
				String code = js.getString("code");
				com.alibaba.fastjson.JSONObject js1 = com.alibaba.fastjson.JSONObject.parseObject(data);
				String respMsg = null;
				if ("000000".equals(code)) {
					maps.put(CommonConstants.RESP_CODE, "999998");
					maps.put(CommonConstants.RESP_MESSAGE, "等待银行扣款中");
					return maps;
				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					if(js1 == null){
						respMsg = js1.getString("respMsg");
						maps.put(CommonConstants.RESP_MESSAGE, respMsg);
					}else{
						maps.put(CommonConstants.RESP_MESSAGE, message);
					}
					this.addOrderCauseOfFailure(orderCode, respMsg, prp.getIpAddress());
					return maps;
				}
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "请求消费失败");
				return maps;
			}
		} catch (Exception e) {
			log.error("CJHK_QUICK商户侧消费接口出现异常======", e.getMessage());
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "消费失败");
			return maps;
		}
	}

	// 快捷支付异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjhklr/fastpay/notify_call/new")
	public void hljcFastPayNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		log.info("CJHK_QUICK快捷支付异步通知进来了===================================");

		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				log.info(key + "=============" + s);
			}
		}
		String data = request.getParameter("data");
		String respCode = request.getParameter("respCode");
		String decrypt = AESUtil.decrypt(data, key);

		net.sf.json.JSONObject fromObject = net.sf.json.JSONObject.fromObject(decrypt);

		log.info("畅捷fromObject======" + fromObject);

		if ("000000".equals(respCode)) {

			String orderCode = fromObject.getString("requestNo");
			String merchantNo = fromObject.getString("merchantNo");
			String orderStatus = fromObject.getString("orderStatus");
			String amount = fromObject.getString("amount");

			log.info("交易流水号orderCode-----------" + orderCode + ",交易金额：" + amount);
			log.info("交易商户号merchantNo-----------" + merchantNo);
			log.info("交易状态orderStatus-----------" + orderStatus);

			PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
			if ("2".equalsIgnoreCase(orderStatus)) {

				log.info("*********************交易成功***********************");

				RestTemplate restTemplate = new RestTemplate();
				MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
				String url = null;
				String result = null;
				url = prp.getIpAddress() + ChannelUtils.getCallBackUrl(prp.getIpAddress());

				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("status", "1");
				requestEntity.add("order_code", orderCode);
				requestEntity.add("third_code", "");
				try {
					result = restTemplate.postForObject(url, requestEntity, String.class);
				} catch (Exception e) {
					e.printStackTrace();
					log.error("", e);
				}

				log.info("订单状态修改成功===================" + orderCode + "====================" + result);
				log.info("=================订单已交易成功!");
				PrintWriter pw = response.getWriter();
				pw.print("success");
				pw.close();
			} else {
				log.info("========================交易异常!");
				PrintWriter pw = response.getWriter();
				pw.print("failed");
				pw.close();
			}
		}
	}

	// 提现
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjhk/withdrawal")
	public @ResponseBody Object withdrawDeposit(@RequestParam(value = "orderCode") String orderCode,
												@RequestParam(defaultValue = "D0", required = false) String walletType) throws Exception {

		log.info("开始畅捷体现接口========================");

		Map<String, Object> maps = new HashMap<String, Object>();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String userName = prp.getUserName();
		String idCard = prp.getIdCard();
		String bankName = prp.getCreditCardBankName();
		String bankCard = prp.getBankCard();
		String realamount = prp.getRealAmount();
		//DO T1
		// walletType=prp.getExtra();
		String bankName1 = Util.queryBankNameByBranchName(bankName);
		BankNumCode bankNumCode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName1);
		CJHKLRRegister cjhklr = topupPayChannelBusiness.getRegister(idCard);
		String Amount = new BigDecimal(realamount).add(new BigDecimal(cjhklr.getExtraFee()))
				.multiply(new BigDecimal("100")).setScale(0).toString();
		log.info("订单总金额，单位分:" + Amount);
		String bankCode = bankNumCode.getBankBranchcode();
		log.info("银行联行号:" + bankCode);

		String merchantCode = cjhklr.getMerchantCode();

		// 获取令牌
		BaseResMessage<TokenRes> tokenRes = new GetSpToken().token(key, spCode);
		String token = tokenRes.getData().getToken();
		// 解密令牌
		String tokenClearText = EncryptUtil.desDecrypt(token, key);

		// 敏感数据3DES加密
		String bankAccountNoCipher = EncryptUtil.desEncrypt(bankCard, key);

		int i = 400;
		if ("D0".equals(walletType)) {
			i = 400;
		} else if ("T1".equals(walletType)) {
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
		signParams.put("bankName", bankName1);
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
		jsonObj.put("bankName", bankName1);
		jsonObj.put("bankSubName", "上海宝山支行");
		jsonObj.put("bankChannelNo", bankCode);
		jsonObj.put("sign", SignUtil.signByMap(key, signParams));

		// 接口访问
		String jsonReq = jsonObj.toJSONString();
		log.info("CJHK_QUICK提现请求信息========================" + jsonReq);
		Response response;
		try {
			response = HttpUtil.sendPost(getServerUrl + RequestURL.IS_Withdraw, jsonReq);
			String jsonRsp = response.body().string();
			log.info("CJHK_QUICK提现返回信息========================" + jsonRsp);
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
			log.error("CJHK_QUICK提现接口出现异常======", e);
			maps.put(CommonConstants.RESP_CODE, "999998");
			maps.put(CommonConstants.RESP_MESSAGE, "请求异常，等待查询");
			return maps;
		}
	}

	// 畅捷补单接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjhk/replenishmentOrder")
	public @ResponseBody Object replenishmentOrder(@RequestParam(value = "orderCode") String orderCode,
												   @RequestParam(value = "walletType", defaultValue = "D0", required = false) String walletType,
												   @RequestParam(value = "replenishmentAmount", required = false) String replenishmentAmount,
												   @RequestParam(value = "replenishmentOrderNo") String replenishmentOrderNo) throws Exception {

		log.info("开始畅捷体现补单接口========================");

		Map<String, Object> maps = new HashMap<String, Object>();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String userName = prp.getUserName();
		String idCard = prp.getIdCard();
		String bankName = prp.getCreditCardBankName();
		String bankCard = prp.getBankCard();
		String realamount = prp.getRealAmount();
		String bankName1 = Util.queryBankNameByBranchName(bankName);
		BankNumCode bankNumCode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName1);
		CJHKLRRegister cjhklr = topupPayChannelBusiness.getRegister(idCard);
		String Amount = new BigDecimal(realamount).add(new BigDecimal(cjhklr.getExtraFee()))
				.multiply(new BigDecimal("100")).setScale(0).toString();
		log.info("订单总金额，单位分:" + Amount);
		String bankCode = bankNumCode.getBankBranchcode();
		log.info("银行联行号:" + bankCode);
		log.info("========================================补单金额：" + replenishmentAmount);
		if (!"".equals(replenishmentAmount) || replenishmentAmount == null) {
			Amount = replenishmentAmount;
		}
		String merchantCode = cjhklr.getMerchantCode();

		// 获取令牌
		BaseResMessage<TokenRes> tokenRes = new GetSpToken().token(key, spCode);
		String token = tokenRes.getData().getToken();
		// 解密令牌
		String tokenClearText = EncryptUtil.desDecrypt(token, key);

		// 敏感数据3DES加密
		String bankAccountNoCipher = EncryptUtil.desEncrypt(bankCard, key);

		int i = 400;
		if ("D0".equals(walletType)) {
			i = 400;
		} else if ("T1".equals(walletType)) {
			i = 402;
		}
		// 构建签名参数
		TreeMap<String, Object> signParams = new TreeMap<String, Object>();
		signParams.put("token", tokenClearText);
		signParams.put("spCode", spCode);
		signParams.put("reqFlowNo", replenishmentOrderNo);
		signParams.put("merchantCode", merchantCode);
		signParams.put("walletType", i);
		signParams.put("amount", Amount);
		signParams.put("bankAccountName", userName);
		signParams.put("bankAccountNo", bankCard);
		signParams.put("bankName", bankName1);
		signParams.put("bankSubName", "上海宝山支行");
		signParams.put("bankChannelNo", bankCode);

		// 构建请求参数
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("token", tokenClearText);
		jsonObj.put("spCode", spCode);
		jsonObj.put("reqFlowNo", replenishmentOrderNo);
		jsonObj.put("merchantCode", merchantCode);
		jsonObj.put("walletType", i);
		jsonObj.put("amount", Amount);
		jsonObj.put("bankAccountName", userName);
		jsonObj.put("bankAccountNo", bankAccountNoCipher);
		jsonObj.put("bankName", bankName1);
		jsonObj.put("bankSubName", "上海宝山支行");
		jsonObj.put("bankChannelNo", bankCode);
		jsonObj.put("sign", SignUtil.signByMap(key, signParams));

		// 接口访问
		String jsonReq = jsonObj.toJSONString();
		log.info("CJHK_QUICK提现请求信息========================" + jsonReq);
		Response response;
		try {
			response = HttpUtil.sendPost(getServerUrl + RequestURL.IS_Withdraw, jsonReq);
			String jsonRsp = response.body().string();
			log.info("CJHK_QUICK提现返回信息========================" + jsonRsp);
			if (response.isSuccessful()) {
				com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(jsonRsp);
				String code = js.getString("code");
				String message = js.getString("message");
				if ("000000".equals(code)) {
					RestTemplate restTemplate = new RestTemplate();
					MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
					String URL = prp.getIpAddress() + "/v1.0/transactionclear/payment/update/thirdordercode";
					requestEntity = new LinkedMultiValueMap<String, String>();
					requestEntity.add("order_code", orderCode);
					requestEntity.add("third_code", replenishmentOrderNo);
					try {
						String results = restTemplate.postForObject(URL, requestEntity, String.class);
						log.info("*********************下单成功，添加第三方流水号***********************");
					} catch (Exception e) {
						e.printStackTrace();
						log.error("", e);
					}
					log.info("添加第三方流水号成功：===================" + orderCode + "===================="
							+ replenishmentOrderNo);
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
			log.error("CJHK_QUICK提现接口出现异常======", e);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "提现请求异常，代码500");
			return maps;
		}
	}

	// 消费状态查询
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjhk/consumeQuery")
	public @ResponseBody Object cjhkconsumeQuery(@RequestParam(value = "orderCode") String orderCode) throws Exception {
		log.info("开始进入消费状态查询接口========================");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		if (prp == null) {
			return ResultWrap.init(CommonConstants.FALIED, "请求查询订单不存在");
		}
		String idCard = prp.getIdCard();
		Map<String, Object> maps = new HashMap<String, Object>();
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

		log.info("===============CJHK_QUICK消费状态查询请求信息: " + jsonReq);

		Response response = HttpUtil.sendPost(getServerUrl + RequestURL.IS_Quick_Consume, jsonReq);
		String message1 = response.message();
		String jsonRsp = response.body().string();
		log.info("==============CJHK_QUICK消费状态查询响应信息: " + jsonRsp);
		if (response.isSuccessful()) {

			JSONObject js = JSONObject.parseObject(jsonRsp);
			String code = js.getString("code");
			String data = js.getString("data");
			JSONObject js1 = JSONObject.parseObject(data);

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
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjhk/modify/channel/rate")
	public @ResponseBody Object cjhkChangeRate(@RequestParam(value = "idCard") String idCard,
											   @RequestParam(value = "rate") String rate, @RequestParam(value = "channelCode") String channelCode,
											   @RequestParam(value = "extraFee") String extraFee) throws Exception {

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
		signParams.put("changeType", 1);
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
		jsonObj.put("changeType", 1);
		jsonObj.put("debitRate", rate);
		jsonObj.put("debitCapAmount", "99999900");
		jsonObj.put("creditRate", rate);
		jsonObj.put("creditCapAmount", "99999900");
		jsonObj.put("withdrawDepositRate", "0");
		jsonObj.put("withdrawDepositSingleFee", extra);
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
					cjhklr.setRate(rate);
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

	public List<CJHKFactory> chooseCityIp(String provinceName, String cityName, String orderCode) {
		List<CJHKFactory> cjhkCityIP;
		if(provinceName==null ||provinceName==""|| cityName==null|| cityName=="") {

			return null;
		}
		if (provinceName.equals(cityName)) {
			cjhkCityIP = topupPayChannelBusiness.getCJHKChooseCityIPBycityName(cityName);
			log.info("=======" + orderCode + "=======省：" + provinceName + "===========市：" + cityName);
		} else {
			cjhkCityIP = topupPayChannelBusiness.getCJHKChooseCityIPBycityName(provinceName + cityName + "");
			log.info("=======" + orderCode + "=======省：" + provinceName + "===========市：" + cityName);
		}
		return cjhkCityIP;
	}

	// 商户钱包查询
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjhk/walletQuery")
	public @ResponseBody Object cjhkwalletQuery(HttpServletRequest request,
												@RequestParam(value = "idCard") String idCard) throws Exception {

		log.info("开始体现接口========================");

		Map<String, Object> maps = new HashMap<String, Object>();

		CJHKLRRegister cjhkRegister = topupPayChannelBusiness.getRegister(idCard);
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
		log.info("商户钱包查询请求信息: " + jsonReq);

		Response response = HttpUtil.sendPost(getServerUrl + "/v2/wallet/walletQuery", jsonReq);
		String jsonRsp = response.body().string();
		log.info("商户钱包查询接口响应信息,金额为分: " + jsonRsp);

		if (response.isSuccessful()) {
			com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(jsonRsp);
			String code = js.getString("code");
			String message = js.getString("message");
			String data = js.getString("data");
			if ("000000".equals(code)) {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "金额为分:" + data);
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

	// 体现结果查询
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cjhk/withdrawQuery")
	public @ResponseBody Object cjhkwithdrawQuery(@RequestParam(value = "orderCode") String orderCode)
			throws Exception {

		log.info("开始查询体现状态接口========================");

		Map<String, Object> maps = new HashMap<String, Object>();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		if (prp == null) {
			return ResultWrap.init(CommonConstants.FALIED, "请求支付失败");
		}
		String idCard = prp.getIdCard();

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

		log.info("提现查询请求信息:============== " + jsonReq);
		Response response;
		try {
			response = HttpUtil.sendPost(getServerUrl + "/v2/trans/withdrawQuery", jsonReq);
			String jsonRsp = response.body().string();
			log.info("提现结果查询响应信息:================== " + jsonRsp);

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
						return maps;
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
			log.error("提现结果查询出现异常======", e);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "提现结果查询失败");
			return maps;
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/tocjquick/bindcard/lr")
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
		return "cjhklrbindcard";
	}

	// 签约状态查询
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/tocjquick/querybindcard/lr")
	public @ResponseBody Object cjBindCardQuery(@RequestParam("idCard") String idCard,
												@RequestParam("requestNo") String requestNo) throws Exception {

		log.info("开始进入签约状态查询接口========================");

		Map<String, Object> maps = new HashMap<String, Object>();
		CJHKLRRegister cjhkRegister = topupPayChannelBusiness.getRegister(idCard);
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
		log.info(sdf.format(new Date()) + "请求信息: " + jsonReq);

		Response response;
		try {
			response = HttpUtil.sendPost(getServerUrl + "/v2/sign/signQuery", jsonReq);

			String message = null;
			String signStatus;
			String respMsg;
			String jsonRsp = response.body().string();
			log.info("响应信息:======================== " + jsonRsp);
			com.alibaba.fastjson.JSONObject js = com.alibaba.fastjson.JSONObject.parseObject(jsonRsp);
			String data = js.getString("data");
			String code = js.getString("code");
			message = js.getString("message");
			if (response.isSuccessful()) {
				com.alibaba.fastjson.JSONObject js1 = com.alibaba.fastjson.JSONObject.parseObject(data);
				signStatus = js1.getString("signStatus");
				respMsg = js1.getString("respMsg");
				if ("000000".equals(code)) {
					log.info("================签约状态" + signStatus);
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
			log.error("签约状态查询接口出现异常", e);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "卡签约失败");
			return maps;
		}

	}


}