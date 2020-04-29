package com.jh.user.repository;

import java.math.BigDecimal;
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
import com.jh.user.pojo.User;

@Repository
public interface UserRepository extends JpaRepository<User,String>,JpaSpecificationExecutor<User>{

	@Query("select user from  User user where user.id=:userid")
	User findUserById(@Param("userid") long userid);
	
	
	@Query("select count(*)  from  User user where user.brandId=:brandId  and  user.createTime >=:startTime and user.createTime <:endTime")
	int queryUserNumByBidandtime(@Param("brandId") long brandId,@Param("startTime") Date startTime,@Param("endTime") Date endTime);
	
	@Query("select count(*)  from  User user where user.brandId=:brandId  and  user.createTime >=:startTime and user.createTime <:endTime")
	int queryUserNumBytime(@Param("startTime") Date startTime,@Param("endTime") Date endTime);
	
	@Query("select user from  User user where user.brandId=:brandId")
	List<User> findUserByBrandId(@Param("brandId") long brandId);
	
	@Query("select user from  User user where  user.grade =:grade")
	List<User> queryUserByGrade(@Param("grade")String grade);
	
	@Query("select user from  User user where user.brandId=:brandId and user.phone=:phone")
    User findUserByPhoneAndBrandId(@Param("phone") String phone, @Param("brandId") long brandId);
	
	@Query("select user from  User user where  user.brandId=:brandId and user.grade =:grade")
	List<User> queryUserByGradeAndBrandId(@Param("brandId") long brandId,@Param("grade")String grade);
	
	@Query("select user from  User user where  user.brandId=:brandId and user.realnameStatus =:realnameStatus")
	List<User> queryUserByGradeAndStatus(@Param("brandId") long brandId,@Param("realnameStatus")String realnameStatus);
	

	@Query("select user from  User user where  user.realnameStatus =:realnameStatus")
	List<User> queryUserByGradeAndStatus(@Param("realnameStatus")String realnameStatus);
	
	@Query("select user from  User user where user.preUserId in (:userids) ")
	List<User> findAfterUserByIds(@Param("userids")Long[]  userIds);
	
	@Query("select user from  User user where user.id in (:userids) ")
	List<User> findAfterUserByIdsPageable(@Param("userids")Long[]  userIds,  Pageable pageAble);
	@Deprecated
	@Query("select user from  User user where user.phone=:phone")
	User findUserByPhone(@Param("phone") String phone);
	
	@Query("select user from  User user where user.phone=:phone")
	List<User>  findUsersByPhone(@Param("phone") String phone);
	
	@Query("select user from  User user where user.phone=:phone and user.brandId=:brandId")
	User findUserByPhoneAndBrandID(@Param("phone") String phone, @Param("brandId") long brandid);
	
	@Query("select user from  User user where user.preUserId=:userid")
	List<User> findAfterUserById(@Param("userid") long userid);
	
	
	@Query("select user from  User user where user.openid=:openid")
	User findUserByOpenid(@Param("openid") String openid);
	
	/**根据手机号和密码判断是否登陆*/
	@Deprecated
	@Query("select user from  User user where user.phone=:phone and user.password=:password")
	User findUserByPhoneAndPassword(@Param("phone") String phone, @Param("password") String password);
	
	/**根据手机号和密码和贴牌Id判断是否登陆*/
	@Query("select user from  User user where user.phone=:phone and user.password=:password and user.brandId=:brandId")
	User findUserByPhoneAndPassword(@Param("phone") String phone, @Param("password") String password, @Param("brandId") long brandId);
	
	/**根据用户id和支付密码判断是否有效*/
	@Query("select user from  User user where user.id=:userid and user.paypass=:paypass")
	User findUserByUseridAndPayPass(@Param("userid") long userid, @Param("paypass") String paypass);
	
	@Query("select user from  User user where user.phone=:phone  and user.brandId=:brandId and  user.createTime >=:startTime and user.createTime <:endTime")
	Page<User> findAllPageUser(@Param("phone") String phone, @Param("brandId") long brandId,  @Param("startTime") Date startTime,  @Param("endTime") Date endTime,  Pageable pageAble);


	@Query("select user from  User user where user.phone=:phone  and user.brandId=:brandId and  user.createTime >=:startTime")
	Page<User> findAllPageUser(@Param("phone") String phone, @Param("brandId") long brandId,  @Param("startTime") Date startTime,  Pageable pageAble);

	
	@Query("select user from  User user where user.phone=:phone  and user.brandId=:brandId")
	Page<User> findAllPageUser(@Param("phone") String phone, @Param("brandId") long brandId,   Pageable pageAble);

	
	@Query("select user from  User user where user.phone=:phone ")
	Page<User> findAllPageUser(@Param("phone") String phone, Pageable pageAble);

	
	@Query("select user from  User user where user.phone=:phone and  user.createTime >=:startTime and user.createTime <:endTime")
	Page<User> findAllPageUser(@Param("phone") String phone,  @Param("startTime") Date startTime,  @Param("endTime") Date endTime,   Pageable pageAble);

	@Query("select user from  User user where user.phone=:phone and  user.createTime >=:startTime")
	Page<User> findAllPageUser(@Param("phone") String phone,  @Param("startTime") Date startTime,  Pageable pageAble);
	

	@Query("select user from  User user where user.brandId=:brandId")
	Page<User> findAllPageUser(@Param("brandId") long brandId,   Pageable pageAble);

	@Query("select user from  User user where user.brandId=:brandId and  user.createTime >=:startTime and user.createTime <:endTime")
	Page<User> findAllPageUser(@Param("brandId") long brandId,  @Param("startTime") Date startTime,  @Param("endTime") Date endTime,   Pageable pageAble);

	@Query("select user from  User user where user.brandId=:brandId and  user.createTime >=:startTime")
	Page<User> findAllPageUser(@Param("brandId") long brandId,  @Param("startTime") Date startTime,  Pageable pageAble);

	
	//多条件查询
	//查询当前品牌下的所有用户
	@Query("select user from  User user where  user.brandId=:brandId ")
	Page<User> findAfterUserall(@Param("brandId") long brandId,Pageable pageable);
	
	//查询当前品牌下的所有用户
	@Query("select user from  User user ")
	Page<User> findAfterUserall(Pageable pageable);
	
	//查询当前品牌下的fullname用户
	@Query("select user from  User user where  user.brandId=:brandId and user.fullname like :fullname")
	Page<User> findAfterUserallByfullname(@Param("brandId") long brandId,@Param("fullname")String fullname,Pageable pageable);
	
	//查询当前品牌下的fullname用户
	@Query("select user from  User user where   user.fullname like '%:fullname%'")
	Page<User> findAfterUserallByfullname(@Param("fullname")String fullname,Pageable pageable);
	
	//查询当前品牌下的审核状态用户
	@Query("select user from  User user where  user.brandId=:brandId and user.realnameStatus=:realnameStatus")
	Page<User>  findAfterUserallByrealnameStatus(@Param("brandId") long brandId,@Param("realnameStatus")String realnameStatus,Pageable pageable);
	
	//查询当前品牌下的审核状态用户
	@Query("select user from  User user where  user.brandId=:brandId and user.shopsStatus=:shopsStatus")
	Page<User>  findAfterUserallByshopsStatus(@Param("brandId") long brandId,@Param("shopsStatus")String shopsStatus,Pageable pageable);
	
	
	//查询当前品牌下的审核状态用户
	@Query("select user from  User user where  user.realnameStatus=:realnameStatus")
	Page<User>  findAfterUserallByrealnameStatus(@Param("realnameStatus")String realnameStatus,Pageable pageable);
		
	//查询当前品牌下的审核状态用户
	@Query("select user from  User user where  user.shopsStatus=:shopsStatus")
	Page<User>  findAfterUserallByshopsStatus(@Param("shopsStatus")String shopsStatus,Pageable pageable);
			
	
	//查询当前品牌下的StartTimeDate用户
	@Query("select user from  User user where  user.brandId=:brandId and   user.createTime >=:startTime")
	Page<User>  findAfterUserallByStartTimeDate(@Param("brandId") long brandId,@Param("startTime") Date startTime,Pageable pageable);
	
	//查询当前品牌下的StartTimeDate用户
	@Query("select user from  User user where   user.createTime >=:startTime")
	Page<User>  findAfterUserallByStartTimeDate(@Param("startTime") Date startTime,Pageable pageable);
	
	//查询当前品牌下的endTime用户
	@Query("select user from  User user where  user.brandId=:brandId and   user.createTime <:endTime")
	Page<User> findAfterUserallByendTime(@Param("brandId") long brandId,@Param("endTime") Date endTime,Pageable pageable);
	
	//查询当前品牌下的endTime用户
	@Query("select user from  User user where    user.createTime <:endTime")
	Page<User> findAfterUserallByendTime(@Param("endTime") Date endTime,Pageable pageable);
	
	//查询当前品牌下的endTime用户
	@Query("select user from  User user  where  user.brandId=:brandId and user.createTime >=:startTime and user.createTime <=:endTime")
	Page<User> findAfterUserallBystartendTime(@Param("brandId") long brandId,@Param("startTime") Date startTime,@Param("endTime") Date endTime,Pageable pageable);
	
	//查询当前品牌下的endTime用户
	@Query("select user from  User user  where user.createTime >=:startTime and user.createTime <=:endTime")
	Page<User> findAfterUserallBystartendTime(@Param("startTime") Date startTime,@Param("endTime") Date endTime,Pageable pageable);

	@Query(value = "select t2.*  from t_user t2, (select (t1.pre_user_phone) as phone, count(t1.pre_user_phone) as cnt from t_user t1  group by t1.pre_user_phone) t3, t_brand t4 where t2.phone = t3.phone and t2.grade = '0' and t2.brand_id = t4.id and t4.auto_upgrade = '1' and t3.cnt >= t4.auto_upgrade_people", nativeQuery = true)
	List<User> findAutoUpgradeUser();	

	@Query(value = "select t2.*  from t_user t2, (select (t1.pre_user_phone) as phone from t_user t1  where t1.grade >= :grade and t1.real_name_status = '1' group by t1.pre_user_phone having count(t1.id) >= :peoplenum) t3, t_brand t4 where t2.phone = t3.phone and t2.grade < :curgrade and t2.brand_id = t4.id and t4.auto_upgrade = '1' and t4.id  = :brandid", nativeQuery = true)
	List<User> findNewAutoUpgradeUser(@Param("grade") long grade, @Param("curgrade") long curgrade, @Param("peoplenum") int peoplenum, @Param("brandid") long brandid);
	
	
	@Query(value = "select t2.*  from t_user t2, (select (t1.pre_user_phone) as phone from t_user t1  where t1.grade >= :grade and t1.real_name_status = '1' group by t1.pre_user_phone having count(t1.id) >= :peoplenum) t3, t_brand t4 where t2.phone = t3.phone and t2.grade < :curgrade and t2.brand_id = t4.id and t4.auto_upgrade = '3' and t4.id  = :brandid", nativeQuery = true)
	List<User> findNewAutoUpgradeUser3(@Param("grade") long grade, @Param("curgrade") long curgrade, @Param("peoplenum") int peoplenum, @Param("brandid") long brandid);
	
	
	@Query(value = "select t2.*  from t_user t2, (select (t1.pre_user_phone) as phone from t_user t1  where  t1.real_name_status = '1' group by t1.pre_user_phone having count(t1.id) >= :peoplenum) t3, t_brand t4 where t2.phone = t3.phone and t2.grade < :curgrade and t2.brand_id = t4.id and t4.auto_upgrade = '2' and t4.id  = :brandid", nativeQuery = true)
	List<User> findNewAutoUpgradeUserNoGrade( @Param("curgrade") long curgrade, @Param("peoplenum") int peoplenum, @Param("brandid") long brandid);

	@Query("select user from  User user where user.id in (:userids) ")
	Page<User> findAfterUserByIdsu(@Param("userids")Long[]  userIds,Pageable pageable);

	//查询当前品牌下的fullname用户
	@Query("select user from  User user where  user.id in (:userids) and user.fullname like '%:fullname%'")
	Page<User> findAfterUserallByfullname(@Param("userids") Long[] userIds,@Param("fullname")String fullname,Pageable pageable);
	
	
	@Query("select user from  User user where  user.id in (:userids) and user.grade =:grade")
	Page<User> findAfterUserallByGrade(@Param("userids") Long[] userIds,@Param("grade")String grade,Pageable pageable);
	
	//查询当前品牌下的审核状态用户
	@Query("select user from  User user where user.id in (:userids) and user.realnameStatus=:realnameStatus")
	Page<User> findAfterUserallByrealnameStatus(@Param("userids") Long[] userIds,@Param("realnameStatus")String realnameStatus,Pageable pageable);
	
	//查询当前品牌下的StartTimeDate用户
	@Query("select user from  User user where  user.id in (:userids) and   user.createTime >=:startTime")
	Page<User> findAfterUserallByStartTimeDate(@Param("userids") Long[] userIds,@Param("startTime") Date startTime,Pageable pageable);
	
	//查询当前品牌下的endTime用户
	@Query("select user from  User user where user.id in (:userids) and   user.createTime <:endTime")
	Page<User> findAfterUserallByendTime(@Param("userids") Long[] userIds,@Param("endTime") Date endTime,Pageable pageable);
	
	//查询当前品牌下的endTime用户
	@Query("select user from  User user  where  user.id in (:userids) and user.createTime >=:startTime and user.createTime <=:endTime")
	Page<User> findAfterUserallBystartendTime(@Param("userids") Long[] userIds,@Param("startTime") Date startTime,@Param("endTime") Date endTime,Pageable pageable);
		
	//查询用户等级返回long[]
	@Query("select id from  User user where  user.brandId=:brandId and user.grade =:grade")
	Long[] queryUserIdByGradeAndBrandId(@Param("brandId") long brandId,@Param("grade")String grade);
	
	//查询用户等级返回long[]
	@Query("select id from  User user where  user.preUserId in (:preUserId)")
	Long[] queryUserIdBypreUserIds(@Param("preUserId") Long[] preUserId);
	
	//查询用户等级返回long[]
	@Query("select id from  User user where  user.id in (:preUserId) and user.grade =:grade")
	Long[] queryUserIdBypreUserIdsAndGrade(@Param("preUserId") Long[] preUserId,@Param("grade")String grade);
	
	//查询用户等级返回long[]
	@Query("select id from  User user where  user.id in (:preUserId) and user.realnameStatus =:realnameStatus")
	Long[] queryUserIdBypreUserIdsAndrealnameStatus(@Param("preUserId") Long[] preUserId,@Param("realnameStatus")String realnameStatus);




	
	
	//==========================
	
	
	@Query("select user from User user where user.province like %:province% and user.city like %:city% and user.county like %:county%")
	List<User> queryUserByProvince1(@Param("province") String province, @Param("city") String city, @Param("county") String county ,Pageable pageable);
	
	
	@Query("select user from User user where user.province like %:province% and user.city like %:city%")
	List<User> queryUserByProvince2(@Param("province") String province, @Param("city") String city, Pageable pageable);
	
	
	@Query("select user from User user where user.city like %:city% and user.county like %:county%")
	List<User> queryUserByProvince3(@Param("city") String city, @Param("county") String county, Pageable pageable);
	
	@Query("select user from User user where user.province like %:province% and user.county like %:county%")
	List<User> queryUserByProvince4(@Param("province") String province, @Param("county") String county, Pageable pageable);
	
	@Query("select user from User user where user.province like %:province%")
	List<User> queryUserByProvince5(@Param("province") String province, Pageable pageable);
	
	@Query("select user from User user where user.city like %:city%")
	List<User> queryUserByProvince6(@Param("city") String city, Pageable pageable);
	
	@Query("select user from User user where user.county like %:county%")
	List<User> queryUserByProvince7(@Param("county") String county, Pageable pageable);
	
	@Query("select count(*) from User user where user.brandId=:brandId")
	int queryCountByBrandId(@Param("brandId")long brandId);
	

	@Query(value="select count(*) from t_user user where user.pre_user_id=:userId and DATE_FORMAT(user.create_time,'%Y-%m-%d')=:today",nativeQuery=true)
	int queryUserAfterTodayCount(@Param("userId")long userId, @Param("today")String todayStr);

	@Query(value="select count(*) from t_user user where user.pre_user_id=:userId and DATE_FORMAT(user.create_time,'%Y-%m-%d')=:yesterday",nativeQuery=true)
	int queryUserAfterYesterdayCount(@Param("userId")long userId, @Param("yesterday")String yesterday);

	
	//用户注销
	@Modifying
	@Query("delete from User user where user.id=:userid")
	void delUserByUserid(@Param("userid") long userid);
	
	
	//通过userid和brandid查询用户
	@Query(value="select user from User user where user.id=:userId and user.brandId=:brandId")
	User queryUserByIdAndBrandId(@Param("userId") long userId, @Param("brandId") long brandId);
	
	//获取用户的实名状态
	@Query("select user.realnameStatus from User user where user.id=:userId")
	String queryUserStatusByUserId(@Param("userId") long userId);
	
	@Modifying
	@Query("update User user set realnameStatus='1' where user.id=:userId")
	void updateUserStatusByUserId(@Param("userId")long userId);


	Page<User> queryUserByIdIn(Long[] id, Pageable pageable);


	List<User> queryUserByIdIn(Long[] id);


	User findByPhone(String phone);


	List<User> findByIdInAndBrandId(Long[] ids, long brandid);
	
	@Modifying
	@Query("update User user set user.preUserPhone=:preUserPhone where user.preUserId=:preUserId")
	void updatePreUserPhoneByPreUserId(@Param("preUserPhone")String preUserPhone, @Param("preUserId")long preUserId);

	@Query("select user from  User user where  user.id in (:userids) and user.grade >=:grade")
	Page<User> findAfterUserallByGradeYibaiguanjia(@Param("userids") Long[] userIds,@Param("grade")String grade,Pageable pageable);

	@Query(value="select t2.*  from t_user t2, (select (t1.pre_user_phone) as phone from t_user_relation t1  where t1.pre_user_grade >0 and t1.first_user_grade>0 group by t1.pre_user_phone having count(t1.first_user_id) >= :peopleNum) t3, t_brand t4 where t2.phone = t3.phone and t2.grade < :grade and t2.brand_id = t4.id and t4.auto_upgrade = '4' and t4.id  = :brandId ", nativeQuery = true)
	List<User> findNewAutoUpgradeUser4(@Param("grade") long grade, @Param("peopleNum") int peopleNum, @Param("brandId") long brandId);

	@Query("select u from User u where u.brandId=:brandId and u.realnameStatus=:realnameStatus")
    List<User> queryUserByBrandIdAndRealname(@Param("brandId") Long brandId,@Param("realnameStatus") String realnameStatus, Pageable pageable);

	@Query("select count(u) from User u where u.brandId=:brandId and u.realnameStatus=1")
    BigDecimal findBrandNumByBrandidAndRealAuth(@Param("brandId") Long brandId);

	@Query("select count(u) from User u where u.brandId=:brandId and u.grade>0")
    BigDecimal queryVipNumByBrandId(@Param("brandId") Long brandId);
	@Query("select u from User u where u.brandId=:brandId and u.older=1")
	Page<User> queryUserByBrandIdAndOlder(@Param("brandId") Long brandId, Pageable pageable);

	@Query(value = "select id from  t_user  where  t_user.pre_user_id in (:userids) and date_format(t_user.create_time, '%Y-%m-%d') = :date",nativeQuery = true)
    Long[] queryUserIdBypreUserIdsAndCreateTime(@Param("userids")Long[] userIds, @Param("date")String date);

	@Query("select id from User u")
	Long[] findUserIds();

	@Query("select id from  User user where  user.preUserId in (:preUserId) and user.grade > :grade")
	Long[] queryUserIdBypreUserIdsAndGradenew(@Param("grade")String grade,@Param("preUserId") Long[] downUserIds);

	@Query("select u from User u where u.brandId=:brandId and u.older=1")
    List<User> findUserByBrandIdAndOlder(@Param("brandId") Long brandId);

    @Query(value = "select count(id) from t_user where t_user.id in (:userids) and t_user.grade > 0",nativeQuery = true)
    int findVipCount(@Param("userids") Long[] userids);
}
