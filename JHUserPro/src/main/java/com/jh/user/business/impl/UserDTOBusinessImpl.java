package com.jh.user.business.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.user.pojo.UserDTO;
import com.jh.user.repository.UserDTORepository;
@Service
public class UserDTOBusinessImpl {
	@Autowired
	UserDTORepository userDTORepository;

	public List<UserDTO> findByRealNameStatus() {
		return userDTORepository.findByRealNameStatus();
	}

	public UserDTO findByphone(String phone) {
		return userDTORepository.findByphone(phone);
	}
}
