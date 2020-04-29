package com.jh.user.repository;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.UserBalanceHistory;

@Repository
public interface UserBalanceRepository extends JpaRepository<UserBalanceHistory,String>,JpaSpecificationExecutor<UserBalanceHistory>{

	
	
	@Query("select balHistory from  UserBalanceHistory balHistory where balHistory.userId=:userid ")
	Page<UserBalanceHistory> findBalHistoryByUserid(@Param("userid") long userid,Pageable pageAble);
	
	@Query("select Sum(balHistory.amount)  from  UserBalanceHistory balHistory where balHistory.userId=:userid and balHistory.createTime>:startTime and balHistory.addOrSub='0'")
	BigDecimal findSumUserBalByUserIdstats0(@Param("userid") long userid , @Param("startTime") Date startTime);
	
	@Query("select Sum(balHistory.amount)  from  UserBalanceHistory balHistory where balHistory.userId=:userid and balHistory.createTime>:startTime and balHistory.addOrSub='1'")
	BigDecimal findSumUserBalByUserIdstats1(@Param("userid") long userid , @Param("startTime") Date startTime);
	
	@Query("select Sum(balHistory.amount)  from  UserBalanceHistory balHistory where balHistory.userId=:userid and balHistory.addOrSub='0' ")
	BigDecimal findSumUserBalByUserIdstats0(@Param("userid") long userid );
	
	@Query("select Sum(balHistory.amount)  from  UserBalanceHistory balHistory where balHistory.userId=:userid and balHistory.addOrSub='1'")
	BigDecimal findSumUserBalByUserIdstats1(@Param("userid") long userid );
	
	//防止判断该笔订单是否已经充值过
	@Query("select balHistory from  UserBalanceHistory balHistory where balHistory.userId=:userid and balHistory.addOrSub=:addOrSub and balHistory.orderCode=:orderCode")
	UserBalanceHistory findUserBalByUidAndorsubAndOrCode(@Param("userid") long userid , @Param("orderCode") String orderCode ,@Param("addOrSub") String addOrSub );
	
	//注销用户余额历史记录
	@Modifying
	@Query("delete from UserBalanceHistory balHistory where balHistory.userId=:userid")
	void delUserBalanceHistoryByUserid(@Param("userid") long userid);
}
