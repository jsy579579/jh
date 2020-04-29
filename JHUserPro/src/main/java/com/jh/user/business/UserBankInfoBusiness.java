package com.jh.user.business;

import java.util.List;

import org.springframework.data.repository.query.Param;

import com.jh.user.pojo.BankAcronym;
import com.jh.user.pojo.BankIcon;
import com.jh.user.pojo.BankNumber;
import com.jh.user.pojo.UserBankInfo;

public interface UserBankInfoBusiness {
	/**通过银行卡号修改用户信息*/
	public void updateUserBankInfoByCardno(String bankBranchName,String province,String city,String lineNo,String securityCode,String expiredTime,String bankno);
	
	/**通过用户的id获取用户的卡片信息*/
	public List<UserBankInfo>   queryUserBankInfoByUserid(long userid);
	
	/**通过用户的id获取用户的卡片信息*/
	public List<UserBankInfo>   queryUserBankInfoByUserid(long userid,String type);
	
	/***获取用户的默任结算卡信息*/
	public UserBankInfo  queryDefUserBankInfoByUserid(long userid);
	
 		
	/**根据卡号获取用户的基本信息*/
	public List<UserBankInfo>  queryUserBankInfoByCardno(String cardno,String type);
	
	/**根据卡号获取用户的基本信息
	 * Type
	 * */
	public List<UserBankInfo>  queryUserBankInfoByCardnoType(String cardno,String type);
		
	/**根据联行号获取用户的基本卡信息*/
	public UserBankInfo   queryUserBankInfoByLineno(String lineno);
	
	/**生成银行账号信息*/
	public UserBankInfo   saveUserBankInfo(UserBankInfo  bankInfo);
	
	public void   updateAllNoDefault(long userid,String type);

	/**设置默认卡*/
	public UserBankInfo  setDefaultBank(long userid,  String cardno);
	
	/**根据银行名称获取银行编号*/
	public BankNumber queryBankNumberByBankName(String bankName);
	
	/**根据银行名称获取银行缩写*/
	public BankAcronym queryBankAcronymByBankName(String bankName);
	
	/**
	 * 根据userId查询用户银行卡的nature
	 * @return
	 */
	public List<UserBankInfo> findNatureByUserId(long userId);
	
	/***
	 * 
	 * @param userId
	 * @param cardNo
	 * @param type
	 * @return 李梦珂
	 */
	public UserBankInfo queryBankNameByUserIdAndCardNo(long userId,String cardNo,String type);
	/**
	 * 根据userId和bankNo查询用户银行卡信息
	 * @param userId
	 * @param bankNo
	 * @return
	 */
	public List<UserBankInfo> findUserBankInfoByUseridAndCardno(long userId, String cardNo);
	
	/*获取银行支行名*/
	public UserBankInfo findBankNumberBybankbranchname(String bankbranchName,String cardNo);

	public int queryCountByUserIdAndCardNoAndCardType(Long userId, String bankCardNumber, String cardType);

	public UserBankInfo findUserBankInfoByUserIdAndCardNoAndState(Long valueOf, String creditCardNumber, String state,String type);
	
	//通过userId更新银行卡状态
	public void updateBankCardByUserId(long userId);

	public UserBankInfo queryByUserIdAndCardNoAndCardType(Long valueOf, String bankCardNumber, String cardType);
	
	//根据卡号、卡类型和userId查询银行卡信息
	public List<UserBankInfo>  queryUserBankInfoByCardnoAndTypeAndUserId(String cardno,String type,long userId);
	
	public List<BankIcon> getBankIcon();
	
	public List<UserBankInfo>   getUserBankInfoByUserIdAndTypeAndNatureAndState(long userId, String type, String nature, String state, String[] isDefault);
	
	public void setDefaultBankByUserIdAndBankCardAndType(long userId,  String bankCard, String type);


    UserBankInfo findUserBankInfoByCardno(String cardNo);
}
