package com.jh.user.business.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.user.business.UserBankInfoBusiness;
import com.jh.user.pojo.BankAcronym;
import com.jh.user.pojo.BankIcon;
import com.jh.user.pojo.BankNumber;
import com.jh.user.pojo.UserBankInfo;
import com.jh.user.repository.BankAcronymRepository;
import com.jh.user.repository.BankIconRepository;
import com.jh.user.repository.BankNumberRepository;
import com.jh.user.repository.UserBankInfoRepository;

@Service
public class UserBankInfoBusinessImpl implements UserBankInfoBusiness{
	
	@Autowired
	private UserBankInfoRepository userBankInfoRepository;
	
	@Autowired
	private BankNumberRepository bankNumberRepository;
	
	@Autowired
	private BankAcronymRepository bankAcronymRepository;
	
	@Autowired
	private BankIconRepository bankIconRepository;
	
	@Autowired
	private EntityManager em;
	
	@Override
	public List<UserBankInfo> queryUserBankInfoByUserid(long userid) {
		return userBankInfoRepository.findUserBankInfoByUserid(userid);
	}
	
	@Override
	public List<UserBankInfo> queryUserBankInfoByUserid(long userid,String type) {
		return userBankInfoRepository.findUserBankInfoByUseridType(userid, type);
	}

	@Override
	public List<UserBankInfo> queryUserBankInfoByCardno(String cardno,String type) {		
		return userBankInfoRepository.findUserBankInfoByCardno(cardno,type);
	}
	
	@Override
	public List<UserBankInfo> queryUserBankInfoByCardnoType(String cardno,String type) {		
		return userBankInfoRepository.findUserBankInfoByCardnoType(cardno, type);
	}

	@Override
	public UserBankInfo queryUserBankInfoByLineno(String lineno) {
		return userBankInfoRepository.findUserBankInfoByLineno(lineno);
	}

	@Transactional
	@Override
	public UserBankInfo saveUserBankInfo(UserBankInfo bankInfo) {
		UserBankInfo result =  userBankInfoRepository.save(bankInfo);
		em.flush();
		return result;
	}
	
	@Transactional
	@Override
	public UserBankInfo setDefaultBank(long userid, String cardno) {
		
		List<UserBankInfo>  userBankInfos  = queryUserBankInfoByCardno(cardno,"2");
		UserBankInfo userBankInfo = null;
		for(UserBankInfo userBankInfoTemp:userBankInfos){
			if(userid == userBankInfoTemp.getUserId()){
				userBankInfo = userBankInfoTemp;
			}
		}
		userBankInfos = queryUserBankInfoByUserid(userid,"2");
		
		for(UserBankInfo  userBankInfotemp: userBankInfos){
			
			if(!userBankInfotemp.getCardNo().equalsIgnoreCase(userBankInfo.getCardNo())){
				userBankInfotemp.setIdDef("0");
			}else{
				userBankInfo = userBankInfotemp;
				
				userBankInfotemp.setIdDef("1");
			}
			
			saveUserBankInfo(userBankInfotemp);
		}
		
		
		return userBankInfo;
	}

	@Transactional
	@Override
	public void updateAllNoDefault(long userid,String type) {
		userBankInfoRepository.updateUserBankInfoByUserid(userid,type);
	}

	@Override
	public UserBankInfo queryDefUserBankInfoByUserid(long userid) {
		
		return userBankInfoRepository.queryDefUserBankInfoByUserid(userid);
		
	}
	
	@Override
	public BankNumber queryBankNumberByBankName(String bankName) {
		
		return bankNumberRepository.queryBankNumberByBankName(bankName);
		
	}
	
	@Override
	public BankAcronym queryBankAcronymByBankName(String bankName) {
		
		return bankAcronymRepository.queryBankNumberByBankName(bankName);
		
	}

	@Transactional
	@Override
	public void updateUserBankInfoByCardno(String bankBranchName, String province, String city, String lineNo,
			String securityCode, String expiredTime, String bankno) {
		userBankInfoRepository.updateUserBankInfoByCardno(bankBranchName, province, city, lineNo, securityCode, expiredTime, bankno);
		
	}

	@Override
	public List<UserBankInfo> findNatureByUserId(long userId) {
		return userBankInfoRepository.findNatureByUserIdLikeNature(userId);
	}

	@Override
	public List<UserBankInfo> findUserBankInfoByUseridAndCardno(long userId, String cardNo) {
		return userBankInfoRepository.findUserBankInfoByUseridAndCardno(userId, cardNo);
	}

	@Override
	public UserBankInfo queryBankNameByUserIdAndCardNo(long userId, String cardNo, String type) {
		
		return userBankInfoRepository.queryBankNameByUserIdAndCardNo(userId, cardNo, type);
	}
	
	//根据cardNo和brankbranchName查询用户银行信息
	@Override
	public UserBankInfo findBankNumberBybankbranchname(String bankbranchName,String cardNo) {
		return userBankInfoRepository.findBankNumberBybankbranchname(bankbranchName,cardNo);
	}

	@Override
	public int queryCountByUserIdAndCardNoAndCardType(Long userId, String bankCardNumber, String cardType) {
		if("0".equals(cardType)){
			return userBankInfoRepository.queryCountByUserIdAndCardNoAndCardType(userId, bankCardNumber,"借记");
		}else if("1".equals(cardType)){
			return userBankInfoRepository.queryCountByUserIdAndCardNoAndCardType(userId, bankCardNumber,"贷记");
		}else{
			return 0;
		}
	}


	@Transactional
	@Override
	public void updateBankCardByUserId(long userId) {
		userBankInfoRepository.UpdateStateByState(userId);
	}

	@Override
	public UserBankInfo findUserBankInfoByUserIdAndCardNoAndState(Long userId, String creditCardNumber, String state,String type) {
		return userBankInfoRepository.findUserBankInfoByUserIdAndCardNoAndStateAndType( userId,creditCardNumber,state,type);
	}

	@Override
	public UserBankInfo queryByUserIdAndCardNoAndCardType(Long userId, String bankCardNumber, String cardType) {
		if("0".equals(cardType)){
			return userBankInfoRepository.queryByUserIdAndCardNoAndCardType(userId,bankCardNumber,"借记");
		}else if("1".equals(cardType)){
			return userBankInfoRepository.queryByUserIdAndCardNoAndCardType(userId,bankCardNumber,"贷记");
		}else{
			return null;
		}
		
	}

	@Override
	public List<UserBankInfo> queryUserBankInfoByCardnoAndTypeAndUserId(String cardno, String type, long userId) {
		em.clear();
		List<UserBankInfo> result = userBankInfoRepository.findUserBankInfoByCardnoAndTypeAndUserId(cardno, type, userId);
		return result;
	}

	@Override
	public List<BankIcon> getBankIcon() {
		em.clear();
		List<BankIcon> result = bankIconRepository.getBankIcon();
		return result;
	}

	@Override
	public List<UserBankInfo> getUserBankInfoByUserIdAndTypeAndNatureAndState(long userId, String type, String nature,
			String state, String[] isDefault) {
		em.clear();
		List<UserBankInfo> result = userBankInfoRepository.getUserBankInfoByUserIdAndTypeAndNatureAndState(userId, type, nature, state, isDefault);
		return result;
	}

	@Transactional
	@Override
	public void setDefaultBankByUserIdAndBankCardAndType(long userId, String bankCard, String type) {
		
		List<UserBankInfo>  userBankInfos  = queryUserBankInfoByCardno(bankCard, type);
		UserBankInfo userBankInfo = null;
		for(UserBankInfo userBankInfoTemp:userBankInfos){
			if(userId == userBankInfoTemp.getUserId()){
				userBankInfo = userBankInfoTemp;
			}
		}
		userBankInfos = queryUserBankInfoByUserid(userId, type);
		
		for(UserBankInfo  userBankInfotemp: userBankInfos){
			
			if(!userBankInfotemp.getCardNo().equalsIgnoreCase(userBankInfo.getCardNo())){
				userBankInfotemp.setIdDef("0");
			}else{
				userBankInfo = userBankInfotemp;
				
				userBankInfotemp.setIdDef("1");
			}
			
			saveUserBankInfo(userBankInfotemp);
		}
		
	}

	@Override
	public UserBankInfo findUserBankInfoByCardno(String cardNo) {
		em.clear();
		UserBankInfo userBankInfoByCardno = userBankInfoRepository.findUserBankInfoByCardno(cardNo);
		return userBankInfoByCardno;
	}

}
