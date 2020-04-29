package cn.jh.risk.business;

import java.util.Date;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import cn.jh.risk.pojo.BlackWhiteList;

public interface BlackWhiteListBusiness {

	
	public BlackWhiteList merge(BlackWhiteList  blackWhiteList);
	

	public BlackWhiteList findBlackWhiteByPhone(String phone, String operationType);
	
	public BlackWhiteList findBlackWhiteByUserId(long userId , String operationType); 
	
	
	public void delBlackWhiteAndOperation(String phone,  String operationtype);
	
	public Page<BlackWhiteList> findBlackWhiteList(String phone,  String brandid, Date startTime,  Date endTime, Pageable pageAble);
	
	
}
