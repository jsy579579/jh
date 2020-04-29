package com.juhe.creditcardapplyfor.business;

import com.juhe.creditcardapplyfor.entity.LoanOrderEntity;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LoanOrderBusiness {

    LoanOrderEntity saveOrder(LoanOrderEntity loanOrderEntity);

    LoanOrderEntity updateOrder(LoanOrderEntity loanOrderEntity);

    LoanOrderEntity getOrderByClientNo(String clientNo);

    List<LoanOrderEntity> listOrderByPhoneAndOrderCode(String phone, String orderCode, Pageable pageable);

    List<LoanOrderEntity> listOrderByPhone(String phone, Pageable pageable);

    List<LoanOrderEntity> listOrderByOrderCode(String orderCode, Pageable pageable);
}
