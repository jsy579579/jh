package com.jh.paymentgateway.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
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
import com.jh.paymentgateway.pojo.LMRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.RegisterPartsBuilderss;
import com.jh.paymentgateway.util.MD5Util;
import com.jh.paymentgateway.util.PhotoCompressUtil;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class LMpageRequest extends BaseChannel {
	private static final Logger LOG = LoggerFactory.getLogger(LMpageRequest.class);

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
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/lm/register")
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
		LMRegister LMRegister = topupPayChannelBusiness.getLMRegisterByidCard(idCard);

		if (LMRegister == null) {
			String requestNo = "XinLi" + System.currentTimeMillis();
			LOG.info("===register请求订单号：" + requestNo);

			/*RestTemplate restTemplate2 = new RestTemplate();
			MultiValueMap<String, String> requestEntity2 = new
			LinkedMultiValueMap<String, String>(); String URL = null; String
			results2 = null; URL = rip + "/v1.0/user/getPicture";
			requestEntity2.add("phone", phoneD); try { results2 =
			restTemplate2.postForObject(URL, requestEntity2, String.class);
			LOG.info("*******************获取用户图片***********************"); }
			catch (Exception e) { e.printStackTrace();
			LOG.error(ExceptionUtil.errInfo(e)); } JSONObject json =
			JSONObject.fromObject(results2); List<String> fliestr =
			(List<String>) json.get("result"); File file = null; List<File>
			filelist = new ArrayList<>(); JSONArray jsonarry =
			JSONArray.fromObject(fliestr); for (int i = 0; i <
			jsonarry.size(); i++) { String base64Byte =
			jsonarry.getString(i); LOG.info("========下标" + i + "图片文件byte流");
			byte[] buffer = Base64.getDecoder().decode(base64Byte); try {
			String str = ""; str = str + (char) (Math.random() * 26 + 'A' +
			i); String zipPath = "/" + phoneD + str + ".jpg"; file = new
			File(zipPath); if (file.exists()) { file.delete(); } OutputStream
			output = new FileOutputStream(file); BufferedOutputStream
			bufferedOutput = new BufferedOutputStream(output);
			bufferedOutput.write(buffer); bufferedOutput.close();
			output.close(); PhotoCompressUtil.compressPhoto(zipPath, zipPath,0.01f); filelist.add(file); } catch (Exception e) {
			  LOG.info("=======================读取文件流异常"); } }*/


			// 获取银行联行号
			BankNumCode bcode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
			String bankChannelNo = bcode.getBankBranchcode();// 支行号

			SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
			parameters.put("mch_id", mch_id);
			parameters.put("app_id", app_id);
			parameters.put("mch_no", requestNo);
			parameters.put("realname", userName);
			parameters.put("bank_card_no", cardNo);
			parameters.put("bank_card_mobile", phoneD);
			parameters.put("bank_lines", bankChannelNo);
			parameters.put("id_card", idCard);
			String mySign = createSign(characterEncoding, parameters, key);
			LOG.info("=======================设置签名" + mySign);
			Part[] parts = null;
			try {
				parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(requestNo)
						.setSign(mySign).setRealname(userName).setCardNo(cardNo).setPhone(phoneD)
						.setBankLines(bankChannelNo).setidCard(idCard).generateParams();
			} catch (Exception e) {
				LOG.info("=======================设置签名出错");
			}

			PostMethod postMethod = new PostMethod(
					"https://pay.longmaoguanjia.com/api/pay/quick_pay_b/sub_merchant_register");
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
					LMRegister lm = new LMRegister();
					lm.setBankCard(cardNo);
					lm.setMainCustomerNum(mch_id);
					lm.setCustomerNum(merchantNo);
					lm.setIdCard(idCard);
					lm.setUserName(userName);
					lm.setPhone(phoneD);
					lm.setStatus("0");
					topupPayChannelBusiness.createLMRegister(lm);
					maps = (Map<String, Object>) openRate(orderCode, rate, extraFee, rip, idCard);
					if ("000000".equals(maps.get("resp_code"))) {
//						RestTemplate restTemplate = new RestTemplate();
//						MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
//
//						// 关闭订单
//						this.addOrderCauseOfFailure(orderCode, "新用户审核资质!", rip);
//						restTemplate = new RestTemplate();
//						URL = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";
//						/** 修改订单状态为已关闭 */
//						requestEntity = new LinkedMultiValueMap<String, String>();
//						requestEntity.add("status", "2");
//						requestEntity.add("third_code", "");
//						requestEntity.add("order_code", orderCode);
//						LOG.info("接口/v1.0/transactionclear/payment/update--参数================" + orderCode + "," + "");
//						String result = restTemplate.postForObject(URL, requestEntity, String.class);
//						LOG.info("接口/v1.0/transactionclear/payment/update--RESULT================" + result);

//						boolean picUpload = picUpload(idCard,filelist,orderCode,rip);
//						if (picUpload) {
							maps = (Map<String, Object>) pay(orderCode, bankCard, rip, idCard, amount, phoneC, creditCardName, rate,
									extraFee, userName);
							
//						}else{
//							maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
//							maps.put(CommonConstants.RESP_MESSAGE, respMessage);
//							this.addOrderCauseOfFailure(orderCode,
//									"上传图片: 失败"+ "[" + requestNo + "]", rip);
							return maps;
//						}
					} else {
						maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						maps.put(CommonConstants.RESP_MESSAGE, respMessage);
						this.addOrderCauseOfFailure(orderCode,
								"开通产品:" + maps.get("resp_message") + "[" + requestNo + "]", rip);
						return maps;
					}
				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, respMessage);
					this.addOrderCauseOfFailure(orderCode, "进件:" + respMessage + "[" + requestNo + "]", rip);
					return maps;
				}
			}
		}
		if (LMRegister.getRate() == null | LMRegister.getExtraFee() == null) {
			maps = (Map<String, Object>) openRate(orderCode, rate, extraFee, rip, idCard);
			if ("999999".equals(maps.get("resp_code"))) {
				return maps;

			}
		}
		if (!rate.equals(LMRegister.getRate()) | !extraFee.equals(LMRegister.getExtraFee())
				| !cardNo.equals(LMRegister.getBankCard())) {
			maps = (Map<String, Object>) setMerchant(userName, phoneD, rate, extraFee, orderCode, bankName, cardNo, rip,
					idCard);
			if ("999999".equals(maps.get("resp_code"))) {
				return maps;

			}
		}
		maps = (Map<String, Object>) pay(orderCode, bankCard, rip, idCard, amount, phoneC, creditCardName, rate,
				extraFee, userName);
		return maps;
	}

	/**
	 * 设置费率
	 * 
	 * @param orderCode
	 * @param rate
	 * @param extraFee
	 * @param rip
	 * @param idCard
	 * @return
	 * @throws IOException
	 */
	public Object openRate(String orderCode, String rate, String extraFee, String rip, String idCard)
			throws IOException {
		Map<String, Object> maps = new HashMap<>();
		LMRegister lm = topupPayChannelBusiness.getLMRegisterByidCard(idCard);
		// 转换分制
		String exFee = new BigDecimal(extraFee).multiply(new BigDecimal(100)).toString();
		String requestNo = "xinli" + System.currentTimeMillis();
		LOG.info("===setRate请求订单号：" + requestNo);
		SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
		parameters.put("mch_id", mch_id);
		parameters.put("app_id", app_id);
		parameters.put("mch_no", requestNo);
		parameters.put("sub_mch_rate", rate);
		parameters.put("single_price", exFee);
		parameters.put("sub_mch_no", lm.getCustomerNum());
		String mySign = createSign(characterEncoding, parameters, key);
		LOG.info("====setRate签名报文：" + mySign.toString());
		Part[] parts = null;
		parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(requestNo)
				.setSubMchRate(rate).setSinglePrice(exFee).setSubM(lm.getCustomerNum()).setSign(mySign)
				.generateParams();

		PostMethod postMethod = new PostMethod(
				"https://pay.longmaoguanjia.com/api/pay/quick_pay_b/sub_merchant_open_firm");
		HttpClient client = new HttpClient();
		postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

		int status = client.executeMethod(postMethod);
		LOG.info("==========status==========" + status);

		if (status == HttpStatus.SC_OK) {
			String backinfo = postMethod.getResponseBodyAsString();
			LOG.info("==========setRate返回响应==========" + backinfo);
			JSONObject jsonstr = JSONObject.fromObject(backinfo);
			String respCode = jsonstr.getString("code");
			String respMessage = jsonstr.getString("message");
			if (respCode.equals("0")) {
				lm.setExtraFee(extraFee);
				lm.setRate(rate);
				lm.setChangeTime(new Date());
				topupPayChannelBusiness.createLMRegister(lm);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				this.addOrderCauseOfFailure(orderCode, "设置费率:" + respMessage + "[" + requestNo + "]", rip);
				return maps;
			}
		}
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
	public Object setMerchant(String userName, String phoneD, String rate, String extraFee, String orderCode,
			String bankName, String cardNo, String rip, String idCard) throws IOException {
		LMRegister lm = topupPayChannelBusiness.getLMRegisterByidCard(idCard);
		Map<String, Object> maps = new HashMap<>();
		String requestNo = "xinli" + System.currentTimeMillis();
		LOG.info("===setMerchant请求订单号：" + requestNo);
		SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
		parameters.put("mch_id", mch_id);
		parameters.put("app_id", app_id);
		parameters.put("mch_no", requestNo);
		parameters.put("sub_mch_no", lm.getCustomerNum());
		parameters.put("realname", userName);

		// 获取缩写
		LMBankNum sbcode = topupPayChannelBusiness.getLMBankNumCodeByBankName(bankName);
		String bankAbbr = sbcode.getBankNum();// 缩写
		parameters.put("bank_code", bankAbbr);

		parameters.put("bank_card_no", cardNo);
		parameters.put("bank_card_mobile", phoneD);

		// 获取银行联行号
		BankNumCode bcode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
		String bankChannelNo = bcode.getBankBranchcode();// 支行号
		parameters.put("bank_lines", bankChannelNo);

		parameters.put("sub_mch_rate", rate);
		String ExtraFee = new BigDecimal(lm.getExtraFee()).multiply(new BigDecimal(100)).toString();
		parameters.put("single_price", ExtraFee);
		String mySign = createSign(characterEncoding, parameters, key);
		LOG.info("====setMerchant签名报文：" + mySign.toString());
		Part[] parts = null;
		parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(requestNo)
				.setSubM(lm.getCustomerNum()).setRealname(userName).setBankChannelNo(bankAbbr).setCardNo(cardNo)
				.setPhone(phoneD).setBankLines(bankChannelNo).setSubMchRate(rate).setSinglePrice(ExtraFee)
				.setSign(mySign).generateParams();
		PostMethod postMethod = new PostMethod("https://pay.longmaoguanjia.com/api/pay/quick_pay_b/sub_merchant_fee");
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
				lm.setBankCard(cardNo);
				lm.setRate(rate);
				lm.setExtraFee(extraFee);
				lm.setChangeTime(new Date());
				topupPayChannelBusiness.createLMRegister(lm);
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
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/lm/queryPayStatus")
	public @ResponseBody Object queryPutPay(@RequestParam(value = "orderCode") String orderCode) throws IOException {
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String idCard = prp.getIdCard();
		LMRegister lm = topupPayChannelBusiness.getLMRegisterByidCard(idCard);
		Map<String, Object> maps = new HashMap<>();
		SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();

		parameters.put("mch_id", mch_id);
		parameters.put("app_id", app_id);
		parameters.put("mch_no", orderCode);
		String mySign = createSign(characterEncoding, parameters, key);
		LOG.info("====pay签名报文：" + mySign.toString());
		Part[] parts = null;
		parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(orderCode).setSign(mySign)
				.generateParams();

		PostMethod postMethod = new PostMethod("https://pay.longmaoguanjia.com/api/pay/quick_pay_b/query");
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
			if (respCode.equals("0")) {
				String data = jsonstr.getString("data");
				JSONObject datastr = JSONObject.fromObject(data);
				String tradeStatus = datastr.getString("trade_status");
				String withdrawStatus = datastr.getString("withdraw_status");
				LOG.info("订单号：" + orderCode + ",交易状态(0：成功 1：处理中 2：失败)：" + tradeStatus + ",提现状态(0：成功 1：处理中 2：失败):"
						+ withdrawStatus);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				this.addOrderCauseOfFailure(orderCode, "交易:" + respMessage + "[" + orderCode + "]", prp.getIpAddress());
				return maps;
			}
		}
		return maps;
	}

	/**
	 * 交易
	 */
	public Object pay(String orderCode, String bankCard, String rip, String idCard, String amount, String phoneC,
			String creditCardName, String rate, String extraFee, String userName) throws IOException {
		
		Map<String, Object> maps = new HashMap<>();
		LMRegister lm = topupPayChannelBusiness.getLMRegisterByidCard(idCard);
		String lmStatus = lm.getStatus();
		if (lmStatus.equals("1")) {
			LOG.info("====================lm商户资质已经是1====================");
			SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
			
			String Amount = new BigDecimal(amount).multiply(new BigDecimal(100)).toString();
			parameters.put("mch_id", mch_id);
			parameters.put("app_id", app_id);
			parameters.put("mch_no", orderCode);
			parameters.put("sub_mch_no", lm.getCustomerNum());
	
			// 获取银行联行号
			LMBankNum bcode = topupPayChannelBusiness.getLMBankNumCodeByBankName(creditCardName);
			String bankAbbr = bcode.getBankNum();// 缩写
			parameters.put("bank_code", bankAbbr);
	
			parameters.put("bank_card_no", bankCard);
			parameters.put("bank_card_mobile", phoneC);
			parameters.put("amount", Amount);// 单位分
			parameters.put("sub_mch_rate", rate);
	
			String ExtraFee = new BigDecimal(lm.getExtraFee()).multiply(new BigDecimal(100)).toString();
			parameters.put("single_price", ExtraFee);
	
			parameters.put("realname", userName);
			parameters.put("id_card", idCard);
	
			String returnUrl = ip + "/v1.0/paymentgateway/topup/lm/notify_call";
			parameters.put("notify_url", returnUrl);
			String mySign = createSign(characterEncoding, parameters, key);
			LOG.info("====pay签名报文：" + mySign.toString());
			Part[] parts = null;
			parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(orderCode)
					.setSubM(lm.getCustomerNum()).setBankChannelNo(bankAbbr).setCardNo(bankCard).setPhone(phoneC)
					.setAmount(Amount).setSubMchRate(rate).setSinglePrice(ExtraFee).setRealname(userName).setidCard(idCard)
					.setSign(mySign).setNotifyUrl(returnUrl).generateParams();
	
			PostMethod postMethod = new PostMethod("https://pay.longmaoguanjia.com/api/pay/quick_pay_b/trade");
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
				if (respCode.equals("0")) {
					String data = jsonstr.getString("data");
					JSONObject datastr = JSONObject.fromObject(data);
					String payUrl = "";
					String payHtml = "";
					payUrl = datastr.getString("pay_url");
					// if (payHtml == null | payHtml.equals("")) {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, respMessage);
					maps.put(CommonConstants.RESULT, payUrl);
					return maps;
					// }
					/*
					 * } else { 找出指定的2个字符在 该字符串里面的 位置 String strStart = "<form";
					 * String strEnd = "</body>"; int strStartIndex =
					 * payHtml.indexOf(strStart); int strEndIndex =
					 * payHtml.indexOf(strEnd); String result =
					 * payHtml.substring(strStartIndex, strEndIndex);
					 * LOG.info("截取后的form开始--------------------------");
					 * System.out.println(result);
					 * LOG.info("截取后的form结束--------------------------");
					 * 
					 * maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					 * maps.put(CommonConstants.RESP_MESSAGE, respMessage);
					 * maps.put("pageContent", result); return maps; }
					 */
				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, respMessage);
					this.addOrderCauseOfFailure(orderCode, "请求交易:" + respMessage + "[" + orderCode + "]", rip);
					return maps;
				}
			}else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "请求超时");
				this.addOrderCauseOfFailure(orderCode, "请求交易: 请求超时[" + orderCode + "]", rip);
				return maps;
			}
		}else{
			LOG.info("====================lm查询商户资质====================");
			maps = (Map<String, Object>) this.queryMerchant(idCard);
			if (maps.get("resp_code").equals("000000")) {

				SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
		
				String Amount = new BigDecimal(amount).multiply(new BigDecimal(100)).toString();
				parameters.put("mch_id", mch_id);
				parameters.put("app_id", app_id);
				parameters.put("mch_no", orderCode);
				parameters.put("sub_mch_no", lm.getCustomerNum());
		
				// 获取银行联行号
				LMBankNum bcode = topupPayChannelBusiness.getLMBankNumCodeByBankName(creditCardName);
				String bankAbbr = bcode.getBankNum();// 缩写
				parameters.put("bank_code", bankAbbr);
		
				parameters.put("bank_card_no", bankCard);
				parameters.put("bank_card_mobile", phoneC);
				parameters.put("amount", Amount);// 单位分
				parameters.put("sub_mch_rate", rate);
		
				String ExtraFee = new BigDecimal(lm.getExtraFee()).multiply(new BigDecimal(100)).toString();
				parameters.put("single_price", ExtraFee);
		
				parameters.put("realname", userName);
				parameters.put("id_card", idCard);
		
				String returnUrl = ip + "/v1.0/paymentgateway/topup/lm/notify_call";
				parameters.put("notify_url", returnUrl);
				String mySign = createSign(characterEncoding, parameters, key);
				LOG.info("====pay签名报文：" + mySign.toString());
				Part[] parts = null;
				parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(orderCode)
						.setSubM(lm.getCustomerNum()).setBankChannelNo(bankAbbr).setCardNo(bankCard).setPhone(phoneC)
						.setAmount(Amount).setSubMchRate(rate).setSinglePrice(ExtraFee).setRealname(userName).setidCard(idCard)
						.setSign(mySign).setNotifyUrl(returnUrl).generateParams();
		
				PostMethod postMethod = new PostMethod("https://pay.longmaoguanjia.com/api/pay/quick_pay_b/trade");
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
					if (respCode.equals("0")) {
						String data = jsonstr.getString("data");
						JSONObject datastr = JSONObject.fromObject(data);
						String payUrl = "";
						String payHtml = "";
						payUrl = datastr.getString("pay_url");
						// if (payHtml == null | payHtml.equals("")) {
						maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						maps.put(CommonConstants.RESP_MESSAGE, respMessage);
						maps.put(CommonConstants.RESULT, payUrl);
						return maps;
						// }
						/*
						 * } else { 找出指定的2个字符在 该字符串里面的 位置 String strStart = "<form";
						 * String strEnd = "</body>"; int strStartIndex =
						 * payHtml.indexOf(strStart); int strEndIndex =
						 * payHtml.indexOf(strEnd); String result =
						 * payHtml.substring(strStartIndex, strEndIndex);
						 * LOG.info("截取后的form开始--------------------------");
						 * System.out.println(result);
						 * LOG.info("截取后的form结束--------------------------");
						 * 
						 * maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						 * maps.put(CommonConstants.RESP_MESSAGE, respMessage);
						 * maps.put("pageContent", result); return maps; }
						 */
					} else {
						maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						maps.put(CommonConstants.RESP_MESSAGE, respMessage);
						this.addOrderCauseOfFailure(orderCode, "请求交易:" + respMessage + "[" + orderCode + "]", rip);
						return maps;
					}
				}else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, "请求超时");
					this.addOrderCauseOfFailure(orderCode, "请求交易: 请求超时[" + orderCode + "]", rip);
					return maps;
				}
			}else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, maps.get("resp_message"));
				this.addOrderCauseOfFailure(orderCode, "请求交易:" + maps.get("resp_message") + "[" + orderCode + "]", rip);
				return maps;
			}
		}
	}

	/**
	 * 上传用户认证图片
	 * 
	 * @param filelist
	 */
	public boolean picUpload(String idCard, List<File> filelist, String orderCode, String rip)
			throws IOException {
		boolean istrue = false;

		String requestNo = "xinli" + System.currentTimeMillis();
		LOG.info("===上传图片请求订单号：" + requestNo);

		LMRegister lm = topupPayChannelBusiness.getLMRegisterByidCard(idCard);
		Map<String, Object> maps = new HashMap<>();
		SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
		parameters.put("mch_id", mch_id);
		parameters.put("app_id", app_id);
		parameters.put("mch_no", requestNo);
		parameters.put("sub_mch_no", lm.getCustomerNum());
		String mySign = null;
		InputStream inputStream = null;
		byte[] pic1 = null;
		sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
		String img1 = null;
		// 上传身份证正面照
		parameters.put("type", "1");
		mySign = createSign(characterEncoding, parameters, key);
		// 加密
		try {
			inputStream = new FileInputStream(filelist.get(0));
			pic1 = new byte[inputStream.available()];
			inputStream.read(pic1);
			inputStream.close();
		} catch (IOException e) {
					e.printStackTrace();
		}
		img1 = encoder.encode(pic1);
		parameters.put("base64_data", "data:image/jpeg;base64," + img1);
		LOG.info("====picUpload签名报文：" + mySign.toString());
		Part[] parts = null;

		try {
			parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(requestNo)
					.setSubM(lm.getCustomerNum()).setType("1").setBase64Data("data:image/jpeg;base64," + img1)
					.setSign(mySign).generateParams();
		} finally {
			for (File filedel : filelist) {
				filedel.delete();
			}
		}

		PostMethod postMethod = new PostMethod(
				"https://pay.longmaoguanjia.com/api/pay/quick_pay_b/sub_merchant_upload_pic");
		HttpClient client = new HttpClient();
		postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
		int status = client.executeMethod(postMethod);
		LOG.info("==========status==========" + status);
		if (status == HttpStatus.SC_OK) {
			String backinfo = postMethod.getResponseBodyAsString();
			LOG.info("==========picUpload返回响应==========" + backinfo);
			JSONObject jsonstr = JSONObject.fromObject(backinfo);
			String respCode = jsonstr.getString("code");
			String respMessage = jsonstr.getString("message");
			if (respCode.equals("0")) {
				String data = jsonstr.getString("data");
				JSONObject datastr = JSONObject.fromObject(data);
				String sub_mch_no = datastr.getString("sub_mch_no");
				String upload_status = datastr.getString("upload_status");
				if (upload_status.equals("0")) {
					maps.put(CommonConstants.RESP_CODE, ip + "/v1.0/paymentgateway/quick/lm/auditing?ipAddress="+ip);
					maps.put(CommonConstants.RESP_MESSAGE, respMessage);
					maps.put(CommonConstants.RESULT, "资质审核照片上传成功");
					istrue = true;
				} else if (upload_status.equals("1")) {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, respMessage);
					maps.put(CommonConstants.RESULT, "资质审核照片上传中");
				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, respMessage);
					maps.put(CommonConstants.RESULT, "资质审核照片上传失败");
				}
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				this.addOrderCauseOfFailure(orderCode, "上传图片:" + respMessage + "[" + orderCode + "]", rip);
			}
		}
		return istrue;
	}

	/**
	 * 回调
	 * 
	 * @param req
	 * @param res
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/lm/notify_call")
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

	/**
	 * 跳转
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/lmjump/top/xinli")
	public @ResponseBody Object file() throws IOException {
		System.out.println("===============" + ip.toString());
		Map<String, Object> maps = new HashMap<String, Object>();
		maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		maps.put(CommonConstants.RESP_MESSAGE, "成功");
		maps.put(CommonConstants.RESULT, ip + "/v1.0/paymentgateway/quick/lmjump/fill?ip=" + ip + "&key="
				+ "dcddf093acb42eed08d077a3d433b03d" + "&mainCustomerNumber=1514312323");
		return maps;
	}

	/**
	 * 跳转到
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/lmjump/fill")
	public String toPay(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
		System.out.println("LM审核资质又进来了");
		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String ip = request.getParameter("ip");
		String key = request.getParameter("key");
		String mainCustomerNumber = request.getParameter("mainCustomerNumber");

		model.addAttribute("ips", ip);
		model.addAttribute("key", key);
		model.addAttribute("mainCustomerNumber", mainCustomerNumber);
		System.out.println("==============" + ip.toString());
		return "lmupload";
	}

	/**
	 * 查询子商户资质
	 * 
	 * @param idCard
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/lm/queryMerchant")
	public @ResponseBody Object queryMerchant(String idCard) throws IOException {
		LMRegister lm = topupPayChannelBusiness.getLMRegisterByidCard(idCard);
		Map<String, Object> maps = new HashMap<>();
		SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
		String requestNo = "xinli" + System.currentTimeMillis();
		LOG.info("===查询商户资质请求订单号：" + requestNo);
		parameters.put("mch_id", mch_id);
		parameters.put("app_id", app_id);
		parameters.put("mch_no", requestNo);
		parameters.put("sub_mch_no", lm.getCustomerNum());
		String mySign = createSign(characterEncoding, parameters, key);
		LOG.info("====queryMerchant签名报文：" + mySign.toString());
		Part[] parts = null;
		parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(requestNo)
				.setSubM(lm.getCustomerNum()).setSign(mySign).generateParams();
		PostMethod postMethod = new PostMethod("https://pay.longmaoguanjia.com/api/pay/quick_pay_b/sub_merchant_query");
		HttpClient client = new HttpClient();
		postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

		int status = client.executeMethod(postMethod);
		LOG.info("==========status==========" + status);
		if (status == HttpStatus.SC_OK) {
			String backinfo = postMethod.getResponseBodyAsString();
			LOG.info("==========queryMerchant返回响应==========" + backinfo);
			JSONObject jsonstr = JSONObject.fromObject(backinfo);
			String respCode = jsonstr.getString("code");
			String respMessage = jsonstr.getString("message");
			if (respCode.equals("10302")) {// 抱歉，未查到此商户信息!
				maps.put(CommonConstants.RESP_CODE, respCode);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			}
			if (respCode.equals("0")) {// 查询成功
				String data = jsonstr.getString("data");
				JSONObject jsonstr1 = JSONObject.fromObject(data);
				String customerNumber = jsonstr1.getString("sub_mch_no");
				String uploadStatus = jsonstr1.getString("upload_status");// 上传状态
																			// 0:全部上传
																			// 1:未上传
																			// 2:部分上传
																			// 3:证件非法
//				LOG.info("=======================" + uploadStatus + "上传状态：0:全部上传1:未上传 2:部分上传 3:证件非法");
				try {
					String picStatus = jsonstr1.getString("status");// 商户状态 0:正常
																	// 1:停用 2:注销
																	// 3:未认证
																	// 4:审核中
																	// 5:审核失败
																	// 6:修改失败
					LOG.info("=======================" + customerNumber + "，审核资质状态：" + picStatus
							+ "--->>商户状态 0:正常1:停用 2:注销3:未认证 4:审核中5:审核失败6:修改失败");
					if (picStatus.equals("0") /*&& uploadStatus.equals("2")*/) {
						if (!lm.getStatus().equals("1")) {
							lm.setStatus("1");
							lm.setChangeTime(new Date());
							topupPayChannelBusiness.createLMRegister(lm);
						}
						maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						maps.put(CommonConstants.RESP_MESSAGE, respMessage);
					} else {
						maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						maps.put(CommonConstants.RESP_MESSAGE, "资质查询：[" + requestNo + "]" + picStatus);
						return maps;
					}
				} catch (Exception e) {
					lm.setStatus("3");
					lm.setChangeTime(new Date());
					topupPayChannelBusiness.createLMRegister(lm);
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, "资质查询：[" + requestNo + "]" + "亲,商户资质审核中,请稍等2分钟");
					return maps;
				}
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "资质查询：[" + requestNo + "]" + "系统异常");
				return maps;
			}

		}
		return maps;
	}

	/**
	 * 补充资质
	 * 
	 * @param request
	 * @param positiveURL
	 * @param positiveFile
	 * @param key
	 * @param mainCustomerNumber
	 * @param idCard
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/lm/changeAuditing")
	public @ResponseBody Object findByBankName(HttpServletRequest request,
			@RequestParam(value = "positiveURL") String positiveURL, // 正面路径
			@RequestParam(value = "positiveFile") MultipartFile positiveFile, // 正面图片文件
		/*	@RequestParam(value = "oppositeURL") String oppositeURL, // 反面路径
			@RequestParam(value = "oppositeFile") MultipartFile oppositeFile, // 反面图片文件*/	
		@RequestParam(value = "mainCustomerNumber", required = false, defaultValue = "1514312323") String mainCustomerNumber,
			@RequestParam(value = "idCard") String idCard)// 身份证号
					throws Exception {

		LMRegister lm = topupPayChannelBusiness.getLMRegisterByidCard(idCard);
		Map<String, Object> maps = new HashMap<>();
		String requestNo = "xinli" + System.currentTimeMillis();
		LOG.info("===补充正面资质请求订单号：" + requestNo);
		File file = null;
		try {
			String zipPath = "/" + idCard + "A.jpg";
			// 图片转类型，压缩
			InputStream ins = positiveFile.getInputStream();
			file = new File(zipPath);
			inputStreamToFile(ins, file);
			PhotoCompressUtil.compressPhoto(new FileInputStream(file), file, 0.1f);

			sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
			FileInputStream inputStream = new FileInputStream(file);
			byte[] pic1 = new byte[inputStream.available()];
			inputStream.read(pic1);
			inputStream.close();
			String img1 = encoder.encode(pic1);

			SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
			parameters.put("mch_id", mch_id);
			parameters.put("app_id", app_id);
			parameters.put("mch_no", requestNo);
			parameters.put("sub_mch_no", lm.getCustomerNum());
			parameters.put("type", "1");
			String mySign = createSign(characterEncoding, parameters, key);
			LOG.info("====update签名报文：" + mySign.toString());
			parameters.put("base64_data", img1);
			Part[] parts = null;
			// 正面照片
			parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(requestNo)
					.setSubM(lm.getCustomerNum()).setType("1").setBase64Data(img1).setSign(mySign).generateParams();
			PostMethod postMethod = new PostMethod(
					"https://pay.longmaoguanjia.com/api/pay/quick_pay_b/sub_merchant_upload_pic");
			HttpClient client = new HttpClient();
			postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

			int status = client.executeMethod(postMethod);
			LOG.info("==========status==========" + status);
			String respMessage = null;
			if (status == HttpStatus.SC_OK) {
				String backinf = postMethod.getResponseBodyAsString();
				LOG.info("==========update返回响应==========" + backinf);
				JSONObject jsonstr = JSONObject.fromObject(backinf);
				String respCode = jsonstr.getString("code");
				respMessage = jsonstr.getString("message");
				if (respCode.equals("0")) {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, respMessage);
					return maps;
				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, respMessage);
					return maps;
				}
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "请求上传正面图片失败");
				return maps;
			}
		} finally {
			file.delete();
		}
	}

	/**
	 * 跳转审核资质中页面
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/lm/auditing")
	public String auditing(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		LOG.info("lm------------------跳转到审核资质中页面");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("ipAddress", ipAddress);

		return "ybnauditing";
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