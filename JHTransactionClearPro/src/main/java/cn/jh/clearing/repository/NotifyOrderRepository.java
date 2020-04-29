package cn.jh.clearing.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import cn.jh.clearing.pojo.NotifyOrder;

@Repository
public interface NotifyOrderRepository extends JpaRepository<NotifyOrder, Long>, JpaSpecificationExecutor<NotifyOrder> {

	List<NotifyOrder> findByNotifyTimeLessThan(Date date);

}
