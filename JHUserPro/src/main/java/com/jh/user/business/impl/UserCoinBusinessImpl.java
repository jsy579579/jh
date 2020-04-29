package com.jh.user.business.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import com.jh.user.pojo.*;
import com.jh.user.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.user.business.UserCoinBusiness;

@Service
public class UserCoinBusinessImpl implements UserCoinBusiness{

	@Autowired
	private UserCoinRepository userCoinRepository;
	
	@Autowired
	private UserAccountRepository accountRepository;
	
	@Autowired
	private RankingListRepository rankingListRepository;
	
	@Autowired
	private EntityManager em;

	@Autowired
	private UserCoinNewRepository userCoinNewRepository;

	@Autowired
	private BrandCoinConfigRepository brandCoinConfigRepository;
	@Override
	public Page<UserCoinHistory> queryUserCoinHistoryByUserid(long userid,
			Pageable pageAble) {
		return userCoinRepository.findCoinHistoryByUserid(userid, pageAble);
	}

	@Override
	public UserCoinHistory saveUserCoinHistory(UserCoinHistory userCoinHistory) {
		return userCoinRepository.save(userCoinHistory);
	}

	@Transactional
	@Override
	public UserAccount updateUserCoin(UserAccount userAccount, int coin,  String  addorsub, String ordercode) {
		
		UserAccount olduserAccount = accountRepository.findUserAccountByUseridLock(userAccount.getUserId());
		

		int curcoin;
		if(addorsub.equalsIgnoreCase("0")){
			
			curcoin = userAccount.getCoin()+coin;
			
		}else{
			
			curcoin = userAccount.getCoin()-coin;
			
		}
		
		if(curcoin < 0 ){
			curcoin = 0;
		}
		
		olduserAccount.setCoin(curcoin);
		UserAccount newAcct = accountRepository.save(olduserAccount);	
			
		UserCoinHistory  userCoinHistory  = new UserCoinHistory();			
		userCoinHistory.setAddOrSub(addorsub);
		userCoinHistory.setOrdercode(ordercode);
		userCoinHistory.setCoin(coin);
		userCoinHistory.setCreateTime(new Date());
		userCoinHistory.setCurCoin(curcoin);
		userCoinHistory.setUserId(userAccount.getUserId());
		userCoinRepository.save(userCoinHistory);
		em.flush();
		return newAcct;
	}

	@Override
	public List<RankingList> queryRankingListCoin(long brandid) {
		
		
		return rankingListRepository.queryRankingListCoin(brandid);
	}

	@Override
	public List<RankingList> queryRankingListEarnings(long brandid) {
		
		return rankingListRepository.queryRankingListEarnings(brandid);
	}
	@Transactional
	@Override
	public RankingList addRankingList(RankingList rankingList) {
		
		return rankingListRepository.save(rankingList);
	}
	
	@Override
	public RankingList queryRankingListByid(long id) {
		
		return rankingListRepository.queryRankingListByid(id);
	}

	//根据brandid和status获取一定时间段内的信息
	@Override
	public int findFullNameByStatus(long brandId, ArrayList<String> status, Date startTimeDate, Date endTimeDate) {
		return userCoinRepository.findFullNameByStatus(brandId, status, startTimeDate, endTimeDate);
	}
	//根据brandid和status获取一定时间段内的信息
	@Override
	public int findFullNameByStatus(ArrayList<String> status, Date startTimeDate, Date endTimeDate) {
		return userCoinRepository.findFullNameByStatus(status, startTimeDate, endTimeDate);
	}

	@Transactional
	@Override
	public UserAccount updateUserCoinNew(UserAccount userAccount, BigDecimal coin, String  addorsub, String ordercode) {

		UserAccount olduserAccount = accountRepository.findUserAccountByUseridLock(userAccount.getUserId());


		BigDecimal curcoin=BigDecimal.ZERO;
		if(addorsub.equalsIgnoreCase("0")){

			curcoin = userAccount.getCoinNew().add(coin);

		}else{

			curcoin = userAccount.getCoinNew().subtract(coin);

		}

		if(curcoin.compareTo(BigDecimal.ZERO)<=0){
			curcoin=BigDecimal.ZERO;
		}

		olduserAccount.setCoinNew(curcoin);
		UserAccount newAcct = accountRepository.save(olduserAccount);

		UserCoinHistoryNew userCoinHistoryNew  = new UserCoinHistoryNew();
		userCoinHistoryNew.setAddOrSub(addorsub);
		userCoinHistoryNew.setOrdercode(ordercode);
		userCoinHistoryNew.setCoin(coin);
		userCoinHistoryNew.setCreateTime(new Date());
		userCoinHistoryNew.setCurCoin(curcoin);
		userCoinHistoryNew.setUserId(userAccount.getUserId());
		userCoinNewRepository.save(userCoinHistoryNew);
		em.flush();
		return newAcct;
	}

	@Override
	public 	List<BrandCoinConfig>findByBrand(Long brandId) {
		return brandCoinConfigRepository.findByBrand(brandId);
	}

	@Override
	public BrandCoinConfig findByBrandAndGradeAndratio(Long brandId, int grade) {
		return brandCoinConfigRepository.findByBrandAndGradeAndratio(brandId,grade);
	}

	@Transactional
	@Override
	public BrandCoinConfig saveBrandCoinConfig(BrandCoinConfig brandCoinConfig) {
		em.clear();
		BrandCoinConfig result=brandCoinConfigRepository.save(brandCoinConfig);
		em.flush();
		return result;
	}

}
