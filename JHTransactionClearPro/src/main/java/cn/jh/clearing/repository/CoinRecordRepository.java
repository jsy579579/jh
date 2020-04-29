package cn.jh.clearing.repository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.jh.clearing.pojo.CoinRecord;

@Repository
public interface CoinRecordRepository extends JpaRepository<CoinRecord,String>,JpaSpecificationExecutor<CoinRecord>{

	@Query("select coinRecord from  CoinRecord coinRecord where coinRecord.userid=:userid")
	Page<CoinRecord> findCoinRecordByUserid(@Param("userid") long userid,Pageable pageAble);
	
	
	@Query("select coinRecord from  CoinRecord coinRecord where coinRecord.userid=:userid and coinRecord.createTime >=:startTime and coinRecord.createTime <:endTime")
	Page<CoinRecord> findAllCoinRecord(@Param("userid") long userid, @Param("startTime") Date startTime, @Param("endTime") Date endTime, Pageable pageAble);
	
	@Query("select coinRecord from  CoinRecord coinRecord where coinRecord.userid=:userid and coinRecord.createTime >=:startTime")
	Page<CoinRecord> findAllCoinRecord(@Param("userid") long userid, @Param("startTime") Date startTime, Pageable pageAble);
	
	@Query("select coinRecord from  CoinRecord coinRecord where coinRecord.userid in (:uids)")
	Page<CoinRecord> findAllCoinRecord(@Param("uids") Long[] uids,Pageable pageAble);
}
