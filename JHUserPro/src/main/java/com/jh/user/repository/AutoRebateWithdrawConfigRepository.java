package com.jh.user.repository;

import com.jh.user.pojo.AutoRebateWithdrawConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutoRebateWithdrawConfigRepository extends JpaRepository<AutoRebateWithdrawConfig,Integer>, JpaSpecificationExecutor<AutoRebateWithdrawConfig> {

    @Query("select a from AutoRebateWithdrawConfig a where a.onOff=:onOff")
    List<AutoRebateWithdrawConfig> queryConfig(@Param("onOff") Integer onOff);
}
