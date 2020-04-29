package com.jh.paymentgateway.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
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
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.YBQuickRegister;
import com.jh.paymentgateway.util.yb.AESUtil;
import com.jh.paymentgateway.util.yb.Conts;
import com.jh.paymentgateway.util.yb.Digest;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import cn.jh.common.utils.PhotoCompressUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class YBnewpageRequest extends BaseChannel {

	// private static String key = Conts.hmacKey; // 商户秘钥

	private static String key = Conts.hmacKey;

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	private static final Logger LOG = LoggerFactory.getLogger(YBnewpageRequest.class);

	/**
	 * 商户入网
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ybn/register")
	public @ResponseBody Object register(@RequestParam(value = "orderCode") String orderCode) throws Exception {

		LOG.info("============ 进入商户入网接口 ============");

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		Map<String, String> maps = new HashMap<String, String>();

		String cardName = prp.getDebitBankName();
		String userName = prp.getUserName();
		String phoneD = prp.getDebitPhone();
		String rate = prp.getRate();
		String idCard = prp.getIdCard();
		String cardNo = prp.getDebitCardNo();
		String bankCard = prp.getBankCard();
		String ExtraFee = prp.getExtraFee();
		String rip = prp.getIpAddress();
		String phoneC = prp.getCreditCardPhone();
		String amount = prp.getAmount();

		String url = prp.getIpAddress() + "/v1.0/user/find/by/userid";
		RestTemplate restTemplate = new RestTemplate();
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("userId", prp.getUserId());
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================" + result);
		String brandid = "";
		try {
			JSONObject jsonObject = JSONObject.fromObject(result);
			JSONObject resultObj = jsonObject.getJSONObject("result");
			brandid = resultObj.getString("brandId");
		} catch (Exception e) {
			LOG.error("查询订单信息失败啦=======");
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "失败");
			maps.put(CommonConstants.RESULT, "查询订单信息有误");

		}

		String notifyurl = ipAddress + "/v1.0/paymentgateway/topup/ybn/notify_call";
		String returnurl = "http://1.xinli2017.applinzi.com/login/back.html";

		if (ExtraFee.contains("."))
			ExtraFee = ExtraFee.substring(0, ExtraFee.indexOf("."));

		if ("平安银行".equals(cardName)) {
			cardName = "深圳发展银行";
		}
		if ("广发银行".equals(cardName)) {
			cardName = "广发银行股份有限公司";
		}
		if ("浦发银行".equals(cardName) || "上海浦东发展银行".equals(cardName) || "浦发银行信用卡中心(63100000)".equals(cardName)) {
			cardName = "浦东发展银行";
		}
		if ("邮政储蓄银行".equals(cardName) || "中国邮政储蓄银行信用卡中心(61000000)".equals(cardName)) {
			cardName = "邮储银行";
		}
		if ("广州银行股份有限公司(64135810)".equals(cardName) || "广州银行".equals(cardName)) {
			cardName = "广州银行股份有限公司";
		}

		String areano = null;

		YBQuickRegister ybQuickRegister = topupPayChannelBusiness.getYBQuickRegisterByidCard(idCard);

		String rateCopy = rate;

		String mainCustomerNumber = Conts.customerNumber; // 代理商编码
		String requestId = UUID.randomUUID().toString().substring(0, 15); // 注册请求号，每次请求唯一
		String customertype = "PERSON";// 企业-ENTERPRISE,个体工商户-INDIVIDUAL
		// 个人-PERSON
		String bindmobile = phoneC;
		String signedname = userName;
		String linkman = "复归";
		String legalperson = userName;// 法人
		String minsettleamount = "1";
		String riskreserveday = "0";
		String bankaccountnumber = cardNo;
		String bankname = cardName;
		String accountname = userName;
		if (null == areano || "null".equals(areano) || "".equals(areano))
			areano = "1111";
		String areaCode = areano;

		if (ybQuickRegister == null) {
			LOG.info("用户需要进件======");

			RestTemplate restTemplate2 = new RestTemplate();
			MultiValueMap<String, String> requestEntity2 = new LinkedMultiValueMap<String, String>();
			String URL = null;
			String results2 = null;
			URL = prp.getIpAddress() + "/v1.0/user/getPicture";
			requestEntity2.add("phone", phoneD);
			try {
				results2 = restTemplate2.postForObject(URL, requestEntity2, String.class);
				LOG.info("*******************获取用户图片***********************");
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("",e);
			}
			JSONObject json = JSONObject.fromObject(results2);
			List<Object> fliestr = (List<Object>) json.get("result");
			File file = null;
			List<File> filelist = new ArrayList<>();
			JSONArray jsonarry = JSONArray.fromObject(fliestr);
			for (int i = 0; i < jsonarry.size(); i++) {
				String base64Byte = jsonarry.getString(i);
				LOG.info("========下标" + i + "图片文件byte流");
				byte[] buffer = Base64.getDecoder().decode(base64Byte);
				try {
					String str = "";
					str = str + (char) (Math.random() * 26 + 'A');
					String zipPath = "/" + phoneD + str + ".jpg";
					file = new File(zipPath);
					if (file.exists()) {
						file.delete();
					}
					OutputStream output = new FileOutputStream(file);
					BufferedOutputStream bufferedOutput = new BufferedOutputStream(output);
					bufferedOutput.write(buffer);
					bufferedOutput.close();
					output.close();
					
					PhotoCompressUtil.compressPhoto(zipPath, zipPath, 0.1f);

					filelist.add(file);
					
				} catch (Exception e) {
					LOG.info("=======================读取文件流异常");
				}
			}

			PostMethod postMethod = new PostMethod(Conts.baseRequestUrl + "/register.action");
			HttpClient client = new HttpClient();
			String manualSettle = "Y";
			try {
				StringBuffer signature = new StringBuffer();
				signature.append(mainCustomerNumber == null ? "" : mainCustomerNumber)
						.append(requestId == null ? "" : requestId).append(customertype == null ? "" : customertype)
						.append(bindmobile == null ? "" : bindmobile).append(signedname == null ? "" : signedname)
						.append(linkman == null ? "" : linkman).append(idCard == null ? "" : idCard)
						.append(legalperson == null ? "" : legalperson)
						.append(minsettleamount == null ? "" : minsettleamount)
						.append(riskreserveday == null ? "" : riskreserveday)
						.append(bankaccountnumber == null ? "" : bankaccountnumber)
						.append(bankname == null ? "" : bankname).append(accountname == null ? "" : accountname)
						.append(areaCode == null ? "" : areaCode).append(manualSettle == null ? "" : manualSettle);

				LOG.info("signature=======================" + signature.toString());
				String hmac = Digest.hmacSign(signature.toString(), key);
				LOG.info("hmac=======================" + hmac);
				Part[] parts = null;
				if (filelist.size() > 0 && filelist.size() < 2) {
					parts = new RegisterPartsBuilderss().setMainCustomerNumber(mainCustomerNumber)
							.setRequestId(requestId).setCustomerType(customertype).setBindMobile(bindmobile)
							.setSignedName(signedname).setLinkMan(linkman).setIdCard(idCard).setLegalPerson(legalperson)
							.setMinSettleAmount(minsettleamount).setRiskReserveDay(riskreserveday)
							.setBankAccountNumber(bankaccountnumber).setBankName(bankname).setAccountName(accountname)
							.setAreaCode(areaCode).setManualSettle(manualSettle).setBankCardPhoto(filelist.get(0))
							.setIdCardBackPhoto(filelist.get(0)).setIdCardPhoto(filelist.get(0))
							.setPersonPhoto(filelist.get(0)).setHmac(hmac).generateParams();
				}
				if (filelist.size() > 0 && filelist.size() < 4) {
					if (filelist.size() > 0 && filelist.size() < 3) {
						parts = new RegisterPartsBuilderss().setMainCustomerNumber(mainCustomerNumber)
								.setRequestId(requestId).setCustomerType(customertype).setBindMobile(bindmobile)
								.setSignedName(signedname).setLinkMan(linkman).setIdCard(idCard).setLegalPerson(legalperson)
								.setMinSettleAmount(minsettleamount).setRiskReserveDay(riskreserveday)
								.setBankAccountNumber(bankaccountnumber).setBankName(bankname).setAccountName(accountname)
								.setAreaCode(areaCode).setManualSettle(manualSettle).setBankCardPhoto(filelist.get(0))
								.setIdCardBackPhoto(filelist.get(1)).setIdCardPhoto(filelist.get(0))
								.setPersonPhoto(filelist.get(0)).setHmac(hmac).generateParams();
					}else{
						parts = new RegisterPartsBuilderss().setMainCustomerNumber(mainCustomerNumber)
								.setRequestId(requestId).setCustomerType(customertype).setBindMobile(bindmobile)
								.setSignedName(signedname).setLinkMan(linkman).setIdCard(idCard).setLegalPerson(legalperson)
								.setMinSettleAmount(minsettleamount).setRiskReserveDay(riskreserveday)
								.setBankAccountNumber(bankaccountnumber).setBankName(bankname).setAccountName(accountname)
								.setAreaCode(areaCode).setManualSettle(manualSettle).setBankCardPhoto(filelist.get(0))
								.setIdCardBackPhoto(filelist.get(1)).setIdCardPhoto(filelist.get(0))
								.setPersonPhoto(filelist.get(2)).setHmac(hmac).generateParams();
					}
				}

				postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
				LOG.info(postMethod.toString());

				int status = client.executeMethod(postMethod);
				LOG.info("==========status==========" + status);

				if (status == HttpStatus.SC_OK) {
					String backinfo = postMethod.getResponseBodyAsString();
					LOG.info("==========postMethod.getResponseBodyAsString()==========" + backinfo);
					JSONObject obj = JSONObject.fromObject(backinfo);
					if (obj.getString("code").equals("0000")) {
						LOG.info("进件成功======");

						String customerNumber = obj.getString("customerNumber");

						String stat = "SUCCESS";
						postMethod = new PostMethod(Conts.baseRequestUrl + "/auditMerchant.action");
						client = new HttpClient();
						StringBuilder hmacStr = new StringBuilder();
						hmacStr.append(customerNumber == null ? "" : customerNumber)
								.append(mainCustomerNumber == null ? "" : mainCustomerNumber)
								.append(stat == null ? "" : stat);
						hmac = Digest.hmacSign(hmacStr.toString(), key);
						Part[] parts2 = new AuditMerchantPartsBuilders().setMainCustomerNumber(mainCustomerNumber)
								.setCustomerNumber(customerNumber).setStatus(stat).setHmac(hmac).generateParams();

						postMethod.setRequestEntity(new MultipartRequestEntity(parts2, postMethod.getParams()));

						status = client.executeMethod(postMethod);
						LOG.info("==========status==========" + status);
						if (status == HttpStatus.SC_OK) {
							backinfo = postMethod.getResponseBodyAsString();
							LOG.info("==========backinfo2==========" + backinfo);
							obj = JSONObject.fromObject(backinfo);
							if (obj.getString("code").equals("0000")) {
								LOG.info("审核成功======");
								boolean setfee = this.setfee(idCard, customerNumber, mainCustomerNumber, rateCopy, "1");
								this.setfee(idCard, customerNumber, mainCustomerNumber, "0", "2");
								this.setfee(idCard, customerNumber, mainCustomerNumber, ExtraFee, "3");
								this.setfee(idCard, customerNumber, mainCustomerNumber, "0", "4");
								this.setfee(idCard, customerNumber, mainCustomerNumber, "0", "5");

								if (setfee) {
									LOG.info("设置费率成功======");
									YBQuickRegister ybQuickRegister2 = new YBQuickRegister();
									ybQuickRegister2.setPhone(phoneD);
									ybQuickRegister2.setBankCard(cardNo);
									ybQuickRegister2.setIdCard(idCard);
									ybQuickRegister2.setCustomerNum(customerNumber);
									ybQuickRegister2.setRate(rate);
									ybQuickRegister2.setExtraFee(ExtraFee);
									ybQuickRegister2.setMainCustomerNum(mainCustomerNumber);

									topupPayChannelBusiness.createYBQuickRegister(ybQuickRegister2);
									
									restTemplate = new RestTemplate();
									url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
									//url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";
									/** 修改订单状态为已关闭  */
									requestEntity = new LinkedMultiValueMap<String, String>();
									requestEntity.add("status", "2");
									requestEntity.add("third_code", "");
									requestEntity.add("order_code", orderCode);
									LOG.info("接口/v1.0/transactionclear/payment/update--参数================" + orderCode + ","
											+ "");
									result = restTemplate.postForObject(url, requestEntity, String.class);
									LOG.info("接口/v1.0/transactionclear/payment/update--RESULT================" + result);
									
									this.addOrderCauseOfFailure(orderCode, "新用户审核资质!", rip);
									
									maps.put(CommonConstants.RESULT,ipAddress + "/v1.0/paymentgateway/quick/ybn/auditing?ipAddress="+ ipAddress);
									maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
									maps.put(CommonConstants.RESP_MESSAGE, "新用户资质审核中!");

									return maps;
								} else {
									LOG.info("设置费率失败=====");

									maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
									maps.put(CommonConstants.RESP_MESSAGE, "设置费率失败!");

									this.addOrderCauseOfFailure(orderCode, "设置费率失败!", rip);

									return maps;
								}

							} else {
								LOG.info("审核失败======" + obj.getString("message"));

								maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
								maps.put(CommonConstants.RESP_MESSAGE, "信息审核失败! 失败原因: " + obj.getString("message"));

								this.addOrderCauseOfFailure(orderCode, obj.getString("message"), rip);

								return maps;
							}
						} else {
							LOG.info("请求失败======");

							maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
							maps.put(CommonConstants.RESP_MESSAGE, "交易排队中,请稍后重试!");

							this.addOrderCauseOfFailure(orderCode, "请求失败", rip);

							return maps;
						}

					} else {
						LOG.info("进件失败,失败原因为: " + obj.getString("message"));

						maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						maps.put(CommonConstants.RESP_MESSAGE, "进件失败,失败原因为: " + obj.getString("message"));

						this.addOrderCauseOfFailure(orderCode, obj.getString("message"), rip);

						return maps;
					}

				} else {
					LOG.info("请求失败======");

					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, "交易排队中,请稍后重试!");

					this.addOrderCauseOfFailure(orderCode, "请求失败", rip);

					return maps;
				}

			} catch (Exception e) {
				LOG.error("请求失败======" + e);
				e.printStackTrace();
				LOG.error("",e);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "交易排队中,请稍后重试!");
				return maps;
			} finally {
				for (int i = 0; i < filelist.size(); i++) {
					File files = filelist.get(i);
					files.delete();
				}
			}
		} else {
			if (!cardNo.equals(ybQuickRegister.getBankCard())) {
				LOG.info("需要修改进件信息======");
				boolean updateMerchant = updateMerchant(idCard, mainCustomerNumber, ybQuickRegister.getCustomerNum(),
						cardNo, bankname);

				if (updateMerchant) {
					ybQuickRegister.setBankCard(cardNo);
					ybQuickRegister.setPhone(phoneD);

					topupPayChannelBusiness.createYBQuickRegister(ybQuickRegister);

					if (!rate.equals(ybQuickRegister.getRate()) | !ExtraFee.equals(ybQuickRegister.getExtraFee())) {

						boolean setfee = setfee(idCard, ybQuickRegister.getCustomerNum(), mainCustomerNumber, rate,
								"1");
						this.setfee(idCard, ybQuickRegister.getCustomerNum(), mainCustomerNumber, "0", "2");
						this.setfee(idCard, ybQuickRegister.getCustomerNum(), mainCustomerNumber, ExtraFee, "3");
						this.setfee(idCard, ybQuickRegister.getCustomerNum(), mainCustomerNumber, "0", "4");
						this.setfee(idCard, ybQuickRegister.getCustomerNum(), mainCustomerNumber, "0", "5");

						if (setfee) {

							ybQuickRegister.setRate(rate);
							ybQuickRegister.setExtraFee(ExtraFee);
							topupPayChannelBusiness.createYBQuickRegister(ybQuickRegister);

							Map pay = (Map) pay(bankCard, phoneC, notifyurl, returnurl, orderCode, mainCustomerNumber,
									ybQuickRegister.getCustomerNum(), amount,phoneD);

							return pay;
						} else {
							LOG.info("设置费率失败=====");

							maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
							maps.put(CommonConstants.RESP_MESSAGE, "设置费率失败!");
							return maps;
						}
					} else {

						Map pay = (Map) pay(bankCard, phoneC, notifyurl, returnurl, orderCode, mainCustomerNumber,
								ybQuickRegister.getCustomerNum(), amount,phoneD);

						return pay;
					}

				} else {
					LOG.info("修改进件信息失败=====");

					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, "修改进件信息失败!");
					return maps;
				}

			} else if (!rate.equals(ybQuickRegister.getRate()) | !ExtraFee.equals(ybQuickRegister.getExtraFee())) {
				LOG.info("需要修改费率");
				boolean setfee = setfee(idCard, ybQuickRegister.getCustomerNum(), mainCustomerNumber, rate, "1");
				this.setfee(idCard, ybQuickRegister.getCustomerNum(), mainCustomerNumber, "0", "2");
				this.setfee(idCard, ybQuickRegister.getCustomerNum(), mainCustomerNumber, ExtraFee, "3");
				this.setfee(idCard, ybQuickRegister.getCustomerNum(), mainCustomerNumber, "0", "4");
				this.setfee(idCard, ybQuickRegister.getCustomerNum(), mainCustomerNumber, "0", "5");

				if (setfee) {

					ybQuickRegister.setRate(rate);
					ybQuickRegister.setExtraFee(ExtraFee);
					topupPayChannelBusiness.createYBQuickRegister(ybQuickRegister);

					Map pay = (Map) pay(bankCard, phoneC, notifyurl, returnurl, orderCode, mainCustomerNumber,
							ybQuickRegister.getCustomerNum(), amount,phoneD);

					return pay;
				} else {
					LOG.info("设置费率失败=====");

					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, "设置费率失败!");
					return maps;
				}

			} else {

				Map pay = (Map) pay(bankCard, phoneC, notifyurl, returnurl, orderCode, mainCustomerNumber,
						ybQuickRegister.getCustomerNum(), amount,phoneD);

				return pay;
			}

		}

	}

	/**
	 * @param result
	 */
	private static void parseResult(String result) {

		JSONObject jsonResult = JSONObject.fromObject(result);

		String url = jsonResult.getString("url");

		url = AESUtil.decrypt(url, key);

		System.out.println("===============");
		System.out.println("url = " + url);
		System.out.println("===============");
	}

	// 设置费率
	public boolean setfee(String idCard, String customerNumber, String mainCustomerNumber, String rateCopy,
			String productType) throws Exception {

		boolean istrue = false;

		PostMethod postMethod = new PostMethod(Conts.baseRequestUrl + "/feeSetApi.action");
		HttpClient client = new HttpClient();
		StringBuffer signature = new StringBuffer();
		signature.append(customerNumber == null ? "" : customerNumber)
				.append(mainCustomerNumber == null ? "" : mainCustomerNumber)
				.append(productType == null ? "" : productType).append(rateCopy == null ? "" : rateCopy);
		LOG.info("source===" + signature.toString());

		String hmac = Digest.hmacSign(signature.toString(), key);
		Part[] parts = new RegisterPartsBuilderss().setCustomerNumber(customerNumber)
				.setMainCustomerNumber(mainCustomerNumber).setProductType(productType).setHmac(hmac).setRate(rateCopy)
				.generateParams();

		postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

		int status = client.executeMethod(postMethod);
		if (status == HttpStatus.SC_OK) {
			String backinfo = postMethod.getResponseBodyAsString();
			JSONObject obj = JSONObject.fromObject(backinfo);
			LOG.info("修改费率返回的 obj======" + obj);
			if (obj.getString("code").equals("0000")) {
				/*
				 * RegisterAuth registerAuth =
				 * registerAuthBusiness.getRegisterAuthByidCard(idCard);
				 * registerAuth.setRate(rateCopy);
				 */
				istrue = true;
			} else {
				LOG.info("失败原因为: " + obj.getString("message"));
			}
		}

		return istrue;
	}

	// 修改进件信息
	public boolean updateMerchant(String idCard, String mainCustomerNumber, String customerNumber, String cardNo,
			String bankname) throws Exception {

		boolean istrue = false;

		PostMethod postMethod = new PostMethod("https://skb.yeepay.com/skb-app/customerInforUpdate.action");

		HttpClient client = new HttpClient();

		StringBuffer signature = new StringBuffer();
		signature.append(mainCustomerNumber == null ? "" : mainCustomerNumber)
				.append(customerNumber == null ? "" : customerNumber).append(cardNo == null ? "" : cardNo)
				.append(bankname == null ? "" : bankname);
		LOG.info("source===" + signature.toString());
		String hmac = Digest.hmacSign(signature.toString(), key);
		LOG.info("hmac====" + hmac);
		Part[] parts = new CustomerInforUpdatePartsBuilders().setMainCustomerNumber(mainCustomerNumber)
				.setCustomerNumber(customerNumber).setModifyType("2").setBankCardNumber(cardNo).setBankName(bankname)
				.setHmac(hmac).generateParams();

		postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

		int status = client.executeMethod(postMethod);
		if (status == HttpStatus.SC_OK) {
			String post = postMethod.getResponseBodyAsString();
			JSONObject obj = JSONObject.fromObject(post);
			LOG.info("post===============" + post);
			if (obj.getString("code").equals("0000")) {
				LOG.info("============修改成功============");
				/*
				 * RegisterAuth registerAuth =
				 * registerAuthBusiness.getRegisterAuthByidCard(idCard);
				 * registerAuth.setBankAccountNumber(cardNo);
				 * registerAuth.setBankName(bankname);
				 */
				istrue = true;
			} else {
				LOG.info("失败原因为: " + obj.getString("message"));
			}

		}

		return istrue;
	}

	// 查询子商户审核状态
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ybn/customerInforQuery")
	public @ResponseBody Object customerInforQuery(
			@RequestParam(value = "mainCustomerNumber", required = false, defaultValue = "10025093920") String mainCustomerNumber,
			@RequestParam(value = "customerNumber") String customerNumber,
			@RequestParam(value = "mobilePhone") String mobilePhone)
			throws Exception {

		Map<String, String> maps = new HashMap<String, String>();

		PostMethod postMethod = new PostMethod("https://skb.yeepay.com/skb-app/customerInforQuery.action");

		HttpClient client = new HttpClient();

		StringBuffer signature = new StringBuffer();
		signature.append(mainCustomerNumber == null ? "" : mainCustomerNumber)
				.append(mobilePhone == null ? "" : mobilePhone);
		LOG.info("source===" + signature.toString());
		String hmac = Digest.hmacSign(signature.toString(), key);
		LOG.info("hmac====" + hmac);
		Part[] parts = new CustomerInforUpdatePartsBuilders().setMainCustomerNumber(mainCustomerNumber)
				.setMobilePhone(mobilePhone)
				.setHmac(hmac).generateParams();

		postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

		int status = client.executeMethod(postMethod);
		if (status == HttpStatus.SC_OK) {
			String post = postMethod.getResponseBodyAsString();
			JSONObject obj = JSONObject.fromObject(post);
			LOG.info("post===============" + post);
			if (obj.getString("code").equals("0000")) {
				LOG.info("============查询成功============");
				String retList = obj.getString("retList");
				String strStart = "[";
				String strEnd = "]";
				int strStartIndex = retList.indexOf(strStart);
				int strEndIndex = retList.indexOf(strEnd);
				String respStr = retList.substring(strStartIndex+1, strEndIndex);
				JSONObject objStatus = JSONObject.fromObject(respStr);
				String idcardStatus = objStatus.getString("idcardStatus");
				String idcardMsg = objStatus.getString("idcardMsg");
				LOG.info("子商户审核资质状态：" + idcardStatus);
				if ("1".equals(idcardStatus)) {
					
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, idcardMsg);
					return maps;
				} else {

					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, idcardMsg);
					return maps;
				}
			} else {
				LOG.info("失败原因为: " + obj.getString("message"));
			}
		}
		return maps;
	}

	/**
	 * 跳转到
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/ybn/toChangeAuditing")
	public String toChangeAuditing(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
		LOG.info("进入修改资质页面");
		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String ipAddress = request.getParameter("ipAddress");
		String orderCode = request.getParameter("orderCode");

		model.addAttribute("ips", ipAddress);
		model.addAttribute("orderCode", orderCode);

		return "ybnupload";
	}

	// 修改资质
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ybn/changeAuditing")
	public @ResponseBody Object changeAuditing(HttpServletRequest request,
			@RequestParam(value = "positiveURL") String positiveURL, // 正面
			@RequestParam(value = "positiveFile") MultipartFile positiveFile, 
			@RequestParam(value = "mainCustomerNumber", required = false, defaultValue = "10025093920") String mainCustomerNumber,
			@RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "orderCode") String orderCode) throws Exception {

		LOG.info("进入修改资质接口--------------------");
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		
		Map<String, Object> maps = new HashMap<>();
		
		YBQuickRegister ybQuickRegister = topupPayChannelBusiness.getYBQuickRegisterByidCard(idCard);
		if (ybQuickRegister==null) {
			
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "请检查您的身份证输入是否正确!");
			return maps;
		}
		File file = null;
		try {
			String zipPath = "/" + idCard + ".jpg";
			// 图片转类型，压缩
			InputStream ins = positiveFile.getInputStream();
			file = new File(zipPath);
			inputStreamToFile(ins, file);
			PhotoCompressUtil.compressPhoto(new FileInputStream(file), file, 0.1f);

			PostMethod postMethod = new PostMethod("https://skb.yeepay.com/skb-app/customerPictureUpdate.action");
	
			HttpClient client = new HttpClient();
			
			StringBuffer signature = new StringBuffer();
			signature.append(mainCustomerNumber == null ? "" : mainCustomerNumber)
					.append(ybQuickRegister.getCustomerNum() == null ? "" : ybQuickRegister.getCustomerNum());
			LOG.info("source===" + signature.toString());
			String hmac = Digest.hmacSign(signature.toString(), key);
			LOG.info("hmac====" + hmac);
			
			Part[] parts = new RegisterPartsBuilderss().setMainCustomerNumber(mainCustomerNumber)
				.setCustomerNumber(ybQuickRegister.getCustomerNum()).setIdCardPhoto(file).setHmac(hmac).generateParams();
	
			postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
			int status = client.executeMethod(postMethod);
			if (status == HttpStatus.SC_OK) {
				String post = postMethod.getResponseBodyAsString();
				JSONObject obj = JSONObject.fromObject(post);
				LOG.info("post===============" + post);
				if (obj.getString("code").equals("0000")) {
					LOG.info("============修改资质成功============");
					
					this.addOrderCauseOfFailure(orderCode, "用户审核资质", prp.getIpAddress());
					RestTemplate restTemplate = new RestTemplate();
					MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
					restTemplate = new RestTemplate();
					
					String url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
					//String url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";
					/** 修改订单状态为已关闭  */
					requestEntity = new LinkedMultiValueMap<String, String>();
					requestEntity.add("status", "2");
					requestEntity.add("third_code", "");
					requestEntity.add("order_code", orderCode);
					LOG.info("接口/v1.0/transactionclear/payment/update--参数================" + orderCode + ","
							+ "");
					String result = restTemplate.postForObject(url, requestEntity, String.class);
					LOG.info("接口/v1.0/transactionclear/payment/update--RESULT================" + result);
					
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, obj.getString("message"));
					maps.put("redirect_url", ipAddress + "/v1.0/paymentgateway/quick/ybn/auditing?ipAddress="+ ipAddress);
					return maps;
				} else {
					LOG.info("失败原因为: " + obj.getString("message"));
					
					this.addOrderCauseOfFailure(orderCode, "修改审核资质失败", prp.getIpAddress());
					
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, obj.getString("message"));
					return maps;
				}
	
			}
		} finally {
			file.delete();
		}
		return maps;
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

	// 发起交易
	public Object pay(String bankcard, String telphone, String notifyurl, String returnurl, String ordercode,
			String mainCustomerNumber, String customerNumber, String amount,String phoneD) throws Exception {

		Map<String, String> maps = new HashMap<String, String>();
		
		maps = (Map<String, String>) customerInforQuery(mainCustomerNumber,customerNumber,phoneD);
		String respCode = maps.get("resp_code");
		String respMsg = maps.get("resp_message");
		if ("000000".equals(respCode)) {
		
			PostMethod postMethod = new PostMethod(Conts.baseRequestUrl + "/receiveApi.action");
			HttpClient client = new HttpClient();
			String source = "B";
			String mcc = "5311";
			String payerBankAccountNo = bankcard;
			String mobileNumber = telphone;
			String callBackUrl = notifyurl;
			String webCallBackUrl = returnurl;
			String requestcode = ordercode.substring(0, 20);
			StringBuilder hmacStr = new StringBuilder();
			hmacStr.append(source == null ? "" : source).append(mainCustomerNumber == null ? "" : mainCustomerNumber)
					.append(customerNumber == null ? "" : customerNumber).append(amount == null ? "" : amount)
					.append(mcc == null ? "" : mcc).append(requestcode == null ? "" : requestcode)
					.append(mobileNumber == null ? "" : mobileNumber).append(callBackUrl == null ? "" : callBackUrl)
					.append(webCallBackUrl == null ? "" : webCallBackUrl)
					.append(payerBankAccountNo == null ? "" : payerBankAccountNo);
	
			String hmac = Digest.hmacSign(hmacStr.toString(), key);
	
			Part[] parts = new ReceviePartsBuilers().setMainCustomerNumber(mainCustomerNumber).setAmount(amount)
					.setCustomerNumber(customerNumber).setHamc(hmac).setMcc(mcc).setMobileNumber(mobileNumber)
					.setCallBackUrl(callBackUrl).setRequestId(requestcode).setSource(source)
					.setWebCallBackUrl(webCallBackUrl).setPayerBankAccountNo(payerBankAccountNo).generateParams();
	
			postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
	
			int status = client.executeMethod(postMethod);
			if (status == HttpStatus.SC_OK) {
				String post = postMethod.getResponseBodyAsString();
				LOG.info("==========post==========" + post);
				JSONObject obj = JSONObject.fromObject(post);
				if (obj.getString("code").equals("0000")) {
					String backurl = obj.getString("url");
					String url1 = AESUtil.decrypt(backurl, key);
					LOG.info("==========url1==========" + url1);
					maps.put(CommonConstants.RESULT, url1);
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, "跳转绑卡页面");
					return maps;
				} else {
					
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, "交易失败：" + obj.getString("message"));
					return maps;
				}
	
			} else {
	
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "交易排队中,请稍后重试!");
				return maps;
			}
		} else {
			LOG.info("==========跳转到修改资质jumpAuditing==========");
			
			maps.put(CommonConstants.RESULT, ipAddress + "/v1.0/paymentgateway/quick/ybn/toChangeAuditing?ipAddress=" + ipAddress
					+ "&orderCode=" + ordercode);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, respMsg);
			return maps;
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
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/ybn/auditing")
	public String auditing(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		LOG.info("ybn------------------跳转到审核资质中页面");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("ipAddress", ipAddress);

		return "ybnauditing";
	}

}

class RegisterPartsBuilderss {

	private List<Part> parts = new ArrayList<Part>(31);

	public Part[] generateParams() {
		return parts.toArray(new Part[parts.size()]);
	}

	public RegisterPartsBuilderss setCustomerNumber(String customerNumber) {
		this.parts.add(new StringPart("customerNumber", customerNumber == null ? "" : customerNumber, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setProductType(String productType) {
		this.parts.add(new StringPart("productType", productType == null ? "" : productType, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setRate(String rate) {
		this.parts.add(new StringPart("rate", rate == null ? "" : rate, "UTF-8"));
		return this;
	}

	/**
	 * @param mainCustomerNumber
	 *            the mainCustomerNumber to set
	 */
	public RegisterPartsBuilderss setMainCustomerNumber(String mainCustomerNumber) {
		this.parts.add(
				new StringPart("mainCustomerNumber", mainCustomerNumber == null ? "" : mainCustomerNumber, "UTF-8"));
		return this;
	}

	/**
	 * @param bindMobile
	 *            the bindMobile to set
	 */
	public RegisterPartsBuilderss setBindMobile(String bindMobile) {
		this.parts.add(new StringPart("bindMobile", bindMobile == null ? "" : bindMobile, "UTF-8"));
		return this;
	}

	/**
	 * @param signedName
	 *            the signedName to set
	 */
	public RegisterPartsBuilderss setSignedName(String signedName) {
		this.parts.add(new StringPart("signedName", signedName == null ? "" : signedName, "UTF-8"));
		return this;
	}

	/**
	 * @param linkMan
	 *            the linkMan to set
	 */
	public RegisterPartsBuilderss setLinkMan(String linkMan) {
		this.parts.add(new StringPart("linkMan", linkMan == null ? "" : linkMan, "UTF-8"));
		return this;
	}

	/**
	 * @param idCard
	 *            the idCard to set
	 */
	public RegisterPartsBuilderss setIdCard(String idCard) {
		this.parts.add(new StringPart("idCard", idCard == null ? "" : idCard, "UTF-8"));
		return this;
	}

	/**
	 * @param legalPerson
	 *            the legalPerson to set
	 */
	public RegisterPartsBuilderss setLegalPerson(String legalPerson) {
		this.parts.add(new StringPart("legalPerson", legalPerson == null ? "" : legalPerson, "UTF-8"));
		return this;
	}

	/**
	 * @param minSettleAmount
	 *            the minSettleAmount to set
	 */
	public RegisterPartsBuilderss setMinSettleAmount(String minSettleAmount) {
		this.parts.add(new StringPart("minSettleAmount", minSettleAmount == null ? "" : minSettleAmount, "UTF-8"));
		return this;
	}

	/**
	 * @param riskReserveDay
	 *            the riskReserveDay to set
	 */
	public RegisterPartsBuilderss setRiskReserveDay(String riskReserveDay) {
		this.parts.add(new StringPart("riskReserveDay", riskReserveDay == null ? "" : riskReserveDay, "UTF-8"));
		return this;
	}

	/**
	 * @param bankAccountNumber
	 *            the bankAccountNumber to set
	 */
	public RegisterPartsBuilderss setBankAccountNumber(String bankAccountNumber) {
		this.parts
				.add(new StringPart("bankAccountNumber", bankAccountNumber == null ? "" : bankAccountNumber, "UTF-8"));
		return this;
	}

	/**
	 * @param bankName
	 *            the bankName to set
	 */
	public RegisterPartsBuilderss setBankName(String bankName) {

		this.parts.add(new StringPart("bankName", bankName == null ? "" : bankName, "UTF-8"));
		return this;
	}

	/**
	 * @param accountName
	 *            the cacountName to set
	 */
	public RegisterPartsBuilderss setAccountName(String accountName) {
		this.parts.add(new StringPart("accountName", accountName == null ? "" : accountName, "UTF-8"));
		return this;
	}

	/**
	 * @param requestId
	 *            the requestId to set
	 */
	public RegisterPartsBuilderss setRequestId(String requestId) {
		this.parts.add(new StringPart("requestId", requestId == null ? "" : requestId, "UTF-8"));
		return this;
	}

	/**
	 * @param customerType
	 *            the customerType to set
	 */
	public RegisterPartsBuilderss setCustomerType(String customerType) {
		this.parts.add(new StringPart("customerType", customerType == null ? "" : customerType, "UTF-8"));
		return this;
	}

	/**
	 * @param hmac
	 *            the hmac to set
	 */
	public RegisterPartsBuilderss setHmac(String hmac) {
		this.parts.add(new StringPart("hmac", hmac == null ? "" : hmac, "UTF-8"));
		return this;
	}

	/**
	 * @param areaCode
	 *            the areaCode to set
	 */
	public RegisterPartsBuilderss setAreaCode(String areaCode) {
		this.parts.add(new StringPart("areaCode", areaCode == null ? "" : areaCode, "UTF-8"));
		return this;
	}

	/**
	 * setter
	 *
	 * @param mailstr
	 */
	public RegisterPartsBuilderss setMailStr(String mailstr) {
		this.parts.add(new StringPart("mailstr", mailstr == null ? "" : mailstr, "UTF-8"));
		return this;
	}

	/**
	 * setter
	 *
	 * @param loginpassword
	 */
	public RegisterPartsBuilderss setLoginPassword(String loginpassword) {
		this.parts.add(new StringPart("loginPassword", loginpassword == null ? "" : loginpassword, "UTF-8"));
		return this;
	}

	/**
	 * setter
	 *
	 * @param tradepassword
	 */
	public RegisterPartsBuilderss setTradePassword(String tradepassword) {
		this.parts.add(new StringPart("tradePassword", tradepassword == null ? "" : tradepassword, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setBankaccounttype(String bankaccounttype) {
		this.parts.add(new StringPart("bankAccountType", bankaccounttype == null ? "" : bankaccounttype, "UTF-8"));
		return this;
	}

	/**
	 * @param businessLicence
	 *            the businessLicence to set
	 */
	public RegisterPartsBuilderss setBusinessLicence(String businessLicence) {
		this.parts.add(new StringPart("businessLicence", businessLicence == null ? "" : businessLicence, "UTF-8"));
		return this;
	}

	/**
	 * @param loginPasswordConfirm
	 *            the loginPasswordConfirm to set
	 */
	public RegisterPartsBuilderss setLoginPasswordConfirm(String loginPasswordConfirm) {
		this.parts.add(new StringPart("loginPasswordConfirm", loginPasswordConfirm == null ? "" : loginPasswordConfirm,
				"UTF-8"));
		return this;
	}

	/**
	 * @param tradePasswordConfirm
	 *            the tradePasswordConfirm to set
	 */
	public RegisterPartsBuilderss setTradePasswordConfirm(String tradePasswordConfirm) {
		this.parts.add(new StringPart("tradePasswordConfirm", tradePasswordConfirm == null ? "" : tradePasswordConfirm,
				"UTF-8"));
		return this;
	}

	/**
	 * 分润方
	 *
	 * @param splitter
	 *            the splitter to set
	 */
	public RegisterPartsBuilderss setSplitter(String splitter) {
		this.parts.add(new StringPart("splitter", splitter == null ? "" : splitter, "UTF-8"));
		return this;
	}

	/** 白名单 */
	public RegisterPartsBuilderss setWhiteList(String whiteList) {
		this.parts.add(new StringPart("whiteList", whiteList == null ? "" : whiteList, "UTF-8"));
		return this;
	}

	/***/
	public RegisterPartsBuilderss setFreezeDays(String freezeDays) {
		this.parts.add(new StringPart("freezeDays", freezeDays == null ? "" : freezeDays, "UTF-8"));
		return this;
	}

	/**
	 * 分润比率
	 *
	 * @param splitterprofitfee
	 *            the splitterprofitfee to set
	 */
	public RegisterPartsBuilderss setSplitterprofitfee(String splitterprofitfee) {
		this.parts
				.add(new StringPart("splitterprofitfee", splitterprofitfee == null ? "" : splitterprofitfee, "UTF-8"));
		return this;
	}

	// [end] jun.lin 2015-03-30 这里是普通入参

	// [start] jun.lin 2015-03-20 这里是文件入参

	private void configFilePart(File f, FilePart fp) {
		String fileName = f.getName();
		fp.setContentType("image/" + fileName.substring(fileName.lastIndexOf('.') + 1));
		fp.setCharSet("UTF-8");

		System.out.println(fp.getContentType());
	}

	private void configPdfFilePart(File f, FilePart fp) {
		String fileName = f.getName();
		fp.setContentType("application/" + fileName.substring(fileName.lastIndexOf('.') + 1));
		fp.setCharSet("UTF-8");

		System.out.println(fp.getContentType());
	}

	/**
	 * @param idCardPhoto
	 *            the idCardPhoto to set
	 * @throws FileNotFoundException
	 */
	public RegisterPartsBuilderss setIdCardPhoto(File idCardPhoto) throws FileNotFoundException {
		FilePart fp = new FilePart("idCardPhoto", idCardPhoto);
		configFilePart(idCardPhoto, fp);
		this.parts.add(fp);

		return this;
	}

	// 目前非必传
	public RegisterPartsBuilderss setIdCardBackPhoto(File idCardBackPhoto) throws FileNotFoundException {
		FilePart fp = new FilePart("idCardBackPhoto", idCardBackPhoto);
		configFilePart(idCardBackPhoto, fp);
		this.parts.add(fp);

		return this;
	}

	/**
	 * @param bankCardPhoto
	 *            the bankCardPhoto to set
	 * @throws FileNotFoundException
	 */
	public RegisterPartsBuilderss setBankCardPhoto(File bankCardPhoto) throws FileNotFoundException {
		FilePart fp = new FilePart("bankCardPhoto", bankCardPhoto);
		configFilePart(bankCardPhoto, fp);
		this.parts.add(fp);
		return this;
	}

	/**
	 * @param personPhoto
	 *            the personPhoto to set
	 * @throws FileNotFoundException
	 */
	public RegisterPartsBuilderss setPersonPhoto(File personPhoto) throws FileNotFoundException {
		FilePart fp = new FilePart("personPhoto", personPhoto);
		configFilePart(personPhoto, fp);
		this.parts.add(fp);
		return this;
	}

	/**
	 * @param businessLicensePhoto
	 *            the businessLicensePhoto to set
	 * @throws FileNotFoundException
	 */
	public RegisterPartsBuilderss setBusinessLicensePhoto(File businessLicensePhoto) throws FileNotFoundException {
		FilePart fp = new FilePart("businessLicensePhoto", businessLicensePhoto);
		configFilePart(businessLicensePhoto, fp);
		this.parts.add(fp);
		return this;
	}

	public RegisterPartsBuilderss setCertFee(String certFee) {
		this.parts.add(new StringPart("certFee", certFee == null ? "" : certFee, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setManualSettle(String manualSettle) {
		this.parts.add(new StringPart("manualSettle", manualSettle == null ? "" : manualSettle, "UTF-8"));
		return this;
	}

	public RegisterPartsBuilderss setElectronicAgreement(File electronicAgreement) throws FileNotFoundException {
		FilePart fp = new FilePart("electronicAgreement", electronicAgreement);
		configPdfFilePart(electronicAgreement, fp);
		this.parts.add(fp);
		return this;
	}
}

class AuditMerchantPartsBuilders {

	private List<Part> parts = new ArrayList<Part>(11);

	public Part[] generateParams() {
		return parts.toArray(new Part[parts.size()]);
	}

	public AuditMerchantPartsBuilders setStatus(String status) {
		this.parts.add(new StringPart("status", status == null ? "" : status, "UTF-8"));
		return this;
	}

	public AuditMerchantPartsBuilders setMainCustomerNumber(String mainCustomerNumber) {
		this.parts.add(
				new StringPart("mainCustomerNumber", mainCustomerNumber == null ? "" : mainCustomerNumber, "UTF-8"));
		return this;
	}

	public AuditMerchantPartsBuilders setCustomerNumber(String customerNumber) {
		this.parts.add(new StringPart("customerNumber", customerNumber == null ? "" : customerNumber, "UTF-8"));
		return this;
	}

	public AuditMerchantPartsBuilders setHmac(String hmac) {
		this.parts.add(new StringPart("hmac", hmac == null ? "" : hmac, "UTF-8"));
		return this;
	}

	public AuditMerchantPartsBuilders setReason(String reason) {
		this.parts.add(new StringPart("reason", reason == null ? "" : reason, "UTF-8"));
		return this;
	}
}

class ReceviePartsBuilers {

	private List<Part> parts = new ArrayList<Part>(11);

	public Part[] generateParams() {
		return parts.toArray(new Part[parts.size()]);
	}

	public ReceviePartsBuilers setSource(String source) {
		this.parts.add(new StringPart("source", source == null ? "" : source, "UTF-8"));
		return this;
	}

	public ReceviePartsBuilers setMainCustomerNumber(String mainCustomerNumber) {
		this.parts.add(
				new StringPart("mainCustomerNumber", mainCustomerNumber == null ? "" : mainCustomerNumber, "UTF-8"));
		return this;
	}

	public ReceviePartsBuilers setCustomerNumber(String customerNumber) {
		this.parts.add(new StringPart("customerNumber", customerNumber == null ? "" : customerNumber, "UTF-8"));
		return this;
	}

	public ReceviePartsBuilers setRequestId(String requestId) {
		this.parts.add(new StringPart("requestId", requestId == null ? "" : requestId, "UTF-8"));
		return this;
	}

	public ReceviePartsBuilers setAmount(String amout) {
		this.parts.add(new StringPart("amount", amout == null ? "" : amout, "UTF-8"));
		return this;
	}

	public ReceviePartsBuilers setMcc(String mcc) {
		this.parts.add(new StringPart("mcc", mcc == null ? "" : mcc, "UTF-8"));
		return this;
	}

	public ReceviePartsBuilers setMobileNumber(String mobileNumber) {
		this.parts.add(new StringPart("mobileNumber", mobileNumber == null ? "" : mobileNumber, "UTF-8"));
		return this;
	}

	public ReceviePartsBuilers setCallBackUrl(String callBackUrl) {
		this.parts.add(new StringPart("callBackUrl", callBackUrl == null ? "" : callBackUrl, "UTF-8"));
		return this;
	}

	public ReceviePartsBuilers setWebCallBackUrl(String webCallBackUrl) {
		this.parts.add(new StringPart("webCallBackUrl", webCallBackUrl == null ? "" : webCallBackUrl, "UTF-8"));
		return this;
	}

	public ReceviePartsBuilers setSmgCallBackUrl(String smgCallBackUrl) {
		this.parts.add(new StringPart("smgCallBackUrl", smgCallBackUrl == null ? "" : smgCallBackUrl, "UTF-8"));
		return this;
	}

	public ReceviePartsBuilers setPayerBankAccountNo(String payerBankAccountNo) {
		this.parts.add(
				new StringPart("payerBankAccountNo", payerBankAccountNo == null ? "" : payerBankAccountNo, "UTF-8"));
		return this;
	}

	public ReceviePartsBuilers setHamc(String hmac) {
		this.parts.add(new StringPart("hmac", hmac == null ? "" : hmac, "UTF-8"));
		return this;
	}

	public ReceviePartsBuilers setCfca(String cfca) {
		this.parts.add(new StringPart("cfca", cfca == null ? "" : cfca, "UTF-8"));
		return this;
	}

	public ReceviePartsBuilers setDescription(String description) {
		this.parts.add(new StringPart("description", description == null ? "" : description, "UTF-8"));
		return this;
	}
	
}
