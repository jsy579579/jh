package com.cardmanager.pro.empty.card.manager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.cardmanager.pro.business.ConsumeTaskPOJOBusiness;
import com.cardmanager.pro.business.RepaymentBillBusiness;
import com.cardmanager.pro.business.RepaymentTaskPOJOBusiness;
import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import com.cardmanager.pro.pojo.RepaymentBill;
import com.cardmanager.pro.pojo.RepaymentTaskPOJO;

import cn.jh.common.utils.DateUtil;
import cn.jh.common.utils.StringUtil;

@Service
public class EmptyCardApplyOrderBusinessImpl implements EmptyCardApplyOrderBusiness {

	@Autowired
	private EmptyCardApplyOrderRepository emptyCardApplyOrderRepository;
	
	@Autowired
	private RepaymentTaskPOJOBusiness repaymentTaskPOJOBusiness;
	
	@Autowired
	private ConsumeTaskPOJOBusiness consumeTaskPOJOBusiness;
	
	@Autowired
	private RepaymentBillBusiness repaymentBillBusiness;
	
	@Autowired
	private BrandAccountBusiness brandAccountBusiness;
	
	@Autowired
	private EntityManager em;

	@Override
	public List<EmptyCardApplyOrder> findByCreditCardNumberAndOrderStatusIn(String creditCardNumber,int[] orderStatus) {
		return emptyCardApplyOrderRepository.findByCreditCardNumberAndOrderStatusIn(creditCardNumber,orderStatus);
	}

	@Override
	@Transactional
	public EmptyCardApplyOrder save(EmptyCardApplyOrder emptyCardApplyOrder) {
		return emptyCardApplyOrderRepository.saveAndFlush(emptyCardApplyOrder);
	}

	@Override
	public Page<EmptyCardApplyOrder> getAppalyOrder(String userId, String phone, String name, String creditCardNumber, String brandId,String orderStatus, String startTime, String endTime, Pageable pageable) {
        Page<EmptyCardApplyOrder> result = emptyCardApplyOrderRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
        	List<Predicate> predicatesList = new ArrayList<>();
    		
    		if (!StringUtil.isNullString(phone)) {
            	predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(EmptyCardApplyOrder_.phone), phone)));
    		}
    		if (!StringUtil.isNullString(userId)) {
    			predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(EmptyCardApplyOrder_.userId), userId)));
    		}
    		if (!StringUtil.isNullString(brandId)) {
            	predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(EmptyCardApplyOrder_.brandId), brandId)));
    		}
    		if (!StringUtil.isNullString(creditCardNumber)) {
            	predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(EmptyCardApplyOrder_.creditCardNumber), creditCardNumber)));
    		}
    		if (!StringUtil.isNullString(orderStatus)) {
            	predicatesList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(EmptyCardApplyOrder_.orderStatus), Integer.valueOf(orderStatus))));
    		}
    		
    		if (!StringUtil.isNullString(name)) {
            	predicatesList.add(criteriaBuilder.and(criteriaBuilder.like(root.get(EmptyCardApplyOrder_.name), "%"+name+"%")));
    		}
    		 // endTime 小于等于 <= 语句
            if (!StringUtil.isNullString(endTime)) {
            	
                predicatesList.add(criteriaBuilder.and(criteriaBuilder.lessThan(root.get(EmptyCardApplyOrder_.createTime), DateUtil.getDateStringConvert(new Date(), endTime, "yyyy-MM-dd HH:mm:ss"))));
            }
            //startTime 大于等于 >= 语句
            if (!StringUtil.isNullString(startTime)) {
                predicatesList.add(criteriaBuilder.and(criteriaBuilder.greaterThan(root.get(EmptyCardApplyOrder_.createTime), DateUtil.getDateStringConvert(new Date(), startTime, "yyyy-MM-dd HH:mm:ss"))));
            }
        	
        	return criteriaBuilder.and(predicatesList.toArray(new Predicate[predicatesList.size()]));
		}, pageable);
		return result;
	}

	@Override
	public EmptyCardApplyOrder findById(Long id) {
		return emptyCardApplyOrderRepository.findOne(id);
	}

	@Override
	public EmptyCardApplyOrder findByPaychargeOrderCode(String orderCode) {
		return emptyCardApplyOrderRepository.findByPaychargeOrderCode(orderCode);
	}

	@Override
	@Transactional
	public void cancelAllTask(String userId,String creditCardNumber,String createTime,String version,BigDecimal debt) {
		List<RepaymentTaskPOJO> repaymentTaskPOJOs = repaymentTaskPOJOBusiness.findByUserIdAndCreditCardNumberAndCreateTime(userId, creditCardNumber, createTime);
		for (RepaymentTaskPOJO repaymentTaskPOJO : repaymentTaskPOJOs) {
			if (repaymentTaskPOJO.getTaskType().intValue() == 2 && repaymentTaskPOJO.getTaskStatus().intValue() == 0) {
				repaymentTaskPOJO.setTaskStatus(2);
				repaymentTaskPOJO.setReturnMessage("任务无法继续执行,自动终止任务");
			}
		}
		repaymentTaskPOJOBusiness.saveArray(repaymentTaskPOJOs.toArray(new RepaymentTaskPOJO[repaymentTaskPOJOs.size()]));
		List<ConsumeTaskPOJO> consumeTaskPOJOs = consumeTaskPOJOBusiness.findByCreateTimeAndCreditCardNumberAndUserIdAndVersion(createTime, creditCardNumber, userId, version);
		for (ConsumeTaskPOJO consumeTaskPOJO2 : consumeTaskPOJOs) {
			if (consumeTaskPOJO2.getTaskType().intValue() == 2 && consumeTaskPOJO2.getTaskStatus().intValue() == 0) {
				consumeTaskPOJO2.setTaskStatus(2);
				consumeTaskPOJO2.setReturnMessage("任务无法继续执行,自动终止任务");
			}
		}
		consumeTaskPOJOBusiness.saveArrayList(consumeTaskPOJOs);
		RepaymentBill repaymentBill = repaymentBillBusiness.findByCreditCardNumberAndCreateTime(creditCardNumber, createTime);
		repaymentBill.setTaskStatus(3);
		repaymentBillBusiness.save(repaymentBill);
		EmptyCardApplyOrder emptyCardApplyOrder = emptyCardApplyOrderRepository.findByCreditCardNumberAndCreateTime(creditCardNumber,DateUtil.getDateStringConvert(new Date(), createTime, "yyyy-MM-dd HH:mm:ss"));
		emptyCardApplyOrder.setDebtAmount(emptyCardApplyOrder.getDebtAmount().add(debt));
		emptyCardApplyOrder.setOrderStatus(6);
		this.save(emptyCardApplyOrder);
		if (debt.compareTo(BigDecimal.ZERO) == 0) {
			BrandAccount brandAccount = brandAccountBusiness.findByBrandId(emptyCardApplyOrder.getBrandId());
			brandAccountBusiness.updateAccount(brandAccount, 2, emptyCardApplyOrder.getReservedAmount(),emptyCardApplyOrder);
		}
	}

	@Override
	public EmptyCardApplyOrder findByCreditCardNumberAndCreateTime(String creditCardNumber, Date createTime) {
		return emptyCardApplyOrderRepository.findByCreditCardNumberAndCreateTime(creditCardNumber, createTime);
	}
}
