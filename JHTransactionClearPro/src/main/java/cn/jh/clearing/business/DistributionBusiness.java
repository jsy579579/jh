package cn.jh.clearing.business;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import cn.jh.clearing.pojo.DistributionRecord;

public interface DistributionBusiness {
	
	Page<DistributionRecord> findDistributionRecordByordercode(String order ,Date strTime,Date endTime,Pageable page);
	
	Page<DistributionRecord> findAllDistributionByPhone(String acqphone ,Date strTime,Date endTime,Pageable page);
	
	Page<DistributionRecord> findAllDistributionByoriPhone(String oriphone,String[] order,Date strTime,Date endTime,Pageable page);
	
	Page<DistributionRecord> findAllDistributionByPhoneAndOrder( String oriphone,  String order,Date strTime,Date endTime,Pageable page);
	
	Page<DistributionRecord> findAllDistributionByPhoneAndacqOrder( String acqphone,  String order,Date strTime,Date endTime,Pageable page);
	
	Page<DistributionRecord> findAllDistributionByPhoneAndoriOrder( String acqphone,  String oriphone,Date strTime,Date endTime,Pageable page);
	
	Page<DistributionRecord> findByAllParams( String acqphone,  String oriphone,String order,Date strTime,Date endTime,Pageable page);
	
	public BigDecimal queryDistributionSumAcqAmountByPhone(String phone,String startTime, String endTime);
	
	public List<Object> getDistributionRecordByAcqUserId(long userId, Date strTime);
	
	public BigDecimal getSumDistributionRecordByDate(long userId, String startTime, String endTime);
	
	public Map getDistributionRecordByUserIdAndDate(long userId, String startTime, String endTime, Pageable pageable);

	DistributionRecord addDistribution(DistributionRecord dbr);
}
