package cn.jh.clearing.business;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import cn.jh.clearing.pojo.CoinRecord;

public interface CoinRecordBusiness {

	Page<CoinRecord> findCoinRecordByUserid(long userid,Pageable pageAble);
	
	CoinRecord mergeCoinRecord(CoinRecord coinRecord);
	
	Page<CoinRecord> findCoinRecordByUserid(String userid, String grade, Date startTime,  Date endTime, Pageable pageAble);
	
	Page<CoinRecord> findCoinRecordByBrandid(String userid,long brandid,String grade, Date startTime,  Date endTime, Pageable pageAble);
	
	
	
	
}
