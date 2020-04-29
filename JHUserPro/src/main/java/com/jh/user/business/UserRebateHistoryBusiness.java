package com.jh.user.business;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jh.user.pojo.UserRebateHistory;

public interface UserRebateHistoryBusiness {

	public UserRebateHistory  saveUserRebateHistory(UserRebateHistory  userRebateHistory);
	
	public Page<UserRebateHistory> queryUserRebateHistoryByUserid(long userid,Pageable pageAble);
	
	public BigDecimal findsumRebateHistoryByUseridAnd( long userid,String[]  orderType, Date startTime,  Date endTime);

	public List<Object[]> findSumRebateHistoryByUserIdBetweenWeek(long userId, String[] orderType, String startTime,String endTime);

	public List<Object[]> findSumByCreateDate(String startDate,String endDate,int limit1, int limit2,String brandId);
	
	public UserRebateHistory  findUserRebateHistory(long userId,String orderType,BigDecimal  rebate,String addOrSub, String ordercode);

	public UserRebateHistory createOne(long userId, String addorsub, BigDecimal rebate, BigDecimal curRebate, String orderType,String orderCode);


    List<UserRebateHistory> findUserRebateHistoryByDate(String dNow,Long brandId);
}
