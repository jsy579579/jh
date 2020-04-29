package com.jh.user.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.UserShops;

@Repository
public interface UserShopsRepository  extends JpaRepository<UserShops,String>,JpaSpecificationExecutor<UserShops>{

	
	
	@Query(" from  UserShops")
	List<UserShops>  findUserShops();
	
	@Query(" from  UserShops usershops where usershops.id=:id ")
	UserShops findUserShopsById(@Param("id")long id);
	
	
	@Query(" from  UserShops usershops where usershops.userId=:userId ")
	UserShops findUserShopsByUid(@Param("userId")long userid);

	@Query("update UserShops up  set up.status=:status where up.userId=:userId")
	UserShops updateUserShopsByUid(@Param("userId")long userid,@Param("status") String status);
	
	//获取shops表中随机的userid
	@Query("select usershops.userId from UserShops usershops where usershops.status='1'")
	String[] queryRandomUseridByAll();
	
	//注销用户商铺记录
	@Modifying
	@Query("delete from UserShops usershops where usershops.userId=:userid")
	void delUserShopsByUserid(@Param("userid") long userid);
	
}
