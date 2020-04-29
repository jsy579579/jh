package com.cardmanager.pro.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cardmanager.pro.pojo.CreditCardAccountHistory;
@Repository
public interface CreditCardAccountHistoryRepository extends JpaRepository<CreditCardAccountHistory, Long>,JpaSpecificationExecutor<CreditCardAccountHistory> {

	List<CreditCardAccountHistory> findByTaskId(String taskId);
	@Query("select creditCardAccountHistory from CreditCardAccountHistory creditCardAccountHistory where creditCardAccountHistory.taskId=:taskId and creditCardAccountHistory.addOrSub=:addOrSub")
	CreditCardAccountHistory findByTaskIdAndAddOrSub(@Param("taskId")String taskId, @Param("addOrSub")int addOrSub);
	
	@Query("select creditCardAccountHistory from CreditCardAccountHistory creditCardAccountHistory where creditCardAccountHistory.creditCardAccountId=:creditCardAccountId and creditCardAccountHistory.addOrSub=:addOrSub order by creditCardAccountHistory.createTime desc")
	List<CreditCardAccountHistory> findByCreditCardAccountIdAndAddOrSubOrderByCreateTimeDesc(@Param("creditCardAccountId")Long creditCardAccountId,@Param("addOrSub")int addOrSub);

}
