package com.jh.user.repository;


import com.jh.user.pojo.BrandCoinGradeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandCoinGradeConfigRepository extends JpaRepository<BrandCoinGradeConfig,Long>, JpaSpecificationExecutor<BrandCoinGradeConfig> {

    @Query("select b from BrandCoinGradeConfig b where b.brandId=:brandId and b.grade=:grade")
    BrandCoinGradeConfig findBrandGradeByGradeAndBrandId(@Param("brandId") long brandId, @Param("grade") int grade);

    @Query("select b from BrandCoinGradeConfig b where b.brandId=:brandId")
    BrandCoinGradeConfig findBrandGradeByBrandId(@Param("brandId")Long brandId);
}
