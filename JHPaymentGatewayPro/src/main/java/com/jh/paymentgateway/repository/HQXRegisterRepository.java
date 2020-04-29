package com.jh.paymentgateway.repository;

import com.jh.paymentgateway.pojo.HQXRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HQXRegisterRepository  extends JpaRepository<HQXRegister, String>, JpaSpecificationExecutor<HQXRegister> {

    @Query("select hqx from  HQXRegister hqx where hqx.idCard=:idCard")
    HQXRegister getHQXRegisterByIdCard(@Param("idCard") String idCard);
   
}
