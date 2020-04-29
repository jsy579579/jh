package com.jh.paymentgateway.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.lang.StringEscapeUtils;
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
import org.springframework.web.multipart.MultipartFile;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.BankNumCode;
import com.jh.paymentgateway.pojo.LMBankNum;
import com.jh.paymentgateway.pojo.LMTAddress;
import com.jh.paymentgateway.pojo.LMTRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.RegisterPartsBuilderss;
import com.jh.paymentgateway.util.MD5Util;
import com.jh.paymentgateway.util.PhotoCompressUtil;
import com.jh.paymentgateway.util.lmt.UnicodeUtils;
import com.jh.paymentgateway.util.ytjf.CHexConver;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class LMTpageRequest extends BaseChannel {
	private static final Logger LOG = LoggerFactory.getLogger(LMTpageRequest.class);

	private String mch_id = "1514312323";
	private String app_id = "to3940105693071a7d63e3e44b0a1639c0";
	private String key = "dcddf093acb42eed08d077a3d433b03d";
	private String characterEncoding = "UTF-8"; // 指定字符集UTF-8

	@Autowired
	RedisUtil redisUtil;

	@Value("${payment.ipAddress}")
	private String ip;

	@Autowired
	TopupPayChannelBusiness topupPayChannelBusiness;

	@SuppressWarnings("null")
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/lmt/register")
	public @ResponseBody Object Register(@RequestParam("orderCode") String orderCode) throws IOException {
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		Map<String, Object> maps = new HashMap<>();
		String userName = prp.getUserName();// 用户名
		String idCard = prp.getIdCard();// 身份证号
		String cardNo = prp.getDebitCardNo();// 到账卡
		String phoneD = prp.getDebitPhone();// 到账卡预留手机号
		String rip = prp.getIpAddress();
		String rate = prp.getRate();
		String amount = prp.getAmount();
		String bankCard = prp.getBankCard();
		String extraFee = prp.getExtraFee();
		String bankName = prp.getDebitBankName();// 到账卡银行名称
		String creditCardName = prp.getCreditCardBankName();
		String phoneC = prp.getCreditCardPhone();
		String expiredTime = prp.getExpiredTime();
		String securityCode = prp.getSecurityCode();
		if (bankName.contains("工商")) {
			bankName = "工商银行";
		} else if (bankName.contains("农业")) {
			bankName = "农业银行";
		} else if (bankName.contains("招商")) {
			bankName = "招商银行";
		} else if (bankName.contains("建设")) {
			bankName = "建设银行";
		} else if (bankName.contains("交通")) {
			bankName = "交通银行";
		} else if (bankName.contains("中信")) {
			bankName = "中信银行";
		} else if (bankName.contains("光大")) {
			bankName = "光大银行";
		} else if (bankName.contains("北京银行")) {
			bankName = "北京银行";
		} else if (bankName.contains("深圳发展")) {
			bankName = "深圳发展银行";
		} else if (bankName.contains("中国银行")) {
			bankName = "中国银行";
		} else if (bankName.contains("兴业")) {
			bankName = "兴业银行";
		} else if (bankName.contains("民生")) {
			bankName = "民生银行";
		}
		if ("邮政储蓄银行".equals(bankName) || "中国邮政储蓄银行信用卡中心(61000000)".equals(bankName)) {
			bankName = "邮储银行";
		}
		LMTRegister lmtRegister = topupPayChannelBusiness.getlmtRegisterByidCard(idCard);

		if (lmtRegister == null) {
			String requestNo = "XinLi" + System.currentTimeMillis();
			LOG.info("===register请求订单号：" + requestNo);

			SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
			parameters.put("mch_id", mch_id);
			parameters.put("app_id", app_id);
			parameters.put("mch_no", requestNo);
			parameters.put("realname", userName);
			parameters.put("bank_card_no", cardNo);
			parameters.put("bank_card_mobile", phoneD);
			parameters.put("id_card", idCard);
			String mySign = createSign(characterEncoding, parameters, key);
			LOG.info("=======================设置签名" + mySign);
			Part[] parts = null;
			try {
				parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(requestNo)
						.setSign(mySign).setRealname(userName).setCardNo(cardNo).setPhone(phoneD).setidCard(idCard)
						.generateParams();
			} catch (Exception e) {
				LOG.info("=======================设置签名出错");
			}

			PostMethod postMethod = new PostMethod(
					"https://pay.longmaoguanjia.com/api/pay/quick_pay_c/sub_merchant_register");
			HttpClient client = new HttpClient();
			postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

			int status = client.executeMethod(postMethod);
			LOG.info("==========status==========" + status);
			if (status == HttpStatus.SC_OK) {
				String backinfo = postMethod.getResponseBodyAsString();
				LOG.info("==========register返回响应==========" + backinfo);
				JSONObject jsonstr = JSONObject.fromObject(backinfo);
				String respCode = jsonstr.getString("code");
				String respMessage = jsonstr.getString("message");
				if (respCode.equals("0")) {
					String data = jsonstr.getString("data");
					JSONObject datastr = JSONObject.fromObject(data);
					String merchantNo = datastr.getString("sub_mch_no");
					LMTRegister lmt = new LMTRegister();
					lmt.setBankCard(cardNo);
					lmt.setMainCustomerNum(mch_id);
					lmt.setCustomerNum(merchantNo);
					lmt.setIdCard(idCard);
					lmt.setUserName(userName);
					lmt.setPhone(phoneD);
					lmt.setStatus("0");
					topupPayChannelBusiness.createlmtRegister(lmt);
            
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, respMessage);
					/*maps.put(CommonConstants.RESULT, ip + "/v1.0/paymentgateway/quick/lmt/pay-view?bankName=" + URLEncoder.encode(bankName, "UTF-8")
								+ "&bankCard=" + cardNo + "&orderCode=" + orderCode + "&ipAddress=" + ip + "&ips="
								+ prp.getIpAddress() + "&phone=" + phoneC
								+ "&amount=" + amount + "&isRegister=1");*/
					maps.put(CommonConstants.RESULT, ip + "/v1.0/paymentgateway/quick/lmt/toHtml?orderCode=" + orderCode 
					+ "&ipAddress=" + ip 
					+ "&isRegister=1");
					return maps;
				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, respMessage);
					this.addOrderCauseOfFailure(orderCode, "进件:" + respMessage + "[" + requestNo + "]", rip);
					return maps;
				}
			}
		}
		if (!cardNo.equals(lmtRegister.getBankCard())) {
			maps = (Map<String, Object>) setMerchant(phoneD, orderCode, bankName, cardNo, rip, idCard);
			if ("999999".equals(maps.get("resp_code"))) {
				return maps;

			}
		}
		
		maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		maps.put(CommonConstants.RESP_MESSAGE, "直接跳转交易页面");
		/*maps.put(CommonConstants.RESULT, ip + "/v1.0/paymentgateway/quick/lmt/pay-view?bankName=" + URLEncoder.encode(bankName, "UTF-8")
		+ "&bankCard=" + cardNo + "&orderCode=" + orderCode + "&ipAddress=" + ip + "&ips="
		+ prp.getIpAddress() + "&phone=" + phoneC
		+ "&amount=" + amount + "&isRegister=1");*/
		maps.put(CommonConstants.RESULT, ip + "/v1.0/paymentgateway/quick/lmt/toHtml?orderCode=" + orderCode 
				+ "&ipAddress=" + ip 
				+ "&isRegister=1");
		return maps;
	}

	/**
	 * 修改银行卡信息
	 * 
	 * @param orderCode
	 * @param bankName
	 * @param cardNo
	 * @param rip
	 * @param idCard
	 * @return
	 * @throws IOException
	 */
	public Object setMerchant(String phoneD, String orderCode, String bankName, String cardNo,
			String rip, String idCard) throws IOException {
		LMTRegister lmt = topupPayChannelBusiness.getlmtRegisterByidCard(idCard);
		Map<String, Object> maps = new HashMap<>();
		String requestNo = "xinli" + System.currentTimeMillis();
		LOG.info("===setMerchant请求订单号：" + requestNo);
		SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
		parameters.put("mch_id", mch_id);
		parameters.put("app_id", app_id);
		parameters.put("mch_no", requestNo);
		parameters.put("sub_mch_no", lmt.getCustomerNum());
		parameters.put("id_card", idCard);

		// 获取缩写
		LMBankNum sbcode = topupPayChannelBusiness.getLMBankNumCodeByBankName(bankName);
		String bankAbbr = sbcode.getBankNum();// 缩写
		parameters.put("bank_code", bankAbbr);

		parameters.put("bank_card_no", cardNo);
		parameters.put("bank_card_mobile", phoneD);

		String mySign = createSign(characterEncoding, parameters, key);
		LOG.info("====setMerchant签名报文：" + mySign.toString());
		Part[] parts = null;
		parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(requestNo)
				.setSubM(lmt.getCustomerNum()).setidCard(idCard).setBankChannelNo(bankAbbr).setCardNo(cardNo)
				.setPhone(phoneD).setSign(mySign).generateParams();
		PostMethod postMethod = new PostMethod("https://pay.longmaoguanjia.com/api/pay/quick_pay_c/sub_merchant_fee");
		HttpClient client = new HttpClient();
		postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

		int status = client.executeMethod(postMethod);
		LOG.info("==========status==========" + status);

		if (status == HttpStatus.SC_OK) {
			String backinfo = postMethod.getResponseBodyAsString();
			LOG.info("==========setMerchant返回响应==========" + backinfo);
			JSONObject jsonstr = JSONObject.fromObject(backinfo);
			String respCode = jsonstr.getString("code");
			String respMessage = jsonstr.getString("message");
			if (respCode.equals("0")) {
				lmt.setBankCard(cardNo);
				lmt.setChangeTime(new Date());
				topupPayChannelBusiness.createlmtRegister(lmt);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				this.addOrderCauseOfFailure(orderCode, "修改银行卡信息:" + respMessage + "[" + requestNo + "]", rip);
				return maps;
			}
		}
		return maps;
	}

	/**
	 * 交易查询
	 * 
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/lmt/queryPayStatus")
	public @ResponseBody Object queryPutPay(@RequestParam(value = "orderCode") String orderCode) throws IOException {
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String idCard = prp.getIdCard();
		LMTRegister lmt = topupPayChannelBusiness.getlmtRegisterByidCard(idCard);
		Map<String, Object> maps = new HashMap<>();
		SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();

		parameters.put("mch_id", mch_id);
		parameters.put("app_id", app_id);
		parameters.put("mch_no", orderCode);
		parameters.put("query_type", "trade");
		parameters.put("sub_mch_no", lmt.getCustomerNum());
		String mySign = createSign(characterEncoding, parameters, key);
		LOG.info("====pay签名报文：" + mySign.toString());
		Part[] parts = null;
		parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(orderCode)
				.setQuery_type("trade").setSubM(lmt.getCustomerNum()).setSign(mySign)
				.generateParams();

		PostMethod postMethod = new PostMethod("https://pay.longmaoguanjia.com/api/pay/quick_pay_c/query");
		HttpClient client = new HttpClient();
		postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

		int status = client.executeMethod(postMethod);
		LOG.info("==========status==========" + status);

		if (status == HttpStatus.SC_OK) {
			String backinfo = postMethod.getResponseBodyAsString();
			LOG.info("==========pay返回响应==========" + backinfo);
			JSONObject jsonstr = JSONObject.fromObject(backinfo);
			String respCode = jsonstr.getString("code");
			String respMessage = jsonstr.getString("message");
			if (respCode.equals("1030009")) {
				LOG.info("1030009：" + "交易成功");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respCode+":"+ respMessage);
				return maps;
			} else if(respCode.equals("1030014")){
				LOG.info("1030014：" + "交易成功，待结算");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respCode+":"+ respMessage);
				return maps;
			} else if(respCode.equals("1030015")){
				LOG.info("1030015：" + "交易成功，结算失败");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respCode+":"+ respMessage);
				return maps;
			} else if(respCode.equals("1030016")){
				LOG.info("1030016：" + "交易成功，结算成功");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respCode+":"+ respMessage);
				return maps;
			}else {
				LOG.info("1050000:不存在该查询类型 "
						+ "1030004:商户订单号不存在"
						+ " 1030011交易已创建 "
						+ "1030013 待交易 "
						+ "1030009交易成功 "
						+ "1030014待结算 "
						+ "1030015结算失败 "
						+ "1030016结算成功 "
						+ "1030008交易失败 "
						+ "1030012交易不存在");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respCode +":"+ respMessage);
				return maps;
			}
		}
		return maps;
	}

	/**
	 * 交易确认
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/lmt/paySure")
	public @ResponseBody Object paySure(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "smsCode") String smsCode) throws IOException {

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String rip = prp.getIpAddress();
		String bankCard = prp.getBankCard();
		String amount =prp.getAmount();
		String realamount = prp.getRealAmount();
		String bankName = prp.getCreditCardBankName();
		
		Map<String, Object> maps = new HashMap<>();
		LOG.info("====================lmt交易确认====================");
		SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();

		parameters.put("mch_id", mch_id);
		parameters.put("app_id", app_id);
		parameters.put("mch_no", orderCode);
		parameters.put("code", smsCode);

		String mySign = createSign(characterEncoding, parameters, key);
		LOG.info("====pay签名报文：" + mySign.toString());
		Part[] parts = null;
		parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id)
				.setMch_no(orderCode).setSmsCode(smsCode).setSign(mySign).generateParams();

		PostMethod postMethod = new PostMethod("https://pay.longmaoguanjia.com/api/pay/quick_pay_c/trade_confirm");
		HttpClient client = new HttpClient();
		postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

		int status = client.executeMethod(postMethod);
		LOG.info("==========status==========" + status);

		if (status == HttpStatus.SC_OK) {
			String backinfo = postMethod.getResponseBodyAsString();
			LOG.info("==========交易确认返回响应==========" + backinfo);
			JSONObject jsonstr = JSONObject.fromObject(backinfo);
			String respCode = jsonstr.getString("code");
			String respMessage = jsonstr.getString("message");
			if (respCode.equals("0")) {		//              
				LOG.info("------------------- 跳转到交易成功中转页面 -------------------");
				maps.put("redirect_url", ip + "/v1.0/paymentgateway/quick/lmt/paysuccess-view?bankName=" + URLEncoder.encode(bankName, "UTF-8")
					+ "&bankCard=" + bankCard 
					+ "&orderCode=" + orderCode 
					+ "&ipAddress=" + ip 
					+ "&ips=" + prp.getIpAddress() 
					+ "&realamount=" + realamount
					+ "&amount=" + amount + "&isRegister=1");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				this.addOrderCauseOfFailure(orderCode, "请求交易:" + respMessage + "[" + orderCode + "]", rip);
				return maps;
			}
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "请求超时");
			this.addOrderCauseOfFailure(orderCode, "请求交易: 请求超时[" + orderCode + "]", rip);
			return maps;
		}
	}
	
	/**
	 * 交易申请/短信
	 * @throws Exception 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/lmt/getsms")
	public @ResponseBody Object putPay(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "provinceOfBank") String provinceOfBank,
			@RequestParam(value = "cityOfBank") String cityOfBank,
			@RequestParam(value = "areaOfBank") String areaOfBank,
			@RequestParam(value = "provinceCode") Long provinceCode,
			@RequestParam(value = "cityCode") Long cityCode,
			@RequestParam(value = "areaCode") Long areaCode) throws Exception {

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String rip = prp.getIpAddress();
		String idCard = prp.getIdCard();
		String creditCardName = prp.getCreditCardBankName();
		String bankCard = prp.getBankCard();
		String phoneC = prp.getCreditCardPhone();
		String expiredTime = prp.getExpiredTime();
		String securityCode = prp.getSecurityCode();
		String amount = prp.getAmount();
		String rate = prp.getRate();
		String ExtraFee = prp.getExtraFee();
		
		Map<String, Object> maps = new HashMap<>();
		LMTRegister lmt = topupPayChannelBusiness.getlmtRegisterByidCard(idCard);
		LOG.info("====================lmt交易申请====================");
		SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();

		parameters.put("mch_id", mch_id);
		parameters.put("app_id", app_id);
		parameters.put("mch_no", orderCode);
		parameters.put("sub_mch_no", lmt.getCustomerNum());

		// 获取银行联行号
		LMBankNum bcode = topupPayChannelBusiness.getLMBankNumCodeByBankName(creditCardName);
		String bankAbbr = bcode.getBankNum();// 缩写
		parameters.put("bank_code", bankAbbr);

		parameters.put("bank_card_no", bankCard);
		parameters.put("bank_card_mobile", phoneC);

		String Expire = this.expiredTimeToMMYY(expiredTime);
		parameters.put("expire_date", Expire);// 有效期 YYMM

		parameters.put("cvn2", securityCode);// 安全码

		String Amount = new BigDecimal(amount).multiply(new BigDecimal(100)).toString();
		parameters.put("amount", Amount);

		parameters.put("sub_mch_rate", rate);

		String extraFee = new BigDecimal(ExtraFee).multiply(new BigDecimal(100)).toString();
		parameters.put("single_price", extraFee);
		//address:{“province":"\u5e7f\u4e1c\u7701","province_code":44,"city":"\u5e7f\u5dde\u5e02","city_code":4401,"area":"\u5929\u6cb3\u533a","area_code":440106}
		Map<String, Object> addressMaps = new TreeMap<String, Object>();
		
		LMTAddress province = topupPayChannelBusiness.getLMTProvinceCode(provinceCode);
		String province_code = province.getCode();
		
		LMTAddress city = topupPayChannelBusiness.getLMTProvinceCode(cityCode);
		String city_code = city.getCode();
		
		LMTAddress area = topupPayChannelBusiness.getLMTProvinceCode(areaCode);
		String area_code = area.getCode();

		addressMaps.put("province",UnicodeUtils.toUnicode(provinceOfBank));
		addressMaps.put("province_code",province_code);
		addressMaps.put("city",UnicodeUtils.toUnicode(cityOfBank));
		addressMaps.put("city_code",city_code);
		addressMaps.put("area",UnicodeUtils.toUnicode(areaOfBank));
		addressMaps.put("area_code",area_code);
		JSONObject json = JSONObject.fromObject(addressMaps);
		
		String unescapeJavaScript = StringEscapeUtils.unescapeJavaScript(json.toString());

		parameters.put("address", unescapeJavaScript);

		String returnUrl = ip + "/v1.0/paymentgateway/topup/lmt/notify_call";
		parameters.put("notify_url", returnUrl);
		
		LOG.info("发送短信请求参数：" + parameters);
		
		String mySign = createSign(characterEncoding, parameters, key);
		LOG.info("====pay签名报文：" + mySign.toString());
		Part[] parts = null;
		parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(orderCode)
				.setSubM(lmt.getCustomerNum()).setBankChannelNo(bankAbbr).setCardNo(bankCard).setPhone(phoneC)
				.setExpire(Expire).setSecurityCode(securityCode).setAmount(Amount).setSubMchRate(rate)
				.setSinglePrice(extraFee).setAddress(unescapeJavaScript).setSign(mySign).setNotifyUrl(returnUrl).generateParams();

		PostMethod postMethod = new PostMethod("https://pay.longmaoguanjia.com/api/pay/quick_pay_c/trade");
		HttpClient client = new HttpClient();
		postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

		int status = client.executeMethod(postMethod);
		LOG.info("==========status==========" + status);

		if (status == HttpStatus.SC_OK) {
			String backinfo = postMethod.getResponseBodyAsString();
			LOG.info("=========交易申请返回响应==========" + backinfo);
			JSONObject jsonstr = JSONObject.fromObject(backinfo);
			String respCode = jsonstr.getString("code");
			String respMessage = jsonstr.getString("message");
			if (respCode.equals("0")) {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			} else {
				LOG.info("------------------- 交易申请失败 -------------------");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				this.addOrderCauseOfFailure(orderCode, "交易申请失败:" + respMessage + "[" + orderCode + "]", rip);
				return maps;
			}
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "请求超时");
			this.addOrderCauseOfFailure(orderCode, "请求交易: 请求超时[" + orderCode + "]", rip);
			return maps;
		}
	}

	/**
	 * 回调
	 * 
	 * @param req
	 * @param res
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/lmt/notify_call")
	public @ResponseBody void notifycall(HttpServletRequest req, HttpServletResponse res) throws IOException {
		LOG.info("进入龙猫回调接口================" + req.getParameterMap().toString());
		// 获取支付报文参数
		Map<String, String[]> params = req.getParameterMap();
		LOG.info("params================" + params);
		Map<String, String> map = new HashMap<String, String>();
		for (String key : params.keySet()) {
			String[] values = params.get(key);
			if (values.length > 0) {
				map.put(key, values[0]);
			}
		}
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(map.get("mch_no"));
		if ("SUCCESS".equals(map.get("order_status"))) {
			RestTemplate restTemplate = new RestTemplate();
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			String URL = null;
			String result = null;
			LOG.info("*********************交易成功***********************");
			
			URL = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
			//URL = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", map.get("mch_no"));
			requestEntity.add("third_code", "");
			try {
				result = restTemplate.postForObject(URL, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error(ExceptionUtil.errInfo(e));
			}

			LOG.info("订单状态修改成功===================" + map.get("mch_no") + "====================" + result);

			LOG.info("订单已交易成功!");
		} else {
			String respMessage = null;
			if ("INIT".equals(map.get("order_status"))) {
				respMessage = "未支付";
			} else if ("FAIL".equals(map.get("order_status"))) {
				respMessage = "失败";
			} else if ("FROZEN".equals(map.get("order_status"))) {
				respMessage = "冻结";
			} else if ("THAWED".equals(map.get("order_status"))) {
				respMessage = "解冻";
			} else if ("REVERSE".equals(map.get("order_status"))) {
				respMessage = "冲正";
			}
			this.addOrderCauseOfFailure(map.get("mch_no"), "支付：" + respMessage, prp.getIpAddress());

		}
		PrintWriter pw = res.getWriter();
		pw.print("SUCCESS");
		pw.close();
	}
	
	/**
	 * 省市区页面跳转到交易中转接口
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws UnsupportedEncodingException 
	 * @throws IOException
	 */              
	@RequestMapping(method=RequestMethod.POST,value=("/v1.0/paymentgateway/lmt/to/pay-view"))
	public @ResponseBody Object toPayView(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "provinceOfBank") String provinceOfBank,
			@RequestParam(value = "cityOfBank") String cityOfBank,
			@RequestParam(value = "areaOfBank") String areaOfBank,
			@RequestParam(value = "provinceCode") String provinceCode,
			@RequestParam(value = "cityCode") String cityCode,
			@RequestParam(value = "areaCode") String areaCode) throws Exception {
		LOG.info("LMT:orderCode------------------------：" + orderCode+","+provinceOfBank+","+cityOfBank+","+areaOfBank+","+provinceCode+","+cityCode+","+areaCode);
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String phoneC = prp.getCreditCardPhone();
		String amount = prp.getAmount();
		String cardNo = prp.getDebitCardNo();
		String bankName = prp.getDebitBankName();
		
		Map<String, Object> maps = new HashMap<>();
		
		maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		maps.put(CommonConstants.RESP_MESSAGE, "直接跳转交易页面");
		maps.put("redirect_url", ip + "/v1.0/paymentgateway/quick/lmt/pay-view?bankName=" + URLEncoder.encode(bankName, "UTF-8")
		+ "&bankCard=" + cardNo 
		+ "&orderCode=" + orderCode 
		+ "&ipAddress=" + ip 
		+ "&ips=" + prp.getIpAddress() 
		+ "&phone=" + phoneC
		+ "&provinceOfBank=" + URLEncoder.encode(provinceOfBank, "UTF-8")
		+ "&cityOfBank=" + URLEncoder.encode(cityOfBank, "UTF-8")
		+ "&areaOfBank=" + URLEncoder.encode(areaOfBank, "UTF-8")
		+ "&provinceCode=" + provinceCode
		+ "&cityCode=" + cityCode
		+ "&areaCode=" + areaCode
		+ "&amount=" + amount + "&isRegister=1");
		return maps;
	}
	
	/**
	 * 跳转到省市区选择页面
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/lmt/toHtml")
	public String toHtml(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
		LOG.info("lmtPay------------------跳转到省市区选择页面");

		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		
		String orderCode = request.getParameter("orderCode");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("orderCode", orderCode);
		model.addAttribute("ipAddress", ipAddress);

		return "lmtlinkage";
	}
	
	/**
	 * 跳转到交易界面
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/lmt/pay-view")
	public String toPay(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
		LOG.info("lmtPay------------------跳转到交易界面");

		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		//                                   
		String ordercode = request.getParameter("orderCode");
		String bankName = request.getParameter("bankName");
		String bankCard = request.getParameter("bankCard");
		String ipAddress = request.getParameter("ipAddress");
		String phone = request.getParameter("phone");
		String ips = request.getParameter("ips");
		String amount = request.getParameter("amount");
		String provinceOfBank = request.getParameter("provinceOfBank");
		String cityOfBank = request.getParameter("cityOfBank");
		String areaOfBank = request.getParameter("areaOfBank");
		String provinceCode = request.getParameter("provinceCode");
		String cityCode = request.getParameter("cityCode");
		String areaCode = request.getParameter("areaCode");
		
		model.addAttribute("ordercode", ordercode);
		model.addAttribute("bankName", bankName);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("ipAddress", ipAddress);
		model.addAttribute("phone", phone);
		model.addAttribute("ips", ips);
		model.addAttribute("amount", amount);
		model.addAttribute("provinceOfBank", provinceOfBank);
		model.addAttribute("cityOfBank", cityOfBank);
		model.addAttribute("areaOfBank", areaOfBank);
		model.addAttribute("provinceCode", provinceCode);
		model.addAttribute("cityCode", cityCode);
		model.addAttribute("areaCode", areaCode);
		
		return "lmtpay";
	}
	
	/**
	 * 跳转到交易成功中转页面
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/lmt/paysuccess-view")
	public String paysuccessView(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
		LOG.info("lmtPay------------------跳转到交易成功中转页面");

		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String ordercode = request.getParameter("orderCode");
		String bankName = request.getParameter("bankName");
		String bankCard = request.getParameter("bankCard");
		String ipAddress = request.getParameter("ipAddress");
		String amount = request.getParameter("amount");
		String ips = request.getParameter("ips");
		String realamount = request.getParameter("realamount");

		model.addAttribute("orderCode", ordercode);
		model.addAttribute("bankName", bankName);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("ipAddress", ipAddress);
		model.addAttribute("amount", amount);
		model.addAttribute("realAmount", realamount);
		model.addAttribute("ips", ips);

		return "lmtpaysuccess";
	}
	
	/**
	 * 根据市id查询该市所有的区
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method=RequestMethod.POST,value=("/v1.0/paymentgateway/lmt/area/queryall"))
	public @ResponseBody Object findArea(@RequestParam(value = "cityId") String cityId) {
		LOG.info("cityId------------------------：" + cityId);
		Map map = new HashMap();
		List<LMTAddress> list = topupPayChannelBusiness.findLMTCityByCityId(cityId);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, list);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}
	
	/**
	 * 根据省份id查询该省份所有的市
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method=RequestMethod.POST,value=("/v1.0/paymentgateway/lmt/city/queryall"))
	public @ResponseBody Object findCity(@RequestParam(value = "provinceId") String provinceId) {
		LOG.info("provinceid---------------------：" + provinceId);
		Map map = new HashMap();
		List<LMTAddress> list = topupPayChannelBusiness.findLMTCityByProvinceId(provinceId);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, list);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}
	
	/**
	 * 查询所有省/直辖市/自治区
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method=RequestMethod.POST,value=("/v1.0/paymentgateway/lmt/province/queryall"))
	public @ResponseBody Object findProvince() {
		Map map = new HashMap();
		List<LMTAddress> list = topupPayChannelBusiness.findLMTProvince();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, list);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}
	
	/**
	 * 生成签名
	 * 
	 * @param characterEncoding
	 * @param parameters
	 * 
	 * @param key
	 * @return
	 */
	public String createSign(String characterEncoding, SortedMap<Object, Object> parameters, String key) {
		StringBuffer sb = new StringBuffer();
		StringBuffer sbkey = new StringBuffer();
		Set es = parameters.entrySet(); // 所有参与传参的参数按照accsii排序（升序）
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			Object v = entry.getValue();
			// 空值不传递，不参与签名组串
			if (null != v && !"".equals(v)) {
				sb.append(k + "=" + v + "&");
				sbkey.append(k + "=" + v + "&");
			}
		}
		/* System.out.println("字符串:" + sb.toString()); */
		sbkey = sbkey.append("key=" + key);
		LOG.info("字符串拼接:" + sbkey.toString());
		// MD5加密,结果转换为大写字符
		String sign = MD5Util.digest(sbkey.toString(), characterEncoding).toUpperCase();
		LOG.info("MD5加密值:" + sign);
		return sign;
	}

	public void inputStreamToFile(InputStream ins, File file) {
		try {
			OutputStream os = new FileOutputStream(file);
			int bytesRead = 0;
			byte[] buffer = new byte[8192];
			while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			os.close();
			ins.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}