package com.cardmanager.pro.business.impl;

import java.math.BigDecimal;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cardmanager.pro.business.ConsumeTaskPOJOBusiness;
import com.cardmanager.pro.business.DeductionChargeBusiness;
import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import com.cardmanager.pro.pojo.DeductionCharge;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;
import com.cardmanager.pro.repository.DeductionChargeRepository;

@Service
public class DeductionChargeBusinessImpl implements DeductionChargeBusiness {
	
	@Autowired
	private DeductionChargeRepository deductionChargeRepository;
	
	@Autowired
	private ConsumeTaskPOJOBusiness consumeTaskPOJOBusiness;

	@Override
	public DeductionCharge findByCreditCardNumber(String creditCardNumber) {
		try {
			return deductionChargeRepository.findByCreditCardNumber(creditCardNumber);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	@Transactional
	public BigDecimal updateAndDel(RepaymentTaskPOJO repaymentTaskPOJO) {
		BigDecimal amount = BigDecimal.ZERO;
		try {
			DeductionCharge deductionCharge = this.findByCreditCardNumber(repaymentTaskPOJO.getCreditCardNumber());
			if (deductionCharge != null) {
				List<ConsumeTaskPOJO> consumeTaskPOJOs = consumeTaskPOJOBusiness.findByRepaymentTaskId(repaymentTaskPOJO.getRepaymentTaskId());
				BigDecimal consumeAmount = BigDecimal.ZERO;
				for (ConsumeTaskPOJO consumeTaskPOJO : consumeTaskPOJOs) {
					if (consumeTaskPOJO.getOrderStatus() == 1) {
						consumeAmount = consumeAmount.add(consumeTaskPOJO.getAmount());
					}
				}
				if (repaymentTaskPOJO.getRealAmount().compareTo(consumeAmount) > 0 && repaymentTaskPOJO.getRealAmount().subtract(consumeAmount).compareTo(repaymentTaskPOJO.getServiceCharge()) <= 0) {
					BigDecimal deductionAmount = deductionCharge.getDeductionAmount();
					BigDecimal realDeductionAmount = repaymentTaskPOJO.getRealAmount().subtract(consumeAmount);
					if (deductionAmount.compareTo(realDeductionAmount) > 0) {
						deductionAmount = deductionAmount.subtract(realDeductionAmount);
						deductionCharge.setDeductionAmount(deductionAmount);
						deductionChargeRepository.saveAndFlush(deductionCharge);
					}else {
						deductionChargeRepository.delete(deductionCharge);
					}
					amount = realDeductionAmount;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return amount;
		}
		return amount;
	}

}
