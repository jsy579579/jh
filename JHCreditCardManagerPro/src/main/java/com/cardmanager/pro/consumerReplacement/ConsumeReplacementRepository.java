package com.cardmanager.pro.consumerReplacement;

import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author jayden
 */
@Repository
public interface ConsumeReplacementRepository extends JpaRepository<ConsumeTaskPOJO,Long>, JpaSpecificationExecutor<ConsumeTaskPOJO> {

    @Query("select c from ConsumeTaskPOJO c where c.consumeTaskId=:failedConsumeTaskId and c.taskStatus=:taskStatus and c.orderStatus=:orderStatus")
    ConsumeTaskPOJO findByConsumeTaskId(@Param("failedConsumeTaskId") String failedConsumeTaskId, @Param("taskStatus") int taskStatus, @Param("orderStatus") int orderStatus);

    @Query("select c from ConsumeTaskPOJO c where c.repaymentTaskId=:repaymentTaskId")
    List<ConsumeTaskPOJO> findConsumeTaskByRepaymentTaskId(@Param("repaymentTaskId") String repaymentTaskId);

    @Query("select c from ConsumeTaskPOJO c where c.consumeTaskId=:consumeId2")
    ConsumeTaskPOJO findConsumeTaskByconsumeTaskId2(@Param("consumeId2") String consumeId2);
}
