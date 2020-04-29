package com.jh.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.UserRebateAccountFreezeHistory;

@Repository
public interface UserRebateFreezeHistoryRepository extends JpaRepository<UserRebateAccountFreezeHistory,String>,JpaSpecificationExecutor<UserRebateAccountFreezeHistory>{

	//注销用户历史记录
	@Modifying
	@Query("delete from UserRebateAccountFreezeHistory userRebateAccountFreezeHistory where userRebateAccountFreezeHistory.userId=:userid")
	void delUserRebateFreeHistoryByUserid(@Param("userid") long userid);
}
