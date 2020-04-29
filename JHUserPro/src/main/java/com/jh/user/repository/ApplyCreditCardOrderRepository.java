package com.jh.user.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.ApplyCreditCardOrder;

@Repository
public interface ApplyCreditCardOrderRepository extends JpaRepository<ApplyCreditCardOrder, Long>,JpaSpecificationExecutor<ApplyCreditCardOrder> {

	ApplyCreditCardOrder findByBankNameAndIdcardAndOrderStatus(String bankName,String idcard, int orderStatus);

	Page<ApplyCreditCardOrder> findByBrandIdAndOrderStatus(Long brandId, Integer orderStatus, Pageable pageable);

	ApplyCreditCardOrder findByOrderCode(String orderCode);

	Page<ApplyCreditCardOrder> findByUserId(Integer userId, Pageable pageable);

	Page<ApplyCreditCardOrder> findByUserIdAndOrderStatus(Integer userId, Integer orderStatus, Pageable pageable);

	ApplyCreditCardOrder findByBankNameAndPhoneLikeAndNameLike(String bankName, String phone, String name);

	Page<ApplyCreditCardOrder> findByOrderCode(String orderCode, Pageable pageable);

	Page<ApplyCreditCardOrder> findByBrandIdAndOrderStatusAndOrderCode(Long brandId, Integer orderStatus,String orderCode, Pageable pageable);

	Page<ApplyCreditCardOrder> findByNameLike(String name, Pageable pageable);

	Page<ApplyCreditCardOrder> findByPhoneLike(String phone, Pageable pageable);

	Page<ApplyCreditCardOrder> findByNameLikeAndPhoneLike(String name, String phone, Pageable pageable);

	Page<ApplyCreditCardOrder> findByBrandId(Long brandId, Pageable pageable);
	
	@Query(value="select applyCreditCardOrder from ApplyCreditCardOrder applyCreditCardOrder group by applyCreditCardOrder.bankName")
	List<ApplyCreditCardOrder> findGroupByBankName();
}
