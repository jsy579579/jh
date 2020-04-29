package com.jh.paymentgateway.util.kb;

import java.math.BigDecimal;


public class OrderQueryReq extends AbstractReq {

    private String orderNo;

    private String orgOrderNo;

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getOrgOrderNo() {
		return orgOrderNo;
	}

	public void setOrgOrderNo(String orgOrderNo) {
		this.orgOrderNo = orgOrderNo;
	}

	@Override
	public String toString() {
		return "OrderQueryReq [orderNo=" + orderNo + ", orgOrderNo=" + orgOrderNo + "]";
	}
    
    
	
    

}
