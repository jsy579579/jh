package com.jh.user.business.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import com.jh.user.pojo.User;
import com.jh.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.user.business.UserLoginRegisterBusiness;
import com.jh.user.business.UserRebateHistoryBusiness;
import com.jh.user.pojo.UserRebateHistory;
import com.jh.user.repository.UserRebateHistoryRepository;

@Service
public class UserRebateHistoryBusinessImpl implements UserRebateHistoryBusiness{

	@Autowired
	private UserRebateHistoryRepository  userRebateHistoryRepository;
	
	@Autowired
	private UserLoginRegisterBusiness  userLoginRegisterBusiness;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private EntityManager em;
	
	@Override
	@Transactional
	public UserRebateHistory saveUserRebateHistory(
			UserRebateHistory userRebateHistory) {
		UserRebateHistory result = userRebateHistoryRepository.save(userRebateHistory);
		em.flush();
		return result;
	}
	
	@Override
	public Page<UserRebateHistory> queryUserRebateHistoryByUserid(long userid,
			Pageable pageAble) {
		return userRebateHistoryRepository.findRebateHistoryByUserid(userid, pageAble);
	}

	@Override
	public BigDecimal findsumRebateHistoryByUseridAnd( long userid,String[]  orderType, Date startTime,  Date endTime){
		
		BigDecimal sumRebate= userRebateHistoryRepository.findsumRebateHistoryByUseridAndTime(userid, orderType, startTime, endTime);
		
		return sumRebate;
		
	}


	@Override
	public List<Object[]> findSumRebateHistoryByUserIdBetweenWeek(long userId, String[] orderType, String startTime,String endTime) {
		return userRebateHistoryRepository.findSumRebateHistoryByUserIdBetweenWeek(userId,orderType,startTime);
	}


	@Override
	public List<Object[]> findSumByCreateDate(String startDate,String endDate,int limit1, int limit2,String brandId) {
		return userRebateHistoryRepository.findSumByCreateDate(startDate,endDate,limit1,limit2,brandId);
	}


	@Override
	public UserRebateHistory findUserRebateHistory(long userId, String orderType, BigDecimal rebate, String addOrSub,String ordercode) {
		List<UserRebateHistory> userRebateHistoryList=userRebateHistoryRepository.findUserRebateHistory(userId,orderType,rebate,addOrSub,ordercode);
		UserRebateHistory userRebateHistory=null;
		if(userRebateHistoryList!=null&&userRebateHistoryList.size()>0) {
			userRebateHistory=userRebateHistoryList.get(0);
		}
		return userRebateHistory;
	}

	@Override
	@Transactional
	public UserRebateHistory createOne(long userId, String addorsub, BigDecimal rebate, BigDecimal curRebate,String orderType, String orderCode) {
		UserRebateHistory  rebateHistory = new  UserRebateHistory();
		/*UserBalanceInfo rebateInfo = userRebateInfoBusiness.findByUserId(userId+"");
		if (rebateInfo == null) {
			User user = userLoginRegisterBusiness.queryUserById(userId);
			
		}
		
		if ("0".equals(addorsub)) {

			
		}*/
		rebateHistory.setAddOrSub(addorsub);
		rebateHistory.setRebate(rebate);
		rebateHistory.setCreateTime(new Date());
		rebateHistory.setCurRebate(curRebate);
		rebateHistory.setOrderType(orderType);
		rebateHistory.setOrderCode(orderCode);
		rebateHistory.setUserId(userId);
		return this.saveUserRebateHistory(rebateHistory);
	}

	@Override
	public List<UserRebateHistory> findUserRebateHistoryByDate(String createDate,Long brandId) {
		List<User> users=userRepository.findUserByBrandId(brandId);
		Long[] userIds=new Long[users.size()];
		for(int i=0;i<users.size();i++){
			userIds[i]=users.get(i).getId();
		}
		return userRebateHistoryRepository.findUserRebateHistoryByDate(createDate,userIds);
	}


}
