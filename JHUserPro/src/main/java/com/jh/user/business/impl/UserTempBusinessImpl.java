package com.jh.user.business.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jh.user.business.UserTempBusiness;
import com.jh.user.pojo.UserTemp;
import com.jh.user.repository.UserTempRepository;

@Service
public class UserTempBusinessImpl implements UserTempBusiness {
	
	@Autowired
	private UserTempRepository userTempRepository;

	@Override
	public List<UserTemp> findAll(Pageable pageable) {
		Page<UserTemp> pageUserTemp = userTempRepository.findAll(pageable);
		if(pageUserTemp.getSize() > 0){
			return new ArrayList<UserTemp>(pageUserTemp.getContent());
		}else{
			return null;
		}
	}

}
