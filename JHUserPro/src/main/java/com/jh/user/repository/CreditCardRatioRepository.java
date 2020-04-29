package com.jh.user.repository;

import com.jh.user.pojo.CreditCardRatio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditCardRatioRepository  extends JpaRepository<CreditCardRatio, Integer>, JpaSpecificationExecutor<CreditCardRatio> {

    @Query("select c from CreditCardRatio c where c.brandId=:brandId")
    List<CreditCardRatio> queryCreditCardRatioByBrandId(@Param("brandId") String brandId);

}
