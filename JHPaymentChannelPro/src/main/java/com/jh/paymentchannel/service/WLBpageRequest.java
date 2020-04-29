package com.jh.paymentchannel.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
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
import com.jh.paymentchannel.business.BranchbankBussiness;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.BranchNo;
import com.jh.paymentchannel.pojo.WLBBindCard;
import com.jh.paymentchannel.pojo.WLBRegister;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.wlb.Des3Encryption;
import com.jh.paymentchannel.util.wlb.HttpUtil;
import com.jh.paymentchannel.util.wlb.Md5Util;
import com.jh.paymentchannel.util.wlb.QuickPayUtil;

import cn.jh.common.tools.Log;
import cn.jh.common.utils.DateUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class WLBpageRequest {

	private static final Logger log = LoggerFactory.getLogger(WLBpageRequest.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private BranchbankBussiness branchbankBussiness;

	@Autowired
	private Util util;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	private static final Charset UTF_8 = StandardCharsets.UTF_8;

	private String signKey = "922gp16Xob99Hdl36g923QaWBB371S0u";

	private String desKey = "u92149F402kl5As71y4YlF04";

	private String channelName = "上海复规网络信息有限公司";

	private String channelNo = "C2544328796";

	private String registerUrl = "https://pay.feifanzhichuang.com/middlepayportal/merchant/in2";

	/**
	 * 进件接口
	 *
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wlb/register")
	public @ResponseBody Object jfShanGaoRegister(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode,
			@RequestParam(value = "provinceOfBank") String provinceOfBank,
			@RequestParam(value = "cityOfBank") String cityOfBank,
			@RequestParam(value = "bankBranchName") String bankBranchName) throws Exception {
		log.info("开始进入进件接口========================");
		Map map = new HashMap();
		Map<String, String> maps = new HashMap<String, String>();
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("RESULT================" + result);
		JSONObject jsonObject;
		JSONObject resultObj;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			log.error("查询订单信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jfcoin");
			map.put("resp_message", "没有该订单信息");
			return map;
		}

		String userid = resultObj.getString("userid");
		// 费率
		String rate = resultObj.getString("rate");
		// 额外费率
		String extraFee = resultObj.getString("extraFee");
		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/userid";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("user_id", userid);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		log.info("RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			log.error("查询默认结算卡出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jfcoin");
			map.put("resp_message", "查询默认结算卡有误");
			return map;
		}
		String respcode = jsonObject.getString("resp_code");

		// 默认提现卡卡号
		String cardNo = resultObj.getString("cardNo");
		String userName = resultObj.getString("userName");
		/** 身份证号 */
		String idcard = resultObj.getString("idcard");
		String phone = resultObj.getString("phone");
		String bankName = resultObj.getString("bankName");

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
			log.error("查询用户ID出错！！！！");
			maps.put("resp_code", "failed");
			maps.put("channel_type", "sdj");
			maps.put("resp_message", "没有查询到用户ID");
			return maps;
		}

		log.info("随机获取的userId" + shopUserId);

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/shops/query/uid";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("userid", shopUserId);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		log.info("RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			log.error("查询商铺信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jfcoin");
			map.put("resp_message", "查询商铺信息有误");

			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
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

		log.info("RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
		} catch (Exception e) {
			log.error("查询商铺省市出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "该商铺省市信息有误");

			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "该商铺省市信息有误");
			result = restTemplate.postForObject(url, requestEntity, String.class);

			return map;
		}
		String province = jsonObject.getString("province");
		String city = jsonObject.getString("city");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/cardno";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("cardno", bankCard);
		requestEntity.add("type", "0");
		result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("接口/v1.0/user/bank/default/cardno--RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			log.error("查询银行卡信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询银行卡信息有误");
			return map;
		}

		String cardName = resultObj.getString("bankName");

		String cardname = Util.queryBankNameByBranchName(cardName);

		String expiredTime = resultObj.getString("expiredTime");
		String securityCode = resultObj.getString("securityCode");
		String cardtype = resultObj.getString("cardType");

		BranchNo findByBankName;
		// 银行总行联行号
		String inBankUnitNo;
		try {
			findByBankName = branchbankBussiness.findByBankName(Util.queryBankNameByBranchName(bankName));
			inBankUnitNo = findByBankName.getBankNo();
		} catch (Exception e1) {
			log.info("查询银行总行联行号有误======"+e1);
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "暂不支持该结算银行,请及时更换结算银行卡!");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "暂不支持该结算银行,请及时更换结算银行卡!");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}

		// =================================

		com.alibaba.fastjson.JSONObject reqJson = new com.alibaba.fastjson.JSONObject(); // 请注意fastjson的版本，需要用自带排序功能的

		reqJson.put("accountName", userName); // 如果bankType为TOPRIVATE，则legalPersonName与accountName必须一致
		String accountNo = cardNo;// 开户账号

		accountNo = Des3Encryption.encode(desKey, accountNo);
		reqJson.put("accountNo", accountNo);
		reqJson.put("alipayT0Fee", rate);
		reqJson.put("alipayT1Fee", rate);
		reqJson.put("alipayType", "2015062600002758"); // 请参考附录 支付宝类别码，需填一级类目
		reqJson.put("bankBranch", bankBranchName);
		reqJson.put("bankCity", cityOfBank);
		reqJson.put("bankCode", inBankUnitNo);
		reqJson.put("bankName", bankName);
		reqJson.put("bankProv", provinceOfBank);
		reqJson.put("bankType", "TOPRIVATE");
		// reqJson.put("businessLicense", "440301104041144");// 营业执照要真实
		// reqJson.put("business_license_type", "NATIONAL_LEGAL");//
		// 营业执照号类别，merchantType非PERSON的必填
		// String creditCardNo = "";//结算人信用卡
		// creditCardNo = Des3Encryption.encode(desKey, creditCardNo);
		// reqJson.put("creditCardNo", creditCardNo);

		reqJson.put("channelName", channelName);// 渠道名称，请用分配的代理商名称
		reqJson.put("channelNo", channelNo);// 渠道编码，请用分配的代理商号

		reqJson.put("installCity", city);
		reqJson.put("installCounty", city);
		reqJson.put("installProvince", province);
		reqJson.put("isOneOrBig", "Y");// 必须为yes
		reqJson.put("legalPersonID", idcard); // 法人身份证
		reqJson.put("legalPersonName", userName); // 法人姓名，如果bankType为TOPRIVATE，则结算账户与法人必须一致,
													// 如果bankType为TOPUBLIC，结算账户名和商户名称是一致的
		reqJson.put("merchantBillName", shopName);// 测试商户简称
		reqJson.put("merchantName", shopName); // 商户全称，企业商户填写营业执照名称
		reqJson.put("merchantPersonName", userName);//
		reqJson.put("merchantPersonPhone", phone);// 商户联系人电话
		reqJson.put("merchantType", "PERSON");
		reqJson.put("operateAddress", address + shopsaddress);
		// reqJson.put("remarks", "商户平台商户号"); //
		// 商户平台全局唯一，并且需要保存，建议填写商户平台商户号,此参数在商户秘钥查询接口中必填
		reqJson.put("wxT0Fee", rate);
		reqJson.put("wxT1Fee", rate);
		reqJson.put("wxType", "158"); // 请参考附录微信类别码

		reqJson.put("province_code", "510000");// 支付宝省代码
		reqJson.put("city_code", "510100");// 支付宝市代码
		reqJson.put("district_code", "510104");// 支付宝地区代码
		String sourceBody = reqJson.toJSONString();
		log.info("签名体：" + sourceBody);
		String requestSign = Md5Util.MD5(sourceBody + signKey);
		reqJson.put("sign", requestSign);
		String requestStr = reqJson.toJSONString();
		log.info("商户入网请求报文：" + requestStr);

		// String url = "https://pay.feifanzhichuang.com/middlepayportal/merchant/in2";
		String respStr = HttpUtil.sendPost(registerUrl, requestStr, HttpUtil.CONTENT_TYPE_JSON);

		log.info("商户入网响应报文：" + respStr);
		com.alibaba.fastjson.JSONObject respJson = JSON.parseObject(respStr);

		// 去掉签名，md5加密，比对是否正确
		String respSign = (String) respJson.remove("sign");
		// 再次加密
		String generSign = Md5Util.MD5(respJson.toString() + signKey);
		log.info("验签结果：" + generSign.equals(respSign));
		// TODO 执行接下来的业务逻辑，写库，保存下发的3个秘钥，balabalabala

		String desKey = respJson.getString("desKey");
		String merchantNo = respJson.getString("merchantNo");
		String queryKey = respJson.getString("queryKey");
		String respCode = respJson.getString("respCode");
		String signkey = respJson.getString("signKey");

		log.info("desKey======" + desKey);
		log.info("merchantNo======" + merchantNo);
		log.info("queryKey======" + queryKey);
		log.info("respCode======" + respCode);
		log.info("signKey======" + signkey);

		if ("0000".equals(respCode)) {
			log.info("商户进件成功=====");
			com.alibaba.fastjson.JSONObject reqJson1 = new com.alibaba.fastjson.JSONObject(); // 请注意fastjson的版本，需要用自带排序功能的

			reqJson1.put("channelName", channelName);// 渠道名称，请用分配的代理商名称
			reqJson1.put("channelNo", channelNo);// 渠道编码，请用分配的代理商号
			reqJson1.put("merchantNo", merchantNo);// 渠道编码，请用分配的商户编号
			reqJson1.put("productType", "QUICKPAY");// 产品类型（银联二维码）
			reqJson1.put("t0Fee", rate);
			reqJson1.put("t1Fee", rate);

			log.info("签名体：" + reqJson1.toJSONString());
			String sign = Md5Util.MD5(reqJson1.toJSONString() + signKey);
			reqJson1.put("sign", sign);
			String requestStr1 = reqJson1.toJSONString();
			log.info("商户添加费率请求报文：" + requestStr1);

			String url1 = "https://pay.feifanzhichuang.com/middlepayportal/merchant/addFee";
			String respStr1 = HttpUtil.sendPost(url1, requestStr1, HttpUtil.CONTENT_TYPE_JSON);

			log.info("商户费率添加响应报文：" + respStr1);
			com.alibaba.fastjson.JSONObject respJson1 = JSON.parseObject(respStr1);

			// 去掉签名，md5加密，比对是否正确
			String respSign1 = (String) respJson1.remove("sign");
			// 再次加密
			String generSign1 = Md5Util.MD5(respJson1.toString() + signKey);
			log.info("验签结果：" + generSign1.equals(respSign1));

			String respCode1 = respJson1.getString("respCode");

			if ("0000".equals(respCode1)) {
				log.info("商户添加费率成功======");

				WLBRegister wlbRegister = new WLBRegister();
				wlbRegister.setPhone(phone);
				wlbRegister.setBankCard(cardNo);
				wlbRegister.setIdCard(idcard);
				wlbRegister.setMerchantNO(merchantNo);
				wlbRegister.setSignKey(signkey);
				wlbRegister.setDesKey(desKey);
				wlbRegister.setQueryKey(queryKey);
				wlbRegister.setRate(rate);
				wlbRegister.setExtraFee(extraFee);

				topupPayChannelBusiness.createWLBRegister(wlbRegister);

				map = (Map) pageOpenCard(request, ordercode);

				return map;
			} else {
				log.info("商户添加费率失败======");
				map.put("resp_code", "failed");
				map.put("channel_type", "jf");
				map.put("resp_message", "进件失败,请稍后重试！");
				
				restTemplate = new RestTemplate();
				uri = util.getServiceUrl("transactionclear", "error url request!");
				url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("ordercode", ordercode);
				requestEntity.add("remark", "商户添加费率失败");
				result = restTemplate.postForObject(url, requestEntity, String.class);
				
				return map;
			}

		} else {
			log.info("请求进件接口失败======");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "进件失败,请稍后重试！");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "进件失败,请稍后重试！");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}

	}

	// 卡开通接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wlb/pageopencard")
	public @ResponseBody Object pageOpenCard(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode) throws Exception {
		log.info("开始进入卡开通接口========================");
		Map map = new HashMap();
		Map<String, String> maps = new HashMap<String, String>();
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("RESULT================" + result);
		JSONObject jsonObject;
		JSONObject resultObj;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			log.error("查询订单信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jfcoin");
			map.put("resp_message", "没有该订单信息");
			return map;
		}

		String userid = resultObj.getString("userid");
		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/userid";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("user_id", userid);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		log.info("RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			log.error("查询默认结算卡出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jfcoin");
			map.put("resp_message", "查询默认结算卡有误");
			return map;
		}
		String respcode = jsonObject.getString("resp_code");

		// 默认提现卡卡号
		String cardNo = resultObj.getString("cardNo");
		String userName = resultObj.getString("userName");
		// 身份证号
		String idcard = resultObj.getString("idcard");
		String phone = resultObj.getString("phone");
		String bankName = resultObj.getString("bankName");

		// 查询信用卡信息
		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/cardno";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("cardno", bankCard);
		requestEntity.add("type", "0");
		result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("接口/v1.0/user/bank/default/cardno--RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			log.error("查询银行卡信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询银行卡信息有误");
			return map;
		}
		
		String userName1 = resultObj.getString("userName");
		/** 身份证号 */
		String idcard1 = resultObj.getString("idcard");
		String phone1 = resultObj.getString("phone");
		String bankName1 = resultObj.getString("bankName");

		String notifyUrl = ipAddress + "/v1.0/paymentchannel/topup/wlb/bindcard/notify_call";

		WLBRegister wlbRegister = topupPayChannelBusiness.getWLBRegisterByIdCard(idcard);

		Map<String, String> reqMap = new LinkedHashMap<String, String>();
		reqMap.put("trxType", "OPEN_CARD");
		reqMap.put("merchantNo", wlbRegister.getMerchantNO());
		reqMap.put("orderNum", ordercode);// 每次请求订单号唯一
		reqMap.put("trxTime", DateUtil.getyyyyMMddHHmmssDateFormat(new Date()));
		String accountNo = bankCard;
		accountNo = Des3Encryption.encode(desKey, accountNo);
		reqMap.put("cardNum", accountNo);

		String callbackUrl = ipAddress + "/v1.0/paymentchannel/topup/wlb/bindcard/return_call";
		if (StringUtils.isNotBlank(callbackUrl)) {
			reqMap.put("callbackUrl", callbackUrl);
		}
		reqMap.put("serverCallbackUrl", notifyUrl);

		String mobile = phone1;
		mobile = Des3Encryption.encode(desKey, mobile);
		reqMap.put("phone", mobile);
		String sign = QuickPayUtil.generSign(reqMap, wlbRegister.getSignKey());
		reqMap.put("sign", sign);

		log.info("提交的数据##" + reqMap.toString());
		String opencardUrl = "https://pay.feifanzhichuang.com/middlepaytrx/kuaiPay";
		try {
			String resultStr = HttpUtil.postAndReturnString(reqMap, opencardUrl);
			log.info("返回的报文" + resultStr);
			JSONObject fromObject = JSONObject.fromObject(resultStr);

			String qrCode = fromObject.getString("r3_qrCode");
			String retCode = fromObject.getString("retCode");
			String retMsg = fromObject.getString("retMsg");

			if ("0000".equals(retCode)) {
				log.info("请求绑卡接口成功======");
				map.put("resp_code", "success");
				map.put("channel_type", "jf");
				map.put("redirect_url", qrCode);
				return map;
			} else {
				log.info("请求绑卡接口失败======");
				map.put("resp_code", "failed");
				map.put("channel_type", "jf");
				map.put("resp_message", retMsg);
				
				restTemplate = new RestTemplate();
				uri = util.getServiceUrl("transactionclear", "error url request!");
				url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("ordercode", ordercode);
				requestEntity.add("remark", "请求绑卡接口失败");
				result = restTemplate.postForObject(url, requestEntity, String.class);
				
				return map;
			}

		} catch (Exception e) {
			log.error("请求绑卡接口失败======");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "交易排队中,请稍后重试！");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "请求绑卡接口失败");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}

	}

	// 支付短信接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wlb/consumesms")
	public @ResponseBody Object consumeSMS(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "ordercode") String ordercode) throws Exception {

		log.info("开始进入支付短信接口========================");
		Map map = new HashMap();
		Map<String, String> maps = new HashMap<String, String>();
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("RESULT================" + result);
		JSONObject jsonObject;
		JSONObject resultObj;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			log.error("查询订单信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jfcoin");
			map.put("resp_message", "没有该订单信息");
			return map;
		}

		String amount = resultObj.getString("amount");
		String userid = resultObj.getString("userid");
		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/userid";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("user_id", userid);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		log.info("RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			log.error("查询默认结算卡出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jfcoin");
			map.put("resp_message", "查询默认结算卡有误");
			return map;
		}
		String respcode = jsonObject.getString("resp_code");

		// 默认提现卡卡号
		String cardNo = resultObj.getString("cardNo");
		String userName = resultObj.getString("userName");
		// 身份证号
		String idcard = resultObj.getString("idcard");

		// 查询信用卡信息
		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/cardno";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("cardno", bankCard);
		requestEntity.add("type", "0");
		result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("接口/v1.0/user/bank/default/cardno--RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			log.error("查询银行卡信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询银行卡信息有误");
			return map;
		}

		// 信用卡卡号
		String cardNo1 = resultObj.getString("cardNo");
		String userName1 = resultObj.getString("userName");
		/** 身份证号 */
		String idcard1 = resultObj.getString("idcard");
		String phone = resultObj.getString("phone");
		String bankName1 = resultObj.getString("bankName");

		WLBRegister wlbRegister = topupPayChannelBusiness.getWLBRegisterByIdCard(idcard);

		WLBBindCard wlbBindCard = topupPayChannelBusiness.getWLBBindCardByBankCard(bankCard);

		Map<String, String> reqMap = new LinkedHashMap<String, String>();
		reqMap.put("trxType", "SMS_CODE");
		reqMap.put("merchantNo", wlbRegister.getMerchantNO());
		reqMap.put("orderNum", ordercode);// 与消费订单一致

		String trxTime = DateUtil.getyyyyMMddHHmmssDateFormat(new Date());
		reqMap.put("trxTime", trxTime);// 与消费时间一致
		reqMap.put("smsType", "02");
		phone = Des3Encryption.encode(wlbRegister.getDesKey(), phone);
		reqMap.put("phone", phone);
		reqMap.put("amount", amount);
		reqMap.put("encrypt", "T0");

		String token = wlbBindCard.getToken();
		token = Des3Encryption.encode(wlbRegister.getDesKey(), token);
		reqMap.put("token", token);
		String sign = QuickPayUtil.generSign(reqMap, wlbRegister.getSignKey());

		reqMap.put("sign", sign);
		log.info("提交的数据##" + reqMap.toString());
		String url1 = "https://pay.feifanzhichuang.com/middlepaytrx/kuaiPay";
		try {
			String resultStr = HttpUtil.postAndReturnString(reqMap, url1);
			log.info("返回的报文" + resultStr);
			JSONObject fromObject = JSONObject.fromObject(resultStr);

			String retCode = fromObject.getString("retCode");
			String retMsg = fromObject.getString("retMsg");

			if ("0000".equals(retCode)) {
				map.put("resp_code", "success");
				map.put("channel_type", "jf");
				map.put("trxTime", trxTime);

			} else {
				map.put("resp_code", "failed");
				map.put("channel_type", "jf");
				map.put("resp_message", retMsg);
				
				restTemplate = new RestTemplate();
				uri = util.getServiceUrl("transactionclear", "error url request!");
				url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("ordercode", ordercode);
				requestEntity.add("remark", retMsg);
				result = restTemplate.postForObject(url, requestEntity, String.class);

			}

		} catch (Exception e) {
			log.info("请求短信接口失败======"+e);
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "交易排队中,请稍后重试!");

			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "请求短信接口失败");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
		}

		return map;
	}

	// 支付接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wlb/consume")
	public @ResponseBody Object cjconsume(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode, @RequestParam(value = "smsCode") String smsCode,
			@RequestParam(value = "payNo") String payNo) throws Exception {

		log.info("开始进入消费支付接口========================");
		Map map = new HashMap();
		Map<String, String> maps = new HashMap<String, String>();
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("RESULT================" + result);
		JSONObject jsonObject;
		JSONObject resultObj;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			log.error("查询订单信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jfcoin");
			map.put("resp_message", "没有该订单信息");
			return map;
		}

		String amount = resultObj.getString("amount");
		String userid = resultObj.getString("userid");
		// 费率
		String rate = resultObj.getString("rate");
		// 额外费率
		String extraFee = resultObj.getString("extraFee");
		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/userid";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("user_id", userid);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		log.info("RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			log.error("查询默认结算卡出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jfcoin");
			map.put("resp_message", "查询默认结算卡有误");
			return map;
		}
		String respcode = jsonObject.getString("resp_code");

		// 默认提现卡卡号
		String cardNo = resultObj.getString("cardNo");
		String phone = resultObj.getString("phone");
		// 身份证号
		String idCard = resultObj.getString("idcard");

		// 查询信用卡信息
		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/cardno";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("cardno", bankCard);
		requestEntity.add("type", "0");
		result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("接口/v1.0/user/bank/default/cardno--RESULT================" + result);
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e) {
			log.error("查询银行卡信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询银行卡信息有误");
			return map;
		}

		String userName = resultObj.getString("userName");
		/** 身份证号 */
		String idcard = resultObj.getString("idcard");
		String phone1 = resultObj.getString("phone");
		String bankName = resultObj.getString("bankName");

		WLBRegister wlbRegister = topupPayChannelBusiness.getWLBRegisterByIdCard(idCard);

		WLBBindCard wlbBindCard = topupPayChannelBusiness.getWLBBindCardByBankCard(bankCard);

		Map<String, String> reqMap = new LinkedHashMap<String, String>();
		reqMap.put("trxType", "CONSUME");
		reqMap.put("merchantNo", wlbRegister.getMerchantNO());
		String token = Des3Encryption.encode(wlbRegister.getDesKey(), wlbBindCard.getToken());// 卡开通异步通知返回的token
		reqMap.put("token", token);
		reqMap.put("goodsName", "充值缴费");
		// reqMap.put("serverDfUrl", "http://www.baidu.com");
		reqMap.put("serverCallbackUrl", ipAddress + "/v1.0/paymentchannel/topup/wlb/pay_notify_call");
		reqMap.put("orderNum", ordercode);// 消费订单号与的卡开通订单号保持一致并且唯一
		reqMap.put("trxTime", payNo);
		String smsCode1 = Des3Encryption.encode(wlbRegister.getDesKey(), smsCode);// 短信消费接口反悔的短信验证码
		reqMap.put("smsCode", smsCode1);//

		// 生成加密报文sign
		String sign = QuickPayUtil.generSign(reqMap, wlbRegister.getSignKey());
		reqMap.put("sign", sign);
		log.info("提交的数据" + reqMap.toString());
		String url1 = "https://pay.feifanzhichuang.com/middlepaytrx/kuaiPay";
		try {
			String resultStr = HttpUtil.postAndReturnString(reqMap, url1);
			log.info("返回的报文" + resultStr);
			JSONObject fromObject = JSONObject.fromObject(resultStr);

			String retCode = fromObject.getString("retCode");
			String retMsg = fromObject.getString("retMsg");

			if ("0000".equals(retCode)) {
				map.put("resp_code", "success");
				map.put("channel_type", "jf");
				map.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/sdjpaysuccess");

			} else {
				map.put("resp_code", "failed");
				map.put("channel_type", "jf");
				// map.put("redirect_url", ipAddress +
				// "/v1.0/paymentchannel/topup/sdjpayerror");
				map.put("resp_message", "支付失败！ 失败原因: " + retMsg);
				
				restTemplate = new RestTemplate();
				uri = util.getServiceUrl("transactionclear", "error url request!");
				url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
				requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("ordercode", ordercode);
				requestEntity.add("remark", retMsg);
				result = restTemplate.postForObject(url, requestEntity, String.class);
				
			}

		} catch (Exception e) {
			log.info("请求支付接口失败======"+e);
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/sdjpayerror");
			
			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", "请求支付接口失败");
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
		}

		return map;
	}

	// 结算卡变更接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wlb/updatecard")
	public @ResponseBody Object updateCard(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode,
			@RequestParam(value = "provinceOfBank") String provinceOfBank,
			@RequestParam(value = "cityOfBank") String cityOfBank,
			@RequestParam(value = "bankBranchName") String bankBranchName) throws Exception {
		log.info("开始进入结算卡变更接口=======");
		Map map = new HashMap();
		Map<String, String> maps = new HashMap<String, String>();
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("RESULT================" + result);
		JSONObject jsonObject = JSONObject.fromObject(result);
		JSONObject resultObj = jsonObject.getJSONObject("result");

		String userid = resultObj.getString("userid");

		String bankCard = resultObj.getString("bankcard");

		restTemplate = new RestTemplate();
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/bank/default/userid";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("user_id", userid);
		result = restTemplate.postForObject(url, requestEntity, String.class);

		log.info("RESULT================" + result);
		jsonObject = JSONObject.fromObject(result);
		resultObj = jsonObject.getJSONObject("result");

		String cardNo = resultObj.getString("cardNo");
		String userName = resultObj.getString("userName");
		/** 身份证号 */
		String idcard = resultObj.getString("idcard");
		String phone = resultObj.getString("phone");
		// 开户行所在省
		log.info("provinceOfBank" + provinceOfBank);
		// 开户行所在市
		log.info("cityOfBank" + cityOfBank);
		// 银行名称
		String bankName = resultObj.getString("bankName");

		String cardType = resultObj.getString("cardType");

		BranchNo findByBankName;
		// 银行总行联行号
		String inBankUnitNo;
		try {
			findByBankName = branchbankBussiness.findByBankName(Util.queryBankNameByBranchName(bankName));
			inBankUnitNo = findByBankName.getBankNo();
		} catch (Exception e1) {
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "查询银行信息有误");
			return map;
		}

		WLBRegister wlbRegister = topupPayChannelBusiness.getWLBRegisterByIdCard(idcard);

		com.alibaba.fastjson.JSONObject reqJson = new com.alibaba.fastjson.JSONObject(); // 请注意fastjson的版本，需要用自带排序功能的
		reqJson.put("changeType", "1");// 变更类型详情看文档编号16
		reqJson.put("channelName", channelName);// 渠道名
		reqJson.put("channelNo", channelNo);// 渠道编号
		reqJson.put("merchantNo", wlbRegister.getMerchantNO());// 商户编号

		// 修改结算卡信息
		String accountNo = cardNo;// 结算卡号
		accountNo = Des3Encryption.encode(desKey, accountNo);
		String oriAccountNo = wlbRegister.getBankCard();// 原始结算卡号，此卡号为商户进件时上传的accountNo参数
		oriAccountNo = Des3Encryption.encode(desKey, oriAccountNo);
		reqJson.put("accountNo", accountNo);
		reqJson.put("bankBranch", bankBranchName);// 结算支行
		reqJson.put("bankProv", provinceOfBank);// 开户所在地
		reqJson.put("bankCity", cityOfBank);// 开户所在地
		reqJson.put("bankCode", inBankUnitNo);// 联行号（参考文档）
		reqJson.put("bankName", bankName);// 银行名
		reqJson.put("bankType", "TOPRIVATE");// 结算银行性质（对公和对私）
		reqJson.put("oriAccountNo", oriAccountNo);

		String sourceBody = reqJson.toJSONString();
		log.info("签名体：" + sourceBody);
		// md5加密
		String sign = Md5Util.MD5(sourceBody + signKey);
		reqJson.put("sign", sign);
		String requestStr = reqJson.toJSONString();
		log.info("商户资料修改请求报文：" + requestStr);
		String url1 = "https://pay.feifanzhichuang.com/middlepayportal/merchant/modify";
		String respStr = HttpUtil.sendPost(url1, requestStr, HttpUtil.CONTENT_TYPE_JSON);
		log.info("商户资料变更响应报文：" + respStr);

		com.alibaba.fastjson.JSONObject respJson = JSON.parseObject(respStr);

		String respCode = respJson.getString("respCode");
		String respMsg = respJson.getString("respMsg");

		if ("0000".equals(respCode)) {
			wlbRegister.setPhone(phone);
			wlbRegister.setBankCard(cardNo);

			topupPayChannelBusiness.createWLBRegister(wlbRegister);

			if (topupPayChannelBusiness.getWLBBindCardByBankCard(bankCard) == null) {
				log.info("用户需要绑卡======");
				map = (Map) pageOpenCard(request, ordercode);

				return map;
			} else {
				map.put("resp_code", "success");
				map.put("channel_type", "jf");
				map.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/wlbpay?ordercode=" + ordercode
						+ "&ipAddress=" + ipAddress);
				return map;
			}
		} else {
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", respMsg);

			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/update/remark";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			requestEntity.add("remark", respMsg);
			result = restTemplate.postForObject(url, requestEntity, String.class);
			
			return map;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wlb/turntopaypage")
	public @ResponseBody Object turnToCJPayPage(HttpServletRequest request,
			@RequestParam(value = "ordercode") String ordercode) throws Exception {

		Map map = new HashMap();

		map.put("resp_code", "success");
		map.put("channel_type", "jf");
		map.put("redirect_url",
				ipAddress + "/v1.0/paymentchannel/topup/wlbpay?ordercode=" + ordercode + "&ipAddress=" + ipAddress);
		return map;

	}

	// 中转接口
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/wlbpay")
	public String returnjfapipay(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		String ordercode = request.getParameter("ordercode");
		String ipAddress = request.getParameter("ipAddress");

		model.addAttribute("ordercode", ordercode);
		model.addAttribute("ipAddress", ipAddress);

		return "wlbpaymessage";
	}

	// 跳转确认提现卡页面的中转接口
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/towlbbankinfo")
	public String tojfshangaobankinfo(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		log.info("/v1.0/paymentchannel/topup/towlbbankinfo=========towlbbankinfo");
		String bankName = request.getParameter("bankName");// 结算卡银行名称
		String bankNo = request.getParameter("bankNo");// 结算卡卡号
		String amount = request.getParameter("amount");
		String ordercode = request.getParameter("ordercode");
		String provinceOfBank = request.getParameter("provinceOfBank");
		String cityOfBank = request.getParameter("cityOfBank");
		String bankBranchName = request.getParameter("bankBranchName");
		String bankBranchId = request.getParameter("bankBranchId");
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
		model.addAttribute("provinceOfBank", provinceOfBank);
		model.addAttribute("cityOfBank", cityOfBank);
		model.addAttribute("bankBranchName", bankBranchName);
		model.addAttribute("bankBranchId", bankBranchId);
		model.addAttribute("cardType", cardType);
		model.addAttribute("isRegister", isRegister);
		model.addAttribute("cardtype", cardtype);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("cardName", cardName);
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("ipAddress", ipAddress);

		return "wlbbankInfo";
	}

	// 支付接口异步通知
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wlb/pay_notify_call")
	public void paynotifyCall(HttpServletRequest request, HttpServletResponse response

	) throws Exception {
		log.info("支付异步通知进来了=======");

		String retCode = request.getParameter("retCode");
		String retMsg = request.getParameter("retMsg");
		String orderNumber = request.getParameter("r2_orderNumber");
		String serialNumber = request.getParameter("r9_serialNumber");

		log.info("retCode=====" + retCode);
		log.info("retMsg=====" + retMsg);
		log.info("orderNumber=====" + orderNumber);
		log.info("serialNumber=====" + serialNumber);

		try {
			Log.setLogFlag(true);
			Log.println("---交易： 订单结果异步通知-------------------------");

			log.info("交易： 订单结果异步通知===================");
			if ("0000".equals(retCode)) { // 订单已支付;

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
					// requestEntity.add("third_code", workId);
					requestEntity.add("order_code", orderNumber);
					String result = restTemplate.postForObject(url, requestEntity, String.class);

					log.info("订单状态修改成功===================");

					// **判断是否有外放的通道的处理， 如果有那么继续回调外放哦*//*
					/*
					 * uri = util.getServiceUrl("transactionclear", "error url request!"); url =
					 * uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
					 * 
					 * requestEntity = new LinkedMultiValueMap<String, String>();
					 * requestEntity.add("order_code", orderId); result =
					 * restTemplate.postForObject(url, requestEntity, String.class);
					 * 
					 * JSONObject jsonObject = JSONObject.fromObject(result); JSONObject resultObj =
					 * jsonObject.getJSONObject("result"); String outMerOrdercode =
					 * resultObj.getString("outMerOrdercode"); String orderdesc =
					 * resultObj.getString("desc"); String phone = resultObj.getString("phone");
					 * String tranamount = resultObj.getString("amount"); String channelTag =
					 * resultObj.getString("channelTag"); String notifyURL =
					 * resultObj.getString("outNotifyUrl"); if (outMerOrdercode != null &&
					 * !outMerOrdercode.equalsIgnoreCase("")) { uri = util.getServiceUrl("channel",
					 * "error url request!"); url = uri.toString() +
					 * "/v1.0/channel/callback/yilian/notify_call"; requestEntity = new
					 * LinkedMultiValueMap<String, String>(); requestEntity.add("merchant_no",
					 * phone); requestEntity.add("amount", tranamount);
					 * requestEntity.add("channel_tag", channelTag); requestEntity.add("order_desc",
					 * URLEncoder.encode(orderdesc, "UTF-8")); requestEntity.add("order_code",
					 * outMerOrdercode); requestEntity.add("sys_order", orderId);
					 * requestEntity.add("notify_url", URLEncoder.encode(notifyURL, "UTF-8"));
					 * result = restTemplate.postForObject(url, requestEntity, String.class); }
					 */

					log.info("订单已支付!");
					PrintWriter writer = response.getWriter();
					writer.print("success");
					writer.close();
				}
			} else {
				// 1、订单支付失败的业务逻辑处理请在本处增加（订单通知可能存在多次通知的情况，需要做多次通知的兼容处理，避免成功后又修改为失败）；
				// 2、返回响应内容

				log.info("订单支付失败!");
			}
		} catch (Exception e) {
			log.error("",e);
		}
		log.info("-----处理完成----");
	}

	// 银联绑卡同步通知
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/wlb/bindcard/return_call")
	public String cjReturnCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {

		Map map = new HashMap();
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		log.info("卡开通同步通知进来了");

		return "cjbindcardsuccess";

	}

	// 卡开通异步通知
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentchannel/topup/wlb/bindcard/notify_call")
	public void jfshangaoNotifyCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		log.info("银联绑卡异步通知进来了=======");

		InputStream inputStream = request.getInputStream();
		ByteArrayOutputStream byteArray = null;
		byteArray = new ByteArrayOutputStream();
		byte[] dat = new byte[2048];
		int l = 0;
		while ((l = inputStream.read(dat, 0, 2048)) != -1) {
			byteArray.write(dat, 0, l);
		}
		byteArray.flush();
		log.info("ByteArrayOutputStream2String=============" + new String(byteArray.toByteArray(), "UTF-8"));
		String info = new String(byteArray.toByteArray(), "UTF-8");
		JSONObject jsonInfo = JSONObject.fromObject(info);
		log.info("jsonInfo=============" + jsonInfo.toString());

		String ordercode = jsonInfo.getString("orderNum");
		String retCode = jsonInfo.getString("retCode");
		String retMsg = jsonInfo.getString("retMsg");
		String token = jsonInfo.getString("token");
		String phone = jsonInfo.getString("phone");

		Map map = new HashMap();
		Map<String, String> maps = new HashMap<String, String>();
		RestTemplate restTemplate = new RestTemplate();
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("order_code", ordercode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		log.info("RESULT================" + result);
		JSONObject jsonObject;
		JSONObject resultObj = null;
		try {
			jsonObject = JSONObject.fromObject(result);
			resultObj = jsonObject.getJSONObject("result");
		} catch (Exception e1) {
			log.error("查询订单信息出错");
			map.put("resp_code", "failed");
			map.put("channel_type", "jfcoin");
			map.put("resp_message", "没有该订单信息");
		}
		// 充值卡卡号
		String bankCard = resultObj.getString("bankcard");

		if ("0000".equals(retCode)) {
			WLBBindCard wlbBindCard = new WLBBindCard();
			wlbBindCard.setPhone(phone);
			wlbBindCard.setBankCard(bankCard);
			wlbBindCard.setToken(token);

			topupPayChannelBusiness.createWLBBindCard(wlbBindCard);

			restTemplate = new RestTemplate();
			uri = util.getServiceUrl("transactionclear", "error url request!");
			url = uri.toString() + "/v1.0/transactionclear/payment/deletepayment/byordercode";
			requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("ordercode", ordercode);
			result = restTemplate.postForObject(url, requestEntity, String.class);

		}

	}

}