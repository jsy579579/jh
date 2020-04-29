package com.jh.user.business;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jh.user.pojo.GetMoney;

public interface GetMoneyBusiness {
	
	public GetMoney createGetMoney(GetMoney getMoney);
	
	public List<GetMoney> getGetMoneyByBrandId(String brandId);
	
	public List<GetMoney> getGetMoneyByBrandIdAndId(String brandId, long[] Id);
	
	public Page<GetMoney> getGetMoneyByBrandIdAndPage(String brandId, Pageable pageAble);
	
	public void deleteGetMoneyByBrandIdAndId(GetMoney getMoney);
	
}
