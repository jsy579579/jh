package com.cardmanager.pro.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.cardmanager.pro.authorization.CreditCardManagerAuthorizationHandle;
import com.cardmanager.pro.business.ConsumeTaskPOJOBusiness;
import com.cardmanager.pro.business.CreditCardAccountBusiness;
import com.cardmanager.pro.business.CreditCardManagerConfigBusiness;
import com.cardmanager.pro.business.RepaymentBillBusiness;
import com.cardmanager.pro.business.RepaymentTaskPOJOBusiness;
import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import com.cardmanager.pro.pojo.CreditCardAccount;
import com.cardmanager.pro.pojo.CreditCardManagerConfig;
import com.cardmanager.pro.pojo.RepaymentBill;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;
import com.cardmanager.pro.util.CardConstss;
import com.cardmanager.pro.util.RestTemplateUtil;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.StringUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class TaskBillService {
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Autowired
	private RestTemplateUtil util;

	@Autowired
	private RepaymentTaskPOJOBusiness repaymentTaskPOJOBusiness;

	@Autowired
	private ConsumeTaskPOJOBusiness consumeTaskPOJOBusiness;

	@Autowired
	private CreditCardAccountBusiness creditCardAccountBusiness;
	
	@Autowired
	private RepaymentBillBusiness repaymentBillBusiness;
	
	@Autowired
	private CreditCardManagerConfigBusiness creditCardManagerConfigBusiness;
	
	@Autowired
	private CreditCardManagerAuthorizationHandle creditCardManagerAuthorizationHandle;
	
	@Autowired
	private RestTemplate restTemplate;
	
	/**
	 * 根据创建时间查询所有还款任务
	 * @param request
	 * @param userId
	 * @param creditCardNumber
	 * @param status:0 全部批次 1:执行成功 2:等待执行 3:执行中 4:失败
	 * @param version
	 * @param page
	 * @param size
	 * @param direction
	 * @param sortProperty
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/query/taskbill/all/batchno")
	public @ResponseBody Object getAllRepaymentTaskByCreateTime(HttpServletRequest request,
			@RequestParam(value = "userId") String userId,
			@RequestParam(value = "creditCardNumber", required=false) String creditCardNumber,
			// status:0 全部批次 1:执行成功 2:等待执行 3:执行中 4:失败
			@RequestParam(value = "status", required = false, defaultValue = "0") String status,
			@RequestParam(value = "version", required = false, defaultValue = "1") String version,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {
		Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> verifyStringFiledIsNullMap = creditCardManagerAuthorizationHandle.verifyStringFiledIsNull(userId);
		if (!CommonConstants.SUCCESS.equals(verifyStringFiledIsNullMap.get(CommonConstants.RESP_CODE))) {
			return verifyStringFiledIsNullMap;
		}
		List<Map<String, Object>> models = new ArrayList<>();
		List<Object[]> repaymentTaskPOJOs = null;
		if(creditCardNumber != null){
			repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByCreateTimeAndVersion(userId, creditCardNumber,version, pageable);
			models = processingData(repaymentTaskPOJOs,status,version);
		}else{
			repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByCreateTimeAndVersion(userId,version, pageable);
			models = processingData(repaymentTaskPOJOs,status,version);
		}
		
//		if ("0".equals(status)) {
//			repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByCreateTime(userId, creditCardNumber, pageable);
//		} else if ("1".equals(status)) {
//			repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByCreateTimeAndTaskStatus1(userId, creditCardNumber,
//					pageable);
//		} else if ("2".equals(status)) {
//			repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByCreateTimeAndExecuteDate1(userId, creditCardNumber,
//					pageable);
//		} else if ("3".equals(status)) {
//			repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByCreateTimeAndExecuteDate0(userId, creditCardNumber,
//					pageable);
//		} else if ("4".equals(status)) {
//			repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByCreateTimeAndTaskStatus2(userId, creditCardNumber,
//					pageable);
//		}
		
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, models);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		return map;
	}

	/**
	 * 拼接数据
	 * @param repaymentTaskPOJOs
	 * @param status
	 * @param version
	 * @return
	 * <p>Description: </p>
	 */
	private List<Map<String, Object>> processingData(List<Object[]> repaymentTaskPOJOs,String status,String version) {
		List<Map<String, Object>> models = new ArrayList<>();
		if (repaymentTaskPOJOs != null && repaymentTaskPOJOs.size() > 0) {
			Map<String, Object> model = null;
			BigDecimal repaymentServiceCharge;
			BigDecimal consumeServiceCharge;
			BigDecimal sumConsumeAmount;
			Date nowTime = new Date();
			for (Object[] objs : repaymentTaskPOJOs) {
				model = new HashMap<>();
				String createTime = (String) objs[5];
				createTime = createTime.substring(0, createTime.lastIndexOf("."));
				
				int count = repaymentTaskPOJOBusiness.findByUserIdAndCreditCardNumberAndCreateTimeAndOrderStatus1CountAndVersion((String)objs[0],(String)objs[1],createTime,version);
				
				
				model.put("userId", objs[0]);
				model.put("creditCardNumber", objs[1]);
				// model.put("sumAmount", objs[2]);
				consumeServiceCharge = (((BigDecimal) objs[3]).add((BigDecimal)objs[11])).divide(BigDecimal.ONE.subtract((BigDecimal) objs[6]),2,BigDecimal.ROUND_UP).subtract((BigDecimal) objs[3]).subtract((BigDecimal)objs[11]);
//				repaymentServiceCharge = ((BigDecimal) objs[4]).subtract(consumeServiceCharge).setScale(2,
//						BigDecimal.ROUND_DOWN);

				if (BigDecimal.valueOf(0.02).compareTo(consumeServiceCharge) >= 0) {
					consumeServiceCharge = BigDecimal.ZERO;
					repaymentServiceCharge = BigDecimal.ZERO;
				}

				repaymentServiceCharge = ((BigDecimal)objs[11]).multiply(BigDecimal.valueOf(count));
				
				model.put("repaymentServiceCharge", repaymentServiceCharge);
				model.put("consumeServiceCharge", consumeServiceCharge);
				model.put("returnServiceCharge", objs[9]);
				if(((BigDecimal)objs[3]).compareTo(BigDecimal.ZERO) == 0){
					sumConsumeAmount = BigDecimal.ZERO;
				}else{
					sumConsumeAmount = ((BigDecimal)objs[3]).add(consumeServiceCharge).add(repaymentServiceCharge);
				}
				model.put("sumAmount",sumConsumeAmount);
				model.put("sumRealAmount", objs[3]);
				model.put("sumTotalServiceCharge", repaymentServiceCharge.add(consumeServiceCharge));
				model.put("createTime", createTime);
				model.put("rate", objs[6]);
				model.put("planSumAmount", objs[2]);
				int maxStatus = (int) objs[7];
				int minStatus = (int) objs[10];
				int orderStatus = (int) objs[12];
				String executeDate = (String) objs[8];
				String nowDate = DateUtil.getDateStringConvert(new String(), nowTime, "yyyy-MM-dd");
				// if(status == 0){
				// if(nowDate.equals(executeDate)){
				// model.put("taskStatus", "3");
				// }else{
				// model.put("taskStatus", "2");
				// }
				// }else if(status == 1){
				// model.put("taskStatus", "1");
				// }else if(status == 2){
				// model.put("taskStatus", "4");
				// }
				
//				今天日期等于执行日期
				if (nowDate.equals(executeDate)) {
					model.put("taskStatus", "3");
//				最大任务状态等于0,未执行
				} else if (maxStatus == 0) {
					model.put("taskStatus", "2");
//				最大任务状态等于2,执行失败
				} else if (maxStatus == 2 || maxStatus ==4 ) {
					if(minStatus == 0){
						model.put("taskStatus", "3");
					}else{
						model.put("taskStatus", "4");
					}
//				最大任务状态等于1
				} else if (maxStatus == 1) {
					if(minStatus == 0){
						model.put("taskStatus", "3");
					}else{
						model.put("taskStatus", "1");
					}
				} else {
					if(((BigDecimal)objs[2]).compareTo(((BigDecimal)objs[3])) != 0){
						model.put("taskStatus", "4");
					}else{
						model.put("taskStatus", "2");
					}
				}
				
				if (orderStatus >= 4) {
					model.put("taskStatus", "3");
				}
				
				if("0".equals(status)){
					models.add(model);
				}else if(((String)model.get("taskStatus")).equals(status)){
					models.add(model);
				}
			}
		}
		return models;
	}

	/**
	 * 处理数据
	 * @param source
	 * @return
	 * <p>Description: </p>
	 */
	private <T> T processingData(T source) {
		Date nowTime = new Date();
		if (source instanceof RepaymentTaskPOJO) {
			RepaymentTaskPOJO model = (RepaymentTaskPOJO) source;
			int status = model.getTaskStatus();
			int orderStatus = model.getOrderStatus();
			String executeDate = model.getExecuteDate();
			String nowDate = DateUtil.getDateStringConvert(new String(), nowTime, "yyyy-MM-dd");
			if (status == 0) {
				if (nowDate.equals(executeDate)) {
					model.setTaskStatus(3);
				} else {
					model.setTaskStatus(2);
				}
			} else if (status == 1 && orderStatus > 1) {
				model.setTaskStatus(3);
			} else if (status == 2 || status == 4) {
				model.setTaskStatus(4);
				model.setServiceCharge(BigDecimal.ZERO);
			}
			return (T) model;
		} else if (source instanceof ConsumeTaskPOJO) {
			ConsumeTaskPOJO model = (ConsumeTaskPOJO) source;
			int status = model.getTaskStatus();
			int orderStatus = model.getOrderStatus();
			String executeDate = model.getExecuteDate();
			String nowDate = DateUtil.getDateStringConvert(new String(), nowTime, "yyyy-MM-dd");
			if (status == 0) {
				if (nowDate.equals(executeDate)) {
					model.setTaskStatus(3);
				} else {
					model.setTaskStatus(2);
				}
			} else if (status == 1 && orderStatus > 1) {
				model.setTaskStatus(3);
			} else if (status == 2 || status == 4) {
				model.setTaskStatus(4);
			}
			return (T) model;
		}
		return source;
	}
	
	/**
	 * 根据创建时间查询指定批次的还款任务
	 * @param request
	 * @param userId
	 * @param creditCardNumber
	 * @param createTime
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/query/taskbill/by/createtime")
	public @ResponseBody Object getRepaymentTaskByCreateTime(HttpServletRequest request,
			@RequestParam(value = "userId") String userId,
			@RequestParam(value = "creditCardNumber") String creditCardNumber,
			@RequestParam(value = "createTime") String createTime,
			@RequestParam(value = "version",required=false) String version
			) {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> verifyStringFiledIsNullMap = creditCardManagerAuthorizationHandle.verifyStringFiledIsNull(userId, creditCardNumber, createTime);
		if (!CommonConstants.SUCCESS.equals(verifyStringFiledIsNullMap.get(CommonConstants.RESP_CODE))) {
			return verifyStringFiledIsNullMap;
		}
//		createTime = createTime + ".0";
		List<RepaymentTaskPOJO> repaymentTaskPOJOs = null;
		if (version != null && !"".equals(version) && !"null".equalsIgnoreCase(version)) {
			repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByCreateTimeAndVersion(userId,creditCardNumber, createTime,version);
		}else {
			repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByUserIdAndCreditCardNumberAndCreateTime(userId,creditCardNumber, createTime);
			
		}
		
		if (repaymentTaskPOJOs != null && repaymentTaskPOJOs.size() > 0) {
			for (RepaymentTaskPOJO repaymentTaskPOJO : repaymentTaskPOJOs) {
				repaymentTaskPOJO = processingData(repaymentTaskPOJO);
			}
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		map.put(CommonConstants.RESULT, repaymentTaskPOJOs);
		return map;
	}

	/**
	 * 根据状态查询还款任务
	 * @param request
	 * @param userId
	 * @param creditCardNumber
	 * @param status: 0:返回还款任务 1:未执行的还款任务 2:已执行但未完成的还款任务 3:已执行且已完成的还款任务  4:完成的还款任务中有失败消费任务的还款任务 5:执行失败的还款任务
	 * @param version
	 * @param page
	 * @param size
	 * @param direction
	 * @param sortProperty
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/query/taskbill/by/condition")
	public @ResponseBody Object getTaskStatus0RepaymentTask(HttpServletRequest request,
			@RequestParam(value = "userId") String userId,
			@RequestParam(value = "creditCardNumber") String creditCardNumber,
			// status字段含义: 0:返回还款任务 1:未执行的还款任务 2:已执行但未完成的还款任务 3:已执行且已完成的还款任务  4:完成的还款任务中有失败消费任务的还款任务 5:执行失败的还款任务
			@RequestParam(value = "status", required = false, defaultValue = "0") String status,
			@RequestParam(value = "version", required = false, defaultValue = "1") String version,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {
		Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
		Map<String, Object> map = new HashMap<>();
		userId = userId.trim();
		creditCardNumber = creditCardNumber.trim();
		status = status.trim();
		List<RepaymentTaskPOJO> repaymentTaskPOJOs = null;

		Map<String, Object> verifyStringFiledIsNullMap = creditCardManagerAuthorizationHandle
				.verifyStringFiledIsNull(userId, creditCardNumber, status);
		if (!CommonConstants.SUCCESS.equals(verifyStringFiledIsNullMap.get(CommonConstants.RESP_CODE))) {
			return verifyStringFiledIsNullMap;
		}

		if ("0".equals(status)) {
			// 全部的还款任务:
			repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByUserIdAndCreditCardNumberAndVersion(userId, creditCardNumber,version,
					pageable);
		} else if ("1".equals(status)) {
			// 未执行的还款任务:taskStatus:0;
			repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByTaskStatus0RepaymentTaskAndVersion(userId, creditCardNumber,version,
					pageable);
		} else if ("2".equals(status)) {
			// 已执行未完成的还款任务:taskStatus:1 orderStatus:0;
			repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByTaskStatus1AndOrderStatus0RepaymentTaskAndVersion(userId,
					creditCardNumber, version,pageable);
		} else if ("3".equals(status)) {
			// 已执行已完成的还款任务:taskStatus:1 orderStatus:1;
			repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByTaskStatus1AndOrderStatus1RepaymentTaskAndVersion(userId,
					creditCardNumber, version,pageable);
		} else if ("4".equals(status)) {
			// 完成的还款任务中有失败消费任务: taskStatus:2 orderStatus:1;
			repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByTaskStatus2AndOrderStatus1RepaymentTaskAndVersion(userId,
					creditCardNumber, version, pageable);
		} else if ("5".equals(status)) {
			// 执行失败的还款任务: taskStatus:2 orderStatus:0
			repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByTaskStatus2AndOrderstatus0RepaymentTaskAndVersion(userId,
					creditCardNumber, version,pageable);
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		map.put(CommonConstants.RESULT, repaymentTaskPOJOs);
		return map;
	}

	/**
	 * 根据任务id查询对应的子任务
	 * @param request
	 * @param repaymentTaskId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/query/consumetask/by/repaymenttaskid")
	public @ResponseBody Object getTaskStatus0RepaymentTask(HttpServletRequest request,
			@RequestParam(value = "repaymentTaskId") String repaymentTaskId) {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> verifyStringFiledIsNullMap = creditCardManagerAuthorizationHandle
				.verifyStringFiledIsNull(repaymentTaskId);
		if (!CommonConstants.SUCCESS.equals(verifyStringFiledIsNullMap.get(CommonConstants.RESP_CODE))) {
			return verifyStringFiledIsNullMap;
		}
		List tasks = consumeTaskPOJOBusiness.findByRepaymentTaskId(repaymentTaskId);
		ConsumeTaskPOJO model = null;
		Date nowTime = new Date();
		if (tasks != null && tasks.size() > 0) {
			for (Object obj : tasks) {
				model = (ConsumeTaskPOJO) obj;
				model = processingData(model);
				model.setTaskType(0);
			}
		}

		RepaymentTaskPOJO repaymentTaskPOJO = repaymentTaskPOJOBusiness.findByRepaymentTaskId(repaymentTaskId);
		if (repaymentTaskPOJO != null) {
			repaymentTaskPOJO = processingData(repaymentTaskPOJO);
			repaymentTaskPOJO.setTaskType(1);
		}
		tasks.add(repaymentTaskPOJO);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		map.put(CommonConstants.RESULT, tasks);
		return map;
	}

	/**
	 * 根据还款任务id删除任务
	 * @param request
	 * @param
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/creditcardmanager/delete/repaymenttask/by/repaymenttaskid")
	public @ResponseBody Object deleteRepaymentTaskByTaskStatus7(HttpServletRequest request,
			@RequestParam(value="repaymentTaskId")String[] repaymentTaskIds
			){
		Map<String,Object>map = new HashMap<>();
		for (String repaymentTaskId : repaymentTaskIds) {
			RepaymentTaskPOJO repaymentTaskPOJO = repaymentTaskPOJOBusiness.findByRepaymentTaskId(repaymentTaskId);
			if(repaymentTaskPOJO==null || repaymentTaskPOJO.getTaskStatus().intValue() == 7 || repaymentTaskPOJO.getTaskStatus().intValue() != 0){
				continue;
			}
			
			Calendar calendar = Calendar.getInstance();
			Date nowDate = calendar.getTime();
			nowDate = DateUtil.getDateStringConvert(new Date(), nowDate, "yyyy-MM-dd HH:mm:ss");
			Date executeDate = DateUtil.getDateStringConvert(new Date(), repaymentTaskPOJO.getExecuteDateTime(), "yyyy-MM-dd HH:mm:ss");
			if(nowDate.getTime() >= executeDate.getTime()){
				map.put(CommonConstants.RESP_MESSAGE, "由于该任务任务正在执行中,无法删除,删除后可能导致还款不成功!");
				continue;
			}
			List<ConsumeTaskPOJO> consumeTaskPOJOs = consumeTaskPOJOBusiness.findByRepaymentTaskId(repaymentTaskId);
			
			boolean isContinue = false;
			for (ConsumeTaskPOJO consumeTaskPOJO : consumeTaskPOJOs) {
				executeDate = DateUtil.getDateStringConvert(new Date(), consumeTaskPOJO.getExecuteDateTime(), "yyyy-MM-dd HH:mm:ss");
				if (nowDate.getTime() >= executeDate.getTime()) {
					map.put(CommonConstants.RESP_MESSAGE, "由于该任务任务正在执行中,无法删除,删除后可能导致还款不成功!");
					isContinue = true;
					break;
				}
			}
			if (isContinue) {
				continue;
			}
			
			repaymentTaskPOJO.setTaskStatus(7);
			repaymentTaskPOJO.setDescription(repaymentTaskPOJO.getDescription()+"|用户已删除");
			
			for(ConsumeTaskPOJO consumeTaskPOJO:consumeTaskPOJOs){
				consumeTaskPOJO.setTaskStatus(7);
				consumeTaskPOJO.setDescription(consumeTaskPOJO.getDescription()+"|用户已删除");
			}
			consumeTaskPOJOBusiness.deleteRepaymentTaskAndConsumeTask(repaymentTaskPOJO,consumeTaskPOJOs);
			String createTime = repaymentTaskPOJO.getCreateTime();
			
			String creditCardNumber = repaymentTaskPOJO.getCreditCardNumber();
			RepaymentBill repaymentBill = repaymentBillBusiness.findByCreditCardNumberAndCreateTime(creditCardNumber,createTime);
			if (repaymentBill != null) {
				BigDecimal taskAmount = repaymentBill.getTaskAmount().subtract(repaymentTaskPOJO.getAmount());
				if (taskAmount.compareTo(BigDecimal.ZERO) <= 0) {
					repaymentBillBusiness.delete(repaymentBill);
				}else {
					List<RepaymentTaskPOJO> repaymentTasks = repaymentTaskPOJOBusiness.findByUserIdAndCreditCardNumberAndCreateTime(repaymentBill.getUserId(), creditCardNumber, createTime);
					if (repaymentTasks == null || repaymentTasks.size() < 1) {
						repaymentBillBusiness.delete(repaymentBill);
					}else {
						Collections.sort(repaymentTasks, new Comparator<RepaymentTaskPOJO>() {
							@Override
							public int compare(RepaymentTaskPOJO arg0, RepaymentTaskPOJO arg1) {
								String executeDateTime0 = arg0.getExecuteDateTime();
								String executeDateTime1 = arg1.getExecuteDateTime();
								Date time0 = DateUtil.getDateStringConvert(new Date(), executeDateTime0, "yyyy-MM-dd HH:mm:ss");
								Date time1 = DateUtil.getDateStringConvert(new Date(), executeDateTime1, "yyyy-MM-dd HH:mm:ss");
								return 	time1.compareTo(time0);
							}
						});
						
						repaymentBill.setLastExecuteDateTime(repaymentTasks.get(0).getExecuteDateTime());
						repaymentBill.setTaskAmount(taskAmount);
						repaymentBill.setTotalServiceCharge(repaymentBill.getTotalServiceCharge().subtract(repaymentTaskPOJO.getTotalServiceCharge()));
						repaymentBill.setTaskCount(repaymentBill.getTaskCount()-1);
						if (repaymentBill.getRepaymentedCount() >= repaymentBill.getTaskCount()) {
							if (repaymentBill.getTaskAmount().compareTo(repaymentBill.getRepaymentedAmount()) > 0) {
								repaymentBill.setTaskStatus(1);
							}else {
								repaymentBill.setTaskStatus(3);
							}
						}
						repaymentBill = repaymentBillBusiness.save(repaymentBill);
					}
				}
			}
		}
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		if (!map.containsKey(CommonConstants.RESP_MESSAGE)) {
			map.put(CommonConstants.RESP_MESSAGE, "删除成功!");
		}
		return map;
	}
	
	/**
	 * 根据userId查询还款任务
	 * @param request
	 * @param userId
	 * @param version
	 * @param page
	 * @param size
	 * @param direction
	 * @param sortProperty
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/creditcardmanager/find/repaymenttask/by/userid")
	public @ResponseBody Object getRepaymentTaskPOJOByUserId(HttpServletRequest request,
			@RequestParam(value="userId")String userId,
			@RequestParam(value="version",required=false,defaultValue="1")String version,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {
		if(page < 0){
			page = 0;
		}
		Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
		Map<String,Object>map = new HashMap<>();
		Page<RepaymentTaskPOJO> models = null;
		models = repaymentTaskPOJOBusiness.findByUserIdAndVersion(userId,version,pageable);
		
		map.put(CommonConstants.RESULT, models);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功!");
		return map;
		
	}

	/**
	 * 计算该卡在该通道的还款金额
	 * @param request
	 * @param userId
	 * @param creditCardNumber
	 * @param version
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/creditcardmanager/find/allamount/by/userid")
	public @ResponseBody Object getUserAllConsumeAmountAndRepaymentAmount(HttpServletRequest request,
			@RequestParam(value="userId")String userId,
			@RequestParam(value="creditCardNumber")String creditCardNumber,
			@RequestParam(value="version",required=false,defaultValue="1")String version
			){
		Map<String,Object>map = new HashMap<>();
		Map<String, Object> verifyStringFiledIsNullMap = creditCardManagerAuthorizationHandle.verifyStringFiledIsNull(userId,creditCardNumber);
		if(!CommonConstants.SUCCESS.equals(verifyStringFiledIsNullMap.get(CommonConstants.RESP_CODE))){
			return verifyStringFiledIsNullMap;
		}
		
		BigDecimal consumeAmount = consumeTaskPOJOBusiness.findAllRealAmountByUserIdAndCreditCardNumberAndOrderStatus1AndVersion(userId,creditCardNumber,version);
		BigDecimal repaymentAmount = repaymentTaskPOJOBusiness.findAllRealAmountByUserIdAndCreditCardNumberAndOrderStatus1AndVersion(userId,creditCardNumber,version);
		if(consumeAmount == null){
			consumeAmount = BigDecimal.ZERO;
		}
		if(repaymentAmount == null){
			repaymentAmount = BigDecimal.ZERO;
		}
//		consumeTaskPOJOBusiness
//		repaymentTaskPOJOBusiness
		map.put("consumeAmount", consumeAmount);
		map.put("repaymentAmount", repaymentAmount);
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "查询成功");
		return map;
	}
	
	/**
	 * 根据userId和信用卡号查询所有还款账单
	 * @param userId
	 * @param creditCardNumber
	 * @param page
	 * @param size
	 * @param direction
	 * @param sortProperty
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(value="/v1.0/creditcardmanager/get/repaymentbill/by/userid")
	public @ResponseBody Object getUserRepaymentBillByUserId(
			@RequestParam(value = "userId") String userId,
			@RequestParam(value = "creditCardNumber") String creditCardNumber,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {
		Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
		Page<RepaymentBill> repaymentBills = repaymentBillBusiness.findByUserIdAndCreditCardNumber(userId,creditCardNumber,pageable);
		List<RepaymentBill> content = repaymentBills.getContent();
		String channelName = null;
		for (RepaymentBill repaymentBill : content) {
			String version = repaymentBill.getVersion();
			if (channelName == null) {
				CreditCardManagerConfig cardManagerConfig = creditCardManagerConfigBusiness.findByVersion(version);
				channelName = cardManagerConfig.getChannelName();
			}
			repaymentBill.setChannelName(channelName);
			String createTime = repaymentBill.getCreateTime();
			if (createTime.contains(".")) {
				repaymentBill.setCreateTime(createTime.substring(0,createTime.indexOf(".")));
			}
			String orderCode = userId +"-"+ DateUtil.getDateStringConvert(new String(), repaymentBill.getCreateTime(), "yyyyMMddHHmmss");
			repaymentBill.setOrderCode(orderCode);
		}
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功",repaymentBills);
	}
	
	
	@RequestMapping(value="/v1.0/creditcardmanager/update/repaymentbill/autorepayment")
	public @ResponseBody Object updateRepaymentBillAutoRepayment(
			@RequestParam(value = "repaymentbillId") String repaymentbillId
			) {
		RepaymentBill repaymentBill = repaymentBillBusiness.findById(Long.valueOf(repaymentbillId));
		if (repaymentBill != null) {
			if (repaymentBill.getTaskStatus() == 1) {
				repaymentBill.setTaskStatus(3);
				repaymentBillBusiness.save(repaymentBill);
				return ResultWrap.init(CommonConstants.SUCCESS, "设置成功!");
			}
		}
		return ResultWrap.init(CommonConstants.FALIED, "设置失败!");
	}
	
	/**
	 * 根据创建时间查询出指定账单
	 * @param userId
	 * @param creditCardNumber
	 * @param createTime
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(value="/v1.0/creditcardmanager/get/repaymentbill/by/createtime")
	public @ResponseBody Object getUserRepaymentBillByCreditCardNumberAndCreateTime(
			@RequestParam(value = "userId") String userId,
			@RequestParam(value = "creditCardNumber") String creditCardNumber,
			@RequestParam(value = "createTime") String createTime
			) {
		RepaymentBill repaymentBill = repaymentBillBusiness.findByCreditCardNumberAndCreateTime(creditCardNumber, createTime);
		List<RepaymentTaskPOJO> repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByUserIdAndCreditCardNumberAndCreateTime(userId, creditCardNumber, createTime);
		if (repaymentBill != null && repaymentTaskPOJOs != null && repaymentTaskPOJOs.size() > 0) {
			String version = repaymentBill.getVersion();
			CreditCardManagerConfig creditCardManagerConfig = creditCardManagerConfigBusiness.findByVersion(version);
			String channelName = "";
			if (creditCardManagerConfig != null) {
				channelName = creditCardManagerConfig.getChannelName();
			}
			repaymentBill.setChannelName(channelName);
			String orderCode = userId +"-"+ DateUtil.getDateStringConvert(new String(), DateUtil.getDateStringConvert(new Date(), createTime, "yyyy-MM-dd HH:mm:ss"), "yyyyMMddHHmmss");
			repaymentBill.setOrderCode(orderCode);
			repaymentBill.setCreateTime(createTime);
			repaymentBill.setLastExecuteDateTime(repaymentBill.getLastExecuteDateTime().substring(0, repaymentBill.getLastExecuteDateTime().length()-2));
			if (repaymentBill.getTaskStatus() == 3 || repaymentBill.getTaskStatus() == 1) {
				int taskStatus = repaymentBill.getTaskStatus();
				repaymentBill.setTaskStatus(1);
				if (repaymentBill.getTaskAmount().compareTo(repaymentBill.getRepaymentedAmount()) > 0) {
					repaymentBill.setTaskStatus(4);
					if (taskStatus != 3) {
						repaymentBill.setAutoRepayment(1);
					}
				}
			}
			
			if (CardConstss.CARD_VERSION_10.equals(repaymentBill.getVersion()) || CardConstss.CARD_VERSION_11.equals(repaymentBill.getVersion())) {
				BigDecimal totalRepaymentdAmount = BigDecimal.ZERO;
				for (RepaymentTaskPOJO repaymentTaskPOJO : repaymentTaskPOJOs) {
					if (repaymentTaskPOJO.getOrderStatus().intValue() == 1 ) {
						totalRepaymentdAmount = totalRepaymentdAmount.add(repaymentTaskPOJO.getRealAmount());
					}
				}
				repaymentBill.setRepaymentedAmount(totalRepaymentdAmount);
			}
			repaymentTaskPOJOs.sort((r1,r2) -> {
				return DateUtil.getDateStringConvert(new Date(), r1.getExecuteDateTime(), "yyyy-MM-dd HH:mm:ss").compareTo(DateUtil.getDateStringConvert(new Date(), r2.getExecuteDateTime(), "yyyy-MM-dd HH:mm:ss"));
			});
			
			Map<String, Object> map = ResultWrap.init(CommonConstants.SUCCESS, "查询成功",repaymentTaskPOJOs);
			map.put("repaymentBill", repaymentBill);
			return map;
		}else if(repaymentBill == null && repaymentTaskPOJOs != null && repaymentTaskPOJOs.size() > 0){
			repaymentBill = new RepaymentBill();
			String version = null;
			BigDecimal rate = BigDecimal.ZERO;
			BigDecimal serviceCharge = BigDecimal.ZERO;
			BigDecimal totalServiceCharge = BigDecimal.ZERO;
			BigDecimal usedCharge = BigDecimal.ZERO;
			BigDecimal reservedAmount = BigDecimal.ZERO;
			BigDecimal taskAmount = BigDecimal.ZERO;
			BigDecimal repaymentedAmount = BigDecimal.ZERO;
			int taskCount = repaymentTaskPOJOs.size();			
			int repaymentedCount = 0;
			int repaymentedSuccessCount = 0;
			int taskStatus = 0;
			String orderCode = "";
			String channelName = "";
			for (RepaymentTaskPOJO repaymentTaskPOJO : repaymentTaskPOJOs) {
				if (version == null) {
					version = repaymentTaskPOJO.getVersion();
					CreditCardManagerConfig creditCardManagerConfig = creditCardManagerConfigBusiness.findByVersion(version);
					if (creditCardManagerConfig != null) {
						channelName = creditCardManagerConfig.getChannelName();
					}
					orderCode = userId +"-"+ DateUtil.getDateStringConvert(new String(), DateUtil.getDateStringConvert(new Date(), createTime, "yyyy-MM-dd HH:mm:ss"), "yyyyMMddHHmmss");
					rate = repaymentTaskPOJO.getRate();
					serviceCharge = repaymentTaskPOJO.getServiceCharge();
				}



				taskAmount = taskAmount.add(repaymentTaskPOJO.getAmount());
				totalServiceCharge = totalServiceCharge.add(repaymentTaskPOJO.getTotalServiceCharge());
				if (reservedAmount.compareTo(repaymentTaskPOJO.getAmount()) < 0) {
					reservedAmount = repaymentTaskPOJO.getAmount();
				}
				if (repaymentTaskPOJO.getTaskStatus() == 0) {
					taskStatus = 2;
				}
				
				if (repaymentTaskPOJO.getTaskStatus() != 0) {
					repaymentedCount++;
				}
				if (repaymentTaskPOJO.getTaskStatus() == 1) {
					repaymentedAmount = repaymentedAmount.add(repaymentTaskPOJO.getRealAmount());
					usedCharge = usedCharge.add(repaymentTaskPOJO.getTotalServiceCharge());
					repaymentedSuccessCount++;
				}
			}
			if (taskAmount.compareTo(repaymentedAmount) <= 0) {
				taskStatus = 1;
			}else if(taskStatus != 2){
				taskStatus = 4;
			}
			if (repaymentedCount == 0) {
				taskStatus = 0;
			}
			
			repaymentBill.setUserId(userId);
			repaymentBill.setCreditCardNumber(creditCardNumber);
			repaymentBill.setVersion(version);
			repaymentBill.setChannelName(channelName);
			repaymentBill.setCreateTime(createTime);
			repaymentBill.setOrderCode(orderCode);
			repaymentBill.setRate(rate);
			repaymentBill.setReservedAmount(reservedAmount);
			repaymentBill.setRepaymentedAmount(repaymentedAmount);
			repaymentBill.setRepaymentedCount(repaymentedCount);
			repaymentBill.setRepaymentedSuccessCount(repaymentedSuccessCount);
			repaymentBill.setServiceCharge(serviceCharge);
			repaymentBill.setTaskAmount(taskAmount);
			repaymentBill.setTaskCount(taskCount);
			repaymentBill.setTaskStatus(taskStatus);
			repaymentBill.setTotalServiceCharge(totalServiceCharge);
			repaymentBill.setUsedCharge(usedCharge);
			repaymentBill.setLastExecuteDateTime(repaymentTaskPOJOs.get(repaymentTaskPOJOs.size()-1).getExecuteDateTime());
			repaymentTaskPOJOs.sort((r1,r2) -> {
				return DateUtil.getDateStringConvert(new Date(), r1.getExecuteDateTime(), "yyyy-MM-dd HH:mm:ss").compareTo(DateUtil.getDateStringConvert(new Date(), r2.getExecuteDateTime(), "yyyy-MM-dd HH:mm:ss"));
			});
			Map<String, Object> map = ResultWrap.init(CommonConstants.SUCCESS, "查询成功",repaymentTaskPOJOs);
			map.put("repaymentBill", repaymentBill);
			return map;
		}else {
			return ResultWrap.init(CommonConstants.FALIED, "暂无数据");
		}
	}
	
	/**
	 * 根据userId和信用卡号根据月份分组查询每月还款成功的金额
	 * @param userId
	 * @param creditCardNumber
	 * @param page
	 * @param size
	 * @param direction
	 * @param sortProperty
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(value="/v1.0/creditcardmanager/get/repaymentbill/by/creditcard/groupby/month")
	public @ResponseBody Object getUserRepaymentBillGroupByMonth(
			@RequestParam(value = "userId") String userId,
			@RequestParam(value = "creditCardNumber") String creditCardNumber,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "month", required = false) String sortProperty) {
		Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
		Page<Map<String,Object>> pages = repaymentTaskPOJOBusiness.findByUserIdAndCreditCardNumberAndOrderStatusGroupByMonth(userId,creditCardNumber,1,pageable);
		List<Map<String,Object>> content = new ArrayList<>(pages.getContent());
		for (Iterator iterator = content.iterator(); iterator.hasNext();) {
			Map<String, Object> map = (Map<String, Object>) iterator.next();

			String month = (String) map.get("month"); 
//			这个月还款成功的金额
			BigDecimal amount = (BigDecimal) map.get("amount");
			List<Map<String,Object>> list = new ArrayList<>();
			List<Object[]> repayments = repaymentTaskPOJOBusiness.findByUserIdAndCreditCardNumberAndOrderStatusAndMonthGroupByCreateTime(userId,creditCardNumber,1,month);
			repayments = new ArrayList<>(repayments);
			for (Object[] objects : repayments) {
				Map<String,Object> repaymentBillmap = new HashMap<>();
				String createTime = DateUtil.getDateStringConvert(new String(), objects[0], "yyyy-MM-dd HH:mm:ss");
//				批次时间
				repaymentBillmap.put("createTime", createTime);
//				这个月还款成功的金额
				repaymentBillmap.put("subAmount", objects[1]);
				RepaymentBill repaymentBill = repaymentBillBusiness.findByCreditCardNumberAndCreateTime(creditCardNumber, createTime);
				int taskStatus = 1;
//				计划还款金额
				BigDecimal taskAmount = BigDecimal.ZERO;
				if (repaymentBill != null) {
					if (repaymentBill.getVersion().equals(CardConstss.CARD_VERSION_19)) {
						map.put("amount", amount.subtract((BigDecimal)objects[1]));
						continue;
					}
					taskStatus = repaymentBill.getTaskStatus();
					taskAmount = repaymentBill.getTaskAmount();

//					taskStatus为3后台代表已关闭,前端任务是执行中,所以需要变成已成功
					if (taskStatus == 3) {
						taskStatus = 1;
					}
//					如果计划金额大于已还款金额,则为失败
					if (taskAmount.compareTo(repaymentBill.getRepaymentedAmount()) > 0) {
						taskStatus = 4;
					}
				}else {
					taskAmount = (BigDecimal) objects[3];
					if (taskAmount.compareTo((BigDecimal)objects[1]) > 0) {
						taskStatus = 4;
					}
				}
				
//				如果还款任务中有taskStatus最小值为0 或者 orderStatus最大值为4 则认为任务在执行中
				if ((int)objects[2] == 0 || (int)objects[4] > 1) {
					taskStatus = 3;
				}
//				taskStatus: 1:成功,2:待执行,3:执行中,4:失败
				repaymentBillmap.put("taskStatus", taskStatus);
				list.add(repaymentBillmap);
			}
			map.put("repaymentBill", list);
		}
		
		/*for (Map<String, Object> map : content) {
			String month = (String) map.get("month");
//			这个月还款成功的金额
			Object amount = map.get("amount");
			List<Map<String,Object>> list = new ArrayList<>();
			List<Object[]> repayments = repaymentTaskPOJOBusiness.findByUserIdAndCreditCardNumberAndOrderStatusAndMonthGroupByCreateTime(userId,creditCardNumber,1,month);
			for (Object[] objects : repayments) {
				Map<String,Object> repaymentBillmap = new HashMap<>();
				String createTime = DateUtil.getDateStringConvert(new String(), objects[0], "yyyy-MM-dd HH:mm:ss");
//				批次时间
				repaymentBillmap.put("createTime", createTime);
//				这个月还款成功的金额
				repaymentBillmap.put("subAmount", objects[1]);
				RepaymentBill repaymentBill = repaymentBillBusiness.findByCreditCardNumberAndCreateTime(creditCardNumber, createTime);
				int taskStatus = 1;
//				计划还款金额
				BigDecimal taskAmount = BigDecimal.ZERO;
				if (repaymentBill != null) {
					if (repaymentBill.getVersion().equals(CardConstss.CARD_VERSION_19)) {
						continue;
					}
					taskStatus = repaymentBill.getTaskStatus();
					taskAmount = repaymentBill.getTaskAmount();

//					taskStatus为3后台代表已关闭,前端任务是执行中,所以需要变成已成功
					if (taskStatus == 3) {
						taskStatus = 1;
					}
//					如果计划金额大于已还款金额,则为失败
					if (taskAmount.compareTo(repaymentBill.getRepaymentedAmount()) > 0) {
						taskStatus = 4;
					}
				}else {
					taskAmount = (BigDecimal) objects[3];
					if (taskAmount.compareTo((BigDecimal)objects[1]) > 0) {
						taskStatus = 4;
					}
				}
				
//				如果还款任务中有taskStatus最小值为0 或者 orderStatus最大值为4 则认为任务在执行中
				if ((int)objects[2] == 0 || (int)objects[4] > 1) {
					taskStatus = 3;
				}
//				taskStatus: 1:成功,2:待执行,3:执行中,4:失败
				repaymentBillmap.put("taskStatus", taskStatus);
				list.add(repaymentBillmap);
			}
			map.put("repaymentBill", list);
		}*/
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成",pages);
	}
	
	/**
	 * 根据月份查询用户信用卡的还款成功的任务
	 * @param userId
	 * @param creditCardNumber
	 * @param month
	 * @param page
	 * @param size
	 * @param direction
	 * @param sortProperty
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(value="/v1.0/creditcardmanager/get/repaymenttask/by/month")
	public @ResponseBody Object getUserRepaymentTaskByUserIdAndCreditCardNumberAndMonth(
			@RequestParam(value = "userId") String userId,
			@RequestParam(value = "creditCardNumber") String creditCardNumber,
			@RequestParam(value = "month") String month,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "executeDateTime", required = false) String sortProperty) {
		Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
		Page<RepaymentTaskPOJO> repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByUserIdAndCreditCardNumberAndOrderStatusAndExecuteDate(userId,creditCardNumber,1,month,pageable);
		List<RepaymentTaskPOJO> content = repaymentTaskPOJOs.getContent();
		for (RepaymentTaskPOJO repaymentTaskPOJO : content) {
			CreditCardManagerConfig cardManagerConfig = creditCardManagerConfigBusiness.findByVersion(repaymentTaskPOJO.getVersion());
			if (cardManagerConfig != null) {
				repaymentTaskPOJO.setChannelName(cardManagerConfig.getChannelName());
			}
		}
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成",repaymentTaskPOJOs);
	}
	
	/**
	 * 查询用户的所有可用银行卡
	 * @param request
	 * @param userId
	 * @param version
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/get/bankcard/by/userid")
	public @ResponseBody Object getCreditBankCardByUserId(HttpServletRequest request, 
			String userId,
			String version) {
		JSONObject creditBankCardByUserId = this.getCreditBankCardByUserId(userId);
		if (!CommonConstants.SUCCESS.equals(creditBankCardByUserId.getString(CommonConstants.RESP_CODE))) {
			return ResultWrap.init(CommonConstants.FALIED, creditBankCardByUserId.getString(CommonConstants.RESP_MESSAGE));
		}
		
		JSONArray creditBankCards = creditBankCardByUserId.getJSONArray(CommonConstants.RESULT);
		for (Object object : creditBankCards) {
			JSONObject json = (JSONObject) object;
			String cardNo = json.getString("cardNo");
			BigDecimal successAmount = BigDecimal.ZERO;
			BigDecimal undoAmount = BigDecimal.ZERO;
			BigDecimal failedAmount = BigDecimal.ZERO;
			BigDecimal allAmount = BigDecimal.ZERO;
			int allRepaymentCount = 0;
			String planCreateTime = "";
			json.put("successAmount", successAmount);
			json.put("undoAmount", undoAmount);
			json.put("failedAmount", failedAmount);
			json.put("allAmount", allAmount);
			json.put("planCreateTime", planCreateTime);
			json.put("allRepaymentCount", allRepaymentCount);
			json.put("repaymentModel", "0");
			boolean doesHaveTaskStatus0AndTaskType2ConsumeTaskPOJO = creditCardManagerAuthorizationHandle.verifyDoesHaveTaskStatus0AndTaskType2ConsumeTaskPOJO(userId, cardNo,version);
			boolean doesHaveTaskStatus0AndTaskType2RepaymentTaskPOJO = creditCardManagerAuthorizationHandle.verifyDoesHaveTaskStatus0AndTaskType2RepaymentTaskPOJO(userId, cardNo,version);
			if(doesHaveTaskStatus0AndTaskType2ConsumeTaskPOJO || doesHaveTaskStatus0AndTaskType2RepaymentTaskPOJO){
				RepaymentTaskPOJO repaymentTaskPOJO = repaymentTaskPOJOBusiness.findByUserIdAndCreditCardNumberAndTaskStatusAndTaskTypeAndOrderStatusAndVersion(userId, cardNo, 0, 2, 0, version);
				if (repaymentTaskPOJO == null) {
					repaymentTaskPOJO = repaymentTaskPOJOBusiness.findByUserIdAndCreditCardNumberAndTaskStatusAndTaskTypeAndOrderStatusAndVersion(userId, cardNo, 1, 2, 4, version);
					if (repaymentTaskPOJO == null) {
						continue;
					}
				}
				planCreateTime = repaymentTaskPOJO.getCreateTime();
				List<ConsumeTaskPOJO> consumeTaskPOJOs = consumeTaskPOJOBusiness.findByCreateTimeAndCreditCardNumberAndUserIdAndVersion(planCreateTime,cardNo,userId,version);
				for (ConsumeTaskPOJO consumeTaskPOJO : consumeTaskPOJOs) {
					if (consumeTaskPOJO.getOrderStatus().intValue() == 1) {
						successAmount = successAmount.add(consumeTaskPOJO.getAmount());
					}else {
						if (consumeTaskPOJO.getTaskStatus().intValue() == 0) {
							undoAmount = undoAmount.add(consumeTaskPOJO.getAmount());
						}else {
							failedAmount = failedAmount.add(consumeTaskPOJO.getAmount());
						}
					}
					allAmount = allAmount.add(consumeTaskPOJO.getAmount());
				}
				List<RepaymentTaskPOJO> repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByCreateTimeAndVersion(userId, cardNo, planCreateTime, version);
				allRepaymentCount = repaymentTaskPOJOs.size();
			}else {
				continue;
			}
			json.put("successAmount", successAmount);
			json.put("undoAmount", undoAmount);
			json.put("failedAmount", failedAmount);
			json.put("allAmount", allAmount);
			json.put("planCreateTime", StringUtil.isNullString(planCreateTime)?planCreateTime:DateUtil.getDateStringConvert(new String(), DateUtil.getDateStringConvert(new Date(), planCreateTime, "yyyy-MM-dd"), "yyyy/MM/dd"));
			json.put("allRepaymentCount", allRepaymentCount);
			if (CardConstss.CARD_VERSION_19.equals(version)) {
				json.put("repaymentModel", "1");
			}
		}
		String replace = creditBankCards.toString().replace("\\\"null\\\"", "");
		replace = replace.replace("null", "\"\"");
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功",JSONArray.fromObject(replace));
	}
	
	/**
	 * 查询用户的所有信用卡
	 * @param request
	 * @param userId
	 * @return
	 * <p>Description: </p>
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/creditcardmanager/get/creditcard/by/userid")
	public @ResponseBody Object getCreditBankCardByUserId(HttpServletRequest request, 
			String userId
			) {
		JSONObject creditBankCardByUserId = this.getCreditBankCardByUserId(userId);
		if (!CommonConstants.SUCCESS.equals(creditBankCardByUserId.getString(CommonConstants.RESP_CODE))) {
			return ResultWrap.init(CommonConstants.FALIED, creditBankCardByUserId.getString(CommonConstants.RESP_MESSAGE));
		}
		
		JSONArray creditBankCards = creditBankCardByUserId.getJSONArray(CommonConstants.RESULT);
		JSONArray jsonArray = new JSONArray();
		for (Object object : creditBankCards) {
			JSONObject json = (JSONObject) object;
			String nature = json.getString("nature");

			if (nature.contains("贷")) {
				String billDay = json.getString("billDay");
				String repaymentDay = json.getString("repaymentDay");
				String cardNo = json.getString("cardNo");
				List<CreditCardAccount> creditCardAccounts = creditCardAccountBusiness.findByCreditCardNumber(cardNo);
				for (CreditCardAccount creditCardAccount : creditCardAccounts) {
					creditCardAccount.setBillDate(Integer.valueOf(billDay));
					creditCardAccount.setRepaymentDate(Integer.valueOf(repaymentDay));
					creditCardAccountBusiness.save(creditCardAccount);
				}
				BigDecimal successAmount = BigDecimal.ZERO;
				BigDecimal undoAmount = BigDecimal.ZERO;
				BigDecimal failedAmount = BigDecimal.ZERO;
				BigDecimal allAmount = BigDecimal.ZERO;
				int allRepaymentCount = 0;
				String planCreateTime = "";
				json.put("successAmount", successAmount);
				json.put("undoAmount", undoAmount);
				json.put("failedAmount", failedAmount);
				json.put("allAmount", allAmount);
				json.put("planCreateTime", planCreateTime);
				json.put("allRepaymentCount", allRepaymentCount);
				json.put("repaymentModel", "0");
				RepaymentTaskPOJO repaymentTaskPOJO = repaymentTaskPOJOBusiness.findByUserIdAndCreditCardNumberAndTaskStatusAndTaskTypeAndOrderStatus(userId, cardNo, 0, 2, 0);
				if (repaymentTaskPOJO != null) {
					String createTime = repaymentTaskPOJO.getCreateTime();
					planCreateTime = createTime;
					String version = repaymentTaskPOJO.getVersion();
					List<ConsumeTaskPOJO> consumeTaskPOJOs = consumeTaskPOJOBusiness.findByCreateTimeAndCreditCardNumberAndUserIdAndVersion(createTime,cardNo,userId,repaymentTaskPOJO.getVersion());
					for (ConsumeTaskPOJO consumeTaskPOJO : consumeTaskPOJOs) {
						if (consumeTaskPOJO.getOrderStatus().intValue() == 1) {
							successAmount = successAmount.add(consumeTaskPOJO.getAmount());
						}else {
							if (consumeTaskPOJO.getTaskStatus().intValue() == 0) {
								undoAmount = undoAmount.add(consumeTaskPOJO.getAmount());
							}else {
								failedAmount = failedAmount.add(consumeTaskPOJO.getAmount());
							}
						}
						allAmount = allAmount.add(consumeTaskPOJO.getAmount());
					}
					List<RepaymentTaskPOJO> repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByCreateTimeAndVersion(userId, cardNo, planCreateTime, version);
					allRepaymentCount = repaymentTaskPOJOs.size();
					
					json.put("successAmount", successAmount);
					json.put("undoAmount", undoAmount);
					json.put("failedAmount", failedAmount);
					json.put("allAmount", allAmount);
					json.put("planCreateTime", StringUtil.isNullString(planCreateTime)?planCreateTime:DateUtil.getDateStringConvert(new String(), DateUtil.getDateStringConvert(new Date(), planCreateTime, "yyyy-MM-dd"), "yyyy/MM/dd"));
					json.put("allRepaymentCount", allRepaymentCount);
					if (CardConstss.CARD_VERSION_19.equals(version)) {
						json.put("repaymentModel", "1");
					}				
				}
				jsonArray.add(json);
			}
		}
		String replace = jsonArray.toString().replace("\\\"null\\\"", "");
		replace = replace.replace("null", "\"\"");
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功",JSONArray.fromObject(replace));
	}
	
	/**
	 * 请求查询用户银行卡接口
	 * @param userId
	 * @return
	 * <p>Description: </p>
	 */
	private JSONObject getCreditBankCardByUserId(String userId) {
		String url = "http://user/v1.0/user/bank/query/useridandtype";
		LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<>();
		requestEntity.add("userId", userId);
		requestEntity.add("type", "0");
		String resultString = restTemplate.postForObject(url, requestEntity, String.class);
//		LOG.info(resultString);
		return JSONObject.fromObject(resultString);
	}
	
	
	@RequestMapping(value="/v1.0/creditcardmanager/get/all/channl/config")
	public @ResponseBody Object getAllChannelConfig() {
		List<CreditCardManagerConfig> creditCardManagerConfigs = creditCardManagerConfigBusiness.findAll();
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功",creditCardManagerConfigs);
	}
	
	@RequestMapping(value="/v1.0/creditcardmanager/get/task")
	public @ResponseBody Object getTask(
			String startTime,
			String endTime,
			String version,
			String creditCardNumber,
			String phone,
			String brandId,
			String createTime,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "executeDateTime", required = false) String sortProperty) {
		Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
		Set<String> userIds = null;
		if (!isNullString(phone)) {
			List<CreditCardAccount> creditCardAccounts =  creditCardAccountBusiness.findByPhone(phone);
			userIds = new HashSet<>();
			for (CreditCardAccount creditCardAccount : creditCardAccounts) {
				userIds.add(creditCardAccount.getUserId());
			}
		}
		
		List<CreditCardManagerConfig> creditCardManagerConfigs = creditCardManagerConfigBusiness.findAll();
		Map<String,String> configMap = new HashMap<>();
		for (CreditCardManagerConfig creditCardManagerConfig : creditCardManagerConfigs) {
			configMap.put(creditCardManagerConfig.getVersion(), creditCardManagerConfig.getChannelName());
		}
		
		Map<String,Object> map =  repaymentTaskPOJOBusiness.getTask(startTime,endTime,version,creditCardNumber,userIds,brandId,createTime,pageable);
		 Page<RepaymentTaskPOJO> result  = (Page<RepaymentTaskPOJO>) map.get(CommonConstants.RESULT);
		for (RepaymentTaskPOJO repaymentTaskPOJO : result) {
			repaymentTaskPOJO.setChannelName(configMap.containsKey(repaymentTaskPOJO.getVersion())?configMap.get(repaymentTaskPOJO.getVersion()):"");
		}
		return map;
	}
	
	public static boolean isNullString(String str) {
		return str==null||"".equals(str.trim())||"null".equalsIgnoreCase(str.trim());
	}

}
