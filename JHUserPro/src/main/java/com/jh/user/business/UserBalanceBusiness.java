package com.jh.user.business;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jh.user.pojo.UserAccount;
import com.jh.user.pojo.UserBalanceHistory;

public interface UserBalanceBusiness {


	/**
	 * 余额转让
	 * @param assignorUserAccount 转让人
	 * @param recipientUserAccount 接收人
	 * @param amount 金额
	 */
	public int userTransferAndReceiveBalance(Long assignorUserAccount,Long recipientUserAccount,BigDecimal amount);
	/**
	 * 信用分转让
	 * @param assignorUserAccount 转让人
	 * @param recipientUserAccount 接收人
	 * @param credit 信用分
	 */
	public int userTransferAndReceiveCredit(Long assignorUserAccount,Long recipientUserAccount,BigDecimal credit);

	/**查询用户的账户信息*/
	public UserAccount queryUserAccountByUserid(long userid);

	
	public UserAccount freezeUserAccount(Long userId, BigDecimal amount, String addorsub, String ordercode) throws Exception;
	
	
	public UserAccount freezeUserRebateAccount(Long userId, BigDecimal amount, String addorsub, String ordercode) throws Exception;
	
	
	/**提现冻结*/
	public UserAccount withdrawFreeAccount(long userid,  BigDecimal amount, String ordercode);
	
	/**分润提现冻结*/
	public UserAccount rebateFreezeAccount(long phone,  BigDecimal amount,  String ordercode);
	
	
	
	public UserAccount saveUserAccount(UserAccount userAccount);
	
	
	/**锁定用户的账户信息*/
	public UserAccount lockUserAccount(long userid);
	
	
	/**生成用户的交易余额变动历史*/
	public UserBalanceHistory  saveUserBalanceHistory(UserBalanceHistory balHistory);
	
	
	/**按用户的id分页查询用户的账户变更历史记录 */
	public Page<UserBalanceHistory>  queryUserBalHistoryByUserid(long userid,  Pageable pageAble);

	
	public UserAccount updateUserAccount(Long userId, BigDecimal amount,  String addorsub, String ordercode) throws Exception;
	
	
	public UserAccount updateUserRebateAccount(Long userId, BigDecimal amount,  String ordercode);
	
	/**按ID查询用户的日。月。总 收入***/
	
	public Map findSumUserBalByUserId( long userid);


	public UserAccount updateUserRebate(Long userId, BigDecimal rebateamount, String order_type,String addorsub, String orderCode);


	UserAccount updateUserManageByuserId(long userId, BigDecimal amount,String status);

    List<UserAccount> findByManage(BigDecimal bigDecimal);

	List<UserAccount> findManageByBrandId(String brandId, Pageable pageable);

	UserAccount findByBrandIdAndPhone(String brandId, String phone);

	List<UserAccount> queryUserAccountByRebateBalanceThan0();

    List<UserAccount> queryUserAccountByUsers(Long[] userids);

    int updataCreditPoints(Long userid,BigDecimal CreditPoints);
}
