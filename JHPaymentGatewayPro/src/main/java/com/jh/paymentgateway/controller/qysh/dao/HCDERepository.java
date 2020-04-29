package com.jh.paymentgateway.controller.qysh.dao;


import com.jh.paymentgateway.controller.qysh.pojo.HCDE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HCDERepository extends JpaRepository<HCDE,Long>, JpaSpecificationExecutor<HCDE> {

    @Query(value = "select  hc from HCDE hc where hc.status =null ")
    public List<HCDE> getAllBus();

}
