package com.jh.paymentchannel.service;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import com.alibaba.fastjson.JSON;
import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.KYRegister;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.ky.APIConstants;
import com.jh.paymentchannel.util.ky.OpenApiUtils;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.tools.Tools;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.Md5Util;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class HTpageRequest extends BaseChannel {

	private static final Logger LOG = LoggerFactory.getLogger(HTpageRequest.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private Util util;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	private String appKey = "100965";

	private String secret = "52374d40-068c-4887-8a5f-c86f834386c9";

	private String orgId = "CSJGJR02";

	private String login_id = "801123456780001";

	private String term_id = "T0707784";

	private String term_mac = "EFA1DA375B624CB6908561703D9BE8AC";

	private String merchant_id = "801123456780001";

	private String URL = "http://tst.txjk.enjoyfin.cn:15202/opengw/router/rest.htm";

	// 海淘商城================================
	private String HTShopUrl = "http://tuotuo.enjoyfin.com:8999/shangcheng/";

	private String merchantNumber = "a15783";

	private String appkey = "100973";

	private String termId = "T1038139";

	private String salt = "3t6tZRixAn4sgO45";

	private static final Charset UTF_8 = StandardCharsets.UTF_8;

	// 下单接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/htshop/createordercode")
	public @ResponseBody Object withdraw(@RequestParam(value = "phone") String phone,
			@RequestParam(value = "brandId", required = false, defaultValue = "-1") String sbrandId,
			@RequestParam(value = "amount") String amount,
			@RequestParam(value = "order_desc", required = false, defaultValue = "境外二维码") String orderdesc) {

		Map<String, Object> map = new HashMap<String, Object>();

		/** 首先看在不在黑名单里面，如果在不能登录 */
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("risk", "error url request!");
		String url = uri.toString() + "/v1.0/risk/blackwhite/query/phone";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("phone", phone);
		/** 0为登陆操作 */
		requestEntity.add("operation_type", "2");// 0 表示登陆无法进行 1表示无法充值 2 表示无法提现
													// 3 无法支付
		JSONObject jsonObject;
		String rescode;
		String result;
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			rescode = jsonObject.getString("resp_code");
		} catch (Exception e) {
			LOG.error("==========/v1.0/risk/blackwhite/query/phone查询黑白名单异常===========" + e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
			return map;
		}
		if (!rescode.equalsIgnoreCase(CommonConstants.SUCCESS)) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "系统正在维护中");
			return map;
		}

		long lbrandId = -1;
		try {
			lbrandId = Long.valueOf(sbrandId);
		} catch (NumberFormatException e2) {
			lbrandId = -1;
		}
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/query/phone";
		/** 根据的用户手机号码查询用户的基本信息 */
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("phone", phone);
		requestEntity.add("brandId", lbrandId + "");
		restTemplate = new RestTemplate();
		JSONObject resultObju;
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			resultObju = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("==========/v1.0/user/query/phone查询用户异常===========" + e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
			return map;
		}
		String userId = "0";// 0表示没有用户，
		if (resultObju.containsKey("id")) {
			userId = resultObju.getString("id");
		}
		String brandId = "-1";// 给贴牌赋值初始值为空
		if (resultObju.containsKey("brandId")) {
			brandId = resultObju.getString("brandId");
		}

		if (Tools.checkAmount(amount) == false) {

			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "支付金额有错");
			return map;
		}

		/** 调用下单，需要得到用户的订单信息 */
		uri = util.getServiceUrl("transactionclear", "error url request!");
		url = uri.toString() + "/v1.0/transactionclear/payment/add";

		/** 根据的用户手机号码查询用户的基本信息 */
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("type", "0");
		requestEntity.add("phone", phone);
		requestEntity.add("amount", amount);
		requestEntity.add("channel_tag", "ABROAD_CONSUME");
		requestEntity.add("desc", orderdesc);
		JSONObject resultObj;
		String order;
		long brandid;
		long userid;
		String realAmount;
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT================" + result);
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
			order = resultObj.getString("ordercode");
			brandid = resultObj.getLong("brandid");
			userid = resultObj.getLong("userid");
			realAmount = resultObj.getString("realAmount");
		} catch (Exception e) {
			LOG.error("==========/v1.0/transactionclear/payment/add 添加订单有误===========" + e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.网络出错了哦,臣妾已经尽力了,请重试~");
			return map;
		}

		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "下单成功");
		map.put(CommonConstants.RESULT, order);
		return map;

	}

	// 验证是否进件接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/htshop/isregister")
	public @ResponseBody Object kyIsRegister(@RequestParam(value = "orderCode") String orderCode) throws Exception {
		LOG.info("开始进入验证是否进件接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		// 充值卡卡号
		String userId = resultObj.getString("userid");

		Map<String, Object> queryBankCardByUserId = this.queryBankCardByUserId(userId);
		if (!"000000".equals(queryBankCardByUserId.get("resp_code"))) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, queryBankCardByUserId.get("resp_message"));
			return maps;
		}

		Object object3 = queryBankCardByUserId.get("result");
		fromObject = JSONObject.fromObject(object3);

		String idCard = fromObject.getString("idcard");

		KYRegister kyRegister = topupPayChannelBusiness.getKYRegisterByIdCard(idCard);

		if (kyRegister == null) {
			LOG.info("发起进件======");

			maps = (Map<String, Object>) kyRegister(orderCode);

			if ("000000".equals(maps.get("resp_code"))) {

				Map<String, Object> kyAccess = (Map<String, Object>) kyAccess(orderCode);

				return kyAccess;
			} else {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, maps.get("resp_message"));
				return maps;
			}

		} else {

			Map<String, Object> kyAccess = (Map<String, Object>) kyAccess(orderCode);

			return kyAccess;

		}

	}

	// 进件接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/htshop/register")
	public @ResponseBody Object kyRegister(@RequestParam(value = "ordercode") String orderCode) throws Exception {
		LOG.info("开始进入进件接口========================");
		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		if (!"000000".equals(queryOrdercode.get("resp_code"))) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, queryOrdercode.get("resp_message"));
			return maps;
		}

		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String userId = resultObj.getString("userid");
		String rate = resultObj.getString("rate");
		String extraFee = resultObj.getString("extraFee");
		String mobile = resultObj.getString("phone");

		Map<String, Object> queryBankCardByUserId = this.queryBankCardByUserId(userId);
		if (!"000000".equals(queryBankCardByUserId.get("resp_code"))) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, queryBankCardByUserId.get("resp_message"));
			return maps;
		}

		Object object3 = queryBankCardByUserId.get("result");
		fromObject = JSONObject.fromObject(object3);

		String cardNo = fromObject.getString("cardNo");
		String userName = fromObject.getString("userName");
		String idCard = fromObject.getString("idcard");
		String phone = fromObject.getString("phone");
		String bankName = fromObject.getString("bankName");

		Map<String, Object> bankUnitNo = this.getBankUnitNo(Util.queryBankNameByBranchName(bankName));
		if (!"000000".equals(bankUnitNo.get("resp_code"))) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, bankUnitNo.get("resp_message"));

			this.addOrderCauseOfFailure(orderCode, bankUnitNo.get("resp_message"));

			return maps;
		}

		String inBankUnitNo = (String) bankUnitNo.get("result");

		Map<String, Object> bankCodeByBankName = this.getBankCodeByBankName(Util.queryBankNameByBranchName(bankName));
		if (!"000000".equals(bankCodeByBankName.get("resp_code"))) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, bankCodeByBankName.get("resp_message"));

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
		String format = APIConstants.API_FORMAT_JSON;// json 指定响应报文格式，默认xml，目前支持：xml、json
		// String appKey = "100158";//系统分配给应用的ID
		String signMethod = APIConstants.API_SIGN_METHOD_MD5;// md5 参数的加密方法选择，可选值：md5、hmac
		// String secret = "5dd301cf-82cc-4f41-b671-72a930f77e70";//签名密钥

		/**
		 * 会员基本信息
		 */
		Map<String, String> mercInfoMap = new HashMap<String, String>();
		mercInfoMap.put("orgId", orgId);// 代理商编号
		mercInfoMap.put("mercId", userId);// 会员号
		mercInfoMap.put("mercUpperType", "2");// 商户大类 1-会员 2-商户
		mercInfoMap.put("mercTyp", "003");// 会员类型 001:餐娱类 002:房产汽车类 003:一般类 004:批发类 005:民生类 006:公益类 007:非标类
		mercInfoMap.put("mercSts", "0");// 状态 0:正常 1:销户 4:黑名单 5:冻结
		mercInfoMap.put("mercStlSts", "0");// 结算状态 0:正常
		mercInfoMap.put("mercCnm", "莘丽");// 会员名称

		/**
		 * 会员营业信息
		 */
		Map<String, String> mercBusiMap = new HashMap<String, String>();
		mercBusiMap.put("crpIdTyp", "00");// 法人证件类型 00:身份证 01:户口本 02:军人身份证 03:警察证 04:港、澳居民往来内地通行证
		// 05:台湾居民来往大陆通行证 06:护照 07:工商营业执照 08:法人证书 09:组织机构代码证 10:其他
		mercBusiMap.put("crpIdNo", idCard);// 法人证件号码
		mercBusiMap.put("crpNm", userName);// 法人名称

		/**
		 * 联系人信息
		 */
		Map<String, String> mercMcntMap = new HashMap<String, String>();
		mercMcntMap.put("cttPsnCnm", userName);// 会员名称
		mercMcntMap.put("mblTel", phone);// 移动电话

		/**
		 * 会员结算账户信息 --以交易时上送为准
		 */
		Map<String, String> mercMactMap = new HashMap<String, String>();
		mercMactMap.put("stlOac", "55555555");// 结算账号
		mercMactMap.put("effDt", DateUtil.getDateStringConvert(new String(), new Date(), "yyyyMMdd"));// 生效日期（注册时间）yyyyMMdd
		mercMactMap.put("stlOacCls", "0");// 结算账户类型 0:银行账户
		mercMactMap.put("effFlg", "1");// 生效标识 0：无效 1：有效 2：作废
		mercMactMap.put("deductSign", "1");// 结算账户标志 0:对公 1对私
		mercMactMap.put("dpsbondAcnm", userName);// 账号户名
		mercMactMap.put("dpsbondLbnkNo", code);// 联行行号（支行）
		mercMactMap.put("dpsbondBnkDesc", bankName);// 银行名称
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
			kyRegister.setIdCard(idCard);
			kyRegister.setRate(rate);
			kyRegister.setExtraFee(extraFee);
			kyRegister.setRequestId(requestId);
			kyRegister.setUserId(userId);

			topupPayChannelBusiness.createKYRegister(kyRegister);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "进件成功");

			return maps;

		} else {

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, message);

			return maps;
		}

	}

	// 海淘商城接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/htshop/pay")
	public @ResponseBody Object htShopPay(
			@RequestParam(value = "userId", required = false, defaultValue = "433837908") String userId)
			throws Exception {
		LOG.info("开始进入海淘商城接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		String uid = userId + "_" + termId + "_" + appkey;

		String str = Md5Util.getMD5(Md5Util.getMD5(uid) + salt).toLowerCase();

		String md5Str = str.substring(0, 3) + str.substring(str.length() - 3, str.length());

		String encrypt = uid + md5Str;

		LOG.info("uid======" + uid);
		LOG.info("str======" + str);
		LOG.info("md5Str======" + md5Str);
		LOG.info("encrypt======" + encrypt);

		return ResultWrap.init(CommonConstants.SUCCESS, "请求成功", "http://tuotuo.enjoyfin.com:8999/shangcheng/?encrypt=" + encrypt + "&merchant_number=" + merchantNumber);

	}

	// 境外二维码接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/htshop/access")
	public @ResponseBody Object kyAccess(@RequestParam(value = "orderCode") String orderCode) throws Exception {
		LOG.info("开始进入支付接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		// 充值卡卡号
		String userId = resultObj.getString("userid");
		String realAmount = resultObj.getString("realAmount");
		String amount = resultObj.getString("amount");

		String v = "1.1";// API协议版本
		String method = "linkea.prsn.order.createOverseasQrOrder";// API接口名称
		String timestamp = DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss");// 时间戳，允许的误差毫秒数(10分钟)
																											// 格式为yyyy-mm-dd
		BigDecimal bigAmount = new BigDecimal(amount); // HH:mm:ss，例如：2013-08-01

		String format = APIConstants.API_FORMAT_JSON;// json 指定响应报文格式，默认xml，目前支持：xml、json
		String signMethod = APIConstants.API_SIGN_METHOD_MD5;// md5 参数的加密方法选择，可选值：md5、hmac
		// 应用级参数
		Map<String, String> bizParams = new HashMap();

		bizParams.put("down_order_no", orderCode);
		bizParams.put("member_no", userId);
		bizParams.put("pay_type", "1");
		bizParams.put("trade_amt", bigAmount.multiply(new BigDecimal("8")) + "");
		bizParams.put("term_id", term_id);
		bizParams.put("term_mac", term_mac);
		bizParams.put("currency", "PHP");
		bizParams.put("callback_url", ipAddress + "/v1.0/paymentchannel/topup/htshop/fastpay/notify_call");
		bizParams.put("merchant_id", merchant_id);

		LOG.info("请求报文======" + bizParams);

		String callOpenApi = OpenApiUtils.callOpenApi(URL, v, method, timestamp, format, appKey, signMethod, secret,
				bizParams);

		LOG.info("callOpenApi======" + callOpenApi);

		JSONObject fromObject2 = JSONObject.fromObject(callOpenApi);

		JSONObject jsonObject = fromObject2.getJSONObject("linkea_prsn_order_createOverseasQrOrder_response");

		LOG.info("jsonObject======" + jsonObject);

		String resultCode = jsonObject.getString("result_code");
		String resultMsg = jsonObject.getString("result_code_msg");

		if ("00".equals(resultCode)) {

			String html_url = jsonObject.getString("html_url");

			return ResultWrap.init(CommonConstants.SUCCESS, "成功", html_url);

		} else {

			return ResultWrap.init(CommonConstants.FALIED, resultMsg);
		}

	}

	// 境外二维码异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/htshop/fastpay/notify_call")
	public void htFastPayNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("支付异步通知进来了=======");

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

		if ("00".equals(resultCode)) {

			String tradeNo = request.getParameter("tradeNo");
			String status = request.getParameter("status");

			if ("3".equals(status)) {

				this.updateOrderCode(tradeNo, "1", "");

			}

		}

	}

}