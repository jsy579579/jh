package cn.jh.clearing.repository;

import cn.jh.clearing.pojo.ProfitRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Repository
public interface ProfitRecordRepository extends JpaRepository<ProfitRecord,String>,JpaSpecificationExecutor<ProfitRecord>{

	
	@Query("select profitRecord from  ProfitRecord profitRecord where profitRecord.acquserid=:userid")
	Page<ProfitRecord> findProfitByUserid(@Param("userid") long userid,Pageable pageAble);


	@Query(value = "select date_format(create_time,'%Y-%m-%d'),SUM(acq_amount) FROM t_profit_record  WHERE acq_user_id = :userId GROUP BY date_format(create_time,'%Y-%m-%d') ORDER BY create_time DESC limit :start ,:size",nativeQuery=true)
	List<Object[]> findProfitInfoByUserId(@Param("userId") long userId,@Param("start")long start, @Param("size")int size);

	@Query(value = "select COUNT(*) FROM t_profit_record  WHERE acq_user_id = :userId",nativeQuery=true)
	int countAllByUserId(@Param("userId") long userId);

	@Query("select profitRecord from  ProfitRecord profitRecord where profitRecord.acquserid in (:uids)")
	Page<ProfitRecord> findProfitByBrandId(@Param("uids") Long[] uids,Pageable pageAble);
	
	
	@Query("select profitRecord from  ProfitRecord profitRecord where profitRecord.acquserid=:userid and  profitRecord.createTime >=:startTime and profitRecord.createTime <:endTime")
	Page<ProfitRecord> findAllProfitRecord(@Param("userid") long userid,  @Param("startTime") Date startTime, @Param("endTime") Date endTime, Pageable pageAble);
	
	
	@Query("select profitRecord from  ProfitRecord profitRecord where profitRecord.acquserid=:userid and  profitRecord.createTime >=:startTime")
	Page<ProfitRecord> findAllProfitRecord(@Param("userid") long userid,  @Param("startTime") Date startTime, Pageable pageAble);
	
	
	@Query("select profitRecord from  ProfitRecord profitRecord where profitRecord.ordercode=:ordercode and  profitRecord.type =:type")
	ProfitRecord findProfitRecordByordercodeAndtype(@Param("ordercode") String ordercode,  @Param("type") String type);
	
	@Query("select sum(acqAmount)  from  ProfitRecord profitRecord where profitRecord.acquserid=:userid")
	BigDecimal findsumProfitRecord(@Param("userid") long acquserid);
	
	//获取当前phone的数据库记录(oriphone为需要查询的手机号)
	@Query("from  ProfitRecord profitRecord where profitRecord.oriphone=:oriphone and profitRecord.ordercode=:ordercode")
	List<ProfitRecord> queryProfitByOriPhone(@Param("oriphone") String oriphone, @Param("ordercode") String ordercode);
	
	//依据ordercode获取分润表中的订单信息
	@Query("from  ProfitRecord profitRecord where profitRecord.ordercode=:ordercode")
	List<ProfitRecord> queryProfitAmount(@Param("ordercode") String ordercode);
	
	@Query("from  ProfitRecord profitRecord where profitRecord.oriphone=:phone and profitRecord.ordercode in (:ordercode) and profitRecord.type in (:type) and profitRecord.createTime>=:startTimeDate and profitRecord.createTime<=:endTimeDate")
	Page<ProfitRecord> queryProfitAmountByPhone(@Param("phone") String phone,@Param("ordercode")String[] ordercode,@Param("type")String[] type,@Param("startTimeDate")Date startTimeDate, @Param("endTimeDate")Date endTimeDate, Pageable pageable);
	
	@Query("from  ProfitRecord profitRecord where profitRecord.acqphone=:phone and profitRecord.type in (:type) and profitRecord.createTime>=:startTimeDate and profitRecord.createTime<=:endTimeDate")
	Page<ProfitRecord> queryProfitAmountByGetPhone(@Param("phone") String phone,@Param("type")String[] type,@Param("startTimeDate")Date startTimeDate, @Param("endTimeDate")Date endTimeDate, Pageable pageable);
	
	@Query("select sum(acqAmount)   from  ProfitRecord profitRecord where profitRecord.acqphone=:phone and profitRecord.type in (:type) and profitRecord.createTime>=:startTimeDate and profitRecord.createTime<=:endTimeDate")
	List<Object> queryProfitAll(@Param("phone") String phone,@Param("type")String[] type,@Param("startTimeDate")Date startTimeDate, @Param("endTimeDate")Date endTimeDate, Pageable pageable);
	
	@Query("from  ProfitRecord profitRecord where profitRecord.oriphone=:phone and profitRecord.acqphone=:getphone and profitRecord.type in (:type) and profitRecord.createTime>=:startTimeDate and profitRecord.createTime<=:endTimeDate")
	Page<ProfitRecord> queryProfitAmountByDoublePhone(@Param("phone") String phone,@Param("getphone") String getphone,@Param("type")String[] type,@Param("startTimeDate")Date startTimeDate, @Param("endTimeDate")Date endTimeDate, Pageable pageable);
	@Query("from  ProfitRecord profitRecord where profitRecord.oriphone=:phone and profitRecord.acqphone=:getphone and profitRecord.ordercode=:order and profitRecord.type in (:type) and profitRecord.createTime>=:startTimeDate and profitRecord.createTime<=:endTimeDate")
	Page<ProfitRecord> queryByAllParams(@Param("phone") String phone,@Param("getphone") String getphone,@Param("order") String order,@Param("type")String[] type,@Param("startTimeDate")Date startTimeDate, @Param("endTimeDate")Date endTimeDate, Pageable pageable);
	
	@Query("from  ProfitRecord profitRecord where profitRecord.oriphone=:phone and profitRecord.ordercode=:ordercode and profitRecord.type in (:type) and profitRecord.createTime>=:startTimeDate and profitRecord.createTime<=:endTimeDate")
	Page<ProfitRecord> queryProfitAmountByOderPhone(@Param("phone") String phone,@Param("ordercode") String ordercode,@Param("type")String[] type,@Param("startTimeDate")Date startTimeDate, @Param("endTimeDate")Date endTimeDate, Pageable pageable);
	
	@Query("from  ProfitRecord profitRecord where profitRecord.acqphone=:getphone and profitRecord.ordercode=:ordercode and profitRecord.type in (:type) and profitRecord.createTime>=:startTimeDate and profitRecord.createTime<=:endTimeDate")
	Page<ProfitRecord> queryProfitAmountByOderGetPhone(@Param("getphone") String phone,@Param("ordercode") String ordercode,@Param("type")String[] type,@Param("startTimeDate")Date startTimeDate, @Param("endTimeDate")Date endTimeDate, Pageable pageable);
	
	@Query("select profitRecord from ProfitRecord profitRecord where profitRecord.ordercode=:orderCode and profitRecord.type in (:type) and profitRecord.createTime>=:startTimeDate and profitRecord.createTime<=:endTimeDate")
	Page<ProfitRecord> finByManyParams(@Param("orderCode")String orderCode,@Param("type")String[] type,@Param("startTimeDate")Date startTimeDate, @Param("endTimeDate")Date endTimeDate, Pageable pageable);
	
	@Query("select profitRecord from ProfitRecord profitRecord where profitRecord.ordercode=:orderCode and profitRecord.createTime>=:startTimeDate and profitRecord.createTime<=:endTimeDate")
	Page<ProfitRecord> finByOrderCode(@Param("orderCode")String orderCode,@Param("startTimeDate")Date startTimeDate, @Param("endTimeDate")Date endTimeDate, Pageable pageable);

	@Query("select acquserid,sum(acqAmount)  from  ProfitRecord profitRecord where profitRecord.acquserid in :acqUserIds group by profitRecord.acquserid")
	List<Object[]> findsumProfitRecordByAcqUserIds(@Param("acqUserIds")long[] acqUserIds);
	
	@Query("select profitRecord.createTime from  ProfitRecord profitRecord where profitRecord.acquserid=:acqUserId and profitRecord.createTime>=:startTimeDate order by profitRecord.createTime desc")
	List<Object> getProfitRecordByAcqUserId(@Param("acqUserId")long acqUserId, @Param("startTimeDate")Date startTimeDate);
	
	@Query(value = "select sum(pr.acq_amount) from t_profit_record pr where pr.acq_user_id=:acqUserId and pr.create_time>=:startTimeDate and pr.create_time<=:endTimeDate", nativeQuery = true)
	BigDecimal getSumProfitRecordByDate(@Param("acqUserId") long acqUserId, @Param("startTimeDate")String startTimeDate, @Param("endTimeDate")String endTimeDate);

	Page<ProfitRecord> findProfitByBrandId(String brandId, Pageable pageAble);

	@Query("select p from ProfitRecord p where p.brandId=:brandId and p.createTime>=:createTime and p.createTime<=:endTime")
	Page<ProfitRecord> findProfitByBrandIdAndTime(@Param("brandId") String brandId, @Param("createTime") Date createTime, @Param("endTime") Date endTime, Pageable pageAble);

	@Query("select p from ProfitRecord p where p.brandId=:brandId ")
	Page<ProfitRecord> findBrandProfitByBrandId(@Param("brandId") String brandId, Pageable pageAble);

	@Query("select sum(p.acqAmount) from ProfitRecord p where p.brandId=:brandId")
	Object queryProfitAllByBrandId(@Param("brandId") String brandId);

	@Query("select sum(p.acqAmount) from ProfitRecord p where p.brandId=:brandId and p.createTime>=:startDate and p.createTime<=:endDate")
	Object queryProfitAllByBrandIdAndTime(@Param("brandId") String brandId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

	@Query("select p from ProfitRecord p where p.brandId=:brandId and p.remark=:type")
    Page<ProfitRecord> findBrandProfitByBrandIdAndType(@Param("brandId") String brandId, @Param("type") String type, Pageable pageAble);

	@Query("select sum(p.acqAmount) from ProfitRecord p where p.brandId=:brandId and p.remark=:type")
	Object queryProfitAllByBrandIdAndType(@Param("brandId") String brandId, @Param("type") String type);

	@Query("select p from ProfitRecord p where p.brandId=:brandId and p.remark=:type and p.createTime>=:startDate and p.createTime<=:endDate")
	Page<ProfitRecord> findBrandProfitByBrandIdAndTypeAndTime(@Param("brandId") String brandId, @Param("type") String type, @Param("startDate") Date startDate, @Param("endDate") Date endDate, Pageable pageAble);

	@Query("select sum(p.acqAmount) from ProfitRecord p where p.brandId=:brandId and p.remark=:type and p.createTime>=:startDate and p.createTime<=:endDate")
	Object queryProfitAllByBrandIdAndTypeAndTime(@Param("brandId")String brandId, @Param("type")String type, @Param("startDate")Date startDate, @Param("endDate")Date endDate);

	@Query("select p.remark from ProfitRecord p group by p.remark")
	List queryRebateType();
}
