package com.cardmanager.pro.empty.card.manager;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cardmanager.pro.channel.ChannelRoot;
import com.cardmanager.pro.channel.behavior.RepaymentTaskExecutor;
import com.cardmanager.pro.pojo.CreditCardAccount;
import com.cardmanager.pro.pojo.RepaymentBill;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;
import com.cardmanager.pro.util.CardConstss;

import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;

@Component
public class EmptyCardRepaymentTaskExecutor extends ChannelRoot implements RepaymentTaskExecutor {
	
	private static final Logger LOG = LoggerFactory.getLogger(EmptyCardRepaymentTaskExecutor.class);
	
	@Autowired
	private EmptyCardApplyOrderBusiness emptyCardApplyOrderBusiness;

	@Override
	public void executeTask(RepaymentTaskPOJO repaymentTaskPOJO) {
		
		LOG.info("开始执行任务=====" + repaymentTaskPOJO);
		String userId = repaymentTaskPOJO.getUserId();
		String amount = repaymentTaskPOJO.getAmount().toString();
		String rate = repaymentTaskPOJO.getRate().toString();
		String orderCode = repaymentTaskPOJO.getOrderCode();
		String creditCardNumber = repaymentTaskPOJO.getCreditCardNumber();
		String description = repaymentTaskPOJO.getDescription();
		String version = repaymentTaskPOJO.getVersion();
		BigDecimal singleServiceCharge = repaymentTaskPOJO.getServiceCharge();
		String serviceCharge = repaymentTaskPOJO.getTotalServiceCharge().toString();
		String channelTag = repaymentTaskPOJO.getChannelTag();
		String repaymentTaskId = repaymentTaskPOJO.getRepaymentTaskId();
		String realAmount = amount;
		repaymentTaskPOJO.setRealAmount(repaymentTaskPOJO.getAmount());;
		CreditCardAccount creditCardAccount = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(repaymentTaskPOJO.getUserId(), repaymentTaskPOJO.getCreditCardNumber(), repaymentTaskPOJO.getVersion());
		if (BigDecimal.ZERO.compareTo(creditCardAccount.getBlance()) > 0) {
			this.failDealWith(repaymentTaskPOJO,new JSONObject(),"还款失败,帐户可用余额不足",null);
			return;
		}
		if (2 == repaymentTaskPOJO.getTaskType()) {
			RepaymentBill repaymentBill = repaymentBillBusiness.findByCreditCardNumberAndCreateTime(creditCardNumber, repaymentTaskPOJO.getCreateTime());
			if (repaymentBill != null) {
				repaymentBill.setRepaymentedCount(repaymentBill.getRepaymentedCount()+1);
				if (repaymentBill.getRepaymentedCount() >= repaymentBill.getTaskCount()) {
					if (repaymentBill.getTaskStatus() == 2  || repaymentBill.getTaskStatus() == 3) {
						repaymentBill.setTaskStatus(3);
					}else {
						repaymentBill.setTaskStatus(1);
					}
				}
				repaymentBillBusiness.save(repaymentBill);
			}
		}
		orderCode = repaymentTaskId;
		repaymentTaskPOJO.setOrderCode(orderCode);
		JSONObject resultJSONObject;
		try {
			resultJSONObject = addCreditCardOrder(userId, rate, "11", amount, realAmount, creditCardNumber, channelTag, orderCode, singleServiceCharge.toString(), description, "");
		} catch (RuntimeException e) {
			e.printStackTrace();LOG.error("",e);
			this.failDealWith(repaymentTaskPOJO,null,null,e);
			return;
		}
		
		if(!CommonConstants.SUCCESS.equals(resultJSONObject.getString(CommonConstants.RESP_CODE))){
			LOG.error("=============="+orderCode+"==============还款任务生成订单失败:原因:"+(resultJSONObject.containsKey(CommonConstants.RESP_MESSAGE)?resultJSONObject.getString(CommonConstants.RESP_MESSAGE):"生成订单失败"));
			this.failDealWith(repaymentTaskPOJO,resultJSONObject,"原因:生成订单失败",null);
			return;
		}
		
		try {
			resultJSONObject = getUserInfo(userId);
		} catch (RuntimeException e) {
			e.printStackTrace();LOG.error("",e);
			this.failDealWith(repaymentTaskPOJO,null,null,e);
			return;
		}
		resultJSONObject = resultJSONObject.getJSONObject(CommonConstants.RESULT);
		String brandcode = resultJSONObject.getString("brandId");
		
		creditCardAccountBusiness.updateCreditCardAccountAndVersion(userId, creditCardNumber, repaymentTaskPOJO.getRepaymentTaskId(),1,repaymentTaskPOJO.getRealAmount(), description,repaymentTaskPOJO.getVersion(),repaymentTaskPOJO.getCreateTime());
		String extra = "0";
		try {
			resultJSONObject = paymentTopupRequest(realAmount, orderCode, description, userId, brandcode, "0", channelTag,extra);
			LOG.info("======================================支付结果:" + resultJSONObject);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("", e);
			repaymentTaskPOJO.setErrorMessage(e.toString().substring(0, e.toString().length()>=250?250:e.toString().length()));
			repaymentTaskPOJO.setTaskStatus(1);
			repaymentTaskPOJO.setOrderStatus(4);
			repaymentTaskPOJO.setReturnMessage("等待出款中,请稍后!");
			RepaymentTaskPOJO findByRepaymentTaskId = repaymentTaskPOJOBusiness.findByRepaymentTaskId(repaymentTaskPOJO.getRepaymentTaskId());
			if (findByRepaymentTaskId.getOrderStatus().intValue() == 1) {
				repaymentTaskPOJO.setOrderStatus(1);
				repaymentTaskPOJO.setReturnMessage("还款成功!");
			}
			repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
			return;
		}
		
		if(!CommonConstants.SUCCESS.equals(resultJSONObject.getString(CommonConstants.RESP_CODE))){
			LOG.error("=============="+orderCode+"==============还款任务获取用户信息失败:原因:"+(resultJSONObject.containsKey(CommonConstants.RESP_MESSAGE)?resultJSONObject.getString(CommonConstants.RESP_MESSAGE):"支付失败"));
			if(CardConstss.WAIT_NOTIFY.equals(resultJSONObject.getString(CommonConstants.RESP_CODE))){
				repaymentTaskPOJO.setTaskStatus(1);
				repaymentTaskPOJO.setOrderStatus(4);
				repaymentTaskPOJO.setReturnMessage("等待出款中,请稍后!");
				RepaymentTaskPOJO findByRepaymentTaskId = repaymentTaskPOJOBusiness.findByRepaymentTaskId(repaymentTaskPOJO.getRepaymentTaskId());
				if (findByRepaymentTaskId.getOrderStatus().intValue() == 1) {
					repaymentTaskPOJO.setOrderStatus(1);
					repaymentTaskPOJO.setReturnMessage("还款成功!");
				}
				repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
			}else{
				creditCardAccountBusiness.updateCreditCardAccountAndVersion(userId, creditCardNumber,repaymentTaskPOJO.getRepaymentTaskId(), 3, repaymentTaskPOJO.getRealAmount(),"还款失败增加余额",repaymentTaskPOJO.getVersion(),repaymentTaskPOJO.getCreateTime());
				this.failDealWith(repaymentTaskPOJO,resultJSONObject,"支付失败",null);
			}
			return;
		}
		
		repaymentTaskPOJO.setReturnMessage("还款成功!");
		if(repaymentTaskPOJO.getAmount().compareTo(repaymentTaskPOJO.getRealAmount())!=0){
			repaymentTaskPOJO.setReturnMessage("还款成功,未还入预定金额!");
		}
		
		repaymentTaskPOJO.setTaskStatus(1);
		repaymentTaskPOJO.setOrderStatus(1);
		repaymentTaskPOJO.setRealAmount(new BigDecimal(realAmount));
		repaymentTaskPOJO = repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
		LOG.info("修改还款任务成功===================" + repaymentTaskPOJO);
		creditCardAccountBusiness.updateCreditCardAccountAndVersion(userId, creditCardNumber,repaymentTaskPOJO.getRepaymentTaskId(), 4, repaymentTaskPOJO.getRealAmount(), "还款成功减少冻结余额",repaymentTaskPOJO.getVersion(),repaymentTaskPOJO.getCreateTime());
	
		
	}
	
	private void failDealWith(RepaymentTaskPOJO repaymentTaskPOJO,JSONObject resultJSONObject,String message,Exception e){
		String errorMessage = "";
		String returnMessage = "";
		if (e == null) {
			errorMessage = resultJSONObject.containsKey(CommonConstants.RESP_MESSAGE)?resultJSONObject.getString(CommonConstants.RESP_MESSAGE):message;
			returnMessage = resultJSONObject.containsKey(CommonConstants.RESP_MESSAGE)?resultJSONObject.getString(CommonConstants.RESP_MESSAGE).replaceAll("[0-9]", "").replaceAll("[\\[|\\]|\\.|\\:]", "").replaceAll("[A-Za-z]", ""):message;
		}else {
			errorMessage = e.toString().substring(0, e.toString().length()>=250?250:e.toString().length());
			returnMessage = "还款失败!";
		}
		repaymentTaskPOJO.setErrorMessage(errorMessage);
		repaymentTaskPOJO.setTaskStatus(2);
		repaymentTaskPOJO.setRealAmount(BigDecimal.ZERO);
		repaymentTaskPOJO.setReturnMessage(returnMessage);
		repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
		this.cancelAllTask(repaymentTaskPOJO);
//		util.pushMessage(repaymentTaskPOJO.getUserId(),"有一笔金额为:"+repaymentTaskPOJO.getAmount()+"的还款任务失败,系统将在当天23:00前进行还款!",repaymentTaskPOJO.getVersion(),repaymentTaskPOJO.getCreditCardNumber(),repaymentTaskPOJO.getReturnMessage(),repaymentTaskPOJO.getOrderCode());
	}
	
	private void cancelAllTask(RepaymentTaskPOJO repaymentTaskPOJO) {
		try {
			emptyCardApplyOrderBusiness.cancelAllTask(repaymentTaskPOJO.getUserId(),repaymentTaskPOJO.getCreditCardNumber(),repaymentTaskPOJO.getCreateTime(),repaymentTaskPOJO.getVersion(),BigDecimal.ZERO);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
