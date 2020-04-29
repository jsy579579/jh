package com.jh.paymentgateway.repository.ypl;

import com.jh.paymentgateway.pojo.ypl.YPLMCC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface YPLMCCRepository extends JpaRepository<YPLMCC, String>, JpaSpecificationExecutor<YPLMCC> {

    @Query("select ypl from YPLMCC ypl where  ypl.parent=?1")
    List<YPLMCC> getYPLMCCByParent(@Param("parent") String parent);
}
