package com.jh.paymentgateway.repository.apdh;

import com.jh.paymentgateway.pojo.apdh.APDHXRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface APDHXRegisterRepository extends JpaRepository<APDHXRegister, Long>, JpaSpecificationExecutor<APDHXRegister> {

    @Query("select bq from APDHXRegister bq where bq.idCard=:idCard")
    public APDHXRegister getBQRegisterByIdCard(@Param("idCard") String idCard);
}
