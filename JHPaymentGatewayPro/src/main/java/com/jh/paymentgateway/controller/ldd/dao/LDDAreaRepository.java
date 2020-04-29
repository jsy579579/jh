package com.jh.paymentgateway.controller.ldd.dao;

import com.jh.paymentgateway.controller.ldd.pojo.LDDArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LDDAreaRepository extends JpaRepository<LDDArea, Long>, JpaSpecificationExecutor<LDDArea> {

    @Query("select l from LDDArea l group by l.province")
    List<LDDArea> queryAll();

    @Query("select l from LDDArea l where l.city=:city")
    List<LDDArea> getAllByCity(@Param("city") String city);

    @Query("select l from LDDArea l where l.province=:province group by city")
    List<LDDArea> getAllByProvince(@Param("province") String province);
}
