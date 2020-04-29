package com.jh.paymentgateway.business;

import com.jh.paymentgateway.pojo.OrderParameter;

public interface OrderParameterBusiness {

	void save(OrderParameter orderParameter);

	OrderParameter findByOrderCode(String orderCode);

}
