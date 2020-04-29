package com.cardmanager.pro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.cardmanager.pro.pojo.DeductionCharge;

@Repository
public interface DeductionChargeRepository extends JpaRepository<DeductionCharge, Long>, JpaSpecificationExecutor<DeductionCharge> {

	DeductionCharge findByCreditCardNumber(String creditCardNumber);

}
