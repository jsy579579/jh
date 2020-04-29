package cn.jh.clearing.repository;

import cn.jh.clearing.pojo.ProfitOnoff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfitOnoffRepository extends JpaRepository<ProfitOnoff,Integer>, JpaSpecificationExecutor<ProfitOnoff> {

    @Query("from ProfitOnoff p where p.brandId = :brandId")
    ProfitOnoff findByBrandId(@Param("brandId") String brandId);
}
