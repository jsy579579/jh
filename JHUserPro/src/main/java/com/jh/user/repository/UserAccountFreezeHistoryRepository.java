package com.jh.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.UserAccountFreezeHistory;

@Repository
public interface UserAccountFreezeHistoryRepository extends JpaRepository<UserAccountFreezeHistory,String>,JpaSpecificationExecutor<UserAccountFreezeHistory>{

	//注销用户历史记录
	@Modifying
	@Query("delete from UserAccountFreezeHistory userAccountFreezeHistory where userAccountFreezeHistory.userId=:userid")
	void delUserAccountHistoryByUserid(@Param("userid") long userid);
}
