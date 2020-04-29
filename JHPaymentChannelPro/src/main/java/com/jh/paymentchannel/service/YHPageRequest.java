package com.jh.paymentchannel.service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
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

import com.jh.paymentchannel.business.RegisterAuthBusiness;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.RegisterAuth;
import com.jh.paymentchannel.pojo.RepaymentInfoMation;
import com.jh.paymentchannel.pojo.YHQuickRegister;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.yh.HttpClientNewUtil;
import com.jh.paymentchannel.util.yh.Signature;
import com.jh.paymentchannel.pojo.BankNumCode;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class YHPageRequest {

	private static final Logger LOG = LoggerFactory.getLogger(YHPageRequest.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private Util util;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	// 商户编号
	//@Value("${yh.org_no}")
	private String orgNo = "00000000523685";

	// 秘钥
	@Value("${yh.key}")
	private String Key;

	//进件地址
	@Value("${yh.registerUrl}")
	private String registerUrl;
	
	// 支付申请地址
	@Value("${yh.payApplyUrl}")
	private String payApplyUrl;

	private String updateRegisterUrl = "http://www.sophiter.com/payment/synmerinfo_Settle_update.do";
	
	private String updateRateUrl = "http://www.sophiter.com/payment/synmerinfo_rate_update.do";
	
	private static final Charset UTF_8 = StandardCharsets.UTF_8;

	@Autowired
	private RegisterAuthBusiness registerAuthBusiness;
	
	//进件接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/yhpay/register")
	public @ResponseBody Object yhRegister(HttpServletRequest request,
			@RequestParam(value = "ordercode") String orderCode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode
			
			) throws Exception {
		LOG.info("开始进入进件接口======");
		
		Map<String, String> map = new TreeMap<String, String>();
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", orderCode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("接口/v1.0/transactionclear/payment/query/ordercode--RESULT================" + result);
		JSONObject jsonObject;
		JSONObject resultObj;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("查询订单信息出错");
			map.put("resp_code", "failed");
			map.put("resp_message", "没有该订单信息");
			return map;
		}
		// 银行卡(支付卡)
		String bankCard = resultObj.getString("bankcard");
		String amount = resultObj.getString("amount");
		String rate = resultObj.getString("rate");
		String extraFee = resultObj.getString("extraFee");
		String userId = resultObj.getString("userid");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/userid";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("user_id", userId);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		LOG.info("RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("查询默认结算卡出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询默认结算卡有误");
			return map;
		}
		
		String cardNo = resultObj.getString("cardNo");// 默认提现卡卡号
		String bankName = resultObj.getString("bankName");//提现卡银行名称
		String userName = resultObj.getString("userName");// 用户姓名
		String idcard = resultObj.getString("idcard");// 身份证号
		String phone = resultObj.getString("phone");//手机号

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/cardno";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("cardno", bankCard);
		requestEntity.add("type", "0");
		result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("接口/v1.0/user/bank/default/cardno--RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("查询信用卡出错");
			map.put("resp_code", "failed");
			map.put("resp_message", "查询信用卡有误");
			return map;
		}
		
		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/query/randomuserid";
		requestEntity = new LinkedMultiValueMap<String, String>();

		String shopUserId;
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			jsonObject = JSONObject.fromObject(result);
			shopUserId = jsonObject.getString("result");
		} catch (Exception e1) {
			LOG.error("查询用户ID出错！！！！");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "没有查询到用户ID");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", orderCode);
			requestEntity.add("remark", "没有查询到用户ID");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}

		LOG.info("随机获取的userId" + shopUserId);

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/shops/query/uid";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("userid", shopUserId);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		LOG.info("RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			LOG.error("查询商铺信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询商铺信息有误");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", orderCode);
			requestEntity.add("remark", "查询商铺信息有误");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}

		String shopName = resultObj.getString("name");// 商户全称
		String address = resultObj.getString("address");
		
		String shopsaddress = resultObj.getString("shopsaddress");
		
		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/shops/find/province";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("userId", shopUserId);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		LOG.info("RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
		} catch (Exception e) {
			LOG.error("查询商铺省市出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询该商铺省市信息有误,请稍后重试!");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", orderCode);
			requestEntity.add("remark", "查询商铺省市出错");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}
		String province = jsonObject.getString("province");
		String city = jsonObject.getString("city");
		
		
		String bankNum;
		String bankBranchcode;
		try {
			BankNumCode bankNumCode = topupPayChannelBusiness.getBankNumCodeByBankName(Util.queryBankNameByBranchName(bankName));
			
			bankNum = bankNumCode.getBankNum();
			bankBranchcode = bankNumCode.getBankBranchcode();
		} catch (Exception e) {
			e.printStackTrace();
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "该通道暂不支持该结算银行,请及时更换结算银行卡!");
			return map;
		}
		
		String extraFees = extraFee.substring(0, extraFee.indexOf("."));
		
		SortedMap<String, String> dto = new TreeMap<String, String>();
		
		dto.put("pmerNo", orgNo);// 服务商编号
		dto.put("signType", "MD5");// 加密方式
		dto.put("merVerType", "1");// 商户类型
		dto.put("phoneNo", phone);// 手机号码
		dto.put("merName", shopName);// 商户名称
		dto.put("merShortName", shopName);// 商户简称
		dto.put("openBranchCode", bankNum);// 开户行编码
		dto.put("openAccountNo", cardNo);// 银行卡号
		dto.put("rcvBranchCode", bankBranchcode);// 开户联行号
		dto.put("rcvBranchName", bankName);// 开户联行名
		dto.put("accName", userName);// 开户姓名
		dto.put("idcard", idcard);// 开户身份证
		//dto.put("isCompay", "D00");// 到账类型
		dto.put("accProvince", "310000");// 开户行省编码
		dto.put("accCity", "310100");// 开户行市编码
		dto.put("accArea", "310113");// 开户行区编码
		dto.put("merProvince", "310000");// 商户所在省
		dto.put("merCity", "310100");// 商户所在市
		dto.put("merArea", "310113");// 商户所在区
		dto.put("merAddr", shopsaddress);// 商户详细地址
		dto.put("feerate", "[{\"FEE00049\":\""+rate+"|"+extraFees+"\"}]");// 商户费率集合
		
		LOG.info("进件的请求报文 dto======" + dto);
		
		String sign = Signature.createSign(dto, Key);
		
		dto.put("signData", sign);// 加密数据
		
		LOG.info("sign======" + sign);
		
		String post = HttpClientNewUtil.post(registerUrl, dto);
		
		LOG.info("post====="+post);
		
		JSONObject fromObject = JSONObject.fromObject(post);
		
		String retCode = fromObject.getString("retCode");
		String retMsg = fromObject.getString("retMsg");
		
		if("1".equals(retCode)) {
			LOG.info("进件成功======");
			String custId = fromObject.getString("cust_id");
			
			YHQuickRegister yhQuickRegister = new YHQuickRegister();
			yhQuickRegister.setPhone(phone);
			yhQuickRegister.setBankCard(cardNo);
			yhQuickRegister.setIdCard(idcard);
			yhQuickRegister.setMerchantCode(custId);
			yhQuickRegister.setRate(rate);
			yhQuickRegister.setExtraFee(extraFee);
			
			topupPayChannelBusiness.createYHQuickRegister(yhQuickRegister);
			
			map = (Map) this.yhpayapply(request, orderCode, expiredTime, securityCode);
			
			return map;
		}else {
			LOG.info("进件失败======");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", retMsg);
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", orderCode);
			requestEntity.add("remark", retMsg);
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}
		
	}
	
	
	
	//快捷支付申请
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/yhpayapply")
	public @ResponseBody Object yhpayapply(HttpServletRequest request,
			@RequestParam(value = "ordercode") String orderCode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode
			
			) throws Exception {
		LOG.info("开始进入快捷支付申请接口=====================");

		Map<String, String> map = new TreeMap<String, String>();
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", orderCode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("接口/v1.0/transactionclear/payment/query/ordercode--RESULT================" + result);
		JSONObject jsonObject;
		JSONObject resultObj;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("查询订单信息出错");
			map.put("resp_code", "failed");
			map.put("resp_message", "没有该订单信息");
			return map;
		}
		// 银行卡(支付卡)
		String bankCard = resultObj.getString("bankcard");
		String amount = resultObj.getString("amount");
		String rate = resultObj.getString("rate");
		String extraFee = resultObj.getString("extraFee");
		String userId = resultObj.getString("userid");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/userid";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("user_id", userId);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		LOG.info("RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("查询默认结算卡出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询默认结算卡有误");
			return map;
		}
		
		String cardNo = resultObj.getString("cardNo");// 默认提现卡卡号
		//String bankName = resultObj.getString("bankName");//提现卡银行名称
		String userName = resultObj.getString("userName");// 用户姓名
		String idcard = resultObj.getString("idcard");// 身份证号
		String phone = resultObj.getString("phone");//手机号

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/cardno";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("cardno", bankCard);
		requestEntity.add("type", "0");
		result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("接口/v1.0/user/bank/default/cardno--RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("查询信用卡出错");
			map.put("resp_code", "failed");
			map.put("resp_message", "查询信用卡有误");
			return map;
		}

		String payBankName = resultObj.getString("bankName"); // 支付卡银行名称
		
		LOG.info("没有转换过的银行名称========payBankName==="+payBankName+"&&&&&&&bankName===");
		
		//将银行名称转换为通道支持的银行名称
		//String bankName1 = Util.strSub(bankName);
		String payBankName1 = Util.strSub(payBankName);
		
		//LOG.info("已经转换过的银行名称========payBankName1==="+payBankName1+"&&&&&&&bankName1==="+bankName1);
		
		//到账卡银行编码
		/*String bankCode;
		try {
			bankCode = topupPayChannelBusiness.getBankCodeByBankName(bankName1);
		} catch (Exception e) {
			LOG.error("查询提现卡银行编码出错啦====="+e);
			map.put("resp_code", "failed");
			map.put("channel_type", "yh");
			map.put("resp_message", "查询提现卡银行编码出错啦");
			return map;
		}*/
		
		//支付卡银行编码
		BankNumCode bankNumCode;
		String bankNum;
		try {
			bankNumCode = topupPayChannelBusiness.getBankNumCodeByBankName(Util.queryBankNameByBranchName(payBankName));
			bankNum = bankNumCode.getBankNum();
		} catch (Exception e) {
			LOG.error("查询支付卡银行编码出错啦====="+e);
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "暂不支持该银行卡,请更换银行卡!");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", orderCode);
			requestEntity.add("remark", "查询支付卡银行编码出错");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}
		
		YHQuickRegister yhQuickRegister = topupPayChannelBusiness.getYHQuickRegisterByIdCard(idcard);
		
		// 交易金额变成以分为单位
		String Amount = new BigDecimal(amount).multiply(new BigDecimal("100")).setScale(0).toString();
		
		try {
			String before = expiredTime.substring(0, 2);
			String after = expiredTime.substring(2, 4);

			BigDecimal big = new BigDecimal(before);
			BigDecimal times = new BigDecimal("12");

			int compareTo = big.compareTo(times);
			// 如果前两位大于12，,代表是年/月的格式
			if (compareTo == 1) {
				expiredTime = after + before;
			} else {
				expiredTime = before + after;
			}
		} catch (Exception e) {
			LOG.error("转换有效期格式有误======="+e);
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "您的信用卡有效期信息不正确,请仔细核对并重新输入!");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", orderCode);
			requestEntity.add("remark", "您的信用卡有效期信息不正确,请仔细核对并重新输入!");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}

		LOG.info("转换过的有效期格式======" + expiredTime);
		
		SortedMap<String, String> dto = new TreeMap<String, String>();
		
		dto.put("versionId", "1.0");// 服务版本号
		dto.put("orderAmount", Amount);// 订单金额(以分为单位)
		dto.put("orderDate", DateUtil.getyyyyMMddHHmmssDateFormat(new Date()));// 订单日期
		dto.put("currency", "RMB");// 货币类型
		dto.put("transType", "0008");// 交易类别
		dto.put("asynNotifyUrl", ipAddress+"/v1.0/paymentchannel/topup/yh/notify_call");// 异步通知URL
		dto.put("synNotifyUrl", ipAddress+"/v1.0/paymentchannel/topup/yh/return_call");// 同步通知URL
		dto.put("signType", "MD5");// 加密方式
		dto.put("merId", orgNo);// 商户编号
		dto.put("sub_merId", yhQuickRegister.getMerchantCode());//子商户号
		dto.put("prdOrdNo", orderCode);// 商户订单号
		dto.put("payMode", "00049");// 支付方式
		dto.put("tranChannel", bankNum);// 银行编码
		dto.put("receivableType", "D00");// 到账类型
		dto.put("prdName", "充值缴费");// 商品名称
		dto.put("acctNo", bankCard);// 消费卡号
		dto.put("cvn2", securityCode);
		dto.put("expDate", expiredTime);
		dto.put("pphoneNo", phone);
		dto.put("customerName", userName);
		
		
		LOG.info("快捷支付申请的报文 dto======" + dto);
		
		String sign = Signature.createSign(dto, Key);
		
		dto.put("signData", sign);// 加密数据
		
		LOG.info("sign======" + sign);
		
		String pageContent = HttpClientNewUtil.post(payApplyUrl, dto);
		LOG.info("我是返回的pageContent======="+pageContent);
		
		map.put("resp_code", "success");
		map.put("channel_type", "jf");
		map.put("pageContent", pageContent);
		
		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("transactionclear", "error url request!");
		url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("ordercode", orderCode);
		requestEntity.add("remark", "开始跳转页面");
		result = restTemplate.postForObject(url, requestEntity, String.class);
		
		return map;
		
	}
	
	
	
	//修改结算信息
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/yhpay/updateregister")
	public @ResponseBody Object yhUpdateRegister(HttpServletRequest request,
			@RequestParam(value = "ordercode") String orderCode,
			@RequestParam(value = "expiredTime", required = false) String expiredTime,
			@RequestParam(value = "securityCode", required = false) String securityCode
			
			) throws Exception {
		LOG.info("开始进入修改进件接口======");
		
		Map<String, String> map = new TreeMap<String, String>();
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", orderCode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("接口/v1.0/transactionclear/payment/query/ordercode--RESULT================" + result);
		JSONObject jsonObject;
		JSONObject resultObj;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("查询订单信息出错");
			map.put("resp_code", "failed");
			map.put("resp_message", "没有该订单信息");
			return map;
		}
		// 银行卡(支付卡)
		String bankCard = resultObj.getString("bankcard");
		String amount = resultObj.getString("amount");
		String rate = resultObj.getString("rate");
		String extraFee = resultObj.getString("extraFee");
		String userId = resultObj.getString("userid");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/userid";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("user_id", userId);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		LOG.info("RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("查询默认结算卡出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询默认结算卡有误");
			return map;
		}
		
		String cardNo = resultObj.getString("cardNo");// 默认提现卡卡号
		String bankName = resultObj.getString("bankName");//提现卡银行名称
		String userName = resultObj.getString("userName");// 用户姓名
		String idcard = resultObj.getString("idcard");// 身份证号
		String phone = resultObj.getString("phone");//手机号

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/cardno";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("cardno", bankCard);
		requestEntity.add("type", "0");
		result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("接口/v1.0/user/bank/default/cardno--RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			LOG.error("查询信用卡出错");
			map.put("resp_code", "failed");
			map.put("resp_message", "查询信用卡有误");
			return map;
		}
		
		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/query/randomuserid";
		requestEntity = new LinkedMultiValueMap<String, String>();

		String shopUserId;
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			jsonObject = JSONObject.fromObject(result);
			shopUserId = jsonObject.getString("result");
		} catch (Exception e1) {
			LOG.error("查询用户ID出错！！！！");
			map.put("resp_code", "failed");
			map.put("channel_type", "sdj");
			map.put("resp_message", "没有查询到用户ID");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", orderCode);
			requestEntity.add("remark", "没有查询到用户ID");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}

		LOG.info("随机获取的userId" + shopUserId);

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/shops/query/uid";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("userid", shopUserId);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		LOG.info("RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			LOG.error("查询商铺信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jfcoin");
			map.put("resp_message", "查询商铺信息有误");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", orderCode);
			requestEntity.add("remark", "查询商铺信息有误");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}

		String shopName = resultObj.getString("name");// 商户全称
		String address = resultObj.getString("address");
		
		String shopsaddress = resultObj.getString("shopsaddress");
		
		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/shops/find/province";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("userId", shopUserId);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		LOG.info("RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
		} catch (Exception e) {
			LOG.error("查询商铺省市出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询该商铺省市信息有误,请稍后重试!");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", orderCode);
			requestEntity.add("remark", "查询商铺省市出错");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}
		String province = jsonObject.getString("province");
		String city = jsonObject.getString("city");
		
		
		String bankNum;
		String bankBranchcode;
		try {
			BankNumCode bankNumCode = topupPayChannelBusiness.getBankNumCodeByBankName(Util.queryBankNameByBranchName(bankName));
			
			bankNum = bankNumCode.getBankNum();
			bankBranchcode = bankNumCode.getBankBranchcode();
		} catch (Exception e) {
			e.printStackTrace();
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "该通道暂不支持该结算银行,请及时更换结算银行卡!");
			return map;
		}
		
		YHQuickRegister yhQuickRegister = topupPayChannelBusiness.getYHQuickRegisterByIdCard(idcard);
		
		String extraFees = extraFee.substring(0, extraFee.indexOf("."));
		
		SortedMap<String, String> dto = new TreeMap<String, String>();
		
		dto.put("merId", yhQuickRegister.getMerchantCode());// 商户号
		dto.put("pmerNo", orgNo);// 服务商编号
		dto.put("phoneNo", phone);// 银行手机号	
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
		
		LOG.info("post====="+post);
		
		JSONObject fromObject = JSONObject.fromObject(post);
		
		String retCode = fromObject.getString("retCode");
		String retMsg = fromObject.getString("retMsg");
		
		if("1".equals(retCode)) {
			LOG.info("修改进件成功======");
			
			yhQuickRegister.setPhone(phone);
			yhQuickRegister.setBankCard(cardNo);
			
			topupPayChannelBusiness.createYHQuickRegister(yhQuickRegister);
			
			map = (Map) this.yhpayapply(request, orderCode, expiredTime, securityCode);
			
			return map;
			
		}else {
			LOG.info("修改进件失败======");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", retMsg);
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", orderCode);
			requestEntity.add("remark", retMsg);
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		} 
		
	}
	
	

	//页面中转接口
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/toyhbankinfo")
	public String returnyhpay(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String bankName = request.getParameter("bankName");//付款卡银行名称
		String bankNo = request.getParameter("bankNo");//付款卡卡号
		String amount = request.getParameter("amount");
		String ordercode = request.getParameter("ordercode");
		String cardType = request.getParameter("cardType");
		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String ipAddress = request.getParameter("ipAddress");
		String isRegister = request.getParameter("isRegister");

		
		model.addAttribute("bankName", bankName);
		model.addAttribute("bankNo",bankNo);
		model.addAttribute("amount", amount);
		model.addAttribute("ordercode", ordercode);
		model.addAttribute("cardType", cardType);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("ipAddress", ipAddress);
		model.addAttribute("isRegister", isRegister);
		
		return "yhpay";
		
	}

	
	//请求支付申请接口返回的页面中转接口
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/toyhback")
	public void returnyhback(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String pageContent = request.getParameter("pageContent");//结算卡银行名称
		
		PrintWriter writer = response.getWriter();
		writer.print(pageContent);
		
		writer.close();
		
	}
	
	
	//支付申请接口同步通知
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/yh/return_call")
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
		
		LOG.info("map============"+map);
		
		/*String signData = "";
		if(map.get("signData") != null) {
			signData = map.get("signData").toString();
		}
		
		String orderStatus = "";
		if(map.get("orderStatus") != null) {
			orderStatus = map.get("orderStatus").toString();
		}
		
		boolean isSign = false;
		if("MD5".equalsIgnoreCase(map.get("signType").toString())){
			//#.md5编码并转成大写 签名：
			map.remove("signData");
			String sign = Signature.createSign(map,Key);
			
			isSign = signData.equalsIgnoreCase(sign);
        }
		
		if ("00".equals(orderStatus)){
			LOG.info("同步回调成功");
			return "sdjsuccess";
		}else {
			LOG.info("同步回调失败");
			return "sdjerror";
		}*/
		
		LOG.info("同步回调成功");
		return "sdjsuccess";
	}
	
	
	// 支付申请接口异步回调
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/yh/notify_call")
	public void yhPayNotifyCallback(HttpServletRequest request, HttpServletResponse response

	) throws Exception {

		LOG.info("异步回调进来了");
		Enumeration e = request.getParameterNames();
		SortedMap<String, String> map = new TreeMap<String, String>();
		while (e.hasMoreElements()) {
			String param = (String) e.nextElement();
			map.put(param, request.getParameter(param));
		}
		
		LOG.info("map============"+map);
		
		String signData = "";
		if(map.get("signData") != null) {
			signData = map.get("signData").toString();
		}
		
		String orderStatus = "";
		if(map.get("orderStatus") != null) {
			orderStatus = map.get("orderStatus").toString();
		}
		
		String dfStatus = "";
		if(map.get("dfStatus") != null) {
			dfStatus = map.get("dfStatus").toString();
		} 
		
		String prdOrdNo = "";
		if(map.get("prdOrdNo") != null) {
			prdOrdNo = map.get("prdOrdNo").toString();
		} 
		
		boolean isSign = false;
		if("MD5".equalsIgnoreCase(map.get("signType").toString())){
			//#.md5编码并转成大写 签名：
			map.remove("signData");
			String sign = Signature.createSign(map,Key);
			
			isSign = signData.equalsIgnoreCase(sign);
        }
		/*String sendMsg = "<root><merchantRes merId=\"" + merId + "\" prdOrdNo=\"" + prdOrdNo + "\" orderAmount=\"" + orderAmount 
				+ "\" orderDate=\"" + orderDate + "\" verifyResult=\"" + verifyResult + "\"/></root>";*/
		OutputStream outStr = response.getOutputStream();
		//成功的话返回SUCCESS
		if(isSign)
			outStr.write("SUCCESS".getBytes());
		else
			outStr.write("FAIL".getBytes());

		outStr.flush();
		outStr.close();
		
		try {
			//LOG.setLOGFlag(true);
			LOG.info("交易： 订单结果异步通知===================");
			if ("01".equals(orderStatus)&&"01".equals(dfStatus)) { // 订单已支付;

				// 1、检查Amount和商户系统的订单金额是否一致
				// 2、订单支付成功的业务逻辑处理请在本处增加（订单通知可能存在多次通知的情况，需要做多次通知的兼容处理）；
				// 3、返回响应内容

				synchronized (this) {
					// **更新订单状态*//*
					// **调用下单，需要得到用户的订单信息*//*
					RestTemplate restTemplate = new RestTemplate();

					URI uri = util.getServiceUrl("transactionclear", "error url request!");
					String url = uri.toString() + "/v1.0/transactionclear/payment/update";

					// **根据的用户手机号码查询用户的基本信息*//*
					MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
					requestEntity.add("status", "1");
					requestEntity.add("order_code", prdOrdNo);
					String result = restTemplate.postForObject(url, requestEntity, String.class);

					LOG.info("订单状态修改成功===================");

					// **判断是否有外放的通道的处理， 如果有那么继续回调外放哦*//*
					/*uri = util.getServiceUrl("transactionclear", "error url request!");
					url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";

					requestEntity = new LinkedMultiValueMap<String, String>();
					//requestEntity.add("order_code", orderId);
					result = restTemplate.postForObject(url, requestEntity, String.class);

					JSONObject jsonObject = JSONObject.fromObject(result);
					JSONObject resultObj = jsonObject.getJSONObject("result");
					String outMerOrdercode = resultObj.getString("outMerOrdercode");
					String orderdesc = resultObj.getString("desc");
					String phone = resultObj.getString("phone");
					String tranamount = resultObj.getString("amount");
					String channelTag = resultObj.getString("channelTag");
					String notifyURL = resultObj.getString("outNotifyUrl");
					if (outMerOrdercode != null && !outMerOrdercode.equalsIgnoreCase("")) {
						uri = util.getServiceUrl("channel", "error url request!");
						url = uri.toString() + "/v1.0/channel/callback/yilian/notify_call";
						requestEntity = new LinkedMultiValueMap<String, String>();
						requestEntity.add("merchant_no", phone);
						requestEntity.add("amount", tranamount);
						requestEntity.add("channel_tag", channelTag);
						requestEntity.add("order_desc", URLEncoder.encode(orderdesc, "UTF-8"));
						requestEntity.add("order_code", outMerOrdercode);
						requestEntity.add("sys_order", prdOrdNo);
						requestEntity.add("notify_url", URLEncoder.encode(notifyURL, "UTF-8"));
						result = restTemplate.postForObject(url, requestEntity, String.class);
					}*/

					LOG.info("订单已支付并代付成功!");
				}
			}else if("01".equals(orderStatus)&&"02".equals(dfStatus)){
				
				LOG.info("订单代付处理中!");
				
			} else {
				// 1、订单支付失败的业务逻辑处理请在本处增加（订单通知可能存在多次通知的情况，需要做多次通知的兼容处理，避免成功后又修改为失败）；
				// 2、返回响应内容

				LOG.info("订单支付失败!");
			}
		} catch (Exception exc) {
			LOG.error(exc.getMessage(),exc);
		}
		LOG.info("-----处理完成----");
	}

	
	
	//根据手机号和channelTag查询默认结算卡
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/querybankcard/byphone/andchanneltag")
	public @ResponseBody Object queryBankCard(@RequestParam(value = "phone") String phone,
			@RequestParam(value = "channel_tag") String channelTag
			) {
		
		LOG.info("channel_tag======"+channelTag);
		Map<String, String> map = new HashMap<String, String>();
		
		String bankCard = null;
		
		if("YB_PAY".equals(channelTag)) {
			RegisterAuth registerAuth = registerAuthBusiness.queryByMobile(phone);
			bankCard = registerAuth.getBankAccountNumber();
			
		}else if("LF_QUICK".equals(channelTag)) {
			bankCard = topupPayChannelBusiness.getLFQuickRegisterByPhone(phone);
			
		}else if("XJY_QUICK1".equals(channelTag)) {
			bankCard = topupPayChannelBusiness.getLDRegisterByPhone(phone);
			
		}else if("WLB_QUICK".equals(channelTag)) {
			bankCard = topupPayChannelBusiness.getWLBRegisterByPhone(phone);
			
		}else if("SDJ_QUICK".equals(channelTag)) {
			bankCard = topupPayChannelBusiness.getXJRegisterByPhone(phone);
			
		}else if("YH_QUICK".equals(channelTag)) {
			bankCard = topupPayChannelBusiness.getYHRegisterByPhone(phone);
			
		}else {
			bankCard = null;
		}
		
		
		if(bankCard != null || !"".equals(bankCard)) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESULT, bankCard);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			return map;
		}else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESULT, "未查询到该通道！！！");
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			return map;
		}
		
	}
	
	
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/paymentchannel/topup/ybcustomernum")
	public  String returnybCustomerNum(HttpServletRequest request, HttpServletResponse response, Model model)throws IOException {
			return "YBCustomerNum";
	}
	
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/paymentchannel/topup/ybnewcustomernum")
	public  String returnybNewCustomerNum(HttpServletRequest request, HttpServletResponse response, Model model)throws IOException {
			return "YBNewCustomerNum";
	}
	
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/paymentchannel/topup/userbankcard")
	public  String returnUserbankcard(HttpServletRequest request, HttpServletResponse response, Model model)throws IOException {
			return "UserBankCard";
	}
	
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/paymentchannel/topup/ybbalance")
	public  String returnybBalance(HttpServletRequest request, HttpServletResponse response, Model model)throws IOException {
			return "YBBalance";
	}
	
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/paymentchannel/topup/ybnewbalance")
	public  String returnybNewBalance(HttpServletRequest request, HttpServletResponse response, Model model)throws IOException {
			return "YBNewBalance";
	}

	@RequestMapping(method=RequestMethod.GET,value="/v1.0/paymentchannel/topup/return/count")
	public  String returnCount(HttpServletRequest request, HttpServletResponse response, Model model)throws IOException {
			return "count";
	}
	
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/paymentchannel/topup/query/repaymentinfoall")
	public  @ResponseBody Object queryRepaymentInfoMation()throws IOException {
		
			List<RepaymentInfoMation> list = topupPayChannelBusiness.getRepaymentInfoMationAll();
			
			if(list != null && list.size() > 0) {
				
				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功!", list);
			}else {
				
				return ResultWrap.init(CommonConstants.SUCCESS, "暂无数据!", list);
			}
		
	}
	
	
}
