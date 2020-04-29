package com.jh.paymentgateway.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.impl.cookie.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.Maps;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.XSAccount;
import com.jh.paymentgateway.pojo.XSBindCard;
import com.jh.paymentgateway.pojo.XSHKProvince;
import com.jh.paymentgateway.pojo.XSRegister;
import com.jh.paymentgateway.util.xs.Config;
import com.jh.paymentgateway.util.xs.Md5Utils;
import com.jh.paymentgateway.util.xs.SdkRequest;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import net.sf.json.JSONObject;

@Controller
public class XSpageRequest extends BaseChannel {
	
	private static final Logger LOG = LoggerFactory.getLogger(XSpageRequest.class);
	
	private RSAPrivateKey privateKey;
	
	@Value("${payment.ipAddress}")
	private String paymentGatewayIp;
	
	@Autowired
	private RedisUtil redisUtil;
	
	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;
	
	private static final String ENVID = "2";
	

	private RSAPrivateKey getPrivateKey() {
		if (privateKey == null) {
			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(Config.PRIVATE_PATH));
				privateKey = (RSAPrivateKey) ois.readObject();
				ois.close();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		return privateKey;
	}
	
	/**
	 * 还款验证是否开户和绑卡接口
	 * @param userName
	 * @param phone
	 * @param idcard
	 * @param bankCard
	 * @param securityCode
	 * @param expiredTime
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentgateway/xs/repayment/isbindcard")
	public @ResponseBody Object isOpenAccountAndBindCard(
			String userName,
			String phone,
			String idCard,
			String bankCard,
			String securityCode,
			String expiredTime
			) {
		XSAccount xsAccount = topupPayChannelBusiness.findXSAccountByIdCard(idCard);
		if (xsAccount == null) {
			Map<String, Object> hkOpenAccount = this.hkOpenAccount(idCard, userName);
			if (!CommonConstants.SUCCESS.equals(hkOpenAccount.get(CommonConstants.RESP_CODE))) {
				return hkOpenAccount;
			}
			xsAccount = (XSAccount) hkOpenAccount.get(CommonConstants.RESULT);
		}
		
		XSBindCard xsBindCard = topupPayChannelBusiness.findByXSBindCardByCardNo(bankCard);
		if (xsBindCard == null) {
			Map<String, Object> hkBindCard = this.hkBindCard(bankCard, userName, phone, idCard, securityCode, expiredTime);
			if (!CommonConstants.SUCCESS.equals(hkBindCard.get(CommonConstants.RESP_CODE))) {
				return hkBindCard;
			}
			xsBindCard = (XSBindCard) hkBindCard.get(CommonConstants.RESULT);
		}
		
		return ResultWrap.init(CommonConstants.SUCCESS, "验证成功");
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentgateway/xs/repayment/query/province")
	public @ResponseBody Object queryHKProvince(@RequestParam(required=false)String province) {
		List<XSHKProvince> xsHKProvinceRepositorys = null;
		List<String> provinces = new ArrayList<>();
		if (province == null || "".equals(province.trim()) || "null".equalsIgnoreCase(province.trim())) {
			xsHKProvinceRepositorys = topupPayChannelBusiness.findXSHKProvince();
			for (XSHKProvince xsHKProvince : xsHKProvinceRepositorys) {
				provinces.add(xsHKProvince.getProvince());
			}
		}else {
			xsHKProvinceRepositorys = topupPayChannelBusiness.findXSHKProvinceByProvince(province);
			for (XSHKProvince xsHKProvince : xsHKProvinceRepositorys) {
				provinces.add(xsHKProvince.getCity());
			}
		}
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功",provinces);
	}
	
	/**
	 * 还款账户余额查询接口
	 * @param idCard
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentgateway/xs/repayment/query/balance")
	public @ResponseBody Object queryAccountBalance(String idCard) {
		return this.hkAccountQuery(idCard);
	}
	
	
	/**
	 * 还款账户余额查询方法
	 * @param idCard
	 * @return
	 * <p>Description: </p>
	 */
	private Map<String,Object> hkAccountQuery(String idCard){
		XSAccount account = topupPayChannelBusiness.findXSAccountByIdCard(idCard);
		 // 业务接口
        String ACTION = "SdkAccountBalance";
        // 发送请求
        Map<String, String> data = Maps.newHashMap();
        data.put("accountCode", account.getAccountCode());
        String resultString = null;
        // 发送请求
        try {
			resultString = SdkRequest.requestAction(ACTION, data, this.getPrivateKey());
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("", e);
			return ResultWrap.err(LOG, CommonConstants.FALIED, "请求异常,请稍后重试!");
		}
        JSONObject resultJSON = JSONObject.fromObject(resultString);
		String code = resultJSON.getString("code");
		String msg = resultJSON.getString("msg");
		if ("000000".equals(code)) {
			String balanceRd = resultJSON.getString("balanceRd");
			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功",new BigDecimal(balanceRd).divide(BigDecimal.valueOf(100),2,BigDecimal.ROUND_DOWN));
		}
		return ResultWrap.err(LOG, CommonConstants.FALIED, msg);
	}
	
	
	/**
	 * 还款开户接口
	 * @param idCard
	 * @param userName
	 * @return
	 * <p>Description: </p>
	 */
	private Map<String,Object> hkOpenAccount(String idCard, String userName){
		 // 业务接口
        String ACTION = "SdkUserChannelAccountOpen";
        // 发送请求
        Map<String, String> data = Maps.newHashMap();
        data.put("userCert", idCard);
        data.put("userAccount", userName);
        data.put("channelId", ENVID);
        String resultString = null;
        // 发送请求
        try {
			resultString = SdkRequest.requestAction(ACTION, data, this.getPrivateKey());
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("", e);
			return ResultWrap.err(LOG, CommonConstants.FALIED, "请求异常,请稍后重试!");
		}
        JSONObject resultJSON = JSONObject.fromObject(resultString);
		String code = resultJSON.getString("code");
		String msg = resultJSON.getString("msg");
		if ("000000".equals(code)) {
			String accountCode = resultJSON.getString("accountCode");
			XSAccount xsAccount = new XSAccount();
			xsAccount.setAccountCode(accountCode);
			xsAccount.setIdCard(idCard);
			xsAccount.setUserName(userName);
			xsAccount.setCreateTime(new Date());
			xsAccount = topupPayChannelBusiness.saveXSAccount(xsAccount);
			return ResultWrap.init(CommonConstants.SUCCESS, "开户成功",xsAccount);
		}
		return ResultWrap.err(LOG, CommonConstants.FALIED, msg);
	}
	
	/**
	 * 还款绑卡接口
	 * @param cardNo
	 * @param userName
	 * @param cardPhone
	 * @param idCard
	 * @param securityCode
	 * @param expiredTime
	 * @return
	 * <p>Description: </p>
	 */
	private Map<String,Object> hkBindCard(String cardNo,String userName,String cardPhone,String idCard,String securityCode,String expiredTime){
		// 业务接口
        String ACTION = "CreditBindUnion";
        // 发送请求
        Map<String, String> data = Maps.newHashMap();
        data.put("linkId", DateUtils.formatDate(new Date(), "yyyyMMddHHmmss"));
        data.put("cardNo", cardNo);
        data.put("cardAccount", userName);
        data.put("cardPhone", cardPhone);
        data.put("cardCert", idCard);
        data.put("cardYxq", this.expiredTimeToMMYY(expiredTime));
        data.put("cardCvv", securityCode);
//        data.put("notifyUrl", "http://www.baidu.com");
//        data.put("frontUrl", "http://www.baidu.com");
        data.put("channelId", ENVID);
        String resultString = null;
        try {
			resultString = SdkRequest.requestAction(ACTION, data, this.getPrivateKey());
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("", e);
			return ResultWrap.err(LOG, CommonConstants.FALIED, "请求异常,请稍后重试!");
		}
        JSONObject resultJSON = JSONObject.fromObject(resultString);
		String code = resultJSON.getString("code");
		String msg = resultJSON.getString("msg");
		if ("000000".equals(code)) {
			String bindStatus = resultJSON.getString("bindStatus");
			String bindAgreeNo = resultJSON.getString("bindAgreeNo");
			if (!"0".equals(bindStatus)) {
				XSBindCard xsBindCard = new XSBindCard();
				xsBindCard.setCardNo(cardNo);
				xsBindCard.setUserName(userName);
				xsBindCard.setCardPhone(cardPhone);
				xsBindCard.setIdCard(idCard);
				xsBindCard.setBindAgreeNo(bindAgreeNo);
				xsBindCard.setCreateTime(new Date());
				xsBindCard = topupPayChannelBusiness.saveXSBindCard(xsBindCard);
				return ResultWrap.init(CommonConstants.SUCCESS, "绑卡成功",xsBindCard);
			}else {
				String bindUrl = resultJSON.getString("bindUrl");
				return ResultWrap.init("999996", "需要绑卡",bindUrl);
			}
		}
		
		if ("13180401".equals(code)) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, "请求授权过于频繁,请1分钟后重试");
		}
		
		return ResultWrap.err(LOG, CommonConstants.FALIED, msg);
	}
	
	/**
	 * 还款消费支付接口
	 * @param bean
	 * @return
	 * <p>Description: </p>
	 */
	private Map<String,Object> hkConsumePay(PaymentRequestParameter bean){
		XSAccount xsAccount = topupPayChannelBusiness.findXSAccountByIdCard(bean.getIdCard());
		XSBindCard xsBindCard = topupPayChannelBusiness.findByXSBindCardByCardNo(bean.getBankCard());
		String extra = bean.getExtra();
		if (extra == null || !extra.contains("-")) {
			extra = "上海市-上海市";
		}else {
			extra = extra.substring(extra.indexOf("|")+1, extra.length());
		}
		// 业务接口
        String ACTION = "CreditAccountPlanOrder";
        // 发送请求
        Map<String, String> data = Maps.newHashMap();
        data.put("linkId", bean.getOrderCode());
        data.put("accountCode", xsAccount.getAccountCode());
        data.put("bindAgreeNo", xsBindCard.getBindAgreeNo());
        data.put("orderAmount", new BigDecimal(bean.getAmount()).multiply(BigDecimal.valueOf(100)).intValue()+"");
        data.put("payRate", new BigDecimal(bean.getRate()).multiply(BigDecimal.valueOf(100)).setScale(2, BigDecimal.ROUND_UP)+"");
        data.put("notifyUrl", paymentGatewayIp+"/v1.0/paymentgateway/quick/tx/notify");
        data.put("goodsName", "充值缴费");
        data.put("areas", extra);
        String resultString = null;
        try {
			resultString = SdkRequest.requestAction(ACTION, data, this.getPrivateKey());
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("", e);
			return ResultWrap.err(LOG, "999998", "请求异常,请稍后重试!");
		}
        JSONObject resultJSON = JSONObject.fromObject(resultString);
		String code = resultJSON.getString("code");
		String msg = resultJSON.getString("msg");
		if ("000000".equals(code)) {
			String orderNo = resultJSON.getString("orderNo");
			this.updatePaymentOrderThirdOrder(bean.getIpAddress(), bean.getOrderCode(), orderNo);
			return ResultWrap.init("999998", "支付处理中");
		}
		return ResultWrap.err(LOG, CommonConstants.FALIED, msg);
	}
	
	/**
	 * 还款代付支付接口
	 * @param bean
	 * @return
	 * <p>Description: </p>
	 */
	private Map<String,Object> hkRepaymentPay(PaymentRequestParameter bean){
		XSAccount xsAccount = topupPayChannelBusiness.findXSAccountByIdCard(bean.getIdCard());
		String expiredTime = bean.getExpiredTime();
		String securityCode = bean.getSecurityCode();
		
		// 业务接口
        String ACTION = "CreditAccountPlanSettle";
        // 发送请求
        Map<String, String> data = Maps.newHashMap();
        data.put("linkId", bean.getOrderCode());
        data.put("accountCode", xsAccount.getAccountCode());
        data.put("settleAmount", new BigDecimal(bean.getRealAmount()).multiply(BigDecimal.valueOf(100)).intValue()+"");
        data.put("payCharge", new BigDecimal(bean.getExtraFee()).multiply(BigDecimal.valueOf(100)).intValue()+"");
        data.put("notifyUrl", paymentGatewayIp+"/v1.0/paymentgateway/repayment/hk/notify");
        data.put("cardNo", bean.getBankCard());
        data.put("cardAccount", bean.getUserName());
        data.put("cardPhone", bean.getCreditCardPhone());
        data.put("cardCert", bean.getIdCard());
        if (expiredTime != null && !"".equals(expiredTime) && !"null".equals(expiredTime)) {
            data.put("cardYxq", this.expiredTimeToMMYY(expiredTime));
		}
        if (securityCode != null && !"".equals(securityCode) && !"null".equals(securityCode)) {
            data.put("cardCvv", securityCode);
		}
        data.put("goodsName", "充值缴费");
        String resultString = null;
        try {
			resultString = SdkRequest.requestAction(ACTION, data, this.getPrivateKey());
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("", e);
			return ResultWrap.err(LOG, "999998", "请求异常,请稍后重试!");
		}
        JSONObject resultJSON = JSONObject.fromObject(resultString);
		String code = resultJSON.getString("code");
		String msg = resultJSON.getString("msg");
		if ("000000".equals(code)) {
			String settleNo = resultJSON.getString("settleNo");
			if (null != bean.getIpAddress()) {
				this.updatePaymentOrderThirdOrder(bean.getIpAddress(), bean.getOrderCode(), settleNo);
			}
			return ResultWrap.init("999998", "支付处理中");
		}
		return ResultWrap.err(LOG, CommonConstants.FALIED, msg);
	}
	
	@RequestMapping(value="/v1.0/paymentgateway/repayment/hk/repaymented/to/debitecard")
	public @ResponseBody Object repaymentToDebiteCard(
			@RequestParam()String amount,
			@RequestParam()String idCard,
			@RequestParam()String bankCard,
			@RequestParam()String bankCardPhone,
			@RequestParam(defaultValue="1")String extraFee
			) {
		PaymentRequestParameter bean = new PaymentRequestParameter();
		bean.setRealAmount(amount);
		bean.setIdCard(idCard);
		bean.setBankCard(bankCard);
		bean.setCreditCardPhone(bankCardPhone);
		bean.setExtraFee(extraFee);
		XSAccount xsAccount = topupPayChannelBusiness.findXSAccountByIdCard(bean.getIdCard());
		bean.setUserName(xsAccount.getUserName());
		bean.setOrderCode(DateUtil.getDateStringConvert(new String(),new Date(), "yyyyMMddHHmmssSSS")+"1");
		Map<String, Object> hkRepaymentPay = this.hkRepaymentPay(bean);
		hkRepaymentPay.put("orderCode", bean.getOrderCode());
		return hkRepaymentPay;
	}
	
	
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/paymentgateway/repayment/hk/notify")
	public @ResponseBody Object xsRepaymentHkNotify(HttpServletRequest request,HttpServletResponse response,
			String linkId,
			String settleNo,
			String settleStatus,
			String settleMemo,
			String sign
			) {
		LOG.info("异步回调进来了");
		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			LOG.info(key+"====="+parameterMap.get(key)[0]);
		}
		
		String md5 = Md5Utils.getMd5(settleNo+settleStatus+Config.KEY);
		if (!md5.equals(sign)) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, "验签失败");
		}
		
		PaymentRequestParameter bean = redisUtil.getPaymentRequestParameter(linkId);
		if ("20".equals(settleStatus)) {
			this.updateSuccessPaymentOrder(bean.getIpAddress(), linkId);
			this.notifyCardManager(bean.getIpAddress(), linkId);
			try {
				response.getWriter().println("success");
			} catch (IOException e) {
				e.printStackTrace();
				return "success";
			}
			return null;
		}
		this.addOrderCauseOfFailure(linkId, settleMemo, bean.getIpAddress());
		return ResultWrap.err(LOG, CommonConstants.FALIED, linkId + "非成功回调",settleMemo);
	}
	
	
	private void notifyCardManager(String ipAddress,String orderCode) {
		String url = ipAddress+"/v1.0/creditcardmanager/update/taskstatus/by/ordercode";
		MultiValueMap<String,String> multiValueMap = new LinkedMultiValueMap<String, String>();
		multiValueMap.add("orderCode", orderCode);
		multiValueMap.add("version", "14");
		try {
			String result = new RestTemplate().postForObject(url, multiValueMap, String.class);
			LOG.info(result);
		} catch (RestClientException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * tx快捷注册接口
	 * @param bean
	 * @return
	 * <p>Description: </p>
	 */
	public Map<String, Object> xsRegister(PaymentRequestParameter bean) {
		 // 业务接口
        String ACTION = "SdkUserStoreBind";
        // 发送请求
        Map<String, String> data = Maps.newHashMap();
        data.put("userId", bean.getUserId());
        data.put("userName", bean.getUserName());
        data.put("userNick", bean.getUserName());
        data.put("userPhone", bean.getCreditCardPhone());
        data.put("userAccount", bean.getUserName());
        data.put("userCert", bean.getIdCard());
        data.put("userEmail", "q355023989@qq.com");
        data.put("userAddress", "上海市宝山区逸仙路2816号");
        data.put("userMemo", "上海莘丽");
        data.put("channelId", ENVID);

        data.put("settleBankNo", bean.getDebitCardNo());
        data.put("settleBankPhone", bean.getDebitPhone());
        data.put("settleBankCnaps", "102345023130");
        String resultString = null;
        // 发送请求
        try {
			resultString = SdkRequest.requestAction(ACTION, data, this.getPrivateKey());
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("", e);
			return ResultWrap.err(LOG, CommonConstants.FALIED, "请求异常,请稍后重试!");
		}
		JSONObject resultJSON = JSONObject.fromObject(resultString);
		String code = resultJSON.getString("code");
		String msg = resultJSON.getString("msg");
		if ("000000".equals(code)) {
			String userCode = resultJSON.getString("userCode");
			String userKey = resultJSON.getString("userKey");
			XSRegister xSRegister = new XSRegister();
			xSRegister.setUserId(bean.getUserId());
			xSRegister.setUserAccount(bean.getUserName());
			xSRegister.setUserPhone(bean.getCreditCardPhone());
			xSRegister.setUserAccount(bean.getUserName());
			xSRegister.setIdCard(bean.getIdCard());
			xSRegister.setSettleBankNo(bean.getDebitCardNo());
			xSRegister.setSettleBankPhone(bean.getDebitPhone());
			xSRegister.setUserCode(userCode);
			xSRegister.setUserKey(userKey);
			xSRegister.setCreateTime(new Date());
			xSRegister = topupPayChannelBusiness.saveXSRegister(xSRegister);
			return ResultWrap.init(CommonConstants.SUCCESS, "注册成功",xSRegister);
		}else {
			return ResultWrap.err(LOG, CommonConstants.FALIED, msg);
		}
	}
	
	/**
	 * 注册信息变更
	 * @param bean
	 * @param xSRegister
	 * @return
	 * <p>Description: </p>
	 */
	public Map<String,Object> updateXSRegister(PaymentRequestParameter bean,XSRegister xSRegister){
		 // 业务接口
        String ACTION = "SdkUserStoreModify";
        // 发送请求
        Map<String, String> data = Maps.newHashMap();
        data.put("userCode", xSRegister.getUserCode());
        data.put("userName", xSRegister.getUserAccount());
        data.put("userNick", xSRegister.getUserAccount());
        data.put("userPhone", xSRegister.getUserPhone());
        data.put("userAccount", xSRegister.getUserAccount());
        data.put("userCert", xSRegister.getIdCard());
        data.put("userEmail", "q355023989@qq.com");
        data.put("userAddress", "上海市宝山区逸仙路2816号");
        data.put("userMemo", "上海莘丽");;

        data.put("settleBankNo", bean.getDebitCardNo());
        data.put("settleBankPhone", bean.getDebitPhone());
        data.put("settleBankCnaps", "102345023130");
        String resultString = null;
        // 发送请求
        try {
        	resultString = SdkRequest.requestAction(ACTION, data, this.getPrivateKey());
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("", e);
			return ResultWrap.err(LOG, CommonConstants.FALIED, "请求异常,请稍后重试!");		
		}
        JSONObject resultJSON = JSONObject.fromObject(resultString);
		String code = resultJSON.getString("code");
		String msg = resultJSON.getString("msg");
		if ("000000".equals(code)) {
			xSRegister.setSettleBankNo(bean.getDebitCardNo());
			xSRegister.setSettleBankPhone(bean.getDebitPhone());
			xSRegister = topupPayChannelBusiness.saveXSRegister(xSRegister);
			return ResultWrap.init(CommonConstants.SUCCESS, "修改成功",xSRegister);
		}else {
			return ResultWrap.err(LOG, CommonConstants.FALIED, msg);
		}
	}
	
	/**
	 * 修改费率接口
	 * @param bean
	 * @param xSRegister
	 * @return
	 * <p>Description: </p>
	 */
	private Map<String,Object> updateRate(PaymentRequestParameter bean,XSRegister xSRegister){
		// 业务接口
        String ACTION = "SdkUserStoreRate";
        
        String orderType = bean.getOrderType();
        String payType = "";
        if (CommonConstants.ORDER_TYPE_TOPUP.equals(orderType)) {
        	payType = "1";
		}else if(CommonConstants.ORDER_TYPE_CONSUME.equals(orderType)) {
        	payType = "16";
		}
        // 发送请求
        Map<String, String> data = Maps.newHashMap();
        data.put("userCode", xSRegister.getUserCode());
        data.put("payType", payType);
        
        data.put("orderRateT0", new BigDecimal(bean.getRate()).multiply(BigDecimal.valueOf(100)).setScale(2, BigDecimal.ROUND_UP)+"");
        data.put("settleChargeT0", bean.getExtraFee());

        data.put("orderRateT1", new BigDecimal(bean.getRate()).multiply(BigDecimal.valueOf(100)).setScale(2, BigDecimal.ROUND_UP)+"");
        data.put("settleChargeT1", bean.getExtraFee());
        // 发送请求
        String resultString = "";
        try {
        	resultString = SdkRequest.requestAction(ACTION, data, this.getPrivateKey());
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("", e);
			return ResultWrap.err(LOG, CommonConstants.FALIED, "请求异常,请稍后重试!");
		}
        JSONObject resultJSON = JSONObject.fromObject(resultString);
		String code = resultJSON.getString("code");
		String msg = resultJSON.getString("msg");
		if ("000000".equals(code)) {
			if (CommonConstants.ORDER_TYPE_TOPUP.equals(bean.getOrderType())) {
				xSRegister.setTxRate(new BigDecimal(bean.getRate()));
				xSRegister.setTxCharge(new BigDecimal(bean.getExtraFee()));
			}else if(CommonConstants.ORDER_TYPE_CONSUME.equals(orderType)) {
				xSRegister.setHkRate(new BigDecimal(bean.getRate()));
				xSRegister.setHkCharge(new BigDecimal(bean.getExtraFee()));
			}
			xSRegister = topupPayChannelBusiness.saveXSRegister(xSRegister);
			return ResultWrap.init(CommonConstants.SUCCESS, "修改成功",xSRegister);
		}else {
			return ResultWrap.err(LOG, CommonConstants.FALIED, msg);
		}
	}
	
	/**
	 * 快捷下单接口
	 * @param bean
	 * @param xSRegister
	 * @return
	 * <p>Description: </p>
	 */
	private Map<String,Object> txRequest(PaymentRequestParameter bean,XSRegister xSRegister){
		 // 业务接口
        String ACTION = "NocardSmsItem";
        // 发送请求
        Map<String, String> data = Maps.newHashMap();
        data.put("linkId", bean.getOrderCode());
        data.put("orderType", "10");
        data.put("amount", new BigDecimal(bean.getAmount()).multiply(BigDecimal.valueOf(100)).intValue()+"");
        data.put("notifyUrl", paymentGatewayIp+"/v1.0/paymentgateway/quick/tx/notify");
        data.put("goodsName", "充值缴费");
        data.put("cardNo", bean.getBankCard());
        data.put("cardAccount", xSRegister.getUserAccount());
        data.put("cardPhone", xSRegister.getUserPhone());
        data.put("cardCert", xSRegister.getIdCard());
        data.put("cardCvv", bean.getSecurityCode());
        data.put("cardYxq", this.expiredTimeToMMYY(bean.getExpiredTime()));

        data.put("settleCardNo", xSRegister.getSettleBankNo());
        data.put("settleCardAccount", xSRegister.getUserAccount());
        data.put("settleCardCert", xSRegister.getIdCard());
        data.put("settleCardPhone", xSRegister.getSettleBankPhone());
        data.put("orderRate", new BigDecimal(bean.getRate()).multiply(BigDecimal.valueOf(100)).setScale(2, BigDecimal.ROUND_UP)+"");
        data.put("settleCharge", new BigDecimal(bean.getExtraFee()).multiply(BigDecimal.valueOf(100)).intValue()+"");
        data.put("settleAmount", new BigDecimal(bean.getRealAmount()).multiply(BigDecimal.valueOf(100)).intValue()+"");
       
        String resultString = "";
        // 发送请求
        try {
			resultString = SdkRequest.requestAction(ACTION, data, this.getPrivateKey());
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("", e);
			return ResultWrap.err(LOG, CommonConstants.FALIED, "请求异常,请稍后重试!");
		}
        JSONObject resultJSON = JSONObject.fromObject(resultString);
        String code = resultJSON.getString("code");
        String msg = resultJSON.getString("msg");
        if (!"000000".equals(code)) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, msg);
		}
        String orderNo = resultJSON.getString("orderNo");
        this.updatePaymentOrderThirdOrder(bean.getIpAddress(), bean.getOrderCode(), orderNo);
		return ResultWrap.init(CommonConstants.SUCCESS, "请求成功", orderNo);
	}
	
	/**
	 * tx快捷/还款消费 支付异步通知接口
	 * @param request
	 * @param response
	 * @param orderNo
	 * @param orderStatus
	 * @param orderMemo
	 * @param sign
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/paymentgateway/quick/tx/notify")
	public @ResponseBody Object xsQuickNotify(HttpServletRequest request,HttpServletResponse response,
			String linkId,
			String orderNo,
			String orderStatus,
			String orderMemo,
			String sign
			) {
		LOG.info("异步回调进来了");
		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet) {
			LOG.info(key+"====="+parameterMap.get(key)[0]);
		}
		
		
		String md5 = Md5Utils.getMd5(orderNo+orderStatus+Config.KEY);
		if (!md5.equals(sign)) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, "验签失败");
		}
		
		PaymentRequestParameter bean = redisUtil.getPaymentRequestParameter(linkId);
		if ("20".equals(orderStatus)) {
			this.updateSuccessPaymentOrder(bean.getIpAddress(), linkId);
			try {
				response.getWriter().println("success");
			} catch (IOException e) {
				e.printStackTrace();
				return "success";
			}
			return null;
		}
		this.addOrderCauseOfFailure(linkId, orderMemo, bean.getIpAddress());
		return ResultWrap.err(LOG, CommonConstants.FALIED, linkId + "非成功回调",orderMemo);
	}
	
	/**
	 * 快捷确认支付接口
	 * @param orderNo
	 * @param smsCode
	 * @return
	 * <p>Description: </p>
	 */
	private Map<String,Object> confirmTXPay(String orderNo,String smsCode){
		Map<String, String> data = Maps.newHashMap();
        data.put("orderNo", orderNo);
        data.put("smsCode", smsCode);
        String resultString = "";
        try {
        	resultString = SdkRequest.requestAction("NocardConfirmPay", data, this.getPrivateKey());
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("", e);
			return ResultWrap.err(LOG, CommonConstants.FALIED, "请求异常,请稍后重试!");		
		}
        JSONObject resultJSON = JSONObject.fromObject(resultString);
        String code = resultJSON.getString("code");
        String msg = resultJSON.getString("msg");
        if (!"000000".equals(code)) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, msg);
		}
        return ResultWrap.init(CommonConstants.SUCCESS, "请求成功");
	}
	
	
	/**
	 * 支付订单查询接口 
	 * @param orderCode
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentgateway/xs/query/order/status")
	public @ResponseBody Object queryPayOrderStatus(String orderCode,String orderType) {
		if (CommonConstants.ORDER_TYPE_CONSUME.equals(orderType)) {
			return this.getTXOrderStatus(orderCode, "");
		}else if(CommonConstants.ORDER_TYPE_REPAYMENT.equals(orderType)) {
			return this.getRepaymentPayOrderStatus(orderCode, "");
		}else {
			return ResultWrap.err(LOG, CommonConstants.FALIED, "订单类型错误");
		}
	}
	
	/**
	 * 代付订单查询接口
	 * @param orderCode
	 * @param thirdOrderCode
	 * @return
	 * <p>Description: </p>
	 */
	private Map<String,Object> getRepaymentPayOrderStatus(String orderCode,String thirdOrderCode){
		Map<String, String> data = Maps.newHashMap();
		data.put("settleNo", thirdOrderCode);
		data.put("linkId", orderCode);
        String resultString = "";
		try {
			resultString = SdkRequest.requestAction("SdkSettleQuery", data, this.getPrivateKey());
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("", e);
			return ResultWrap.err(LOG, CommonConstants.FALIED, "请求异常,请稍后重试!");		
		}
        JSONObject resultJSON = JSONObject.fromObject(resultString);
		String code = resultJSON.getString("code");
        String msg = resultJSON.getString("msg");
        if (!"000000".equals(code)) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, msg);
		}
		String settleStatus = resultJSON.getString("settleStatus");
        String settleMemo = resultJSON.getString("settleMemo");
        if ("20".equals(settleStatus.trim())) {
            return ResultWrap.init(CommonConstants.SUCCESS, "支付成功");
		}else if("21".equals(settleStatus.trim())) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, settleMemo);
		}
        return ResultWrap.init("999998", "支付处理中");
	}
	
	/**
	 * 快捷订单查询
	 * @param orderCode
	 * @param thirdOrderCode
	 * @return
	 * <p>Description: </p>
	 */
	private Map<String, Object> getTXOrderStatus(String orderCode,String thirdOrderCode) {
		Map<String, String> data = Maps.newHashMap();
		data.put("orderNo", thirdOrderCode);
		data.put("linkId", orderCode);
        String resultString = "";
		try {
			resultString = SdkRequest.requestAction("OrderStatus", data, this.getPrivateKey());
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("", e);
			return ResultWrap.err(LOG, CommonConstants.FALIED, "请求异常,请稍后重试!");		
		}
        JSONObject resultJSON = JSONObject.fromObject(resultString);
		String code = resultJSON.getString("code");
        String msg = resultJSON.getString("msg");
        if (!"000000".equals(code)) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, msg);
		}
		String orderStatus = resultJSON.getString("orderStatus");
        String orderMemo = resultJSON.getString("orderMemo");
        if ("20".equals(orderStatus.trim())) {
            return ResultWrap.init(CommonConstants.SUCCESS, "支付成功");
		}else if("21".equals(orderStatus.trim())) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, orderMemo);
		}
        return ResultWrap.init("999998", "支付处理中");
	}
	

	/**
	 * 跳转到快捷支付输入短信验证码页面
	 * @param ordercode
	 * @param bankName
	 * @param bankCard
	 * @param cardType
	 * @param ipAddress
	 * @param amount
	 * @param model
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/paymentgateway/quick/xs/to/paymessagepage")
	public String toPayMessagePage(
			String ordercode,
			String bankName,
			String bankCard,
			String cardType,
			String ipAddress,
			String amount,
			Model model) {
		model.addAttribute("ordercode", ordercode);
		model.addAttribute("bankName", bankName);
		model.addAttribute("bankCard", bankCard);
		model.addAttribute("cardType", cardType);
		model.addAttribute("amount", amount);
		model.addAttribute("ipAddress", ipAddress);
		return "xspaymessage";
	}
	
	/**
	 * tx快捷下单接口
	 * @param ordercode
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentgateway/topup/xs/readytxpay")
	public @ResponseBody Object readyTXPay(String ordercode) {
		PaymentRequestParameter bean = redisUtil.getPaymentRequestParameter(ordercode);
		XSRegister xsRegister = topupPayChannelBusiness.getXSRegisterByIdCard(bean.getIdCard());
		Map<String, Object> txRequest = this.txRequest(bean, xsRegister);
		if (!CommonConstants.SUCCESS.equals(txRequest.get(CommonConstants.RESP_CODE))) {
			return txRequest;
		}
		String orderId = (String) txRequest.get(CommonConstants.RESULT);
		Map<String,Object> map = new HashMap<>();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "请求成功");
		map.put("orderId", orderId);
		return map;
	}
	
	/**
	 * tx快捷确认支付接口
	 * @param ordercode
	 * @param smsCode
	 * @param orderId
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentgateway/topup/xs/confirmtxpay")
	public @ResponseBody Object confirmTXPay(String ordercode,
			String smsCode,
			String orderId
			) {
		Map<String, Object> confirmTXPay = this.confirmTXPay(orderId, smsCode);
		return confirmTXPay;
	}
	
	
	public Map<String, Object> topupRequest(PaymentRequestParameter bean,XSRegister xSRegister) {
		String orderType = bean.getOrderType();
		String rate = bean.getRate();
		String extraFee = bean.getExtraFee();
		
		BigDecimal bigRate = new BigDecimal(rate);
		BigDecimal bigExtraFee = new BigDecimal(extraFee);
		if (CommonConstants.ORDER_TYPE_TOPUP.equals(orderType)) {
			String debitCardNo = bean.getDebitCardNo();
			
			if (bigRate.compareTo(xSRegister.getTxRate()) != 0 || bigExtraFee.compareTo(xSRegister.getTxCharge()) != 0) {
				Map<String, Object> updateRate = this.updateRate(bean, xSRegister);
				if (!CommonConstants.SUCCESS.equals(updateRate.get(CommonConstants.RESP_CODE))) {
					return updateRate;
				}
				xSRegister = (XSRegister) updateRate.get(CommonConstants.RESULT);
			}
			
			if (!xSRegister.getSettleBankNo().trim().equals(debitCardNo.trim())) {
				Map<String, Object> updateXSRegister = this.updateXSRegister(bean, xSRegister);
				if (!CommonConstants.SUCCESS.equals(updateXSRegister.get(CommonConstants.RESP_CODE))) {
					return updateXSRegister;
				}
				xSRegister = (XSRegister) updateXSRegister.get(CommonConstants.RESULT);
			}
			
			try {
				return ResultWrap.init(CommonConstants.SUCCESS, "请求成功",paymentGatewayIp + "/v1.0/paymentgateway/quick/xs/to/paymessagepage?bankName="
						+ URLEncoder.encode(bean.getCreditCardBankName(), "UTF-8") + "&bankCard="
						+ bean.getBankCard() + "&ordercode=" + bean.getOrderCode() + "&cardType="
						+ URLEncoder.encode(bean.getCreditCardCardType(), "UTF-8") + "&amount=" + bean.getAmount() + "&ipAddress=" + paymentGatewayIp);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return ResultWrap.err(LOG, CommonConstants.FALIED, "请求异常,请稍后重试!");
			}
		}
		
		if (CommonConstants.ORDER_TYPE_CONSUME.equals(orderType)) {
			return this.hkConsumePay(bean);
		}
		
		if (CommonConstants.ORDER_TYPE_REPAYMENT.equals(orderType)) {
			return this.hkRepaymentPay(bean);
		}
		
		
		return ResultWrap.err(LOG, CommonConstants.FALIED, "订单类型有误!");
	}
	
}
