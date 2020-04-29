package com.jh.paymentgateway.controller.hqt.repository;


import com.jh.paymentgateway.controller.hqt.pojo.HQTRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HQTRegisterRepository extends JpaRepository<HQTRegister,Long>, JpaSpecificationExecutor<HQTRegister> {
    @Query("select hqt from HQTRegister hqt where hqt.idCard=:idCard")
    HQTRegister getHQTRegisterByIdCard(@Param("idCard")String idCard);

}
