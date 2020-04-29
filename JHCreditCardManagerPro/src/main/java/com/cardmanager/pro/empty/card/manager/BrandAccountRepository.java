package com.cardmanager.pro.empty.card.manager;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandAccountRepository extends JpaRepository<BrandAccount, Long>, JpaSpecificationExecutor<BrandAccount> {

	BrandAccount findByBrandId(String brandId);

	@Lock(value = LockModeType.PESSIMISTIC_WRITE)
	@Query(value="select brandAccount from BrandAccount brandAccount where brandAccount.id=:id")
	BrandAccount findByIdLock(@Param("id")Long id);

}
