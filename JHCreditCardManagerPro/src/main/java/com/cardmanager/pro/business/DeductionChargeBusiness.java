package com.cardmanager.pro.business;

import java.math.BigDecimal;

import com.cardmanager.pro.pojo.DeductionCharge;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;

public interface DeductionChargeBusiness {

	DeductionCharge findByCreditCardNumber(String creditCardNumber);

	BigDecimal updateAndDel(RepaymentTaskPOJO repaymentTaskPOJO);

}
