package com.jh.paymentchannel.business;

import com.jh.paymentchannel.pojo.CardEvaInfo;
import com.jh.paymentchannel.pojo.UserQueryCount;

public interface CardEvaluationBusiness {
	
	
	public UserQueryCount createUserQueryCount(UserQueryCount userQueryCount);
	
	public UserQueryCount getUserQueryCountByUserId(String userId);
	
	public void createCardEvaInfo(CardEvaInfo cardEvaInfo);
	
	public CardEvaInfo getCardEvaInfoByUserId(long userId);
	
	
}
