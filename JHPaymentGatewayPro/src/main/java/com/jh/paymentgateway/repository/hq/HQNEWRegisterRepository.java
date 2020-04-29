package com.jh.paymentgateway.repository.hq;

import com.jh.paymentgateway.pojo.hq.HQNEWRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HQNEWRegisterRepository
        extends JpaRepository<HQNEWRegister, String>, JpaSpecificationExecutor<HQNEWRegister> {

     @Query("select hqnew  from  HQNEWRegister hqnew where hqnew.idCard=:idCard")
    HQNEWRegister getHQNEWRegisterByIdCard(@Param("idCard") String idCard);
//     @Query("select hqbnew from   HQNEWRegister  hqnew where hqnew.merchantCode=:merchno")
//    HQNEWRegister getHQNEWRegisterByMerchantCode(@Param("merchno")String merchno);
}
