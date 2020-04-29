package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.CJHKLRBindCard;

/**
 * @author 作者:lirui
 * @version 创建时间：2019年5月27日 下午5:58:56 类说明
 */
@Repository
public interface CJHKLRBindCardRepository
		extends JpaRepository<CJHKLRBindCard, String>, JpaSpecificationExecutor<CJHKLRBindCardRepository> {
	@Query("select cjhk from CJHKLRBindCard cjhk where cjhk.bankCard=:bankCard")
	CJHKLRBindCard getCJHKBindCardByBankCard(@Param("bankCard")String bankCard);
}
