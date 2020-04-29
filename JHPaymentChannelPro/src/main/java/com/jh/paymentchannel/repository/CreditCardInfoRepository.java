package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.CreditCardInfo;

@Repository
public interface CreditCardInfoRepository extends JpaRepository<CreditCardInfo, Long>,JpaSpecificationExecutor<CreditCardInfo>{

	CreditCardInfo findByCardNo(String cardNo);

}
