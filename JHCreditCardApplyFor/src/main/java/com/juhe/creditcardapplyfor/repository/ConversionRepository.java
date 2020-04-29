package com.juhe.creditcardapplyfor.repository;


import com.juhe.creditcardapplyfor.entity.ConversionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversionRepository extends JpaRepository<ConversionEntity,Integer>, JpaSpecificationExecutor<ConversionEntity> {

    @Query(value = "select * from conversion where client_no = :clientNo", nativeQuery = true)
    ConversionEntity findByClientNo(@Param("clientNo") String clientNo);
}
