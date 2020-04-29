package com.cardmanager.pro.empty.card.manager;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cardmanager.pro.channel.ChannelRoot;
import com.cardmanager.pro.channel.behavior.RepaymentTaskCheckor;
import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;
import com.cardmanager.pro.scanner.ConsumeTaskScanner;
import com.cardmanager.pro.service.CreditCardManagerTaskService;
import com.cardmanager.pro.util.CardConstss;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import net.sf.json.JSONObject;

@Component
public class EmptyCardRepaymentTaskCheckor extends ChannelRoot implements RepaymentTaskCheckor {

	private static final Logger LOG = LoggerFactory.getLogger(EmptyCardRepaymentTaskCheckor.class);
	
	@Autowired
	private CreditCardManagerTaskService creditCardManagerTaskService;
	
	@Autowired
	private EmptyCardApplyOrderBusiness emptyCardApplyOrderBusiness;
	
	@Autowired
	private ConsumeTaskScanner consumeTaskScanner;
	
	@Override
	public void checkTask(RepaymentTaskPOJO repaymentTaskPOJO) {
		JSONObject resultJSON = null;
		String respCode = "";
		String respMessage = "";
		try {
			resultJSON = this.getOrderStatusByVersion(repaymentTaskPOJO.getOrderCode(),  CommonConstants.ORDER_TYPE_REPAYMENT, repaymentTaskPOJO.getVersion());
			LOG.info("repaymentTaskPOJO查询结果====" + resultJSON + "|" + repaymentTaskPOJO.toString());
			respCode = resultJSON.getString(CommonConstants.RESP_CODE);
			respMessage = (resultJSON.containsKey(CommonConstants.RESP_MESSAGE)?resultJSON.getString(CommonConstants.RESP_MESSAGE):"还款失败!");
		} catch (RuntimeException e) {
			LOG.info("查询异常.将该笔还款任务修改为失败=====" + repaymentTaskPOJO.toString());
			e.printStackTrace();
			respCode = CardConstss.WAIT_NOTIFY;
			respMessage = "查询异常,出款中...";
		}
		Date orderExecuteTime = DateUtil.getDateStringConvert(new Date(), repaymentTaskPOJO.getExecuteDateTime(), "yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR_OF_DAY, -3);
		if( CommonConstants.FALIED.equals(respCode)){
			this.failed(repaymentTaskPOJO, orderExecuteTime, respMessage, resultJSON);
		}else if(CardConstss.WAIT_NOTIFY.equals(respCode) && calendar.getTime().compareTo(orderExecuteTime) > 0) {
			calendar = Calendar.getInstance();
			calendar.add(Calendar.HOUR_OF_DAY, -24);
			if (calendar.getTime().compareTo(orderExecuteTime) > 0 && !CardConstss.CARD_VERSION_8.equals(repaymentTaskPOJO.getVersion())) {
				this.failed(repaymentTaskPOJO, orderExecuteTime, respMessage, resultJSON);
			}else {
				repaymentTaskPOJO.setOrderStatus(5);
				repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
			}
		}else if (CommonConstants.SUCCESS.equals(respCode)) {
			LOG.info(repaymentTaskPOJO.toString() + "将该还款任务修改为成功" + "|查询订单结果为:" + resultJSON);
			creditCardManagerTaskService.updateTaskStatusByOrderCode(null, repaymentTaskPOJO.getOrderCode(),repaymentTaskPOJO.getVersion());
			this.updatePaymentOrderByOrderCode(repaymentTaskPOJO.getOrderCode());
			this.updateApplyOrderRepaymentAmount(repaymentTaskPOJO);
			List<ConsumeTaskPOJO> repaymentTaskId = this.consumeTaskPOJOBusiness.findByRepaymentTaskId(repaymentTaskPOJO.getRepaymentTaskId());
			for (ConsumeTaskPOJO consumeTaskPOJO : repaymentTaskId) {
				if (consumeTaskPOJO.getTaskStatus().intValue() == 0) {
					consumeTaskScanner.addConsumeTaskToPool(consumeTaskPOJO);
				}
			}
		}
	}
	
	private void failed(RepaymentTaskPOJO repaymentTaskPOJO,Date orderExecuteTime,String respMessage,JSONObject resultJSON) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, -15);
		if (calendar.getTime().compareTo(orderExecuteTime) > 0) {
			LOG.info(repaymentTaskPOJO.toString() + "将该还款任务修改为失败" + "|查询订单结果为:" + resultJSON);
			repaymentTaskPOJO.setTaskStatus(2);
			repaymentTaskPOJO.setOrderStatus(0);
			repaymentTaskPOJO.setRealAmount(BigDecimal.ZERO);
			repaymentTaskPOJO.setReturnMessage(respMessage);
			repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
			creditCardAccountBusiness.updateCreditCardAccountAndVersion(repaymentTaskPOJO.getUserId(), repaymentTaskPOJO.getCreditCardNumber(),repaymentTaskPOJO.getRepaymentTaskId(),3,repaymentTaskPOJO.getRealAmount(),"还款失败增加余额",repaymentTaskPOJO.getVersion(),repaymentTaskPOJO.getCreateTime());
//			util.pushMessage(repaymentTaskPOJO.getUserId(),"有一笔金额为:"+repaymentTaskPOJO.getAmount()+"的还款任务失败,系统将在当天23:00前进行还款!",repaymentTaskPOJO.getVersion(),repaymentTaskPOJO.getCreditCardNumber(),repaymentTaskPOJO.getReturnMessage(),repaymentTaskPOJO.getOrderCode());
			this.cancelAllTask(repaymentTaskPOJO);
		}
	}
	
	private void cancelAllTask(RepaymentTaskPOJO repaymentTaskPOJO) {
		try {
			emptyCardApplyOrderBusiness.cancelAllTask(repaymentTaskPOJO.getUserId(),repaymentTaskPOJO.getCreditCardNumber(),repaymentTaskPOJO.getCreateTime(),repaymentTaskPOJO.getVersion(),BigDecimal.ZERO);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateApplyOrderRepaymentAmount(RepaymentTaskPOJO repaymentTaskPOJO) {
		EmptyCardApplyOrder emptyCardApplyOrder = emptyCardApplyOrderBusiness.findByCreditCardNumberAndCreateTime(repaymentTaskPOJO.getCreditCardNumber(),DateUtil.getDateStringConvert(new Date(), repaymentTaskPOJO.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
		if (emptyCardApplyOrder != null) {
			emptyCardApplyOrder.setRepaymentedAmount(emptyCardApplyOrder.getRepaymentedAmount().add(repaymentTaskPOJO.getAmount()));
			emptyCardApplyOrderBusiness.save(emptyCardApplyOrder);
		}
	}
	
}
