package com.jh.paymentgateway.controller.tldh.Repository;


import com.jh.paymentgateway.controller.tldh.pojo.TLRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TLRegisterRepostory extends JpaRepository<TLRegister,Long>, JpaSpecificationExecutor<TLRegister> {
    @Query("select tl from TLRegister tl where tl.idcard=:idcard")
    TLRegister getTLRegisterByIdcard(@Param("idcard") String idcard);
}
