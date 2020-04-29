package com.jh.user.repository;


import com.jh.user.pojo.UserOlderRebateHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserOlderRebateHistoryRepository extends JpaRepository<UserOlderRebateHistory,Long>, JpaSpecificationExecutor<UserOlderRebateHistory> {
}
