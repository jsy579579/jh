package com.jh.paymentgateway.controller.hqk.Repository;


import com.jh.paymentgateway.controller.hqk.pojo.HQKadree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HQKadreeRepository extends JpaRepository<HQKadree,Long>, JpaSpecificationExecutor<HQKadree> {

    @Query("select DISTINCT hq.province from HQKadree hq")
    List<HQKadree> findAllAdree();

//    @Query(value = "select DISTINCT city from t_hqk_adree hq where province=:province and city !='1'",nativeQuery = true)
//    List<Object[]> findCityByProvince(@Param("province")String province);
//
    @Query(value = "select DISTINCT hq.city from HQKadree hq where hq.province=:province and hq.city !='1'")
    List<HQKadree> findCityByProvince(@Param("province")String province);

    @Query(value = "select hq from HQKadree hq where hq.city=:city and hq.city !='1'")
    HQKadree findProvinceCodeByCity(@Param("city")String city);
}
