package com.jh.notice.repository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.notice.pojo.SMSInform;

@Repository
public interface SmsInformRepository extends  PagingAndSortingRepository<SMSInform, String>{

	
	
	@Query("select SMSInform from  SMSInform SMSInform where SMSInform.phone=:phone ")
	Page<SMSInform> findSMSInform(@Param("phone") String phone,Pageable pageAble);
	
	
	@Query(value = "select SMSInform.* from  t_sms_record SMSInform where SMSInform.phone=:phone order by SMSInform.create_time desc limit 0,1", nativeQuery = true)
	SMSInform findLastestSMSInform(@Param("phone") String phone);

	@Query("select SMSInform from  SMSInform SMSInform where SMSInform.phone=:phone and SMSInform.createTime >= :startTime")
	Page<SMSInform> findSMSInformByPhoneStartTime(@Param("phone") String phone,@Param("startTime") Date startTime, Pageable pageAble);
	
	@Query("select SMSInform from  SMSInform SMSInform where SMSInform.phone=:phone and SMSInform.createTime >= :startTime  and SMSInform.createTime < :endTime")
	Page<SMSInform> findSMSInformByPhoneStartEndTime(@Param("phone") String phone, @Param("startTime") Date startTime, @Param("endTime") Date endTime, Pageable pageAble);
	
	@Query("select SMSInform from  SMSInform SMSInform where SMSInform.createTime >= :startTime")
	Page<SMSInform> findSMSInformByStartTime(@Param("startTime") Date startTime, Pageable pageAble);
	
	@Query("select SMSInform from  SMSInform SMSInform where SMSInform.createTime >= :startTime  and SMSInform.createTime < :endTime")
	Page<SMSInform> findSMSInformByStartEndTime(@Param("startTime") Date startTime, @Param("endTime") Date endTime, Pageable pageAble);

	@Query("select count(*) from SMSInform SMSInform where SMSInform.ipAddress like %:ipAddress% and SMSInform.createDate =:createDate")
	int findCountByIpAddress(@Param("ipAddress")String ipAddress,@Param("createDate")String createDate);

	@Query("select count(*) from SMSInform SMSInform where SMSInform.phone =:phone and SMSInform.createDate =:createDate")
	int findCountByPhoneAndDate(@Param("phone")String phone,@Param("createDate")String createDate);
}
