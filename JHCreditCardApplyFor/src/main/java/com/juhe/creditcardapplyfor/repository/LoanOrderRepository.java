package com.juhe.creditcardapplyfor.repository;



import com.juhe.creditcardapplyfor.entity.LoanOrderEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanOrderRepository extends JpaRepository<LoanOrderEntity,Integer>, JpaSpecificationExecutor<LoanOrderEntity> {

    @Query(value = "select * from loan_order where client_no = :clientNo", nativeQuery = true)
    LoanOrderEntity findByClientNo(@Param("clientNo") String clientNo);

    @Query("select loanOrderEntity from LoanOrderEntity loanOrderEntity where loanOrderEntity.mobile = :phone and loanOrderEntity.clientNo = :orderCode")
    List<LoanOrderEntity> findByPhoneAndOrderCode(@Param("phone") String phone, @Param("orderCode") String orderCode, Pageable pageable);

    @Query("select loanOrderEntity from LoanOrderEntity loanOrderEntity where loanOrderEntity.mobile = :phone")
    List<LoanOrderEntity> findByPhone(@Param("phone") String phone, Pageable pageable);

    @Query("select loanOrderEntity from LoanOrderEntity loanOrderEntity where loanOrderEntity.clientNo = :orderCode")
    List<LoanOrderEntity> findByOrderCode(@Param("orderCode") String orderCode, Pageable pageable);
}
