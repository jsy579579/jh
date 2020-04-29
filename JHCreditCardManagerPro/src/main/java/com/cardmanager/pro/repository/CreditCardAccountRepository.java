package com.cardmanager.pro.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cardmanager.pro.pojo.CreditCardAccount;
@Repository
public interface CreditCardAccountRepository extends JpaRepository<CreditCardAccount, Long>,JpaSpecificationExecutor<CreditCardAccount>{

	CreditCardAccount findByUserIdAndCreditCardNumberAndVersion(String userId, String creditCardNumber,String version);

	@Query("select creditCardAccount from CreditCardAccount creditCardAccount where creditCardAccount.blance >:firstAmount and creditCardAccount.version=:version")
	Page<CreditCardAccount> findCreditCardAccountByBlanceNotZeroAndVersion(@Param("firstAmount")BigDecimal firstAmount,@Param("version")String version,Pageable pageable);

	@Query("select creditCardAccount from CreditCardAccount creditCardAccount where creditCardAccount.freezeBlance > '0' and creditCardAccount.version=:version ")
	List<CreditCardAccount> findByFreezeBlanceGreaterThan0AndVersion(@Param("version")String version);

	CreditCardAccount findByCreditCardNumberAndVersion(String creditCardNumber,String version);

	List<CreditCardAccount> findByFreezeBlanceAndVersion(BigDecimal amount,String version);

	@Query("select creditCardAccount from CreditCardAccount creditCardAccount where creditCardAccount.blance < '-10' and creditCardAccount.version=:version")
	Page<CreditCardAccount> findCreditCardAccountByBlanceLessTenAndVersion(@Param("version")String version,Pageable pageable);

	List<CreditCardAccount> findByBlanceAndVersionAndLastUpdateTimeLessThan(BigDecimal blance, String version,Date time);

	@Lock(value = LockModeType.PESSIMISTIC_WRITE)
	CreditCardAccount findByCreditCardNumberAndUserIdAndVersion(String creditCardNumber, String userId, String version);

	List<CreditCardAccount> findByCreditCardNumber(String creditCardNumber);

	List<CreditCardAccount> findByPhone(String phone);

	@Query("select c from CreditCardAccount c where c.version =:version and c.creditCardNumber =:creditCardNumber")
	CreditCardAccount getCreditCardAccount (@Param("version") String version,@Param("creditCardNumber")String creditCardNumber);
}
