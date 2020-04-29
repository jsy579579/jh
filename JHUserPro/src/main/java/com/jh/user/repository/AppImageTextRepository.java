package com.jh.user.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.AppImageText;
import com.jh.user.pojo.AppSlideshow;
import com.jh.user.pojo.UserShops;

@Repository
public interface AppImageTextRepository  extends JpaRepository<AppImageText,String>,JpaSpecificationExecutor<AppImageText>{

	@Query(" from  AppImageText")
	List<AppImageText>  findAppImageText();
	
	@Query(" from  AppImageText appImageText where appImageText.id=:id ")
	AppImageText findAppImageTextById(@Param("id")long id);
	
	@Query(" from  AppImageText appImageText where appImageText.brandId=:brandId  order by appImageText.id desc")
	List<AppImageText> findAppImageTextBybrandId(@Param("brandId")long brandId);
	
	@Modifying
	@Query("delete from AppImageText appImageText  where appImageText.id=:id")
	void delAppImageTextByById(@Param("id")long id);
}
