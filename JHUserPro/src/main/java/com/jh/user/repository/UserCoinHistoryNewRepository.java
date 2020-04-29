package com.jh.user.repository;

import com.jh.user.pojo.UserCoinHistoryNew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCoinHistoryNewRepository extends JpaRepository<UserCoinHistoryNew,Long>, JpaSpecificationExecutor<UserCoinHistoryNew> {

}
