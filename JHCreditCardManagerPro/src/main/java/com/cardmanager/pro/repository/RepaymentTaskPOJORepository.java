package com.cardmanager.pro.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cardmanager.pro.pojo.RepaymentTaskPOJO;
@Repository
public interface RepaymentTaskPOJORepository extends JpaRepository<RepaymentTaskPOJO, Long>,JpaSpecificationExecutor<RepaymentTaskPOJO> {

	@Query("select count(*) from RepaymentTaskPOJO repaymentTaskPOJO where repaymentTaskPOJO.userId=:userId and repaymentTaskPOJO.creditCardNumber=:creditCardNumber and repaymentTaskPOJO.taskStatus=:taskStatus and repaymentTaskPOJO.version=:version")
	int queryTaskStatusCountAndVersion(@Param("userId")String userIdStr, @Param("creditCardNumber")String creditCardNumber, @Param("taskStatus")int taskStatus,@Param("version")String version);

	@Query("select repaymentTaskPOJO from RepaymentTaskPOJO repaymentTaskPOJO where repaymentTaskPOJO.taskType=:taskType and repaymentTaskPOJO.taskStatus=:taskStatus and repaymentTaskPOJO.executeDateTime <=:nowTime and repaymentTaskPOJO.executeDate=:executeDate and repaymentTaskPOJO.version=:version order by repaymentTaskPOJO.executeDateTime ASC")
	List<RepaymentTaskPOJO> findTaskTypeAndTaskStatusRepaymentTaskAndVersion(@Param("taskType")int taskType,@Param("taskStatus") int taskStatus,@Param("nowTime") String nowTime,@Param("executeDate")String executeDate,@Param("version")String version);

	@Query("select count(*) from RepaymentTaskPOJO repaymentTaskPOJO where repaymentTaskPOJO.userId=:userId and repaymentTaskPOJO.creditCardNumber=:creditCardNumber and repaymentTaskPOJO.taskStatus=:taskStatus and repaymentTaskPOJO.taskType=:taskType and repaymentTaskPOJO.version=:version")
	int queryTaskStatusAndTaskTypeCountAndVersion(@Param("userId")String userId,@Param("creditCardNumber") String creditCardNumber, @Param("taskStatus")int taskStatus, @Param("taskType")int taskType,@Param("version")String version);

	@Query("select count(*) from RepaymentTaskPOJO repaymentTaskPOJO where repaymentTaskPOJO.userId=:userId and repaymentTaskPOJO.creditCardNumber=:creditCardNumber and repaymentTaskPOJO.orderStatus=:orderStatus and repaymentTaskPOJO.taskType=:taskType and repaymentTaskPOJO.version=:version")
	int queryOrderStatusAndTaskTypeCountAndVersion(@Param("userId")String userId, @Param("creditCardNumber")String creditCardNumber, @Param("orderStatus")int orderStatus, @Param("taskType")int taskType,@Param("version")String version);

	@Query("select repaymentTaskPOJO from RepaymentTaskPOJO repaymentTaskPOJO where repaymentTaskPOJO.userId=:userId and repaymentTaskPOJO.creditCardNumber=:creditCardNumber and repaymentTaskPOJO.taskStatus=:taskStatus and repaymentTaskPOJO.version=:version")
	List<RepaymentTaskPOJO> findByTaskStatusRepaymentTaskAndVersion(@Param("userId")String userId, @Param("creditCardNumber")String creditCardNumber, @Param("taskStatus")int taskStatus,@Param("version")String version,Pageable pageable);

	@Query("select repaymentTaskPOJO from RepaymentTaskPOJO repaymentTaskPOJO where repaymentTaskPOJO.userId=:userId and repaymentTaskPOJO.creditCardNumber=:creditCardNumber and repaymentTaskPOJO.taskStatus=:taskStatus and repaymentTaskPOJO.orderStatus=:orderStatus and repaymentTaskPOJO.version=:version")
	List<RepaymentTaskPOJO> findByTaskStatusAndOrderStatusRepaymentTaskAndVersion(@Param("userId")String userId, @Param("creditCardNumber")String creditCardNumber, @Param("taskStatus")int taskStatus,@Param("orderStatus")int orderStatus,@Param("version")String version, Pageable pageable);

	@Query("select repaymentTaskPOJO from RepaymentTaskPOJO repaymentTaskPOJO where repaymentTaskPOJO.userId=:userId and repaymentTaskPOJO.creditCardNumber=:creditCardNumber and repaymentTaskPOJO.taskStatus!='7' and repaymentTaskPOJO.version=:version")
	List<RepaymentTaskPOJO> findByUserIdAndCreditCardNumberAndVersion(@Param("userId")String userId,@Param("creditCardNumber")String creditCardNumber,@Param("version")String version, Pageable pageable);

	RepaymentTaskPOJO findByRepaymentTaskId(String repaymentTaskId);

	RepaymentTaskPOJO findByOrderCode(String orderCode);

	@Query("select repaymentTaskPOJO from RepaymentTaskPOJO repaymentTaskPOJO where repaymentTaskPOJO.userId=:userId and repaymentTaskPOJO.creditCardNumber=:creditCardNumber and repaymentTaskPOJO.taskType=:taskType and repaymentTaskPOJO.taskStatus=:taskStatus and repaymentTaskPOJO.version=:version")
	RepaymentTaskPOJO findByTaskTypeAndTaskStatusRepaymentTaskPOJOAndVersion(@Param("userId")String userId,@Param("creditCardNumber") String creditCardNumber,@Param("taskType")int taskType,@Param("taskStatus")int taskStatus,@Param("version")String version);

	@Query("select repaymentTaskPOJO from RepaymentTaskPOJO repaymentTaskPOJO where repaymentTaskPOJO.userId=:userId and repaymentTaskPOJO.creditCardNumber=:creditCardNumber and repaymentTaskPOJO.taskStatus!='7' and repaymentTaskPOJO.createTime=:createTime and repaymentTaskPOJO.version=:version order by repaymentTaskPOJO.executeDateTime")
	List<RepaymentTaskPOJO> findByCreateTimeAndVersion(@Param("userId")String userId, @Param("creditCardNumber")String creditCardNumber,@Param("createTime") String createTime,@Param("version")String version);

	@Query("select count(*) from RepaymentTaskPOJO repaymentTaskPOJO where repaymentTaskPOJO.userId=:userId and repaymentTaskPOJO.creditCardNumber=:creditCardNumber and repaymentTaskPOJO.orderStatus=:orderStatus and repaymentTaskPOJO.taskType=:taskType and repaymentTaskPOJO.version=:version")
	int findByOrderStatusAndTaskTypeCountAndVersion(@Param("userId")String userId, @Param("creditCardNumber")String creditCardNumber,@Param("orderStatus") int orderStatus, @Param("taskType")int taskType,@Param("version")String version);

	@Query("select repaymentTaskPOJO.userId,repaymentTaskPOJO.creditCardNumber,sum(repaymentTaskPOJO.amount),sum(repaymentTaskPOJO.realAmount),sum(repaymentTaskPOJO.totalServiceCharge),repaymentTaskPOJO.createTime,repaymentTaskPOJO.rate,max(repaymentTaskPOJO.taskStatus),repaymentTaskPOJO.executeDate,sum(repaymentTaskPOJO.returnServiceCharge),min(repaymentTaskPOJO.taskStatus),repaymentTaskPOJO.serviceCharge,repaymentTaskPOJO.orderStatus from RepaymentTaskPOJO repaymentTaskPOJO where repaymentTaskPOJO.userId=:userId and repaymentTaskPOJO.creditCardNumber=:creditCardNumber and repaymentTaskPOJO.taskStatus!='7' and repaymentTaskPOJO.version=:version group by repaymentTaskPOJO.createTime")
	List<Object[]> findByCreateTimeAndVersion(@Param("userId")String userId,@Param("creditCardNumber") String creditCardNumber,@Param("version")String version,Pageable pageable);
	
	@Query("select repaymentTaskPOJO.userId,repaymentTaskPOJO.creditCardNumber,sum(repaymentTaskPOJO.amount),sum(repaymentTaskPOJO.realAmount),sum(repaymentTaskPOJO.totalServiceCharge),repaymentTaskPOJO.createTime,repaymentTaskPOJO.rate,max(repaymentTaskPOJO.taskStatus),repaymentTaskPOJO.executeDate,sum(repaymentTaskPOJO.returnServiceCharge),min(repaymentTaskPOJO.taskStatus) from RepaymentTaskPOJO repaymentTaskPOJO where repaymentTaskPOJO.userId=:userId and repaymentTaskPOJO.creditCardNumber=:creditCardNumber and repaymentTaskPOJO.taskStatus!='7' and max(repaymentTaskPOJO.taskStatus)>=:taskStatus and repaymentTaskPOJO.version=:version group by repaymentTaskPOJO.createTime")
	List<Object[]> findByCreateTimeAndTaskStatusAndVersion(@Param("userId")String userId, @Param("creditCardNumber")String creditCardNumber,@Param("taskStatus")int taskStatus,@Param("version")String version,Pageable pageable);

	@Query("select repaymentTaskPOJO.userId,repaymentTaskPOJO.creditCardNumber,sum(repaymentTaskPOJO.amount),sum(repaymentTaskPOJO.realAmount),sum(repaymentTaskPOJO.totalServiceCharge),repaymentTaskPOJO.createTime,repaymentTaskPOJO.rate,max(repaymentTaskPOJO.taskStatus),repaymentTaskPOJO.executeDate,sum(repaymentTaskPOJO.returnServiceCharge),min(repaymentTaskPOJO.taskStatus) from RepaymentTaskPOJO repaymentTaskPOJO where repaymentTaskPOJO.userId=:userId and repaymentTaskPOJO.creditCardNumber=:creditCardNumber and repaymentTaskPOJO.taskStatus!='7' and repaymentTaskPOJO.executeDate=:executeDate and repaymentTaskPOJO.version=:version group by repaymentTaskPOJO.createTime")
	List<Object[]> findByCreateTimeAndExecuteDate0AndVersion(@Param("userId")String userId, @Param("creditCardNumber")String creditCardNumber,@Param("executeDate")String executeDate,@Param("version")String version,Pageable pageable);

	@Query("select repaymentTaskPOJO.userId,repaymentTaskPOJO.creditCardNumber,sum(repaymentTaskPOJO.amount),sum(repaymentTaskPOJO.realAmount),sum(repaymentTaskPOJO.totalServiceCharge),repaymentTaskPOJO.createTime,repaymentTaskPOJO.rate,max(repaymentTaskPOJO.taskStatus),repaymentTaskPOJO.executeDate,sum(repaymentTaskPOJO.returnServiceCharge),min(repaymentTaskPOJO.taskStatus) from RepaymentTaskPOJO repaymentTaskPOJO where repaymentTaskPOJO.userId=:userId and repaymentTaskPOJO.creditCardNumber=:creditCardNumber and repaymentTaskPOJO.taskStatus!='7' and repaymentTaskPOJO.executeDate>:executeDate and repaymentTaskPOJO.version=:version group by repaymentTaskPOJO.createTime")
	List<Object[]> findByCreateTimeAndExecuteDate1AndVersion(@Param("userId")String userId, @Param("creditCardNumber")String creditCardNumber, @Param("executeDate")String date,@Param("version")String version,Pageable pageable);

	@Query("select repaymentTaskPOJO from RepaymentTaskPOJO repaymentTaskPOJO where repaymentTaskPOJO.userId=:userId and repaymentTaskPOJO.creditCardNumber=:creditCardNumber and repaymentTaskPOJO.taskStatus!='7' and repaymentTaskPOJO.version=:version order by repaymentTaskPOJO.executeDate desc")
	List<RepaymentTaskPOJO> findByExecuteDateAndVersion(@Param("userId")String userId, @Param("creditCardNumber")String creditCardNumber,@Param("version")String version);

	@Query("select repaymentTaskPOJO from RepaymentTaskPOJO repaymentTaskPOJO where repaymentTaskPOJO.userId=:userId and repaymentTaskPOJO.taskStatus!='7' and repaymentTaskPOJO.version=:version")
	Page<RepaymentTaskPOJO> findByUserIdAndVersion(@Param("userId")String userId,@Param("version")String version,Pageable pageable);

	@Query("select sum(repaymentTaskPOJO.realAmount) from RepaymentTaskPOJO repaymentTaskPOJO where repaymentTaskPOJO.userId=:userId and repaymentTaskPOJO.creditCardNumber=:creditCardNumber and repaymentTaskPOJO.orderStatus=:orderStatus and repaymentTaskPOJO.version=:version")
	BigDecimal findSumRealAmountByUserIdAndCreditCardNumberAndOrderStatusAndVersion(@Param("userId")String userId,@Param("creditCardNumber") String creditCardNumber,@Param("orderStatus")int orderStatus,@Param("version")String version);

	@Query("select count(*) from RepaymentTaskPOJO repaymentTaskPOJO where repaymentTaskPOJO.userId=:userId and repaymentTaskPOJO.creditCardNumber=:creditCardNumber and repaymentTaskPOJO.taskStatus=:taskStatus and repaymentTaskPOJO.orderStatus=:orderStatus and repaymentTaskPOJO.version=:version")
	int queryTaskStatusAndOrderStatusCountAndVersion(@Param("userId")String userId, @Param("creditCardNumber")String creditCardNumber,@Param("taskStatus") int taskStatus, @Param("orderStatus")int orderStatus,@Param("version")String version);

	RepaymentTaskPOJO findByCreditCardNumberAndTaskTypeAndVersion(String creditCardNumber, int taskType,String version);

	@Query("select count(*) from RepaymentTaskPOJO repaymentTaskPOJO where repaymentTaskPOJO.userId=:userId and repaymentTaskPOJO.creditCardNumber=:creditCardNumber and repaymentTaskPOJO.orderStatus=:orderStatus and repaymentTaskPOJO.createTime =:createTime and repaymentTaskPOJO.version=:version")
	int findByUserIdAndCreditCardNumberAndCreateTimeAndOrderStatusCountAndVersion(@Param("userId")String userId, @Param("creditCardNumber")String creditCardNumber,@Param("createTime")String createTime, @Param("orderStatus")int orderStatus,@Param("version")String version);

	List<RepaymentTaskPOJO> findByExecuteDateAndOrderStatusAndVersion(String executeDate, int orderStatus,String version);

	List<RepaymentTaskPOJO> findByUserIdAndCreditCardNumberAndTaskStatusAndTaskTypeAndOrderStatusAndVersion(String userId,String creditCardNumber, int taskStatus, int taskType, int orderStatus, String version);
	
	@Query("select repaymentTaskPOJO.userId,repaymentTaskPOJO.creditCardNumber,sum(repaymentTaskPOJO.amount),sum(repaymentTaskPOJO.realAmount),sum(repaymentTaskPOJO.totalServiceCharge),repaymentTaskPOJO.createTime,repaymentTaskPOJO.rate,max(repaymentTaskPOJO.taskStatus),repaymentTaskPOJO.executeDate,sum(repaymentTaskPOJO.returnServiceCharge),min(repaymentTaskPOJO.taskStatus),repaymentTaskPOJO.serviceCharge,repaymentTaskPOJO.orderStatus from RepaymentTaskPOJO repaymentTaskPOJO where repaymentTaskPOJO.userId=:userId and repaymentTaskPOJO.taskStatus!='7' and repaymentTaskPOJO.version=:version group by repaymentTaskPOJO.createTime")
	List<Object[]> findByCreateTimeAndVersion(@Param("userId")String userId, @Param("version")String version, Pageable pageable);

	List<RepaymentTaskPOJO> findByTaskTypeAndOrderStatusAndVersion(int taskType, int orderStatus, String version,Pageable pageable);

	List<RepaymentTaskPOJO> findByTaskTypeAndTaskStatusAndVersionAndExecuteDate(int taskType, int taskStatus,String version, String executeDate);

	List<RepaymentTaskPOJO> findByTaskTypeAndOrderStatusAndVersionAndExecuteDateTimeLessThan(int taskType,int orderStatus, String version, String nowTime,Pageable pageable);

	List<RepaymentTaskPOJO> findByUserIdAndCreditCardNumberAndCreateTimeOrderByExecuteDateTimeAsc(String userId,String creditCardNumber, String createTime);

	@Query(value="select DATE_FORMAT(execute_date,'%Y-%m'),SUM(real_amount) from t_repayment_task where user_id=:userId and credit_card_number=:creditCardNumber and task_status!=7 group by DATE_FORMAT(execute_date,'%Y-%m') order by DATE_FORMAT(execute_date,'%Y-%m') DESC",nativeQuery=true)
	List<Object[]> findByUserIdAndCreditCardNumberGroupByMonth(@Param("userId")String userId, @Param("creditCardNumber")String creditCardNumber);

	@Query("select repaymentTaskPOJO from RepaymentTaskPOJO repaymentTaskPOJO where repaymentTaskPOJO.userId=:userId and repaymentTaskPOJO.creditCardNumber=:creditCardNumber and DATE_FORMAT(repaymentTaskPOJO.executeDate,'%Y-%m')=:month")
	Page<RepaymentTaskPOJO> findByUserIdAndCreditCardNumberAndExecuteDate(@Param("userId")String userId,@Param("creditCardNumber")String creditCardNumber, @Param("month")String month, Pageable pageable);

	List<RepaymentTaskPOJO> findByUserIdAndCreditCardNumberAndCreateTimeAndTaskStatusNotOrderByExecuteDateTimeAsc(String userId, String creditCardNumber, String createTime, int taskStatus);

	@Query(value="select create_time,SUM(real_amount),MIN(task_status),SUM(amount),MAX(order_status) from t_repayment_task where user_id=:userId and credit_card_number=:creditCardNumber and DATE_FORMAT(execute_date,'%Y-%m')=:month and task_status!=7 group by create_time order by create_time ASC",nativeQuery=true)
	List<Object[]> findByUserIdAndCreditCardNumberAndMonthGroupByCreateTime(@Param("userId")String userId,@Param("creditCardNumber")String creditCardNumber,@Param("month")String month);

	List<RepaymentTaskPOJO> findByUserIdAndCreditCardNumberAndTaskStatusAndTaskTypeAndOrderStatus(String userId,String cardNo, int taskStatus, int taskType, int orderStatus);

	@Query("select count(*) from RepaymentTaskPOJO repaymentTaskPOJO where repaymentTaskPOJO.creditCardNumber=:creditCardNumber and repaymentTaskPOJO.taskStatus=:taskStatus and repaymentTaskPOJO.taskType =:taskType")
	int findByCreditCardNumberAndTaskTypeAndTaskStatus(@Param("creditCardNumber")String creditCardNumber, @Param("taskType")int taskType, @Param("taskStatus")int taskStatus);

	@Query("select count(*) from RepaymentTaskPOJO repaymentTaskPOJO where repaymentTaskPOJO.creditCardNumber=:creditCardNumber and repaymentTaskPOJO.taskType=:taskType and repaymentTaskPOJO.taskStatus=:taskStatus and repaymentTaskPOJO.executeDateTime >=:nowTime")
	int findByCreditCardNumberAndTaskTypeAndTaskStatusAndExecuteDateTimeGrantThan(@Param("creditCardNumber")String creditCardNumber, @Param("taskType")int taskType, @Param("taskStatus")int taskStatus, @Param("nowTime")String nowTime);
	@Query("select  t from RepaymentTaskPOJO t where t.description like %?1% and t.executeDateTime>=?2 and t.executeDateTime<=?3 and t.version = ?4 and t.taskStatus = '2'")
    List<RepaymentTaskPOJO> findByDescriptionLikeAndExecuteDate(String tips, String oldtime, String nowTime, String version);

//	@Modifying
//	@Query("delete from RepaymentTaskPOJO r where r.taskStatus=:taskStatus and r.returnMessage is empty ")
//	void deleteAllByStatusAndReturnMessage(@Param("taskStatus") int taskStatus);
	@Query("select c from RepaymentTaskPOJO c where c.userId=:userId and c.brandId=:brandId and (c.taskStatus=2 or c.taskStatus=4)")
	List<RepaymentTaskPOJO> findByUserIdAndBrandId(@Param("userId")String userId,@Param("brandId") String brandId, Pageable pageable);
	@Query("select c from RepaymentTaskPOJO c where c.brandId=:brandId and (c.taskStatus=2 or c.taskStatus=4)")
	List<RepaymentTaskPOJO> findByBrandId(@Param("brandId") String brandId, Pageable pageable);

	@Query("select r from RepaymentTaskPOJO r where r.repaymentTaskId=:repaymentTaskId")
	RepaymentTaskPOJO findRepaymentTaskByRepaymentTaskId(@Param("repaymentTaskId") String repaymentTaskId);

	@Query("select r from RepaymentTaskPOJO r where  r.executeDate=:executeDate and r.taskStatus=:taskStatus and r.orderStatus=:orderStatus and r.version in (:version) and r.description like CONCAT('%',:description,'%')")
	List<RepaymentTaskPOJO> findRepaymentTaskByDescriptionAndVersionAndExeCuteDate(@Param("description") String description, @Param("version") String[] version, @Param("executeDate") String executeDate, @Param("taskStatus") int taskStatus, @Param("orderStatus") int orderStatus);

	@Query("select r from RepaymentTaskPOJO r where  r.executeDate like CONCAT(:executeDate,'%') and r.returnMessage like CONCAT('%',:message,'%')")
    List<RepaymentTaskPOJO> findByExecuteDateAndMessage(@Param("message") String message,@Param("executeDate")String time);
}
