package com.jh.paymentchannel.business;

import java.util.List;

import com.jh.paymentchannel.pojo.RegisterAuth;

public interface RegisterAuthBusiness {
	
	//保存数据
	public RegisterAuth saveAuth(RegisterAuth registerAuth);
	
	//查询数据
	public RegisterAuth queryByMobile(String mobile);
	
	//修改数据
	public RegisterAuth updateAuth(RegisterAuth registerAuth);
	
	/**
	 * 根据userId查询
	 * @param userId
	 * @return HLRegister
	 */
//	public HLRegister findHLRegisterByUserId(String userId);
	
	/**
	 * 保存HLRegister
	 * @param model
	 * @return
	 */
	

	public RegisterAuth getRegisterAuthByIdCard(String idCard);
	
}
