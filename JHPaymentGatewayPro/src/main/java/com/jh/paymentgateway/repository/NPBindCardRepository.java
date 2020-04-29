package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.NPBindCard;


@Repository
public interface NPBindCardRepository extends JpaRepository<NPBindCard, String>, JpaSpecificationExecutor<NPBindCard>{
	
	@Query("select np from NPBindCard np where np.bankCard=:bankCard")
	public NPBindCard getNPBindCardByBankCard(@Param("bankCard") String bankCard);
	
}
