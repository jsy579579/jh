package com.jh.notice.business;

import com.jh.notice.pojo.SMSBlackList;

public interface SMSBlackListBusiness {

	SMSBlackList findByIpAddress(String ipAddress);

	SMSBlackList save(SMSBlackList smsBlackList);

}
