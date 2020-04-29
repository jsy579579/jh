package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.BankCard4AuthHistory;

@Repository
public interface BankCard4AuthHistoryRepository extends JpaRepository<BankCard4AuthHistory, Integer>,JpaSpecificationExecutor<BankCard4AuthHistory> {

}
