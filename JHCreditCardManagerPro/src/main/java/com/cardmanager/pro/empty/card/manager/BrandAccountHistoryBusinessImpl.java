package com.cardmanager.pro.empty.card.manager;

import java.math.BigDecimal;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BrandAccountHistoryBusinessImpl implements BrandAccountHistoryBusiness {
	
	@Autowired
	private BrandAccountHistoryRepository rrandAccountHistoryRepository;

	@Override
	public BrandAccountHistory findByBrandAccountIdAndApplyOrderIdAndAddOrSub(Long brandAccountId, Long applyOrderId,
			int addOrSub) {
		return rrandAccountHistoryRepository.findByBrandAccountIdAndApplyOrderIdAndAddOrSub(brandAccountId, applyOrderId,addOrSub);
	}

	@Override
	@Transactional
	public BrandAccountHistory createOne(Long brandAccountId, Long applyOrderId, int addOrSub,BigDecimal reservedAmount, BigDecimal balance, BigDecimal freezeBalance) {
		BrandAccountHistory brandAccountHistory = new BrandAccountHistory();
		brandAccountHistory.setAddOrSub(addOrSub);
		brandAccountHistory.setAmount(reservedAmount);
		brandAccountHistory.setBrandAccountId(brandAccountId);
		brandAccountHistory.setApplyOrderId(applyOrderId);
		brandAccountHistory.setResidueBalance(balance);
		brandAccountHistory.setResidueFreezeBalance(freezeBalance);
		return this.save(brandAccountHistory);
	}

	@Override
	@Transactional
	public BrandAccountHistory save(BrandAccountHistory brandAccountHistory) {
		return rrandAccountHistoryRepository.saveAndFlush(brandAccountHistory);
	}

}
