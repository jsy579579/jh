package com.jh.paymentchannel.business;

import com.jh.paymentchannel.pojo.BatchNo;

public interface BatchNoBusiness {
	BatchNo findByBindId(String BindId);
	
	BatchNo save(BatchNo batchno);
}
