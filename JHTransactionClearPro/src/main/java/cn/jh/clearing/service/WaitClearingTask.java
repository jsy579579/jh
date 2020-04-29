package cn.jh.clearing.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.jh.clearing.business.PaymentOrderBusiness;
import cn.jh.clearing.pojo.PaymentOrder;
import cn.jh.clearing.util.Util;
import cn.jh.common.utils.ExceptionUtil;

@Configuration
@Controller
@EnableScheduling
public class WaitClearingTask {

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Autowired
	private PaymentOrderBusiness paymentOrderBusiness;

	@Autowired
	Util util;

	@Autowired
	private PaymentService paymentService;

	@Value("${schedule-task.on-off}")
	private String scheduleTaskOnOff;

	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/transactionclear/test1")
	public @ResponseBody Object test() {
		scheduler();
		return "OK";
	}

	/**
	 * 周一早上10
	 * 
	 */
	@Scheduled(cron = "0 10 0 ? * MON-FRI")
	public void scheduler() {
		if ("true".equals(scheduleTaskOnOff)) {
			List<PaymentOrder> paymentorders = paymentOrderBusiness.queryWaitClearingOrders();
			int allCount = 0;
			if (paymentorders != null) {
				allCount = paymentorders.size();
			}
			LOG.info("开始执行。。。。。。。。。。。。。。。。。。。。。。。。。。。WaitClearingTask:订单总数:" + allCount);
			int successCount = 0;
			int failedCount = 0;
			for (PaymentOrder order : paymentorders) {
				LOG.info("修改订单开始========================================订单号" + order.getOrdercode());
				try {
					paymentService.updatePaymentOrder(null, order.getOrdercode(), "1", null, null);
				} catch (Exception e) {
					e.printStackTrace();LOG.error("",e);
					LOG.error(
							"修改订单失败=====================================失败订单号:" + order.getOrdercode() + ",失败原因:");
					e.printStackTrace();LOG.error("",e);
					failedCount += 1;
					continue;
				}
				// /** 根据的用户手机号码查询用户的基本信息 */
				// URI uri = util.getServiceUrl("transactionclear", "error url
				// request!");
				// String url = uri.toString() +
				// "/v1.0/transactionclear/payment/update";
				// RestTemplate restTemplate = new RestTemplate();
				// MultiValueMap<String, String> requestEntity = new
				// LinkedMultiValueMap<String, String>();
				// requestEntity.add("order_code", order.getOrdercode());
				// requestEntity.add("status", "1");
				// String result = restTemplate.postForObject(url,
				// requestEntity, String.class);
				LOG.info("修改订单结束" + order.getOrdercode());
				successCount += 1;
			}
			LOG.info("==========================成功数:" + successCount);
			LOG.info("==========================失败数:" + failedCount);
		}
	}
}
