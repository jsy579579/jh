package com.cardmanager.pro.empty.card.manager;

import java.math.BigDecimal;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BrandAccountBusinessImpl implements BrandAccountBusiness {

	@Autowired
	private BrandAccountRepository brandAccountRepository;
	
	@Autowired
	private BrandAccountHistoryBusiness brandAccountHistoryBusiness;

	@Override
	public BrandAccount findByBrandId(String brandId) {
		return brandAccountRepository.findByBrandId(brandId);
	}

	@Override
	@Transactional
	public BrandAccount save(BrandAccount brandAccount) {
		return brandAccountRepository.saveAndFlush(brandAccount);
	}

	@Override
	@Transactional
//	addOrSub:0 增加余额    1:减少balance增加freezeBalance 2:增加balance减少freezeBalance 3:减少freezeBalance
	public BrandAccount updateAccount(BrandAccount brandAccount, int addOrSub, BigDecimal reservedAmount,EmptyCardApplyOrder emptyCardApplyOrder) {
		brandAccount = brandAccountRepository.findByIdLock(brandAccount.getId());
		BrandAccountHistory brandAccountHistory = brandAccountHistoryBusiness.findByBrandAccountIdAndApplyOrderIdAndAddOrSub(brandAccount.getId(),emptyCardApplyOrder.getId(),addOrSub);
		if (brandAccountHistory == null) {
			brandAccount = brandAccountRepository.findOne(brandAccount.getId());
			
			BigDecimal balance = brandAccount.getBalance();
			BigDecimal freezeBalance = brandAccount.getFreezeBalance();
			if (0 == addOrSub) {
				brandAccount.setBalance(balance.add(reservedAmount));
			}else if(1 == addOrSub) {
				brandAccount.setBalance(balance.subtract(reservedAmount));
				brandAccount.setFreezeBalance(freezeBalance.add(reservedAmount));
			}else if(2 == addOrSub) {
				brandAccount.setBalance(balance.add(reservedAmount));
				brandAccount.setFreezeBalance(freezeBalance.subtract(reservedAmount));
			}else if(3 == addOrSub) {
				brandAccount.setFreezeBalance(freezeBalance.subtract(reservedAmount));
			}

			brandAccountHistory = brandAccountHistoryBusiness.createOne(brandAccount.getId(),emptyCardApplyOrder.getId(),addOrSub,reservedAmount,brandAccount.getBalance(),brandAccount.getFreezeBalance());
			this.save(brandAccount);
		}
		return brandAccount;
	}
}
