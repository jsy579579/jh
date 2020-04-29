package com.jh.paymentgateway.controller.qysh.dao;


import com.jh.paymentgateway.controller.qysh.pojo.QYSHRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QYSHRegisterRepository extends JpaRepository<QYSHRegister, Long>, JpaSpecificationExecutor<QYSHRegister> {

    @Query("select re from QYSHRegister re where re.idCard =:idcard")
    QYSHRegister getQYSHRegisterByIdCard(@Param("idcard") String idcard);

}
