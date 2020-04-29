package com.jh.paymentgateway.controller.hqm.repository;


import com.jh.paymentgateway.controller.hqm.pojo.HQMRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HQMRegisterRepository extends JpaRepository<HQMRegister,Long>, JpaSpecificationExecutor<HQMRegister> {
    @Query("select hqt from HQMRegister hqt where hqt.idCard=:idCard")
    HQMRegister getHQTRegisterByIdCard(@Param("idCard") String idCard);

}
