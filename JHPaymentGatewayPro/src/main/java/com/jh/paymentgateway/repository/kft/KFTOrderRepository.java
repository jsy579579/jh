package com.jh.paymentgateway.repository.kft;

import com.jh.paymentgateway.pojo.kft.KFTOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KFTOrderRepository extends JpaRepository<KFTOrder,Long> {


}
