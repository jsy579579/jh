package com.jh.paymentgateway.repository.kft;

import com.jh.paymentgateway.pojo.kft.KFTVerifyCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KFTVerifyCardRepository extends JpaRepository<KFTVerifyCard,Long> {




}
