package com.cardmanager.pro.business;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;

public interface RepaymentTaskPOJOBusiness {

	RepaymentTaskPOJO save(RepaymentTaskPOJO repaymentTaskPOJO);

	int queryTaskStatus0CountAndVersion(String userIdStr, String creditCardNumber, int taskStatus,String version);

	List<RepaymentTaskPOJO> findTaskTypeAndTaskStatus0RepaymentTaskAndVersion(int taskType,String version);

	int queryTaskStatus0AndTaskType0CountAndVersion(String userId, String creditCardNumber, int taskStatus, int taskType,String version);

	int queryOrderStatus1AndTaskType0CountAndVersion(String userId, String creditCardNumber, int orderStatus, int taskType,String version);

	RepaymentTaskPOJO[] saveArray(RepaymentTaskPOJO[] repaymentTaskPOJOs);

	List<RepaymentTaskPOJO> findByTaskStatus0RepaymentTaskAndVersion(String userId, String creditCardNumber,String version,Pageable pageable);

	List<RepaymentTaskPOJO> findByTaskStatus1AndOrderStatus0RepaymentTaskAndVersion(String userId, String creditCardNumber,String version,
			Pageable pageable);

	List<RepaymentTaskPOJO> findByTaskStatus1AndOrderStatus1RepaymentTaskAndVersion(String userId, String creditCardNumber,String version,
			Pageable pageable);

	List<RepaymentTaskPOJO> findByTaskStatus2AndOrderStatus1RepaymentTaskAndVersion(String userId, String creditCardNumber,String version,
			Pageable pageable);

	List<RepaymentTaskPOJO> findByTaskStatus2AndOrderstatus0RepaymentTaskAndVersion(String userId, String creditCardNumber,String version,
			Pageable pageable);

	List<RepaymentTaskPOJO> findByUserIdAndCreditCardNumberAndVersion(String userId, String creditCardNumber,String version, Pageable pageable);

	RepaymentTaskPOJO findByRepaymentTaskId(String repaymentTaskId);

	RepaymentTaskPOJO findByOrderCode(String orderCode);

	void updateTaskStatus4AndReturnMessageByRepaymentTaskId(String repaymentTaskId, String string);

	RepaymentTaskPOJO createNewRepaymentTaskPOJO(BigDecimal amount, String userId, String creditCardNumber, int taskType,String description,String createTime,BigDecimal serviceCharge,BigDecimal rate,BigDecimal returnServiceCharge,String channelId,String channelTag,String version,Date executeDate,String brandId);

	RepaymentTaskPOJO findByTaskType0AndTaskStatus0RepaymentTaskPOJOAndVersion(String userId, String creditCardNumber,String version);

	List<Object[]> findByCreateTimeAndVersion(String userId, String creditCardNumber,String version,Pageable pageable);

	List<RepaymentTaskPOJO> findByCreateTimeAndVersion(String userId, String creditCardNumber,String createTime,String version);

	int findByOrderStatus1AndTaskType0CountAndVersion(String userId, String creditCardNumber,String version);

	List<Object[]> findByCreateTimeAndTaskStatus1AndVersion(String userId, String creditCardNumber,String version,Pageable pageable);

	List<Object[]> findByCreateTimeAndTaskStatus2AndVersion(String userId, String creditCardNumber,String version,Pageable pageable);

	List<Object[]> findByCreateTimeAndExecuteDate0AndVersion(String userId, String creditCardNumber,String version,Pageable pageable);

	List<Object[]> findByCreateTimeAndExecuteDate1AndVersion(String userId, String creditCardNumber,String version,Pageable pageable);

	List<RepaymentTaskPOJO> findByExecuteDateAndVersion(String userId, String creditCardNumber,String version);

	Page<RepaymentTaskPOJO> findByUserIdAndVersion(String userId,String version,Pageable pageable);

	BigDecimal findAllRealAmountByUserIdAndCreditCardNumberAndOrderStatus1AndVersion(String userId, String creditCardNumber,String version);

	int queryTaskStatus1AndOrderStatus4CountAndVersion(String userId, String creditCardNumber, int taskStatus, int orderStatus,String version);

	RepaymentTaskPOJO findByCreditCardNumberAndTaskTypeAndVersion(String creditCardNumber, int taskType,String version);

	int findByUserIdAndCreditCardNumberAndCreateTimeAndOrderStatus1CountAndVersion(String userId, String creditCardNumber,String createTime,String version);

	List<RepaymentTaskPOJO> findByExecuteDateAndOrderStatusAndVersion(String executeDate, int orderStatus,String version);

	RepaymentTaskPOJO findByUserIdAndCreditCardNumberAndTaskStatusAndTaskTypeAndOrderStatusAndVersion(String userId, String creditCardNumber, int taskStatus,int taskType,int orderStatus,String version);

	List<Object[]> findByCreateTimeAndVersion(String userId, String version, Pageable pageable);

	List<RepaymentTaskPOJO> findByTaskTypeAndOrderStatusAndVersion(int taskType, int orderStatus, String version,Pageable pageable);

	void saveReapymentTaskAndConsumeTask(RepaymentTaskPOJO repaymentTaskPOJO, ConsumeTaskPOJO consumeTaskPOJO);

	RepaymentTaskPOJO updateTaskStatusAndOrderStatusAndReturnMessageByRepaymentTaskId(int taskStatus, int orderStatus, String returnMessage,String repaymentTaskId);

	List<RepaymentTaskPOJO> findByTaskTypeAndTaskStatusAndVersionAndExecuteDate(int taskType,int taskStatus, String version, String e);

	List<RepaymentTaskPOJO> findByUserIdAndCreditCardNumberAndCreateTime(String userId, String creditCardNumber,String createTime);

	Page<Map<String,Object>> findByUserIdAndCreditCardNumberAndOrderStatusGroupByMonth(String userId, String creditCardNumber,int orderStatus, Pageable pageable);

	void delete(RepaymentTaskPOJO repaymentTaskPOJO);

	Page<RepaymentTaskPOJO> findByUserIdAndCreditCardNumberAndOrderStatusAndExecuteDate(String userId,String creditCardNumber, int orderStatus, String month, Pageable pageable);

	List<Object[]> findByUserIdAndCreditCardNumberAndOrderStatusAndMonthGroupByCreateTime(String userId,String creditCardNumber, int orderStatus,String month);

	RepaymentTaskPOJO createNewRepaymentTaskPOJO(RepaymentTaskPOJO repaymentTaskPOJO);

	RepaymentTaskPOJO findByUserIdAndCreditCardNumberAndTaskStatusAndTaskTypeAndOrderStatus(String userId,String cardNo, int taskStatus, int taskType, int orderStatus);

	int findByCreditCardNumberAndTaskTypeAndTaskStatus(String creditCardNumber, int taskType, int taskStatus);

	Map<String, Object> getTask(String startTime, String endTime, String version, String creditCardNumber,Set<String> userIds, String brandId, String createTime, Pageable pageable);

	int findByCreditCardNumberAndTaskTypeAndTaskStatusAndExecuteDateTimeGrantThan(String creditCardNumber, int taskType, int taskStatus,String nowTime);

    List<RepaymentTaskPOJO> findAllStatus2(String tips, String oldtime, String nowTime, String version);

//	void deleteAllByStatusAndReturnMessage(int taskStatus);

	List<RepaymentTaskPOJO> findByUserIdAndBrandId(String userId, String brandId, Pageable pageable);

	List<RepaymentTaskPOJO> findByBrandId(String brandId, Pageable pageable);

	// 查询所有失败的订单
	List<RepaymentTaskPOJO> findTask();


    List<RepaymentTaskPOJO> findByExecuteDateAndMessage(String message, String time);

}
