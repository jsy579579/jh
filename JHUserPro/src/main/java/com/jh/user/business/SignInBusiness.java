package com.jh.user.business;

import java.util.List;

import com.jh.user.pojo.SignCalc;
import com.jh.user.pojo.SignCoin;
import com.jh.user.pojo.SignCommonCoin;
import com.jh.user.pojo.SignDetail;

public interface SignInBusiness {
	
	public SignDetail createSignDetail(SignDetail signDetail);
	
	public SignDetail getSignDetailByUserIdAndDate(String userId, String date);
	
	public List<SignDetail> getSignDetailByUserIdAndStartTimeAndEndTime(String userId, String startTime, String endTime);
	
	public List<String> getSignDateByUserIdAndStartTimeAndEndTime(String userId, String startTime, String endTime);
	
	public SignCalc createSignCalc(SignCalc signCalc);
	
	public SignCalc getSignCalcByUserId(String userId);
	
	public List<SignCalc> getSignCalcAll();
	
	public int getSignCoin(String brandId, String grade, int continueDays);
	
	public List<SignCoin> getSignCoinByBrandIdAndGrade(String brandId, String grade);
	
	public SignCoin createSignCoin(SignCoin signCoin);
	
	public SignCoin getSignCoinByBrandIdAndGradeAndContinueDays(String brandId, String grade, int continueDays);
	
	public List<SignCoin> getSignCoinByBrandId(String brandId);
	
	public List<SignCoin> getSignCoinByBrandIdAndId(String brandId, long[] id);
	
	public void deleteSignCoin(SignCoin signCoin);
	
	public SignCommonCoin createSignCommonCoin(SignCommonCoin signCommonCoin);
	
	public SignCommonCoin getSignCommonCoinByBrandIdAndGrade(String brandId, String grade);
	
	public SignCommonCoin getSignCommonCoinByBrandIdAndGradeaAndCoin(String brandId, String grade, String bonusCoin);
	
	public List<SignCommonCoin> getSignCommonCoinByBrandId(String brandId);
	
	public List<SignCommonCoin> getSignCommonCoinByBrandIdAndId(String brandId, long[] id);
	
	public void deleteSignCommonCoin(SignCommonCoin signCommonCoin);
	
}
