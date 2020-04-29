package com.jh.paymentchannel.business;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.jh.paymentchannel.pojo.RealNameAuth;

public interface RealnameAuthBusiness {

	
	public RealNameAuth  realNameAuth(String realname, String idcard, long userid);
	
	public RealNameAuth findRealNamesByall(long userid,String idcard,String realname);
	
	public List<RealNameAuth> findAllRealnames(Pageable pageable);
	
	
	public RealNameAuth  findRealNamesByIdcard(String idcard);
	
	public List<RealNameAuth> findRealNamesAuthByName(String realname);
	
	public RealNameAuth findRealNamesAuthByUsserId(long userid);
	
	//用户注销实名信息
	public void delRealnameByUserid(long userid);

	public List<RealNameAuth> findRealNamesAuthByUserIds(long[] userIds);
	
	public RealNameAuth findRealNamesAuthByUserId(long userId);
	
	public void addRealnameAuth(RealNameAuth realnameAuth);
	
	public void updateRealnameMsg(long userId,String result);
	
	public RealNameAuth findRealnameAuthById(long userId);

    List<RealNameAuth> findRealNamesAuthByUserIdsAndDate(long[] suserIds, String date);

    List<RealNameAuth> findRealNamesAuthByUserIdsAndDateNew(Long[] userIds, String status, String date);

    Integer findRealNameCountsByUserIds(Long[] userIds, String status);

}
