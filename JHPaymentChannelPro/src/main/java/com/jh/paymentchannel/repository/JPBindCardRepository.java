package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.JPBindCard;





@Repository
public interface JPBindCardRepository extends JpaRepository<JPBindCard, String>, JpaSpecificationExecutor<JPBindCard>{
	
	@Query("select cjhk from JPBindCard cjhk where cjhk.bankCard=:bankCard")
	public JPBindCard getJPBindCardByBankCard(@Param("bankCard") String bankCard);
	
}
