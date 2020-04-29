package com.cardmanager.pro.scanner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cardmanager.pro.business.ConsumeTaskPOJOBusiness;
import com.cardmanager.pro.business.CreditCardAccountBusiness;
import com.cardmanager.pro.business.CreditCardManagerConfigBusiness;
import com.cardmanager.pro.business.RepaymentTaskPOJOBusiness;
import com.cardmanager.pro.config.PropertiesConfig;
import com.cardmanager.pro.executor.BaseExecutor;
import com.cardmanager.pro.executor.ThreadConsumeCheckor;
import com.cardmanager.pro.executor.ThreadConsumeExecutor;
import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import com.cardmanager.pro.pojo.CreditCardAccount;
import com.cardmanager.pro.pojo.CreditCardManagerConfig;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;
import com.cardmanager.pro.service.CreditCardManagerTaskService;
import com.cardmanager.pro.util.CardConstss;
import com.cardmanager.pro.util.SpringContextUtil;

import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import net.sf.json.JSONObject;

@Configuration
@Controller
@EnableScheduling
public class ConsumeTaskScanner extends ApplicationObjectSupport{

	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private RepaymentTaskPOJOBusiness repaymentTaskPOJOBusiness;
	
	@Autowired
	private ConsumeTaskPOJOBusiness consumeTaskPOJOBusiness;
	
	@Autowired
	private CreditCardAccountBusiness creditCardAccountBusiness;
	
	@Autowired
	private CreditCardManagerConfigBusiness creditCardManagerConfigBusiness;
	
	@Autowired
	private CreditCardManagerTaskService creditCardManagerTaskService;
	
	@Autowired
	private BaseExecutor baseExecutor;
	
	@Autowired
	private PropertiesConfig propertiesConfig;
	
	private static volatile List<ConsumeTaskPOJO> consumeTaskPOJOList = new ArrayList<>();
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/creditcardmanager/test")
	public @ResponseBody Object test(HttpServletRequest request,
			@RequestParam(value="version")String version
			){
		List<ConsumeTaskPOJO> consumeTaskPOJOs = this.findTaskType2AndTaskStatus0RepaymentTask(version);
		this.executeTask(consumeTaskPOJOs);
		return "OK";
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/creditcardmanager/consume/add")
	public @ResponseBody Object test2(HttpServletRequest request,
			@RequestParam(value="version")String version
			){
		int page = 0;
		List<ConsumeTaskPOJO> findAccountLessTen = new ArrayList<>();
		do{
			Pageable pageable = new PageRequest(page, 20, new Sort(Sort.Direction.DESC, "blance"));
			findAccountLessTen = this.findAccountLessTen(version,pageable);
			if (findAccountLessTen != null && findAccountLessTen.size() > 0) {
				this.executeTask(findAccountLessTen);
			}
			page += 1;
		}while(findAccountLessTen != null && findAccountLessTen.size() > 0);
		return "OK";
	}
	
	@Scheduled(cron = "0/20 * * * * ?")
	public void autoExecuteTask() {
		this.executeTask();
	}
	
	private void executeTask() {
		if("true".equals(propertiesConfig.getScanOnOff())){
			LOG.info("当前待执行消费任务数量==============================" + consumeTaskPOJOList.size());
			ApplicationContext applicationContext = this.getApplicationContext();
			ThreadConsumeExecutor threadConsumeExecutor = null;
			ConsumeTaskPOJO consumeTaskPOJO = null;
			for(int i = 0;i < 80;i++){
				if (consumeTaskPOJOList.size() == 0) {
					break;
				}
				consumeTaskPOJO = consumeTaskPOJOList.get(0);
				threadConsumeExecutor = applicationContext.getBean(ThreadConsumeExecutor.class);
				threadConsumeExecutor.setConsumeTaskPOJO(consumeTaskPOJO);
				new Thread(threadConsumeExecutor).start();
				LOG.info("执行还款任务==============================" + consumeTaskPOJO);
				consumeTaskPOJOList.remove(consumeTaskPOJO);
			}
		}
	}
	
	@Scheduled(cron = "0 0/1 * * * ?")
	public void autoScanRepaymentTask() {
		this.readyExecuteTask();
	}
	
	private void readyExecuteTask() {
		if("true".equals(propertiesConfig.getScanOnOff())){
			List<CreditCardManagerConfig> configs = this.getConfig();
			for (CreditCardManagerConfig creditCardManagerConfig : configs) {
				if (1 == creditCardManagerConfig.getScanOnOff() && 1 == creditCardManagerConfig.getConsumeOnOff()) {
					LOG.info("====================version"+creditCardManagerConfig.getVersion()+"消费任务开始执行====================");
					List<ConsumeTaskPOJO> consumeTaskPOJOs = this.findTaskType2AndTaskStatus0RepaymentTask(creditCardManagerConfig.getVersion());
					if (consumeTaskPOJOs != null && consumeTaskPOJOs.size() > 0 && consumeTaskPOJOList.size() <= 400) {
						int count = 0;
						for (ConsumeTaskPOJO consumeTaskPOJO : consumeTaskPOJOs) {
							if (consumeTaskPOJOList.size() > 400 || count > 40) {
								break;
							}
							consumeTaskPOJOList.add(consumeTaskPOJO);
							consumeTaskPOJO.setTaskStatus(3);
							consumeTaskPOJOBusiness.save(consumeTaskPOJO);
							count++;
						} 
					}
				}
			}
		}
	}
	
	public void addConsumeTaskToPool(ConsumeTaskPOJO consumeTaskPOJO) {
		consumeTaskPOJOList.add(consumeTaskPOJO);
		consumeTaskPOJO.setTaskStatus(3);
		consumeTaskPOJOBusiness.save(consumeTaskPOJO);
	}
	
	@Scheduled(cron = "0 0/8 * * * ?")
	public void autoUpdateTaskType0OrderStatus4ConsumeTask(){
		if ("true".equals(propertiesConfig.getScanOnOff())) {
			List<CreditCardManagerConfig> configs = this.getConfig();
			for (CreditCardManagerConfig creditCardManagerConfig : configs) {
				if (1 == creditCardManagerConfig.getScanOnOff()  && 1 == creditCardManagerConfig.getConsumeOnOff()) {
					LOG.info("====================version"+creditCardManagerConfig.getVersion()+"修改首笔消费待完成任务状态开始执行====================");
					this.checkTaskType0ConsumeTaskOrderStatusByVersion(creditCardManagerConfig.getVersion());
				}
			}
		}
	}
	
	@Scheduled(cron = "0 0/4 * * * ?")
	public void autoUpdateTaskType2OrderStatus4ConsumeTask(){
		if ("true".equals(propertiesConfig.getScanOnOff())) {
			List<CreditCardManagerConfig> configs = this.getConfig();
			for (CreditCardManagerConfig creditCardManagerConfig : configs) {
				if (1 == creditCardManagerConfig.getScanOnOff()  && 1 == creditCardManagerConfig.getConsumeOnOff()) {
					LOG.info("====================修改version"+creditCardManagerConfig.getVersion()+"消费待完成任务状态开始执行====================");
					List<ConsumeTaskPOJO> consumeTaskPOJOs = consumeTaskPOJOBusiness.findByTaskTypeAndOrderStatusAndVersion(2,4,creditCardManagerConfig.getVersion());
					this.checkConsumeTaskPOJO(consumeTaskPOJOs);
				}
			}
		}
	}
	//2019.7.9更新定时器扫描时间间隔
	//@Scheduled(cron = "0 0 0/3 * * ?")
	@Scheduled(cron = "0 0/5 * * * ?")
	public void autoUpdateTaskType2OrderStatus5ConsumeTask(){
		if ("true".equals(propertiesConfig.getScanOnOff())) {
			List<CreditCardManagerConfig> configs = this.getConfig();
			for (CreditCardManagerConfig creditCardManagerConfig : configs) {
				if (1 == creditCardManagerConfig.getScanOnOff()  && 1 == creditCardManagerConfig.getConsumeOnOff()) {
					LOG.info("====================修改version"+creditCardManagerConfig.getVersion()+"消费待完成任务状态开始执行====================");
					List<ConsumeTaskPOJO> consumeTaskPOJOs = consumeTaskPOJOBusiness.findByTaskTypeAndOrderStatusAndVersion(2,5,creditCardManagerConfig.getVersion());
					this.checkConsumeTaskPOJO(consumeTaskPOJOs);
				}
			}
		}
	}
	
	private List<CreditCardManagerConfig> getConfig(){
		List<CreditCardManagerConfig> creditCardManagerConfigs = creditCardManagerConfigBusiness.findAll();
		return creditCardManagerConfigs;
	}
	
//	获取当天及当前时间之前批量生成的未执行消费任务
	public List<ConsumeTaskPOJO> findTaskType2AndTaskStatus0RepaymentTask(String version){
		List<ConsumeTaskPOJO> consumeTaskPOJOs = consumeTaskPOJOBusiness.findTaskType2AndTaskStatus0RepaymentTaskAndVersion(version);
		return consumeTaskPOJOs;
	}
	
	public List<ConsumeTaskPOJO> findAccountLessTen(String version,Pageable pageable){
		List<CreditCardAccount> creditCardAccounts = creditCardAccountBusiness.findCreditCardAccountByBlanceLessTenAndVersion(version,pageable);
		List<ConsumeTaskPOJO> consumeTaskPOJOs = null;
		if(creditCardAccounts != null && creditCardAccounts.size() > 0){
			consumeTaskPOJOs = new ArrayList<>();
			ConsumeTaskPOJO consumeTaskPOJO = null;
			for(CreditCardAccount creditCardAccount:creditCardAccounts){
				consumeTaskPOJO = this.genreateConsumeTaskPOJO(creditCardAccount,version);
				if(consumeTaskPOJO != null){
					consumeTaskPOJOs.add(consumeTaskPOJO);
				}
			}
		}
		return consumeTaskPOJOs;
	}
	
	public void executeTask(List<ConsumeTaskPOJO> consumeTaskPOJOs){
		ApplicationContext applicationContext = this.getApplicationContext();
		ThreadConsumeExecutor threadConsumeExecutor = null;
		ConsumeTaskPOJO consumeTaskPOJO = null;
		for(int i = 0;i < consumeTaskPOJOs.size();i++){
			consumeTaskPOJO = consumeTaskPOJOs.get(i);
			threadConsumeExecutor = applicationContext.getBean(ThreadConsumeExecutor.class);
			threadConsumeExecutor.setConsumeTaskPOJO(consumeTaskPOJO);
			new Thread(threadConsumeExecutor).start();
			if( i != 0 && i % 30 ==0){
				long time = System.currentTimeMillis();
				boolean isTrue = true;
				while(isTrue){
					if(System.currentTimeMillis() - time >= 10000){
						isTrue = false;
					}
				}
			}
		}
	}
	
	private ConsumeTaskPOJO genreateConsumeTaskPOJO(CreditCardAccount creditCardAccount,String version){
		BigDecimal amount = creditCardAccount.getBlance();
		ConsumeTaskPOJO consumeTaskPOJO = null;
		if(BigDecimal.valueOf(-10).compareTo(amount) > 0){
			String userId = creditCardAccount.getUserId();
			String creditCardNumber = creditCardAccount.getCreditCardNumber();
			String description = "尾号" + creditCardNumber.substring(creditCardNumber.length() - 4) + "|系统冲正任务";
			String createTime = null;
			String channelId = null;
			String channelTag = null;
			BigDecimal serviceCharge = BigDecimal.ZERO;
			BigDecimal rate = BigDecimal.ZERO;
			BigDecimal returnServiceCharge = BigDecimal.ZERO;
			createTime = DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss");
			channelId = "101";
			channelTag = "WF_QUICK";
			consumeTaskPOJO = consumeTaskPOJOBusiness.createNewConsumeTaskPOJO(amount, userId,creditCardNumber, 1, description, createTime, serviceCharge, rate, returnServiceCharge, channelId,channelTag,version);
			
		}
		return consumeTaskPOJO;
	}
	
//	修改Version2首笔消费失败的任务状态
	private void checkTaskType0ConsumeTaskOrderStatusByVersion(String version){
		List<ConsumeTaskPOJO> consumeTaskPOJOs = consumeTaskPOJOBusiness.findByTaskTypeAndOrderStatusAndVersion(0,4,version);
		if(consumeTaskPOJOs != null && consumeTaskPOJOs.size() > 0){
			for(ConsumeTaskPOJO consumeTaskPOJO:consumeTaskPOJOs){
				JSONObject resultJSON = null;
				String respCode = "";
				String respMessage = "";
				try {
					resultJSON = baseExecutor.getOrderStatusByVersion(consumeTaskPOJO.getOrderCode(), CommonConstants.ORDER_TYPE_CONSUME, consumeTaskPOJO.getVersion());
					respCode = resultJSON.getString(CommonConstants.RESP_CODE);
					respMessage = (resultJSON.containsKey(CommonConstants.RESP_MESSAGE)?resultJSON.getString(CommonConstants.RESP_MESSAGE):"扣款失败!");
				} catch (RuntimeException e) {
					LOG.info("查询异常.将该首笔消费任务修改为失败=====" + consumeTaskPOJO.toString());
					e.printStackTrace();
					respCode = CommonConstants.FALIED;
					respMessage = "扣款失败!";
				}
				Date orderExecuteTime = DateUtil.getDateStringConvert(new Date(), consumeTaskPOJO.getExecuteDateTime(), "yyyy-MM-dd HH:mm:ss");
				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY)-3);
				
				if(CommonConstants.FALIED.equals(respCode) || (CardConstss.WAIT_NOTIFY.equals(respCode) && calendar.getTime().compareTo(orderExecuteTime) > 0)){
					calendar = Calendar.getInstance();
					calendar.add(Calendar.MINUTE, -15);
					if (calendar.getTime().compareTo(orderExecuteTime) > 0) {
						LOG.info(consumeTaskPOJO.toString() + "将该首笔消费任务修改为失败" + "|查询订单结果为:" + resultJSON);
						consumeTaskPOJO.setOrderStatus(0);
						consumeTaskPOJO.setTaskStatus(0);
						consumeTaskPOJO.setReturnMessage(respMessage);
						consumeTaskPOJOBusiness.save(consumeTaskPOJO);
					}
				}else if (CommonConstants.SUCCESS.equals(respCode)) {
					LOG.info(consumeTaskPOJO.toString() + "将该首笔消费任务修改为成功" + "|查询订单结果为:" + resultJSON);
					creditCardManagerTaskService.updateTaskStatusByOrderCode(null, consumeTaskPOJO.getOrderCode(), consumeTaskPOJO.getVersion());
					baseExecutor.updatePaymentOrderByOrderCode(consumeTaskPOJO.getOrderCode());
//					LOG.info("修改订单状态结果:" + updateOrderJSONObject.toString());
				}
			}
		}
	}
	
//	修改消费待完成的任务状态
	private void checkConsumeTaskPOJO(List<ConsumeTaskPOJO> consumeTaskPOJOs){
		if(consumeTaskPOJOs != null && consumeTaskPOJOs.size() > 0){
			int i = 0;
			for(ConsumeTaskPOJO consumeTaskPOJO:consumeTaskPOJOs){
				ThreadConsumeCheckor threadConsumeCheckor = SpringContextUtil.getBeanOfClass(ThreadConsumeCheckor.class);
				threadConsumeCheckor.setConsumeTaskPOJO(consumeTaskPOJO);
				new Thread(threadConsumeCheckor).start();
				if( i != 0 && i % 20 ==0){
					long time = System.currentTimeMillis();
					boolean isTrue = true;
					while(isTrue){
						if(System.currentTimeMillis() - time >= 5000){
							isTrue = false;
						}
					}
				}
				i++;
			}
		}
	}
	
	/**
	 * 具体校验方法
	 * @param consumeTaskPOJO
	 */
	public void checkTaskType2ConsumeTaskOrderStatusConsumeTask(ConsumeTaskPOJO consumeTaskPOJO) {
		JSONObject resultJSON=null;
		String respCode = "";
		String respMessage = "";
		try {
			resultJSON = baseExecutor.getOrderStatusByVersion(consumeTaskPOJO.getOrderCode(), CommonConstants.ORDER_TYPE_CONSUME, consumeTaskPOJO.getVersion());
			respCode = resultJSON.getString(CommonConstants.RESP_CODE);
			respMessage = (resultJSON.containsKey(CommonConstants.RESP_MESSAGE)?resultJSON.getString(CommonConstants.RESP_MESSAGE):"扣款失败!");
		} catch (RuntimeException e) {
			LOG.info("查询异常.将该笔消费任务修改为失败=====" + consumeTaskPOJO.toString());
			e.printStackTrace();
			respCode = CommonConstants.FALIED;
			respMessage = "扣款失败!";
		}
		Date orderExecuteTime = DateUtil.getDateStringConvert(new Date(), consumeTaskPOJO.getExecuteDateTime(), "yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY)-3);
		
		if(CommonConstants.FALIED.equals(respCode) || (CardConstss.WAIT_NOTIFY.equals(respCode) && calendar.getTime().compareTo(orderExecuteTime) > 0)){
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
				RepaymentTaskPOJO repaymentTaskPOJO = repaymentTaskPOJOBusiness.findByRepaymentTaskId(consumeTaskPOJO.getRepaymentTaskId());
				BigDecimal consumeReturnServiceCharge = consumeTaskPOJO.getAmount().multiply(repaymentTaskPOJO.getRate());
				repaymentTaskPOJO.setReturnServiceCharge(repaymentTaskPOJO.getReturnServiceCharge().add(consumeReturnServiceCharge).setScale(2, BigDecimal.ROUND_DOWN));
				repaymentTaskPOJO = repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
				creditCardAccountBusiness.updateCreditCardAccountAndVersion(repaymentTaskPOJO.getUserId(), repaymentTaskPOJO.getCreditCardNumber(), consumeTaskPOJO.getConsumeTaskId(),5, consumeReturnServiceCharge, "消费失败退还手续费",repaymentTaskPOJO.getVersion(),repaymentTaskPOJO.getCreateTime());
			}
		}else if (CommonConstants.SUCCESS.equals(respCode)) {
			LOG.info(consumeTaskPOJO.toString() + "将该消费任务修改为成功" + "|查询订单结果为:" + resultJSON);
			creditCardManagerTaskService.updateTaskStatusByOrderCode(null, consumeTaskPOJO.getConsumeTaskId(), consumeTaskPOJO.getVersion());
			baseExecutor.updatePaymentOrderByOrderCode(consumeTaskPOJO.getConsumeTaskId());
//			LOG.info("修改订单状态结果:" + updateOrderJSONObject.toString());
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
