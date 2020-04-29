package com.jh.notice.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.notice.pojo.HttpNotify;
import com.jh.notice.pojo.SMSRecord;

@Repository
public interface  HttpNotifyRepository extends  PagingAndSortingRepository<HttpNotify, String>{

	
	@Query("select httpNotify from  HttpNotify httpNotify where httpNotify.status='1' and httpNotify.remainCnt > 0 and httpNotify.nextCallTime <= :curDate ")
	List<HttpNotify> findHttpNotifys(@Param("curDate") Date curDate);
	
	@Query("select httpNotify from  HttpNotify httpNotify where httpNotify.status=:status ")
	Page<HttpNotify> findHttpNotifyByStatus(@Param("status") String status,Pageable pageAble);
	
	@Query("select httpNotify from  HttpNotify httpNotify where httpNotify.status=:status and httpNotify.createTime >= :startTime")
	Page<HttpNotify> findHttpNotifyByStatusStartTime(@Param("status") String status,@Param("startTime") Date startTime, Pageable pageAble);
	
	@Query("select httpNotify from  HttpNotify httpNotify where httpNotify.status=:status and httpNotify.createTime >= :startTime and httpNotify.createTime < :endTime")
	Page<HttpNotify> findHttpNotifyByStatusStartEndTime(@Param("status") String status, @Param("startTime") Date startTime, @Param("endTime") Date endTime, Pageable pageAble);
	
	
	@Query("select httpNotify from  HttpNotify httpNotify where  httpNotify.createTime >= :startTime")
	Page<HttpNotify> findHttpNotifyByStartTime(@Param("startTime") Date startTime, Pageable pageAble);
	
	
	@Query("select httpNotify from  HttpNotify httpNotify where  httpNotify.createTime >= :startTime and httpNotify.createTime < :endTime")
	Page<HttpNotify> findHttpNotifyByStartEndTime(@Param("startTime") Date startTime, @Param("endTime") Date endTime, Pageable pageAble);
	
}
