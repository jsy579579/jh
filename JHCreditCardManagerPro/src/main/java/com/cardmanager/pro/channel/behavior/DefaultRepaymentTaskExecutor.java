package com.cardmanager.pro.channel.behavior;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cardmanager.pro.business.DeductionChargeBusiness;
import com.cardmanager.pro.channel.ChannelRoot;
import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import com.cardmanager.pro.pojo.CreditCardAccount;
import com.cardmanager.pro.pojo.CreditCardAccountHistory;
import com.cardmanager.pro.pojo.DeductionCharge;
import com.cardmanager.pro.pojo.RepaymentBill;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;
import com.cardmanager.pro.util.CardConstss;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.ExceptionUtil;
import net.sf.json.JSONObject;

@Component
public class DefaultRepaymentTaskExecutor extends ChannelRoot implements RepaymentTaskExecutor {
	
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private DeductionChargeBusiness deductionChargeBusiness;

	@Override
	public void executeTask(RepaymentTaskPOJO repaymentTaskPOJO) {
		CreditCardAccount creditCardAccount = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(repaymentTaskPOJO.getUserId(), repaymentTaskPOJO.getCreditCardNumber(), repaymentTaskPOJO.getVersion());
		if(repaymentTaskPOJO.getDescription().contains("系统自动")){
			repaymentTaskPOJO.setRealAmount(repaymentTaskPOJO.getAmount());
		}else {
			repaymentTaskPOJO.setRealAmount(creditCardAccount.getBlance());
		}
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
		String realAmount = repaymentTaskPOJO.getRealAmount().toString();
		String channelTag = repaymentTaskPOJO.getChannelTag();
		String repaymentTaskId = repaymentTaskPOJO.getRepaymentTaskId();
		
		Random random = new Random();
		if(!"0".equals(orderCode)){
			orderCode = DateUtil.getDateStringConvert(new String(), new Date(),"yyyyMMddHHSSS")+random.nextInt(9)+random.nextInt(9)+random.nextInt(9)+random.nextInt(9)+"1";
		}else {
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
		}
		
		if(creditCardAccount != null && !CardConstss.CARD_VERSION_10.equals(version) && !CardConstss.CARD_VERSION_11.equals(version)){
			if(creditCardAccount.getBlance().compareTo(BigDecimal.ZERO) <= 0){
				this.failDealWith(repaymentTaskPOJO,new JSONObject(),"帐户余额为零,无法继续还款",null);
				return;
			}
		}
		
		BigDecimal deductionCharge = BigDecimal.ZERO;
		if (CardConstss.CARD_VERSION_6.equals(version) || CardConstss.CARD_VERSION_60.equals(version)) {
			if (new BigDecimal(amount).compareTo(new BigDecimal(realAmount)) < 0) {
				realAmount = amount;
			}
			deductionCharge = this.version6DeductionCharge(repaymentTaskPOJO);
		}
		
		String extra = "0";
		if (CardConstss.CARD_VERSION_3.equals(version) || CardConstss.CARD_VERSION_5.equals(version) || CardConstss.CARD_VERSION_7.equals(version) || CardConstss.CARD_VERSION_15.equals(version)|| CardConstss.CARD_VERSION_18.equals(version)|| CardConstss.CARD_VERSION_19.equals(version)
		|| CardConstss.CARD_VERSION_25.equals(version) || CardConstss.CARD_VERSION_26.equals(version) || CardConstss.CARD_VERSION_68.equals(version)) {
			if (this.isT1Version3(creditCardAccount)) {
				extra = "T1";
			}
		}else if(CardConstss.CARD_VERSION_6.equals(version) || CardConstss.CARD_VERSION_60.equals(version)){
			extra = "";
			BigDecimal taskAmount = BigDecimal.ZERO;
			List<ConsumeTaskPOJO> consumeTaskPOJOs = consumeTaskPOJOBusiness.findByRepaymentTaskId(repaymentTaskId);
			for (ConsumeTaskPOJO consumeTaskPOJO : consumeTaskPOJOs) {
				if (consumeTaskPOJO.getOrderStatus().intValue() == 1) {
					extra = extra + consumeTaskPOJO.getOrderCode() + ",";
					taskAmount = taskAmount.add(consumeTaskPOJO.getAmount());
				}
			}
			realAmount = taskAmount.add(deductionCharge).toString();
			extra = extra.substring(0, extra.length()-1);
		}else if(CardConstss.CARD_VERSION_10.equals(version) || CardConstss.CARD_VERSION_11.equals(version)) {
			realAmount = amount;
			List<ConsumeTaskPOJO> consumeTaskPOJOs = consumeTaskPOJOBusiness.findByRepaymentTaskId(repaymentTaskId);
			JSONObject json = new JSONObject();
			json.put("XFTask1", consumeTaskPOJOs.get(0));
			json.put("XFTask2", consumeTaskPOJOs.get(1));
			
			repaymentTaskPOJO.setExecuteDateTime(DateUtil.getDateStringConvert(new String(), repaymentTaskPOJO.getExecuteDateTime(), "yyyy-MM-dd HH:mm:ss"));
			json.put("HKTask", repaymentTaskPOJO);
			extra = json.toString();
		}
		
		repaymentTaskPOJO.setRealAmount(new BigDecimal(realAmount));
		if (!CardConstss.CARD_VERSION_10.equals(version) && !CardConstss.CARD_VERSION_11.equals(version)) {
			repaymentTaskPOJO.setExecuteDateTime(DateUtil.getDateStringConvert(new String(), new Date(),"yyyy-MM-dd HH:mm:ss"));
		}
		repaymentTaskPOJO.setOrderCode(orderCode);
		if(!realAmount.equals(amount)){
			amount = realAmount;
		}
		
		String orderRealAmount = realAmount;
		if (CardConstss.CARD_VERSION_3.equals(version) || CardConstss.CARD_VERSION_5.equals(version) || CardConstss.CARD_VERSION_7.equals(version) || CardConstss.CARD_VERSION_8.equals(version) || CardConstss.CARD_VERSION_15.equals(version) || CardConstss.CARD_VERSION_16.equals(version)) {
			orderRealAmount = new BigDecimal(realAmount).add(singleServiceCharge).toString();
		}
		
		JSONObject resultJSONObject;
		try {
			resultJSONObject = addCreditCardOrder(userId, rate, "11", amount, orderRealAmount, creditCardNumber, channelTag, orderCode, singleServiceCharge.toString(), description, "");
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
		
		
		if(!CommonConstants.SUCCESS.equals(resultJSONObject.getString(CommonConstants.RESP_CODE))){
			LOG.error("=============="+orderCode+"==============还款任务获取用户信息失败:原因:"+(resultJSONObject.containsKey(CommonConstants.RESP_MESSAGE)?resultJSONObject.getString(CommonConstants.RESP_MESSAGE):"获取用户信息失败"));
			this.failDealWith(repaymentTaskPOJO,resultJSONObject,"原因:获取用户信息失败",null);
			return;
		}
		resultJSONObject = resultJSONObject.getJSONObject(CommonConstants.RESULT);
		String brandcode = resultJSONObject.getString("brandId");
		
		creditCardAccountBusiness.updateCreditCardAccountAndVersion(userId, creditCardNumber, repaymentTaskPOJO.getRepaymentTaskId(),1,repaymentTaskPOJO.getRealAmount().subtract(deductionCharge), description,repaymentTaskPOJO.getVersion(),repaymentTaskPOJO.getCreateTime());
	
		try {
			resultJSONObject = paymentTopupRequest(realAmount, orderCode, description, userId, brandcode, "0", channelTag,extra);
			LOG.info("======================================支付结果:" + resultJSONObject);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("",e);
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
		
		if (CardConstss.CARD_VERSION_6.equals(version) || CardConstss.CARD_VERSION_60.equals(version)) {
			try {
				deductionChargeBusiness.updateAndDel(repaymentTaskPOJO);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
		//util.pushMessage(repaymentTaskPOJO.getUserId(),"有一笔金额为:"+repaymentTaskPOJO.getAmount()+"的还款任务失败,系统将在当天23:00前进行还款!",repaymentTaskPOJO.getVersion(),repaymentTaskPOJO.getCreditCardNumber(),repaymentTaskPOJO.getReturnMessage(),repaymentTaskPOJO.getOrderCode());
		if (!repaymentTaskPOJO.getDescription().contains("系统自动")) {
			util.pushMessage(repaymentTaskPOJO.getUserId(), "有一笔金额为:" + repaymentTaskPOJO.getAmount() + "的还款任务失败,系统将在当天23:00前进行自动还款!", repaymentTaskPOJO.getVersion(), repaymentTaskPOJO.getCreditCardNumber(), repaymentTaskPOJO.getReturnMessage(), repaymentTaskPOJO.getOrderCode());
		}
		if (repaymentTaskPOJO.getTaskType().intValue() == 2 && !CardConstss.CARD_VERSION_10.equals(repaymentTaskPOJO.getVersion()) && !CardConstss.CARD_VERSION_11.equals(repaymentTaskPOJO.getVersion()) && !CardConstss.CARD_VERSION_6.equals(repaymentTaskPOJO.getVersion()) && !CardConstss.CARD_VERSION_60.equals(repaymentTaskPOJO.getVersion())) {
			repaymentTaskPOJOBusiness.createNewRepaymentTaskPOJO(repaymentTaskPOJO);
		}
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
	
	private BigDecimal version6DeductionCharge(RepaymentTaskPOJO repaymentTaskPOJO) {
		if (CardConstss.CARD_VERSION_6.equals(repaymentTaskPOJO.getVersion())) {
			try {
				DeductionCharge deductionCharge = deductionChargeBusiness.findByCreditCardNumber(repaymentTaskPOJO.getCreditCardNumber());
				if (deductionCharge != null) {
					if (deductionCharge.getDeductionAmount().compareTo(BigDecimal.ZERO) > 0) {
						if (deductionCharge.getDeductionAmount().compareTo(repaymentTaskPOJO.getServiceCharge()) >= 0) {
							return repaymentTaskPOJO.getServiceCharge();
						}else {
							return deductionCharge.getDeductionAmount();
						}
					}else {
						return BigDecimal.ZERO;
					}
				}
			} catch (Exception e) {
				return BigDecimal.ZERO;
			}
		}
		return BigDecimal.ZERO;
	}

}
