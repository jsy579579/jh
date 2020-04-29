package com.jh.paymentgateway.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
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

import com.alibaba.fastjson.JSON;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.BankNumCode;
import com.jh.paymentgateway.pojo.CJBindCard;
import com.jh.paymentgateway.pojo.CJRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.Util;
import com.jh.paymentgateway.util.cjhk.AESUtil;
import com.jh.paymentgateway.util.cjhk.ApiClient;
import com.jh.paymentgateway.util.cjhk.ApiRequest;
import com.jh.paymentgateway.util.cjhk.ApiResponse;
import com.jh.paymentgateway.util.cjhk.EncryptTypeEnum;
import com.jh.paymentgateway.util.jf.AES;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class CJpageRequest extends BaseChannel {
	@Autowired
	RedisUtil redisUtil;

	@Value("${payment.ipAddress}")
	private String ip;

	@Autowired
	private Util util;
	
	private String realnamePic = "/usr/share/nginx/photo";

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	private static final Logger LOG = LoggerFactory.getLogger(CJpageRequest.class);
	public static final String baseurl = "http://120.55.52.144:8899";
	public static final String merchantNo = "121804261562";
	public static final String key = "914a8687201f4134a426faa603c6e06b";
	private static final String channelProductCode = "KJTPAY_01";

	/**
	 * 进件
	 * @param orderCode
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cj/register")
	public @ResponseBody Object   cjhkRegister(@RequestParam(value = "orderCode") String orderCode) throws Exception {

		LOG.info("CJ快捷支付开始-------------------------");
		LOG.info("判断进入接口-------------------------");

		Map<String, Object> maps = new HashMap<String, Object>();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String userName = prp.getUserName();
		String phoneD = prp.getDebitPhone();
		String phoneC = prp.getCreditCardPhone();// 信用卡手机号
		String cardName = prp.getCreditCardBankName();// 信用卡名称
		String securityCode = prp.getSecurityCode();
		String expiredTime = prp.getExpiredTime();
		String idCard = prp.getIdCard();
		String bankName = prp.getDebitBankName();//收款卡银行名
		String bankNo = prp.getDebitCardNo();// 收款卡
		String bankCard = prp.getBankCard();// 支付卡
		String rate = prp.getRate();
		String extraFee = prp.getExtraFee();
		String userId = prp.getUserId();
		String rip = prp.getIpAddress();

		// 获取银行 卡信息
		BankNumCode bCode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
		String code = bCode.getBankCode();
		String inBankUnitNo = bCode.getBankBranchcode();

		CJRegister cJRegister = topupPayChannelBusiness.getCJRegisterByIdCard(idCard);
		String cjRate = "";
		if (cJRegister != null) {
			cjRate = cJRegister.getRate();
		}
		CJBindCard cJBindCard = topupPayChannelBusiness.getCJBindCardByBankCard(bankCard);

		if (cJRegister == null) {
			LOG.info("进入进件接口-------------------------");

			
			String img1 = "http://apiserver.qiniudn.com/jianshe.png";
			String img2 = "http://apiserver.qiniudn.com/pingan.png";
			String img3 = "http://apiserver.qiniudn.com/zhaoshang.png";	
			String img4 = "http://apiserver.qiniudn.com/xingye.png";	
			
			
			String url = baseurl + "/rest/v1.0/paybar/registMerchant";
			ApiRequest apiRequest = new ApiRequest(merchantNo, key);
			apiRequest.setSupportSign(false);
			apiRequest.setEncryptType(EncryptTypeEnum.AES);
			apiRequest.addParam("requestNo", String.valueOf(System.currentTimeMillis()));
			apiRequest.addParam("merchantName ", "上海莘丽网络");
			apiRequest.addParam("shortName", "莘丽");
			apiRequest.addParam("channelProductCode", channelProductCode);
			apiRequest.addParam("bindMobile", phoneD);
			apiRequest.addParam("bindEmail", "q355023989@qq.com");
			apiRequest.addParam("address", "上海宝山区逸仙路2816号");
			apiRequest.addParam("idCardNo", idCard);
			apiRequest.addParam("settleBankAccountNo", bankNo);
			apiRequest.addParam("settleBankAccountName", userName);
			apiRequest.addParam("settleBankAccountType", "PRIVATE");
			apiRequest.addParam("settleBankName", bankName);
			apiRequest.addParam("settleBankSubName", "上海宝山支行");
			apiRequest.addParam("settleBankAbbr", code);
			apiRequest.addParam("settleBankChannelNo", inBankUnitNo);
			apiRequest.addParam("settleBankCardProvince", "上海");
			apiRequest.addParam("settleBankCardCity", "上海");
			apiRequest.addParam("settlementType", "AUTO_SETTLE");
			apiRequest.addParam("debitRate", rate);
			apiRequest.addParam("creditRate", rate);
			apiRequest.addParam("debitCapAmount", "999999");
			apiRequest.addParam("creditCapAmount", "999999");
			apiRequest.addParam("withdrawDepositRate", "0.0");
			apiRequest.addParam("withdrawDepositSingleFee", extraFee);
			// 以http开头，传照片地址，当产品编码为YBPAY_01和KJTPAY_01时必填
			apiRequest.addParam("idCardPhoto", img1);
			apiRequest.addParam("idCardBackPhoto", img2);
			apiRequest.addParam("bankCardPhoto", img3);
			apiRequest.addParam("personPhoto", img4);

			LOG.info("进件请求参数---apiRequest:" + apiRequest.toString());
			ApiResponse post = ApiClient.post(url, apiRequest);
			LOG.info("进件返回参数---post:" + JSON.toJSONString(post));
			JSONObject fromObject2 = JSONObject.fromObject(post);
			String state = fromObject2.getString("state");
			LOG.info("state======" + state);
			JSONObject resultMap = fromObject2.getJSONObject("resultMap");

			JSONObject fromObject3 = JSONObject.fromObject(resultMap);
			LOG.info("fromObject3======" + fromObject3);
			String bizCode = fromObject3.getString("bizCode");
			String bizMsg = fromObject3.getString("bizMsg");
			String code1 = fromObject3.getString("code");
			if ("200".equals(code1) && "1".equals(bizCode) && "SUCCESS".equals(state)) {
				String encryptKey = fromObject3.getString("encryptKey");
				String merchantCode = fromObject3.getString("merchantNo");

				LOG.info("进件返回成功------------------");

				CJRegister cjRegister = new CJRegister();
				cjRegister.setPhone(phoneD);
				cjRegister.setIdCard(idCard);
				cjRegister.setUserName(userName);
				cjRegister.setBankCard(bankNo);
				cjRegister.setMerchantCode(merchantCode);
				cjRegister.setEncryptKey(encryptKey);
				cjRegister.setRate(rate);
				cjRegister.setExtraFee(extraFee);

				topupPayChannelBusiness.createCJRegister(cjRegister);
				
				maps.put(CommonConstants.RESULT,
						ip + "/v1.0/paymentgateway/topup/cj/toConfirmBindCard?bankCard=" + bankCard + "&userName="
								+ userName + "&orderCode=" + orderCode + "&phoneC=" + phoneC + "&idCard=" + idCard
								+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&securityCode=" + securityCode + "&expiredTime=" + expiredTime
								+ "&ipAddress=" + ip);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "跳转绑卡页面");
				return maps;
			} else {
				LOG.info("进件返回异常------------------" + bizMsg);

				this.addOrderCauseOfFailure(orderCode, bizMsg, rip);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;
			}
		}else if(!rate.equals(cjRate) && !bankNo.equals(cJRegister.getBankCard())){//费率不同，结算卡也不同
			LOG.info("修改费率+结算卡---------------------");
			maps = (Map<String, Object>) changeRate(rate, orderCode, idCard);
			String respMsg = (String) maps.get("resp_message");
			LOG.info(respMsg);
			if ("000000".equals(maps.get("resp_code"))) {
				LOG.info("修改费率成功-->>修改结算卡---------------------");
				
				maps = (Map<String, Object>) modifyMerchantSettlementInfo(orderCode, idCard, phoneD, bankNo, userName, bankName);
				String respMsgs = (String) maps.get("resp_message");
				LOG.info(respMsgs);
				if ("000000".equals(maps.get("resp_code"))) {
					LOG.info("修改费率成功,修改结算卡成功---------------------");
					
					if (cJBindCard == null) {
						LOG.info("修改费率+结算卡成功，进入绑卡页面---------------------");
						//
						maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						maps.put(CommonConstants.RESULT,
								ip + "/v1.0/paymentgateway/topup/cj/toConfirmBindCard?bankCard=" + bankCard + "&userName="
										+ userName + "&orderCode=" + orderCode + "&phoneC=" + phoneC + "&idCard=" + idCard
										+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&securityCode=" + securityCode + "&expiredTime=" + expiredTime
										+ "&ipAddress=" + ip);
						maps.put(CommonConstants.RESP_MESSAGE, "跳转绑卡页面");
						return maps;
					}else if(cJBindCard != null && !"SUCCESS".equals(cJBindCard.getStatus())){	
						LOG.info("修改费率+结算卡成功，以前申请过绑卡，状态不是成功---------------------");

						maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						maps.put(CommonConstants.RESULT,
								ip + "/v1.0/paymentgateway/topup/cj/toConfirmBindCard?bankCard=" + bankCard + "&userName="
										+ userName + "&orderCode=" + orderCode + "&phoneC=" + phoneC + "&idCard=" + idCard
										+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&securityCode=" + securityCode + "&expiredTime=" + expiredTime
										+ "&ipAddress=" + ip);
						maps.put(CommonConstants.RESP_MESSAGE, "跳转绑卡页面");
						return maps;
					}else{
						LOG.info("修改费率+结算卡成功，已绑卡，直接交易---------------------");

						maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						maps.put(CommonConstants.RESULT, ip + "/v1.0/paymentgateway/topup/tocj/pay?orderCode=" + orderCode
								+ "&bankCard=" + bankCard + "&bankName=" + URLEncoder.encode(bankName, "UTF-8") + "&ipAddress=" + ip);
						maps.put(CommonConstants.RESP_MESSAGE, "跳转交易页面");
						return maps;
					}
				}else{
					
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, "修改结算卡异常");
					return maps;
				}
			}else{
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "修改费率异常");
				return maps;
			}
		} else if (!rate.equals(cjRate)) {
			LOG.info("只修改费率---------------------");
   
			maps = (Map<String, Object>) changeRate(rate, orderCode, idCard);
			String respMsg = (String) maps.get("resp_message");
			LOG.info(respMsg);
			if ("000000".equals(maps.get("resp_code"))) {
				LOG.info("修改费率成功---------------------");
				
				if (cJBindCard == null) {
					LOG.info("修改费率成功，进入绑卡页面---------------------");
					//
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESULT,
							ip + "/v1.0/paymentgateway/topup/cj/toConfirmBindCard?bankCard=" + bankCard + "&userName="
									+ userName + "&orderCode=" + orderCode + "&phoneC=" + phoneC + "&idCard=" + idCard
									+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&securityCode=" + securityCode + "&expiredTime=" + expiredTime
									+ "&ipAddress=" + ip);
					maps.put(CommonConstants.RESP_MESSAGE, "跳转绑卡页面");
					return maps;
				}else if(cJBindCard != null && !"SUCCESS".equals(cJBindCard.getStatus())){	
					LOG.info("修改费率成功，以前申请过绑卡，状态不是成功---------------------");

					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESULT,
							ip + "/v1.0/paymentgateway/topup/cj/toConfirmBindCard?bankCard=" + bankCard + "&userName="
									+ userName + "&orderCode=" + orderCode + "&phoneC=" + phoneC + "&idCard=" + idCard
									+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&securityCode=" + securityCode + "&expiredTime=" + expiredTime
									+ "&ipAddress=" + ip);
					maps.put(CommonConstants.RESP_MESSAGE, "跳转绑卡页面");
					return maps;
				}else{
					LOG.info("修改费率成功，已绑卡，直接交易---------------------");

					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESULT, ip + "/v1.0/paymentgateway/topup/tocj/pay?orderCode=" + orderCode
							+ "&bankCard=" + bankCard + "&bankName=" + URLEncoder.encode(bankName, "UTF-8") + "&ipAddress=" + ip);
					maps.put(CommonConstants.RESP_MESSAGE, "跳转交易页面");
					return maps;
				}
			}else{
				LOG.info("修改费率失败---------------------");
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "修改费率异常");
				return maps;
			}
			
		} else if (!bankNo.equals(cJRegister.getBankCard())) {
			LOG.info("只修改结算卡---------------------");
                
			maps = (Map<String, Object>) modifyMerchantSettlementInfo(orderCode, idCard, phoneD, bankNo, userName, bankName);
			String respMsg = (String) maps.get("resp_message");
			LOG.info(respMsg);
			if ("000000".equals(maps.get("resp_code"))) {
				LOG.info("修改结算卡成功---------------------");
				
				if (cJBindCard == null) {
					LOG.info("修改结算卡成功，进入绑卡页面---------------------");
					//
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESULT,
							ip + "/v1.0/paymentgateway/topup/cj/toConfirmBindCard?bankCard=" + bankCard + "&userName="
									+ userName + "&orderCode=" + orderCode + "&phoneC=" + phoneC + "&idCard=" + idCard
									+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&securityCode=" + securityCode + "&expiredTime=" + expiredTime
									+ "&ipAddress=" + ip);
					maps.put(CommonConstants.RESP_MESSAGE, "跳转绑卡页面");
					return maps;
				}else if(cJBindCard != null && !"SUCCESS".equals(cJBindCard.getStatus())){	
					LOG.info("修改结算卡成功，以前申请过绑卡，状态不是成功---------------------");

					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESULT,
							ip + "/v1.0/paymentgateway/topup/cj/toConfirmBindCard?bankCard=" + bankCard + "&userName="
									+ userName + "&orderCode=" + orderCode + "&phoneC=" + phoneC + "&idCard=" + idCard
									+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&securityCode=" + securityCode + "&expiredTime=" + expiredTime
									+ "&ipAddress=" + ip);
					maps.put(CommonConstants.RESP_MESSAGE, "跳转绑卡页面");
					return maps;
				}else{
					LOG.info("修改结算卡成功，已绑卡，直接交易---------------------");

					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESULT, ip + "/v1.0/paymentgateway/topup/tocj/pay?orderCode=" + orderCode
							+ "&bankCard=" + bankCard + "&bankName=" + URLEncoder.encode(bankName, "UTF-8") + "&ipAddress=" + ip);
					maps.put(CommonConstants.RESP_MESSAGE, "跳转交易页面");
					return maps;
				}
			}else{
				LOG.info("修改结算卡失败---------------------");
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);
				return maps;
			}
			
		} else if (cJBindCard != null && !"SUCCESS".equals(cJBindCard.getStatus())) {// 以前申请过绑卡，状态不是成功
			LOG.info("以前申请过绑卡，状态不是成功---------------------");
			LOG.info("再次进入绑卡页面---------------------");

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESULT,
					ip + "/v1.0/paymentgateway/topup/cj/toConfirmBindCard?bankCard=" + bankCard + "&userName="
							+ userName + "&orderCode=" + orderCode + "&phoneC=" + phoneC + "&idCard=" + idCard
							+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&securityCode=" + securityCode + "&expiredTime=" + expiredTime
							+ "&ipAddress=" + ip);
			maps.put(CommonConstants.RESP_MESSAGE, "跳转绑卡页面");
			return maps;
		} else if (cJBindCard == null) {
			LOG.info("进入绑卡页面---------------------");
			//
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESULT,
					ip + "/v1.0/paymentgateway/topup/cj/toConfirmBindCard?bankCard=" + bankCard + "&userName="
							+ userName + "&orderCode=" + orderCode + "&phoneC=" + phoneC + "&idCard=" + idCard
							+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&securityCode=" + securityCode + "&expiredTime=" + expiredTime
							+ "&ipAddress=" + ip);
			maps.put(CommonConstants.RESP_MESSAGE, "跳转绑卡页面");
			return maps;
		}  else {
			LOG.info("直接交易---------------------");

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESULT, ip + "/v1.0/paymentgateway/topup/tocj/pay?orderCode=" + orderCode
					+ "&bankCard=" + bankCard + "&bankName=" + URLEncoder.encode(bankName, "UTF-8") + "&ipAddress=" + ip);
			maps.put(CommonConstants.RESP_MESSAGE, "跳转交易页面");
			return maps;
		}

	}

	/**
	 * 开通产品的接口
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cj/product")
	public @ResponseBody Object cjhkProduct(@RequestParam(value = "merchantCode") String merchantCode,
			@RequestParam(value = "rate") String rate, @RequestParam(value = "orderCode") String orderCode)
					throws Exception {
		LOG.info("开始进入开通产品接口========================");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String rip = prp.getIpAddress();

		String url = baseurl + "/rest/v1.0/paybar/registMerchantProduct";
		Map<String, Object> maps = new HashMap<String, Object>();
		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("merchantNo", merchantCode);
		apiRequest.addParam("channelProductCode", channelProductCode);
		apiRequest.addParam("debitRate", rate);
		apiRequest.addParam("debitCapAmount", "999999");
		apiRequest.addParam("creditRate", rate);
		apiRequest.addParam("creditCapAmount", "999999");

		LOG.info("开通产品请求参数---apiRequest:" + apiRequest.toString());
		ApiResponse post = ApiClient.post(url, apiRequest);
		LOG.info("开通产品返回参数---post:" + JSON.toJSONString(post));

		JSONObject fromObject2 = JSONObject.fromObject(post);
		String state = fromObject2.getString("state");
		LOG.info("status======" + state);
		JSONObject resultMap = fromObject2.getJSONObject("resultMap");

		JSONObject fromObject3 = JSONObject.fromObject(resultMap);
		LOG.info("fromObject3======" + fromObject3);
		String bizCode = fromObject3.getString("bizCode");
		String bizMsg = fromObject3.getString("bizMsg");
		String code1 = fromObject3.getString("code");
		if ("200".equals(code1) && "1".equals(bizCode) && "SUCCESS".equals(state)) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
			return maps;
		} else {

			this.addOrderCauseOfFailure(orderCode, bizMsg, rip);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
			return maps;
		}

	}

	/**
	 * 修改费率
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cj/changeRate")
	public @ResponseBody Object changeRate(@RequestParam(value = "rate") String rate,
			@RequestParam(value = "orderCode") String orderCode, @RequestParam(value = "idCard") String idCard)
					throws Exception {
		LOG.info("开始进入修改费率接口-------------");
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String rip = prp.getIpAddress();

		CJRegister cJRegister = topupPayChannelBusiness.getCJRegisterByIdCard(idCard);
		String MerchantCode = cJRegister.getMerchantCode();

		String url = baseurl + "/rest/v1.0/paybar/modifyMerchantFeeInfo";
		Map<String, Object> maps = new HashMap<String, Object>();

		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("merchantNo", MerchantCode);
		apiRequest.addParam("channelProductCode", channelProductCode);
		apiRequest.addParam("cardType", "CREDIT");// cardType:借记卡-DEBIT
													// 贷记卡-CREDIT
		apiRequest.addParam("bizType", "TRADE");// bizType:交易-TRADE
												// 提现费率-WITHDRAW_RATE
												// 单笔提现费-WITHDRAW_SIGLE
		apiRequest.addParam("feeValue", rate);// feeValue:费率，精度为小数位后4位
												// 注：当修改提现手续费时精度为小数位后2位，不能大于等于0.05
		apiRequest.addParam("capAmount", "");// capAmount:封顶金额，精度为小数位后2位,不填默认不修改

		LOG.info("修改费率参数---apiRequest:" + apiRequest.toString());
		ApiResponse post = ApiClient.post(url, apiRequest);
		LOG.info("修改费率返回参数---post:" + JSON.toJSONString(post));

		JSONObject fromObject2 = JSONObject.fromObject(post);

		String state = fromObject2.getString("state");
		LOG.info("state-----" + state);

		JSONObject resultMap = fromObject2.getJSONObject("resultMap");

		JSONObject fromObject3 = JSONObject.fromObject(resultMap);
		LOG.info("fromObject3-----" + fromObject3);

		String bizCode = fromObject3.getString("bizCode");
		String bizMsg = fromObject3.getString("bizMsg");
		String code = fromObject3.getString("code");

		if ("200".equals(code) && "1".equals(bizCode) && "SUCCESS".equals(state)) {
			LOG.info("修改费率成功-----" + bizMsg);

			cJRegister.setRate(rate);
			topupPayChannelBusiness.createCJRegister(cJRegister);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
			return maps;

		} else {
			LOG.info("修改费率失败-----" + bizMsg);

			this.addOrderCauseOfFailure(orderCode, bizMsg, rip);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
			return maps;
		}

	}

	/**
	 * 商户查询结算卡信息
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cj/bqueryMerchantFeeInfoindcard")
	public @ResponseBody Object queryMerchantFeeInfo(
			@RequestParam(value = "MerchantCode") String MerchantCode) throws Exception {
		LOG.info("查询结算卡开始----------------------");
		
		String url = baseurl + "/rest/v1.0/paybar/queryMerchantFeeInfo";
		Map<String, Object> maps = new HashMap<String, Object>();

		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("merchantNo", MerchantCode);

		LOG.info("查询结算卡发送参数---apiRequest:" + apiRequest.toString());
		ApiResponse post = ApiClient.post(url, apiRequest);
		LOG.info("查询结算卡返回参数---post:" + JSON.toJSONString(post));

		JSONObject fromObject2 = JSONObject.fromObject(post);

		String state = fromObject2.getString("state");
		LOG.info("state-----" + state);

		JSONObject resultMap = fromObject2.getJSONObject("resultMap");

		JSONObject fromObject3 = JSONObject.fromObject(resultMap);
		LOG.info("fromObject3-----" + fromObject3);

		String bizCode = fromObject3.getString("bizCode");
		String bizMsg = fromObject3.getString("bizMsg");
		String code = fromObject3.getString("code");

		if ("200".equals(code) && "1".equals(bizCode) && "SUCCESS".equals(state)) {
			LOG.info("查询结算卡成功-----" + bizMsg);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
			return maps;
		} else {
			LOG.info("查询结算卡失败-----" + bizMsg);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
			return maps;
		}

	}

	/**
	 * 修改结算卡
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cj/modifyMerchantSettlementInfo")
	public @ResponseBody Object modifyMerchantSettlementInfo(
			@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "bindMobile") String bindMobile,
			@RequestParam(value = "settleBankAccountNo") String settleBankAccountNo,
			@RequestParam(value = "settleBankAccountName") String settleBankAccountName,
			@RequestParam(value = "settleBankName") String settleBankName) throws Exception {

		LOG.info("开始进入修改结算卡接口======");
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String rip = prp.getIpAddress();

		String url = baseurl + "/rest/v1.0/paybar/modifyMerchantSettlementInfo";
		Map<String, Object> maps = new HashMap<String, Object>();

		CJRegister cJRegister = topupPayChannelBusiness.getCJRegisterByIdCard(idCard);
		String MerchantCode = cJRegister.getMerchantCode();

		// 获取银行 卡信息
		BankNumCode bCode = topupPayChannelBusiness.getBankNumCodeByBankName(settleBankName);
		String code = bCode.getBankCode();
		String inBankUnitNo = bCode.getBankBranchcode();

		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("merchantNo", MerchantCode);
		apiRequest.addParam("bindMobile", bindMobile);
		apiRequest.addParam("settleBankAccountNo", settleBankAccountNo);// 结算卡号
		apiRequest.addParam("settleBankAccountName", settleBankAccountName);// 结算卡开户名
		apiRequest.addParam("settleBankName", settleBankName);// 银行名称
		apiRequest.addParam("settleBankSubName", "宝山支行");// 支行名称
		apiRequest.addParam("settleBankAbbr", code);// 银行缩写
		apiRequest.addParam("settleBankChannelNo", inBankUnitNo);// 银联号
		apiRequest.addParam("settleBankCardProvince", "上海");// 开户省
		apiRequest.addParam("settleBankCardCity", "上海市");// 开户市
		// photocopy_portrait_address   photocopy_emblem_address  photocopy_handheld_face_addr

		LOG.info("修改结算卡请求参数---apiRequest:" + apiRequest.toString());
		ApiResponse post = ApiClient.post(url, apiRequest);
		LOG.info("修改结算卡返回参数---post:" + JSON.toJSONString(post));

		JSONObject fromObject2 = JSONObject.fromObject(post);

		String state = fromObject2.getString("state");
		LOG.info("state-----" + state);

		JSONObject resultMap = fromObject2.getJSONObject("resultMap");

		JSONObject fromObject3 = JSONObject.fromObject(resultMap);
		LOG.info("fromObject3======" + fromObject3);
		String bizCode = fromObject3.getString("bizCode");
		String bizMsg = fromObject3.getString("bizMsg");
		String code1 = fromObject3.getString("code");

		if ("200".equals(code1) && "1".equals(bizCode) && "SUCCESS".equals(state)) {
			LOG.info("修改结算卡成功--------------");

			cJRegister.setBankCard(settleBankAccountNo);
			topupPayChannelBusiness.createCJRegister(cJRegister);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
			return maps;
		} else {
			LOG.info("修改结算卡失败--------------");

			this.addOrderCauseOfFailure(orderCode, bizMsg, rip);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
			return maps;
		}
	}

	/**
	 * 银联绑卡接口
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cj/bindcard")
	public @ResponseBody Object cjhkBindCard(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "userName") String userName,
			@RequestParam(value = "phone") String phoneC, @RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "bankName") String cardName,
			@RequestParam(value = "securityCode") String securityCode,
			@RequestParam(value = "expiredTime") String expiredTime) throws Exception {
		LOG.info("开始进入绑卡接口======");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String rip = prp.getIpAddress();

		String url = baseurl + "/rest/v1.0/paybar/bindCard";
		Map<String, Object> maps = new HashMap<String, Object>();
		//
		String expired = this.expiredTimeToYYMM(expiredTime);// 银行卡有效期，年在前、月在后（信用卡必填）
		LOG.info("转换过后的有效期YYMM：" + expired);

		// 获取银行 卡信息
		BankNumCode bCode = topupPayChannelBusiness.getBankNumCodeByBankName(cardName);
		String code = bCode.getBankCode();

		CJRegister cJRegister = topupPayChannelBusiness.getCJRegisterByIdCard(idCard);
		String MerchantCode = cJRegister.getMerchantCode();

		SimpleDateFormat sd = new SimpleDateFormat("YYYYMMddHHmmss");
		String date = sd.format(new Date());
		String requestNo = MerchantCode + date;
		LOG.info("绑卡请求流水号：" + requestNo);

		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("merchantNo", MerchantCode);
		apiRequest.addParam("requestNo", requestNo);
		apiRequest.addParam("channelProductCode", channelProductCode);
		apiRequest.addParam("bankCardNo", bankCard);
		apiRequest.addParam("bankAccountName", userName);
		apiRequest.addParam("cardType", "CREDIT");
		apiRequest.addParam("bankMobile ", phoneC);
		apiRequest.addParam("certType", "PRC_ID");
		apiRequest.addParam("certNo", idCard);
		apiRequest.addParam("cvn2", securityCode);
		apiRequest.addParam("expired", expired);
		apiRequest.addParam("bankAbbr", code);
		apiRequest.addParam("serverCallbackUrl", ip + "/v1.0/paymentgateway/topup/cj/bindcard/notify_call");

		LOG.info("绑卡请求参数---apiRequest:" + apiRequest.toString());
		ApiResponse post = ApiClient.post(url, apiRequest);
		LOG.info("绑卡返回参数---post:" + JSON.toJSONString(post));

		JSONObject fromObject2 = JSONObject.fromObject(post);

		String state = fromObject2.getString("state");
		LOG.info("state-----" + state);

		JSONObject resultMap = fromObject2.getJSONObject("resultMap");

		JSONObject fromObject3 = JSONObject.fromObject(resultMap);
		LOG.info("fromObject3======" + fromObject3);
		String bizCode = fromObject3.getString("bizCode");
		String bizMsg = fromObject3.getString("bizMsg");
		String code1 = fromObject3.getString("code");

		CJBindCard cjBindCardByBankCard = topupPayChannelBusiness.getCJBindCardByBankCard(bankCard);

		if ("200".equals(code1) && "1".equals(bizCode) && "SUCCESS".equals(state)) {
			String bindStatus = fromObject3.getString("bindStatus");
			CJBindCard cJBindCard = new CJBindCard();
			if ("SUCCESS".equals(bindStatus)) {
				LOG.info("绑卡成功-----" + bizMsg);
				if (cjBindCardByBankCard == null) {

					cJBindCard.setUserName(userName);
					cJBindCard.setPhone(phoneC);
					cJBindCard.setStatus(bindStatus);// SUCCESS:绑卡成功
														// PENDING:待确认绑卡
														// FAIL:绑卡失败
					cJBindCard.setBankCard(bankCard);
					cJBindCard.setRequestNo(requestNo);
					cJBindCard.setIdCard(idCard);
					topupPayChannelBusiness.createCJBindCard(cJBindCard);

				} else {
					cjBindCardByBankCard.setRequestNo(requestNo);
					topupPayChannelBusiness.createCJBindCard(cjBindCardByBankCard);
				}

				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;
			} else if ("PENDING".equals(bindStatus)) {
				LOG.info("待确认绑卡-----" + bizMsg);
				if (cjBindCardByBankCard == null) {
					cJBindCard.setUserName(userName);
					cJBindCard.setPhone(phoneC);
					cJBindCard.setStatus(bindStatus);// SUCCESS:绑卡成功
														// PENDING:待确认绑卡
														// FAIL:绑卡失败
					cJBindCard.setBankCard(bankCard);
					cJBindCard.setRequestNo(requestNo);
					cJBindCard.setIdCard(idCard);

					topupPayChannelBusiness.createCJBindCard(cJBindCard);

				} else {
					cjBindCardByBankCard.setRequestNo(requestNo);

					topupPayChannelBusiness.createCJBindCard(cjBindCardByBankCard);
				}

				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;
			} else {
				LOG.info("绑卡失败-----" + bizMsg);

				this.addOrderCauseOfFailure(orderCode, bizMsg, rip);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;
			}
		} else {
			LOG.info("绑卡异常-----" + bizMsg);

			this.addOrderCauseOfFailure(orderCode, bizMsg, rip);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
			return maps;
		}

	}

	/**
	 * 绑卡确认
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cj/confirmBindCard")
	public @ResponseBody Object confirmBindCard(HttpServletRequest request,
			@RequestParam(value = "smsCode") String smsCode, @RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "orderCode") String orderCode, @RequestParam(value = "bankName") String bankName,
			@RequestParam(value = "idCard") String idCard) throws Exception {
		LOG.info("开始进入绑卡确认接口--------");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String rip = prp.getIpAddress();

		String url = baseurl + "/rest/v1.0/paybar/confirmBindCard";
		Map<String, Object> maps = new HashMap<String, Object>();

		CJRegister cJRegister = topupPayChannelBusiness.getCJRegisterByIdCard(idCard);
		String MerchantCode = cJRegister.getMerchantCode();

		CJBindCard cJBindCard = topupPayChannelBusiness.getCJBindCardByBankCard(bankCard);
		String requestNo = cJBindCard.getRequestNo();
		LOG.info("绑卡请求流水号：" + requestNo);

		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("merchantNo", MerchantCode);
		apiRequest.addParam("bindRequestNo", requestNo);
		apiRequest.addParam("smsCode", smsCode);

		LOG.info("确认绑卡请求参数---apiRequest:" + apiRequest.toString());
		ApiResponse post = ApiClient.post(url, apiRequest);
		LOG.info("确认绑卡返回参数---post:" + JSON.toJSONString(post));

		JSONObject fromObject2 = JSONObject.fromObject(post);

		String state = fromObject2.getString("state");
		LOG.info("state-----" + state);

		JSONObject resultMap = fromObject2.getJSONObject("resultMap");

		JSONObject fromObject3 = JSONObject.fromObject(resultMap);
		LOG.info("fromObject3======" + fromObject3);
		String bizCode = fromObject3.getString("bizCode");
		String bizMsg = fromObject3.getString("bizMsg");
		String code1 = fromObject3.getString("code");

		if ("200".equals(code1) && "1".equals(bizCode) && "SUCCESS".equals(state)) {
			String bindStatus = fromObject3.getString("bindStatus");
			if ("SUCCESS".equals(bindStatus)) {
				LOG.info("绑卡成功-----" + bizMsg);
				LOG.info("绑卡成功跳转到支付页面-----");

				cJBindCard.setStatus(bindStatus);// SUCCESS:绑卡成功 PENDING:待确认绑卡
													// FAIL:绑卡失败
				topupPayChannelBusiness.createCJBindCard(cJBindCard);

				maps.put("redirect_url", ip + "/v1.0/paymentgateway/topup/tocj/pay?orderCode=" + orderCode
						+ "&bankCard=" + bankCard + "&bankName=" + bankName + "&ipAddress=" + ip);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;
			} else if ("PENDING".equals(bindStatus)) {
				LOG.info("待确认绑卡-----" + bizMsg);
				LOG.info("待确认有误，重新绑卡-----");

				cJBindCard.setStatus(bindStatus);// SUCCESS:绑卡成功 PENDING:待确认绑卡
													// FAIL:绑卡失败
				topupPayChannelBusiness.createCJBindCard(cJBindCard);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "绑卡有误，请重新绑卡");
				return maps;
			} else {
				LOG.info("绑卡失败-----" + bizMsg);

				this.addOrderCauseOfFailure(orderCode, bizMsg, rip);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;
			}
		} else {
			LOG.info("绑卡异常-----" + bizMsg);

			this.addOrderCauseOfFailure(orderCode, bizMsg, rip);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
			return maps;
		}

	}

	/**
	 * 交易申请
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cj/pay")
	public @ResponseBody Object pay(HttpServletRequest request, @RequestParam(value = "orderCode") String orderCode)
			throws Exception {
		LOG.info("开始进入交易申请接口--------");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String idCard = prp.getIdCard();
		String bankCardNo = prp.getBankCard();
		String cvn2 = prp.getSecurityCode();
		String expiredTime = prp.getExpiredTime();
		String expired = this.expiredTimeToYYMM(expiredTime);// YYMM
		String amount = prp.getAmount();
		String clientIp = prp.getIpAddress();
		String rip = prp.getIpAddress();

		CJRegister cJRegister = topupPayChannelBusiness.getCJRegisterByIdCard(idCard);
		String MerchantCode = cJRegister.getMerchantCode();

		String url = baseurl + "/rest/v1.0/paybar/pay";
		Map<String, Object> maps = new HashMap<String, Object>();

		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("merchantNo", MerchantCode);
		apiRequest.addParam("requestNo", orderCode);
		apiRequest.addParam("channelProductCode", channelProductCode);
		apiRequest.addParam("bankCardNo", bankCardNo);
		apiRequest.addParam("cvn2", cvn2);
		apiRequest.addParam("expired", expired);
		apiRequest.addParam("amount", amount);
		apiRequest.addParam("productName", "Crocs新款休闲鞋");// 产品名称 最长允许40个汉字
		apiRequest.addParam("productDesc", "CROCS/卡洛驰/低帮/休闲鞋");// 产品描述 最长允许40个汉字
		apiRequest.addParam("serverCallbackUrl", ip + "/v1.0/paymentgateway/topup/cj/pay/notify_call");
		apiRequest.addParam("webCallbackUrl", "");// 前台页面跳转地址 （当产品为YSBPAY_01时必传）
		apiRequest.addParam("clientIp", "101.86.131.84");// 如192.168.1.1

		LOG.info("交易请求参数---apiRequest:" + apiRequest.toString());
		ApiResponse post = ApiClient.post(url, apiRequest);
		LOG.info("交易返回参数---post:" + JSON.toJSONString(post));

		JSONObject fromObject2 = JSONObject.fromObject(post);

		String state = fromObject2.getString("state");
		LOG.info("state-----" + state);

		JSONObject resultMap = fromObject2.getJSONObject("resultMap");

		JSONObject fromObject3 = JSONObject.fromObject(resultMap);
		LOG.info("fromObject3======" + fromObject3);
		String bizCode = fromObject3.getString("bizCode");
		String bizMsg = fromObject3.getString("bizMsg");
		String code1 = fromObject3.getString("code");

		if ("200".equals(code1) && "1".equals(bizCode) && "SUCCESS".equals(state)) {
			LOG.info("返回交易申请状态--------");

			String status = fromObject3.getString("status");
			String isSms = fromObject3.getString("isSms");// 1需要发短信，0不需要发短信
			System.out.println(isSms);

			if ("SUCCESS".equals(status)) {
				LOG.info("交易成功-----" + bizMsg);

				maps.put("redirect_url", "http://106.15.47.73/v1.0/paymentchannel/topup/sdjpaysuccess");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;
			} else if ("UNPAY".equals(status)) {
				LOG.info("交易待确认-----" + bizMsg);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;
			} else if ("PROCESS".equals(status)) {
				LOG.info("支付中-----" + bizMsg);

				this.addOrderCauseOfFailure(orderCode, bizMsg, rip);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;
			} else if ("UNKNOW".equals(status)) {
				LOG.info("未知-----" + bizMsg);

				this.addOrderCauseOfFailure(orderCode, bizMsg, rip);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;
			} else {// FAILURE
				LOG.info("失败-----" + bizMsg);

				this.addOrderCauseOfFailure(orderCode, bizMsg, rip);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;
			}
		} else {
			LOG.info("交易异常--------");

			this.addOrderCauseOfFailure(orderCode, bizMsg, rip);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
			return maps;
		}
	}

	/**
	 * 交易确认
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cj/confirmPay")
	public @ResponseBody Object confirmPay(HttpServletRequest request,
			@RequestParam(value = "orderCode") String orderCode, @RequestParam(value = "smsCode") String smsCode)
					throws Exception {
		LOG.info("开始进入交易确定接口--------");

		String url = baseurl + "/rest/v1.0/paybar/confirmPay";
		Map<String, Object> maps = new HashMap<String, Object>();

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String idCard = prp.getIdCard();
		String rip = prp.getIpAddress();

		CJRegister cJRegister = topupPayChannelBusiness.getCJRegisterByIdCard(idCard);
		String MerchantCode = cJRegister.getMerchantCode();

		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("merchantNo", MerchantCode);
		apiRequest.addParam("payRequestNo", orderCode);
		apiRequest.addParam("smsCode", smsCode);
		LOG.info("交易确认请求参数---apiRequest:" + apiRequest.toString());
		ApiResponse post = ApiClient.post(url, apiRequest);
		LOG.info("交易确认返回参数---post:" + JSON.toJSONString(post));

		JSONObject fromObject2 = JSONObject.fromObject(post);

		String state = fromObject2.getString("state");
		LOG.info("state-----" + state);

		JSONObject resultMap = fromObject2.getJSONObject("resultMap");

		JSONObject fromObject3 = JSONObject.fromObject(resultMap);
		LOG.info("fromObject3======" + fromObject3);
		String bizCode = fromObject3.getString("bizCode");
		String bizMsg = fromObject3.getString("bizMsg");
		String code1 = fromObject3.getString("code");

		if ("200".equals(code1) && "1".equals(bizCode) && "SUCCESS".equals(state)) {
			LOG.info("返回交易申请状态--------");
			String status = fromObject3.getString("status");

			if ("SUCCESS".equals(status)) {
				LOG.info("交易成功-----" + "订单号：" + orderCode + "-------------" + bizMsg);

				maps.put("redirect_url", "http://106.15.47.73/v1.0/paymentchannel/topup/sdjpaysuccess");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;
			} else if ("UNPAY".equals(status)) {
				LOG.info("交易待确认-----" + bizMsg);
				
				maps.put("redirect_url", "http://106.15.47.73/v1.0/paymentchannel/topup/yldzpaying");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;
			} else if ("PROCESS".equals(status)) {
				LOG.info("支付中-----" + bizMsg);

				maps.put("redirect_url", "http://106.15.47.73/v1.0/paymentchannel/topup/yldzpaying");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;
			} else if ("UNKNOW".equals(status)) {
				LOG.info("未知-----" + bizMsg);

				maps.put("redirect_url", "http://106.15.47.73/v1.0/paymentchannel/topup/yldzpaying");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;
			} else {// FAILURE
				LOG.info("失败-----" + bizMsg);

				this.addOrderCauseOfFailure(orderCode, bizMsg, rip);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;
			}
		} else {
			LOG.info("交易异常--------");

			this.addOrderCauseOfFailure(orderCode, bizMsg, rip);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
			return maps;
		}
	}

	/**
	 * 绑卡查询
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cj/queryBindCard")
	public @ResponseBody Object queryBindCard(HttpServletRequest request,
			@RequestParam(value = "MerchantCode") String MerchantCode,
			@RequestParam(value = "requestNo") String requestNo) throws Exception {
		LOG.info("开始进入绑卡查询接口--------");
		String url = baseurl + "/rest/v1.0/paybar/queryBindCard";
		Map<String, Object> maps = new HashMap<String, Object>();

		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("merchantNo", MerchantCode);
		apiRequest.addParam("requestNo", requestNo);
		LOG.info("绑卡查询请求参数---apiRequest:" + apiRequest.toString());
		ApiResponse post = ApiClient.post(url, apiRequest);
		LOG.info("绑卡查询返回参数---post:" + JSON.toJSONString(post));

		JSONObject fromObject2 = JSONObject.fromObject(post);

		String state = fromObject2.getString("state");
		LOG.info("state-----" + state);

		JSONObject resultMap = fromObject2.getJSONObject("resultMap");

		JSONObject fromObject3 = JSONObject.fromObject(resultMap);
		LOG.info("fromObject3======" + fromObject3);
		String bizCode = fromObject3.getString("bizCode");
		String bizMsg = fromObject3.getString("bizMsg");
		String code1 = fromObject3.getString("code");

		if ("200".equals(code1) && "1".equals(bizCode) && "SUCCESS".equals(state)) {
			String bindStatus = fromObject3.getString("bindStatus");
			if ("SUCCESS".equals(bindStatus)) {
				LOG.info("绑卡成功-----" + bizMsg);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;
			} else if ("PENDING".equals(bindStatus)) {
				LOG.info("待确认绑卡-----" + bizMsg);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;
			} else {
				LOG.info("绑卡失败-----" + bizMsg);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
				return maps;
			}
		} else {
			LOG.info("绑卡异常-----" + bizMsg);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
			return maps;
		}
	}
	
	/**
	 * 商户查询已绑卡
	 * */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cj/queryBankCardList")
	public @ResponseBody Object queryBankCardList(HttpServletRequest request,
			@RequestParam(value = "idCard") String idCard) throws Exception {
		LOG.info("开始进入查询已绑卡接口--------");
		
		CJRegister cJRegister = topupPayChannelBusiness.getCJRegisterByIdCard(idCard);
		String MerchantCode = cJRegister.getMerchantCode();
		
		String url = baseurl + "/rest/v1.0/paybar/queryBankCardList";
		Map<String, Object> maps = new HashMap<String, Object>();

		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("merchantNo", MerchantCode);
		apiRequest.addParam("channelProductCode", channelProductCode);
		LOG.info("查询已绑卡请求参数---apiRequest:" + apiRequest.toString());
		ApiResponse post = ApiClient.post(url, apiRequest);
		LOG.info("查询已绑卡返回参数---post:" + JSON.toJSONString(post));

 
		JSONObject fromObject2 = JSONObject.fromObject(post);

		String state = fromObject2.getString("state");
		LOG.info("state-----" + state);

		JSONObject resultMap = fromObject2.getJSONObject("resultMap");

		JSONObject fromObject3 = JSONObject.fromObject(resultMap);
		LOG.info("fromObject3======" + fromObject3);
		String bizCode = fromObject3.getString("bizCode");
		String bizMsg = fromObject3.getString("bizMsg");
		String code1 = fromObject3.getString("code");
		String bankCardNoList = fromObject3.getString("bankCardNoList");
 
		if ("200".equals(code1) && "1".equals(bizCode) && "SUCCESS".equals(state)) {

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, bankCardNoList);
			return maps;
		} else {
			LOG.info("查询已绑卡异常-----" + bizMsg);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
			return maps;
		}
	}
	
	/**
	 * 交易查询 
	 * */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/cj/queryPay")
	public @ResponseBody Object queryPay(HttpServletRequest request,
			@RequestParam(value = "orderCode") String orderCode) throws Exception {
		LOG.info("开始进入交易查询接口--------");
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String idCard = prp.getIdCard();
		
		CJRegister cJRegister = topupPayChannelBusiness.getCJRegisterByIdCard(idCard);
		String MerchantCode = cJRegister.getMerchantCode();
		
		String url = baseurl + "/rest/v1.0/paybar/queryPay";
		Map<String, Object> maps = new HashMap<String, Object>();

		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("merchantNo", MerchantCode);
		apiRequest.addParam("requestNo", orderCode);
		LOG.info("交易查询请求参数---apiRequest:" + apiRequest.toString());
		ApiResponse post = ApiClient.post(url, apiRequest);
		LOG.info("交易查询返回参数---post:" + JSON.toJSONString(post));

 
		JSONObject fromObject2 = JSONObject.fromObject(post);

		String state = fromObject2.getString("state");
		LOG.info("state-----" + state);

		JSONObject resultMap = fromObject2.getJSONObject("resultMap");

		JSONObject fromObject3 = JSONObject.fromObject(resultMap);
		LOG.info("fromObject3======" + fromObject3);

		String bizCode = fromObject3.getString("bizCode");
		String bizMsg = fromObject3.getString("bizMsg");
		String code1 = fromObject3.getString("code");

		if ("200".equals(code1) && "1".equals(bizCode) && "SUCCESS".equals(state)) {
			LOG.info("返回交易查询状态--------");
			String status = fromObject3.getString("status");

			if ("SUCCESS".equals(status)) {
				LOG.info("交易成功-----" + "订单号：" + orderCode + "-------------" + bizMsg);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "交易成功");
				return maps;
			} else if ("UNPAY".equals(status)) {
				LOG.info("交易待确认-----" + bizMsg);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "交易待确认");
				return maps;
			} else if ("PROCESS".equals(status)) {
				LOG.info("支付中-----" + bizMsg);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "支付中");
				return maps;
			} else if ("UNKNOW".equals(status)) {
				LOG.info("未知-----" + bizMsg);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "未知");
				return maps;
			} else {// FAILURE
				LOG.info("失败-----" + bizMsg);
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "交易失败");
				return maps;
			}
		} else {
			LOG.info("交易异常--------");

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, bizMsg);
			return maps;
		}
	}
	
	/**
	 * 跳转绑卡页面
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/cj/toConfirmBindCard")
	public String toConfirmBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String orderCode = request.getParameter("orderCode");
		String bankCard = request.getParameter("bankCard");
		String userName = request.getParameter("userName");
		String phone = request.getParameter("phoneC");
		String idCard = request.getParameter("idCard");
		String bankName = request.getParameter("cardName");
		String securityCode = request.getParameter("securityCode");
		String expiredTime = request.getParameter("expiredTime");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("orderCode", orderCode);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("userName", userName);
		model.addAttribute("phone", phone);
		model.addAttribute("idCard", idCard);
		model.addAttribute("bankName", bankName);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("ipAddress", ipAddress);

		return "cjbindcard";
	}

	/**
	 * 绑卡异步回调
	 */
	@RequestMapping(method = { RequestMethod.POST,
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/cj/bindcard/notify_call")
	public void bindcardNotify(HttpServletRequest request, HttpServletResponse response) throws IOException {

		LOG.info("绑卡回调回来了！！！！！！！！！！！！！！");

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

		LOG.info("fromObject======"+fromObject);

		String requestNo = fromObject.getString("requestNo");
		String merchantNo = fromObject.getString("merchantNo");
		String bindStatus = fromObject.getString("bindStatus");
		LOG.info("请求绑卡流水号requestNo-----------" + requestNo);
		LOG.info("请求绑卡商户号merchantNo-----------" + merchantNo);
		LOG.info("bindStatus-----------" + bindStatus);

		if ("SUCCESS".equals(bindStatus)) {
			LOG.info("*********************绑卡成功***********************");

			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();

		} else {
			LOG.info("绑卡异常!");

			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();
		}
	}

	/**
	 * 交易回调
	 */
	@RequestMapping(method = { RequestMethod.POST,
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/cj/pay/notify_call")
	public void payNotifyCall(HttpServletRequest request, HttpServletResponse response) throws IOException {

		LOG.info("交易回调回来了！！！！！！！！！！！！！！");

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

		LOG.info("fromObject======"+fromObject);

		String orderCode = fromObject.getString("requestNo");
		String merchantNo = fromObject.getString("merchantNo");
		String status = fromObject.getString("status");
		String amount = fromObject.getString("amount");
		String bizMsg = fromObject.getString("bizMsg");

		LOG.info("交易流水号orderCode-----------" + orderCode + ",交易金额：" + amount);
		LOG.info("交易商户号merchantNo-----------" + merchantNo);
		LOG.info("交易状态status-----------" + status);
		LOG.info("返回信息bizMsg-----------" + bizMsg);

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		if ("SUCCESS".equals(status)) {
			LOG.info("*********************交易成功***********************");

			RestTemplate restTemplate = new RestTemplate();
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			String url = null;
			String result = null;

			url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
			//url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", orderCode);
			requestEntity.add("third_code", "");
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("",e);
			}

			LOG.info("订单状态修改成功===================" + orderCode + "====================" + result);

			LOG.info("订单已交易成功!");

			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();

		}else{
			LOG.info("交易异常!");

			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();
		}
	}

	/**
	 * 跳转支付页面
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/tocj/pay")
	public String pay(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String ordercode = request.getParameter("orderCode");
		String bankName = request.getParameter("bankName");
		String bankCard = request.getParameter("bankCard");
		String ipAddress = request.getParameter("ipAddress");
		System.out.println(ordercode);

		model.addAttribute("ordercode", ordercode);
		model.addAttribute("bankName", bankName);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("ipAddress", ipAddress);

		return "cjpaymessage";
	}

}
