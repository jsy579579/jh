package com.jh.paymentgateway.repository.hx;

import com.jh.paymentgateway.pojo.hxdhd.HXDHDRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HXDHDRegisterRepository extends JpaRepository<HXDHDRegister,Integer> , JpaSpecificationExecutor<HXDHDRegister> {
    @Query("select hxdhd from HXDHDRegister hxdhd where hxdhd.idCard=:idCard")
    HXDHDRegister getHXDHDRegisterByIdCard(@Param("idCard") String idCard);

    @Query("select hxdhd from HXDHDRegister hxdhd where hxdhd.bankCard=:accountNumber")
    HXDHDRegister getHXDHDRegisterByBankCard(@Param("accountNumber")String accountNumber);
}
