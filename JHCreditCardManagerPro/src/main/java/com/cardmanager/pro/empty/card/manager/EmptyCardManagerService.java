package com.cardmanager.pro.empty.card.manager;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cardmanager.pro.authorization.CreditCardManagerAuthorizationHandle;
import com.cardmanager.pro.business.CreditCardAccountBusiness;
import com.cardmanager.pro.business.CreditCardManagerConfigBusiness;
import com.cardmanager.pro.executor.BaseExecutor;
import com.cardmanager.pro.pojo.CreditCardAccount;
import com.cardmanager.pro.pojo.CreditCardManagerConfig;
import com.cardmanager.pro.util.CardConstss;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class EmptyCardManagerService extends BaseExecutor{
	
	private static Logger LOG = LoggerFactory.getLogger(EmptyCardManagerService.class);
	
	@Autowired
	private CreditCardManagerAuthorizationHandle creditCardManagerAuthorizationHandle;
	
	@Autowired
	private CreditCardAccountBusiness creditCardAccountBusiness;
	
	@Autowired
	private CreditCardManagerConfigBusiness creditCardManagerConfigBusiness;
	
	@RequestMapping(value="/v1.0/creditcardmanager/empty/card/get/channel")
	public @ResponseBody Object getEmptyCardChannel() {
		CreditCardManagerConfig cardManagerConfig = creditCardManagerConfigBusiness.findByVersion("19");
		if (cardManagerConfig == null) {
			return ResultWrap.init(CommonConstants.FALIED, "暂无可用通道");
		}else {
			return ResultWrap.init(CommonConstants.SUCCESS, "查询成功",cardManagerConfig);
		}
	}
	
	
	@RequestMapping(value="/v1.0/creditcardmanager/empty/card/verify/card")
	public @ResponseBody Object verifyCardAPI(
			@RequestParam()String userId,
			@RequestParam()String creditCardNumber,
			@RequestParam()String version
			) {
		return this.verifyCard(userId, creditCardNumber, version);
	}
	
	private Object verifyCard(String userId,String creditCardNumber,String version){
		Map<String,Object> map = new HashMap<>();
		JSONObject resultJSONObject = creditCardManagerAuthorizationHandle.verifyCreditCard(userId, creditCardNumber);
		if(!CommonConstants.SUCCESS.equalsIgnoreCase(resultJSONObject.getString(CommonConstants.RESP_CODE))){
			map.put(CommonConstants.RESP_CODE, CardConstss.FAIL_CODE);
			map.put(CommonConstants.RESP_MESSAGE, resultJSONObject.getString(CommonConstants.RESP_MESSAGE).isEmpty()?"验证失败,原因:该卡不可用,请更换一张信用卡!":resultJSONObject.getString(CommonConstants.RESP_MESSAGE));
			return map;
		}
		JSONObject resultBankCardJSONObject = resultJSONObject.getJSONObject(CommonConstants.RESULT);
		String securityCode = resultBankCardJSONObject.getString("securityCode");
		String billDay = resultBankCardJSONObject.getString("billDay");
		String repaymentDay = resultBankCardJSONObject.getString("repaymentDay");
		String bankName = resultBankCardJSONObject.getString("bankName");
		
		Map<String, Object> verifyDoesSupportBank = creditCardManagerAuthorizationHandle.verifyDoesSupportBank(version, bankName);
		if (!CommonConstants.SUCCESS.equals(verifyDoesSupportBank.get(CommonConstants.RESP_CODE))) {
			return verifyDoesSupportBank;
		}
		
		if(securityCode != null && (securityCode.length() !=3 || !securityCode.matches("^[0-9]*$"))){
			return ResultWrap.init(CardConstss.NO_CVN_OR_EXTIME, "请重新设置安全码");
		}
		if ("0".equals(billDay) || "0".equals(repaymentDay)) {
			return ResultWrap.init(CardConstss.NO_CVN_OR_EXTIME, "请重新设置账单日/还款日");
		}
		
		int repaymentDate;
		int billDate;
		try {
			repaymentDate = Integer.valueOf(repaymentDay);
			billDate = Integer.valueOf(billDay);
		} catch (NumberFormatException e) {
			return ResultWrap.init(CardConstss.NO_CVN_OR_EXTIME, "账单日/还款日设置有误,请重新设置");
		}
		if (repaymentDate - billDate < 3 && repaymentDate - billDate > -3) {
			return ResultWrap.init(CardConstss.NO_CVN_OR_EXTIME, "账单日/还款日设置有误,请重新设置");
		}
		
		JSONObject userInfo = this.getUserInfo(userId);
		if (!CommonConstants.SUCCESS.equals(userInfo.getString(CommonConstants.RESP_CODE))) {
			return userInfo;
		}
		userInfo = userInfo.getJSONObject(CommonConstants.RESULT);
		String brandId = userInfo.getString("brandId");
		String phone = userInfo.getString("phone");
		Map<String, Object> userChannelRate = this.getUserChannelRate(userId, brandId, version);
		if(!CommonConstants.SUCCESS.equalsIgnoreCase((String) userChannelRate.get(CommonConstants.RESP_CODE))){
			userChannelRate.put(CommonConstants.RESP_CODE, CardConstss.FAIL_CODE);
			return userChannelRate;
		}
		resultJSONObject = (JSONObject) userChannelRate.get(CommonConstants.RESULT);
		String rateStr = resultJSONObject.getString("rate");
		String extraFeeStr = resultJSONObject.getString("extraFee");
		String withdrawFeeStr = resultJSONObject.getString("withdrawFee");
		BigDecimal serviceCharge = new BigDecimal(extraFeeStr).add(new BigDecimal(withdrawFeeStr)).setScale(2, BigDecimal.ROUND_UP);
//		BigDecimal rate = new BigDecimal(rateStr).setScale(4, BigDecimal.ROUND_UP);
		
		Map<String, Object> verifyDoesHaveBandCard = creditCardManagerAuthorizationHandle.verifyDoesHaveBandCard(userId, creditCardNumber, rateStr, serviceCharge.toString(), version,resultBankCardJSONObject);
		if(!CommonConstants.SUCCESS.equals(verifyDoesHaveBandCard.get(CommonConstants.RESP_CODE))) {
			if(!CardConstss.TO_BAND_CARD.equals(verifyDoesHaveBandCard.get(CommonConstants.RESP_CODE))){
				return ResultWrap.init(CardConstss.NONSUPPORT,(String)verifyDoesHaveBandCard.get(CommonConstants.RESP_MESSAGE));
			}
			return verifyDoesHaveBandCard;
		}
		CreditCardAccount creditCardAccount = creditCardAccountBusiness.findByCreditCardNumberAndVersion(creditCardNumber, version);
		if (creditCardAccount == null) {
			creditCardAccountBusiness.createNewAccount(userId, creditCardNumber, version, phone, billDate, repaymentDate, BigDecimal.ZERO, brandId);
		}
		
		return ResultWrap.init(CommonConstants.SUCCESS, "验证成功");
	}
	
	@RequestMapping(value="/v1.0/creditcardmanager/empty/card/calculate/reservedamount")
	public @ResponseBody Object calculateReservedAmountAPI(
			@RequestParam()String userId,
			@RequestParam()String creditCardNumber,
			@RequestParam()String amount,
			@RequestParam(required=false,defaultValue="2")String dayRepaymentCounts,
			String allRepaymentCounts,
			@RequestParam()String[] executeDate,
			@RequestParam()String version
			) {
		CreditCardManagerConfig creditCardManagerConfig = creditCardManagerConfigBusiness.findByVersion(version);
		return this.calculateReservedAmount(creditCardManagerConfig, userId, creditCardNumber, amount, dayRepaymentCounts, executeDate.length*Integer.valueOf(dayRepaymentCounts)+"", executeDate);
	}
	
	private Object calculateReservedAmount(CreditCardManagerConfig creditCardManagerConfig,String userId,String creditCardNumber,String amount,String dayRepaymentCounts,String allRepaymentCounts,String[] executeDate) {
		BigDecimal taskAmount = new BigDecimal(amount);
		int dayRepaymentCount = Integer.valueOf(dayRepaymentCounts);
		int allRepaymentCount = Integer.valueOf(allRepaymentCounts);
		
		int conSingleLimitCount = creditCardManagerConfig.getConSingleLimitCount();
		BigDecimal conSingleLimitMoney = creditCardManagerConfig.getConSingleLimitMoney();
		BigDecimal conSingleMaxMoney = creditCardManagerConfig.getConSingleMaxMoney();
		
		int paySingleLimitCount = creditCardManagerConfig.getPaySingleLimitCount();
		BigDecimal paySingleLimitMoney = creditCardManagerConfig.getPaySingleLimitMoney();
		BigDecimal paySingleMaxMoney = creditCardManagerConfig.getPaySingleMaxMoney();
		
//		验证日期格式是否正确
		Date[] executeDates = new Date[executeDate.length];
		try {
			for(int i= 0;i < executeDate.length;i++){
				executeDates[i] = DateUtil.getDateStringConvert(new Date(), executeDate[i],"yyyy-MM-dd");
			}
		} catch (Exception e) {
			return ResultWrap.init(CommonConstants.FALIED, "选择日期格式有误,正确格式为:2000-01-01");
		}
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, +1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date dateNow = DateUtil.getDateStringConvert(new Date(), calendar.getTime(), "yyyy-MM-dd");
//		验证日期是否是今天以后
		for(int i = 0;i < executeDates.length;i++){
			if(dateNow.compareTo(executeDates[i]) > 0){
				return ResultWrap.init(CommonConstants.FALIED, "只能选择除今天以后的日期,请重新选择!");
			}
		}
		
		if (paySingleLimitCount < dayRepaymentCount) {
			return ResultWrap.init(CommonConstants.FALIED, "单日还款笔数不能超过"+paySingleLimitCount+"笔");
		}
		
		if (paySingleLimitCount*executeDate.length < allRepaymentCount) {
			return ResultWrap.init(CommonConstants.FALIED, "总还款笔数不能超过"+paySingleLimitCount*executeDate.length+"笔");
		}
		
		JSONObject userInfo = this.getUserInfo(userId);
		if (!CommonConstants.SUCCESS.equals(userInfo.getString(CommonConstants.RESP_CODE))) {
			return userInfo;
		}
		userInfo = userInfo.getJSONObject(CommonConstants.RESULT);
		String brandId = userInfo.getString("brandId");
		Map<String, Object> userChannelRate = this.getUserChannelRate(userId, brandId, creditCardManagerConfig.getVersion());
		if(!CommonConstants.SUCCESS.equalsIgnoreCase((String) userChannelRate.get(CommonConstants.RESP_CODE))){
			userChannelRate.put(CommonConstants.RESP_CODE, CardConstss.FAIL_CODE);
			return userChannelRate;
		}
		JSONObject resultJSONObject = (JSONObject) userChannelRate.get(CommonConstants.RESULT);
		String rateStr = resultJSONObject.getString("rate");
		String extraFeeStr = resultJSONObject.getString("extraFee");
		String withdrawFeeStr = resultJSONObject.getString("withdrawFee");
		BigDecimal rate = new BigDecimal(rateStr);
		BigDecimal serviceCharge = new BigDecimal(extraFeeStr).add(new BigDecimal(withdrawFeeStr)).setScale(2, BigDecimal.ROUND_UP);
		
		BigDecimal totalServiceCharge = taskAmount.add(serviceCharge.multiply(BigDecimal.valueOf(allRepaymentCount))).divide(BigDecimal.ONE.subtract(rate),2,BigDecimal.ROUND_UP).subtract(taskAmount);
		
		BigDecimal avgAmount = taskAmount.divide(BigDecimal.valueOf(allRepaymentCount-1),0,BigDecimal.ROUND_UP);
		
		int randomPercent = 5 + new Random().nextInt(15);
		BigDecimal reservedAmount = avgAmount.multiply(BigDecimal.ONE.add(BigDecimal.valueOf(randomPercent).divide(BigDecimal.valueOf(100)))).setScale(0, BigDecimal.ROUND_UP);
		
		if (conSingleLimitMoney.compareTo(reservedAmount) > 0) {
			return ResultWrap.init(CommonConstants.FALIED, "计算失败,请增加还款金额或者减少还款笔数");
		}
		if (conSingleMaxMoney.compareTo(reservedAmount) < 0) {
			return ResultWrap.init(CommonConstants.FALIED, "计算失败,请减少还款金额或者增加还款笔数");
		}
		
		BigDecimal firstAmount = totalServiceCharge;
		if (firstAmount.compareTo(creditCardManagerConfig.getPaySingleLimitMoney()) < 0) {
			firstAmount = creditCardManagerConfig.getPaySingleLimitMoney();
		}
		firstAmount = firstAmount.setScale(0, BigDecimal.ROUND_UP).divide(BigDecimal.ONE.subtract(rate), 2, BigDecimal.ROUND_UP);
		
		Map<String, Object> map = ResultWrap.init(CommonConstants.SUCCESS, "计算成功");
		map.put("needAmount", firstAmount.toString());
		map.put("reservedAmount", reservedAmount.toString());
		map.put("totalServiceCharge", totalServiceCharge.toString());
		map.put("userId", userId);
		map.put("creditCardNumber", creditCardNumber);
		map.put("rate", rate.toString());
		map.put("serviceCharge", serviceCharge.toString());
		map.put("dayRepaymentCount", dayRepaymentCount);
		map.put("allRepaymentCount", allRepaymentCount);
		map.put("allConsumeCount", allRepaymentCount);
		map.put("taskAmount", taskAmount.toString());
		map.put("executeDate", executeDate);
		return map;
	}
	
}
