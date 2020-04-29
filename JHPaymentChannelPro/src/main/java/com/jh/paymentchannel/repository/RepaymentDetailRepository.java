package com.jh.paymentchannel.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.RepaymentDetail;

@Repository
public interface RepaymentDetailRepository  extends JpaRepository<RepaymentDetail,String>,JpaSpecificationExecutor<RepaymentDetail>{

	@Query("select rd from RepaymentDetail rd")
	List<RepaymentDetail>  getRepaymentDetailAll();

	List<RepaymentDetail> findByVersionInOrderBySortAsc(String[] versions);
	
}
