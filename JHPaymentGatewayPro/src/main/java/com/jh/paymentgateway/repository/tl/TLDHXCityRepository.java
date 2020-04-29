package com.jh.paymentgateway.repository.tl;

import com.jh.paymentgateway.pojo.tl.TLDHXCity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TLDHXCityRepository extends JpaRepository<TLDHXCity, String>, JpaSpecificationExecutor<TLDHXCity> {
    @Query("select hxdhx from TLDHXCity hxdhx where hxdhx.pid=:pid")
    List<TLDHXCity> getTLDHXCitysByPid(@Param("pid") String pid);
}
