package com.jh.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.jh.user.pojo.BrandCoin;

@Repository
public interface BrandCoinRepository extends JpaRepository<BrandCoin,String>,JpaSpecificationExecutor<BrandCoin>{

	@Query("select brandCoin from  BrandCoin brandCoin where brandCoin.brandId=:brandid")
	BrandCoin findBrandCoinBybrandid(@Param("brandid") long brandid);

}
