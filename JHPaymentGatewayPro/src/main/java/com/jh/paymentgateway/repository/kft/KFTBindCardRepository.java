package com.jh.paymentgateway.repository.kft;


import com.jh.paymentgateway.pojo.kft.KFTBindCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface KFTBindCardRepository extends JpaRepository<KFTBindCard,Long> {

    @Query("select kftbc from KFTBindCard kftbc where kftbc.bankCard=:bankCard")
    KFTBindCard getKftBindCardByBankCard(@Param("bankCard") String bankCard);


}
