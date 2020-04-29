package com.jh.paymentgateway.repository;

import com.jh.paymentgateway.pojo.XSBindCard;
import com.jh.paymentgateway.pojo.YCBindCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface YCBindCardRepository extends JpaRepository<YCBindCard, Long>,JpaSpecificationExecutor<YCBindCard>{

	@Query("select ycb from YCBindCard ycb where ycb.cardNo =?1")
	YCBindCard findByCardNo(@Param("bankCard")String bankCard);
}
