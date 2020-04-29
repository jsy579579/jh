package com.jh.paymentgateway.repository.apdh;

import com.jh.paymentgateway.pojo.apdh.APDHCityCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface APAddressRepository extends JpaRepository<APDHCityCode, Long>, JpaSpecificationExecutor<APDHCityCode> {
    @Query("select bq from APDHCityCode bq where bq.cityCode like '%0000' ")
    public List<APDHCityCode> findByCityCodeLike0();

    @Query("select bq from APDHCityCode bq where bq.cityCode like :cityCode% group by bq.cityCode")
    public List<APDHCityCode> findByCityCodeLike1(@Param("cityCode") String cityCode);
}
