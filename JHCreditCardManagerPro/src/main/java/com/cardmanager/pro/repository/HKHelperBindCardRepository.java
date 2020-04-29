package com.cardmanager.pro.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.cardmanager.pro.pojo.HKHelperBindCard;

@Repository
public interface HKHelperBindCardRepository extends JpaRepository<HKHelperBindCard, Long>,JpaSpecificationExecutor<HKHelperBindCard>{

	List<HKHelperBindCard> findByUserId(String userId);

	HKHelperBindCard findByUserIdAndCardNo(String userId, String creditCardNumber);

}
