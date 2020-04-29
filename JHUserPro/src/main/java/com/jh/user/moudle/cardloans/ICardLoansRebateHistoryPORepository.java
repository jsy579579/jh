package com.jh.user.moudle.cardloans;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ICardLoansRebateHistoryPORepository extends JpaRepository<CardLoansRebateHistoryPO, Long>, JpaSpecificationExecutor<CardLoansRebateHistoryPO> {

	Page<CardLoansRebateHistoryPO> findByReceiveUserId(String userId, Pageable pageable);

}
