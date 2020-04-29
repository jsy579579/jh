package com.jh.paymentgateway.controller.hqk.Repository;


import com.jh.paymentgateway.controller.hqk.pojo.HQKRegister;
import com.jh.paymentgateway.pojo.hq.HQNEWRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HQKregisterRepository extends JpaRepository<HQKRegister,Long>, JpaSpecificationExecutor<HQKRegister> {


    @Query("select hqnew  from  HQKRegister hqnew where hqnew.idCard=:idCard")
    HQKRegister getHQKRegisterByIdCard(@Param("idCard") String idCard);

}
