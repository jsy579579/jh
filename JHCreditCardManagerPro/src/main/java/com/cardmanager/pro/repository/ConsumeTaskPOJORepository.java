package com.cardmanager.pro.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cardmanager.pro.pojo.ConsumeTaskPOJO;

import javax.transaction.Transactional;

@Repository
public interface ConsumeTaskPOJORepository extends JpaRepository<ConsumeTaskPOJO, Long>,JpaSpecificationExecutor<ConsumeTaskPOJO>{

	@Query("select count(*) from ConsumeTaskPOJO consumeTaskPOJO where consumeTaskPOJO.userId=:userId and consumeTaskPOJO.creditCardNumber =:creditCardNumber and consumeTaskPOJO.taskStatus=:taskStatus and consumeTaskPOJO.version=:version")
	int queryTaskStatusCountAndVersion(@Param("userId")String userIdStr,@Param("creditCardNumber")String creditCardNumber,@Param("taskStatus")int taskStatus,@Param("version")String version);

	@Query("select consumeTaskPOJO from ConsumeTaskPOJO consumeTaskPOJO where consumeTaskPOJO.taskType=:taskType and consumeTaskPOJO.taskStatus=:taskStatus and consumeTaskPOJO.executeDateTime <=:nowTime and consumeTaskPOJO.executeDate=:executeDate and consumeTaskPOJO.version=:version order by consumeTaskPOJO.executeDateTime ASC")
	List<ConsumeTaskPOJO> findTaskTypeAndTaskStatusRepaymentTaskAndVersion(@Param("taskType")int taskType,@Param("taskStatus")int taskStatus,@Param("nowTime")String nowTime,@Param("executeDate")String executeDate,@Param("version")String version);

	@Query("select count(*) from ConsumeTaskPOJO consumeTaskPOJO where consumeTaskPOJO.userId=:userId and consumeTaskPOJO.creditCardNumber =:creditCardNumber and consumeTaskPOJO.taskStatus=:taskStatus and consumeTaskPOJO.taskType=:taskType and consumeTaskPOJO.version=:version")
	int queryTaskStatusAndTaskTypeCountAndVersion(@Param("userId")String userId,@Param("creditCardNumber") String creditCardNumber,@Param("taskStatus") int taskStatus, @Param("taskType")int taskType,@Param("version")String version);

	@Query("select count(*) from ConsumeTaskPOJO consumeTaskPOJO where consumeTaskPOJO.userId=:userId and consumeTaskPOJO.creditCardNumber =:creditCardNumber and consumeTaskPOJO.orderStatus=:orderStatus and consumeTaskPOJO.taskType=:taskType and consumeTaskPOJO.version=:version")
	int queryOrderStatusAndTaskTypeCountAndVersion(@Param("userId")String userId,@Param("creditCardNumber") String creditCardNumber, @Param("orderStatus")int orderStatus, @Param("taskType")int taskType,@Param("version")String version);

	List<ConsumeTaskPOJO> findByRepaymentTaskId(String repaymentTaskId);

	ConsumeTaskPOJO findByOrderCode(String orderCode);

	@Query("select consumeTaskPOJO from ConsumeTaskPOJO consumeTaskPOJO where consumeTaskPOJO.userId=:userId and consumeTaskPOJO.creditCardNumber =:creditCardNumber and consumeTaskPOJO.taskType=:taskType and consumeTaskPOJO.taskStatus=:taskStatus and consumeTaskPOJO.version=:version")
	ConsumeTaskPOJO findByTaskTypeAndTaskStatusConsumeTaskPOJOAndVersion(@Param("userId")String userId, @Param("creditCardNumber")String creditCardNumber, @Param("taskType")int taskType, @Param("taskStatus")int taskStatus,@Param("version")String version);

	ConsumeTaskPOJO findByConsumeTaskId(String consumeTaskId);

	@Query("select sum(consumeTaskPOJO.realAmount) from ConsumeTaskPOJO consumeTaskPOJO where consumeTaskPOJO.userId=:userId and consumeTaskPOJO.creditCardNumber =:creditCardNumber and consumeTaskPOJO.orderStatus=:orderStatus and consumeTaskPOJO.version=:version")
	BigDecimal findSumRealAmountByUserIdAndCreditCardNumberAndOrderStatusAndVersion(@Param("userId")String userId, @Param("creditCardNumber")String creditCardNumber,@Param("orderStatus")int orderStatus,@Param("version")String version);

	ConsumeTaskPOJO findByUserIdAndCreditCardNumberAndTaskTypeAndVersion(String userIdStr, String creditCardNumber, int taskType,String version);

	ConsumeTaskPOJO findByCreditCardNumberAndTaskTypeAndVersion(String creditCardNumber, int taskType,String version);

	List<ConsumeTaskPOJO> findByTaskTypeAndOrderStatusAndVersion(int taskType, int orderStatus, String version);

	List<ConsumeTaskPOJO> findByTaskTypeAndTaskStatusAndVersionAndExecuteDate(int taskType, int taskStatus,String version, String executeDate);

	ConsumeTaskPOJO findByUserIdAndCreditCardNumberAndTaskStatusAndTaskTypeAndOrderStatusAndVersion(String userId,String creditCardNumber, int taskStatus, int taskType, int orderStatus, String version);

	List<ConsumeTaskPOJO> findByTaskTypeAndOrderStatusAndVersionAndExecuteDateTimeLessThan(int taskType,int orderStatus, String version, String nowTime);

	List<ConsumeTaskPOJO> findByCreateTimeAndCreditCardNumberAndUserIdAndVersion(String createTime, String cardNo,String userId, String version);
	
	@Query("select count(*) from ConsumeTaskPOJO consumeTaskPOJO where consumeTaskPOJO.creditCardNumber=:creditCardNumber and consumeTaskPOJO.taskType=:taskType and consumeTaskPOJO.taskStatus=:taskStatus and consumeTaskPOJO.executeDateTime >=:nowTime")
	int findByCreditCardNumberAndTaskTypeAndTaskStatusAndExecuteDateTimeGrantThan(@Param("creditCardNumber")String creditCardNumber, @Param("taskType")int taskType,@Param("taskStatus")int taskStatus, @Param("nowTime")String executeDateTime);

	List<ConsumeTaskPOJO> findByCreditCardNumberAndOrderStatusAndVersionInAndExecuteDateBetween(String creditCardNumber,int orderStatus, String[] versions, String startDate, String endDate);


	@Query("select c from ConsumeTaskPOJO c where c.orderStatus =:orderStatus")
	List<ConsumeTaskPOJO> findAllStatus4 (@Param("orderStatus") Integer orderStatus);

	@Query("select c from ConsumeTaskPOJO c where c.repaymentTaskId=:repaymentTaskId")
    List<ConsumeTaskPOJO> findAllByRepayment(@Param("repaymentTaskId") String repaymentTaskId);

	@Query("select c from ConsumeTaskPOJO c where c.userId=:userId and c.brandId=:brandId and (c.taskStatus=2 or c.taskStatus=4)")
	List<ConsumeTaskPOJO> findByUserIdAndBrandId(@Param("userId")String userId,@Param("brandId") String brandId, Pageable pageable);
	@Query("select c from ConsumeTaskPOJO c where  c.brandId=:brandId and (c.taskStatus=2 or c.taskStatus=4)")
	List<ConsumeTaskPOJO> findByBrandId(@Param("brandId")String brandId, Pageable pageable);

//	@Modifying
//	@Query("delete from ConsumeTaskPOJO c where c.consumeTaskId in (select a.consumeTaskId from (select d.consumeTaskId from ConsumeTaskPOJO d where d.taskStatus=:taskStatus and d.returnMessage is empty) a)")
//	void deleteAllByStatusAndReturnMessage(@Param("taskStatus") int taskStatus);


    //通过还款单号找到对应的消费订单号
    @Query("select c.consumeTaskId from ConsumeTaskPOJO c where c.repaymentTaskId=:repaymentTaskId and c.orderStatus='1' and c.taskStatus!='7'")
    List<ConsumeTaskPOJO> findByRepaymentTaskid(@Param("repaymentTaskId")String repaymentTaskId);
}
