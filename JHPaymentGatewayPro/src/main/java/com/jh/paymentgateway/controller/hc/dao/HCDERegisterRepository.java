package com.jh.paymentgateway.controller.hc.dao;


import com.jh.paymentgateway.controller.hc.pojo.HCDERegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HCDERegisterRepository extends JpaRepository<HCDERegister, Long>, JpaSpecificationExecutor<HCDERegister> {

    @Query("select re from HCDERegister re where re.idCard =:idcard")
    HCDERegister getHCDERegisterByIdCard(@Param("idcard") String idcard);

}
