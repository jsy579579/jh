package com.cardmanager.pro.business;

import java.util.List;

import com.cardmanager.pro.pojo.HKHelperBindCard;

public interface HKHelperBindCardBusiness {

	List<HKHelperBindCard> findByUserId(String userId);

	HKHelperBindCard findByUserIdAndCardNo(String userId, String creditCardNumber);

	HKHelperBindCard saveNew(HKHelperBindCard hkHelperBindCard);

}
