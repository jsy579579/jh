package com.jh.paymentgateway.repository.ypl;

import com.jh.paymentgateway.pojo.ypl.YPLRegister;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface YPLRegisterRepository extends JpaRepository<YPLRegister, String>, JpaSpecificationExecutor<YPLRegister> {

    @Query("select ypl from YPLRegister ypl where ypl.idCard=?1")
    YPLRegister getYPLRegisterByIdCard(@Param("idCard") String idCard);
}
