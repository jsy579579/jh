package com.jh.user.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.SignDetail;

@Repository
public interface SignDetailRepository  extends JpaRepository<SignDetail,String>,JpaSpecificationExecutor<SignDetail>{

	
	@Query("select sd from SignDetail sd where sd.userId=:userId and sd.signDate=:signDate")
	SignDetail  getSignDetailByUserIdAndDate(@Param("userId")String userId, @Param("signDate")String signDate);
	
	@Query("select sd from SignDetail sd where sd.userId=:userId and sd.signDate>=:startTime and sd.signDate<=:endTime")
	List<SignDetail>  getSignDetailByUserIdAndStartTimeAndEndTime(@Param("userId")String userId, @Param("startTime")String startTime, @Param("endTime")String endTime);
	
	@Query(value = "select sd.sign_date from t_sign_detail sd where sd.user_id=:userId and sd.create_time>=:startTime and sd.create_time<=:endTime", nativeQuery = true)
	List<String>  getSignDateByUserIdAndStartTimeAndEndTime(@Param("userId")String userId, @Param("startTime")String startTime, @Param("endTime")String endTime);

	
}
