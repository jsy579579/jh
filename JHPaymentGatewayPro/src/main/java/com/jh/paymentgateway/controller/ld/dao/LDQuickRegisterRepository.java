package com.jh.paymentgateway.controller.ld.dao;


import com.jh.paymentgateway.controller.ld.pojo.LDQuickRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LDQuickRegisterRepository extends JpaRepository<LDQuickRegister, Long>, JpaSpecificationExecutor<LDQuickRegister> {

    @Query("select re from LDQuickRegister re where re.idCard =:idcard")
    LDQuickRegister queryByIdCard(@Param("idcard") String idcard);

}
