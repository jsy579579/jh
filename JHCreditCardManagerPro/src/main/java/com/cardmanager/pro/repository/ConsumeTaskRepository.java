package com.cardmanager.pro.repository;

import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsumeTaskRepository extends JpaRepository<ConsumeTaskPOJO, String>, JpaSpecificationExecutor<ConsumeTaskPOJO> {

    @Query("select c from ConsumeTaskPOJO c where c.returnMessage=:message and c.orderStatus=:orderStatus and c.taskStatus=:taskStatus and c.executeDate=:executeDate")
    List<ConsumeTaskPOJO> findAbnormalConsumeTaskByMessage(@Param("message") String message, @Param("orderStatus") int orderStatus, @Param("taskStatus") int taskStatus, @Param("executeDate") String executeDate);

    @Query("select c from ConsumeTaskPOJO c where c.repaymentTaskId=:repaymentTaskId")
    List<ConsumeTaskPOJO> findConsumeTaskByRepaymentTaskId(@Param("repaymentTaskId") String repaymentTaskId);

    @Query("select c from ConsumeTaskPOJO c where c.returnMessage=:message and c.orderStatus=:orderStatus and c.taskStatus=:taskStatus and c.executeDate>=:executeDate and c.userId=:userId and c.creditCardNumber=:cardNo")
    List<ConsumeTaskPOJO> findAbnormalConsumeTaskByMessageUserId(@Param("message") String message, @Param("orderStatus") int orderStatus, @Param("taskStatus") int taskStatus, @Param("executeDate") String executeDate, @Param("userId") String userId, @Param("cardNo") String cardNo);

    @Query("select c from ConsumeTaskPOJO c where c.consumeTaskId=:consumeTaskId2")
    ConsumeTaskPOJO findConsumeTaskByconsumeTaskId2(@Param("consumeTaskId2") String consumeTaskId2);

    @Query("select c from ConsumeTaskPOJO c where c.version in (18,49) and c.taskStatus=0 and c.amount > 1000.00 ")
    List<ConsumeTaskPOJO> findByives();

    @Query("select c from ConsumeTaskPOJO c where c.version=:version and c.executeDate>=:executeDate group by c.creditCardNumber")
    List<ConsumeTaskPOJO> findByVersion(@Param("version") String version,@Param("executeDate") String executeDate);

    @Query("select c.creditCardNumber from ConsumeTaskPOJO c where c.version=:version and c.taskStatus=:taskStatus and c.orderStatus=:orderStatus and c.returnMessage=:returnMessage and c.executeDate=:executeDate group by c.creditCardNumber")
    List<String> findFailOrderByVersionAndReturnMessage(@Param("version") String version, @Param("taskStatus") int taskStatus, @Param("orderStatus") int orderStatus, @Param("returnMessage") String returnMessage, @Param("executeDate") String executeDate);


    @Query("select c.creditCardNumber from ConsumeTaskPOJO c where c.version=:version and c.taskStatus=:taskStatus and c.orderStatus=:orderStatus  and c.executeDate=:executeDate group by c.creditCardNumber")
    List<String> findFailOrderByVersionAndReturnMessage(@Param("version") String version, @Param("taskStatus") int taskStatus, @Param("orderStatus") int orderStatus, @Param("executeDate") String executeDate);

    @Query("select c from ConsumeTaskPOJO c where c.creditCardNumber in (:creditCardNos) and c.version=:version and c.executeDate>=:executeDate and c.taskStatus=:taskStatus and c.orderStatus=:orderStatus")
    List<ConsumeTaskPOJO> findOrderByCreditCardNo(@Param("creditCardNos") List<String> creditCardNos, @Param("executeDate") String executeDate,@Param("version") String version,@Param("taskStatus")int taskStatus,@Param("orderStatus")int orderStatus);
}
