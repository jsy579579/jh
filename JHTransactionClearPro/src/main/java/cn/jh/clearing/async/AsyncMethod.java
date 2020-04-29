package cn.jh.clearing.async;

import java.math.BigDecimal;
import java.util.Date;

import cn.jh.clearing.service.BrandScheduleService;
import cn.jh.clearing.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import cn.jh.clearing.business.NotifyOrderBusiness;
import cn.jh.clearing.pojo.NotifyOrder;
import cn.jh.clearing.pojo.PaymentOrder;


@Component
@Lazy(true)
public class AsyncMethod{
	
	@Autowired
	private PaymentService paymentService;

	@Autowired
	private NotifyOrderBusiness notifyOrderBusiness;

	@Autowired
	private BrandScheduleService brandScheduleService;
	
	@Async
	public void rebate(String ordercode, long userid, long channelId, BigDecimal amount, BigDecimal currate,long brandid, String orderType) {
		paymentService.rebate(ordercode, userid, channelId, amount, currate, brandid, orderType);
	}
	
	@Async
	public void brandClearing(PaymentOrder paymentOrder) {
		brandScheduleService.brandClearing(paymentOrder);
	}

	@Async
	public void sendNotifyOrder(NotifyOrder notifyOrder) {
		try {
			LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<>();
			requestEntity.add("data", notifyOrder.getContent());
			String resultString = new RestTemplate().postForObject(notifyOrder.getNotifyUrl(), requestEntity, String.class);
			System.out.println(resultString);
			if ("success".equals(resultString)) {
				notifyOrderBusiness.delete(notifyOrder);
			}else {
				this.dealNotifyOrder(notifyOrder);
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.dealNotifyOrder(notifyOrder);
		}
		
	}
	
	private void dealNotifyOrder(NotifyOrder notifyOrder) {
		int count = notifyOrder.getCount();
		if (count < 9) {
			long time = 0l;
			for (int i = 0; i < count+1; i++) {
				if (i == 0) {
					time = 60000l;
				}
				time = time*2;
			}
			notifyOrder.setNotifyTime(new Date(notifyOrder.getNotifyTime().getTime()+time));
			notifyOrder.setCount(count+1);
			notifyOrderBusiness.save(notifyOrder);
		}else {
			notifyOrderBusiness.delete(notifyOrder);
		}
	}
	
}
