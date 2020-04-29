package com.cardmanager.pro.empty.card.manager;

import java.math.BigDecimal;

public interface BrandAccountHistoryBusiness {

	BrandAccountHistory findByBrandAccountIdAndApplyOrderIdAndAddOrSub(Long brandAccountId, Long applyOrderId, int addOrSub);

	BrandAccountHistory createOne(Long brandAccountId, Long applyOrderId, int addOrSub, BigDecimal reservedAmount, BigDecimal balance,BigDecimal freezeBalance);

	BrandAccountHistory save(BrandAccountHistory brandAccountHistory);
}
