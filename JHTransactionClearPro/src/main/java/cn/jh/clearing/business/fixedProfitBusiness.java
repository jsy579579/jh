package cn.jh.clearing.business;

import java.util.List;

import cn.jh.clearing.pojo.fixedProfit;

public interface fixedProfitBusiness {

	public fixedProfit getfixedProfitByBrandIdAndGrade(String brandId, long grade);
	
	public List<fixedProfit> getfixedProfitByBrandId(String brandId);
	
	public void createfixedProfit(fixedProfit fixedProfit);
	
	public void deletefixedProfit(fixedProfit fixedProfit);
	
	public fixedProfit getfixedProfitByBrandIdAndId(String brandId, long id);
	
}
