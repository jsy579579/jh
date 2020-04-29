package com.jh.paymentgateway.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.BankNumCode;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.YHQuickRegister;

import com.jh.paymentgateway.util.yh.HttpClientNewUtil;
import com.jh.paymentgateway.util.yh.Signature;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class YHpageRequest extends BaseChannel {

	private static final Logger LOG = LoggerFactory.getLogger(YHpageRequest.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	RedisUtil redisUtil;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	private String orgNo = "00000000532917";

	// 秘钥
	private String Key = "Di03xcxNHZks";

	// 进件地址
	private String registerUrl = "http://www.sophiter.com/payment/synmerinfo_faci.do";

	// 支付申请地址
	private String payApplyUrl = "http://www.sophiter.com/payment/UnionQuickDtPayApply.do";

	private String updateRegisterUrl = "http://www.sophiter.com/payment/synmerinfo_Settle_update.do";

	// 进件接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/yh/register")
	public @ResponseBody Object yhRegister(HttpServletRequest request,
			@RequestParam(value = "ordercode") String orderCode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode

	) throws Exception {
		LOG.info("开始进入进件接口======");

		Map<String, Object> map = new HashMap<>();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String phoneD = prp.getDebitPhone();
		String bankName = prp.getDebitBankName();
		String cardNo = prp.getDebitCardNo();
		String idCard = prp.getIdCard();
		String userName = prp.getUserName();
		String rate = prp.getRate();
		String extraFee = prp.getExtraFee();
		String rip = prp.getIpAddress();

		BankNumCode bc = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
		String bankNum = bc.getBankCode();
		String bankBranchcode = bc.getBankBranchcode();
		SortedMap<String, String> dto = new TreeMap<String, String>();

		dto.put("pmerNo", orgNo);// 服务商编号
		dto.put("signType", "MD5");// 加密方式
		dto.put("merVerType", "1");// 商户类型
		dto.put("phoneNo", phoneD);// 手机号码
		dto.put("merName", "上海莘丽");// 商户名称
		dto.put("merShortName", "上海莘丽");// 商户简称
		dto.put("openBranchCode", bankNum);// 开户行编码
		dto.put("openAccountNo", cardNo);// 银行卡号
		dto.put("rcvBranchCode", bankBranchcode);// 开户联行号
		dto.put("rcvBranchName", bankName);// 开户联行名
		dto.put("accName", userName);// 开户姓名
		dto.put("idcard", idCard);// 开户身份证
		// dto.put("isCompay", "D00");// 到账类型
		dto.put("accProvince", "310000");// 开户行省编码
		dto.put("accCity", "310100");// 开户行市编码
		dto.put("accArea", "310113");// 开户行区编码
		dto.put("merProvince", "310000");// 商户所在省
		dto.put("merCity", "310100");// 商户所在市
		dto.put("merArea", "310113");// 商户所在区
		dto.put("merAddr", "上海宝山区长江南路华滋奔腾控股集团1号楼309");// 商户详细地址
		dto.put("feerate", "[{\"FEE00049\":\"" + rate + "|" + extraFee + "\"}]");// 商户费率集合

		LOG.info("进件的请求报文 dto======" + dto);

		String sign = Signature.createSign(dto, Key);

		dto.put("signData", sign);// 加密数据

		LOG.info("sign======" + sign);

		String post = HttpClientNewUtil.post(registerUrl, dto);

		LOG.info("post=====" + post);

		JSONObject fromObject = JSONObject.fromObject(post);

		String retCode = fromObject.getString("retCode");
		String retMsg = fromObject.getString("retMsg");

		if ("1".equals(retCode)) {
			LOG.info("进件成功======");
			String custId = fromObject.getString("cust_id");

			YHQuickRegister yhQuickRegister = new YHQuickRegister();
			yhQuickRegister.setPhone(phoneD);
			yhQuickRegister.setBankCard(cardNo);
			yhQuickRegister.setIdCard(idCard);
			yhQuickRegister.setMerchantCode(custId);
			yhQuickRegister.setRate(rate);
			yhQuickRegister.setExtraFee(extraFee);
			yhQuickRegister.setUserName(userName);

			topupPayChannelBusiness.createYHQuickRegister(yhQuickRegister);

			map = (Map) this.yhpayapply(request, orderCode, expiredTime, securityCode);

			return map;
		} else {
			LOG.info("进件失败======");
			this.addOrderCauseOfFailure(orderCode, retMsg, rip);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, retMsg);

			return map;
		}

	}

	// 快捷支付申请
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/yh/yhpayapply")
	public @ResponseBody Object yhpayapply(HttpServletRequest request,
			@RequestParam(value = "ordercode") String orderCode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode

	) throws Exception {
		LOG.info("开始进入快捷支付申请接口=====================");
		Map<String, Object> map = new HashMap<>();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String amount = prp.getAmount();
		String idCard = prp.getIdCard();
		String bankName = prp.getCreditCardBankName();
		String bankCard = prp.getBankCard();
		String phoneC = prp.getCreditCardPhone();
		String userName = prp.getUserName();

		BankNumCode bc = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
		String bankNum = bc.getBankCode();

		YHQuickRegister yhQuickRegister = topupPayChannelBusiness.getYHQuickRegisterByIdCard(idCard);

		// 交易金额变成以分为单位
		String Amount = new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0).toString();

		LOG.info("转换过的有效期格式======" + expiredTime);

		SortedMap<String, String> dto = new TreeMap<String, String>();

		dto.put("versionId", "1.0");// 服务版本号
		dto.put("orderAmount", Amount);// 订单金额(以分为单位)
		dto.put("orderDate", DateUtil.getyyyyMMddHHmmssDateFormat(new Date()));// 订单日期
		dto.put("currency", "RMB");// 货币类型
		dto.put("transType", "0008");// 交易类别
		dto.put("asynNotifyUrl", ipAddress + "/v1.0/paymentgateway/topup/yh/notify_call");// 异步通知URL
		dto.put("synNotifyUrl", ipAddress + "/v1.0/paymentgateway/topup/yh/return_call");// 同步通知URL
		dto.put("signType", "MD5");// 加密方式
		dto.put("merId", orgNo);// 商户编号
		dto.put("sub_merId", yhQuickRegister.getMerchantCode());// 子商户号
		dto.put("prdOrdNo", orderCode);// 商户订单号
		dto.put("payMode", "00049");// 支付方式
		dto.put("tranChannel", bankNum);// 银行编码
		dto.put("receivableType", "D00");// 到账类型
		dto.put("prdName", "充值缴费");// 商品名称
		dto.put("acctNo", bankCard);// 消费卡号
		dto.put("cvn2", securityCode);
		dto.put("expDate", expiredTime);
		dto.put("pphoneNo", phoneC);
		dto.put("customerName", userName);

		LOG.info("快捷支付申请的报文 dto======" + dto);

		String sign = Signature.createSign(dto, Key);

		dto.put("signData", sign);// 加密数据

		LOG.info("sign======" + sign);

		String pageContent = HttpClientNewUtil.post(payApplyUrl, dto);
		LOG.info("我是返回的pageContent=======" + pageContent);

		map.put("resp_code", "success");
		map.put("channel_type", "jf");
		map.put("pageContent", pageContent);

		return map;

	}

	// 修改结算信息
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/yh/updateregister")
	public @ResponseBody Object yhUpdateRegister(HttpServletRequest request,
			@RequestParam(value = "ordercode") String orderCode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode

	) throws Exception {
		LOG.info("开始进入修改进件接口======");
		Map<String, Object> map = new HashMap<>();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String phoneD = prp.getDebitPhone();
		String cardNo = prp.getDebitCardNo();
		String bankName = prp.getDebitBankName();
		String idCard = prp.getIdCard();
		String rip = prp.getIpAddress();
		// 获取银行编号，获取联行行号
		BankNumCode bc = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
		String bankNum = bc.getBankCode();
		String bankBranchcode = bc.getBankBranchcode();

		YHQuickRegister yhQuickRegister = topupPayChannelBusiness.getYHQuickRegisterByIdCard(idCard);

		SortedMap<String, String> dto = new TreeMap<String, String>();

		dto.put("merId", yhQuickRegister.getMerchantCode());// 商户号
		dto.put("pmerNo", orgNo);// 服务商编号
		dto.put("phoneNo", phoneD);// 银行手机号
		dto.put("openBranchCode", bankNum);// 开户行编号
		dto.put("openAccountNo", cardNo);// 银行卡号
		dto.put("rcvBranchCode", bankBranchcode);// 开户行联行号
		dto.put("rcvBranchName", bankName);// 开户行支行
		dto.put("accProvince", "310000");// 开户行省编码
		dto.put("accCity", "310100");// 开户行市编码
		dto.put("accArea", "310113");// 开户行区编码
		dto.put("isCompay", "0");// 对公对私
		dto.put("signType", "MD5");// 加密方式

		LOG.info("修改结算信息的请求报文 dto======" + dto);

		String sign = Signature.createSign(dto, Key);

		dto.put("signData", sign);// 加密数据

		LOG.info("sign======" + sign);

		String post = HttpClientNewUtil.post(updateRegisterUrl, dto);

		LOG.info("post=====" + post);

		JSONObject fromObject = JSONObject.fromObject(post);

		String retCode = fromObject.getString("retCode");
		String retMsg = fromObject.getString("retMsg");

		if ("1".equals(retCode)) {
			LOG.info("修改进件成功======");

			yhQuickRegister.setPhone(phoneD);
			yhQuickRegister.setBankCard(cardNo);

			topupPayChannelBusiness.createYHQuickRegister(yhQuickRegister);

			map = (Map) this.yhpayapply(request, orderCode, expiredTime, securityCode);

			return map;

		} else {
			LOG.info("修改进件失败======");
			this.addOrderCauseOfFailure(orderCode, retMsg, rip);
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", retMsg);
			return map;
		}

	}

	// 页面中转接口
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/yh/toyhbankinfo")
	public String returnyhpay(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String bankName = request.getParameter("bankName");// 付款卡银行名称
		String bankNo = request.getParameter("bankNo");// 付款卡卡号
		String amount = request.getParameter("amount");
		String ordercode = request.getParameter("ordercode");
		String cardType = request.getParameter("cardType");
		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String ipAddress = request.getParameter("ipAddress");
		String isRegister = request.getParameter("isRegister");

		model.addAttribute("bankName", bankName);
		model.addAttribute("bankNo", bankNo);
		model.addAttribute("amount", amount);
		model.addAttribute("ordercode", ordercode);
		model.addAttribute("cardType", cardType);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("ipAddress", ipAddress);
		model.addAttribute("isRegister", isRegister);

		return "yhpay";

	}

	// 支付申请接口同步通知
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/yh/return_call")
	public String yhPayReturnCallback(HttpServletRequest request, HttpServletResponse response

	) throws Exception {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		LOG.info("同步通知进来了");
		Enumeration e = request.getParameterNames();
		SortedMap<String, String> map = new TreeMap<String, String>();
		while (e.hasMoreElements()) {
			String param = (String) e.nextElement();
			map.put(param, request.getParameter(param));
		}

		LOG.info("map============" + map);

		LOG.info("同步回调成功");
		return "sdjsuccess";
	}

	// 支付申请接口异步回调
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/yh/notify_call")
	public void yhPayNotifyCallback(HttpServletRequest request, HttpServletResponse response

	) throws Exception {

		LOG.info("异步回调进来了");
		Enumeration e = request.getParameterNames();
		SortedMap<String, String> map = new TreeMap<String, String>();
		while (e.hasMoreElements()) {
			String param = (String) e.nextElement();
			map.put(param, request.getParameter(param));
		}

		LOG.info("map============" + map);

		String signData = "";
		if (map.get("signData") != null) {
			signData = map.get("signData").toString();
		}

		String orderStatus = "";
		if (map.get("orderStatus") != null) {
			orderStatus = map.get("orderStatus").toString();
		}

		String dfStatus = "";
		if (map.get("dfStatus") != null) {
			dfStatus = map.get("dfStatus").toString();
		}

		String prdOrdNo = "";
		if (map.get("prdOrdNo") != null) {
			prdOrdNo = map.get("prdOrdNo").toString();
		}

		boolean isSign = false;
		if ("MD5".equalsIgnoreCase(map.get("signType").toString())) {
			// #.md5编码并转成大写 签名：
			map.remove("signData");
			String sign = Signature.createSign(map, Key);

			isSign = signData.equalsIgnoreCase(sign);
		}
		OutputStream outStr = response.getOutputStream();
		// 成功的话返回SUCCESS
		if (isSign)
			outStr.write("SUCCESS".getBytes());
		else
			outStr.write("FAIL".getBytes());

		outStr.flush();
		outStr.close();

		try {
			LOG.info("交易： 订单结果异步通知===================");
			if ("01".equals(orderStatus) && "01".equals(dfStatus)) { // 订单已支付;

				// 1、检查Amount和商户系统的订单金额是否一致
				// 2、订单支付成功的业务逻辑处理请在本处增加（订单通知可能存在多次通知的情况，需要做多次通知的兼容处理）；
				// 3、返回响应内容
				PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(prdOrdNo);
				RestTemplate restTemplate = new RestTemplate();
				MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
				String URL = null;
				String result = null;

				LOG.info("*********************交易成功***********************");
				
				URL = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
				//URL = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";

				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("status", "1");
				requestEntity.add("order_code", prdOrdNo);
				requestEntity.add("third_code", "");
				try {
					result = restTemplate.postForObject(URL, requestEntity, String.class);
				} catch (Exception e1) {
					e1.printStackTrace();
					LOG.error(ExceptionUtil.errInfo(e1));
				}

				LOG.info("订单状态修改成功===================" + prdOrdNo + "====================" + result);

			} else if ("01".equals(orderStatus) && "02".equals(dfStatus)) {

				LOG.info("订单代付处理中!");

			} else {
				// 1、订单支付失败的业务逻辑处理请在本处增加（订单通知可能存在多次通知的情况，需要做多次通知的兼容处理，避免成功后又修改为失败）；
				// 2、返回响应内容

				LOG.info("订单支付失败!");
			}
		} catch (Exception exc) {
			LOG.error(exc.getMessage(), exc);
		}
		LOG.info("-----处理完成----");
	}
}
