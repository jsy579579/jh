package com.jh.paymentchannel.business.impl;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.paymentchannel.business.RegisterAuthBusiness;
import com.jh.paymentchannel.pojo.RegisterAuth;
import com.jh.paymentchannel.repository.RegisterAuthRepository;

@Service
public class RegisterAuthBusinessImpl implements RegisterAuthBusiness {

	
	@Autowired
	private RegisterAuthRepository registerAuthRepository;
	
	@Autowired
	private EntityManager em;
	
	//增加数据
	@Transactional
	@Override
	public RegisterAuth saveAuth(RegisterAuth registerAuth) {
		
		RegisterAuth result = registerAuthRepository.save(registerAuth);
		em.flush();
		return result;
	}

	//查询数据
	@Override
	public RegisterAuth queryByMobile(String mobile) {
		RegisterAuth queryByRequestId = registerAuthRepository.queryByMobile(mobile);
		return queryByRequestId;
	}

	//修改数据
	@Transactional
	@Override
	public RegisterAuth updateAuth(RegisterAuth registerAuth) {
		RegisterAuth result = registerAuthRepository.save(registerAuth);
		em.flush();
		return result;
	}

//	@Override
//	public HLRegister findHLRegisterByUserId(String userId) {
//		return hlRegisterRepository.findByUserId(userId);
//	}


	@Override
	public RegisterAuth getRegisterAuthByIdCard(String idCard) {
		RegisterAuth result = registerAuthRepository.getRegisterAuthByIdCard(idCard);
		em.clear();
		return result;
	}
	

}
