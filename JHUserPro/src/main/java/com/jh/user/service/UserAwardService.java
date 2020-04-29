package com.jh.user.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.jh.user.business.BrandManageBusiness;
import com.jh.user.business.UserAwardConfigBusiness;
import com.jh.user.business.UserAwardHistoryBusiness;
import com.jh.user.business.UserBalanceBusiness;
import com.jh.user.business.UserLoginRegisterBusiness;
import com.jh.user.business.UserRebateHistoryBusiness;
import com.jh.user.pojo.Brand;
import com.jh.user.pojo.User;
import com.jh.user.pojo.UserAccount;
import com.jh.user.pojo.UserAwardConfig;
import com.jh.user.pojo.UserAwardHistory;
import com.jh.user.pojo.UserRebateHistory;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class UserAwardService {
	
	private static final Logger LOG = LoggerFactory.getLogger(UserAwardService.class);
	
	@Autowired
	private UserAwardConfigBusiness userAwardConfigBusiness;
	
	@Autowired
	private UserLoginRegisterBusiness userLoginRegisterBusiness;
	
	@Autowired
	private BrandManageBusiness brandManageBusiness;
	
	@Autowired
	private UserBalanceBusiness UserBalanceBusiness;
	
	@Autowired
	private UserRebateHistoryBusiness userRebateHistoryBusiness;
	
	@Autowired
	private UserAwardHistoryBusiness userAwardHistoryBusiness;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@RequestMapping(value="/v1.0/user/get/award/config/by/brandid")
	public @ResponseBody Object getUserAwardConfigByBrandId(
			String brandId
			) {
		UserAwardConfig userAwardConfig = userAwardConfigBusiness.findByBrandId(brandId);
		if (userAwardConfig == null) {
			userAwardConfig = new UserAwardConfig();
			userAwardConfig.setBrandId(brandId);
			userAwardConfig.setOnOff(false);
		}
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功",userAwardConfig);
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/set/award/config")
	public @ResponseBody Object setUserAwardConfig(
//			状态： 0：关闭      1：开启
			String status,
//			奖励金额
			String awardMoney,
//			推荐人奖励金额
			String preAwardMoney,
//			贴牌号
			String brandId,
//			奖励模式： 0：只奖励注册人  1：奖励注册人和推荐人  2:奖励推荐人
			String type
			){
		if (preAwardMoney == null || awardMoney == null || "".equals(status.trim()) || "null".equalsIgnoreCase(status.trim())) {
			return ResultWrap.init(CommonConstants.FALIED, "奖励金额不能为空");
		}
		BigDecimal bigAwardMoney = new BigDecimal(awardMoney);
		BigDecimal bigPreAwardMoney = new BigDecimal(preAwardMoney);
		UserAwardConfig userAwardConfig = userAwardConfigBusiness.findByBrandId(brandId);
		if (userAwardConfig == null) {
			userAwardConfig = new UserAwardConfig();
		}
		userAwardConfig.setAwardMoney(bigAwardMoney);
		userAwardConfig.setPreAwardMoney(bigPreAwardMoney);
		userAwardConfig.setBrandId(brandId);
		if ("1".equals(status)) {
			userAwardConfig.setOnOff(true);
		}else {
			userAwardConfig.setOnOff(false);
		}

		if ("0".equals(type)) {
			userAwardConfig.setType("0");
		}else if ("1".equals(type)) {
			userAwardConfig.setType("1");
		}else if ("2".equals(type)){
			userAwardConfig.setType("2");
		}else {
			userAwardConfig.setType("0");
		}
		userAwardConfig = userAwardConfigBusiness.save(userAwardConfig);
		return ResultWrap.init(CommonConstants.SUCCESS, "设置成功！");
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/send/user/award")
	public @ResponseBody Map<String,Object> sendUserAward(String userId) {
		User user = userLoginRegisterBusiness.queryUserById(Long.valueOf(userId));
		long brandId = user.getBrandId();
		String channelTag = "JIEFUBAO";
		Brand brand = brandManageBusiness.findBrandById(brandId);
		if (brand == null) {
			return ResultWrap.err(LOG,CommonConstants.FALIED, "贴牌为空");
		}
		long manageid = brand.getManageid();
		
		UserAwardConfig awardConfig = userAwardConfigBusiness.findByBrandId(brandId+"");
		if (awardConfig == null || !awardConfig.isOnOff()) {
			return ResultWrap.err(LOG,CommonConstants.FALIED, "未启用用户注册奖励");
		}
		
		BigDecimal awardMoney = awardConfig.getAwardMoney();
		BigDecimal preAwardMoney = awardConfig.getPreAwardMoney();
		
		String type = awardConfig.getType();
		
		UserAwardHistory userAwardHistory = userAwardHistoryBusiness.findByUserIdAndAwardMoney(userId,awardMoney);
		if (userAwardHistory != null) {
			return ResultWrap.err(LOG,CommonConstants.FALIED, "注册奖励只能获得一次");
		}
		
		UserAccount userAccount = UserBalanceBusiness.queryUserAccountByUserid(manageid);
		if ("1".equals(type)) {
			if (userAccount==null || userAccount.getBalance().compareTo(awardMoney.multiply(BigDecimal.valueOf(2))) < 0 ) {
				return ResultWrap.err(LOG,CommonConstants.FALIED, "平台帐户余额不足!");
			}
			long preUserId = user.getPreUserId();
			this.addPaymentOrderAndUpdatePaymentOrder(userId, awardMoney.toString(), channelTag, "新用户注册奖励"+awardMoney.toString()+"元");
			this.addPaymentOrderAndUpdatePaymentOrder(preUserId+"", preAwardMoney.toString(), channelTag, "推广新用户注册奖励"+preAwardMoney.toString()+"元");
		}else {
			if (userAccount==null || userAccount.getBalance().compareTo(awardMoney)< 0 ) {
				return ResultWrap.err(LOG,CommonConstants.FALIED, "平台帐户余额不足!");
			}
			if ("0".equals(type)) {
				this.addPaymentOrderAndUpdatePaymentOrder(userId, awardMoney.toString(), channelTag, "新用户注册奖励"+awardMoney.toString()+"元");
			}else {
				this.addPaymentOrderAndUpdatePaymentOrder(user.getPreUserId()+"", preAwardMoney.toString(), channelTag, "推广新用户注册奖励"+preAwardMoney.toString()+"元");
			}
		}
		return ResultWrap.init(CommonConstants.SUCCESS, "发放奖励成功");
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/user/send/user/award/by/userid")
	public @ResponseBody Map<String,Object> sendUserAwardByUserId(
			String userId,
			String amount,
			String description
			) {
		String channelTag = "JIEFUBAO";
		boolean isTrue = this.addPaymentOrderAndUpdatePaymentOrder(userId, amount, channelTag, description);
		if (isTrue) {
			return  ResultWrap.init(CommonConstants.SUCCESS, "发放奖励成功!");
		}else {
			return  ResultWrap.init(CommonConstants.FALIED, "发放奖励失败!");
		}
	}
	
	private boolean addPaymentOrderAndUpdatePaymentOrder(String userId,String amount,String channelTag,String description) {
		try {
			JSONObject paymentOrder = this.addPaymentOrder(userId, amount, channelTag, description,"RedPayment");
			paymentOrder = paymentOrder.getJSONObject(CommonConstants.RESULT);
			String orderCode = paymentOrder.getString("ordercode");
			paymentOrder = this.updatePaymentOrder(orderCode);
			if (CommonConstants.SUCCESS.equals(paymentOrder.get(CommonConstants.RESP_CODE))) {
				UserAwardHistory userAwardHistory = new UserAwardHistory();
				userAwardHistory.setAwardMoney(new BigDecimal(amount));
				userAwardHistory.setUserId(userId);
				userAwardHistory.setCreateTime(new Date());
				userAwardHistory = userAwardHistoryBusiness.save(userAwardHistory);
				UserRebateHistory  rebateHistory = new  UserRebateHistory();
				rebateHistory.setAddOrSub("0");
				rebateHistory.setRebate(new BigDecimal(amount));
				rebateHistory.setCreateTime(new Date());
				rebateHistory.setCurRebate(new BigDecimal(amount));
				rebateHistory.setOrderType("0");
				rebateHistory.setOrderCode(orderCode);
				rebateHistory.setUserId(Long.valueOf(userId));
				userRebateHistoryBusiness.saveUserRebateHistory(rebateHistory);
				return true;
			}else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	public JSONObject addPaymentOrder(String userId,String amount,String channelTag,String description,String descCode) {
		String url = "http://transactionclear/v1.0/transactionclear/payment/type1/add";
		LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("type", "0");
		requestEntity.add("userid", userId);
		requestEntity.add("amount", amount);
		requestEntity.add("channel_tag", channelTag);
		requestEntity.add("desc", description);
		requestEntity.add("desc_code", descCode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("http://transactionclear/v1.0/transactionclear/payment/type1/add=====" + result);
		return JSONObject.fromObject(result);
	}
	
	public JSONObject updatePaymentOrder(String orderCode) {
		String url = "http://transactionclear/v1.0/transactionclear/payment/type1/update";
		LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("status", "1");
		requestEntity.add("order_code", orderCode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		LOG.info("http://transactionclear/v1.0/transactionclear/payment/type1/update=====" + result);
		return JSONObject.fromObject(result);
	}

}
