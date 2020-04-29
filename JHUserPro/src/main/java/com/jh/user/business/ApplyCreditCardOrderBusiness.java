package com.jh.user.business;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jh.user.pojo.ApplyCreditCardOrder;

public interface ApplyCreditCardOrderBusiness {

	ApplyCreditCardOrder findByBankNameAndIdcardAndOrderStatus(String bankName,String idcard, int orderStatus);

	ApplyCreditCardOrder save(ApplyCreditCardOrder applyCreditCardOrder);

	Page<ApplyCreditCardOrder> findByBrandIdAndOrderStatus(Long brandId, Integer orderStatus, String orderCode,Pageable pageable);

	ApplyCreditCardOrder findByOrderCode(String orderCode);

	Page<ApplyCreditCardOrder> findByUserId(Integer userId, Integer orderStatus, Pageable pageable);

	ApplyCreditCardOrder findByBankNameAndPhoneLikeAndNameLike(String bankName, String phone, String name);

	Page<ApplyCreditCardOrder> findByNameLike(String name, Pageable pageable);

	Page<ApplyCreditCardOrder> findByPhoneLike(String phone, Pageable pageable);

	Page<ApplyCreditCardOrder> findByNameLikeAndPhoneLike(String name, String phone, Pageable pageable);
	
	List<ApplyCreditCardOrder> findGroupByBankName();

	Page<ApplyCreditCardOrder> findByConditio(String brandId, String orderStatus, String orderCode, String name,String phone, String bankName, Pageable pageable);

}
