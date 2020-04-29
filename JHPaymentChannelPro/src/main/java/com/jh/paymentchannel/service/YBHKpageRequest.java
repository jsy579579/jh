package com.jh.paymentchannel.service;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.YBHKBindCard;
import com.jh.paymentchannel.pojo.YBHKRegister;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.ybhk.YeepayService;
import com.jh.paymentchannel.util.yeepay.Conts;
import com.jh.paymentchannel.util.yeepay.Digest;
import com.jh.paymentchannel.util.yeepay.Img2Small;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class YBHKpageRequest extends BaseChannel {

	private static final Logger LOG = LoggerFactory.getLogger(YBHKpageRequest.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private Util util;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	private static final Charset UTF_8 = StandardCharsets.UTF_8;

	private String realnamePic = "/usr/share/nginx/photo";

	private static String key = "Lp7L74br3H14C5k5ba4i7pA58C11doUu483Kv5Q8Ef85D7z205Pa5ii5hE74";

	private static String mainCustomerNumber = "10022572125";
	
	// 还款对接的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ybhk/to/repayment")
	public @ResponseBody Object HLJCRegister(HttpServletRequest request,
			@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "rate") String rate,
			@RequestParam(value = "extraFee") String extraFee, @RequestParam(value = "userId") String userId,
			@RequestParam(value = "returnUrl", required = false) String returnUrl) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0", userId);
		Object object2 = queryBankCardByCardNoAndUserId.get("result");
		JSONObject fromObject = JSONObject.fromObject(object2);

		String idCard = fromObject.getString("idcard");

		YBHKRegister ybhkRegister = topupPayChannelBusiness.getYBHKRegisterByIdCard(idCard);
		YBHKBindCard ybhkBindCard1 = topupPayChannelBusiness.getYBHKBindCardByBankCard(bankCard);
		
		if(ybhkRegister == null) {
			map = (Map<String, Object>) ybhkRegister(request, bankCard, rate, extraFee, userId);
			Object respCode = map.get("resp_code");
			Object respMessage = map.get("resp_message");
			LOG.info("respCode====="+respCode);
			
			if("000000".equals(respCode.toString())) {
				LOG.info("进件成功,开始绑卡======");
				map = (Map<String, Object>) ybhkBindCard(request, bankCard, returnUrl, userId);
				Object respCode1 = map.get("resp_code");
				Object respMessage1 = map.get("resp_message");
				if("000000".equals(respCode1.toString())) {
					
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, respMessage1);
					return maps;
				}else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, respMessage1);
					return maps;
				}
				
			}else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			}
			
		}else {
			
			if(ybhkBindCard1 == null || !"1".equals(ybhkBindCard1.getStatus())) {
				maps = (Map<String, Object>) ybhkBindCard(request, bankCard, returnUrl, userId);
				return maps;
			}
			
			else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "已完成进件和绑卡");
				return maps;
			}
			
		}
		
	}

	// 进件接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ybhk/register")
	public @ResponseBody Object ybhkRegister(HttpServletRequest request,
			@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "rate") String rate,
			@RequestParam(value = "extraFee") String extraFee, @RequestParam(value = "userId") String userId)
			throws Exception {
		LOG.info("开始进入进件接口========================");
		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryBankCardByUserId = this.queryBankCardByUserId(userId);
		if (!"000000".equals(queryBankCardByUserId.get("resp_code"))) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put("resp_message", "该通道需绑定默认提现借记卡!");
			return maps;
		}

		Object object3 = queryBankCardByUserId.get("result");
		JSONObject fromObject = JSONObject.fromObject(object3);

		String cardNo = fromObject.getString("cardNo");
		String userName = fromObject.getString("userName");
		String idcard = fromObject.getString("idcard");
		String phone = fromObject.getString("phone");
		String bankName = fromObject.getString("bankName");

		if ("平安银行".equals(bankName)) {
			bankName = "深圳发展银行";
		}
		if("广发银行".equals(bankName)) {
			bankName = "广发银行股份有限公司";
		}	
		if("浦发银行".equals(bankName) || "上海浦东发展银行".equals(bankName) || "浦发银行信用卡中心(63100000)".equals(bankName)) {
			bankName = "浦东发展银行";
		}
		if("邮政储蓄银行".equals(bankName) || "中国邮政储蓄银行信用卡中心(61000000)".equals(bankName)) {
			bankName = "邮储银行";
		}
		if("广州银行股份有限公司(64135810)".equals(bankName) || "广州银行".equals(bankName)) {
			bankName = "广州银行股份有限公司";
		}
		
		Map<String, Object> bankUnitNo = this.getBankUnitNo(Util.queryBankNameByBranchName(bankName));
		if (!"000000".equals(bankUnitNo.get("resp_code"))) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put("resp_message", bankUnitNo.get("resp_message"));
			return maps;
		}

		String inBankUnitNo = (String) bankUnitNo.get("result");

		Map<String, Object> bankCodeByBankName = this.getBankCodeByBankName(Util.queryBankNameByBranchName(bankName));
		if (!"000000".equals(bankCodeByBankName.get("resp_code"))) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put("resp_message", bankCodeByBankName.get("resp_message"));
			return maps;
		}

		String code = (String) bankCodeByBankName.get("result");

		String areano = null;
		String rateCopy = rate;
		// String mainCustomerNumber = "10022572125"; // 代理商编码
		String requestId = UUID.randomUUID().toString().substring(0, 15); // 注册请求号，每次请求唯一
		String customertype = "PERSON";// 企业-ENTERPRISE,个体工商户-INDIVIDUAL
		String bindmobile = phone;
		String signedname = userName;
		String linkman = "莘丽";
		String legalperson = userName;// 法人
		String minsettleamount = "1";
		String riskreserveday = "0";
		String bankaccountnumber = cardNo;
		String bankname = bankName;
		String accountname = userName;
		if (null == areano || "null".equals(areano) || "".equals(areano))
			areano = "1111";
		String areaCode = areano;

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
		File file = new File("/mnt/share/nginx/html/" + "2" + "/realname/" + phone);
		String[] filelist = file.list();
		if (filelist != null) {
			for (int i = 0; i < filelist.length; i++) {
				Img2Small.resizeImage("/mnt/share/nginx/html/" + "2" + "/realname/" + phone + "/" + filelist[i],
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
			LOG.info("fileCopy======" + fileCopy);
			String[] fileCopyList = fileCopy.list();
			if (fileCopyList.length == 0) {
				maps.put("channel_type", "jf");
				maps.put("resp_code", "failed");
				maps.put("resp_message", "失败原因：图片信息为空");
				return maps;
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
					.append(bankaccountnumber == null ? "" : bankaccountnumber).append(bankname == null ? "" : bankname)
					.append(accountname == null ? "" : accountname).append(areaCode == null ? "" : areaCode)
					.append(manualSettle == null ? "" : manualSettle);

			LOG.info("signature=======================" + signature.toString());
			String hmac = Digest.hmacSign(signature.toString(), key);
			LOG.info("hmac=======================" + hmac);
			Part[] parts = null;
			if (filelist.length > 2) {
				parts = new RegisterPartsBuilderss().setMainCustomerNumber(mainCustomerNumber).setRequestId(requestId)
						.setCustomerType(customertype).setBindMobile(bindmobile).setSignedName(signedname)
						.setLinkMan(linkman).setIdCard(idcard).setLegalPerson(legalperson)
						.setMinSettleAmount(minsettleamount).setRiskReserveDay(riskreserveday)
						.setBankAccountNumber(bankaccountnumber).setBankName(bankname).setAccountName(accountname)
						.setAreaCode(areaCode).setManualSettle(manualSettle).setBankCardPhoto(f1).setIdCardBackPhoto(f2)
						.setIdCardPhoto(f1).setPersonPhoto(f3).setHmac(hmac).generateParams();
			} else {
				parts = new RegisterPartsBuilderss().setMainCustomerNumber(mainCustomerNumber).setRequestId(requestId)
						.setCustomerType(customertype).setBindMobile(bindmobile).setSignedName(signedname)
						.setLinkMan(linkman).setIdCard(idcard).setLegalPerson(legalperson)
						.setMinSettleAmount(minsettleamount).setRiskReserveDay(riskreserveday)
						.setBankAccountNumber(bankaccountnumber).setBankName(bankname).setAccountName(accountname)
						.setAreaCode(areaCode).setManualSettle(manualSettle).setBankCardPhoto(f1).setIdCardBackPhoto(f2)
						.setIdCardPhoto(f1).setPersonPhoto(f2).setHmac(hmac).generateParams();
			}

			LOG.info("parts======" + parts);

			postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
			LOG.info(postMethod.toString());

			int status = client.executeMethod(postMethod);
			LOG.info("==========status==========" + status);

			if (status == HttpStatus.SC_OK) {
				String backinfo = postMethod.getResponseBodyAsString();
				LOG.info("==========postMethod.getResponseBodyAsString()==========" + backinfo);
				JSONObject obj = JSONObject.fromObject(backinfo);
				if (obj.getString("code").equals("0000")) {
					LOG.info("进件成功=======");
					String customerNumber = obj.getString("customerNumber");
					String message = obj.getString("message");

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
							boolean setfee = this.setfee(idcard, customerNumber, mainCustomerNumber, rateCopy, "8");
							
							
							String extraFees = null;
							if (extraFee.contains(".")) {
								extraFees = extraFee.substring(0, extraFee.indexOf("."));
							}
								
							
							 this.setfee(idcard, customerNumber, mainCustomerNumber, extraFees, "2");
							 this.setfee(idcard, customerNumber, mainCustomerNumber, extraFees, "3");
							 this.setfee(idcard, customerNumber, mainCustomerNumber, "0", "4");
							 this.setfee(idcard, customerNumber, mainCustomerNumber, "0", "5");
							 

							if (setfee) {
								LOG.info("设置费率成功======");

								YBHKRegister ybhkRegister = new YBHKRegister();
								ybhkRegister.setPhone(phone);
								ybhkRegister.setBankCard(cardNo);
								ybhkRegister.setIdCard(idcard);
								ybhkRegister.setCustomerNum(customerNumber);
								ybhkRegister.setRate(rate);
								ybhkRegister.setExtraFee(extraFee);
								ybhkRegister.setMainCustomerNum(mainCustomerNumber);

								topupPayChannelBusiness.createYBHKRegister(ybhkRegister);

								maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
								maps.put("channel_type", "sdj");
								maps.put(CommonConstants.RESP_MESSAGE, message);

							} else {
								LOG.info("设置费率失败=====");
								maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
								maps.put("channel_type", "sdj");
								maps.put("resp_message", "设置费率失败!");
								return maps;
							}

						}

					}

				}

			}

		} catch (Exception e) {
			LOG.error("请求失败======" + e);
			e.printStackTrace();
			LOG.error("",e);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put("resp_message", "交易排队中,请稍后重试!");
			return maps;
		}

		maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
		maps.put("channel_type", "sdj");
		maps.put("resp_message", "交易排队中,请稍后重试!");
		return maps;

	}

	// 银联绑卡接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ybhk/bindcard")
	public @ResponseBody Object ybhkBindCard(HttpServletRequest request,
			@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "returnUrl", required = false) String returnUrl,
			@RequestParam(value = "userId") String userId) throws Exception {
		LOG.info("开始进入绑卡接口======");
		Map<String, String> maps = new HashMap<String, String>();

		Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0", userId);
		Object object2 = queryBankCardByCardNoAndUserId.get("result");
		JSONObject fromObject = JSONObject.fromObject(object2);

		String phone = fromObject.getString("phone");
		String idCard = fromObject.getString("idcard");
		String userName = fromObject.getString("userName");
		String bankName = fromObject.getString("bankName");
		String securityCode = fromObject.getString("securityCode");
		String expiredTime = fromObject.getString("expiredTime");

		YBHKRegister ybhkRegister = topupPayChannelBusiness.getYBHKRegisterByIdCard(idCard);

		String customerNumber = ybhkRegister.getCustomerNum();
		String requestId = UUID.randomUUID().toString().substring(0, 15);
		String callbackUrl = ipAddress + "/v1.0/paymentchannel/topup/ybhk/bindcard/notify_call";
		String amount = "0";
		
		/*String mcc="5311"; 
		String src="B";*/
		 
		String bankCardNo = bankCard;
		String bindMobile = phone;
		
		/*String repayPlanNo="1";
		String repayPlanStage="1";
		
		String ip=request.getRemoteAddr();;
		String productName="充值缴费";*/

		//String[] before=new String[]{amount,bankCardNo,bindMobile,callbackUrl,customerNumber,mainCustomerNumber,ip,mcc,productName,repayPlanNo,repayPlanStage,src,requestId};
		String[] before = new String[] { amount, bankCardNo, bindMobile, callbackUrl, customerNumber,mainCustomerNumber, requestId };
		String hmac = YeepayService.madeHmac(before, key);
		String receiveApiURL = "https://skb.yeepay.com/skb-app/bindOrPay.action";
		LOG.info("请求地址======" + receiveApiURL);
		// 发送请求
		
		 // String[] reqskb=new String[]{mainCustomerNumber,customerNumber,requestId,amount,callbackUrl, bankCardNo,bindMobile,ip,mcc,src,productName,repayPlanNo,repayPlanStage,hmac}; 
		 
		  //String receiveApi[]={"mainCustomerNumber","customerNumber","requestId","amount", "callbackUrl","bankCardNo","bindMobile","ip","mcc","src","productName", "repayPlanNo","repayPlanStage","hmac"};
		 

		String[] reqskb = new String[] { mainCustomerNumber, customerNumber, requestId, amount, callbackUrl, bankCardNo, bindMobile, hmac };
		String receiveApi[] = { "mainCustomerNumber", "customerNumber", "requestId", "amount", "callbackUrl", "bankCardNo", "bindMobile", "hmac" };

		TreeMap<String, Object> responseMap = YeepayService.sendToSkb(reqskb, receiveApi, receiveApiURL);
		LOG.info("返回结果======" + responseMap);

		JSONObject fromObject2 = JSONObject.fromObject(responseMap);

		LOG.info("fromObject2======" + fromObject2);

		String code = fromObject2.getString("code");
		String message = fromObject2.getString("message");

		if ("0000".equals(code)) {
			String url = fromObject2.getString("url");

			maps.put(CommonConstants.RESP_CODE, "999996");
			maps.put(CommonConstants.RESP_MESSAGE, "首次使用需进行绑卡授权,点击确定进入授权页面!");
			maps.put(CommonConstants.RESULT, url);

			YBHKBindCard ybhkBindCard = topupPayChannelBusiness.getYBHKBindCardByBankCard(bankCardNo);

			if (ybhkBindCard == null) {

				YBHKBindCard ybhkBindCard1 = new YBHKBindCard();

				ybhkBindCard1.setPhone(phone);
				ybhkBindCard1.setBankCard(bankCardNo);
				ybhkBindCard1.setIdCard(idCard);
				ybhkBindCard1.setStatus("0");

				topupPayChannelBusiness.createYBHKBindCard(ybhkBindCard1);

			} else {

				ybhkBindCard.setBankCard(bankCardNo);
				ybhkBindCard.setStatus("0");

				topupPayChannelBusiness.createYBHKBindCard(ybhkBindCard);
			}

			return maps;
		} else {

		}

		return null;
	}

	// 修改交易费率接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ybhk/updatemerchant")
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

		return null;
	}


	// 快捷支付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ybhk/fastpay")
	public @ResponseBody Object ybhkFastPay(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode,
			@RequestParam(value = "mcc") String mcc
			) throws Exception {
		LOG.info("开始进入快捷支付接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");
		String realAmount = resultObj.getString("realAmount");
		String amount = resultObj.getString("amount");
		String userId = resultObj.getString("userid");

		Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0", userId);
		Object object2 = queryBankCardByCardNoAndUserId.get("result");
		fromObject = JSONObject.fromObject(object2);

		String phone = fromObject.getString("phone");
		String idCard = fromObject.getString("idcard");

		YBHKRegister ybhkRegister = topupPayChannelBusiness.getYBHKRegisterByIdCard(idCard);

		String customerNumber = ybhkRegister.getCustomerNum();
		String ip = request.getRemoteAddr();
		String src = "B";
		String cardLastNo = bankCard;
		String callBackUrl = ipAddress + "/v1.0/paymentchannel/topup/ybhk/fastpay/notify_call";
		String repayPlanNo = UUID.randomUUID().toString().substring(0, 15).replace("-", "");
		String repayPlanStage = "1";

		String productName = "hehehe";

		String[] before=new String[]{mainCustomerNumber,customerNumber,ordercode,realAmount,ip,mcc,src,cardLastNo,callBackUrl,productName,repayPlanNo,repayPlanStage};
	    String hmac=YeepayService.madeHmac(before,key);
	    String SecondpayApiURL = "https://skb.yeepay.com/skb-app/orderSecondPayApi.action";
	    System.out.println("请求地址："+SecondpayApiURL);
	    //发送请求
	    
	    LOG.info("请求报文======mainCustomerNumber="+mainCustomerNumber+",customerNumber="+customerNumber+",ordercode="+ordercode+",amount="+realAmount+",ip="+ip+",mcc="+mcc+",src="+src+",cardLastNo="+cardLastNo+",callBackUrl="+callBackUrl+",productName="+productName+",repayPlanNo="+repayPlanNo+",hmac="+hmac);
	    
	    String[] reqskb=new String[]{mainCustomerNumber,customerNumber,ordercode,realAmount,ip,mcc,src,cardLastNo,callBackUrl,productName,repayPlanNo,repayPlanStage,hmac};
	    String receiveApi[]={"mainCustomerNumber","customerNumber","requestId","amount","ip","mcc","src","cardLastNo","callBackUrl","productName","repayPlanNo","repayPlanStage","hmac"};

	    TreeMap<String, Object> responseMap=YeepayService.sendToSkb(reqskb,receiveApi,SecondpayApiURL);
		LOG.info("返回结果：" + responseMap);

		JSONObject fromObject2 = JSONObject.fromObject(responseMap);

		LOG.info("fromObject2======" + fromObject2);

		String code = fromObject2.getString("code");
		String message = fromObject2.getString("message");
		
		if("0000".equals(code)) {
			
			maps.put(CommonConstants.RESP_CODE, "999998");
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, "等待银行扣款中");
			return maps;
			
		}else {
			
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, "等待银行扣款中");
			return maps;
		}
		
	}

	// 代付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ybhk/transfer")
	public @ResponseBody Object ybhkTransfer(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode, 
			@RequestParam(value = "extra", required = false) String extra)
			throws Exception {
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
		String userId = resultObj.getString("userid");

		Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0", userId);
		Object object2 = queryBankCardByCardNoAndUserId.get("result");
		fromObject = JSONObject.fromObject(object2);

		String phone = fromObject.getString("phone");
		String idCard = fromObject.getString("idcard");
		String userName = fromObject.getString("userName");
		String idcard = fromObject.getString("idcard");
		String bankName = fromObject.getString("bankName");

		YBHKRegister ybhkRegister = topupPayChannelBusiness.getYBHKRegisterByIdCard(idcard);
		
		String customerNumber = ybhkRegister.getCustomerNum();
		//String externalNo = UUID.randomUUID().toString().substring(0, 15);
		String externalNo = ordercode;
		
		String transferWay = "1";
		if("T1".equals(extra)) {
			transferWay = "2";
		}
		
		String callBackUrl = ipAddress + "/v1.0/paymentchannel/topup/ybhk/transfer/notify_call";
		String bankAccountNum = bankCard;
		String salesProduct = "SKBDHT";
		
		String[] before=new String[]{realAmount,bankAccountNum,customerNumber,externalNo,mainCustomerNumber,transferWay,salesProduct,callBackUrl};
		String hmac = YeepayService.madeHmac(before,key);
		String withDrawApiURL = "https://skb.yeepay.com/skb-app/withDrawByCardApi.action";
		LOG.info("请求地址："+withDrawApiURL);
		//发送请求
		String[] reqskb=new String[]{realAmount,bankAccountNum,customerNumber,externalNo,mainCustomerNumber,transferWay,salesProduct,callBackUrl,hmac};		
	    String withDrawApiQuery[]={"amount","bankAccountNum","customerNumber","externalNo","mainCustomerNumber","transferWay","salesProduct","callBackUrl","hmac"};

		TreeMap<String, Object> responseMap=YeepayService.sendToSkb(reqskb,withDrawApiQuery,withDrawApiURL);
		LOG.info("返回结果："+responseMap);
		
		JSONObject fromObject2 = JSONObject.fromObject(responseMap);

		LOG.info("fromObject2======" + fromObject2);

		String code = fromObject2.getString("code");
		String message = fromObject2.getString("message");
		
		
		if("0000".equals(code)) {
			String externalNo1 = fromObject2.getString("externalNo");
			
			String url = "http://transactionclear/v1.0/transactionclear/payment/update/thirdordercode";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("order_code", ordercode);
			requestEntity.add("third_code", externalNo1);
			String result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("接口/v1.0/transactionclear/payment/update/thirdordercode====RESULT================" + result);
			
			maps.put(CommonConstants.RESP_CODE, "999998");
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, "等待银行出款中");
			return maps;
			
		}else {
			
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put("channel_type", "sdj");
			maps.put(CommonConstants.RESP_MESSAGE, "等待银行出款中");
			return maps;
		}
		
	}

	
	// 订单查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ybhk/ordercodequery")
	public @ResponseBody Object orderCodeQuery(HttpServletRequest request,
			@RequestParam(value = "orderCode") String ordercode) throws Exception {
		LOG.info("开始进入订单查询接口======");
		Map<String, String> maps = new HashMap<String, String>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");
		String createTime = resultObj.getString("createTime");

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date dd = df.parse(createTime);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dd);
		calendar.add(Calendar.HOUR, 2);
		
		String format = df.format(calendar.getTime());
		
		Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
		Object object2 = queryBankCardByCardNo.get("result");
		fromObject = JSONObject.fromObject(object2);

		String idCard = fromObject.getString("idcard");

		YBHKRegister ybhkRegister = topupPayChannelBusiness.getYBHKRegisterByIdCard(idCard);
		
		Map<String,String>  map=new LinkedHashMap<String,String>();
		map.put("mainCustomerNumber",mainCustomerNumber);
		map.put("customerNumber",ybhkRegister.getCustomerNum());
		map.put("requestId",ordercode);
		map.put("createTimeBegin",createTime);
		map.put("createTimeEnd",format);
		map.put("payTimeBegin","");
		map.put("payTimeEnd","");
		map.put("lastUpdateTimeBegin","");
		map.put("lastUpdateTimeEnd","");
		map.put("status","");
		map.put("busiType","");
		map.put("pageNo","1");
		String hmac=YeepayService.makeHmac(map,key);
		map.put("hmac",hmac);
		LOG.info("添加所有参数的map："+map);
			List< NameValuePair > list=new ArrayList<NameValuePair>();
		for(Map.Entry<String, String> entry : map.entrySet()) {
			String key		= entry.getKey();

			String value	= entry.getValue();

			list.add(new NameValuePair(key, format(value)));
		}
		NameValuePair[]  pairs	= new NameValuePair[list.size()];
		for(int i=0;i<pairs.length;i++){
			pairs[i]=list.get(i);

		}
		String tradeReviceQueryURL="https://skb.yeepay.com/skb-app/queryOrderApi.action";
		LOG.info("请求地址："+tradeReviceQueryURL);

		TreeMap<String, Object> responsedateMap=YeepayService.send(pairs,tradeReviceQueryURL);
		LOG.info("返回结果："+responsedateMap);
		
		JSONObject fromObject2 = JSONObject.fromObject(responsedateMap);
		
		LOG.info("fromObject2======"+fromObject2);
		
		String code = fromObject2.getString("code");
		String message = fromObject2.getString("message");
		
		if("0000".equals(code)) {
			
			JSONArray jsonArray = fromObject2.getJSONArray("tradeReceives");
			
			LOG.info("jsonArray======"+jsonArray);
			
			JSONObject json = null;
			String status = null;
			if(jsonArray.size()>0) {
				json = (JSONObject) jsonArray.get(0);
				status = json.getString("status");
			
			}else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "查询失败");
				return maps;
			}
			
			
			
			if("SUCCESS".equalsIgnoreCase(status)) {
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "支付成功");
				return maps;
				
			}else if("FAIL".equalsIgnoreCase(status) || "FAILED".equals(status)) {
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "支付失败");
				return maps;
				
			}else {
				
				maps.put(CommonConstants.RESP_CODE, "999998");
				maps.put(CommonConstants.RESP_MESSAGE, "等待支付");
				return maps;
				
			}
			
		}else {
			
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, message);
			return maps;
			
		}
		
	}

	// 代付查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ybhk/transferquery")
	public @ResponseBody Object transferQuery(HttpServletRequest request,
			@RequestParam(value = "orderCode") String ordercode,
			@RequestParam(value = "transferWay", required = false) String transferWay
			) throws Exception {
		LOG.info("开始进入代付查询接口======");

		Map<String, String> maps = new HashMap<String, String>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");
		String createTime = resultObj.getString("createTime");

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date dd = df.parse(createTime);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dd);
		calendar.add(Calendar.HOUR, 2);//加一天
		
		String format = df.format(calendar.getTime());
		
		Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
		Object object2 = queryBankCardByCardNo.get("result");
		fromObject = JSONObject.fromObject(object2);

		String idCard = fromObject.getString("idcard");

		YBHKRegister ybhkRegister = topupPayChannelBusiness.getYBHKRegisterByIdCard(idCard);

		Map<String,String> map=new LinkedHashMap<String,String>();

		map.put("customerNumber",ybhkRegister.getCustomerNum());
		map.put("externalNo",ordercode);
		map.put("mainCustomerNumber",mainCustomerNumber);
		map.put("pageNo","1");
		map.put("requestDateSectionBegin",createTime);
		map.put("requestDateSectionEnd",format);
		//map.put("serialNo",serialNo);
		//map.put("transferStatus",transferStatus);
		
		if(transferWay != null) {
			
			map.put("transferWay",transferWay);
		}else {
			
			map.put("transferWay","1");
		}
		

		String hmac=YeepayService.makeHmac(map,key);
		map.put("hmac",hmac);
		System.out.print("添加所有参数的map："+map);
		List<NameValuePair> list=new ArrayList<NameValuePair>();
		for(Map.Entry<String, String> entry : map.entrySet()) {
			String key		= entry.getKey();
			String value	= entry.getValue();
			list.add(new NameValuePair(key, format(value)));
		}
		NameValuePair[]  pairs	= new NameValuePair[list.size()];
		for(int i=0;i<pairs.length;i++){
			pairs[i]=list.get(i);
		}
		String transferQueryURL="https://skb.yeepay.com/skb-app/transferQuery.action";
		LOG.info("请求地址："+transferQueryURL);

		TreeMap<String, Object> responsedateMap=YeepayService.send(pairs,transferQueryURL);
		LOG.info("返回结果："+responsedateMap);
		
		JSONObject fromObject2 = JSONObject.fromObject(responsedateMap);
		
		LOG.info("fromObject2======"+fromObject2);
		
		String code = fromObject2.getString("code");
		String message = fromObject2.getString("message");
		
		if("0000".equals(code)) {
			
			JSONArray jsonArray = fromObject2.getJSONArray("transferRequests");
			
			LOG.info("jsonArray======"+jsonArray);
			
			JSONObject json = null;
			String status = null;
			if(jsonArray.size()>0) {
				json = (JSONObject) jsonArray.get(0);
				status = json.getString("transferStatus");
			
			}else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "查询失败");
				return maps;
			}
			
			
			
			if("SUCCESSED".equalsIgnoreCase(status)) {
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "代付成功");
				return maps;
				
			}else if("FAILED".equalsIgnoreCase(status)) {
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "代付失败");
				return maps;
				
			}else {
				
				maps.put(CommonConstants.RESP_CODE, "999998");
				maps.put(CommonConstants.RESP_MESSAGE, "等待出款");
				return maps;
				
			}
			
		}else {
			
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, message);
			return maps;
			
		}
		
	}

	// 代付异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ybhk/transfer/notify_call")
	public void hljcTransferNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("代付异步通知进来了=======");

		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}
		
		String transferStatus = request.getParameter("transferStatus");
		String externalNo = request.getParameter("externalNo");
		
		if("SUCCESSED".equals(transferStatus)) {
			
			String url = "http://creditcardmanager/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("orderCode", externalNo);
			requestEntity.add("version", "7");
			String result = null;
			JSONObject jsonObject;
			JSONObject resultObj;
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("RESULT================" + result);
				jsonObject = JSONObject.fromObject(result);
				resultObj = jsonObject.getJSONObject("result");
			} catch (Exception e) {
				e.printStackTrace();LOG.error("",e);
			}

			url = "http://transactionclear/v1.0/transactionclear/payment/update";

			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", externalNo);
			requestEntity.add("third_code", "");
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();LOG.error("",e);
			}
			
			
			
			LOG.info("订单状态修改成功==================="+externalNo+"====================" + result);

			LOG.info("订单已代付!");
			
			PrintWriter pw = response.getWriter();
			pw.print("SUCCESS");
			pw.close();
			
		}

	}

	
	// 快捷支付异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ybhk/fastpay/notify_call")
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

		String code = request.getParameter("code");
		String status = request.getParameter("status");
		String requestId = request.getParameter("requestId");
		
		if("SUCCESS".equals(status)) {
			
			String url = "http://creditcardmanager/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("orderCode", requestId);
			requestEntity.add("version", "7");
			String result = null;
			JSONObject jsonObject;
			JSONObject resultObj;
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("RESULT================" + result);
				jsonObject = JSONObject.fromObject(result);
				resultObj = jsonObject.getJSONObject("result");
			} catch (Exception e) {
				e.printStackTrace();LOG.error("",e);
			}

			url = "http://transactionclear/v1.0/transactionclear/payment/update";

			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", requestId);
			requestEntity.add("third_code", "");
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();LOG.error("",e);
			}

			LOG.info("订单状态修改成功==================="+requestId+"====================" + result);

			LOG.info("订单已支付!");
			
			PrintWriter pw = response.getWriter();
			pw.print("SUCCESS");
			pw.close();
			
		}
		
		

	}

	// 绑卡异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ybhk/bindcard/notify_call")
	public void hljcBindCardNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("绑卡异步通知进来了=======");

		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}

		String code = request.getParameter("code");
		String message = request.getParameter("message");
		String bankCardNo = request.getParameter("bankCardNo");
		String bindStatus = request.getParameter("bindStatus");

		if ("0000".equals(code) && "BIND_SUCCESS".equals(bindStatus)) {

			YBHKBindCard ybhkBindCard = topupPayChannelBusiness.getYBHKBindCardByBankCard(bankCardNo);

			ybhkBindCard.setStatus("1");

			topupPayChannelBusiness.createYBHKBindCard(ybhkBindCard);
		}

	}

	// 审核接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ybhk/hfaushk")
	public @ResponseBody Object ebgsfijh(HttpServletRequest request,
			@RequestParam(value = "customerNumber") String customerNumber) throws Exception {

		String stat = "SUCCESS";
		PostMethod postMethod = new PostMethod(Conts.baseRequestUrl + "/auditMerchant.action");
		HttpClient client = new HttpClient();
		StringBuilder hmacStr = new StringBuilder();
		hmacStr.append(customerNumber == null ? "" : customerNumber)
				.append(mainCustomerNumber == null ? "" : mainCustomerNumber).append(stat == null ? "" : stat);
		String hmac = Digest.hmacSign(hmacStr.toString(), key);
		Part[] parts2 = new AuditMerchantPartsBuilders().setMainCustomerNumber(mainCustomerNumber)
				.setCustomerNumber(customerNumber).setStatus(stat).setHmac(hmac).generateParams();

		postMethod.setRequestEntity(new MultipartRequestEntity(parts2, postMethod.getParams()));

		int status = client.executeMethod(postMethod);
		LOG.info("==========status==========" + status);
		if (status == HttpStatus.SC_OK) {
			String backinfo = postMethod.getResponseBodyAsString();
			LOG.info("==========backinfo2==========" + backinfo);
			JSONObject obj = JSONObject.fromObject(backinfo);
		}

		return null;
	}

	
	//查询余额接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ybhk/balancequery")
	public @ResponseBody Object balanceQuery(HttpServletRequest request,
			@RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "balanceType") String balanceType
			) throws Exception {
		
		Map<String,Object> map = new HashMap<String, Object>();
		
		YBHKRegister ybhkRegister = topupPayChannelBusiness.getYBHKRegisterByIdCard(idCard);
		
		String customerNumber = ybhkRegister.getCustomerNum();
		
		String[] before=new String[]{mainCustomerNumber,customerNumber,balanceType};
		String hmac=YeepayService.madeHmac(before,key);
		String customerBalanceQueryURL="https://skb.yeepay.com/skb-app/customerBalanceQuery.action";
		LOG.info("请求地址："+customerBalanceQueryURL);
		//发送请求
		String[] reqstrskb=new String[]{mainCustomerNumber,customerNumber,balanceType,hmac};		
		String customerBalanceQuery[]={"mainCustomerNumber","customerNumber","balanceType","hmac"};		
		TreeMap<String, Object> responseMap=YeepayService.sendToSkb(reqstrskb,customerBalanceQuery,customerBalanceQueryURL);
		LOG.info("返回结果："+responseMap);
		
		String code = (String) responseMap.get("code");
		String message = (String) responseMap.get("message");
		
		if("0000".equals(code)) {
			
			String balance = (String) responseMap.get("balance");
			
			if("1".equals(balanceType)) {
				
				return ResultWrap.init(CommonConstants.SUCCESS, "T0 自助结算可用余额: " + balance);
			
			}else if("2".equals(balanceType)){
				
				return ResultWrap.init(CommonConstants.SUCCESS, "T1 自助结算可用余额: " + balance);
			
			}else {
				
				return ResultWrap.init(CommonConstants.SUCCESS, "账户余额: " + balance);
				
			}
			
		}else {
			
			return ResultWrap.init(CommonConstants.FALIED, message);
		}
		
	}
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ybhk/fewfwaef")
	public @ResponseBody Object fsdgfsefd(HttpServletRequest request, @RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "customerNumber") String customerNumber,
			@RequestParam(value = "mainCustomerNumber") String mainCustomerNumber,
			@RequestParam(value = "rateCopy") String rateCopy, @RequestParam(value = "productType") String productType)
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

				istrue = true;
			} else {
				LOG.info("失败原因为: " + obj.getString("message"));
			}
		}

		return istrue;

		// return null;
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

				istrue = true;
			} else {
				LOG.info("失败原因为: " + obj.getString("message"));
			}
		}

		return istrue;
	}

	
	private static NameValuePair[] param2 = {
			// 大商户编号
			new NameValuePair("mainCustomerNumber", ""),
			/*
			 * // 小商户编号 new NameValuePair("customerNumber", ""),
			 */
			// 出款订单号
			new NameValuePair("requestId", ""),

			new NameValuePair("createTimeBegin", ""), new NameValuePair("pageNo", ""),
			// 签名串
			new NameValuePair("hmac", ""),

	};

	private static String hmacSign2() {
		StringBuilder hmacStr = new StringBuilder();
		for (NameValuePair nameValuePair : param2) {
			if (nameValuePair.getName().equals("hmac")) {
				continue;
			}
			hmacStr.append(nameValuePair.getValue() == null ? "" : nameValuePair.getValue());

		}

		String hmac = Digest.hmacSign(hmacStr.toString(),
				"oF34lTpB9x9v05D2B0eP1r18EDX71THlT4Go5X0s6V7T85gh2J63j30iPh38");

		return hmac;
	}
	public  String format(String text){
		return text==null?"":text.trim();
	}
}
