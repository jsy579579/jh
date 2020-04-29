package com.jh.user.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.UserAwardHistory;

@Repository
public interface UserAwardHistoryRepository extends JpaRepository<UserAwardHistory, Long>,JpaSpecificationExecutor<UserAwardHistory>{

	UserAwardHistory findByUserIdAndAwardMoney(String userId, BigDecimal awardMoney);

}
