package com.jh.paymentgateway.repository.apdh;

import com.jh.paymentgateway.pojo.apdh.APDHIps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface APDHIpsRepository extends JpaRepository<APDHIps, Long>, JpaSpecificationExecutor<APDHIps> {

    @Query("select a from APDHIps a where a.city like %:s%")
    public List<APDHIps> findIpsByCity(@Param("s") String s);

    @Query("select a from APDHIps a where a.province like %:province%")
    List<APDHIps> findIpsByProvince(@Param("province") String province);
}
