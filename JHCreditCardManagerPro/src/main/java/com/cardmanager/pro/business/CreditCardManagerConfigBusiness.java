package com.cardmanager.pro.business;

import java.util.List;

import com.cardmanager.pro.pojo.CreditCardManagerConfig;

public interface CreditCardManagerConfigBusiness {

	CreditCardManagerConfig save(CreditCardManagerConfig model);

	CreditCardManagerConfig findByVersion(String version);

	List<CreditCardManagerConfig> findAll();

	CreditCardManagerConfig findByVersionLock(String version);

	List<CreditCardManagerConfig> findByCreateOnOff(int createOnOff);

    CreditCardManagerConfig findCardManangerByVersion(String version);
}
