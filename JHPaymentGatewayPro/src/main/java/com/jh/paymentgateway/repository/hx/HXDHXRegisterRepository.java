package com.jh.paymentgateway.repository.hx;


import com.jh.paymentgateway.pojo.hx.HXDHXRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HXDHXRegisterRepository extends JpaRepository<HXDHXRegister,String >, JpaSpecificationExecutor<HXDHXRegister> {
    @Query("select hxdhx from HXDHXRegister hxdhx where hxdhx.idCard=:idCard")
    HXDHXRegister getHXDHXRegisterByIdCard(@Param("idCard") String idCard);
}
