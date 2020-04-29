package cn.jh.clearing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.jh.clearing.pojo.fixedProfit;

@Repository
public interface fixedProfitRepository extends JpaRepository<fixedProfit,String>,JpaSpecificationExecutor<fixedProfit>{

	
	@Query("select fp from  fixedProfit fp where fp.brandId=:brandId and fp.grade=:grade")
	public fixedProfit getfixedProfitByBrandIdAndGrade(@Param("brandId") String brandId, @Param("grade") long grade);

	@Query("select fp from  fixedProfit fp where fp.brandId=:brandId")
	public List<fixedProfit> getfixedProfitByBrandId(@Param("brandId") String brandId);
	
	@Query("select fp from  fixedProfit fp where fp.brandId = :brandId and fp.id = :id")
	public fixedProfit getfixedProfitByBrandIdAndId(@Param("brandId") String brandId, @Param("id") long id);
	
}

