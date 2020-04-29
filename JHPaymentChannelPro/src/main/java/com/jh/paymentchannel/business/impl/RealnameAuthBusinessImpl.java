package com.jh.paymentchannel.business.impl;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.paymentchannel.business.RealnameAuthBusiness;
import com.jh.paymentchannel.config.PropertiesConfig;
import com.jh.paymentchannel.pojo.RealNameAuth;
import com.jh.paymentchannel.pojo.RealNameAuthRoute;
import com.jh.paymentchannel.repository.RealnameAuthRepository;
import com.jh.paymentchannel.repository.RealnameAuthRouteRepository;
import com.jh.paymentchannel.util.PaymentChannelConstants;

import net.sf.json.JSONObject;

@Service
public class RealnameAuthBusinessImpl implements RealnameAuthBusiness{
	
	@Autowired
    private RealnameAuthRepository realNameAuthrepository;
	
	@Autowired
    private RealnameAuthRouteRepository realNameAuthRouterepository;
	
	@Autowired
	private EntityManager em;
	
	@Autowired
	private PropertiesConfig propertiesConfig;
	
	@Override
	public List<RealNameAuth> findAllRealnames(Pageable pageable) {
		Page<RealNameAuth> page =  realNameAuthrepository.findAll(pageable);
		return page.getContent();
	}
	
	@Transactional
	@Override
	public RealNameAuth realNameAuth(String realname, String idcard, long userid) {
		
		RealNameAuthRoute realNameAuthRoute = realNameAuthRouterepository.getCurActiveRealNameAuthChannel();
		
		String realnameChannel = realNameAuthRoute.getCurChannel();
		String result = "";
		if(realnameChannel.equalsIgnoreCase(PaymentChannelConstants.REALNAME_CHANNEL_1)){
			result = JuHeAPIRealnameAuthService.realNameAuth(idcard, realname,propertiesConfig.getRealNameUrl(),propertiesConfig.getRealNameKey());
		}
				
		/**如果userid已经存在那么直接更新*/
		List<RealNameAuth>  realnameList = realNameAuthrepository.findRealNameByUserId(userid);
		
		RealNameAuth realNameAuth = null;
		
		if(realnameList.size()>0){
			realNameAuth=realnameList.get(0);
		}else{
			realNameAuth= null;
		}
		
		if(realNameAuth != null ){
			realNameAuth.setRealname(realname);
			realNameAuth.setIdcard(idcard);
			realNameAuth.setAuthTime(new Date());
			
		}else{
			
			realNameAuth = new RealNameAuth();
			realNameAuth.setAuthTime(new Date());
			realNameAuth.setIdcard(idcard);
			realNameAuth.setRealname(realname);
			realNameAuth.setUserId(userid);
			
		}
		
		JSONObject jsonObject =  JSONObject.fromObject(result);
		JSONObject resObject  =  jsonObject.getJSONObject("result");
		if(resObject == null || resObject.isNullObject()){
			resObject = new JSONObject();
		}
		if(resObject.has("realname")){
			realNameAuth.setResult(resObject.getString("res"));
			if(resObject.getString("res").equalsIgnoreCase("1")){
				realNameAuth.setMessage("匹配");
			}else{
				realNameAuth.setMessage("不匹配");
			}
		}else{
			realNameAuth.setResult("2");
			realNameAuth.setMessage("不匹配");
		}
		
		RealNameAuth tempResult =  realNameAuthrepository.save(realNameAuth);
		
		em.flush();
		return tempResult;
	}


	@Override
	public RealNameAuth findRealNamesByIdcard(String idcard) {		
		return realNameAuthrepository.findRealNameByIdcard(idcard);
	}

	@Override
	public RealNameAuth findRealNamesByall(long userid,String idcard,String realname) {		
		return realNameAuthrepository.findRealNamesByall(userid,idcard,realname);
	}

	@Override
	public List<RealNameAuth> findRealNamesAuthByName(String realname) {	
		return realNameAuthrepository.findRealNameByName(realname);
	}
	
	@Transactional
	@Override
	public RealNameAuth findRealNamesAuthByUsserId(long userid){
		List<RealNameAuth> realnameList=realNameAuthrepository.findRealNameByUserId(userid);
		if(realnameList.size()>0){
			return realnameList.get(0);
		}
		return null;
	}

	@Override
	public List<RealNameAuth> findRealNamesAuthByUserIds(long[] userIds) {
		return realNameAuthrepository.findRealNamesAuthByUserIds(userIds);
	}

	@Override
	public RealNameAuth findRealNamesAuthByUserId(long userId) {
		return realNameAuthrepository.findByUserId(userId);
	}

	@Override
	public void addRealnameAuth(RealNameAuth realnameAuth) {
		 realNameAuthrepository.save(realnameAuth);
		 em.clear();
		
	}

	@Transactional
	@Override
	public void updateRealnameMsg(long userId, String result) {
		   realNameAuthrepository.updateResultById(userId, result);
	}

	@Override
	public RealNameAuth findRealnameAuthById(long userId) {
		RealNameAuth realnameAuth = realNameAuthrepository.findRealnameAuthById(userId);
		return realnameAuth;
	}

	@Override
	public List<RealNameAuth> findRealNamesAuthByUserIdsAndDate(long[] suserIds, String date) {
		return realNameAuthrepository.findRealNameByUserIdsAndCreateTime(suserIds,date);
	}

	@Override
	public void delRealnameByUserid(long userid) {
		
		
	}

    @Override
    public List<RealNameAuth> findRealNamesAuthByUserIdsAndDateNew(Long[] userIds, String status, String date) {
        return realNameAuthrepository.findRealNameByUserIdsAndCreateTimeNew(userIds,status,date);
    }

    @Override
    public Integer findRealNameCountsByUserIds(Long[] userIds, String status) {
        return realNameAuthrepository.findRealNameCountsByUserIds(userIds,status);
    }

}
