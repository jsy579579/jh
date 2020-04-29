package com.jh.user.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.NewsClassifiCation;

@Repository
public interface NewsClassifiCationRepository  extends JpaRepository<NewsClassifiCation,String>,JpaSpecificationExecutor<NewsClassifiCation>{

	@Query("select n from NewsClassifiCation n where n.brandId=:brandId")
	List<NewsClassifiCation>  getNewsClassifiCationByBrandId(@Param("brandId")String brandId);
	
	@Query("select n from NewsClassifiCation n where n.brandId=:brandId and n.classifiCation=:classifiCation")
	NewsClassifiCation  getNewsClassifiCationByBrandIdAndClassifiCation(@Param("brandId")String brandId, @Param("classifiCation")String classifiCation);

	@Query("select n from NewsClassifiCation n where n.brandId=:brandId and n.id in (:id)")
	List<NewsClassifiCation>  getNewsClassifiCationByBrandIdAndId(@Param("brandId")String brandId, @Param("id")long[] id);
	
}
