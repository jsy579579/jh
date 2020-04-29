package com.jh.user.business.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jh.user.business.ApplyCreditCardOrderBusiness;
import com.jh.user.pojo.ApplyCreditCardOrder;
import com.jh.user.pojo.ApplyCreditCardOrder_;
import com.jh.user.repository.ApplyCreditCardOrderRepository;

@Service
public class ApplyCreditCardOrderBusinessImpl implements ApplyCreditCardOrderBusiness {

	@Autowired
	private ApplyCreditCardOrderRepository applyCreditCardOrderRepository;

	@Override
	public ApplyCreditCardOrder findByBankNameAndIdcardAndOrderStatus(String bankName,String idcard, int orderStatus) {
		return applyCreditCardOrderRepository.findByBankNameAndIdcardAndOrderStatus(bankName,idcard, orderStatus);
	}

	@Override
	public ApplyCreditCardOrder save(ApplyCreditCardOrder applyCreditCardOrder) {
		return applyCreditCardOrderRepository.saveAndFlush(applyCreditCardOrder);
	}

	@Override
	public Page<ApplyCreditCardOrder> findByBrandIdAndOrderStatus(Long brandId, Integer orderStatus,String orderCode,Pageable pageable) {
		if (3 == orderStatus.intValue()) {
			if (orderCode == null || "".equals(orderCode.trim()) || "null".equalsIgnoreCase(orderCode.trim())) {
				return applyCreditCardOrderRepository.findByBrandId(brandId,pageable);
			}else {
				return applyCreditCardOrderRepository.findByOrderCode(orderCode,pageable);
			}
		}else{
			if (orderCode == null || "".equals(orderCode.trim()) || "null".equalsIgnoreCase(orderCode.trim())) {
				return applyCreditCardOrderRepository.findByBrandIdAndOrderStatus(brandId, orderStatus,pageable);
			}else {
				return applyCreditCardOrderRepository.findByBrandIdAndOrderStatusAndOrderCode(brandId, orderStatus,orderCode,pageable);
			}
		}
	}

	@Override
	public ApplyCreditCardOrder findByOrderCode(String orderCode) {
		return applyCreditCardOrderRepository.findByOrderCode(orderCode);
	}

	@Override
	public Page<ApplyCreditCardOrder> findByUserId(Integer userId, Integer orderStatus, Pageable pageable) {
		if(3 == orderStatus.intValue()){
			return applyCreditCardOrderRepository.findByUserId(userId,pageable);
		}else{
			return applyCreditCardOrderRepository.findByUserIdAndOrderStatus(userId,orderStatus,pageable);
		}
	}

	@Override
	public ApplyCreditCardOrder findByBankNameAndPhoneLikeAndNameLike(String bankName, String phone,
			String name) {
		phone = phone.contains("*")?phone.replace("*", "%"):phone;
		name = name.contains("*")?name.replace("*", "%"):name;
		return applyCreditCardOrderRepository.findByBankNameAndPhoneLikeAndNameLike(bankName, phone,name);
	}
	
	@Override
	public Page<ApplyCreditCardOrder> findByNameLike(String name, Pageable pageable) {
		name = name.contains("*")?name.replace("*", "%"):name;
		return applyCreditCardOrderRepository.findByNameLike(name, pageable);
	}

	@Override
	public Page<ApplyCreditCardOrder> findByPhoneLike(String phone, Pageable pageable) {
		phone = phone.contains("*")?phone.replace("*", "%"):phone;
		return applyCreditCardOrderRepository.findByPhoneLike(phone, pageable);
	}

	@Override
	public Page<ApplyCreditCardOrder> findByNameLikeAndPhoneLike(String name, String phone, Pageable pageable) {
		phone = phone.contains("*")?phone.replace("*", "%"):phone;
		name = name.contains("*")?name.replace("*", "%"):name;
		return applyCreditCardOrderRepository.findByNameLikeAndPhoneLike(name, phone, pageable);
	}
	
	@Override
	public List<ApplyCreditCardOrder> findGroupByBankName() {
		return applyCreditCardOrderRepository.findGroupByBankName();
	}
	
	@Override
	public Page<ApplyCreditCardOrder> findByConditio(String brandId, String orderStatus, String orderCode, String name,String phone, String bankName, Pageable pageable) {
		return applyCreditCardOrderRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicatesList = new ArrayList<>();
            if (isNotNull(brandId)) {
                predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(ApplyCreditCardOrder_.brandId), brandId)));
			}
            if (isNotNull(orderStatus) && !"3".equals(orderStatus)) {
                predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(ApplyCreditCardOrder_.orderStatus), Integer.valueOf(orderStatus))));
			}
            
            if (isNotNull(orderCode)) {
                predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(ApplyCreditCardOrder_.orderCode), orderCode)));
            }
            if (isNotNull(name)) {
                predicatesList.add(criteriaBuilder.and(criteriaBuilder.like(root.get(ApplyCreditCardOrder_.name), "%"+name+"%")));
            }
            if (isNotNull(phone)) {
                predicatesList.add(criteriaBuilder.and(criteriaBuilder.like(root.get(ApplyCreditCardOrder_.phone), "%"+phone+"%")));
            }
            if (isNotNull(bankName)) {
                predicatesList.add(criteriaBuilder.and(criteriaBuilder.like(root.get(ApplyCreditCardOrder_.bankName), "%"+bankName+"%")));
            }
            return criteriaBuilder.and(predicatesList.toArray(new Predicate[predicatesList.size()]));
		}, pageable);
	}
	
	private static boolean isNotNull(String str) {
		return !(str == null || "".equals(str) || "null".equalsIgnoreCase(str));
	}
}
