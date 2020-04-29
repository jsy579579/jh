package com.jh.user.repository;

import com.jh.user.pojo.BalanceTransfeRrecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;

@Repository
public interface BalanceTransfeRrecordRepository extends JpaRepository<BalanceTransfeRrecord,Long>, JpaSpecificationExecutor<BalanceTransfeRrecord> {

    //bankName
    @Query("select sum(balanceTransfeRrecord.amout) from  BalanceTransfeRrecord balanceTransfeRrecord " +
            "where balanceTransfeRrecord.assignor=:assignor and balanceTransfeRrecord.paystartTime>= :start and balanceTransfeRrecord.paystartTime< :end")
    BigDecimal queryAllAmount(@Param("assignor") Long id, @Param("start")Date start,@Param("end")Date end);
}
