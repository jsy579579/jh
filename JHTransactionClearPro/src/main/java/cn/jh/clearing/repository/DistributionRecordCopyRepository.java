package cn.jh.clearing.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.jh.clearing.pojo.DistributionRecordCopy;
import cn.jh.clearing.pojo.ProfitRecordCopy;

@Repository
public interface DistributionRecordCopyRepository extends JpaRepository<DistributionRecordCopy,String>,JpaSpecificationExecutor<DistributionRecordCopy>{
	
	@Query("select profitRecord from  DistributionRecordCopy profitRecord where profitRecord.ordercode=:ordercode")
	Page<DistributionRecordCopy> getDistributionRecordCopyByOrderCode(@Param("ordercode") String ordercode,Pageable pageAble);
	
}
