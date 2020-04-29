package com.jh.paymentgateway.repository.tl;

import com.jh.paymentgateway.pojo.tl.TLDHXRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TLDHXRegisterRepository extends JpaRepository<TLDHXRegister,String >, JpaSpecificationExecutor<TLDHXRegister> {
    @Query("select tldhx from TLDHXRegister tldhx where tldhx.idCard=:idCard")
    TLDHXRegister getTLDHXRegisterByIdCard(@Param("idCard") String idCard);
}
