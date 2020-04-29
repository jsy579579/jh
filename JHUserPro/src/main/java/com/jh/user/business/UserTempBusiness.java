package com.jh.user.business;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.jh.user.pojo.UserTemp;

public interface UserTempBusiness {

	List<UserTemp> findAll(Pageable pageable);

}
