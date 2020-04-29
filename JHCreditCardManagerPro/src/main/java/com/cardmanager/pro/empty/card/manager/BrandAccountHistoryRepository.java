package com.cardmanager.pro.empty.card.manager;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandAccountHistoryRepository extends JpaRepository<BrandAccountHistory, Long>, JpaSpecificationExecutor<BrandAccountHistory> {

	@Query("select brandAccountHistory from BrandAccountHistory brandAccountHistory where brandAccountHistory.brandAccountId=:brandAccountId and brandAccountHistory.applyOrderId=:applyOrderId and brandAccountHistory.addOrSub=:addOrSub")
	BrandAccountHistory findByBrandAccountIdAndApplyOrderIdAndAddOrSub(@Param("brandAccountId")Long brandAccountId, @Param("applyOrderId")Long applyOrderId,@Param("addOrSub")int addOrSub);

}
