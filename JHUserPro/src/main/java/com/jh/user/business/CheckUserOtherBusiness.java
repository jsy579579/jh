package com.jh.user.business;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jh.user.pojo.CheckUserOther;

public interface CheckUserOtherBusiness {
	
	public CheckUserOther saveCheckUserOther(CheckUserOther checkUser);
	
	public Page<CheckUserOther> queryUserById(Pageable pageAble);
	


}
