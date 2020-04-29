package com.cardmanager.pro.empty.card.manager;

import java.math.BigDecimal;

public interface BrandAccountBusiness {

	BrandAccount findByBrandId(String brandId);

	BrandAccount save(BrandAccount brandAccount);

	BrandAccount updateAccount(BrandAccount brandAccount, int addOrSub, BigDecimal reservedAmount,EmptyCardApplyOrder emptyCardApplyOrder);

}
