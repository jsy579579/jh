package com.jh.notice.repository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.notice.pojo.SMSRecord;

@Repository
public interface SmsRepository extends  PagingAndSortingRepository<SMSRecord, String>{

	
	
	@Query("select smsRecord from  SMSRecord smsRecord where smsRecord.phone=:phone ")
	Page<SMSRecord> findSmsRecord(@Param("phone") String phone,Pageable pageAble);
	
	
	@Query(value = "select smsRecord.* from  t_sms_record smsRecord where smsRecord.phone=:phone order by smsRecord.create_time desc limit 0,1", nativeQuery = true)
	SMSRecord findLastestSmsRecord(@Param("phone") String phone);

	@Query("select smsRecord from  SMSRecord smsRecord where smsRecord.phone=:phone and smsRecord.createTime >= :startTime")
	Page<SMSRecord> findSmsRecordByPhoneStartTime(@Param("phone") String phone,@Param("startTime") Date startTime, Pageable pageAble);
	
	@Query("select smsRecord from  SMSRecord smsRecord where smsRecord.phone=:phone and smsRecord.createTime >= :startTime  and smsRecord.createTime < :endTime")
	Page<SMSRecord> findSmsRecordByPhoneStartEndTime(@Param("phone") String phone, @Param("startTime") Date startTime, @Param("endTime") Date endTime, Pageable pageAble);
	
	@Query("select smsRecord from  SMSRecord smsRecord where smsRecord.createTime >= :startTime")
	Page<SMSRecord> findSmsRecordByStartTime(@Param("startTime") Date startTime, Pageable pageAble);
	
	@Query("select smsRecord from  SMSRecord smsRecord where smsRecord.createTime >= :startTime  and smsRecord.createTime < :endTime")
	Page<SMSRecord> findSmsRecordByStartEndTime(@Param("startTime") Date startTime, @Param("endTime") Date endTime, Pageable pageAble);

	@Query("select count(*) from SMSRecord smsRecord where smsRecord.ipAddress like %:ipAddress% and smsRecord.createDate =:createDate")
	int findCountByIpAddress(@Param("ipAddress")String ipAddress,@Param("createDate")String createDate);

	@Query("select count(*) from SMSRecord smsRecord where smsRecord.phone =:phone and smsRecord.createDate =:createDate")
	int findCountByPhoneAndDate(@Param("phone")String phone,@Param("createDate")String createDate);
}
