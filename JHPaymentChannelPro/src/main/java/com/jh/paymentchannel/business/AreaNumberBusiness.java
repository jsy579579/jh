package com.jh.paymentchannel.business;

import java.util.List;

import com.jh.paymentchannel.pojo.AreaNumber;

public interface AreaNumberBusiness {
	
	public List<AreaNumber> queryAreaNumberByAll(String province, String city, String area);
	
}
