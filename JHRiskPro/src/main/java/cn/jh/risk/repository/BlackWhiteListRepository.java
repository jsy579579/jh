package cn.jh.risk.repository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.jh.risk.pojo.BlackWhiteList;

@Repository
public interface BlackWhiteListRepository extends JpaRepository<BlackWhiteList,String>,JpaSpecificationExecutor<BlackWhiteList>{

	@Query("select blackWhite from  BlackWhiteList blackWhite where blackWhite.userid=:userid and blackWhite.brandid=:brandid")
	BlackWhiteList findBlackWhiteList(@Param("userid") long userid, @Param("brandid") long brandid);
		
	@Query("select blackWhite from  BlackWhiteList blackWhite where blackWhite.phone=:phone and blackWhite.operationType=:operationType")
	BlackWhiteList findBlackWhiteList(@Param("phone") String phone, @Param("operationType") String operationtype);
	
	@Query("select blackWhite from  BlackWhiteList blackWhite where blackWhite.userid=:userid and blackWhite.operationType=:operationType")
	BlackWhiteList findBlackWhiteByUserId(@Param("userid") long userid, @Param("operationType") String operationtype);
	
	@Query("select blackWhite from  BlackWhiteList blackWhite where blackWhite.phone=:phone")
	Page<BlackWhiteList> findBlackWhiteList(@Param("phone") String phone, Pageable pageAble);

	@Query("select blackWhite from  BlackWhiteList blackWhite where blackWhite.phone=:phone and blackWhite.brandid=:brandid and blackWhite.createTime >= :startTime  and blackWhite.createTime < :endTime")
	Page<BlackWhiteList> findBlackWhiteList(@Param("phone") String phone,  @Param("brandid") long brandid,  @Param("startTime") Date startTime,  @Param("endTime") Date endTime, Pageable pageAble);
	
	@Query("select blackWhite from  BlackWhiteList blackWhite where blackWhite.phone=:phone and blackWhite.brandid=:brandid and blackWhite.createTime >= :startTime")
	Page<BlackWhiteList> findBlackWhiteList(@Param("phone") String phone, @Param("brandid") long brandid,  @Param("startTime") Date startTime, Pageable pageAble);

	@Query("select blackWhite from  BlackWhiteList blackWhite where blackWhite.phone=:phone and blackWhite.brandid=:brandid")
	Page<BlackWhiteList> findBlackWhiteList(@Param("phone") String phone, @Param("brandid") long brandid, Pageable pageAble);
	
	@Query("select blackWhite from  BlackWhiteList blackWhite where blackWhite.phone=:phone and blackWhite.createTime >= :startTime  and blackWhite.createTime < :endTime")
	Page<BlackWhiteList> findBlackWhiteList(@Param("phone") String phone,  @Param("startTime") Date startTime,  @Param("endTime") Date endTime, Pageable pageAble);
	
	@Query("select blackWhite from  BlackWhiteList blackWhite where blackWhite.phone=:phone and blackWhite.createTime >= :startTime  and blackWhite.createTime < :endTime")
	Page<BlackWhiteList> findBlackWhiteList(@Param("phone") String phone,  @Param("startTime") Date startTime,  Pageable pageAble);
	
	@Query("select blackWhite from  BlackWhiteList blackWhite where blackWhite.brandid=:brandid and blackWhite.createTime >= :startTime  and blackWhite.createTime < :endTime")
	Page<BlackWhiteList> findBlackWhiteList(@Param("brandid") long brandid,  @Param("startTime") Date startTime, @Param("endTime") Date endTime, Pageable pageAble);
	
	@Query("select blackWhite from  BlackWhiteList blackWhite where blackWhite.brandid=:brandid and blackWhite.createTime >= :startTime")
	Page<BlackWhiteList> findBlackWhiteList(@Param("brandid") long brandid,  @Param("startTime") Date startTime, Pageable pageAble);

	@Query("select blackWhite from  BlackWhiteList blackWhite where blackWhite.brandid=:brandid")
	Page<BlackWhiteList> findBlackWhiteList(@Param("brandid") long brandid, Pageable pageAble);

	@Modifying
	@Query("delete from BlackWhiteList where phone=:phone")
	void delBlackWhite(@Param("phone") String phone);

	@Modifying
	@Query("delete from BlackWhiteList where phone=:phone and operationType=:operationType")
	void delBlackWhite(@Param("phone") String phone, @Param("operationType") String operationType);
	
	
}
