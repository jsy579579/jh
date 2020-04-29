package com.jh.paymentchannel.business;

import java.util.List;

import com.jh.paymentchannel.pojo.RepaymentDetail;

public interface RepaymentDetailBusiness {
	
	
	public List<RepaymentDetail> getRepaymentDetailAll();

	public List<RepaymentDetail> findByVersionIn(String[] versions);
	
}
