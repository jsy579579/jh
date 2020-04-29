package cn.jh.clearing.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.jh.clearing.pojo.ProfitRecord;
import cn.jh.clearing.pojo.ProfitRecordCopy;

@Repository
public interface ProfitRecordCopyRepository extends JpaRepository<ProfitRecordCopy,String>,JpaSpecificationExecutor<ProfitRecordCopy>{

	@Query("select profitRecord from  ProfitRecordCopy profitRecord where profitRecord.ordercode=:ordercode")
	Page<ProfitRecordCopy> getProfitRecordCopyByOrderCode(@Param("ordercode") String ordercode,Pageable pageAble);
	

}
