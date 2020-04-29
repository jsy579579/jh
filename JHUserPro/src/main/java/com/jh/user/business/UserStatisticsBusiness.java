package com.jh.user.business;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jh.user.pojo.User;

public interface UserStatisticsBusiness{

	
	public Page<User>  findPageUser(String phone,  String brandid,  Date startTime,  Date endTime, Pageable pageAble);
	
}
