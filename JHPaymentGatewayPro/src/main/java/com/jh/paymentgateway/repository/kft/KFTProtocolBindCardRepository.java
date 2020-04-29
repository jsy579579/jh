package com.jh.paymentgateway.repository.kft;

import com.jh.paymentgateway.pojo.kft.KFTProtocolBindCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KFTProtocolBindCardRepository extends JpaRepository<KFTProtocolBindCard,Long> {

}
