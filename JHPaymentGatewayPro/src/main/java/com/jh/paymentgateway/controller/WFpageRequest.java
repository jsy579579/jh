package com.jh.paymentgateway.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.WFBindCard;
import com.jh.paymentgateway.pojo.WFRegister;
import com.jh.paymentgateway.util.HttpUtils;
import com.jh.paymentgateway.util.wf.DESPlus;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.UUIDGenerator;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class WFpageRequest extends BaseChannel {

	private static final Logger LOG = LoggerFactory.getLogger(WFpageRequest.class);

	@Value("${payment.ipAddress}")
	private String ip;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	private String appKey = "dzco3ug7";

	private String secretKey = "3D749070B2754399BBD36EB82AD6FE11";

	// private String WFurl = "http://apinode2.wowpay.cn/pay/receive";

	private String WFurl = "https://gateway.wowpay.cn/pay/receive";

	private static final Charset UTF_8 = StandardCharsets.UTF_8;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private RedisUtil redisUtil;

	// 跟还款对接的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/wf/torepayment")
	public @ResponseBody Object HLJCRegister(HttpServletRequest request,
			@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "phone") String phone, @RequestParam(value = "userName") String userName,
			@RequestParam(value = "bankName") String bankName, @RequestParam(value = "rate") String rate,
			@RequestParam(value = "extraFee") String extraFee) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();

		WFRegister wfRegister = topupPayChannelBusiness.getWFRegisterByIdCard(idCard);

		WFBindCard wfBindCard = topupPayChannelBusiness.getWFBindCardByBankCard(bankCard);

		if (wfRegister == null) {

			map = (Map<String, Object>) wfRegister(bankCard, idCard, phone, userName, bankName, rate, extraFee, "0");
			Object respCode = map.get("resp_code");
			Object respMessage = map.get("resp_message");
			LOG.info("respCode=====" + respCode);

			if ("000000".equals(respCode.toString())) {

				map.put(CommonConstants.RESP_CODE, "999996");
				map.put(CommonConstants.RESULT,
						ip + "/v1.0/paymentgateway/topup/towf/bindcard?bankName=" + URLEncoder.encode(bankName, "UTF-8")
								+ "&idCard=" + idCard + "&bankCard=" + bankCard + "&phone=" + phone + "&expiredTime="
								+ "" + "&securityCode=" + "" + "&ipAddress=" + ip);
				map.put(CommonConstants.RESP_MESSAGE, "用户需要进行绑卡授权操作");

				return map;

			} else {

				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "进件失败");
				return map;

			}

		} else {

			if (wfBindCard == null || !"1".equals(wfBindCard.getStatus())) {

				map.put(CommonConstants.RESP_CODE, "999996");
				map.put(CommonConstants.RESULT,
						ip + "/v1.0/paymentgateway/topup/towf/bindcard?bankName=" + URLEncoder.encode(bankName, "UTF-8")
								+ "&idCard=" + idCard + "&bankCard=" + bankCard + "&phone=" + phone + "&expiredTime="
								+ "" + "&securityCode=" + "" + "&ipAddress=" + ip);
				map.put(CommonConstants.RESP_MESSAGE, "用户需要进行绑卡授权操作");

				return map;

			} else {

				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, "已完成绑卡");
				return map;

			}

		}

	}

	// 注册/修改注册 的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/wf/register")
	public @ResponseBody Object wfRegister(@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "idCard") String idCard, @RequestParam(value = "phone") String phone,
			@RequestParam(value = "userName") String userName, @RequestParam(value = "bankName") String bankName,
			@RequestParam(value = "rate") String rate, @RequestParam(value = "extraFee") String extraFee,
			@RequestParam(value = "isTrue", required = false, defaultValue = "0") String isTrue) throws Exception {

		Map<String, String> maps = new HashMap<String, String>();

		if ("1".equals(isTrue)) {

			WFRegister wfRegister = topupPayChannelBusiness.getWFRegisterByIdCard(idCard);

			String bigRate = new BigDecimal(rate).multiply(new BigDecimal("100")).setScale(2).toString();

			JSONObject jsonObj = new JSONObject();
			JSONObject jsonObj1 = new JSONObject();

			jsonObj1.put("orderNo", UUID.randomUUID().toString().replace("-", ""));// 订单号
			jsonObj1.put("merchantNo", wfRegister.getMerchantNo());
			jsonObj1.put("merchantName", "上海莘丽");
			jsonObj1.put("province", "上海");
			jsonObj1.put("city", "上海");
			jsonObj1.put("address", "上海市宝山区逸仙路2816号");
			jsonObj1.put("legName", userName);
			jsonObj1.put("legPhone", wfRegister.getPhone());
			jsonObj1.put("legEmail", "q355023989@qq.com");
			jsonObj1.put("accountNo", wfRegister.getBankCard());
			jsonObj1.put("idCardNo", idCard);
			jsonObj1.put("merchantFee", bigRate);
			jsonObj1.put("minFee", "0");
			jsonObj1.put("maxFee", "1000");
			jsonObj1.put("txFee", extraFee);

			jsonObj.put("bizName", "merchantReg");
			jsonObj.put("data", jsonObj1);

			DESPlus des = new DESPlus(secretKey);
			// 加密数据
			String encrypt = des.encrypt(jsonObj.toString());

			String URL = WFurl + "?appKey=" + appKey + "&data=" + encrypt;
			JSONObject jsonInfo = null;
			String resultCode = null;
			String merchantNo = null;
			try {
				LOG.info("===================发送修改费率的报文:" + jsonObj);
				String doGet = HttpUtils.doGet(URL);

				String decrypt = des.decrypt(doGet);

				jsonInfo = JSONObject.fromObject(decrypt);

				LOG.info("jsonInfo====" + jsonInfo);

				JSONObject head = jsonInfo.getJSONObject("head");

				LOG.info("head====" + head);

				resultCode = head.getString("result_code");

				LOG.info("resultCode===" + resultCode);

				if ("SUCCESS".equalsIgnoreCase(resultCode)) {

					wfRegister.setRate(rate);
					wfRegister.setExtraFee(extraFee);

					topupPayChannelBusiness.createWFRegister(wfRegister);

					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, "修改成功");
					return maps;
				} else {

					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, "修改失败");
					return maps;

				}

			} catch (Exception e) {

				LOG.error("请求修改费率接口出现异常======",e);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "修改失败");
				return maps;

			}

		} else {

			String bigRate = new BigDecimal(rate).multiply(new BigDecimal("100")).setScale(2).toString();

			JSONObject jsonObj = new JSONObject();
			JSONObject jsonObj1 = new JSONObject();

			jsonObj1.put("orderNo", UUID.randomUUID().toString().replace("-", ""));// 订单号
			jsonObj1.put("merchantName", "上海莘丽");
			jsonObj1.put("province", "上海");
			jsonObj1.put("city", "上海");
			jsonObj1.put("address", "上海市宝山区逸仙路2816号");
			jsonObj1.put("legName", userName);
			jsonObj1.put("legPhone", phone);
			jsonObj1.put("legEmail", "q355023989@qq.com");
			jsonObj1.put("accountNo", bankCard);
			jsonObj1.put("idCardNo", idCard);
			jsonObj1.put("merchantFee", bigRate);
			jsonObj1.put("minFee", "0");
			jsonObj1.put("maxFee", "1000");
			jsonObj1.put("txFee", extraFee);

			jsonObj.put("bizName", "merchantReg");
			jsonObj.put("data", jsonObj1);

			DESPlus des = new DESPlus(secretKey);
			// 加密数据
			String encrypt = des.encrypt(jsonObj.toString());

			String URL = WFurl + "?appKey=" + appKey + "&data=" + encrypt;
			JSONObject jsonInfo = null;
			String resultCode = null;
			String merchantNo = null;
			try {
				LOG.info("===================发送进件的报文:" + jsonObj);
				String doGet = HttpUtils.doGet(URL);

				String decrypt = des.decrypt(doGet);

				jsonInfo = JSONObject.fromObject(decrypt);

				LOG.info("jsonInfo====" + jsonInfo);

				JSONObject head = jsonInfo.getJSONObject("head");

				LOG.info("head====" + head);

				resultCode = head.getString("result_code");

				LOG.info("resultCode===" + resultCode);

				if ("SUCCESS".equalsIgnoreCase(resultCode)) {
					JSONObject content = jsonInfo.getJSONObject("content");

					merchantNo = content.getString("merchant_no");

					WFRegister wfRegister = new WFRegister();
					wfRegister.setPhone(phone);
					wfRegister.setBankCard(bankCard);
					wfRegister.setIdCard(idCard);
					wfRegister.setMerchantNo(merchantNo);
					wfRegister.setRate(rate);
					wfRegister.setExtraFee(extraFee);

					topupPayChannelBusiness.createWFRegister(wfRegister);

					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, "注册成功");

					return maps;

				} else {

					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, "注册失败");

					return maps;

				}

			} catch (Exception e) {

				LOG.error("请求注册接口出现异常======" + e);

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "注册失败");

				return maps;

			}

		}

	}

	// 绑卡申请接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/wf/bindcard")
	public @ResponseBody Object wmykBindCard(@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "idCard") String idCard, @RequestParam(value = "phone") String phone,
			@RequestParam(value = "expiredTime") String expiredTime,
			@RequestParam(value = "securityCode") String securityCode) throws Exception {
		LOG.info("开始进入绑卡申请接口========");
		Map<String, String> maps = new HashMap<String, String>();

		WFRegister wfRegister = topupPayChannelBusiness.getWFRegisterByIdCard(idCard);

		String orderNo = UUID.randomUUID().toString().substring(0, 16).replace("-", "");

		JSONObject jsonObj = new JSONObject();
		JSONObject jsonObj1 = new JSONObject();

		jsonObj1.put("orderNo", orderNo);
		jsonObj1.put("merchantNo", wfRegister.getMerchantNo());
		jsonObj1.put("mobileNo", phone);
		jsonObj1.put("accountNo", bankCard);
		jsonObj1.put("expDate", this.expiredTimeToMMYY(expiredTime));
		jsonObj1.put("cvn2", securityCode);

		jsonObj.put("bizName", "bindCardAuth");
		jsonObj.put("data", jsonObj1);

		DESPlus des = new DESPlus(secretKey);
		// 加密数据
		String encrypt = des.encrypt(jsonObj.toString());

		String URL = WFurl + "?appKey=" + appKey + "&data=" + encrypt;
		JSONObject jsonInfo = null;
		String resultCode = null;
		String resultMsg = null;
		String merchantNo = null;
		try {
			LOG.info("===================发送绑卡申请的报文:" + jsonObj);
			String doGet = HttpUtils.doGet(URL);

			String decrypt = des.decrypt(doGet);

			jsonInfo = JSONObject.fromObject(decrypt);

			LOG.info("jsonInfo====" + jsonInfo);

			JSONObject head = jsonInfo.getJSONObject("head");

			LOG.info("head====" + head);

			resultCode = head.getString("result_code");

			LOG.info("resultCode===" + resultCode);

			if ("SUCCESS".equalsIgnoreCase(resultCode)) {

				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put("orderNo", orderNo);

				return maps;

			} else {

				resultMsg = head.getString("result_msg");

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, resultMsg);

				return maps;

			}

		} catch (Exception e) {

			LOG.error("请求绑卡申请接口出现异常======" + e);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "交易排队中,请稍后重试!");

			return maps;

		}

	}

	// 绑卡确认接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/wf/bindcardconfirm")
	public @ResponseBody Object wfBindCardConfirm(@RequestParam(value = "orderNo") String orderNo,
			@RequestParam(value = "bankCard") String bankCard, @RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "phone") String phone, @RequestParam(value = "smsCode") String smsCode)
			throws Exception {
		LOG.info("开始进入绑卡确认接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		String order = UUID.randomUUID().toString().substring(0, 16).replace("-", "");

		WFRegister wfRegister = topupPayChannelBusiness.getWFRegisterByIdCard(idCard);

		JSONObject jsonObj = new JSONObject();
		JSONObject jsonObj1 = new JSONObject();

		jsonObj1.put("orderNo", order);
		jsonObj1.put("merchantNo", wfRegister.getMerchantNo());
		jsonObj1.put("mobileNo", phone);
		jsonObj1.put("accountNo", bankCard);
		jsonObj1.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/wf/bindcard/notify_call");
		jsonObj1.put("acqCode", "02");
		jsonObj1.put("smsCode", smsCode);

		jsonObj.put("bizName", "bindCard");
		jsonObj.put("data", jsonObj1);

		DESPlus des = new DESPlus(secretKey);
		// 加密数据
		String encrypt = des.encrypt(jsonObj.toString());

		String URL = WFurl + "?appKey=" + appKey + "&data=" + encrypt;
		JSONObject jsonInfo = null;
		String resultCode = null;
		String resultMsg = null;
		String merchantNo = null;
		try {
			LOG.info("===================发送绑卡确认的报文:" + jsonObj);
			String doGet = HttpUtils.doGet(URL);

			String decrypt = des.decrypt(doGet);

			jsonInfo = JSONObject.fromObject(decrypt);

			LOG.info("jsonInfo====" + jsonInfo);

			JSONObject head = jsonInfo.getJSONObject("head");

			LOG.info("head====" + head);

			resultCode = head.getString("result_code");

			LOG.info("resultCode===" + resultCode);

			if ("SUCCESS".equalsIgnoreCase(resultCode)) {

				WFBindCard wfBindCard = topupPayChannelBusiness.getWFBindCardByBankCard(bankCard);

				if (wfBindCard == null) {

					WFBindCard wfb = new WFBindCard();
					wfb.setPhone(phone);
					wfb.setIdCard(idCard);
					wfb.setBankCard(bankCard);
					wfb.setStatus("1");
					wfb.setOrderCode(order);

					topupPayChannelBusiness.createWFBindCard(wfb);

				} else {

					wfBindCard.setOrderCode(order);

					topupPayChannelBusiness.createWFBindCard(wfBindCard);

				}

				maps.put(CommonConstants.RESP_CODE, "000000");
				maps.put("redirect_url", "http://www.shanqi111.cn/v1.0/paymentchannel/topup/wmyk/bindcardsuccess");

				return maps;

			} else {

				resultMsg = head.getString("result_msg");

				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, resultMsg);

				return maps;

			}

		} catch (Exception e) {

			LOG.error("请求绑卡确认接口出现异常======" + e);

			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "交易排队中,请稍后重试!");

			return maps;

		}

	}

	// 快捷支付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/wf/fastpay")
	public @ResponseBody Object wfFastPay(@RequestParam(value = "orderCode") String orderCode) throws Exception {
		LOG.info("开始进入快捷支付接口======");

		RestTemplate rt = new RestTemplate();

		Map<Object, Object> maps = new HashMap<>();

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		WFRegister wfRegister = topupPayChannelBusiness.getWFRegisterByIdCard(prp.getIdCard());

		JSONObject jsonObj = new JSONObject();
		JSONObject jsonObj1 = new JSONObject();

		jsonObj1.put("orderNo", orderCode);
		jsonObj1.put("merchantNo", wfRegister.getMerchantNo());
		jsonObj1.put("mobileNo", prp.getCreditCardPhone());
		jsonObj1.put("accountNo", prp.getBankCard());
		jsonObj1.put("transAmount", prp.getRealAmount());
		jsonObj1.put("goods", "01");
		jsonObj1.put("product", "充值缴费");
		jsonObj1.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/wf/fastpay/notify_call");
		jsonObj1.put("acqCode", "02");

		jsonObj.put("bizName", "bindCardPay");
		jsonObj.put("data", jsonObj1);

		DESPlus des = new DESPlus(secretKey);
		// 加密数据
		String encrypt = des.encrypt(jsonObj.toString());

		String URL = WFurl + "?appKey=" + appKey + "&data=" + encrypt;
		JSONObject jsonInfo = null;
		String resultCode = null;
		String resultMsg = null;
		String orderStatus = null;
		try {
			LOG.info("===================发送快捷支付的报文:" + jsonObj);
			String doGet = HttpUtils.doGet(URL);

			String decrypt = des.decrypt(doGet);

			jsonInfo = JSONObject.fromObject(decrypt);

			LOG.info("jsonInfo====" + jsonInfo);

			JSONObject head = jsonInfo.getJSONObject("head");

			LOG.info("head====" + head);

			resultCode = head.getString("result_code");

			LOG.info("resultCode===" + resultCode);

			if ("SUCCESS".equalsIgnoreCase(resultCode)) {

				JSONObject content = jsonInfo.getJSONObject("content");

				orderStatus = content.getString("orderStatus");

				if ("SUCCESS".equals(orderStatus)) {
					this.updateSuccessPaymentOrder(prp.getIpAddress(), orderCode);
					LOG.info("订单已支付!");
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put("channel_type", "sdj");
					maps.put(CommonConstants.RESP_MESSAGE, "支付成功");
					return maps;

				} else {
					return ResultWrap.init("999998", "支付待查询!");
				}

			} else {
				resultMsg = head.getString("result_msg");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, resultMsg);
				return maps;
			}

		} catch (Exception e) {
			LOG.error("请求支付接口出现异常,调用查询接口======",e);
			return ResultWrap.init("999998", "支付异常,待查询!");
		}

	}

	// 代付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/wf/transfer")
	public @ResponseBody Object wfTransfer(@RequestParam(value = "orderCode") String orderCode) throws Exception {

		LOG.info("开始进入代付接口======");
		RestTemplate rt = new RestTemplate();
		
		Map<String, Object> maps = new HashMap<String, Object>();

		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);

		WFRegister wfRegister = topupPayChannelBusiness.getWFRegisterByIdCard(prp.getIdCard());

		JSONObject jsonObj = new JSONObject();
		JSONObject jsonObj1 = new JSONObject();

		jsonObj1.put("orderNo", orderCode);
		jsonObj1.put("merchantNo", wfRegister.getMerchantNo());
		jsonObj1.put("mobileNo", prp.getCreditCardPhone());
		jsonObj1.put("accountNo", prp.getBankCard());
		jsonObj1.put("transAmount", prp.getRealAmount());
		jsonObj1.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/wf/transfer/notify_call");
		jsonObj1.put("acqCode", "02");

		jsonObj.put("bizName", "bindCardTransfer");
		jsonObj.put("data", jsonObj1);

		DESPlus des = new DESPlus(secretKey);
		// 加密数据
		String encrypt = des.encrypt(jsonObj.toString());

		String URL = WFurl + "?appKey=" + appKey + "&data=" + encrypt;
		JSONObject jsonInfo = null;
		String resultCode = null;
		String resultMsg = null;
		String orderStatus = null;
		try {
			LOG.info("===================发送代付的报文:" + jsonObj);
			String doGet = HttpUtils.doGet(URL);

			String decrypt = des.decrypt(doGet);

			jsonInfo = JSONObject.fromObject(decrypt);

			LOG.info("jsonInfo====" + jsonInfo);

			JSONObject head = jsonInfo.getJSONObject("head");

			LOG.info("head====" + head);

			resultCode = head.getString("result_code");

			LOG.info("resultCode===" + resultCode);

			if ("SUCCESS".equalsIgnoreCase(resultCode)) {
				
				JSONObject content = jsonInfo.getJSONObject("content");

				orderStatus = content.getString("orderStatus");

				if ("SUCCESS".equals(orderStatus)) {
					this.updateSuccessPaymentOrder(prp.getIpAddress(), orderCode);
					LOG.info("订单已代付!");
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, "支付成功啦");
					return maps;

				} else {
					return ResultWrap.init("999998", "支付待查询!");
				}
				
			} else {
				resultMsg = head.getString("result_msg");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, resultMsg);
				return maps;

			}

		} catch (Exception e) {
			LOG.error("请求支付接口出现异常,调用查询接口======", e);
			return ResultWrap.init("999998", "支付异常,待查询!");
		}

	}

	
	// 手动代付到储蓄卡的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/wf/transferbymanual")
	public @ResponseBody Object wfTransferByManual(String phone,
			String brandId,
			String realAmount,
			@RequestParam(value = "ipAddress", required = false, defaultValue = "http://106.15.47.73") String ipAddress)
			throws Exception {

		LOG.info("开始进入手动代付接口======");

		Map<String, Object> maps = new HashMap<String, Object>();

		RestTemplate restTemplate = new RestTemplate();
		String url = ipAddress + "/v1.0/user/query/phone";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("phone", phone);
		requestEntity.add("brandId", brandId);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("RESULT================" + result);
		JSONObject jsonObject;
		JSONObject resultObj;
		long userId;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
			userId = resultObj.getLong("id");
		} catch (Exception e) {
			LOG.error("根据手机号查询用户信息失败=============================", e);

			return ResultWrap.init(CommonConstants.FALIED, "根据手机号查询用户信息失败,请确认手机号是否正确!");
		}

		url = ipAddress + "/v1.0/user/bank/default/userid";
		requestEntity.add("user_id", userId + "");
		result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("接口/v1.0/user/bank/default/userid====RESULT=========" + result);

		jsonObject = JSONObject.fromObject(result);
		if (CommonConstants.SUCCESS.equals(jsonObject.getString(CommonConstants.RESP_CODE))) {
			resultObj = jsonObject.getJSONObject(CommonConstants.RESULT);
			String bankCard = resultObj.getString("cardNo");
			String mobileNo = resultObj.getString("phone");
			String idCard = resultObj.getString("idcard");

			String uuid = UUIDGenerator.getUUID();
			LOG.info("生成的代付订单号=====" + uuid);

			WFRegister wfRegister = topupPayChannelBusiness.getWFRegisterByIdCard(idCard);

			JSONObject jsonObj = new JSONObject();
			JSONObject jsonObj1 = new JSONObject();

			jsonObj1.put("orderNo", uuid);
			jsonObj1.put("merchantNo", wfRegister.getMerchantNo());
			jsonObj1.put("mobileNo", mobileNo);
			jsonObj1.put("accountNo", bankCard);
			jsonObj1.put("transAmount", realAmount);
			jsonObj1.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/wf/transfer/notify_call");
			jsonObj1.put("acqCode", "02");

			jsonObj.put("bizName", "bindCardTransfer");
			jsonObj.put("data", jsonObj1);

			DESPlus des = new DESPlus(secretKey);
			// 加密数据
			String encrypt = des.encrypt(jsonObj.toString());

			String URL = WFurl + "?appKey=" + appKey + "&data=" + encrypt;
			JSONObject jsonInfo = null;
			String resultCode = null;
			String resultMsg = null;
			String orderStatus = null;
			try {
				LOG.info("===================发送代付的报文:" + jsonObj);
				String doGet = HttpUtils.doGet(URL);

				String decrypt = des.decrypt(doGet);

				jsonInfo = JSONObject.fromObject(decrypt);

				LOG.info("jsonInfo====" + jsonInfo);

				JSONObject head = jsonInfo.getJSONObject("head");

				LOG.info("head====" + head);

				resultCode = head.getString("result_code");

				LOG.info("resultCode===" + resultCode);

				if ("SUCCESS".equalsIgnoreCase(resultCode)) {

					JSONObject content = jsonInfo.getJSONObject("content");

					orderStatus = content.getString("orderStatus");

					if ("SUCCESS".equals(orderStatus)) {
						// this.updateSuccessPaymentOrder(prp.getIpAddress(), orderCode);
						LOG.info("订单已代付!");
						maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						maps.put(CommonConstants.RESP_MESSAGE, "支付成功啦");
						return maps;

					} else {
						return ResultWrap.init("999998", "支付待查询!");
					}

				} else {
					resultMsg = head.getString("result_msg");
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, resultMsg);
					return maps;

				}

			} catch (Exception e) {
				LOG.error("请求支付接口出现异常,调用查询接口======", e);
				return ResultWrap.init("999998", "代付异常,待查询!");
			}

		} else {

			return jsonObject;
		}

	}
	
	// 订单查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/wf/ordercodequery")
	public @ResponseBody Object OrderCodeQuery(@RequestParam(value = "orderCode") String ordercode,
			@RequestParam(value = "transType") String transType) throws Exception {
		LOG.info("开始进入订单查询接口======");
		Map<String, String> maps = new HashMap<String, String>();

		JSONObject jsonObj = new JSONObject();
		JSONObject jsonObj1 = new JSONObject();

		jsonObj1.put("orderNo", ordercode);// 订单号
		jsonObj1.put("transType", transType);// 业务名称

		jsonObj.put("bizName", "search");
		jsonObj.put("data", jsonObj1);

		LOG.info("===================发送订单查询的报文:" + jsonObj);

		DESPlus des = new DESPlus(secretKey);
		String encrypt = des.encrypt(jsonObj.toString());

		String URL = WFurl + "?appKey=" + appKey + "&data=" + encrypt;
		// 请求通道支付接口
		String doGet = HttpUtils.doGet(URL);

		// 请求返回的数据解密
		String decrypt = des.decrypt(doGet);

		JSONObject jsonInfo = JSONObject.fromObject(decrypt);

		LOG.info("订单查询  jsonInfo====" + jsonInfo);

		JSONObject head = jsonInfo.getJSONObject("head");

		LOG.info("订单查询   head====" + head);

		String resultCode = head.getString("result_code");

		String resultMsg = head.containsKey("result_msg") ? head.getString("result_msg") : "";

		LOG.info("订单查询  resultCode===" + resultCode);
		LOG.info("订单查询  resultMsg===" + resultMsg);

		if ("SUCCESS".equalsIgnoreCase(resultCode)) {
			LOG.info("订单查询结果为已成功======");

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "成功");
		} else if ("SEND".equalsIgnoreCase(resultCode)) {
			
			if("请核对订单号".equals(resultMsg)) {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, resultMsg);
				
			}else {
				LOG.info("订单查询结果为待处理======");

				maps.put(CommonConstants.RESP_CODE, "999998");
				maps.put(CommonConstants.RESP_MESSAGE, resultMsg);
			}
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, resultMsg);
		}

		return maps;
	}

	
	//账户余额查询
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/wf/balancequery")
	public @ResponseBody Object balanceQuery(@RequestParam(value = "idCard") String idCard
			) throws Exception {
		LOG.info("开始进入账户余额查询接口======");
		Map<String, String> maps = new HashMap<String, String>();

		WFRegister wfRegister = topupPayChannelBusiness.getWFRegisterByIdCard(idCard);
		
		JSONObject jsonObj = new JSONObject();
		JSONObject jsonObj1 = new JSONObject();

		jsonObj1.put("merchantNo", wfRegister.getMerchantNo());
		jsonObj1.put("acqCode", "02");

		jsonObj.put("bizName", "bindCardBalance");
		jsonObj.put("data", jsonObj1);

		LOG.info("===================发送账户余额查询的报文:" + jsonObj);

		DESPlus des = new DESPlus(secretKey);
		String encrypt = des.encrypt(jsonObj.toString());

		String URL = WFurl + "?appKey=" + appKey + "&data=" + encrypt;
		// 请求通道支付接口
		String doGet = HttpUtils.doGet(URL);

		// 请求返回的数据解密
		String decrypt = des.decrypt(doGet);

		JSONObject jsonInfo = JSONObject.fromObject(decrypt);

		LOG.info("账户余额查询  jsonInfo====" + jsonInfo);

		JSONObject head = jsonInfo.getJSONObject("head");

		String resultCode = head.getString("result_code");

		String resultMsg = head.containsKey("result_msg") ? head.getString("result_msg") : "";

		if ("SUCCESS".equalsIgnoreCase(resultCode)) {
			JSONObject content = jsonInfo.getJSONObject("content");
			
			String balance = content.getString("balance");

			maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			maps.put(CommonConstants.RESP_MESSAGE, "余额为: "+balance);
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, resultMsg);
		}

		return maps;
	}
	
	
	// 绑卡异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/wf/bindcard/notify_call")
	public void wfBindCardNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
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

	// 支付异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/wf/fastpay/notify_call")
	public void wfFastPayNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("支付异步通知进来了=======");

		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}

	}

	// 代付异步通知接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/wf/transfer/notify_call")
	public void wfTransferNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("代付异步通知进来了=======");

		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}

	}

	// 跳转到绑卡页面
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/topup/towf/bindcard")
	public String returnWMYKNewBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String bankName = request.getParameter("bankName");
		String bankCard = request.getParameter("bankCard");
		String idCard = request.getParameter("idCard");
		String phone = request.getParameter("phone");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("bankName", bankName);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("idCard", idCard);
		model.addAttribute("phone", phone);
		model.addAttribute("ipAddress", ipAddress);

		return "wfbindcard";
	}

}
