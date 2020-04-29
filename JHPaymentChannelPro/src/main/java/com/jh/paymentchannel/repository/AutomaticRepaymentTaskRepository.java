package com.jh.paymentchannel.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.AutomaticRepaymentTask;
@Repository
public interface AutomaticRepaymentTaskRepository extends JpaRepository<AutomaticRepaymentTask, Integer>,JpaSpecificationExecutor<AutomaticRepaymentTask>{

	AutomaticRepaymentTask findByOrderCode(String orderCode);
	
	List<AutomaticRepaymentTask> findByUserId(int userId);
	
	@Query(value="select id,batch_no,pay_card,user_id,bind_id,order_code,execution_time,rate,realamount,amount,single_fee,status,type,create_time from t_automatic_repayment_task where execution_time between ?1 and ?2 and type=?3 and status='0'",nativeQuery = true)
	List<AutomaticRepaymentTask> findByExecutionTime(String start,String end,String type);
	
	@Query("select task from  AutomaticRepaymentTask task where task.userId=:userId and task.bindId=:bindId and task.status='0' order by task.executionTime")
	List<AutomaticRepaymentTask> findByUserIdAndStatusA(@Param("userId")int userId,@Param("bindId")String bindId);
	
	@Query("select task from  AutomaticRepaymentTask task where task.userId=:userId and task.bindId=:bindId and task.status!='0' order by task.executionTime")
	List<AutomaticRepaymentTask> findByUserIdAndStatusB(@Param("userId")int userId,@Param("bindId")String bindId);
	
	@Query("select task from  AutomaticRepaymentTask task where task.userId=:userId and task.bindId=:bindId order by task.executionTime")
	List<AutomaticRepaymentTask> findByUserIdAndStatusC(@Param("userId")int userId,@Param("bindId")String bindId);
	
	@Query("select task from  AutomaticRepaymentTask task where task.userId=:userId and task.payCard=:payCard and task.status='0' order by task.executionTime")
	List<AutomaticRepaymentTask> findByUserIdAndStatusD(@Param("userId")int userId,@Param("payCard")String bankNo);
	
	@Query("select task from  AutomaticRepaymentTask task where task.userId=:userId and task.payCard=:payCard and task.status!='0' order by task.executionTime")
	List<AutomaticRepaymentTask> findByUserIdAndStatusE(@Param("userId")int userId,@Param("payCard")String bankNo);

	List<AutomaticRepaymentTask> findByBindId(String bindId);
	
	@Query(value="select id,batch_no,pay_card,user_id,bind_id,order_code,execution_time,rate,realamount,amount,single_fee,status,type,create_time from t_automatic_repayment_task where (execution_time between ?1 and ?2) and type=?3 and pay_card=?4 and status in ('0','1')",nativeQuery = true)
	List<AutomaticRepaymentTask> findByPayCardAndStatus(String start,String end,String type,String payCard);
	
	@Query(value="select sum(amount) from t_automatic_repayment_task where execution_time between ?1 and ?2",nativeQuery = true)
	String SumAmount(String start,String end);
	
	@Query(value="select task  from AutomaticRepaymentTask task where batchNo=:batchNo")
	List<AutomaticRepaymentTask> findAutomaticByBatchNo(@Param("batchNo") String batchNo);
}
