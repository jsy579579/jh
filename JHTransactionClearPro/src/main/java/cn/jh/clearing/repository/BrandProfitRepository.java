package cn.jh.clearing.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.jh.clearing.pojo.BrandProfit;

@Repository
public interface BrandProfitRepository extends JpaRepository<BrandProfit, Long>,JpaSpecificationExecutor<BrandProfit>{

	@Query("select bp from BrandProfit bp where bp.brandId=:brandId")
	List<BrandProfit> getBrandProfitByBrandId(@Param("brandId") long brandId);

	@Query("select bp from BrandProfit bp where bp.tradeTime=:tradeTime")
	Page<BrandProfit> getBrandProfitByTradeTime(@Param("tradeTime") String tradeTime, Pageable pageAble);
	
	@Query("select bp from BrandProfit bp where bp.brandId=:brandId and bp.tradeTime=:tradeTime")
	Page<BrandProfit> getBrandProfitByBrandIdAndTradeTime(@Param("brandId") long brandId, @Param("tradeTime") String tradeTime, Pageable pageAble);
	
	
}
