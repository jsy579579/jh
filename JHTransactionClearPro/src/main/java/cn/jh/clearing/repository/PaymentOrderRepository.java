package cn.jh.clearing.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.jh.clearing.pojo.PaymentOrder;

@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder,String>,JpaSpecificationExecutor<PaymentOrder>{
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.phone=:phone and paymentOrder.createTime >=:starttime and paymentOrder.createTime <:endtime")
	List<PaymentOrder> findPaymentOrderByTimeAndPhone(@Param("phone") String phone,@Param("starttime") Date startTime,@Param("endtime") Date endtime);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid=:userid and paymentOrder.type in :type")
	Page<PaymentOrder> findPaymentOrderByUserid(@Param("userid") long userid, @Param("type") String[] type, Pageable pageAble);

	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid=:userid and paymentOrder.type=:type  and paymentOrder.brandid=:brandid")
	Page<PaymentOrder> findPaymentOrderByUseridbrandid(@Param("userid") long userid, @Param("type") String type, @Param("brandid") long brandid, Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid in (:userid) and paymentOrder.type=:type")
	Page<PaymentOrder> findPaymentOrderByUserid(@Param("userid") Long[] userid, @Param("type") String type, Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.brandid=:brandid and paymentOrder.type=:type")
	Page<PaymentOrder> findPaymentOrderBybrandid(@Param("brandid") Long brandid, @Param("type") String type, Pageable pageAble);
	
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.type in :type")
	Page<PaymentOrder> findPaymentOrderByUserid( @Param("type") String[] type, Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid=:userid and paymentOrder.type in :type and paymentOrder.createTime >=:starttime")
	Page<PaymentOrder> findPaymentOrderByUseridStart(@Param("userid") long userid, @Param("type") String[] type, @Param("starttime") Date startTime, Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid=:userid and paymentOrder.type=:type and paymentOrder.createTime >=:starttime  and paymentOrder.brandid=:brandid")
	Page<PaymentOrder> findPaymentOrderByUseridStartbrandid(@Param("userid") long userid, @Param("type") String type, @Param("starttime") Date startTime,  @Param("brandid") long brandid,Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid in (:userid) and paymentOrder.type=:type and paymentOrder.createTime >=:starttime")
	Page<PaymentOrder> findPaymentOrderByUseridStart(@Param("userid") Long[] userid, @Param("type") String type, @Param("starttime") Date startTime, Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.brandid=:brandid and paymentOrder.type=:type and paymentOrder.createTime >=:starttime")
	Page<PaymentOrder> findPaymentOrderBybrandidStart(@Param("brandid") long brandid, @Param("type") String type, @Param("starttime") Date startTime, Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.type in :type and paymentOrder.createTime >=:starttime")
	Page<PaymentOrder> findPaymentOrderByUseridStart( @Param("type") String[] type, @Param("starttime") Date startTime, Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid=:userid and paymentOrder.type in :type and paymentOrder.createTime <:endtime")
	Page<PaymentOrder> findPaymentOrderByUseridEnd(@Param("userid") long userid, @Param("type") String[] type, @Param("endtime") Date endtime, Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid=:userid and paymentOrder.type=:type and paymentOrder.createTime <:endtime and paymentOrder.brandid=:brandid")
	Page<PaymentOrder> findPaymentOrderByUseridEndbrandid(@Param("userid") long userid, @Param("type") String type, @Param("endtime") Date endtime, @Param("brandid") long brandid, Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid in (:userid) and paymentOrder.type=:type and paymentOrder.createTime <:endtime")
	Page<PaymentOrder> findPaymentOrderByUseridEnd(@Param("userid") Long[] userid, @Param("type") String type, @Param("endtime") Date endtime, Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.brandid=:brandid and paymentOrder.type=:type and paymentOrder.createTime <:endtime")
	Page<PaymentOrder> findPaymentOrderBybrandidEnd(@Param("brandid") long brandid, @Param("type") String type, @Param("endtime") Date endtime, Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.type in :type and paymentOrder.createTime <:endtime")
	Page<PaymentOrder> findPaymentOrderByUseridEnd(@Param("type") String[] type, @Param("endtime") Date endtime, Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid=:userid and paymentOrder.type in :type and paymentOrder.createTime >=:starttime  and paymentOrder.createTime <:endtime")
	Page<PaymentOrder> findPaymentOrderByUserid(@Param("userid") long userid, @Param("type") String[] type, @Param("starttime") Date startTime,  @Param("endtime") Date endTime, Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid=:userid and paymentOrder.type=:type and paymentOrder.createTime >=:starttime  and paymentOrder.createTime <:endtime  and paymentOrder.brandid=:brandid")
	Page<PaymentOrder> findPaymentOrderByUbrandid(@Param("userid") long userid, @Param("type") String type, @Param("starttime") Date startTime,  @Param("endtime") Date endTime, @Param("brandid") long brandid, Pageable pageAble);
	
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid  in (:userid)  and paymentOrder.type=:type and paymentOrder.createTime >=:starttime  and paymentOrder.createTime <:endtime")
	Page<PaymentOrder> findPaymentOrderByUserid(@Param("userid") Long[] userid, @Param("type") String type, @Param("starttime") Date startTime,  @Param("endtime") Date endTime, Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.brandid=:brandid  and paymentOrder.type=:type and paymentOrder.createTime >=:starttime  and paymentOrder.createTime <:endtime")
	Page<PaymentOrder> findPaymentOrderBybrandid(@Param("brandid") Long brandid, @Param("type") String type, @Param("starttime") Date startTime,  @Param("endtime") Date endTime, Pageable pageAble);
	
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where  paymentOrder.type in :type and paymentOrder.createTime >=:starttime  and paymentOrder.createTime <:endtime")
	Page<PaymentOrder> findPaymentOrderByUserid(@Param("type") String[] type, @Param("starttime") Date startTime,  @Param("endtime") Date endTime, Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.ordercode=:ordercode")
	PaymentOrder findPaymentOrderByCode(@Param("ordercode") String ordercode);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.outMerOrdercode=:outMerOrdercode")
	PaymentOrder findPaymentOrderByOutCode(@Param("outMerOrdercode") String ordercode);
	
	
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.thirdOrdercode=:thirdcode")
	PaymentOrder findPaymentOrderByThirdCode(@Param("thirdcode") String thirdcode);
	
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.type = '2' and paymentOrder.status = '3' and paymentOrder.channelTag = 'YILIAN'")
	List<PaymentOrder> findWaitWithdrawOrder();
	
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.status = '4'")
	List<PaymentOrder> findWaitCleaingOrder();
	
	
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.ordercode=:ordercode and paymentOrder.status =:status")
	PaymentOrder findPaymentOrderByCodeAndStatus(@Param("ordercode") String ordercode, @Param("status") String status);
	
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid=:userid and paymentOrder.createTime >=:starttime and paymentOrder.createTime <:endtime")
	Page<PaymentOrder> findAllPaymentOrder(@Param("userid") long userid, @Param("starttime") Date startTime,  @Param("endtime") Date endTime, Pageable pageAble);

	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid=:userid and paymentOrder.createTime >=:starttime and paymentOrder.createTime <:endtime and paymentOrder.brandid=:brandid")
	Page<PaymentOrder> findAllPaymentOrderbrandid(@Param("userid") long userid, @Param("starttime") Date startTime,  @Param("endtime") Date endTime, @Param("brandid") long brandid, Pageable pageAble);
	

	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid=:userid and  paymentOrder.type in :type and paymentOrder.status in (:status)")
	Page<PaymentOrder> findAllPaymentOrderByts(@Param("userid") Long uids, @Param("type") String[] type,  @Param("status") String[] status, Pageable pageAble);
	
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid=:userid and  paymentOrder.type =:type and paymentOrder.status in (:status) and paymentOrder.brandid=:brandid")
	Page<PaymentOrder> findAllPaymentOrderBytsbrandid(@Param("userid") Long uids, @Param("type") String type,  @Param("status") String[] status,  @Param("brandid") long brandid, Pageable pageAble);
	
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.brandid =:brandid and  paymentOrder.type =:type and paymentOrder.status in (:status)")
	Page<PaymentOrder> findAllPaymentOrderBytsbrandid(@Param("brandid") Long brandid, @Param("type") String type,  @Param("status") String[] status, Pageable pageAble);

	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.brandid =:brandid and  paymentOrder.type =:type and paymentOrder.status in (:status) and paymentOrder.createTime >=:starttime and paymentOrder.createTime <:endtime")
	Page<PaymentOrder> findAllPaymentOrderBytsbrandid(@Param("brandid") Long brandid, @Param("type") String type,  @Param("status") String[] status, @Param("starttime") Date startTime,  @Param("endtime") Date endTime, Pageable pageAble);

	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.brandid =:brandid and  paymentOrder.type =:type and paymentOrder.status in (:status) and paymentOrder.createTime >=:starttime ")
	Page<PaymentOrder> findAllPaymentOrderBytsbrandid(@Param("brandid") Long brandid, @Param("type") String type,  @Param("status") String[] status, @Param("starttime") Date startTime, Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.brandid =:brandid and  paymentOrder.type =:type and paymentOrder.status in (:status) and paymentOrder.createTime <:endtime")
	Page<PaymentOrder> findAllPaymentOrderBytsbrandidend(@Param("brandid") long brandid, @Param("type") String type,  @Param("status") String[] status, @Param("endtime") Date endtime, Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid in (:userid) and  paymentOrder.type =:type and paymentOrder.status in (:status)")
	Page<PaymentOrder> findAllPaymentOrderByts(@Param("userid") Long[] userid, @Param("type") String type,  @Param("status") String[] status, Pageable pageAble);
	
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where  paymentOrder.type in :type and paymentOrder.status in (:status)")
	Page<PaymentOrder> findAllPaymentOrderByts( @Param("type") String[] type,  @Param("status") String[] status, Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid in (:userid) and paymentOrder.createTime >=:starttime and paymentOrder.createTime <:endtime")
	Page<PaymentOrder> findAllPaymentOrder(@Param("userid") Long[] userid, @Param("starttime") Date startTime,  @Param("endtime") Date endTime, Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.brandid=:brandid and paymentOrder.createTime >=:starttime and paymentOrder.createTime <:endtime")
	Page<PaymentOrder> findAllPaymentOrderbrandid(@Param("brandid") long brandid, @Param("starttime") Date startTime,  @Param("endtime") Date endTime, Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where  paymentOrder.createTime >=:starttime and paymentOrder.createTime <:endtime")
	Page<PaymentOrder> findAllPaymentOrderByNoUserid( @Param("starttime") Date startTime,  @Param("endtime") Date endTime, Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid =:userid and paymentOrder.createTime >=:starttime")
	Page<PaymentOrder> findAllPaymentOrder(@Param("userid") long userid, @Param("starttime") Date startTime,  Pageable pageAble);
	

	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid =:userid and paymentOrder.createTime >=:starttime and paymentOrder.brandid=:brandid")
	Page<PaymentOrder> findAllPaymentOrderbrandid(@Param("userid") long userid, @Param("starttime") Date startTime,  @Param("brandid") long brandid, Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid =:userid and paymentOrder.createTime <=:endtime")
	Page<PaymentOrder> findAllPaymentOrderByEndTime(@Param("userid") long userid, @Param("endtime") Date endTime,  Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.createTime <=:endtime")
	Page<PaymentOrder> findAllPaymentOrderByEndTime( @Param("endtime") Date endTime,  Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid in (:userid) and paymentOrder.createTime >=:starttime")
	Page<PaymentOrder> findAllPaymentOrder(@Param("userid") Long[] userid, @Param("starttime") Date startTime,  Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.brandid=:brandid and paymentOrder.createTime >=:starttime")
	Page<PaymentOrder> findAllPaymentOrderbrandid(@Param("brandid") long brandid, @Param("starttime") Date startTime,  Pageable pageAble);
	
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid in (:userid) and paymentOrder.createTime <:endtime")
	Page<PaymentOrder> findAllPaymentOrderByEndTime(@Param("userid") Long[] userid, @Param("endtime") Date endTime,  Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.brandid=:brandid and paymentOrder.createTime <:endtime")
	Page<PaymentOrder> findAllPaymentOrderBybrandidEndTime(@Param("brandid") long brandid, @Param("endtime") Date endTime,  Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where  paymentOrder.createTime >=:starttime")
	Page<PaymentOrder> findAllPaymentOrderByNoUserid( @Param("starttime") Date startTime,  Pageable pageAble);
	
	@Modifying
	@Query("update PaymentOrder paymentOrder set paymentOrder.thirdOrdercode=:thirdOrdercode where paymentOrder.ordercode=:ordercode")
	void updatePaymentThirdcodeByOrdercode(@Param("thirdOrdercode") String thirdOrdercode ,@Param("ordercode") String ordercode);

	@Modifying
	@Query("update PaymentOrder paymentOrder set paymentOrder.autoClearing=:autoclearing where paymentOrder.ordercode=:ordercode")
	void updateAutoClearingByOrdercode(@Param("autoclearing") String autoclearing, @Param("ordercode") String ordercode);

	@Query(value = "select paymentOrder from PaymentOrder paymentOrder where (paymentOrder.type  = '2' and paymentOrder.channelTag != 'JIEFUBAO')  and paymentOrder.status = '1' and paymentOrder.createTime >= :pridate and paymentOrder.createTime < :curdate")
	List<PaymentOrder>  findBrandWithdrawRebate(@Param("pridate") Date pridate ,@Param("curdate") Date curdate);
	
	@Query(value = "select paymentOrder from PaymentOrder paymentOrder where paymentOrder.type  in ('0',11)  and paymentOrder.channelTag != 'JIEFUBAO' and paymentOrder.autoClearing='1'  and paymentOrder.status = '1' and paymentOrder.createTime >= :pridate and paymentOrder.createTime < :curdate")
	List<PaymentOrder>  findBrandWithdrawclearRebate(@Param("pridate") Date pridate ,@Param("curdate") Date curdate);
	
	
	@Query(value = "select paymentOrder from PaymentOrder paymentOrder where (paymentOrder.type  = '2' and paymentOrder.channelTag != 'JIEFUBAO')  and paymentOrder.status = '1' and paymentOrder.createTime >= :pridate and paymentOrder.createTime < :curdate and paymentOrder.brandid=:brandid")
	List<PaymentOrder>  findBrandWithdrawRebate(@Param("pridate") Date pridate ,@Param("curdate") Date curdate,@Param("brandid") long brandid);
	
	@Query(value = "select paymentOrder from PaymentOrder paymentOrder where paymentOrder.type  in ('0',11)  and paymentOrder.channelTag != 'JIEFUBAO' and paymentOrder.autoClearing='1'  and paymentOrder.status = '1' and paymentOrder.createTime >= :pridate and paymentOrder.createTime < :curdate and paymentOrder.brandid=:brandid")
	List<PaymentOrder>  findBrandWithdrawclearRebate(@Param("pridate") Date pridate ,@Param("curdate") Date curdate,@Param("brandid") long brandid);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid=:userid")
	Page<PaymentOrder> findAllPaymentOrder(@Param("userid") long userid, Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid=:userid  and paymentOrder.brandid=:brandid")
	Page<PaymentOrder> findAllPaymentOrderbrandid(@Param("userid") long userid, @Param("brandid") long brandid, Pageable pageAble);
	
	@Query("select sum(paymentOrder.realAmount) from  PaymentOrder paymentOrder where paymentOrder.type in (:type) and paymentOrder.status in (:status)  and paymentOrder.userid=:userid and paymentOrder.autoClearing=:autoClearing")
	BigDecimal findsumPaymentOrder(@Param("userid") long userid ,@Param("type") String[] type,@Param("status") String[] status , @Param("autoClearing") String autoClearing );
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.type in (:type) and paymentOrder.status in (:status)  and paymentOrder.userid=:userid and paymentOrder.descCode=:desccode and paymentOrder.createTime>=:startTimeDate and paymentOrder.createTime < :endTimeDate")
	List<PaymentOrder> findsumPaymentOrderByDescCode(@Param("userid") long userid ,@Param("type") String[] type,@Param("status") String[] status , @Param("desccode") String desccode  ,@Param("startTimeDate") Date startTimeDate,@Param("endTimeDate") Date endTimeDate );
	
	
	@Query("select sum(paymentOrder.realAmount) from  PaymentOrder paymentOrder where paymentOrder.type in (:type) and paymentOrder.status in (:status)  and paymentOrder.userid=:userid and paymentOrder.autoClearing=:autoClearing and paymentOrder.createTime>=:startTimeDate and paymentOrder.createTime < :endTimeDate")
	BigDecimal findsumPaymentOrder(@Param("userid") long userid ,@Param("type") String[] type,@Param("status") String[] status , @Param("autoClearing") String autoClearing ,@Param("startTimeDate") Date startTimeDate,@Param("endTimeDate") Date endTimeDate);
	
	@Query("select sum(paymentOrder.amount) from  PaymentOrder paymentOrder where paymentOrder.type in (:type) and paymentOrder.status in (:status)  and paymentOrder.userid=:userid and paymentOrder.autoClearing=:autoClearing and paymentOrder.createTime>=:startTimeDate and paymentOrder.createTime < :endTimeDate")
	BigDecimal findsumPaymentOrderAmount(@Param("userid") long userid ,@Param("type") String[] type,@Param("status") String[] status , @Param("autoClearing") String autoClearing ,@Param("startTimeDate") Date startTimeDate,@Param("endTimeDate") Date endTimeDate);
	
	@Query("select count(*)  from  PaymentOrder paymentOrder where paymentOrder.type in (:type) and paymentOrder.status in (:status)  and paymentOrder.userid=:userid and paymentOrder.autoClearing=:autoClearing and paymentOrder.createTime>=:startTimeDate and paymentOrder.createTime < :endTimeDate")
	int findsumPaymentOrdercount(@Param("userid") long userid ,@Param("type") String[] type,@Param("status") String[] status , @Param("autoClearing") String autoClearing ,@Param("startTimeDate") Date startTimeDate,@Param("endTimeDate") Date endTimeDate);
	
	@Query("select sum(paymentOrder.realAmount) from  PaymentOrder paymentOrder where paymentOrder.type in (:type) and paymentOrder.status in (:status)  and paymentOrder.brandid=:brand and paymentOrder.autoClearing in (:autoClearing) and paymentOrder.createTime>=:startTimeDate and paymentOrder.createTime < :endTimeDate")
	BigDecimal findsumPaymentOrderBrand(@Param("brand") long brand ,@Param("type") String[] type,@Param("status") String[] status , @Param("autoClearing") String[] autoClearing ,@Param("startTimeDate") Date startTimeDate,@Param("endTimeDate") Date endTimeDate);
	
	@Query("select count(*)  from  PaymentOrder paymentOrder where paymentOrder.type in (:type) and paymentOrder.status in (:status)  and paymentOrder.brandid=:brand and paymentOrder.autoClearing in (:autoClearing) and paymentOrder.createTime>=:startTimeDate and paymentOrder.createTime < :endTimeDate")
	int findsumPaymentOrderBrandcount(@Param("brand") long brand  ,@Param("type") String[] type,@Param("status") String[] status , @Param("autoClearing") String[] autoClearing ,@Param("startTimeDate") Date startTimeDate,@Param("endTimeDate") Date endTimeDate);
	
	@Query("select sum(paymentOrder.realAmount) from  PaymentOrder paymentOrder where paymentOrder.type in (:type) and paymentOrder.status in (:status)  and  paymentOrder.autoClearing in (:autoClearing) and paymentOrder.createTime>=:startTimeDate and paymentOrder.createTime < :endTimeDate")
	BigDecimal findsumPaymentOrderPlatform(@Param("type") String[] type,@Param("status") String[] status , @Param("autoClearing") String[] autoClearing ,@Param("startTimeDate") Date startTimeDate,@Param("endTimeDate") Date endTimeDate);
	
	@Query("select count(*)  from  PaymentOrder paymentOrder where paymentOrder.type in (:type) and paymentOrder.status in (:status)  and  paymentOrder.autoClearing in (:autoClearing) and paymentOrder.createTime>=:startTimeDate and paymentOrder.createTime < :endTimeDate")
	int findsumPaymentOrderPlatformcount(@Param("type") String[] type,@Param("status") String[] status , @Param("autoClearing") String[] autoClearing ,@Param("startTimeDate") Date startTimeDate,@Param("endTimeDate") Date endTimeDate);
	
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid in (:userid)")
	Page<PaymentOrder> findAllPaymentOrder(@Param("userid") Long[] userid, Pageable pageAble);
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.brandid=:brandid")
	Page<PaymentOrder> findAllPaymentOrderbrandid(@Param("brandid") long  brandid, Pageable pageAble);
	
	@Query("select paymentOrder.userid from  PaymentOrder paymentOrder where paymentOrder.brandid =:brandid")
	Long[] findAllPaymentOrderbyBrandid(@Param("brandid") Long brandid);
	
	@Query("select paymentOrder.ordercode from  PaymentOrder paymentOrder where paymentOrder.brandid =:brandid and paymentOrder.type in (:type)  and paymentOrder.status in (:status)  ")
	String[] findAllPaymentOrderordercodesbyBrandid(@Param("brandid") Long brandid,@Param("type") String[] type,@Param("status") String[] status );
	
	@Query("select paymentOrder from PaymentOrder paymentOrder where paymentOrder.channelTag = :channelTag")
	List<PaymentOrder> findPaymentOrderByChannelTag(@Param("channelTag") String channelTag);

	
	//=========================================
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid=:userid and paymentOrder.brandid=:brandid and paymentOrder.ordercode=:ordercode and paymentOrder.status in (:status) and paymentOrder.thirdOrdercode=:thirdOrdercode and paymentOrder.channelTag in (:channelTag)")
	Page<PaymentOrder> findPaymentOrderbrandid1(@Param("brandid") long brandid, @Param("userid") long  userid, @Param("ordercode") String ordercode,@Param("thirdOrdercode") String thirdOrdercode, @Param("status") String[] status, @Param("channelTag") String[] channelTag, Pageable pageAble);

	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.brandid=:brandid and paymentOrder.ordercode=:ordercode and paymentOrder.status in (:status) and paymentOrder.thirdOrdercode=:thirdOrdercode and paymentOrder.channelTag in (:channelTag)")
	Page<PaymentOrder> findPaymentOrderbrandid2(@Param("brandid") long  brandid,@Param("ordercode") String ordercode,@Param("thirdOrdercode") String thirdOrdercode, @Param("status") String[] status, @Param("channelTag") String[] channelTag, Pageable pageAble);

	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid=:userid and paymentOrder.brandid=:brandid and paymentOrder.status in (:status) and paymentOrder.thirdOrdercode=:thirdOrdercode and paymentOrder.channelTag in (:channelTag)")
	Page<PaymentOrder> findPaymentOrderbrandid3(@Param("brandid") long brandid, @Param("userid") long  userid, @Param("thirdOrdercode") String thirdOrdercode, @Param("status") String[] status, @Param("channelTag") String[] channelTag, Pageable pageAble);

	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid=:userid and paymentOrder.brandid=:brandid and paymentOrder.ordercode=:ordercode and paymentOrder.status in (:status) and paymentOrder.channelTag in (:channelTag)")
	Page<PaymentOrder> findPaymentOrderbrandid4(@Param("brandid") long brandid, @Param("userid") long  userid, @Param("ordercode") String ordercode, @Param("status") String[] status, @Param("channelTag") String[] channelTag, Pageable pageAble);

	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid=:userid and paymentOrder.ordercode=:ordercode and paymentOrder.status in (:status) and paymentOrder.thirdOrdercode=:thirdOrdercode and paymentOrder.channelTag in (:channelTag)")
	Page<PaymentOrder> findPaymentOrderuserid5(@Param("userid") long  userid, @Param("ordercode") String ordercode,@Param("thirdOrdercode") String thirdOrdercode, @Param("status") String[] status, @Param("channelTag") String[] channelTag, Pageable pageAble);

	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid=:userid and paymentOrder.brandid=:brandid and paymentOrder.status in (:status) and paymentOrder.channelTag in (:channelTag)")
	Page<PaymentOrder> findPaymentOrderuserid6(@Param("brandid") long brandid, @Param("userid") long  userid, @Param("status") String[] status, @Param("channelTag") String[] channelTag, Pageable pageAble);

	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.brandid=:brandid and paymentOrder.ordercode=:ordercode and paymentOrder.status in (:status) and paymentOrder.channelTag in (:channelTag)")
	Page<PaymentOrder> findPaymentOrderbrandid7(@Param("brandid") long brandid, @Param("ordercode") String ordercode, @Param("status") String[] status, @Param("channelTag") String[] channelTag, Pageable pageAble);

	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.brandid=:brandid and paymentOrder.status in (:status) and paymentOrder.thirdOrdercode=:thirdOrdercode and paymentOrder.channelTag in (:channelTag)")
	Page<PaymentOrder> findPaymentOrderbrandid8(@Param("brandid") long brandid, @Param("thirdOrdercode") String thirdOrdercode, @Param("status") String[] status, @Param("channelTag") String[] channelTag, Pageable pageAble);

	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid=:userid and paymentOrder.ordercode=:ordercode and paymentOrder.status in (:status) and paymentOrder.channelTag in (:channelTag)")
	Page<PaymentOrder> findPaymentOrderbrandid9(@Param("userid") long  userid, @Param("ordercode") String ordercode, @Param("status") String[] status, @Param("channelTag") String[] channelTag, Pageable pageAble);

	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid=:userid and paymentOrder.status in (:status) and paymentOrder.thirdOrdercode=:thirdOrdercode and paymentOrder.channelTag in (:channelTag)")
	Page<PaymentOrder> findPaymentOrderbrandid10(@Param("userid") long  userid, @Param("thirdOrdercode") String thirdOrdercode, @Param("status") String[] status, @Param("channelTag") String[] channelTag, Pageable pageAble);

	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.ordercode=:ordercode and paymentOrder.status in (:status) and paymentOrder.thirdOrdercode=:thirdOrdercode and paymentOrder.channelTag in (:channelTag)")
	Page<PaymentOrder> findPaymentOrderbrandid11(@Param("ordercode") String ordercode,@Param("thirdOrdercode") String thirdOrdercode, @Param("status") String[] status, @Param("channelTag") String[] channelTag, Pageable pageAble);

	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.brandid=:brandid and paymentOrder.status in (:status) and paymentOrder.channelTag in (:channelTag)")
	Page<PaymentOrder> findPaymentOrderbrandid12(@Param("brandid") long brandid, @Param("status") String[] status, @Param("channelTag") String[] channelTag, Pageable pageAble);

	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.userid=:userid and paymentOrder.status in (:status) and paymentOrder.channelTag in (:channelTag)")
	Page<PaymentOrder> findPaymentOrderbrandid13(@Param("userid") long  userid, @Param("status") String[] status, @Param("channelTag") String[] channelTag, Pageable pageAble);

	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.ordercode=:ordercode and paymentOrder.status in (:status) and paymentOrder.channelTag in (:channelTag)")
	Page<PaymentOrder> findPaymentOrderbrandid14(@Param("ordercode") String ordercode, @Param("status") String[] status, @Param("channelTag") String[] channelTag, Pageable pageAble);

	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.status in (:status) and paymentOrder.thirdOrdercode=:thirdOrdercode and paymentOrder.channelTag in (:channelTag)")
	Page<PaymentOrder> findPaymentOrderbrandid15(@Param("thirdOrdercode") String thirdOrdercode, @Param("status") String[] status, @Param("channelTag") String[] channelTag, Pageable pageAble);

	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.status in (:status) and paymentOrder.channelTag in (:channelTag)")
	Page<PaymentOrder> findPaymentOrderbrandid16(@Param("status") String[] status, @Param("channelTag") String[] channelTag, Pageable pageAble);

	PaymentOrder findByOrdercode(String orderCode);
	
	//依据用户phone获取用户该时间段内所有order
	@Query("from  PaymentOrder paymentOrder where paymentOrder.phone=:phone and paymentOrder.brandid=:brandid and paymentOrder.createTime>=:startTimeDate and paymentOrder.createTime<=:endTimeDate")
	List<PaymentOrder> findOrderByphoneAndbrandid(@Param("phone") String phone, @Param("brandid") long brandid, @Param("startTimeDate") Date startTimeDate, @Param("endTimeDate") Date endTimeDate,Pageable pageable);
	
	//根据条件查询出userId
	@Query("select paymentOrder.userid from PaymentOrder paymentOrder where paymentOrder.status = 1 and paymentOrder.brandid =:brandId and paymentOrder.type = 0 and paymentOrder.userid not in (select autoRebateHistory.userId from AutoRebateHistory autoRebateHistory where autoRebateHistory.rebateConfigId =:autoRebateConfigId) group by paymentOrder.userid having sum(paymentOrder.amount) >=:limitAmount")
	List<Long> queryUserIdsByAmount(@Param("brandId")Long brandId,@Param("limitAmount") BigDecimal limitAmount, @Param("autoRebateConfigId")Long autoRebateConfigId);

	//调用此方法获取待结算订单
	@Query("select paymentOrder from PaymentOrder paymentOrder where paymentOrder.userid=:userid and paymentOrder.status in (:status)")
	List<PaymentOrder> findOrderByUseridAndStatus(@Param("userid")Long userid, @Param("status") String[] status);

	@Query("select paymentOrder.userid,sum(paymentOrder.realAmount) from  PaymentOrder paymentOrder where paymentOrder.type in (:type) and paymentOrder.status in (:status) and paymentOrder.autoClearing=:autoClearing and paymentOrder.userid in (:userIds) group by paymentOrder.userid")
	List<Object[]> findSumByUserIds(@Param("userIds")long[] userIds,@Param("type")String type, @Param("status")String status, @Param("autoClearing")String autoClearing);
	
	@Modifying
	@Query(value = "delete from t_payment_order where order_code=:ordercode", nativeQuery = true)
	void deletePaymentOrderByOrderCode(@Param("ordercode")String ordercode);
	
	@Modifying
	@Query("update PaymentOrder set remark=:remark where ordercode=:ordercode")
	void updateOrderCodeMsg(@Param("ordercode") String ordercode,@Param("remark") String remark);

	PaymentOrder findByThirdOrdercode(String order_code);
	
	@Query(value = "select tpo.* from  t_payment_order tpo where  tpo.create_time >=:starttime and tpo.create_time <=:endtime and tpo.channel_tag in (:channelTag) and tpo.order_status=:status and tpo.remark=:remark", nativeQuery = true)
	List<PaymentOrder> findOrderByTimeAndChannelTagAndStatus( @Param("starttime") String startTime,  @Param("endtime") String endTime, @Param("channelTag") String[] channelTag, @Param("status") String status, @Param("remark") String remark);
	
	@Query(value = "select tpo.* from  t_payment_order tpo where  tpo.create_time >=:starttime and tpo.create_time <=:endtime and tpo.channel_tag in (:channelTag) and tpo.order_status=:status and tpo.remark != ''", nativeQuery = true)
	List<PaymentOrder> findOrderByTimeAndChannelTagAndStatusAndRemark( @Param("starttime") String startTime,  @Param("endtime") String endTime, @Param("channelTag") String[] channelTag, @Param("status") String status);

	PaymentOrder findByOutMerOrdercode(String outOrderCode);
	
	@Query(value = "select tpo.order_code from  t_payment_order tpo where  tpo.create_time >=:starttime and tpo.create_time <=:endtime and tpo.channel_tag=:channelTag and tpo.order_status in (:status) and tpo.remark = ''", nativeQuery = true)
	List<String> findYBpayOrder( @Param("starttime") String startTime,  @Param("endtime") String endTime, @Param("channelTag") String channelTag, @Param("status") String[] status);

	
	@Query(value = "select sum(tpo.amount) from  t_payment_order tpo where tpo.order_status=:status  and tpo.user_id=:userid and tpo.channel_tag=:channelTag and tpo.create_time >=:starttime and tpo.create_time <=:endtime", nativeQuery = true)
	BigDecimal getEveryDayMaxLimit(@Param("userid") long userid ,@Param("channelTag") String channelTag,@Param("status") String status , @Param("starttime") String startTime,  @Param("endtime") String endTime);
	
	
	@Query("select paymentOrder from  PaymentOrder paymentOrder where paymentOrder.phone=:phone and paymentOrder.brandid=:brandid and paymentOrder.channelTag=:channelTag and paymentOrder.status=:status")
	List<PaymentOrder> getPaymentOrderByPhoneAndChannelTagAndOrderStatus(@Param("phone") String phone, @Param("brandid") long brandid, @Param("channelTag") String channelTag, @Param("status") String status);
	
	
	@Query("select sum(paymentOrder.amount) from  PaymentOrder paymentOrder where paymentOrder.userid in (:userid) and paymentOrder.status= '1' and paymentOrder.type in (:type) and paymentOrder.createTime between :startTimeDate and :endTimeDate")
	BigDecimal queryPaymentOrderSumAmountByUserIds(@Param("userid") long[] userid, @Param("type")String[] type, @Param("startTimeDate") Date startTimeDate,@Param("endTimeDate") Date endTimeDate);
	
	@Query(value = "select tpo.* from t_payment_order tpo where tpo.user_id=:userid and tpo.channel_tag=:channelTag  and tpo.order_status=:status and tpo.create_time>=:startTime and tpo.create_time <= :endTime and tpo.remark != ''", nativeQuery = true)
	List<PaymentOrder> getPaymentOrderByUserIdAndChannelTagAndStatus(@Param("userid") long userid ,@Param("channelTag") String channelTag,@Param("status") String status, @Param("startTime") String startTime,@Param("endTime") String endTime);

	@Query(value = "select tpo.order_code from t_payment_order tpo where tpo.order_status= '1' and tpo.order_type not in (:type) and tpo.channel_tag not in (:channelTag) and tpo.create_time>=:startTime and tpo.create_time <= :endTime", nativeQuery = true)
	List<String> getPaymentOrderByStartTimeAndEndTime(@Param("type")String[] type, @Param("channelTag") String[] channelTag, @Param("startTime") String startTime,@Param("endTime") String endTime);

	@Query(value = "select tpo.* from  t_payment_order tpo where tpo.update_time >=:starttime and tpo.update_time <=:endtime and tpo.channel_tag in (:channelTag) and tpo.order_status=:status and tpo.remark=:remark", nativeQuery = true)
	List<PaymentOrder> findOrderByUpdateTimeTimeAndChannelTagAndStatus( @Param("starttime") String startTime,  @Param("endtime") String endTime, @Param("channelTag") String[] channelTag, @Param("status") String status, @Param("remark") String remark);

	
}
