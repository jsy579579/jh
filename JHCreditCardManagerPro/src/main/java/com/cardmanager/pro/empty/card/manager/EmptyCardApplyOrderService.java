package com.cardmanager.pro.empty.card.manager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.cardmanager.pro.authorization.CreditCardManagerAuthorizationHandle;
import com.cardmanager.pro.business.CreditCardAccountBusiness;
import com.cardmanager.pro.business.CreditCardManagerConfigBusiness;
import com.cardmanager.pro.executor.BaseExecutor;
import com.cardmanager.pro.pojo.ConsumeTaskVO;
import com.cardmanager.pro.pojo.CreditCardManagerConfig;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;
import com.cardmanager.pro.pojo.RepaymentTaskVO;
import com.cardmanager.pro.service.CreditCardManagerTaskService;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.StringUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@EnableAutoConfiguration
public class EmptyCardApplyOrderService extends BaseExecutor{
	
	private static Logger LOG = LoggerFactory.getLogger(EmptyCardApplyOrderService.class);
	
	@Autowired
	private CreditCardManagerAuthorizationHandle creditCardManagerAuthorizationHandle;
	
	@Autowired
	private EmptyCardApplyOrderBusiness emptyCardApplyOrderBusiness;
	
	@Autowired
	private BrandAccountBusiness brandAccountBusiness;
	
	@Autowired
	private CreditCardAccountBusiness CreditCardAccountBusiness;
	
	@Autowired
	private CreditCardManagerConfigBusiness creditCardManagerConfigBusiness;
	
	@Autowired
	private CreditCardManagerTaskService creditCardManagerTaskService;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@RequestMapping(value="/v1.0/creditcardmanager/empty/card/put/apply/order")
	public @ResponseBody Object putEmptyCardApplyOrderAPI(
			@RequestParam()String userId,
			@RequestParam()String creditCardNumber,
			@RequestParam()String taskAmount,
			@RequestParam()String reservedAmount,
			@RequestParam(required=false,defaultValue="2")String dayRepaymentCounts,
			@RequestParam()String executeDate,
			@RequestParam()String secondPhone,
			@RequestParam()String secondName,
			@RequestParam()String version,
			@ModelAttribute()EmptyCardApplyOrder emptyCardApplyOrder
			) {
		List<EmptyCardApplyOrder> emptyCardApplyOrders = emptyCardApplyOrderBusiness.findByCreditCardNumberAndOrderStatusIn(creditCardNumber,new int[] {0,1});
		for (EmptyCardApplyOrder emptyCardApplyOrder2 : emptyCardApplyOrders) {
			if (emptyCardApplyOrder2.getOrderStatus() == 0) {
				return ResultWrap.init(CommonConstants.FALIED, "您已有申请正在审核中,请等待审核......");
			}
			if (emptyCardApplyOrder2.getOrderStatus() == 1 || emptyCardApplyOrder2.getOrderStatus() == 3) {
				return ResultWrap.init(CommonConstants.FALIED, "您已有还款任务正在执行中,请等待任务执行完毕");
			}
		}
		
		boolean doesHaveTaskStatus0AndTaskType2RepaymentTaskPOJO = creditCardManagerAuthorizationHandle.verifyDoesHaveTaskStatus0AndTaskType2RepaymentTaskPOJO(userId, creditCardNumber,version);
		boolean verifyDoesHaveTaskStatus0AndTaskType2ConsumeTaskPOJO = creditCardManagerAuthorizationHandle.verifyDoesHaveTaskStatus0AndTaskType2ConsumeTaskPOJO(userId, creditCardNumber, version);
		if(doesHaveTaskStatus0AndTaskType2RepaymentTaskPOJO || verifyDoesHaveTaskStatus0AndTaskType2ConsumeTaskPOJO){
			return ResultWrap.init(CommonConstants.FALIED, "您有未执行计划,请等待任务执行完后再生成计划!");
		}
		
		String allRepaymentCount = executeDate.split(",").length*Integer.valueOf(dayRepaymentCounts)+"";
		JSONObject userInfo = this.getUserInfo(userId);
		if (!CommonConstants.SUCCESS.equals(userInfo.getString(CommonConstants.RESP_CODE))) {
			return userInfo;
		}
		userInfo = userInfo.getJSONObject(CommonConstants.RESULT);
		String brandId = userInfo.getString("brandId");
		String phone = userInfo.getString("phone");
		Map<String, Object> userChannelRate = this.getUserChannelRate(userId, brandId, version);
		if (!CommonConstants.SUCCESS.equals(userChannelRate.get(CommonConstants.RESP_CODE))) {
			return userChannelRate;
		}
		JSONObject userRate = (JSONObject) userChannelRate.get(CommonConstants.RESULT);
		String rateStr = userRate.getString("rate");
		String extraFeeStr = userRate.getString("extraFee");
		String withdrawFeeStr = userRate.getString("withdrawFee");
		BigDecimal rate = new BigDecimal(rateStr);
		BigDecimal serviceCharge = new BigDecimal(extraFeeStr).add(new BigDecimal(withdrawFeeStr)).setScale(2, BigDecimal.ROUND_UP);
		BigDecimal totalServiceCharge = new BigDecimal(taskAmount).multiply(rate).add(serviceCharge.multiply(new BigDecimal(allRepaymentCount))).setScale(2, BigDecimal.ROUND_UP);
		
		
		JSONObject resultJSONObject = creditCardManagerAuthorizationHandle.verifyCreditCard(userId, creditCardNumber);
		if(!CommonConstants.SUCCESS.equalsIgnoreCase(resultJSONObject.getString(CommonConstants.RESP_CODE))){
			return ResultWrap.init(CommonConstants.FALIED, resultJSONObject.getString(CommonConstants.RESP_MESSAGE).isEmpty()?"验证失败,原因:该卡不可用,请更换一张信用卡!":resultJSONObject.getString(CommonConstants.RESP_MESSAGE));
		}
		JSONObject resultBankCardJSONObject = resultJSONObject.getJSONObject(CommonConstants.RESULT);
		String idCard = resultBankCardJSONObject.getString("idcard");
		String name = resultBankCardJSONObject.getString("userName");
		String bankName = resultBankCardJSONObject.getString("bankName");
		
		emptyCardApplyOrder.setRate(rate);
		emptyCardApplyOrder.setServiceCharge(serviceCharge);
		emptyCardApplyOrder.setBrandId(brandId);
		emptyCardApplyOrder.setPhone(phone);
		emptyCardApplyOrder.setIdCard(idCard);
		emptyCardApplyOrder.setName(name);
		emptyCardApplyOrder.setBankName(bankName);
		emptyCardApplyOrder.setTotalServiceCharge(totalServiceCharge);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, +1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		emptyCardApplyOrder.setCloseTime(calendar.getTime());
		emptyCardApplyOrder.setCreateTime(new Date(System.currentTimeMillis()+new Random().nextInt(1800)*1000));
		emptyCardApplyOrder.setDayRepaymentCount(Integer.valueOf(dayRepaymentCounts));
		emptyCardApplyOrder.setAllRepaymentCount(Integer.valueOf(allRepaymentCount));
		emptyCardApplyOrder = emptyCardApplyOrderBusiness.save(emptyCardApplyOrder);
		
		return ResultWrap.init(CommonConstants.SUCCESS, "申请提交成功,请等待审核通过",emptyCardApplyOrder);
	}
	
	@RequestMapping(value="/v1.0/creditcardmanager/empty/card/get/apply/order")
	public @ResponseBody Object getEmptyCardApplyOrder(			
			String phone,
			String userId,
			String name,
			String creditCardNumber,
			String brandId,
			String status,
			String startTime,
			String endTime,
			@RequestParam(value = "page", defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size,
			@RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
			@RequestParam(value = "sort", defaultValue = "createTime", required = false) String sortProperty) {
		Pageable pageable = new PageRequest(page, size, new Sort(direction, sortProperty));
		Page<EmptyCardApplyOrder> emptyCardApplyOrders = emptyCardApplyOrderBusiness.getAppalyOrder(userId,phone,name,creditCardNumber,brandId,status,startTime,endTime,pageable);
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功",emptyCardApplyOrders);
	}
	
	@RequestMapping(value="/v1.0/creditcardmanager/empty/card/set/apply/order")
	public @ResponseBody Object getEmptyCardApplyOrder(
			@RequestParam()String applyOrderId,
//			1:通过  2:拒绝
 			@RequestParam()String orderStatus
			) {
		EmptyCardApplyOrder emptyCardApplyOrder = emptyCardApplyOrderBusiness.findById(Long.valueOf(applyOrderId));
		if (emptyCardApplyOrder == null) {
			return ResultWrap.init(CommonConstants.FALIED, "订单不存在");
		}
		BigDecimal reservedAmount = emptyCardApplyOrder.getReservedAmount();
		String brandId = emptyCardApplyOrder.getBrandId();
		BrandAccount brandAccount = brandAccountBusiness.findByBrandId(brandId);
		if (brandAccount == null || brandAccount.getBalance().compareTo(reservedAmount) < 0 && "1".equals(orderStatus)) {
			return ResultWrap.init(CommonConstants.FALIED, "品牌帐户余额不足,无法通过申请");
		}
		if (new Date().compareTo(emptyCardApplyOrder.getCloseTime()) > 0) {
			emptyCardApplyOrder.setOrderStatus(5);
			emptyCardApplyOrder = emptyCardApplyOrderBusiness.save(emptyCardApplyOrder);
			return ResultWrap.info(LOG, CommonConstants.FALIED, "该订单已超时关闭");
		}
		
		if ("1".equals(orderStatus) && emptyCardApplyOrder.getOrderStatus() == 0) {
			emptyCardApplyOrder.setOrderStatus(1);
			Map<String,Object> updateApplyOrderByOrder = (Map<String, Object>) this.updateApplyOrderByOrder(null,emptyCardApplyOrder.getId()+"");
			if (!CommonConstants.SUCCESS.equals(updateApplyOrderByOrder.get(CommonConstants.RESP_CODE))) {
				return updateApplyOrderByOrder;
			}
			brandAccount = brandAccountBusiness.updateAccount(brandAccount,1,reservedAmount,emptyCardApplyOrder);
		}
		if ("2".equals(orderStatus)) {
			emptyCardApplyOrder.setOrderStatus(2);
		}
		emptyCardApplyOrder = emptyCardApplyOrderBusiness.save(emptyCardApplyOrder);
		return ResultWrap.init(CommonConstants.SUCCESS, "审核成功",emptyCardApplyOrder);
	}
	
	
	/*@RequestMapping(value="/v1.0/creditcardmanager/empty/card/pay/charge")
	public @ResponseBody Object payRepaymentServiceCharge(HttpServletRequest request,HttpServletResponse response,
			@RequestParam()String applyOrderId
			) throws Exception {
		String channelTag = "SPALI_PAY";
		EmptyCardApplyOrder emptyCardApplyOrder = emptyCardApplyOrderBusiness.findById(Long.valueOf(applyOrderId));
		if (1 != emptyCardApplyOrder.getOrderStatus()) {
			return ResultWrap.init(CommonConstants.FALIED, "无需要缴费订单");
		}
		
		if (new Date().compareTo(emptyCardApplyOrder.getCloseTime()) > 0) {
			emptyCardApplyOrder.setOrderStatus(5);
			emptyCardApplyOrder = emptyCardApplyOrderBusiness.save(emptyCardApplyOrder);
			return ResultWrap.info(LOG, CommonConstants.FALIED, "该订单已超时关闭");
		}
		
		BigDecimal totalServiceCharge = emptyCardApplyOrder.getTotalServiceCharge();
		String phone = emptyCardApplyOrder.getPhone();
		String brandId = emptyCardApplyOrder.getBrandId();
		JSONObject addPaymentOrder = this.addPaymentOrder(totalServiceCharge.toString(), phone, channelTag);
		if (!CommonConstants.SUCCESS.equals(addPaymentOrder.getString(CommonConstants.RESP_CODE))) {
			return addPaymentOrder;
		}
		addPaymentOrder = addPaymentOrder.getJSONObject(CommonConstants.RESULT);
		String orderCode = addPaymentOrder.getString("ordercode");
		emptyCardApplyOrder.setPaychargeOrderCode(orderCode);
		emptyCardApplyOrder = emptyCardApplyOrderBusiness.save(emptyCardApplyOrder);
		
		JSONObject requestChannel = this.requestChannel(totalServiceCharge.toString(), orderCode, brandId, channelTag);
		if (!CommonConstants.SUCCESS.equals(requestChannel.getString(CommonConstants.RESP_CODE))) {
			Map<String,Object> map = new HashMap<>();
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "亲.支付失败!");
			map.put(CommonConstants.RESULT,"http://106.15.47.73/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲,支付失败!", "UTF-8"));
			response.sendRedirect("http://106.15.47.73/v1.0/facade/purchase/tofailepage?resp_message=" + URLEncoder.encode("亲,支付失败!", "UTF-8"));
			return map;
		}
		try {
			response.setContentType("text/html;charset=utf-8");
			response.getWriter().println(requestChannel.getString(CommonConstants.RESULT));
			response.getWriter().flush();
			response.getWriter().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}*/
	
	@RequestMapping(value="/v1.0/creditcardmanager/empty/card/update/applyorder/by/order")
	public @ResponseBody Object updateApplyOrderByOrder(
			String order,
			String applyOrderId
			) {
		EmptyCardApplyOrder emptyCardApplyOrder = null;
		if (!StringUtil.isNullString(order)) {
			JSONObject paymentOrder = JSONObject.fromObject(order);
			String orderCode = paymentOrder.getString("ordercode");
			emptyCardApplyOrder = emptyCardApplyOrderBusiness.findByPaychargeOrderCode(orderCode);
		}else if(!StringUtil.isNullString(applyOrderId)) {
			emptyCardApplyOrder = emptyCardApplyOrderBusiness.findById(Long.valueOf(applyOrderId));
		}else {
			return ResultWrap.info(LOG, CommonConstants.FALIED, "订单不存在");
		}

		if (emptyCardApplyOrder == null) {
			return ResultWrap.info(LOG, CommonConstants.FALIED, "订单不存在");
		}
		if (emptyCardApplyOrder.getOrderStatus() != 1) {
			return ResultWrap.info(LOG, CommonConstants.FALIED, "该订单非需缴费状态");
		}
		
		CreditCardManagerConfig creditCardManagerConfig = creditCardManagerConfigBusiness.findByVersion(emptyCardApplyOrder.getVersion());
		List<RepaymentTaskVO> createRepaymentTaskVO = createRepaymentTaskVO(emptyCardApplyOrder, creditCardManagerConfig);
		Map<String,Object> result = (Map<String, Object>) creditCardManagerTaskService.saveRepaymentTaskAndConsumeTaskAndTaskBill(null, JSONArray.fromObject(createRepaymentTaskVO).toString(), null, emptyCardApplyOrder.getTaskAmount().toString(), emptyCardApplyOrder.getReservedAmount().toString(), emptyCardApplyOrder.getVersion());
		if (!CommonConstants.SUCCESS.equals(result.get(CommonConstants.RESP_CODE))) {
			return result;
		}
		
		emptyCardApplyOrder.setOrderStatus(3);
		emptyCardApplyOrder.setResidueServiceCharge(emptyCardApplyOrder.getTotalServiceCharge());
		emptyCardApplyOrderBusiness.save(emptyCardApplyOrder);
		return ResultWrap.init(CommonConstants.SUCCESS, "缴费成功",createRepaymentTaskVO);
	}
	
//	public static void main(String[] args) {
//		EmptyCardApplyOrder emptyCardApplyOrder = new EmptyCardApplyOrder();
//		emptyCardApplyOrder.setAllRepaymentCount(4);
//		emptyCardApplyOrder.setDayRepaymentCount(2);
//		emptyCardApplyOrder.setExecuteDate("2019-02-28,2019-02-28");
//		emptyCardApplyOrder.setTaskAmount(BigDecimal.valueOf(2000));
//		emptyCardApplyOrder.setReservedAmount(BigDecimal.valueOf(700));
//		emptyCardApplyOrder.setRate(BigDecimal.valueOf(0.006));
//		emptyCardApplyOrder.setServiceCharge(BigDecimal.valueOf(1));
//		emptyCardApplyOrder.setTotalServiceCharge(emptyCardApplyOrder.getRate().multiply(emptyCardApplyOrder.getTaskAmount()));
//		CreditCardManagerConfig creditCardManagerConfig = new CreditCardManagerConfig();
//		creditCardManagerConfig.setPaySingleLimitMoney(BigDecimal.valueOf(10));
//		creditCardManagerConfig.setConSingleLimitCount(1);
//		creditCardManagerConfig.setConSingleLimitMoney(BigDecimal.valueOf(10));
//		creditCardManagerConfig.setConSingleMaxMoney(BigDecimal.valueOf(1000));
//		List<RepaymentTaskVO> createRepaymentTaskVO = createRepaymentTaskVO(emptyCardApplyOrder, creditCardManagerConfig);
//		for (RepaymentTaskVO repaymentTaskVO : createRepaymentTaskVO) {
//			System.out.println(repaymentTaskVO);
//		}
//	}
	
	private static List<RepaymentTaskVO> createRepaymentTaskVO(EmptyCardApplyOrder emptyCardApplyOrder,CreditCardManagerConfig creditCardManagerConfig){
		List<BigDecimal> createRepaymentAmount = createRepaymentAmount(emptyCardApplyOrder,creditCardManagerConfig);
		int allRepaymentCount = emptyCardApplyOrder.getAllRepaymentCount();
		int dayRepaymentCount = emptyCardApplyOrder.getDayRepaymentCount();
		int amountIndex = 0;
		Date nowTime = DateUtil.getDateStringConvert(new Date(), emptyCardApplyOrder.getCreateTime(), "yyyy-MM-dd HH:mm:ss");
		String executeDates = emptyCardApplyOrder.getExecuteDate();
		String[] split = executeDates.split(",");
		List<RepaymentTaskVO> repaymentTaskVOs = new ArrayList<>();
		
		int j = 0;
		out:
		for (String executeDate : split) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(DateUtil.getDateStringConvert(new Date(), executeDate, "yyyy-MM-dd"));
			calendar.set(Calendar.HOUR_OF_DAY, 9);
			calendar.set(Calendar.MINUTE, 0);
			String startExecuteDateTime = DateUtil.getDateStringConvert(new String(), calendar.getTime(), "yyyy-MM-dd HH:mm:ss");
			for (int i = 0; i < dayRepaymentCount; i++) {
				if (amountIndex == allRepaymentCount) {
					break out;
				}
				RepaymentTaskVO createRepaymentTaskVO = createRepaymentTaskVO(1, creditCardManagerConfig.getConSingleLimitMoney(), creditCardManagerConfig.getConSingleMaxMoney(), createRepaymentAmount.get(amountIndex), emptyCardApplyOrder.getUserId(),emptyCardApplyOrder.getCreditCardNumber(), emptyCardApplyOrder.getRate(), emptyCardApplyOrder.getServiceCharge(), creditCardManagerConfig.getChannelTag(), creditCardManagerConfig.getChannelId(), nowTime, startExecuteDateTime, emptyCardApplyOrder.getVersion());
				startExecuteDateTime = createRepaymentTaskVO.getConsumeTaskVOs().get(0).getExecuteDateTime();
				String repaymentExecuteTime = createRepaymentTaskVO.getExecuteDateTime();
				if (i == 0 && j == 0) {
					createRepaymentTaskVO.getConsumeTaskVOs().get(0).setExecuteDateTime(repaymentExecuteTime);
					createRepaymentTaskVO.setExecuteDateTime(startExecuteDateTime);
				}

				repaymentTaskVOs.add(createRepaymentTaskVO);
				amountIndex++;
			}
			j++;
		}
		return repaymentTaskVOs;
	}
	
	private static List<BigDecimal> createRepaymentAmount(EmptyCardApplyOrder emptyCardApplyOrder,CreditCardManagerConfig creditCardManagerConfig){
		BigDecimal taskAmount = emptyCardApplyOrder.getTaskAmount();
		int allRepaymentCount = emptyCardApplyOrder.getAllRepaymentCount() - 1;
		BigDecimal reservedAmount = emptyCardApplyOrder.getReservedAmount();
		
		BigDecimal firstAmount = emptyCardApplyOrder.getTotalServiceCharge();
		if (emptyCardApplyOrder.getTotalServiceCharge().compareTo(creditCardManagerConfig.getPaySingleLimitMoney()) < 0) {
			firstAmount = creditCardManagerConfig.getPaySingleLimitMoney();
		}
		firstAmount = firstAmount.setScale(0, BigDecimal.ROUND_UP);
		
		taskAmount = taskAmount.subtract(firstAmount);
		
		BigDecimal avgAmount = taskAmount.divide(BigDecimal.valueOf(allRepaymentCount),0,BigDecimal.ROUND_UP);
		List<BigDecimal> repaymentAmouns = new ArrayList<>();
		int count = allRepaymentCount/2;
		int randomAmount = reservedAmount.subtract(avgAmount).intValue();
		BigDecimal totalAmount = BigDecimal.ZERO;
		for (int i = 0; i < count; i++) {
			BigDecimal singleAmount1 = avgAmount.add(BigDecimal.valueOf(new Random().nextInt(randomAmount)));
			BigDecimal singleAmount2 = avgAmount.multiply(BigDecimal.valueOf(2)).subtract(singleAmount1);
			repaymentAmouns.add(singleAmount1);
			repaymentAmouns.add(singleAmount2);
			totalAmount = totalAmount.add(singleAmount1).add(singleAmount2);
		}
		if (taskAmount.compareTo(totalAmount) > 0) {
			repaymentAmouns.add(taskAmount.subtract(totalAmount));
		}else{
			repaymentAmouns.set(0, repaymentAmouns.get(0).subtract(totalAmount.subtract(taskAmount)));
		}
		repaymentAmouns.add(0, firstAmount);
		return repaymentAmouns;
	}
	
	private static RepaymentTaskVO createRepaymentTaskVO(int consumeCount,BigDecimal minConsumeAmount,BigDecimal maxConsumeAmount,BigDecimal repaymentAmount,String userId,String creditCardNumber,BigDecimal rate,BigDecimal serviceCharge,String channelTag,String channelId,Date now,String startExecuteDateTime,String version) {
		RepaymentTaskVO repaymentTaskVO = new RepaymentTaskVO();
		repaymentTaskVO.setAmount(repaymentAmount);
		repaymentTaskVO.setUserId(userId);
		repaymentTaskVO.setCreditCardNumber(creditCardNumber);
		repaymentTaskVO.setChannelId(channelId);
		repaymentTaskVO.setChannelTag(channelTag);
		repaymentTaskVO.setRate(rate);
		repaymentTaskVO.setServiceCharge(serviceCharge);
		repaymentTaskVO.setCreateTime(DateUtil.getDateStringConvert(new String(), now, "yyyy-MM-dd HH:mm:ss"));
		
		String executeDate = DateUtil.getDateStringConvert(new String(), DateUtil.getDateStringConvert(new Date(), startExecuteDateTime, "yyyy-MM-dd HH:mm:ss") , "yyyy-MM-dd");
		Date startTime = DateUtil.getDateStringConvert(new Date(), startExecuteDateTime, "yyyy-MM-dd HH:mm:ss");
		long minRandomTime = 18 * 60 * 1000;
		List<String> consumeExcuteDateTimes = new ArrayList<>();
		String consumeExcuteDateTime = null;
		for (int i = 0; i < consumeCount+1; i++) {
			consumeExcuteDateTime = DateUtil.getDateStringConvert(new String(),new Date(startTime.getTime()+minRandomTime+(new Random().nextInt(18*60*1000))),"yyyy-MM-dd HH:mm:ss");
			startTime = DateUtil.getDateStringConvert(new Date(),consumeExcuteDateTime , "yyyy-MM-dd HH:mm:ss");
			consumeExcuteDateTimes.add(consumeExcuteDateTime);
		}
		
		BigDecimal totalServiceCharge = BigDecimal.ZERO;
		totalServiceCharge = repaymentAmount.add(serviceCharge).divide(BigDecimal.ONE.subtract(rate),2,BigDecimal.ROUND_UP).subtract(repaymentAmount);
		repaymentTaskVO.setTotalServiceCharge(totalServiceCharge);
		
		repaymentTaskVO.setDescription("还款计划");
		repaymentTaskVO.setExecuteDateTime(consumeExcuteDateTimes.get(0));
		repaymentTaskVO.setExecuteDate(executeDate);
		repaymentTaskVO.setRepaymentTaskId(repaymentTaskVO.getExecuteDate().replace("-", "")+DateUtil.getDateStringConvert(new String(), new Date(),"HHSSS")+ new Random().nextInt(9)+ new Random().nextInt(9)+new Random().nextInt(9)+new Random().nextInt(9)+"1");
		List<String> consumeTypeName = new ArrayList<>();
		consumeTypeName.add("|娱乐");
		consumeTypeName.add("|购物");
		consumeTypeName.add("|其他");
		consumeTypeName.add("|通信");
		consumeTypeName.add("|交通");
		consumeTypeName.add("|住宿");
		consumeTypeName.add("|餐饮");
		
		BigDecimal avgConsumeAmount = minConsumeAmount;
		
		BigDecimal residueAmount = repaymentAmount.subtract(avgConsumeAmount.multiply(BigDecimal.valueOf(consumeCount)));
		List<BigDecimal> consumeAmounts = new ArrayList<>();
		for (int i = 0; i < consumeCount; i++) {
			BigDecimal consumeAmount = null;
			BigDecimal randomAmount;
			if (residueAmount.intValue() <= 0) {
				randomAmount = BigDecimal.ZERO;
			}else {
				randomAmount = BigDecimal.valueOf(new Random().nextInt(residueAmount.intValue()));
			}
			if (residueAmount.compareTo(BigDecimal.ZERO) > 0) {
				consumeAmount = avgConsumeAmount.add(randomAmount);
			}else {
				consumeAmount = avgConsumeAmount;
			}
			
			
			if (i == consumeCount-1) {
				consumeAmount = avgConsumeAmount.add(residueAmount);
				randomAmount = residueAmount;
			}
			
			if (consumeAmount.compareTo(maxConsumeAmount.subtract(totalServiceCharge)) > 0) {
				int random = new Random().nextInt(10);
				consumeAmount = maxConsumeAmount.subtract(totalServiceCharge).setScale(0, BigDecimal.ROUND_DOWN).subtract(BigDecimal.valueOf(random));
				randomAmount = consumeAmount.subtract(avgConsumeAmount);
			}
			
			residueAmount = residueAmount.subtract(randomAmount);
			consumeAmounts.add(consumeAmount);
//			System.out.println("residueAmount:"+residueAmount);
		}
		
		if (residueAmount.compareTo(BigDecimal.ZERO) > 0) {
			consumeAmounts.set(0, consumeAmounts.get(0).add(residueAmount));
		}
		
		BigDecimal totalAmount = BigDecimal.ZERO;
		for (BigDecimal bigDecimal : consumeAmounts) {
//			System.out.println(bigDecimal);
			totalAmount = totalAmount.add(bigDecimal);
		}
//		System.out.println("totalAmount:"+totalAmount);
		for (int i = 0; i < consumeCount; i++) {
			repaymentTaskVO.getConsumeTaskVOs().add(new ConsumeTaskVO());
			ConsumeTaskVO consumeTaskVO = repaymentTaskVO.getConsumeTaskVOs().get(i);
			consumeTaskVO.setUserId(userId);
			consumeTaskVO.setCreditCardNumber(creditCardNumber);
			consumeTaskVO.setChannelId(channelId);
			consumeTaskVO.setChannelTag(channelTag);
			// 设置消费任务
			consumeTaskVO.setDescription("消费计划");
			// 设置消费类型
			int randomInt = new Random().nextInt(consumeTypeName.size());
			consumeTaskVO.setConsumeType(consumeTypeName.get(randomInt));
			consumeTypeName.remove(randomInt);
			consumeTaskVO.setAmount(consumeAmounts.get(i));
			consumeTaskVO.setRealAmount(consumeAmounts.get(i).add(totalServiceCharge));
			consumeTaskVO.setServiceCharge(totalServiceCharge);
			consumeTaskVO.setRepaymentTaskId(repaymentTaskVO.getRepaymentTaskId());
			consumeTaskVO.setConsumeTaskId(Long.valueOf(repaymentTaskVO.getRepaymentTaskId())+(i+1)+"");
			consumeTaskVO.setExecuteDateTime(consumeExcuteDateTime);
			consumeTaskVO.setExecuteDate(executeDate);
			consumeTaskVO.setCreateTime(DateUtil.getDateStringConvert(new String(), now, "yyyy-MM-dd HH:mm:ss"));
		}
//		System.out.println(consumeExcuteDateTimes);
		return repaymentTaskVO;
	}	
	
	/*private JSONObject addPaymentOrder(String amount,String phone,String channelTag) {
		String url = "http://transactionclear/v1.0/transactionclear/payment/add";
		LinkedMultiValueMap<String,String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("type", CommonConstants.ORDER_TYPE_REPAYMENT_CHARGE);
		requestEntity.add("phone", phone);
		requestEntity.add("amount", amount);
		requestEntity.add("channel_tag", channelTag);
		requestEntity.add("desc", "空卡代还手续费");
		return restTemplate.postForObject(url, requestEntity, JSONObject.class);
	}
	
	private JSONObject requestChannel(String ammount,String orderCode,String brandId,String channelTag) {
		String url = "http://paymentchannel/v1.0/paymentchannel/topup/request";
		LinkedMultiValueMap<String,String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("amount", ammount);
		requestEntity.add("ordercode", orderCode);
		requestEntity.add("brandcode", brandId);
		requestEntity.add("orderdesc", "空卡代还手续费");
		requestEntity.add("channel_tag", channelTag);
		requestEntity.add("channel_type", "1");
		return restTemplate.postForObject(url, requestEntity, JSONObject.class);
	}*/

	
	@RequestMapping(value="/v1.0/creditcardmanager/empty/card/get/brand/account")
	public @ResponseBody Object getBrandAccountByBrandId(
			@RequestParam()String brandId
			) {
		BrandAccount brandAccount = brandAccountBusiness.findByBrandId(brandId);
		if (brandAccount == null) {
			brandAccount = new BrandAccount();
			brandAccount.setBrandId(brandId);
		}
		return ResultWrap.init(CommonConstants.SUCCESS, "查询成功",brandAccount);
	}
	
	@RequestMapping(value="/v1.0/creditcardmanager/empty/card/set/brand/account")
	public @ResponseBody Object setBrandAccountByBrandId(
			@RequestParam()String brandId,
//			0:加  1减
			@RequestParam()String addOrSub,
			@RequestParam()String amount
			) {
		BrandAccount brandAccount = brandAccountBusiness.findByBrandId(brandId);
		if ("0".equals(addOrSub)) {
			if (brandAccount == null) {
				brandAccount = new BrandAccount();
				brandAccount.setBrandId(brandId);
			}
			brandAccount.setBalance(brandAccount.getBalance().add(new BigDecimal(amount)).setScale(2, BigDecimal.ROUND_HALF_UP));
			brandAccount = brandAccountBusiness.save(brandAccount);
		}else if("1".equals(addOrSub)) {
			if (brandAccount == null) {
				return ResultWrap.init(CommonConstants.FALIED, "无该贴牌帐号");
			}
			BigDecimal balance = brandAccount.getBalance().subtract(new BigDecimal(amount)).setScale(2, BigDecimal.ROUND_HALF_UP);
			if (BigDecimal.ZERO.compareTo(balance) > 0) {
				balance = BigDecimal.ZERO;
			}
			brandAccount.setBalance(balance);
			brandAccount = brandAccountBusiness.save(brandAccount);
		}
		return ResultWrap.init(CommonConstants.SUCCESS, "修改成功",brandAccount);
	}
	
	@RequestMapping(value="/v1.0/creditcardmanager/empty/card/test")
	public @ResponseBody Object test(
			String userId,
			String createTime,
			String version,
			String creditCardNumber
			) {
		List<RepaymentTaskPOJO> repayments = repaymentTaskPOJOBusiness.findByCreateTimeAndVersion(userId, creditCardNumber, createTime, version);
		repayments.sort((r1,r2)->{
			return DateUtil.getDateStringConvert(new Date (), r1.getExecuteDateTime(), "yyyy-MM-dd HH:mm:ss").compareTo(DateUtil.getDateStringConvert(new Date (), r2.getExecuteDateTime(), "yyyy-MM-dd HH:mm:ss"));
		});
		return repayments;
//		return emptyCardApplyOrderBusiness.findByCreditCardNumberAndCreateTime(creditCardNumber, DateUtil.getDateStringConvert(new Date(), createTime, "yyyy-MM-dd HH:mm:ss"));
	}
	
}
