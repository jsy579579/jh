package com.jh.notice.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.jh.notice.pojo.SMSRoute;

@Repository
public interface SmsRouteRepository extends  PagingAndSortingRepository<SMSRoute, String>{

	@Query("select smsRoute from  SMSRoute smsRoute where smsRoute.activeStatus='1' ")
	public SMSRoute	getCurActiveSmsChannel();
	
}
