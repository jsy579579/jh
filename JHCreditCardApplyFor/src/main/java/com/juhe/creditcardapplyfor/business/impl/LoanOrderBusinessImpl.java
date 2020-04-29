package com.juhe.creditcardapplyfor.business.impl;


import com.juhe.creditcardapplyfor.business.LoanOrderBusiness;
import com.juhe.creditcardapplyfor.entity.LoanOrderEntity;
import com.juhe.creditcardapplyfor.repository.LoanOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author huhao
 * @title: LoanOrderServiceImpl
 * @projectName juhe
 * @description: TODO
 * @date 2019/7/23 002311:05
 */

@Transactional
@Service
public class LoanOrderBusinessImpl implements LoanOrderBusiness {


    @Autowired
    private LoanOrderRepository loanOrderRepository;

    @Override
    public LoanOrderEntity saveOrder(LoanOrderEntity loanOrderEntity) {
        return loanOrderRepository.save(loanOrderEntity);
    }

    @Override
    public LoanOrderEntity updateOrder(LoanOrderEntity loanOrderEntity) {
        return loanOrderRepository.save(loanOrderEntity);
    }

    @Override
    public LoanOrderEntity getOrderByClientNo(String clientNo) {
        return loanOrderRepository.findByClientNo(clientNo);
    }

    @Override
    public List<LoanOrderEntity> listOrderByPhoneAndOrderCode(String phone, String orderCode, Pageable pageable) {
        return loanOrderRepository.findByPhoneAndOrderCode(phone,orderCode,pageable);
    }

    @Override
    public List<LoanOrderEntity> listOrderByPhone(String phone, Pageable pageable) {
        return loanOrderRepository.findByPhone(phone,pageable);
    }

    @Override
    public List<LoanOrderEntity> listOrderByOrderCode(String orderCode, Pageable pageable) {
        return loanOrderRepository.findByOrderCode(orderCode,pageable);
    }
}
