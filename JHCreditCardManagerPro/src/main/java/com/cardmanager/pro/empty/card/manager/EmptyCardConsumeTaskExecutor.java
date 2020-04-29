package com.cardmanager.pro.empty.card.manager;

import java.math.BigDecimal;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cardmanager.pro.channel.ChannelRoot;
import com.cardmanager.pro.channel.behavior.ConsumeTaskExecutor;
import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;
import com.cardmanager.pro.util.CardConstss;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

@Component
public class EmptyCardConsumeTaskExecutor extends ChannelRoot implements ConsumeTaskExecutor{
	
	private static final Logger LOG = LoggerFactory.getLogger(EmptyCardConsumeTaskExecutor.class);
	
	@Autowired
	private EmptyCardApplyOrderBusiness emptyCardApplyOrderBusiness;

	@Override
	public void executeTask(ConsumeTaskPOJO consumeTaskPOJO) {
		LOG.info("开始执行任务=====" + consumeTaskPOJO);
		String userId = consumeTaskPOJO.getUserId();
		String realAmount = consumeTaskPOJO.getRealAmount().toString();
		String amount = consumeTaskPOJO.getAmount().toString();
		String creditCardNumber = consumeTaskPOJO.getCreditCardNumber();
		String orderCode = consumeTaskPOJO.getConsumeTaskId();
		String description = consumeTaskPOJO.getDescription();
		String serviceCharge = consumeTaskPOJO.getServiceCharge().toString();
		String consumeTaskId = consumeTaskPOJO.getConsumeTaskId();
		String channelTag = consumeTaskPOJO.getChannelTag();
		String version = consumeTaskPOJO.getVersion();
		String brandId = consumeTaskPOJO.getBrandId();
		consumeTaskPOJO.setAmount(consumeTaskPOJO.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP));
		String rate = repaymentTaskPOJOBusiness.findByRepaymentTaskId(consumeTaskPOJO.getRepaymentTaskId()).getRate().toString();
		consumeTaskPOJO.setOrderCode(orderCode);
		if (!CardConstss.CARD_VERSION_10.equals(version) && !CardConstss.CARD_VERSION_11.equals(version)) {
			consumeTaskPOJO.setExecuteDateTime(DateUtil.getDateStringConvert(new String(), new Date(),"yyyy-MM-dd HH:mm:ss"));
		}
		consumeTaskPOJOBusiness.save(consumeTaskPOJO);

		JSONObject resultJSONObject;
		try {
			resultJSONObject = addCreditCardOrder(userId,rate, "10", amount, realAmount, creditCardNumber,channelTag, orderCode, serviceCharge, description, "");
		} catch (RuntimeException e) {
			e.printStackTrace();
			LOG.error("",e);
			this.failDealWith(consumeTaskPOJO,null, consumeTaskId,null,e);
			return;
		}
		
		if(!CommonConstants.SUCCESS.equals(resultJSONObject.getString(CommonConstants.RESP_CODE))){
			LOG.error("=============="+orderCode+"==============消费任务生成订单失败:原因:"+(resultJSONObject.containsKey(CommonConstants.RESP_MESSAGE)?resultJSONObject.getString(CommonConstants.RESP_MESSAGE):"生成订单失败"));
			this.failDealWith(consumeTaskPOJO,resultJSONObject, consumeTaskId, "原因:生成订单失败",null);
			return;
		}
		
		String extra = description;
		try {
			resultJSONObject = paymentTopupRequest(realAmount, orderCode, description, userId, brandId, "0", channelTag,extra);
			LOG.info("==============="+orderCode+"==============支付结果:" + resultJSONObject);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("",e);
			consumeTaskPOJO.setErrorMessage(e != null?ExceptionUtil.errInfo(e).substring(0, 250):"无错误信息");
			consumeTaskPOJO.setTaskStatus(1);
			consumeTaskPOJO.setOrderStatus(4);
			consumeTaskPOJO.setReturnMessage("等待银行扣款,请稍后!");
			ConsumeTaskPOJO findByConsumeTaskId = consumeTaskPOJOBusiness.findByConsumeTaskId(consumeTaskId);
			if (findByConsumeTaskId.getOrderStatus() == 1) {
				consumeTaskPOJO.setOrderStatus(1);
				consumeTaskPOJO.setReturnMessage("消费成功!");
			}
			consumeTaskPOJOBusiness.save(consumeTaskPOJO);
			return;
		}
		
		if(!CommonConstants.SUCCESS.equals(resultJSONObject.getString(CommonConstants.RESP_CODE))){
			if(CardConstss.WAIT_NOTIFY.equals(resultJSONObject.getString(CommonConstants.RESP_CODE))) {
				consumeTaskPOJO.setTaskStatus(1);
				consumeTaskPOJO.setOrderStatus(4);
				consumeTaskPOJO.setReturnMessage("等待银行扣款,请稍后!");
				ConsumeTaskPOJO findByConsumeTaskId = consumeTaskPOJOBusiness.findByConsumeTaskId(consumeTaskId);
				if (findByConsumeTaskId.getOrderStatus() == 1) {
					consumeTaskPOJO.setOrderStatus(1);
					consumeTaskPOJO.setReturnMessage("消费成功!");
				}
				consumeTaskPOJOBusiness.save(consumeTaskPOJO);
			}else {
				LOG.error("=============="+orderCode+"==============消费任务支付失败:原因:"+(resultJSONObject.containsKey(CommonConstants.RESP_MESSAGE)?resultJSONObject.getString(CommonConstants.RESP_MESSAGE):"支付失败"));
				this.failDealWith(consumeTaskPOJO,resultJSONObject, consumeTaskId, "原因:支付失败!",null);
			}
			return;
		}
		consumeTaskPOJO.setTaskStatus(1);
		consumeTaskPOJO.setOrderStatus(1);
		consumeTaskPOJO.setReturnMessage("消费成功!");
		consumeTaskPOJOBusiness.save(consumeTaskPOJO);
		creditCardAccountBusiness.updateCreditCardAccountAndVersion(userId, creditCardNumber, consumeTaskPOJO.getConsumeTaskId(), 0, consumeTaskPOJO.getAmount(), description,consumeTaskPOJO.getVersion(),consumeTaskPOJO.getCreateTime());
	}

	
	private void updateConsumeService(String consumeTaskId,String repaymentTaskId){
		ConsumeTaskPOJO consumeTaskPOJO = consumeTaskPOJOBusiness.findByConsumeTaskId(consumeTaskId);
		RepaymentTaskPOJO repaymentTaskPOJO = repaymentTaskPOJOBusiness.findByRepaymentTaskId(repaymentTaskId);
		repaymentTaskPOJO.setTotalServiceCharge(BigDecimal.ZERO);
		repaymentTaskPOJO = repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
		consumeTaskPOJO.setServiceCharge(BigDecimal.ZERO);
		consumeTaskPOJO = consumeTaskPOJOBusiness.save(consumeTaskPOJO);
	}
	
	
	private void failDealWith(ConsumeTaskPOJO consumeTaskPOJO,JSONObject resultJSONObject,String consumeTaskId,String message,Exception e){
		String errorMessage = "";
		String returnMessage = "";
		if (e != null) {
			errorMessage = e != null?ExceptionUtil.errInfo(e).substring(0, 250):"无错误信息";
			returnMessage = "扣款失败!";
		}else {
			errorMessage = resultJSONObject!=null&&resultJSONObject.containsKey(CommonConstants.RESP_MESSAGE)?resultJSONObject.getString(CommonConstants.RESP_MESSAGE):message;
			returnMessage = resultJSONObject.containsKey(CommonConstants.RESP_MESSAGE)?resultJSONObject.getString(CommonConstants.RESP_MESSAGE).replaceAll("[0-9]", "").replaceAll("[\\[|\\]|\\.|\\:]", "").replaceAll("[A-Za-z]", ""):message;
		}
		consumeTaskPOJO.setErrorMessage(errorMessage);
		consumeTaskPOJO.setTaskStatus(2);
		consumeTaskPOJO.setRealAmount(BigDecimal.ZERO);
		consumeTaskPOJO.setReturnMessage(returnMessage);
		consumeTaskPOJOBusiness.save(consumeTaskPOJO);
//		util.pushMessage(consumeTaskPOJO.getUserId(),"有一笔金额为:"+consumeTaskPOJO.getAmount()+"的消费任务失败,可能会影响您的还款金额,请合理分配资金,避免信用卡逾期!",consumeTaskPOJO.getVersion(),consumeTaskPOJO.getCreditCardNumber(),consumeTaskPOJO.getReturnMessage(),consumeTaskPOJO.getOrderCode());
		this.cancelAllTask(consumeTaskPOJO);
	}
	
	private void cancelAllTask(ConsumeTaskPOJO consumeTaskPOJO) {
		try {
			emptyCardApplyOrderBusiness.cancelAllTask(consumeTaskPOJO.getUserId(),consumeTaskPOJO.getCreditCardNumber(),consumeTaskPOJO.getCreateTime(),consumeTaskPOJO.getVersion(),consumeTaskPOJO.getRealAmount());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
