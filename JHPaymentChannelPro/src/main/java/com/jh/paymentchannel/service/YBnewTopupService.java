package com.jh.paymentchannel.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

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
import org.springframework.stereotype.Service;

import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.business.AreaNumberBusiness;
import com.jh.paymentchannel.business.RegisterAuthBusiness;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.AreaNumber;
import com.jh.paymentchannel.pojo.PaymentOrder;
import com.jh.paymentchannel.pojo.YBQuickRegister;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.yeepay.AESUtil;
import com.jh.paymentchannel.util.yeepay.Conts;
import com.jh.paymentchannel.util.yeepay.Digest;
import com.jh.paymentchannel.util.yeepay.Img2Small;

import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

@Service
public class YBnewTopupService extends BaseChannel implements TopupRequest {

	private static final Logger LOG = LoggerFactory.getLogger(YBnewTopupService.class);
	private String realnamePic = "/usr/share/nginx/photo";

	// private static String key = Conts.hmacKey; // 商户秘钥

	private static String key = "hf6Kjql0340f2769N82CCAlj0k23570W2uGP8Z2V4qeF9Z2B941hmio7K65w";

	@Autowired
	Util util;

	@Autowired
	private AreaNumberBusiness areaNumberBusiness;

	@Autowired
	private RegisterAuthBusiness registerAuthBusiness;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	@Override
	public Map<String, String> topupRequest(Map<String,Object> params) throws Exception {
		PaymentOrder paymentOrder = (PaymentOrder) params.get("paymentOrder");
		String ordercode = paymentOrder.getOrdercode();
		String amount = paymentOrder.getAmount().toString();
		
		Map maps = new HashMap();

		Map map = new HashMap();

		String notifyurl = ipAddress + "/v1.0/paymentchannel/topup/ybnew/notify_call";
		String returnurl = "http://1.xinli2017.applinzi.com/login/back.html";

		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");
		LOG.info("resultObj=====" + resultObj);

		String bankcard = resultObj.getString("bankcard");
		String userId = resultObj.getString("userid");
		String rate = resultObj.getString("rate");
		String extraFee = resultObj.getString("extraFee");
		String brandid = resultObj.getString("brandid");
		String phone = resultObj.getString("phone");
		
		if (extraFee.contains("."))
			extraFee = extraFee.substring(0, extraFee.indexOf("."));

		Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankcard, "0");
		Object object2 = queryBankCardByCardNo.get("result");
		fromObject = JSONObject.fromObject(object2);
		// resultObj = fromObject.getJSONObject("result");
		// log.info("resultObj====="+resultObj);

		String idcard = fromObject.getString("idcard");// 身份证号
		String userName = fromObject.getString("userName");// 用户姓名

		Map<String, Object> queryBankCardByUserId = this.queryBankCardByUserId(userId);
		Object object3 = queryBankCardByUserId.get("result");
		fromObject = JSONObject.fromObject(object3);
		// resultObj = fromObject.getJSONObject("result");
		// log.info("resultObj====="+resultObj);

		String telphone = fromObject.getString("phone");

		String bankNameJJ = fromObject.getString("bankName");// 借记卡银行名
		if ("平安银行".equals(bankNameJJ)) {
			bankNameJJ = "深圳发展银行";
		}
		if ("广发银行".equals(bankNameJJ)) {
			bankNameJJ = "广发银行股份有限公司";
		}
		if ("浦发银行".equals(bankNameJJ) || "上海浦东发展银行".equals(bankNameJJ) || "浦发银行信用卡中心(63100000)".equals(bankNameJJ)) {
			bankNameJJ = "浦东发展银行";
		}
		if ("邮政储蓄银行".equals(bankNameJJ) || "中国邮政储蓄银行信用卡中心(61000000)".equals(bankNameJJ)) {
			bankNameJJ = "邮储银行";
		}
		if ("广州银行股份有限公司(64135810)".equals(bankNameJJ) || "广州银行".equals(bankNameJJ)) {
			bankNameJJ = "广州银行股份有限公司";
		}
		String cardNo = fromObject.getString("cardNo");// 借记卡卡号
		String province = fromObject.getString("province");
		String city = fromObject.getString("city");
		AreaNumber areaNumber = null;
		String areano = null;
		/*
		 * if (!"null".equals(province) && null != province && !"".equals(province)) {
		 * 
		 * List<AreaNumber> queryAreaNumberByAll =
		 * areaNumberBusiness.queryAreaNumberByAll(province, city, "");
		 * 
		 * areaNumber = queryAreaNumberByAll.get(0); areano = areaNumber.getAreano();
		 * 
		 * }
		 */

		// RegisterAuth registerAuth =
		// registerAuthBusiness.getRegisterAuthByIdCard(idcard);

		YBQuickRegister ybQuickRegister = topupPayChannelBusiness.getYBQuickRegisterByIdCard(idcard);

		String rateCopy = rate;

		String mainCustomerNumber = "10015053457"; // 代理商编码
		String requestId = UUID.randomUUID().toString().substring(0, 15); // 注册请求号，每次请求唯一
		String customertype = "PERSON";// 企业-ENTERPRISE,个体工商户-INDIVIDUAL
		// 个人-PERSON
		String bindmobile = telphone;
		String signedname = userName;
		String linkman = "复归";
		String legalperson = userName;// 法人
		String minsettleamount = "1";
		String riskreserveday = "0";
		String bankaccountnumber = cardNo;
		String bankname = bankNameJJ;
		String accountname = userName;
		if (null == areano || "null".equals(areano) || "".equals(areano))
			areano = "1111";
		String areaCode = areano;

		if (ybQuickRegister == null) {
			LOG.info("用户需要进件======");

			File dir = new File(realnamePic + "/" + phone);
			// 创建目录
			if (dir.mkdirs()) {
				LOG.info("创建目录" + realnamePic + "/" + phone + "成功！");
				LOG.info("=================创建目录成功================");
			} else {
				File[] tempfiles = dir.listFiles();
				for (File file : tempfiles) {
					file.delete();
				}
				LOG.info("创建目录" + realnamePic + "/" + phone + "失败！");
			}
			File file = new File("/mnt/share/nginx/html/" + brandid + "/realname/" + phone);
			
			
			String[] filelist = file.list();
			if (filelist != null) {
				for (int i = 0; i < filelist.length; i++) {
					Img2Small.resizeImage(
							"/mnt/share/nginx/html/" + brandid + "/realname/" + phone + "/" + filelist[i],
							realnamePic + "/" + phone + "/" + filelist[i], 2000, 2000);
				}
			}
			
			file = new File(realnamePic + "/" + phone);
			filelist = file.list();
			File f1 = null;
			File f2 = null;
			File f3 = null;
			if (filelist.length > 2) {
				f1 = new File(realnamePic + "/" + phone + "/" + filelist[0]);
				f2 = new File(realnamePic + "/" + phone + "/" + filelist[1]);
				f3 = new File(realnamePic + "/" + phone + "/" + filelist[2]);
			} else {
				File fileCopy = new File("/mnt/share/nginx/html/-1/realname/");
				LOG.info("fileCopy======"+fileCopy);
				String[] fileCopyList = fileCopy.list();
//				if (filelist.length == 0) {
//					File fileCopy = new File("/mnt/share/nginx/html/-1/realname/");
//					LOG.info("fileCopy======"+fileCopy);
//					String[] fileCopyList = fileCopy.list();
//					
//					if(fileCopyList.length>0) {
//						for (int i = 0; i < fileCopyList.length; i++) {
//							Img2Small.resizeImage("/mnt/share/nginx/html/-1/realname/" + phone + "/" + fileCopyList[i],
//									realnamePic + "/" + phone + "/" + fileCopyList[i], 2000, 2000);
//						}
//					}else {
//						map.put("channel_type", "jf");
//						map.put("resp_code", "failed");
//						map.put("resp_message", "hehehe");
//						return map;
//					}
//					
//
//				}
//				file = new File(realnamePic + "/" + phone);
//				filelist = file.list();
				if (fileCopyList.length == 0) {
					map.put("channel_type", "jf");
					map.put("resp_code", "failed");
					map.put("resp_message", "失败原因：图片信息为空");
					return map;
				}
				if (fileCopyList.length != 0) {
					for (int i = 0; i < fileCopyList.length; i++) {
						if (i == 0) {
							f1 = new File("/mnt/share/nginx/html/-1/realname/" + fileCopyList[0]);
						} else if (i == 1) {
							f2 = new File("/mnt/share/nginx/html/-1/realname/" + fileCopyList[1]);
						} else if (i == 2) {
							f3 = new File("/mnt/share/nginx/html/-1/realname/" + fileCopyList[2]);
						}
					}
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
						.append(linkman == null ? "" : linkman).append(idcard == null ? "" : idcard)
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
				if (filelist.length > 2) {
					parts = new RegisterPartsBuilderss().setMainCustomerNumber(mainCustomerNumber)
							.setRequestId(requestId).setCustomerType(customertype).setBindMobile(bindmobile)
							.setSignedName(signedname).setLinkMan(linkman).setIdCard(idcard).setLegalPerson(legalperson)
							.setMinSettleAmount(minsettleamount).setRiskReserveDay(riskreserveday)
							.setBankAccountNumber(bankaccountnumber).setBankName(bankname).setAccountName(accountname)
							.setAreaCode(areaCode).setManualSettle(manualSettle).setBankCardPhoto(f1)
							.setIdCardBackPhoto(f2).setIdCardPhoto(f1).setPersonPhoto(f3).setHmac(hmac)
							.generateParams();
				} else {
					parts = new RegisterPartsBuilderss().setMainCustomerNumber(mainCustomerNumber)
							.setRequestId(requestId).setCustomerType(customertype).setBindMobile(bindmobile)
							.setSignedName(signedname).setLinkMan(linkman).setIdCard(idcard).setLegalPerson(legalperson)
							.setMinSettleAmount(minsettleamount).setRiskReserveDay(riskreserveday)
							.setBankAccountNumber(bankaccountnumber).setBankName(bankname).setAccountName(accountname)
							.setAreaCode(areaCode).setManualSettle(manualSettle).setBankCardPhoto(f1)
							.setIdCardBackPhoto(f2).setIdCardPhoto(f1).setPersonPhoto(f2).setHmac(hmac)
							.generateParams();
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
								boolean setfee = this.setfee(idcard, customerNumber, mainCustomerNumber, rateCopy, "1");
								this.setfee(idcard, customerNumber, mainCustomerNumber, "0", "2");
								this.setfee(idcard, customerNumber, mainCustomerNumber, "0", "3");
								this.setfee(idcard, customerNumber, mainCustomerNumber, "0", "4");
								this.setfee(idcard, customerNumber, mainCustomerNumber, "0", "5");

								if (setfee) {
									LOG.info("设置费率成功======");
									YBQuickRegister ybQuickRegister2 = new YBQuickRegister();
									ybQuickRegister2.setPhone(phone);
									ybQuickRegister2.setBankCard(cardNo);
									ybQuickRegister2.setIdCard(idcard);
									ybQuickRegister2.setCustomerNum(customerNumber);
									ybQuickRegister2.setRate(rate);
									ybQuickRegister2.setExtraFee(extraFee);
									ybQuickRegister2.setMainCustomerNum(mainCustomerNumber);
									
									topupPayChannelBusiness.createYBQuickRegister(ybQuickRegister2);
									
									map = (Map) pay(bankcard, telphone, notifyurl, returnurl, ordercode, mainCustomerNumber,
											customerNumber, amount);
									return map;
								} else {
									LOG.info("设置费率失败=====");
									map.put("channel_type", "jf");
									map.put("resp_code", "failed");
									map.put("resp_message", "设置费率失败!");
									return map;
								}

							} else {
								LOG.info("审核失败======" + obj.getString("message"));
								map.put("channel_type", "jf");
								map.put("resp_code", "failed");
								map.put("resp_message", "信息审核失败! 失败原因: " + obj.getString("message"));
								return map;
							}
						} else {
							LOG.info("请求失败======");
							map.put("channel_type", "jf");
							map.put("resp_code", "failed");
							map.put("resp_message", "交易排队中,请稍后重试!");
							return map;
						}

					} else {
						LOG.info("进件失败,失败原因为: " + obj.getString("message"));
						map.put("channel_type", "jf");
						map.put("resp_code", "failed");
						map.put("resp_message", "进件失败,失败原因为: " + obj.getString("message"));
						return map;
					}

				} else {
					LOG.info("请求失败======");
					map.put("channel_type", "jf");
					map.put("resp_code", "failed");
					map.put("resp_message", "交易排队中,请稍后重试!");
					return map;
				}

			} catch (Exception e) {
				LOG.error("请求失败======" + e);
				e.printStackTrace();LOG.error("",e);
				map.put("channel_type", "jf");
				map.put("resp_code", "failed");
				map.put("resp_message", "交易排队中,请稍后重试!");
				return map;
			}
		} else {
			if (!cardNo.equals(ybQuickRegister.getBankCard())) {
				LOG.info("需要修改进件信息======");
				boolean updateMerchant = updateMerchant(idcard, mainCustomerNumber, ybQuickRegister.getCustomerNum(),
						cardNo, bankname);

				if (updateMerchant) {
					ybQuickRegister.setBankCard(cardNo);
					ybQuickRegister.setPhone(phone);
					
					topupPayChannelBusiness.createYBQuickRegister(ybQuickRegister);
					
					if (!rate.equals(ybQuickRegister.getRate())) {

						boolean setfee = setfee(idcard, ybQuickRegister.getCustomerNum(), mainCustomerNumber, rate, "1");
						this.setfee(idcard, ybQuickRegister.getCustomerNum(), mainCustomerNumber, "0", "2");
						this.setfee(idcard, ybQuickRegister.getCustomerNum(), mainCustomerNumber, "0", "3");
						this.setfee(idcard, ybQuickRegister.getCustomerNum(), mainCustomerNumber, "0", "4");
						this.setfee(idcard, ybQuickRegister.getCustomerNum(), mainCustomerNumber, "0", "5");
						
						if (setfee) {
							
							ybQuickRegister.setRate(rate);
							topupPayChannelBusiness.createYBQuickRegister(ybQuickRegister);
							
							Map pay = (Map) pay(bankcard, telphone, notifyurl, returnurl, ordercode, mainCustomerNumber,
									ybQuickRegister.getCustomerNum(), amount);

							return pay;
						} else {
							LOG.info("设置费率失败=====");
							map.put("channel_type", "jf");
							map.put("resp_code", "failed");
							map.put("resp_message", "设置费率失败!");
							return map;
						}
					} else {

						Map pay = (Map) pay(bankcard, telphone, notifyurl, returnurl, ordercode, mainCustomerNumber,
								ybQuickRegister.getCustomerNum(), amount);

						return pay;
					}

				} else {
					LOG.info("修改进件信息失败=====");
					map.put("channel_type", "jf");
					map.put("resp_code", "failed");
					map.put("resp_message", "修改进件信息失败!");
					return map;
				}

			} else if (!rate.equals(ybQuickRegister.getRate())) {
				LOG.info("需要修改费率");
				boolean setfee = setfee(idcard, ybQuickRegister.getCustomerNum(), mainCustomerNumber, rate, "1");
				this.setfee(idcard, ybQuickRegister.getCustomerNum(), mainCustomerNumber, "0", "2");
				this.setfee(idcard, ybQuickRegister.getCustomerNum(), mainCustomerNumber, "0", "3");
				this.setfee(idcard, ybQuickRegister.getCustomerNum(), mainCustomerNumber, "0", "4");
				this.setfee(idcard, ybQuickRegister.getCustomerNum(), mainCustomerNumber, "0", "5");
				
				if (setfee) {
					
					ybQuickRegister.setRate(rate);
					topupPayChannelBusiness.createYBQuickRegister(ybQuickRegister);
					
					Map pay = (Map) pay(bankcard, telphone, notifyurl, returnurl, ordercode, mainCustomerNumber,
							ybQuickRegister.getCustomerNum(), amount);

					return pay;
				} else {
					LOG.info("设置费率失败=====");
					map.put("channel_type", "jf");
					map.put("resp_code", "failed");
					map.put("resp_message", "设置费率失败!");
					return map;
				}

			} else {

				Map pay = (Map) pay(bankcard, telphone, notifyurl, returnurl, ordercode, mainCustomerNumber,
						ybQuickRegister.getCustomerNum(), amount);

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
	public boolean setfee(String idCard, String customerNumber, String mainCustomerNumber, String rateCopy, String productType)
			throws Exception {

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
				/*RegisterAuth registerAuth = registerAuthBusiness.getRegisterAuthByIdCard(idCard);
				registerAuth.setRate(rateCopy);*/
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
				/*RegisterAuth registerAuth = registerAuthBusiness.getRegisterAuthByIdCard(idCard);
				registerAuth.setBankAccountNumber(cardNo);
				registerAuth.setBankName(bankname);*/
				istrue = true;
			} else {
				LOG.info("失败原因为: " + obj.getString("message"));
			}

		}

		return istrue;
	}

	// 发起交易
	public Object pay(String bankcard, String telphone, String notifyurl, String returnurl, String ordercode,
			String mainCustomerNumber, String customerNumber, String amount) throws Exception {

		Map<String, String> map = new HashMap<String, String>();
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
				map.put("redirect_url", url1);
				map.put("resp_code", "success");
				map.put("channel_type", "jf");
				return map;
			} else {
				map.put("channel_type", "jf");
				map.put("resp_code", "failed");
				map.put("resp_message", "交易失败：" + obj.getString("message"));
				return map;
			}
				
		}else {
			map.put("channel_type", "jf");
			map.put("resp_code", "failed");
			map.put("resp_message", "交易排队中,请稍后重试!");
			return map;
		}

		
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
