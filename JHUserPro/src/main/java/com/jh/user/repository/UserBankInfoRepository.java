package com.jh.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import com.jh.user.pojo.UserBankInfo;

@Repository
public interface UserBankInfoRepository extends JpaRepository<UserBankInfo,String>,JpaSpecificationExecutor<UserBankInfo>{
	//李梦珂 根据条件查询出需要的银行名称                                                                                                                                                                                        
	@Query("select bankinfo from  UserBankInfo bankinfo where bankinfo.userId=:userId and bankinfo.cardNo=:cardNo and bankinfo.type=:type and bankinfo.state = '0'")
	UserBankInfo queryBankNameByUserIdAndCardNo(@Param(value="userId")long userId,@Param(value="cardNo")String cardNo,@Param(value="type")String type);
	/**根据用户的id查询所有的绑定卡*/
	@Query("select bankinfo from  UserBankInfo bankinfo where bankinfo.userId=:userId and bankinfo.state = '0'")
	List<UserBankInfo> findUserBankInfoByUserid(@Param("userId") long userId);
	
	/**根据用户的id和卡号查询预留手机号码*/
	@Query("select bankinfo from  UserBankInfo bankinfo where bankinfo.userId=:userId and bankinfo.cardNo=:cardNo and bankinfo.state = '0'")
	List<UserBankInfo> findUserBankInfoByUseridAndCardno(@Param("userId") long userId,@Param("cardNo") String cardNo);
	
	
	/**根据用户的id查询所有type的绑定卡*/
	@Query("select bankinfo from  UserBankInfo bankinfo where bankinfo.userId=:userId and bankinfo.state = '0' and bankinfo.type=:type")
	List<UserBankInfo> findUserBankInfoByUseridType(@Param("userId") long userId,@Param("type") String type);
	
	@Modifying 
	@Query("update UserBankInfo bankinfo set bankinfo.idDef = '0' where bankinfo.userId=:userId and bankinfo.type=:type ")
	void updateUserBankInfoByUserid(@Param("userId") long userId,@Param("type") String  type);
	
	@Modifying
	@Query("update UserBankInfo bankinfo set bankinfo.bankBranchName=:bankBranchName,bankinfo.province=:province,bankinfo.city=:city,bankinfo.lineNo=:lineNo,bankinfo.securityCode=:securityCode,bankinfo.expiredTime=:expiredTime where bankinfo.state='0' and bankinfo.cardNo=:cardNo")
	void updateUserBankInfoByCardno(@Param("bankBranchName") String bankBranchName,@Param("province") String province,@Param("city") String city,@Param("lineNo") String lineNo,@Param("securityCode") String securityCode,@Param("expiredTime") String expiredTime,@Param("cardNo") String cardNo);
	
	/**获取用户的提现默认卡信息*/
	@Query("select bankinfo from  UserBankInfo bankinfo where bankinfo.userId=:userId and bankinfo.idDef = '1' and bankinfo.type='2' and bankinfo.state = '0'")
	UserBankInfo queryDefUserBankInfoByUserid(@Param("userId") long userId);
	
	/**根据卡号将用户的信息查询出来*/
	@Query("select bankinfo from  UserBankInfo bankinfo where bankinfo.cardNo=:cardNo  and bankinfo.state = '0' and bankinfo.type=:type")
	List<UserBankInfo> findUserBankInfoByCardno(@Param("cardNo") String cardNo,@Param("type") String type);

	@Query("select bankinfo from  UserBankInfo bankinfo where bankinfo.cardNo=:cardNo  and bankinfo.state = '0' ")
	UserBankInfo findUserBankInfoByCardno(@Param("cardNo") String cardNo);

	/**根据卡号将用户的信息type查询出来*/
	@Query("select bankinfo from  UserBankInfo bankinfo where bankinfo.cardNo=:cardNo and bankinfo.type=:type and bankinfo.state = '0'")
	List<UserBankInfo> findUserBankInfoByCardnoType(@Param("cardNo") String cardNo,@Param("type") String type);
	
	/**根据联行号将用户的卡信息查出来*/
	@Query("select bankinfo from  UserBankInfo bankinfo where bankinfo.lineNo=:lineNo")
	UserBankInfo findUserBankInfoByLineno(@Param("lineNo") String lineNo);

	@Query("select bankinfo from UserBankInfo bankinfo where bankinfo.userId=:userId and bankinfo.nature like '%贷记%' and bankinfo.state='0'")
	List<UserBankInfo> findNatureByUserIdLikeNature(@Param("userId")long userId);
	
	//根据cardNo和brankbranchName查询用户银行信息
	
	
	@Query("from UserBankInfo bankinfo where bankinfo.bankBranchName=:bankbranchName and bankinfo.cardNo=:cardNo")
	UserBankInfo findBankNumberBybankbranchname(@Param("bankbranchName") String bankbranchName,@Param("cardNo") String cardNo);
		
	//根据userid注销用户的bankinfo信息
	@Modifying
	@Query("delete from UserBankInfo bankinfo where bankinfo.userId=:userId")
	void delBankInfoByUserid(@Param("userId") long userId);
	
	@Query("select count(*) from UserBankInfo bankinfo where bankinfo.userId=:userId and bankinfo.cardNo=:bankCardNumber and bankinfo.state='0' and bankinfo.nature like %:orderType% ")
	int queryCountByUserIdAndCardNoAndCardType(@Param("userId")Long userId, @Param("bankCardNumber")String bankCardNumber, @Param("orderType")String orderType);


	@Modifying
	@Query("update UserBankInfo bankInfo set bankInfo.state='1' where bankInfo.userId=:userId")
	void UpdateStateByState(@Param("userId") long userId);
	
	UserBankInfo findUserBankInfoByUserIdAndCardNoAndStateAndType(Long userId, String creditCardNumber, String state,String type);
	
	@Query("select bankinfo from UserBankInfo bankinfo where bankinfo.userId=:userId and bankinfo.cardNo=:bankCardNumber and bankinfo.state='0' and bankinfo.nature like %:orderType% ")
	UserBankInfo queryByUserIdAndCardNoAndCardType(@Param("userId")Long userId, @Param("bankCardNumber")String bankCardNumber, @Param("orderType")String cardType);

	@Query("select bankinfo from  UserBankInfo bankinfo where bankinfo.cardNo=:cardNo  and bankinfo.state = '0' and bankinfo.type=:type and bankinfo.userId=:userId")
	List<UserBankInfo> findUserBankInfoByCardnoAndTypeAndUserId(@Param("cardNo") String cardNo,@Param("type") String type,@Param("userId")Long userId);
	
	@Query("select bankinfo from  UserBankInfo bankinfo where bankinfo.userId=:userId and bankinfo.type=:type and bankinfo.nature like %:nature% and bankinfo.state=:state and bankinfo.idDef in :idDef order by bankinfo.createTime desc")
	List<UserBankInfo> getUserBankInfoByUserIdAndTypeAndNatureAndState(@Param("userId") long userId, @Param("type") String type, @Param("nature") String nature, @Param("state") String state, @Param("idDef") String[] idDef);


}
