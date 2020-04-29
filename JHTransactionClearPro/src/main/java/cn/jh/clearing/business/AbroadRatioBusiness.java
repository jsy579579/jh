package cn.jh.clearing.business;

import cn.jh.clearing.pojo.AbroadRatio;

public interface AbroadRatioBusiness {

	public AbroadRatio getAbroadRatioByBrandIdAndGrade(int brandId, int grade);
	
	public void createAbroadRatio(AbroadRatio abroadRatio);
	
}
