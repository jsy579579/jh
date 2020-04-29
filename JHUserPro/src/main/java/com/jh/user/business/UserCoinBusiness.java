package com.jh.user.business;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jh.user.pojo.BrandCoinConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jh.user.pojo.RankingList;
import com.jh.user.pojo.UserAccount;
import com.jh.user.pojo.UserCoinHistory;

public interface UserCoinBusiness {

	
	/**分页获取用户的积分*/
	public Page<UserCoinHistory>  queryUserCoinHistoryByUserid(long userid,  Pageable pageAble);
	
	
	public UserCoinHistory  saveUserCoinHistory(UserCoinHistory userCoinHistory);
	
	
	public UserAccount updateUserCoin(UserAccount userAccount, int coin,  String  addorsub, String ordercode);
	
	
	/***积分排行***/
	
	public List<RankingList> queryRankingListCoin(long brandid);
	
	
	/***收益排行***/
	
	public List<RankingList> queryRankingListEarnings(long brandid);
	
	/***排行添加**/
	
	public RankingList addRankingList(RankingList rankingList);
	
	/***指定查询***/
	
	public RankingList queryRankingListByid(long id);
	
	//根据brandid和status获取一定时间段内的信息
	public int findFullNameByStatus(long brandId, ArrayList<String> status, Date startTimeDate, Date endTimeDate);
	//根据brandid和status获取一定时间段内的信息
	public int findFullNameByStatus(ArrayList<String> status, Date startTimeDate, Date endTimeDate);

	UserAccount updateUserCoinNew(UserAccount userAccount, BigDecimal coin, String addorsub, String orderCode);

	List<BrandCoinConfig>findByBrand(Long brandid);

	BrandCoinConfig findByBrandAndGradeAndratio(Long brandId, int grade);

	BrandCoinConfig saveBrandCoinConfig(BrandCoinConfig brandCoinConfig);
}
