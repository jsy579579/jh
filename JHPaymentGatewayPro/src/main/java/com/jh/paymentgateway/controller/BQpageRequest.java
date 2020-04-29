package com.jh.paymentgateway.controller;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
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
import com.jh.paymentgateway.pojo.BQBankCard;
import com.jh.paymentgateway.pojo.BQRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.bq.CertificateUtils;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;

@Controller
@EnableAutoConfiguration
public class BQpageRequest extends BaseChannel {

	private static final Logger LOG = LoggerFactory.getLogger(BQpageRequest.class);

	@Autowired
	private RedisUtil redisUtil;

	@Value("${payment.ipAddress}")
	private String ip;
	
	@Value("${bq.cerpath}")
	private String cerpath;
	
	@Value("${bq.jkspath}")
	private String jkspath;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

//	private String privateKeyUrl = jkspath;
	private static String password = "1125@123";
	private static PrivateKey privateKey = null;
	private static String regOrgCode = "1125";

	/**
	 * 注册/修改费率
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/bq/register")
	public @ResponseBody Object register(@RequestParam(value = "settleName") String settleName,
			@RequestParam(value = "userId") String userId, 
			@RequestParam(value = "settlePhone") String settlePhone,
			@RequestParam(value = "settleIdNum") String settleIdNum,
			@RequestParam(value = "settleNum") String settleNum, 
			@RequestParam(value = "transRate") String transRate)
					throws Exception {

		LOG.info("开始进入注册接口----------");

		Map<String, Object> map = new HashMap<String, Object>();

		SimpleDateFormat sd = new SimpleDateFormat("YYYYMMddHHmmss");
		String date = sd.format(new Date());

		BQRegister bqRegister = topupPayChannelBusiness.getBQRegisterByIdNum(settleIdNum);
		BQRegister bQ = new BQRegister();
		// 百分制
		String bigRate = new BigDecimal(transRate).multiply(new BigDecimal("100")).setScale(2).toString();

		if (bqRegister == null) {// 未注册
			
			Map<String, String> req = new TreeMap();
			req.put("orderId", "jh" + date);// 交易流水号 系统唯⼀
			req.put("regOrgCode", regOrgCode);// 注册机构号
			req.put("mchtName", settleName + userId);// 商户名称
			req.put("settleName", settleName);// 商户姓名
			req.put("settleNum", settleNum);// 商户结算卡号
			req.put("settlePhone", settlePhone);// 商户结算手机号
			req.put("settleIdNum", settleIdNum);// 商户证件号
			req.put("areaCode", "021");// 地区号 不严谨
			req.put("transChannel", "04");// 支付类型 固定
			req.put("transRate", bigRate);// 商户交易费率 0.41% 最低 5.0
			req.put("withDrawRate", "2");// 代付费率 固定 2
			String sign = sign(req);
			req.put("sign", sign);

			LOG.info("进件请求参数:" + req);

			// 保存用户信息
			bQ.setUserId(userId);
			bQ.setUserName(settleName);
			bQ.setPhone(settlePhone);
			bQ.setBankCard(settleNum);// 结算卡
			bQ.setIdNum(settleIdNum);// 身份号
			bQ.setRate(bigRate);// 商户交易费率
			bQ.setExtraFee("2");// 代付费率 固定 2

			String resp = sendPost("http://47.96.160.164:8080/gatewaysite/p/regist", JSON.toJSONString(req));
			TreeMap<String, String> treeMap = JSON.parseObject(resp, TreeMap.class);
			boolean verify = verify(treeMap);

			LOG.info("进件返回参数:" + resp);

			JSONObject json_obj = JSON.parseObject(resp);
			String orgId = json_obj.getString("orgId");// 注册返回的机构号 后期交易用
			String mchtId = json_obj.getString("mchtId");// 注册返回的商户号 后期交易用
			String respCode = json_obj.getString("respCode");// 返回码
			String respMsg = json_obj.getString("respMsg");// 返回信息
			if ("0000".equals(respCode)) {
				bQ.setMchtId(mchtId);
				bQ.setOrgId(orgId);
				bQ.setCreateTime(new Date());
				// 记录用户信息
				System.out.println("《《-------------------------记录用户信息开始----------------------------------》》");
				LOG.info("姓名:" + settleName + "，id:" + userId + "，phone:" + settlePhone + "，结算卡:" + settleNum
						+ "，注册交易订单号:" + "jh" + date + "，机构号" + orgId + "，商户号:" + mchtId);
				System.out.println("《《-------------------------记录用户信息结束----------------------------------》》");

				topupPayChannelBusiness.createBQRegister(bQ);

				map.put(CommonConstants.RESULT, respMsg);
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "进件成功");
				return map;
			} else {
				LOG.info("进件异常-------");
				map.put(CommonConstants.RESULT, respMsg);
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "进件失败");
				return map;
			}
		} else {// 已注册过
			bqRegister = topupPayChannelBusiness.getBQRegisterByIdNum(settleIdNum);
			String rate = bqRegister.getRate();// 数据库中的费率
			String mchtId = bqRegister.getMchtId();
			String orgId = bqRegister.getOrgId();
			// 判断费率是否相同，不同修改费率
			System.out.println(rate + "---" + bigRate);
			if (!rate.equals(bigRate)) {
				LOG.info("费率不同，修改费率，更新用户信息-------");

				Map<String, String> req = new TreeMap();
				req.put("orderId", "jh" + date);// 交易流水号 系统唯⼀
				req.put("regOrgCode", regOrgCode);// 注册机构号  
				req.put("mchtId", mchtId);// 注册返回的机构号
//				req.put("orgId", orgId);// 注册机构号
				req.put("mchtName", settleName + userId);// 商户名称
				req.put("settleName", settleName);// 商户姓名
				req.put("settleNum", settleNum);// 商户结算卡号
				req.put("settlePhone", settlePhone);// 商户结算手机号
				req.put("settleIdNum", settleIdNum);// 商户证件号
//				req.put("areaCode", "021");// 地区号 不严谨
				req.put("transChannel", "04");// 支付类型 固定
				req.put("transRate", bigRate);// 商户交易费率 0.41% 最低 5.0
				req.put("withDrawRate", "2");// 代付费率 固定 2
				String sign = sign(req);
				req.put("sign", sign);

				LOG.info("修改费率请求参数:" + req);	

				String resp = sendPost("http://47.96.160.164:8080/gatewaysite/p/mchtModify", JSON.toJSONString(req));
				TreeMap<String, String> treeMap = JSON.parseObject(resp, TreeMap.class);
				boolean verify = verify(treeMap);

				LOG.info("修改费率返回参数:" + resp);

				JSONObject json_obj = JSON.parseObject(resp);
				String respCode = json_obj.getString("respCode");// 返回码
				String respMsg = json_obj.getString("respMsg");// 返回信息
				if ("0000".equals(respCode)) {
					//修改费率保存
					bqRegister.setRate(bigRate);
					bqRegister.setChangeTime(new Date());
					// 记录用户信息
					System.out.println("《《-------------------------重新记录用户信息开始----------------------------------》》");
					LOG.info("姓名:" + settleName + "，id:" + userId + "，phone:" + settlePhone + "，修改后的费率:" + bigRate
							+ "，注册交易订单号:" + "jh" + date + "，机构号" + orgId + "，商户号:" + mchtId);
					System.out.println("《《-------------------------重新记录用户信息结束----------------------------------》》");

					topupPayChannelBusiness.createBQRegister(bqRegister);

					map.put(CommonConstants.RESULT, respMsg);
					map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					map.put(CommonConstants.RESP_MESSAGE, "修改费率成功");
					return map;
				} else {
					LOG.info("修改费率异常-------");
					map.put(CommonConstants.RESULT, respMsg);
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "修改费率失败");
					return map;
				}
			} else {
				LOG.info("已进件-------");
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "用户已进件");
				return map;
			}

		}

	}

	/**
	 * 预签约
	 * 
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/bq/beSign")
	public @ResponseBody Object beSign(
			@RequestParam(value = "bankCard") String acct_no,
			@RequestParam(value = "name") String acct_name, 
			@RequestParam(value = "phone") String acct_phone,
			@RequestParam(value = "idCard") String idNum, 
			@RequestParam(value = "securityCode") String acct_cvv2,
			@RequestParam(value = "expiredTime") String acct_validdate
			) throws Exception {

		LOG.info("开始进入预签约接口--------");

		Map<String, String> maps = new HashMap<String, String>();

		BQRegister bQRegister = topupPayChannelBusiness.getBQRegisterByIdNum(idNum);// 注册

		BQBankCard bQBankCard = topupPayChannelBusiness.getBQBankCardByIdNum(idNum, acct_no);// 查询该卡是否已经绑定
		BQBankCard bQBankCardWait = topupPayChannelBusiness.getBQBankCardByIdNumSure(idNum, acct_no);// 查询该卡是否已经预签约
		BQBankCard bqBankCard = new BQBankCard();
		String bankCard = "";
		String status = "";
		if (bQBankCard != null) {
			bankCard = bQBankCard.getAcctNo();
			status = bQBankCard.getStatus();// 卡绑定的状态 0：未绑定 1：已绑定
		}

		SimpleDateFormat sd = new SimpleDateFormat("YYYYMMddHHmmss");
		String date = sd.format(new Date());
		String orderId = "jh" + date;
		if (bQRegister != null && bQBankCard == null && bQBankCardWait == null) {// 已进件,未绑卡,未预签约
			// 获取用户信息
			String userId = bQRegister.getUserId();
			String userName = bQRegister.getUserName();
			String phone = bQRegister.getPhone();
			String orgId = bQRegister.getOrgId();
			String mchtId = bQRegister.getMchtId();

			Map<String, String> req = new TreeMap();
			req.put("orderId", orderId);// 交易流水号 系统唯⼀
			req.put("orgId", orgId);// 机构号
			req.put("mchtId", mchtId);// 商户号
			req.put("transTime", date);// 交易时间
			req.put("transChannel", "04");// 支付类型 固定
			req.put("acct_no", acct_no);// 交易卡号
			req.put("acct_name", acct_name);// 交易人姓名
			req.put("acct_phone", acct_phone);// 交易人电话
			req.put("idNum", idNum);// 交易人身份证号
			req.put("acct_cvv2", acct_cvv2);// 安全码

			String acctValiddate = this.expiredTimeToYYMM(acct_validdate);
			System.out.println(acctValiddate);
			req.put("acct_validdate", acctValiddate);// 有效期 YYMM

			String sign = sign(req);
			req.put("sign", sign);

			LOG.info("签约请求参数:" + req);

			String resp = sendPost("http://47.96.160.164:8080/gatewaysite/c/beSign", JSON.toJSONString(req));
			TreeMap<String, String> treeMap = JSON.parseObject(resp, TreeMap.class);
			boolean verify = verify(treeMap);
			LOG.info("签约请求返回参数:" + resp);
			JSONObject json_obj = JSON.parseObject(resp);
			String respCode = json_obj.getString("respCode");// 返回码
			String respMsg = json_obj.getString("respMsg");// 返回信息
			if ("0000".equals(respCode)) {
				// 保存用户信息
				bqBankCard.setUserId(userId);
				bqBankCard.setUserName(userName);
				bqBankCard.setPhone(phone);
				bqBankCard.setIdNum(idNum);
				bqBankCard.setOrgId(orgId);
				bqBankCard.setMchtId(mchtId);
				bqBankCard.setAcctNo(acct_no);
				bqBankCard.setAcctCvv2(acct_cvv2);
				bqBankCard.setAcctValiddate(acctValiddate);// YYMM
				bqBankCard.setCreateTime(new Date());
				bqBankCard.setStatus("0");// 绑卡状态 0：预签约 1：确认签约

				bqBankCard = topupPayChannelBusiness.createBQBankCard(bqBankCard);
				
				maps.put(CommonConstants.RESULT,
						ip + "/v1.0/paymentgateway/topup/bq/bindcard?orderId=" + orderId + "&orgId=" + orgId
								+ "&mchtId=" + mchtId + "&acct_no=" + acct_no + "&expiredTime=" + acctValiddate
								+ "&securityCode=" + acct_cvv2 + "&name=" + userName + "&idCard=" + idNum + "&phone="
								+ phone + "&ipAddress=" + ip);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);

				return maps;
			} else {
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);

				return maps;
			}
		} else if (bQBankCard != null && !bankCard.equals(acct_no)) {// 卡已经绑定，再次绑定其他卡
			// 获取用户信息
			String userId = bQRegister.getUserId();
			String userName = bQRegister.getUserName();
			String phone = bQRegister.getPhone();
			String orgId = bQRegister.getOrgId();
			String mchtId = bQRegister.getMchtId();

			Map<String, String> req = new TreeMap();
			req.put("orderId", "jh" + date);// 交易流水号 系统唯⼀
			req.put("orgId", orgId);// 机构号
			req.put("mchtId", mchtId);// 商户号
			req.put("transTime", date);// 交易时间
			req.put("transChannel", "04");// 支付类型 固定
			req.put("acct_no", acct_no);// 交易卡号
			req.put("acct_name", acct_name);// 交易人姓名
			req.put("acct_phone", acct_phone);// 交易人电话
			req.put("idNum", idNum);// 交易人身份证号
			req.put("acct_cvv2", acct_cvv2);// 安全码

			String acctValiddate = this.expiredTimeToYYMM(acct_validdate);
			System.out.println(acctValiddate);
			req.put("acct_validdate", acctValiddate);// 有效期 YYMM

			String sign = sign(req);
			req.put("sign", sign);

			LOG.info("签约请求参数:" + req);

			String resp = sendPost("http://47.96.160.164:8080/gatewaysite/c/beSign", JSON.toJSONString(req));
			TreeMap<String, String> treeMap = JSON.parseObject(resp, TreeMap.class);
			boolean verify = verify(treeMap);
			LOG.info("签约请求返回参数:" + resp);
			JSONObject json_obj = JSON.parseObject(resp);
			String respCode = json_obj.getString("respCode");// 返回码
			String respMsg = json_obj.getString("respMsg");// 返回信息
			if ("0000".equals(respCode)) {
				// 再次绑卡
				BQBankCard bQBankCardTwo = new BQBankCard();
				bQBankCardTwo.setUserId(userId);
				bQBankCardTwo.setUserName(userName);
				bQBankCardTwo.setPhone(phone);
				bQBankCardTwo.setIdNum(idNum);
				bQBankCardTwo.setOrgId(orgId);
				bQBankCardTwo.setMchtId(mchtId);
				bQBankCardTwo.setAcctNo(acct_no);
				bQBankCardTwo.setAcctCvv2(acct_cvv2);
				bQBankCardTwo.setAcctValiddate(acctValiddate);
				bQBankCardTwo.setCreateTime(new Date());
				bQBankCardTwo.setStatus("0");// 绑卡状态 0：预签约 1：确认签约

				bQBankCardTwo = topupPayChannelBusiness.createBQBankCard(bQBankCardTwo);

				maps.put(CommonConstants.RESULT,
						ip + "/v1.0/paymentgateway/topup/bq/bindcard?orderId =" + orderId + "&orgId=" + orgId
								+ "&mchtId=" + mchtId + "&acct_no=" + acct_no + "&expiredTime=" + acctValiddate
								+ "&securityCode=" + acct_cvv2 + "&name=" + userName + "&idCard=" + idNum + "&phone="
								+ phone + "&ipAddress=" + ip);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);

				return maps;
			} else {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);

				return maps;
			}
		} else if (bQBankCardWait != null) {// 预签约，待绑卡
			// 获取用户信息
			String orgId = bQRegister.getOrgId();
			String mchtId = bQRegister.getMchtId();

			Map<String, String> req = new TreeMap();
			req.put("orderId", "jh" + date);// 交易流水号 系统唯⼀
			req.put("orgId", orgId);// 机构号
			req.put("mchtId", mchtId);// 商户号
			req.put("transTime", date);// 交易时间
			req.put("transChannel", "04");// 支付类型 固定
			req.put("acct_no", acct_no);// 交易卡号
			req.put("acct_name", acct_name);// 交易人姓名
			req.put("acct_phone", acct_phone);// 交易人电话
			req.put("idNum", idNum);// 交易人身份证号
			req.put("acct_cvv2", acct_cvv2);// 安全码

			String acctValiddate = this.expiredTimeToYYMM(acct_validdate);
			System.out.println(acctValiddate);
			req.put("acct_validdate", acctValiddate);// 有效期 YYMM

			String sign = sign(req);
			req.put("sign", sign);

			LOG.info("签约请求参数:" + req);

			String resp = sendPost("http://47.96.160.164:8080/gatewaysite/c/beSign", JSON.toJSONString(req));
			TreeMap<String, String> treeMap = JSON.parseObject(resp, TreeMap.class);
			boolean verify = verify(treeMap);
			LOG.info("签约请求返回参数:" + resp);
			JSONObject json_obj = JSON.parseObject(resp);
			String respCode = json_obj.getString("respCode");// 返回码
			String respMsg = json_obj.getString("respMsg");// 返回信息
			if ("0000".equals(respCode)) {
				// 已有用户信息，不需要再次保存
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);

				return maps;
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);

				return maps;
			}
		} else if (bQBankCard != null && bankCard.equals(acct_no)) {// 已进件,已绑卡
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "已绑卡");
			return maps;
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "用户未进件");
			return maps;
		}
	}

	/**
	 * 签约
	 * 
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/bq/sign")
	public @ResponseBody Object sign(@RequestParam(value = "bankCard") String acct_no,
			@RequestParam(value = "smsCode") String verify_code, @RequestParam(value = "idCard") String idNum)
					throws Exception {
		LOG.info("开始进入签约接口--------");

		Map<String, String> maps = new HashMap<String, String>();

		SimpleDateFormat sd = new SimpleDateFormat("YYYYMMddHHmmss");
		String date = sd.format(new Date());

		BQBankCard bQBankCard = topupPayChannelBusiness.getBQBankCardByIdNumSure(idNum, acct_no);
		String bankCard = "";
		String orderId = "jh" + date;
		String transChannel = "04";// 固定值
		if (bQBankCard != null) { //预绑卡，待确定
			bankCard = bQBankCard.getAcctNo();
			String orgId = bQBankCard.getOrgId();
			String mchtId = bQBankCard.getMchtId();

			Map<String, String> req = new TreeMap();
			req.put("orderId", orderId);// 交易流水号 系统唯⼀
			req.put("orgId", orgId);// 机构号
			req.put("mchtId", mchtId);// 商户号
			req.put("transTime", date);// 交易时间
			req.put("transChannel", transChannel);// 支付类型 固定
			req.put("acct_no", acct_no);// 交易卡号
			req.put("verify_code", verify_code);// 短信验证码
			String sign = sign(req);
			req.put("sign", sign);

			LOG.info("签约发送参数:" + req);

			String resp = sendPost("http://47.96.160.164:8080/gatewaysite/c/sign", JSON.toJSONString(req));
			TreeMap<String, String> treeMap = JSON.parseObject(resp, TreeMap.class);
			boolean verify = verify(treeMap);

			LOG.info("签约返回参数:" + resp);

			JSONObject json_obj = JSON.parseObject(resp);
			String respCode = json_obj.getString("respCode");// 返回码
			String respMsg = json_obj.getString("respMsg");// 返回信息
			if ("0000".equals(respCode)) {
				// 修改绑卡状态
				bQBankCard.setStatus("1");// 绑卡状态 0：预签约 1：确认签约
				
				topupPayChannelBusiness.createBQBankCard(bQBankCard);

				LOG.info("签约返回参数:" + respMsg);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put("redirect_url", ip + "/v1.0/paymentgateway/topup/bq/bqdhbindcardsuccess");
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);
				return maps;
			} else {

				LOG.info("签约返回参数:" + respMsg);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);
				return maps;
			}
		} else {
			LOG.info("未进行预绑卡");

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "未进行预绑卡");
			return maps;
		}

	}

	/**
	 * 创建无卡订单 协议扣款
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/bq/payment")
	public @ResponseBody Object createNoCardOrder(@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "acct_cvv2") String acct_cvv2,
			@RequestParam(value = "acct_validdate") String acct_validdate) throws Exception {
		LOG.info("开始进入协议扣款接口--------");

		Map<String, String> maps = new HashMap<String, String>();

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		// String realAmount = prp.getRealAmount();
		String amount = prp.getAmount();
		String idNum = prp.getIdCard();
		String acct_no = prp.getBankCard();
		String acct_name = prp.getUserName();
		String acct_phone = prp.getDebitPhone();
		// String acct_cvv2 = prp.getSecurityCode();
		// String acct_validdate = prp.getExpiredTime();

		// 查询用户机构号，商户号
		BQRegister bQRegister = topupPayChannelBusiness.getBQRegisterByIdNum(idNum);
		if (bQRegister != null) {
			String orgId = bQRegister.getOrgId();
			String mchtId = bQRegister.getMchtId();

			// 金额 单位分
			String bigRate = new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0).toString();

			LOG.info("金额，单位分:" + bigRate);
			
			SimpleDateFormat sd = new SimpleDateFormat("YYYYMMddHHmmss");
			String date = sd.format(new Date());

			Map<String, String> req = new TreeMap();
			req.put("orderId", orderCode);// 订单号
			req.put("orgId", orgId);// 机构号
			req.put("mchtId", mchtId);// 商户号

			req.put("transTime", date);// 交易时间
			req.put("transChannel", "04");// 支付类型
			req.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/bq/payment/notifyurl");
			req.put("tranAmt", bigRate);// 交易金额
			req.put("acct_no", acct_no);// 交易卡号
			req.put("acct_name", acct_name);// 交易人姓名
			req.put("acct_phone", acct_phone);// 交易人电话
			req.put("idNum", idNum);// 交易人身份证号
			req.put("acct_cvv2", acct_cvv2);// 安全码

			String acctValiddate = this.expiredTimeToYYMM(acct_validdate);
			System.out.println(acctValiddate);
			req.put("acct_validdate", acctValiddate);// 有效期 YYMM

			String sign = sign(req);
			req.put("sign", sign);

			LOG.info("协议代扣请求参数:" + req);

			String resp = sendPost("http://47.96.160.164:8080/gatewaysite/c/createNoCardOrder", JSON.toJSONString(req));
			TreeMap<String, String> treeMap = JSON.parseObject(resp, TreeMap.class);
			boolean verify = verify(treeMap);

			JSONObject json_obj = JSON.parseObject(resp);
			String respCode = json_obj.getString("respCode");// 返回码
			String respMsg = json_obj.getString("respMsg");// 返回信息
			String resOrderId = json_obj.getString("respMsg");// 返回信息
			if ("0000".equals(respCode)) {
				LOG.info("协议代扣返回信息:" + respMsg);
				
				//跳转到查询
				maps.put(CommonConstants.RESULT,
						ip + "/v1.0/paymentgateway/topup/bq/transQuery?orderCode=" + orderCode);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);
				return maps;
			} else {
				LOG.info("协议代扣返回信息:" + respMsg);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);
				return maps;
			}
		} else {
			LOG.info("用户未进件");

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "用户未进件");
			return maps;
		}

	}

	/**
	 * 信用卡还款
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/bq/repayment")
	public @ResponseBody Object creditCardPayment(@RequestParam(value = "orderCode") String orderCode)
			throws Exception {

		LOG.info("开始进入信用卡还款接口--------");

		Map<String, String> maps = new HashMap<String, String>();

		SimpleDateFormat sd = new SimpleDateFormat("YYYYMMddHHmmss");
		String date = sd.format(new Date());

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String realAmount = prp.getRealAmount();
		String amount = prp.getAmount();
		String idnum = prp.getIdCard();
		String settlePan = prp.getBankCard();// 结算卡号
		String name = prp.getUserName();
		String mobile = prp.getDebitPhone();
		// String acct_cvv2 = prp.getSecurityCode();
		// String acct_validdate = prp.getExpiredTime();
		// String acctValiddate = this.expiredTimeToYYMM(acct_validdate);
		String settleBank = prp.getCreditCardBankName();

		// 查询用户机构号，商户号
		BQRegister bQRegister = topupPayChannelBusiness.getBQRegisterByIdNum(idnum);
		if (bQRegister != null) {
			String orgId = bQRegister.getOrgId();
			String mchtId = bQRegister.getMchtId();

			// 金额 单位分
			String bigAmount = new BigDecimal(realAmount).multiply(new BigDecimal("100")).setScale(0).toString();

			Map<String, String> req = new TreeMap();
			req.put("mchtOrderId", orderCode);// 订单号
			req.put("orgId", orgId);// 机构号
			req.put("mchtId", mchtId);// 商户号
			req.put("transTime", date);// 交易时间
			req.put("tranAmt", bigAmount);// 交易金额
			req.put("currency", "CNY");// 交易币种 固定值
			req.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/bq/repayment/notifyurl");// 异步回调地址
			req.put("idnum", idnum);// 交易人身份证号
			req.put("idtype", "1");// 身份类型 固定值 1 身份证
			req.put("settleBank", settleBank);// 结算卡所属银行
			req.put("settlePan", settlePan);// 结算卡号
			req.put("name", name);
			req.put("mobile", mobile);
			req.put("bankProvince", "上海市");
			req.put("bankCity", "上海市");
			req.put("bankCounty", "宝山区");
			String sign = sign(req);
			req.put("sign", sign);

			LOG.info("信用卡还款请求参数:" + req);

			String resp = sendPost("http://47.96.160.164:8180/withdrawsite/w/pay", JSON.toJSONString(req));
			TreeMap<String, String> treeMap = JSON.parseObject(resp, TreeMap.class);
			boolean verify = verify(treeMap);

			JSONObject json_obj = JSON.parseObject(resp);
			String respCode = json_obj.getString("respCode");// 返回码
			String respMsg = json_obj.getString("respMsg");// 返回信息
			if ("0000".equals(respCode)) {
				LOG.info("信用卡还款返回信息:" + respMsg);

				//跳转到查询
				maps.put(CommonConstants.RESULT,
						ip + "/v1.0/paymentgateway/topup/bq/Query?orderCode=" + orderCode);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);
				return maps;
			} else {
				LOG.info("信用卡还款返回信息:" + respMsg);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);
				return maps;
			}
		} else {
			LOG.info("用户未进件");

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "用户未进件");
			return maps;
		}

	}

	/**
	 * 信用卡还款查询
	 * 
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/bq/Query")
	public @ResponseBody Object Query(@RequestParam(value = "orderCode") String orderCode) throws Exception {

		LOG.info("开始进入代还查询接口--------");

		Map<String, String> maps = new HashMap<String, String>();
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		
		BQRegister bQRegister = topupPayChannelBusiness.getBQRegisterByIdNum(prp.getIdCard());

		String orgId = "";
		String mchtId = "";

		if (bQRegister != null) {
			SimpleDateFormat sd = new SimpleDateFormat("yyyyMMddHHmmss");
			String date = sd.format(bQRegister.getCreateTime());

			orgId = bQRegister.getOrgId();// 机构号
			mchtId = bQRegister.getMchtId();// 商户号

			Map<String, String> req = new TreeMap();
			req.put("orderId", orderCode);// 交易流水号
			req.put("orgId", orgId);// 机构号 注册成功后返回用于交易中填写
			req.put("mchtId", mchtId);// mchtId 商户号 注册成功后返回⽤于交易中填写
			req.put("transTime", date);// 代付请求日期和时间
			req.put("origOrderId", orderCode);// 原交易流水号
			String sign = sign(req);
			req.put("sign", sign);

			LOG.info("代还查询请求参数:" + req);

			String resp = sendPost("http://47.96.160.164:8180/withdrawsite/w/query", JSON.toJSONString(req));
			TreeMap<String, String> treeMap = JSON.parseObject(resp, TreeMap.class);
			boolean verify = verify(treeMap);

			JSONObject json_obj = JSON.parseObject(resp);
			String respCode = json_obj.getString("respCode");// 返回码
			String respMsg = json_obj.getString("respMsg");// 返回信息
			
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			String url = null;
			String result = null;
			RestTemplate rt = new RestTemplate();
			if ("0000".equals(respCode)) {
				LOG.info("代还查询返回信息:" + respMsg);
				
				LOG.info("*********************代还成功***********************");
				
				//20190507 update
				url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
				//url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("status", "1");
				requestEntity.add("order_code", orderCode);
				requestEntity.add("third_code", "");
				try {
					result = rt.postForObject(url, requestEntity, String.class);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("",e);
				}

				LOG.info("订单状态修改成功===================" + orderCode + "====================" + result);

				LOG.info("订单已代还成功!");
				
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);
				return maps;
			} else {
				LOG.info("代还查询返回信息:" + respMsg);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);
				return maps;
			}
		} else {
			LOG.info("未找到订单号");

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "未找到订单号");
			return maps;

		}

	}

	/**
	 * 协议扣款查询
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/bq/transQuery")
	public @ResponseBody Object transQuery(@RequestParam(value = "orderCode") String orderCode) throws Exception {

		LOG.info("开始进入协议扣款查询接口--------");

		Map<String, String> maps = new HashMap<String, String>();

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		
		BQRegister bQRegister = topupPayChannelBusiness.getBQRegisterByIdNum(prp.getIdCard());

		String orgId = "";
		String mchtId = "";

		if (bQRegister != null) {
			SimpleDateFormat sd = new SimpleDateFormat("yyyyMMddHHmmss");
			String date = sd.format(bQRegister.getCreateTime());

			orgId = bQRegister.getOrgId();// 机构号
			mchtId = bQRegister.getMchtId();// 商户号

			Map<String, String> req = new TreeMap();
			req.put("orderId", "cx" + orderCode);//交易流水号
			req.put("orgId", orgId);// 机构号 注册成功后返回用于交易中填写
			req.put("mchtId", mchtId);// mchtId 商户号 注册成功后返回⽤于交易中填写
			req.put("transTime", date);// 代付请求日期和时间
			req.put("origOrderId", orderCode);// 原交易流水号
			String sign = sign(req);
			req.put("sign", sign);
			LOG.info("协议扣款查询请求参数:" + req);
			String resp = sendPost("http://47.96.160.164:8080/gatewaysite/p/transquery", JSON.toJSONString(req));
			TreeMap<String, String> treeMap = JSON.parseObject(resp, TreeMap.class);
			boolean verify = verify(treeMap);

			JSONObject json_obj = JSON.parseObject(resp);
			String respCode = json_obj.getString("respCode");// 返回码
			String respMsg = json_obj.getString("respMsg");// 返回信息
			
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			String url = null;
			String result = null;
			RestTemplate rt = new RestTemplate();
			if ("0000".equals(respCode)) {
				LOG.info("协议扣款查询返回信息:" + respMsg);
				
				LOG.info("*********************交易成功***********************");
				
				url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
				
				//url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("status", "1");
				requestEntity.add("order_code", orderCode);
				requestEntity.add("third_code", "");
				try {
					result = rt.postForObject(url, requestEntity, String.class);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("",e);
				}

				LOG.info("订单状态修改成功===================" + orderCode + "====================" + result);

				LOG.info("订单已交易成功!");

				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);
				return maps;
			} else {
				LOG.info("协议扣款查询返回信息:" + respMsg);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMsg);
				return maps;
			}
		} else {
			LOG.info("未找到订单号");

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "未找到订单号");
			return maps;

		}
	}

	// 跳转到绑卡页面
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/bq/bindcard")
	public String returnBQDHBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String orderId = request.getParameter("orderId");
		String orgId = request.getParameter("orgId");
		String mchtId = request.getParameter("mchtId");
		String transTime = request.getParameter("transTime");
		String transChannel = request.getParameter("transChannel");
		String acct_no = request.getParameter("acct_no");
		String verify_code = request.getParameter("verify_code");
		String sign = request.getParameter("sign");
		String ipAddress = request.getParameter("ipAddress");
		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String name = request.getParameter("name");
		String phone = request.getParameter("phone");
		String idCard = request.getParameter("idCard");

		model.addAttribute("ordercode", orderId);
		model.addAttribute("orgId", orgId);
		model.addAttribute("mchtId", mchtId);
		model.addAttribute("transTime", transTime);
		model.addAttribute("transChannel", transChannel);
		model.addAttribute("bankCard", acct_no);
		model.addAttribute("smsCode", verify_code);
		model.addAttribute("sign", sign);
		model.addAttribute("ipAddress", ipAddress);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("name", name);
		model.addAttribute("phone", phone);
		model.addAttribute("idCard", idCard);

		return "bqdhbindcard";
	}

	// 跳转到绑卡成功页面
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/bq/bqdhbindcardsuccess")
	public String returnBQDHbindcardsuccess()throws IOException {

		return "bqdhbindcardsuccess";
	}

	public String sign(Map<String, String> resp) {
		StringBuffer sb = new StringBuffer();
		for (String key : resp.keySet()) {
			if (resp.get(key) != null && !resp.get(key).equals(""))
				sb.append(key + "=" + resp.get(key) + "&");
		}
		String queryString = sb.substring(0, sb.length() - 1);// 构造待签名字符串
		FileInputStream fis = null;
		String sign = null;
		try {
			if (privateKey == null) {
				fis = new FileInputStream(jkspath);
				privateKey = CertificateUtils.getPrivateKey(fis, null, password);
			}
			Signature signature = Signature.getInstance("SHA1withRSA");
			signature.initSign(privateKey);
			signature.update(queryString.getBytes("UTF-8"));
			sign = Base64.encodeBase64String(signature.sign());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return sign;
	}

	public String sendPost(String url, String json) {
		System.out.println(json);
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(url);
			URLConnection conn = realUrl.openConnection();
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			OutputStream outputStream = conn.getOutputStream();
			outputStream.write(json.getBytes("UTF-8"));
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		System.out.print(result);
		return result;
	}

	/**
	 * 验证
	 */
	public boolean verify(TreeMap<String, String> map) throws Exception {
		InputStream in = new FileInputStream(cerpath);
		PublicKey publicKey = CertificateUtils.getPublicKey(in);
		String signature = map.remove("sign") + "";
		StringBuffer sb = new StringBuffer();
		for (Map.Entry key : map.entrySet()) {
			sb.append(key.getKey() + "=" + key.getValue() + "&");
		}
		String string = sb.substring(0, sb.length() - 1);
		Signature st = Signature.getInstance("SHA1withRSA");
		st.initVerify(publicKey);
		st.update(string.getBytes("UTF-8"));
		boolean result = st.verify(Base64.decodeBase64(signature.getBytes("UTF-8")));
		return result;
	}

}
