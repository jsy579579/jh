package com.jh.paymentgateway.controller;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.HQERegion;
import com.jh.paymentgateway.pojo.HQXBindCard;
import com.jh.paymentgateway.pojo.HQXRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.hqx.HashMapConver;
import com.jh.paymentgateway.util.hqx.SmartRepayChannel;
import net.sf.json.JSONObject;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@EnableAutoConfiguration
public class HQXpageRequest extends BaseChannel {
	private static final Logger LOG = LoggerFactory.getLogger(HQXpageRequest.class);

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;

	public static final String cardType = "02"; // 卡类型

	/**
	 * 还款对接接口
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqx/Dockentrance")
	public @ResponseBody Object Dockentrance(@RequestParam(value = "bankCard") String bankCard,
											 @RequestParam(value = "idCard") String idCard, @RequestParam(value = "phone") String phone,
											 @RequestParam(value = "userName") String userName, @RequestParam(value = "bankName") String bankName,
											 @RequestParam(value = "rate") String rate, @RequestParam(value = "extraFee") String extraFee,
											 @RequestParam(value = "securityCode") String securityCode,
											 @RequestParam(value = "expiredTime") String expiredTime) throws Exception {
		HQXBindCard hqbindCard = topupPayChannelBusiness.getHQXBindCardByBankCard(bankCard);
		HQXRegister hqregister = topupPayChannelBusiness.getHQXRegisterByIdCard(idCard);
		Map<String, Object> maps = new HashMap<>();
		// 进件注册
		if (hqregister == null) {
			maps = (Map<String, Object>) this.hqToRegister(bankCard, idCard, phone, userName, rate, extraFee);
			if (!"000000".equals(maps.get("resp_code"))) {
				return maps;
			}

		}

		// 绑卡
		if (hqbindCard == null || "0".equals(hqbindCard.getStatus())) {
			maps = (Map<String, Object>) this.hqToBindCard(bankCard, idCard, phone, userName, securityCode,
					this.expiredTimeToMMYY(expiredTime));
			return maps;

		}

		return ResultWrap.init(CommonConstants.SUCCESS, "已签约");

	}

	/**
	 * 进件
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqx/hqxRegister")
	public @ResponseBody Object hqToRegister(@RequestParam(value = "bankCard") String bankCard,
											 @RequestParam(value = "idCard") String idCard, @RequestParam(value = "phone") String phone,
											 @RequestParam(value = "userName") String userName, @RequestParam(value = "rate") String rate,
											 @RequestParam(value = "extraFee") String extraFee) {

		LOG.info("开始进件======================");
		String rate1 = new BigDecimal(rate).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString();
		String extraFee1 = new BigDecimal(extraFee).multiply(new BigDecimal("100")).toString();

		Map<String, Object> map = new HashMap<>();

		SmartRepayChannel smart = new SmartRepayChannel();

		Map<String, String> param = HashMapConver.getOrderByMap();

		param.put("methodname", "register"); // 方法
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		String ordersn = format.format(date);
		String dsorderid = "hq" + ordersn;
		param.put("dsorderid", dsorderid); // 商户订单号
		param.put("bankcard", bankCard); // 银行卡号 //信用卡
		param.put("username", userName); // 真实姓名
		param.put("idcard", idCard);// 身份证号
		param.put("mobile", phone); // 预留手机号
		param.put("futureRateValue", rate1); // 扣款费率
		param.put("fixAmount", extraFee1); // 还款手续费
		LOG.info("/hqx/hqxRegister=====================" + param.toString());
		Map<String, String> resultMap = smart.allRequestMethod(param);
		LOG.info("=============环球小额落地：" + resultMap.toString());
		String code = resultMap.get("returncode"); // 返回码
		String errtext = resultMap.get("errtext"); // 详情

		if ("0000".equals(code)||"0052".equals(code)) {
			String subMerchantNo = resultMap.get("subMerchantNo"); // 商户号

			HQXRegister hqRegister = new HQXRegister();
			hqRegister.setUserName(userName);
			hqRegister.setIdCard(idCard);
			hqRegister.setMerchantCode(subMerchantNo);
			hqRegister.setPhone(phone);
			hqRegister.setRate(rate);
			hqRegister.setBankCard(bankCard);
			hqRegister.setExtraFee(extraFee);
			topupPayChannelBusiness.createHQXRegister(hqRegister);

			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, errtext); // 描述
			return map;
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, errtext); // 描述
			return map;
		}
	}

	// 绑卡接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqx/bindCard")
	public Object hqToBindCard(@RequestParam(value = "bankCard") String bankCard,
							   @RequestParam(value = "idCard") String idCard, @RequestParam(value = "phone") String phone,
							   @RequestParam(value = "userName") String userName,
							   @RequestParam(value = "securityCode") String securityCode,
							   @RequestParam(value = "expiredTime") String expiredTime) {
		LOG.info("开始绑卡==================");
		HQXBindCard hqxbind = topupPayChannelBusiness.getHQXBindCardByBankCard(bankCard);
		Map<String, Object> map = new HashMap<>();

		SmartRepayChannel smart = new SmartRepayChannel();
		Map<String, String> param = HashMapConver.getOrderByMap();
		param.put("methodname", "bindCard"); // 方法名
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		String ordersn = format.format(date);
		String dsorderid = "xt" + ordersn;
		param.put("ordersn", ordersn); // 流水号
		param.put("dsorderid", dsorderid); // 商户订单号
		HQXRegister hqRegister = topupPayChannelBusiness.getHQXRegisterByIdCard(idCard);
		String subMerchantNo = hqRegister.getMerchantCode();
		param.put("merchno", subMerchantNo);
		param.put("subMerchantNo", subMerchantNo); // 子商户号 register方法返回
		param.put("bankcard", bankCard); // 银行卡号
		param.put("username", userName);// 真实姓名
		param.put("idcard", idCard);// 身份证号
		param.put("mobile", phone);// 预留手机号
		param.put("cardType", "02"); // 卡类型 借记卡：01 贷记卡：02
		param.put("cvn2", securityCode); // CVN2 安全码 卡类型为贷记卡时必填
		param.put("expireDate", expiredTime); // 信用卡有效期 格式：MMYY卡类型为贷记卡时必填
		param.put("returnUrl", ip +"/v1.0/paymentchannel/topup/wmyk/bindcardsuccess");// 前台跳转地址 TODO
		param.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/hqx/bindcard/call_back");// 异步通知地址
		LOG.info("/hqx/bindCard=====================" + param.toString());
		Map<String, String> resultMap = smart.allRequestMethod(param);
		LOG.info("=============环球小额落地：" + resultMap.toString());
		String code = resultMap.get("returncode");
		String errtext = resultMap.get("errtext");
		if ("0000".equals(code)||"0055".equals(code)) {
			if (resultMap.containsKey("bindId")) {
				String bindId = resultMap.get("bindId");
				LOG.info("存储本地绑卡orderid==================" + dsorderid);
				HQXBindCard hqBindCard = new HQXBindCard();
				hqBindCard.setBankCard(bankCard);
				hqBindCard.setCreateTime(new Date());
				hqBindCard.setIdCard(idCard);
				hqBindCard.setPhone(phone);
				hqBindCard.setUserName(userName);
				hqBindCard.setOrderId(dsorderid);
				hqBindCard.setBindId(bindId);
				hqBindCard.setStatus("1");
				topupPayChannelBusiness.createHQXBindCard(hqBindCard);
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "绑卡成功");
				return map;
			}
			// 跳转绑卡银联页面
			if (resultMap.containsKey("bindUrl")) {
				String bindUrl = resultMap.get("bindUrl");
				LOG.info("存储本地绑卡orderid==================" + dsorderid);
				if (hqxbind == null) {
					HQXBindCard hqBindCard = new HQXBindCard();
					hqBindCard.setBankCard(bankCard);
					hqBindCard.setUserName(userName);
					hqBindCard.setCreateTime(new Date());
					hqBindCard.setIdCard(idCard);
					hqBindCard.setPhone(phone);
					hqBindCard.setOrderId(dsorderid);
					hqBindCard.setStatus("0");
					topupPayChannelBusiness.createHQXBindCard(hqBindCard);
				} else if ("0".equals(hqxbind.getStatus())) {
					hqxbind.setOrderId(dsorderid);
					topupPayChannelBusiness.createHQXBindCard(hqxbind);
				}
				map.put(CommonConstants.RESP_CODE, "999996");
				map.put(CommonConstants.RESP_MESSAGE, "请求绑卡成功,等待回调");
				map.put(CommonConstants.RESULT, bindUrl);
				return map;
			}

		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, errtext);
			return map;
		}
		return map;
	}

	/**
	 * 绑卡回调
	 *
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(method = { RequestMethod.POST,
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/hqx/bindcard/call_back")
	public void bindcardNotify(HttpServletRequest request, HttpServletResponse response) throws IOException {

		LOG.info("绑卡回调回来了！！！！！！！！！！！！！！");
		// TODO JSON 返回的参数
		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}

		String merchno = request.getParameter("merchno");// 商户号
		String paytime = request.getParameter("paytime");// 交易时间
		String status = request.getParameter("status"); // 状态 00:成功，02:失败
		String orderid = request.getParameter("orderid");// 我司订单号
		String dsorderid = request.getParameter("dsorderid"); // 商户订单号
		String subMerchantNo = request.getParameter("subMerchantNo");// 子商户号
		String bindId = request.getParameter("bindId");// 绑卡标识
		String sign = request.getParameter("sign");// 加密校验值
		LOG.info("返回参数=====" + merchno + paytime + status + orderid + dsorderid + subMerchantNo + bindId + sign);

		LOG.info("请求绑卡流水号dsorderid-----------" + dsorderid);
		LOG.info("请求绑卡商户号merchno-----------" + merchno);
		LOG.info("status-----------" + status);

		if ("00".equals(status)) {
			LOG.info("*********************绑卡成功***********************");
			HQXBindCard hqxBindCard = topupPayChannelBusiness.getHQXBindCardByOrderId(dsorderid);
			hqxBindCard.setBindId(bindId);
			hqxBindCard.setStatus("1");
			hqxBindCard.setUpdateTime(new Date());
			topupPayChannelBusiness.createHQXBindCard(hqxBindCard);

			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();
		} else {
			LOG.info("绑卡异常!");
			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();
		}
	}

	/**
	 * 修改商户费率
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqx/updateRate")
	public Object changeRate(@RequestParam(value = "idCard") String idCard,
							 @RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "rate") String rate,
							 @RequestParam(value = "extraFee") String extraFee) {
		LOG.info("开始修改费率=============================");

		Map<String, Object> map = new HashMap<>();

		String bigRate = new BigDecimal(rate).multiply(new BigDecimal("100")).setScale(2).stripTrailingZeros()
				.toPlainString();
		String bigExtraFee = new BigDecimal(extraFee).multiply(new BigDecimal("100")).setScale(0).toString();

		SmartRepayChannel smart = new SmartRepayChannel();

		Map<String, String> request = HashMapConver.getOrderByMap();
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		String ordersn = format.format(date);
		String dsorderid = "xt" + ordersn;
		request.put("methodname", "update"); // 方法名
		request.put("ordersn", ordersn); // 流水号
		request.put("dsorderid", dsorderid); // 商户订单号
		HQXRegister hqRegister = topupPayChannelBusiness.getHQXRegisterByIdCard(idCard);
		String subMerchantNo = hqRegister.getMerchantCode();
		request.put("subMerchantNo", subMerchantNo);// 子商户号 register返回

		request.put("futureRateValue", bigRate); // 扣款费率
		request.put("fixAmount", bigExtraFee); // 还款手续费
		LOG.info("/hqx/updateRate==========" + request.toString());
		Map<String, String> resultMap = smart.allRequestMethod(request);
		LOG.info("=============环球小额落地：" + resultMap.toString());
		String code = resultMap.get("returncode"); // 返回码
		String errtext = resultMap.get("errtext");
		if ("0000".equals(code)) {
			LOG.info("修改费率成功===============");
			hqRegister.setExtraFee(extraFee);
			hqRegister.setRate(rate);
			topupPayChannelBusiness.createHQXRegister(hqRegister);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			return map;
		} else {
			LOG.info("修改费率失败===============");
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, errtext);
			return map;
		}
	}

	/**
	 * 开始消费
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqx/topay")
	public Object topay(@RequestParam(value = "orderCode") String orderCode) throws Exception {
		LOG.info("开始支付================================"); // TODO 消费城市

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();
		String realAmount = prp.getRealAmount();
		String bigRealAmount = new BigDecimal(realAmount).multiply(new BigDecimal("100")).setScale(0).toString();

		String extra = prp.getExtra();// //消费计划|福建省-泉州市
		String cityName = extra.substring(extra.indexOf("-") + 1);
		LOG.info("=======================================消费城市：" + cityName);
		String provinceCode = null;
		try {
			List<HQERegion> hqe = topupPayChannelBusiness.getHQERegionByParentName(cityName);
			provinceCode = hqe.get(0).getRegionCode();
			LOG.info("=======================================HQX消费城市编码：" + provinceCode);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.info("=======================================消费城市：" + cityName + "未匹配");
		}
		Map<String, Object> map = new HashMap<>();
		HQXBindCard hqBindCard = topupPayChannelBusiness.getHQXBindCardByBankCard(bankCard);
		String brandId = hqBindCard.getBindId();
		HQXRegister hqRegister = topupPayChannelBusiness.getHQXRegisterByIdCard(idCard);
		String subMerchantNo = hqRegister.getMerchantCode();

		SmartRepayChannel smart = new SmartRepayChannel();

		Map<String, String> param = HashMapConver.getOrderByMap();
		param.put("methodname", "pay");
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		String ordersn = "xl" + format.format(date);
		param.put("ordersn", ordersn); // 流水号
		param.put("dsorderid", orderCode); // 商户订单号
		param.put("subMerchantNo", subMerchantNo);// 子商户号
		param.put("bindId", brandId); // TODO bindCard 返回 绑卡标识
		param.put("bankcard", bankCard); // 银行卡号
		param.put("amount", bigRealAmount); // 单位（分） 金额
		// param.put("userFee",);扣款手续费 该字段不传则按入驻上传的扣款费率计算手续费hqnew
		if (provinceCode != null) {
			param.put("province", provinceCode); // 消费城市
		}
		// param.put("mcc", "");//行业x
		LOG.info("hqx/topay==========" + param.toString());
		param.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/hqx/pay/call_back"); // 异步通知地址
		Map<String, String> resultMap = smart.allRequestMethod(param);
		LOG.info("=============环球小额落地：" + resultMap.toString());
		String errtext = resultMap.get("errtext");
		String code = resultMap.get("returncode"); // 返回码
		if ("0000".equals(code)) {
			map.put(CommonConstants.RESP_CODE, "999998");
			map.put(CommonConstants.RESP_MESSAGE, "支付成功，等待银行扣款");
			return map;

		} else if ("0003".equals(code)) {
			map.put(CommonConstants.RESP_CODE, "999998");
			map.put(CommonConstants.RESP_MESSAGE, "支付处理中，等待银行扣款");
			return map;
		} else if ("0002".equals(code)) {
			map.put(CommonConstants.RESP_CODE, "999998");
			map.put(CommonConstants.RESP_MESSAGE, "支付状态异常，等待查询");
			return map;
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, errtext);
			return map;
		}
	}

	// 消费交易异步通知
	@RequestMapping(method = { RequestMethod.POST,
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/hqx/pay/call_back")
	public void fayNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {

		LOG.info("消费支付异步回调进来了======");

		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}
		String transtype = request.getParameter("transtype");
		String merchno = request.getParameter("merchno");
		String signType = request.getParameter("signType");
		String status = request.getParameter("status");
		String message = request.getParameter("message");
		String orderid = request.getParameter("orderid");
		String dsorderid = request.getParameter("dsorderid");
		String paytime = request.getParameter("paytime");
		LOG.info("回调参数====" + transtype + merchno + signType + status + message + orderid + dsorderid + paytime);
		LOG.info("第三方流水号======orderid" + orderid);
		LOG.info("订单===========dsorderid" + dsorderid);

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(dsorderid);

		String channelTag = prp.getChannelTag();
		String ipAddress = prp.getIpAddress();

		if ("00".equals(status)) {

			LOG.info("支付成功=============");
			String version = null; // TODO 修改通道标识
			if ("HQG_QUICK".equalsIgnoreCase(channelTag)) {
				version = "18";
			}
			LOG.info("version======" + version);
			RestTemplate restTemplate = new RestTemplate();

			String url = prp.getIpAddress() + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("orderCode", dsorderid);
			requestEntity.add("version", version);

			String result = null;
			JSONObject jsonObject;
			JSONObject resultObj;
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("RESULT================" + result);
				jsonObject = JSONObject.fromObject(result);
				resultObj = jsonObject.getJSONObject("result");
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("", e);
			}

			url = prp.getIpAddress() + ChannelUtils.getCallBackUrl(prp.getIpAddress());
			// url = prp.getIpAddress() +
			// "/v1.0/transactionclear/payment/update";

			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", dsorderid);
			requestEntity.add("third_code", orderid); // 第三方订单号
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("", e);
			}

			LOG.info("订单状态修改成功===================" + dsorderid + "====================" + result);

			LOG.info("订单已支付!");

			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();

		} else if ("01".equals(status)) {
			LOG.info("交易处理中!");

			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();
		} else {

			LOG.info("交易处理失败!");
			addOrderCauseOfFailure(dsorderid, message, ipAddress);
			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();

		}
	}

	/**
	 * 开始代付
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqx/transfer")
	public Object transfer(@RequestParam(value = "orderCode") String orderCode) throws Exception {
		LOG.info("开始进入还款计划========================");

		Map<String, Object> map = new HashMap();

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String idCard = prp.getIdCard();
		String bankCard = prp.getBankCard();
		HQXBindCard hqbindCard = topupPayChannelBusiness.getHQXBindCardByBankCard(bankCard);
		HQXRegister hqRegister = topupPayChannelBusiness.getHQXRegisterByIdCard(idCard);
		String subMerchantNo = hqRegister.getMerchantCode();
		String realAmount = prp.getRealAmount();
		String bigRealAmount = new BigDecimal(realAmount).multiply(new BigDecimal("100")).setScale(0).toString();

		String bindId = hqbindCard.getBindId();

		SmartRepayChannel smart = new SmartRepayChannel();

		Map<String, String> request = HashMapConver.getOrderByMap();
		request.put("methodname", "withDraw");
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		String ordersn = "xl" + format.format(date);
		request.put("ordersn", ordersn); // 流水号
		request.put("dsorderid", orderCode); // 商户订单号
		request.put("subMerchantNo", subMerchantNo);// 子商户号
		request.put("bindId", bindId); // 绑卡标识 bindCard 返回
		request.put("bankcard", bankCard);// 银行卡号 信用卡
		// request.put("userFee","");// 还款手续费 该字段不传则按入驻上传的还款手续费计算
		request.put("amount", bigRealAmount);// 金额 （分）
		request.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/hqx/transfer/call_back"); // 异步通知地址
		LOG.info("/hqx/transfer================" + request.toString());
		Map<String, String> resultMap = smart.allRequestMethod(request);
		LOG.info("=============环球小额落地：" + resultMap.toString());
		String code = resultMap.get("returncode");// 放回码
		String errtext = resultMap.get("errtext");
		if ("0000".equals(code)) {
			map.put(CommonConstants.RESP_CODE, "999998");
			map.put(CommonConstants.RESP_MESSAGE, "支付成功，等待银行扣款");
			return map;

		} else if ("0003".equals(code)) {
			map.put(CommonConstants.RESP_CODE, "999998");
			map.put(CommonConstants.RESP_MESSAGE, "支付处理中，等待银行扣款");
			return map;
		} else if ("0002".equals(code)) {
			map.put(CommonConstants.RESP_CODE, "999998");
			map.put(CommonConstants.RESP_MESSAGE, "支付状态异常，等待查询");
			return map;
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, errtext);
			return map;
		}

	}

	// 代付交易异步通知
	@RequestMapping(method = { RequestMethod.POST,
			RequestMethod.GET }, value = "/v1.0/paymentgateway/topup/hqx/transfer/call_back")
	public void transferNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {

		LOG.info("代付支付异步回调进来了======");

		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}

		String transtype = request.getParameter("transtype");
		String merchno = request.getParameter("merchno");
		String signType = request.getParameter("signType");
		String status = request.getParameter("status");
		String message = request.getParameter("message");
		String orderid = request.getParameter("orderid");
		String dsorderid = request.getParameter("dsorderid");
		String paytime = request.getParameter("paytime");
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(dsorderid);
		String ipAddress = prp.getIpAddress();
		String channelTag = prp.getChannelTag();

		if ("00".equals(status)) {
			LOG.info("*********************支付成功***********************");

			String version = null;
			if ("HQG_QUICK".equalsIgnoreCase(channelTag)) {
				version = "18";
			}
			LOG.info("version======" + version);
			RestTemplate restTemplate = new RestTemplate();

			String url = prp.getIpAddress() + "/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
			MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("orderCode", dsorderid);
			requestEntity.add("version", version);
			String result = null;
			JSONObject jsonObject;
			JSONObject resultObj;
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("RESULT================" + result);
				jsonObject = JSONObject.fromObject(result);
				resultObj = jsonObject.getJSONObject("result");
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("", e);
			}

			url = prp.getIpAddress() + ChannelUtils.getCallBackUrl(prp.getIpAddress());
			// url = prp.getIpAddress() +
			// "/v1.0/transactionclear/payment/update";

			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("status", "1");
			requestEntity.add("order_code", dsorderid);
			requestEntity.add("third_code", orderid); // 第三方订单号
			try {
				result = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("", e);
			}

			LOG.info("订单状态修改成功===================" + dsorderid + "====================" + result);

			LOG.info("订单已支付!");

			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();

		} else if ("01".equals(status)) {
			LOG.info("交易处理中!");

			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();
		} else {

			LOG.info("交易处理失败!");
			addOrderCauseOfFailure(dsorderid, message, ipAddress);
			PrintWriter pw = response.getWriter();
			pw.print("success");
			pw.close();

		}
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqx/balanceQuery1")
	public Object balanceQuery1(@RequestParam(value = "bankCard") String bankCard,
							   @RequestParam(value = "idCard") String idCard) throws Exception {
		Map<String, Object> o = (Map<String, Object>) balanceQuery(bankCard, idCard);
		String resp_message = o.get("resp_message").toString();
		String str = resp_message.split("可用余额")[1].toString();
		com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
		jsonObject.put("balance",str);
		return jsonObject;
	}
	/**
	 * 商户余额查询
	 *
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqx/balanceQuery")
	public Object balanceQuery(@RequestParam(value = "bankCard") String bankCard,
							   @RequestParam(value = "idCard") String idCard) throws Exception {
		LOG.info("开始进入商户余额查询========================");
		Map<String, Object> map = new HashMap<>();
		HQXBindCard hqxBindCard = topupPayChannelBusiness.getHQXBindCardByBankCard(bankCard);
		HQXRegister hqxRegister = topupPayChannelBusiness.getHQXRegisterByIdCard(idCard);
		String bindId = hqxBindCard.getBindId();
		String subMerchantNo = hqxRegister.getMerchantCode();

		SmartRepayChannel smart = new SmartRepayChannel();
		Map<String, String> request = HashMapConver.getOrderByMap();
		request.put("methodname", "queryBalance");
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		String dsorderid = format.format(date);
		request.put("dsorderid", dsorderid); // 商户订单号
		request.put("subMerchantNo", subMerchantNo);// 子商户号
		request.put("bindId", bindId);// 绑卡标识
		Map<String, Object> resultMap = smart.allRequestMethodquery(request);
		String code = resultMap.get("returncode").toString();
		String frozenamount = resultMap.get("frozenamount").toString();// 商户冻结余额
		String currAccountBalance = resultMap.get("currAccountBalance").toString();// 当前可用余额
		String errtext = resultMap.get("errtext").toString();
		if ("0000".equals(code)) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "商户冻结余额" + frozenamount + "当前可用余额" + currAccountBalance);
			return map;

		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, errtext);
			return map;
		}

	}

	/**
	 * 查询接口
	 *
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hqx/payQuery")
	public Object payQuery(@RequestParam(value = "orderCode") String orderCode,
						   @RequestParam(value = "transType") String transtype) throws Exception {
		LOG.info("开始进行交易查询=======================");
		Map<String, Object> map = new HashMap<>();

		SmartRepayChannel smart = new SmartRepayChannel();
		Map<String, String> request = HashMapConver.getOrderByMap();
		Date date = new Date();
		request.put("dsorderid", orderCode); // 产生 商户订单号
		request.put("transtype", transtype); // TODO 133 交易 134 代付

		Map<String, String> resultMap = smart.allRequestMethod(request);
		String status = resultMap.get("status");
		String amount = resultMap.get("amount"); // 金额
		String orderid = resultMap.get("orderid");// 第三方流水号
		String dsorderid = resultMap.get("dsorderid");// 订单号
		String message = resultMap.get("message");// 返回信息\
		LOG.info("amount==" + amount + "orderid=" + orderid + "dsorderid=" + dsorderid);
		LOG.info("=============环球小额落地：" + resultMap.toString());
		if ("00".equals(status)) {
			LOG.info("订单执行成功==================");
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, message);
			return map;
		} else if ("01".equals(status)) {
			LOG.info("订单处理中==================");
			map.put(CommonConstants.RESP_CODE, CommonConstants.WAIT_CHECK);
			map.put(CommonConstants.RESP_MESSAGE, message);
			return map;
		} else if ("02".equals(status)) {
			LOG.info("订单执行失败==================");
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, message);
			return map;
		} else if ("04".equals(status)) {
			LOG.info("订单关闭==================");
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, message);
			return map;
		} else {
			LOG.info("订单号不存在==================");
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, message);
			return map;
		}
	}
}
