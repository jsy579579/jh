package com.jh.paymentgateway.controller.tldh.Repository;


import com.jh.paymentgateway.controller.tldh.pojo.TLAree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TLAreeRepository  extends JpaRepository<TLAree,Long>, JpaSpecificationExecutor<TLAree> {

    @Query("select distinct tl.province from TLAree tl ")
    List<TLAree> getAllProvince();

    @Query("select distinct tl.city from TLAree tl where tl.province=:province")
    List<TLAree> getCityByProvince(@Param("province") String province);

    @Query("select distinct tl from TLAree tl where tl.city=:city")
    List<TLAree> getCityCodeByCity(@Param("city") String city);
}
