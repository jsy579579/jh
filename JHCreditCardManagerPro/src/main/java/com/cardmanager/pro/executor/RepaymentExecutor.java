package com.cardmanager.pro.executor;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cardmanager.pro.business.ConsumeTaskPOJOBusiness;
import com.cardmanager.pro.business.CreditCardAccountBusiness;
import com.cardmanager.pro.business.CreditCardAccountHistoryBusiness;
import com.cardmanager.pro.business.RepaymentTaskPOJOBusiness;
import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import com.cardmanager.pro.pojo.CreditCardAccount;
import com.cardmanager.pro.pojo.CreditCardAccountHistory;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;
import com.cardmanager.pro.util.CardConstss;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import net.sf.json.JSONObject;

@Component
public class RepaymentExecutor extends BaseExecutor {
	
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private RepaymentTaskPOJOBusiness repaymentTaskPOJOBusiness;
	
	@Autowired
	private ConsumeTaskPOJOBusiness consumeTaskPOJOBusiness;
	
	@Autowired
	private CreditCardAccountHistoryBusiness creditCardAccountHistoryBusiness;
	
	@Autowired
	private CreditCardAccountBusiness creditCardAccountBusiness;
	
	public Map<String,Object> executeRepaymentTask(RepaymentTaskPOJO repaymentTaskPOJO){
		Map<String,Object>map = new HashMap<>();
		String userId = repaymentTaskPOJO.getUserId();
		String amount = repaymentTaskPOJO.getAmount().toString();
		String version = repaymentTaskPOJO.getVersion();
		String realAmount = repaymentTaskPOJO.getRealAmount().toString();
		String rate = repaymentTaskPOJO.getRate().toString();
		String orderCode = repaymentTaskPOJO.getOrderCode();
		String repaymentTaskId = repaymentTaskPOJO.getRepaymentTaskId();
		String creditCardNumber = repaymentTaskPOJO.getCreditCardNumber();
		String description = repaymentTaskPOJO.getDescription();
		BigDecimal singleServiceCharge = repaymentTaskPOJO.getServiceCharge();
		String serviceCharge = repaymentTaskPOJO.getTotalServiceCharge().toString();
		String channelTag = repaymentTaskPOJO.getChannelTag();
		
		CreditCardAccount findByUserIdAndCreditCardNumber;
		
		if(CardConstss.CARD_VERSION_2.equals(version)){
			ConsumeTaskPOJO consumeTaskPOJO = consumeTaskPOJOBusiness.findByCreditCardNumberAndTaskTypeAndVersion(creditCardNumber, 0, version);
			if(consumeTaskPOJO.getOrderStatus() == 1){
				CreditCardAccountHistory creditCardAccountHistory = creditCardAccountHistoryBusiness.findByTaskIdAndAddOrSub(consumeTaskPOJO.getConsumeTaskId(), 0);
				Date createTime = creditCardAccountHistory.getCreateTime();
				Date nowTime = new Date();
				if((nowTime.getTime() - createTime.getTime()) < 5 * 60 *1000){
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, "银行入账中,请5分钟后再点击完成验证授权操作!");
					return map;
				}
			}else{
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "银行扣款失败,请联系管理员进行处理!");
				return map;
			}
		}
		
		findByUserIdAndCreditCardNumber = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(userId,creditCardNumber,version);
		if(findByUserIdAndCreditCardNumber != null){
			if(findByUserIdAndCreditCardNumber.getBlance().compareTo(BigDecimal.ZERO) == 0){
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "您有待完成的首笔还款任务,请等待完成后再点击,如果等待时间过长,请联系管理员!");
				return map;
			}else{
				realAmount = findByUserIdAndCreditCardNumber.getBlance().toString();
			}
		}else{
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "还款失败,原因:帐户异常,请联系管理员!");
			return map;
		}
		
		Random random = new Random();
		
		if(!"0".equals(orderCode)){
			orderCode = DateUtil.getDateStringConvert(new String(), new Date(),"yyyyMMddHHSSS")+random.nextInt(9)+random.nextInt(9)+random.nextInt(9)+random.nextInt(9)+"1";
		}else {
			orderCode = repaymentTaskId;
		}
		
		if (CardConstss.CARD_VERSION_3.equals(version) || CardConstss.CARD_VERSION_5.equals(version) || CardConstss.CARD_VERSION_7.equals(version)  || CardConstss.CARD_VERSION_8.equals(version) || CardConstss.CARD_VERSION_15.equals(version)) {
			realAmount = new BigDecimal(realAmount).add(singleServiceCharge).toString();
		}
		
		if (CardConstss.CARD_VERSION_1.equals(version)) {
			singleServiceCharge = singleServiceCharge.subtract(BigDecimal.valueOf(2));
		}
		
		JSONObject resultJSONObject = addCreditCardOrder(userId, rate, "11", amount, realAmount, creditCardNumber,channelTag , orderCode, singleServiceCharge.toString(), description, "");
		if(!CommonConstants.SUCCESS.equals(resultJSONObject.getString(CommonConstants.RESP_CODE))){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, resultJSONObject.containsKey(CommonConstants.RESP_MESSAGE)?resultJSONObject.getString(CommonConstants.RESP_MESSAGE):"生成订单失败");
			return map;
		}
		
		if (CardConstss.CARD_VERSION_3.equals(version) || CardConstss.CARD_VERSION_5.equals(version) || CardConstss.CARD_VERSION_7.equals(version)  || CardConstss.CARD_VERSION_8.equals(version)|| CardConstss.CARD_VERSION_15.equals(version) ||CardConstss.CARD_VERSION_16.equals(version)) {
			realAmount = new BigDecimal(realAmount).subtract(singleServiceCharge).toString();
		}
		
		repaymentTaskPOJO.setOrderCode(orderCode);
		repaymentTaskPOJO.setExecuteDateTime(DateUtil.getDateStringConvert(new String(), new Date(),"yyyy-MM-dd HH:mm:ss"));
		repaymentTaskPOJO = repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
		
		resultJSONObject = getUserInfo(userId);
		if(!CommonConstants.SUCCESS.equals(resultJSONObject.getString(CommonConstants.RESP_CODE))){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, resultJSONObject.containsKey(CommonConstants.RESP_MESSAGE)?resultJSONObject.getString(CommonConstants.RESP_MESSAGE):"获取用户信息失败");
			return map;
		}
		resultJSONObject = resultJSONObject.getJSONObject(CommonConstants.RESULT);
		String brandcode = resultJSONObject.getString("brandId");
		
		creditCardAccountBusiness.updateCreditCardAccountAndVersion(userId, creditCardNumber, repaymentTaskPOJO.getRepaymentTaskId(),1,repaymentTaskPOJO.getRealAmount(), description,version,repaymentTaskPOJO.getCreateTime());
		
		String extra = "0";
		if (CardConstss.CARD_VERSION_3.equals(version) || CardConstss.CARD_VERSION_5.equals(version)  || CardConstss.CARD_VERSION_7.equals(version)  || CardConstss.CARD_VERSION_15.equals(version)|| CardConstss.CARD_VERSION_18.equals(version) || CardConstss.CARD_VERSION_19.equals(version)) {
			if (this.isT1Version3(findByUserIdAndCreditCardNumber)) {
				extra = "T1";
			}
		}else if (CardConstss.CARD_VERSION_6.equals(version) || CardConstss.CARD_VERSION_60.equals(version)) {
			List<ConsumeTaskPOJO> consumeTaskPOJOs = consumeTaskPOJOBusiness.findByRepaymentTaskId(repaymentTaskId);
			extra = consumeTaskPOJOs.get(0).getOrderCode();
		}
		
		try {
			resultJSONObject = paymentTopupRequest(realAmount, orderCode, description, userId, brandcode, "0", channelTag,extra);
		} catch (RuntimeException e) {
			e.printStackTrace();
			return ResultWrap.init(CardConstss.WAIT_NOTIFY, "等待出款中,请稍后!");
		}
		LOG.info("===================="+repaymentTaskPOJO+"==================支付结果:" + resultJSONObject);
		if(!CommonConstants.SUCCESS.equals(resultJSONObject.getString(CommonConstants.RESP_CODE))){
			if("999998".equals(resultJSONObject.getString(CommonConstants.RESP_CODE))){
				map.put(CommonConstants.RESP_CODE, "999998");
				map.put(CommonConstants.RESP_MESSAGE, "等待出款中,请稍后!");
			}else{
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, resultJSONObject.containsKey(CommonConstants.RESP_MESSAGE)?resultJSONObject.getString(CommonConstants.RESP_MESSAGE):"支付失败");
			}
			return map;
		}
		
		repaymentTaskPOJO.setRealAmount(new BigDecimal(realAmount));
		repaymentTaskPOJO = repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "还款成功!");
		map.put(CommonConstants.RESULT, repaymentTaskPOJO);
		return map;
	}
	
	/**
	 * 判断是否是T1出款
	 * @author Robin-QQ/WX:354476429 
	 * @date 2018年6月13日  
	 * @param creditCardAccount
	 * @return
	 */
	private boolean isT1Version3(CreditCardAccount creditCardAccount){
		boolean isTrue = false;
		List<CreditCardAccountHistory> creditCardAccountHistorys = creditCardAccountHistoryBusiness.findByCreditCardAccountIdAndAddOrSubOrderByCreateTimeDesc(creditCardAccount.getId(),0);
		if (creditCardAccountHistorys != null && creditCardAccountHistorys.size() > 0) {
			CreditCardAccountHistory creditCardAccountHistory = creditCardAccountHistorys.get(0);
			Date createTime = creditCardAccountHistory.getCreateTime();
			Calendar instance = Calendar.getInstance();
			instance.set(Calendar.HOUR_OF_DAY, 0);
			instance.set(Calendar.MINUTE, 0);
			if (createTime.compareTo(instance.getTime()) < 0) {
				isTrue = true;
			}
		}
		return isTrue;
	}
}
