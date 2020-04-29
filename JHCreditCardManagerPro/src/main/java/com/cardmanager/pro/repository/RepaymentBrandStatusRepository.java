package com.cardmanager.pro.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.cardmanager.pro.pojo.RepaymentBrandStatus;

@Repository
public interface RepaymentBrandStatusRepository  extends JpaRepository<RepaymentBrandStatus,String>,JpaSpecificationExecutor<RepaymentBrandStatus>{

	RepaymentBrandStatus getRepaymentBrandStatusByBrandIdAndVersion(int brandId, String version);

}
