package com.jh.user.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.UserBankCardLimit;

@Repository
public interface UserBankCardLimitRepository extends JpaRepository<UserBankCardLimit, Long>, JpaSpecificationExecutor<UserBankCardLimit> {
	@Query("select count(*) from UserBankCardLimit ubcl where ubcl.userId=:userId and ubcl.createDate=:date")
	int queryTodayCount(@Param("userId") long userId, @Param("date") String date);

	@Query("select count(*) from UserBankCardLimit ubcl where ubcl.userId=:userId and ubcl.idcard=:idcard and ubcl.bankCard=:bankCard and ubcl.createDate=:date")
	int queryTodySameCount(@Param("userId") long userId, @Param("idcard") String idcard, @Param("date") String date,@Param("bankCard")String bankCard);

}
