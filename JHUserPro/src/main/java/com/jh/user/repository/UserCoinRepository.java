package com.jh.user.repository;

import java.util.ArrayList;
import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jh.user.pojo.UserCoinHistory;

public interface UserCoinRepository extends JpaRepository<UserCoinHistory,String>,JpaSpecificationExecutor<UserCoinHistory>{

	@Query("select coinHistory from  UserCoinHistory coinHistory where coinHistory.userId=:userid ")
	Page<UserCoinHistory> findCoinHistoryByUserid(@Param("userid") long userid,Pageable pageAble);
	
	//根据brandid和status获取一定时间段内的信息
	@Query("select count(*) from User user where user.realnameStatus in(:status) and user.brandId=:brandId and user.createTime>=:startTimeDate and user.createTime<=:endTimeDate")
	int findFullNameByStatus(@Param("brandId") long brandId, @Param("status") ArrayList<String> status, @Param("startTimeDate") Date startTimeDate, @Param("endTimeDate") Date endTimeDate);
	
	//根据brandid和status获取一定时间段内的信息
	@Query("select count(*) from User user where user.realnameStatus in(:status) and user.createTime>=:startTimeDate and user.createTime<=:endTimeDate")
	int findFullNameByStatus(@Param("status") ArrayList<String> status, @Param("startTimeDate") Date startTimeDate, @Param("endTimeDate") Date endTimeDate);

	//注销用户历史记录
	@Modifying
	@Query("delete from UserCoinHistory coinHistory where coinHistory.userId=:userid")
	void delUserCoinByUserid(@Param("userid") long userid);
}
