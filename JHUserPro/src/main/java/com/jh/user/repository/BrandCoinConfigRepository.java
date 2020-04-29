package com.jh.user.repository;

import com.jh.user.pojo.BrandCoinConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BrandCoinConfigRepository extends JpaRepository<BrandCoinConfig,Long>, JpaSpecificationExecutor<BrandCoinConfig> {

    @Query("select b from BrandCoinConfig b where b.brandId=:brandId and b.grade=:grade and b.status=:status")
    BrandCoinConfig findByBrandIdAndGradeAndStatus(@Param("brandId") Long brandId,@Param("grade") int grade, @Param("status") int status);

    @Query("select b from BrandCoinConfig b where b.brandId=:brandId ")
    List<BrandCoinConfig> findByBrand(@Param("brandId") Long brandId);

    @Query("select b from BrandCoinConfig b where b.brandId=:brandId and b.grade=:grade ")
   BrandCoinConfig findByBrandAndGradeAndratio(@Param("brandId")Long brandId, @Param("grade") int grade);
}
