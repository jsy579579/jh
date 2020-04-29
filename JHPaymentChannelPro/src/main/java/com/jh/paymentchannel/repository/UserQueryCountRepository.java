package com.jh.paymentchannel.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.UserQueryCount;

@Repository
public interface UserQueryCountRepository  extends JpaRepository<UserQueryCount,String>,JpaSpecificationExecutor<UserQueryCount>{

	
	
	@Query("select u from UserQueryCount u where u.userId=:userId")
	UserQueryCount  getUserQueryCountByUserId(@Param("userId")String userId);
	
	
}
