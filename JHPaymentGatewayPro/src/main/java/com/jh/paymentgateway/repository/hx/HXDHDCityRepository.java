package com.jh.paymentgateway.repository.hx;


import com.jh.paymentgateway.pojo.hxdhd.HXDCity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HXDHDCityRepository extends JpaRepository<HXDCity,Integer>, JpaSpecificationExecutor<HXDCity> {

    @Query("select city from HXDCity city where city.pid = :pid")
    List<HXDCity> findByPid(@Param("pid") int id);
}
