package com.jh.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.ThirdLevelDistribution;

@Repository
public interface ThirdLevelDistributionRepository extends JpaRepository<ThirdLevelDistribution,String>,JpaSpecificationExecutor<ThirdLevelDistribution>{

	
	@Query("select thirdlevel from  ThirdLevelDistribution thirdlevel where thirdlevel.brandId = :brandid and thirdlevel.status = '0' order by  thirdlevel.grade  desc")
	List<ThirdLevelDistribution> findAllThirdLevel(@Param("brandid") long brandid);
		
	@Query("select thirdlevel from  ThirdLevelDistribution thirdlevel where thirdlevel.brandId = :brandid and thirdlevel.grade = :grade and thirdlevel.status = '0'")
	ThirdLevelDistribution findAllThirdLevelByBrandidandlevelStatus(@Param("brandid") long brandid, @Param("grade") int grade);
	
	@Query("select thirdlevel from  ThirdLevelDistribution thirdlevel where thirdlevel.brandId = :brandid and thirdlevel.grade = :grade")
	ThirdLevelDistribution findAllThirdLevelByBrandidandlevel(@Param("brandid") long brandid, @Param("grade") int grade);
	
	@Query("select thirdlevel from  ThirdLevelDistribution thirdlevel where thirdlevel.id =:id and thirdlevel.status = '0'")
	ThirdLevelDistribution findAllThirdLevelByid(@Param("id") long id);
	
	@Modifying
	@Query("update ThirdLevelDistribution set  status='1' where id=:id")
	void delThirdLevelByid(@Param("id") long id);
	
	//根据brandid查询最高等级
	@Query("select max(thirdlevel.grade) from ThirdLevelDistribution thirdlevel where thirdlevel.brandId = :brandid")
	int findThirdLevelDistributionByBrandid(@Param("brandid") long brandid);
	
	//根据grade删除
	@Modifying
	@Query("delete from ThirdLevelDistribution thirdlevel where thirdlevel.grade = :grade and thirdlevel.brandId = :brandid")
	void deleteThirdLevelDistributionByGrade(@Param("grade") int grade,@Param("brandid") long brandid);

}
