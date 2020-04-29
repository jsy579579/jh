package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.HZLRBindCard;

/**
 * @author 作者:lirui
 * @version 创建时间：2019年6月7日 下午4:16:27 类说明
 */
@Repository
public interface HZLRBindCardRepository extends JpaRepository<HZLRBindCard, String>, JpaSpecificationExecutor<HZLRBindCard> {
	@Query("select hz from HZLRBindCard hz where hz.bankCard=:bankCard")
	public HZLRBindCard getHZLRBindCardByBankCard(@Param("bankCard") String bankCard);
}
