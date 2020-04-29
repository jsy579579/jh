package com.cardmanager.pro.channel.behavior;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cardmanager.pro.channel.ChannelRoot;
import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;
import com.cardmanager.pro.service.CreditCardManagerTaskService;
import com.cardmanager.pro.util.CardConstss;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import net.sf.json.JSONObject;

@Component
public class DefaultConsumeTaskCheckor extends ChannelRoot implements ConsumeTaskCheckor {
	
	private static final Logger LOG = LoggerFactory.getLogger(DefaultConsumeTaskCheckor.class);

	@Autowired
	private CreditCardManagerTaskService creditCardManagerTaskService;
	
	@Override
	public void checkTask(ConsumeTaskPOJO consumeTaskPOJO) {
		JSONObject resultJSON=null;
		String respCode = "";
			String respMessage = "";
			try {
				resultJSON = this.getOrderStatusByVersion(consumeTaskPOJO.getOrderCode(), CommonConstants.ORDER_TYPE_CONSUME, consumeTaskPOJO.getVersion());
				LOG.info("consumeTaskPOJO查询结果====" + resultJSON + "|" + consumeTaskPOJO.toString());
				respCode = resultJSON.getString(CommonConstants.RESP_CODE);
				respMessage = (resultJSON.containsKey(CommonConstants.RESP_MESSAGE)?resultJSON.getString(CommonConstants.RESP_MESSAGE):"扣款失败!");
			} catch (RuntimeException e) {
				LOG.info("查询异常.将该笔消费任务修改为失败=====" + consumeTaskPOJO.toString());
				e.printStackTrace();
				respCode = CardConstss.WAIT_NOTIFY;
				respMessage = "查询异常,支付处理中...";
		}
		Date orderExecuteTime = DateUtil.getDateStringConvert(new Date(), consumeTaskPOJO.getExecuteDateTime(), "yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR_OF_DAY, -3);
		if(CommonConstants.FALIED.equals(respCode) || (CardConstss.CARD_VERSION_6.equals(consumeTaskPOJO.getVersion()) || CardConstss.CARD_VERSION_60.equals(consumeTaskPOJO.getVersion())&& CardConstss.WAIT_NOTIFY.equals(respCode) && calendar.getTime().compareTo(orderExecuteTime) > 0)){
			this.failed(consumeTaskPOJO,orderExecuteTime, respMessage, resultJSON);
		}else if(CardConstss.WAIT_NOTIFY.equals(respCode)  && calendar.getTime().compareTo(orderExecuteTime) > 0) {
			calendar = Calendar.getInstance();
			calendar.add(Calendar.HOUR_OF_DAY, -24);
			if (calendar.getTime().compareTo(orderExecuteTime) > 0 && !CardConstss.CARD_VERSION_8.equals(consumeTaskPOJO.getVersion())) {
				this.failed(consumeTaskPOJO,orderExecuteTime, respMessage, resultJSON);
			}else {
				consumeTaskPOJO.setOrderStatus(5);
				consumeTaskPOJOBusiness.save(consumeTaskPOJO);
			}
		}else if (CommonConstants.SUCCESS.equals(respCode)) {
			LOG.info(consumeTaskPOJO.toString() + "将该消费任务修改为成功" + "|查询订单结果为:" + resultJSON);
			creditCardManagerTaskService.updateTaskStatusByOrderCode(null, consumeTaskPOJO.getConsumeTaskId(), consumeTaskPOJO.getVersion());
			this.updatePaymentOrderByOrderCode(consumeTaskPOJO.getConsumeTaskId());
//			LOG.info("修改订单状态结果:" + updateOrderJSONObject.toString());
		}
	}
	
	private void failed(ConsumeTaskPOJO consumeTaskPOJO,Date orderExecuteTime,String respMessage,JSONObject resultJSON) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, -15);
		if (calendar.getTime().compareTo(orderExecuteTime) > 0) {
			LOG.info(consumeTaskPOJO.toString() + "将该消费任务修改为失败" + "|查询订单结果为:" + resultJSON);
			consumeTaskPOJO.setOrderStatus(0);
			consumeTaskPOJO.setTaskStatus(2);
			consumeTaskPOJO.setRealAmount(BigDecimal.ZERO);
			consumeTaskPOJO.setReturnMessage(respMessage);
			consumeTaskPOJOBusiness.save(consumeTaskPOJO);
			String consumeTaskId = consumeTaskPOJO.getConsumeTaskId();
			if("2".equals(consumeTaskId.substring(consumeTaskId.length()-1))){
				consumeTaskPOJOBusiness.updateTaskStatus4AndReturnMessageByRepaymentTaskId(consumeTaskPOJO.getRepaymentTaskId(),"还款任务中首笔消费失败,无法继续执行该笔任务");
				this.updateConsumeService(consumeTaskId, consumeTaskPOJO.getRepaymentTaskId());
			}else {
				if (consumeTaskPOJO.getServiceCharge().compareTo(BigDecimal.ZERO) <= 0) {
					RepaymentTaskPOJO repaymentTaskPOJO = repaymentTaskPOJOBusiness.findByRepaymentTaskId(consumeTaskPOJO.getRepaymentTaskId());
					BigDecimal consumeReturnServiceCharge = consumeTaskPOJO.getAmount().multiply(repaymentTaskPOJO.getRate());
					repaymentTaskPOJO.setTotalServiceCharge(repaymentTaskPOJO.getTotalServiceCharge().subtract(consumeReturnServiceCharge).setScale(2, BigDecimal.ROUND_DOWN));
					repaymentTaskPOJO.setReturnServiceCharge(repaymentTaskPOJO.getReturnServiceCharge().add(consumeReturnServiceCharge).setScale(2, BigDecimal.ROUND_DOWN));
					repaymentTaskPOJO = repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
					creditCardAccountBusiness.updateCreditCardAccountAndVersion(repaymentTaskPOJO.getUserId(), repaymentTaskPOJO.getCreditCardNumber(), consumeTaskPOJO.getConsumeTaskId(),5, consumeReturnServiceCharge, "消费失败退还手续费",repaymentTaskPOJO.getVersion(),repaymentTaskPOJO.getCreateTime());
				}
			}
			util.pushMessage(consumeTaskPOJO.getUserId(),"有一笔金额为:"+consumeTaskPOJO.getAmount()+"的消费任务失败,可能会影响您的还款金额,请合理分配资金,避免信用卡逾期!",consumeTaskPOJO.getVersion(),consumeTaskPOJO.getCreditCardNumber(),consumeTaskPOJO.getReturnMessage(),consumeTaskPOJO.getOrderCode());
		}
	}
	
	private void updateConsumeService(String consumeTaskId,String repaymentTaskId){
		ConsumeTaskPOJO consumeTaskPOJO = consumeTaskPOJOBusiness.findByConsumeTaskId(consumeTaskId);
		RepaymentTaskPOJO repaymentTaskPOJO = repaymentTaskPOJOBusiness.findByRepaymentTaskId(repaymentTaskId);
		repaymentTaskPOJO.setTotalServiceCharge(BigDecimal.ZERO);
		repaymentTaskPOJO = repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
		consumeTaskPOJO.setServiceCharge(BigDecimal.ZERO);
		consumeTaskPOJO = consumeTaskPOJOBusiness.save(consumeTaskPOJO);
	}

}
