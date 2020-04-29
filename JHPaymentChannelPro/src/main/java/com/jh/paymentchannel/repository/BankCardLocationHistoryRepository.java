package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.BankCardLocationHistory;

@Repository
public interface BankCardLocationHistoryRepository extends JpaRepository<BankCardLocationHistory, Integer>,JpaSpecificationExecutor<BankCardLocationHistory> {

}
