package com.jh.user.business;

import com.jh.user.pojo.Brand;
import com.jh.user.pojo.User;
import com.jh.user.pojo.UserOlderRebateHistory;
import com.jh.user.pojo.UserRoleForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface UserLoginRegisterBusiness {

	/** 注册成会员 */
	public User saveUser(User user);

	/** 判断是否登陆 */
	public User isValidUser(String phone, String password);

	/** 判断是否登陆 */
	public User isValidUser(String phone, String password, long brandId);

	/** 根据id获取用户基本信息 */
	public User queryUserById(long userid);

	/** 根据brandid和时间获取当前时间的人数 */
	public int queryUserNumByBidandtime(long brandId, Date StartTimeDate, Date endTimeDate);

	public User createOutNewUser(User user);
	
	/** 根据用户的手机号码贴牌获取用户 */
    public User queryUserByPhoneBrandid(String phone, long brandid);
	
	/** 根据用户的手机号码获取用户 */
    @Deprecated
	public User queryUserByPhone(String phone);

	/** 根据users的手机号码获取用户 */
	public List<User> queryUsersByPhone(String phone);

	/** 根据手机号码和贴牌id */
	public User queryUserByPhoneAndBrandid(String phone, long brandid);

	/** 根据brandId获取用户基本信息 */
	public List<User> queryUserByBrandId(long brandId);

	/*** 根据等级和贴牌查询 ***/
	public List<User> queryUserByGrade(long brandid, String grade);

	/*** 根据等级和状态查询 ***/
	public List<User> queryUserByStatus(long brandid, String status);

	/*** 根据状态查询 ***/
	public List<User> queryUserByStatus(String status);

	/** 根据openid获取用户 */
	public User queryUserByOpenid(String openid);

	/** 注册创建一个新用户 */
	public User createNewUser(User user);

	/** 验证支付密码是否正确 */
	public User isPaypassValid(long userid, String paypass);

	/**
	 * 获取用户信息 phone cardNo银行卡号 brandId通道ID brandname通道名称 fullname全称 realname真名
	 * realnameStatus审核状态
	 ***/
	public Map<String,Object> findInfoUserByall(long brandid, String fullname, String realnameStatus, String shopsStatus,Date StartTimeDate, Date endTimeDate, Pageable pageable);

	public Map<String,Object> findInfoUserByall(String fullname, String realnameStatus, String shopsStatus, Date StartTimeDate,Date endTimeDate, Pageable pageable);
	
	public Map<String,Object> findInfoUserByallSql(long brandid,String grade, String fullname, String realnameStatus, String shopsStatus,Date StartTimeDate, Date endTimeDate, Pageable pageable, String isDownload);

	public Map<String,Object> findInfoUserByallSql(String grade, String fullname, String realnameStatus, String shopsStatus, Date StartTimeDate,Date endTimeDate, Pageable pageable, String isDownload);
	
	/** 获取改用户的下级会员 */
	public List<User> findAfterUsers(long userid);

	/** 获取改用户的下级会员 */
	public List<User> findInfoUsers(Long[] userIds);

	/** 获取改用户的下级会员 */
	public List<User> findInfoUsersPageable(Long[] userIds, Pageable pageable);

	/***
	 * 下级会员条件查询
	 **/
	public Page<User> findUserInfoByall(Long[] str2, String fullname, String realnameStatus, String grade,
			Date StartTimeDate, Date endTimeDate, Pageable pageable);

	/****
	 * 获取贴牌等级uid
	 ***/
	public Long[] queryUserIdByGradeAndBrandId(long brandid, String grade);

	/****
	 * 获取下级会员IDS
	 **/
	public Long[] queryUserIdBypreUserIds(Long[] userIds);

	/****
	 * 获取等级下级会员IDS
	 **/
	public Long[] queryUserIdBypreUserIdsAndGrade(String grade, Long[] userIds);

	/****
	 * 下级实名状态查询
	 **/
	public Long[] queryUserIdBypreUserIdsAndrealnameStatus(String realnameStatus, Long[] userIds);

	// 根据省市县模糊查询用户信息
	public List<User> queryUserByProvince(String province, String city, String county, Pageable pageable);

	// 查询该贴牌下的用户数量
	public int queryBrandUserCount(long brandId);

	public int queryUserAfterTodayCount(long userId, String dateFromStr);

	public int queryUserAfterYesterdayCount(long userId, String yesterday);
	
	//用户注销
	public void delUserByUserid(long userid);
	
	//用户注销user表中记录
	public void delUserByUserId(long userid);
	
	//通过phone和brandid查询用户 
	public User findUserByIdAndBrandId(long phone,long brandId);
	
	//根据id查询用户的实名状态
	public String  findStatusByUserId(long userId);
	
	public void updateUserStatusByUserId(long userId);
	
	//临时
	public Brand findBrandByBrandid(long brandid);
	
	public void updateByAll(long brandManage, long userid, long brandid);

	//直推排行
	public List<UserRoleForm> findUserPreList(long brandid,String startTime,String endTime,int size);

	public Page<User> queryUserByIdIn(Long[] str2, Pageable pageable);
	
	public List<User> queryUserByIdIn(Long[] str2);

	public User findByPhone(String string);

	public List<User> findByIdInAndBrandId(Long[] ids, long brandid);
	
	public void updatePreUserPhoneByPreUserId(String preUserPhone, long preUserId);

    List<User> queryUserByBrandIdAndRealname(Long brandId,String realnameStatus, Pageable pageable);

	BigDecimal findBrandNumByBrandidAndRealAuth(Long brandId);

    BigDecimal queryVipNumByBrandId(Long brandId);

	Page<User> queryUserByBrandIdAndOlder(Long brandid, Pageable pageable);

	Long[] queryUserIdBypreUserIdsAndCreateTime(Long[] userIds, String date);

	Long[] findUserIds();

	Long[] queryUserIdBypreUserIdsAndGradenew(String s, Long[] downUserIds);

	Long[] queryUserIdBypreUserIdsAndrealnameStatusnew(String s, Long[] downUserIds);

	List<User> findUserByBrandIdAndOlder(Long brandid);

    public int findVipCount(Long[] userIds);

}
