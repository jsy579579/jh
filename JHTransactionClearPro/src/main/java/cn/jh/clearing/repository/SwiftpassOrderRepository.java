package cn.jh.clearing.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.jh.clearing.pojo.SwiftpassOrder;
@Repository
public interface SwiftpassOrderRepository extends JpaRepository<SwiftpassOrder, String>,JpaSpecificationExecutor<SwiftpassOrder>{
	@Query("select count(*) from SwiftpassOrder s where s.createTime between :startTime and :endTime")
	int findCountByCreateTime(@Param("startTime")Date startTime,@Param("endTime")Date endTime);
	
	List<SwiftpassOrder> findByOrderCode(String orderCode);
	@Query("select s from SwiftpassOrder s where s.createTime between :startTime and :endTime")
	List<SwiftpassOrder> findByCreateTime(@Param("startTime")Date startTime, @Param("endTime")Date endTime);
	@Modifying
	void deleteByOrderCode(String orderCode);
}
