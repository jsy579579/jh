package com.jh.paymentgateway.controller.yxe.repository;


import com.jh.paymentgateway.controller.yxe.pojo.YXERegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface YXERegisterRepository extends JpaRepository<YXERegister,Long>, JpaSpecificationExecutor<YXERegister> {

    @Query("select yxe from YXERegister yxe where yxe.idCard=:idCard and yxe.bankCard=:bankCard")
    YXERegister getYXERegisterByIdCard(@Param("idCard") String idCard, @Param("bankCard") String bankCard);

}
