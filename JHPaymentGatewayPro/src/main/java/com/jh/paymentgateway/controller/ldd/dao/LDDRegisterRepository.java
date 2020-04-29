package com.jh.paymentgateway.controller.ldd.dao;

import com.jh.paymentgateway.controller.ldd.pojo.LDDRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LDDRegisterRepository extends JpaRepository<LDDRegister, Long>, JpaSpecificationExecutor<LDDRegister> {

    @Query("select l from LDDRegister l where l.idCard=:idCard")
    LDDRegister queryByIdCard(@Param("idCard") String idCard);
}
