package com.jh.paymentchannel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.RepaymentSupportBank;
import com.jh.paymentchannel.pojo.WMYKBindCard;





@Repository
public interface RepaymentSupportBankRepository extends JpaRepository<RepaymentSupportBank, String>, JpaSpecificationExecutor<RepaymentSupportBank>{
	
	@Query("select rsb from RepaymentSupportBank rsb where rsb.version=:version")
	public List<RepaymentSupportBank> getRepaymentSupportBankByVersion(@Param("version") String version);
	
	
}
