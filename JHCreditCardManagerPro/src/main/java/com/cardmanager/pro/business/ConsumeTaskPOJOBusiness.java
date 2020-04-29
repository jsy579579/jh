package com.cardmanager.pro.business;

import java.math.BigDecimal;
import java.util.List;

import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;
import org.springframework.data.domain.Pageable;

public interface ConsumeTaskPOJOBusiness {

	ConsumeTaskPOJO save(ConsumeTaskPOJO consumeTaskPOJO);

	int queryTaskStatus0CountAndVersion(String userIdStr, String creditCardNumber, int taskStatus,String version);

	List<ConsumeTaskPOJO> findTaskType2AndTaskStatus0RepaymentTaskAndVersion(String version);

	int queryTaskStatus0AndTaskType0CountAndVersion(String userId, String creditCardNumber, int taskStatus, int taskType,String version);

	int queryOrderStatus1AndTaskType0CountAndVersion(String userId, String creditCardNumber, int OrderStatus, int TaskType,String version);

	List<ConsumeTaskPOJO> saveArrayList(List<ConsumeTaskPOJO> consumeTaskPOJOs);

	List<ConsumeTaskPOJO> findByRepaymentTaskId(String repaymentTaskId);

	ConsumeTaskPOJO findByOrderCode(String orderCode);

	void updateTaskStatus4AndReturnMessageByRepaymentTaskId(String repaymentTaskId, String string);

	void saveArrayListTaskAll(List<ConsumeTaskPOJO> consumeTaskPOJOs, RepaymentTaskPOJO[] repaymentTaskPOJOs);

	int queryOrderStatus1AndTaskType0CountAndVersion(String userId, String creditCardNumber,String version);

	ConsumeTaskPOJO findByTaskType0AndTaskStatus0ConsumeTaskPOJOAndVersion(String userId, String creditCardNumber,String version);

	void deleteRepaymentTaskAndConsumeTask(RepaymentTaskPOJO repaymentTaskPOJO, List<ConsumeTaskPOJO> consumeTaskPOJOs);

	ConsumeTaskPOJO findByConsumeTaskId(String consumeTaskId);

	BigDecimal findAllRealAmountByUserIdAndCreditCardNumberAndOrderStatus1AndVersion(String userId, String creditCardNumber, String version);

	ConsumeTaskPOJO findByUserIdAndCreditCardNumberAndTaskTypeAndVersion(String userIdStr, String creditCardNumber, int taskType,String version);

	ConsumeTaskPOJO findByCreditCardNumberAndTaskTypeAndVersion(String creditCardNumber, int taskType,String version);

	ConsumeTaskPOJO createNewConsumeTaskPOJO(BigDecimal amount, String userId, String creditCardNumber, int i,
			String description, String createTime, BigDecimal serviceCharge, BigDecimal rate,
			BigDecimal returnServiceCharge, String channelId, String channelTag,String version);

	List<ConsumeTaskPOJO> findByTaskTypeAndOrderStatusAndVersion(int taskType, int orderStatus, String version);

	ConsumeTaskPOJO updateTaskStatusAndOrderStatusAndReturnMessageByConsumeTaskId(int taskStatus, int orderStatus, String returnMessage,String orderCode);

	List<ConsumeTaskPOJO> findByTaskTypeAndTaskStatusAndVersionAndExecuteDate(int taskType, int taskStatus, String version,
			String executeDate);

	ConsumeTaskPOJO findByUserIdAndCreditCardNumberAndTaskStatusAndTaskTypeAndOrderStatusAndVersion(String userId,String creditCardNumber, int taskStatus, int taskType, int orderStatus, String version);

	List<ConsumeTaskPOJO> findByCreateTimeAndCreditCardNumberAndUserIdAndVersion(String createTime, String cardNo,String userId, String version);

	int findByCreditCardNumberAndTaskTypeAndTaskStatusAndExecuteDateTimeGrantThan(String creditCardNumber, int taskType, int taskStatus,String executeDateTime);

	List<ConsumeTaskPOJO> findByCreditCardNumberAndOrderStatusAndVersionInAndExecuteDateBetween(String creditCardNumber, int orderStatus,String[] versions,String startDate, String endDate);

	public List<ConsumeTaskPOJO> findAllStatus4 (Integer orderStatus);

	List<ConsumeTaskPOJO> findAllByRepayment(String repaymentTaskId);

//	void deleteAllByStatusAndReturnMessage(int taskStatus);

	List<ConsumeTaskPOJO> findByUserIdAndBrandId(String userId, String brandId, Pageable pageable);

	List<ConsumeTaskPOJO> findByBrandId(String brandId, Pageable pageable);

	//通过还款订单号找到对应的消费订单号
    List<ConsumeTaskPOJO> findByRepaymentTaskid(String repaymentTaskId);
}
