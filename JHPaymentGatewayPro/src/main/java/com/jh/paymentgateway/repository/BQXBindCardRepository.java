
package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.jh.paymentgateway.pojo.BQXBindCard;
@Repository
public interface BQXBindCardRepository extends JpaRepository<BQXBindCard, String>,JpaSpecificationExecutor<BQXBindCard>{
	@Query("select bqx from BQXBindCard bqx where bqx.bankCard=:bankCard")
	public BQXBindCard getBQXBindCardByBankCard(@Param("bankCard") String bankCard);
}
