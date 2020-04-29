package com.jh.paymentchannel.service;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentchannel.pojo.PaymentOrder;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.yeepay.AESUtil;
import com.jh.paymentchannel.util.yeepay.Conts;
import com.jh.paymentchannel.util.yeepay.Digest;
import com.jh.paymentchannel.util.yeepay.Img2Small;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

/**
 * 易宝交易
 * 
 * @author ycl
 * @create 2017-10-10-上午10:20
 */
@Service
public class YB2TopupService implements TopupRequest {

	private static final Logger LOG = LoggerFactory.getLogger(YB2TopupService.class);
	private String realnamePic = "/usr/share/nginx/photo";
	private String realnameCopyPic = "/mnt/share/nginx/html/";
	private static String key = Conts.hmacKey; // 商户秘钥

	@Autowired
	Util util;

	@Override
	public Map<String, String> topupRequest(Map<String, Object> params) throws Exception {
		PaymentOrder paymentOrder = (PaymentOrder) params.get("paymentOrder");
		String extra = (String) params.get("extra");
		String notifyurl = (String) params.get("notifyURL");
		String returnurl = (String) params.get("returnURL");
		String ordercode = paymentOrder.getOrdercode();
		String amount = paymentOrder.getAmount().toString();

		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("接口/v1.0/transactionclear/payment/query/ordercode--RESULT================" + result);
		JSONObject jsonObject = JSONObject.fromObject(result);
		JSONObject resultObj = jsonObject.getJSONObject("result");
		String bankcard = resultObj.getString("bankcard");
		String channelid = resultObj.getString("channelid");
		String telphone = resultObj.getString("phone");
		String extraFee = resultObj.getString("extraFee");
		String brandid = resultObj.getString("brandid");
		if (extraFee.contains("."))
			extraFee = extraFee.substring(0, extraFee.indexOf("."));
		/** 根据订单号获取相应的订单 */
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/cardno";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("cardno", bankcard);
		requestEntity.add("type", "0");
		result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("接口/v1.0/user/bank/default/cardno--RESULT================" + result);
		jsonObject = JSONObject.fromObject(result);
		resultObj = jsonObject.getJSONObject("result");
		String idcard = resultObj.getString("idcard");// 身份证号
		String userName = resultObj.getString("userName");// 用户姓名
		long userid = resultObj.getLong("userId");// 用户ID

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/userid";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("user_id", String.valueOf(userid));
		result = restTemplate.postForObject(url, requestEntity, String.class);
		String respCode = "";
		LOG.info("接口/v1.0/user/bank/default/userid--RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
			respCode = jsonObject.getString("resp_code");
		} catch (Exception e1) {
			LOG.error("查询用户默认结算卡异常" + e1.getMessage(),e1);
			Map map = new HashMap();
			map.put("resp_code", "failed");
			map.put("resp_message", "没有结算卡");
			return map;
		}

		if (!"000000".equals(respCode)) {
			Map map = new HashMap();
			map.put("resp_code", "failed");
			map.put("resp_message", "没有结算卡");
			return map;
		}

		// if(null==resultObj||resultObj.isNullObject()||resultObj.isEmpty()){
		// Map map = new HashMap();
		// map.put("resp_code", "failed");
		// map.put("resp_message", "没有结算卡");
		// return map;
		// }

		String bankNameJJ = resultObj.getString("bankName");// 借记卡银行名
		if ("平安银行".equals(bankNameJJ))
			bankNameJJ = "深圳发展银行";
		String cardNo = resultObj.getString("cardNo");// 借记卡卡号
		String province = resultObj.getString("province");
		String city = resultObj.getString("city");
		String areano = null;
		if (!"null".equals(province) && null != province && !"".equals(province)) {
			uri = util.getServiceUrl("paymentchannel", "error url request!");
			String road = uri.toString() + "/v1.0/paymentchannel/areanumber/queryareano";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("province", province);
			requestEntity.add("city", city);
			restTemplate = new RestTemplate();
			String resultObjx = restTemplate.postForObject(road, requestEntity, String.class);
			LOG.info("接口/v1.0/paymentchannel/areanumber/queryareano--RESULT================" + resultObjx);
			jsonObject = JSONObject.fromObject(resultObjx);
			areano = jsonObject.getString("result");
			// areano = resultObj.getString("areano");
		}
		uri = util.getServiceUrl("user", "error url request!");
		String road = uri.toString() + "/v1.0/user/channel/rate/query/userid";

		/** 根据的渠道标识或去渠道的相关信息 */
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("channel_id", channelid);
		requestEntity.add("user_id", String.valueOf(userid));
		restTemplate = new RestTemplate();
		String resultObjx = restTemplate.postForObject(road, requestEntity, String.class);
		LOG.info("接口/v1.0/user/channel/rate/query/userid--RESULT================" + resultObjx);
		jsonObject = JSONObject.fromObject(resultObjx);

		if (!CommonConstants.SUCCESS.equals(jsonObject.getString(CommonConstants.RESP_CODE))) {
			Map<String, String> map = new HashMap<String, String>();
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.支付失败,臣妾已经尽力了,请重试!");
			return map;
		}

		resultObj = jsonObject.getJSONObject("result");
		String rate = resultObj.getString("rate");
		String rateCopy = rate;
		int withdrawFed = resultObj.getInt("withdrawFee");
		String withdrawFee = String.valueOf(withdrawFed);
		uri = util.getServiceUrl("paymentchannel", "error url request!");
		url = uri.toString() + "/v1.0/paymentchannel/registerAuth/query";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("mobile", telphone);
		LOG.info("接口/v1.0/paymentchannel/registerAuth/query--参数================" + requestEntity.toString());
		result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("接口/v1.0/paymentchannel/registerAuth/query--result================" + result.toString());
		jsonObject = JSONObject.fromObject(result);
		String bankAccountNumber = null;
		if (!"null".equals(jsonObject.getString("result")) && null != jsonObject.getString("result")
				&& !"".equals(jsonObject.getString("result"))) {
			JSONObject obj = JSONObject.fromObject(jsonObject.getString("result"));
			bankAccountNumber = obj.getString("bankAccountNumber");
			LOG.info("============bankAccountNumber============" + bankAccountNumber);
		}
		String mainCustomerNumber = Conts.customerNumber; // 代理商编码
		String requestId = UUID.randomUUID().toString().substring(0, 15); // 注册请求号，每次请求唯一
		String customertype = "PERSON";// 企业-ENTERPRISE,个体工商户-INDIVIDUAL
		// 个人-PERSON
		String bindmobile = telphone;
		String signedname = userName;
		String linkman = "莘丽";
		String legalperson = userName;// 法人
		String minsettleamount = "1";
		String riskreserveday = "0";
		String bankaccountnumber = cardNo;
		String bankname = bankNameJJ;
		String accountname = userName;
		if (null == areano || "null".equals(areano) || "".equals(areano))
			areano = "1111";
		String areaCode = areano;
		Map map = new HashMap();
		if (null != extra && !"".equals(extra)) {
			if (!cardNo.equals(bankAccountNumber)) {
				uri = util.getServiceUrl("paymentchannel", "error url request!");
				url = uri.toString() + "/v1.0/paymentchannel/registerAuth/update";
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("mobile", telphone);
				requestEntity.add("bank_account_number", cardNo);
				requestEntity.add("bank_name", bankname);
				LOG.info("接口/v1.0/paymentchannel/registerAuth/update--参数================" + requestEntity.toString());
				result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("接口/v1.0/paymentchannel/registerAuth/update--result================" + result.toString());

				PostMethod postMethod = new PostMethod("https://skb.yeepay.com/skb-app/customerInforUpdate.action");

				HttpClient client = new HttpClient();

				StringBuffer signature = new StringBuffer();
				signature.append(mainCustomerNumber == null ? "" : mainCustomerNumber)
						.append(extra == null ? "" : extra).append(cardNo == null ? "" : cardNo)
						.append(bankname == null ? "" : bankname);
				System.out.println("source===" + signature.toString());
				String hmac = Digest.hmacSign(signature.toString(), key);
				System.out.println("hmac====" + hmac);
				Part[] parts = new CustomerInforUpdatePartsBuilder().setMainCustomerNumber(mainCustomerNumber)
						.setCustomerNumber(extra).setModifyType("2").setBankCardNumber(cardNo).setBankName(bankname)
						.setHmac(hmac).generateParams();

				postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

				int status = client.executeMethod(postMethod);
				if (status == HttpStatus.SC_OK) {
					result = postMethod.getResponseBodyAsString();
					JSONObject obj = JSONObject.fromObject(result);
					System.out.println("===============");
					System.out.println("result" + result);
					System.out.println("===============");
					if (obj.getString("code").equals("0000")) {
						LOG.info("============修改成功============");
					}

				}
			}

			LOG.info("============已注册路径============");
			PostMethod postMethod = null;
			HttpClient client = null;
			StringBuffer signature = null;
			String customerNumber = extra;
			String productType = "1";
			int status = 0;
			int statusAll = 0;
			String backinfo = null;
			String hmac = null;
			Part[] parts = null;
			for (int i = 1; i < 6; i++) {
				postMethod = new PostMethod(Conts.baseRequestUrl + "/feeSetApi.action");
				client = new HttpClient();
				signature = new StringBuffer();
				if (i == 2)
					continue;
				if (i == 3)
					rateCopy = extraFee;
				if (i == 4)
					rateCopy = "0";
				if (i == 5)
					rateCopy = "0";
				productType = String.valueOf(i);
				signature.append(customerNumber == null ? "" : customerNumber)
						.append(mainCustomerNumber == null ? "" : mainCustomerNumber)
						.append(productType == null ? "" : productType).append(rateCopy == null ? "" : rateCopy);
				System.out.println("source===" + signature.toString());

				hmac = Digest.hmacSign(signature.toString(), key);
				parts = new RegisterPartsBuilder().setCustomerNumber(customerNumber)
						.setMainCustomerNumber(mainCustomerNumber).setProductType(productType).setHmac(hmac)
						.setRate(rateCopy).generateParams();

				postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

				status = client.executeMethod(postMethod);
				backinfo = postMethod.getResponseBodyAsString();
				LOG.info("==========backinfo" + i + "===========" + backinfo);
				if (status == HttpStatus.SC_OK) {
					JSONObject obj = JSONObject.fromObject(backinfo);
					if (obj.getString("code").equals("0000")) {
						statusAll++;
					}
				}
			}
			if (statusAll == 4) {
				JSONObject obj = JSONObject.fromObject(backinfo);
				if (obj.getString("code").equals("0000")) {
					postMethod = new PostMethod(Conts.baseRequestUrl + "/receiveApi.action");
					client = new HttpClient();
					String source = "B";
					String mcc = "5311";
					String payerBankAccountNo = bankcard;
					String mobileNumber = telphone;
					String callBackUrl = notifyurl;
					String webCallBackUrl = returnurl;
					String requestcode = ordercode.substring(0, 20);
					StringBuilder hmacStr = new StringBuilder();
					hmacStr.append(source == null ? "" : source)
							.append(mainCustomerNumber == null ? "" : mainCustomerNumber)
							.append(customerNumber == null ? "" : customerNumber).append(amount == null ? "" : amount)
							.append(mcc == null ? "" : mcc).append(requestcode == null ? "" : requestcode)
							.append(mobileNumber == null ? "" : mobileNumber)
							.append(callBackUrl == null ? "" : callBackUrl)
							.append(webCallBackUrl == null ? "" : webCallBackUrl)
							.append(payerBankAccountNo == null ? "" : payerBankAccountNo);

					hmac = Digest.hmacSign(hmacStr.toString(), key);

					parts = new ReceviePartsBuiler().setMainCustomerNumber(mainCustomerNumber).setAmount(amount)
							.setCustomerNumber(customerNumber).setHamc(hmac).setMcc(mcc).setMobileNumber(mobileNumber)
							.setCallBackUrl(callBackUrl).setRequestId(requestcode).setSource(source)
							.setWebCallBackUrl(webCallBackUrl).setPayerBankAccountNo(payerBankAccountNo)
							.generateParams();

					postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

					status = client.executeMethod(postMethod);
					if (status == HttpStatus.SC_OK) {
						backinfo = postMethod.getResponseBodyAsString();
						LOG.info("==========backinfo4==========" + backinfo);
						obj = JSONObject.fromObject(backinfo);
						if (obj.getString("code").equals("0000")) {
							String backurl = obj.getString("url");
							url = AESUtil.decrypt(backurl, key);
							LOG.info("==========url==========" + url);
							map.put("redirect_url", url);
							map.put("resp_code", "success");
							map.put("channel_type", "yb");

						} else {
							map.put("channel_type", "yb");
							map.put("resp_code", "failed");
							map.put("resp_message", "交易失败：" + obj.getString("message"));
						}
					} else if (status == HttpStatus.SC_MOVED_PERMANENTLY || status == HttpStatus.SC_MOVED_TEMPORARILY) {
						// 从头中取出转向的地址
						Header locationHeader = postMethod.getResponseHeader("location");
						String location = null;
						if (locationHeader != null) {
							location = locationHeader.getValue();
							System.out.println("The page was redirected to:" + location);
						} else {
							System.err.println("Location field value is null.");
						}
					} else {
						System.out.println("fail======" + status);
					}

				} else {
					map.put("channel_type", "yb");
					map.put("resp_code", "failed");
					map.put("resp_message", "设置费率失败：" + obj.getString("message"));
				}
			} else if (status == HttpStatus.SC_MOVED_PERMANENTLY || status == HttpStatus.SC_MOVED_TEMPORARILY) {

				Header locationHeader = postMethod.getResponseHeader("location");
				String location = null;
				if (locationHeader != null) {
					location = locationHeader.getValue();
					System.out.println("The page was redirected to:" + location);
				} else {
					System.err.println("Location field value is null.");
				}
			} else {
				map.put("channel_type", "yb");
				map.put("resp_code", "failed");
				map.put("resp_message", "设置费率失败!");
			}
			return map;

		} else {

			LOG.info("============未注册路径============");
			File dir = new File(realnamePic + "/" + telphone);
			// 创建目录
			if (dir.mkdirs()) {
				System.out.println("创建目录" + realnamePic + "/" + telphone + "成功！");
				LOG.info("=================创建目录成功================");
			} else {
				File[] tempfiles = dir.listFiles();
				for (File file : tempfiles) {
					file.delete();
				}
				LOG.info("=================创建目录失败================");
				System.out.println("创建目录" + realnamePic + "/" + telphone + "失败！");
			}
			File file = new File(realnameCopyPic + brandid + "/realname/" + telphone);
			String[] filelist = file.list();
			if (filelist != null) {
				for (int i = 0; i < filelist.length; i++) {
					Img2Small.resizeImage(realnameCopyPic + brandid + "/realname/" + telphone + "/" + filelist[i],
							realnamePic + "/" + telphone + "/" + filelist[i], 2000, 2000);
				}
			}
			file = new File(realnamePic + "/" + telphone);
			filelist = file.list();
			File f1 = null;
			File f2 = null;
			File f3 = null;
			if (filelist.length > 2) {
				f1 = new File(realnamePic + "/" + telphone + "/" + filelist[0]);
				f2 = new File(realnamePic + "/" + telphone + "/" + filelist[1]);
				f3 = new File(realnamePic + "/" + telphone + "/" + filelist[2]);
			} else {
				f1 = new File(realnamePic + "/" + telphone + "/" + filelist[0]);
				f2 = new File(realnamePic + "/" + telphone + "/" + filelist[1]);
			}
			PostMethod postMethod = new PostMethod(Conts.baseRequestUrl + "/register.action");
			// PostMethod postMethod = new
			// PostMethod("http://10.151.30.4:8057/skb-app/register.action");
			HttpClient client = new HttpClient();
			String manualSettle = "N";
			try {
				StringBuffer signature = new StringBuffer();
				signature.append(mainCustomerNumber == null ? "" : mainCustomerNumber)
						.append(requestId == null ? "" : requestId).append(customertype == null ? "" : customertype)
						.append(bindmobile == null ? "" : bindmobile).append(signedname == null ? "" : signedname)
						.append(linkman == null ? "" : linkman).append(idcard == null ? "" : idcard)
						.append(legalperson == null ? "" : legalperson)
						.append(minsettleamount == null ? "" : minsettleamount)
						.append(riskreserveday == null ? "" : riskreserveday)
						.append(bankaccountnumber == null ? "" : bankaccountnumber)
						.append(bankname == null ? "" : bankname).append(accountname == null ? "" : accountname)
						.append(areaCode == null ? "" : areaCode).append(manualSettle == null ? "" : manualSettle);

				System.out.println("signature=======================" + signature.toString());
				String hmac = Digest.hmacSign(signature.toString(), key);
				System.out.println("hmac=======================" + hmac);
				Part[] parts = null;
				if (filelist.length > 2) {
					parts = new RegisterPartsBuilder().setMainCustomerNumber(mainCustomerNumber).setRequestId(requestId)
							.setCustomerType(customertype).setBindMobile(bindmobile).setSignedName(signedname)
							.setLinkMan(linkman).setIdCard(idcard).setLegalPerson(legalperson)
							.setMinSettleAmount(minsettleamount).setRiskReserveDay(riskreserveday)
							.setBankAccountNumber(bankaccountnumber).setBankName(bankname).setAccountName(accountname)
							.setAreaCode(areaCode).setManualSettle(manualSettle).setBankCardPhoto(f1)
							.setIdCardBackPhoto(f2).setIdCardPhoto(f1).setPersonPhoto(f3).setHmac(hmac)
							.generateParams();
				} else {
					parts = new RegisterPartsBuilder().setMainCustomerNumber(mainCustomerNumber).setRequestId(requestId)
							.setCustomerType(customertype).setBindMobile(bindmobile).setSignedName(signedname)
							.setLinkMan(linkman).setIdCard(idcard).setLegalPerson(legalperson)
							.setMinSettleAmount(minsettleamount).setRiskReserveDay(riskreserveday)
							.setBankAccountNumber(bankaccountnumber).setBankName(bankname).setAccountName(accountname)
							.setAreaCode(areaCode).setManualSettle(manualSettle).setBankCardPhoto(f1)
							.setIdCardBackPhoto(f2).setIdCardPhoto(f1).setPersonPhoto(f2).setHmac(hmac)
							.generateParams();

				}

				postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
				System.out.println(postMethod.toString());

				int status = client.executeMethod(postMethod);
				LOG.info("==========status==========" + status);

				if (status == HttpStatus.SC_OK) {
					String backinfo = postMethod.getResponseBodyAsString();
					LOG.info("==========postMethod.getResponseBodyAsString()==========" + backinfo);
					JSONObject obj = JSONObject.fromObject(backinfo);
					if (obj.getString("code").equals("0000")) {
						String customerNumber = obj.getString("customerNumber");

						uri = util.getServiceUrl("paymentchannel", "error url request!");
						road = uri.toString() + "/v1.0/paymentchannel/registerAuth/save";
						/** 根据的渠道标识或去渠道的相关信息 */
						requestEntity = new LinkedMultiValueMap<String, String>();
						requestEntity.add("request_id", requestId);
						requestEntity.add("mobile", telphone);
						requestEntity.add("id_card", idcard);
						requestEntity.add("legal_person", userName);
						requestEntity.add("min_settle_amoun", minsettleamount);
						requestEntity.add("risk_reserve_day", riskreserveday);
						requestEntity.add("bank_account_number", bankaccountnumber);
						requestEntity.add("bank_name", bankname);
						requestEntity.add("customer_number", customerNumber);
						requestEntity.add("status", "0");// 0成功
						requestEntity.add("rate", rate);
						requestEntity.add("charge", extraFee);
						restTemplate = new RestTemplate();
						LOG.info("接口/v1.0/paymentchannel/registerAuth/save--参数================"
								+ restTemplate.toString());
						resultObjx = restTemplate.postForObject(road, requestEntity, String.class);
						LOG.info("接口/v1.0/paymentchannel/registerAuth/save--RESULT================" + resultObjx);

						String stat = "SUCCESS";
						postMethod = new PostMethod(Conts.baseRequestUrl + "/auditMerchant.action");
						client = new HttpClient();
						StringBuilder hmacStr = new StringBuilder();
						hmacStr.append(customerNumber == null ? "" : customerNumber)
								.append(mainCustomerNumber == null ? "" : mainCustomerNumber)
								.append(stat == null ? "" : stat);
						hmac = Digest.hmacSign(hmacStr.toString(), key);
						Part[] parts2 = new AuditMerchantPartsBuilder().setMainCustomerNumber(mainCustomerNumber)
								.setCustomerNumber(customerNumber).setStatus(stat).setHmac(hmac).generateParams();

						postMethod.setRequestEntity(new MultipartRequestEntity(parts2, postMethod.getParams()));

						status = client.executeMethod(postMethod);
						LOG.info("==========status==========" + status);
						if (status == HttpStatus.SC_OK) {
							backinfo = postMethod.getResponseBodyAsString();
							LOG.info("==========backinfo2==========" + backinfo);
							obj = JSONObject.fromObject(backinfo);
							if (obj.getString("code").equals("0000")) {
								int statusAll = 0;
								for (int i = 1; i < 6; i++) {
									postMethod = new PostMethod(Conts.baseRequestUrl + "/feeSetApi.action");
									client = new HttpClient();
									signature = new StringBuffer();
									if (i == 2)
										continue;
									if (i == 3)
										rateCopy = extraFee;
									if (i == 4)
										rateCopy = "0";
									if (i == 5)
										rateCopy = "0";
									String productType = String.valueOf(i);
									signature.append(customerNumber == null ? "" : customerNumber)
											.append(mainCustomerNumber == null ? "" : mainCustomerNumber)
											.append(productType == null ? "" : productType)
											.append(rateCopy == null ? "" : rateCopy);
									System.out.println("source===" + signature.toString());

									hmac = Digest.hmacSign(signature.toString(), key);
									parts = new RegisterPartsBuilder().setCustomerNumber(customerNumber)
											.setMainCustomerNumber(mainCustomerNumber).setProductType(productType)
											.setHmac(hmac).setRate(rateCopy).generateParams();

									postMethod.setRequestEntity(
											new MultipartRequestEntity(parts, postMethod.getParams()));

									status = client.executeMethod(postMethod);
									LOG.info("==========backinfo" + i + "===========" + backinfo);
									if (status == HttpStatus.SC_OK) {
										backinfo = postMethod.getResponseBodyAsString();
										obj = JSONObject.fromObject(backinfo);
										if (obj.getString("code").equals("0000")) {
											statusAll++;
										}
									}
								}
								if (statusAll == 4) {
									backinfo = postMethod.getResponseBodyAsString();
									LOG.info("==========backinfo3==========" + backinfo);
									obj = JSONObject.fromObject(backinfo);
									if (obj.getString("code").equals("0000")) {
										postMethod = new PostMethod(Conts.baseRequestUrl + "/receiveApi.action");
										client = new HttpClient();
										String source = "B";
										String mcc = "5311";
										String payerBankAccountNo = bankcard;
										String mobileNumber = telphone;
										String callBackUrl = notifyurl;
										String webCallBackUrl = returnurl;
										String requestcode = ordercode.substring(0, 20);
										hmacStr = new StringBuilder();
										hmacStr.append(source == null ? "" : source)
												.append(mainCustomerNumber == null ? "" : mainCustomerNumber)
												.append(customerNumber == null ? "" : customerNumber)
												.append(amount == null ? "" : amount).append(mcc == null ? "" : mcc)
												.append(requestcode == null ? "" : requestcode)
												.append(mobileNumber == null ? "" : mobileNumber)
												.append(callBackUrl == null ? "" : callBackUrl)
												.append(webCallBackUrl == null ? "" : webCallBackUrl)
												.append(payerBankAccountNo == null ? "" : payerBankAccountNo);

										hmac = Digest.hmacSign(hmacStr.toString(), key);

										parts = new ReceviePartsBuiler().setMainCustomerNumber(mainCustomerNumber)
												.setAmount(amount).setCustomerNumber(customerNumber).setHamc(hmac)
												.setMcc(mcc).setMobileNumber(mobileNumber).setCallBackUrl(callBackUrl)
												.setRequestId(requestcode).setSource(source)
												.setWebCallBackUrl(webCallBackUrl)
												.setPayerBankAccountNo(payerBankAccountNo).generateParams();

										postMethod.setRequestEntity(
												new MultipartRequestEntity(parts, postMethod.getParams()));

										status = client.executeMethod(postMethod);
										if (status == HttpStatus.SC_OK) {
											backinfo = postMethod.getResponseBodyAsString();
											LOG.info("==========backinfo4==========" + backinfo);
											obj = JSONObject.fromObject(backinfo);
											if (obj.getString("code").equals("0000")) {
												String backurl = obj.getString("url");
												url = AESUtil.decrypt(backurl, key);
												LOG.info("==========url==========" + url);
												map.put("redirect_url", url);
												map.put("resp_code", "success");
												map.put("channel_type", "yb");

											} else {
												map.put("channel_type", "yb");
												map.put("resp_code", "failed");
												map.put("resp_message", "交易失败：" + obj.getString("message"));
											}
										} else if (status == HttpStatus.SC_MOVED_PERMANENTLY
												|| status == HttpStatus.SC_MOVED_TEMPORARILY) {
											// 从头中取出转向的地址
											Header locationHeader = postMethod.getResponseHeader("location");
											String location = null;
											if (locationHeader != null) {
												location = locationHeader.getValue();
												System.out.println("The page was redirected to:" + location);
											} else {
												System.err.println("Location field value is null.");
											}
										} else {
											System.out.println("fail======" + status);
										}

									} else {
										map.put("channel_type", "yb");
										map.put("resp_code", "failed");
										map.put("resp_message", "设置费率失败：" + obj.getString("message"));
									}
								} else if (status == HttpStatus.SC_MOVED_PERMANENTLY
										|| status == HttpStatus.SC_MOVED_TEMPORARILY) {

									Header locationHeader = postMethod.getResponseHeader("location");
									String location = null;
									if (locationHeader != null) {
										location = locationHeader.getValue();
										System.out.println("The page was redirected to:" + location);
									} else {
										System.err.println("Location field value is null.");
									}
								} else {
									map.put("channel_type", "yb");
									map.put("resp_code", "failed");
									map.put("resp_message", "设置费率失败：" + obj.getString("message"));
								}
							} else {
								map.put("channel_type", "yb");
								map.put("resp_code", "failed");
								map.put("resp_message", "审核失败：" + obj.getString("message"));
							}

						} else if (status == HttpStatus.SC_MOVED_PERMANENTLY
								|| status == HttpStatus.SC_MOVED_TEMPORARILY) {

							Header locationHeader = postMethod.getResponseHeader("location");
							String location = null;
							if (locationHeader != null) {
								location = locationHeader.getValue();
								System.out.println("The page was redirected to:" + location);
							} else {
								System.err.println("Location field value is null.");
							}
						} else {
							System.out.println("fail======" + status);
						}
					} else {
						map.put("channel_type", "yb");
						map.put("resp_code", "failed");
						map.put("resp_message", "注册失败：" + obj.getString("message"));
					}
				} else if (status == HttpStatus.SC_MOVED_PERMANENTLY || status == HttpStatus.SC_MOVED_TEMPORARILY) {
					// 从头中取出转向的地址
					Header locationHeader = postMethod.getResponseHeader("location");
					String location = null;
					if (locationHeader != null) {
						location = locationHeader.getValue();
						System.out.println("The page was redirected to:" + location);
					} else {
						System.err.println("Location field value is null.");
					}
				} else {
					map.put("channel_type", "yb");
					map.put("resp_code", "failed");
					map.put("resp_message", "失败码：" + status);
				}
			} catch (Exception e) {
				map.put("channel_type", "yb");
				map.put("resp_code", "failed");
				map.put("resp_message", "失败信息：" + e.getMessage());
				e.printStackTrace();
				LOG.error("",e);
			} finally {
				// 释放连接
				postMethod.releaseConnection();
				return map;
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
}
