package cn.jh.clearing.business;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import cn.jh.clearing.pojo.BrandProfit;
import cn.jh.clearing.pojo.BrandWithdrawRebate;
import cn.jh.clearing.pojo.PaymentOrder;
import cn.jh.clearing.pojo.PaymentOrderNumber;

public interface PaymentOrderBusiness {
	
	public List<PaymentOrder> findPaymentOrderByTimeAndPhone(String phone,Date strDate,Date endDate);
	
	public Page<PaymentOrder> queryPaymentOrderByUserid(long userid, String type,Date StartTimeDate,Date endTimeDate, 
			Pageable pageAble); 
	
	public List<PaymentOrder> queryWaitClearingOrders();
	
	
	public List<PaymentOrder> queryWeekBrandWithdrawRebate(Date startDate,  Date endDate);
	
	public List<PaymentOrder> queryWeekBrandWithdrawRebate(Date startDate,  Date endDate ,long brandid);

	
	public List<PaymentOrder> queryWeekBrandWithdrawClearRebate(Date startDate, Date endDate);
	
	public List<PaymentOrder> queryWeekBrandWithdrawClearRebate(Date startDate, Date endDate,long brandid);

	
	public Page<PaymentOrder> queryAllPaymentOrder(String userid, String type,String[] status, Date startTime, Date endTime, 
			Pageable pageAble); 
	
	public Page<PaymentOrder> queryAllPaymentOrderByBrand(String userid,long brandid, String type,String[] status, Date startTime, Date endTime,Pageable pageAble); 
	
	public PaymentOrder  mergePaymentOrder(PaymentOrder order);
	
	public PaymentOrder  queryPaymentOderByOutOrdercode(String outordercode);
	
	public PaymentOrder  queryPaymentOrderByThirdcode(String thirdcode);
	
	public void updateThirdcodeByOrdercode(String thirdcode,  String ordercode);
	
	
	public void updateAutoClearingByOrdercode(String ordercode, String autoclearing);
	
	public PaymentOrder  queryPaymentOrderBycode(String ordercode);
	
	public PaymentOrder  queryPaymentOrderBycodeAndStatus(String ordercode, String status);
	
	public BigDecimal findsumPaymentOrder(long userid,String[] type  ,String[] status ,String autoClearing);
	
	public List<PaymentOrder> findsumPaymentOrderByDescCode(long userid,String[] type  ,String[] status ,String desccode,Date startTimeDate,Date endTimeDate);
	
	public BigDecimal findsumPaymentOrder(long userid,String[] type  ,String[] status ,String autoClearing ,Date startTimeDate,Date endTimeDate);
	
	public BigDecimal findsumPaymentOrderAmount(long userid,String[] type  ,String[] status ,String autoClearing ,Date startTimeDate,Date endTimeDate);
	
	public int findsumPaymentOrderCount(long userid,String[] type  ,String[] status ,String autoClearing ,Date startTimeDate,Date endTimeDate);
	
	public BigDecimal findsumPaymentOrderBrand(long brand,String[] type  ,String[] status ,String[] autoClearing ,Date startTimeDate,Date endTimeDate);
	
	public int findsumPaymentOrderBrandCount(long brand,String[] type  ,String[] status ,String[] autoClearing ,Date startTimeDate,Date endTimeDate);
	
	public BigDecimal findsumPaymentOrderPlatform(String[] type  ,String[] status ,String[] autoClearing ,Date startTimeDate,Date endTimeDate);
	
	public int findsumPaymentOrderPlatformCount(String[] type  ,String[] status ,String[] autoClearing ,Date startTimeDate,Date endTimeDate);
	
	
	//查询用公众号支付商铺扫码的订单
	public Page<PaymentOrder> queryPaymentOrder(String userid, String brandid, String ordercode, String[] status, String thirdOrdercode, String[] channelTag, Pageable pageAble);
	
	
	//根据多个条件查询订单信息
	public Map queryPaymentOrderAll(String startTime, String endTime, String phone, String ordercode, String ordertype, String orderstatus, long brandid, String bankCard, String userName, String bankName, String debitBankName, Pageable pageAble);
	
	
	//根据手机号和brand_id查询订单号
	public Map queryOrdercodeByPhone(String phone, long brandid);
	
	//修改订单号
	public PaymentOrder updatePaymentOrder(String orderCode, String orderNo);
	
	//根据phone和brandid查询用户所有订单号
	public List<PaymentOrder> findOrderByphoneAndbrandid(String phone,long brandid, Date startTimeDate, Date endTimeDate, Pageable pageable);

	public List<Long> queryUserIdsByAmount(Long brandId, BigDecimal limitAmount, Long autoRebateConfigId);
	
	//调用此方法获取待结算订单
	public List<PaymentOrder> findOrderByUseridAndStatus(long userid, String[] status);

//	批量查询总金额
	public List<Object[]> findSumByUserIds(long[] userIds, String type, String status, String autoClearing);

	
	//将订单失败的信息存入数据库
	public void addErrorByOrderCode(String ordercode,String remark);
	
	/**
	 * 成功率
	 * ***/
	public  List<PaymentOrderNumber>  findOrderSuccessRate(long brandId,Date StartTimeDate, Date endTimeDate);

	public PaymentOrder findByThirdOrdercode(String order_code);
	
	public void deletePaymentOrderByOrderCode(String ordercode);
	
	public List<PaymentOrder> findOrderByTimeAndChannelTagAndStatus(String startTimeDate, String endTimeDate, String[] channelTag, String orderStatus, String remark);
	
	public List<PaymentOrder> findOrderByTimeAndChannelTagAndStatusAndRemark(String startTimeDate, String endTimeDate, String[] channelTag, String orderStatus);
	
	public Object queryPaymentOrderSumAmountByUserId(long[] userId, String startTime, String endTime);

	public BigDecimal queryPaymentOrderSumAmountByUserIds(long[] userId, String[] type, Date startTime, Date endTime);
	
	public PaymentOrder findByOutMerOrdercode(String outOrderCode); 
	
	public List<String> findYBpayOrder(String startTimeDate, String endTimeDate, String channelTag, String[] orderStatus);
	
	public BigDecimal getEveryDayMaxLimit(String startTimeDate, String endTimeDate, String channelTag, String orderStatus, long userId);
	
	public BigDecimal getCalculationProfit(String channelTag, String repaymentOrQuick, String rate, String extraFee, String startTimeDate, String endTimeDate);
	
	public List<PaymentOrder> getPaymentOrderByUserIdAndChannelTagAndStatus(long userId, String channelTag, String orderStatus,  String startTime, String endTime);
	
	public Map getTransactionByChannelTagAndTypeAndBrandIdAndDate(String channelTag, BigDecimal brandMinRate, BigDecimal brandExtraFee, BigDecimal costRate, BigDecimal costExtraFee, String type, int isBankRate, String brandId, String startTime, String endTime);
	
	public void createBrandProfit(BrandProfit brandProfit);
	
	public Map<String, Object> getBrandProfitByBrandId(long brandId, Pageable pageAble);
	
	public Map<String, Object> getBrandProfitByTradeTime(String tradeTime, Pageable pageAble);
	
	public Page<BrandProfit> getBrandProfitByBrandIdAndTradeTime(long brandId, String tradeTime, Pageable pageAble);
	
	public Map<String, Object> getSumProfitByStartTimeAndEndTimeAndBrandIdAndChannelTag(String startTime, String endTime, long brandId, String channelTag);
	
	//public List<String> getPaymentOrderByStartTimeAndEndTime(String[] orderType, String[] channelTag,String startTime, String endTime);

	public List<PaymentOrder> findOrderByUpdateTimeTimeAndChannelTagAndStatus(String startTimeDate, String endTimeDate, String[] channelTag, String orderStatus, String remark);
	public Page<PaymentOrder> queryPaymentOrderByUserIdsAndMore(String[] userId, String type, String status, Date startTime, Date endTime, Pageable pageAble); 
	
	// 用户下级分润统计
	public Map<String, Object>  findsumPaymentOrderByUserIdAndLevel(long userid,String[] type ,String[] status,String autoClearing, String level,Date startTimeDate,Date endTimeDate);
	
	public Map<String, Object>  findsumPaymentOrderAmountByUserIdAndLevel(long userid,String[] type ,String[] status, String autoClearing, String level,Date startTimeDate,Date endTimeDate);
	
}
