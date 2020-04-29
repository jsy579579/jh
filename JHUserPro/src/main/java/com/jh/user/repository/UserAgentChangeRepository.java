package com.jh.user.repository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.UserAgentChange;

@Repository
public interface UserAgentChangeRepository extends JpaRepository<UserAgentChange,String>,JpaSpecificationExecutor<UserAgentChange>{

	
	
	
	@Query("select agentChange from  UserAgentChange agentChange where agentChange.userId=:userid")
	Page<UserAgentChange> findUserAgentChangeByUserid(@Param("userid") long userid, Pageable pageAble);
	
	
	@Query("select agentChange from  UserAgentChange agentChange where agentChange.createTime >=:startTime")
	Page<UserAgentChange> findUserAgentChangeByStartTime(@Param("startTime") Date startTime, Pageable pageAble);
	
	
	@Query("select agentChange from  UserAgentChange agentChange where agentChange.createTime >=:startTime and agentChange.createTime <:endTime")
	Page<UserAgentChange> findUserAgentChangeByStartEndTime(@Param("startTime") Date startTime, @Param("endTime") Date endTime, Pageable pageAble);
}
