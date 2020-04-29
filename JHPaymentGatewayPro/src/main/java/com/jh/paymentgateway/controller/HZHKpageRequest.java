package com.jh.paymentgateway.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.BankNumCode;
import com.jh.paymentgateway.pojo.HZHKBindCard;
import com.jh.paymentgateway.pojo.HZHKCode;
import com.jh.paymentgateway.pojo.HZHKOrder;
import com.jh.paymentgateway.pojo.HZHKRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.hzhk.HttpRequest;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.ExceptionUtil;

@Controller
@EnableAutoConfiguration
public class HZHKpageRequest  extends BaseChannel{
	private static final Logger LOG = LoggerFactory.getLogger(HZHKpageRequest.class);

	@Autowired
	private RedisUtil redisUtil;

	@Value("${payment.ipAddress}")
	private String ip;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	private static String spCode = "SHFG_B7926437";
	
	// 跟还款对接的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hzhk/torepayment")
	public  @ResponseBody Object HZHKButtJoint(HttpServletRequest request,
			@RequestParam(value = "bankCard") String bankCard, 
			@RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "phone") String phone, 
			@RequestParam(value = "userName") String userName,
			@RequestParam(value = "cardType") String cardType,
			@RequestParam(value = "bankName") String bankName, 
			@RequestParam(value = "securityCode") String securityCode,
			@RequestParam(value = "expiredTime") String expiredTime,
			@RequestParam(value = "extraFee") String extraFee,
			@RequestParam(value = "rate") String rate)throws Exception {

		LOG.info("进来-------------------");
	
		Map<String, Object> map = new HashMap<String, Object>();
		HZHKRegister hzhkRegister = topupPayChannelBusiness.getHZHKRegisterByidCard(idCard);
		HZHKBindCard hzhkBankCard = topupPayChannelBusiness.getHZHKBindCardByBankCard(bankCard);
		
		try {
		if(hzhkRegister==null) { //判断是否进件,未进件
			map = (Map<String, Object>) this.HZHKregister(userName, idCard, bankCard, phone, bankName, expiredTime, securityCode, rate, extraFee, cardType);
			if ("000000".equals(map.get("resp_code"))) {
					map.put(CommonConstants.RESP_CODE, "999996");
					map.put(CommonConstants.RESP_MESSAGE, "需要获取激活短信申请进行激活授权操作");
					String url = ip +"/v1.0/paymentgateway/quick/hzhk/bangka?expiredTime="
							+ expiredTime + "&securityCode=" + securityCode + "&bankName=" 
							+ bankName + "&cardType=" + URLEncoder.encode("贷记卡", "UTF-8")+ "&bankCard=" + bankCard
							+ "&userName=" + userName + "&phone=" + phone+"&idCard="+idCard
							+ "&ipAddress=" + ip;
					map.put(CommonConstants.RESULT, url);
			}else {
				   map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				   map.put(CommonConstants.RESP_MESSAGE, "进件失败");
				   return map;
			}
		}else{
			if(hzhkBankCard==null) {//已进件,未激活卡
				map.put(CommonConstants.RESP_CODE, "999996");
				map.put(CommonConstants.RESP_MESSAGE, "需要获取激活短信申请进行激活授权操作");
				String url = ip +"/v1.0/paymentgateway/quick/hzhk/bangka?expiredTime="
						+ expiredTime + "&securityCode=" + securityCode + "&bankName=" 
						+ bankName + "&cardType=" + URLEncoder.encode("贷记卡", "UTF-8")+ "&bankCard=" + bankCard
						+ "&userName=" + userName + "&phone=" + phone+"&idCard="+idCard
						+ "&ipAddress=" + ip;
				map.put(CommonConstants.RESULT, url);
				return map;
			}else {
				if(!"1".equals(hzhkBankCard.getStatus())) {//1已激活卡，0未激活卡
					map.put(CommonConstants.RESP_CODE, "999996");
					map.put(CommonConstants.RESP_MESSAGE, "需要获取激活短信申请进行激活授权操作");
					String url = ip +"/v1.0/paymentgateway/quick/hzhk/bangka?expiredTime="
							+ expiredTime + "&securityCode=" + securityCode + "&bankName=" 
							+ bankName + "&cardType=" + URLEncoder.encode("贷记卡", "UTF-8")+ "&bankCard=" + bankCard
							+ "&userName=" + userName + "&phone=" + phone+"&idCard="+idCard
							+ "&ipAddress=" + ip;
				map.put(CommonConstants.RESULT, url);
				return map;
			}else {
				map.put(CommonConstants.RESP_CODE, "000000");//已进件，已激活卡
			    map.put(CommonConstants.RESP_MESSAGE, "已激活卡");
				return map;
				}
			}
		}
		}catch (Exception e) {
			   LOG.error("与还款对接接口出现异常======", e);
			   map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			   map.put(CommonConstants.RESP_MESSAGE, "与还款对接失败");
			   return map;
		}
		return map;
}
	
	// 注册进件接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hzhk/register")
	public  @ResponseBody Object HZHKregister(
			@RequestParam(value = "user_name") String userName,
			@RequestParam(value = "id_no") String idCard, 
			@RequestParam(value = "card_no") String bankCard,
			@RequestParam(value = "phone") String phone, 
			@RequestParam(value = "bank_name") String bankName,
			@RequestParam(value = "validity") String expiredTime,
			@RequestParam(value = "cvv2") String securityCode, 
			@RequestParam(value = "rate") String rate, 
			@RequestParam(value = "extraFee") String extraFee, 
			@RequestParam(value = "cardType") String cardType)
			throws Exception {

		LOG.info("开始进入注册进件接口========");

		TreeMap<String, Object> repMap = new TreeMap<String, Object>();
		String expired = this.expiredTimeToMMYY(expiredTime);
		Map<String, Object> map = new HashMap<String, Object>();
		BankNumCode bankNumCode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
		String Branchcode = bankNumCode.getBankBranchcode();    // 联行号
		String bankNumCodebankcode = bankNumCode.getBankCode(); // 银行缩写
		String bankNumCodebankName = bankNumCode.getBankName(); // 银行名
		LOG.info("联行号为：" + Branchcode);
		LOG.info("银行缩写为：" + bankNumCodebankcode);
		LOG.info("银行名为：" + bankNumCodebankName);
		
	    	 if (cardType.contains("借记")) {
					cardType = "1";// 1 储蓄卡
				}else {
					cardType = "6";// 6 信用卡
				}
		LOG.info("入网的卡类型：" + cardType + "-------------------------------");

		String rate1 = new BigDecimal(rate).multiply(new BigDecimal("100")).toString();
		LOG.info("费率金额：" + rate1);
			
		try {
			LOG.info("==============用户进件==============");
			repMap.put("auth_order_no", "Cupid");
			repMap.put("merchant_no", spCode);
			repMap.put("user_name", userName);
			repMap.put("id_no", idCard);
			repMap.put("card_no", bankCard);
			repMap.put("phone", phone);
			repMap.put("bank_name", bankNumCodebankName);    // 银行名
			repMap.put("bank_branch", "上海宝山支行");        // 储蓄卡:银行卡开户支行名称,信用卡:银行卡开户行名称
			repMap.put("bank_code",Branchcode);              // 联行号
			repMap.put("bank_coding", bankNumCodebankcode);  // 银行缩写
			repMap.put("province", "上海市");
			repMap.put("city", "上海市");
			repMap.put("county", "宝山区");
			repMap.put("address", "淞良路");
			repMap.put("validity", expired);  //有效期
			repMap.put("cvv2", securityCode); //安全码
			repMap.put("bank_type", cardType);//银行卡类型 1表示储蓄卡6表示信用卡
			String sign = HttpRequest.getSign(repMap);
			repMap.put("sign", sign);
			LOG.info("======================进件请求参数：" + repMap);

			String result = HttpRequest.sendPost("http://xapi.ypt5566.com/api/User/enterNet",HttpRequest.getUrlParamsByMap(repMap));
			JSONObject resp = JSON.parseObject(result);
			LOG.info("======================进件返回参数：" + resp);
			String Code = resp.getString("Code");
			String Msg = resp.getString("Msg");
			String Respcode = resp.getString("Resp_code");
			String Respmsg = resp.getString("Resp_msg");
			String userNo = resp.getString("user_no");
			
			if ("10000".equals(Code)) {
				if("40000".equals(Respcode)) {
					HZHKRegister saveRegister = new HZHKRegister();
					saveRegister.setBankCard(bankCard);
					saveRegister.setUserName(userName);
					saveRegister.setIdCard(idCard);
					saveRegister.setPhone(phone);
					saveRegister.setBankName(bankNumCodebankName);
					saveRegister.setSecurityCode(securityCode);
					saveRegister.setExpiredTime(expiredTime);
					saveRegister.setBankCode(Branchcode);
					saveRegister.setMerchantNo(userNo);
					saveRegister.setRate(rate1);
					saveRegister.setExtraFee(extraFee);
					topupPayChannelBusiness.createHZHKRegister(saveRegister);
				    LOG.info("------------------------用户进件成功------------------------");

					map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					map.put(CommonConstants.RESP_MESSAGE, "进件成功");
					return map;
				  }else {
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, Respmsg);
					return map;
				  }
				} else {
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, Msg);
					return map;
			} 
		} catch (Exception e) {
			LOG.error("进件接口出现异常======", e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "进件失败");
		}
		return map;
	}

	// 修改个体商户信息接口 （目前只能修改银行开户支行名称，联行号，有效期与安全码）
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hzhk/updateMerInfo")
	public @ResponseBody Object HZHKupdateMerInfo(
			@RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "bankName") String bankName,
			@RequestParam(value = "expiredTime") String expiredTime,
			@RequestParam(value = "bankCode") String bankCode,
			@RequestParam(value = "securityCode") String securityCode)
			throws Exception {
		LOG.info("开始修改个体商户信息接口========");
		
		HZHKRegister hzhkRegister = topupPayChannelBusiness.getHZHKRegisterByidCard(idCard);
		String merchantNo = hzhkRegister.getMerchantNo();
		String phone = hzhkRegister.getPhone();

		BankNumCode bankNumCode = topupPayChannelBusiness.getBankNumCodeByBankName(bankName);
		String Branchcode = bankNumCode.getBankBranchcode();// 联行号
		String bankNumCodebankcode = bankNumCode.getBankCode(); // 银行缩写
		String bankNumCodebankName = bankNumCode.getBankName(); // 银行名
		LOG.info("联行号为：" + Branchcode);
		LOG.info("银行缩写为：" + bankNumCodebankcode);
		LOG.info("银行名为：" + bankNumCodebankName);

		Map<String, Object> map = new HashMap<String, Object>();

		try {
		TreeMap<String, Object> reqMap = new TreeMap<String, Object>();
		reqMap.put("merchant_no", spCode);
		reqMap.put("user_no", merchantNo);
		reqMap.put("bank_branch", bankName);
		reqMap.put("bank_code", Branchcode);
		reqMap.put("bank_phone", phone);
		reqMap.put("validity", expiredTime);
		reqMap.put("cvv2", securityCode);
		String sign = HttpRequest.getSign(reqMap);
		reqMap.put("sign", sign);
		LOG.info("======================修改商户请求参数：" + reqMap);
		
		String result = HttpRequest.sendPost("http://xapi.ypt5566.com/api/User/modifyInfo",HttpRequest.getUrlParamsByMap(reqMap));
		JSONObject resp = JSON.parseObject(result);
		LOG.info("======================修改商户返回参数：" + resp);
		String Code = resp.getString("Code");
		String Msg = resp.getString("Msg");
		String RespCode = resp.getString("Resp_code");
		String RespMsg = resp.getString("Resp_msg");
		if ("10000".equals(Code)) {
			if("40000".equals(RespCode)) {
				hzhkRegister.setSecurityCode(securityCode);
				hzhkRegister.setExpiredTime(expiredTime);
				hzhkRegister.setBankName(bankName);
				hzhkRegister.setBankCode(Branchcode);
			topupPayChannelBusiness.createHZHKRegister(hzhkRegister);
			
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功"); 
			return map;
		} else {
			LOG.info("修改异常-------");
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, RespMsg);
			return map;
		}
		}else {
			LOG.info("修改异常-------");
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, Msg);
			return map;
		}
		} catch (Exception e) {
			LOG.error("修改个体商户信息接口出现异常======", e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "修改失败");
		}
		return map;
	}

	// 获取验证码短信接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hzhk/getsmsCode")
	public  @ResponseBody Object HZHKgetsmsCode(
			@RequestParam(value = "userName") String userName,
			@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "idCard") String idCard) throws Exception {

		LOG.info("开始获取激活短信接口=============");
		Map<String, Object> map = new HashMap<String, Object>();
		HZHKRegister hzhkRegister = topupPayChannelBusiness.getHZHKRegisterByidCard(idCard);
		String merchantno = hzhkRegister.getMerchantNo();
		
		TreeMap<String, Object> reqMap = new TreeMap<String, Object>();
		reqMap.put("merchant_no", spCode);
		reqMap.put("channel_no", "S18HHZTP");
		reqMap.put("business_no", "back_channel");
		reqMap.put("user_no", merchantno);
		String sign = HttpRequest.getSign(reqMap);
		reqMap.put("sign", sign);
		LOG.info("======================获取验证码请求信息" + reqMap);

		try {
			String result = HttpRequest.sendPost("http://xapi.ypt5566.com/api/Repay/entryCard",HttpRequest.getUrlParamsByMap(reqMap));
			JSONObject resp = JSON.parseObject(result);
			LOG.info("======================获取验证码返回信息" + resp);
			String Code = resp.getString("Code");
			String Msg = resp.getString("Msg");
			String Respcode = resp.getString("Resp_code");
			String Respmsg = resp.getString("Resp_msg");
			String requestNo = resp.getString("ypt_order_no");// 激活订单号

			if ("10000".equals(Code)) {
				if ("40000".equals(Respcode)) {
				LOG.info("获取短信成功-------"+Respmsg);
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, requestNo);
				return map;
			} else {
				LOG.info("获取短信失败-------"+Respmsg);
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, Respmsg);
				return map;
			}
		} else {
			LOG.info("获取短信失败-------");
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, Msg);
			return map;
		}
		}catch (Exception e) {
			LOG.error("获取短信异常======");
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "获取短信失败");
			return map;
		}
	}

	// 激活短信接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hzhk/confirmSmsCode")
	public  @ResponseBody Object HZHKconfirmSmsCode(
			@RequestParam(value = "bankCard") String bankCard,
			@RequestParam(value = "userName") String userName,
			@RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "requestNo") String requestNo,
			@RequestParam(value = "smsCode") String smsCode) throws Exception {
		LOG.info("开始进入激活短信接口========");

		HZHKRegister hzhkRegister = topupPayChannelBusiness.getHZHKRegisterByidCard(idCard);
		String merchantNo = hzhkRegister.getMerchantNo();
		String rate = hzhkRegister.getRate();
		String extraFee = hzhkRegister.getExtraFee();

		Map<String, Object> map = new HashMap<String, Object>();
		TreeMap<String, Object> reqMap = new TreeMap<String, Object>();

		reqMap.put("merchant_no", spCode);
		reqMap.put("channel_no", "S18HHZTP");
		reqMap.put("business_no", "back_channel");
		reqMap.put("user_no", merchantNo);
		reqMap.put("rate", rate);
		reqMap.put("single_payment", extraFee);
		reqMap.put("ypt_order_no", requestNo);
		reqMap.put("smsCode", smsCode);
		String sign = HttpRequest.getSign(reqMap);
		reqMap.put("sign", sign);
		LOG.info("======================激活短信请求信息" + reqMap);

		try {
		String result = HttpRequest.sendPost("http://xapi.ypt5566.com/api/Repay/entryCardConfirm",HttpRequest.getUrlParamsByMap(reqMap));
		JSONObject resp = JSON.parseObject(result);
		LOG.info("======================激活短信返回信息" + resp);
		String Code = resp.getString("Code");
		String Msg = resp.getString("Msg");
		String Respcode = resp.getString("Resp_code");
		String Respmsg = resp.getString("Resp_msg");
		String configNo = resp.getString("config_no");// 用户配置号
		LOG.info("用户配置号-----------------------------" + configNo);
		if ("10000".equals(Code)) {
			if("40000".equals(Respcode)) {
				HZHKBindCard hzhkBindCard = topupPayChannelBusiness.getHZHKBindCardByBankCard(bankCard);
				if (hzhkBindCard == null) {
					HZHKBindCard bindCard = new HZHKBindCard();
					bindCard.setIdCard(idCard);
					bindCard.setUserName(userName);
					bindCard.setBankCard(bankCard);
					bindCard.setYptOrderno(requestNo);
					bindCard.setConfigNo(configNo);
					bindCard.setStatus("1");
					topupPayChannelBusiness.createHZHKBindCard(bindCard);
					LOG.info("激活短信成功,配置号：===================="+configNo);

				    map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				    map.put(CommonConstants.RESP_MESSAGE, "短信激活成功");
				    map.put("redirect_url","http://www.shanqi111.cn/v1.0/paymentchannel/topup/wmyk/bindcardsuccess");
				    return map;
				}else {
					hzhkBindCard.setYptOrderno(requestNo);
					hzhkBindCard.setConfigNo(configNo);
					hzhkBindCard.setStatus("1");
					topupPayChannelBusiness.createHZHKBindCard(hzhkBindCard);
				}
		} else {
			LOG.info("激活短信失败====================");
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, Msg);
			return map;
	    }
		}else {
			LOG.info("激活短信失败====================");
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, Respmsg);
			return map;
		}
		}catch (Exception e) {
			LOG.error("激活短信异常====================");
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "激活短信失败");
			return map;
		}
		return map;
	}

	// 快捷接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hzhk/pay")
	public  @ResponseBody Object HZHKpay(
			@RequestParam(value = "orderCode") String orderCode,
			@RequestParam(value = "areaCode") String areaCode) throws Exception {
		LOG.info("开始进入快捷接口======================="+orderCode);
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String bankCard = prp.getBankCard();
		String idCard = prp.getIdCard();
		String amount = prp.getRealAmount();
		String userName = prp.getUserName();
		
		HZHKRegister hzhkRegister = topupPayChannelBusiness.getHZHKRegisterByidCard(idCard);
		HZHKBindCard hzhkBindCard = topupPayChannelBusiness.getHZHKBindCardByBankCard(bankCard);
		String merchantNo = hzhkRegister.getMerchantNo();
		String configNo = hzhkBindCard.getConfigNo(); 
		
		Map<String, Object> map = new HashMap<String, Object>();
		//String orderNo = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());//自定义订单号 长度不小于16
		TreeMap<String, Object> reqMap = new TreeMap<String, Object>();
		reqMap.put("merchant_no", spCode);
		reqMap.put("channel_no", "S18HHZTP");
		reqMap.put("business_no", "back_channel");
		reqMap.put("user_no", merchantNo);
		reqMap.put("config_no", configNo);
		reqMap.put("order_no", orderCode);
		reqMap.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/hzhk/payCallback"); // 异步通知地址
		reqMap.put("price", amount);
		reqMap.put("areaCode", areaCode);
		String sign = HttpRequest.getSign(reqMap);
		reqMap.put("sign", sign);
		LOG.info("======================快捷请求信息" + reqMap);                                                                                                                                                                    

		try {
		String result = HttpRequest.sendPost("http://xapi.ypt5566.com/api/Repay/Pay",HttpRequest.getUrlParamsByMap(reqMap));
		JSONObject resp = JSON.parseObject(result);
		LOG.info("======================快捷返回信息" + resp);                        
		String Code = resp.getString("Code");
		String Msg = resp.getString("Msg");
		String Respcode = resp.getString("Resp_code");
		String Respmsg = resp.getString("Resp_msg");
		String orderno = resp.getString("order_no");
		String yptOrderno = resp.getString("ypt_order_no");

		if ("10000".equals(Code)) {
			if("40000".equals(Respcode)) {
				LOG.info("支付成功===================="+orderno);
				HZHKOrder hzhkOrder = new HZHKOrder();
				hzhkOrder.setYptOrderNo(yptOrderno);
				hzhkOrder.setUserName(userName);
				hzhkOrder.setIdCard(idCard);
				hzhkOrder.setOrderCode(orderCode);
				topupPayChannelBusiness.createHZHKOrder(hzhkOrder);

				map.put(CommonConstants.RESP_CODE, "999998");
				map.put(CommonConstants.RESP_MESSAGE, yptOrderno);
				return map;
			}else {
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, Respmsg);
				return map;
			}
		} else {
			LOG.info("支付失败-------");
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, Msg);
			return map;
		}
		}catch (Exception e) {
			LOG.error("支付接口出现异常======",e);
			map.put(CommonConstants.RESP_CODE, "999998");
			map.put(CommonConstants.RESP_MESSAGE, "请求异常，等待查询");
			return map;
		}
	}

	// 快捷异步通知接口
	@RequestMapping(method = {RequestMethod.POST,RequestMethod.GET },value = "/v1.0/paymentgateway/topup/hzhk/payCallback")
	public void payCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOG.info("快捷异步回调进来了======");
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		
		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}
		
	    String Code = request.getParameter("Code");
		String Msg = request.getParameter("Msg");
		String RespCode = request.getParameter("Resp_code");
		String Respmsg = request.getParameter("Resp_msg");
		String yptOrderNo = request.getParameter("ypt_order_no");//洋仆淘订单号
		String orderNo = request.getParameter("order_no");//渠道商订单号
		
		LOG.info("Code=============="+Code);
		LOG.info("Msg=============="+Msg);
		LOG.info("RespCode=============="+RespCode);
		LOG.info("Respmsg=============="+Respmsg);
		LOG.info("洋仆淘订单号yptOrderNo=============="+yptOrderNo);
		LOG.info("渠道商订单号orderNo=============="+orderNo);
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderNo);
	    try {
		RestTemplate restTemplate = new RestTemplate();
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		String result = null;
		String url = null;
		
		if ("10000".equals(Code)) {
			if ("40000".equals(RespCode)) {
				LOG.info("快捷支付交易成功==============");
				
				url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
				//url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";
				requestEntity.add("status", "1");
				requestEntity.add("order_code", orderNo);
				requestEntity.add("third_code", yptOrderNo);

				try {
					result = restTemplate.postForObject(url, requestEntity, String.class);
					LOG.info("RESULT================" + result);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("",e);
				}

				LOG.info("修改订单状态成功---------：" + orderNo);
				LOG.info("订单支付成功!");
				PrintWriter writer = response.getWriter();
				writer.print("success");
				writer.close();
			}else {
				LOG.info("订单支付失败!");
				PrintWriter writer = response.getWriter();
				writer.print("failed");
				writer.close();
			}
		}else{
			LOG.info("订单支付失败!");
			PrintWriter writer = response.getWriter();
			writer.print("failed");
			writer.close();
		}
		}catch (Exception e) {
			LOG.error("", e);
		}
		
	}

	// 代还接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hzhk/repayment")
	public @ResponseBody Object HZHKrepayment(@RequestParam(value = "orderCode") String orderCode) throws Exception {
		LOG.info("开始进入代还接口==========================="+orderCode);
		
		Map<String, Object> map = new HashMap<String, Object>();
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		String idCard = prp.getIdCard();
		String amount = prp.getRealAmount();
		String bankCard = prp.getBankCard();
		String userName = prp.getUserName();

		HZHKRegister hzhkRegister = topupPayChannelBusiness.getHZHKRegisterByidCard(idCard);
		HZHKBindCard hzhkBindCard = topupPayChannelBusiness.getHZHKBindCardByBankCard(bankCard);
		String configNo = hzhkBindCard.getConfigNo();
		String merchantNo = hzhkRegister.getMerchantNo();
		
		try {
		//String orderNo = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());//自定义订单号 长度不小于16
		TreeMap<String, Object> reqMap = new TreeMap<String, Object>();
		reqMap.put("merchant_no", spCode);
		reqMap.put("channel_no", "S18HHZTP");
		reqMap.put("business_no", "back_channel");
		reqMap.put("user_no", merchantNo);
		reqMap.put("config_no", configNo);
		reqMap.put("order_no", orderCode);
		reqMap.put("notifyUrl", ip + "/v1.0/paymentgateway/topup/hzhk/repaymentCallback");
		reqMap.put("price", amount);
		String sign = HttpRequest.getSign(reqMap);
		reqMap.put("sign", sign);
		LOG.info("======================代还请求信息" + reqMap);

		String result = HttpRequest.sendPost("http://xapi.ypt5566.com/api/Repay/insteadPay",HttpRequest.getUrlParamsByMap(reqMap));
		JSONObject resp = JSON.parseObject(result);
		LOG.info("======================代还返回信息" + resp);
		String Code = resp.getString("Code");
		String Msg = resp.getString("Msg");
		String Respcode = resp.getString("Resp_code");
		String Respmsg = resp.getString("Resp_msg");
		String orderno = resp.getString("order_no");
		String yptOrderno = resp.getString("ypt_order_no");
		
		
		if ("10000".equals(Code)) {
			if("40000".equals(Respcode)) {
				LOG.info("=======================代还成功"+orderno);
				HZHKOrder hzhkOrder = new HZHKOrder();
				hzhkOrder.setReyptOrderNo(yptOrderno);
				hzhkOrder.setUserName(userName);
				hzhkOrder.setIdCard(idCard);
				hzhkOrder.setOrderCode(orderCode);
				topupPayChannelBusiness.createHZHKOrder(hzhkOrder);
					
				map.put(CommonConstants.RESP_CODE, "999998");
				map.put(CommonConstants.RESP_MESSAGE, yptOrderno);
				return map;
			
		}else{
			LOG.info("代还失败===================="+orderno);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, Respmsg);
			return map;
		}
		}else{
			LOG.info("代还失败===================="+orderno);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, Respmsg);
			return map;
		}
		}catch (Exception e) {
			LOG.error("代还接口出现异常======",e);
			map.put(CommonConstants.RESP_CODE, "999998");
			map.put(CommonConstants.RESP_MESSAGE, "请求异常，等待查询");
			return map;
		}
	}
	
	// 代还异步通知接口
	@RequestMapping(method = { RequestMethod.POST,RequestMethod.GET },value = "/v1.0/paymentgateway/topup/hzhk/repaymentCallback")
	public void repaymentCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {

		LOG.info("代还异步回调进来了==================");

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		
		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			String[] strings = parameterMap.get(key);
			for (String s : strings) {
				LOG.info(key + "=============" + s);
			}
		}
		
	    String Code = request.getParameter("Code");
		String Msg = request.getParameter("Msg");
		String RespCode = request.getParameter("Resp_code");
		String Respmsg = request.getParameter("Resp_msg");
		String yptOrderNo = request.getParameter("ypt_order_no");//洋仆淘订单号
		String orderNo = request.getParameter("order_no");//渠道商订单号
		
		LOG.info("Code=============="+Code);
		LOG.info("Msg=============="+Msg);
		LOG.info("RespCode=============="+RespCode);
		LOG.info("Respmsg=============="+Respmsg);
		LOG.info("洋仆淘订单号yptOrderNo=============="+yptOrderNo);
		LOG.info("渠道商订单号orderNo=============="+orderNo);
		
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderNo);
	    try {
		RestTemplate restTemplate = new RestTemplate();
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		String result = null;
		String url = null;
		JSONObject jsonObject;
		JSONObject resultObj;
		
		if ("10000".equals(Code)) {
			if ("40000".equals(RespCode)) {
				LOG.info("代还成功======");
				
				url = prp.getIpAddress()+ChannelUtils.getCallBackUrl(prp.getIpAddress());
				//url = prp.getIpAddress() + "/v1.0/transactionclear/payment/update";
				requestEntity.add("status", "1");
				requestEntity.add("order_code", orderNo);
				requestEntity.add("third_code", yptOrderNo);

				try {
					result = restTemplate.postForObject(url, requestEntity, String.class);
					LOG.info("RESULT================" + result);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("",e);
				}

				LOG.info("修改订单状态成功---------：" + orderNo);
				LOG.info("订单支付成功!");
				PrintWriter writer = response.getWriter();
				writer.print("success");
				writer.close();
			}else {
				LOG.info("订单支付失败!");
				PrintWriter writer = response.getWriter();
				writer.print("failed");
				writer.close();
			}
		}else{
			LOG.info("订单支付失败!");
			PrintWriter writer = response.getWriter();
			writer.print("failed");
			writer.close();
		}
		}catch (Exception e) {
			LOG.error("", e);
		}
		
	}


	// 查询接口 (快捷根据YPT订单号来查)
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hzhk/payquery")
	public @ResponseBody Object HZHKpayquery(HttpServletRequest request,
		   @RequestParam(value = "orderCode") String orderCode) throws Exception {
		LOG.info("开始进入快捷查询接口========快捷订单号orderCode为：" +orderCode);

		Map<String, Object> map = new HashMap<String, Object>();
		
		HZHKOrder hzhkOrder = topupPayChannelBusiness.getHZHKOrderByorderCode(orderCode);
		String yptOrder = hzhkOrder.getYptOrderNo();
		
		TreeMap<String, Object> reqMap = new TreeMap<String, Object>();
		reqMap.put("merchant_no", spCode);
		reqMap.put("channel_no", "S18HHZTP");
		reqMap.put("business_no", "back_channel");
		reqMap.put("ypt_order_no", yptOrder);
		String sign = HttpRequest.getSign(reqMap);
		reqMap.put("sign", sign);
		LOG.info("请求信息" + reqMap);

		try {
			String result = HttpRequest.sendPost("http://xapi.ypt5566.com/api/Repay/checkOrder",HttpRequest.getUrlParamsByMap(reqMap));
			JSONObject resp = JSON.parseObject(result);
			String Code = resp.getString("Code");
			String Msg = resp.getString("Msg");
			String Respcode = resp.getString("Resp_code");
			String Respmsg = resp.getString("Resp_msg");
			String orderNo = resp.getString("order_no");
			String yptOrderno = resp.getString("ypt_order_no");
			LOG.info("自定义商户订单号========" + orderNo);
			LOG.info("YPT订单号========" + yptOrderno);

			if ("10000".equals(Code)) {
				if("40000".equals(Respcode)) {
					LOG.info("查询成功，订单号为：===================="+orderNo);
					map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					map.put(CommonConstants.RESP_MESSAGE, orderNo);
					return map;
			} else {
				LOG.info("查询失败：-------"+Respmsg);
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, Respmsg);
				return map;
			}
			}else {
				LOG.info("查询失败:"+Msg);
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, Msg);
				return map;
			}
		} catch (Exception e) {
			LOG.error("查询异常======", e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败");
			return map;
		}
	}
	
	// 查询接口 (代还根据YPT订单号来查)
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hzhk/repayquery")
	public @ResponseBody Object HZHKrepayquery(HttpServletRequest request,
		     @RequestParam(value = "orderCode") String orderCode) throws Exception {
		LOG.info("开始进入代付查询接口========代付订单号orderCode为：" +orderCode);

		Map<String, Object> map = new HashMap<String, Object>();
		HZHKOrder hzhkOrder = topupPayChannelBusiness.getHZHKOrderByorderCode(orderCode);
		String reyptOrder = hzhkOrder.getReyptOrderNo();
		
		TreeMap<String, Object> reqMap = new TreeMap<String, Object>();
		reqMap.put("merchant_no", spCode);
		reqMap.put("channel_no", "S18HHZTP");
		reqMap.put("business_no", "back_channel");
		reqMap.put("ypt_order_no", reyptOrder);
		String sign = HttpRequest.getSign(reqMap);
		reqMap.put("sign", sign);
		LOG.info("请求信息" + reqMap);

		try {
			String result = HttpRequest.sendPost("http://xapi.ypt5566.com/api/Repay/checkOrder",HttpRequest.getUrlParamsByMap(reqMap));
			JSONObject resp = JSON.parseObject(result);
			String Code = resp.getString("Code");
			String Msg = resp.getString("Msg");
			String Respcode = resp.getString("Resp_code");
			String Respmsg = resp.getString("Resp_msg");
			String orderNo = resp.getString("order_no");
			String yptOrderno = resp.getString("ypt_order_no");
			LOG.info("自定义商户订单号========" + orderNo);
			LOG.info("YPT订单号========" + yptOrderno);

			if ("10000".equals(Code)) {
				if("40000".equals(Respcode)) {
					LOG.info("查询成功，订单号为：===================="+orderNo);
					map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
					map.put(CommonConstants.RESP_MESSAGE, orderNo);
					return map;
			} else {
				LOG.info("查询失败：-------"+Respmsg);
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, Respmsg);
				return map;
			}
			}else {
				LOG.info("查询失败:"+Msg);
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, Msg);
				return map;
			}
		} catch (Exception e) {
			LOG.error("查询异常======", e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询失败");
			return map;
		}
	}

	// 商户余额查询接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hzhk/queryBalance")
	public @ResponseBody Object HZHKqueryBalance(HttpServletRequest request,
			@RequestParam(value = "idCard") String idCard) throws Exception {
		LOG.info("开始进入商户余额查询接口========");

		Map<String, Object> map = new HashMap<String, Object>();
		HZHKRegister hzhkRegister = topupPayChannelBusiness.getHZHKRegisterByidCard(idCard);
		String bankCard = hzhkRegister.getBankCard();
		HZHKBindCard hzhkBankCard = topupPayChannelBusiness.getHZHKBindCardByBankCard(bankCard);
		String configNo = hzhkBankCard.getConfigNo();

		TreeMap<String, Object> reqMap = new TreeMap<String, Object>();
		reqMap.put("merchant_no", spCode);
		reqMap.put("channel_no", "S18HHZTP");
		reqMap.put("business_no", "back_channel");
		reqMap.put("config_no", configNo);
		String sign = HttpRequest.getSign(reqMap);
		reqMap.put("sign", sign);
		LOG.info("请求信息" + reqMap);

		String result = HttpRequest.sendPost("http://xapi.ypt5566.com/api/Repay/queryCustomBalance",
				HttpRequest.getUrlParamsByMap(reqMap));
		JSONObject resp = JSON.parseObject(result);
		String Code = resp.getString("Code");
		String Msg = resp.getString("Msg");
		String balance = resp.getString("balance");

		if ("10000".equals(Code)) {
			LOG.info("余额查询====================" + balance);
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, balance);
			return map;
		} else {
			LOG.info("余额查询异常-------" + Msg);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, Msg);
			return map;
		}
	}

	// 修改费率接口（手续费是由费率修改来的）
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/hzhk/updateRate")
	public @ResponseBody Object HZHKupdateRate(
			@RequestParam(value = "idCard") String idCard,
			@RequestParam(value = "bankCard") String bankCard)
			throws Exception {
		LOG.info("开始进入修改费率接口========");

		HZHKRegister hzhkRegister = topupPayChannelBusiness.getHZHKRegisterByidCard(idCard);
		HZHKBindCard hzhkBindCard = topupPayChannelBusiness.getHZHKBindCardByBankCard(bankCard);
		String configNo = hzhkBindCard.getConfigNo();
		String rate = hzhkRegister.getRate();
		String extraFee = hzhkRegister.getExtraFee();
		Map<String, Object> map = new HashMap<String, Object>();

		TreeMap<String, Object> reqMap = new TreeMap<String, Object>();
		reqMap.put("merchant_no", spCode);
		reqMap.put("channel_no", "S18HHZTP");
		reqMap.put("business_no", "back_channel");
		reqMap.put("config_no", configNo);
		reqMap.put("rate", rate);
		reqMap.put("single_payment", extraFee);
		String sign = HttpRequest.getSign(reqMap);
		reqMap.put("sign", sign);
		LOG.info("请求信息" + reqMap);

		try {
		String result = HttpRequest.sendPost("http://xapi.ypt5566.com/api/Repay/setUserConfigRate",HttpRequest.getUrlParamsByMap(reqMap));
		JSONObject resp = JSON.parseObject(result);
		LOG.info("返回信息" + resp);
		String Code = resp.getString("Code");
		String Msg = resp.getString("Msg");
		String Respcode = resp.getString("Resp_code");
		String Respmsg = resp.getString("Resp_msg");

		if ("10000".equals(Code)) {
			if("40000".equals(Respcode)) {
				hzhkRegister.setRate(rate);
				hzhkRegister.setExtraFee(extraFee);
				topupPayChannelBusiness.createHZHKRegister(hzhkRegister);
				
				LOG.info("修改费率成功：====================费率为：" + rate);
				map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
				map.put(CommonConstants.RESP_MESSAGE, rate);
				return map;
			}else {
				LOG.info("修改费率失败：====================" +Respmsg);
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, Respmsg);
				return map;
			}
		} else {
			LOG.info("修改费率失败-------" + Respmsg);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, Msg);
			return map;
		}
		}catch (Exception e) {
			LOG.error("修改费率异常=====", e);
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "修改费率失败");
			return map;
		}
	}
	
	// 查询所有的省份、直辖市、自治区
	@RequestMapping(method = RequestMethod.POST, value = ("/v1.0/paymentgateway/topup/hzhk/province/queryall"))
	public @ResponseBody Object findProvince() {
		LOG.info("开始进入查询所有的省份接口=================================");

		Map map = new HashMap();
		List<HZHKCode> list = topupPayChannelBusiness.findHZHKCodeProvince();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, list);		
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}

	// 查询指定区域的下级城市
	@RequestMapping(method = RequestMethod.POST, value = ("/v1.0/paymentgateway/topup/hzhk/city/queryall"))
	public @ResponseBody Object findCity(@RequestParam(value = "provinceId")String provinceId) {
		LOG.info("开始进入查询指定区域的下级城市区接口=================================");

		Map map = new HashMap();
		List<HZHKCode> list = topupPayChannelBusiness.findHZHKCodeCity(provinceId);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, list);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}
	
	// 跳转绑卡页面
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentgateway/quick/hzhk/bangka")
	public String returnCJBindCard(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		LOG.info("/v1.0/paymentgateway/quick/hzhk/bangka=========tohzhkbindcard");

		String expiredTime = request.getParameter("expiredTime");
		String securityCode = request.getParameter("securityCode");
		String bankName = request.getParameter("bankName");
		String cardType = request.getParameter("cardType");
		String bankCard = request.getParameter("bankCard");
		String ipAddress = request.getParameter("ipAddress");
		String userName = request.getParameter("userName");
		String phone = request.getParameter("phone");
		String idCard = request.getParameter("idCard");
		
		model.addAttribute("expiredTime", expiredTime);
		model.addAttribute("securityCode", securityCode);
		model.addAttribute("bankName", bankName);
		model.addAttribute("cardType", cardType);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("ipAddress", ipAddress);
		model.addAttribute("userName", userName);
		model.addAttribute("phone", phone);
		model.addAttribute("idCard", idCard);
		return "hzhkbindcard";
	}
}
