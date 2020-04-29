package com.jh.paymentgateway.repository.qj;

import com.jh.paymentgateway.pojo.qj.QJRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QJRegisterRepository
         extends JpaRepository<QJRegister,String>, JpaSpecificationExecutor<QJRegister> {
    @Query("select qj from QJRegister  qj  where qj.idCard=:idCard")
    QJRegister getQJRegisterByIdCard(@Param("idCard") String idCard);


}
