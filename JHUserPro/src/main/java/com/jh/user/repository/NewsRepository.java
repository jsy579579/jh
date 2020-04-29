package com.jh.user.repository;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.News;

@Repository
public interface NewsRepository  extends JpaRepository<News,String>,JpaSpecificationExecutor<News>{

	
	
	@Query("select n from News n where n.brandId=:brandId")
	List<News>  getNewsByBrandId(@Param("brandId")String brandId);
	
	@Query("select n from  News n where n.brandId=:brandId")
	Page<News> getNewsByBrandIdAndPage(@Param("brandId")String brandId, Pageable pageAble);
	
	@Query("select n from News n where n.brandId=:brandId and n.classifiCation=:classifiCation")
	Page<News>  getNewsByBrandIdAndClassifiCation(@Param("brandId")String brandId, @Param("classifiCation")String classifiCation, Pageable pageAble);
	
	@Query("select n from News n where n.brandId=:brandId and n.classifiCation=:classifiCation")
	List<News>  getNewsByBrandIdAndClassifiCation(@Param("brandId")String brandId, @Param("classifiCation")String classifiCation);
	
	@Query("select n from News n where n.brandId=:brandId and n.classifiCation=:classifiCation and n.title like :title")
	Page<News>  getNewsByBrandIdAndClassifiCationAndTitleAndPage(@Param("brandId")String brandId, @Param("classifiCation")String classifiCation, @Param("title")String title, Pageable pageAble);

	@Query("select n from News n where n.brandId=:brandId and n.title like :title")
	Page<News>  getNewsByBrandIdAndTitleAndPage(@Param("brandId")String brandId, @Param("title")String title, Pageable pageAble);

	@Query("select n from News n where n.brandId=:brandId and n.id in (:id)")
	List<News>  getNewsByBrandIdAndId(@Param("brandId")String brandId, @Param("id")long[] id);
	
	@Query("select n from News n where n.brandId=:brandId and n.id=:id")
	News  getNewsByBrandIdAndId(@Param("brandId")String brandId, @Param("id")long id);
	
	
	
}
