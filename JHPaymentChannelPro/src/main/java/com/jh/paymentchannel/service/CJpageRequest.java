package com.jh.paymentchannel.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
import org.springframework.ui.Model;
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
import com.jh.paymentchannel.pojo.CJBindCard;
import com.jh.paymentchannel.pojo.CJHKBindCard;
import com.jh.paymentchannel.pojo.CJHKRegister;
import com.jh.paymentchannel.pojo.CJRegister;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.cjhk.AESUtil;
import com.jh.paymentchannel.util.cjhk.ApiClient;
import com.jh.paymentchannel.util.cjhk.ApiRequest;
import com.jh.paymentchannel.util.cjhk.ApiResponse;
import com.jh.paymentchannel.util.cjhk.EncryptTypeEnum;

import cn.jh.common.tools.Log;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class CJpageRequest extends BaseChannel {

	private static final Logger LOG = LoggerFactory.getLogger(CJpageRequest.class);

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

	private String Url = "http://apis.miaowpay.com";

	private String key = "914a8687201f4134a426faa603c6e06b";

	private String channelProductCode = "GHTPAY_01";

	private static final Charset UTF_8 = StandardCharsets.UTF_8;

	// 进件接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cj/register")
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

		Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");

		if (!"000000".equals(queryBankCardByCardNo.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", queryBankCardByCardNo.get("resp_message"));
			return maps;
		}
		Object object2 = queryBankCardByCardNo.get("result");
		fromObject = JSONObject.fromObject(object2);

		String userName = fromObject.getString("userName");
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
		String userName1 = fromObject.getString("userName");
		String idcard1 = fromObject.getString("idcard");
		String phone1 = fromObject.getString("phone");
		String bankName1 = fromObject.getString("bankName");

		Map<String, Object> bankUnitNo = this.getBankUnitNo(Util.queryBankNameByBranchName(bankName1));
		if (!"000000".equals(bankUnitNo.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", bankUnitNo.get("resp_message"));
			
			this.addOrderCauseOfFailure(orderCode, bankUnitNo.get("resp_message"));
			
			return maps;
		}

		String inBankUnitNo = (String) bankUnitNo.get("result");

		Map<String, Object> bankCodeByBankName = this.getBankCodeByBankName(Util.queryBankNameByBranchName(bankName1));
		if (!"000000".equals(bankCodeByBankName.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", bankCodeByBankName.get("resp_message"));
			
			this.addOrderCauseOfFailure(orderCode, bankCodeByBankName.get("resp_message"));
			
			return maps;
		}

		String code = (String) bankCodeByBankName.get("result");


		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("requestNo", String.valueOf(System.currentTimeMillis()));
		apiRequest.addParam("merchantName ", "上海莘丽网络");
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
		apiRequest.addParam("settlementType", "AUTO_SETTLE");
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

				CJRegister cjRegister = new CJRegister();
				cjRegister.setPhone(mobile);
				cjRegister.setIdCard(idcard1);
				cjRegister.setBankCard(cardNo);
				cjRegister.setMerchantCode(merchantCode);
				cjRegister.setEncryptKey(encryptKey);
				cjRegister.setRate(rate);
				cjRegister.setExtraFee(extraFee);

				topupPayChannelBusiness.createCJRegister(cjRegister);

				maps.put("resp_code", "success");
				maps.put("channel_type", "jf");
				maps.put("redirect_url",
						ipAddress + "/v1.0/paymentchannel/topup/tocj/bindcard?bankName=" + URLEncoder.encode(bankName, "UTF-8")
								+ "&cardType=" + URLEncoder.encode(cardtype, "UTF-8") + "&bankCard=" + bankCard + "&ordercode="
								+ orderCode + "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&ipAddress="
								+ ipAddress);

				return maps;
			} else {
				maps.put("resp_code", "failed");
				maps.put("channel_type", "jf");
				maps.put("resp_message", bizMsg);
				
				this.addOrderCauseOfFailure(orderCode, bizMsg);
				
				return maps;
			}
		} else {
			LOG.info("请求失败====");
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", bizMsg);
			
			this.addOrderCauseOfFailure(orderCode, bizMsg);
			
			return maps;
		}

	}

	// 开通产品的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cj/product")
	public @ResponseBody Object cjhkProduct(HttpServletRequest request,
			@RequestParam(value = "merchantCode") String merchantCode, @RequestParam(value = "rate") String rate)
			throws Exception {

		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("merchantNo ", "1318050716488390");
		apiRequest.addParam("channelProductCode", channelProductCode);
		apiRequest.addParam("debitRate", rate);
		apiRequest.addParam("debitCapAmount", "999999");
		apiRequest.addParam("creditRate", rate);
		apiRequest.addParam("creditCapAmount", "999999");

		LOG.info("开通产品的请求报文 apiRequest======" + apiRequest.toString());

		ApiResponse post = ApiClient.post(Url + "/rest/v1.0/paybar/registMerchantProduct", apiRequest);

		LOG.info("请求返回的 post======" + JSON.toJSONString(post));

		return null;
	}

	// 银联绑卡接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cj/bindcard")
	public @ResponseBody Object cjhkBindCard(HttpServletRequest request,
			@RequestParam(value = "ordercode") String orderCode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode) throws Exception {
		LOG.info("开始进入绑卡接口======");
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

		Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
		Object object2 = queryBankCardByCardNo.get("result");
		fromObject = JSONObject.fromObject(object2);

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
			
			this.addOrderCauseOfFailure(orderCode, bankCodeByBankName.get("resp_message"));
			
			return maps;
		}

		String code = (String) bankCodeByBankName.get("result");

		CJRegister cjRegister = topupPayChannelBusiness.getCJRegisterByIdCard(idCard);

		String orderNo = UUID.randomUUID().toString().replace("-", "");

		String requestNo = String.valueOf(System.currentTimeMillis());

		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("merchantNo", cjRegister.getMerchantCode());
		apiRequest.addParam("requestNo", requestNo);
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
		apiRequest.addParam("serverCallbackUrl", ipAddress + "/v1.0/paymentchannel/topup/cj/bindcard/notify_call");

		LOG.info("绑卡的请求报文 apiRequest======" + apiRequest.toString());

		ApiResponse post = ApiClient.post(Url + "/rest/v1.0/paybar/bindCard", apiRequest);

		LOG.info("请求返回的 post======" + JSON.toJSONString(post));

		JSONObject fromObject2 = JSONObject.fromObject(post);

		String state = fromObject2.getString("state");

		JSONObject resultMap = fromObject2.getJSONObject("resultMap");

		JSONObject fromObject3 = JSONObject.fromObject(resultMap);
		LOG.info("fromObject3======" + fromObject3);
		String bizCode = fromObject3.getString("bizCode");
		String bizMsg = fromObject3.getString("bizMsg");
		String code1 = fromObject3.getString("code");

		CJBindCard cjBindCardByBankCard = topupPayChannelBusiness.getCJBindCardByBankCard(bankCard);
		
		if ("200".equals(code1) && "1".equals(bizCode)) {
			if ("SUCCESS".equals(state)) {
				String bindStatus = fromObject3.getString("bindStatus");
				if ("SUCCESS".equalsIgnoreCase(bindStatus)) {
					LOG.info("绑卡成功======");
					
					if(cjBindCardByBankCard == null) {
						
						CJBindCard cjBindCard = new CJBindCard();
						cjBindCard.setPhone(phone);
						cjBindCard.setBankCard(bankCard);
						cjBindCard.setIdCard(idCard);
						cjBindCard.setStatus("1");

						topupPayChannelBusiness.createCJBindCard(cjBindCard);
					
					}else {
						cjBindCardByBankCard.setStatus("1");
						
						topupPayChannelBusiness.createCJBindCard(cjBindCardByBankCard);
					}
					
					maps.put("resp_code", "success");
					maps.put("channel_type", "jf");
					maps.put("redirect_url",
							ipAddress + "/v1.0/paymentchannel/topup/tocj/pay?bankName=" + URLEncoder.encode(bankName, "UTF-8")
									+ "&cardType=" + URLEncoder.encode(cardtype, "UTF-8") + "&bankCard=" + bankCard
									+ "&ordercode=" + orderCode + "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&ipAddress=" + ipAddress);

					return maps;
					
				} else if ("PENDING".equalsIgnoreCase(bindStatus)) {
					LOG.info("等待确认======");
					if(cjBindCardByBankCard == null) {
						
						CJBindCard cjBindCard = new CJBindCard();
						cjBindCard.setPhone(phone);
						cjBindCard.setBankCard(bankCard);
						cjBindCard.setIdCard(idCard);
						cjBindCard.setStatus("0");

						topupPayChannelBusiness.createCJBindCard(cjBindCard);
					
					}else {
						cjBindCardByBankCard.setStatus("0");
						
						topupPayChannelBusiness.createCJBindCard(cjBindCardByBankCard);
					}

					maps.put("resp_code", "failed");
					maps.put("channel_type", "jf");
					maps.put("resp_message", "等待确认绑卡成功!");
					return maps;
					
				} else {
					LOG.info("绑卡失败======");
					if(cjBindCardByBankCard == null) {
						
						CJBindCard cjBindCard = new CJBindCard();
						cjBindCard.setPhone(phone);
						cjBindCard.setBankCard(bankCard);
						cjBindCard.setIdCard(idCard);
						cjBindCard.setStatus("2");

						topupPayChannelBusiness.createCJBindCard(cjBindCard);
					
					}else {
						cjBindCardByBankCard.setStatus("2");
						
						topupPayChannelBusiness.createCJBindCard(cjBindCardByBankCard);
					}
					
					maps.put("resp_code", "failed");
					maps.put("channel_type", "jf");
					maps.put("resp_message", "绑卡失败!");
					return maps;
					
				}

			} else {
				maps.put("resp_code", "failed");
				maps.put("channel_type", "jf");
				maps.put("resp_message", bizMsg);
				
				this.addOrderCauseOfFailure(orderCode, bizMsg);
				
				return maps;
			}

		} else {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", bizMsg);
			
			this.addOrderCauseOfFailure(orderCode, bizMsg);
			
			return maps;
		}

	}

	// 修改费率接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cj/updatemerchant")
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

	
	// 获取短信验证码接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cj/getsmscode")
	public @ResponseBody Object cjhkFastPay(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode, 
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode
			) throws Exception {
		LOG.info("开始进入获取短信验证码接口======");

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

		expiredTime = this.expiredTimeToYYMM(expiredTime);

		LOG.info("转换过的有效期格式======" + expiredTime);

		CJRegister cjRegister = topupPayChannelBusiness.getCJRegisterByIdCard(idCard);

		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("merchantNo", cjRegister.getMerchantCode());
		apiRequest.addParam("requestNo", ordercode);
		apiRequest.addParam("channelProductCode", channelProductCode);
		apiRequest.addParam("bankCardNo", bankCard);
		apiRequest.addParam("cvn2", securityCode);
		apiRequest.addParam("expired", expiredTime);
		apiRequest.addParam("amount", amount);

		apiRequest.addParam("productName", "充值缴费");
		apiRequest.addParam("productDesc", "充值缴费");
		apiRequest.addParam("serverCallbackUrl ", ipAddress + "/v1.0/paymentchannel/topup/cj/fastpay/notify_call");

		LOG.info("获取短信验证码的请求报文 apiRequest======" + apiRequest.toString());

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
				if ("UNPAY".equals(status)) {
					LOG.info("请求短信发送成功 status======" + status);
					maps.put("resp_code", "success");
					maps.put("channel_type", "jf");
					return maps;

				} else {
					LOG.info("请求短信发送失败 status======" + status);
					maps.put("resp_code", "failed");
					maps.put("channel_type", "jf");
					maps.put("resp_message", "等待银行扣款中");
					return maps;
				}

			} else {
				maps.put("resp_code", "failed");
				maps.put("channel_type", "jf");
				maps.put("resp_message", bizMsg);
				
				this.addOrderCauseOfFailure(ordercode, bizMsg);
				
				return maps;
			}

		} else {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", bizMsg);
			
			this.addOrderCauseOfFailure(ordercode, bizMsg);
			
			return maps;
		}

	}

	// 代付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cj/transfer")
	public @ResponseBody Object cjhkTransfer(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode) throws Exception {
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

	
	//确认支付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cj/fastpay")
	public @ResponseBody Object consumeSMS(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "ordercode") String ordercode, @RequestParam(value = "smsCode") String smsCode)
			throws Exception {
		
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

		CJRegister cjRegister = topupPayChannelBusiness.getCJRegisterByIdCard(idCard);
		
		
		ApiRequest apiRequest = new ApiRequest(merchantNo, key);
		apiRequest.setSupportSign(false);
		apiRequest.setEncryptType(EncryptTypeEnum.AES);
		apiRequest.addParam("merchantNo", cjRegister.getMerchantCode());
		apiRequest.addParam("payRequestNo", ordercode);
		apiRequest.addParam("smsCode", smsCode);
		

		LOG.info("确认支付的请求报文 apiRequest======" + apiRequest.toString());

		ApiResponse post = ApiClient.post(Url + "/rest/v1.0/paybar/confirmPay", apiRequest);

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
				LOG.info("请求支付成功======");
				
				maps.put("resp_code", "success");
				maps.put("channel_type", "jf");
				maps.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/sdjpaysuccess");
				
				return maps;
			} else {
				maps.put("resp_code", "failed");
				maps.put("channel_type", "jf");
				maps.put("resp_message", bizMsg);
				
				this.addOrderCauseOfFailure(ordercode, bizMsg);
				
				return maps;
			}

		} else {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", bizMsg);
			
			this.addOrderCauseOfFailure(ordercode, bizMsg);
			
			return maps;
		}
		
	}
	
	
	// 订单查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cj/ordercodequery")
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

	// 代理商余额查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cj/transferquery")
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
				} else if ("UNPAY".equals(remitStatus) && "PROCESS".equals(remitStatus)) {
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

	// 跳转绑卡页面
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/tocj/bindcard")
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

		return "cjbindcard";
	}

	// 跳转支付页面
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/tocj/pay")
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
		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");

		model.addAttribute("ordercode", ordercode);
		model.addAttribute("bankName", bankName);
		model.addAttribute("cardType", cardType);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("ipAddress", ipAddress);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);

		return "cjpaymessage";
	}

	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/tocj/bindcards")
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
				ipAddress + "/v1.0/paymentchannel/topup/tocj/bindcard?bankName=" + URLEncoder.encode(bankName, "UTF-8")
						+ "&cardType=" + URLEncoder.encode(cardtype, "UTF-8") + "&bankCard=" + bankCard + "&ordercode="
						+ orderCode + "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&ipAddress="
						+ ipAddress);

		return maps;
	}

	
	
	// 代付异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cj/transfer/notify_call")
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
			requestEntity.add("version", "2");
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
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cj/fastpay/notify_call")
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

		LOG.info("fromObject======"+fromObject);
		
		String code = fromObject.getString("code");
		String bizCode = fromObject.getString("bizCode");
		
		String requestNo = fromObject.getString("requestNo");;
		/*if(fromObject.containsKey("requestNo")) {
			requestNo = 
		}*/
		
		String status = fromObject.getString("status");

		if ("200".equals(code) && "1".equals(bizCode)) {

			
			String url = "http://transactionclear/v1.0/transactionclear/payment/update";

			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", requestNo);
			requestEntity.add("third_code", "");
			String result = null;
			JSONObject jsonObject;
			JSONObject resultObj;
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("",e);
			}

			LOG.info("订单状态修改成功===================" + requestNo + "====================" + result);

			LOG.info("订单已支付!");
			
			PrintWriter writer = response.getWriter();
			writer.print("success");
			writer.close();

		}

	}

	// 绑卡异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/cj/bindcard/notify_call")
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

	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/tocjbankinfo")
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

		return "cjbankinfo";
	}

	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/tocj/paypage")
	public @ResponseBody Object returnHLJCQuickPayPage(HttpServletRequest request, 
			@RequestParam(value = "ordercode") String orderCode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode
			)throws IOException {
		
		Map<String,Object> maps = new HashMap<String, Object>();
		
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
				ipAddress + "/v1.0/paymentchannel/topup/tocj/pay?bankName=" + URLEncoder.encode(bankName, "UTF-8")
						+ "&cardType=" + URLEncoder.encode(cardtype, "UTF-8") + "&bankCard=" + bankCard
						+ "&ordercode=" + orderCode + "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&ipAddress=" + ipAddress);

		return maps;
	}
	
}