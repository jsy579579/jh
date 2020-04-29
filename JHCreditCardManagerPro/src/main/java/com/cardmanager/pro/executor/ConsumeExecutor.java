package com.cardmanager.pro.executor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cardmanager.pro.business.ConsumeTaskPOJOBusiness;
import com.cardmanager.pro.business.CreditCardAccountBusiness;
import com.cardmanager.pro.business.RepaymentTaskPOJOBusiness;
import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import com.cardmanager.pro.pojo.CreditCardAccount;
import com.cardmanager.pro.util.CardConstss;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import net.sf.json.JSONObject;

@Component
public class ConsumeExecutor extends BaseExecutor{
	
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private ConsumeTaskPOJOBusiness consumeTaskPOJOBusiness;
	
	@Autowired
	private CreditCardAccountBusiness creditCardAccountBusiness;
	
	@Autowired
	private RepaymentTaskPOJOBusiness repaymentTaskPOJOBusiness;
	
	public Map<String,Object> executeConsumeTask(ConsumeTaskPOJO consumeTaskPOJO){
		Map<String,Object>map = new HashMap<>();
		String userId = consumeTaskPOJO.getUserId();
		String version = consumeTaskPOJO.getVersion();
		String realAmount = consumeTaskPOJO.getRealAmount().toString();
		String amount = consumeTaskPOJO.getAmount().toString();
		String creditCardNumber = consumeTaskPOJO.getCreditCardNumber();
		String orderCode = consumeTaskPOJO.getOrderCode();
		String consumeTaskId = consumeTaskPOJO.getConsumeTaskId();
		String description = consumeTaskPOJO.getDescription();
		String serviceCharge = consumeTaskPOJO.getServiceCharge().toString();
		String channelTag = consumeTaskPOJO.getChannelTag();
		Random random = new Random();
		String rate = repaymentTaskPOJOBusiness.findByRepaymentTaskId(consumeTaskPOJO.getRepaymentTaskId()).getRate().toString();
		
		CreditCardAccount findByUserIdAndCreditCardNumber;
		findByUserIdAndCreditCardNumber = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(userId,creditCardNumber,version);
		
		if(findByUserIdAndCreditCardNumber != null){
			if(findByUserIdAndCreditCardNumber.getBlance().compareTo(BigDecimal.ZERO) > 0){
				map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
				map.put(CommonConstants.RESP_MESSAGE, "消费失败,原因:已进行过消费,无法再进行首笔验证消费!");
				return map;
			}
		}
		
		if(!"0".equals(orderCode)){
			orderCode = DateUtil.getDateStringConvert(new String(), new Date(),"yyyyMMddHHSSS")+random.nextInt(9)+random.nextInt(9)+random.nextInt(9)+random.nextInt(9)+"2";
		}else {
			orderCode = consumeTaskId;
		}
		
		consumeTaskPOJO.setOrderCode(orderCode);
		consumeTaskPOJO.setExecuteDateTime(DateUtil.getDateStringConvert(new String(), new Date(),"yyyy-MM-dd HH:mm:ss"));
		consumeTaskPOJO = consumeTaskPOJOBusiness.save(consumeTaskPOJO);
		
		JSONObject resultJSONObject = addCreditCardOrder(userId,rate, "10", amount, realAmount, creditCardNumber, channelTag, orderCode, serviceCharge, description, "");

		if(!CommonConstants.SUCCESS.equals(resultJSONObject.getString(CommonConstants.RESP_CODE))){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, resultJSONObject.containsKey(CommonConstants.RESP_MESSAGE)?resultJSONObject.getString(CommonConstants.RESP_MESSAGE):"生成订单失败");
			return map;
		}
		

		
		resultJSONObject = getUserInfo(userId);
		if(!CommonConstants.SUCCESS.equals(resultJSONObject.getString(CommonConstants.RESP_CODE))){
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, resultJSONObject.containsKey(CommonConstants.RESP_MESSAGE)?resultJSONObject.getString(CommonConstants.RESP_MESSAGE):"获取用户信息失败");
			return map;
		}
		resultJSONObject = resultJSONObject.getJSONObject(CommonConstants.RESULT);
		String brandcode = resultJSONObject.getString("brandId");
		
		String extra = "0";
		if (CardConstss.CARD_VERSION_14.equals(version)) {
			extra = "消费计划|浙江省-杭州市";
		}
		
		try {
			resultJSONObject = paymentTopupRequest(realAmount, orderCode, description, userId, brandcode, "0", channelTag,extra);
		} catch (RuntimeException e) {
			e.printStackTrace();
			return ResultWrap.init(CardConstss.WAIT_NOTIFY, "等待扣款中,请稍后!");
		}
		LOG.info("===================="+consumeTaskPOJO+"==================支付结果:" + resultJSONObject);
		if(!CommonConstants.SUCCESS.equals(resultJSONObject.getString(CommonConstants.RESP_CODE))){
			map.put(CommonConstants.RESP_CODE, resultJSONObject.getString(CommonConstants.RESP_CODE));
			map.put(CommonConstants.RESP_MESSAGE, resultJSONObject.containsKey(CommonConstants.RESP_MESSAGE)?resultJSONObject.getString(CommonConstants.RESP_MESSAGE):"支付失败");
			return map;
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "消费成功!");
		return map;
	}
}
