package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.JFSBindCard;

@Repository
public interface JFSBindCardRepository extends JpaRepository<JFSBindCard, String>, JpaSpecificationExecutor<JFSBindCard> {
	@Query("select jfs from JFSBindCard jfs where jfs.bankCard=:bankCard")
	public JFSBindCard getJFSBindCardByBankCard(@Param("bankCard") String bankCard);
}
