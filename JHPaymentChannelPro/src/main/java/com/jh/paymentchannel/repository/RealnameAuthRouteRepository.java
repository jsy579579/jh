package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.RealNameAuthRoute;

@Repository
public interface RealnameAuthRouteRepository extends JpaRepository<RealNameAuthRoute,String>,JpaSpecificationExecutor<RealNameAuthRoute>{ 

	@Query("select realNameAuthRoute from  RealNameAuthRoute realNameAuthRoute where realNameAuthRoute.activeStatus='1' ")
	public RealNameAuthRoute	getCurActiveRealNameAuthChannel();
	
	
	
}
