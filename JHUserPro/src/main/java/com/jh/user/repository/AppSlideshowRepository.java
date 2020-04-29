package com.jh.user.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.AppSlideshow;
import com.jh.user.pojo.UserShops;

@Repository
public interface AppSlideshowRepository  extends JpaRepository<AppSlideshow,String>,JpaSpecificationExecutor<AppSlideshow>{

	@Query(" from  AppSlideshow")
	List<AppSlideshow>  findAppSlideshow();
	
	@Query(" from  AppSlideshow appSlideshow where appSlideshow.id=:id ")
	AppSlideshow findAppSlideshowById(@Param("id")long id);
	
	@Query(" from  AppSlideshow appSlideshow where appSlideshow.brandId=:brandId ")
	List<AppSlideshow> findAppSlideshowBybrandId(@Param("brandId")long brandId);
	
	@Modifying
	@Query("delete from AppSlideshow appSlideshow  where appSlideshow.id=:id")
	void delAppSlideshowById(@Param("id")long id);
	
}
