package com.jh.user.repository;

import javax.persistence.LockModeType;

import com.jh.user.pojo.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.UserAccount;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount,Long>,JpaSpecificationExecutor<UserAccount>{


	@Lock(value = LockModeType.PESSIMISTIC_WRITE)
	@Query("select account from  UserAccount account where account.userId=:userid")
	UserAccount findUserAccountByUseridLock(@Param("userid") long userid);
	
	//用户转让余额
	@Modifying
	@Query("update UserAccount account  set account.balance =account.balance- :balance where account.userId=:userid")
	int userTransferTheBalance(@Param("userid") long userid, @Param("balance")BigDecimal balance);
	//用户接收余额
	@Modifying
	@Query("update UserAccount account  set account.balance =account.balance+ :balance where account.userId=:userid")
	int userReceiveTheBalance(@Param("userid") long userid,@Param("balance") BigDecimal balance);


	//用户转让信用积分
	@Modifying
	@Query("update UserAccount account  set account.coinNew =account.coinNew- :coin where account.userId=:userid")
	int userTransferTheCredit(@Param("userid") long userid, @Param("coin")BigDecimal coinNew);
	//用户接收信用积分
	@Modifying
	@Query("update UserAccount account  set account.coinNew =account.coinNew+ :coin where account.userId=:userid")
	int userReceiveTheCredit(@Param("userid") long userid,@Param("coin") BigDecimal coinNew);




	@Query("select account from  UserAccount account where account.userId=:userid")
	UserAccount findUserAccountByUserid(@Param("userid") long userid);
	
	//根据userid删除用户余额记录
	@Modifying
	@Query("delete from UserAccount account where account.userId=:userid")
	void delUserAccount(@Param("userid") long userid);

	@Query("select a from UserAccount a where a.manage>:bigDecimal")
	List<UserAccount> findUserAccountBymanage(@Param("bigDecimal")BigDecimal bigDecimal);

	@Query("select a from UserAccount a where a.userId in (:userIds)")
	List<UserAccount> findManageByBrandId(@Param("userIds") Long[] userIds, Pageable pageable);

	@Query("select u from UserAccount u where u.rebateBalance>0")
    List<UserAccount> queryUserAccountByRebateBalanceThan0();

	@Query("select a from UserAccount a where a.userId in (:userIds)")
    List<UserAccount> queryUserAccountByUsers(@Param("userIds") Long[] userIds);

	@Modifying
	@Query("update  UserAccount set creditPoints=creditPoints+:CreditPoints where userId = :userId")
	int updataCreditPoints(@Param("userId") Long userIds,@Param("CreditPoints")BigDecimal CreditPoints);
}
