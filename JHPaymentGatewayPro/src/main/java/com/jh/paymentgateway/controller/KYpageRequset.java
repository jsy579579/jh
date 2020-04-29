package com.jh.paymentgateway.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
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
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.KYBindCard;
import com.jh.paymentgateway.pojo.KYRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.ky.HttpClientUtil;
import com.jh.paymentgateway.util.ky.SignUtils;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;

@Controller
@EnableAutoConfiguration
public class KYpageRequset extends BaseChannel {

	@Autowired
	RedisUtil redisUtil;

	@Value("${payment.ipAddress}")
	private String ip;

	@Autowired
	TopupPayChannelBusiness topupPayChannelBusiness;

	private static final String requestUrl = "http://syxl.openroutemng.com:15202/opengw/router/rest.htm";
	private static final Logger LOG = LoggerFactory.getLogger(KYpageRequset.class);
	// 系统级参数
	private static String v = "1.1";// API协议版本
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static String format = "json";// json 指定响应报文格式，默认xml，目前支持：xml、json
	private static String appKey = "109008";// 系统分配给应用的ID
	private static String signMethod = "md5";// md5 参数的加密方法选择，可选值：md5、hmac
	private static String secret = "a852736a-3634-4cc0-9d45-714870009965";// 签名密钥
	private static String term_id = "T2114781";
	private static String term_mac = "F7B5AE01FB6E4710962B8D4E5603639D";
	private static String login_id = "601201808030001";
	private static String orgId = "SHXL001";

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ky/register")
	public @ResponseBody Object getRegister(@RequestParam(value = "orderCode") String orderCode) throws IOException {

		String method = "mposquery.merchant.incom";// API接口名称
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String userName = prp.getUserName();
		String idCard = prp.getIdCard();
		String phone = prp.getDebitPhone();
		String bankCard = prp.getBankCard();
		String bankNo = prp.getDebitCardNo();
		String bankName = prp.getDebitBankName();
		String cardName = prp.getCreditCardBankName();
		String cardType = prp.getDebitCardCardType();
		String cardtype = prp.getCreditCardCardType();
		String amount = prp.getAmount();
		String exTime = prp.getExpiredTime();
		String expiredTime = this.expiredTimeToMMYY(exTime);
		String securityCode = prp.getSecurityCode();
		String rip = prp.getIpAddress();

		KYRegister kyR = topupPayChannelBusiness.getKYRegisterByIdCard(idCard);
		KYBindCard kyB = topupPayChannelBusiness.getKYBindCardByBankCard(bankCard);
		Map<String, Object> maps = new HashMap<String, Object>();
		if (cardName.contains("中国银行")) {

			if (new BigDecimal(amount).compareTo(new BigDecimal("5000")) > 0) {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "中国银行卡交易金额限制为5000以内,请核对重新输入金额!");

				this.addOrderCauseOfFailure(orderCode, "中国银行卡交易金额限制为5000以内,请核对重新输入金额!", rip);

				return maps;

			}

		} else if (cardName.contains("邮政")) {
			if (new BigDecimal(amount).compareTo(new BigDecimal("4000")) > 0) {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "邮政银行卡交易金额限制为4000以内,请核对重新输入金额!");

				this.addOrderCauseOfFailure(orderCode, "邮政银行卡交易金额限制为4000以内,请核对重新输入金额!", rip);

				return maps;

			}
		} else if (cardName.contains("招商")) {
			if (new BigDecimal(amount).compareTo(new BigDecimal("5000")) > 0) {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "招商银行卡交易金额限制为5000以内,请核对重新输入金额!");

				this.addOrderCauseOfFailure(orderCode, "招商银行卡交易金额限制为5000以内,请核对重新输入金额!", rip);

				return maps;
			}
		} else if (cardName.contains("光大 ")) {
			if (new BigDecimal(amount).compareTo(new BigDecimal("5000")) > 0) {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "光大银行卡交易金额限制为5000以内,请核对重新输入金额!");

				this.addOrderCauseOfFailure(orderCode, "光大银行卡交易金额限制为5000以内,请核对重新输入金额!", rip);

				return maps;
			}
		} else if (cardName.contains("华夏")) {
			if (new BigDecimal(amount).compareTo(new BigDecimal("5000")) > 0) {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "华夏银行卡交易金额限制为5000以内,请核对重新输入金额!");

				this.addOrderCauseOfFailure(orderCode, "华夏银行卡交易金额限制为5000以内,请核对重新输入金额!", rip);

				return maps;
			}
		} else if (cardName.contains("北京")) {
			if (new BigDecimal(amount).compareTo(new BigDecimal("5000")) > 0) {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "北京银行卡交易金额限制为5000以内,请核对重新输入金额!");

				this.addOrderCauseOfFailure(orderCode, "北京银行卡交易金额限制为5000以内,请核对重新输入金额!", rip);

				return maps;
			}
		} else if (cardName.contains("上海")) {
			if (new BigDecimal(amount).compareTo(new BigDecimal("5000")) > 0) {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "上海银行卡交易金额限制为5000以内,请核对重新输入金额!");

				this.addOrderCauseOfFailure(orderCode, "上海银行卡交易金额限制为5000以内,请核对重新输入金额!", rip);

				return maps;
			}
		} else if (cardName.contains("江苏")) {
			if (new BigDecimal(amount).compareTo(new BigDecimal("5000")) > 0) {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "江苏银行卡交易金额限制为5000以内,请核对重新输入金额!");

				this.addOrderCauseOfFailure(orderCode, "江苏银行卡交易金额限制为5000以内,请核对重新输入金额!", rip);

				return maps;
			}
		} else if (cardName.contains("浙江")) {
			if (new BigDecimal(amount).compareTo(new BigDecimal("5000")) > 0) {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "浙江银行卡交易金额限制为5000以内,请核对重新输入金额!");

				this.addOrderCauseOfFailure(orderCode, "浙江银行卡交易金额限制为5000以内,请核对重新输入金额!", rip);

				return maps;
			}
		}

		if (kyR == null) {

			LOG.info("===============KY_QUICK_R=======卡友首次进件==============");
			String requsetNo = System.currentTimeMillis() + "";
			LOG.info("========================卡友申请会员号mercId:" + requsetNo);
			/**
			 * 会员基本信息
			 */
			Map<String, String> mercInfoMap = new HashMap<>();
			mercInfoMap.put("orgId", orgId);// 代理商编号
			mercInfoMap.put("mercId", requsetNo);// 会员号 5211314
			mercInfoMap.put("mercUpperType", "1");// 商户大类 1-会员 2-商户
			mercInfoMap.put("mercTyp", "007");// 会员类型 001:餐娱类 002:房产汽车类 003:一般类
												// 004:批发类 005:民生类 006:公益类
												// 007:非标类
			mercInfoMap.put("mercSts", "0");// 状态 0:正常 1:销户 4:黑名单 5:冻结
			mercInfoMap.put("mercStlSts", "0");// 结算状态 0:正常
			mercInfoMap.put("mercCnm", userName);// 会员名称 李瑞

			/**
			 * 会员营业信息
			 */
			Map<String, String> mercBusiMap = new HashMap<String, String>();
			mercBusiMap.put("crpIdTyp", "00");// 法人证件类型 00:身份证 01:户口本 02:军人身份证
												// 03:警察证 04:港、澳居民往来内地通行证
			// 05:台湾居民来往大陆通行证 06:护照 07:工商营业执照 08:法人证书 09:组织机构代码证 10:其他
			mercBusiMap.put("crpIdNo", idCard);// 法人证件号码
			mercBusiMap.put("crpNm", userName);// 法人名称

			/**
			 * 联系人信息
			 */
			Map<String, String> mercMcntMap = new HashMap<>();
			mercMcntMap.put("cttPsnCnm", userName);// 会员名称
			mercMcntMap.put("mblTel", phone);// 移动电话

			/**
			 * 会员结算账户信息 --以交易时上送为准
			 */
			Map<String, String> mercMactMap = new HashMap<>();
			mercMactMap.put("stlOac", bankNo);// 结算账号
			SimpleDateFormat dateFormatS = new SimpleDateFormat("yyyyMMdd");
			mercMactMap.put("effDt", dateFormatS.format(new Date()));// 生效日期（注册时间）yyyyMMdd
			mercMactMap.put("stlOacCls", "0");// 结算账户类型 0:银行账户
			mercMactMap.put("effFlg", "1");// 生效标识 0：无效 1：有效 2：作废
			mercMactMap.put("deductSign", "1");// 结算账户标志 0:对公 1对私
			mercMactMap.put("dpsbondAcnm", userName);// 账号户名
			mercMactMap.put("dpsbondLbnkNo", "304553013791");// 联行行号（支行）
			mercMactMap.put("dpsbondBnkDesc", "上海宝山区支行");// 银行名称（支行）
			mercMactMap.put("dpsbondSign", "1");// 结算账户标识 0:对公 1对私
			mercMactMap.put("dpsbondBnkProv", "43");// 开户行所在省
			mercMactMap.put("dpsbondBnkCity", "4301");// 开户行所在市

			/**
			 * 会员费率信息 --以交易时上送为准
			 */
			Map<String, String> mercFeeMap = new HashMap<>();
			mercFeeMap.put("t1DebitFeeRat", "0.35");// 借记卡T1交易费率（%）
			mercFeeMap.put("t1DebitFixedFee", "0");// 借记卡T1交易固定手续费（元）
			mercFeeMap.put("t1DebitMinFeeAmt", "0.01");// 借记卡T1交易最低手续费（元）
			mercFeeMap.put("t1DebitMaxFeeAmt", "0");// 借记卡T1交易封顶手续费（元） 当为0时不封顶
			mercFeeMap.put("t1CreditFeeRat", "0.36");// 贷记卡T1交易费率（%）
			mercFeeMap.put("t1CreditFixedFee", "0");// 贷记卡T1交易固定手续费（元）
			mercFeeMap.put("t1CreditMinFeeAmt", "0.01");// 贷记卡T1交易最低手续费（元）
			mercFeeMap.put("t1CreditMaxFeeAmt", "0");// 贷记卡T1交易封顶手续费（元） 当为0时不封顶
			mercFeeMap.put("d0FeeRat", "0.36");// D0交易费率（%）
			mercFeeMap.put("d0FixedFee", "0");// D0交易固定手续费（元）
			mercFeeMap.put("d0MinFeeAmt", "0.01");// D0交易最低手续费（元）
			mercFeeMap.put("d0MaxFeeAmt", "0");// D0交易封顶手续费（元） 当为0时不封顶
			mercFeeMap.put("d0QrFeeRat", "0.36");// 二维码D0交易费率（%）
			mercFeeMap.put("d0QrFixedFee", "0");// 二维码D0交易固定手续费（元）
			mercFeeMap.put("d0QrMinFeeAmt", "0.01");// 二维码D0交易最低手续费（元）
			mercFeeMap.put("d0QrMaxFeeAmt", "0");// 二维码D0交易封顶手续费（元） 当为0时不封顶
			mercFeeMap.put("t1QrFeeRat", "0.34");// 二维码T1交易费率（%）
			mercFeeMap.put("t1QrFixedFee", "0");// 二维码T1交易固定手续费（元）
			mercFeeMap.put("t1QrMinFeeAmt", "0.01");// 二维码T1交易最低手续费（元）
			mercFeeMap.put("t1QrMaxFeeAmt", "0");// 二维码T1交易封顶手续费（元） 当为0时不封顶

			/**
			 * 会员证件信息
			 */
			Map<String, String> mercFileMap = new HashMap<>();

			Map<String, String> body = new HashMap<>();
			body.put("mercInfo", JSON.toJSONString(mercInfoMap));// 会员基本信息
			body.put("mercBusi", JSON.toJSONString(mercBusiMap));// 会员营业信息
			body.put("mercMcnt", JSON.toJSONString(mercMcntMap));// 联系人信息
			body.put("mercFile", JSON.toJSONString(mercFileMap));// 会员证件信息
			body.put("mercMact", JSON.toJSONString(mercMactMap));// 会员结算账户信息
			body.put("mercFee", JSON.toJSONString(mercFeeMap));// 会员费率信息

			// 应用级参数
			Map<String, String> bizParams = new HashMap<>();
			bizParams.put("action", "010");// 代理商会员新增/修改
			bizParams.put("time_interval", System.currentTimeMillis() + "");// 请求的时间戳。
			LOG.info("========================卡友请求进件requestNo:" + requsetNo);
			bizParams.put("request_id", requsetNo);// 请求唯一标识
			bizParams.put("body", JSON.toJSONString(body));

			Map<String, String> signParams = new HashMap<>();
			// 系统级别输入参数
			signParams.put("v", v);// API协议版本，可选值：1.1
			signParams.put("method", method);// API接口名称
			signParams.put("timestamp", dateFormat.format(new Date()));// 时间戳，格式为yyyy-mm-dd
			// HH:mm:ss，例如：2013-08-01 09:02:05
			signParams.put("format", format);// 指定响应报文格式，默认xml，目前支持：xml、json
			signParams.put("app_key", appKey);// 系统分配给应用的ID
			signParams.put("sign_method", signMethod);// 参数的加密方法选择，可选值：md5、hmac
			// 应用级输入参数
			signParams.putAll(bizParams);
			// 计算签名
			String sign = SignUtils.sign(signParams, secret, "md5");
			signParams.put("sign", sign);// 签名

			LOG.info("===========卡友首次进件请求参数：" + JSON.toJSONString(bizParams));
			String result = HttpClientUtil.post(requestUrl, signParams).replaceAll("\n", "").replaceAll("\r", "");
			LOG.info("===========卡友首次进件返回参数：" + result);
			JSONObject jsonobj = JSONObject.parseObject(result);
			String rsJSON = jsonobj.getString("mposquery_merchant_incom_response");
			JSONObject merchantJson = JSONObject.parseObject(rsJSON);
			String respCode = merchantJson.getString("code");
			String respMessage = merchantJson.getString("message");
			String requestid = merchantJson.getString("requset_id");
			LOG.info("返回描述:" + respMessage);
			if ("0000".equals(respCode)) {
				KYRegister saveRegister = new KYRegister();
				saveRegister.setBankCard(bankCard);
				saveRegister.setIdCard(idCard);
				saveRegister.setRequestId(requestid);
				saveRegister.setPhone(phone);
				saveRegister.setUserName(userName);
				saveRegister.setStatus("1");
				topupPayChannelBusiness.createKYRegister(saveRegister);
				LOG.info("-------------------------卡友首次进件成功------------------------");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				maps.put(CommonConstants.RESULT,
						ip + "/v1.0/paymentgateway/quick/ky/jump-Receivablescard-view?bankName="
								+ URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + bankNo + "&bankCard=" + bankCard
								+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount
								+ "&ordercode=" + orderCode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
								+ "&cardtype=" + URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime
								+ "&securityCode=" + securityCode + "&ipAddress=" + ip + "&isRegister=1");
			} else {
				this.addOrderCauseOfFailure(orderCode, respMessage + "[卡友进件异常:" + requsetNo + "]", rip);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
			}
		} else if ("0".equals(kyR.getStatus()) && kyB == null) {
			LOG.info("卡友重新进件=============未绑卡==============发起支付");
			LOG.info("===============KY_QUICK_R=======卡友重新进件==============");
			/**
			 * 会员基本信息
			 */
			String requsetNo = System.currentTimeMillis() + "";
			LOG.info("========================卡友申请会员mercId:" + requsetNo);
			Map<String, String> mercInfoMap = new HashMap<>();
			mercInfoMap.put("orgId", orgId);// 代理商编号
			mercInfoMap.put("mercId", requsetNo);// 会员号 5211314
			mercInfoMap.put("mercUpperType", "1");// 商户大类 1-会员 2-商户
			mercInfoMap.put("mercTyp", "007");// 会员类型 001:餐娱类 002:房产汽车类 003:一般类
												// 004:批发类 005:民生类 006:公益类
												// 007:非标类
			mercInfoMap.put("mercSts", "0");// 状态 0:正常 1:销户 4:黑名单 5:冻结
			mercInfoMap.put("mercStlSts", "0");// 结算状态 0:正常
			mercInfoMap.put("mercCnm", userName);// 会员名称 李瑞

			/**
			 * 会员营业信息
			 */
			Map<String, String> mercBusiMap = new HashMap<String, String>();
			mercBusiMap.put("crpIdTyp", "00");// 法人证件类型 00:身份证 01:户口本 02:军人身份证
												// 03:警察证 04:港、澳居民往来内地通行证
			// 05:台湾居民来往大陆通行证 06:护照 07:工商营业执照 08:法人证书 09:组织机构代码证 10:其他
			mercBusiMap.put("crpIdNo", idCard);// 法人证件号码
			mercBusiMap.put("crpNm", userName);// 法人名称

			/**
			 * 联系人信息
			 */
			Map<String, String> mercMcntMap = new HashMap<>();
			mercMcntMap.put("cttPsnCnm", userName);// 会员名称
			mercMcntMap.put("mblTel", phone);// 移动电话

			/**
			 * 会员结算账户信息 --以交易时上送为准
			 */
			Map<String, String> mercMactMap = new HashMap<>();
			mercMactMap.put("stlOac", bankNo);// 结算账号
			SimpleDateFormat dateFormatS = new SimpleDateFormat("yyyyMMdd");
			mercMactMap.put("effDt", dateFormatS.format(new Date()));// 生效日期（注册时间）yyyyMMdd
			mercMactMap.put("stlOacCls", "0");// 结算账户类型 0:银行账户
			mercMactMap.put("effFlg", "1");// 生效标识 0：无效 1：有效 2：作废
			mercMactMap.put("deductSign", "1");// 结算账户标志 0:对公 1对私
			mercMactMap.put("dpsbondAcnm", userName);// 账号户名
			mercMactMap.put("dpsbondLbnkNo", "304553013791");// 联行行号（支行）
			mercMactMap.put("dpsbondBnkDesc", "上海宝山区支行");// 银行名称（支行）
			mercMactMap.put("dpsbondSign", "1");// 结算账户标识 0:对公 1对私
			mercMactMap.put("dpsbondBnkProv", "43");// 开户行所在省
			mercMactMap.put("dpsbondBnkCity", "4301");// 开户行所在市

			/**
			 * 会员费率信息 --以交易时上送为准
			 */
			Map<String, String> mercFeeMap = new HashMap<>();
			mercFeeMap.put("t1DebitFeeRat", "0.35");// 借记卡T1交易费率（%）
			mercFeeMap.put("t1DebitFixedFee", "0");// 借记卡T1交易固定手续费（元）
			mercFeeMap.put("t1DebitMinFeeAmt", "0.01");// 借记卡T1交易最低手续费（元）
			mercFeeMap.put("t1DebitMaxFeeAmt", "0");// 借记卡T1交易封顶手续费（元） 当为0时不封顶
			mercFeeMap.put("t1CreditFeeRat", "0.36");// 贷记卡T1交易费率（%）
			mercFeeMap.put("t1CreditFixedFee", "0");// 贷记卡T1交易固定手续费（元）
			mercFeeMap.put("t1CreditMinFeeAmt", "0.01");// 贷记卡T1交易最低手续费（元）
			mercFeeMap.put("t1CreditMaxFeeAmt", "0");// 贷记卡T1交易封顶手续费（元） 当为0时不封顶
			mercFeeMap.put("d0FeeRat", "0.36");// D0交易费率（%）
			mercFeeMap.put("d0FixedFee", "0");// D0交易固定手续费（元）
			mercFeeMap.put("d0MinFeeAmt", "0.01");// D0交易最低手续费（元）
			mercFeeMap.put("d0MaxFeeAmt", "0");// D0交易封顶手续费（元） 当为0时不封顶
			mercFeeMap.put("d0QrFeeRat", "0.36");// 二维码D0交易费率（%）
			mercFeeMap.put("d0QrFixedFee", "0");// 二维码D0交易固定手续费（元）
			mercFeeMap.put("d0QrMinFeeAmt", "0.01");// 二维码D0交易最低手续费（元）
			mercFeeMap.put("d0QrMaxFeeAmt", "0");// 二维码D0交易封顶手续费（元） 当为0时不封顶
			mercFeeMap.put("t1QrFeeRat", "0.34");// 二维码T1交易费率（%）
			mercFeeMap.put("t1QrFixedFee", "0");// 二维码T1交易固定手续费（元）
			mercFeeMap.put("t1QrMinFeeAmt", "0.01");// 二维码T1交易最低手续费（元）
			mercFeeMap.put("t1QrMaxFeeAmt", "0");// 二维码T1交易封顶手续费（元） 当为0时不封顶

			/**
			 * 会员证件信息
			 */
			Map<String, String> mercFileMap = new HashMap<>();

			Map<String, String> body = new HashMap<>();
			body.put("mercInfo", JSON.toJSONString(mercInfoMap));// 会员基本信息
			body.put("mercBusi", JSON.toJSONString(mercBusiMap));// 会员营业信息
			body.put("mercMcnt", JSON.toJSONString(mercMcntMap));// 联系人信息
			body.put("mercFile", JSON.toJSONString(mercFileMap));// 会员证件信息
			body.put("mercMact", JSON.toJSONString(mercMactMap));// 会员结算账户信息
			body.put("mercFee", JSON.toJSONString(mercFeeMap));// 会员费率信息

			// 应用级参数
			Map<String, String> bizParams = new HashMap<>();
			bizParams.put("action", "010");// 代理商会员新增/修改
			bizParams.put("time_interval", System.currentTimeMillis() + "");// 请求的时间戳。
			LOG.info("========================卡友请求进件requestNo:" + requsetNo);
			bizParams.put("request_id", requsetNo);// 请求唯一标识
			bizParams.put("body", JSON.toJSONString(body));

			Map<String, String> signParams = new HashMap<>();
			// 系统级别输入参数
			signParams.put("v", v);// API协议版本，可选值：1.1
			signParams.put("method", method);// API接口名称
			signParams.put("timestamp", dateFormat.format(new Date()));// 时间戳，格式为yyyy-mm-dd
			// HH:mm:ss，例如：2013-08-01 09:02:05
			signParams.put("format", format);// 指定响应报文格式，默认xml，目前支持：xml、json
			signParams.put("app_key", appKey);// 系统分配给应用的ID
			signParams.put("sign_method", signMethod);// 参数的加密方法选择，可选值：md5、hmac
			// 应用级输入参数
			signParams.putAll(bizParams);
			// 计算签名
			String sign = SignUtils.sign(signParams, secret, "md5");
			signParams.put("sign", sign);// 签名

			LOG.info("===========卡友二次进件请求参数：" + JSON.toJSONString(bizParams));
			String result = HttpClientUtil.post(requestUrl, signParams).replaceAll("\n", "").replaceAll("\r", "");
			LOG.info("===========卡友二次进件返回参数：" + result);
			JSONObject jsonobj = JSONObject.parseObject(result);
			String rsJSON = jsonobj.getString("mposquery_merchant_incom_response");
			JSONObject merchantJson = JSONObject.parseObject(rsJSON);
			String respCode = merchantJson.getString("code");
			String respMessage = merchantJson.getString("message");
			String requestid = merchantJson.getString("requset_id");
			LOG.info("返回描述:" + respMessage);
			if ("0000".equals(respCode)) {
				kyR.setBankCard(bankCard);
				kyR.setIdCard(idCard);
				kyR.setRequestId(requestid);
				kyR.setPhone(phone);
				kyR.setUserName(userName);
				kyR.setStatus("1");
				kyR.setUpdateTime(new Date());
				topupPayChannelBusiness.createKYRegister(kyR);
				LOG.info("=====================卡友重新进件成功===========================");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				maps.put(CommonConstants.RESULT,
						ip + "/v1.0/paymentgateway/quick/ky/jump-Receivablescard-view?bankName="
								+ URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + bankNo + "&bankCard=" + bankCard
								+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount
								+ "&ordercode=" + orderCode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
								+ "&cardtype=" + URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime
								+ "&securityCode=" + securityCode + "&ipAddress=" + ip + "&isRegister=1");
			} else {
				this.addOrderCauseOfFailure(orderCode, respMessage + "[卡友二次进件异常:" + requsetNo + "]", rip);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			}
		} else if ("0".equals(kyR.getStatus()) && "0".equals(kyB.getStatus())) {
			LOG.info("卡友重新进件=============绑卡未审核================发起支付");
			LOG.info("===============KY_QUICK_R=======卡友重新进件==============");
			
			/**
			 * 会员基本信息
			 */
			String requsetNo = System.currentTimeMillis() + "";
			LOG.info("========================卡友申请会员mercId:" + requsetNo);
			Map<String, String> mercInfoMap = new HashMap<>();
			mercInfoMap.put("orgId", orgId);// 代理商编号
			mercInfoMap.put("mercId", requsetNo);// 会员号 5211314
			mercInfoMap.put("mercUpperType", "1");// 商户大类 1-会员 2-商户
			mercInfoMap.put("mercTyp", "007");// 会员类型 001:餐娱类 002:房产汽车类 003:一般类
												// 004:批发类 005:民生类 006:公益类
												// 007:非标类
			mercInfoMap.put("mercSts", "0");// 状态 0:正常 1:销户 4:黑名单 5:冻结
			mercInfoMap.put("mercStlSts", "0");// 结算状态 0:正常
			mercInfoMap.put("mercCnm", userName);// 会员名称 李瑞

			/**
			 * 会员营业信息
			 */
			Map<String, String> mercBusiMap = new HashMap<String, String>();
			mercBusiMap.put("crpIdTyp", "00");// 法人证件类型 00:身份证 01:户口本 02:军人身份证
												// 03:警察证 04:港、澳居民往来内地通行证
			// 05:台湾居民来往大陆通行证 06:护照 07:工商营业执照 08:法人证书 09:组织机构代码证 10:其他
			mercBusiMap.put("crpIdNo", idCard);// 法人证件号码
			mercBusiMap.put("crpNm", userName);// 法人名称

			/**
			 * 联系人信息
			 */
			Map<String, String> mercMcntMap = new HashMap<>();
			mercMcntMap.put("cttPsnCnm", userName);// 会员名称
			mercMcntMap.put("mblTel", phone);// 移动电话

			/**
			 * 会员结算账户信息 --以交易时上送为准
			 */
			Map<String, String> mercMactMap = new HashMap<>();
			mercMactMap.put("stlOac", bankNo);// 结算账号
			SimpleDateFormat dateFormatS = new SimpleDateFormat("yyyyMMdd");
			mercMactMap.put("effDt", dateFormatS.format(new Date()));// 生效日期（注册时间）yyyyMMdd
			mercMactMap.put("stlOacCls", "0");// 结算账户类型 0:银行账户
			mercMactMap.put("effFlg", "1");// 生效标识 0：无效 1：有效 2：作废
			mercMactMap.put("deductSign", "1");// 结算账户标志 0:对公 1对私
			mercMactMap.put("dpsbondAcnm", userName);// 账号户名
			mercMactMap.put("dpsbondLbnkNo", "304553013791");// 联行行号（支行）
			mercMactMap.put("dpsbondBnkDesc", "上海宝山区支行");// 银行名称（支行）
			mercMactMap.put("dpsbondSign", "1");// 结算账户标识 0:对公 1对私
			mercMactMap.put("dpsbondBnkProv", "43");// 开户行所在省
			mercMactMap.put("dpsbondBnkCity", "4301");// 开户行所在市

			/**
			 * 会员费率信息 --以交易时上送为准
			 */
			Map<String, String> mercFeeMap = new HashMap<>();
			mercFeeMap.put("t1DebitFeeRat", "0.35");// 借记卡T1交易费率（%）
			mercFeeMap.put("t1DebitFixedFee", "0");// 借记卡T1交易固定手续费（元）
			mercFeeMap.put("t1DebitMinFeeAmt", "0.01");// 借记卡T1交易最低手续费（元）
			mercFeeMap.put("t1DebitMaxFeeAmt", "0");// 借记卡T1交易封顶手续费（元） 当为0时不封顶
			mercFeeMap.put("t1CreditFeeRat", "0.36");// 贷记卡T1交易费率（%）
			mercFeeMap.put("t1CreditFixedFee", "0");// 贷记卡T1交易固定手续费（元）
			mercFeeMap.put("t1CreditMinFeeAmt", "0.01");// 贷记卡T1交易最低手续费（元）
			mercFeeMap.put("t1CreditMaxFeeAmt", "0");// 贷记卡T1交易封顶手续费（元） 当为0时不封顶
			mercFeeMap.put("d0FeeRat", "0.36");// D0交易费率（%）
			mercFeeMap.put("d0FixedFee", "0");// D0交易固定手续费（元）
			mercFeeMap.put("d0MinFeeAmt", "0.01");// D0交易最低手续费（元）
			mercFeeMap.put("d0MaxFeeAmt", "0");// D0交易封顶手续费（元） 当为0时不封顶
			mercFeeMap.put("d0QrFeeRat", "0.36");// 二维码D0交易费率（%）
			mercFeeMap.put("d0QrFixedFee", "0");// 二维码D0交易固定手续费（元）
			mercFeeMap.put("d0QrMinFeeAmt", "0.01");// 二维码D0交易最低手续费（元）
			mercFeeMap.put("d0QrMaxFeeAmt", "0");// 二维码D0交易封顶手续费（元） 当为0时不封顶
			mercFeeMap.put("t1QrFeeRat", "0.34");// 二维码T1交易费率（%）
			mercFeeMap.put("t1QrFixedFee", "0");// 二维码T1交易固定手续费（元）
			mercFeeMap.put("t1QrMinFeeAmt", "0.01");// 二维码T1交易最低手续费（元）
			mercFeeMap.put("t1QrMaxFeeAmt", "0");// 二维码T1交易封顶手续费（元） 当为0时不封顶

			/**
			 * 会员证件信息
			 */
			Map<String, String> mercFileMap = new HashMap<>();

			Map<String, String> body = new HashMap<>();
			body.put("mercInfo", JSON.toJSONString(mercInfoMap));// 会员基本信息
			body.put("mercBusi", JSON.toJSONString(mercBusiMap));// 会员营业信息
			body.put("mercMcnt", JSON.toJSONString(mercMcntMap));// 联系人信息
			body.put("mercFile", JSON.toJSONString(mercFileMap));// 会员证件信息
			body.put("mercMact", JSON.toJSONString(mercMactMap));// 会员结算账户信息
			body.put("mercFee", JSON.toJSONString(mercFeeMap));// 会员费率信息

			// 应用级参数
			Map<String, String> bizParams = new HashMap<>();
			bizParams.put("action", "010");// 代理商会员新增/修改
			bizParams.put("time_interval", System.currentTimeMillis() + "");// 请求的时间戳。
			LOG.info("========================卡友请求进件requestNo:" + requsetNo);
			bizParams.put("request_id", requsetNo);// 请求唯一标识
			bizParams.put("body", JSON.toJSONString(body));

			Map<String, String> signParams = new HashMap<>();
			// 系统级别输入参数
			signParams.put("v", v);// API协议版本，可选值：1.1
			signParams.put("method", method);// API接口名称
			signParams.put("timestamp", dateFormat.format(new Date()));// 时间戳，格式为yyyy-mm-dd
			// HH:mm:ss，例如：2013-08-01 09:02:05
			signParams.put("format", format);// 指定响应报文格式，默认xml，目前支持：xml、json
			signParams.put("app_key", appKey);// 系统分配给应用的ID
			signParams.put("sign_method", signMethod);// 参数的加密方法选择，可选值：md5、hmac
			// 应用级输入参数
			signParams.putAll(bizParams);
			// 计算签名
			String sign = SignUtils.sign(signParams, secret, "md5");
			signParams.put("sign", sign);// 签名

			LOG.info("===========卡友二次进件请求参数：" + JSON.toJSONString(bizParams));
			String result = HttpClientUtil.post(requestUrl, signParams).replaceAll("\n", "").replaceAll("\r", "");
			LOG.info("===========卡友二次进件返回参数：" + result);
			JSONObject jsonobj = JSONObject.parseObject(result);
			String rsJSON = jsonobj.getString("mposquery_merchant_incom_response");
			JSONObject merchantJson = JSONObject.parseObject(rsJSON);
			String respCode = merchantJson.getString("code");
			String respMessage = merchantJson.getString("message");
			String requestid = merchantJson.getString("requset_id");
			LOG.info("返回描述:" + respMessage);
			if ("0000".equals(respCode)) {
				kyR.setBankCard(bankCard);
				kyR.setIdCard(idCard);
				kyR.setRequestId(requestid);
				kyR.setPhone(phone);
				kyR.setUserName(userName);
				kyR.setStatus("1");
				kyR.setUpdateTime(new Date());
				topupPayChannelBusiness.createKYRegister(kyR);
				LOG.info("=====================卡友重新进件成功===========================");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				maps.put(CommonConstants.RESULT,
						ip + "/v1.0/paymentgateway/quick/ky/jump-Receivablescard-view?bankName="
								+ URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + bankNo + "&bankCard=" + bankCard
								+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount
								+ "&ordercode=" + orderCode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
								+ "&cardtype=" + URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime
								+ "&securityCode=" + securityCode + "&ipAddress=" + ip + "&isRegister=1");
			} else {
				this.addOrderCauseOfFailure(orderCode, respMessage + "[卡友二次进件异常:" + requsetNo + "]", rip);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			}
		} else if ("0".equals(kyR.getStatus()) && "1".equals(kyB.getStatus())) {
			LOG.info("=============================重新进件,发起支付====================");
			LOG.info("===============KY_QUICK_R=======卡友重新进件==============");
			/**
			 * 会员基本信息
			 */
			String requsetNo = System.currentTimeMillis() + "";
			LOG.info("========================卡友申请会员mercId:" + requsetNo);
			Map<String, String> mercInfoMap = new HashMap<>();
			mercInfoMap.put("orgId", orgId);// 代理商编号
			mercInfoMap.put("mercId", requsetNo);// 会员号 5211314
			mercInfoMap.put("mercUpperType", "1");// 商户大类 1-会员 2-商户
			mercInfoMap.put("mercTyp", "007");// 会员类型 001:餐娱类 002:房产汽车类 003:一般类
												// 004:批发类 005:民生类 006:公益类
												// 007:非标类
			mercInfoMap.put("mercSts", "0");// 状态 0:正常 1:销户 4:黑名单 5:冻结
			mercInfoMap.put("mercStlSts", "0");// 结算状态 0:正常
			mercInfoMap.put("mercCnm", userName);// 会员名称 李瑞

			/**
			 * 会员营业信息
			 */
			Map<String, String> mercBusiMap = new HashMap<String, String>();
			mercBusiMap.put("crpIdTyp", "00");// 法人证件类型 00:身份证 01:户口本 02:军人身份证
												// 03:警察证 04:港、澳居民往来内地通行证
			// 05:台湾居民来往大陆通行证 06:护照 07:工商营业执照 08:法人证书 09:组织机构代码证 10:其他
			mercBusiMap.put("crpIdNo", idCard);// 法人证件号码
			mercBusiMap.put("crpNm", userName);// 法人名称

			/**
			 * 联系人信息
			 */
			Map<String, String> mercMcntMap = new HashMap<>();
			mercMcntMap.put("cttPsnCnm", userName);// 会员名称
			mercMcntMap.put("mblTel", phone);// 移动电话

			/**
			 * 会员结算账户信息 --以交易时上送为准
			 */
			Map<String, String> mercMactMap = new HashMap<>();
			mercMactMap.put("stlOac", bankNo);// 结算账号
			SimpleDateFormat dateFormatS = new SimpleDateFormat("yyyyMMdd");
			mercMactMap.put("effDt", dateFormatS.format(new Date()));// 生效日期（注册时间）yyyyMMdd
			mercMactMap.put("stlOacCls", "0");// 结算账户类型 0:银行账户
			mercMactMap.put("effFlg", "1");// 生效标识 0：无效 1：有效 2：作废
			mercMactMap.put("deductSign", "1");// 结算账户标志 0:对公 1对私
			mercMactMap.put("dpsbondAcnm", userName);// 账号户名
			mercMactMap.put("dpsbondLbnkNo", "304553013791");// 联行行号（支行）
			mercMactMap.put("dpsbondBnkDesc", "上海宝山区支行");// 银行名称（支行）
			mercMactMap.put("dpsbondSign", "1");// 结算账户标识 0:对公 1对私
			mercMactMap.put("dpsbondBnkProv", "43");// 开户行所在省
			mercMactMap.put("dpsbondBnkCity", "4301");// 开户行所在市

			/**
			 * 会员费率信息 --以交易时上送为准
			 */
			Map<String, String> mercFeeMap = new HashMap<>();
			mercFeeMap.put("t1DebitFeeRat", "0.35");// 借记卡T1交易费率（%）
			mercFeeMap.put("t1DebitFixedFee", "0");// 借记卡T1交易固定手续费（元）
			mercFeeMap.put("t1DebitMinFeeAmt", "0.01");// 借记卡T1交易最低手续费（元）
			mercFeeMap.put("t1DebitMaxFeeAmt", "0");// 借记卡T1交易封顶手续费（元） 当为0时不封顶
			mercFeeMap.put("t1CreditFeeRat", "0.36");// 贷记卡T1交易费率（%）
			mercFeeMap.put("t1CreditFixedFee", "0");// 贷记卡T1交易固定手续费（元）
			mercFeeMap.put("t1CreditMinFeeAmt", "0.01");// 贷记卡T1交易最低手续费（元）
			mercFeeMap.put("t1CreditMaxFeeAmt", "0");// 贷记卡T1交易封顶手续费（元） 当为0时不封顶
			mercFeeMap.put("d0FeeRat", "0.36");// D0交易费率（%）
			mercFeeMap.put("d0FixedFee", "0");// D0交易固定手续费（元）
			mercFeeMap.put("d0MinFeeAmt", "0.01");// D0交易最低手续费（元）
			mercFeeMap.put("d0MaxFeeAmt", "0");// D0交易封顶手续费（元） 当为0时不封顶
			mercFeeMap.put("d0QrFeeRat", "0.36");// 二维码D0交易费率（%）
			mercFeeMap.put("d0QrFixedFee", "0");// 二维码D0交易固定手续费（元）
			mercFeeMap.put("d0QrMinFeeAmt", "0.01");// 二维码D0交易最低手续费（元）
			mercFeeMap.put("d0QrMaxFeeAmt", "0");// 二维码D0交易封顶手续费（元） 当为0时不封顶
			mercFeeMap.put("t1QrFeeRat", "0.34");// 二维码T1交易费率（%）
			mercFeeMap.put("t1QrFixedFee", "0");// 二维码T1交易固定手续费（元）
			mercFeeMap.put("t1QrMinFeeAmt", "0.01");// 二维码T1交易最低手续费（元）
			mercFeeMap.put("t1QrMaxFeeAmt", "0");// 二维码T1交易封顶手续费（元） 当为0时不封顶

			/**
			 * 会员证件信息
			 */
			Map<String, String> mercFileMap = new HashMap<>();

			Map<String, String> body = new HashMap<>();
			body.put("mercInfo", JSON.toJSONString(mercInfoMap));// 会员基本信息
			body.put("mercBusi", JSON.toJSONString(mercBusiMap));// 会员营业信息
			body.put("mercMcnt", JSON.toJSONString(mercMcntMap));// 联系人信息
			body.put("mercFile", JSON.toJSONString(mercFileMap));// 会员证件信息
			body.put("mercMact", JSON.toJSONString(mercMactMap));// 会员结算账户信息
			body.put("mercFee", JSON.toJSONString(mercFeeMap));// 会员费率信息

			// 应用级参数
			Map<String, String> bizParams = new HashMap<>();
			bizParams.put("action", "010");// 代理商会员新增/修改
			bizParams.put("time_interval", System.currentTimeMillis() + "");// 请求的时间戳。
			LOG.info("========================卡友请求进件requestNo:" + requsetNo);
			bizParams.put("request_id", requsetNo);// 请求唯一标识
			bizParams.put("body", JSON.toJSONString(body));

			Map<String, String> signParams = new HashMap<>();
			// 系统级别输入参数
			signParams.put("v", v);// API协议版本，可选值：1.1
			signParams.put("method", method);// API接口名称
			signParams.put("timestamp", dateFormat.format(new Date()));// 时间戳，格式为yyyy-mm-dd
			// HH:mm:ss，例如：2013-08-01 09:02:05
			signParams.put("format", format);// 指定响应报文格式，默认xml，目前支持：xml、json
			signParams.put("app_key", appKey);// 系统分配给应用的ID
			signParams.put("sign_method", signMethod);// 参数的加密方法选择，可选值：md5、hmac
			// 应用级输入参数
			signParams.putAll(bizParams);
			// 计算签名
			String sign = SignUtils.sign(signParams, secret, "md5");
			signParams.put("sign", sign);// 签名

			LOG.info("===========卡友二次进件请求参数：" + JSON.toJSONString(bizParams));
			String result = HttpClientUtil.post(requestUrl, signParams).replaceAll("\n", "").replaceAll("\r", "");
			LOG.info("===========卡友二次进件返回参数：" + result);
			JSONObject jsonobj = JSONObject.parseObject(result);
			String rsJSON = jsonobj.getString("mposquery_merchant_incom_response");
			JSONObject merchantJson = JSONObject.parseObject(rsJSON);
			String respCode = merchantJson.getString("code");
			String respMessage = merchantJson.getString("message");
			String requestid = merchantJson.getString("requset_id");
			LOG.info("返回描述:" + respMessage);
			if ("0000".equals(respCode)) {
				kyR.setBankCard(bankCard);
				kyR.setIdCard(idCard);
				kyR.setRequestId(requestid);
				kyR.setPhone(phone);
				kyR.setUserName(userName);
				kyR.setStatus("1");
				kyR.setUpdateTime(new Date());
				topupPayChannelBusiness.createKYRegister(kyR);
				LOG.info("=====================卡友重新进件成功===========================");

				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, "成功");
				maps.put(CommonConstants.RESULT,
						ip + "/v1.0/paymentgateway/quick/ky/jump-Receivablescard-view?bankName="
								+ URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + bankNo + "&bankCard=" + bankCard
								+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount
								+ "&ordercode=" + orderCode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8")
								+ "&cardtype=" + URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime
								+ "&securityCode=" + securityCode + "&ipAddress=" + ip + "&isRegister=2");
			} else {
				this.addOrderCauseOfFailure(orderCode, respMessage + "[卡友二次进件异常:" + requsetNo + "]", rip);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			}
		} else if (kyB == null) {
			LOG.info("卡友进件===================未绑卡==========================发起支付");
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "成功");
			maps.put(CommonConstants.RESULT,
					ip + "/v1.0/paymentgateway/quick/ky/jump-Receivablescard-view?bankName="
							+ URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + bankNo + "&bankCard=" + bankCard
							+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount + "&ordercode="
							+ orderCode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8") + "&cardtype="
							+ URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime + "&securityCode="
							+ securityCode + "&ipAddress=" + ip + "&isRegister=1");
		} else if ("0".equals(kyB.getStatus())) {
			LOG.info("卡友进件=========================绑卡未审核======================发起支付");
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "成功");
			maps.put(CommonConstants.RESULT,
					ip + "/v1.0/paymentgateway/quick/ky/jump-Receivablescard-view?bankName="
							+ URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + bankNo + "&bankCard=" + bankCard
							+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount + "&ordercode="
							+ orderCode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8") + "&cardtype="
							+ URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime + "&securityCode="
							+ securityCode + "&ipAddress=" + ip + "&isRegister=1");

		} else {
			LOG.info("=============================直接发起支付====================");
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "成功");
			maps.put(CommonConstants.RESULT,
					ip + "/v1.0/paymentgateway/quick/ky/jump-Receivablescard-view?bankName="
							+ URLEncoder.encode(bankName, "UTF-8") + "&bankNo=" + bankNo + "&bankCard=" + bankCard
							+ "&cardName=" + URLEncoder.encode(cardName, "UTF-8") + "&amount=" + amount + "&ordercode="
							+ orderCode + "&cardType=" + URLEncoder.encode(cardType, "UTF-8") + "&cardtype="
							+ URLEncoder.encode(cardtype, "UTF-8") + "&expiredTime=" + expiredTime + "&securityCode="
							+ securityCode + "&ipAddress=" + ip + "&isRegister=2");
		}
		return maps;

	}

	/**
	 * 跳转结算卡页面
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/ky/jump-Receivablescard-view")
	public String JumpCard(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		LOG.info("/v1.0/paymentgateway/quick/ky/jump-Receivablescard-view=========tokybankinfo");
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

	/**
	 * 开通支付账户
	 * 
	 * @param orderCode
	 * @param expiredTime
	 * @param securityCode
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ky/open-card")
	public @ResponseBody Object OpenPaymentCard(@RequestParam(value = "ordercode") String orderCode,
			@RequestParam(value = "expiredTime") String expiredTime,
			@RequestParam(value = "securityCode") String securityCode) throws IOException {

		LOG.info("===============KY_QUICK_R=======开通支付账户==============");
		String method = "prsncollect.quickpay.openorder.create";// API接口名称
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String bankCard = prp.getBankCard();
		String userName = prp.getUserName();
		String phoneC = prp.getCreditCardPhone();
		String idCard = prp.getIdCard();
		String rip = prp.getIpAddress();
		Map<String, Object> maps = new HashMap<String, Object>();

		KYRegister kyRQuery = topupPayChannelBusiness.getKYRegisterByIdCard(idCard);
		// 应用级参数
		Map<String, String> bizParams = new HashMap<>();
		String requestNo = System.currentTimeMillis() + "";
		LOG.info("====================开通支付账户requestNo:" + requestNo);
		bizParams.put("third_trade_no", requestNo);// 订单号
		bizParams.put("arrive_account_type", "40");// 业务类型 40 开通快捷业务
		bizParams.put("from_card_holder_cert_id", idCard);// 支付卡持卡人身份证号
		bizParams.put("from_card_holder_mobile", phoneC);// 支付卡持卡人手机号
		bizParams.put("from_card_holder_name", userName);// 支付卡持卡人姓名
		bizParams.put("from_card_no", bankCard);// 支付卡号
		bizParams.put("notify_url", ip + "/v1.0/paymentgateway/kybind?orderCode=" + orderCode);// 后台通知地址
		bizParams.put("front_url", ip + "/v1.0/paymentgateway/quick/ky/pay-view?ordercode=" + orderCode);// 前台通知地址
		bizParams.put("login_id", login_id);// 接入商户号
		bizParams.put("term_id", term_id);// 终端ID
		bizParams.put("term_mac", term_mac);// 终端Mac
		bizParams.put("third_member_no", kyRQuery.getRequestId());// 第三方会员号
		bizParams.put("org_id", orgId);// 机构号

		Map<String, String> signParams = new HashMap<>();
		// 系统级别输入参数
		signParams.put("v", v);// API协议版本，可选值：1.1
		signParams.put("method", method);// API接口名称
		signParams.put("timestamp", dateFormat.format(new Date()));// 时间戳，格式为yyyy-mm-dd
		// HH:mm:ss，例如：2013-08-01
		// 09:02:05
		signParams.put("format", format);// 指定响应报文格式，默认xml，目前支持：xml、json
		signParams.put("app_key", appKey);// 系统分配给应用的ID
		signParams.put("sign_method", signMethod);// 参数的加密方法选择，可选值：md5、hmac
		// 应用级输入参数
		signParams.putAll(bizParams);
		// 计算签名
		String sign = SignUtils.sign(signParams, secret, "md5");
		signParams.put("sign", sign);// 签名

		LOG.info("=================卡友绑卡请求参数:" + JSON.toJSONString(signParams));
		String result = HttpClientUtil.post(requestUrl, signParams).replaceAll("\n", "").replaceAll("\r", "");
		LOG.info("=================卡友绑卡返回参数:" + result);
		JSONObject jsonObject = JSONObject.parseObject(result);
		String skey = jsonObject.getString("prsncollect_quickpay_openorder_create_response");
		JSONObject htmlJson = JSONObject.parseObject(skey);
		String respCode = htmlJson.getString("result_code");
		String respMessage = htmlJson.getString("result_code_msg");
		String html_url = htmlJson.getString("html_url");
		KYBindCard ky = topupPayChannelBusiness.getKYBindCardByBankCard(bankCard);
		if ("SUCCESS".equals(respCode)) {
			if (ky == null) {
				LOG.info("===============================卡未存在，待通过审核");
				KYBindCard kys = new KYBindCard();
				kys.setBankCard(bankCard);
				kys.setIdCard(idCard);
				kys.setPhone(phoneC);
				kys.setUserName(userName);
				kys.setStatus("0");
				topupPayChannelBusiness.createKYBindCard(kys);
			} else if ("0".equals(ky.getStatus())) {
				LOG.info("================================卡已存在，待通过审核");
			}
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, respMessage);
			maps.put("redirect_url", html_url);
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, respMessage);
			LOG.info("===========================卡友开通绑卡异常：" + respMessage);
			this.addOrderCauseOfFailure(orderCode, respMessage + "[请求绑卡异常:" + requestNo + "]", rip);
		}

		return maps;

	}

	/**
	 * 开卡异步通知
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(method = { RequestMethod.POST, RequestMethod.GET }, value = "/v1.0/paymentgateway/kybind")
	public void openFront(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LOG.info("卡友---开卡异步返回参数：" + request.toString());
		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}

		}
		String resultCode = request.getParameter("resultCode");
		String orderCode = request.getParameter("orderCode");
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String bankCard = prp.getBankCard();
		String rip = prp.getIpAddress();
		if ("00".equals(resultCode)) {
			KYBindCard kyb = topupPayChannelBusiness.getKYBindCardByBankCard(bankCard);
			kyb.setStatus("1");
			try {
				topupPayChannelBusiness.createKYBindCard(kyb);
				LOG.info("==========================开卡审核成功:" + bankCard);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.info("==========================开卡审核成功,保存失败:" + bankCard);
			}
			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();
		} else {
			LOG.info("=============================开卡审核失败：" + bankCard);
			this.addOrderCauseOfFailure(orderCode, "开卡审核失败", rip);

			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();
		}

	}

	/**
	 * 支付页面转接
	 * 
	 * @param orderCode
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ky/pay-transfer")
	public @ResponseBody Object PayTransfer(@RequestParam(value = "ordercode") String orderCode) throws IOException {
		Map<String, Object> maps = new HashMap<String, Object>();
		maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		maps.put(CommonConstants.RESP_MESSAGE, "成功");
		maps.put("redirect_url", ip + "/v1.0/paymentgateway/quick/ky/pay-view?ordercode=" + orderCode);
		return maps;

	}

	/**
	 * 跳转支付页面
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/ky/pay-view")
	public String returnHLJCQuickPay(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		String ordercode = request.getParameter("ordercode");
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(ordercode);
		String bankName = prp.getCreditCardBankName();
		String cardType = prp.getCreditCardCardType();
		String bankCard = prp.getBankCard();

		model.addAttribute("ordercode", ordercode);
		model.addAttribute("bankName", bankName);
		model.addAttribute("cardType", cardType);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("ipAddress", ip);

		return "kypaymessage";
	}

	/**
	 * 请求快捷下单
	 * 
	 * @author lirui
	 * @param orderCode
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ky/place-order-sms")
	public @ResponseBody Object PlaceOrder(@RequestParam(value = "ordercode") String orderCode) throws IOException {
		LOG.info("===============KY_QUICK_R=======卡友请求快捷下单==============");
		String method = "prsncollect.quickpay.channelorder.create";// API接口名称
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		Map<String, Object> maps = new HashMap<String, Object>();
		String rip = prp.getIpAddress();
		String bankNo = prp.getDebitCardNo();
		String bankCard = prp.getBankCard();
		String amount = prp.getAmount();
		String idCard = prp.getIdCard();
		String phoneC = prp.getCreditCardPhone();
		String userName = prp.getUserName();
		String cvn2 = prp.getSecurityCode();
		String ExpiredTime = prp.getExpiredTime();
		String expiredTime = this.expiredTimeToMMYY(ExpiredTime);
		String realAmount = prp.getRealAmount();
		String fee = getFee(amount, realAmount);

		KYRegister kyRQuery = topupPayChannelBusiness.getKYRegisterByIdCard(idCard);
		// 到账卡行号
		String bankName = prp.getDebitBankName();
		// 应用级参数
		Map<String, String> bizParams = new HashMap<>();
		bizParams.put("amount", amount);// 金额 (元)
		bizParams.put("arrive_account_type", "32");// 业务类型 32有积分 35无积分 36新无卡
													// 37对公收款
		// bizParams.put("agency_type", "");//通道标识
		bizParams.put("from_card_holder_cert_id", idCard);// 支付卡持卡人身份证号
		bizParams.put("from_card_holder_mobile", phoneC);// 支付卡持卡人手机号
		bizParams.put("from_card_holder_name", userName);// 支付卡持卡人姓名
		bizParams.put("from_card_no", bankCard);// 支付卡号
		bizParams.put("fee", fee);// 手续费(元)
		bizParams.put("notify_url", ip + "/v1.0/paymentgateway/topup/ky/fastpay/call-back");// 交易结果回调地址
		bizParams.put("html_url", "http://106.15.47.73/v1.0/paymentchannel/topup/yldzpaying");// 前台地址
		bizParams.put("login_id", login_id);// 会员号
		bizParams.put("to_card_bank_code", "304553013791");// 到账卡银行行号
		bizParams.put("to_card_bank_name", bankName);// 到账卡银行名称
		bizParams.put("to_card_holder_name", userName);// 到账卡持卡人姓名
		bizParams.put("to_card_no", bankNo);// 到账卡号
		bizParams.put("term_id", term_id);// 终端ID
		bizParams.put("term_mac", term_mac);// 终端Mac
		bizParams.put("third_member_no", kyRQuery.getRequestId());// 第三方会员号，即平台商户号,会员同步mercId
		bizParams.put("third_trade_no", orderCode);// 接入机构订单号
		bizParams.put("cvn2", cvn2);// cvn2
		bizParams.put("exp_date", expiredTime);// 有效期

		Map<String, String> signParams = new HashMap<>();
		// 系统级别输入参数
		signParams.put("v", v);// API协议版本，可选值：1.1
		signParams.put("method", method);// API接口名称
		signParams.put("timestamp", dateFormat.format(new Date()));// 时间戳，格式为yyyy-mm-dd
		// HH:mm:ss，例如：2013-08-01
		// 09:02:05
		signParams.put("format", format);// 指定响应报文格式，默认xml，目前支持：xml、json
		signParams.put("app_key", appKey);// 系统分配给应用的ID
		signParams.put("sign_method", signMethod);// 参数的加密方法选择，可选值：md5、hmac
		// 应用级输入参数
		signParams.putAll(bizParams);
		// 计算签名
		String sign = SignUtils.sign(signParams, secret, "md5");
		signParams.put("sign", sign);// 签名

		System.out.println("============卡友快捷下单请求参数:" + JSON.toJSONString(signParams));
		String result = HttpClientUtil.post(requestUrl, signParams).replaceAll("\n", "").replaceAll("\r", "");
		System.out.println("============卡友快捷下单返回参数:" + result);
		JSONObject jsonObject = JSONObject.parseObject(result);
		String headstr = jsonObject.getString("prsncollect_quickpay_channelorder_create_response");
		JSONObject respJson = JSONObject.parseObject(headstr);
		String respCode = respJson.getString("result_code");
		String respMessage = respJson.getString("result_code_msg");
		String trade_no = respJson.getString("trade_no");
		LOG.info("===============================卡友第三方订单流水号:" + trade_no);
		if ("SUCCESS".equals(respCode)) {
			RestTemplate restTemplate = new RestTemplate();
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			String results = null;
			String url = rip + "/v1.0/transactionclear/payment/update/thirdordercode";

			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("order_code", orderCode);
			requestEntity.add("third_code", trade_no);
			try {
				results = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("第三方流水号添加成功" + trade_no);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("",e);
			}

			LOG.info("第三方流水号添加成功===================" + orderCode + "====================" + results);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, respMessage);
			maps.put("orderId", trade_no);
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, respMessage);
			LOG.info("===========================卡友请求下单异常：" + respMessage);
			this.addOrderCauseOfFailure(orderCode, respMessage + "[下单异常：" + orderCode + "]", rip);
		}
		return maps;
	}

	/**
	 * 快捷支付
	 * 
	 * @author lirui
	 * @param orderCode
	 * @param smsCode
	 * @param trade_no
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ky/fast-pay")
	public @ResponseBody Object fastPay(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "smsCode") String smsCode, @RequestParam(value = "orderId") String trade_no)
					throws IOException {

		LOG.info("===============KY_QUICK_R=======卡友支付确认==============");
		Map<String, Object> maps = new HashMap<String, Object>();

		String method = "prsncollect.quickpay.order.pay";// API接口名称
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String cvn2 = prp.getSecurityCode();
		String ex = prp.getExpiredTime();
		String expiredTime = this.expiredTimeToMMYY(ex);
		String rip = prp.getIpAddress();
		// 应用级参数
		Map<String, String> bizParams = new HashMap<>();
		bizParams.put("trade_no", trade_no);// 统一订单号
		LOG.info("====================确认支付三方:" + trade_no);
		bizParams.put("sms_code", smsCode);// 短信验证码
		bizParams.put("cvn2", cvn2);// cvn2
		bizParams.put("exp_date", expiredTime);// 有效期

		Map<String, String> signParams = new HashMap<>();
		// 系统级别输入参数
		signParams.put("v", v);// API协议版本，可选值：1.1
		signParams.put("method", method);// API接口名称
		signParams.put("timestamp", dateFormat.format(new Date()));// 时间戳，格式为yyyy-mm-dd
		// HH:mm:ss，例如：2013-08-01
		// 09:02:05
		signParams.put("format", format);// 指定响应报文格式，默认xml，目前支持：xml、json
		signParams.put("app_key", appKey);// 系统分配给应用的ID
		signParams.put("sign_method", signMethod);// 参数的加密方法选择，可选值：md5、hmac
		// 应用级输入参数
		signParams.putAll(bizParams);
		// 计算签名
		String sign = SignUtils.sign(signParams, secret, "md5");
		signParams.put("sign", sign);// 签名

		System.out.println("卡友---确认支付---请求参数:" + JSON.toJSONString(signParams));
		String result = HttpClientUtil.post(requestUrl, signParams).replaceAll("\n", "").replaceAll("\r", "");
		System.out.println("卡友---确认支付---返回参数:" + result);
		JSONObject jsonObject = JSONObject.parseObject(result);
		String headJson = jsonObject.getString("prsncollect_quickpay_order_pay_response");
		JSONObject jsonp = JSONObject.parseObject(headJson);
		String respCode = jsonp.getString("result_code");
		String respMessage = jsonp.getString("result_code_msg");
		if ("SUCCESS".equals(respCode)) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, respMessage);
			maps.put("redirect_url", "http://106.15.47.73/v1.0/paymentchannel/topup/sdjpaysuccess");
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, respMessage);
			this.addOrderCauseOfFailure(orderCode, respMessage + "[请求支付异常:" + trade_no + "]", rip);
		}

		return maps;

	}

	/**
	 * 支付异步通知
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(method = { RequestMethod.POST,
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/ky/fastpay/call-back")
	public void paycallback(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LOG.info("卡友---支付异步返回参数：" + request.toString());
		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}
		String tradeNo = request.getParameter("tradeNo");
		String resultCode = request.getParameter("resultCode");
		String resultMsg = request.getParameter("resultMsg");
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(tradeNo);
		if ("00".equals(resultCode)) {
			RestTemplate restTemplate = new RestTemplate();
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			String url = null;
			String result = null;
			LOG.info("*********************交易成功***********************");
			
			url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
			//url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", tradeNo);
			requestEntity.add("third_code", "");
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("",e);
			}

			LOG.info("订单状态修改成功===================" + tradeNo + "====================" + result);

			LOG.info("订单已交易成功!");

			PrintWriter pw = response.getWriter();
			pw.close();
		} else if ("02".equals(resultCode)) {
			LOG.info("交易失败");
			this.addOrderCauseOfFailure(tradeNo, resultMsg + "[支付状态码:02]", prp.getIpAddress());
			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();

		} else if ("05".equals(resultCode)) {
			LOG.info("交易未支付");
			this.addOrderCauseOfFailure(tradeNo, resultMsg + "[支付状态码:05]", prp.getIpAddress());
			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();
		} else if ("98".equals(resultCode)) {
			LOG.info("交易处理中，请稍后查询");
			this.addOrderCauseOfFailure(tradeNo, resultMsg + "[支付状态码:98]", prp.getIpAddress());
			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();
		} else {
			this.addOrderCauseOfFailure(tradeNo, resultMsg + "[未知渠道状态]", prp.getIpAddress());
			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();
		}

	}

	/**
	 * 交易查询
	 * 
	 * @param orderCode
	 *            订单号
	 * 
	 * @param day
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ky/query-pay")
	public @ResponseBody Object QUERY(@RequestParam(value = "orderCode") String third_trade_no,
			@RequestParam(value = "day") String day) throws IOException {
		Map<String, Object> maps = new HashMap<String, Object>();

		String method = "prsncollect.order.payresult.query";// API接口名称

		// 应用级参数
		Map<String, String> bizParams = new HashMap<>();
		bizParams.put("third_trade_no", third_trade_no);// 统一订单号
		bizParams.put("day", day);// yyyy-mm-dd
		bizParams.put("app_key", appKey);
		Map<String, String> signParams = new HashMap<>();
		// 系统级别输入参数
		signParams.put("v", v);// API协议版本，可选值：1.1
		signParams.put("method", method);// API接口名称
		signParams.put("timestamp", dateFormat.format(new Date()));// 时间戳，格式为yyyy-mm-dd
		// HH:mm:ss，例如：2013-08-01
		// 09:02:05
		signParams.put("format", format);// 指定响应报文格式，默认xml，目前支持：xml、json
		signParams.put("app_key", appKey);// 系统分配给应用的ID
		signParams.put("sign_method", signMethod);// 参数的加密方法选择，可选值：md5、hmac
		// 应用级输入参数
		signParams.putAll(bizParams);
		// 计算签名
		String sign = SignUtils.sign(signParams, secret, "md5");
		signParams.put("sign", sign);// 签名

		System.out.println("==============卡友交易查询请求参数:" + JSON.toJSONString(signParams));
		String result = HttpClientUtil.post(requestUrl, signParams).replaceAll("\n", "").replaceAll("\r", "");
		System.out.println("==============卡友交易查询返回参数:" + result);
		JSONObject jsonObject = JSONObject.parseObject(result);
		String headJson = jsonObject.getString("prsncollect_order_payresult_query_response");
		JSONObject jsonp = JSONObject.parseObject(headJson);
		String respCode = jsonp.getString("result_code");
		String respMessage = jsonp.getString("result_code_msg");
		String orderJson = jsonp.getString("prsn_collect_order_entity");
		JSONObject statusJson = JSONObject.parseObject(orderJson);
		String res_msg = statusJson.getString("res_msg");
		if ("SUCCESS".equals(respCode)) {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, res_msg);
			maps.put(CommonConstants.RESULT, result);
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, respMessage);
		}

		return maps;

	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/ky/query-bankCard")
	public @ResponseBody Object queryBankCard(@RequestParam(value = "bankCard") String bankCard) throws IOException {
		Map<String, Object> maps = new HashMap<String, Object>();

		String method = "prsncollect.query.pan.bind";// API接口名称

		// 应用级参数
		Map<String, String> bizParams = new HashMap<>();
		bizParams.put("arrive_account_type", "32");// 统一订单号
		bizParams.put("from_card_no", bankCard);// yyyy-mm-dd
		bizParams.put("org_id", orgId);
		bizParams.put("term_id", term_id);
		Map<String, String> signParams = new HashMap<>();
		// 系统级别输入参数
		signParams.put("v", v);// API协议版本，可选值：1.1
		signParams.put("method", method);// API接口名称
		signParams.put("timestamp", dateFormat.format(new Date()));// 时间戳，格式为yyyy-mm-dd
		// HH:mm:ss，例如：2013-08-01
		// 09:02:05
		signParams.put("format", format);// 指定响应报文格式，默认xml，目前支持：xml、json
		signParams.put("app_key", appKey);// 系统分配给应用的ID
		signParams.put("sign_method", signMethod);// 参数的加密方法选择，可选值：md5、hmac
		// 应用级输入参数
		signParams.putAll(bizParams);
		// 计算签名
		String sign = SignUtils.sign(signParams, secret, "md5");
		signParams.put("sign", sign);// 签名

		System.out.println("==============卡友绑卡查询请求参数:" + JSON.toJSONString(signParams));
		String result = HttpClientUtil.post(requestUrl, signParams).replaceAll("\n", "").replaceAll("\r", "");
		System.out.println("==============卡友绑卡查询返回参数:" + result);
		return result;
	}

	/**
	 * 手续费自清
	 * 
	 * @param amount
	 * @param realAmount
	 * @return
	 */
	public static String getFee(String amount, String realAmount) {
		BigDecimal amt = new BigDecimal(amount);
		BigDecimal Ramt = new BigDecimal(realAmount);
		BigDecimal fee = amt.subtract(Ramt);
		return fee.toString();

	}

}
