package com.jh.paymentgateway.repository.hx;

import com.jh.paymentgateway.pojo.hx.HXDHAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HXDHAddressRepository extends JpaRepository<HXDHAddress, Long>, JpaSpecificationExecutor<HXDHAddress> {

    @Query("select bq from HXDHAddress bq group by bq.province")
    public List<HXDHAddress> getHXDHAddresses();

    @Query("select bq from HXDHAddress bq where bq.province=:province")
    public List<HXDHAddress> getHXDHAddressesByProvince(@Param("province") String province);

    @Query("select a from HXDHAddress a where a.city like %:city%")
    List<HXDHAddress> getHXDHAddressbyCity(@Param("city") String city);
}
