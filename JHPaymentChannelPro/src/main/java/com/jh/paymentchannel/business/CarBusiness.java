package com.jh.paymentchannel.business;

import java.util.List;

import com.jh.paymentchannel.pojo.CarQueryHistory;
import com.jh.paymentchannel.pojo.CarSupportProvince;
import com.jh.paymentchannel.pojo.Province;
import com.jh.paymentchannel.pojo.UserQueryCount;

public interface CarBusiness {
	
	
	public CarSupportProvince getCarSupportProvinceByProvince(String province);
	
	public List<CarSupportProvince> getCarSupportProvince();
	
	public List<String> getProvince();
	
	public Province getProvinceByProcince(String province);
	
	public List<String> getCityByProvinceId(String provinceId);
	
	public UserQueryCount createUserQueryCount(UserQueryCount userQueryCount);
	
	public UserQueryCount getUserQueryCountByUserId(String userId);
	
	public CarQueryHistory createCarQueryHistory(CarQueryHistory carQueryHistory);
	
	public List<CarQueryHistory> getCarQueryHistoryByUserId(String userId);
	
	public CarQueryHistory getCarQueryHistoryByUserIdAndId(String userId, long id);
	
}
