package com.jh.paymentchannel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.RealNameAuth;

@Repository
public interface RealnameAuthRepository extends  PagingAndSortingRepository<RealNameAuth, String>{

	@Query("select realname from  RealNameAuth realname where realname.idcard=:idcard ")
	RealNameAuth findRealNameByIdcard(@Param("idcard") String idcard);
	
	@Query("select realname from  RealNameAuth realname where realname.idcard=:idcard and realname.realname=:realname and realname.userId=:userid")
	RealNameAuth findRealNamesByall(@Param("userid") long userid,@Param("idcard") String idcard,@Param("realname") String realname);
	
	@Query("select realname from  RealNameAuth realname where realname.realname like %:realname%")
	List<RealNameAuth> findRealNameByName(@Param("realname") String realname);
	
	@Query("select realname from  RealNameAuth realname where realname.userId=:userId ")
	List<RealNameAuth> findRealNameByUserId(@Param("userId") long userid);
	
	//根据userid注销用户的实名信息
	@Modifying
	@Query("delete from RealNameAuth realname where realname.userId=:userid")
	void delRealnameByUserid(@Param("userid") long userid);
	
	@Query("select realname from RealNameAuth realname where realname.userId in :userIds")
	List<RealNameAuth> findRealNamesAuthByUserIds(@Param("userIds")long[] userIds);

	RealNameAuth findByUserId(long userId);
	
	@Modifying
	@Query("update RealNameAuth set result=:result where userId=:userId")
	void updateResultById(@Param("userId") long userId,@Param("result") String result);
	
	@Query("select realname from RealNameAuth realname where realname.userId=:userId")
	RealNameAuth findRealnameAuthById(@Param("userId") long userId);


	@Query(value = "select * from  t_real_name_auth  where  t_real_name_auth.user_id in (:suserIds) and date_format(t_real_name_auth.auth_time, '%Y-%m-%d') = :date",nativeQuery = true)
    List<RealNameAuth> findRealNameByUserIdsAndCreateTime(@Param("suserIds")long[] suserIds,@Param("date") String date);

    @Query(value = "select * from  t_real_name_auth  where  t_real_name_auth.user_id in (:userIds) and t_real_name_auth.message = :status and date_format(t_real_name_auth.auth_time, '%Y-%m-%d') = :date",nativeQuery = true)
    List<RealNameAuth> findRealNameByUserIdsAndCreateTimeNew(@Param("userIds")Long[] userIds, @Param("status") String status, @Param("date") String date);

    @Query(value = "select count(*) from  t_real_name_auth  where  t_real_name_auth.user_id in (:userIds) and t_real_name_auth.message = :status ",nativeQuery = true)
    Integer findRealNameCountsByUserIds(@Param("userIds")Long[] userIds, @Param("status") String status);

}
