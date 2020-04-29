package com.cardmanager.pro.scanner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cardmanager.pro.business.CreditCardAccountBusiness;
import com.cardmanager.pro.business.CreditCardManagerConfigBusiness;
import com.cardmanager.pro.business.RepaymentBillBusiness;
import com.cardmanager.pro.business.RepaymentTaskPOJOBusiness;
import com.cardmanager.pro.channel.ChannelBaseAPI;
import com.cardmanager.pro.channel.ChannelFactory;
import com.cardmanager.pro.config.PropertiesConfig;
import com.cardmanager.pro.executor.BaseExecutor;
import com.cardmanager.pro.executor.ThreadRepaymentCheckor;
import com.cardmanager.pro.executor.ThreadRepaymentExecutor;
import com.cardmanager.pro.pojo.ConsumeTaskVO;
import com.cardmanager.pro.pojo.CreditCardAccount;
import com.cardmanager.pro.pojo.CreditCardManagerConfig;
import com.cardmanager.pro.pojo.RepaymentBill;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;
import com.cardmanager.pro.pojo.RepaymentTaskVO;
import com.cardmanager.pro.service.CreditCardManagerTaskService;
import com.cardmanager.pro.util.CardConstss;
import com.cardmanager.pro.util.RestTemplateUtil;
import com.cardmanager.pro.util.SpringContextUtil;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Configuration
@Controller
@EnableScheduling
public class RepaymentTaskScanner extends ApplicationObjectSupport {
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Autowired
	private RepaymentTaskPOJOBusiness repaymentTaskPOJOBusiness;

	@Autowired
	private CreditCardAccountBusiness creditCardAccountBusiness;
	
	@Autowired
	private CreditCardManagerConfigBusiness creditCardManagerConfigBusiness;
	
	@Autowired
	private CreditCardManagerTaskService creditCardManagerTaskService;
	
	@Autowired
	private RepaymentBillBusiness repaymentBillBusiness;
	
	@Autowired
	private BaseExecutor baseExecutor;
	
	@Autowired
	private RestTemplateUtil util;
	
	@Autowired
	private PropertiesConfig propertiesConfig;
	
	@Autowired
	private ChannelFactory channelFactory;
	
	private static volatile List<RepaymentTaskPOJO> repaymentTaskPOJOList = new ArrayList<>();
	
	/**
	 * 手动执行未执行的还款任务
	 * @param request
	 * @param version
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/get/user/channel/account")
	public @ResponseBody Object test(HttpServletRequest request,
			@RequestParam(value="version")String version,
			@RequestParam(value="idCard")String idCard
			) {
		ChannelBaseAPI channelBaseAPI = channelFactory.getChannelBaseAPI(version);
		LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<>();
		requestEntity.add("idCard", idCard);
		return 	channelBaseAPI.getChannelUserAccount(requestEntity);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/test/queryorder")
	public @ResponseBody Object test(HttpServletRequest request,
			@RequestParam(value="version")String version,
			@RequestParam(value="orderCode")String orderCode,
			@RequestParam(value="orderType")String orderType
			) {
		ChannelBaseAPI channelBaseAPI = channelFactory.getChannelBaseAPI(version);
		LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<>();
		requestEntity.add("orderCode", orderCode);
		requestEntity.add("orderType", orderType);
		JSONObject orderStatus = channelBaseAPI.getOrderStatus(requestEntity,orderType);
		JSONObject orderStatusByVersion = baseExecutor.getOrderStatusByVersion(orderCode, orderType, version);
		return orderStatusByVersion;
	}

	/**
	 * 手动清除 帐户余额有钱的帐户
	 * @param request
	 * @param version
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/test2")
	public @ResponseBody Object test2(HttpServletRequest request,
			@RequestParam(value="version")String version
			) {
		executeRepaymentByBlanceThan0(version);
		return "OK";
	}
	
	
	/**
	 * 手动将帐户中冻结余额转入余额中
	 * @param request
	 * @param version
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/creditcardmanager/test3")
	public @ResponseBody Object test3(HttpServletRequest request,
			@RequestParam(value="version")String version
			){
		this.clearCreditAccountFreezeBlance(version);
		return "OK";
	}
	
	/**
	 * 手动对单个帐户进行出款
	 * @param request
	 * @param userId
	 * @param creditCardNumber
	 * @param version
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/clear/by/userid/creditcardnumber")
	public @ResponseBody Object clearAccountByUserIdAndCreditCardNumber(HttpServletRequest request,
			@RequestParam(value = "userId") String userId,
			@RequestParam(value = "creditCardNumber") String creditCardNumber,
			@RequestParam(value = "amount",required=false) String amountStr,
			@RequestParam(value = "isT1",required=false,defaultValue="0") String isT1,
			@RequestParam(value = "version") String version
			) {
		Map<String, Object> map = new HashMap<>();
		CreditCardAccount creditCardAccount = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(userId,creditCardNumber,version);
		if (creditCardAccount == null) {
			return ResultWrap.init(CommonConstants.FALIED, "无该卡号的帐户信息,请确认请求地址或卡号!");
		}
		
		BigDecimal amount = null;
		RepaymentTaskPOJO repaymentTaskPOJO = null;
//		if (amountStr != null && "".equals(amountStr)) {
//			amount = new BigDecimal(amountStr);
//		}
		if (amountStr != null && !"".equals(amountStr)) {
			amount = new BigDecimal(amountStr);
		}
		if (CardConstss.CARD_VERSION_6.equals(version) || CardConstss.CARD_VERSION_60.equals(version)) {
			repaymentTaskPOJO = repaymentTaskPOJOBusiness.findByUserIdAndCreditCardNumberAndTaskStatusAndTaskTypeAndOrderStatusAndVersion(userId, creditCardNumber, 2, 2, 0, version);
		}else {
			repaymentTaskPOJO = this.genreateRepaymentTaskPOJO(creditCardAccount,version,amount);
		}
		if (repaymentTaskPOJO == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "生成任务失败,无法进行出款");
			return map;
		}
		
		repaymentTaskPOJO.setTaskStatus(3);
		repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
		
		List<RepaymentTaskPOJO> repaymentTaskPOJOs = new ArrayList<>();
		repaymentTaskPOJOs.add(repaymentTaskPOJO);
		this.executeTask(repaymentTaskPOJOs,"1".equals(isT1));
		
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "出款任务执行中");
		map.put(CommonConstants.RESULT, repaymentTaskPOJO);
		return map;
	}

	/**
	 * 对首笔还款待完成任务进行状态更新
	 */
	@Scheduled(cron = "0 0/10 * * * ?")
	public void autoUpdateTaskType0OrderStatus4ConsumeTask(){
		if ("true".equals(propertiesConfig.getScanOnOff())) {
			List<CreditCardManagerConfig> configs = this.getConfig();
			for (CreditCardManagerConfig creditCardManagerConfig : configs) {
				if (1 == creditCardManagerConfig.getScanOnOff() && 1 == creditCardManagerConfig.getRepaymentOnOff()) {
					LOG.info("====================version"+creditCardManagerConfig.getVersion()+"修改首笔还款待完成任务状态开始执行====================");
					this.checkTaskType0RepaymentTaskByVersion(creditCardManagerConfig.getVersion());
				}
			}
		}
	}
	
	/**
	 * 对还款待完成任务进行状态更新
	 */
	@Scheduled(cron = "0 0/5 * * * ?")
	public void autoUpdateTaskType2OrderStatus4ConsumeTask(){
		if ("true".equals(propertiesConfig.getScanOnOff())) {
			List<CreditCardManagerConfig> configs = this.getConfig();
			Pageable pageable = new PageRequest(0, 200, new Sort(Sort.Direction.DESC, "executeDateTime"));
			for (CreditCardManagerConfig creditCardManagerConfig : configs) {
				if (1 == creditCardManagerConfig.getScanOnOff() && 1 == creditCardManagerConfig.getRepaymentOnOff()) {
					LOG.info("====================修改还款version"+creditCardManagerConfig.getVersion()+"待完成任务状态开始执行====================");
					List<RepaymentTaskPOJO> repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByTaskTypeAndOrderStatusAndVersion(2, 4,creditCardManagerConfig.getVersion(),pageable);
					this.checkTaskRepaymentTask(repaymentTaskPOJOs);
					repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByTaskTypeAndOrderStatusAndVersion(1, 4,creditCardManagerConfig.getVersion(),pageable);
					this.checkTaskRepaymentTask(repaymentTaskPOJOs);
				}
			}
		}
	}
	
	/**
	 * 对还款待完成任务进行状态更新
	 */
	@Scheduled(cron = "0 0 0/3 * * ?")
	public void autoUpdateTaskType2OrderStatus5ConsumeTask(){
		if ("true".equals(propertiesConfig.getScanOnOff())) {
			List<CreditCardManagerConfig> configs = this.getConfig();
			Pageable pageable = new PageRequest(0, 200, new Sort(Sort.Direction.DESC, "executeDateTime"));
			for (CreditCardManagerConfig creditCardManagerConfig : configs) {
				if (1 == creditCardManagerConfig.getScanOnOff() && 1 == creditCardManagerConfig.getRepaymentOnOff()) {
					LOG.info("====================修改还款version"+creditCardManagerConfig.getVersion()+"待完成任务状态开始执行====================");
					List<RepaymentTaskPOJO> repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByTaskTypeAndOrderStatusAndVersion(2, 5,creditCardManagerConfig.getVersion(),pageable);
					this.checkTaskRepaymentTask(repaymentTaskPOJOs);
					repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByTaskTypeAndOrderStatusAndVersion(1, 5,creditCardManagerConfig.getVersion(),pageable);
					this.checkTaskRepaymentTask(repaymentTaskPOJOs);
				}
			}
		}
	}
	
	@Scheduled(cron = "0/20 * * * * ?")
	public void autoExecuteTask() {
		this.executeTask();
	}
	
	public void executeTask() {
		if("true".equals(propertiesConfig.getScanOnOff())){
			LOG.info("当前待执行还款任务数量==============================" + repaymentTaskPOJOList.size());
			ApplicationContext applicationContext = this.getApplicationContext();
			ThreadRepaymentExecutor threadRepaymentExecutor = null;
			for (int i = 0;i < 70;i++) {
				RepaymentTaskPOJO repaymentTaskPOJO = null;
				try {
					if (repaymentTaskPOJOList.size() == 0) {
						break;
					}
					repaymentTaskPOJO = repaymentTaskPOJOList.get(0);
					threadRepaymentExecutor = applicationContext.getBean(ThreadRepaymentExecutor.class);
					threadRepaymentExecutor.setRepaymentTaskPOJO(repaymentTaskPOJO);
					new Thread(threadRepaymentExecutor).start();
					LOG.info("执行还款任务==============================" + repaymentTaskPOJO);
					repaymentTaskPOJOList.remove(repaymentTaskPOJO);
				} catch (Exception e) {
					if (repaymentTaskPOJO != null) {
						repaymentTaskPOJO.setTaskStatus(2);
						repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
						repaymentTaskPOJOList.remove(repaymentTaskPOJO);
					}
					LOG.error("",e);
					continue;
				}
			}
		}
	}
	
	
	
	/**
	 * 执行还款任务定时器
	 */
	@Scheduled(cron = "0 0/2 * * * ?")
	public void autoScanTaskType2AndTaskStatus0RepaymentTask() {
		this.readyExecuteTask();
	}
	
	/*@Scheduled(cron = "0 10 16 * * ?")
	public void autoScanTaskType2AndTaskStatus0RepaymentTask() {
		CreditCardManagerConfig config = creditCardManagerConfigBusiness.findByVersionLock("0");
		if (config != null) {
			LOG.info("拿到令牌,正在执行======");
			try {
				Thread.sleep(20 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			config.setScanOnOff(1);
			creditCardManagerConfigBusiness.save(config);
		}else {
			LOG.info("未拿到令牌,跳过执行======");
		}
//		this.readyExecuteTask();
	}*/
	
	private void readyExecuteTask() {
		if ("true".equals(propertiesConfig.getScanOnOff())) {
			List<CreditCardManagerConfig> configs = this.getConfig();
			for (CreditCardManagerConfig creditCardManagerConfig : configs) {
				if (1 == creditCardManagerConfig.getScanOnOff() && 1 == creditCardManagerConfig.getRepaymentOnOff()) {
					LOG.info("====================version"+creditCardManagerConfig.getVersion()+"还款任务开始执行====================");
					List<RepaymentTaskPOJO> repaymentTask = this.findTaskTypeAndTaskStatus0RepaymentTask(2,creditCardManagerConfig.getVersion());
					this.addTaskToPool(repaymentTask);
					repaymentTask = this.findTaskTypeAndTaskStatus0RepaymentTask(1,creditCardManagerConfig.getVersion());
					this.addTaskToPool(repaymentTask);
				}
			}
		}
	}
	
	private void addTaskToPool(List<RepaymentTaskPOJO> repaymentTask) {
		if (repaymentTask!=null && repaymentTask.size() > 0 && repaymentTaskPOJOList.size() <= 300) {
			int count = 0;
			for (RepaymentTaskPOJO repaymentTaskPOJO : repaymentTask) {
				if (repaymentTaskPOJOList.size() > 400 || count > 40) {
					break;
				}
				repaymentTaskPOJOList.add(repaymentTaskPOJO);
				repaymentTaskPOJO.setTaskStatus(3);
				repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
				count++;
			}
		}
	}
	
	private void addTaskToPoolNoLimit(List<RepaymentTaskPOJO> repaymentTask) {
		if (repaymentTask!=null && repaymentTask.size() > 0 && repaymentTaskPOJOList.size() <= 1000) {
			for (RepaymentTaskPOJO repaymentTaskPOJO : repaymentTask) {
				if (repaymentTaskPOJOList.size() > 1000) {
					break;
				}
				repaymentTaskPOJOList.add(repaymentTaskPOJO);
				repaymentTaskPOJO.setTaskStatus(3);
				repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
			}
		}
	}

	
	//@Scheduled(cron = "0 0 01 * * ?")
	public void autoCreateRepayment() {
		this.createTask();
	}
	
	/**
	 * 对帐户余额有钱的帐户自动执行出款定时器
	 */
	@Scheduled(cron = "0 45 20 * * ?")
	public void autoScanCreditCardAccountBlanceNotZero() {
		this.autoRepayment();
	}
	
	/**
	 * 对帐户余额有钱的帐户自动执行出款定时器
	 */
	/*@Scheduled(cron = "0/10 * * * * ?")
	public void autoScanCreditCardAccountBlanceNotZero3() {
		repaymentBillBusiness.addTaskToThreadPool(repaymentTaskPOJOList,new Date());
	}*/
	
	/**
	 * 对帐户余额有钱的帐户自动执行出款定时器
	 */
	//@Scheduled(cron = "0 10 09 * * ?")
	@Scheduled(cron = "0 00 08 * * ?")
	public void autoScanCreditCardAccountBlanceNotZero2() {
		this.autoRepayment();
	}
	
	/**
	 * 对有余额的帐户进行批量出款
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/repayment/tests")
	public void autoRepayment() {
		if ("true".equals(propertiesConfig.getScanOnOff())) {
			List<CreditCardManagerConfig> configs = this.getConfig();
			for (CreditCardManagerConfig creditCardManagerConfig : configs) {
				if (1 == creditCardManagerConfig.getScanOnOff() && 1 == creditCardManagerConfig.getRepaymentOnOff()) {
					LOG.info("====================version"+creditCardManagerConfig.getVersion()+"自动出款任务开始执行====================");
					this.executeRepaymentByBlanceThan0(creditCardManagerConfig.getVersion());
				}
			}
		}
	}
	
	/**
	 * 获取还款配置
	 * @return
	 */
	private List<CreditCardManagerConfig> getConfig(){
		List<CreditCardManagerConfig> creditCardManagerConfigs = creditCardManagerConfigBusiness.findAll();
		return creditCardManagerConfigs;
	}

	
	private void executeRepaymentByBlanceThan0(String version){
		if ("true".equals(propertiesConfig.getScanOnOff())) {
			this.clearCreditAccountFreezeBlance(version);
			int page = 0;
			List<RepaymentTaskPOJO> findCreditCardAccountBlanceNotZero = new ArrayList<>();
			CreditCardManagerConfig cardManagerConfig = creditCardManagerConfigBusiness.findByVersion(version);
			if (cardManagerConfig != null && 1 == cardManagerConfig.getScanOnOff()) {
				int firstMoney = cardManagerConfig.getFirstMoney();
				do{
					Pageable pageable = new PageRequest(page, 20, new Sort(Sort.Direction.DESC, "blance"));
					findCreditCardAccountBlanceNotZero = this.findCreditCardAccountBlanceNotZero(BigDecimal.valueOf(firstMoney),version,pageable);
					if (findCreditCardAccountBlanceNotZero != null && findCreditCardAccountBlanceNotZero.size() > 0) {
						LOG.info("执行出款的任务数==========" + findCreditCardAccountBlanceNotZero.size());
						this.addTaskToPoolNoLimit(findCreditCardAccountBlanceNotZero);
//						this.executeTask(findCreditCardAccountBlanceNotZero,false);
					}
					page += 1;
				}while(findCreditCardAccountBlanceNotZero != null);
			}
		}
	}
	
	
	/**
	 * 获取当天当前时间之前批量生成的未执行还款任务
	 * @author Robin-QQ/WX:354476429 
	 * @date 2018年6月12日  
	 * @param version
	 * @return
	 */
	private List<RepaymentTaskPOJO> findTaskTypeAndTaskStatus0RepaymentTask(int taskType,String version) {
		List<RepaymentTaskPOJO> repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findTaskTypeAndTaskStatus0RepaymentTaskAndVersion(taskType,version);
		return repaymentTaskPOJOs;
	}
	
	/**
	 * 获取帐户中blance大于0的帐户并生成自动出款任务
	 * @author Robin-QQ/WX:354476429 
	 * @date 2018年6月12日  
	 * @param version
	 * @param pageable
	 * @return
	 */
	private List<RepaymentTaskPOJO> findCreditCardAccountBlanceNotZero(BigDecimal firstAmount,String version,Pageable pageable) {
		List<CreditCardAccount> creditCardAccounts = creditCardAccountBusiness.findCreditCardAccountByBlanceNotZeroAndVersion(firstAmount,version,pageable);
		List<RepaymentTaskPOJO> repaymentTaskPOJOs = null;
		if (creditCardAccounts != null && creditCardAccounts.size() > 0) {
			LOG.info("获取到有余额的帐户数=========="+ creditCardAccounts.size());
			repaymentTaskPOJOs = new ArrayList<>();
			RepaymentTaskPOJO genreateRepaymentTaskPOJO;
			for (CreditCardAccount creditCardAccount : creditCardAccounts) {
				try {
					if (firstAmount.compareTo(creditCardAccount.getBlance()) < 0) {
						if (CardConstss.CARD_VERSION_6.equals(version) || CardConstss.CARD_VERSION_60.equals(version)) {
							genreateRepaymentTaskPOJO = repaymentTaskPOJOBusiness.findByUserIdAndCreditCardNumberAndTaskStatusAndTaskTypeAndOrderStatusAndVersion(creditCardAccount.getUserId(), creditCardAccount.getCreditCardNumber(), 2, 2, 0, version);
							LOG.info("genreateRepaymentTaskPOJO=========="+ genreateRepaymentTaskPOJO);
						}else {
							genreateRepaymentTaskPOJO = this.genreateRepaymentTaskPOJO(creditCardAccount,version,null);
						}
						if (genreateRepaymentTaskPOJO != null) {
							repaymentTaskPOJOs.add(genreateRepaymentTaskPOJO);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
			return repaymentTaskPOJOs;
		}else {
			return null;
		}
	}

	/**
	 * 多线程执行扫描到的任务
	 * @author Robin-QQ/WX:354476429 
	 * @date 2018年6月12日  
	 * @param repaymentTaskPOJOs
	 */
	public void executeTask(List<RepaymentTaskPOJO> repaymentTaskPOJOs,boolean isT1) {
		ApplicationContext applicationContext = this.getApplicationContext();
		CreditCardAccount creditCardAccount = null;
		ThreadRepaymentExecutor threadRepaymentExecutor = null;
		if (repaymentTaskPOJOs != null && repaymentTaskPOJOs.size() > 0) {
			RepaymentTaskPOJO repaymentTaskPOJO = null;
			for (int i = 0;i < repaymentTaskPOJOs.size();i++) {
				repaymentTaskPOJO = repaymentTaskPOJOs.get(i);
				
				creditCardAccount = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(repaymentTaskPOJO.getUserId(), repaymentTaskPOJO.getCreditCardNumber(),repaymentTaskPOJO.getVersion());
//				repaymentTaskPOJO.setRealAmount(creditCardAccount.getBlance());
				repaymentTaskPOJO.setRealAmount(repaymentTaskPOJOs.get(i).getAmount());
//				repaymentTaskPOJO.setAmount(repaymentTaskPOJOs.get(i).getAmount());
				threadRepaymentExecutor = applicationContext.getBean(ThreadRepaymentExecutor.class);
				threadRepaymentExecutor.setRepaymentTaskPOJO(repaymentTaskPOJO);
				new Thread(threadRepaymentExecutor).start();
				LOG.info("执行任务=============================="+repaymentTaskPOJO);
				/*if( i != 0 && i % 30 ==0){
					long time = System.currentTimeMillis();
					boolean isTrue = true;
					while(isTrue){
						if(System.currentTimeMillis() - time >= 10000){
							isTrue = false;
						}
					}
				}*/
			}
		}
	}

	
	/**
	 * 创建出款任务
	 * @author Robin-QQ/WX:354476429 
	 * @date 2018年6月12日  
	 * @param creditCardAccount
	 * @param version
	 * @return
	 */
	public RepaymentTaskPOJO genreateRepaymentTaskPOJO(CreditCardAccount creditCardAccount,String version,BigDecimal amount) {
		if (amount == null) {
			amount = creditCardAccount.getBlance();
		}
		String userId = creditCardAccount.getUserId();
		String creditCardNumber = creditCardAccount.getCreditCardNumber();
		String description = "尾号" + creditCardNumber.substring(creditCardNumber.length() - 4) + "|系统自动生成出款任务";
		String createTime = null;
		String channelId = null;
		String channelTag = null;
		BigDecimal serviceCharge = BigDecimal.ZERO;
		BigDecimal rate = BigDecimal.ZERO;
		BigDecimal returnServiceCharge = BigDecimal.ZERO;
		List<RepaymentTaskPOJO> repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByExecuteDateAndVersion(userId,creditCardNumber,version);
		if (repaymentTaskPOJOs != null && repaymentTaskPOJOs.size() > 0) {
				createTime = repaymentTaskPOJOs.get(0).getCreateTime();
				serviceCharge = repaymentTaskPOJOs.get(0).getServiceCharge();
				rate = repaymentTaskPOJOs.get(0).getRate();
				channelId = repaymentTaskPOJOs.get(0).getChannelId();
				channelTag = repaymentTaskPOJOs.get(0).getChannelTag();
				createTime = createTime == null? DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss") : createTime;
				RepaymentTaskPOJO repaymentTaskPOJO = repaymentTaskPOJOBusiness.createNewRepaymentTaskPOJO(amount, userId,creditCardNumber, 1, description, createTime, serviceCharge, rate, returnServiceCharge, channelId,channelTag,version,null,creditCardAccount.getBrandId());
				return repaymentTaskPOJO;
		} else {
			//查询用户费率
			Map<String, Object> userChannelRate = getUserChannelRate(userId,"100247",version);
			JSONObject resultJSONObject = (JSONObject) userChannelRate.get(CommonConstants.RESULT);
			String rateStr = resultJSONObject.getString("rate");
			rate = new BigDecimal(rateStr).setScale(4, BigDecimal.ROUND_UP);
			CreditCardManagerConfig byVersion = creditCardManagerConfigBusiness.findByVersion(version);
			channelId = byVersion.getChannelId();
			channelTag = byVersion.getChannelTag();
			serviceCharge = new BigDecimal("1");
			createTime = DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss");
			RepaymentTaskPOJO repaymentTaskPOJO = repaymentTaskPOJOBusiness.createNewRepaymentTaskPOJO(amount, userId,creditCardNumber, 1, description, createTime, serviceCharge, rate, returnServiceCharge, channelId,channelTag,version,null,creditCardAccount.getBrandId());
			return repaymentTaskPOJO;
		}

	}
	/**
	 * 获取用户费率
	 * @param userId
	 * @param brandId
	 * @param version
	 * @return
	 * <p>Description: </p>
	 */
	public Map<String,Object> getUserChannelRate(String userId,String brandId,String version){
		return 	baseExecutor.getUserChannelRate(userId, brandId, version);
	}
	/**
	 * 解除冻结余额
	 * @author Robin-QQ/WX:354476429 
	 * @date 2018年6月12日  
	 * @param version
	 */
	public void clearCreditAccountFreezeBlance(String version){
		List<CreditCardAccount> creditCardAccounts = creditCardAccountBusiness.findByFreezeBlanceGreaterThan0AndVersion(version);		
		if(creditCardAccounts!=null && creditCardAccounts.size() > 0){
			for(CreditCardAccount creditCardAccount:creditCardAccounts){
				RepaymentTaskPOJO repaymentTaskPOJO = repaymentTaskPOJOBusiness.findByUserIdAndCreditCardNumberAndTaskStatusAndTaskTypeAndOrderStatusAndVersion(creditCardAccount.getUserId(), creditCardAccount.getCreditCardNumber(), 1, 2, 4, version);
				if (repaymentTaskPOJO != null) {
					this.checkTaskType2OrderStatus4RepaymentTask(repaymentTaskPOJO);
				}
			}
		}
	}
	
	
	/**
	 * 修改首笔还款待完成的任务状态
	 * @author Robin-QQ/WX:354476429 
	 * @date 2018年6月12日  
	 * @param version
	 */
	private void checkTaskType0RepaymentTaskByVersion(String version){
		Pageable pageable = new PageRequest(0, 200, new Sort(Sort.Direction.DESC, "executeDateTime"));
		List<RepaymentTaskPOJO> repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByTaskTypeAndOrderStatusAndVersion(0,4,version,pageable);
		if(repaymentTaskPOJOs !=null && repaymentTaskPOJOs.size() > 0){
			for(RepaymentTaskPOJO repaymentTaskPOJO:repaymentTaskPOJOs){
				JSONObject resultJSON = null;
				String respCode = "";
				String respMessage = "";
				try {
					resultJSON = baseExecutor.getOrderStatusByVersion(repaymentTaskPOJO.getOrderCode(), CommonConstants.ORDER_TYPE_REPAYMENT, repaymentTaskPOJO.getVersion());
					respCode = resultJSON.getString(CommonConstants.RESP_CODE);
					respMessage = (resultJSON.containsKey(CommonConstants.RESP_MESSAGE)?resultJSON.getString(CommonConstants.RESP_MESSAGE):"还款失败!");
				} catch (RuntimeException e) {
					LOG.info("查询异常.将该首笔还款任务修改为失败=====" + repaymentTaskPOJO.toString());
					e.printStackTrace();
					respCode = CommonConstants.FALIED;
					respMessage = "还款失败!";
				}
				Date orderExecuteTime = DateUtil.getDateStringConvert(new Date(), repaymentTaskPOJO.getExecuteDateTime(), "yyyy-MM-dd HH:mm:ss");
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.HOUR_OF_DAY, -3);
				if(CommonConstants.FALIED.equals(respCode) || (CardConstss.WAIT_NOTIFY.equals(respCode) && calendar.getTime().compareTo(orderExecuteTime) > 0)){
					calendar = Calendar.getInstance();
					calendar.add(Calendar.MINUTE, -15);
					if (calendar.getTime().compareTo(orderExecuteTime) > 0) {
						LOG.info(repaymentTaskPOJO.toString() + "将该首笔还款任务修改为失败" + "|查询订单结果为:" + resultJSON);
						repaymentTaskPOJO.setTaskStatus(0);
						repaymentTaskPOJO.setOrderStatus(0);
						repaymentTaskPOJO.setReturnMessage(respMessage);
						repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
						creditCardAccountBusiness.updateCreditCardAccountAndVersion(repaymentTaskPOJO.getUserId(), repaymentTaskPOJO.getCreditCardNumber(), repaymentTaskPOJO.getRepaymentTaskId(), 3, repaymentTaskPOJO.getRealAmount(), "首笔还款失败,解除冻结金额", repaymentTaskPOJO.getVersion(),repaymentTaskPOJO.getCreateTime());
					}
				}else if (CommonConstants.SUCCESS.equals(respCode)) {
					LOG.info(repaymentTaskPOJO.toString() + "将该首笔还款该任务修改为成功" + "|查询订单结果为:" + resultJSON);
					creditCardManagerTaskService.updateTaskStatusByOrderCode(null, repaymentTaskPOJO.getOrderCode(),repaymentTaskPOJO.getVersion());
					baseExecutor.updatePaymentOrderByOrderCode(repaymentTaskPOJO.getOrderCode());
//					LOG.info("修改订单状态结果:" + updateOrderJSONObject.toString());
				}
			}
		}
	}
	
	/**
	 * 校验待完成的批量生成的任务
	 * @author Robin-QQ/WX:354476429 
	 * @date 2018年6月12日  
	 * @param version
	 */
	private void checkTaskRepaymentTask(List<RepaymentTaskPOJO> repaymentTaskPOJOs){
		int i = 0;
		for(RepaymentTaskPOJO repaymentTaskPOJO:repaymentTaskPOJOs){
			ThreadRepaymentCheckor threadRepaymentCheckor = SpringContextUtil.getBeanOfClass(ThreadRepaymentCheckor.class);
			threadRepaymentCheckor.setRepaymentTaskPOJO(repaymentTaskPOJO);
			new Thread(threadRepaymentCheckor).start();
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
	
	/**
	 * 具体校验还款状态方法
	 * @author Robin-QQ/WX:354476429 
	 * @date 2018年6月12日  
	 * @param repaymentTaskPOJO
	 */
	private void checkTaskType2OrderStatus4RepaymentTask(RepaymentTaskPOJO repaymentTaskPOJO){
		JSONObject resultJSON = null;
		String respCode = "";
		String respMessage = "";
		try {
			resultJSON = baseExecutor.getOrderStatusByVersion(repaymentTaskPOJO.getOrderCode(),  CommonConstants.ORDER_TYPE_REPAYMENT, repaymentTaskPOJO.getVersion());
			respCode = resultJSON.getString(CommonConstants.RESP_CODE);
			respMessage = (resultJSON.containsKey(CommonConstants.RESP_MESSAGE)?resultJSON.getString(CommonConstants.RESP_MESSAGE):"还款失败!");
		} catch (RuntimeException e) {
			LOG.info("查询异常.将该笔还款任务修改为失败=====" + repaymentTaskPOJO.toString());
			e.printStackTrace();
			respCode = CommonConstants.FALIED;
			respMessage = "还款失败!";
		}
		Date orderExecuteTime = DateUtil.getDateStringConvert(new Date(), repaymentTaskPOJO.getExecuteDateTime(), "yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR_OF_DAY, -3);
		if(CommonConstants.FALIED.equals(respCode) || (CardConstss.WAIT_NOTIFY.equals(respCode) && calendar.getTime().compareTo(orderExecuteTime) > 0)){
			calendar = Calendar.getInstance();
			calendar.add(Calendar.MINUTE, -15);
			if (calendar.getTime().compareTo(orderExecuteTime) > 0) {
				LOG.info(repaymentTaskPOJO.toString() + "将该还款任务修改为失败" + "|查询订单结果为:" + resultJSON);
				repaymentTaskPOJO.setTaskStatus(2);
				repaymentTaskPOJO.setOrderStatus(0);
				repaymentTaskPOJO.setRealAmount(BigDecimal.ZERO);
				repaymentTaskPOJO.setReturnMessage(respMessage);
				repaymentTaskPOJOBusiness.save(repaymentTaskPOJO);
				creditCardAccountBusiness.updateCreditCardAccountAndVersion(repaymentTaskPOJO.getUserId(), repaymentTaskPOJO.getCreditCardNumber(),repaymentTaskPOJO.getRepaymentTaskId(),3,repaymentTaskPOJO.getRealAmount(),"还款失败增加余额",repaymentTaskPOJO.getVersion(),repaymentTaskPOJO.getCreateTime());
				//util.pushMessage(repaymentTaskPOJO.getUserId(),"有一笔金额为:"+repaymentTaskPOJO.getAmount()+"的还款任务失败,系统将在当天23:00前进行自动还款!",repaymentTaskPOJO.getVersion(),repaymentTaskPOJO.getCreditCardNumber(),repaymentTaskPOJO.getReturnMessage(),repaymentTaskPOJO.getOrderCode());
				if (!repaymentTaskPOJO.getDescription().contains("系统自动")) {
					util.pushMessage(repaymentTaskPOJO.getUserId(), "有一笔金额为:" + repaymentTaskPOJO.getAmount() + "的还款任务失败,系统将在当天23:00前进行自动还款!", repaymentTaskPOJO.getVersion(), repaymentTaskPOJO.getCreditCardNumber(), repaymentTaskPOJO.getReturnMessage(), repaymentTaskPOJO.getOrderCode());
				}
			}
		}else if (CommonConstants.SUCCESS.equals(respCode)) {
			LOG.info(repaymentTaskPOJO.toString() + "将该还款任务修改为成功" + "|查询订单结果为:" + resultJSON);
			creditCardManagerTaskService.updateTaskStatusByOrderCode(null, repaymentTaskPOJO.getOrderCode(),repaymentTaskPOJO.getVersion());
			baseExecutor.updatePaymentOrderByOrderCode(repaymentTaskPOJO.getOrderCode());
//			LOG.info("修改订单状态结果:" + updateOrderJSONObject.toString());
		}
	}
	
	/**
	 * 自动创建在还款日未还完的还款任务
	 * 
	 * <p>Description: </p>
	 */
	@RequestMapping(value="/v1.0/creditcardmanager/auto/createtask")
	private void createTask() {
		if ("true".equals(propertiesConfig.getScanOnOff())) {
			List<CreditCardManagerConfig> configs = this.getConfig();
			String now = DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss");
			for (CreditCardManagerConfig creditCardManagerConfig : configs) {
				int page = 0;
				Page<RepaymentBill> repaymentBills;
				if (1 == creditCardManagerConfig.getScanOnOff() && 1 == creditCardManagerConfig.getRepaymentOnOff()) {
					do{
						Pageable pageable = new PageRequest(page, 500, new Sort(Sort.Direction.ASC, "createTime"));
						repaymentBills = repaymentBillBusiness.findByVersionAndLastExecuteDateTimeLessThanAndTaskStatusNot(creditCardManagerConfig.getVersion(),now,3,pageable);
						List<RepaymentBill> content = repaymentBills.getContent();
						for (RepaymentBill repaymentBill : content) {
							try {
								CreditCardAccount creditCardAccount = creditCardAccountBusiness.findByUserIdAndCreditCardNumberAndVersion(repaymentBill.getUserId(),repaymentBill.getCreditCardNumber(), repaymentBill.getVersion());
								Calendar calendar = Calendar.getInstance();
								int date = calendar.get(Calendar.DATE);
								Integer repaymentDate = creditCardAccount.getRepaymentDate();
								Integer billDate = creditCardAccount.getBillDate();
								int days = repaymentDate;
								if (repaymentDate.intValue() > billDate.intValue()) {
									if (date > repaymentDate) {
										repaymentBill.setTaskStatus(3);
										repaymentBillBusiness.save(repaymentBill);
										continue;
									}
								}else{
									if (date > repaymentDate.intValue() && date < billDate.intValue()) {
										repaymentBill.setTaskStatus(3);
										repaymentBillBusiness.save(repaymentBill);
										continue;
									}
									if (date > billDate.intValue()) {
										calendar.add(Calendar.MONTH, +1);
										calendar.set(Calendar.DAY_OF_MONTH, 1);
										calendar.add(Calendar.DAY_OF_MONTH, -1);
										int monthDay = calendar.get(Calendar.DAY_OF_MONTH);
										days = monthDay+repaymentDate;
									}
								}
								String[] executeDates = new String[days+1-date];
								calendar = Calendar.getInstance();
								calendar.add(Calendar.DATE, -1);
								for (int i = 0; i < days+1-date; i++) {
									calendar.add(Calendar.DATE, +1);
									String executeDate = DateUtil.getDateStringConvert(new String(), calendar.getTime(), "yyyy-MM-dd");
									executeDates[i] = executeDate;
								}
								BigDecimal amount = repaymentBill.getTaskAmount().subtract(repaymentBill.getRepaymentedAmount());
								BigDecimal consumedAmount = repaymentBill.getTaskAmount().subtract(repaymentBill.getConsumedAmount());
								if (BigDecimal.ZERO.compareTo(amount) >= 0 || BigDecimal.ZERO.compareTo(consumedAmount) >= 0) {
									repaymentBill.setTaskStatus(3);
									repaymentBillBusiness.save(repaymentBill);
									continue;
								}
								
								JSONObject userInfo = baseExecutor.getUserInfo(repaymentBill.getUserId());
								if (!CommonConstants.SUCCESS.equals(userInfo.get(CommonConstants.RESP_CODE))) {
									continue;
								}
								userInfo = userInfo.getJSONObject(CommonConstants.RESULT);
								String brandId = userInfo.getString("brandId");
								List<RepaymentTaskVO> temporaryPlan = channelFactory.getChannelRoot(creditCardManagerConfig.getVersion()).creatTemporaryPlan(repaymentBill.getUserId(), repaymentBill.getCreditCardNumber(), amount.toString(), repaymentBill.getReservedAmount().toString(), brandId, creditCardManagerConfig, executeDates,null,0);
								for (RepaymentTaskVO repaymentTaskVO : temporaryPlan) {
									repaymentTaskVO.setCreateTime(repaymentBill.getCreateTime());
									repaymentTaskVO.setDescription(repaymentTaskVO.getDescription()+"(系统补单还款)");
									for (ConsumeTaskVO consumeTaskVO : repaymentTaskVO.getConsumeTaskVOs()) {
										consumeTaskVO.setDescription(consumeTaskVO.getDescription()+"系统补单消费");
										consumeTaskVO.setCreateTime(repaymentBill.getCreateTime());
									}
								}
								Object object = creditCardManagerTaskService.saveRepaymentTaskAndConsumeTask(null, JSONArray.fromObject(temporaryPlan).toString(), null, repaymentBill.getVersion(), null);
								Map<String,Object> result = (Map<String, Object>) object;
								if (CommonConstants.SUCCESS.equals(result.get(CommonConstants.RESP_CODE))) {
									repaymentBill.setTaskCount(repaymentBill.getTaskCount() + temporaryPlan.size());
									repaymentBill.setTaskStatus(3);
									repaymentBillBusiness.save(repaymentBill);
								}else {
									repaymentBill.setTaskStatus(3);
									repaymentBillBusiness.save(repaymentBill);
								}
								LOG.info("自动生成任务结果:"+ object);
							} catch (Exception e) {
								LOG.error("", e);
								repaymentBill.setTaskStatus(3);
								repaymentBillBusiness.save(repaymentBill);
								continue;
							}
						}
						page += 1;
					}while(repaymentBills != null && repaymentBills.getContent() != null &&repaymentBills.getContent().size() > 0);
				}
			}
		}
	}
	/**
	 * 执行首笔验证出款任务
	 * @author Robin-QQ/WX:354476429 
	 * @date 2018年6月12日  
	 * @param version
	 */
	/*private void executeFirstRepayment(String version){
		CreditCardManagerConfig creditCardManagerConfig = creditCardManagerConfigBusiness.findByVersion(version);
		if (creditCardManagerConfig == null) {
			creditCardManagerConfig = new CreditCardManagerConfig();
			creditCardManagerConfig.setFirstMoney(10);
		}
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, -1);
		Date time = calendar.getTime();
		List<CreditCardAccount> creditCardAccounts = creditCardAccountBusiness.findByBlanceAndVersionAndLastUpdateTimeLessThan(BigDecimal.valueOf(creditCardManagerConfig.getFirstMoney()),version,time);
		for(CreditCardAccount creditCardAccount:creditCardAccounts){
			RestTemplate restTemplate = new RestTemplate();
			LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("userId", creditCardAccount.getUserId());
			requestEntity.add("creditCardNumber", creditCardAccount.getCreditCardNumber());
			requestEntity.add("phone", creditCardAccount.getPhone());
			requestEntity.add("brandId", "2");
			requestEntity.add("version", version);
			String url = "http://106.15.104.38/v1.0/creditcardmanager/first/use/credit/card/manager";
			String postForObject;
			try {
				postForObject = restTemplate.postForObject(url, requestEntity, String.class);
			} catch (RestClientException e) {
				System.out.println(creditCardAccount.getCreditCardNumber() + "==================失败");
				continue;
			}
			System.out.println(creditCardAccount.getCreditCardNumber() + "==================" +postForObject);
		}
		System.out.println(creditCardAccounts.size());
	}*/
	
	
}
