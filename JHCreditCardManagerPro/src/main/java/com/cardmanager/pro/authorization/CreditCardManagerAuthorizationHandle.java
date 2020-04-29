package com.cardmanager.pro.authorization;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.cardmanager.pro.util.CardConstss;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.cardmanager.pro.business.ConsumeTaskPOJOBusiness;
import com.cardmanager.pro.business.CreditCardAccountBusiness;
import com.cardmanager.pro.business.CreditCardManagerConfigBusiness;
import com.cardmanager.pro.business.RepaymentTaskPOJOBusiness;
import com.cardmanager.pro.channel.ChannelBaseAPI;
import com.cardmanager.pro.channel.ChannelFactory;
import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import com.cardmanager.pro.pojo.CreditCardAccount;
import com.cardmanager.pro.pojo.CreditCardManagerConfig;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import net.sf.json.JSONObject;

@Component
public class CreditCardManagerAuthorizationHandle {
	
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Autowired
	private CreditCardAccountBusiness creditCardAccountBusiness;
	
	@Autowired
	private RepaymentTaskPOJOBusiness repaymentTaskPOJOBusiness;
	
	@Autowired
	private ConsumeTaskPOJOBusiness consumeTaskPOJOBusiness;
	
	@Autowired
    private RedisTemplate redisTemplate;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private CreditCardManagerConfigBusiness creditCardManagerConfigBusiness;
	
	@Autowired
	private ChannelFactory channelFactory;
	
	/**
	 * 验证是否是可用信用卡
	 * @param userId
	 * @param creditCardNumber
	 * @return 返回JSON对象,根据对象中的resp_code判断是否为可用信用卡,000000为可用,999999为不可用
	 */
	public JSONObject verifyCreditCard(String userId,String creditCardNumber){
//		RestTemplate restTemplate = new RestTemplate();
//		URI uri = util.getServiceUrl("user", "error url request");
		String url = "http://user/v1.0/user/bank/verify/isuseable";
		LinkedMultiValueMap<String,String> requestEntity = new LinkedMultiValueMap<String,String>();
		requestEntity.add("userId", userId);
		requestEntity.add("bankCardNumber", creditCardNumber);
		String resultString = restTemplate.postForObject(url, requestEntity, String.class);
		JSONObject resultJSONObject = JSONObject.fromObject(resultString);
		return resultJSONObject;
	}
	
	/**
	 * 验证是否注册
	 * @param userId
	 * @param creditCardNumber
	 * @return 000000为已注册,999999为未注册
	 */
	public Map<String,Object> verifyIsRegister(String userId,String creditCardNumber,String version){
		Map<String,Object>map = new HashMap<>();
		CreditCardAccount creditCardAccount = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(userId,creditCardNumber,version);
		if(creditCardAccount==null){
			map.put(CommonConstants.RESP_MESSAGE,"无该卡数据,需要进行首笔验证!");
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
		}else{
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "验证成功,可进入生成还款计划!");
		}
		map.put(CommonConstants.RESULT, creditCardAccount);
		return map;
	}
	
	/**
	 * 验证该卡是否已使用过该功能
	 * @param creditCardNumber
	 * @return
	 */
	public Map<String,Object> verifyCreditCardNumberIsUse(String creditCardNumber,String userId,String version){
		Map<String,Object> map = new HashMap<>();
		CreditCardAccount creditCardAccount = creditCardAccountBusiness.findByCreditCardNumberAndVersion(creditCardNumber,version);
		RepaymentTaskPOJO repaymentTaskPOJO = repaymentTaskPOJOBusiness.findByCreditCardNumberAndTaskTypeAndVersion(creditCardNumber, 0,version);
		
		if((creditCardAccount != null && !creditCardAccount.getUserId().equals(userId))   ||  (repaymentTaskPOJO != null && !repaymentTaskPOJO.getUserId().equals(userId))){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "该银行卡已在其他号码使用过该功能,无法重复使用!");
			return map;
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "验证通过");
		return map;
	}
	
	/**
	 * 验证是否完成首笔还款任务
	 * @param userId
	 * @param creditCardNumber
	 * @return
	 */
	public Map<String,Object> verifyIsCompletedFirstRepaymentTask(String userId,String creditCardNumber,String version){
		Map<String,Object>map = new HashMap<>();
		RepaymentTaskPOJO firstRepaymentTaskPOJO = repaymentTaskPOJOBusiness.findByUserIdAndCreditCardNumberAndTaskStatusAndTaskTypeAndOrderStatusAndVersion(userId, creditCardNumber, 1, 0, 1, version);
		if(firstRepaymentTaskPOJO == null){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "未完成首笔还款任务!");
			return map;
		}else{
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "验证成功!");
		}
		return map;
	}
	
	/**
	 * 验证是否有待执行消费任务
	 * @param userId
	 * @param creditCardNumber
	 * @return false为无待执行消费任务,true为有待执行消费任务
	 */
	public boolean verifyDoesHaveTaskStatus0ConsumeTaskPOJO(String userId,String creditCardNumber,String version){
		boolean isTrue = false;
		int consumeTaskStatus0Count = consumeTaskPOJOBusiness.queryTaskStatus0CountAndVersion(userId,creditCardNumber,0,version);
		if(consumeTaskStatus0Count != 0){
			isTrue = true;
		}
		return isTrue;
	}
	
	/**
	 *  验证是否有待执行还款任务
	 * @param userId
	 * @param creditCardNumber
	 * @return false 为无待执行还款任务,true为有待执行还款任务
	 */
	public boolean verifyDoesHaveTaskStatus0RepaymentTaskPOJO(String userId,String creditCardNumber,String version){
		boolean isTrue = false;
		int repaymentTaskStatus0Count = repaymentTaskPOJOBusiness.queryTaskStatus0CountAndVersion(userId,creditCardNumber,0,version);
		if(repaymentTaskStatus0Count != 0){
			isTrue = true;
		}
		return isTrue;
	}
	
	/**
	 * 验证是否有首次生成的待执行消费任务
	 * @param userId
	 * @param creditCardNumber
	 * @return false 为无待执行消费任务,true为有待执行消费任务
	 */
	public ConsumeTaskPOJO verifyDoesHaveTaskStatus0AndTaskType0ConsumeTaskPOJO(String userId,String creditCardNumber,String version){
		ConsumeTaskPOJO firstConsumeTaskPOJO = consumeTaskPOJOBusiness.findByUserIdAndCreditCardNumberAndTaskStatusAndTaskTypeAndOrderStatusAndVersion(userId, creditCardNumber, 0, 0, 0, version);
		return firstConsumeTaskPOJO;
	}
	
	/**
	 * 验证是否有首次生成且执行成功的消费任务
	 * @param userId
	 * @param creditCardNumber
	 * @return false 为无首次生成且执行成功的消费任务,true为有首次生成且执行成功的消费任务
	 */
	public ConsumeTaskPOJO verifyDoesHaveOrderStatus1AndTaskType0ConsumeTaskPOJO(String userId,String creditCardNumber,String version){
		ConsumeTaskPOJO firstConsumeTaskPOJO = consumeTaskPOJOBusiness.findByUserIdAndCreditCardNumberAndTaskStatusAndTaskTypeAndOrderStatusAndVersion(userId, creditCardNumber, 1, 0, 1, version);
		return firstConsumeTaskPOJO;
	}
	
	/**
	 * 验证是否有首次生成且执行成功的还款任务
	 * @param userId
	 * @param creditCardNumber
	 * @return false 为无首次生成且执行成功的还款任务,true为有首次生成且执行成功的还款任务
	 */
	public RepaymentTaskPOJO verifyDoesHaveOrderStatus1AndTaskType0RepaymentTaskPOJO(String userId,String creditCardNumber,String version){
		RepaymentTaskPOJO firstRepaymentTaskPOJO = repaymentTaskPOJOBusiness.findByUserIdAndCreditCardNumberAndTaskStatusAndTaskTypeAndOrderStatusAndVersion(userId, creditCardNumber, 1, 0, 1, version);
		return firstRepaymentTaskPOJO;
	}
	
	
	/**
	 * 验证是否有首次生成的待执行还款任务
	 * @param userId
	 * @param creditCardNumber
	 * @return false 为无待执行还款任务,true为有待执行还款任务
	 */
	public RepaymentTaskPOJO verifyDoesHaveTaskStatus0AndTaskType0RepaymentTaskPOJO(String userId,String creditCardNumber,String version){
		RepaymentTaskPOJO firstRepaymentTaskPOJO = repaymentTaskPOJOBusiness.findByUserIdAndCreditCardNumberAndTaskStatusAndTaskTypeAndOrderStatusAndVersion(userId, creditCardNumber, 0, 0, 0, version);
		return firstRepaymentTaskPOJO;
	}
	
	
	public boolean verifyDoesHaveTaskStatus1AndTaskType0AndOrderStatus4RepaymentTaskPOJO(String userId,String creditCardNumber,String version){
		boolean isTrue = false;
		RepaymentTaskPOJO repaymentTaskStatus0TaskType0Count = repaymentTaskPOJOBusiness.findByUserIdAndCreditCardNumberAndTaskStatusAndTaskTypeAndOrderStatusAndVersion(userId,creditCardNumber,1,0,4,version);
		if(repaymentTaskStatus0TaskType0Count != null){
			isTrue = true;
		}
		return isTrue;
	}
	
	/**
	 * 验证是否有首次生成且已执行过且在等待完成的还款任务
	 * @param userId
	 * @param creditCardNumber
	 * @return
	 */
	public RepaymentTaskPOJO verifyDoesHaveTaskStatus1AndOrderStatus4RepaymentTaskPOJO(String userId,String creditCardNumber,String version){
		RepaymentTaskPOJO waitNotifyRepaymentTaskPOJO = repaymentTaskPOJOBusiness.findByUserIdAndCreditCardNumberAndTaskStatusAndTaskTypeAndOrderStatusAndVersion(userId, creditCardNumber, 1, 0, 4, version);
		return waitNotifyRepaymentTaskPOJO;
	}
	
	/**
	 * 验证是否有首次生成且已执行过且在等待完成的消费任务
	 * @param userId
	 * @param creditCardNumber
	 * @return
	 */
	public ConsumeTaskPOJO verifyDoesHaveTaskStatus1AndOrderStatus4ConsumeTaskPOJO(String userId,String creditCardNumber,String version){
		ConsumeTaskPOJO waitNotifyConsumeTaskPOJO = consumeTaskPOJOBusiness.findByUserIdAndCreditCardNumberAndTaskStatusAndTaskTypeAndOrderStatusAndVersion(userId, creditCardNumber, 1, 0, 4, version);
		return waitNotifyConsumeTaskPOJO;
	}
	
	/**
	 * 验证是否有批量生成的待执行消费任务
	 * @param userId
	 * @param creditCardNumber
	 * @return false 为无待执行消费任务,true为有待执行消费任务
	 */
	public boolean verifyDoesHaveTaskStatus0AndTaskType2ConsumeTaskPOJO(String userId,String creditCardNumber,String version){
		boolean isTrue = false;
		int consumeTaskStatus0TaskType2Count = consumeTaskPOJOBusiness.findByCreditCardNumberAndTaskTypeAndTaskStatusAndExecuteDateTimeGrantThan(creditCardNumber,2,0,DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss"));
		if(consumeTaskStatus0TaskType2Count != 0){
			isTrue = true;
		}
		return isTrue;
	}
	
	/**
	 * 验证是否有批量生成的待执行还款任务
	 * @param userId
	 * @param creditCardNumber
	 * @return false 为无待执行消费任务,true为有待执行消费任务
	 */
	public boolean verifyDoesHaveTaskStatus0AndTaskType2RepaymentTaskPOJO(String userId,String creditCardNumber,String version){
		boolean isTrue = false;
		int repaymentTaskStatus0TaskType2Count = repaymentTaskPOJOBusiness.findByCreditCardNumberAndTaskTypeAndTaskStatusAndExecuteDateTimeGrantThan(creditCardNumber,2,0,DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss"));
		if(repaymentTaskStatus0TaskType2Count != 0){
			isTrue = true;
		}
		return isTrue;
	}
	
	/**
	 * 验证是否是合法金额并返回指定BigDecimal格式数据
	 * @param amountStr
	 * @return
	 */
	public Map<String,Object> verifyMoney(String amountStr,int scale,int bigDecimalRound){
		Map<String,Object>map = new HashMap<>();
		amountStr = amountStr.trim();
		BigDecimal amount;
		try {
			amount = new BigDecimal(amountStr).setScale(scale, bigDecimalRound);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "您输入的金额有误,请重新输入!");
			return map;
		}
		
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "验证成功");
		map.put(CommonConstants.RESULT, amount);
		return map;
	}
	
	/**
	 * 验证是否是合法时间日期并返回指定格式的时间日期
	 * @param dateStr
	 * @param pattern
	 * @return
	 */
	public Map<String,Object> verifyDate(String dateStr,String pattern){
		Map<String,Object> map = new HashMap<>();
		Date date = null;
		try {
			date = DateUtil.getDateStringConvert(new Date(), dateStr, pattern);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "您输入的日期格式不正确,请重新输入!");
			return map;
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "验证成功");
		map.put(CommonConstants.RESULT, date);
		return map;
	}
	
	/**
	 * 验证String是否是空字符串或null
	 * @param fileds
	 * @return
	 */
	public Map<String,Object> verifyStringFiledIsNull(String ...fileds){
		Map<String,Object> map = new HashMap<>();
		for(String str:fileds){
			if(str!=null){
				str = str.trim();
			}
			if(str==null||"".equals(str)){
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE,"参数不能为空");
				return map;
			}
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "验证通过");
		return map;
	}
	
	/**
	 * 验证信用卡是否填写有效期和安全码
	 * @param userId
	 * @param creditCardNumber
	 * @return
	 */
	public Map<String,Object> verifyCreditCardDoesHaveSecurityCodeAndExpiredTime(String userId,String creditCardNumber){
		Map<String,Object>map = new HashMap<>();
//		RestTemplate restTemplate = new RestTemplate();
//		URI uri = util.getServiceUrl("user", "error url request");
		String url = "http://user/v1.0/user/bank/verify/cecuritycode/expiredtime";
		LinkedMultiValueMap<String,String> requestEntity = new LinkedMultiValueMap<String,String>();
		requestEntity.add("userId", userId);
		requestEntity.add("creditCardNumber", creditCardNumber);
		String resultString = restTemplate.postForObject(url, requestEntity, String.class);
		JSONObject resultJSONObject = JSONObject.fromObject(resultString);
		
		if(!CommonConstants.SUCCESS.equals(resultJSONObject.getString(CommonConstants.RESP_CODE))){
			map.put(CommonConstants.RESP_CODE, CardConstss.NO_CVN_OR_EXTIME);
			map.put(CommonConstants.RESP_MESSAGE, "有效期/安全码/账单日/还款日不能为空!");
			return map;
		}
		resultJSONObject = resultJSONObject.getJSONObject(CommonConstants.RESULT);
		String securityCode = resultJSONObject.getString("securityCode");
		String expiredTime = resultJSONObject.getString("expiredTime");
		String billDay = resultJSONObject.getString("billDay");
		String repaymentDay = resultJSONObject.getString("repaymentDay");
		Map<String, Object> verifyStringFiledIsNullMap = this.verifyStringFiledIsNull(securityCode,expiredTime);
		if(!CommonConstants.SUCCESS.equals(verifyStringFiledIsNullMap.get(CommonConstants.RESP_CODE)) || "0".equals(billDay) || "0".equals(repaymentDay)){
			map.put(CommonConstants.RESP_CODE, CardConstss.NO_CVN_OR_EXTIME);
			map.put(CommonConstants.RESP_MESSAGE, "有效期/安全码/账单日/还款日不能为空!");
			return map;
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "验证通过,有效期和安全码已填写!");
		map.put(CommonConstants.RESULT, resultJSONObject);
		return map;
	}
	
	/**
	 * 缓存限制用户连续请求时间
	 * @param cacheName
	 * @param cacheTime
	 * @param keys
	 * @return
	 */
	public Map<String,Object> restClientLimit(String cacheName,long cacheTime,String ...keys){
		Map<String,Object> map = new HashMap<String,Object>();
		boolean hasKey = false;
		String key = cacheName+":";
		for(int i = 0;i < keys.length;i++){
			key = key + keys[i];
		}
		ValueOperations<String, String> operations = redisTemplate.opsForValue();
		hasKey = redisTemplate.hasKey(key);
		if(hasKey){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "操作过于频繁,请"+cacheTime+"秒后重试!");
			return map;
		}
		operations.set(key, key, cacheTime, TimeUnit.SECONDS);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "验证通过");
		return map;
	}
	
	
	public  Map<String,Object> verifyOpenTime(String version){
		Map<String,Object> map = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		Calendar calendar2 = (Calendar) calendar.clone();
		Calendar calendar3 = (Calendar) calendar.clone();
		if(CardConstss.CARD_VERSION_1.equals(version)) {
			calendar2.set(Calendar.HOUR_OF_DAY, 6);
			calendar2.set(Calendar.MINUTE, 30);
			calendar3.set(Calendar.HOUR_OF_DAY, 21);
			calendar3.set(Calendar.MINUTE, 30);
		}else if(CardConstss.CARD_VERSION_2.equals(version)) {
			calendar2.set(Calendar.HOUR_OF_DAY, 9);
			calendar2.set(Calendar.MINUTE, 00);
			calendar3.set(Calendar.HOUR_OF_DAY, 20);
			calendar3.set(Calendar.MINUTE, 00);
		}else if(CardConstss.CARD_VERSION_3.equals(version)  || CardConstss.CARD_VERSION_5.equals(version) || CardConstss.CARD_VERSION_8.equals(version)|| CardConstss.CARD_VERSION_17.equals(version)) {
			calendar2.set(Calendar.HOUR_OF_DAY, 9);
			calendar2.set(Calendar.MINUTE, 00);
			calendar3.set(Calendar.HOUR_OF_DAY, 21);
			calendar3.set(Calendar.MINUTE, 00);
		}else if(CardConstss.CARD_VERSION_4.equals(version)) {
			calendar2.set(Calendar.HOUR_OF_DAY, 8);
			calendar2.set(Calendar.MINUTE, 00);
			calendar3.set(Calendar.HOUR_OF_DAY, 22);
			calendar3.set(Calendar.MINUTE, 00);
		}else if(CardConstss.CARD_VERSION_12.equals(version) || CardConstss.CARD_VERSION_13.equals(version)) {
			calendar2.set(Calendar.HOUR_OF_DAY, 0);
			calendar2.set(Calendar.MINUTE, 30);
			calendar3.set(Calendar.HOUR_OF_DAY, 22);
			calendar3.set(Calendar.MINUTE, 00);
		}else if(CardConstss.CARD_VERSION_14.equals(version)) {
			calendar2.set(Calendar.HOUR_OF_DAY, 7);
			calendar2.set(Calendar.MINUTE, 00);
			calendar3.set(Calendar.HOUR_OF_DAY, 21);
			calendar3.set(Calendar.MINUTE, 00);
		}else if(CardConstss.CARD_VERSION_15.equals(version)||CardConstss.CARD_VERSION_18.equals(version)) {
			calendar2.set(Calendar.HOUR_OF_DAY, 9);
			calendar2.set(Calendar.MINUTE, 00);
			calendar3.set(Calendar.HOUR_OF_DAY, 22);
			calendar3.set(Calendar.MINUTE, 00);
		}else if(CardConstss.CARD_VERSION_16.equals(version)) {
			calendar2.set(Calendar.HOUR_OF_DAY, 7);
			calendar2.set(Calendar.MINUTE, 00);
			calendar3.set(Calendar.HOUR_OF_DAY, 21);
			calendar3.set(Calendar.MINUTE, 50);
		}else if(CardConstss.CARD_VERSION_20.equals(version)) {
			calendar2.set(Calendar.HOUR_OF_DAY, 8);
			calendar2.set(Calendar.MINUTE, 00);
			calendar3.set(Calendar.HOUR_OF_DAY, 21);
			calendar3.set(Calendar.MINUTE, 00);
		}else if(CardConstss.CARD_VERSION_21.equals(version)) {
			calendar2.set(Calendar.HOUR_OF_DAY, 0);
			calendar2.set(Calendar.MINUTE, 30);
			calendar3.set(Calendar.HOUR_OF_DAY, 22);
			calendar3.set(Calendar.MINUTE, 00);
		}else if(CardConstss.CARD_VERSION_22.equals(version)) {
			calendar2.set(Calendar.HOUR_OF_DAY, 7);
			calendar2.set(Calendar.MINUTE, 30);
			calendar3.set(Calendar.HOUR_OF_DAY, 21);
			calendar3.set(Calendar.MINUTE, 30);
		}
//		System.out.println(calendar.compareTo(calendar2));
//		System.out.println(calendar.compareTo(calendar3));
		if(calendar.compareTo(calendar2) != 1 || calendar.compareTo(calendar3) != -1){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			if(CardConstss.CARD_VERSION_1.equals(version)) {
				map.put(CommonConstants.RESP_MESSAGE, "抱歉,该功能开放时间为6:30~21:30,请在该时间段使用,谢谢配合");
			}else if(CardConstss.CARD_VERSION_2.equals(version)) {
				map.put(CommonConstants.RESP_MESSAGE, "抱歉,该功能开放时间为9:00~20:00,请在该时间段使用,谢谢配合");
			}else if(CardConstss.CARD_VERSION_3.equals(version) || CardConstss.CARD_VERSION_5.equals(version) || CardConstss.CARD_VERSION_8.equals(version)) {
				map.put(CommonConstants.RESP_MESSAGE, "抱歉,该功能开放时间为9:00~21:00,请在该时间段使用,谢谢配合");
			}else if (CardConstss.CARD_VERSION_4.equals(version)) {
				map.put(CommonConstants.RESP_MESSAGE, "抱歉,该功能开放时间为8:00~22:00,请在该时间段使用,谢谢配合");
			}else if (CardConstss.CARD_VERSION_12.equals(version) || CardConstss.CARD_VERSION_13.equals(version)) {
				map.put(CommonConstants.RESP_MESSAGE, "抱歉,该功能开放时间为00:30~22:00,请在该时间段使用,谢谢配合");
			}else if (CardConstss.CARD_VERSION_14.equals(version)) {
				map.put(CommonConstants.RESP_MESSAGE, "抱歉,该功能开放时间为07:00~21:00,请在该时间段使用,谢谢配合");
			}else if (CardConstss.CARD_VERSION_15.equals(version)) {
				map.put(CommonConstants.RESP_MESSAGE, "抱歉,该功能开放时间为09:00~22:00,请在该时间段使用,谢谢配合");
			}else if (CardConstss.CARD_VERSION_16.equals(version)) {
				map.put(CommonConstants.RESP_MESSAGE, "抱歉,该功能开放时间为07:00~21:50,请在该时间段使用,谢谢配合");
			}else if (CardConstss.CARD_VERSION_20.equals(version)) {
				map.put(CommonConstants.RESP_MESSAGE, "抱歉,该功能开放时间为08:00~21:00,请在该时间段使用,谢谢配合");
			}else if (CardConstss.CARD_VERSION_21.equals(version)) {
				map.put(CommonConstants.RESP_MESSAGE, "抱歉,该功能开放时间为00:30~22:00,请在该时间段使用,谢谢配合");
			}else if (CardConstss.CARD_VERSION_22.equals(version)) {
				map.put(CommonConstants.RESP_MESSAGE, "抱歉,该功能开放时间为07:30~21:30,请在该时间段使用,谢谢配合");
			}else {
				return ResultWrap.init(CommonConstants.SUCCESS, "验证通过");
			}
			return map;
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "验证通过");
		return map;
	}
	
	public Map<String,Object> verifyDoesHaveBandCard(String userId ,String creditCardNumber,String rate,
													 String totalServiceCharge,String version,JSONObject bankJSON){
		String url = null;
		String userName = bankJSON.getString("userName");
		String bankName = bankJSON.getString("bankName");
		String phone = bankJSON.getString("phone");
		String idCard = bankJSON.getString("idcard");
		String securityCode = bankJSON.getString("securityCode");
		String expiredTime = bankJSON.getString("expiredTime");
		String nature = bankJSON.getString("nature");
		String cardType=bankJSON.getString("cardType");
		LinkedMultiValueMap<String,String> requestEntity = new LinkedMultiValueMap<String,String>();
		requestEntity.add("bankCard", creditCardNumber);
		requestEntity.add("idCard", idCard);
		requestEntity.add("phone", phone);
		requestEntity.add("userName", userName);
		requestEntity.add("bankName", bankName);
		requestEntity.add("securityCode", securityCode);
		requestEntity.add("expiredTime", expiredTime);
		requestEntity.add("rate",rate);
		requestEntity.add("extraFee", totalServiceCharge);
		requestEntity.add("userId", userId);
		requestEntity.add("cardType", nature);
		
		if (CardConstss.CARD_VERSION_15.equals(version)|CardConstss.CARD_VERSION_25.equals(version)|
                CardConstss.CARD_VERSION_26.equals(version)| CardConstss.CARD_VERSION_29.equals(version)|
                CardConstss.CARD_VERSION_35.equals(version)|CardConstss.CARD_VERSION_45.equals(version)||
				CardConstss.CARD_VERSION_40.equals(version)||CardConstss.CARD_VERSION_41.equals(version)||
				CardConstss.CARD_VERSION_42.equals(version)||CardConstss.CARD_VERSION_43.equals(version)||
				CardConstss.CARD_VERSION_53.equals(version) || CardConstss.CARD_VERSION_54.equals(version)||
				CardConstss.CARD_VERSION_62.equals(version) || CardConstss.CARD_VERSION_68.equals(version) ||
				CardConstss.CARD_VERSION_66.equals(version) || CardConstss.CARD_VERSION_69.equals(version) || CardConstss.CARD_VERSION_70.equals(version)
		) {
			url = "http://user/v1.0/user/bank/default/userid";
			requestEntity = new LinkedMultiValueMap<String,String>();
			requestEntity.add("user_id", userId);
			String resultString = restTemplate.postForObject(url, requestEntity, String.class);
			JSONObject resultJSON = JSONObject.fromObject(resultString);
			String respCode = resultJSON.getString(CommonConstants.RESP_CODE);
			if (!CommonConstants.SUCCESS.equals(respCode)) {
				return ResultWrap.init(resultJSON.getString(CommonConstants.RESP_CODE),resultJSON.getString(CommonConstants.RESP_MESSAGE),resultJSON.containsKey(CommonConstants.RESULT)?resultJSON.get(CommonConstants.RESULT):null);
			}
			resultJSON = resultJSON.getJSONObject(CommonConstants.RESULT);
			String dphong = resultJSON.getString("phone");
			String dbankName = resultJSON.getString("bankName");
			String dcardNo = resultJSON.getString("cardNo");
			requestEntity = new LinkedMultiValueMap<String,String>();
			requestEntity.add("bankCard", creditCardNumber);
			requestEntity.add("idCard", idCard);
			requestEntity.add("phone", phone);
			requestEntity.add("userName", userName); 
			requestEntity.add("bankName", bankName);
			requestEntity.add("securityCode", bankJSON.getString("securityCode"));
			requestEntity.add("expiredTime", bankJSON.getString("expiredTime"));
			requestEntity.add("rate",rate);
			requestEntity.add("extraFee", totalServiceCharge);
			
			requestEntity.add("dbankCard", dcardNo);
			requestEntity.add("dphone", dphong);
			requestEntity.add("dbankName", dbankName);
			requestEntity.add("cardtype", cardType);
		}
		JSONObject resultJSON = null;
		try {
			ChannelBaseAPI channelBaseAPI = channelFactory.getChannelBaseAPI(version);
			resultJSON = channelBaseAPI.isRegisterToChannel(requestEntity);
			if (resultJSON == null || !resultJSON.containsKey(CommonConstants.RESP_CODE)) {
				return ResultWrap.init(CommonConstants.FALIED, "授权失败,请选择其他通道使用!");
			}
		} catch (Exception e) {
			LOG.error("判断用户是否注册异常==========", e);
			throw new RuntimeException(e);
		}
		
		
		return ResultWrap.init(resultJSON.getString(CommonConstants.RESP_CODE),resultJSON.getString(CommonConstants.RESP_MESSAGE),resultJSON.containsKey(CommonConstants.RESULT)?resultJSON.get(CommonConstants.RESULT):null);
	}

	
	public Map<String,Object> verifyVersion2DoesHaveCompleteFirstConsume(String userId,String creditCardNumber,String version){
		ConsumeTaskPOJO consumeTaskPOJO = consumeTaskPOJOBusiness.findByCreditCardNumberAndTaskTypeAndVersion(creditCardNumber, 0, version);
		if(consumeTaskPOJO != null){
			if(4 == consumeTaskPOJO.getOrderStatus().intValue()){
				return ResultWrap.init(CommonConstants.FALIED, "等待银行扣款中,请稍后");
			}else{
				return ResultWrap.init(CommonConstants.SUCCESS, "无待完成的首笔消费任务");
			}
		}else{
			return ResultWrap.init(CommonConstants.SUCCESS, "无待完成的首笔消费任务");
		}
	}

	public boolean verifyDoesHaveTaskType0Task(String userIdStr, String creditCardNumber, String version) {
		ConsumeTaskPOJO consumeTaskPOJO = consumeTaskPOJOBusiness.findByUserIdAndCreditCardNumberAndTaskTypeAndVersion(userIdStr, creditCardNumber, 0, version);
		RepaymentTaskPOJO repaymentTaskPOJO = repaymentTaskPOJOBusiness.findByCreditCardNumberAndTaskTypeAndVersion(creditCardNumber, 0, version);
		if (consumeTaskPOJO != null || repaymentTaskPOJO != null) {
			return false;
		}
		return true;
	}
	
	public Map<String, Object> verifyDoesSupportBank(String version,String bankName){
		CreditCardManagerConfig cardManagerConfig = creditCardManagerConfigBusiness.findByVersion(version);
		if (cardManagerConfig == null || cardManagerConfig.getCreateOnOff() != 1) {
			return ResultWrap.init(CardConstss.NONSUPPORT, "因该还款通道维护，建议用户更换其他通道使用，已制定任务会继续执行，该通道开放时间等待通知，给您带来不便我们深表歉意！");
		}
		String noSupportBank = cardManagerConfig.getNoSupportBank();
		String[] noSupportBanks = null;
		if (noSupportBank != null && !"0".equals(noSupportBank)) {
			noSupportBanks = noSupportBank.split("-");
		}
		
		if (noSupportBanks != null && noSupportBanks.length > 0 && !"0".equals(noSupportBank) ) {
			for (int i = 0; i < noSupportBanks.length; i++) {
				if (bankName.trim().contains(noSupportBanks[i].trim())) {
					return ResultWrap.init(CardConstss.NONSUPPORT, "抱歉,该银行卡暂不支持使用此功能");
				}
				
			}
		}
		return ResultWrap.init(CommonConstants.SUCCESS, "验证通过");
	}
	
	public Map<String, Object> isOpen(String version){
		CreditCardManagerConfig creditCardManagerConfig = creditCardManagerConfigBusiness.findByVersion(version);
		if (creditCardManagerConfig == null) {
			return ResultWrap.init(CommonConstants.FALIED, "制定失败!原因：抱歉，该通道正在维护中，请使用其他还款通道!");
		}else if (creditCardManagerConfig.getCreateOnOff() == 0) {
			return ResultWrap.init(CommonConstants.FALIED, "制定失败!原因：抱歉，该通道正在维护中，请使用其他还款通道!");
		}
		return ResultWrap.init(CommonConstants.SUCCESS, "验证成功!",creditCardManagerConfig);
	}
	
	public Map<String, Object> isUsableCardAndSupportAndOpen(String version,String userId,String creditCardNumber){
		Map<String, Object> isOpen = this.isOpen(version);
		if (!CommonConstants.SUCCESS.equals(isOpen.get(CommonConstants.RESP_CODE))) {
			return isOpen;
		}
		CreditCardManagerConfig creditCardManagerConfig = (CreditCardManagerConfig) isOpen.get(CommonConstants.RESULT);
		String url = "http://user/v1.0/user/bank/verify/isuseable";
		LinkedMultiValueMap<String,String> requestEntity = new LinkedMultiValueMap<String,String>();
		requestEntity.add("userId", userId);
		requestEntity.add("bankCardNumber", creditCardNumber);
		String resultString = restTemplate.postForObject(url, requestEntity, String.class);
		JSONObject resultJSONObject = JSONObject.fromObject(resultString);
		if(!CommonConstants.SUCCESS.equalsIgnoreCase(resultJSONObject.getString(CommonConstants.RESP_CODE))){
			return ResultWrap.init(CommonConstants.FALIED, resultJSONObject.getString(CommonConstants.RESP_MESSAGE).isEmpty()?"生成计划失败,原因:该卡不可用,请更换一张信用卡!":resultJSONObject.getString(CommonConstants.RESP_MESSAGE));
		}
		JSONObject bankInfoJSON = resultJSONObject.getJSONObject(CommonConstants.RESULT);
		String bankName = bankInfoJSON.getString("bankName");
		Map<String, Object> verifyDoesSupportBank = this.verifyDoesSupportBank(version, bankName);
		if (!CommonConstants.SUCCESS.equals(verifyDoesSupportBank.get(CommonConstants.RESP_CODE))) {
			return verifyDoesSupportBank;
		}
		return ResultWrap.init(CommonConstants.SUCCESS, "验证成功!",creditCardManagerConfig);
	}
	
	public Map<String, Object> isCorrentDate(String version,String userId,String creditCardNumber){
		CreditCardAccount creditCardAccount = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(userId, creditCardNumber, version);
		if (creditCardAccount == null) {
			return ResultWrap.init(CommonConstants.FALIED, "还款帐户未创建，无法制定计划！");
		}
		int billDate = creditCardAccount.getBillDate().intValue();
		int repaymentDate = creditCardAccount.getRepaymentDate().intValue();
		if (billDate <= 0 || billDate > 30 || repaymentDate <= 0 || repaymentDate > 30 || billDate == repaymentDate) {
			return ResultWrap.init(CommonConstants.FALIED, "账单日/还款日未设置正确!");
		}
		return ResultWrap.init(CommonConstants.SUCCESS, "验证成功!",creditCardAccount);
	}

		
	
}
