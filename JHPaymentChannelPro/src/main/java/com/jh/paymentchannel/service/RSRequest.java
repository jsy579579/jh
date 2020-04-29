package com.jh.paymentchannel.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.epayplusplus.api.EpayppApiException;
import com.epayplusplus.api.EpayppConstants;
import com.epayplusplus.api.EpayppEncrypt;
import com.epayplusplus.api.EpayppSignature;
import com.epayplusplus.api.enums.EncryptTypeEnum;
import com.epayplusplus.api.enums.SignMethodEnum;

import com.epayplusplus.api.request.EpayppMerchantProductOpenRequest;
import com.epayplusplus.api.request.EpayppMerchantProductRateSetRequest;
import com.epayplusplus.api.request.EpayppMerchantRegisterRequest;
import com.epayplusplus.api.request.EpayppMerchantSettleAccountSetRequest;
import com.epayplusplus.api.request.EpayppTradeCreateRequest;
import com.epayplusplus.api.request.EpayppTradePayRequest;
import com.epayplusplus.api.request.EpayppWithoutCardTradeExpressVerifyCodeSubmitRequest;

import com.epayplusplus.api.response.EpayppMerchantProductOpenResponse;
import com.epayplusplus.api.response.EpayppMerchantProductRateSetResponse;
import com.epayplusplus.api.response.EpayppMerchantRegisterResponse;
import com.epayplusplus.api.response.EpayppMerchantSettleAccountSetResponse;
import com.epayplusplus.api.response.EpayppTradeCreateResponse;
import com.epayplusplus.api.response.EpayppTradePayResponse;
import com.epayplusplus.api.response.EpayppWithoutCardTradeExpressVerifyCodeSubmitResponse;

import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.business.RSBusiness;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.RSRegister;
import com.jh.paymentchannel.util.EpayppEnvironmentData;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;

@Controller
@EnableAutoConfiguration
public class RSRequest extends BaseChannel {

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private RSBusiness rsBusiness;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	private static final Logger log = LoggerFactory.getLogger(RSRequest.class);

	// 注册
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/rs/register")
	public @ResponseBody Object test(HttpServletRequest request,
			@RequestParam(value = "orderCode", required = false) String orderCode)
			throws EpayppApiException, UnsupportedEncodingException {
		Date date = new Date();
		// 1.根据订单号获取订单信息
		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		if (!"000000".equals(queryOrdercode.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "rs");
			maps.put("resp_message", queryOrdercode.get("resp_message"));
			log.info(maps.toString());
			return maps;

		}
		Object object = queryOrdercode.get("result");
		net.sf.json.JSONObject fromObject = net.sf.json.JSONObject.fromObject(object);
		net.sf.json.JSONObject resultObj = fromObject.getJSONObject("result");

		/* 用户id;手机号;费率 */
		String out_user_id = resultObj.getString("userid");
		String rate = resultObj.getString("rate");
		String amount = resultObj.getString("amount");
		String bankCard = resultObj.getString("bankcard");
		String extraFee = resultObj.getString("extraFee");

		// 2.获取充值卡信息

		Map<String, Object> queryBankCardByUserId = this.queryBankCardByUserId(out_user_id);
		if (!"000000".equals(queryBankCardByUserId.get("resp_code"))) {
			maps.put("resp_code", "failed");
			maps.put("channel_type", "rs");
			maps.put("resp_message", queryBankCardByUserId.get("resp_message"));
			return maps;
		}
		Object object2 = queryBankCardByUserId.get("result");
		fromObject = net.sf.json.JSONObject.fromObject(object2);

		// 银行卡号
		String cardNo = fromObject.getString("cardNo");
		String userName = fromObject.getString("userName");
		// 身份证号
		String cert_no = fromObject.getString("idcard");
		String phone = fromObject.getString("phone");

		Map<String, Object> maps1 = new HashMap<String, Object>();

		// 判断用户是否在注册表内
		RSRegister rs = topupPayChannelBusiness.getRSRegisterByIdCard(cert_no);

		if (rs == null) {
		
			log.info("判定一：=========================这是朕的第一次哦!   =========================================");

			// *******注册******
			JSONObject bcJson = new JSONObject();

			bcJson.put("out_user_id", out_user_id);// 商户在合作伙伴系统的唯一编号
			bcJson.put("material_no", out_user_id);// 材料单号
			bcJson.put("merchant_type", "PRIVATE_ACCOUNT");// 商户类型
			bcJson.put("merchant_name", "上海莘丽网络");// 商户名称
			bcJson.put("cert_type", "IDCARD");// 证件类型
			bcJson.put("cert_no", cert_no);// 证件号
			bcJson.put("contact_name", userName);// 联系人名称
			bcJson.put("contact_mobile", phone);// 联系人手机
			bcJson.put("contact_email", "q355023989@qq.com");// 联系人邮箱
			bcJson.put("province", "310000");// 省份编号
			bcJson.put("city", "310000");// 城市编号
			bcJson.put("district", "310113");// 县/区编号
			bcJson.put("address", "上海宝山区逸仙路2816号");// 地址

			log.info("商户注册参数：" + bcJson.toString());

			EpayppMerchantRegisterRequest Erequest = new EpayppMerchantRegisterRequest();
			Erequest.setBizContent(bcJson.toString());
			EpayppMerchantRegisterResponse response_E = rsBusiness.getEpayppClient().execute(Erequest);
			log.info("商户注册响应：" + response_E);
			if (response_E.isSuccess()) {
				// 注册
				RSRegister rsRegister = new RSRegister();
				rsRegister.setMerchantName("上海莘丽网络");
				rsRegister.setUserid(out_user_id);// 用户id
				rsRegister.setRate(rate);// 费率
				rsRegister.setExtraFee(extraFee);// 额外手续费
				rsRegister.setFixed("3.0");// 暂为空
				rsRegister.setCreateTime(new Date());
				try {
					rsRegister = topupPayChannelBusiness.createRSRegister(rsRegister);
					log.info("商户注册反馈：添加成功");
				} catch (Exception e) {
					log.info("商户注册反馈：添加失败");

				} 

				// ********2.设置结算卡*******
				JSONObject bcJson2 = new JSONObject();
				bcJson2.put("out_user_id", out_user_id);
				bcJson2.put("bank_account_type", "PRIVATE_ACCOUNT");
				bcJson2.put("bank_account_no", cardNo);
				bcJson2.put("cert_type", "IDCARD");
				bcJson2.put("cert_no", cert_no);
				bcJson2.put("name", userName);
				bcJson2.put("mobile", phone);

				log.info("设置结算卡参数：" + bcJson2.toString());
				EpayppMerchantSettleAccountSetRequest Brequest = new EpayppMerchantSettleAccountSetRequest();
				Brequest.setBizContent(bcJson2.toString());
				EpayppMerchantSettleAccountSetResponse B_response = rsBusiness.getEpayppClient().execute(Brequest);
				log.info("设置结算卡响应：" + B_response);
				if (B_response.isSuccess()) {
					// 绑卡
					rsRegister.setBankAccountType("PRIVATE_ACCOUNT");
					rsRegister.setBankAccountNo(cardNo);// 银行卡
					rsRegister.setUserid(out_user_id);// 用户id
					rsRegister.setIdCard(cert_no);// 身份证号码
					rsRegister.setContactName(userName);// 开户人姓名
					rsRegister.setPhone(phone);// 电话
					rsRegister.setChangeTime(new Date());
					RSRegister rsRegisterTwo = new RSRegister();
					rsRegisterTwo = topupPayChannelBusiness.createRSRegister(rsRegister);
					if (rsRegisterTwo != null) {
						log.info("商户绑卡反馈：添加成功");
					} else {
						log.info("商户绑卡反馈：添加失败");
					}

					// ******3.产品开通*******
					JSONObject bcJson3 = new JSONObject();
					bcJson3.put("out_user_id", out_user_id);
					bcJson3.put("product", "3008");
					bcJson3.put("bottom", "0");
					bcJson3.put("top", "0");
					bcJson3.put("fixed", extraFee);
					bcJson3.put("rate", rate);
					log.info("产品开通参数：" + bcJson3.toString());
					EpayppMerchantProductOpenRequest request_open = new EpayppMerchantProductOpenRequest();
					request_open.setBizContent(bcJson3.toString());
					EpayppMerchantProductOpenResponse response_open = rsBusiness.getEpayppClient()
							.execute(request_open);
					log.info("产品开通响应：" + response_open);
					if (response_open.isSuccess()) {
						// 产品开通
						rsRegister.setProduct("3008");
						rsRegister.setUserid(out_user_id);// 用户id
						rsRegister.setExtraFee(extraFee);// 手续费
						rsRegister.setRate(rate);// 费率
						rsRegister.setChangeTime(new Date());

						rsRegister = topupPayChannelBusiness.createRSRegister(rsRegister);
						if (rsRegister != null) {
							log.info("商户绑卡反馈：添加成功");
						} else {
							log.info("商户绑卡反馈：添加失败");
						}
						// ****获取充值卡信息****
						Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
						if (!"000000".equals(queryBankCardByCardNo.get("resp_code"))) {
							maps.put("resp_code", "failed");
							maps.put("channel_type", "rs");
							maps.put("resp_message", queryBankCardByCardNo.get("resp_message"));
							return maps;
						}
						Object object3 = queryBankCardByCardNo.get("result");
						net.sf.json.JSONObject fromObject_pay = net.sf.json.JSONObject.fromObject(object3);
						// 银行卡号
						String cardNo_p = fromObject_pay.getString("cardNo");
						String userName_p = fromObject_pay.getString("userName");

						// 身份证号
						String cert_no_p = fromObject_pay.getString("idcard");
						String phone_p = fromObject_pay.getString("phone");
						String cvn2 = fromObject_pay.getString("securityCode");
						String expiredTime = fromObject_pay.getString("expiredTime");
						// 有效期转码
						String expired = this.expiredTimeToMMYY(expiredTime);
						// *******4.交易创建并请求结算******
						EpayppTradeCreateRequest request_pay = new EpayppTradeCreateRequest();
						// 是否需要对业务参数进行加密
						request_pay.setNeedEncrypt(true);
						// 异步通知地址
						request_pay.setNotifyUrl(EpayppEnvironmentData.getNotifyUrl());
						// 同步跳转地址，对于网页收银台需要此参数
						request_pay.setReturnUrl(EpayppEnvironmentData.getReturnUrl());

						// SDK已经封装掉了公共参数，这里只需要传入业务参数
						JSONObject bcJson4 = new JSONObject();
						// 交易产品
						bcJson4.put("product", "3008");
						// 卖家用户号，子商户号
						bcJson4.put("out_user_id", out_user_id);
						// 交易单号
						bcJson4.put("out_trade_no", orderCode);
						// 终端编号，固定值
						bcJson4.put("terminal_id", "000000");
						// 异步通知地址
						bcJson4.put("notify_url", ipAddress + "/v1.0/paymentchannel/topup/rs/trade/notify.htm");

						// 订单支付超时时间，单位：秒（默认为600s）
						bcJson4.put("timeout", "3600");

						// 默认货币
						bcJson4.put("currency", "156");
						// 支付金额
						bcJson4.put("total_fee", amount);

						// 商品条款
						// 摘要，收银界面显示

						bcJson4.put("summary", "上海莘丽网络");
						// 商品类目

						// 商户订单创建时间格式：yyyy-MM-dd HH:mm:ss
						bcJson4.put("gmt_out_create", getCurrentDateStr());

						// 创建交易并支付，可选 支付参数
						bcJson4.put("other_params",
								"realName^" + userName_p + "|certNo^" + cert_no_p + "|bankAccountNo^" + cardNo_p
										+ "|mobile^" + phone_p + "|cvn2^" + cvn2 + "|expired^" + expired);

						// 将所有的业务参数放入bizContent参数中
						request_pay.setBizContent(bcJson4.toString());

						log.info("创建交易参数：" + bcJson4.toString());

						// 构建易支付客户端实例，执行调用请求
						EpayppTradeCreateResponse response_pay = rsBusiness.getEpayppClient().execute(request_pay);
						log.info("交易创建响应：" + response_pay);
						if (response_pay.isSuccess()) {
							maps1.put(CommonConstants.RESP_CODE, "success");
							maps1.put("channel_type", "quick");
							maps1.put("redirect_url",
									ipAddress + "/v1.0/paymentchannel/topup/rs/jump?orderCode="
											+ URLEncoder.encode(orderCode, "UTF-8") + "&total_fee="
											+ URLEncoder.encode(amount, "UTF-8") + "&mobile=" + phone_p + "&pay_card="
											+ cert_no_p + "&ipAddress=" + ipAddress + "&realName="
											+ URLEncoder.encode(userName_p, "UTF-8") + "&bankAccountNo=" + cardNo_p
											+ "&certNo=" + cert_no_p + "&cvn2=" + cvn2 + "&expired=" + expired);
							maps1.put(CommonConstants.RESP_MESSAGE, "交易创建成功，跳转页面");
						} else {
							maps1.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
							maps1.put(CommonConstants.RESULT, response_pay);
							maps1.put(CommonConstants.RESP_MESSAGE, "交易创建失败");
						}
					} else {
						maps1.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						maps1.put(CommonConstants.RESULT, response_open);
						maps1.put(CommonConstants.RESP_MESSAGE, "结算卡设置失败");
					}
				} else {
					maps1.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps1.put(CommonConstants.RESULT, B_response);
					maps1.put(CommonConstants.RESP_MESSAGE, "产品开通失败");
				}
			} else {
				maps1.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps1.put(CommonConstants.RESULT, response_E);
				maps1.put(CommonConstants.RESP_MESSAGE, "商户注册失败");
			}
		} else if (rate.equals(rs.getRate()) && cardNo.equals(rs.getBankAccountNo())) {
			log.info("判定二：================== 直接发起快捷支付  ===============================");
			// ****获取充值卡信息****
			Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
			if (!"000000".equals(queryBankCardByCardNo.get("resp_code"))) {
				maps.put("resp_code", "failed");
				maps.put("channel_type", "rs");
				maps.put("resp_message", queryBankCardByCardNo.get("resp_message"));
				return maps;
			}
			Object object3 = queryBankCardByCardNo.get("result");
			net.sf.json.JSONObject fromObject_pay = net.sf.json.JSONObject.fromObject(object3);
			// 银行卡号
			String cardNo_p = fromObject_pay.getString("cardNo");
			String userName_p = fromObject_pay.getString("userName");

			// 身份证号
			String cert_no_p = fromObject_pay.getString("idcard");
			String phone_p = fromObject_pay.getString("phone");
			String cvn2 = fromObject_pay.getString("securityCode");
			String expiredTime = fromObject_pay.getString("expiredTime");
			// 有效期转码
			String expired = this.expiredTimeToMMYY(expiredTime);
			// *******4.交易创建并请求结算******
			EpayppTradeCreateRequest request_pay = new EpayppTradeCreateRequest();
			// 是否需要对业务参数进行加密
			request_pay.setNeedEncrypt(true);
			// 异步通知地址
			request_pay.setNotifyUrl(EpayppEnvironmentData.getNotifyUrl());
			// 同步跳转地址，对于网页收银台需要此参数
			request_pay.setReturnUrl(EpayppEnvironmentData.getReturnUrl());

			// SDK已经封装掉了公共参数，这里只需要传入业务参数
			JSONObject bcJson4 = new JSONObject();
			// 交易产品
			bcJson4.put("product", "3008");
			// 卖家用户号，子商户号
			bcJson4.put("out_user_id", out_user_id);
			// 交易单号
			bcJson4.put("out_trade_no", orderCode);
			// 终端编号，固定值
			bcJson4.put("terminal_id", "000000");
			// 异步通知地址
			bcJson4.put("notify_url", ipAddress + "/v1.0/paymentchannel/topup/rs/trade/notify.htm");

			// 订单支付超时时间，单位：秒（默认为600s）
			bcJson4.put("timeout", "3600");

			// 默认货币
			bcJson4.put("currency", "156");
			// 支付金额
			bcJson4.put("total_fee", amount);

			// 商品条款
			// 摘要，收银界面显示

			bcJson4.put("summary", "上海莘丽网络");
			// 商品类目

			// 商户订单创建时间格式：yyyy-MM-dd HH:mm:ss
			bcJson4.put("gmt_out_create", getCurrentDateStr());

			// 创建交易并支付，可选 支付参数
			bcJson4.put("other_params", "realName^" + userName_p + "|certNo^" + cert_no_p + "|bankAccountNo^" + cardNo_p
					+ "|mobile^" + phone_p + "|cvn2^" + cvn2 + "|expired^" + expired);

			// 将所有的业务参数放入bizContent参数中
			request_pay.setBizContent(bcJson4.toString());

			log.info("创建交易参数：" + bcJson4.toString());

			// 构建易支付客户端实例，执行调用请求
			EpayppTradeCreateResponse response_pay = rsBusiness.getEpayppClient().execute(request_pay);
			log.info("交易创建响应：" + response_pay);
			if (response_pay.isSuccess()) {

				maps1.put(CommonConstants.RESP_CODE, "success");
				maps1.put("channel_type", "quick");
				maps1.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/rs/jump?orderCode="
						+ URLEncoder.encode(orderCode, "UTF-8") + "&total_fee=" + URLEncoder.encode(amount, "UTF-8")
						+ "&mobile=" + phone_p + "&pay_card=" + cert_no_p + "&ipAddress=" + ipAddress + "&realName="
						+ URLEncoder.encode(userName_p, "UTF-8") + "&bankAccountNo=" + cardNo_p + "&certNo=" + cert_no_p
						+ "&cvn2=" + cvn2 + "&expired=" + expired);
				maps1.put(CommonConstants.RESP_MESSAGE, "交易创建成功，跳转页面");
			} else {
				maps1.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps1.put(CommonConstants.RESULT, response_pay);
				maps1.put(CommonConstants.RESP_MESSAGE, "交易创建失败");
			}
		} else if (!(rs.getRate()).equals(rate) && cardNo.equals(rs.getBankAccountNo())) {
			log.info("判定三：===================== 修改费率  ==================================");
			JSONObject bcJson = new JSONObject();
			bcJson.put("out_user_id", out_user_id);
			bcJson.put("product", "3008");
			bcJson.put("bottom", "0");
			bcJson.put("top", "0");
			bcJson.put("fixed", "1.5");
			bcJson.put("rate", rate);

			log.info("修改费率参数：" + bcJson.toString());
			EpayppMerchantProductRateSetRequest request_v = new EpayppMerchantProductRateSetRequest();
			request_v.setBizContent(bcJson.toJSONString());
			EpayppMerchantProductRateSetResponse response_v = rsBusiness.getEpayppClient().execute(request_v);
			log.info("修改费率响应" + response_v);
			if (response_v.isSuccess()) {
				try {
					rs.setRate(rate);
					rs.setChangeTime(date);
					topupPayChannelBusiness.createRSRegister(rs);
				} catch (Exception e) {
					log.info("修改费率失败!");
				}

				log.info("修改费率成功，发起快捷支付===================================================》》");
				// ****获取充值卡信息****
				Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
				if (!"000000".equals(queryBankCardByCardNo.get("resp_code"))) {
					maps.put("resp_code", "failed");
					maps.put("channel_type", "rs");
					maps.put("resp_message", queryBankCardByCardNo.get("resp_message"));
					return maps;
				}
				Object object3 = queryBankCardByCardNo.get("result");
				net.sf.json.JSONObject fromObject_pay = net.sf.json.JSONObject.fromObject(object3);
				// 银行卡号
				String cardNo_p = fromObject_pay.getString("cardNo");
				String userName_p = fromObject_pay.getString("userName");

				// 身份证号
				String cert_no_p = fromObject_pay.getString("idcard");
				String phone_p = fromObject_pay.getString("phone");
				String cvn2 = fromObject_pay.getString("securityCode");
				String expiredTime = fromObject_pay.getString("expiredTime");
				// 有效期转码
				String expired = this.expiredTimeToMMYY(expiredTime);
				// *******4.交易创建并请求结算******
				EpayppTradeCreateRequest request_pay = new EpayppTradeCreateRequest();
				// 是否需要对业务参数进行加密
				request_pay.setNeedEncrypt(true);
				// 异步通知地址
				request_pay.setNotifyUrl(EpayppEnvironmentData.getNotifyUrl());
				// 同步跳转地址，对于网页收银台需要此参数
				request_pay.setReturnUrl(EpayppEnvironmentData.getReturnUrl());

				// SDK已经封装掉了公共参数，这里只需要传入业务参数
				JSONObject bcJson4 = new JSONObject();
				// 交易产品
				bcJson4.put("product", "3008");
				// 卖家用户号，子商户号
				bcJson4.put("out_user_id", out_user_id);
				// 交易单号
				bcJson4.put("out_trade_no", orderCode);
				// 终端编号，固定值
				bcJson4.put("terminal_id", "000000");
				// 异步通知地址
				bcJson4.put("notify_url", ipAddress + "/v1.0/paymentchannel/topup/rs/trade/notify.htm");

				// 订单支付超时时间，单位：秒（默认为600s）
				bcJson4.put("timeout", "3600");

				// 默认货币
				bcJson4.put("currency", "156");
				// 支付金额
				bcJson4.put("total_fee", amount);

				// 商品条款
				// 摘要，收银界面显示

				bcJson4.put("summary", "上海莘丽网络");
				// 商品类目

				// 商户订单创建时间格式：yyyy-MM-dd HH:mm:ss
				bcJson4.put("gmt_out_create", getCurrentDateStr());

				// 创建交易并支付，可选 支付参数
				bcJson4.put("other_params", "realName^" + userName_p + "|certNo^" + cert_no_p + "|bankAccountNo^"
						+ cardNo_p + "|mobile^" + phone_p + "|cvn2^" + cvn2 + "|expired^" + expired);

				// 将所有的业务参数放入bizContent参数中
				request_pay.setBizContent(bcJson4.toString());

				log.info("创建交易参数：" + bcJson4.toString());

				// 构建易支付客户端实例，执行调用请求
				EpayppTradeCreateResponse response_pay = rsBusiness.getEpayppClient().execute(request_pay);
				log.info("交易创建响应：" + response_pay);
				if (response_pay.isSuccess()) {
					System.out.println("触发");
					maps1.put(CommonConstants.RESP_CODE, "success");
					maps1.put("channel_type", "quick");
					maps1.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/rs/jump?orderCode="
							+ URLEncoder.encode(orderCode, "UTF-8") + "&total_fee=" + URLEncoder.encode(amount, "UTF-8")
							+ "&mobile=" + phone_p + "&pay_card=" + cert_no_p + "&ipAddress=" + ipAddress + "&realName="
							+ URLEncoder.encode(userName_p, "UTF-8") + "&bankAccountNo=" + cardNo_p + "&certNo="
							+ cert_no_p + "&cvn2=" + cvn2 + "&expired=" + expired);
					maps1.put(CommonConstants.RESP_MESSAGE, "交易创建成功，跳转页面");
				} else {
					maps1.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps1.put(CommonConstants.RESULT, response_pay);
					maps1.put(CommonConstants.RESP_MESSAGE, "交易创建失败");
				}

			} else {
				maps1.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps1.put(CommonConstants.RESULT, response_v);
				maps1.put(CommonConstants.RESP_MESSAGE, "修改费率失败");
			}
		} else if ((rs.getRate()).equals(rate) && !cardNo.equals(rs.getBankAccountNo())) {
			log.info("判定四：=================== 修改结算卡  ==================================");
			JSONObject bcJson2 = new JSONObject();
			bcJson2.put("out_user_id", out_user_id);
			bcJson2.put("bank_account_type", "PRIVATE_ACCOUNT");
			bcJson2.put("bank_account_no", cardNo);
			bcJson2.put("cert_type", "IDCARD");
			bcJson2.put("cert_no", cert_no);
			bcJson2.put("name", userName);
			bcJson2.put("mobile", phone);

			log.info("修改设置结算卡参数：" + bcJson2.toString());
			EpayppMerchantSettleAccountSetRequest Brequest = new EpayppMerchantSettleAccountSetRequest();
			Brequest.setBizContent(bcJson2.toString());
			EpayppMerchantSettleAccountSetResponse B_response = rsBusiness.getEpayppClient().execute(Brequest);
			log.info("修改设置结算卡响应：" + B_response);
			if (B_response.isSuccess()) {
				try {
					rs.setBankAccountNo(cardNo);
					rs.setChangeTime(date);
					topupPayChannelBusiness.createRSRegister(rs);
					log.info("修改绑卡成功!");
				} catch (Exception e) {
					log.info("修改绑卡失败!");
				}

				log.info("修改绑卡成功，发起快捷支付=============================================》》");
				// ****获取充值卡信息****
				Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
				if (!"000000".equals(queryBankCardByCardNo.get("resp_code"))) {
					maps.put("resp_code", "failed");
					maps.put("channel_type", "rs");
					maps.put("resp_message", queryBankCardByCardNo.get("resp_message"));
					return maps;
				}
				Object object3 = queryBankCardByCardNo.get("result");
				net.sf.json.JSONObject fromObject_pay = net.sf.json.JSONObject.fromObject(object3);
				// 银行卡号
				String cardNo_p = fromObject_pay.getString("cardNo");
				String userName_p = fromObject_pay.getString("userName");

				// 身份证号
				String cert_no_p = fromObject_pay.getString("idcard");
				String phone_p = fromObject_pay.getString("phone");
				String cvn2 = fromObject_pay.getString("securityCode");
				String expiredTime = fromObject_pay.getString("expiredTime");
				// 有效期转码
				String expired = this.expiredTimeToMMYY(expiredTime);
				// *******4.交易创建并请求结算******
				EpayppTradeCreateRequest request_pay = new EpayppTradeCreateRequest();
				// 是否需要对业务参数进行加密
				request_pay.setNeedEncrypt(true);
				// 异步通知地址
				request_pay.setNotifyUrl(EpayppEnvironmentData.getNotifyUrl());
				// 同步跳转地址，对于网页收银台需要此参数
				request_pay.setReturnUrl(EpayppEnvironmentData.getReturnUrl());

				// SDK已经封装掉了公共参数，这里只需要传入业务参数
				JSONObject bcJson4 = new JSONObject();
				// 交易产品
				bcJson4.put("product", "3008");
				// 卖家用户号，子商户号
				bcJson4.put("out_user_id", out_user_id);
				// 交易单号
				bcJson4.put("out_trade_no", orderCode);
				// 终端编号，固定值
				bcJson4.put("terminal_id", "000000");
				// 异步通知地址
				bcJson4.put("notify_url", ipAddress + "/v1.0/paymentchannel/topup/rs/trade/notify.htm");

				// 订单支付超时时间，单位：秒（默认为600s）
				bcJson4.put("timeout", "3600");

				// 默认货币
				bcJson4.put("currency", "156");
				// 支付金额
				bcJson4.put("total_fee", amount);

				// 商品条款
				// 摘要，收银界面显示

				bcJson4.put("summary", "上海莘丽网络");
				// 商品类目

				// 商户订单创建时间格式：yyyy-MM-dd HH:mm:ss
				bcJson4.put("gmt_out_create", getCurrentDateStr());

				// 创建交易并支付，可选 支付参数
				bcJson4.put("other_params", "realName^" + userName_p + "|certNo^" + cert_no_p + "|bankAccountNo^"
						+ cardNo_p + "|mobile^" + phone_p + "|cvn2^" + cvn2 + "|expired^" + expired);

				// 将所有的业务参数放入bizContent参数中
				request_pay.setBizContent(bcJson4.toString());

				log.info("创建交易参数：" + bcJson4.toString());

				// 构建易支付客户端实例，执行调用请求
				EpayppTradeCreateResponse response_pay = rsBusiness.getEpayppClient().execute(request_pay);
				log.info("交易创建响应：" + response_pay);
				if (response_pay.isSuccess()) {
					System.out.println("触发");
					maps1.put(CommonConstants.RESP_CODE, "success");
					maps1.put("channel_type", "quick");
					maps1.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/rs/jump?orderCode="
							+ URLEncoder.encode(orderCode, "UTF-8") + "&total_fee=" + URLEncoder.encode(amount, "UTF-8")
							+ "&mobile=" + phone_p + "&pay_card=" + cert_no_p + "&ipAddress=" + ipAddress + "&realName="
							+ URLEncoder.encode(userName_p, "UTF-8") + "&bankAccountNo=" + cardNo_p + "&certNo="
							+ cert_no_p + "&cvn2=" + cvn2 + "&expired=" + expired);
					maps1.put(CommonConstants.RESP_MESSAGE, "交易创建成功，跳转页面");
				} else {
					maps1.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps1.put(CommonConstants.RESULT, response_pay);
					maps1.put(CommonConstants.RESP_MESSAGE, "交易创建失败");
				}

			} else {
				maps1.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps1.put(CommonConstants.RESULT, B_response);
				maps1.put(CommonConstants.RESP_MESSAGE, "修绑卡失败");
			}

		} else if (!(rs.getRate()).equals(rate) && !cardNo.equals(rs.getBankAccountNo())) {
			log.info("判定五：==================修改结算卡 ,修改费率================================");
			JSONObject bcJson2 = new JSONObject();
			bcJson2.put("out_user_id", out_user_id);
			bcJson2.put("bank_account_type", "PRIVATE_ACCOUNT");
			bcJson2.put("bank_account_no", cardNo);
			bcJson2.put("cert_type", "IDCARD");
			bcJson2.put("cert_no", cert_no);
			bcJson2.put("name", userName);
			bcJson2.put("mobile", phone);

			log.info("设置结算卡参数：" + bcJson2.toString());
			EpayppMerchantSettleAccountSetRequest Brequest = new EpayppMerchantSettleAccountSetRequest();
			Brequest.setBizContent(bcJson2.toString());
			EpayppMerchantSettleAccountSetResponse B_response = rsBusiness.getEpayppClient().execute(Brequest);
			log.info("设置结算卡响应：" + B_response);
			if (B_response.isSuccess()) {
				try {
					rs.setBankAccountNo(cardNo);
					rs.setChangeTime(date);
					topupPayChannelBusiness.createRSRegister(rs);
					log.info("修改结算卡记录成功!");
				} catch (Exception e) {
					log.info("修改结算卡记录失败!");
				}

				JSONObject bcJson = new JSONObject();
				bcJson.put("out_user_id", out_user_id);
				bcJson.put("product", "3008");
				bcJson.put("bottom", "0");
				bcJson.put("top", "0");
				bcJson.put("fixed", "1.5");
				bcJson.put("rate", rate);

				log.info("修改费率参数：" + bcJson.toString());
				EpayppMerchantProductRateSetRequest request_v = new EpayppMerchantProductRateSetRequest();
				request_v.setBizContent(bcJson.toJSONString());
				EpayppMerchantProductRateSetResponse response_v = rsBusiness.getEpayppClient().execute(request_v);
				log.info("修改费率响应" + response_v);
				if (response_v.isSuccess()) {
					try {
						rs.setRate(rate);
						rs.setChangeTime(date);
						topupPayChannelBusiness.createRSRegister(rs);
						log.info("修改费率数据记录成功!");
					} catch (Exception e) {
						log.info("修改费率数据记录失败!");
					}
					log.info("修改费率成功，修改结算卡成功，发起快捷支付===========================================》》");
					// ****获取充值卡信息****
					Map<String, Object> queryBankCardByCardNo = this.queryBankCardByCardNo(bankCard, "0");
					if (!"000000".equals(queryBankCardByCardNo.get("resp_code"))) {
						maps.put("resp_code", "failed");
						maps.put("channel_type", "rs");
						maps.put("resp_message", queryBankCardByCardNo.get("resp_message"));
						return maps;
					}
					Object object3 = queryBankCardByCardNo.get("result");
					net.sf.json.JSONObject fromObject_pay = net.sf.json.JSONObject.fromObject(object3);
					// 银行卡号
					String cardNo_p = fromObject_pay.getString("cardNo");
					String userName_p = fromObject_pay.getString("userName");

					// 身份证号
					String cert_no_p = fromObject_pay.getString("idcard");
					String phone_p = fromObject_pay.getString("phone");
					String cvn2 = fromObject_pay.getString("securityCode");
					String expiredTime = fromObject_pay.getString("expiredTime");
					// 有效期转码
					String expired = this.expiredTimeToMMYY(expiredTime);
					// *******4.交易创建并请求结算******
					EpayppTradeCreateRequest request_pay = new EpayppTradeCreateRequest();
					// 是否需要对业务参数进行加密
					request_pay.setNeedEncrypt(true);
					// 异步通知地址
					request_pay.setNotifyUrl(EpayppEnvironmentData.getNotifyUrl());
					// 同步跳转地址，对于网页收银台需要此参数
					request_pay.setReturnUrl(EpayppEnvironmentData.getReturnUrl());

					// SDK已经封装掉了公共参数，这里只需要传入业务参数
					JSONObject bcJson4 = new JSONObject();
					// 交易产品
					bcJson4.put("product", "3008");
					// 卖家用户号，子商户号
					bcJson4.put("out_user_id", out_user_id);
					// 交易单号
					bcJson4.put("out_trade_no", orderCode);
					// 终端编号，固定值
					bcJson4.put("terminal_id", "000000");
					// 异步通知地址
					bcJson4.put("notify_url", ipAddress + "/v1.0/paymentchannel/topup/rs/trade/notify.htm");

					// 订单支付超时时间，单位：秒（默认为600s）
					bcJson4.put("timeout", "3600");

					// 默认货币
					bcJson4.put("currency", "156");
					// 支付金额
					bcJson4.put("total_fee", amount);

					// 商品条款
					// 摘要，收银界面显示

					bcJson4.put("summary", "上海莘丽网络");
					// 商品类目

					// 商户订单创建时间格式：yyyy-MM-dd HH:mm:ss
					bcJson4.put("gmt_out_create", getCurrentDateStr());

					// 创建交易并支付，可选 支付参数
					bcJson4.put("other_params", "realName^" + userName_p + "|certNo^" + cert_no_p + "|bankAccountNo^"
							+ cardNo_p + "|mobile^" + phone_p + "|cvn2^" + cvn2 + "|expired^" + expired);

					// 将所有的业务参数放入bizContent参数中
					request_pay.setBizContent(bcJson4.toString());

					log.info("创建交易参数：" + bcJson4.toString());

					// 构建易支付客户端实例，执行调用请求
					EpayppTradeCreateResponse response_pay = rsBusiness.getEpayppClient().execute(request_pay);
					log.info("交易创建响应：" + response_pay);
					if (response_pay.isSuccess()) {
						maps1.put(CommonConstants.RESP_CODE, "success");
						maps1.put("channel_type", "quick");
						maps1.put("redirect_url",
								ipAddress + "/v1.0/paymentchannel/topup/rs/jump?orderCode="
										+ URLEncoder.encode(orderCode, "UTF-8") + "&total_fee="
										+ URLEncoder.encode(amount, "UTF-8") + "&mobile=" + phone_p + "&pay_card="
										+ cert_no_p + "&ipAddress=" + ipAddress + "&realName="
										+ URLEncoder.encode(userName_p, "UTF-8") + "&bankAccountNo=" + cardNo_p
										+ "&certNo=" + cert_no_p + "&cvn2=" + cvn2 + "&expired=" + expired);
						maps1.put(CommonConstants.RESP_MESSAGE, "交易创建成功，跳转页面");
					} else {
						maps1.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						maps1.put(CommonConstants.RESULT, response_pay);
						maps1.put(CommonConstants.RESP_MESSAGE, "交易创建失败");
					}
				} else {
					maps1.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps1.put(CommonConstants.RESULT, response_v);
					maps1.put(CommonConstants.RESP_MESSAGE, "修改费率失败");
				}

			} else {
				maps1.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps1.put(CommonConstants.RESULT, B_response);
				maps1.put(CommonConstants.RESP_MESSAGE, "修改结算卡失败");
			}
		}
		return maps1;

	}

	// 跳转支付页面
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/rs/jump")
	public String returnCJBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String orderCode = request.getParameter("orderCode");
		String total_fee = request.getParameter("total_fee");
		String pay_card = request.getParameter("pay_card");
		String mobile = request.getParameter("mobile");
		String ipAddress = request.getParameter("ipAddress");
		String realName = request.getParameter("realName");
		String certNo = request.getParameter("certNo");
		String bankAccountNo = request.getParameter("bankAccountNo");
		String cvn2 = request.getParameter("cvn2");
		String expired = request.getParameter("expired");

		model.addAttribute("bankCard", pay_card);
		model.addAttribute("total_fee", total_fee);
		model.addAttribute("mobile", mobile);
		model.addAttribute("out_trade_no", orderCode);
		model.addAttribute("ipAddress", ipAddress);
		model.addAttribute("realName", realName);
		model.addAttribute("certNo", certNo);
		model.addAttribute("bankAccountNo", bankAccountNo);
		model.addAttribute("cvn2", cvn2);
		model.addAttribute("expired", expired);

		return "RSpaymessage";
	}

	/**
	 * 获取验证码
	 * 
	 * @author lirui
	 * @param request
	 * @param out_trade_no
	 * @return
	 * @throws EpayppApiException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/rs/send/code")
	public @ResponseBody Object Send(HttpServletRequest request, @RequestParam("ordercode") String out_trade_no,
			@RequestParam("realName") String userName_p, @RequestParam("certNo") String cert_no_p,
			@RequestParam("bankAccountNo") String cardNo_p, @RequestParam("mobile") String mobile,
			@RequestParam("cvn2") String cvn2, @RequestParam("expired") String expired

	) throws EpayppApiException {
		Map maps = new HashMap();
		log.info(mobile);
		JSONObject bcJson = new JSONObject();
		bcJson.put("out_trade_no", out_trade_no);
		bcJson.put("other_params", "realName^" + userName_p + "|certNo^" + cert_no_p + "|bankAccountNo^" + cardNo_p
				+ "|mobile^" + mobile + "|cvn2^" + cvn2 + "|expired^" + expired);
		System.out.println(bcJson.toString());

		EpayppTradePayRequest request_send = new EpayppTradePayRequest();
		request_send.setBizContent(bcJson.toString());

		EpayppTradePayResponse response = rsBusiness.getEpayppClient().execute(request_send);
		if (response.isSuccess()) {
			maps.put(CommonConstants.RESP_CODE, "success");
			maps.put(CommonConstants.RESULT, response);
			maps.put(CommonConstants.RESP_MESSAGE, "请求支付成功，发送验证码");
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "获取验证码失败");
		}
		return maps;
	}

	/**
	 * 支付验证
	 * 
	 * @author lirui
	 * @param out_trade_no
	 * @param verify_code
	 * @param mobile
	 * @return
	 * @throws EpayppApiException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/rs/fastpay")
	public @ResponseBody Object Payment_Verification_Code(@RequestParam("ordercode") String out_trade_no,
			@RequestParam("smsCode") String verify_code, @RequestParam("mobile") String mobile)
			throws EpayppApiException {
		Map maps = new HashMap();
		// SDK已经封装掉了公共参数，这里只需要传入业务参数
		JSONObject bcJson = new JSONObject();
		bcJson.put("out_trade_no", out_trade_no);
		bcJson.put("verify_code", verify_code);
		bcJson.put("mobile", mobile);

		log.info("支付验证参数：" + bcJson.toString());
		EpayppWithoutCardTradeExpressVerifyCodeSubmitRequest request = new EpayppWithoutCardTradeExpressVerifyCodeSubmitRequest();
		request.setBizContent(bcJson.toString());
		EpayppWithoutCardTradeExpressVerifyCodeSubmitResponse response = rsBusiness.getEpayppClient().execute(request);
		log.info("支付验证响应：" + response);
		// 调用成功，则处理业务逻辑
		if (response.isSuccess()) {
			log.info("支付成功，进入异步回调地址");
			maps.put(CommonConstants.RESP_CODE, "success");
			maps.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/rs/jump/success");
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESULT, response);
			maps.put("resp_message", response.getSubMsg());
		}
		return maps;

	}

	private String getCurrentDateStr() {
		Long timestamp = System.currentTimeMillis();
		DateFormat df = new SimpleDateFormat(EpayppConstants.DATE_TIME_FORMAT);
		df.setTimeZone(TimeZone.getTimeZone(EpayppConstants.DATE_TIMEZONE));
		return df.format(new Date(timestamp));
	}

	// 跳转支付成功界面
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/rs/jump/success")
	public String Jumppage(HttpServletRequest request, HttpServletResponse response) throws IOException {
		return "sdjsuccess";
	}

	// 跳转支付失败界面
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/rs/jump/error")
	public String Jumperror(HttpServletResponse response) throws IOException {
		return "sdjerror";
	}

	public static String getexpiredTime(String expiredTime) {
		String a = expiredTime;
		String b = a.substring(0, 2);
		String c = a.substring(2, 4);
		String d = c + "" + b;
		log.info("有效期：" + d);
		return d;
	}

	/**
	 * 支付异步回调路径
	 */
	@RequestMapping(method = { RequestMethod.POST,
			RequestMethod.GET }, value = "/v1.0/paymentchannel/topup/rs/trade/notify.htm")
	public void home(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// 将异步通知中收到的所有参数都存放到map中
		Map<String, String> paramsMap = getRequestParams(request);

		// 调用SDK验证签名
		boolean signVerified = EpayppSignature.rsaCheck(paramsMap, EpayppEnvironmentData.getPKCS8PublicKey(), "UTF-8",
				SignMethodEnum.RSA);
		if (signVerified) {
			// 验证签名成功后解密
			// 1.RSA解密random_key
			String randomKey = paramsMap.get("random_key");
			// 解密后的随机密钥
			randomKey = EpayppEncrypt.decryptContent(randomKey, EncryptTypeEnum.RSA,
					EpayppEnvironmentData.getPKCS8PrivateKey(), "UTF-8");

			// 2.AES解密biz_content
			String bizContent = paramsMap.get("biz_content");
			bizContent = EpayppEncrypt.decryptContent(bizContent, EncryptTypeEnum.AES, randomKey, "UTF-8");

			net.sf.json.JSONObject notice = net.sf.json.JSONObject.fromObject(bizContent);
			String requestNo = notice.getString("out_trade_no");
			String tradeStatus = notice.getString("trade_status");

			log.info("异步通知参数：" + notice);
			log.info("回调订单号" + requestNo);
			Map maps = new HashMap();
			if ("TRADE_FINISHED".equals(tradeStatus)) {
				maps = this.updateOrderCode(requestNo, "1", "");
				if ("000000".equals(maps.get("RESP_CODE"))) {
					log.info("订单状态修改成功===================" + requestNo + "====================" + maps);
					log.info("订单已支付!");
				} else {
					log.info("订单状态修改失败===================" + requestNo + "====================" + maps);
				}
			} else {
				log.info("验签不通过!");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				if ("TRADE_CLOSED".equals(tradeStatus)) {
					tradeStatus = "交易关闭";
				} else if ("TRADE_CLOSED_BY_SYS".equals(tradeStatus)) {
					tradeStatus = "交易超时,请在下单后半个小时内支付";// 创建订单半小时后没有支付或者支付失败
														// 会变成超时关闭
				}
				maps.put(CommonConstants.RESP_MESSAGE, tradeStatus);
			}

		} else {
			System.out.println("验签不通过");
		}

		// 处理成功后，需要返回SUCCESS，通知中心接受到SUCCESS后将不再通知
		PrintWriter pw = response.getWriter();
		pw.print("SUCCESS");
		pw.close();
	}

	/**
	 * 从请求中获取所有的请求参数
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<String, String> getRequestParams(HttpServletRequest request) {
		Map<String, String> params = new TreeMap<String, String>();

		// 模拟测试参数
		// params.put("biz_content",
		// "CEB965DDB0C124CFCAD5643E391342E8917099A89C5F06B7A1CFC7D15F4806C156E4841D892E2376F73AA99AF5D3AAEF5262FCBB855A44C4CE9AC6B6296AC2BBC18D88DDCE72959D518FFDD2A0EF48BFA88C6833FF454E48E052A22AFB910F43C5F5ABBBF1558E5386DC138527BDF0A579AB46162F22C7F6CFC7368CA86E540EE0D167620F89C5CBC21E03BA7487EAECC8A82F286D9E60786F14B9F2AFBE8313284F2A14390C989D87A198C74B663616EE4033299D2E392C1391AFBF2A38A699CC69E5B5FFCA5D5ACC730B1C2F49F68DD4B0BD3B7120749E00B23D5B964302C0BF149031732980BB12817F247C8262B73DE2ABB11DFDA984AEE5C2B6FED4D3D98E4AE795ABEF3D4217D381C682D403023F7A933A847972EE871C9284D59AFB67907E7C35EFFBD94C48B43CE0D96ECB3E");
		// params.put("sign",
		// "4d87b03c85639d4521f79e41f996ae35536e1cba35e66c1c9ff9a1e3440965975ef456134b9759e7456eee217733d1b4cfd92a04bf55d0bee216b43bb61bc198e75b3e51260dae9bfd1bd38bd4c903ffcec9ab9903302a766c98c93386f5f7c29c14427e90aad901c8d4dc61cf7e91517ae9b8a57b31b9703ed786fcdb9e9b1e");
		// params.put("random_key",
		// "a8d03590f3ebc47d8d53247f98dcea46b5a750f68063524134c29adc70cea3287b4a6e6ccb1e20e4ee1c55e9a0cc48e37e36cefe06899756c7ec5903c677030e2e4dcb737301a960946a9aa8c9704db73e2a9a32eaa99d9aa3ad3f75298623ff485db3a8d4ea3172efde6a1872bf30ed9ccfa162dfcf4faeade85f128930ac12");

		// 获取所有请求参数名
		Enumeration<String> names = request.getParameterNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			String value = request.getParameter(name);

			params.put(name, value);
		}
		return params;
	}

}
