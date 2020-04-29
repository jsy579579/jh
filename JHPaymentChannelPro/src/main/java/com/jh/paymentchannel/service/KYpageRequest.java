package com.jh.paymentchannel.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
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
import com.jh.paymentchannel.pojo.BankNumCode;
import com.jh.paymentchannel.pojo.CJRegister;
import com.jh.paymentchannel.pojo.KYRegister;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.cjhk.ApiClient;
import com.jh.paymentchannel.util.cjhk.ApiRequest;
import com.jh.paymentchannel.util.cjhk.ApiResponse;
import com.jh.paymentchannel.util.cjhk.EncryptTypeEnum;
import com.jh.paymentchannel.util.ky.APIConstants;
import com.jh.paymentchannel.util.ky.OpenApiUtils;

import cn.jh.common.tools.Log;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.ExceptionUtil;
import cn.jh.common.utils.Md5Util;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class KYpageRequest extends BaseChannel {

	private static final Logger LOG = LoggerFactory.getLogger(KYpageRequest.class);

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

	private String appKey = "109008";

	private String secret = "a852736a-3634-4cc0-9d45-714870009965";

	private String orgId = "SHXL001";

	private String login_id = "601201808030001";

	private String term_id = "T2114781";

	private String term_mac = "F7B5AE01FB6E4710962B8D4E5603639D";

	private String URL = "http://syxl.openroutemng.com:15202/opengw/router/rest.htm";

	// 无积分的参数
	private String appKey1 = "109008";

	private String secret1 = "ce8db602-d0c6-4515-b644-c6b7a7ab4034";

	private String orgId1 = "SHXL01";

	private String login_id1 = "601201807020001";

	private String term_id1 = "T2180959";

	private String term_mac1 = "BB65610A25CC461D8D5D9033B4738290";

	private String URL1 = "http://www.uytong.cn:15202/opengw/router/rest.htm";

	private static final Charset UTF_8 = StandardCharsets.UTF_8;

	// 进件接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ky/register")
	public @ResponseBody Object kyRegister(HttpServletRequest request,
			@RequestParam(value = "ordercode") String orderCode,
			@RequestParam(value = "arriveAccountType") String arriveAccountType) throws Exception {
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

		// 系统级参数
		String v = "1.1";// API协议版本
		String method = "mposquery.merchant.incom";// API接口名称
		String timestamp = DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss");// 时间戳，允许的误差毫秒数(10分钟)
																											// 格式为yyyy-mm-dd
																											// HH:mm:ss，例如：2013-08-01
																											// 09:02:05
		String format = APIConstants.API_FORMAT_JSON;// json
														// 指定响应报文格式，默认xml，目前支持：xml、json
		// String appKey = "100158";//系统分配给应用的ID
		String signMethod = APIConstants.API_SIGN_METHOD_MD5;// md5
																// 参数的加密方法选择，可选值：md5、hmac
		// String secret = "5dd301cf-82cc-4f41-b671-72a930f77e70";//签名密钥

		/**
		 * 会员基本信息
		 */
		Map<String, String> mercInfoMap = new HashMap<String, String>();
		mercInfoMap.put("orgId", orgId);// 代理商编号
		mercInfoMap.put("mercId", userId);// 会员号
		mercInfoMap.put("mercUpperType", "2");// 商户大类 1-会员 2-商户
		mercInfoMap.put("mercTyp", "003");// 会员类型 001:餐娱类 002:房产汽车类 003:一般类
											// 004:批发类 005:民生类 006:公益类 007:非标类
		mercInfoMap.put("mercSts", "0");// 状态 0:正常 1:销户 4:黑名单 5:冻结
		mercInfoMap.put("mercStlSts", "0");// 结算状态 0:正常
		mercInfoMap.put("mercCnm", "莘丽");// 会员名称

		/**
		 * 会员营业信息
		 */
		Map<String, String> mercBusiMap = new HashMap<String, String>();
		mercBusiMap.put("crpIdTyp", "00");// 法人证件类型 00:身份证 01:户口本 02:军人身份证
											// 03:警察证 04:港、澳居民往来内地通行证
		// 05:台湾居民来往大陆通行证 06:护照 07:工商营业执照 08:法人证书 09:组织机构代码证 10:其他
		mercBusiMap.put("crpIdNo", idcard1);// 法人证件号码
		mercBusiMap.put("crpNm", userName1);// 法人名称

		/**
		 * 联系人信息
		 */
		Map<String, String> mercMcntMap = new HashMap<String, String>();
		mercMcntMap.put("cttPsnCnm", userName1);// 会员名称
		mercMcntMap.put("mblTel", phone1);// 移动电话

		/**
		 * 会员结算账户信息 --以交易时上送为准
		 */
		Map<String, String> mercMactMap = new HashMap<String, String>();
		mercMactMap.put("stlOac", "55555555");// 结算账号
		mercMactMap.put("effDt", DateUtil.getDateStringConvert(new String(), new Date(), "yyyyMMdd"));// 生效日期（注册时间）yyyyMMdd
		mercMactMap.put("stlOacCls", "0");// 结算账户类型 0:银行账户
		mercMactMap.put("effFlg", "1");// 生效标识 0：无效 1：有效 2：作废
		mercMactMap.put("deductSign", "1");// 结算账户标志 0:对公 1对私
		mercMactMap.put("dpsbondAcnm", userName1);// 账号户名
		mercMactMap.put("dpsbondLbnkNo", code);// 联行行号（支行）
		mercMactMap.put("dpsbondBnkDesc", bankName1);// 银行名称
		mercMactMap.put("dpsbondSign", "1");// 结算账户标识 0:对公 1对私
		mercMactMap.put("dpsbondBnkProv", "43");// 开户行所在省
		mercMactMap.put("dpsbondBnkCity", "4301");// 开户行所在市

		String bigRate = new BigDecimal(rate).multiply(new BigDecimal("100")).setScale(2).toString();

		/**
		 * 会员费率信息 --以交易时上送为准
		 */
		Map<String, String> mercFeeMap = new HashMap<String, String>();
		mercFeeMap.put("t1DebitFeeRat", bigRate);// 借记卡T1交易费率（%）
		mercFeeMap.put("t1DebitFixedFee", "0");// 借记卡T1交易固定手续费（元）
		mercFeeMap.put("t1DebitMinFeeAmt", bigRate);// 借记卡T1交易最低手续费（元）
		mercFeeMap.put("t1DebitMaxFeeAmt", "0");// 借记卡T1交易封顶手续费（元） 当为0时不封顶
		mercFeeMap.put("t1CreditFeeRat", bigRate);// 贷记卡T1交易费率（%）
		mercFeeMap.put("t1CreditFixedFee", "0");// 贷记卡T1交易固定手续费（元）
		mercFeeMap.put("t1CreditMinFeeAmt", bigRate);// 贷记卡T1交易最低手续费（元）
		mercFeeMap.put("t1CreditMaxFeeAmt", "0");// 贷记卡T1交易封顶手续费（元） 当为0时不封顶
		mercFeeMap.put("d0FeeRat", bigRate);// D0交易费率（%）
		mercFeeMap.put("d0FixedFee", "0");// D0交易固定手续费（元）
		mercFeeMap.put("d0MinFeeAmt", bigRate);// D0交易最低手续费（元）
		mercFeeMap.put("d0MaxFeeAmt", "0");// D0交易封顶手续费（元） 当为0时不封顶
		mercFeeMap.put("d0QrFeeRat", bigRate);// 二维码D0交易费率（%）
		mercFeeMap.put("d0QrFixedFee", "0");// 二维码D0交易固定手续费（元）
		mercFeeMap.put("d0QrMinFeeAmt", bigRate);// 二维码D0交易最低手续费（元）
		mercFeeMap.put("d0QrMaxFeeAmt", "0");// 二维码D0交易封顶手续费（元） 当为0时不封顶
		mercFeeMap.put("t1QrFeeRat", bigRate);// 二维码T1交易费率（%）
		mercFeeMap.put("t1QrFixedFee", "0");// 二维码T1交易固定手续费（元）
		mercFeeMap.put("t1QrMinFeeAmt", bigRate);// 二维码T1交易最低手续费（元）
		mercFeeMap.put("t1QrMaxFeeAmt", "0");// 二维码T1交易封顶手续费（元） 当为0时不封顶

		/**
		 * 会员证件信息
		 */
		Map<String, String> mercFileMap = new HashMap<String, String>();

		Map<String, Object> body = new HashMap<String, Object>();
		body.put("mercInfo", mercInfoMap);// 会员基本信息
		body.put("mercBusi", mercBusiMap);// 会员营业信息
		body.put("mercMcnt", mercMcntMap);// 联系人信息
		body.put("mercFile", mercFileMap);// 会员证件信息
		body.put("mercMact", mercMactMap);// 会员结算账户信息
		body.put("mercFee", mercFeeMap);// 会员费率信息

		// 应用级参数
		Map<String, String> bizParams = new HashMap();
		bizParams.put("action", "010");// 代理商会员新增/修改
		bizParams.put("time_interval", System.currentTimeMillis() + "");// 请求的时间戳。
		bizParams.put("request_id", System.currentTimeMillis() + "");// 请求唯一标识
		bizParams.put("body", JSON.toJSONString(body));
		LOG.info("会员新增---请求参数：" + JSON.toJSONString(bizParams));

		String callOpenApi = OpenApiUtils.callOpenApi(URL, v, method, timestamp, format, appKey, signMethod, secret,
				bizParams);

		LOG.info("callOpenApi======" + callOpenApi);

		JSONObject fromObject2 = JSONObject.fromObject(callOpenApi);

		JSONObject jsonObject = fromObject2.getJSONObject("mposquery_merchant_incom_response");

		LOG.info("jsonObject======" + jsonObject);

		String code1 = jsonObject.getString("code");
		String message = jsonObject.getString("message");

		if ("0000".equals(code1)) {
			String requestId = jsonObject.getString("requset_id");

			KYRegister kyRegister = new KYRegister();
			kyRegister.setPhone(phone);
			kyRegister.setBankCard(cardNo);
			kyRegister.setIdCard(idcard);
			kyRegister.setRate(rate);
			kyRegister.setExtraFee(extraFee);
			kyRegister.setRequestId(requestId);

			topupPayChannelBusiness.createKYRegister(kyRegister);

			maps = (Map<String, Object>) kyFastPay(request, orderCode, arriveAccountType);

			return maps;

		} else {

			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", message);

			return maps;
		}

	}

	// 请求支付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ky/fastpay")
	public @ResponseBody Object kyFastPay(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode,
			@RequestParam(value = "arriveAccountType") String arriveAccountType) throws Exception {
		LOG.info("开始进入请求支付接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		// 充值卡卡号
		String userId = resultObj.getString("userid");
		String bankCard = resultObj.getString("bankcard");
		String realAmount = resultObj.getString("realAmount");
		String amount = resultObj.getString("amount");

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

		Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0", userId);

		Object object2 = queryBankCardByCardNoAndUserId.get("result");
		fromObject = JSONObject.fromObject(object2);

		String phone = fromObject.getString("phone");
		String idCard = fromObject.getString("idcard");
		String bankName = fromObject.getString("bankName");
		String userName = fromObject.getString("userName");

		/*
		 * Map<String, Object> bankCodeByBankName =
		 * this.getBankCodeByBankName(Util.queryBankNameByBranchName(bankName1))
		 * ; if (!"000000".equals(bankCodeByBankName.get("resp_code"))) {
		 * maps.put("resp_code", "failed"); maps.put("channel_type", "jf");
		 * maps.put("resp_message", bankCodeByBankName.get("resp_message"));
		 * 
		 * this.addOrderCauseOfFailure(ordercode,
		 * bankCodeByBankName.get("resp_message"));
		 * 
		 * return maps; }
		 * 
		 * String code = (String) bankCodeByBankName.get("result");
		 */

		String bankNum;
		String bankBranchcode;
		try {
			BankNumCode bankNumCode = topupPayChannelBusiness
					.getBankNumCodeByBankName(Util.queryBankNameByBranchName(bankName));

			bankNum = bankNumCode.getBankNum();
			bankBranchcode = bankNumCode.getBankBranchcode();
		} catch (Exception e) {
			e.printStackTrace();
			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", "该通道暂不支持该结算银行,请及时更换结算银行卡!");
			return maps;
		}

		String v = "1.1";// API协议版本
		String method = "prsncollect.quickpay.channelorder.create";// API接口名称
		String timestamp = DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss");// 时间戳，允许的误差毫秒数(10分钟)
																											// 格式为yyyy-mm-dd
																											// HH:mm:ss，例如：2013-08-01
																											// 09:02:05
		String format = APIConstants.API_FORMAT_JSON;// json
														// 指定响应报文格式，默认xml，目前支持：xml、json
		// String appKey = "";//系统分配给应用的ID 100162
		String signMethod = APIConstants.API_SIGN_METHOD_MD5;// md5
																// 参数的加密方法选择，可选值：md5、hmac
		// String secret = "";//签名密钥
		// 应用级参数
		Map<String, String> bizParams = new HashMap();

		bizParams.put("third_trade_no", ordercode);// 第三方订单号
		bizParams.put("amount", amount);// 金额 (元)
		bizParams.put("arrive_account_type", arriveAccountType);// 业务类型 32
																// 有积分，35 无积分，36
																// 封顶
		bizParams.put("from_card_holder_cert_id", idCard);// 支付卡持卡人身份证号
		bizParams.put("from_card_holder_mobile", phone);// 支付卡持卡人手机号
		bizParams.put("from_card_holder_name", userName);// 支付卡持卡人姓名
		bizParams.put("from_card_no", bankCard);// 支付卡号

		BigDecimal bigAmount = new BigDecimal(amount);
		BigDecimal bigRealAmount = new BigDecimal(realAmount);

		bizParams.put("fee", bigAmount.subtract(bigRealAmount) + "");// 手续费(元)
		bizParams.put("notify_url", ipAddress + "/v1.0/paymentchannel/topup/ky/fastpay/notify_call");// 后台通知地址
		bizParams.put("html_url", ipAddress + "/v1.0/paymentchannel/topup/sdjpaysuccess");// 前台通知地址

		bizParams.put("to_card_bank_code", bankBranchcode);// 到账卡银行行号
		bizParams.put("to_card_bank_name", bankName1);// 到账卡银行名称
		bizParams.put("to_card_holder_name", userName1);// 到账卡持卡人姓名
		bizParams.put("to_card_no", cardNo);// 到账卡号
		bizParams.put("third_member_no", userId);// 第三方会员号

		JSONObject fromObject2 = null;
		JSONObject jsonObject = null;

		if ("32".equals(arriveAccountType)) {

			bizParams.put("login_id", login_id);// 接入商户号

			bizParams.put("term_id", term_id);// 终端ID
			bizParams.put("term_mac", term_mac);// 终端Mac

			String callOpenApi = OpenApiUtils.callOpenApi(URL, v, method, timestamp, format, appKey, signMethod, secret,
					bizParams);

			LOG.info("callOpenApi======" + callOpenApi);

			fromObject2 = JSONObject.fromObject(callOpenApi);

			jsonObject = fromObject2.getJSONObject("prsncollect_quickpay_channelorder_create_response");

		} else {

			bizParams.put("login_id", login_id1);// 接入商户号

			bizParams.put("term_id", term_id1);// 终端ID
			bizParams.put("term_mac", term_mac1);// 终端Mac

			String callOpenApi = OpenApiUtils.callOpenApi(URL1, v, method, timestamp, format, appKey1, signMethod,
					secret1, bizParams);

			LOG.info("callOpenApi======" + callOpenApi);

			fromObject2 = JSONObject.fromObject(callOpenApi);

			jsonObject = fromObject2.getJSONObject("prsncollect_quickpay_channelorder_create_response");

		}

		LOG.info("jsonObject======" + jsonObject);

		String success = jsonObject.getString("success");
		String result_code = jsonObject.getString("result_code");

		if ("true".equals(success) && "SUCCESS".equalsIgnoreCase(result_code)) {
			String html_url = jsonObject.getString("html_url");

			String tradeNo = jsonObject.getString("trade_no");

			String url = "http://transactionclear/v1.0/transactionclear/payment/update/thirdordercode";
			MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<String, String>();
			multiValueMap.add("order_code", ordercode);
			multiValueMap.add("third_code", tradeNo);
			String result = restTemplate.postForObject(url, multiValueMap, String.class);

			LOG.info("html_url======" + html_url);

			if ("32".equals(arriveAccountType)) {

				maps.put("resp_code", "success");
				maps.put("channel_type", "jf");
				maps.put("redirect_url", html_url);

			} else if ("35".equals(arriveAccountType)) {

				maps.put("resp_code", "success");
				maps.put("channel_type", "jf");
				maps.put("redirect_url", html_url);

			}

			return maps;

		} else {

			String result_code_msg = jsonObject.getString("result_code_msg");

			maps.put("resp_code", "failed");
			maps.put("channel_type", "jf");
			maps.put("resp_message", result_code_msg);

			this.addOrderCauseOfFailure(ordercode, result_code_msg);

			return maps;
		}

	}

	// 订单查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ky/ordercodequery")
	public @ResponseBody Object orderCodeQuery(HttpServletRequest request,
			@RequestParam(value = "orderCode") String orderCode) throws Exception {
		LOG.info("开始进入订单查询接口======");
		Map<String, String> maps = new HashMap<String, String>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String thirdOrdercode = resultObj.getString("thirdOrdercode");

		String v = "1.1";// API协议版本
		String method = "prsncollect.order.status.query";// API接口名称
		String timestamp = DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss");// 时间戳，允许的误差毫秒数(10分钟)
																											// 格式为yyyy-mm-dd
																											// HH:mm:ss，例如：2013-08-01
																											// 09:02:05
		String format = APIConstants.API_FORMAT_JSON;// json
														// 指定响应报文格式，默认xml，目前支持：xml、json
		String signMethod = APIConstants.API_SIGN_METHOD_MD5;// md5
																// 参数的加密方法选择，可选值：md5、hmac
		// 应用级参数
		Map<String, String> bizParams = new HashMap();

		bizParams.put("trade_no", thirdOrdercode);

		String callOpenApi = OpenApiUtils.callOpenApi(URL, v, method, timestamp, format, appKey, signMethod, secret,
				bizParams);

		LOG.info("callOpenApi======" + callOpenApi);

		JSONObject fromObject2 = JSONObject.fromObject(callOpenApi);
		JSONObject jsonObject = fromObject2.getJSONObject("prsncollect_order_status_query_response");

		LOG.info("jsonObject======" + jsonObject);

		String success = jsonObject.containsKey("success") ? jsonObject.getString("success") : "";
		String result_code = jsonObject.getString("result_code");
		String resp_message = jsonObject.containsKey("resp_message") ? jsonObject.getString("resp_message") : "";

		if ("true".equals(success) && "SUCCESS".equalsIgnoreCase(result_code)) {

			JSONObject jsonObject2 = jsonObject.getJSONObject("prsn_collect_order_entity");
			String status = jsonObject2.getString("status");

			if ("3".equals(status)) {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "支付成功");
				return maps;
			} else if ("0".equals(status)) {
				maps.put(CommonConstants.RESP_CODE, "999998");
				maps.put(CommonConstants.RESP_MESSAGE, "未支付");
				return maps;
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, resp_message);
				return maps;
			}
		} else {

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, resp_message);
			return maps;
		}

	}

	// 通道订单查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ky/thirdordercodequery")
	public @ResponseBody Object thirdOrderCodeQuery(HttpServletRequest request,
			@RequestParam(value = "thirdOrderCode") String thirdOrderCode) throws Exception {
		LOG.info("开始进入订单查询接口======");
		Map<String, String> maps = new HashMap<String, String>();

		String v = "1.1";// API协议版本
		String method = "prsncollect.order.status.query";// API接口名称
		String timestamp = DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss");// 时间戳，允许的误差毫秒数(10分钟)
																											// 格式为yyyy-mm-dd
																											// HH:mm:ss，例如：2013-08-01
																											// 09:02:05
		String format = APIConstants.API_FORMAT_JSON;// json
														// 指定响应报文格式，默认xml，目前支持：xml、json
		String signMethod = APIConstants.API_SIGN_METHOD_MD5;// md5
																// 参数的加密方法选择，可选值：md5、hmac
		// 应用级参数
		Map<String, String> bizParams = new HashMap();

		bizParams.put("trade_no", thirdOrderCode);

		String callOpenApi = OpenApiUtils.callOpenApi(URL, v, method, timestamp, format, appKey, signMethod, secret,
				bizParams);

		LOG.info("callOpenApi======" + callOpenApi);

		JSONObject fromObject2 = JSONObject.fromObject(callOpenApi);
		JSONObject jsonObject = fromObject2.getJSONObject("prsncollect_order_status_query_response");

		LOG.info("jsonObject======" + jsonObject);

		String success = jsonObject.containsKey("success") ? jsonObject.getString("success") : "";
		String result_code = jsonObject.getString("result_code");
		String resp_message = jsonObject.containsKey("resp_message") ? jsonObject.getString("resp_message") : "";

		if ("true".equals(success) && "SUCCESS".equalsIgnoreCase(result_code)) {

			JSONObject jsonObject2 = jsonObject.getJSONObject("prsn_collect_order_entity");
			String status = jsonObject2.getString("status");

			if ("3".equals(status)) {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "支付成功");
				return maps;
			} else if ("0".equals(status)) {
				maps.put(CommonConstants.RESP_CODE, "999998");
				maps.put(CommonConstants.RESP_MESSAGE, "未支付");
				return maps;
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, resp_message);
				return maps;
			}

		} else {

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, resp_message);
			return maps;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/toky/bindcards")
	public @ResponseBody Object returnkyQuickBindCard(HttpServletRequest request,
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
				ipAddress + "/v1.0/paymentchannel/topup/toky/bindcard?bankName=" + URLEncoder.encode(bankName, "UTF-8")
						+ "&cardType=" + URLEncoder.encode(cardtype, "UTF-8") + "&bankCard=" + bankCard + "&ordercode="
						+ orderCode + "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&ipAddress="
						+ ipAddress);

		return maps;
	}

	// 快捷支付异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ky/fastpay/notify_call")
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

		String resultCode = request.getParameter("resultCode");
		String resultMsg = request.getParameter("resultMsg");
		String tradeNo = request.getParameter("tradeNo");
		String status = request.getParameter("status");

		if ("3".equals(status) && "00".equals(resultCode)) {

			this.updateOrderCode(tradeNo, "1", "");

			LOG.info("订单状态修改成功===================" + tradeNo + "====================");

			LOG.info("订单已支付!");

			PrintWriter writer = response.getWriter();
			writer.print("success");
			writer.close();

		}

	}

	// 绑卡异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ky/bindcard/notify_call")
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

	}

	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/tokybankinfo")
	public String tokybankinfo(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		LOG.info("/v1.0/paymentchannel/topup/tokybankinfo=========tokybankinfo");
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

		return "kybankinfo";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/toky/paypage")
	public @ResponseBody Object returnHLJCQuickPayPage(HttpServletRequest request,
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
				ipAddress + "/v1.0/paymentchannel/topup/toky/pay?bankName=" + URLEncoder.encode(bankName, "UTF-8")
						+ "&cardType=" + URLEncoder.encode(cardtype, "UTF-8") + "&bankCard=" + bankCard + "&ordercode="
						+ orderCode + "&expiredTime=" + expiredTime + "&securityCode=" + securityCode + "&ipAddress="
						+ ipAddress);

		return maps;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/toky/qwer")
	public @ResponseBody Object KYQuickqwer(HttpServletRequest request,
			@RequestParam(value = "ordercode") String orderCode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode) throws Exception {

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		// 充值卡卡号
		String userId = resultObj.getString("userid");
		String bankCard = resultObj.getString("bankcard");
		String realAmount = resultObj.getString("realAmount");
		String amount = resultObj.getString("amount");

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

		Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0", userId);

		Object object2 = queryBankCardByCardNoAndUserId.get("result");
		fromObject = JSONObject.fromObject(object2);

		String phone = fromObject.getString("phone");
		String idCard = fromObject.getString("idcard");
		String bankName = fromObject.getString("bankName");
		String userName = fromObject.getString("userName");

		String v = "1.1";// API协议版本
		String method = "prsncollect.quickpay.openorder.create";// API接口名称
		String timestamp = DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss");// 时间戳，允许的误差毫秒数(10分钟)
																											// 格式为yyyy-mm-dd
																											// HH:mm:ss，例如：2013-08-01
																											// 09:02:05
		String format = APIConstants.API_FORMAT_JSON;// json
														// 指定响应报文格式，默认xml，目前支持：xml、json
		// String appKey = "";//系统分配给应用的ID 100162
		String signMethod = APIConstants.API_SIGN_METHOD_MD5;// md5
																// 参数的加密方法选择，可选值：md5、hmac
		// String secret = "";//签名密钥
		// 应用级参数
		Map<String, String> bizParams = new HashMap();

		bizParams.put("arrive_account_type", "40");
		bizParams.put("from_card_holder_cert_id", idCard);// 支付卡持卡人身份证号
		bizParams.put("from_card_holder_mobile", phone);// 支付卡持卡人手机号
		bizParams.put("from_card_holder_name", userName);// 支付卡持卡人姓名
		bizParams.put("from_card_no", bankCard);// 支付卡号

		bizParams.put("front_url", ipAddress + "/v1.0/paymentchannel/topup/sdjpaysuccess");// 支付卡号
		bizParams.put("from_card_no", bankCard);// 支付卡号

		bizParams.put("third_trade_no", orderCode);// 第三方订单号
		bizParams.put("login_id", login_id);// 接入商户号

		bizParams.put("notify_url", ipAddress + "/v1.0/paymentchannel/topup/ky/bindcard/notify_call");
		bizParams.put("third_member_no", userId);
		bizParams.put("third_trade_no", orderCode);

		bizParams.put("term_id", term_id1);// 终端ID
		bizParams.put("term_mac", term_mac1);// 终端Mac
		bizParams.put("org_id", term_mac1);

		BigDecimal bigAmount = new BigDecimal(amount);
		BigDecimal bigRealAmount = new BigDecimal(realAmount);

		bizParams.put("fee", bigAmount.subtract(bigRealAmount) + "");// 手续费(元)
		bizParams.put("notify_url", ipAddress + "/v1.0/paymentchannel/topup/ky/fastpay/notify_call");// 后台通知地址
		bizParams.put("html_url", ipAddress + "/v1.0/paymentchannel/topup/sdjpaysuccess");// 前台通知地址

		// bizParams.put("to_card_bank_code",code);//到账卡银行行号
		bizParams.put("to_card_bank_name", bankName1);// 到账卡银行名称
		bizParams.put("to_card_holder_name", userName1);// 到账卡持卡人姓名
		bizParams.put("to_card_no", cardNo);// 到账卡号
		bizParams.put("third_member_no", userId);// 第三方会员号

		JSONObject fromObject2 = null;
		JSONObject jsonObject = null;

		String callOpenApi = OpenApiUtils.callOpenApi(URL, v, method, timestamp, format, appKey, signMethod, secret,
				bizParams);

		LOG.info("callOpenApi======" + callOpenApi);

		fromObject2 = JSONObject.fromObject(callOpenApi);

		jsonObject = fromObject2.getJSONObject("prsncollect_quickpay_channelorder_create_response");

		LOG.info("jsonObject======" + jsonObject);

		return null;
	}

	/**
	 * 卡友注册信息接口
	 * 
	 * @param bankCard
	 * @param extraFee
	 * @param idCard
	 * @param phone
	 * @param rate
	 * @param requestId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ky/createky")
	public @ResponseBody Object createKY(@RequestParam(value = "bankCard", required = false) String bankCard,
			@RequestParam(value = "extraFee", required = false) String extraFee,
			@RequestParam(value = "idCard", required = false) String idCard,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "rate", required = false) String rate,
			@RequestParam(value = "requestId", required = false) String requestId) {
		Map<String, Object> maps = new HashMap<String, Object>();
		LOG.info("添加注册表信息************/v1.0/paymentchannel/topup/ky/createky");
		KYRegister kyr = new KYRegister();
		kyr.setBankCard(bankCard);
		kyr.setExtraFee(extraFee);
		kyr.setIdCard(idCard);
		kyr.setPhone(phone);
		kyr.setRate(rate);
		kyr.setRequestId(requestId);
		try {
			topupPayChannelBusiness.createKYRegister(kyr);
			LOG.info("卡友进件成功,注册表信息添加成功");
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "添加成功");

		} catch (Exception e) {
			e.printStackTrace();
			LOG.info("卡友进件成功,注册表信息添加失败");
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "添加失败");

		}

		return maps;

	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/ky/queryky")
	public @ResponseBody Object queryKYByidCard(@RequestParam(value = "idCard") String idCard) {
		Map<String, Object> maps = new HashMap<String, Object>();
		LOG.info("获取注册表信息************/v1.0/paymentchannel/topup/ky/queryky");
		KYRegister kyRegister;
		try {
			kyRegister = topupPayChannelBusiness.getKYRegisterByIdCard(idCard);
			if (kyRegister != null) {
				maps.put(CommonConstants.RESP_CODE, "1");
				maps.put(CommonConstants.RESULT, kyRegister);
			} else {
				maps.put(CommonConstants.RESP_CODE, "0");
			}
		} catch (Exception e) {
			e.printStackTrace();
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "查询失败");
		}

		return maps;

	}

}