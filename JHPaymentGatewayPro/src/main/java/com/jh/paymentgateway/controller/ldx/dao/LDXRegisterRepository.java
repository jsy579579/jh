package com.jh.paymentgateway.controller.ldx.dao;

import com.jh.paymentgateway.controller.ldx.pojo.LDXRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LDXRegisterRepository extends JpaRepository<LDXRegister, Long>, JpaSpecificationExecutor<LDXRegister> {

    @Query("select l from LDXRegister l where l.idCard=:idCard")
    LDXRegister queryByIdCard(@Param("idCard") String idCard);
}
