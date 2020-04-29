package com.cardmanager.pro.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.cardmanager.pro.business.CreditCardManagerConfigBusiness;
import com.cardmanager.pro.business.HKHelperBindCardBusiness;
import com.cardmanager.pro.executor.BaseExecutor;
import com.cardmanager.pro.pojo.CreditCardManagerConfig;
import com.cardmanager.pro.pojo.HKHelperBindCard;
import com.cardmanager.pro.util.MD5;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
public class HKHelperService extends BaseExecutor{

	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private HKHelperBindCardBusiness  hkHelperBindCardBusiness;
	
	@Autowired
	private CreditCardManagerTaskService  creditCardManagerTaskService;
	
	@Autowired
	private CreditCardManagerConfigBusiness creditCardManagerConfigBusiness;
	
	private static final String APPID = "B8875C116018FAE0829C303376C37CCF";
	
	private static final String SIGNKEY = "DCCEB02E17B1C3E82EF44582DBE145E2";
	
	private static final String URL = "http://api.51bnh.com/gateway";
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/creditcardmanager/get/hkhelper/info")
	public @ResponseBody Object getHKHelperInfo(
			@RequestParam(required=false)String brandId
			) {
		Map<String,Object> map = new HashMap<>();
		Map<String, String> appIdAndSignKey = this.getAppIdAndSignKey(brandId);
		String appId = appIdAndSignKey.get("appId");
		String signKey = appIdAndSignKey.get("signKey");
		String url = appIdAndSignKey.get("url");
		map.put("appId", appId);
		map.put("signKey", signKey);
		map.put("url", url);
		return ResultWrap.init(CommonConstants.SUCCESS, "获取成功",map);
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/creditcardmanager/get/hk/user/info")
	public @ResponseBody Object getUserInfoToHKHelper(
//			用户当前登录app的token
			@RequestParam(value="token")String token,
//			用户编号 确保渠道系统内唯一userId
			@RequestParam(value="accountNo")String userId
			) {
		Map<String,Object> result = new HashMap<>();
		Map<String,Object> cifAccount = new HashMap<>();
		JSONObject userInfo = this.getUserInfo(userId);
		userInfo = userInfo.getJSONObject(CommonConstants.RESULT);
		String brandId = userInfo.getString("brandId");
		String phone = userInfo.getString("phone");
		
		cifAccount.put("accountNo", userId);

		cifAccount.put("accountMobile", phone);

		JSONObject userImage = this.getUserImage(brandId, phone);
		JSONArray jsonArray = userImage.getJSONArray(CommonConstants.RESULT);
		JSONObject infouser = userImage.getJSONObject("infouser");
		if (jsonArray != null && jsonArray.size() > 1) {
			cifAccount.put("img1", jsonArray.get(0));
			cifAccount.put("img2", jsonArray.get(1));
		}
		String realname = infouser.getString("realname");
		String idcard = infouser.getString("idcard");
		cifAccount.put("accountName", realname);
		cifAccount.put("customerNo", idcard);
		
		result.put("cifAccount",cifAccount);
		List<HKHelperBindCard> cards = hkHelperBindCardBusiness.findByUserId(userId);
		result.put("cifCards",cards);
		result.put("cifExtend","");
		return result;
	}
	
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/creditcardmanager/update/hk/order")
	public @ResponseBody Object hkHelperNotify(HttpServletRequest request,
//			订单编号
			@RequestParam(value="orderNo")String orderNo,
//			子订单编号
			@RequestParam(value="orderDetailNo")String orderDetailNo,
//			订单金额（元）
			@RequestParam(value="orderAmt")String orderAmt,
//			子订单金额（元）
			@RequestParam(value="orderDetailAmt")String orderDetailAmt,
//			用户编号 确保渠道系统内唯一userId
			@RequestParam(value="accountNo")String userId,
//			交易卡号
			@RequestParam(value="cardNo")String cardNo,
//			订单是否结束 0-未结束 1-已结束
			@RequestParam(value="isEnd")String isEnd
			) {
		LOG.info("orderNo:"+orderNo+"====orderDetailNo:"+orderDetailNo+"====orderAmt:"+orderAmt+"====orderDetailAmt:"+orderDetailAmt+"====accountNo:"+userId+"====cardNo:"+cardNo+"====isEnd:"+isEnd+"====");
		JSONObject userInfo = this.getUserInfo(userId);
		String brandId = userInfo.getString("brandId");
		Map<String,Object> result = (Map<String, Object>) creditCardManagerTaskService.getChannelRateByUserId(request, userId, brandId, "12");
		LOG.info("result=====1"+result);
		BigDecimal rate = (BigDecimal) result.get("rate");
		BigDecimal serviceCharge = (BigDecimal) result.get("serviceCharge");
		CreditCardManagerConfig cardManagerConfig = creditCardManagerConfigBusiness.findByVersion("12");
		this.addCreditCardOrder(userId, rate.toString(), "12", orderDetailAmt, orderDetailAmt, cardNo, cardManagerConfig.getChannelTag(), orderDetailNo, serviceCharge.toString(), "还款任务", "");
		this.updatePaymentOrderByOrderCode(orderDetailNo);
		return "success";
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/creditcardmanager/add/hkhelper/creditcard")
	public @ResponseBody Object hkHelperBindCard(HttpServletRequest request,
			@RequestParam(value="userId")String userId,
			@RequestParam(value="creditCardNumber")String creditCardNumber,
			@RequestParam(value="bundle")String bundle
			) {
		HKHelperBindCard hkHelperBindCard= hkHelperBindCardBusiness.findByUserIdAndCardNo(userId,creditCardNumber);
		if (hkHelperBindCard != null) {
			return ResultWrap.init(CommonConstants.SUCCESS, "绑定成功");
		}
		JSONObject result = this.getUserBankInfo(userId, creditCardNumber);
		JSONObject userBankInfo = result.getJSONObject(CommonConstants.RESULT);
		String userName = userBankInfo.getString("userName");
		String phone = userBankInfo.getString("phone");

		JSONObject hkHelperBindCard2 = this.hkHelperBindCard(userId, creditCardNumber, userName, phone,bundle);
		LOG.info(hkHelperBindCard2+"");
		if ("00".equals(hkHelperBindCard2.getString("respCode"))) {
			hkHelperBindCard = new HKHelperBindCard();
			hkHelperBindCard.setCardNo(creditCardNumber);
			hkHelperBindCard.setName(userName);
			hkHelperBindCard.setPhone(phone);
			hkHelperBindCard.setUserId(userId);
			hkHelperBindCard = hkHelperBindCardBusiness.saveNew(hkHelperBindCard);
			return ResultWrap.init(CommonConstants.SUCCESS, "绑定成功");
		}else {
			return ResultWrap.init(CommonConstants.FALIED, "绑定失败");
		}
	}
	
	
	private JSONObject getUserImage(String brandId,String phone) throws RuntimeException{
		String url = "http://user/v1.0/user/download/realname";
		LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("brandId", brandId);
		requestEntity.add("phone", phone);
		JSONObject resultJSONObject;
		try {
			String resultString = restTemplate.postForObject(url, requestEntity, String.class);
			resultJSONObject = JSONObject.fromObject(resultString);
		} catch (Exception e) {
			e.printStackTrace();LOG.error("",e);
			throw new RuntimeException(e);
		}
		return resultJSONObject;
	}
	
	private JSONObject getUserDefaultBankInfo(String cardNo,String userId) throws RuntimeException{
		String url = "http://user/v1.0/user/bank/default/cardnoand/type";
		LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("type", "0");
		requestEntity.add("cardno", cardNo);
		requestEntity.add("userId", userId);
		JSONObject resultJSONObject;
		try {
			String resultString = restTemplate.postForObject(url, requestEntity, String.class);
			resultJSONObject = JSONObject.fromObject(resultString);
		} catch (Exception e) {
			e.printStackTrace();LOG.error("",e);
			throw new RuntimeException(e);
		}
		return resultJSONObject;
	}
	
	private JSONObject hkHelperBindCard(String userId,String cardNo,String cardName,String cardMobile,String bundle) throws RuntimeException{
		Map<String, String> appIdAndSignKey = this.getAppIdAndSignKey(null);
		String appId = appIdAndSignKey.get("appId");
		String signKey = appIdAndSignKey.get("signKey");
		String url = appIdAndSignKey.get("url");
//		生产地址
//		String url = "http://api.51bnh.com/gateway";
		LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("appid", appId);
		requestEntity.add("method", "bms.account.card.create");
		requestEntity.add("version", "1.0");
		
		requestEntity.add("bundle", bundle);
		requestEntity.add("token", userId);
		requestEntity.add("accountNo", userId);
		requestEntity.add("cardNo", cardNo);
		requestEntity.add("cardName", cardName);
		requestEntity.add("cardMobile", cardMobile);
		Map<String,String> map = new TreeMap<>();
		Map<String, String> singleValueMap = requestEntity.toSingleValueMap();
		singleValueMap.putAll(map);
		Set<String> keySet = map.keySet();
		StringBuffer sb = new StringBuffer();
		for (String key : keySet) {
			String value = map.get(key);
			sb.append(key + "=" +value+"&");
		}
		String sign = sb.toString() + "signKey="+signKey;
		LOG.info("sign明文=====" + sign);
		sign = MD5.MD5Encode(sign);
		requestEntity.add("sign", sign);
		JSONObject resultJSONObject;
		try {
			String resultString = new RestTemplate().postForObject(url, requestEntity, String.class);
			resultJSONObject = JSONObject.fromObject(resultString);
		} catch (Exception e) {
			e.printStackTrace();LOG.error("",e);
			throw new RuntimeException(e);
		}
		return resultJSONObject;
	}
	
	
	private Map<String,String> getAppIdAndSignKey(String brandId){
		if (brandId == null || "".equals(brandId.trim()) || "null".equalsIgnoreCase(brandId)) {
			Map<String,String> map = new HashMap<>();
			map.put("appId", APPID);
			map.put("signKey", SIGNKEY);
			map.put("url", URL);
			return map;
		}else {
			return null;
		}
	}
}
