package com.jh.paymentgateway.repository;

import com.jh.paymentgateway.pojo.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AreaRepository extends JpaRepository<Area,Integer>, JpaSpecificationExecutor<Area> {

    @Query("select area from Area area where area.areaParentId = :areaParentId")
    List<Area> findByAreaParentId(@Param("areaParentId") int areaParentId);
}
