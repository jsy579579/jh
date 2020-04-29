package com.jh.paymentgateway.controller.ldx.dao;

import com.jh.paymentgateway.controller.ldx.pojo.LDXArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LDXAreaRepository extends JpaRepository<LDXArea, Long>, JpaSpecificationExecutor<LDXArea> {

    @Query("select l from LDXArea l group by l.province")
    List<LDXArea> queryAll();

    @Query("select l from LDXArea l where l.city=:city")
    List<LDXArea> getAllByCity(@Param("city") String city);

    @Query("select l from LDXArea l where l.province=:province group by city")
    List<LDXArea> getAllByProvince(@Param("province") String province);
}
