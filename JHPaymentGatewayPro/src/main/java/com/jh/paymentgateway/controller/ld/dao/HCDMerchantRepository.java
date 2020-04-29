package com.jh.paymentgateway.controller.ld.dao;

import com.jh.paymentgateway.controller.ld.pojo.HCDMerchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;

@Repository
public interface HCDMerchantRepository extends JpaRepository<HCDMerchant, Long>, JpaSpecificationExecutor<HCDMerchant> {

    @Query(nativeQuery=true,value="select * from t_international_company h where h.province is null limit 0,10000")
    List<HCDMerchant> queryAll();

    @Query(nativeQuery=true,value="select * from t_international_company h ")
    List<HCDMerchant> queryProvince();
}
