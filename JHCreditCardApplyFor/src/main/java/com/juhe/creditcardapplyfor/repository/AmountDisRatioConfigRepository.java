package com.juhe.creditcardapplyfor.repository;

import com.juhe.creditcardapplyfor.entity.AmountDisRatioConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AmountDisRatioConfigRepository extends JpaRepository<AmountDisRatioConfig,Integer>, JpaSpecificationExecutor<AmountDisRatioConfig> {

    @Query("select a from AmountDisRatioConfig a where a.brandId=:brandId")
    AmountDisRatioConfig queryConfigByBrandId(@Param("brandId") String brandId);
}
