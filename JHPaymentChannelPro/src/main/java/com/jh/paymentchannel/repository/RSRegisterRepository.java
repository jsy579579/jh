package com.jh.paymentchannel.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.RSRegister;

@Repository
public interface RSRegisterRepository extends JpaRepository<RSRegister, String>, JpaSpecificationExecutor<RSRegister>{
	
	@Query("select rs from RSRegister rs where rs.idCard=:idCard")
	public RSRegister getRSRegisterByIdCard(@Param("idCard") String idCard);
	
	@Modifying
	@Query("update RSRegister set rate=:rate,changeTime=:date where userid=:userId")
	void updateRateByIdCard(@Param("userId") String userId,@Param("rate") String rate,@Param("date") Date date);
	
	@Modifying
	@Query("update RSRegister set bankAccountNo=:cardNo,changeTime=:date where userid=:userId")
	void updateCardNoByUserId(@Param("cardNo") String cardNo,@Param("userId") String userId,@Param("date") Date date);
	
	@Modifying
	@Query("update RSRegister set bankAccountNo=:cardNo,rate=:rate,changeTime=:date where userid=:userId")
	void updateCardNoAndRateByUserId(@Param("cardNo") String cardNo,@Param("rate") String rate,@Param("userId") String userId,@Param("date") Date date);
	
	@Modifying
	@Query("update RSRegister set bankAccountNo=:cardNo,idCard=:certNo,userName=:userName,phone=:phone,changeTime=:date where userid=:userId")
	void updateCardNo(@Param("userId") String userId,@Param("cardNo") String cardNo,@Param("certNo") String certNo,@Param("userName") String userName,@Param("phone") String phone,@Param("date") Date date);
	
}
