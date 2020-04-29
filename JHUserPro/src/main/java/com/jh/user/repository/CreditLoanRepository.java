package com.jh.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.CreditLoan;

@Repository
public interface CreditLoanRepository extends JpaRepository<CreditLoan, Long>,JpaSpecificationExecutor<CreditLoan> {

	Page<CreditLoan> findByBrandIdAndStatus(Long brandId, Integer status, Pageable pageable);

}
