package com.jh.user.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.UserOld;

@Repository
@Deprecated
public interface UserOldRepository extends JpaRepository<UserOld,String>,JpaSpecificationExecutor<UserOld>{

	@Query("select user from  UserOld user where user.id=:userid")
	UserOld findUserById(@Param("userid") long userid);
	
	@Query("select count(*)  from  UserOld user where user.brandId=:brandId  and  user.createTime >=:startTime and user.createTime <:endTime")
	int queryUserNumByBidandtime(@Param("brandId") long brandId,@Param("startTime") Date startTime,@Param("endTime") Date endTime);
	
	@Query("select count(*)  from  UserOld user where user.brandId=:brandId  and  user.createTime >=:startTime and user.createTime <:endTime")
	int queryUserNumBytime(@Param("startTime") Date startTime,@Param("endTime") Date endTime);
	
	@Query("select user from  UserOld user where user.brandId=:brandId")
	List<UserOld> findUserByBrandId(@Param("brandId") long brandId);
	
	@Query("select user from  UserOld user where  user.grade =:grade")
	List<UserOld> queryUserByGrade(@Param("grade")String grade);
	
	@Query("select user from  UserOld user where user.brandId=:brandId and user.phone=:phone")
	UserOld findUserByPhoneAndBrandId(@Param("phone") String phone, @Param("brandId") long brandId);
	
	@Query("select user from  UserOld user where  user.brandId=:brandId and user.grade =:grade")
	List<UserOld> queryUserByGradeAndBrandId(@Param("brandId") long brandId,@Param("grade")String grade);
	
	@Query("select user from  UserOld user where  user.brandId=:brandId and user.realnameStatus =:realnameStatus")
	List<UserOld> queryUserByGradeAndStatus(@Param("brandId") long brandId,@Param("realnameStatus")String realnameStatus);
	

	@Query("select user from  UserOld user where  user.realnameStatus =:realnameStatus")
	List<UserOld> queryUserByGradeAndStatus(@Param("realnameStatus")String realnameStatus);
	
	@Query("select user from  UserOld user where user.preUserId in (:userids) ")
	List<UserOld> findAfterUserByIds(@Param("userids")Long[]  userIds);
	
	@Query("select user from  UserOld user where user.id in (:userids) ")
	List<UserOld> findAfterUserByIdsPageable(@Param("userids")Long[]  userIds,  Pageable pageAble);
	
	@Query("select user from  UserOld user where user.phone=:phone")
	UserOld findUserByPhone(@Param("phone") String phone);
	
	@Query("select user from  UserOld user where user.phone=:phone")
	List<UserOld>  findUsersByPhone(@Param("phone") String phone);
	
	@Query("select user from  UserOld user where user.phone=:phone and user.brandId=:brandId")
	UserOld findUserByPhoneAndBrandID(@Param("phone") String phone, @Param("brandId") long brandid);
	
	@Query("select user from  UserOld user where user.preUserId=:userid")
	List<UserOld> findAfterUserById(@Param("userid") long userid);
	
	
	@Query("select user from  UserOld user where user.openid=:openid")
	UserOld findUserByOpenid(@Param("openid") String openid);
	
	/**根据手机号和密码判断是否登陆*/
	@Query("select user from  UserOld user where user.phone=:phone and user.password=:password")
	UserOld findUserByPhoneAndPassword(@Param("phone") String phone, @Param("password") String password);
	
	/**根据手机号和密码和贴牌Id判断是否登陆*/
	@Query("select user from  UserOld user where user.phone=:phone and user.password=:password and user.brandId=:brandId")
	UserOld findUserByPhoneAndPassword(@Param("phone") String phone, @Param("password") String password, @Param("brandId") long brandId);
	
	/**根据用户id和支付密码判断是否有效*/
	@Query("select user from  UserOld user where user.id=:userid and user.paypass=:paypass")
	UserOld findUserByUseridAndPayPass(@Param("userid") long userid, @Param("paypass") String paypass);
	
	@Query("select user from  UserOld user where user.phone=:phone  and user.brandId=:brandId and  user.createTime >=:startTime and user.createTime <:endTime")
	Page<UserOld> findAllPageUser(@Param("phone") String phone, @Param("brandId") long brandId,  @Param("startTime") Date startTime,  @Param("endTime") Date endTime,  Pageable pageAble);


	@Query("select user from  UserOld user where user.phone=:phone  and user.brandId=:brandId and  user.createTime >=:startTime")
	Page<UserOld> findAllPageUser(@Param("phone") String phone, @Param("brandId") long brandId,  @Param("startTime") Date startTime,  Pageable pageAble);

	
	@Query("select user from  UserOld user where user.phone=:phone  and user.brandId=:brandId")
	Page<UserOld> findAllPageUser(@Param("phone") String phone, @Param("brandId") long brandId,   Pageable pageAble);

	
	@Query("select user from  UserOld user where user.phone=:phone ")
	Page<UserOld> findAllPageUser(@Param("phone") String phone, Pageable pageAble);

	
	@Query("select user from  UserOld user where user.phone=:phone and  user.createTime >=:startTime and user.createTime <:endTime")
	Page<UserOld> findAllPageUser(@Param("phone") String phone,  @Param("startTime") Date startTime,  @Param("endTime") Date endTime,   Pageable pageAble);

	@Query("select user from  UserOld user where user.phone=:phone and  user.createTime >=:startTime")
	Page<UserOld> findAllPageUser(@Param("phone") String phone,  @Param("startTime") Date startTime,  Pageable pageAble);
	

	@Query("select user from  UserOld user where user.brandId=:brandId")
	Page<UserOld> findAllPageUser(@Param("brandId") long brandId,   Pageable pageAble);

	@Query("select user from  UserOld user where user.brandId=:brandId and  user.createTime >=:startTime and user.createTime <:endTime")
	Page<UserOld> findAllPageUser(@Param("brandId") long brandId,  @Param("startTime") Date startTime,  @Param("endTime") Date endTime,   Pageable pageAble);

	@Query("select user from  UserOld user where user.brandId=:brandId and  user.createTime >=:startTime")
	Page<UserOld> findAllPageUser(@Param("brandId") long brandId,  @Param("startTime") Date startTime,  Pageable pageAble);

	
	//多条件查询
	//查询当前品牌下的所有用户
	@Query("select user from  UserOld user where  user.brandId=:brandId ")
	Page<UserOld> findAfterUserall(@Param("brandId") long brandId,Pageable pageable);
	
	//查询当前品牌下的所有用户
	@Query("select user from  UserOld user ")
	Page<UserOld> findAfterUserall(Pageable pageable);
	
	//查询当前品牌下的fullname用户
	@Query("select user from  UserOld user where  user.brandId=:brandId and user.fullname like '%:fullname%'")
	Page<UserOld> findAfterUserallByfullname(@Param("brandId") long brandId,@Param("fullname")String fullname,Pageable pageable);
	
	//查询当前品牌下的fullname用户
	@Query("select user from  UserOld user where   user.fullname like '%:fullname%'")
	Page<UserOld> findAfterUserallByfullname(@Param("fullname")String fullname,Pageable pageable);
	
	//查询当前品牌下的审核状态用户
	@Query("select user from  UserOld user where  user.brandId=:brandId and user.realnameStatus=:realnameStatus")
	Page<UserOld>  findAfterUserallByrealnameStatus(@Param("brandId") long brandId,@Param("realnameStatus")String realnameStatus,Pageable pageable);
	
	//查询当前品牌下的审核状态用户
	@Query("select user from  UserOld user where  user.brandId=:brandId and user.shopsStatus=:shopsStatus")
	Page<UserOld>  findAfterUserallByshopsStatus(@Param("brandId") long brandId,@Param("shopsStatus")String shopsStatus,Pageable pageable);
	
	
	//查询当前品牌下的审核状态用户
	@Query("select user from  UserOld user where  user.realnameStatus=:realnameStatus")
	Page<UserOld>  findAfterUserallByrealnameStatus(@Param("realnameStatus")String realnameStatus,Pageable pageable);
		
	//查询当前品牌下的审核状态用户
	@Query("select user from  UserOld user where  user.shopsStatus=:shopsStatus")
	Page<UserOld>  findAfterUserallByshopsStatus(@Param("shopsStatus")String shopsStatus,Pageable pageable);
			
	
	//查询当前品牌下的StartTimeDate用户
	@Query("select user from  UserOld user where  user.brandId=:brandId and   user.createTime >=:startTime")
	Page<UserOld>  findAfterUserallByStartTimeDate(@Param("brandId") long brandId,@Param("startTime") Date startTime,Pageable pageable);
	
	//查询当前品牌下的StartTimeDate用户
	@Query("select user from  UserOld user where   user.createTime >=:startTime")
	Page<UserOld>  findAfterUserallByStartTimeDate(@Param("startTime") Date startTime,Pageable pageable);
	
	//查询当前品牌下的endTime用户
	@Query("select user from  UserOld user where  user.brandId=:brandId and   user.createTime <:endTime")
	Page<UserOld> findAfterUserallByendTime(@Param("brandId") long brandId,@Param("endTime") Date endTime,Pageable pageable);
	
	//查询当前品牌下的endTime用户
	@Query("select user from  UserOld user where    user.createTime <:endTime")
	Page<UserOld> findAfterUserallByendTime(@Param("endTime") Date endTime,Pageable pageable);
	
	//查询当前品牌下的endTime用户
	@Query("select user from  UserOld user  where  user.brandId=:brandId and user.createTime >=:startTime and user.createTime <=:endTime")
	Page<UserOld> findAfterUserallBystartendTime(@Param("brandId") long brandId,@Param("startTime") Date startTime,@Param("endTime") Date endTime,Pageable pageable);
	
	//查询当前品牌下的endTime用户
	@Query("select user from  UserOld user  where user.createTime >=:startTime and user.createTime <=:endTime")
	Page<UserOld> findAfterUserallBystartendTime(@Param("startTime") Date startTime,@Param("endTime") Date endTime,Pageable pageable);
		
	
	@Query(value = "select t2.*  from t_user_old t2, (select (t1.pre_user_phone) as phone, count(t1.pre_user_phone) as cnt from t_user_old t1  group by t1.pre_user_phone) t3, t_brand t4 where t2.phone = t3.phone and t2.grade = '0' and t2.brand_id = t4.id and t4.auto_upgrade = '1' and t3.cnt >= t4.auto_upgrade_people", nativeQuery = true)
	List<UserOld> findAutoUpgradeUser();	
	
	
	@Query(value = "select t2.*  from t_user_old t2, (select (t1.pre_user_phone) as phone from t_user_old t1  where t1.grade >= :grade and t1.real_name_status = '1' group by t1.pre_user_phone having count(t1.id) >= :peoplenum) t3, t_brand t4 where t2.phone = t3.phone and t2.grade < :curgrade and t2.brand_id = t4.id and t4.auto_upgrade = '1' and t4.id  = :brandid", nativeQuery = true)
	List<UserOld> findNewAutoUpgradeUser(@Param("grade") long grade, @Param("curgrade") long curgrade, @Param("peoplenum") int peoplenum, @Param("brandid") long brandid);
	
	
	@Query("select user from  UserOld user where user.id in (:userids) ")
	List<UserOld> findAfterUserByIdsu(@Param("userids")Long[]  userIds);
	
	
	
	//查询当前品牌下的fullname用户
	@Query("select user from  UserOld user where  user.id in (:userids) and user.fullname like '%:fullname%'")
	List<UserOld> findAfterUserallByfullname(@Param("userids") Long[] userIds,@Param("fullname")String fullname);
	
	
	@Query("select user from  UserOld user where  user.id in (:userids) and user.grade =:grade")
	List<UserOld> findAfterUserallByGrade(@Param("userids") Long[] userIds,@Param("grade")String grade);
	
	//查询当前品牌下的审核状态用户
	@Query("select user from  UserOld user where user.id in (:userids) and user.realnameStatus=:realnameStatus")
	List<UserOld> findAfterUserallByrealnameStatus(@Param("userids") Long[] userIds,@Param("realnameStatus")String realnameStatus);
	
	//查询当前品牌下的StartTimeDate用户
	@Query("select user from  UserOld user where  user.id in (:userids) and   user.createTime >=:startTime")
	List<UserOld> findAfterUserallByStartTimeDate(@Param("userids") Long[] userIds,@Param("startTime") Date startTime);
	
	//查询当前品牌下的endTime用户
	@Query("select user from  UserOld user where user.id in (:userids) and   user.createTime <:endTime")
	List<UserOld> findAfterUserallByendTime(@Param("userids") Long[] userIds,@Param("endTime") Date endTime);
	
	//查询当前品牌下的endTime用户
	@Query("select user from  UserOld user  where  user.id in (:userids) and user.createTime >=:startTime and user.createTime <=:endTime")
	List<UserOld> findAfterUserallBystartendTime(@Param("userids") Long[] userIds,@Param("startTime") Date startTime,@Param("endTime") Date endTime);
		
	//查询用户等级返回long[]
	@Query("select id from  UserOld user where  user.brandId=:brandId and user.grade =:grade")
	Long[] queryUserIdByGradeAndBrandId(@Param("brandId") long brandId,@Param("grade")String grade);
	
	//查询用户等级返回long[]
	@Query("select id from  UserOld user where  user.preUserId in (:preUserId)")
	Long[] queryUserIdBypreUserIds(@Param("preUserId") Long[] preUserId);
	
	//查询用户等级返回long[]
	@Query("select id from  UserOld user where  user.id in (:preUserId) and user.grade =:grade")
	Long[] queryUserIdBypreUserIdsAndGrade(@Param("preUserId") Long[] preUserId,@Param("grade")String grade);
	
	//查询用户等级返回long[]
	@Query("select id from  UserOld user where  user.id in (:preUserId) and user.realnameStatus =:realnameStatus")
	Long[] queryUserIdBypreUserIdsAndrealnameStatus(@Param("preUserId") Long[] preUserId,@Param("realnameStatus")String realnameStatus);
	
	
	//==========================
	
	
	@Query("select user from UserOld user where user.province like %:province% and user.city like %:city% and user.county like %:county%")
	List<UserOld> queryUserByProvince1(@Param("province") String province, @Param("city") String city, @Param("county") String county ,Pageable pageable);
	
	
	@Query("select user from UserOld user where user.province like %:province% and user.city like %:city%")
	List<UserOld> queryUserByProvince2(@Param("province") String province, @Param("city") String city, Pageable pageable);
	
	
	@Query("select user from UserOld user where user.city like %:city% and user.county like %:county%")
	List<UserOld> queryUserByProvince3(@Param("city") String city, @Param("county") String county, Pageable pageable);
	
	@Query("select user from UserOld user where user.province like %:province% and user.county like %:county%")
	List<UserOld> queryUserByProvince4(@Param("province") String province, @Param("county") String county, Pageable pageable);
	
	@Query("select user from UserOld user where user.province like %:province%")
	List<UserOld> queryUserByProvince5(@Param("province") String province, Pageable pageable);
	
	@Query("select user from UserOld user where user.city like %:city%")
	List<UserOld> queryUserByProvince6(@Param("city") String city, Pageable pageable);
	
	@Query("select user from UserOld user where user.county like %:county%")
	List<UserOld> queryUserByProvince7(@Param("county") String county, Pageable pageable);
	
	@Query("select count(*) from UserOld user where user.brandId=:brandId")
	int queryCountByBrandId(@Param("brandId")long brandId);
	

	@Query(value="select count(*) from t_user_old user where user.pre_user_id=:userId and user.create_time like %:today%",nativeQuery=true)
	int queryUserAfterTodayCount(@Param("userId")long userId, @Param("today")String todayStr);

	@Query(value="select count(*) from t_user_old user where user.pre_user_id=:userId and user.create_time like %:yesterday%",nativeQuery=true)
	int queryUserAfterYesterdayCount(@Param("userId")long userId, @Param("yesterday")String yesterday);

	
	//用户注销
	@Modifying
	@Query("delete from UserOld user where user.id=:userid")
	void delUserByUserid(@Param("userid") long userid);
	
	
	@Modifying
	@Query("update UserOld user set user.preUserPhone=:preUserPhone where user.preUserId=:preUserId")
	void updatePreUserPhoneByPreUserIdOld(@Param("preUserPhone")String preUserPhone, @Param("preUserId")long preUserId);
	
}
