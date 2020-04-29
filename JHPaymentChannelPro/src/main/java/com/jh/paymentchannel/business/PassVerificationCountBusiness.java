package com.jh.paymentchannel.business;


import java.util.List;

import com.jh.paymentchannel.pojo.PassVerificationCount;

public interface PassVerificationCountBusiness {
	/**
	 * 保存PassVerificationCount
	 * @param model
	 * @return
	 */
	public PassVerificationCount save(PassVerificationCount model);
	
    /**根据user_id查询所有*/
    public PassVerificationCount findPassByUserId(long userId);
    
    /**根据brandid查询所有*/
    public List<PassVerificationCount> findPassByBrandid(long brandid);
}
