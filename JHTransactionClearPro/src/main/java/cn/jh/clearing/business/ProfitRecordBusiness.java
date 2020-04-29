package cn.jh.clearing.business;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import cn.jh.clearing.pojo.DistributionRecord;
import cn.jh.clearing.pojo.DistributionRecordCopy;
import cn.jh.clearing.pojo.ProfitRecord;
import cn.jh.clearing.pojo.ProfitRecordCopy;

public interface ProfitRecordBusiness {

	
	Page<ProfitRecord> findProfitByUserid(long userid,Pageable pageAble);
	
	Page<ProfitRecord> findProfitByUserid(String userid, String grade ,Date startTime,  Date endTime, Pageable pageAble);
	
	public Page<ProfitRecord> findProfitByBrandId(long brandid,String userid,  String grade,Date startTime,
			Date endTime, Pageable pageAble);
	
	public  ProfitRecord merge(ProfitRecord profitRecord);
	
	public  BigDecimal findsumProfitRecord(long acquserid);
	
	public  DistributionRecord merge(DistributionRecord profitRecord);
	
	Page<DistributionRecord> findDistributionRecordByUserid(long userid,Date startTime,  Date endTime, Pageable pageAble);
	
	Page<DistributionRecord> findDistributionRecordByPlatform(long brandid,String userid,String order_code,Date startTime,  Date endTime, Pageable pageAble);
	
	
	public  ProfitRecord  queryProfitRecordByordercode(String ordercode,  String type);
	
	//获取当前phone的数据库记录(oriphone为需要查询的手机号)
	public List<ProfitRecord> queryProfitByOriPhone(String oriphone, String ordercode);
	//依据ordercode获取分润表中的订单信息
	public List<ProfitRecord> queryProfitAmount(String ordercode);
	
	public Page<ProfitRecord> queryProfitAmountByPhone(String phone,String[] order,String[] type,Date startTime, Date endTime, Pageable pageable);
	
	public Page<ProfitRecord> queryProfitAmountByGetPhone( String phone,String[] type,Date startTime, Date endTime, Pageable pageable);
	
	public Object queryProfitAll( String phone,String[] type,Date startTime, Date endTime, Pageable pageable);
	//依据phone获取用户所有返佣信息
	public List<DistributionRecord> findAllDistributionByPhone(String acqphone,Date startTime, Date endTime, Pageable pageable);

	Page<ProfitRecord> findProfitByUserid(String orderCode, Date startTimeDate, Date endTimeDate,
			Pageable pageable);
	
	public Page<ProfitRecord> finByManyParams(String orderCode,String[] type, Date startTimeDate, Date endTimeDate,
			Pageable pageable);

	public Page<ProfitRecord> queryProfitAmountByDoublePhone(String phone,String getphone,String[] type,Date startTime, Date endTime, Pageable pageable);
	
	public Page<ProfitRecord> queryByAllParams(String phone,String getphone,String order,String[] type,Date startTime, Date endTime, Pageable pageable);
	
	List<Object[]> findsumProfitRecordByAcqUserIds(long[] acqUserIds);
	
	Page<ProfitRecord> queryProfitAmountByOderGetPhone(String getphone,String order,String[] type,Date startTime, Date endTime, Pageable pageable);
	Page<ProfitRecord> queryProfitAmountByOderPhone(String phone,String order,String[] type,Date startTime, Date endTime, Pageable pageable);
	void createNewProfitRecord(BigDecimal rebate, String preUserPhone, BigDecimal rate, Long preUserId,
			BigDecimal amount, String ordercode, Long firstUserId, String firstUserPhone, BigDecimal currate,
			String type, BigDecimal scale, String description, String brandId, long level, String firstUserName, String preUserName);
	
	public BigDecimal queryProfitRecordSumAcqAmountByPhone(String phone,String startTime, String endTime);
	
	public List<Object> getProfitRecordByAcqUserId(long userId, Date startTime);
	
	public BigDecimal getSumProfitRecordByDate(long userId, String startTime, String endTime);
	
	public Map getProfitRecordByUserIdAndDate(long userId, String startTime, String endTime, Pageable pageable);
	
	void createProfitRecordCopy(BigDecimal rebate, String preUserPhone, BigDecimal rate, Long preUserId,
			BigDecimal amount, String ordercode, Long firstUserId, String firstUserPhone, BigDecimal currate,
			String type, BigDecimal scale, String description, String brandId, long level, String firstUserName, String preUserName);
	
	public  DistributionRecordCopy mergeCopy(DistributionRecordCopy profitRecord);
	
	public Page<ProfitRecordCopy> getProfitRecordCopyByOrderCode(String orderCode, Pageable pageAble);
	
	public Page<DistributionRecordCopy> getDistributionRecordCopyByOrderCode(String orderCode, Pageable pageAble);


	Page<ProfitRecord> findProfitByBrandIdAndTime(String brandId, Date createTime, Date endTime, Pageable pageAble);

	Page<ProfitRecord> findBrandProfitByBrandId(String brandId, Pageable pageAble);

	Object queryProfitAllByBrandId(String brandId);

	Object queryProfitAllByBrandIdAndTime(String brandId, Date startDate, Date endDate);

	cn.jh.clearing.util.Page<Object[]> listProfitByUserId(long userId,int start,int size);

	int countByUserId(long userId);

    Page<ProfitRecord> findBrandProfitByBrandIdAndType(String brandId, String type, Pageable pageAble);

	Object queryProfitAllByBrandIdAndType(String brandId, String type);

	Page<ProfitRecord> findBrandProfitByBrandIdAndTypeAndTime(String brandId, String type, Date startDate, Date endDate, Pageable pageAble);

	Object queryProfitAllByBrandIdAndTypeAndTime(String brandId, String type, Date startDate, Date endDate);

	List queryRebateType();
}
