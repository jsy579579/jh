
package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.jh.paymentgateway.pojo.BQXBindCard;
import com.jh.paymentgateway.pojo.LMDHBindCard;
@Repository
public interface LMDHBindCardRepository extends JpaRepository<LMDHBindCard, String>,JpaSpecificationExecutor<LMDHBindCard>{
	@Query("select lm from LMDHBindCard lm where lm.bankCard=:bankCard")
	public LMDHBindCard getLMDHBindCardByBankCard(@Param("bankCard") String bankCard);
}
