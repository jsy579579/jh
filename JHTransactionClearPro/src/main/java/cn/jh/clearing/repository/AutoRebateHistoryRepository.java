package cn.jh.clearing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import cn.jh.clearing.pojo.AutoRebateHistory;
@Repository
public interface AutoRebateHistoryRepository extends JpaRepository<AutoRebateHistory, Long>,JpaSpecificationExecutor<AutoRebateHistory>{

}
