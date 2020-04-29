package com.jh.user.repository;

import java.math.BigDecimal;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.UserRebateHistory;

@Repository
public interface UserRebateHistoryRepository extends JpaRepository<UserRebateHistory,String>,JpaSpecificationExecutor<UserRebateHistory>{

	
	@Query("select rebateHistory from  UserRebateHistory rebateHistory where rebateHistory.userId=:userid ")
	Page<UserRebateHistory> findRebateHistoryByUserid(@Param("userid") long userid,Pageable pageAble);
	
	@Query("select sum(rebateHistory.rebate) from  UserRebateHistory rebateHistory where rebateHistory.userId=:userid and rebateHistory.orderType in (:orderType) and rebateHistory.addOrSub='0' and rebateHistory.createTime>:startTime and  rebateHistory.createTime<:endTime")
	BigDecimal findsumRebateHistoryByUseridAndTime(@Param("userid") long userid,@Param("orderType") String[]  orderType, @Param("startTime") Date startTime, @Param("endTime") Date endTime);
	
	//注销用户历史记录
	@Modifying
	@Query("delete from UserRebateHistory rebateHistory where rebateHistory.userId=:userid")
	void delUserRebateHistoryByUserid(@Param("userid") long userid);

	@Query("select rebateHistory.createDate,sum(rebateHistory.rebate) from UserRebateHistory rebateHistory where rebateHistory.userId=:userId and rebateHistory.createDate >=:startDate and rebateHistory.orderType in (:orderType) group by rebateHistory.createDate ")
	List<Object[]> findSumRebateHistoryByUserIdBetweenWeek(@Param("userId")long userId,@Param("orderType")String[] orderType,@Param("startDate") String startTime);

	@Query(value="select sum(userRebateHistory.rebate),userRebateHistory.user_id from t_user_rebate_history userRebateHistory where userRebateHistory.create_date>=:startDate and userRebateHistory.create_date<=:endDate and userRebateHistory.add_or_sub='0' and userRebateHistory.user_id in (select id from t_user where brand_id=:brandId and pre_user_id != 0) group by userRebateHistory.user_id order by sum(userRebateHistory.rebate) desc limit :limit1,:limit2",nativeQuery = true)
	List<Object[]> findSumByCreateDate(@Param("startDate")String startDate,@Param("endDate")String endDate, @Param("limit1")int limit1, @Param("limit2")int limit2,@Param("brandId")String brandId);
	
	

	@Query("select rebateHistory from  UserRebateHistory rebateHistory where rebateHistory.userId=:userid and rebateHistory.orderType=:orderType and rebateHistory.rebate=:rebate and rebateHistory.addOrSub=:addOrSub and rebateHistory.orderCode=:orderCode ")
	List<UserRebateHistory> findUserRebateHistory(@Param("userid") long userId, @Param("orderType")String orderType, @Param("rebate")BigDecimal rebate,  @Param("addOrSub")String addOrSub,@Param("orderCode")String ordercode);

	@Query("select rebate from UserRebateHistory rebate where rebate.createDate=:createDate and rebate.addOrSub='0' and rebate.userId in (:userIds)")
	List<UserRebateHistory> findUserRebateHistoryByDate(@Param("createDate") String createDate,@Param("userIds")Long[] userIds);
}
