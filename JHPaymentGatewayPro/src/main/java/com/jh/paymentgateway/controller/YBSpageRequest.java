package com.jh.paymentgateway.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
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
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.RegisterPartsBuilderss;
import com.jh.paymentgateway.pojo.YBSRegister;
import com.jh.paymentgateway.util.MD5Util;
import com.jh.paymentgateway.util.PhotoCompressUtil;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class YBSpageRequest extends BaseChannel {
	private static final Logger LOG = LoggerFactory.getLogger(YBSpageRequest.class);

	private String mch_id = "1514312323";
	private String app_id = "to3940105693071a7d63e3e44b0a1639c0";
	private String key = "dcddf093acb42eed08d077a3d433b03d";
	private String headUrl = "https://pay.longmaoguanjia.com/api/pay/yeepay/into_net/";
	private String characterEncoding = "UTF-8"; // 指定字符集UTF-8

	@Autowired
	RedisUtil redisUtil;

	@Value("${payment.ipAddress}")
	private String ip;

	@Autowired
	TopupPayChannelBusiness topupPayChannelBusiness;

	@SuppressWarnings("null")
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ybs/register")
	public @ResponseBody Object ybsRegister(@RequestParam("orderCode") String orderCode,
			@RequestParam(value = "positiveURL") String positiveURL, @RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "positiveFile") MultipartFile positiveFile) throws IOException {
		LOG.info("======ybsRegister=======龙猫orderCode===" + orderCode);
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		Map<String, Object> maps = new HashMap<>();
		String userName = prp.getUserName();// 用户名
		String cardNo = prp.getDebitCardNo();// 到账卡
		String phone = prp.getDebitPhone();// 到账卡预留手机号
		String rip = prp.getIpAddress();
		String rate = prp.getRate();
		String amount = prp.getAmount();
		String bankCard = prp.getBankCard();
		String extraFee = prp.getExtraFee();
		String bankName = prp.getDebitBankName();// 到账卡银行名称
		if (bankName.contains("工商")) {
			bankName = "工商银行";
		} else if (bankName.contains("农业")) {
			bankName = "农业银行";
		} else if (bankName.contains("招商")) {
			bankName = "招商银行";
		} else if (bankName.contains("建设")) {
			bankName = "建设银行";
		} else if (bankName.contains("交通")) {
			bankName = "交通银行";
		} else if (bankName.contains("中信")) {
			bankName = "中信银行";
		} else if (bankName.contains("光大")) {
			bankName = "光大银行";
		} else if (bankName.contains("北京银行")) {
			bankName = "北京银行";
		} else if (bankName.contains("深圳发展")) {
			bankName = "深圳发展银行";
		} else if (bankName.contains("中国银行")) {
			bankName = "中国银行";
		} else if (bankName.contains("兴业")) {
			bankName = "兴业银行";
		} else if (bankName.contains("民生")) {
			bankName = "民生银行";
		}
		YBSRegister ybsRegister = topupPayChannelBusiness.getYBSRegisterByidCard(idCard);
		String requestNo = "xinli" + System.currentTimeMillis();
		LOG.info("===register请求订单号：" + requestNo);
		if (ybsRegister == null) {
			File file = null;
			Part[] parts = null;
			try {
				String zipPath = "/" + idCard + ".jpg";
				// 图片转类型，压缩
				InputStream ins = positiveFile.getInputStream();
				file = new File(zipPath);
				inputStreamToFile(ins, file);
				PhotoCompressUtil.compressPhoto(new FileInputStream(file), file, 0.1f);

				SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
				parameters.put("mch_id", mch_id);
				parameters.put("app_id", app_id);
				parameters.put("mch_no", requestNo);
				parameters.put("realname", userName);
				parameters.put("bank_card_no", cardNo);
				parameters.put("bank_name", bankName);
				parameters.put("bank_card_mobile", phone);
				parameters.put("id_card", idCard);
				parameters.put("area_code", "2900");
				String mySign = createSign(characterEncoding, parameters, key);
				LOG.info("====register签名报文：" + mySign.toString());

				parameters.put("idcard_pic_front", file);// 身份证正面照(不超过512k)
				parameters.put("idcard_pic_back", file);// 身份证反面照(不超过512k)
				parameters.put("bankcard_pic_front", file);// 银行卡正面照(不超过512k)
				parameters.put("idcard_pic_middle", file);// 身份证与银行卡手持正面照(不超过512k)

				parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(requestNo)
						.setRealname(userName).setCardNo(cardNo).setBankName(bankName).setPhone(phone).setidCard(idCard)
						.setAreaCode("2900").setIdCardFront(file).setIdCardBack(file).setCardNoFront(file)
						.setCardNoAndIdCard(file).setSign(mySign).generateParams();

			} catch (Exception e) {
				LOG.info("==================实名注册文件异常:" + orderCode);
				LOG.info("==================实名注册文件异常:" + e);
			} finally {
				file.delete();
			}
			PostMethod postMethod = new PostMethod(headUrl + "sub_merchant_register");
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
					YBSRegister ybs = new YBSRegister();
					ybs.setBankCard(cardNo);
					ybs.setMainCustomerNum(mch_id);
					ybs.setCustomerNum(merchantNo);
					ybs.setIdCard(idCard);
					ybs.setUserName(userName);
					ybs.setPhone(phone);
					ybs.setStatus("0");
					topupPayChannelBusiness.createYBSRegister(ybs);
					maps = (Map<String, Object>) YBSsetRate(orderCode, rate, extraFee, rip, idCard);
					if ("000000".equals(maps.get("resp_code"))) {
						maps = (Map<String, Object>) this.queryMerchant(idCard, phone);
						if ("000000".equals(maps.get("resp_code"))) {
							maps = (Map<String, Object>) pay(orderCode, bankCard, rip, idCard, amount, phone);
							return maps;
						} else {
							this.addOrderCauseOfFailure(orderCode, (String) maps.get("resp_message"), rip);
							return maps;
						}
					} else {
						return maps;
					}
				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, respMessage);
					this.addOrderCauseOfFailure(orderCode, "进件:" + respMessage + "[" + requestNo + "]", rip);
					return maps;
				}
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "请求注册系统异常");
				this.addOrderCauseOfFailure(orderCode, "进件:请求注册系统异常" + "[" + requestNo + "]", rip);
				return maps;
			}

		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "用户已注册,请误重复提交");
			this.addOrderCauseOfFailure(orderCode, "用户已注册,请误重复提交" + "[" + requestNo + "]", rip);
			return maps;
		}

	}

	/**
	 * 设置费率
	 * 
	 * @param orderCode
	 * @param rate
	 * @param extraFee
	 * @param rip
	 * @param idCard
	 * @return
	 * @throws IOException
	 */
	public Object YBSsetRate(String orderCode, String rate, String extraFee, String rip, String idCard)
			throws IOException {
		LOG.info("======YBSsetRate=======龙猫orderCode:" + orderCode);
		Map<String, Object> maps = new HashMap<>();
		YBSRegister ybs = topupPayChannelBusiness.getYBSRegisterByidCard(idCard);
		// 转换分制
		String exFee = new BigDecimal(extraFee).multiply(new BigDecimal(100)).toString();
		String requestNo = "xinli" + System.currentTimeMillis();
		LOG.info("===setRate请求订单号：" + requestNo);
		SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
		parameters.put("mch_id", mch_id);
		parameters.put("app_id", app_id);
		parameters.put("mch_no", requestNo);
		parameters.put("rate", rate);
		parameters.put("sub_mch_no", ybs.getCustomerNum());
		parameters.put("single_price", exFee);
		String mySign = createSign(characterEncoding, parameters, key);
		LOG.info("====setRate签名报文：" + mySign.toString());
		Part[] parts = null;
		parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(requestNo).setSign(mySign)
				.setSubM(ybs.getCustomerNum()).setRate(rate).setSinglePrice(exFee).generateParams();

		PostMethod postMethod = new PostMethod(headUrl + "sub_merchant_fee");
		HttpClient client = new HttpClient();
		postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

		int status = client.executeMethod(postMethod);
		LOG.info("==========status==========" + status);

		if (status == HttpStatus.SC_OK) {
			String backinfo = postMethod.getResponseBodyAsString();
			LOG.info("==========setRate返回响应==========" + backinfo);
			JSONObject jsonstr = JSONObject.fromObject(backinfo);
			String respCode = jsonstr.getString("code");
			String respMessage = jsonstr.getString("message");
			if (respCode.equals("0")) {
				ybs.setExtraFee(extraFee);
				ybs.setRate(rate);
				ybs.setChangeTime(new Date());
				topupPayChannelBusiness.createYBSRegister(ybs);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				this.addOrderCauseOfFailure(orderCode, "设置费率:" + respMessage + "[" + requestNo + "]", rip);
				return maps;
			}
		}
		return maps;

	}

	/**
	 * 修改银行卡信息
	 * 
	 * @param orderCode
	 * @param bankName
	 * @param cardNo
	 * @param rip
	 * @param idCard
	 * @return
	 * @throws IOException
	 */
	public Object setMerchant(String orderCode, String bankName, String cardNo, String rip, String idCard)
			throws IOException {
		LOG.info("======setMerchant=======龙猫orderCode:" + orderCode);
		YBSRegister ybs = topupPayChannelBusiness.getYBSRegisterByidCard(idCard);
		Map<String, Object> maps = new HashMap<>();
		String requestNo = "xinli" + System.currentTimeMillis();
		LOG.info("===setMerchant请求订单号：" + requestNo);
		SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
		parameters.put("mch_id", mch_id);
		parameters.put("app_id", app_id);
		parameters.put("mch_no", requestNo);
		parameters.put("bank_card_no", cardNo);
		parameters.put("sub_mch_no", ybs.getCustomerNum());
		parameters.put("bank_name", bankName);
		String mySign = createSign(characterEncoding, parameters, key);
		LOG.info("====setMerchant签名报文：" + mySign.toString());
		Part[] parts = null;
		parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(requestNo).setSign(mySign)
				.setSubM(ybs.getCustomerNum()).setCardNo(cardNo).setBankName(bankName).generateParams();
		PostMethod postMethod = new PostMethod(headUrl + "sub_merchant_bank_card_modify");
		HttpClient client = new HttpClient();
		postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

		int status = client.executeMethod(postMethod);
		LOG.info("==========status==========" + status);

		if (status == HttpStatus.SC_OK) {
			String backinfo = postMethod.getResponseBodyAsString();
			LOG.info("==========setMerchant返回响应==========" + backinfo);
			JSONObject jsonstr = JSONObject.fromObject(backinfo);
			String respCode = jsonstr.getString("code");
			String respMessage = jsonstr.getString("message");
			if (respCode.equals("0")) {
				ybs.setBankCard(cardNo);
				ybs.setChangeTime(new Date());
				topupPayChannelBusiness.createYBSRegister(ybs);
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				return maps;
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				this.addOrderCauseOfFailure(orderCode, "修改银行卡信息:" + respMessage + "[" + requestNo + "]", rip);
				return maps;
			}
		}
		return maps;
	}

	/**
	 * 下单
	 * 
	 * @param orderCode
	 * @param bankCard
	 * @param rip
	 * @param idCard
	 * @param amount
	 * @param phone
	 * @return
	 * @throws IOException
	 */
	public Object pay(String orderCode, String bankCard, String rip, String idCard, String amount, String phone)
			throws IOException {
		LOG.info("======pay=======龙猫orderCode:" + orderCode);
		YBSRegister ybs = topupPayChannelBusiness.getYBSRegisterByidCard(idCard);
		Map<String, Object> maps = new HashMap<>();
		SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
		String mcc;
		if (Integer.valueOf(amount) <= 500) {
			mcc = "5311";
		} else if (Integer.valueOf(amount) <= 1000) {
			mcc = "4733";
		} else {
			mcc = "4511";
		}
		String Amount = new BigDecimal(amount).multiply(new BigDecimal(100)).toString();
		parameters.put("mch_id", mch_id);
		parameters.put("app_id", app_id);
		parameters.put("mch_no", orderCode);
		parameters.put("sub_mch_no", ybs.getCustomerNum());
		parameters.put("amount", Amount);
		parameters.put("pay_bank_no", bankCard);
		parameters.put("mcc", mcc);
		parameters.put("withdraw_card_no", ybs.getBankCard());
		parameters.put("sub_mch_rate", ybs.getRate());
		String extraFee = new BigDecimal(ybs.getExtraFee()).multiply(new BigDecimal(100)).toString();
		parameters.put("single_price", extraFee);
		String returnUrl = ip + "/v1.0/paymentgateway/topup/ybs/notify_call";
		parameters.put("notify_url", returnUrl);
		String mySign = createSign(characterEncoding, parameters, key);
		LOG.info("====pay签名报文：" + mySign.toString());
		Part[] parts = null;
		parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(orderCode)
				.setSubM(ybs.getCustomerNum()).setAmount(Amount).setPaybankCard(bankCard).setMcc(mcc)
				.setWithdrawCardno(ybs.getBankCard()).setSubMchRate(ybs.getRate()).setSinglePrice(extraFee)
				.setSign(mySign).setNotifyUrl(returnUrl).generateParams();

		PostMethod postMethod = new PostMethod("https://pay.longmaoguanjia.com/api/pay/yeepay/trade/quick_pay");
		HttpClient client = new HttpClient();
		postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

		int status = client.executeMethod(postMethod);
		LOG.info("==========status==========" + status);

		if (status == HttpStatus.SC_OK) {
			String backinfo = postMethod.getResponseBodyAsString();
			LOG.info("==========pay返回响应==========" + backinfo);
			JSONObject jsonstr = JSONObject.fromObject(backinfo);
			String respCode = jsonstr.getString("code");
			String respMessage = jsonstr.getString("message");
			if (respCode.equals("0")) {
				String data = jsonstr.getString("data");
				JSONObject datastr = JSONObject.fromObject(data);
				String payURL = datastr.getString("url");
				maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				maps.put(CommonConstants.RESULT, payURL);
				return maps;
			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				this.addOrderCauseOfFailure(orderCode, "请求交易:" + respMessage + "[" + orderCode + "]", rip);
				return maps;
			}
		}
		return maps;

	}

	/**
	 * 回调
	 * 
	 * @param req
	 * @param res
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ybs/notify_call")
	public @ResponseBody void notifycall(HttpServletRequest req, HttpServletResponse res) throws IOException {
		LOG.info("进入易宝回调接口================" + req.getParameterMap().toString());
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
		PrintWriter pw = res.getWriter();
		pw.print("SUCCESS");
		pw.close();
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

	/**
	 * 跳转
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/quick/jump/top/xinli")
	public @ResponseBody Object file() throws IOException {
		System.out.println("===============" + ip.toString());
		Map<String, Object> maps = new HashMap<String, Object>();
		maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		maps.put(CommonConstants.RESP_MESSAGE, "成功");
		maps.put(CommonConstants.RESULT, ip + "/v1.0/paymentgateway/quick/jump/fill-b?ip=" + ip + "&key="
				+ "dcddf093acb42eed08d077a3d433b03d" + "&mainCustomerNumber=1514312323");
		return maps;
	}

	/**
	 * 跳转到
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/jump/fill")
	public String toPay(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String ip = request.getParameter("ip");
		String key = request.getParameter("key");
		String orderCode = request.getParameter("orderCode");
		String mainCustomerNumber = request.getParameter("mainCustomerNumber");
		System.out.println("==============开启实名认证上传=========" + orderCode);

		model.addAttribute("ips", ip);
		model.addAttribute("key", key);
		model.addAttribute("orderCode", orderCode);
		model.addAttribute("mainCustomerNumber", mainCustomerNumber);
		System.out.println("==============" + ip.toString());
		return "ybupload";
	}

	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/jump/fill-b")
	public String setrealname(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String ip = request.getParameter("ip");
		String key = request.getParameter("key");
		String orderCode = request.getParameter("orderCode");
		String mainCustomerNumber = request.getParameter("mainCustomerNumber");

		System.out.println("==============开启补充认证上传=====" + orderCode);
		model.addAttribute("ips", ip);
		model.addAttribute("key", key);
		model.addAttribute("orderCode", orderCode);
		model.addAttribute("mainCustomerNumber", mainCustomerNumber);
		System.out.println("==============" + ip.toString());
		return "ybuploadB";
	}

	/**
	 * 查询子商户资质
	 * 
	 * @param idCard
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/ybs/queryMerchant")
	public @ResponseBody Object queryMerchant(String idCard, String phone) throws IOException {
		YBSRegister ybs = topupPayChannelBusiness.getYBSRegisterByidCard(idCard);
		Map<String, Object> maps = new HashMap<>();
		SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
		String requestNo = "xinli" + System.currentTimeMillis();
		LOG.info("===查询商户资质请求订单号：" + requestNo);
		parameters.put("mch_id", mch_id);
		parameters.put("app_id", app_id);
		parameters.put("mch_no", requestNo);
		parameters.put("bank_card_mobile", phone);
		String mySign = createSign(characterEncoding, parameters, key);
		LOG.info("====queryMerchant签名报文：" + mySign.toString());
		Part[] parts = null;
		parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setMch_no(requestNo).setSign(mySign)
				.setPhone(phone).generateParams();
		PostMethod postMethod = new PostMethod(
				"https://pay.longmaoguanjia.com/api/pay/yeepay/into_net/sub_merchant_info");
		HttpClient client = new HttpClient();
		postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

		int status = client.executeMethod(postMethod);
		LOG.info("==========status==========" + status);
		if (status == HttpStatus.SC_OK) {
			String backinfo = postMethod.getResponseBodyAsString();
			LOG.info("==========queryMerchant返回响应====" + idCard + "==========" + backinfo);
			JSONObject jsonstr = JSONObject.fromObject(backinfo);
			String respCode = jsonstr.getString("code");
			String respMessage = jsonstr.getString("message");
			if (respCode.equals("10302")) {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "资质查询：[" + requestNo + "]" + respMessage);
				return maps;
			}
			if (respCode.equals("0")) {
				String data = jsonstr.getString("data");
				JSONObject jsonstr1 = JSONObject.fromObject(data);
				String info = jsonstr1.getString("info");
				JSONObject infoJson = JSONObject.fromObject(info);
				try {
					String auditStatus = infoJson.getString("auditStatus");// 商户审核状态
					String idcardStatus = infoJson.getString("idcardStatus");// ocr审核身份证状态
					String idcardMsg = infoJson.getString("idcardMsg");
					if (auditStatus.equals("2") && idcardStatus.equals("1")) {
						ybs.setStatus("1");
						ybs.setChangeTime(new Date());
						topupPayChannelBusiness.createYBSRegister(ybs);
						maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
						maps.put(CommonConstants.RESP_MESSAGE, "资质查询：[" + requestNo + "]" + idcardMsg);
						return maps;
					} else {
						maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						maps.put(CommonConstants.RESP_MESSAGE, "资质查询：[" + requestNo + "]" + idcardMsg);
						return maps;
					}
				} catch (Exception e) {
					ybs.setStatus("3");
					ybs.setChangeTime(new Date());
					topupPayChannelBusiness.createYBSRegister(ybs);
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, "资质查询：[" + requestNo + "]" + "亲,商户资质审核中,请稍等2分钟");
					return maps;
				}

			} else {
				maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				maps.put(CommonConstants.RESP_MESSAGE, "资质查询：[" + requestNo + "]" + respMessage);
				return maps;

			}
		} else {
			maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			maps.put(CommonConstants.RESP_MESSAGE, "资质查询：[" + requestNo + "]" + "系统异常");
			return maps;
		}
	}

	/**
	 * 补充资质
	 * 
	 * @param request
	 * @param positiveURL
	 * @param positiveFile
	 * @param key
	 * @param mainCustomerNumber
	 * @param idCard
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/ybs/update")
	public @ResponseBody Object findByBankName(HttpServletRequest request,
			@RequestParam(value = "positiveURL") String positiveURL, // 正面
			@RequestParam(value = "positiveFile") MultipartFile positiveFile, @RequestParam(value = "key") String key,
			@RequestParam(value = "mainCustomerNumber") String mainCustomerNumber,
			@RequestParam(value = "idCard") String idCard, @RequestParam(value = "orderCode") String orderCode)
					throws Exception {
		LOG.info("===========================龙猫补充资质订单号：" + orderCode);
		YBSRegister ybs = topupPayChannelBusiness.getYBSRegisterByidCard(idCard);
		Map<String, Object> maps = new HashMap<>();
		String requestNo = "xinli" + System.currentTimeMillis();
		LOG.info("===补充资质请求号：" + requestNo);
		File file = null;
		try {
			String zipPath = "/" + idCard + ".jpg";
			// 图片转类型，压缩
			InputStream ins = positiveFile.getInputStream();
			file = new File(zipPath);
			inputStreamToFile(ins, file);
			PhotoCompressUtil.compressPhoto(new FileInputStream(file), file, 0.2f);

			SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
			parameters.put("mch_id", mch_id);
			parameters.put("app_id", app_id);
			parameters.put("mch_no", requestNo);
			parameters.put("sub_mch_no", ybs.getCustomerNum());
			String mySign = createSign(characterEncoding, parameters, key);
			LOG.info("====update签名报文：" + mySign.toString());
			Part[] parts = null;
			parts = new RegisterPartsBuilderss().setMch_id(mch_id).setApp_id(app_id).setSubM(ybs.getCustomerNum())
					.setMch_no(requestNo).setSign(mySign).setIdCardFront(file).setIdCardBack(file).setCardNoFront(file)
					.setCardNoAndIdCard(file).generateParams();
			PostMethod postMethod = new PostMethod(
					"https://pay.longmaoguanjia.com/api/pay/yeepay/into_net/sub_merchant_pic_update");
			HttpClient client = new HttpClient();
			postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

			int status = client.executeMethod(postMethod);
			LOG.info("==========status==========" + status);

			if (status == HttpStatus.SC_OK) {
				String backinfo = postMethod.getResponseBodyAsString();
				LOG.info("==========update返回响应==========" + backinfo);
				JSONObject jsonstr = JSONObject.fromObject(backinfo);
				String respCode = jsonstr.getString("code");
				String respMessage = jsonstr.getString("message");
				if (respCode.equals("0")) {
					ybs.setStatus("3");
					ybs.setChangeTime(new Date());
					topupPayChannelBusiness.createYBSRegister(ybs);
					maps.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				} else {
					maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					maps.put(CommonConstants.RESP_MESSAGE, respMessage);
				}
			}
		} catch (Exception e) {
			LOG.info("==================补充实名审核异常:" + idCard);
			LOG.info("==================补充实名审核异常:" + e);
		} finally {
			file.delete();
		}
		return maps;

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