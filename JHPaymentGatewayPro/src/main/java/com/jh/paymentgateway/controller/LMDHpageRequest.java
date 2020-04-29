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
import com.jh.paymentgateway.pojo.GHTBindCard;
import com.jh.paymentgateway.pojo.LMBankNum;
import com.jh.paymentgateway.pojo.LMDHBindCard;
import com.jh.paymentgateway.pojo.LMDHRegister;
import com.jh.paymentgateway.pojo.LMTAddress;
import com.jh.paymentgateway.pojo.LMTRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.RegisterPartsBuilderss;
import com.jh.paymentgateway.util.MD5Util;
import com.jh.paymentgateway.util.PhotoCompressUtil;
import com.jh.paymentgateway.util.lmt.UnicodeUtils;
import com.jh.paymentgateway.util.ytjf.CHexConver;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class LMDHpageRequest extends BaseChannel {
	private static final Logger LOG = LoggerFactory.getLogger(LMDHpageRequest.class);

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

	// 跟还款对接的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/lmdh/torepayment")
	public @ResponseBody Object ghtdhToRepayment(HttpServletRequest request,
			@RequestParam(value = "bankCard") String bankCard, 
			@RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "phone") String phone, 
			@RequestParam(value = "bankName") String bankName,
			@RequestParam(value = "userName") String userName, 
			@RequestParam(value = "expiredTime") String expiredTime,
			@RequestParam(value = "securityCode") String securityCode) throws Exception {

		LMDHBindCard lmdhBindCardByBankCard = topupPayChannelBusiness.getLMDHBindCardByBankCard(bankCard);

		Map<String, Object> maps = new HashMap<String, Object>();
		
		LMDHRegister lmdhRegister = topupPayChannelBusiness.getlmdhRegisterByidCard(idCard);
		if (lmdhRegister==null) {
			maps = (Map<String, Object>) this.Register(userName,idCard,phone);
			if (!"000000".equals(maps.get("resp_code"))) {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "进件失败");
			   return maps;
			}
		}
		
		if (lmdhBindCardByBankCard == null) {
			return ResultWrap.init("999996", "用户需要进行绑卡授权操作",
					ip + "/v1.0/paymentgateway/topup/tolm/bindcard?bankName=" + URLEncoder.encode(bankName, "UTF-8")
					+ "&cardType=" + URLEncoder.encode("信用卡", "UTF-8") 
					+ "&bankCard=" + bankCard 
					+ "&phone=" + phone 
					+ "&userName=" + URLEncoder.encode(userName, "UTF-8") 
					+ "&idCard=" + idCard
					+ "&expiredTime=" + expiredTime 
					+ "&securityCode=" + securityCode 
					+ "&ipAddress=" + ip);
		} else {
			if (!"1".equals(lmdhBindCardByBankCard.getStatus())) {
				return ResultWrap.init("999996", "用户需要进行绑卡授权操作",
						ip + "/v1.0/paymentgateway/topup/tolm/bindcard?bankName=" + URLEncoder.encode(bankName, "UTF-8")
								+ "&cardType=" + URLEncoder.encode("信用卡", "UTF-8") 
								+ "&bankCard=" + bankCard 
								+ "&phone=" + phone 
								+ "&userName=" + URLEncoder.encode(userName, "UTF-8") 
								+ "&idCard=" + idCard
								+ "&expiredTime=" + expiredTime 
								+ "&securityCode=" + securityCode 
								+ "&ipAddress=" + ip);
			} else {

				return ResultWrap.init(CommonConstants.SUCCESS, "已完成鉴权验证!");
			}
		}

	}
	
	/**
	 * 跳转到绑卡页面
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/tolm/bindcard")
	public String JumpBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		LOG.info("LMDHBindCard------------------跳转到绑卡界面");
		//bankName  cardType  bankCard  phone  userName  idCard  expiredTime  securityCode  ipAddress
		String bankName = request.getParameter("bankName");
		String cardType = request.getParameter("cardType");
		String bankCard = request.getParameter("bankCard");
		String phone = request.getParameter("phone");
		String userName = request.getParameter("userName");
		String idCard = request.getParameter("idCard");
		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("bankName", bankName);
		model.addAttribute("cardType", cardType);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("phone", phone);
		model.addAttribute("userName", userName);
		model.addAttribute("idCard", idCard);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("ipAddress", ipAddress);
		return "lmdhbindcard";
	}

	/**
	 * 进件
	 * */
	@SuppressWarnings("null")
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/lmdh/register")
	public @ResponseBody Object Register(@RequestParam("userName") String userName,
			@RequestParam("idCard") String idCard,
			@RequestParam("phone") String phone) throws IOException {
		Map<String, Object> maps = new HashMap<>();

		String requestNo = "XinLi" + System.currentTimeMillis();
		LOG.info("===register请求订单号：" + requestNo);

		SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
		parameters.put("mch_id", mch_id);
		parameters.put("app_id", app_id);
		parameters.put("mch_no", requestNo);
		parameters.put("realname", userName);
		parameters.put("id_card", idCard);
		String mySign = createSign(characterEncoding, parameters, key);
		LOG.info("=======================设置签名" + mySign);
		Part[] parts = null;
		try {
			parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(requestNo)
					.setSign(mySign).setRealname(userName).setidCard(idCard).generateParams();
		} catch (Exception e) {
			LOG.info("=======================设置签名出错");
		}

		PostMethod postMethod = new PostMethod(
				"https://pay.longmaoguanjia.com/api/pay/large_pay_x/sub_merchant_register");
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
				LMDHRegister lmdh = new LMDHRegister();
				lmdh.setMainCustomerNum(mch_id);
				lmdh.setCustomerNum(merchantNo);
				lmdh.setIdCard(idCard);
				lmdh.setUserName(userName);
				lmdh.setPhone(phone);
				topupPayChannelBusiness.createlmdhRegister(lmdh);

				LOG.info("---------------------- 龙猫代还进件成功 ----------------------");

				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "进件成功");
				return maps;
			} else {
				LOG.error("龙猫代还进件失败======" + requestNo);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			}
		} else {
			LOG.error("龙猫代还进件接口出现异常======连接超时");
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "请求超时");
			return maps;
		}
	}

	/**
	 * 申请绑卡/签约
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/lmdh/bindcard")
	public @ResponseBody Object bindcard(
			@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "phone") String phone,
			@RequestParam(value = "userName") String userName,
			@RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "expiredTime") String expiredTime,
			@RequestParam(value = "securityCode") String securityCode
			) throws IOException {
		LOG.info("====================lmdh申请绑卡/签约====================");
		
		LMDHRegister lmdhRegister = topupPayChannelBusiness.getlmdhRegisterByidCard(idCard);

		Map<String, Object> maps = new HashMap<>();

		SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
		
		String requestNo = "XinLi" + System.currentTimeMillis();
		LOG.info("===申请绑卡请求订单号：" + requestNo);

		parameters.put("mch_id", mch_id);
		parameters.put("app_id", app_id);
		parameters.put("mch_no", requestNo);
		parameters.put("sub_mch_no", lmdhRegister.getCustomerNum());
		parameters.put("bank_card_no", bankCard);
		parameters.put("bank_card_mobile", phone);

		String expired = this.expiredTimeToMMYY(expiredTime);
		parameters.put("expire_date", expired);// mmyy

		parameters.put("cvn2", securityCode);

		String mySign = createSign(characterEncoding, parameters, key);
		LOG.info("====pay签名报文：" + mySign.toString());
		Part[] parts = null;
		parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(requestNo)
				.setSubM(lmdhRegister.getCustomerNum()).setCardNo(bankCard).setPhone(phone).setExpire(expired)
				.setSecurityCode(securityCode).setSign(mySign).generateParams();

		PostMethod postMethod = new PostMethod("https://pay.longmaoguanjia.com/api/pay/large_pay_x/sub_merchant_sign");
		HttpClient client = new HttpClient();
		postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

		int status = client.executeMethod(postMethod);
		LOG.info("==========status==========" + status);

		if (status == HttpStatus.SC_OK) {
			String backinfo = postMethod.getResponseBodyAsString();
			LOG.info("==========申请绑卡返回响应==========" + backinfo);
			JSONObject jsonstr = JSONObject.fromObject(backinfo);
			String respCode = jsonstr.getString("code");
			String respMessage = jsonstr.getString("message");
			if (respCode.equals("0")) { //
				LOG.info("------------------- 申请绑卡成功 -------------------");
				LMDHBindCard lmdhBindCard = topupPayChannelBusiness.getLMDHBindCardByBankCard(bankCard);
				if (lmdhBindCard == null) {
					LMDHBindCard lmdhbindCard = new LMDHBindCard();
					lmdhbindCard.setIdCard(idCard);
					lmdhbindCard.setPhone(phone);
					lmdhbindCard.setStatus("0");
					lmdhbindCard.setBankCard(bankCard);
					lmdhbindCard.setUserName(userName);
					lmdhbindCard.setCreateTime(new Date());
					topupPayChannelBusiness.createLMDHBindCard(lmdhbindCard);
				}
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			} else {
				LOG.info("LMDH申请绑卡失败：" + respMessage + ",卡号：" + bankCard);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				
				return maps;
			}
		} else {
			LOG.info("LMDH申请绑卡请求超时卡号：" + bankCard);
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "请求超时");
			return maps;
		}
	}

	/**
	 * 确认绑卡/确认签约
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/lmdh/sure/bindcard")
	public @ResponseBody Object sureBindcard(@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "smsCode") String smsCode) throws IOException {
		LOG.info("====================lmdh确认绑卡/确认签约====================");

		String requestNo = "XinLi" + System.currentTimeMillis();
		LOG.info("===确认绑卡请求订单号：" + requestNo);
		
		LMDHRegister lmdhRegister = topupPayChannelBusiness.getlmdhRegisterByidCard(idCard);

		Map<String, Object> maps = new HashMap<>();

		SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();

		parameters.put("mch_id", mch_id);
		parameters.put("app_id", app_id);
		parameters.put("mch_no", requestNo);
		parameters.put("sub_mch_no", lmdhRegister.getCustomerNum());
		parameters.put("bank_card_no", bankCard);
		parameters.put("code", smsCode);

		String mySign = createSign(characterEncoding, parameters, key);
		LOG.info("====pay签名报文：" + mySign.toString());
		Part[] parts = null;
		parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(requestNo)
				.setSubM(lmdhRegister.getCustomerNum()).setCardNo(bankCard).setSmsCode(smsCode).setSign(mySign)
				.generateParams();

		PostMethod postMethod = new PostMethod(
				"https://pay.longmaoguanjia.com/api/pay/large_pay_x/sub_merchant_sign_confirm");
		HttpClient client = new HttpClient();
		postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

		int status = client.executeMethod(postMethod);
		LOG.info("==========status==========" + status);

		if (status == HttpStatus.SC_OK) {
			String backinfo = postMethod.getResponseBodyAsString();
			LOG.info("==========确认绑卡返回响应==========" + backinfo);
			JSONObject jsonstr = JSONObject.fromObject(backinfo);
			String respCode = jsonstr.getString("code");
			String respMessage = jsonstr.getString("message");
			if (respCode.equals("0")) { //
				LOG.info("------------------- 确认绑卡成功 -------------------");

				LMDHBindCard lmdhBindCard = topupPayChannelBusiness.getLMDHBindCardByBankCard(bankCard);
				lmdhBindCard.setStatus("1");
				lmdhBindCard.setChangeTime(new Date());
				topupPayChannelBusiness.createLMDHBindCard(lmdhBindCard);

				maps.put("redirect_url", "http://www.shanqi111.cn/v1.0/paymentchannel/topup/wmyk/bindcardsuccess");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			} else {
				LOG.info("LMDH确认绑卡失败：" + respMessage + ",卡号：" + bankCard);
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			}
		} else {
			LOG.info("LMDH确认绑卡请求超时卡号：" + bankCard);
			
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "请求超时");
			return maps;
		}
	}

	/**
	 * 交易查询
	 * 
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/lmdh/trade")
	public @ResponseBody Object trade(@RequestParam(value = "orderCode") String orderCode) throws IOException {
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String idCard = prp.getIdCard();
		LMDHRegister lmdh = topupPayChannelBusiness.getlmdhRegisterByidCard(idCard);
		Map<String, Object> maps = new HashMap<>();
		SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();

		parameters.put("mch_id", mch_id);
		parameters.put("app_id", app_id);
		parameters.put("mch_no", orderCode);
		parameters.put("query_type", "trade");// trade：交易       withdraw：提现
		parameters.put("sub_mch_no", lmdh.getCustomerNum());
		String mySign = createSign(characterEncoding, parameters, key);
		LOG.info("====pay签名报文：" + mySign.toString());
		Part[] parts = null;
		parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(orderCode)
				.setQuery_type("trade").setSubM(lmdh.getCustomerNum()).setSign(mySign).generateParams();

		PostMethod postMethod = new PostMethod("https://pay.longmaoguanjia.com/api/pay/large_pay_x/query");
		HttpClient client = new HttpClient();
		postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

		int status = client.executeMethod(postMethod);
		LOG.info("==========status==========" + status);

		if (status == HttpStatus.SC_OK) {
			String backinfo = postMethod.getResponseBodyAsString();
			LOG.info("==========交易查询返回响应==========" + backinfo);
			JSONObject jsonstr = JSONObject.fromObject(backinfo);
			String respCode = jsonstr.getString("code");
			String respMessage = jsonstr.getString("message");
			if (respCode.equals("1030009")) {
				LOG.info("1030009：" + "交易成功" + "，订单号:" + orderCode);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respCode + ":" + respMessage);
				return maps;
			} else if (respCode.equals("1030008")) {
				LOG.info("1030008：" + "交易失败" + "，订单号:" + orderCode);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respCode + ":" + respMessage);
				return maps;
			} else if (respCode.equals("1030004")) {
				LOG.info("1030004：" + "商户订单号不存在" + "，订单号:" + orderCode);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respCode + ":" + respMessage);
				return maps;
			} else if (respCode.equals("1030012")) {
				LOG.info("1030012：" + "交易不存在" + "，订单号:" + orderCode);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respCode + ":" + respMessage);
				return maps;
			} else if(respCode.equals("1030011")){
				LOG.info("1030011：" + "交易已创建,未支付" + "，订单号:" + orderCode);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respCode + ":" + respMessage);
				return maps;
			} else {
				LOG.info("1050000不存在该查询类型   1030004商户订单号不存在   1030011交易已创建    1030009交易成功    1030008交易失败     1030012交易不存在");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respCode + ":" + respMessage);
				return maps;
			}
		}
		return maps;
	}
	
	/**
	 * 提现查询
	 * 
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/lmdh/withdraw")
	public @ResponseBody Object withdraw(@RequestParam(value = "orderCode") String orderCode) throws IOException {
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String idCard = prp.getIdCard();
		LMDHRegister lmdh = topupPayChannelBusiness.getlmdhRegisterByidCard(idCard);
		Map<String, Object> maps = new HashMap<>();
		SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();

		parameters.put("mch_id", mch_id);
		parameters.put("app_id", app_id);
		parameters.put("mch_no", orderCode);
		parameters.put("query_type", "withdraw");// trade：交易       withdraw：提现
		parameters.put("sub_mch_no", lmdh.getCustomerNum());
		String mySign = createSign(characterEncoding, parameters, key);
		LOG.info("====pay签名报文：" + mySign.toString());
		Part[] parts = null;
		parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(orderCode)
				.setQuery_type("withdraw").setSubM(lmdh.getCustomerNum()).setSign(mySign).generateParams();

		PostMethod postMethod = new PostMethod("https://pay.longmaoguanjia.com/api/pay/large_pay_x/query");
		HttpClient client = new HttpClient();
		postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

		int status = client.executeMethod(postMethod);
		LOG.info("==========status==========" + status);

		if (status == HttpStatus.SC_OK) {
			String backinfo = postMethod.getResponseBodyAsString();
			LOG.info("==========提现返回响应==========" + backinfo);
			JSONObject jsonstr = JSONObject.fromObject(backinfo);
			String respCode = jsonstr.getString("code");
			String respMessage = jsonstr.getString("message");
			if (respCode.equals("1030009")) {
				LOG.info("1030009：" + "交易成功" + "，订单号:" + orderCode);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respCode + ":" + respMessage);
				return maps;
			} else if (respCode.equals("1030008")) {
				LOG.info("1030008：" + "交易失败" + "，订单号:" + orderCode);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respCode + ":" + respMessage);
				return maps;
			} else if (respCode.equals("1030004")) {
				LOG.info("1030004：" + "商户订单号不存在" + "，订单号:" + orderCode);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respCode + ":" + respMessage);
				return maps;
			} else if (respCode.equals("1030012")) {
				LOG.info("1030012：" + "交易不存在" + "，订单号:" + orderCode);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respCode + ":" + respMessage);
				return maps;
			} else if(respCode.equals("1030011")){
				LOG.info("1030011：" + "交易已创建,未支付" + "，订单号:" + orderCode);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respCode + ":" + respMessage);
				return maps;
			} else {
				LOG.info("1050000不存在该查询类型   1030004商户订单号不存在   1030011交易已创建    1030009交易成功    1030008交易失败     1030012交易不存在");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respCode + ":" + respMessage);
				return maps;
			}
		}
		return maps;
	}

	/**
	 * 提现
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/lmdh/transfer")
	public @ResponseBody Object transfer(@RequestParam(value = "orderCode") String orderCode) throws IOException {

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String rip = prp.getIpAddress();
		String bankCard = prp.getBankCard();
		String bankName = prp.getCreditCardBankName();
		String idCard = prp.getIdCard();
		String ExtraFee = prp.getExtraFee();
		String realAmount = prp.getRealAmount();

		Map<String, Object> maps = new HashMap<>();
		LOG.info("====================lmdh提现====================");
		SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
		
		LMDHRegister lmdh = topupPayChannelBusiness.getlmdhRegisterByidCard(idCard);
		
		parameters.put("mch_id", mch_id);
		parameters.put("app_id", app_id);
		parameters.put("mch_no", orderCode);
		parameters.put("sub_mch_no", lmdh.getCustomerNum());
		parameters.put("id_card", idCard);

		// 获取缩写
		LMBankNum sbcode = topupPayChannelBusiness.getLMBankNumCodeByBankName(bankName);
		String bankAbbr = sbcode.getBankNum();// 缩写
		parameters.put("bank_code", bankAbbr);

		parameters.put("bank_card_no", bankCard);
		String Amount = new BigDecimal(realAmount).multiply(new BigDecimal(100)).toString();
		String realA = Amount.substring(0, Amount.lastIndexOf("."));
		String extraFee = new BigDecimal(ExtraFee).multiply(new BigDecimal(100)).toString();
		String extra = extraFee.substring(0, extraFee.lastIndexOf("."));
		int a = Integer.valueOf(realA);
		int e = Integer.valueOf(extra);
		String am = String .valueOf(a + e);
		LOG.info("实际到账金额（分）：" + realA + ",手续费（分）：" + extra + "提现的金额（分，实际金额+手续费）：" + am);
		parameters.put("amount", am);
		
		parameters.put("sub_mch_rate", "0");// 提现费率
		parameters.put("single_price", extra);
		
		String returnUrl = ip + "/v1.0/paymentgateway/topup/lmdh/put/notify_call";
		parameters.put("notify_url", returnUrl);

		String mySign = createSign(characterEncoding, parameters, key);
		LOG.info("====提现签名报文：" + mySign.toString());
		Part[] parts = null;
		parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(orderCode)
				.setSubM(lmdh.getCustomerNum()).setidCard(idCard).setBankChannelNo(bankAbbr)
				.setCardNo(bankCard).setAmount(am).setSubMchRate("0").setSinglePrice(extra)
				.setNotifyUrl(returnUrl).setSign(mySign).generateParams();

		PostMethod postMethod = new PostMethod("https://pay.longmaoguanjia.com/api/pay/large_pay_x/withdraw");
		HttpClient client = new HttpClient();
		postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

		int status = client.executeMethod(postMethod);
		LOG.info("==========status==========" + status);

		if (status == HttpStatus.SC_OK) {
			String backinfo = postMethod.getResponseBodyAsString();
			LOG.info("==========提现返回响应==========" + backinfo);
			JSONObject jsonstr = JSONObject.fromObject(backinfo);
			String respCode = jsonstr.getString("code");
			String respMessage = jsonstr.getString("message");
			if (respCode.equals("0")) { //
				LOG.info("------------------- 提现成功-------------------");
				
				maps.put(CommonConstants.RESP_CODE, "999998");
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				this.addOrderCauseOfFailure(orderCode, "请求提现:" + respMessage + "[" + orderCode + "]", rip);
				return maps;
			}
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "请求超时");
			this.addOrderCauseOfFailure(orderCode, "请求提现: 请求超时[" + orderCode + "]", rip);
			return maps;
		}
	}
	
	/**
	 * 余额查询
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/lmdh/queryBalance")
	public @ResponseBody Object queryBalance(@RequestParam(value = "idCard") String idCard) throws IOException {

		Map<String, Object> maps = new HashMap<>();
		LOG.info("====================lmdh查询余额====================");
		SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
		
		LMDHRegister lmdh = topupPayChannelBusiness.getlmdhRegisterByidCard(idCard);
		
		String requestNo = "XinLi" + System.currentTimeMillis();
		LOG.info("===余额查询请求订单号：" + requestNo);
		
		parameters.put("mch_id", mch_id);
		parameters.put("app_id", app_id);
		parameters.put("mch_no", requestNo);
		parameters.put("sub_mch_no", lmdh.getCustomerNum());

		String mySign = createSign(characterEncoding, parameters, key);
		LOG.info("====pay签名报文：" + mySign.toString());
		Part[] parts = null;
		parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(requestNo)
				.setSubM(lmdh.getCustomerNum()).setSign(mySign).generateParams();

		PostMethod postMethod = new PostMethod("https://pay.longmaoguanjia.com/api/pay/large_pay_x/sub_merchant_query");
		HttpClient client = new HttpClient();
		postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

		int status = client.executeMethod(postMethod);
		LOG.info("==========status==========" + status);

		if (status == HttpStatus.SC_OK) {
			String backinfo = postMethod.getResponseBodyAsString();
			LOG.info("==========查询余额返回响应==========" + backinfo);
			JSONObject jsonstr = JSONObject.fromObject(backinfo);
			String respCode = jsonstr.getString("code");
			String respMessage = jsonstr.getString("message");
			if (respCode.equals("0")) { //
				String data = jsonstr.getString("data");
				JSONObject dataStr = JSONObject.fromObject(data);
				String balance = dataStr.getString("account_balance");
				LOG.info("------------------- 查询余额成功 -------------------");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage + ",余额：" + balance);
				return maps;
			} else {
				LOG.info("------------------- 查询余额失败-------------------");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			}
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "请求超时");
			return maps;
		}
	}

	/**
	 * 交易
	 * 
	 * @throws Exception
	 */ 
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/lmdh/getsms")
	public @ResponseBody Object putPay(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "provinceOfBank") String provinceOfBank,
			@RequestParam(value = "cityOfBank") String cityOfBank,
			@RequestParam(value = "areaOfBank") String areaOfBank,
			@RequestParam(value = "provinceCode") String provinceCode, 
			@RequestParam(value = "cityCode") String cityCode,
			@RequestParam(value = "areaCode") String areaCode) throws Exception {

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String rip = prp.getIpAddress();
		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();
		String amount = prp.getAmount();
		String rate = prp.getRate();
		String ExtraFee = prp.getExtraFee();
		String bankName = prp.getCreditCardBankName();

		Map<String, Object> maps = new HashMap<>();
		LMDHRegister lmdh = topupPayChannelBusiness.getlmdhRegisterByidCard(idCard);
		LOG.info("====================lmdh交易====================");
		SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();

		parameters.put("mch_id", mch_id);
		parameters.put("app_id", app_id);
		parameters.put("mch_no", orderCode);
		parameters.put("sub_mch_no", lmdh.getCustomerNum());
		
		// 获取缩写
		LMBankNum sbcode = topupPayChannelBusiness.getLMBankNumCodeByBankName(bankName);
		String bankAbbr = sbcode.getBankNum();// 缩写
		parameters.put("bank_code", bankAbbr);

		parameters.put("bank_card_no", bankCard);

		String Amount = new BigDecimal(amount).multiply(new BigDecimal(100)).toString();
		parameters.put("amount", Amount);

		parameters.put("sub_mch_rate", rate);

		parameters.put("single_price", "0");

		// address:{“province":"\u5e7f\u4e1c\u7701","province_code":44,"city":"\u5e7f\u5dde\u5e02","city_code":4401,"area":"\u5929\u6cb3\u533a","area_code":440106}
		Map<String, Object> addressMaps = new TreeMap<String, Object>();

		addressMaps.put("province", UnicodeUtils.toUnicode(provinceOfBank));
		addressMaps.put("province_code", provinceCode);
		addressMaps.put("city", UnicodeUtils.toUnicode(cityOfBank));
		addressMaps.put("city_code", cityCode);
		addressMaps.put("area", UnicodeUtils.toUnicode(areaOfBank));
		addressMaps.put("area_code", areaCode);
		JSONObject json = JSONObject.fromObject(addressMaps);

		String unescapeJavaScript = StringEscapeUtils.unescapeJavaScript(json.toString());
		parameters.put("address", unescapeJavaScript);

		String returnUrl = ip + "/v1.0/paymentgateway/topup/lmdh/pay/notify_call";
		parameters.put("notify_url", returnUrl);

		LOG.info("交易请求参数：" + parameters);

		String mySign = createSign(characterEncoding, parameters, key);
		LOG.info("====pay签名报文：" + mySign.toString());
		Part[] parts = null;
		parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(orderCode)
				.setSubM(lmdh.getCustomerNum()).setBankChannelNo(bankAbbr).setCardNo(bankCard)
				.setAmount(Amount).setSubMchRate(rate).setSinglePrice("0")
				.setAddress(unescapeJavaScript).setSign(mySign).setNotifyUrl(returnUrl)
				.generateParams();

		PostMethod postMethod = new PostMethod("https://pay.longmaoguanjia.com/api/pay/large_pay_x/trade");
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
				maps.put(CommonConstants.RESP_CODE,  "999998");
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			} else {
				LOG.info("------------------- 交易失败  -------------------");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				this.addOrderCauseOfFailure(orderCode, "交易失败:" + respMessage + "[" + orderCode + "]", rip);
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
	 * 交易回调
	 * 
	 * @param req
	 * @param res
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/lmdh/pay/notify_call")
	public @ResponseBody void notifycall(HttpServletRequest req, HttpServletResponse res) throws IOException {
		LOG.info("进入龙猫交易回调接口================" + req.getParameterMap().toString());
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
	 * 提现回调
	 * 
	 * @param req
	 * @param res
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/lmdh/put/notify_call")
	public @ResponseBody void putnotifycall(HttpServletRequest req, HttpServletResponse res) throws IOException {
		LOG.info("进入龙猫提现回调接口================" + req.getParameterMap().toString());
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
	@RequestMapping(method = RequestMethod.POST, value = ("/v1.0/paymentgateway/lmdh/to/pay-view"))
	public @ResponseBody Object toPayView(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "provinceOfBank") String provinceOfBank,
			@RequestParam(value = "cityOfBank") String cityOfBank,
			@RequestParam(value = "areaOfBank") String areaOfBank,
			@RequestParam(value = "provinceCode") String provinceCode,
			@RequestParam(value = "cityCode") String cityCode, @RequestParam(value = "areaCode") String areaCode)
					throws Exception {
		LOG.info("lmdh:orderCode------------------------：" + orderCode + "," + provinceOfBank + "," + cityOfBank + ","
				+ areaOfBank + "," + provinceCode + "," + cityCode + "," + areaCode);
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String phoneC = prp.getCreditCardPhone();
		String amount = prp.getAmount();
		String cardNo = prp.getDebitCardNo();
		String bankName = prp.getDebitBankName();

		Map<String, Object> maps = new HashMap<>();

		maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		maps.put(CommonConstants.RESP_MESSAGE, "直接跳转交易页面");
		maps.put("redirect_url", ip + "/v1.0/paymentgateway/quick/lmdh/pay-view?bankName="
				+ URLEncoder.encode(bankName, "UTF-8") + "&bankCard=" + cardNo + "&orderCode=" + orderCode
				+ "&ipAddress=" + ip + "&ips=" + prp.getIpAddress() + "&phone=" + phoneC + "&provinceOfBank="
				+ URLEncoder.encode(provinceOfBank, "UTF-8") + "&cityOfBank=" + URLEncoder.encode(cityOfBank, "UTF-8")
				+ "&areaOfBank=" + URLEncoder.encode(areaOfBank, "UTF-8") + "&provinceCode=" + provinceCode
				+ "&cityCode=" + cityCode + "&areaCode=" + areaCode + "&amount=" + amount + "&isRegister=1");
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
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/lmdh/toHtml")
	public String toHtml(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
		LOG.info("lmdhPay------------------跳转到省市区选择页面");

		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String orderCode = request.getParameter("orderCode");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("orderCode", orderCode);
		model.addAttribute("ipAddress", ipAddress);

		return "lmdhlinkage";
	}

	/**
	 * 跳转到交易界面
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/lmdh/pay-view")
	public String toPay(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
		LOG.info("lmdhPay------------------跳转到交易界面");

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

		return "lmdhpay";
	}

	/**
	 * 跳转到交易成功中转页面
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/lmdh/paysuccess-view")
	public String paysuccessView(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		LOG.info("lmdhPay------------------跳转到交易成功中转页面");

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

		return "lmdhpaysuccess";
	}

	/**
	 * 根据市id查询该市所有的区
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = ("/v1.0/paymentgateway/lmdh/area/queryall"))
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
	@RequestMapping(method = RequestMethod.POST, value = ("/v1.0/paymentgateway/lmdh/city/queryall"))
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
	@RequestMapping(method = RequestMethod.POST, value = ("/v1.0/paymentgateway/lmdh/province/queryall"))
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