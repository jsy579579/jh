package com.jh.user.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.JdpushHistory;

@Repository
public interface JdpushHistoryRepository  extends JpaRepository<JdpushHistory,Long>,JpaSpecificationExecutor<JdpushHistory>{

	
	
	@Query("select jdh from  JdpushHistory jdh where jdh.userid=:userid")
	Page<JdpushHistory> findJdpushHistoryByuserId(@Param("userid") long userid ,Pageable pageable);
	
	@Query("select jdh from  JdpushHistory jdh where jdh.brandId=:brandId")
	Page<JdpushHistory> findJdpushHistoryByBrandId(@Param("brandId") long brandId ,Pageable pageable);
	
	@Modifying
	@Query("delete  from  JdpushHistory  where brandId=:brandId and btype=:btype")
	void delJdpushHistoryByBrandId(@Param("brandId") long brandId,@Param("btype") String btype);
	
	@Query("select jdh.id from  JdpushHistory jdh where jdh.userid=:userid and jdh.brandId=:brandid order by jdh.createTime desc")
	long[] findJdpushHistorytop30(@Param("userid") long userid , @Param("brandid") long brandid );
	
	/*@Modifying
	@Query("delete from  JdpushHistory jdh where jdh.id NOT IN (:ids) and  jdh.userid=:userid and jdh.brandId=:brandid  ")
	void delJdpushHistory(@Param("ids") long[] id,@Param("userid") long userid, @Param("brandid") long brandid );*/
	
	//通过id删除推送
	@Modifying
	@Query("delete from JdpushHistory jdh where jdh.id=:id")
	void delJdpushHistoryById(@Param("id") long id);
	
	//通过userid删除推送
	@Modifying
	@Query("delete from JdpushHistory jdh where jdh.userid=:userid")
	void delJdpushHistoryByUserid(@Param("userid") long userid);
}
