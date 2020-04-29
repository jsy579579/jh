package cn.jh.clearing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import cn.jh.clearing.pojo.AbroadRatio;

@Repository
public interface AbroadRatioRepository extends JpaRepository<AbroadRatio,String>,JpaSpecificationExecutor<AbroadRatio>{

	public AbroadRatio getAbroadRatioByBrandIdAndGrade(int brandId, int grade);
	
}
