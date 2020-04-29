package com.jh.user.business;

import java.util.Date;
import java.util.List;

import com.jh.user.pojo.UserRealtion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jh.user.pojo.UserAgentChange;

public interface UserRelationBusiness {

	
	public Page<UserAgentChange>  findUserAgentChangeByUserid(long userid, Pageable pageAble);
		
	public Page<UserAgentChange>  findUserAgentChange(Pageable pageAble);
	
	
	public Page<UserAgentChange>  findUserAgentChange(Date startTime, Pageable pageAble);
	
	public Page<UserAgentChange>  findUserAgentChange(Date startTime,  Date endTime, Pageable pageAble);
	
	
	public UserAgentChange  saveUserAgentChange(UserAgentChange userAgentChange);

	public List<Long> findUserAgentChangeByTimeAndPhone(String startTime, String endTime, String phone);
	
	public List<Long> findUserAgentChangeByTimeAndPhoneAndLevel(String startTime, String endTime, String phone, String level);

	Long[] findByCount(Long countt, Long firstUser,int grade);

	Long[] findByCounts(Long countt, Long firstUser, int[] grade);

    Long[] queryFansByPreUserIdAndLevelAndCreateTime(long userId, int level, String todayTime);

    Long[] queryFansByPreUserIdAndLevel(long userId, int level);

    Long[] queryAllByPreUserIdAndCreateTime(long userId, String todayTime);

    Long[] findByPreUserId(long userId);
}
