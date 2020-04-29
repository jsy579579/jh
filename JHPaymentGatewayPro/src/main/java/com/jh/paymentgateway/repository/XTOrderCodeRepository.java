package com.jh.paymentgateway.repository;

import com.jh.paymentgateway.pojo.XTOrderCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface XTOrderCodeRepository extends JpaRepository<XTOrderCode, String>, JpaSpecificationExecutor<XTOrderCode> {
	@Query("select xtoc.orderCode from XTOrderCode xtoc where xtoc.status='0'")
	public List<String> getXTOrderCodeByStatus();

	@Query("select xt from XTOrderCode  xt where xt.orderCode = ?1")
	public XTOrderCode getxtorderbyordercode(@Param("ordercode")String ordercode);
}
