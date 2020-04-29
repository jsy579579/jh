package cn.jh.clearing.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.jh.clearing.pojo.DistributionRecord;
import cn.jh.clearing.pojo.ProfitRecord;

@Repository
public interface DistributionRecordRepository extends JpaRepository<DistributionRecord,String>,JpaSpecificationExecutor<DistributionRecord>{
	
	/**通过userID的时间段获取***/
	@Query("select distributionRecord from  DistributionRecord distributionRecord where distributionRecord.acquserid=:userid and  distributionRecord.createTime >=:startTime and distributionRecord.createTime <:endTime  order by distributionRecord.createTime DESC")
	Page<DistributionRecord> findDistributionRecordByUserId(@Param("userid") long userid,  @Param("startTime") Date startTime, @Param("endTime") Date endTime, Pageable pageAble);
	
	/**通过时间段获取***/
	@Query("select distributionRecord from  DistributionRecord distributionRecord where distributionRecord.createTime >=:startTime and distributionRecord.createTime <:endTime  and distributionRecord.ordercode in (:ordercode)  order by distributionRecord.createTime DESC")
	Page<DistributionRecord> findDistributionRecordBystartend( @Param("ordercode") String[] ordercode, @Param("startTime") Date startTime, @Param("endTime") Date endTime, Pageable pageAble);
	
	@Query("select distributionRecord from  DistributionRecord distributionRecord where distributionRecord.createTime >=:startTime and distributionRecord.createTime <:endTime  and distributionRecord.ordercode =:ordercode  order by distributionRecord.createTime DESC")
	Page<DistributionRecord> findDistributionRecordByordercode( @Param("ordercode") String ordercode, @Param("startTime") Date startTime, @Param("endTime") Date endTime, Pageable pageAble);
	/**通过phone获取该时间段内的所有返佣信息***/
	@Query("from  DistributionRecord distributionRecord where distributionRecord.acqphone=:acqphone and  distributionRecord.createTime>=:startTimeDate and distributionRecord.createTime<=:endTimeDate  order by distributionRecord.createTime DESC")
	Page<DistributionRecord> findAllDistributionByPhone(@Param("acqphone") String acqphone,  @Param("startTimeDate") Date startTimeDate, @Param("endTimeDate") Date endTimeDate, Pageable pageable);
	/**通过phone获取该时间段内的所有返佣信息***/
	@Query("from  DistributionRecord distributionRecord where distributionRecord.oriphone=:oriphone and distributionRecord.ordercode in (:ordercode) and  distributionRecord.createTime>=:startTimeDate and distributionRecord.createTime<=:endTimeDate  order by distributionRecord.createTime DESC")
	Page<DistributionRecord> findAllDistributionByoriPhone(@Param("oriphone") String oriphone, @Param("ordercode") String[] ordercode, @Param("startTimeDate") Date startTimeDate, @Param("endTimeDate") Date endTimeDate, Pageable pageable);
	@Query("from  DistributionRecord distributionRecord where distributionRecord.oriphone=:oriphone and distributionRecord.ordercode =:order and  distributionRecord.createTime>=:startTimeDate and distributionRecord.createTime<=:endTimeDate  order by distributionRecord.createTime DESC")
	Page<DistributionRecord> findAllDistributionByPhoneAndOrder(@Param("oriphone") String oriphone, @Param("order") String order, @Param("startTimeDate") Date startTimeDate, @Param("endTimeDate") Date endTimeDate, Pageable pageable);
	@Query("from  DistributionRecord distributionRecord where distributionRecord.acqphone=:acqphone and distributionRecord.ordercode =:order and  distributionRecord.createTime>=:startTimeDate and distributionRecord.createTime<=:endTimeDate  order by distributionRecord.createTime DESC")
	Page<DistributionRecord> findAllDistributionByPhoneAndacqOrder(@Param("acqphone") String acqphone, @Param("order") String order, @Param("startTimeDate") Date startTimeDate, @Param("endTimeDate") Date endTimeDate, Pageable pageable);
	@Query("from  DistributionRecord distributionRecord where distributionRecord.acqphone=:acqphone and distributionRecord.oriphone =:oriphone and  distributionRecord.createTime>=:startTimeDate and distributionRecord.createTime<=:endTimeDate  order by distributionRecord.createTime DESC")
	Page<DistributionRecord> findAllDistributionByPhoneAndoriOrder(@Param("acqphone") String acqphone, @Param("oriphone") String oriphone, @Param("startTimeDate") Date startTimeDate, @Param("endTimeDate") Date endTimeDate, Pageable pageable);
	@Query("from  DistributionRecord distributionRecord where distributionRecord.acqphone=:acqphone and distributionRecord.oriphone =:oriphone and distributionRecord.ordercode =:order  and  distributionRecord.createTime>=:startTimeDate and distributionRecord.createTime<=:endTimeDate  order by distributionRecord.createTime DESC")
	Page<DistributionRecord> findByAllParams(@Param("acqphone") String acqphone, @Param("oriphone") String oriphone, @Param("order") String order, @Param("startTimeDate") Date startTimeDate, @Param("endTimeDate") Date endTimeDate, Pageable pageable);
	
	/**获取人查询**/
	@Query("select distributionRecord from  DistributionRecord distributionRecord where distributionRecord.acquserid=:userid  order by distributionRecord.createTime DESC")
	Page<DistributionRecord> findDistributionRecordByUserid(@Param("userid") long userid,Pageable pageAble);
	
	/**获取人查询**/
	@Query("select distributionRecord from  DistributionRecord distributionRecord   order by distributionRecord.createTime DESC")
	Page<DistributionRecord> findAllDistributionRecord(Pageable pageAble);
	
	/**userID 的指定时间至今*/
	@Query("select distributionRecord from  DistributionRecord distributionRecord where distributionRecord.acquserid=:userid and  distributionRecord.createTime >=:startTime  order by distributionRecord.createTime DESC")
	Page<DistributionRecord> findDistributionRecordByUserIdstart(@Param("userid") long userid,  @Param("startTime") Date startTime, Pageable pageAble);
	
	/**userID 的指定时间之前的数据*/
	@Query("select distributionRecord from  DistributionRecord distributionRecord where distributionRecord.acquserid=:userid and  distributionRecord.createTime <:endTime  order by distributionRecord.createTime DESC")
	Page<DistributionRecord> findDistributionRecordByUserIdend(@Param("userid") long userid,  @Param("endTime") Date endTime, Pageable pageAble);
	
	
	/***--------------------贴牌/平台查询--------------------**/
	/**获取人查询和订单号**/
	@Query("select distributionRecord from  DistributionRecord distributionRecord where distributionRecord.acquserid=:userid and distributionRecord.ordercode=:ordercode  order by distributionRecord.createTime DESC")
	Page<DistributionRecord> findDistributionRecordByUseridOrdercode(@Param("userid") long userid,@Param("ordercode") String ordercode,Pageable pageAble);

	/**订单号**/
	@Query("select distributionRecord from  DistributionRecord distributionRecord where distributionRecord.ordercode=:ordercode  order by distributionRecord.createTime DESC")
	Page<DistributionRecord> findDistributionRecordByOrdercode(@Param("ordercode") String ordercode,Pageable pageAble);

	/**通过时间段获取***/
	@Query("select distributionRecord from  DistributionRecord distributionRecord where distributionRecord.createTime >=:startTime and distributionRecord.createTime <:endTime  order by distributionRecord.createTime DESC")
	Page<DistributionRecord> findDistributionRecordBystartend(  @Param("startTime") Date startTime, @Param("endTime") Date endTime, Pageable pageAble);
	
	
	/**获取人查询 订单号 结束时间**/
	@Query("select distributionRecord from  DistributionRecord distributionRecord where distributionRecord.acquserid=:userid and distributionRecord.ordercode=:ordercode and  distributionRecord.createTime <:endTime")
	Page<DistributionRecord> findDistributionRecordByUseridOrdercodeend(@Param("userid") long userid,@Param("ordercode") String ordercode, @Param("endTime") Date endTime,Pageable pageAble);

	/**获取人查询 订单号 开始时间**/
	@Query("select distributionRecord from  DistributionRecord distributionRecord where distributionRecord.acquserid=:userid and distributionRecord.ordercode=:ordercode and  distributionRecord.createTime >=:startTime")
	Page<DistributionRecord> findDistributionRecordByUseridOrdercodestart(@Param("userid") long userid,@Param("ordercode") String ordercode, @Param("startTime") Date startTime,Pageable pageAble);

	/**获取人查询 订单号 结束 开始**/
	@Query("select distributionRecord from  DistributionRecord distributionRecord where distributionRecord.acquserid=:userid and distributionRecord.ordercode=:ordercode and  distributionRecord.createTime >=:startTime and  distributionRecord.createTime <:endTime")
	Page<DistributionRecord> findDistributionRecordByUseridOrdercodestartend(@Param("userid") long userid,@Param("ordercode") String ordercode, @Param("startTime") Date startTime,@Param("endTime") Date endTime,Pageable pageAble);
	
	/******************贴牌************************/
	
	/**获取人查询**/
	@Query("select distributionRecord from  DistributionRecord distributionRecord where distributionRecord.ordercode in (:ordercode)")
	Page<DistributionRecord> findAllDistributionRecord(@Param("ordercode") String[] ordercode,Pageable pageAble);
	
	
	/**获取人查询**/
	@Query("select distributionRecord from  DistributionRecord distributionRecord where distributionRecord.acquserid=:userid and distributionRecord.ordercode in (:ordercode)")
	Page<DistributionRecord> findDistributionRecordByUserid(@Param("ordercode") String[] ordercode,@Param("userid") long userid,Pageable pageAble);
	
	/**通过userID的时间段获取***/
	@Query("select distributionRecord from  DistributionRecord distributionRecord where distributionRecord.acquserid=:userid and  distributionRecord.createTime >=:startTime and distributionRecord.createTime <:endTime and distributionRecord.ordercode in (:ordercode)")
	Page<DistributionRecord> findDistributionRecordByUserId(@Param("ordercode") String[] ordercode,@Param("userid") long userid,  @Param("startTime") Date startTime, @Param("endTime") Date endTime, Pageable pageAble);
	
	/**userID 的指定时间至今*/
	@Query("select distributionRecord from  DistributionRecord distributionRecord where distributionRecord.acquserid=:userid and  distributionRecord.createTime >=:startTime and distributionRecord.ordercode in (:ordercode)")
	Page<DistributionRecord> findDistributionRecordByUserIdstart(@Param("ordercode") String[] ordercode,@Param("userid") long userid,  @Param("startTime") Date startTime, Pageable pageAble);
	
	/**userID 的指定时间之前的数据*/
	@Query("select distributionRecord from  DistributionRecord distributionRecord where distributionRecord.acquserid=:userid and  distributionRecord.createTime <:endTime  and distributionRecord.ordercode in (:ordercode)")
	Page<DistributionRecord> findDistributionRecordByUserIdend(@Param("ordercode") String[] ordercode,@Param("userid") long userid,  @Param("endTime") Date endTime, Pageable pageAble);
	
	@Query("select distributionRecord.createTime from  DistributionRecord distributionRecord where distributionRecord.acquserid=:userid and distributionRecord.createTime>=:startTimeDate order by distributionRecord.createTime desc")
	List<Object> getDistributionRecordByAcqUserId(@Param("userid") long userid, @Param("startTimeDate")Date startTimeDate);
	
	@Query(value = "select sum(dr.acq_amount) from  t_distribution_record dr where dr.acq_user_id=:acquserid and dr.create_time>=:startTimeDate and dr.create_time<=:endTimeDate", nativeQuery = true)
	BigDecimal getSumDistributionRecordByDate(@Param("acquserid") long acquserid, @Param("startTimeDate")String startTimeDate, @Param("endTimeDate")String endTimeDate);
	
	
}
