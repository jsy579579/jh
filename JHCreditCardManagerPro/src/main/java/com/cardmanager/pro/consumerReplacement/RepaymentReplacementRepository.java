package com.cardmanager.pro.consumerReplacement;

import com.cardmanager.pro.pojo.RepaymentTaskPOJO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RepaymentReplacementRepository extends JpaRepository<RepaymentTaskPOJO,Long>, JpaSpecificationExecutor<RepaymentTaskPOJO> {

    @Query("select c from RepaymentTaskPOJO c where c.repaymentTaskId=:repaymentTaskId")
    RepaymentTaskPOJO findRepaymentTaskByRepaymentTaskId(@Param("repaymentTaskId") String repaymentTaskId);
}
